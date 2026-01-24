package com.example.fleamarketsystem.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fleamarketsystem.entity.AppOrder;
import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.service.AppOrderService;
import com.example.fleamarketsystem.service.ReviewService;
import com.example.fleamarketsystem.service.UserService;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final AppOrderController appOrderController;
	// レビューに関するビジネスロジックを扱うサービス
	private final ReviewService reviewService;
	// 注文情報を取得するサービス
	private final AppOrderService appOrderService;
	// ユーザー情報を取得するサービス
	private final UserService userService;

	// コンストラクタインジェクションで必要なサービスを受け取る
	public ReviewController(ReviewService reviewService, AppOrderService appOrderService, UserService userService, AppOrderController appOrderController) {
		// 評価サービスをフィールドに設定
		this.reviewService = reviewService;
		// 注文サービスをフィールドに設定
		this.appOrderService = appOrderService;
		// ユーザーサービスをフィールドに設定
		this.userService = userService;

	}

	// 新規レビュー入力フォームを表示するためのハンドラ(GET /reviews/new/{orderId})
	@GetMapping("/new/{orderId}")
	public String showReviewForm(@PathVariable("orderId") Long orderId, Model model) {
		// orderIdから対象の注文情報を取得し、存在しなければ例外を投げる
		AppOrder order = appOrderService.getOrderById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
		// 画面で利用できるように注文情報をModelに格納
		model.addAttribute("order", order);
		// review_form.htmlのテンプレート名を返す
		return "review_form";
	}

	// レビュー送信処理を行うハンドラ(POST /reviews)
	@PostMapping
	public String submitReview(
			// ログイン中のユーザー情報をSpring Securityから取得
			@AuthenticationPrincipal UserDetails userDetails,
			// 対象となる注文IDをフォームから受け取る
			@RequestParam("orderId") Long orderId,
			// 評価点をフォームから受け取る
			@RequestParam("rating") int rating,
			// コメントをフォームから受け取る
			@RequestParam("comment") String comment,
			// リダイレクト先へ一度だけ渡すメッセージを設定するためのオブジェクト
			RedirectAttributes redirectAttributes
			) {
		// ログインユーザーのメールアドレスからUserエンティティを取得し、存在しなければ例外を投げる
		User reviewer = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

		try {
			// 注文サービスを使ってレビューを登録する(注文ID・評価者・点数・コメント)
			reviewService.submitReview(orderId, reviewer, rating, comment);
			// 正常終了時のメッセージをフラッシュ属性に設定
			redirectAttributes.addFlashAttribute("successMessage", "評価を送信しました");
		}catch(IllegalStateException | IllegalArgumentException e) {
			// ビジネスロジック上の不正や異常事態が発生した場合、エラーメッセージをフラッシュ属性に設定
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		// 購入者側の注文情報ページへリダイレクトする
		return "redirect:/my-page/orders";
	}
}
