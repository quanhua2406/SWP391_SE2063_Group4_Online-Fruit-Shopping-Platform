# =============================================================
# FreshFruit - verification for every TienPQ feature
#   1.0 / 2.0  Auth, Profile, Address, User Admin
#   16.0       Role Management
#   17.0       Product Administration
#   18.0       Shop Owner Management + Commission reports
#   19.0       Order & Delivery Administration
#   24.0       Inventory & Pricing
#
# Checks both the screens (HTTP status) and the validation rules
# (they must reject bad input and must NOT write to the database).
#
# Requires: the app running on http://localhost:9999/OnlineFruitShopping
#           and the demo data loaded (database/demo_data.sql).
#
# Run:  powershell -NoProfile -ExecutionPolicy Bypass -File tools\verify_tienpq.ps1
# =============================================================

$ErrorActionPreference = 'Continue'
$base = 'http://localhost:9999/OnlineFruitShopping'
$cs   = 'Server=localhost,1433;Database=OnlineFruitShopping;User Id=sa;Password=123;TrustServerCertificate=True;'
$pass = 0; $fail = 0

function Check($n, $c) {
    if ($c) { $script:pass++; Write-Output ('PASS ' + $n) }
    else    { $script:fail++; Write-Output ('FAIL ' + $n) }
}

function Sql($q) {
    $cn = New-Object System.Data.SqlClient.SqlConnection $cs; $cn.Open()
    $cmd = $cn.CreateCommand(); $cmd.CommandText = $q
    if ($q.TrimStart().ToUpper().StartsWith('SELECT')) {
        $r = $cmd.ExecuteReader(); $v = $null; if ($r.Read()) { $v = $r[0] }; $cn.Close(); return $v
    }
    $r = $cmd.ExecuteReader(); while ($r.NextResult()) { }
    if ($r.FieldCount -gt 0 -and $r.Read()) { $v = $r[0]; $r.Close(); $cn.Close(); return $v }
    $r.Close(); $cn.Close(); return 0
}

function Login($e, $role) {
    $s = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    Invoke-WebRequest "$base/login" -Method Post -WebSession $s -UseBasicParsing `
        -Body @{ email = $e; password = 'Tien123456'; role = $role } -MaximumRedirection 5 | Out-Null
    return $s
}

function Code($url, $session) {
    try { $p = @{ Uri = $url; UseBasicParsing = $true; TimeoutSec = 20 }; if ($session) { $p.WebSession = $session }
        $r = Invoke-WebRequest @p; return [int]$r.StatusCode } catch {
        if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }; return 0 }
}

function RawCode($url, $session) {
    try { $p = @{ Uri = $url; UseBasicParsing = $true; TimeoutSec = 20; MaximumRedirection = 0 }; if ($session) { $p.WebSession = $session }
        $r = Invoke-WebRequest @p; return [int]$r.StatusCode } catch {
        if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }; return 0 }
}

function PostCode($url, $session, $body) {
    try { $p = @{ Uri = $url; Method = 'Post'; UseBasicParsing = $true; TimeoutSec = 20; Body = $body; MaximumRedirection = 0 }
        if ($session) { $p.WebSession = $session }
        $r = Invoke-WebRequest @p; return [int]$r.StatusCode } catch {
        if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }; return 0 }
}

$admin  = Login 'he180293phamquangtien@gmail.com' 'Admin'
$owner  = Login 'shopowner@freshfruit.com'       'ShopOwner'
$cust   = Login 'customer@freshfruit.com'        'Customer'
$shipper= Login 'delivery@freshfruit.com'        'DeliveryStaff'
Write-Output '--- logins done ---'

# ---------- 17.0 Product Administration ----------
Check '17.0 product list'           ((Code "$base/admin/product-management" $admin) -eq 200)
Check '17.0 product search'         ((Code "$base/admin/product-management?keyword=Fruit" $admin) -eq 200)
Check '17.0 product status filter'  ((Code "$base/admin/product-management?status=published" $admin) -eq 200)
Check '17.0 product add form'       ((Code "$base/admin/product-add" $admin) -eq 200)
Check '17.0 product edit form'      ((Code "$base/admin/product-edit?id=1" $admin) -eq 200)
Check '17.0 product edit bad id -> 302' ((RawCode "$base/admin/product-edit?id=abc" $admin) -eq 302)
Check '17.0 variants page'          ((Code "$base/admin/product-variants?productId=1" $admin) -eq 200)
Check '17.0 category management'    ((Code "$base/admin/category-management" $admin) -eq 200)

# ---------- 18.0 Shop Owner Management ----------
Check '18.0 user management list'   ((Code "$base/admin/user-management" $admin) -eq 200)
Check '18.0 user search'            ((Code "$base/admin/user-management?action=search&keyword=customer" $admin) -eq 200)
Check '18.0 user role filter'       ((Code "$base/admin/user-management?action=search&role=SHOPOWNER" $admin) -eq 200)
Check '18.0 user bad role -> 400'   ((Code "$base/admin/user-management?action=search&role=HACKER" $admin) -eq 400)
Check '18.0 user detail'            ((Code "$base/admin/user-management?action=detail&userId=2" $admin) -eq 200)
Check '18.0 user detail missing'    ((Code "$base/admin/user-management?action=detail" $admin) -eq 400)
Check '18.0 user detail bad id'     ((Code "$base/admin/user-management?action=detail&userId=zzz" $admin) -eq 400)
Check '18.0 user detail notfound'   ((Code "$base/admin/user-management?action=detail&userId=99999" $admin) -eq 404)
Check '18.0 fee collection report'  ((Code "$base/admin/commission-report" $admin) -eq 200)
Check '18.0 fee payment report'     ((Code "$base/store-owner/commission-report" $owner) -eq 200)

# ---------- 19.0 Order & Delivery Administration ----------
Check '19.0 orders list'            ((Code "$base/admin/orders" $admin) -eq 200)
Check '19.0 orders search'          ((Code "$base/admin/orders?search=1" $admin) -eq 200)
Check '19.0 orders status filter'   ((Code "$base/admin/orders?status=delivered" $admin) -eq 200)
Check '19.0 order detail'           ((Code "$base/admin/order-detail?orderId=1" $admin) -eq 200)
Check '19.0 order detail bad id -> 302' ((RawCode "$base/admin/order-detail?orderId=abc" $admin) -eq 302)
Check '19.0 order detail notfound -> 302' ((RawCode "$base/admin/order-detail?orderId=99999" $admin) -eq 302)
Check '19.0 deliveries list'        ((Code "$base/admin/deliveries" $admin) -eq 200)
Check '19.0 delivery staff list'    ((Code "$base/admin/delivery-staff" $admin) -eq 200)
Check '19.0 delivery assign page'   ((Code "$base/admin/delivery-assign" $admin) -eq 200)
Check '19.0 order issues page'      ((Code "$base/admin/order-issues" $admin) -eq 200)

# ---------- 24.0 Inventory & Pricing ----------
Check '24.0 inventory view'         ((Code "$base/store-owner/inventory" $owner) -eq 200)
Check '24.0 inventory update'       ((Code "$base/store-owner/inventory/update" $owner) -eq 200)
Check '24.0 inventory restock'      ((Code "$base/store-owner/inventory/restock" $owner) -eq 200)
Check '24.0 inventory low-stock'    ((Code "$base/store-owner/inventory/low-stock" $owner) -eq 200)
Check '24.0 inventory history'      ((Code "$base/store-owner/inventory/history" $owner) -eq 200)
Check '24.0 pricing page'           ((Code "$base/store-owner/pricing" $owner) -eq 200)

# ---------- 16.0 Role Management ----------
Check '16.0 role management'        ((Code "$base/admin/roles" $admin) -eq 200)

# ---------- 1.0 / 2.0 Auth, Profile, Address ----------
Check '1.0 login page'              ((Code "$base/login" $null) -eq 200)
Check '1.0 register page'           ((Code "$base/register" $null) -eq 200)
Check '1.0 forgot password page'    ((Code "$base/forgot-password" $null) -eq 200)
Check '2.0 profile page'            ((Code "$base/profile" $cust) -eq 200)
Check '2.0 addresses page'          ((Code "$base/addresses" $cust) -eq 200)

# ---------- Authorization ----------
Check 'AUTH customer blocked from /admin/products (403)' ((Code "$base/admin/product-management" $cust) -eq 403)
Check 'AUTH shipper blocked from /admin/orders (403)'    ((Code "$base/admin/orders" $shipper) -eq 403)
Check 'AUTH anonymous /admin/orders -> 302'              ((RawCode "$base/admin/orders" $null) -eq 302)
Check 'AUTH anonymous /profile -> 302'                   ((RawCode "$base/profile" $null) -eq 302)
Check 'AUTH customer blocked from /store-owner/pricing (403)' ((Code "$base/store-owner/pricing" $cust) -eq 403)

# =========================================================
# Validation rules - bad input must be rejected AND must not
# reach the database.
# =========================================================

# 19.0 delivery note > 255 chars
$longNote = 'x' * 300
$r = PostCode "$base/admin/delivery-assign" $admin @{ action='assign'; orderId='4'; staffId='4'; note=$longNote }
Check 'VAL 19.0 overlong note rejected' ($r -eq 302)
Check 'VAL 19.0 overlong note did NOT write delivery' ([int](Sql "SELECT COUNT(*) FROM deliveries WHERE order_id=4") -eq 0)

# 19.0 assign to a non DeliveryStaff account
$r = PostCode "$base/admin/delivery-assign" $admin @{ action='assign'; orderId='4'; staffId='2'; note='' }
Check 'VAL 19.0 assign non-staff blocked (nothing written)' ([int](Sql "SELECT COUNT(*) FROM deliveries WHERE order_id=4") -eq 0)

# 19.0 a valid assign really writes
$r = PostCode "$base/admin/delivery-assign" $admin @{ action='assign'; orderId='4'; staffId='7'; note='Audit assign' }
Check 'VAL 19.0 valid assign wrote delivery (assigned)' ((Sql "SELECT status FROM deliveries WHERE order_id=4") -eq 'assigned')
$did = Sql "SELECT TOP 1 delivery_id FROM deliveries WHERE order_id=4"
$r = PostCode "$base/admin/delivery-assign" $admin @{ action='unassign'; orderId='4'; deliveryId="$did"; note='' }
Check 'VAL 19.0 unassign removed delivery' ([int](Sql "SELECT COUNT(*) FROM deliveries WHERE order_id=4") -eq 0)

# 19.0 unknown resolution leaves the order alone
$before = Sql "SELECT status FROM orders WHERE order_id=7"
$r = PostCode "$base/admin/order-issues" $admin @{ orderId='7'; resolution='hack'; note='' }
Check 'VAL 19.0 invalid resolution does not change order' ((Sql "SELECT status FROM orders WHERE order_id=7") -eq $before)

# 19.0 block must ignore non DeliveryStaff accounts
$r = PostCode "$base/admin/delivery-staff" $admin @{ action='block'; staffId='2' }
Check 'VAL 19.0 block non-staff ignored (customer stays active)' ((Sql "SELECT status FROM users WHERE user_id=2") -eq 'active')

# 24.0 pricing rejects 0 and negative prices
$variantId = Sql "SELECT TOP 1 variant_id FROM product_variants ORDER BY variant_id"
$origPrice = Sql "SELECT TOP 1 price FROM product_variants ORDER BY variant_id"
$r = PostCode "$base/store-owner/pricing" $owner @{ variantId="$variantId"; price='0' }
Check 'VAL 24.0 price 0 rejected (unchanged)' ([decimal](Sql "SELECT TOP 1 price FROM product_variants WHERE variant_id=$variantId") -eq [decimal]$origPrice)
$r = PostCode "$base/store-owner/pricing" $owner @{ variantId="$variantId"; price='-5' }
Check 'VAL 24.0 negative price rejected (unchanged)' ([decimal](Sql "SELECT TOP 1 price FROM product_variants WHERE variant_id=$variantId") -eq [decimal]$origPrice)

# 17.0 add with a non-existent shop
$beforeProducts = Sql "SELECT COUNT(*) FROM products"
$r = PostCode "$base/admin/product-add" $admin @{ shopId='99999'; categoryId='1'; productName='AuditBadShop'; sku='AUDIT-BAD-SHOP'; price='10000'; weightValue='1'; commissionRate='5'; origin='VN'; packagingType='Bag' }
Check 'VAL 17.0 add with bad shop rejected (no product written)' ([int](Sql "SELECT COUNT(*) FROM products") -eq [int]$beforeProducts)

# 17.0 edit with a non-existent category
$origCat = Sql "SELECT category_id FROM products WHERE product_id=1"
$r = PostCode "$base/admin/product-edit" $admin @{ productId='1'; categoryId='99999'; productName='Fruit'; origin='VN'; commissionRate='5'; status='published' }
Check 'VAL 17.0 edit with bad category rejected (unchanged)' ([int](Sql "SELECT category_id FROM products WHERE product_id=1") -eq [int]$origCat)

# 18.0 user edit rejects an invalid email
$origEmail = Sql "SELECT email FROM users WHERE user_id=2"
$r = PostCode "$base/admin/user-management" $admin @{ action='update'; userId='2'; fullName='Customer One'; email='not-an-email'; phone='0900000000' }
Check 'VAL 18.0 invalid email rejected (unchanged)' ((Sql "SELECT email FROM users WHERE user_id=2") -eq $origEmail)

# 18.0 an admin account can never be blocked
$r = PostCode "$base/admin/user-management" $admin @{ action='block'; userId='1' }
Check 'VAL 18.0 admin account not blockable' ((Sql "SELECT status FROM users WHERE user_id=1") -eq 'active')

# 16.0 role change rejects an unknown role id
$origRole = Sql "SELECT TOP 1 role_id FROM user_roles WHERE user_id=2 ORDER BY role_id"
$r = PostCode "$base/admin/roles" $admin @{ action='update'; userId='2'; roleId='99999' }
Check 'VAL 16.0 invalid role id rejected (unchanged)' ([int](Sql "SELECT TOP 1 role_id FROM user_roles WHERE user_id=2 ORDER BY role_id") -eq [int]$origRole)

Write-Output ''
Write-Output ("RESULT: {0} passed, {1} failed" -f $pass, $fail)
