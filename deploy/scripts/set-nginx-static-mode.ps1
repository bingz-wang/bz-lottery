$ErrorActionPreference = 'Stop'

. (Join-Path $PSScriptRoot 'common.ps1')

Ensure-LotteryEnv

$content = Get-Content $script:EnvFile -Raw
if ($content -match '(?m)^FRONTEND_MODE=')
{
    $content = [regex]::Replace($content, '(?m)^FRONTEND_MODE=.*$', 'FRONTEND_MODE=static')
}
else
{
    $content = $content.TrimEnd() + "`r`nFRONTEND_MODE=static`r`n"
}

Set-Content -Path $script:EnvFile -Value $content -Encoding UTF8
Write-Host 'FRONTEND_MODE has been set to static.'

Invoke-LotteryCompose -ComposeFiles @('compose.edge.yml') -ComposeArgs @('up', '-d', '--build', 'nginx')
