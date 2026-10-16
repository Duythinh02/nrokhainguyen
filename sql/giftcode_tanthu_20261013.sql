-- Giftcode tân thủ: "tanthu" (mỗi nhân vật nhập 1 lần, không giới hạn số lượt)
--   -2   Ngọc xanh            x1.000.000
--   457  Thỏi vàng            x100.000
--   14   Ngọc Rồng 1 sao      x20
--   220-224 Đá nâng cấp trang bị (Lục bảo, Saphia, Ruby, Titan, Thạch anh tím) x9999 mỗi loại
--   987  Đá bảo vệ            x9999
-- count_left = 999999999 (không dùng -1 vì nút Reload GiftCode ở admin panel không quy đổi -1 → code báo "đã hết")
-- Cần 9 ô hành trang trống khi nhập.

INSERT INTO giftcode (code, count_left, item, `option`, listIdPlayers, datecreate, expired, detail)
SELECT 'tanthu', 999999999,
  '[{"id":-2,"quantity":1000000},{"id":457,"quantity":100000},{"id":14,"quantity":20},{"id":220,"quantity":9999},{"id":221,"quantity":9999},{"id":222,"quantity":9999},{"id":223,"quantity":9999},{"id":224,"quantity":9999},{"id":987,"quantity":9999}]',
  '[]', '[]', NOW(), '2030-12-31 23:59:59',
  '[{"id":-2,"quantity":1000000,"options":[]},{"id":457,"quantity":100000,"options":[]},{"id":14,"quantity":20,"options":[]},{"id":220,"quantity":9999,"options":[]},{"id":221,"quantity":9999,"options":[]},{"id":222,"quantity":9999,"options":[]},{"id":223,"quantity":9999,"options":[]},{"id":224,"quantity":9999,"options":[]},{"id":987,"quantity":9999,"options":[]}]'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM giftcode WHERE code = 'tanthu');

SELECT id, code, count_left, expired FROM giftcode WHERE code = 'tanthu';
