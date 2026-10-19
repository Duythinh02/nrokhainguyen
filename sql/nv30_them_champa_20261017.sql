-- NV 30 "Thần Hủy Diệt thức tỉnh": thêm bước hạ Champa x2 sau Berus, trước Báo cáo Whis
-- Thứ tự bước theo ducvupro (PRIMARY KEY): 165 -> 166 (Berus) -> 208 (Champa) -> 209 (Whis)
SET NAMES utf8mb4;
START TRANSACTION;
UPDATE task_sub_template SET ducvupro = 209 WHERE ducvupro = 190 AND task_main_id = 30;
INSERT INTO task_sub_template (ducvupro, task_main_id, NAME, max_count, notify, npc_id, map, idmain)
VALUES (208, 30, 'Hạ Thần Hủy Diệt Champa', 2, 'Tiếp theo là Champa ở Trạm Huỷ Diệt Namec (đi qua Vách núi Moori)', -1, -1, 0);
UPDATE task_main_template SET detail = 'Thần Hủy Diệt Berus đã tỉnh giấc ở Trạm Huỷ Diệt Trái Đất (đi qua mép trái Vách núi Aru). Hạ ngài 2 lần, sau đó hạ Thần Hủy Diệt Champa 2 lần ở Trạm Huỷ Diệt Namec (đi qua Vách núi Moori) rồi đến Hành tinh Bill báo cáo với Whis.' WHERE id = 30;
COMMIT;

-- Hoàn tác:
-- DELETE FROM task_sub_template WHERE ducvupro = 208;
-- UPDATE task_sub_template SET ducvupro = 190 WHERE ducvupro = 209;
