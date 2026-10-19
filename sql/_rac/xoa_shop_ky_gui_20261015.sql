-- Xóa toàn bộ vật phẩm shop ký gửi (không hoàn trả người bán). CHỈ chạy khi server ĐÃ TẮT,
-- vì server giữ danh sách trong bộ nhớ và sẽ ghi đè lại khi lưu.
-- Sao lưu: sql/backup_shop_ky_gui_20261015.sql (hoàn tác: chạy file sao lưu đó)
DELETE FROM shop_ky_gui;
