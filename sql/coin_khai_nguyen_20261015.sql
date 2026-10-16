-- Coin Khai Nguyên: tiền tệ ký gửi, đổi tại Tori-Bot (1 điểm săn boss = 1 coin)
-- Icon 17400 (data/icon/x1..x4/17400.png), stack giống Thỏi vàng (457)

INSERT INTO item_template (id, TYPE, gender, NAME, description, level, icon_id, part, is_up_to_up, power_require,
                           gold, gem, head, body, leg, gold_sell, gem_sell, ruby, ruby_sell, TypeEvent, isGender)
VALUES (2691, 27, 3, 'Coin Khai Nguyên', 'Dùng để mua bán tại Ký gửi', 1, 17400, -1, 1, 0,
        0, 0, -1, -1, -1, 0, 0, 0, 0, 0, -1);

-- Giá bán bằng coin (-1 = tin đăng cũ bằng vàng/ngọc)
ALTER TABLE shop_ky_gui ADD COLUMN coin INT NOT NULL DEFAULT -1;

-- Hoàn tác:
-- DELETE FROM item_template WHERE id = 2691;
-- ALTER TABLE shop_ky_gui DROP COLUMN coin;
