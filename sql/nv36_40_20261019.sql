-- Nhiệm vụ 36 - 40 (sau NV 35 Baby/Gomah). Không có phần thưởng riêng, không giới hạn sát thương.
-- NV 36 trước đây là ô "bạn đã xong nhiệm vụ rồi": chuyển ô đó sang NV 41.
-- Thứ tự bước theo ducvupro (AUTO_INCREMENT), nên các bước được chèn theo đúng thứ tự.
-- Số thứ tự bước phải khớp với ConstTask.TASK_3x_y trong TaskService.
SET NAMES utf8mb4;
START TRANSACTION;

-- Ô kết thúc chuyển sang NV 41
INSERT INTO task_main_template (id, NAME, detail) VALUES (41, '..', '...');
UPDATE task_sub_template SET task_main_id = 41 WHERE ducvupro = 182 AND task_main_id = 36;

UPDATE task_main_template SET NAME = 'Băng hải tặc vũ trụ',
  detail = 'Băng hải tặc vũ trụ của Bojack đã đổ bộ xuống Trái Đất.\nĐạt 120 tỷ sức mạnh, hạ lần lượt Bujin, Kogu, Zangya, Bido,\nrồi hạ Bojack và hạ Siêu Bojack 2 lần (Rừng Bamboo, Rừng dương xỉ, Nam Kamê...).\nXong về Đảo Kame báo cáo với Quy Lão.'
  WHERE id = 36;
INSERT INTO task_main_template (id, NAME, detail) VALUES
(37, 'Dấu vết Death Beam', 'Fide Vàng đang gọi đám lính Death Beam tới.\nĐạt 130 tỷ sức mạnh, hạ lần lượt Death Beam 1 đến 5\n(Fide Vàng gọi ra ở Đông Karin, 21h - 22h),\nrồi hạ Chiler 2 lần ở Khu hang động, Rừng nguyên thủy.\nXong đến Hành tinh Bill báo cáo với Whis.'),
(38, 'Rồng ác Tà Long', 'Năng lượng âm của ngọc rồng đã sinh ra Rồng ác.\nĐạt 140 tỷ sức mạnh, hạ lần lượt Khỉ đột, Nhật thần, Nguyệt thần\n(Nhật thần và Nguyệt thần đi cùng nhau, hạ một người thì người kia bỏ đi),\nrồi hạ Nhất Tinh Long 3 lần ở Làng Kakarot.\nXong lên Thần điện báo cáo với Thượng Đế.'),
(39, 'Kẻ sống sót cuối cùng', 'Kẻ sống sót cuối cùng của tộc Cereal đang gây chiến.\nĐạt 150 tỷ sức mạnh, hạ lần lượt phân thân Gas, Oil, Elec, Macki ở Thành Phố Sugar,\nrồi hạ phân thân Granola ở Hành Tinh Cereal.'),
(40, 'Hợp thể tối thượng', 'Gogeta và Omega xuất hiện!\nĐạt 170 tỷ sức mạnh, hạ Omega 2 lần rồi hạ Gogeta 2 lần\n(hai kẻ đi cùng nhau, hạ một kẻ thì kẻ kia bỏ đi),\nsau đó hạ Pikkon ở Rừng Bamboo, Rừng dương xỉ, Nam Kamê.\nXong về Đảo Kame báo cáo với Quy Lão.');

INSERT INTO task_sub_template (task_main_id, NAME, max_count, notify, npc_id, map, idmain) VALUES
-- NV 36: TASK_36_0 .. TASK_36_7
(36, 'Đạt 120 tỷ sức mạnh', 1, 'Hãy đạt 120 tỷ sức mạnh', -1, -1, 0),
(36, 'Hạ Bujin', 1, 'Tiếp theo là Bujin, đi cùng băng Bojack (Rừng Bamboo, Rừng dương xỉ, Nam Kamê...)', -1, -1, 0),
(36, 'Hạ Kogu', 1, 'Tiếp theo là Kogu', -1, -1, 0),
(36, 'Hạ Zangya', 1, 'Tiếp theo là Zangya', -1, -1, 0),
(36, 'Hạ Bido', 1, 'Tiếp theo là Bido', -1, -1, 0),
(36, 'Hạ Bojack', 1, 'Tiếp theo là Bojack', -1, -1, 0),
(36, 'Hạ Siêu Bojack', 2, 'Tiếp theo là hạ Siêu Bojack 2 lần', -1, -1, 0),
(36, 'Báo cáo với Quy Lão', 1, 'Hãy về Đảo Kame báo cáo với Quy Lão', 13, 5, 0),
-- NV 37: TASK_37_0 .. TASK_37_7
(37, 'Đạt 130 tỷ sức mạnh', 1, 'Hãy đạt 130 tỷ sức mạnh', -1, -1, 0),
(37, 'Hạ Death Beam 1', 1, 'Tiếp theo là Death Beam 1, do Fide Vàng gọi ra ở Đông Karin (21h - 22h)', -1, 6, 0),
(37, 'Hạ Death Beam 2', 1, 'Tiếp theo là Death Beam 2', -1, 6, 0),
(37, 'Hạ Death Beam 3', 1, 'Tiếp theo là Death Beam 3', -1, 6, 0),
(37, 'Hạ Death Beam 4', 1, 'Tiếp theo là Death Beam 4', -1, 6, 0),
(37, 'Hạ Death Beam 5', 1, 'Tiếp theo là Death Beam 5', -1, 6, 0),
(37, 'Hạ Chiler', 2, 'Tiếp theo là hạ Chiler 2 lần ở Khu hang động, Rừng nguyên thủy', -1, -1, 0),
(37, 'Báo cáo với Whis', 1, 'Hãy đến Hành tinh Bill báo cáo với Whis', 56, 154, 0),
-- NV 38: TASK_38_0 .. TASK_38_5
(38, 'Đạt 140 tỷ sức mạnh', 1, 'Hãy đạt 140 tỷ sức mạnh', -1, -1, 0),
(38, 'Hạ Khỉ đột', 1, 'Tiếp theo là Khỉ đột, xuất hiện ở các làng, đồi của 3 hành tinh', -1, -1, 0),
(38, 'Hạ Nhật thần', 1, 'Tiếp theo là Nhật thần (đi cùng Nguyệt thần)', -1, -1, 0),
(38, 'Hạ Nguyệt thần', 1, 'Tiếp theo là Nguyệt thần (đi cùng Nhật thần)', -1, -1, 0),
(38, 'Hạ Nhất Tinh Long', 3, 'Tiếp theo là hạ Nhất Tinh Long 3 lần ở Làng Kakarot khu 0', -1, 14, 0),
(38, 'Báo cáo với Thượng Đế', 1, 'Hãy lên Thần điện báo cáo với Thượng Đế', 19, 45, 0),
-- NV 39: TASK_39_0 .. TASK_39_5 (hạ Granola xong là hoàn thành)
(39, 'Đạt 150 tỷ sức mạnh', 1, 'Hãy đạt 150 tỷ sức mạnh', -1, -1, 0),
(39, 'Hạ phân thân Gas', 1, 'Tiếp theo là phân thân Gas ở Thành Phố Sugar', -1, 199, 0),
(39, 'Hạ phân thân Oil', 1, 'Tiếp theo là phân thân Oil ở Thành Phố Sugar', -1, 199, 0),
(39, 'Hạ phân thân Elec', 1, 'Tiếp theo là phân thân Elec ở Thành Phố Sugar', -1, 199, 0),
(39, 'Hạ phân thân Macki', 1, 'Tiếp theo là phân thân Macki ở Thành Phố Sugar', -1, 199, 0),
(39, 'Hạ phân thân Granola', 1, 'Tiếp theo là phân thân Granola ở Hành Tinh Cereal', -1, 194, 0),
-- NV 40: TASK_40_0 .. TASK_40_4
(40, 'Đạt 170 tỷ sức mạnh', 1, 'Hãy đạt 170 tỷ sức mạnh', -1, -1, 0),
(40, 'Hạ Omega', 2, 'Tiếp theo là hạ Omega 2 lần (đi cùng Gogeta)', -1, -1, 0),
(40, 'Hạ Gogeta', 2, 'Tiếp theo là hạ Gogeta 2 lần (đi cùng Omega)', -1, -1, 0),
(40, 'Hạ Pikkon', 1, 'Tiếp theo là Pikkon ở Rừng Bamboo, Rừng dương xỉ, Nam Kamê', -1, -1, 0),
(40, 'Báo cáo với Quy Lão', 1, 'Hãy về Đảo Kame báo cáo với Quy Lão', 13, 5, 0);

COMMIT;

-- Hoàn tác:
-- DELETE FROM task_sub_template WHERE task_main_id BETWEEN 36 AND 40;
-- UPDATE task_sub_template SET task_main_id = 36 WHERE ducvupro = 182;
-- DELETE FROM task_main_template WHERE id BETWEEN 37 AND 41;
-- UPDATE task_main_template SET NAME = '..', detail = '...' WHERE id = 36;

-- ===== Bổ sung 2026-10-19: NV 37 thêm bước "Hạ Gomah, nhặt Nhẫn thời không" (sau Death Beam 5, trước Chiler) =====
-- NV 37 sau khi sửa: 0 sức mạnh, 1-5 Death Beam, 6 Nhẫn thời không (TASK_37_6), 7 Chiler (TASK_37_7), 8 Whis (TASK_37_8)
START TRANSACTION;
UPDATE task_sub_template SET ducvupro = 244 WHERE ducvupro = 225 AND task_main_id = 37; -- Báo cáo Whis
UPDATE task_sub_template SET ducvupro = 243 WHERE ducvupro = 224 AND task_main_id = 37; -- Hạ Chiler
INSERT INTO task_sub_template (ducvupro, task_main_id, NAME, max_count, notify, npc_id, map, idmain)
VALUES (224, 37, 'Hạ Gomah, nhặt Nhẫn thời không', 1, 'Tiếp theo là hạ Gomah ở Hành Tinh Cereal, Mảnh Đất Bỏ Hoang để nhặt Nhẫn thời không', -1, -1, 0);
UPDATE task_main_template SET detail = 'Fide Vàng đang gọi đám lính Death Beam tới.\nĐạt 130 tỷ sức mạnh, hạ lần lượt Death Beam 1 đến 5\n(Fide Vàng gọi ra ở Đông Karin, 21h - 22h),\nhạ Gomah ở Hành Tinh Cereal để nhặt Nhẫn thời không,\nrồi hạ Chiler 2 lần ở Khu hang động, Rừng nguyên thủy.\nXong đến Hành tinh Bill báo cáo với Whis.' WHERE id = 37;
COMMIT;
-- Hoàn tác phần bổ sung:
-- DELETE FROM task_sub_template WHERE ducvupro = 224 AND task_main_id = 37;
-- UPDATE task_sub_template SET ducvupro = 224 WHERE ducvupro = 243; UPDATE task_sub_template SET ducvupro = 225 WHERE ducvupro = 244;
