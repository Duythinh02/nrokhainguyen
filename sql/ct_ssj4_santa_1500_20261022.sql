-- CT Goku SSJ4 / CT Cađíc SSJ4 ở shop SANTA_COIN (tab 113): thêm +15% sức đánh chí mạng (option 5), giá 1500 Coin Khai Nguyên.
UPDATE item_shop SET cost = 1500 WHERE tab_id = 113 AND temp_id IN (1553, 1693);
DELETE o FROM item_shop_option o JOIN item_shop i ON i.id = o.item_shop_id
  WHERE i.tab_id = 113 AND i.temp_id IN (1553, 1693) AND o.option_id = 5;
INSERT INTO item_shop_option (item_shop_id, option_id, param)
  SELECT id, 5, 15 FROM item_shop WHERE tab_id = 113 AND temp_id IN (1553, 1693);
UPDATE item_template SET description = 'Mặc cùng CT Cađíc SSJ4 để hợp thể Gogeta. Santa: 1500 Coin Khai Nguyên' WHERE id = 1553;
UPDATE item_template SET description = 'Mặc cùng CT Goku SSJ4 để hợp thể Gogeta. Santa: 1500 Coin Khai Nguyên' WHERE id = 1693;

-- Hoàn tác:
-- UPDATE item_shop SET cost = 500 WHERE tab_id = 113 AND temp_id IN (1553, 1693);
-- DELETE o FROM item_shop_option o JOIN item_shop i ON i.id = o.item_shop_id WHERE i.tab_id = 113 AND i.temp_id IN (1553, 1693) AND o.option_id = 5;
