-- Thẻ nhiệm vụ: dùng để mở khoá nhiệm vụ mới
-- Icon 17401 (data/icon/x1..x4/17401.png), loại 27 giống Coin Khai Nguyên (2691)
-- Cần restart server (item_template chỉ nạp lúc khởi động).
SET NAMES utf8mb4;
INSERT INTO item_template (id, TYPE, gender, NAME, description, level, icon_id, part, is_up_to_up, power_require,
                           gold, gem, head, body, leg, gold_sell, gem_sell, ruby, ruby_sell, TypeEvent, isGender)
VALUES (2692, 27, 3, 'Thẻ nhiệm vụ', 'Dùng để mở khoá nhiệm vụ mới', 1, 17401, -1, 1, 0,
        0, 0, -1, -1, -1, 0, 0, 0, 0, 0, -1);

-- Hoàn tác:
-- DELETE FROM item_template WHERE id = 2692;
