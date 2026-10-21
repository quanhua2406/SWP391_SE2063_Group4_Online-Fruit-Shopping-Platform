$base = 'http://localhost:9999/OnlineFruitShopping'
$work = 'D:\Temp\opencode'

function Check($name, $url, $cookie, $mustContain) {
    $out = Join-Path $work 'page.html'
    $code = curl.exe -s -b $cookie -o $out -w "%{http_code}" $url
    $ok = ($code -eq '200')
    $detail = ''
    if ($mustContain) {
        $content = Get-Content $out -Raw
        foreach ($m in $mustContain) {
            if (-not $content.Contains($m)) { $ok = $false; $detail += " MISSING[$m]" }
        }
    }
    $status = if ($ok) { 'PASS' } else { 'FAIL' }
    Write-Output ("{0} {1} -> {2}{3}" -f $status, $name, $code, $detail)
}

# ---- logins ----
curl.exe -s -c "$work\cookie-owner.txt" -o NUL -d "email=shopowner@freshfruit.com&password=Tien123456&role=SHOPOWNER" "$base/login"
curl.exe -s -c "$work\cookie-admin.txt" -o NUL -d "email=he180293phamquangtien@gmail.com&password=Tien123456&role=ADMIN" "$base/login"
Write-Output '--- logins done ---'

# ---- owner commission report: default (overview) figures must stay 1.250.000 / 187.500 / 1.062.500 ----
Check 'owner report overview' "$base/store-owner/commission-report" "$work\cookie-owner.txt" `
    @('1,250,000', '187,500', '1,062,500', 'Fee Breakdown', 'Gross sales', '2,331,000', '185,000', '25,000', '1,037,500', 'section-nav', 'Overview')

# ---- owner report sections ----
Check 'owner report section=products' "$base/store-owner/commission-report?section=products" "$work\cookie-owner.txt" `
    @('Platform Fee by Product', '1,250,000', '187,500')
Check 'owner report section=schedule' "$base/store-owner/commission-report?section=schedule" "$work\cookie-owner.txt" `
    @('Current Fee Schedule', 'APPLE-1KG')
Check 'owner report section=rates' "$base/store-owner/commission-report?section=rates" "$work\cookie-owner.txt" `
    @('Platform Fee by Commission Rate', '187,500')
Check 'owner report section=status' "$base/store-owner/commission-report?section=status" "$work\cookie-owner.txt" `
    @('Fee by Order Status', 'Delivered')
Check 'owner report bad section -> overview' "$base/store-owner/commission-report?section=zzz" "$work\cookie-owner.txt" `
    @('Fee Breakdown', 'Gross sales')

# ---- owner report filtered to the preparing status (overview shows summary + breakdown) ----
Check 'owner report status=preparing' "$base/store-owner/commission-report?status=preparing" "$work\cookie-owner.txt" `
    @('611,000', '15,000', '74,650', 'value="preparing" selected')
Check 'owner report preparing+products' "$base/store-owner/commission-report?status=preparing&section=products" "$work\cookie-owner.txt" `
    @('Platform Fee by Product', '611,000', 'value="preparing" selected')

# ---- owner report filtered by date range covering everything ----
Check 'owner report date range' "$base/store-owner/commission-report?from=2026-10-01&to=2026-10-31" "$work\cookie-owner.txt" `
    @('1,250,000', '187,500')

# ---- owner report date range with no delivered order ----
Check 'owner report empty range' "$base/store-owner/commission-report?from=2026-10-18&to=2026-10-19" "$work\cookie-owner.txt" `
    @('0', 'Fee Breakdown')

# ---- admin commission report ----
Check 'admin report overview' "$base/admin/commission-report" "$work\cookie-admin.txt" `
    @('1,250,000', '187,500', '1,062,500', 'Fee Breakdown', 'Paid to shops (net)', '1,037,500', 'section-nav')
Check 'admin report section=shops' "$base/admin/commission-report?section=shops" "$work\cookie-admin.txt" `
    @('Platform Fee by Shop', '1,250,000')
Check 'admin report section=rates' "$base/admin/commission-report?section=rates" "$work\cookie-admin.txt" `
    @('Platform Fee by Commission Rate', '187,500')
Check 'admin report status=cancelled' "$base/admin/commission-report?status=cancelled" "$work\cookie-admin.txt" `
    @('185,000', '3,700', '162,050')

# ---- CSV export with filters ----
$out = "$work\csv1.csv"
$code = curl.exe -s -b "$work\cookie-owner.txt" -o $out -w "%{http_code}" "$base/store-owner/commission-report?format=csv&status=preparing"
$csv = Get-Content $out -Raw
$ok = ($code -eq '200') -and $csv.Contains('Fee Breakdown') -and $csv.Contains('Filter,') -and $csv.Contains('Promotional discount')
Write-Output ("{0} owner CSV filtered -> {1}" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $code)

$out = "$work\csv2.csv"
$code = curl.exe -s -b "$work\cookie-admin.txt" -o $out -w "%{http_code}" "$base/admin/commission-report?format=csv&from=2026-10-01&to=2026-10-31"
$csv = Get-Content $out -Raw
$ok = ($code -eq '200') -and $csv.Contains('Fee Breakdown') -and $csv.Contains('Filter,')
Write-Output ("{0} admin CSV filtered -> {1}" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $code)

# ---- inventory: five small pages ----
Check 'inventory view stock' "$base/store-owner/inventory" "$work\cookie-owner.txt" `
    @('View Stock Quantity', 'Stock Management', 'View Stock', 'Update Stock', 'Restock', 'Low Stock', 'History', 'APPLE-1KG')
Check 'inventory update page' "$base/store-owner/inventory/update" "$work\cookie-owner.txt" `
    @('Update Stock Quantity', 'batchId', 'Inventory History')
Check 'inventory restock page' "$base/store-owner/inventory/restock" "$work\cookie-owner.txt" `
    @('Restock Products', 'variantId', 'batchCode')
Check 'inventory low-stock page' "$base/store-owner/inventory/low-stock" "$work\cookie-owner.txt" `
    @('Low Stock Monitoring', 'Email Alert', 'notifyLowStock', 'setThreshold')
Check 'inventory history page' "$base/store-owner/inventory/history" "$work\cookie-owner.txt" `
    @('Inventory History', 'htype')
# unknown sub-page redirects to the view-stock page
$out = "$work\nonsense.html"
$code = curl.exe -s -L -b "$work\cookie-owner.txt" -o $out -w "%{http_code}" "$base/store-owner/inventory/nonsense"
$content = Get-Content $out -Raw
$ok = ($code -eq '200') -and $content.Contains('View Stock Quantity')
Write-Output ("{0} inventory unknown subpage -> view ({1})" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $code)

# filters on the new pages
Check 'inventory update filter' "$base/store-owner/inventory/update?bstatus=active" "$work\cookie-owner.txt" `
    @('value="active" selected')
Check 'inventory history filter' "$base/store-owner/inventory/history?htype=import" "$work\cookie-owner.txt" `
    @('value="import" selected')
# weight filter: only the 1 kg variant must remain in the table
$out = "$work\wf2.html"
curl.exe -s -b "$work\cookie-owner.txt" -o $out "$base/store-owner/inventory?vweight=1"
$content = Get-Content $out -Raw
$ok = $content.Contains('APPLE-1KG') -and -not $content.Contains('APPLE-0.5KG')
Write-Output ("{0} inventory view weight filter" -f $(if ($ok) { 'PASS' } else { 'FAIL' }))

# ---- inventory actions redirect back to their own small page ----
$out = "$work\restock-redir.txt"
$code = curl.exe -s -b "$work\cookie-owner.txt" -D $out -o NUL -w "%{http_code}" -d "action=restock&variantId=1&quantity=abc" "$base/store-owner/inventory/restock"
$loc = (Get-Content $out -Raw)
$ok = ($code -eq '302') -and $loc.Contains('/store-owner/inventory/restock?msg=invalid')
Write-Output ("{0} restock POST -> back to restock page ({1})" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $code)

$out = "$work\update-redir.txt"
$code = curl.exe -s -b "$work\cookie-owner.txt" -D $out -o NUL -w "%{http_code}" -d "action=update&batchId=-1&quantity=1" "$base/store-owner/inventory/update"
$loc = (Get-Content $out -Raw)
$ok = ($code -eq '302') -and $loc.Contains('/store-owner/inventory/update?msg=invalid')
Write-Output ("{0} update POST -> back to update page ({1})" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $code)

$out = "$work\threshold-redir.txt"
$code = curl.exe -s -b "$work\cookie-owner.txt" -D $out -o NUL -w "%{http_code}" -d "action=setThreshold&threshold=-5" "$base/store-owner/inventory/low-stock"
$loc = (Get-Content $out -Raw)
$ok = ($code -eq '302') -and $loc.Contains('/store-owner/inventory/low-stock?msg=invalid')
Write-Output ("{0} threshold POST -> back to low-stock page ({1})" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $code)

# with the threshold set below every batch, nothing is low -> graceful "none"
curl.exe -s -b "$work\cookie-owner.txt" -o NUL -d "action=setThreshold&threshold=1" "$base/store-owner/inventory/low-stock"
$out = "$work\notify.html"
curl.exe -s -b "$work\cookie-owner.txt" -L -o $out -d "action=notifyLowStock" "$base/store-owner/inventory/low-stock" | Out-Null
$content = Get-Content $out -Raw
if ($content.Contains('Nothing is below the safety threshold')) {
    Write-Output 'PASS notifyLowStock (nothing low) -> no email, friendly message'
} else {
    Write-Output 'FAIL notifyLowStock (nothing low)'
}

# raise the threshold so every variant is low -> alert email is attempted
curl.exe -s -b "$work\cookie-owner.txt" -o NUL -d "action=setThreshold&threshold=999" "$base/store-owner/inventory/low-stock"
$out = "$work\notify2.html"
curl.exe -s -b "$work\cookie-owner.txt" -L -o $out -d "action=notifyLowStock" "$base/store-owner/inventory/low-stock" | Out-Null
$content = Get-Content $out -Raw
if ($content.Contains('Low stock alert email sent')) {
    Write-Output 'PASS notifyLowStock (low stock) -> email sent'
} elseif ($content.Contains('could not be sent')) {
    Write-Output 'PASS notifyLowStock (low stock) -> graceful SMTP failure'
} else {
    Write-Output 'FAIL notifyLowStock (low stock) -> no known message'
}
# restore the original threshold
curl.exe -s -b "$work\cookie-owner.txt" -o NUL -d "action=setThreshold&threshold=10" "$base/store-owner/inventory/low-stock"

# ---- error pages ----
$out = "$work\err404.html"
$code = curl.exe -s -o $out -w "%{http_code}" "$base/no-such-page"
$content = Get-Content $out -Raw
$ok = ($code -eq '404') -and $content.Contains('Page not found')
Write-Output ("{0} friendly 404 -> {1}" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $code)

$out = "$work\err500.jsp.html"
$code = curl.exe -s -b "$work\cookie-owner.txt" -o $out -w "%{http_code}" "$base/jsp/system/error-500.jsp"
$content = Get-Content $out -Raw
$ok = ($code -eq '200') -and $content.Contains('Something went wrong')
Write-Output ("{0} error-500.jsp renders -> {1}" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $code)

# ---- regression: every screen of this module still renders ----
Check 'owner dashboard'  "$base/store-owner/dashboard"          "$work\cookie-owner.txt" @('Stock Management', 'Commission Report')
Check 'owner pricing'    "$base/store-owner/pricing"            "$work\cookie-owner.txt" @('APPLE-1KG')
Check 'owner promotions' "$base/store-owner/promotions"         "$work\cookie-owner.txt" @('Approval')
Check 'admin dashboard'  "$base/admin/dashboard"                "$work\cookie-admin.txt" @('Commission')
Check 'admin products'   "$base/admin/product-management"       "$work\cookie-admin.txt" @('Apple')
Check 'admin edit prod'  "$base/admin/product-edit?id=1"        "$work\cookie-admin.txt" @('commissionRate')
Check 'admin variants'   "$base/admin/product-variants?productId=1" "$work\cookie-admin.txt" @('APPLE-0.5KG')
Check 'admin promos'     "$base/admin/promotion-approvals"      "$work\cookie-admin.txt" @('Approval')

# ---- 18.0 admin shop-owner management ----
Check 'admin shops list'  "$base/admin/shops"                     "$work\cookie-admin.txt" @('Shop Owner Management', 'Shop List', 'Fruit', 'shopowner@freshfruit.com')
Check 'admin shop search' "$base/admin/shops?action=search&keyword=fruit&status=active" "$work\cookie-admin.txt" @('Fruit', 'action=detail')
Check 'admin shop detail' "$base/admin/shops?action=detail&shopId=1" "$work\cookie-admin.txt" @('Shop Profile', 'Owner Information', 'Block Shop')

$out = "$work\shopbad.html"
$code = curl.exe -s -b "$work\cookie-admin.txt" -o $out -w "%{http_code}" "$base/admin/shops?action=search&status=bogus"
Write-Output ("{0} admin shop bad status -> {1}" -f $(if ($code -eq '400') { 'PASS' } else { 'FAIL' }), $code)

$out = "$work\shopbad2.html"
$code = curl.exe -s -b "$work\cookie-admin.txt" -o $out -w "%{http_code}" "$base/admin/shops?action=detail&shopId=abc"
Write-Output ("{0} admin shop bad id -> {1}" -f $(if ($code -eq '400') { 'PASS' } else { 'FAIL' }), $code)

$out = "$work\shopbad3.html"
$code = curl.exe -s -b "$work\cookie-admin.txt" -o $out -w "%{http_code}" "$base/admin/shops?action=detail&shopId=99999"
Write-Output ("{0} admin shop not found -> {1}" -f $(if ($code -eq '404') { 'PASS' } else { 'FAIL' }), $code)

$out = "$work\shopblock.html"
$code = curl.exe -s -b "$work\cookie-admin.txt" -L -o $out -w "%{http_code}" -d "action=block&shopId=1" "$base/admin/shops"
$content = Get-Content $out -Raw
$ok = ($code -eq '200') -and $content.Contains('Shop blocked')
Write-Output ("{0} admin shop block -> {1}" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $code)

# while blocked the list shows the suspended shop and offers Unblock
$out = "$work\shopblocked.html"
$code = curl.exe -s -b "$work\cookie-admin.txt" -o $out -w "%{http_code}" "$base/admin/shops"
$content = Get-Content $out -Raw
$ok = ($code -eq '200') -and $content.Contains('suspended') -and $content.Contains('Unblock')
Write-Output ("{0} admin shop blocked state -> {1}" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $code)

$out = "$work\shopunblock.html"
$code = curl.exe -s -b "$work\cookie-admin.txt" -L -o $out -w "%{http_code}" -d "action=unblock&shopId=1" "$base/admin/shops"
$content = Get-Content $out -Raw
$ok = ($code -eq '200') -and $content.Contains('Shop unblocked')
Write-Output ("{0} admin shop unblock -> {1}" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $code)
