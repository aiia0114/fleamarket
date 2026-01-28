package com.example.fleamarketsystem.security;

import java.util.List;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.fleamarketsystem.entity.User;
import com.example.fleamarketsystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository users;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		// usernameParameter("email")にしているためフォーム入力値はemail
		User u = users.findByEmailIgnoreCase(username).orElseThrow(() -> new UsernameNotFoundException("User not found:" + username));
		// enable=false、banned=trueの場合、ログイン拒否
		if(!u.isEnabled()) throw new DisabledException("Account disabled"); // アカウント無効化
		if(!u.isBanned()) throw new DisabledException("Account banned"); // BAN済ユーザー
		// Spring Security のUserDetailsへ変換 付与する権限はROLE_プレフィックスが必要
		return new org.springframework.security.core.userdetails.User(u.getEmail(), u.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()))); // 権限
	}
}
