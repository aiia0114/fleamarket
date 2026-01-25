package com.example.fleamarketsystem.service;

// カテゴリエンティティを扱うための import
import com.example.fleamarketsystem.entity.Category;
// リポジトリ IF を import
import com.example.fleamarketsystem.repository.CategoryRepository;
// DI 対象サービスを示すアノテーションを import
import org.springframework.stereotype.Service;
// 一覧返却に使う List を import
import java.util.List;
// Optional を返すために import
import java.util.Optional;

@Service
public class CategoryService {
	// カテゴリリポジトリの参照
	private final CategoryRepository categoryRepository;
	// 依存性をコンストラクタで注入
	public CategoryService(CategoryRepository categoryRepository) {
		// フィールドへ設定
		this.categoryRepository = categoryRepository;
	}
	// 全てのカテゴリを取得
	public List<Category> getAllCategory(){
		// 全件取得を委譲
		return categoryRepository.findAll();
	}
	// 主キーでカテゴリを取得
	public Optional<Category> getCategoryById(Long id) {
		// Optional をそのまま返す
		return categoryRepository.findById(id);
	}
	// 名称でカテゴリを取得(名称は一意前提)
	public Optional<Category> getCategoryByName(String name) {
		// 名称検索で委譲
		return categoryRepository.findByName(name);
	}

	// 新規/更新保存
	public Category saveCategory(Category category) {
		// saveに委譲
		return categoryRepository.save(category);
	}
	// 削除
	public void deleteCategory(Long id) {
		// ID指定で削除
		categoryRepository.deleteById(id);
	}
}
