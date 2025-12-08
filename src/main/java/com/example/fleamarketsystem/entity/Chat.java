package com.example.fleamarketsystem.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "item_id", unique = false)
	private Item item;

	@ManyToOne
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	// メッセージ本文
	@Column(columnDefinition = "TEXT")
	private String message;

	//メッセージ送信日時
	@Column(name = "create_at", unique = false)
	private LocalDateTime createdAt = LocalDateTime.now();
}
