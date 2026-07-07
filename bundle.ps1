# =============================================================================
# bundle.ps1 - Build a standalone, fully self-contained Windows app for JEngine.
#
# Produces a folder (and zip) containing:
#   JEngine.exe   - native launcher
#   runtime\      - bundled Java runtime (target machine needs NO Java installed)
#   app\          - JEngine.jar
#   Assets\       - game assets, next to the exe so they resolve at runtime
#
# Usage (from the project root):
#   Right-click -> Run with PowerShell,  or:   powershell -ExecutionPolicy Bypass -File bundle.ps1
#
# This uses the CURRENT dist\JEngine.jar. Build/clean the project in NetBeans
# first if you want to bundle fresh code. (Or run the "bundle" Ant target, which
# recompiles the jar automatically before bundling.)
# =============================================================================

$ErrorActionPreference = 'Stop'
$Root = $PSScriptRoot
Set-Location $Root

$Name    = 'JEngine'
$MainCls = 'Framework.Main'
$Jar     = Join-Path $Root 'dist\JEngine.jar'
$Icon    = Join-Path $Root 'src\Resources\JEngineIcon.ico'
$Assets  = Join-Path $Root 'Assets'
$Staging = Join-Path $Root 'build\bundle'
$Input   = Join-Path $Root 'build\bundle-input'
$AppDir  = Join-Path $Staging $Name
$Zip     = Join-Path $Root "dist\$Name-standalone.zip"

# --- Locate a JDK that has jpackage ------------------------------------------
function Find-Jpackage {
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += (Join-Path $env:JAVA_HOME 'bin\jpackage.exe') }
    $candidates += (Get-Command jpackage.exe -ErrorAction SilentlyContinue | ForEach-Object { $_.Source })
    $candidates += (Get-ChildItem 'C:\Program Files\Java\jdk*\bin\jpackage.exe' -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName })
    foreach ($c in $candidates) { if ($c -and (Test-Path $c)) { return $c } }
    return $null
}

$Jpackage = Find-Jpackage
if (-not $Jpackage) {
    Write-Error "Could not find jpackage.exe. Install a JDK (17+) or set JAVA_HOME."
    exit 1
}
Write-Host "Using jpackage: $Jpackage"

if (-not (Test-Path $Jar)) {
    Write-Error "dist\JEngine.jar not found. Build the project in NetBeans first."
    exit 1
}

# --- Fresh staging -----------------------------------------------------------
if (Test-Path $Staging) { Remove-Item $Staging -Recurse -Force }
if (Test-Path $Input)   { Remove-Item $Input   -Recurse -Force }
New-Item -ItemType Directory -Path $Input | Out-Null
Copy-Item $Jar (Join-Path $Input "$Name.jar")

# --- Build the self-contained app image --------------------------------------
Write-Host "Running jpackage (bundles a Java runtime; takes a minute)..."
& $Jpackage `
    --type app-image `
    --name $Name `
    --input $Input `
    --main-jar "$Name.jar" `
    --main-class $MainCls `
    --dest $Staging `
    --icon $Icon `
    --add-modules 'java.base,java.desktop,jdk.unsupported' `
    --java-options '-Dsun.java2d.d3d=true' `
    --java-options '-Dsun.java2d.uiScale=1' `
    --java-options '-Xmx4096m'
if ($LASTEXITCODE -ne 0) { Write-Error "jpackage failed."; exit 1 }

# --- Assets sit next to the exe (app resolves them via user.dir) -------------
Write-Host "Copying Assets next to the launcher..."
Copy-Item $Assets (Join-Path $AppDir 'Assets') -Recurse

# --- Shareable zip -----------------------------------------------------------
Write-Host "Zipping distribution..."
if (Test-Path $Zip) { Remove-Item $Zip -Force }
Compress-Archive -Path $AppDir -DestinationPath $Zip

Write-Host ""
Write-Host "====================================================="
Write-Host "Standalone build complete."
Write-Host "  Run it here:  $AppDir\$Name.exe"
Write-Host "  Share this:   $Zip"
Write-Host "====================================================="
