package com.example.fleamarketsystem.repository;

import com.example.fleamarketsystem.entity.AppOrder;
import com.example.fleamarketsystem.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppOrderRepository extends JpaRepository<AppOrder, Long> {
	List<AppOrder> findByBuyer(User buyer);

	List<AppOrder> findByItem_Seller(User seller);

	// PaymentIntent ID から注文を一件取得するメソッドを追加
	Optional<AppOrder> findByPaymentIntentId(String paymentIntentId);
}