package com.example.fleamarketsystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fleamarketsystem.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	// メールアドレスでユーザー検索
	Optional<User> findByEmail(String email);
	// 大文字小文字を区別せずメールアドレスで検索するメソッドを追加
	Optional<User> findByEmailIgnoreCase(String email);

	// 指定ユーザーの平均評価を取得するカスタムクエリ
	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.seller.id = :userId")
	Double averageRatingForUser(@Param("userId") Long userId);
}