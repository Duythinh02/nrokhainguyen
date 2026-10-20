-- Bông tai VIP: giá 500 Coin Khai Nguyên (type_sell 6 = COST_COIN_KHAI_NGUYEN, ShopService); cả 2 người phải có bông tai.
UPDATE item_shop SET type_sell = 6, cost = 500 WHERE temp_id = 2693;
UPDATE item_template SET description =
  'Hợp thể Gogeta với người chơi khác. Giá Uron: 500 Coin Khai Nguyên'
  WHERE id = 2693;

-- Hoàn tác (về giá 500 điểm săn boss):
-- UPDATE item_shop SET type_sell = 5, cost = 500 WHERE temp_id = 2693;
