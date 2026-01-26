package com.example.fleamarketsystem.service;

//チャットエンティティを扱うための import
import com.example.fleamarketsystem.entity.Chat;
//商品エンティティを扱うための import
import com.example.fleamarketsystem.entity.Item;
//ユーザエンティティを扱うための import
import com.example.fleamarketsystem.entity.User;
//チャットの検索/保存に使うリポジトリを import
import com.example.fleamarketsystem.repository.ChatRepository;
//商品の存在確認に使うリポジトリを import
import com.example.fleamarketsystem.repository.ItemRepository;
//DI 対象のサービスであることを示すアノテーションを import
import org.springframework.stereotype.Service;
//送信日時を記録するために LocalDateTime を import
import java.time.LocalDateTime;
//履歴表示に使う List を import
import java.util.List;

// サービス層として登録
@Service
public class ChatService {
	// チャットリポジトリの参照
	private final ChatRepository chatRepository;
	// 商品リポジトリの参照
	private final ItemRepository itemRepository;
	// LINE通知サービスの参照
	private final LineNotifyService lineNotifyService;

	public ChatService(ChatRepository chatRepository, ItemRepository itemRepository, LineNotifyService lineNotifyService) {
		// フィールド設定
		this.chatRepository = chatRepository;
		this.itemRepository = itemRepository;
		this.lineNotifyService = lineNotifyService;
	}

	// 商品IDに紐づくチャット履歴を昇順で取得
	public List<Chat> getChatMessagesByItem(Long itemId) {
		// 商品の存在を確認(なければ404相当の例外)
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
		// 作成日時昇順でリストを返す
		return chatRepository.findByItemOrderByCreatedAtAsc(item);
	}

	// メッセージ送信:保存して相手にLINE通知(可能なら)を行う
	public Chat sendMessage(Long itemId, User sender, String message) {
		// 対象商品を取得(存在しなければ例外)
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
		// 新規チャットエンティティを構築
		Chat chat = new Chat();
		// 商品を紐付け
		chat.setItem(item);
		// 送信者を紐付け
		chat.setSender(sender);
		// 本文を設定
		chat.setMessage(message);
		// 現在時刻で送信時刻を設定
		chat.setCreatedAt(LocalDateTime.now());
		// 保存して永続化
		Chat savedChat = chatRepository.save(chat);
		// 簡易実装:受信者を出品者とみなして通知(詳細な相手は拡張で対応)
		User receiver = item.getSeller();
		// 受信者が通知トークンを設定していれば通知を送る
		if(receiver != null && receiver.getLineNotifyToken() != null) {
			// 通知本文を作成
			String notificationMessage = String.format("¥n商品「%s」に関する新しいメッセージが届きました!¥n送信者: %s¥nメッセージ: %s", item.getName(), sender.getName(), message);
			// LINE Notify へ送信
			lineNotifyService.sendMessage(receiver.getLineNotifyToken(), notificationMessage);
		}
		return savedChat;
	}
}
