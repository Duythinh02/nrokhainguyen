-- Giftcode sự kiện 20/10: "2010" (mỗi nhân vật nhập 1 lần, hết hạn 01/11/2026)
--   1095 Hạt mầm             x3
--   1093 Đất trồng cây       x20
--   1094 Phân bón            x10
--   1096 Thuốc tăng trưởng   x2
--   1097 Chậu sứ             x1
--   1719 Hộp quà 20/10       x1 (hạn 30 ngày)
-- Nguyên liệu KHÔNG gắn option để gộp chung ô với nguyên liệu nhặt từ quái
-- (đồ khác option nằm ô riêng, công thức trồng hoa chỉ đếm 1 ô). Hộp quà không giao dịch được.
-- Cần 6 ô hành trang trống. Sau khi chạy: Panel -> Reload GiftCode (không cần khởi động lại).

INSERT INTO giftcode (code, count_left, item, `option`, listIdPlayers, datecreate, expired, detail)
SELECT '2010', 999999999,
  '[{"id":1095,"quantity":3},{"id":1093,"quantity":20},{"id":1094,"quantity":10},{"id":1096,"quantity":2},{"id":1097,"quantity":1},{"id":1719,"quantity":1}]',
  '[]', '[]', NOW(), '2026-11-01 00:00:00',
  '[{"id":1095,"quantity":3,"options":[]},{"id":1093,"quantity":20,"options":[]},{"id":1094,"quantity":10,"options":[]},{"id":1096,"quantity":2,"options":[]},{"id":1097,"quantity":1,"options":[]},{"id":1719,"quantity":1,"options":[{"id":30,"param":0},{"id":93,"param":30}]}]'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM giftcode WHERE code = '2010');

SELECT id, code, count_left, expired FROM giftcode WHERE code = '2010';
