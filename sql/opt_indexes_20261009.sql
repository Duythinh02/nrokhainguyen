-- Tối ưu DB nro_merge: thêm index cho các truy vấn hay chạy mà đang phải quét cả bảng.
-- An toàn để chạy nhiều lần (IF NOT EXISTS). Không đổi dữ liệu.

-- Tìm nhân vật theo tên: đăng nhập, kiểm tra trùng tên khi tạo/đổi tên, bot tạo tên, tìm bạn
-- (MrBlue.java, Input.java, Controller.java, BotNguoiChoiManager.java)
CREATE INDEX IF NOT EXISTS idx_player_name ON player (name);

-- Đại hội võ thuật siêu hạng: SuperRankDAO tìm theo player_id mỗi lần lưu nhân vật, và theo rank khi xếp hạng
CREATE INDEX IF NOT EXISTS idx_super_rank_player_id ON super_rank (player_id);
CREATE INDEX IF NOT EXISTS idx_super_rank_rank ON super_rank (`rank`);

-- Kiểm tra
SHOW INDEX FROM player;
SHOW INDEX FROM super_rank;
