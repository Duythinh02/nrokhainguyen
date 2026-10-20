-- Sư đồ (2026-10-20): mỗi đệ tử 1 sư phụ, mỗi sư phụ tối đa 3 đệ tử (SuDoService).
-- milestone: số mốc sức mạnh đệ tử đã đạt (thưởng điểm săn boss cho sư phụ); = số mốc tối đa là đã xuất sư.
-- pending_points: điểm thưởng chờ sư phụ nhận khi đăng nhập (lúc đệ tử đạt mốc mà sư phụ offline).
CREATE TABLE IF NOT EXISTS su_do (
  disciple_id INT NOT NULL PRIMARY KEY,
  master_id INT NOT NULL,
  milestone TINYINT NOT NULL DEFAULT 0,
  pending_points INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_master (master_id)
);

-- Hoàn tác:
-- DROP TABLE su_do;
