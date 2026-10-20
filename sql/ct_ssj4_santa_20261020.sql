-- CT Goku SSJ4 (1553) và CT Cađíc SSJ4 (1693) bán ở Santa (Đảo Kame), tab Cải Trang (tab_id 18),
-- giá 500 Coin Khai Nguyên (type_sell 6 = COST_COIN_KHAI_NGUYEN, ShopService). Shop nạp lúc khởi động.
-- Chỉ số giống bản SHOP_VIP: SĐ 30%, HP 30%, KI 30%, +30% SĐ chí mạng, không ảnh hưởng bởi cái lạnh.
INSERT INTO item_shop (tab_id, temp_id, is_new, is_sell, type_sell, cost, icon_spec, costgold) VALUES (18, 1553, 1, 1, 6, 500, 0, 0);
SET @goku = LAST_INSERT_ID();
INSERT INTO item_shop_option (item_shop_id, option_id, param) VALUES
  (@goku, 50, 30), (@goku, 77, 30), (@goku, 103, 30), (@goku, 5, 30), (@goku, 106, 1);

INSERT INTO item_shop (tab_id, temp_id, is_new, is_sell, type_sell, cost, icon_spec, costgold) VALUES (18, 1693, 1, 1, 6, 500, 0, 0);
SET @cadic = LAST_INSERT_ID();
INSERT INTO item_shop_option (item_shop_id, option_id, param) VALUES
  (@cadic, 50, 30), (@cadic, 77, 30), (@cadic, 103, 30), (@cadic, 5, 30), (@cadic, 106, 1);

-- Hoàn tác:
-- DELETE o FROM item_shop_option o JOIN item_shop i ON i.id = o.item_shop_id WHERE i.tab_id = 18 AND i.temp_id IN (1553, 1693) AND i.type_sell = 6;
-- DELETE FROM item_shop WHERE tab_id = 18 AND temp_id IN (1553, 1693) AND type_sell = 6;
