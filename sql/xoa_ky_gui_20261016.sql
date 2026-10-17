-- Xóa toàn bộ vật phẩm ký gửi (kể cả tin cũ bằng vàng/ngọc và món đã bán chưa nhận tiền).
-- CHỈ CHẠY KHI SERVER ĐÃ TẮT, nếu không server sẽ ghi đè lại danh sách cũ từ RAM.
TRUNCATE TABLE shop_ky_gui;
