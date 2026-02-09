package com.example.fleamarketsystem.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.service.AppOrderService;
import com.example.fleamarketsystem.service.BidService;
import com.example.fleamarketsystem.service.FavoriteService;
import com.example.fleamarketsystem.service.ItemService;
import com.example.fleamarketsystem.service.ReviewService;
import com.example.fleamarketsystem.service.UserService;

@Controller
@RequestMapping("/my-page")
public class UserController {
	// User情報関連のビジネスロジックを扱うサービス
	private final UserService userService;
	// 出品商品情報取得用サービス
	private final ItemService itemService;
	// 注文情報取得用サービス
	private final AppOrderService appOrderService;
	// お気に入り情報取得・追加・削除を扱うサービス
	private final FavoriteService favoriteService;
	// レビュー情報取得用サービス
	private final ReviewService reviewService;
	// 入札情報取得用サービス
	private final BidService bidService;

	public UserController(
			UserService userService, ItemService itemService, AppOrderService appOrderService,
			FavoriteService favoriteService, ReviewService reviewService, BidService bidService) {
		this.userService = userService;
		this.itemService = itemService;
		this.appOrderService = appOrderService;
		this.favoriteService = favoriteService;
		this.reviewService = reviewService;
		this.bidService = bidService;
	}

	// マイページ表示(GET /my-page)
	@GetMapping
	public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// ログイン中ユーザーをメールアドレスから取得、存在しない場合は例外
		User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
		// viewで利用できるようにModelにログインユーザー情報を追加
		model.addAttribute("user", currentUser);
		// my_page.htmlへ遷移
		return "my_page";
	}

	// 出品一覧(GET /my_page/seller)
	@GetMapping("/selling")
	public String mySellingItems(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// ログインユーザー取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
		// ログインユーザーが出品している商品一覧をModelに追加
		model.addAttribute("sellingItems", itemService.getItemsBySeller(currentUser));
		// seller_items.htmlへ遷移
		return "seller_items";
	}

	// 購入履歴(GET /my-page/order)
	@GetMapping("/order")
	public String myOrder(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// ログインユーザー取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
		// ユーザーが購入した注文履歴を取得しModelへ追加
		model.addAttribute("myOrder", appOrderService.getOrderByBuyer(currentUser));
		// buyer_app_orders.html へ遷移
		return "buyer_app_order";
	}

	// 販売履歴(GET /my-page/sales)
	@GetMapping("/sales")
	public String mySales(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// ログインユーザー取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
		// ユーザーが販売者として売った商品の注文一覧をModelへ追加
		model.addAttribute("mySales", appOrderService.getOrdersBySeller(currentUser));
		// seller_app_order.htmlへ遷移
		return "seller_app_order";
	}

	// お気に入り一覧 (GET /my-page/favorites)
	@GetMapping("/favorites")
	public String myFavorites(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		// ログインユーザー取得
		User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
		// お気に入り一覧をModelに追加
		model.addAttribute("FavoriteItems", favoriteService.getFavoriteItemsByUser(currentUser));
		// my_favorites.html へ遷移
		 return "my_favorites";
	}

	// 自分が投稿したレビュー一覧(GET /my-page/reviews)
	@GetMapping("/reviews")
	public String myReviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
		model.addAttribute("reviews", reviewService.getReviewsByReviewer(currentUser));
		return "user_reviews";
	}

	// 入落札の確認(GET /my-page/bids)
	@GetMapping("/bids")
	public String myBids(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User currentUser = userService.getUserByEmail(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
		model.addAttribute("myBids", bidService.getBidsByBuyer(currentUser));
		return "my_bids";
	}
}
