-- Shop Coin Khai Nguyên (2026-10-20): shop đặc biệt (type_shop 3 = SPEC_SHOP) hiện giá kèm icon Coin Khai Nguyên
-- (icon_spec 17400 = icon item 2691). Mở từ nút "Cửa hàng Coin" ở Uron / Santa. Trừ coin: ShopService.subIemByItemShop.
-- Chuyển Bông tai VIP (Uron) và CT Goku SSJ4 / CT Cađíc SSJ4 (Santa) từ shop thường (giá hiện 0) sang đây.
INSERT INTO shop (npc_id, tag_name, type_shop) VALUES (16, 'URON_COIN', 3);
SET @uron = LAST_INSERT_ID();
INSERT INTO tab_shop (shop_id, NAME) VALUES (@uron, 'Coin<>Khai Nguyên');
SET @uronTab = LAST_INSERT_ID();
INSERT INTO item_shop (tab_id, temp_id, is_new, is_sell, type_sell, cost, icon_spec, costgold)
  VALUES (@uronTab, 2693, 1, 1, 0, 500, 17400, 0);

INSERT INTO shop (npc_id, tag_name, type_shop) VALUES (39, 'SANTA_COIN', 3);
SET @santa = LAST_INSERT_ID();
INSERT INTO tab_shop (shop_id, NAME) VALUES (@santa, 'Coin<>Khai Nguyên');
SET @santaTab = LAST_INSERT_ID();
-- Giữ nguyên chỉ số: chuyển luôn 2 dòng cải trang (kèm option) sang tab mới
UPDATE item_shop SET tab_id = @santaTab, type_sell = 0, icon_spec = 17400
  WHERE tab_id = 18 AND temp_id IN (1553, 1693) AND type_sell = 6;

-- Gỡ Bông tai VIP khỏi shop thường của Uron
DELETE o FROM item_shop_option o JOIN item_shop i ON i.id = o.item_shop_id WHERE i.temp_id = 2693 AND i.type_sell = 6;
DELETE FROM item_shop WHERE temp_id = 2693 AND type_sell = 6;

-- Hoàn tác: xoá 2 shop URON_COIN / SANTA_COIN (và tab, item của chúng), chạy lại các file bong_tai_vip / ct_ssj4.
