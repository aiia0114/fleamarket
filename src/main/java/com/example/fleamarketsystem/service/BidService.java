package com.example.fleamarketsystem.service;

import com.example.fleamarketsystem.entity.Bid;
import com.example.fleamarketsystem.entity.Item;
import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.BidRepository;
import com.example.fleamarketsystem.repository.ItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BidService {
	private final BidRepository bidRepository;
	private final ItemRepository itemRepository;

	public BidService(BidRepository bidRepository, ItemRepository itemRepository) {
		this.bidRepository = bidRepository;
		this.itemRepository = itemRepository;
	}

	// 入札する（通常入札 or 即時落札）
	@Transactional
	public Bid placeBid(User buyer, Long itemId, BigDecimal bidPrice) {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
		if (!item.isAuction()) {
			throw new IllegalStateException("この商品はオークションではありません");
		}
		if (item.isAuctionEnded()) {
			throw new IllegalStateException("このオークションは終了しています");
		}
		if (item.getSeller().getId().equals(buyer.getId())) {
			throw new IllegalStateException("出品者は入札できません");
		}

		BigDecimal minPrice = item.getCurrentBidPrice() != null ? item.getCurrentBidPrice() : item.getStartPrice();
		if (bidPrice == null || bidPrice.compareTo(minPrice) < 0) {
			throw new IllegalArgumentException("入札額は現在価格以上で入力してください");
		}

		// 即時落札価格以上なら落札済にする
		if (item.getReservePrice() != null && bidPrice.compareTo(item.getReservePrice()) >= 0) {
			item.setCurrentBidPrice(bidPrice);
			item.setStatus("落札済");
			itemRepository.save(item);
		} else {
			item.setCurrentBidPrice(bidPrice);
			itemRepository.save(item);
		}

		Bid bid = new Bid();
		bid.setItem(item);
		bid.setBuyer(buyer);
		bid.setBidPrice(bidPrice);
		bid.setCreatedAt(LocalDateTime.now());
		return bidRepository.save(bid);
	}

	// 商品ごとの入札一覧（高い順）
	public List<Bid> getBidsByItemId(Long itemId) {
		return bidRepository.findByItemIdOrderByBidPriceDesc(itemId);
	}

	// 入札者ごとの入札一覧（新しい順）
	public List<Bid> getBidsByBuyer(User buyer) {
		return bidRepository.findByBuyerOrderByCreatedAtDesc(buyer);
	}

	// 商品の最高入札を1件取る
	public Optional<Bid> getHighestBid(Long itemId) {
		return bidRepository.findFirstByItemIdOrderByBidPriceDesc(itemId);
	}

	// 終了時刻を過ぎたオークションを締め切る（スケジュール or 手動用）
	@Transactional
	public void closeEndedAuctions() {
		LocalDateTime now = LocalDateTime.now();
		List<Item> all = itemRepository.findAll();
		for (Item item : all) {
			if (!item.isAuction() || !item.isAuctionEnded()) {
				continue;
			}
			if (!"出品中".equals(item.getStatus())) {
				continue;
			}
			Optional<Bid> top = bidRepository.findFirstByItemIdOrderByBidPriceDesc(item.getId());
			if (top.isPresent()) {
				item.setStatus("落札済");
				item.setCurrentBidPrice(top.get().getBidPrice());
				itemRepository.save(item);
			} else {
				item.setStatus("終了");
				itemRepository.save(item);
			}
		}
	}
}
