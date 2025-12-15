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
    // Spring Securityのフィルタチェーンを定義する
	@Bean
	public SecurityFilterChain securityFulterChain(HttpSecurity http) throws Exception{
		// HttpSecurityに対して認可・ログイン・ログアウト・CSRF設定を行う
		http
		// 認可(アクセス制御)に対する設定を行う
		.authorizeHttpRequests(auth -> auth
		// ログイン画面および静的リソースへのアクセスパスを指定
		.requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**")
		// 上記パスは未ログインでもアクセス可能
		.permitAll()
		// /admin配下はADMINロールのみアクセス許可
		.requestMatchers("/admin/**").hasRole("ADMIN")
		// それ以外の全てのリクエストは認証済みユーザーのみアクセス許可
		.anyRequest().authenticated())
		// フォームログインページのURLを指定
		.formLogin(form -> form
		// カスタムログインページのURLを指定
		.loginPage("/login")
		// ログイン成功後の遷移先 URL（/items）を強制的に指定
		.defaultSuccessUrl("/items", true) // ログイン成功後
		// ログインページへのアクセスを未ログインでも許可
		.permitAll())
		// ログアウト処理の設定を行う
		.logout(logout -> logout
		// ログアウト処理を実行するURL (通常はPOST/ logout)
		.logoutUrl("/logout") // POST /logout
		// ログアウト成功後にリダイレクトするURL(クリエパラメーターで状態を付与)
		.logoutSuccessUrl("/login?logour")
		// ログアウトURLへのアクセスを許可
		.permitAll())
		// CSRF対策をデフォルト設定で有効化
		.csrf(Customizer.withDefaults());

		// 設定を反映したSecurityFileterChainインスタンスを生成して返却
		return http.build();
	}
}
