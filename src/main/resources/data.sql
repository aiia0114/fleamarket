-- 初期ユーザ投入（開発用）。NoOpPasswordEncoder 前提で平文パスワード
INSERT INTO users (name, email, password, role) VALUES
-- 出品者：メールとパスワード
('出品者 A',
'sellerA@example.com',	'password',	'USER'),
-- 購入者：わかりやすいメールに修正（'z'は誤りだと運用上混乱するため）
('購入者 B',	'buyerB@example.com',	'password',	'USER'),
-- 管理者：管理用アカウント
('運営者 C',	'adminC@example.com',	'adminpass',	'ADMIN');
-- 初期カテゴリ投入（よく使う 4 種）
INSERT INTO category (name) VALUES
-- 書籍カテゴリ
('本'),
-- 家電カテゴリ
('家電'),
-- ファッションカテゴリ
('ファッション'),
-- 玩具カテゴリ
('おもちゃ');
-- 初期商品投入（出品者 A が 2 商品を出品）
INSERT INTO item (user_id, name, description, price, category_id, status, image_url)
VALUES
-- Java 入門書（カテゴリ：本、出品中）
(
(SELECT id FROM users WHERE email = 'sellerA@example.com'),
	'Java プログラミング入門',
	'初心者向けの Java 入門書です。',
	1500.00,
(SELECT id FROM category WHERE name = '本'),
	'出品中',
	NULL
),
-- イヤホン（カテゴリ：家電、出品中）
(
(SELECT id FROM users WHERE email =	'sellerA@example.com'),
	'ワイヤレスイヤホン',
	'ノイズキャンセリング機能付き。',
	8000.00,
(SELECT id FROM category WHERE name = '家電'),
	'出品中',
	NULL
);

-- オークション商品の投入（出品者 A が出品）
INSERT INTO item (user_id, name, description, price, category_id, status, image_url, type, start_price, current_bid_price, reserve_price, auction_end_time)
VALUES
(
(SELECT id FROM users WHERE email = 'sellerA@example.com'),
	'限定版フィギュア（オークション）',
	'入手困難なフィギュアです。',
(SELECT id FROM category WHERE name = 'おもちゃ'),
	'出品中',
	NULL,
	'AUCTION', 					-- type を 'AUCTION' に設定
	3000.00, 					-- start_price: 開始価格
	3000.00, 					-- current_bid_price: 初期値として start_price と同額
	10000.00, 					-- reserve_price: 即決価格
	'2026-01-01 12:00:00' 		-- auction_end_time: 終了日時（テスト時は未来の日付を設定）
);

-- 入札データの投入（購入者 B が限定版フィギュアに入札）
-- 注意: item.current_bid_price と bid.bid_price は一致させておく必要があります
INSERT INTO bid (item_id, user_id, bid_price, created_at) -- user_idは入札者IDを指す前提
VALUES (
	(SELECT id FROM item WHERE name = '限定版フィギュア（オークション）'),
	(SELECT id FROM users WHERE email = 'buyerB@example.com'),
	3500.00,
	CURRENT_TIMESTAMP
);

-- 入札されたため、itemテーブルの current_bid_price を更新
UPDATE item SET current_bid_price = 3500.00
WHERE name = '限定版フィギュア（オークション）';

-- （任意）サンプル注文：コメントアウト例。必要時にコメント解除
-- INSERT INTO app_order (item_id, buyer_id, price, status)
-- VALUES (
-- (SELECT id FROM item WHERE name =
-- (SELECT id FROM users WHERE email =
-- 'Java プログラミング入門'),
-- 'buyerB@example.com'),
-- 1500.00,
-- '購入済'
-- );