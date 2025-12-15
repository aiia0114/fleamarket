package com.example.fleamarketsystem.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.UserRepository;
import com.example.fleamarketsystem.service.AppOrderservice;
import com.example.fleamarketsystem.service.ItemService;

@Controller
public class DashboardController {

	// ログインユーザー情報などをDBから取得するためのリポジトリ
	private final UserRepository userRepository;
	// 商品情報(出品一覧など)を取得するためのサービス
	private final ItemService itemService;
	// 注文情報（売上・注文履歴など）を取得するためのサービス
	private final AppOrderService appOrderService;

	public DashboardController(UserRepository userRepository, ItemService itemService, AppOrderService appOrderService, UserComplaintRepository userComplaintRepository, AppOrderRepository appOrderRepository) {
		// 渡されたUserRepositoryをフィールドに格納
		this.userRepository = userRepository;
		// 渡されたItemServiceをフィールドに格納
		this.itemService = itemService;
		// 渡されたAppOrderServiceをフィールドに格納
		this.appOrderService = appOrderService;
	}

	// ダッシュボード画面の表示を行うハンドラメソッド(GET /dashboard)
	@GetMapping("/dashboard")
	public String dashboard(
			// 現在ログイン中のユーザー情報(UserDetails)をSpring Securityから取得
			@AuthenticationPrincipal UserDetails userDetails,
			// 画面に値すためのModelオブジェクト
			Model model) {
		User currentUser = userRepository.findByEmailIgnoreCase(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
		if ("ADMIN".equals(currentUser.getRole())) {
			model.addAttribute("recentItems", itemService.getAllItems());
			model.addAttribute("recentOrders", appOrderService.getAllOrders());
			return "admin_dashboard";
		} else {
			return "redirect:/items";
		}
	}
}
