# Bù quà giftcode "tanthu" cho 1 nhân vật bị trừ lượt mà không nhận được quà.
# Dùng: powershell -File bu_qua_tanthu.ps1 -PlayerId 1398 [-Apply]
#  - Không có -Apply: chỉ in ra lệnh SQL để xem trước.
#  - Có -Apply: sao lưu rồi chạy, CHỈ khi nhân vật đang offline và đã có "tanthu" trong danh sách đã dùng.
param([Parameter(Mandatory)][int]$PlayerId, [switch]$Apply)

$ErrorActionPreference = 'Stop'
$mysql = 'C:\xampp\mysql\bin\mysql.exe'
$dir = Split-Path -Parent $MyInvocation.MyCommand.Path
$env:MYSQL_PWD = 'Thinhduy02@'
# Gửi SQL qua stdin: PowerShell 5.1 xoá dấu " khi truyền qua tham số (-e) làm hỏng dữ liệu item
function Q([string]$sql) { $sql | & $mysql -uthinhduy07 --default-character-set=utf8mb4 nro_merge -N --raw }

# Quà lấy thẳng từ bảng giftcode
$detail = (Q "SELECT detail FROM giftcode WHERE code='tanthu'") | ConvertFrom-Json
$row = (Q "SELECT p.name, a.last_time_login < a.last_time_logout, p.giftcode, p.items_bag FROM player p JOIN account a ON a.id=p.account_id WHERE p.id=$PlayerId") -split "`t"
if ($row.Count -lt 4) { throw "Không tìm thấy nhân vật id $PlayerId" }
$name = $row[0]; $offline = $row[1]; $gift = $row[2]; $bagJson = $row[3]
# PowerShell 5.1 trả mảng JSON thành 1 phần tử, phải trải ra
$bag = @(($bagJson | ConvertFrom-Json) | ForEach-Object { $_ })

$gem = 0
$sets = New-Object System.Collections.Generic.List[string]
$guards = New-Object System.Collections.Generic.List[string]
$used = @{}
$t = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
$empty = @(for ($i = 0; $i -lt $bag.Count; $i++) { if ($bag[$i].StartsWith('[-1,')) { $i } })
$e = 0

foreach ($g in $detail) {
    $id = [int]$g.id; $qty = [int]$g.quantity
    if ($id -eq 77) { $gem += $qty; continue }   # Ngọc: cộng thẳng vào ngọc
    # cộng dồn vào chồng có sẵn cùng id, option mặc định [73,0]
    $stack = -1
    for ($i = 0; $i -lt $bag.Count; $i++) {
        if (-not $used.ContainsKey($i) -and $bag[$i] -match "^\[$id,(\d+),""\[\\""\[73,0\]\\""\]"",(\d+)\]$") { $stack = $i; break }
    }
    if ($stack -ge 0) {
        $old = [int]$Matches[1]; $time = $Matches[2]
        $sets.Add("'`$[$stack]', '[$id,$($old + $qty),`"[\\`"[73,0]\\`"]`",$time]'")
        $guards.Add("JSON_VALUE(items_bag,'`$[$stack]') LIKE '[$id,$old,%'")
        $used[$stack] = $true
    } else {
        if ($e -ge $empty.Count) { throw "$name không đủ ô trống" }
        $slot = $empty[$e]; $e++
        $sets.Add("'`$[$slot]', '[$id,$qty,`"[\\`"[73,0]\\`"]`",$t]'")
        $guards.Add("JSON_VALUE(items_bag,'`$[$slot]') LIKE '[-1,%'")
        $used[$slot] = $true
    }
}

$sql = "UPDATE player SET items_bag = JSON_SET(items_bag, " + ($sets -join ', ') + ")" +
       ", data_inventory = JSON_SET(data_inventory, '`$[1]', JSON_EXTRACT(data_inventory, '`$[1]') + $gem)" +
       " WHERE id = $PlayerId AND giftcode LIKE '%tanthu%' AND " + ($guards -join ' AND ') + ";"

"Nhân vật: $name (id $PlayerId) | offline=$offline | giftcode=$gift"
$sql

if ($Apply) {
    if ($offline -ne '1') { throw "$name đang online, không sửa." }
    if ($gift -notlike '*tanthu*') { throw "$name chưa có tanthu trong danh sách đã dùng." }
    $stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
    "$bagJson`r`n$(Q "SELECT data_inventory FROM player WHERE id=$PlayerId")" | Set-Content -Encoding utf8 "$dir\backup_bu_tanthu_${PlayerId}_$stamp.txt"
    $out = Q "$sql SELECT ROW_COUNT();"
    "Số dòng đã sửa: $out"
}
