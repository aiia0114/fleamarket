package com.example.fleamarketsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fleamarketsystem.entity.Review;
import com.example.fleamarketsystem.entity.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>{
	// 出品者に紐づくレビュー一覧を取得
	List<Review> findBySeller(User seller);

	// 注文IDに紐づくレビューを一件取得
	Optional<Review> findByOrderId(Long orderId);

	// レビューワ(投稿者）別のレビュー一覧を取得
	List<Review> findByReviewer(User reviewer); //Add this line
}
