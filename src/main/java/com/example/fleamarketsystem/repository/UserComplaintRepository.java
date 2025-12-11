package com.example.fleamarketsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fleamarketsystem.entity.UserComplaint;

public interface UserComplaintRepository extends JpaRepository<UserComplaint, Long>{
	// 指定ユーザーに対する通報件数を取得
	long countByReportedUserId(Long reportedUserId);
	// 通報対象ユーザーの通報経歴を作成日時を降順で取得
	List<UserComplaint> findByReportedUserIdOrderByCreatedAtDesc(Long reportedUserId);
}
