package com.example.fleamarketsystem.service;

//お気に入りエンティティの import
import com.example.fleamarketsystem.entity.FavoriteItem;
//商品エンティティの import
import com.example.fleamarketsystem.entity.Item;
//ユーザエンティティの import
import com.example.fleamarketsystem.entity.User;
//お気に入り用リポジトリの import
import com.example.fleamarketsystem.repository.FavoriteItemRepository;
//商品リポジトリの import
import com.example.fleamarketsystem.repository.ItemRepository;
//サービスアノテーションの import
import org.springframework.stereotype.Service;
//トランザクション境界を宣言するための import
import org.springframework.transaction.annotation.Transactional;
//コレクション操作のための import
import java.util.List;
//Stream で変換するための import
import java.util.stream.Collectors;

@Service
public class FavoriteService {
	// リポジトリの参照(お気に入り)
	private final FavoriteItemRepository favoriteItemRepository;
	// リポジトリの参照(商品)
	private final ItemRepository itemRepository;
	// 依存性をコンストラクタで注入
	public FavoriteService(FavoriteItemRepository favoriteItemRepository, ItemRepository itemRepository) {
		// お気に入りリポジトリを設定
		this.favoriteItemRepository = favoriteItemRepository;
		// 商品リポジトリを設定
		this.itemRepository = itemRepository;
	}
	// お気に入り追加(同一ユーザー✖商品は一意️)
	@Transactional
	public FavoriteItem addFavorite(User user, Long itemId) {
		// 商品存在チェック
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
		// 既に登録済みならエラー
		if(favoriteItemRepository.existsByUserAndItem(user, item)) {
			// 2重登録を防止
			throw new IllegalStateException("Item is already favorite by this user.");
		}
		FavoriteItem favoriteItem = new FavoriteItem();
		// ユーザーを設定
		favoriteItem.setUser(user);
		// 商品を設定
		favoriteItem.setItem(item);
		// 保存して返す
		return favoriteItemRepository.save(favoriteItem);
	}

	// お気に入り解除
	@Transactional
	public void removeFavorite(User user, Long itemId) {
		// 商品取得(存在チェック)
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
		// ユーザー✖️商品でお気に入りを取得(なければエラー)
		FavoriteItem favoriteItem = favoriteItemRepository.findByUserAndItem(user, item).orElseThrow(() -> new IllegalStateException("Favorite not found"));
		// 削除実行
		favoriteItemRepository.delete(favoriteItem);
	}
	// お気に入りかどうかの判定
	public boolean isFavorited(User user, Long itemId) {
		// 商品取得(存在チェック)
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
		// 存在するかどうかを返す
		return favoriteItemRepository.existsByUserAndItem(user, item);
	}
	// ユーザーのお気に入り商品一覧を返す
	public List<Item> getFavoriteItemsByUser(User user) {
		// FavoriteItemからItemへマッピング
		return favoriteItemRepository.findByUser(user).stream().map(FavoriteItem::getItem).collect(Collectors.toList());
	}
}
