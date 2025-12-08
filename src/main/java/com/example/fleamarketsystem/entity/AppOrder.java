package com.example.fleamarketsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppOrder {
	// 主キーの定義(AUTO_INCREMENT)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 注文紐づく商品(必須)
	@ManyToOne
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	//買い手ユーザー（必須）
	@ManyToOne
	@JoinColumn(name = "buyer_id", nullable = false)
	private User buyer;

	// 決済金額のスナップショット(必須)
	@Column(nullable = false)
	private BigDecimal price;

	//注文状態(購入済/発送済/発送待ち 等)
	@Column(nullable = false)
	private String status = "購入済";

	// 注文の作成日時を表すカラム。created_at という列名で必須項目とする
	@Column(name = "created_at", nullable = false) // new field
	// デフォルトで現在日時
	private LocalDateTime createdAt = LocalDateTime.now();

	// Stripeなどの決済サービスのPaymentIntent ID を保存するカラム(一意制約付き)
	@Column(name = "payment_intent_id", unique = true)
	private String paymentIntentId;

	// paymrntIntentIdのgetter
	public String getPaymentIntentId() {
		return paymentIntentId;
	}
	// paymentIntentIdのsetter
	public void setPaymentIntentId(String paymentIntentId) {
		this.paymentIntentId = paymentIntentId;
	}
}
