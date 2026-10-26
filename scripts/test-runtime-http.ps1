param([string]$Base='http://127.0.0.1:8081/OnlineFruitShopping')
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Net.Http
$root=Split-Path $PSScriptRoot -Parent
$results=[Collections.Generic.List[object]]::new()
$credentials=@{}
Get-Content (Join-Path $root 'config/test-accounts.properties') | Where-Object { $_ -match '^[^#].*=' } | ForEach-Object { $k,$v=$_ -split '=',2; $credentials[$k]=[regex]::Replace($v,'\\([!:=# ])','$1') }
function Client {
    $handler=[Net.Http.HttpClientHandler]::new(); $handler.AllowAutoRedirect=$false
    $handler.CookieContainer=[Net.CookieContainer]::new()
    return @{http=[Net.Http.HttpClient]::new($handler); cookies=$handler.CookieContainer}
}
function Request($client,$path,$form=$null) {
    if($null -eq $form) { $r=$client.http.GetAsync("$Base$path").Result }
    else {
        $pairs=[Collections.Generic.Dictionary[string,string]]::new()
        foreach($k in $form.Keys) { $pairs.Add($k,[string]$form[$k]) }
        $r=$client.http.PostAsync("$Base$path",[Net.Http.FormUrlEncodedContent]::new($pairs)).Result
    }
    $body=$r.Content.ReadAsStringAsync().Result
    return @{status=[int]$r.StatusCode; location=[string]$r.Headers.Location; body=$body; type=[string]$r.Content.Headers.ContentType}
}
function Record($name,$response,$expected,$condition=$true) {
    $results.Add([pscustomobject]@{case=$name; status=$response.status; location=$response.location; expected=$expected; result=$(if($response.status -eq $expected -and $condition){'PASS'}else{'FAIL'})})
}
function Login($email,$role) {
    $c=Client
    $r=Request $c '/login' @{email=$email;password=$credentials[$email];role=$role}
    Record "login $email" $r 302
    return $c
}
function Token($c,$path) {
    $r=Request $c $path
    $m=[regex]::Match($r.body,'name="csrfToken"\s+value="([^"]+)"')
    if(!$m.Success) { throw "No CSRF token at $path (HTTP $($r.status))" }
    return $m.Groups[1].Value
}
function Sql($query) {
    $c=[Data.SqlClient.SqlConnection]::new('Server=localhost;Database=OnlineFruitShopping_Test;Integrated Security=true;TrustServerCertificate=true')
    $c.Open()
    try { $cmd=$c.CreateCommand(); $cmd.CommandText="IF DB_NAME()<>N'OnlineFruitShopping_Test' THROW 51000,N'Wrong database',1; $query"; return $cmd.ExecuteScalar() }
    finally { $c.Dispose() }
}
$guest=Client
Record 'login page' (Request $guest '/login') 200
foreach($url in @('/shopowner','/shopowner/login')) {
    Record "GET legacy $url" (Request $guest $url) 302
    Record "POST legacy $url" (Request $guest $url @{}) 303
}
Record 'internal JSP' (Request $guest '/jsp/shopowner/views/detail.jsp') 404
$a=Login 'customer.a@fruitshop.test' 'Customer'
$b=Login 'customer.b@fruitshop.test' 'Customer'
$owner=Login 'owner.a@fruitshop.test' 'ShopOwner'
$shipper=Login 'delivery.a@fruitshop.test' 'DeliveryStaff'
$inactive=Login 'delivery.b@fruitshop.test' 'DeliveryStaff'
$admin=Login 'admin@fruitshop.test' 'Admin'
foreach($entry in @(@($a,'/customer/dashboard'),@($owner,'/store-owner/dashboard'),@($shipper,'/delivery/dashboard'),@($admin,'/admin/dashboard'))) {
    Record "dashboard $($entry[1])" (Request $entry[0] $entry[1]) 200
}
Record 'customer own detail' (Request $a '/shopowner/delivery/detail?id=1') 200
$dashboard=Request $a '/customer/dashboard'
Record 'customer name rendered from principal' $dashboard 200 ($dashboard.body -match 'Customer A')
Record 'customer cross detail' (Request $a '/shopowner/delivery/detail?id=2') 404
Record 'inactive shipper detail' (Request $inactive '/shopowner/delivery/detail?id=2') 404
foreach($path in @('/store-owner/commission','/store-owner/commission-report','/store-owner/commission-report?format=csv','/store-owner/create-shop','/store-owner/inventory','/store-owner/inventory/update','/store-owner/inventory/restock','/store-owner/inventory/low-stock','/store-owner/inventory/history','/store-owner/pricing','/store-owner/promotions')) {
    $r=Request $owner $path; Record "owner GET $path" $r $(if($path -eq '/store-owner/create-shop'){302}else{200})
    if($path -like '*format=csv' -and $r.status -eq 200) { [IO.File]::WriteAllText((Join-Path $root '.runtime/commission-report.csv'),$r.body) }
}
$token=Token $shipper '/shopowner/delivery/detail?id=1'
Record 'CSRF missing' (Request $shipper '/shopowner/delivery/update' @{id=1;action='accept'}) 403
Record 'CSRF wrong' (Request $shipper '/shopowner/delivery/update' @{id=1;action='accept';csrfToken='wrong'}) 403
$before=Sql 'SELECT delivery_status FROM deliveries WHERE delivery_id=2'
$r=Request $shipper '/shopowner/delivery/update' @{id=2;action='accept';csrfToken=$token}
Record 'shipper cross update unchanged' $r 302 ((Sql 'SELECT delivery_status FROM deliveries WHERE delivery_id=2') -eq $before)
$r=Request $inactive '/shopowner/delivery/update' @{id=2;action='accept';csrfToken=(Token $inactive '/shopowner/delivery/list')}
Record 'inactive shipper update unchanged' $r 302 ((Sql 'SELECT delivery_status FROM deliveries WHERE delivery_id=2') -eq $before)
$r=Request $owner '/store-owner/pricing' @{variantId=(Sql "SELECT v.variant_id FROM product_variants v JOIN products p ON v.product_id=p.product_id JOIN shops s ON p.shop_id=s.shop_id WHERE s.shop_name=N'Fixture Shop B'");price=75}
Record 'owner cross price forbidden' $r 302 ($r.location -match 'forbidden')
# Keep browser session locally for click-based testing; never commit this file.
$a.cookies.GetCookies([uri]$Base)['JSESSIONID'].Value | Set-Content (Join-Path $root '.runtime/customer-session.txt')
try {
    Sql "UPDATE users SET status='blocked' WHERE email='customer.b@fruitshop.test'" | Out-Null
    Record 'locked account next request' (Request $b '/shopowner/delivery/list') 302
} finally { Sql "UPDATE users SET status='active' WHERE email='customer.b@fruitshop.test'" | Out-Null }
try {
    Sql "DELETE ur FROM user_roles ur JOIN users u ON u.user_id=ur.user_id WHERE u.email='delivery.b@fruitshop.test'" | Out-Null
    Record 'revoked role next request' (Request $inactive '/shopowner/delivery/list') 302
} finally {
    Sql "INSERT user_roles(user_id,role_id) SELECT u.user_id,r.role_id FROM users u CROSS JOIN roles r WHERE u.email='delivery.b@fruitshop.test' AND UPPER(r.role_name)='DELIVERYSTAFF'" | Out-Null
}
$newOwner=Login 'owner.new@fruitshop.test' 'ShopOwner'
$r=Request $newOwner '/store-owner/create-shop' @{shopName='Runtime Created Shop';description='Isolated test fixture'}
Record 'create shop POST' $r 302 ((Sql "SELECT COUNT(*) FROM shops s JOIN users u ON s.owner_id=u.user_id WHERE u.email='owner.new@fruitshop.test'") -eq 1)
$variant=Sql "SELECT v.variant_id FROM product_variants v JOIN products p ON v.product_id=p.product_id JOIN shops s ON p.shop_id=s.shop_id WHERE s.shop_name=N'Fixture Shop A'"
$r=Request $owner '/store-owner/pricing' @{variantId=$variant;price=95}
Record 'own price POST persisted' $r 302 ((Sql "SELECT price FROM product_variants WHERE variant_id=$variant") -eq 95)
$r=Request $owner '/store-owner/promotions' @{variantId=$variant;promoPrice=80;startDate=(Get-Date -Format 'yyyy-MM-dd');endDate=((Get-Date).AddDays(7).ToString('yyyy-MM-dd'))}
Record 'variant promotion pending' $r 302 ((Sql "SELECT status FROM variant_promotions WHERE variant_id=$variant") -eq 'pending')
$r=Request $owner '/store-owner/promotions' @{action='removePromo';variantId=$variant}
Record 'variant promotion removed' $r 302 ((Sql "SELECT COUNT(*) FROM variant_promotions WHERE variant_id=$variant") -eq 0)
$r=Request $owner '/store-owner/inventory/low-stock' @{action='setThreshold';threshold=150}
Record 'threshold POST persisted' $r 302 ((Sql "SELECT low_stock_threshold FROM shops WHERE shop_name=N'Fixture Shop A'") -eq 150)
$r=Request $owner '/store-owner/inventory/low-stock' @{action='notifyLowStock'}
Record 'low-stock alert POST' $r 302
$batch=Sql "SELECT batch_id FROM inventory_batches WHERE variant_id=$variant"
$ownerId=Sql "SELECT user_id FROM users WHERE email='owner.a@fruitshop.test'"
# Deterministic starting point: the assertions below compute the expected
# quantities from these inputs (100 - 10 = 90 and a new batch of 7).
Sql "DELETE FROM inventory_adjustments WHERE batch_id=$batch OR batch_id IN (SELECT batch_id FROM inventory_batches WHERE variant_id=$variant AND batch_code LIKE 'RUNTIME-RESTOCK%'); DELETE FROM inventory_batches WHERE variant_id=$variant AND batch_code LIKE 'RUNTIME-RESTOCK%'; UPDATE inventory_batches SET quantity=100 WHERE batch_id=$batch;" | Out-Null
Record 'inventory update POST' (Request $owner '/store-owner/inventory/update' @{action='update';batchId=$batch;quantity=90;reason='Runtime test'}) 302
Record 'inventory restock POST' (Request $owner '/store-owner/inventory/restock' @{action='restock';variantId=$variant;quantity=7;batchCode='RUNTIME-RESTOCK';expiryDate=((Get-Date).AddDays(20).ToString('yyyy-MM-dd'));reason='Runtime test'}) 302
$updQty=Sql "SELECT quantity FROM inventory_batches WHERE batch_id=$batch"
$updCount=Sql "SELECT COUNT(*) FROM inventory_adjustments WHERE batch_id=$batch"
$updChg=Sql "SELECT TOP 1 quantity_change FROM inventory_adjustments WHERE batch_id=$batch ORDER BY adjustment_id DESC"
$updActor=Sql "SELECT TOP 1 adjusted_by FROM inventory_adjustments WHERE batch_id=$batch ORDER BY adjustment_id DESC"
$updReason=Sql "SELECT TOP 1 reason FROM inventory_adjustments WHERE batch_id=$batch ORDER BY adjustment_id DESC"
Record 'inventory update changes batch and writes one log row' @{status=302;location=''} 302 ($updQty -eq 90 -and $updCount -eq 1 -and $updChg -eq -10 -and $updReason -eq 'Runtime test')
Record 'inventory update records actor from session' @{status=302;location=''} 302 ($updActor -eq $ownerId)
$restockBatch=Sql "SELECT batch_id FROM inventory_batches WHERE variant_id=$variant AND batch_code='RUNTIME-RESTOCK'"
$restockQty=Sql "SELECT quantity FROM inventory_batches WHERE batch_id=$restockBatch"
$restockCount=Sql "SELECT COUNT(*) FROM inventory_adjustments WHERE batch_id=$restockBatch"
$restockChg=Sql "SELECT TOP 1 quantity_change FROM inventory_adjustments WHERE batch_id=$restockBatch ORDER BY adjustment_id DESC"
$restockActor=Sql "SELECT TOP 1 adjusted_by FROM inventory_adjustments WHERE batch_id=$restockBatch ORDER BY adjustment_id DESC"
Record 'inventory restock creates batch and one log row' @{status=302;location=''} 302 ($restockQty -eq 7 -and $restockCount -eq 1 -and $restockChg -eq 7 -and $restockActor -eq $ownerId)
$history=Request $owner '/store-owner/inventory/history'
Record 'inventory history shows quantity actor reason' $history 200 ($history.body -match '<td>RUNTIME-RESTOCK</td>' -and $history.body -match '<td>7</td>' -and $history.body -match '<td>10</td>' -and $history.body -match '<td>Owner A</td>' -and $history.body -match '<td>Runtime test</td>')
$bBatch=Sql "SELECT TOP 1 b.batch_id FROM inventory_batches b JOIN product_variants v ON v.variant_id=b.variant_id JOIN products p ON p.product_id=v.product_id JOIN shops s ON s.shop_id=p.shop_id WHERE s.shop_name=N'Fixture Shop B'"
$bQty=Sql "SELECT quantity FROM inventory_batches WHERE batch_id=$bBatch"
$bLogs=Sql "SELECT COUNT(*) FROM inventory_adjustments WHERE batch_id=$bBatch"
$r=Request $owner '/store-owner/inventory/update' @{action='update';batchId=$bBatch;quantity=5;reason='cross shop'}
Record 'inventory cross-shop update forbidden' $r 302 ($r.location -match 'forbidden' -and (Sql "SELECT quantity FROM inventory_batches WHERE batch_id=$bBatch") -eq $bQty -and (Sql "SELECT COUNT(*) FROM inventory_adjustments WHERE batch_id=$bBatch") -eq $bLogs)
# Same-connection transaction: the log insert must fail AFTER the batch update.
try {
    Sql "EXEC(N'CREATE OR ALTER TRIGGER dbo.runtime_test_adjustment_failure ON dbo.inventory_adjustments AFTER INSERT AS BEGIN THROW 51010, ''Runtime inventory rollback probe'', 1; END')" | Out-Null
    $r=Request $owner '/store-owner/inventory/update' @{action='update';batchId=$batch;quantity=55;reason='rollback probe'}
    Record 'inventory update rolls back when log insert fails' $r 500 ((Sql "SELECT quantity FROM inventory_batches WHERE batch_id=$batch") -eq 90 -and (Sql "SELECT COUNT(*) FROM inventory_adjustments WHERE batch_id=$batch") -eq 1)
} finally { Sql 'DROP TRIGGER IF EXISTS dbo.runtime_test_adjustment_failure' | Out-Null }
# ---- Commission report fixtures: expected values computed from input data ----
Sql @"
UPDATE p SET commission_rate=10 FROM products p JOIN shops s ON s.shop_id=p.shop_id WHERE s.shop_name=N'Fixture Shop A';
DELETE oi FROM order_items oi JOIN orders o ON o.order_id=oi.order_id WHERE o.note=N'runtime-report-fixture';
DELETE FROM orders WHERE note=N'runtime-report-fixture';
DECLARE @shopA INT=(SELECT shop_id FROM shops WHERE shop_name=N'Fixture Shop A'),
        @shopB INT=(SELECT shop_id FROM shops WHERE shop_name=N'Fixture Shop B'),
        @custA INT=(SELECT user_id FROM users WHERE email='customer.a@fruitshop.test'),
        @custB INT=(SELECT user_id FROM users WHERE email='customer.b@fruitshop.test'),
        @varA INT=(SELECT v.variant_id FROM product_variants v JOIN products p ON p.product_id=v.product_id JOIN shops s ON s.shop_id=p.shop_id WHERE s.shop_name=N'Fixture Shop A'),
        @varB INT=(SELECT v.variant_id FROM product_variants v JOIN products p ON p.product_id=v.product_id JOIN shops s ON s.shop_id=p.shop_id WHERE s.shop_name=N'Fixture Shop B'),
        @o1 INT, @o2 INT, @o3 INT;
INSERT INTO orders(shop_id,user_id,order_date,total_amount,coupon_discount,receiver_name,receiver_phone,shipping_address,order_status,payment_method,payment_status,note)
VALUES(@shopA,@custA,GETDATE(),200,0,N'Customer A','0000000000',N'Test',N'Completed',N'COD',N'Paid',N'runtime-report-fixture');
SET @o1=SCOPE_IDENTITY();
INSERT INTO order_items(order_id,variant_id,price,quantity) VALUES(@o1,@varA,100,2);
INSERT INTO orders(shop_id,user_id,order_date,total_amount,coupon_discount,receiver_name,receiver_phone,shipping_address,order_status,payment_method,payment_status,note)
VALUES(@shopA,@custA,GETDATE(),50,0,N'Customer A','0000000000',N'Test',N'Cancelled',N'COD',N'Unpaid',N'runtime-report-fixture');
SET @o2=SCOPE_IDENTITY();
INSERT INTO order_items(order_id,variant_id,price,quantity) VALUES(@o2,@varA,50,1);
INSERT INTO orders(shop_id,user_id,order_date,total_amount,coupon_discount,receiver_name,receiver_phone,shipping_address,order_status,payment_method,payment_status,note)
VALUES(@shopB,@custB,GETDATE(),300,0,N'Customer B','0000000000',N'Test',N'Completed',N'COD',N'Paid',N'runtime-report-fixture');
SET @o3=SCOPE_IDENTITY();
INSERT INTO order_items(order_id,variant_id,price,quantity) VALUES(@o3,@varB,150,2);
SELECT 1;
"@ | Out-Null
$report=Request $owner '/store-owner/commission-report?section=products'
Record 'commission report totals match fixture' $report 200 ($report.body -match 'Fixture Shop A Apple' -and $report.body -match '200 \u0111' -and $report.body -match '20 \u0111' -and $report.body -notmatch 'Fixture Shop B Apple')
Record 'commission report status options use real statuses' $report 200 ($report.body -match '>Completed \(fee basis\)<' -and $report.body -match 'value="Completed"')
$csv=Request $owner '/store-owner/commission-report?format=csv'
Record 'commission CSV matches fixture revenue' $csv 200 ($csv.body -match 'Fixture Shop A Apple,2\.00,200\.00,10\.00,20\.00,180\.00' -and $csv.body -notmatch 'Fixture Shop B Apple')
$csvBytes=$owner.http.GetByteArrayAsync("$Base/store-owner/commission-report?format=csv").Result
Record 'commission CSV is UTF-8 with BOM' $csv 200 ($csvBytes.Length -gt 3 -and $csvBytes[0] -eq 0xEF -and $csvBytes[1] -eq 0xBB -and $csvBytes[2] -eq 0xBF -and $csv.type -match 'charset=UTF-8')
$cancelled=Request $owner '/store-owner/commission-report?section=products&status=Cancelled'
Record 'commission status filter uses order_status' $cancelled 200 ($cancelled.body -match '50 \u0111' -and $cancelled.body -notmatch '200 \u0111')
$fromDate=(Get-Date).AddDays(1).ToString('yyyy-MM-dd')
$none=Request $owner "/store-owner/commission-report?section=products&from=$fromDate"
Record 'commission date filter uses order_date' $none 200 ($none.body -notmatch '200 \u0111')
# CSV escaping: temporarily rename the product, then restore it. The
# name has a quote, a comma and a non-ASCII character (built with
# NCHAR so this script itself stays ASCII for PowerShell 5.1).
try {
    Sql "UPDATE products SET product_name=N'Runtime ""Apple"", '+NCHAR(273)+N' special' WHERE product_id=(SELECT p.product_id FROM products p JOIN shops s ON s.shop_id=p.shop_id WHERE s.shop_name=N'Fixture Shop A')" | Out-Null
    $esc=Request $owner '/store-owner/commission-report?format=csv'
    Record 'commission CSV escapes quotes/commas and keeps UTF-8' $esc 200 ($esc.body.Contains('"Runtime ""Apple"", ' + [char]0x111 + ' special"'))
} finally {
    Sql "UPDATE products SET product_name=N'Fixture Shop A Apple' WHERE product_id=(SELECT p.product_id FROM products p JOIN shops s ON s.shop_id=p.shop_id WHERE s.shop_name=N'Fixture Shop A')" | Out-Null
}
# SQL Server transaction: induce a failure AFTER the delivery/order updates.
$baselineNotifications=Sql 'SELECT COUNT(*) FROM notifications'
try {
    Sql "UPDATE deliveries SET delivery_status=N'Assigned',note=NULL WHERE delivery_id=1; UPDATE orders SET order_status=N'Confirmed' WHERE order_id=1; EXEC(N'CREATE OR ALTER TRIGGER dbo.runtime_test_notification_failure ON dbo.notifications AFTER INSERT AS BEGIN THROW 51009, ''Runtime rollback probe'', 1; END');" | Out-Null
    $r=Request $shipper '/shopowner/delivery/update' @{id=1;action='accept';csrfToken=$token}
    $unchanged=(Sql 'SELECT delivery_status FROM deliveries WHERE delivery_id=1') -eq 'Assigned'
    $unchanged=$unchanged -and ((Sql 'SELECT order_status FROM orders WHERE order_id=1') -eq 'Confirmed')
    $unchanged=$unchanged -and ((Sql 'SELECT COUNT(*) FROM notifications') -eq $baselineNotifications)
    Record 'SQL Server rollback after notification failure' $r 302 $unchanged
} finally { Sql 'DROP TRIGGER IF EXISTS dbo.runtime_test_notification_failure' | Out-Null }
$second=Login 'delivery.a@fruitshop.test' 'DeliveryStaff'
$secondToken=Token $second '/shopowner/delivery/detail?id=1'
$tasks=@()
foreach($pair in @(@($shipper,$token),@($second,$secondToken))) {
    $form=[Collections.Generic.Dictionary[string,string]]::new()
    $form.Add('id','1'); $form.Add('action','accept'); $form.Add('csrfToken',$pair[1])
    $tasks += $pair[0].http.PostAsync("$Base/shopowner/delivery/update",[Net.Http.FormUrlEncodedContent]::new($form))
}
[Threading.Tasks.Task]::WaitAll([Threading.Tasks.Task[]]$tasks)
$accepted=(Sql 'SELECT delivery_status FROM deliveries WHERE delivery_id=1') -eq 'Accepted'
$once=(Sql 'SELECT COUNT(*) FROM notifications') -eq ($baselineNotifications+1)
Record 'concurrent accept commits once' @{status=[int]$tasks[0].Result.StatusCode;location='two concurrent POSTs'} 302 ($accepted -and $once -and [int]$tasks[1].Result.StatusCode -eq 302)
$r=Request $shipper '/shopowner/delivery/update' @{id=1;action='accept';csrfToken=$token}
Record 'retry accepted does not notify twice' $r 302 ((Sql 'SELECT COUNT(*) FROM notifications') -eq ($baselineNotifications+1))
$r=Request $a '/logout' @{csrfToken=(Token $a '/shopowner/delivery/list')}
Record 'logout with token' $r 302
Record 'after logout protected route' (Request $a '/shopowner/delivery/list') 302
$r=Request $a '/login' @{email='owner.a@fruitshop.test';password=$credentials['owner.a@fruitshop.test'];role='ShopOwner'}
Record 'same client role switch preserves saved redirect' $r 302 ($r.location -eq '/OnlineFruitShopping/shopowner/delivery/list')
$r=Request $a '/store-owner/dashboard'
Record 'role switched owner page' $r 200 ($r.body -match 'Owner A')
# Re-authenticate a fresh session for browser click tests.
$a=Login 'customer.a@fruitshop.test' 'Customer'
$a.cookies.GetCookies([uri]$Base)['JSESSIONID'].Value | Set-Content (Join-Path $root '.runtime/customer-session.txt')
$results | ConvertTo-Json -Depth 4 | Set-Content (Join-Path $root '.runtime/http-results.json')
$results | Format-Table -AutoSize
