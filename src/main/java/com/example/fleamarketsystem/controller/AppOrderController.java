package com.example.fleamarketsystem.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.AppOrderRepository;
import com.example.fleamarketsystem.service.AppOrderService;
import com.example.fleamarketsystem.service.ItemService;
import com.example.fleamarketsystem.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@Controller
@RequestMapping("/orders")
public class AppOrderController {

    private final AppOrderRepository appOrderRepository;
	// アプリ全体の注文処理ロジックを担うサービス
	private final AppOrderService appOrderService;
	// ユーザー情報取得などを行うサービス
	private final UserService userService;
	// 商品情報を扱うサービス(必要に応じて商品情報取得などで利用想定)
	private final ItemService itemService;

	// application.yml / properties からStripeの公開鍵を読み込むフィールド
	@Value("${stripe.public.key}")
	private String stripePublicKey;

	// コンストラクタインジェクションで必要なサービスを受け取る
	public AppOrderController(AppOrderService appOrderService, UserService userService, ItemService itemService, AppOrderRepository appOrderRepository) {
		this.appOrderRepository = appOrderRepository;
		// 注文サービスをフィールドに設定
		this.appOrderService = appOrderService;
		// ユーザーサービスをフィールドに設定
		this.userService = userService;
		// 商品サービスをフィールドに設定
		this.itemService = itemService;
	}
	// 購入処理開始用のエンドポイント(決済Intentを作成し、クライアントシークレットを取得する)
	@PostMapping("/initiate-purchase") // New endpoint to initiate purchase and get cliet secret
	public String initiatePurchase(
			// 現在ログイン中のユーザー情報(UserDetails)をSpring Security から取得
			@AuthenticationPrincipal UserDetails userDetails,
			// 購入対象商品のIDをリクエストパラメータから取得する
			@RequestParam("itemId") Long itemId,
			// リダイレクト先に一度だけ渡す属性を保持するためのオブジェクト
			RedirectAttributes redirectAttributes) {
		// ログインユーザーのメールアドレスからUser エンティティを取得(見つからなければ例外)
		User buyer = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("Buyer not found"));
		try {
			// サービス層でstripeのpaymentIntentを作成し、決済開始処理を行う
			PaymentIntent paymentIntent = appOrderService.initiatePurchase(itemId, buyer);
			// フロント層でStripe Elements に渡すためのクライアントシークレットをFlash属性に詰める
			redirectAttributes.addFlashAttribute("clientSecret", paymentIntent.getClientSecret());
			// どの商品に対する決済かを保持するため、itemIdもFlash属性に詰める
			redirectAttributes.addFlashAttribute("itemId", itemId);
			// 支払い確認画面へリダイレクト(Flash属性がModelAttributeとして引き継がれる)
			return "redirect:/orders/confirm-payment";
		}catch(IllegalStateException | IllegalArgumentException | StripeException e){
			// ビジネスロジックの問題やStripe連携エラーが発生した場合はエラーメッセージを表示させる
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/items/" + itemId; // Redirect back to item detail with error
		}
	}

	// Stripe Element を用いてクライアント側で決済確認を行う画面を表示する
	@GetMapping("/confirm-payment") // Page to confirm payment with Stripe Element
	public String confirmPayment(
			// initiatePurchase からの FlashAttribute として渡された clientSecret を受け取る
			@ModelAttribute("clientSecret") String clientSecret,
			// 同じく購入対象 itemIdを ModelAttribute として受け取る
			@ModelAttribute("itemId") Long itemId,
			// 画面に値を返すためのModel
			Model model
			) {
		// 必要な情報がない場合は不正なアクセスとみなし、商品一覧へリダイレクト
		if (clientSecret == null || itemId == null) {
			return "redirect:/items"; // Redirect if no payment intent data
		}
		// テンプレートでStripe決済処理を行うためにclientSecretをModelに格納
		model.addAttribute("clientSecret", clientSecret);
		// 対象商品のIDもModelに格納
		model.addAttribute("itemId", itemId);
		// Stripeの公開鍵をフロント側に渡すためModelに格納
		model.addAttribute("stripePublicKey", stripePublicKey);
		// 決済確認用のビュー(payment_confirmation.html)を表示
		return "payment_confirmation";
	}

	// クライアント側(Stripe.js)で決済完了後に呼び出されるエンドポイント
	@GetMapping("/complete-purchase") // Endpoint called by Stripe.js after payment is confirmed on client-side
	public String completePurchase(
			// クライアント側で取得したPaymentIntentのIdをクリエパラメーターから受け取る
			@RequestParam("paymentIntentId") String paymentIntentId,
			// 結果メッセージなどをリダイレクト先へ渡すためのオブジェクト
			RedirectAttributes redirectAttributes) {
		try {
			// サービス層でPaymentIntentを元に購入処理を確定させる(注文確定・在庫更新など)
			appOrderService.completePurchase(paymentIntentId);
			// 商品が正常に完了した旨のメッセージをFlash属性に設定
			redirectAttributes.addFlashAttribute("successMessage", "商品を購入しました");
			// 購入完了後にレビュー投稿ページへ遷移させるため、直近の完了注文IDを取得
			// 実運用ではPaymentIntent のメタデータなどから注文IDを辿る設計が望ましい
			return appOrderService.getLatestCompletedOrderId()
					.map(orderId -> "redirect:/reviews/new" + orderId)
					.orElseGet(() -> {
						redirectAttributes.addFlashAttribute("errorMessage", "購入は完了しましたが、評価ページへのリダイレクトに失敗しました。");
						return "redirect:/my-page/orders";
					});

		}catch(StripeException | IllegalStateException e){
			// 決済処理やビジネスロジック中にエラーが発生した場合の処理
			redirectAttributes.addFlashAttribute("errorMessage", "決済処理中にエラーが発生しました:" + e.getMessage());
			// 汎用的に商品一覧などへ戻す(別途エラー画面を用意してもよい)
			return "redirect:/items"; // Redirect to item list or a generic error page
		}
	}
}
