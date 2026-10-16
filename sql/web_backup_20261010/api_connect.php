<?php
session_set_cookie_params([
    'httponly' => true,
    'samesite' => 'Strict'
]);
if (session_status() == PHP_SESSION_NONE) {
	session_start();
}
$ip_sv = "127.0.0.1";
$dbname_sv ="nro";
$user_sv = "root";
$pass_sv = "";

date_default_timezone_set('Asia/Ho_Chi_Minh');

$conn = new mysqli($ip_sv, $user_sv, $pass_sv,$dbname_sv);
$conn->set_charset("utf8mb4");    
?>