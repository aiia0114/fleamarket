package com.example.fleamarketsystem.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.UserRepository;
import com.example.fleamarketsystem.service.AdminUserService;

@Controller
//このクラスが扱う URL のプレフィックスを /admin/users に固定する
@RequestMapping("/admin/users")
//このクラスの全メソッドを実行するにはADMINロールが必要
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
	// 管理者用ユーザー管理ロジックを提供するサービスを保持するフィールド
	private final AdminUserService service;
	// ユーザー情報へのDBアクセスを行うリポジトリを保持するフィールド
	private final UserRepository users;

	// コンストラクタインジェクションでサービスとリポジトリを受け取る
	public AdminUserController(AdminUserService service, UserRepository users) {
		// 引数のAdminUserServiceをフィールドにセット
		this.service = service;
		// 引数のUserRepositoryをフィールドにセット
		this.users= users;
	}

	// ユーザー一覧画面の表示を担当するハンドラー(GET /admin/users)
	@GetMapping
	public String list(@RequestParam(value = "q", required = false) String q,
						@RequestParam(value = "sort", required = false, defaultValue = "id")
						String sort, Model model) {
		// 全ユーザーイリランをサービスから取得
		List<User> list = service.listAllUser();

		// 検索キーワードが指定されている場合のみフィルタリングを実施
		if (StringUtils.hasText(q)) {
			// 検索キーワードを小文字に変換して大文字・小文字を区別しない検索に対応
			String qq = q.toLowerCase();
			// ストリームAPIでユーザー名またはメールアドレスに検索キーワードを含むレコードを抽出
			list = list.stream().filter(u -> (u.getName() != null && u.getName().toLowerCase().contains(qq)) ||
															(u.getEmail() != null && u.getEmail().toLowerCase().contains(qq))).toList();
			}
		// sort パラメーターの値に応じてソート条件を切り替える
		list = switch (sort) {
		// 名前順ソート:nullは最後に回し、大文字小文字を無視して比較
		case "name" -> list.stream().sorted(Comparator.comparing(User::getName, Comparator.nullsLast(String::compareToIgnoreCase))).toList();
		//メールアドレス順ソート:同様にnullを最後、大文字小文字無視
		case "email" -> list.stream().sorted(Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareToIgnoreCase))).toList();
		// BANフラグ順ソート:BANされているユーザーを優先表示するための降順(tureが先)
		case "banned" -> list.stream().sorted(Comparator.comparing(User::isBanned).reversed()).toList();
		// デフォルトはソートなし(習得した順のまま)
		default -> list;
		};
		// 画面に表示するユーザー一覧をModelに格納
		model.addAttribute("users", list);
		// 検索キーワードを再表示用にModelに格納
		model.addAttribute("q", q);
		// 現在のソート条件も画面側で利用できるようにModelに格納
		model.addAttribute("sort", sort);
		// ユーザー一覧画面に太陽するテンプレートを返却
		return "admin/users/list";
	}
	// 個別ユーザー詳細画面の表示を担当するハンドラー(GET /admin/users/{id})
	@GetMapping("/{id}")
	public String detail(@PathVariable Long id, Model model) {
		// 指定IDのユーザー情報をサービスから取得
		User user  = service.findUser(id);
		// 指定ユーザーの平均評価値を取得
		Double avg = service.averageRating(id);
		// 指定ユーザーに対するクレーム件数を取得
		long complaints = service.complaintCount(id);
		// ユーザー情報を画面表示用にModelに格納
		model.addAttribute("user", user);
		// 平均評価をModelに格納
		model.addAttribute("avgRating", avg);
		// クレーム件数をModelに格納
		model.addAttribute("complaintCount", complaints);
		// クレーム詳細一覧をModelに格納
		model.addAttribute("complaints", service.complaints(id));
		// ユーザー詳細画面に対応するテンプレート名を返却
		return "admin/users/detail";
	}
	// ユーザーのBANを解除する処理を担当するハンドラー(POST /admin/users/{id}/unban)
	@PostMapping("/{id}/unban")
	public String unban(@PathVariable Long id) {
		// 指定ユーザーのBAN状態を解除するようにサービス依頼
		service.unbanUser(id);
		// 対象ユーザー詳細画面へリダイレクトし、クリエパラメーターで解除済みであることを通知
		return "redirect:/admin/users/" + id + "?unbanned";
	}
}
