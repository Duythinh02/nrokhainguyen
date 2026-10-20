-- Tháo Găng Hủy Diệt (659) khỏi đệ tử chưa đủ 80 tỷ, trả về ô 55 hành trang chủ: ngocr0ng (1391), laze1hp (1505).
-- quanhai38 (1388) đang online nên chưa xử lý. Chỉ chạy khi nhân vật OFFLINE (server ghi đè khi lưu).
-- Sao lưu: sql/backup_thao_gang_hd_de_20261019.sql
SET NAMES utf8mb4;
UPDATE player p JOIN account a ON a.id = p.account_id
SET p.items_bag = JSON_SET(p.items_bag, '$[55]', JSON_UNQUOTE(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[2]')), '$[2]'))),
    p.pet = JSON_SET(p.pet, '$[2]', REPLACE(CAST(JSON_SET(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[2]')), '$[2]',
              CONCAT('[-1,0,"[]",', JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[2]')), '$[2]')), '$[3]'), ']')) AS CHAR), '", "', '","'))
WHERE p.id IN (1391, 1505)
  AND a.last_time_logout > a.last_time_login
  AND JSON_UNQUOTE(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[2]')), '$[2]')) LIKE '[659,%'
  AND JSON_UNQUOTE(JSON_EXTRACT(p.items_bag, '$[55]')) LIKE '[-1,%';
SELECT ROW_COUNT() so_dong_da_sua;

-- ===== quanhai38 (1388): ĐÃ CHẠY 19/10 17:17 lúc nhân vật offline (sao lưu: sql/backup_thao_gang_hd_de_quanhai38_20261019.sql) =====
-- UPDATE player p JOIN account a ON a.id = p.account_id
-- SET p.items_bag = JSON_SET(p.items_bag, '$[55]', JSON_UNQUOTE(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[2]')), '$[2]'))),
--     p.pet = JSON_SET(p.pet, '$[2]', REPLACE(CAST(JSON_SET(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[2]')), '$[2]',
--               CONCAT('[-1,0,"[]",', JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[2]')), '$[2]')), '$[3]'), ']')) AS CHAR), '", "', '","'))
-- WHERE p.id = 1388
--   AND JSON_UNQUOTE(JSON_EXTRACT(JSON_UNQUOTE(JSON_EXTRACT(p.pet, '$[2]')), '$[2]')) LIKE '[659,%'
--   AND JSON_UNQUOTE(JSON_EXTRACT(p.items_bag, '$[55]')) LIKE '[-1,%';
