param(
    [string]$Server = 'localhost',
    [string]$SmoDirectory = 'C:\Program Files\Microsoft SQL Server\150\Setup Bootstrap\SQL2019\x64',
    [switch]$ExportOnly
)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
foreach ($name in @('ConnectionInfo','Management.Sdk.Sfc','Smo','SqlEnum')) {
    Add-Type -Path "$SmoDirectory\Microsoft.SqlServer.$name.dll"
}
$source = New-Object Microsoft.SqlServer.Management.Smo.Server $Server
$db = $source.Databases['OnlineFruitShopping']
if (!$db) { throw 'Source database not found.' }
if (!$ExportOnly -and $source.Databases.Contains('OnlineFruitShopping_Test')) { throw 'Test database already exists; inspect it rather than overwrite.' }
$scripter = New-Object Microsoft.SqlServer.Management.Smo.Scripter $source
$scripter.Options.ScriptData = $false
$scripter.Options.ScriptSchema = $true
$scripter.Options.DriAll = $true
$scripter.Options.Indexes = $true
$scripter.Options.WithDependencies = $true
$scripter.Options.IncludeDatabaseContext = $false
$scripter.Options.NoFileGroup = $true
$urns = [Microsoft.SqlServer.Management.Sdk.Sfc.Urn[]]@($db.Tables | Where-Object { !$_.IsSystemObject } | ForEach-Object { $_.Urn })
$batches = $scripter.Script($urns)
$guard = "IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;"
$output = Join-Path $root 'database/runtime-test-schema.sql'
$script = "-- Generated from SQL Server metadata; schema only, no user data.`r`n" + (($batches | ForEach-Object { "$guard`r`n$_`r`nGO" }) -join "`r`n")
$script = [regex]::Replace($script, '(?m)[ \t]+(?=\r?$)', '')
[IO.File]::WriteAllText($output, $script, [Text.UTF8Encoding]::new($false))
if ($ExportOnly) { Write-Output 'Schema exported; no database changes.'; return }
$admin = New-Object System.Data.SqlClient.SqlConnection "Server=$Server;Database=master;Integrated Security=true;TrustServerCertificate=true"
$admin.Open()
try { $cmd = $admin.CreateCommand(); $cmd.CommandText = 'CREATE DATABASE [OnlineFruitShopping_Test]'; $cmd.ExecuteNonQuery() | Out-Null }
finally { $admin.Close() }
$connection = New-Object System.Data.SqlClient.SqlConnection "Server=$Server;Database=OnlineFruitShopping_Test;Integrated Security=true;TrustServerCertificate=true"
$connection.Open()
try {
    foreach ($batch in $batches) {
        $cmd = $connection.CreateCommand(); $cmd.CommandText = "$guard`n$batch"; $cmd.ExecuteNonQuery() | Out-Null
    }
} finally { $connection.Close() }
Write-Output 'Created OnlineFruitShopping_Test with schema only. Apply guarded test migrations next.'
