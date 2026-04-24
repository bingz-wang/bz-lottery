$ErrorActionPreference = 'Stop'

. (Join-Path $PSScriptRoot 'common.ps1')

Ensure-LotteryEnv

$content = Get-Content $script:EnvFile -Raw
if ($content -match '(?m)^FRONTEND_MODE=')
{
    $content = [regex]::Replace($content, '(?m)^FRONTEND_MODE=.*$', 'FRONTEND_MODE=dev')
}
else
{
    $content = $content.TrimEnd() + "`r`nFRONTEND_MODE=dev`r`n"
}

Set-Content -Path $script:EnvFile -Value $content -Encoding UTF8
Write-Host 'FRONTEND_MODE has been set to dev.'

Invoke-LotteryCompose -ComposeFiles @('compose.edge.yml') -ComposeArgs @('up', '-d', '--build', 'nginx')
