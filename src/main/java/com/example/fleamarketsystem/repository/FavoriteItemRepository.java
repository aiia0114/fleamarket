package com.example.fleamarketsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fleamarketsystem.entity.FavoriteItem;
import com.example.fleamarketsystem.entity.Item;
import com.example.fleamarketsystem.entity.User;

@Repository
public interface FavoriteItemRepository extends JpaRepository<FavoriteItem, Long>{
	Optional<FavoriteItem> findByUserAndItem(User user, Item item);

	List<FavoriteItem> findByUser(User user);

	boolean existsByUserAndItem(User user, Item item);
}
