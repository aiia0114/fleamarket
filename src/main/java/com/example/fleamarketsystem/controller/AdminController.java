package com.example.fleamarketsystem.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.fleamarketsystem.service.AppOrderService;
import com.example.fleamarketsystem.service.ItemService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
// このクラスの全メソッドを実行するにはADMINロールが必要
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
	// 商品関連のビジネスロジックを扱うサービスクラスのフィールド
	private final ItemService itemService;
	// 注文・売上などのアプリ全体の注文情報を扱うサービスクラスのフィールド
	private final AppOrderService appOrderService;

	// コンストラクタインジェクションにより、サービスを受け取ってフィールドに設定
	public AdminController(ItemService itemService, AppOrderService appOrderService) {
		// 引数で受け取ったItemServiceをフィールドに格納
		this.itemService = itemService;
		// 引数で受け取ったItemServiceをフィールドに格納
		this.appOrderService = appOrderService;
	}

	// 管理者向けの商品一覧画面を表示するハンドラ(GET /admin/items)
	@GetMapping("/items")
	public String manageItem(Model model) {
		// 全ての商品一覧を取得して、ビューに渡すためのModelへ登録
		model.addAttribute("items", itemService.getAllItems());
		// admin_item.htmlというテンプレート名を返し、商品管理画面を表示
		return "admin_items";
	}

	// 管理者が商品を削除するためのハンドラ(POST /admin/items/{id}/delete)
	@PostMapping("/items/{id}/delete")
	public String deleteItemByAdmin(@PathVariable("id") Long itemId) {
		// パスから取得した商品IDを使って商品削除処理をサービスに依頼
		itemService.deleteItem(itemId);
		// 削除終了後、商品一覧画面へリダイレクトし、クリエパラメーターで成功メッセージを付加
		return "redirect:/admin/item?success=deleted";
	}

	// 売上統計を表示する画面用ハンドラ(GET /admin/statistics)
	@GetMapping("/statistics")
	public String showStatistics(//開始日をクリエパラメーターから取得(任意)指定がない場合はnullになる
								 	@RequestParam(value = "startDate", required = false)
								 // 文字列の日付をISO形式(yyyy-MM-dd)としてLocalDateに変換する指定
								 	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
								 // 終了日をクリエパラメーターから取得(任意)指定がない場合はnullになる
								 	@RequestParam(value = "endDate", required = false)
								 // 同様に、ISO形式でLocalDateに変換する指定
								 	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
								 // 画面に渡す値を詰め込むためのModelオブジェクト
								 	Model model) {
		// startDateが指定されなかった場合、デフォルトで「一ヶ月前の日付」を開始日に設定
		if (startDate == null)
			startDate = LocalDate.now().minusMonths(1);
		// endDate が指定されなかった場合、デフォルトで「本日」を終了日に設定
		if(endDate == null)
			endDate = LocalDate.now();
		// 画面側で選択状態を表示できるように、開始日をModelに登録
		model.addAttribute("startDate", startDate);
		// 終了日も同様にModelに登録
		model.addAttribute("endDate", endDate);
		// 指定期間の総売上金額をサービスから取得し、画面表示用にModelに登録
		model.addAttribute("totalSales", appOrderService.getTotalSales(startDate, endDate));
		// 指定期間のステータス別注文数(例:完了・キャンセルなど)を習得しModelへ登録
		model.addAttribute("orderCountByStatus", appOrderService.getOrderCountByStatus(startDate, endDate));
		// admin_statistics.html というテンプレート名を返し、統計画面を表示
		return "admin_statistics";
	}
	// 売上統計をCSV形式でダウンロードさせるためのハンドラ(GET /admin/statistics/csv)
	@GetMapping("/statistics/csv")
	public void exportStatisticsCsv(//開始日をクリエパラメーターから取得(任意)指定がない場合はnullになる
									 	@RequestParam(value = "startDate", required = false)
									 // 文字列の日付をISO形式(yyyy-MM-dd)としてLocalDateに変換する指定
									 	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
									 // 終了日をクリエパラメーターから取得(任意)指定がない場合はnullになる
									 	@RequestParam(value = "endDate", required = false)
									 // 同様に、ISO形式でLocalDateに変換する指定
									 	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
									 // HTTPレスポンス(ヘッダ設定や出力ストリーム取得に使用)
									 	HttpServletResponse response) throws IOException {
		// startDateが指定されなかった場合、デフォルトで「一ヶ月前の日付」を開始日に設定
		if (startDate == null)
			startDate = LocalDate.now().minusMonths(1);
		// endDate が指定されなかった場合、デフォルトで「本日」を終了日に設定
		if(endDate == null)
			endDate = LocalDate.now();

		// レスポンスのコンテンツタイプをcsv(UTF-8)としてクライアントに通知
		response.setContentType("text/csv; charset=UTF-8");
		// ブラウザに「ファイルとしてダウンロードさせる」ためのヘッダを設定(ファイル名も指定)
		response.setHeader("Content-Disposition", "attachment; filename=\"flea_market_statistics\"");
		// try-with-resources構文でPrintWriterを取得し、自動でクローズさせる
		try(PrintWriter writer= response.getWriter()){
			// 統計期間の情報を1行目に出力
			writer.append("統計期間:").append(String.valueOf(startDate)).append("から").append(String.valueOf(endDate)).append("¥n¥n");
			// 2ブロック目として、期間内の総売上を出力
			writer.append("総売上:").append(String.valueOf(appOrderService.getTotalSales(startDate, endDate))).append("¥n¥n");
			// ステータス別注文数のヘッダ行を出力
			writer.append("ステータス別注文数¥n");
			// ステータスごとの件数マップを取り出し、1行ずつ「ステータス、件数」の形式で出力
			appOrderService.getOrderCountByStatus(startDate, endDate)
								.forEach((status, count) -> writer.append(status).append(",").append(String.valueOf(count)).append("¥n"));
		}

	}
}
