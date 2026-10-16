<?php
// Dev By Huỳnh Cường

#config-------------------------------------------------------------
global $config;
$serverName = "localhost";
$userName = "root";
$password = "";
$dbName = "nro";


#tên server---------------------------------------------------------
$tieude = "Trang Chủ - Ngọc rồng Hoa Sen";
$ten_server = "Ngọc rồng Hoa Sen";
$link_web = "";
//Được vận hành bởi:
$doingu = "Ngọc rồng Hoa Sen";

#Link boxZalo-------------------------------------------------------
$linkbox = "https://zalo.me/g/nmighv153";

#logo---------------------------------------------------------------
//head
$logohead = "../public/images/logo/logoweb.gif";
//end
$logoend = "../public/images/logo/logoweb.gif";
//icon-web
$iconweb = "../public/images/logo/goku.png";
	
	
#Mở thành viên------------------------------------------------------
$gia_mtv = 10000; //giá sẽ trừ vào số dư để mtv

#Nhận giftcode------------------------------------------------------
$gia_nhan_gift = 50000;

#Màu nền------------------------------------------------------------
$maunen ="
	z-index: 1;  
	position: relative;
	border-radius: 7px; 
	border: 2px solid #FB1B1B; 
	box-shadow: 0 0 15px #FB1B1B; 
	background-color: #fff6c4;
	padding: 10px;
";

#Màu thanh công cụ ( -1- )------------------------------------------

$maunavbar1 ="
	padding: 0.5rem 1rem; 
	text-align: center; 
	box-shadow: 0 0 7px #FB1B1B; 
	border-color: #FB1B1B; 
	text-decoration: none; 
	background-color: #000; 
	color: #fff; font-size: 0.9rem; 
	white-space: nowrap;
";

#Nút Download File---------------------------------------------------
$nutdownloadfile = "    
	.btn-download {
		background-color: #FB1B1B;
		border-color: black;
		text-shadow: 1px 1px 0 black;
	}
	.btn-download:hover,
	.btn-download:focus {
		background-color: #b8f9f1;
		border-color: 1px solid black;
		font-weight: bold;
	}
";

#Nút đăng nhập đăng kí index------------------------------------------
$nutdangnhapdangki = "   
	.btn-action {
		background-color: #a10303;
		border-color: black;
		font-weight: bold;
	}

	.btn-action:hover,
	.btn-action:focus {
		background-color: #ffca85 ;
		border-color: black;  
		font-weight: bold;
	}
";

#Nút đăng kí TO-------------------------------------------------------
$nutdangnhapdangkiTO = "
	font-weight: bold; 
	background-color: #FB1B1B; 
	border-radius: 10px; 
	width: 100%; 
	height: 50px;  
	white-space: nowrap; 
	overflow: hidden;
";

$onmouseover = "
	this.style.backgroundColor='#b8f9f1'
";
#---> ( Chỉ thay cái này )
$onmouseout = "
	this.style.backgroundColor='#FB1B1B'
";

function getUserIP() {
    if (!empty($_SERVER['HTTP_CLIENT_IP'])) {
        return $_SERVER['HTTP_CLIENT_IP'];
    } elseif (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
        return $_SERVER['HTTP_X_FORWARDED_FOR'];
    } else {
        return $_SERVER['REMOTE_ADDR'];
    }
}

#Link download + box chat---------------------------------------------
$jar = "";
$pc = "Link";
$adr = "Link";
$adrpro = "";
$ios = "";
$testflight = "";

#Captcha Google - config----------------------------------------------
$site_key = "6Lec3B0rAAAAANv2BGEAPZ49EXAAtZpMPDQdrHRJ";
$select_key = "6Lec3B0rAAAAAEJ_tKAS4w11FThN0y_kgIixhhI-";
$config = mysqli_connect($serverName, $userName, $password, $dbName);
if (mysqli_connect_errno()) {
    echo "Sai hoặc Chưa kết nối Database!";
    exit();
}
?>