package com.example.fleamarketsystem.service;

//外部設定から既定 URL を読み込むために@Value を import
import org.springframework.beans.factory.annotation.Value;
//HTTP リクエストを構築するための型を import
import org.springframework.http.HttpEntity;
//HTTP ヘッダを構築するための型を import
import org.springframework.http.HttpHeaders;
//コンテントタイプの列挙を import
import org.springframework.http.MediaType;
//DI 対象のサービスであることを示すアノテーションを import
import org.springframework.stereotype.Service;
//フォームパラメータを組み立てるためのユーティリティを import
import org.springframework.util.LinkedMultiValueMap;
//フォームのマップ表現を import
import org.springframework.util.MultiValueMap;
//HTTP クライアントとして RestTemplate を import
import org.springframework.web.client.RestTemplate;

@Service
public class LineNotifyService {
	// APIエンドポイントURL(未設定ならデフォルト値を使う)
	@Value("${line.notify.api.url:https://notify-api.line.me/api/notify}")
	private String lineNotifyApiUrl;

	// HTTPクライアントの参照
	private final RestTemplate restTemplate;

	// 依存性をコンストラクタで注入
	public LineNotifyService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	// アクセストークンと本文を受け取り、LINE Notifyへ送信
	public void sendMessage(String accessToken, String message) {
		// リクエストヘッダを構築
		HttpHeaders headers = new HttpHeaders();
		// フォームURLエンコードを指定
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		// Bearerトークンをセット
		headers.setBearerAuth(accessToken);

		// フォームボディを構築
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		// messageキーに本文を格納
		map.add("message", message);
		//  ヘッダ+本文でエンティティを作成
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		// 送信施行(失敗はログに残して握りつぶす)
		try {
			// POSTでAPIへ投げる
			restTemplate.postForEntity(lineNotifyApiUrl, request, String.class);
			// 成功ログを標準出力へ
			System.out.println("LINE Notify message sent successfully");
		}catch(Exception e) {
			// 失敗時は標準エラーへ出力して処理継続
			System.out.println("Failed to send LINE Notify message:" + e.getMessage());
		}
	}
}
