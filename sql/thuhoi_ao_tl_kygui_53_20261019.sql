-- Thu hồi Áo Thần Linh TD (555) lỗi chỉ số (mang chỉ số giày: KI 23 / 28) đang ký gửi của quanhai38 (player 1388), giá 15 coin.
-- CHỈ CHẠY KHI SERVER ĐÃ TẮT: siêu thị nằm trong bộ nhớ, khi lưu server xóa sạch bảng rồi ghi lại -> chạy lúc đang mở sẽ bị ghi đè.
-- Bản sao lưu dòng này: sql/backup_thuhoi_ao_tl_20261019.sql
DELETE FROM shop_ky_gui
WHERE id = 53 AND item_id = 555 AND player_id = 1388 AND isBuy = 0;

SELECT COUNT(*) AS con_lai FROM shop_ky_gui WHERE id = 53;
