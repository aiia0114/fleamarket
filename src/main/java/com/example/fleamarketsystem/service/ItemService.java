package com.example.fleamarketsystem.service;

import com.example.fleamarketsystem.entity.Item;
import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.ItemRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
	private final ItemRepository itemRepository;

	private final CategoryService categoryService;

	private final CloudinaryService cloudinaryService;

	public ItemService(ItemRepository itemRepository, CategoryService categoryService, CloudinaryService cloudinaryService) {
		this.itemRepository= itemRepository;
		this.categoryService = categoryService;
		this.cloudinaryService = cloudinaryService;
	}
	// 商品検索: キーワード/カテゴリ/ページっbぐを組み合わせ、公開中のみ返す
	public Page<Item> searchItems(String keyword, Long categoryId, int page, int size) {
		// ページング指定を作成
		Pageable pageable = PageRequest.of(page,  size);
		// キーワードとカテゴリ両方指定時の検索
		if(keyword != null && !keyword.isEmpty() && categoryId != null) {
			// 名前LIKE✖カテゴリ✖出品中で検索
			return itemRepository.findByNameContainingIgnoreCaseAndCategoryIdAndStatus(keyword, categoryId, "出品中", pageable);
		// キーワード飲み指定時の検索
		} else if (keyword != null && !keyword.isEmpty()){
			// 名前LIKE✖出品中で検索
			return itemRepository.findByNameContainingIgnoreCaseAndCategoryIdAndStatus(keyword, "出品中", pageable);
		// カテゴリの指定時の検索
		}else if(categoryId != null) {
			// カテゴリ✖出品中で検索
			return itemRepository.findByCategoryIdAndStatus(categoryId, "出品中", pageable);
		// 条件未指定時のデフォルト検索
		}else {
			// 出品中のみ全件ページング返す
			return itemRepository.findByStatus("出品中", pageable);
		}
	}
	//　全商品一覧を返す(管理用など)
	public List<Item> getAllItems(){
		//リポジトリの全権取得を委譲
		return itemRepository.findAll();
	}
	// 主キーで商品を取得
	public Optional<Item> getItemById(Long id){
		// Optionalをそのまま返却 optional ←あるかもしれない nullを許容
		return itemRepository.findById(id);
	}
	// 商品保存: 必要なら画像をCloudinaryへアップロードしてURLを保存
	public Item saveItem(Item item, MultipartFile imageFile) throws IOException{
		// 画像が添付されている場合にのみアップロード処理を実行
		if (imageFile != null && !imageFile.isEmpty()) {
			// CloudinaryへアップロードしURLを受け取る
			String imageUrl = cloudinaryService.uploadFile(imageFile);
			// 画像URLをエンティティへ設定
			item.setImageUrl(imageUrl);
		}
		return itemRepository.save(item);
	}
	// 商品削除: Cloudinary 上の画像も可能なら削除してから DB 削除
	public void deleteItem(long id) {
		// まず対象商品を取得し、存在する場合にのみ削除処理を進める
		itemRepository.findById(id).ifPresent(item -> {
			// 画像URLがある場合はCloudinary側の削除を試みる
			if (item.getImageUrl() != null) {
				try {
					// URLからpublic idを指定し削除
					cloudinaryService.deleteFile(item.getImageUrl());
				}catch (IOException e) {
					// 画像削除失敗は致命ではないためログ出力に留める
					System.err.println("Failed to delete image from Cloudinary:" + e.getMessage());
				}
			}
			// 最後にDBから商品レコードを削除
			itemRepository.deleteById(id);
		});
	}
	// 出品者の出品一覧を取得
	public List<Item> getItemsBySeller(User seller){
		// seller条件で検索
		return itemRepository.findBySeller(seller);
	}
	//売却確定: 商品ステータスを売却済へ変更
	public void markItemAsSold(Long itemId) {
		//商品を取得して存在する場合のみ更新
		itemRepository.findById(itemId).ifPresent(item -> {
			item.setStatus("売却済");
			// 変更を保存
			itemRepository.save(item);
		});
	}
}
