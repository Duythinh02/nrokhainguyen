-- trumxayda (id 1375): nội tại Tự phát nổ (id 19) giảm 80% thời gian hồi chiêu (bình thường tối đa 65%)
-- Cũ: [17,19,0,0,false,14,120000,1791979629126] (Antomic +19% sát thương)
-- Chỉ chạy khi nhân vật offline, nếu không server sẽ ghi đè khi lưu.
UPDATE player p JOIN account a ON a.id = p.account_id
SET p.data_intrinsic = JSON_SET(p.data_intrinsic, '$[0]', 19, '$[1]', 80, '$[2]', 0)
WHERE p.id = 1375 AND p.name = 'trumxayda' AND a.last_time_logout >= a.last_time_login;

-- Hoàn tác:
-- UPDATE player SET data_intrinsic = '[17,19,0,0,false,14,120000,1791979629126]' WHERE id = 1375;
