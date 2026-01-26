// 画像アップロード・削除の薄いラッパ
package com.example.fleamarketsystem.service;

//Cloudinary の Java SDK のエントリポイントを import
import com.cloudinary.Cloudinary;
//アップロード/削除で使うユーティリティを import
import com.cloudinary.utils.ObjectUtils;
//設定値を外部から注入するためのアノテーションを import
import org.springframework.beans.factory.annotation.Value;
//DI 対象のサービスであることを示すアノテーションを import
import org.springframework.stereotype.Service;
//Spring のファイルアップロード表現を import
import org.springframework.web.multipart.MultipartFile;
//I/O 例外処理のための import
import java.io.IOException;
//アップロード結果を受け取る Map を import
import java.util.Map;

@Service
public class CloudinaryService {
	// Cloudinaryクライアントの参照
	private final Cloudinary cloudinary;
	// 必要な承認情報をコンストラクタインジェクションで受け取る
	public CloudinaryService(
			// クラウド名をapplication.propertiesから注入
			@Value("${coludinary.cloud-name}") String cloudName,
			// APIキーを注入
			@Value("${cloudinary.api-Key}") String apiKey,
			// APIシークレットを注入
			@Value("${cloudinary.api-secret}") String apiSecret
			) {
		// 渡された資格情報でCloudinaryクライアントを初期化
		cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", cloudName,
													  "api-key", apiKey,
													  "api_secret", apiSecret));

	}

	// 画像をアップロードして公開URLを返す(からファイルはnull)
	public String uploadFile(MultipartFile file) throws IOException{
		// アップロード無しの場合はnullを返す
		if(file.isEmpty()) {
			return null;
		}
		// バイト配列をそのままアップロード(オプションは既定)
		Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
		return uploadResult.get("url").toString();
	}

	// Cloudinary上のレソースを削除(URLからpublic_idを推定)
	public void deleteFile(String publicId) throws IOException{
		// URLを/で分割して末尾のファイル名を取り出す
		String[] parts = publicId.split("/");
		// 配列末尾=ファイル名部分を取得
		String fileName = parts[parts.length -1];
		// 拡張子をのぞいたpublic_idを指定
		String publicIdWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
		// public_idを指定して削除APIを呼び出す
		cloudinary.uploader().destroy(publicIdWithoutExtension, ObjectUtils.emptyMap());
		}
}
