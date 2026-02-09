package com.example.fleamarketsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fleamarketsystem.entity.Bid;
import com.example.fleamarketsystem.entity.User;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

	// 商品ごとの入札一覧(入札額の高い順)
	List<Bid> findByItemIdOrderByBidPriceDesc(Long itemId);

	// 購入者ごとの入札一覧(新しい順)
	List<Bid> findByBuyerOrderByCreatedAtDesc(User buyer);

	// 商品の最高入札を1件取得(入札がある場合)
	Optional<Bid> findFirstByItemIdOrderByBidPriceDesc(Long itemId);
}
