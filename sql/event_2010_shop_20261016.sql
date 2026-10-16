-- Sự kiện 20/10 (16/10/2026)
-- Shop 20_10_EVENT (tab 76) và shop đổi điểm 20_10_EVENT_2 (tab 111).
-- Shop chỉ được nạp lúc khởi động server, chạy file này lúc nào cũng được.
-- Bản sao trước khi sửa: C:\setup\backup\event2010_20261016\shop_tables_before.sql

START TRANSACTION;

-- ===== Tab 76: Shop sự kiện =====
-- Ẩn Hộp quà 20/10 bản thỏi vàng (trùng id với bản hồng ngọc, bấm mua luôn bị tính giá bản hồng ngọc)
UPDATE item_shop SET is_sell = 0 WHERE id = 1208;
-- Ẩn Rương VIP rồng: chưa có code mở, mua về không dùng được
UPDATE item_shop SET is_sell = 0 WHERE id = 1209;
-- Chậu sứ: 100 -> 10 hồng ngọc
UPDATE item_shop SET cost = 10, type_sell = 3 WHERE id = 1211;

-- Hạt mầm: 200.000 vàng (icon_spec 930 = vàng)
INSERT INTO item_shop (tab_id, temp_id, is_new, is_sell, type_sell, cost, icon_spec, create_time)
VALUES (76, 1095, 1, 1, 0, 200000, 930, '2026-10-16 10:00:02');
-- Thuốc tăng trưởng: 5 hồng ngọc (icon_spec 7743 = hồng ngọc)
INSERT INTO item_shop (tab_id, temp_id, is_new, is_sell, type_sell, cost, icon_spec, create_time)
VALUES (76, 1096, 1, 1, 3, 5, 7743, '2026-10-16 10:00:01');

-- ===== Tab 111: Đổi điểm 20/10 (cost = số điểm, icon_spec 10484 = Chậu hoa hồng xanh) =====
DELETE o FROM item_shop_option o JOIN item_shop i ON i.id = o.item_shop_id WHERE i.tab_id = 111;
DELETE FROM item_shop WHERE tab_id = 111;

INSERT INTO item_shop (tab_id, temp_id, is_new, is_sell, type_sell, cost, icon_spec, create_time) VALUES
(111, 1011, 1, 1, 3, 400, 10484, '2026-10-16 10:00:11'),
(111, 1363, 1, 1, 3, 350, 10484, '2026-10-16 10:00:10'),
(111, 1520, 1, 1, 3, 250, 10484, '2026-10-16 10:00:09'),
(111, 1519, 1, 1, 3, 250, 10484, '2026-10-16 10:00:08'),
(111, 1100, 1, 1, 3, 200, 10484, '2026-10-16 10:00:07'),
(111, 1515, 1, 1, 3, 150, 10484, '2026-10-16 10:00:06'),
(111, 1102, 1, 1, 3, 25, 10484, '2026-10-16 10:00:05'),
(111, 1143, 1, 1, 3, 15, 10484, '2026-10-16 10:00:04'),
(111, 1719, 1, 1, 3, 10, 10484, '2026-10-16 10:00:03'),
(111, 1150, 1, 1, 3, 5, 10484, '2026-10-16 10:00:02'),
(111, 1151, 1, 1, 3, 5, 10484, '2026-10-16 10:00:01'),
(111, 1152, 1, 1, 3, 5, 10484, '2026-10-16 10:00:00');

-- Chỉ số: 50 = sức đánh %, 77 = HP %, 103 = KI %, 14 = chí mạng, 94 = giáp %, 30 = không giao dịch, 93 = hạn dùng (ngày)
INSERT INTO item_shop_option (item_shop_id, option_id, param)
SELECT i.id, v.option_id, v.param FROM item_shop i JOIN (
          SELECT 1011 temp_id, 50 option_id, 20 param UNION ALL SELECT 1011, 77, 20 UNION ALL SELECT 1011, 103, 20 UNION ALL SELECT 1011, 30, 0 UNION ALL SELECT 1011, 93, 15
UNION ALL SELECT 1363, 50, 12 UNION ALL SELECT 1363, 77, 12 UNION ALL SELECT 1363, 103, 12 UNION ALL SELECT 1363, 30, 0 UNION ALL SELECT 1363, 93, 15
UNION ALL SELECT 1520, 50, 17 UNION ALL SELECT 1520, 77, 17 UNION ALL SELECT 1520, 103, 17 UNION ALL SELECT 1520, 14, 10 UNION ALL SELECT 1520, 30, 0 UNION ALL SELECT 1520, 93, 15
UNION ALL SELECT 1519, 50, 17 UNION ALL SELECT 1519, 77, 17 UNION ALL SELECT 1519, 103, 17 UNION ALL SELECT 1519, 94, 10 UNION ALL SELECT 1519, 30, 0 UNION ALL SELECT 1519, 93, 15
UNION ALL SELECT 1100, 50, 16 UNION ALL SELECT 1100, 77, 16 UNION ALL SELECT 1100, 103, 16 UNION ALL SELECT 1100, 94, 11 UNION ALL SELECT 1100, 30, 0 UNION ALL SELECT 1100, 93, 15
UNION ALL SELECT 1515, 50, 15 UNION ALL SELECT 1515, 77, 15 UNION ALL SELECT 1515, 103, 15 UNION ALL SELECT 1515, 30, 0 UNION ALL SELECT 1515, 93, 15
UNION ALL SELECT 1102, 30, 0 UNION ALL SELECT 1102, 93, 30
UNION ALL SELECT 1143, 30, 0
UNION ALL SELECT 1719, 30, 0 UNION ALL SELECT 1719, 93, 30
UNION ALL SELECT 1150, 30, 0 UNION ALL SELECT 1151, 30, 0 UNION ALL SELECT 1152, 30, 0
) v ON v.temp_id = i.temp_id
WHERE i.tab_id = 111;

COMMIT;
