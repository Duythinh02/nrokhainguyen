-- Bông tai VIP (2026-10-20): hợp thể Gogeta với người chơi khác (PlayerFusionService).
-- id phải liền sau id lớn nhất (Manager.ITEM_TEMPLATES đọc theo thứ tự id). Icon: data/icon/x1..x4/32760.png
-- Bán ở shop Uron (tab Phụ kiện, tab_id 19) giá 500 điểm săn boss (type_sell 5 = COST_BOSS_POINT, ShopService).
INSERT INTO item_template (id, TYPE, gender, NAME, description, level, icon_id, part, is_up_to_up, power_require,
  gold, gem, head, body, leg, gold_sell, gem_sell, ruby, ruby_sell, TypeEvent, isGender)
VALUES (2693, 27, 3, 'Bông tai VIP',
  'Hợp thể Gogeta với người chơi khác. Giá Uron: 500 Coin Khai Nguyên',
  1, 32760, -1, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, -1);

INSERT INTO item_shop (tab_id, temp_id, is_new, is_sell, type_sell, cost, icon_spec, costgold)
VALUES (19, 2693, 1, 1, 5, 500, 0, 0);

-- Hoàn tác:
-- DELETE FROM item_shop WHERE temp_id = 2693; DELETE FROM item_template WHERE id = 2693;
