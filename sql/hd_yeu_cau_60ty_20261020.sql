-- Đồ Hủy Diệt (650 - 662) ở shop: yêu cầu sức mạnh 80 tỷ -> 60 tỷ (khớp InventoryService.POWER_DO_HUY_DIET).
-- Shop nạp lúc khởi động server nên có hiệu lực sau lần restart tới.
UPDATE item_shop_option o JOIN item_shop i ON i.id = o.item_shop_id
SET o.param = 60
WHERE i.temp_id BETWEEN 650 AND 662 AND o.option_id = 21 AND o.param = 80;

-- Hoàn tác:
-- UPDATE item_shop_option o JOIN item_shop i ON i.id = o.item_shop_id SET o.param = 80 WHERE i.temp_id BETWEEN 650 AND 662 AND o.option_id = 21 AND o.param = 60;
