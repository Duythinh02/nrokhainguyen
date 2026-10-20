-- Thẻ nhiệm vụ (2692) mở khoá NV 36: bước 0 đổi thành "Dùng Thẻ nhiệm vụ", không tự hoàn thành khi đủ 120 tỷ sức mạnh.
-- Logic ở TaskService.useTheNhiemVu. Cần build + restart server.
SET NAMES utf8mb4;
UPDATE task_sub_template
SET NAME = 'Dùng Thẻ nhiệm vụ (cần 120 tỷ sức mạnh)',
    notify = 'Đạt 120 tỷ sức mạnh rồi dùng Thẻ nhiệm vụ để mở khoá nhiệm vụ mới'
WHERE ducvupro = 210 AND task_main_id = 36;
UPDATE task_main_template
SET detail = REPLACE(detail, 'Đạt 120 tỷ sức mạnh, hạ lần lượt', 'Đạt 120 tỷ sức mạnh, dùng Thẻ nhiệm vụ để mở khoá, rồi hạ lần lượt')
WHERE id = 36;

-- Hoàn tác:
-- UPDATE task_sub_template SET NAME = 'Đạt 120 tỷ sức mạnh', notify = 'Hãy đạt 120 tỷ sức mạnh' WHERE ducvupro = 210;
-- UPDATE task_main_template SET detail = REPLACE(detail, ', dùng Thẻ nhiệm vụ để mở khoá, rồi hạ lần lượt', ', hạ lần lượt') WHERE id = 36;

-- Lời nhắc bước 0: hướng dẫn hạ boss Quy Lão để lấy thẻ
UPDATE task_sub_template
SET notify = 'Đến Đảo Kame, chọn "Mở khoá nhiệm vụ" ở Quy Lão, hạ Quy Lão để nhận Thẻ nhiệm vụ rồi dùng thẻ (cần 120 tỷ sức mạnh)'
WHERE ducvupro = 210 AND task_main_id = 36;
