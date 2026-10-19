-- Sửa danh hiệu "Kẻ thao túng sói" (item 1286) và chỉ số danh hiệu KOL (item 1297)
-- Sao lưu trước khi chạy:
--   mysqldump -uthinhduy07 -p nro_merge data_badges task_badges_template > backup_badges_20261019.sql
-- Cần restart server sau khi chạy (data_badges / task_badges_template chỉ nạp lúc khởi động).

-- 1) Sói chưa có dòng trong data_badges -> Santa luôn báo "chưa mở khóa". idEffect 215 = part của item 1286.
INSERT INTO data_badges (idEffect, idItem, NAME, Options)
SELECT 215, 1286, 'Kẻ thao túng sói', '[{"param":5,"id":50},{"param":5,"id":77},{"param":5,"id":103},{"param":30,"id":93}]'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM data_badges WHERE idItem = 1286);

-- 2) Nhiệm vụ 7 đang thưởng 1286 (id item) thay vì idEffect 215
UPDATE task_badges_template SET idBadgesReward = 215 WHERE id = 7;

-- 3) KOL bị đảo id/param: {"param":103,"id":10} -> KI +10% (option 103)
UPDATE data_badges SET Options = '[{"param":10,"id":50},{"param":10,"id":77},{"param":10,"id":103},{"param":30,"id":93}]'
WHERE idItem = 1297;

SELECT * FROM data_badges WHERE idItem IN (1286, 1297);
SELECT * FROM task_badges_template WHERE id IN (7, 18);
