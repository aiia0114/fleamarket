package com.example.fleamarketsystem.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.entity.UserComplaint;
import com.example.fleamarketsystem.repository.UserComplaintRepository;
import com.example.fleamarketsystem.repository.UserRepository;

@Service
public class AdminUserService {
	
	private final UserRepository userRepository;
	private final UserComplaintRepository complaintRepository;
	
	// コンストラクタインジェクション
	public AdminUserService(UserRepository userRepository, UserComplaintRepository complaintRepository) {
		this.userRepository = userRepository;
		this.complaintRepository = complaintRepository;
	}
	// 全ユーザー一覧を取得
	public List<User> listAllUser() {
		return userRepository.findAll();
	}
	// 単一ユーザー取得(見つからなければ例外)
	public User findUser(long id) {
		return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found:" + id));
	}
	// 対象ユーザーの平均レビュー評価を取得(nullの場合は0として返す) 
	public Double averageRating(Long userId) {
		Double avg = userRepository.averageRatingForUser(userId);
		return (avg == null) ? 0.0 : avg; 
	}
	//指定ユーザーの通報件数を取得
}
