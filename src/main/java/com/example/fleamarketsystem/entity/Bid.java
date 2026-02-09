package com.example.fleamarketsystem.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * オークション入札（schema.bid テーブル対応）
 */
@Entity
@Table(name = "bid")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bid {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	@ManyToOne
	@JoinColumn(name = "buyer_id", nullable = false)
	private User buyer;

	@Column(name = "bid_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal bidPrice;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();
}
