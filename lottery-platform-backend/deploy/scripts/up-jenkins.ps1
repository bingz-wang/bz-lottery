$ErrorActionPreference = 'Stop'

. (Join-Path $PSScriptRoot 'common.ps1')

Invoke-LotteryCompose -ComposeFiles @('compose.dev-tools.yml') -ComposeArgs @('up', '-d', '--build', 'jenkins')
