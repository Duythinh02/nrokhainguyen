-- Cân bằng NV 36 - 40 (19/10/2026): số lần hạ boss + câu chữ. Máu boss nằm trong code (QuestBossHp.java).
-- Cần restart server (task_*_template chỉ nạp lúc khởi động).
SET NAMES utf8mb4;
START TRANSACTION;

-- NV 36: Bujin, Kogu, Zangya, Bido, Bojack x2; Siêu Bojack x3
UPDATE task_sub_template SET max_count = 2, notify = 'Tiếp theo là hạ Bujin 2 lần, đi cùng băng Bojack (Rừng nấm, Rừng xương, Đảo Kamê, Đông Karin, Rừng Bamboo, Rừng dương xỉ, Nam Kamê, Đảo Bulông)' WHERE ducvupro = 211;
UPDATE task_sub_template SET max_count = 2, notify = 'Tiếp theo là hạ Kogu 2 lần' WHERE ducvupro = 212;
UPDATE task_sub_template SET max_count = 2, notify = 'Tiếp theo là hạ Zangya 2 lần' WHERE ducvupro = 213;
UPDATE task_sub_template SET max_count = 2, notify = 'Tiếp theo là hạ Bido 2 lần' WHERE ducvupro = 214;
UPDATE task_sub_template SET max_count = 2, notify = 'Tiếp theo là hạ Bojack 2 lần' WHERE ducvupro = 215;
UPDATE task_sub_template SET max_count = 3, notify = 'Tiếp theo là hạ Siêu Bojack 3 lần' WHERE ducvupro = 216;
UPDATE task_main_template SET detail = 'Băng hải tặc vũ trụ của Bojack đã đổ bộ xuống Trái Đất.\nĐạt 120 tỷ sức mạnh, dùng Thẻ nhiệm vụ để mở khoá (hạ Quy Lão ở Đảo Kame để nhận thẻ),\nrồi hạ lần lượt Bujin, Kogu, Zangya, Bido, Bojack mỗi tên 2 lần và Siêu Bojack 3 lần\n(Rừng nấm, Rừng xương, Đảo Kamê, Đông Karin, Rừng Bamboo, Rừng dương xỉ, Nam Kamê, Đảo Bulông).\nXong về Đảo Kame báo cáo với Quy Lão.' WHERE id = 36;

-- NV 37: Chiler x5 (nhẫn Gomah ~1/15, trong code Gomah.java)
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Gomah ở Hành Tinh Cereal, Mảnh Đất Bỏ Hoang để nhặt Nhẫn thời không (rất hiếm, khoảng 15 con mới rơi 1 nhẫn)' WHERE ducvupro = 224;
UPDATE task_sub_template SET max_count = 5, notify = 'Tiếp theo là hạ Chiler 5 lần ở Khu hang động, Rừng nguyên thủy' WHERE ducvupro = 243;
UPDATE task_main_template SET detail = 'Fide Vàng đang gọi đám lính Death Beam tới.\nĐạt 130 tỷ sức mạnh, hạ lần lượt Death Beam 1 đến 5\n(Fide Vàng gọi ra ở Đông Karin, 21h - 22h),\nhạ Gomah ở Hành Tinh Cereal để nhặt Nhẫn thời không (rất hiếm),\nrồi hạ Chiler 5 lần ở Khu hang động, Rừng nguyên thủy.\nXong đến Hành tinh Bill báo cáo với Whis.' WHERE id = 37;

-- NV 38: Nhất Tinh Long x5
UPDATE task_sub_template SET max_count = 5, notify = 'Tiếp theo là hạ Nhất Tinh Long 5 lần ở Làng Kakarot khu 0' WHERE ducvupro = 230;
UPDATE task_main_template SET detail = 'Năng lượng âm của ngọc rồng đã sinh ra Rồng ác.\nĐạt 140 tỷ sức mạnh, hạ lần lượt Khỉ đột, Nhật thần, Nguyệt thần\n(Nhật thần và Nguyệt thần đi cùng nhau, hạ một người thì người kia bỏ đi),\nrồi hạ Nhất Tinh Long 5 lần ở Làng Kakarot.\nXong lên Thần điện báo cáo với Thượng Đế.' WHERE id = 38;

-- NV 39: mỗi phân thân x2
UPDATE task_sub_template SET max_count = 2, notify = 'Tiếp theo là hạ phân thân Gas 2 lần ở Thành Phố Sugar' WHERE ducvupro = 233;
UPDATE task_sub_template SET max_count = 2, notify = 'Tiếp theo là hạ phân thân Oil 2 lần ở Thành Phố Sugar' WHERE ducvupro = 234;
UPDATE task_sub_template SET max_count = 2, notify = 'Tiếp theo là hạ phân thân Elec 2 lần ở Thành Phố Sugar' WHERE ducvupro = 235;
UPDATE task_sub_template SET max_count = 2, notify = 'Tiếp theo là hạ phân thân Macki 2 lần ở Thành Phố Sugar' WHERE ducvupro = 236;
UPDATE task_sub_template SET max_count = 2, notify = 'Tiếp theo là hạ phân thân Granola 2 lần ở Hành Tinh Cereal' WHERE ducvupro = 237;
UPDATE task_main_template SET detail = 'Kẻ sống sót cuối cùng của tộc Cereal đang gây chiến.\nĐạt 150 tỷ sức mạnh, hạ lần lượt phân thân Gas, Oil, Elec, Macki ở Thành Phố Sugar,\nrồi hạ phân thân Granola ở Hành Tinh Cereal, mỗi phân thân 2 lần.' WHERE id = 39;

-- NV 40: Omega x10, Gogeta x10, Pikkon x5
UPDATE task_sub_template SET max_count = 10, notify = 'Tiếp theo là hạ Omega 10 lần (đi cùng Gogeta)' WHERE ducvupro = 239;
UPDATE task_sub_template SET max_count = 10, notify = 'Tiếp theo là hạ Gogeta 10 lần (đi cùng Omega)' WHERE ducvupro = 240;
UPDATE task_sub_template SET max_count = 5, notify = 'Tiếp theo là hạ Pikkon 5 lần ở Rừng Bamboo, Rừng dương xỉ, Nam Kamê' WHERE ducvupro = 241;
UPDATE task_main_template SET detail = 'Gogeta và Omega xuất hiện!\nĐạt 170 tỷ sức mạnh, hạ Omega 10 lần rồi hạ Gogeta 10 lần\n(hai kẻ đi cùng nhau, hạ một kẻ thì kẻ kia bỏ đi),\nsau đó hạ Pikkon 5 lần ở Rừng Bamboo, Rừng dương xỉ, Nam Kamê.\nXong về Đảo Kame báo cáo với Quy Lão.' WHERE id = 40;

COMMIT;
