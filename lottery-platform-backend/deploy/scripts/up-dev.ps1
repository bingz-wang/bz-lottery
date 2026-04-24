$ErrorActionPreference = 'Stop'

. (Join-Path $PSScriptRoot 'common.ps1')

Invoke-LotteryCompose -ComposeFiles @('compose.dev-infra.yml') -ComposeArgs @('up', '-d')
