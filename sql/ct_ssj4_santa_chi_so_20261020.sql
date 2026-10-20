-- CT Goku SSJ4 / CT Cađíc SSJ4 ở Santa (tab 18, giá 500 Coin Khai Nguyên): HP 30%, KI 30%, sức đánh 27%,
-- 4 dòng chỉ số ẩn (option 210: mở ngẫu nhiên khi mặc lần đầu, InventoryService.handleOption210).
DELETE o FROM item_shop_option o JOIN item_shop i ON i.id = o.item_shop_id
  WHERE i.tab_id = 18 AND i.temp_id IN (1553, 1693) AND i.type_sell = 6;
INSERT INTO item_shop_option (item_shop_id, option_id, param)
  SELECT i.id, v.option_id, v.param FROM item_shop i
  JOIN (SELECT 77 AS option_id, 30 AS param UNION ALL SELECT 103, 30 UNION ALL SELECT 50, 27 UNION ALL SELECT 210, 4) v
  WHERE i.tab_id = 18 AND i.temp_id IN (1553, 1693) AND i.type_sell = 6;

-- Hoàn tác (về bộ chỉ số trước: SĐ/HP/KI 30%, SĐ chí mạng 30%, chống lạnh): chạy lại sql/ct_ssj4_santa_20261020.sql phần option.
