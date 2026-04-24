$ErrorActionPreference = 'Stop'

. (Join-Path $PSScriptRoot 'common.ps1')

Invoke-LotteryCompose -ComposeFiles @('compose.edge.yml') -ComposeArgs @('up', '-d', '--build')
