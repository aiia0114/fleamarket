// ホーム画面（ルートパス "/"）へのアクセス時の振る舞いを制御するコントローラ
// src/main/java/com/example/fleamarketsystem/controller/HomeController.java
package com.example.fleamarketsystem.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	// アプリのルートURL("/")へのGETアクセスを受け取るハンドラメソッド
	@GetMapping("/")
	public String home(Authentication auth) {
		// 認証情報がnull(未ログイン)または承認されていない場合は商品一覧ページへリダイレクトする
		// 『未ログインでもとりあえず/itemsに飛ばす』というポリシー
		if(auth == null || !auth.isAuthenticated()) {
			// 商品一覧画面(/items)へリダイレクト
			return "redirect:/items";
		}
		// 承認済みの場合、ユーザーがADMINロールを持っているかを判定する
		boolean isAdmin = auth.getAuthorities().stream()
				.anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		// 管理者なら管理ユーザー一覧画面へ、それ以外は商品一覧画面へリダイレクト
		return isAdmin ? "redirect:/admin/users" : "redirect:/items";
	}
}
