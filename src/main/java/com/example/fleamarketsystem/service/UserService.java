package com.example.fleamarketsystem.service;

//ユーザエンティティを扱うための import
import com.example.fleamarketsystem.entity.User;
//リポジトリ IF を import
import com.example.fleamarketsystem.repository.UserRepository;
//DI 対象のサービスを表すアノテーションを import
import org.springframework.stereotype.Service;
//変更系でトランザクションを開始するためのアノテーションを import
import org.springframework.transaction.annotation.Transactional;
//一覧返却に使う List を import
import java.util.List;
//Optional を返すための import
import java.util.Optional;

@Service
public class UserService {
	// ユーザーリポジトリの参照
	private final UserRepository userRepository;

	// 依存性をコンストラクタで注入
	public UserService(UserRepository userRepository) {
		// フィールドへ設定
		this.userRepository = userRepository;
	}

	// 全てのユーザーを取得
	public List<User> getAllUsers(){
		// 全件取得を委譲
		return userRepository.findAll();
	}

	// 主キーでユーザを取得
	public Optional<User> getUserById(Long id){
		// Optionalを返す
		return userRepository.findById(id);
	}

	// メールアドレスでユーザーを取得
	public Optional<User> getUserByEmail(String email){
		// Optionalを返す
		return userRepository.findByEmail(email);
	}

	// 新規/更新保存
	@Transactional
	public User saveUser(User user) {
		return userRepository.save(user);
	}

	// 削除
	@Transactional
	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	// 有効/無効のフラグのトグル
	@Transactional
	public void toggleUserEnabled(Long userId) {
		// IDでユーザーを取得(なければ400相当の例外)
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		// 既存の属性は変更せずenableだけ反転
		user.setEnabled(!user.isEnabled());
		// 保存して確定
		userRepository.save(user);
	}
}
