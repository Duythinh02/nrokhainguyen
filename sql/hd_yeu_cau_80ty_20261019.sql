-- Đồ Hủy Diệt bán ở shop Bill (shop 11, tab 21-23) thiếu option 21 (yêu cầu sức mạnh) -> thêm 21 = 80 (80 tỷ).
-- Server cũng chặn trong code (InventoryService.getPowerRequire) cho cả đồ cũ đã mua. Cần restart (shop nạp lúc khởi động).
INSERT INTO item_shop_option (item_shop_id, option_id, param)
SELECT i.id, 21, 80 FROM item_shop i
WHERE i.temp_id BETWEEN 650 AND 662
  AND NOT EXISTS (SELECT 1 FROM item_shop_option o WHERE o.item_shop_id = i.id AND o.option_id = 21);

-- Hoàn tác:
-- DELETE o FROM item_shop_option o JOIN item_shop i ON i.id = o.item_shop_id WHERE i.temp_id BETWEEN 650 AND 662 AND o.option_id = 21 AND o.param = 80;
