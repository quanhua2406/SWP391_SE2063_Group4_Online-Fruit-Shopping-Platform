$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$connection = New-Object System.Data.SqlClient.SqlConnection 'Server=localhost;Database=OnlineFruitShopping_Test;Integrated Security=true;TrustServerCertificate=true'
$connection.Open()
try {
    $guard = "IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000,N'Test database required',1;"
    foreach ($file in @('runtime-test-migrations.sql','runtime-test-login-schema.sql','add_variant_promotions.sql','add_variant_promotion_approval.sql','add_product_commission.sql','add_hidden_by_admin.sql')) {
        $text = Get-Content -Raw (Join-Path $root "database/$file")
        foreach ($batch in [regex]::Split($text,'(?im)^GO\s*$')) {
            if (!$batch.Trim()) { continue }
            $cmd=$connection.CreateCommand(); $cmd.CommandText="$guard`n$batch"; $cmd.ExecuteNonQuery() | Out-Null
        }
    }
    $hashLine = Get-Content (Join-Path $root 'config/test-accounts.properties') | Where-Object { $_ -like 'fixture.hash=*' }
    if (!$hashLine) { throw 'Generate fixture credentials first using RuntimeTools.' }
    $cmd=$connection.CreateCommand(); $cmd.CommandText=Get-Content -Raw (Join-Path $root 'database/runtime-test-fixtures.sql')
    $cmd.Parameters.AddWithValue('@FixtureHash',$hashLine.Substring('fixture.hash='.Length)) | Out-Null
    $cmd.ExecuteNonQuery() | Out-Null
} finally { $connection.Close() }
# A dedicated SQL-authentication login; no elevated server role is granted.
$password = [Guid]::NewGuid().ToString('N') + 'aA!9'
$admin = New-Object System.Data.SqlClient.SqlConnection 'Server=localhost;Database=master;Integrated Security=true;TrustServerCertificate=true'
$admin.Open()
try {
    $cmd=$admin.CreateCommand()
    $cmd.CommandText="IF SUSER_ID(N'fruitshop_runtime_test') IS NOT NULL THROW 51002,N'Test login already exists; inspect it',1; CREATE LOGIN fruitshop_runtime_test WITH PASSWORD='$password', CHECK_POLICY=ON;"
    $cmd.ExecuteNonQuery() | Out-Null
    $cmd.CommandText='USE OnlineFruitShopping_Test; CREATE USER fruitshop_runtime_test FOR LOGIN fruitshop_runtime_test; ALTER ROLE db_datareader ADD MEMBER fruitshop_runtime_test; ALTER ROLE db_datawriter ADD MEMBER fruitshop_runtime_test;'
    $cmd.ExecuteNonQuery() | Out-Null
} finally { $admin.Close() }
$config = "db.url=jdbc:sqlserver://localhost:1433;databaseName=OnlineFruitShopping_Test;encrypt=true;trustServerCertificate=true`ndb.user=fruitshop_runtime_test`ndb.password=$password`n"
[IO.File]::WriteAllText((Join-Path $root 'config/db-test.properties'),$config,[Text.UTF8Encoding]::new($false))
Write-Output 'Test fixture and dedicated SQL login created; credentials saved locally.'
