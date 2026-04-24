$ErrorActionPreference = 'Stop'

. (Join-Path $PSScriptRoot 'common.ps1')

Invoke-LotteryCompose -ComposeFiles @(
    'compose.dev-infra.yml',
    'compose.dev-tools.yml',
    'compose.edge.yml'
) -ComposeArgs @('down')
