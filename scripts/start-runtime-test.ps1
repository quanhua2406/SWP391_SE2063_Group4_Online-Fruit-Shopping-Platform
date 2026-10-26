param([string]$TomcatHome='C:\Program Files\Apache Software Foundation\Tomcat 10.1',
      [string]$JavaHome='C:\Program Files\Java\jdk-17', [int]$Port=8081)
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$base=Join-Path $root '.runtime/tomcat-test'
$war=Join-Path $root 'target/OnlineFruitShopping-1.0-SNAPSHOT.war'
$config=Join-Path $root 'config/db-test.properties'
if (!(Test-Path $war) -or !(Test-Path $config)) { throw 'Build WAR and configure test database first.' }
if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) { throw "Port $Port is in use." }
foreach($dir in @('conf','conf/Catalina/localhost','logs','temp','webapps','work','bin')) { New-Item -ItemType Directory -Force (Join-Path $base $dir) | Out-Null }
# Copy only base configuration files, never existing context descriptors.
Get-ChildItem (Join-Path $TomcatHome 'conf') -File | Copy-Item -Destination (Join-Path $base 'conf')
[xml]$server=Get-Content (Join-Path $base 'conf/server.xml')
$server.Server.SetAttribute('port','-1')
$connector=$server.Server.Service.Connector
$connector.SetAttribute('address','127.0.0.1'); $connector.SetAttribute('port',"$Port")
$server.Save((Join-Path $base 'conf/server.xml'))
$escaped=[Security.SecurityElement]::Escape($war)
[IO.File]::WriteAllText((Join-Path $base 'conf/Catalina/localhost/OnlineFruitShopping.xml'),"<Context docBase=`"$escaped`" />")
$env:CATALINA_HOME=$TomcatHome; $env:CATALINA_BASE=$base; $env:JAVA_HOME=$JavaHome
$env:FRUITSHOP_DB_CONFIG=$config
& (Join-Path $TomcatHome 'bin/catalina.bat') run
