package com.example.fleamarketsystem.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;

@Entity
@Table(name ="item")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", unique = false)
	private User seller;

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private BigDecimal price;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	private String status = "出品中";

	private String imageUrl;

	@Column(name = "created_at", unique = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	// オークション追加フィールド
	@Column(name = "type", nullable = false, length = 20)
	private String type = "FIXED";

	@Column(name = "start_price", precision = 10, scale = 2)
	private BigDecimal startPrice;

	@Column(name = "current_bid_price", precision = 10, scale = 2)
	private BigDecimal currentBidPrice;

	@Column(name = "reserve_price", precision = 10, scale = 2)
	private BigDecimal reservePrice;

	@Column(name = "auction_end_time")
	private LocalDateTime auctionEndTime;

	
	public boolean isAuction() {
		return "AUCTION".equals(type);
	}

	
	public boolean isAuctionEnded() {
		return auctionEndTime != null && LocalDateTime.now().isAfter(auctionEndTime);
	}

	
	@PrePersist
	public void beforePersist() {
		if (!isAuction() && auctionEndTime == null) {
			auctionEndTime = createdAt != null ? createdAt : LocalDateTime.now();
		}
	}
}
