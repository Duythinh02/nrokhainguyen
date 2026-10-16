-- Mở lại giftcode "tanthu" cho tdtssss (đã bị đánh dấu dùng nhưng không nhận được quà do lỗi reload giftcode)
-- CHẠY KHI SERVER ĐANG TẮT (hoặc nhân vật offline), nếu không server sẽ ghi đè khi lưu nhân vật.
UPDATE player
SET giftcode = JSON_REMOVE(giftcode, JSON_UNQUOTE(JSON_SEARCH(giftcode, 'one', 'tanthu')))
WHERE id = 1392 AND name = 'tdtssss' AND JSON_SEARCH(giftcode, 'one', 'tanthu') IS NOT NULL;

SELECT id, name, giftcode FROM player WHERE id = 1392;
