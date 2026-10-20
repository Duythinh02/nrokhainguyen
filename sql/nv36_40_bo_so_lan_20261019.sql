-- NV 36 - 40: lời nhắc / mô tả chỉ ghi boss ở đâu, không ghi số lần hạ (số lần đã hiện ở thanh nhiệm vụ).
SET NAMES utf8mb4;
START TRANSACTION;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Bujin, đi cùng băng Bojack (Rừng nấm, Rừng xương, Đảo Kamê, Đông Karin, Rừng Bamboo, Rừng dương xỉ, Nam Kamê, Đảo Bulông)' WHERE ducvupro = 211;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Kogu, đi cùng băng Bojack' WHERE ducvupro = 212;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Zangya, đi cùng băng Bojack' WHERE ducvupro = 213;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Bido, đi cùng băng Bojack' WHERE ducvupro = 214;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Bojack (Rừng nấm, Rừng xương, Đảo Kamê, Đông Karin, Rừng Bamboo, Rừng dương xỉ, Nam Kamê, Đảo Bulông)' WHERE ducvupro = 215;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Siêu Bojack (hạ Bojack thì hắn biến thành Siêu Bojack)' WHERE ducvupro = 216;
UPDATE task_main_template SET detail = 'Băng hải tặc vũ trụ của Bojack đã đổ bộ xuống Trái Đất.\nĐạt 120 tỷ sức mạnh, dùng Thẻ nhiệm vụ để mở khoá (hạ Quy Lão ở Đảo Kame để nhận thẻ),\nrồi hạ lần lượt Bujin, Kogu, Zangya, Bido, Bojack và Siêu Bojack\n(Rừng nấm, Rừng xương, Đảo Kamê, Đông Karin, Rừng Bamboo, Rừng dương xỉ, Nam Kamê, Đảo Bulông).\nXong về Đảo Kame báo cáo với Quy Lão.' WHERE id = 36;

UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Gomah ở Hành Tinh Cereal, Mảnh Đất Bỏ Hoang để nhặt Nhẫn thời không (rất hiếm)' WHERE ducvupro = 224;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Chiler ở Khu hang động, Rừng nguyên thủy' WHERE ducvupro = 243;
UPDATE task_main_template SET detail = 'Fide Vàng đang gọi đám lính Death Beam tới.\nĐạt 130 tỷ sức mạnh, hạ lần lượt Death Beam 1 đến 5\n(Fide Vàng gọi ra ở Đông Karin, 21h - 22h),\nhạ Gomah ở Hành Tinh Cereal để nhặt Nhẫn thời không (rất hiếm),\nrồi hạ Chiler ở Khu hang động, Rừng nguyên thủy.\nXong đến Hành tinh Bill báo cáo với Whis.' WHERE id = 37;

UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Nhất Tinh Long ở Làng Kakarot khu 0' WHERE ducvupro = 230;
UPDATE task_main_template SET detail = 'Năng lượng âm của ngọc rồng đã sinh ra Rồng ác.\nĐạt 140 tỷ sức mạnh, hạ lần lượt Khỉ đột, Nhật thần, Nguyệt thần\n(Nhật thần và Nguyệt thần đi cùng nhau, hạ một người thì người kia bỏ đi),\nrồi hạ Nhất Tinh Long ở Làng Kakarot.\nXong lên Thần điện báo cáo với Thượng Đế.' WHERE id = 38;

UPDATE task_sub_template SET notify = 'Tiếp theo là hạ phân thân Gas ở Thành Phố Sugar' WHERE ducvupro = 233;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ phân thân Oil ở Thành Phố Sugar' WHERE ducvupro = 234;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ phân thân Elec ở Thành Phố Sugar' WHERE ducvupro = 235;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ phân thân Macki ở Thành Phố Sugar' WHERE ducvupro = 236;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ phân thân Granola ở Hành Tinh Cereal' WHERE ducvupro = 237;
UPDATE task_main_template SET detail = 'Kẻ sống sót cuối cùng của tộc Cereal đang gây chiến.\nĐạt 150 tỷ sức mạnh, hạ lần lượt phân thân Gas, Oil, Elec, Macki ở Thành Phố Sugar,\nrồi hạ phân thân Granola ở Hành Tinh Cereal.' WHERE id = 39;

UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Omega (đi cùng Gogeta)' WHERE ducvupro = 239;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Gogeta (đi cùng Omega)' WHERE ducvupro = 240;
UPDATE task_sub_template SET notify = 'Tiếp theo là hạ Pikkon ở Rừng Bamboo, Rừng dương xỉ, Nam Kamê' WHERE ducvupro = 241;
UPDATE task_main_template SET detail = 'Gogeta và Omega xuất hiện!\nĐạt 170 tỷ sức mạnh, hạ Omega rồi hạ Gogeta\n(hai kẻ đi cùng nhau, hạ một kẻ thì kẻ kia bỏ đi),\nsau đó hạ Pikkon ở Rừng Bamboo, Rừng dương xỉ, Nam Kamê.\nXong về Đảo Kame báo cáo với Quy Lão.' WHERE id = 40;
COMMIT;
