$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$prodDir = Split-Path -Parent $scriptDir
$envFile = Join-Path $prodDir 'env\prod.env'
$envExampleFile = Join-Path $prodDir 'env\prod.env.example'

if (-not (Test-Path $envFile))
{
    Copy-Item $envExampleFile $envFile
}

Set-Location $prodDir

Write-Host 'This is a production deployment draft script.'
Write-Host 'Review images, secrets, volumes, TLS, and domain settings before using it on a real server.'
Write-Host 'Example command:'
Write-Host 'docker compose --env-file .\env\prod.env -f .\compose\compose.prod.yml up -d'
Write-Host 'Current structure uses .\conf for runtime configuration and .\data for persistent data.'
