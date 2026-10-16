<?php
	$apikey = 'D72488133C0E2DE9713CCADACA5B84'; //API key, lấy từ website thesieutoc.net thay vào trong cặp dấu ''
	// database Mysql config
	$local_db = "localhost";
	$user_db = "root";
	$pass_db = "";
	$name_db = "nro";
	# đừng đụng vào 
  $conn = new mysqli($local_db, $user_db, $pass_db, $name_db);
  $conn->set_charset("utf8");
    
?>