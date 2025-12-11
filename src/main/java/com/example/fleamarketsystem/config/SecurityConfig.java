package com.example.fleamarketsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// このクラスがSpringの設定クラスであることを示すアノテーション
@Configuration
// メソッド単位のセキュリティ(@PreAuthoriza など)を有効かするアノテーション
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // PasswordEncode をDIコンテナに登録するBean定義
    @Bean
    public	PasswordEncoder passwordEncoder() {
		//　委譲型の PasswordEncoder を生成（{bcrypt} や {noop} などの ID 付きエンコードに対応）
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFulterChain(HttpSecurity http) throws Exception{
		http
		.authorizeHttpRequests(auth -> auth
		.requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**")
		.permitAll()
		.requestMatchers("/admin/**").hasRole("ADMIN")
		.anyRequest().authenticated())
		.formLogin(form -> form
		.loginPage("/login")
		.defaultSuccessUrl("/items", true)
		.permitAll())
		.logout(logout -> logout
		.logoutUrl("/logout")
		.logoutSuccessUrl("/login?logour")
		.permitAll())
		.csrf(Customizer.withDefaults());

		return http.build();
	}
}
