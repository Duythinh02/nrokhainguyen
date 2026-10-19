-- Gỡ hút máu/hút KI khỏi giáp tập luyện (bag[26])
UPDATE player SET items_bag = JSON_SET(items_bag, '$[26]', '[1716,1,"[\\"[9,9994]\\",\\"[97,100]\\",\\"[107,20]\\",\\"[102,20]\\",\\"[30,1]\\"]",1792233453011]') WHERE id=1505 AND name='laze1hp' AND JSON_VALUE(items_bag,'$[26]') LIKE '[1716,1,%[95,50]%';
-- Thêm hút máu 50% / hút KI 50% vào cải trang Hắc Mị Nương (body[5])
UPDATE player SET items_body = JSON_SET(items_body, '$[5]', '[1557,1,"[\\"[50,25]\\",\\"[77,25]\\",\\"[117,25]\\",\\"[236,25]\\",\\"[95,50]\\",\\"[96,50]\\"]",1792233453011]') WHERE id=1505 AND name='laze1hp' AND JSON_VALUE(items_body,'$[5]') LIKE '[1557,1,%' AND JSON_VALUE(items_body,'$[5]') NOT LIKE '%[95,%';
