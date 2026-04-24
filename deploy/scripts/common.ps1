$ErrorActionPreference = 'Stop'

$script:ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$script:DeployDir = Split-Path -Parent $script:ScriptDir
$script:EnvDir = Join-Path $script:DeployDir 'env'
$script:ComposeDir = Join-Path $script:DeployDir 'compose'
$script:DockerDir = Join-Path $script:DeployDir 'docker'
$script:EnvFile = Join-Path $script:EnvDir '.env'
$script:EnvExampleFile = Join-Path $script:EnvDir '.env.example'

function Ensure-LotteryEnv
{
    if (-not (Test-Path $script:EnvFile))
    {
        Copy-Item $script:EnvExampleFile $script:EnvFile
    }
}

function Invoke-LotteryCompose
{
    param(
        [string[]]$ComposeFiles,
        [string[]]$ComposeArgs = @()
    )

    Ensure-LotteryEnv
    Set-Location $script:DeployDir

    $arguments = @('--env-file', $script:EnvFile)
    foreach ($composeFile in $ComposeFiles)
    {
        $arguments += @('-f', (Join-Path $script:ComposeDir $composeFile))
    }
    $arguments += $ComposeArgs

    & docker compose @arguments
    if ($LASTEXITCODE -ne 0)
    {
        throw "docker compose exited with code $LASTEXITCODE"
    }
}
