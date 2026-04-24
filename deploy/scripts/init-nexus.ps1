$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$deployDir = Split-Path -Parent $scriptDir
$dockerDir = Join-Path $deployDir 'docker'
$envFile = Join-Path $deployDir 'env\.env'
$envExampleFile = Join-Path $deployDir 'env\.env.example'

Set-Location $dockerDir

if (-not (Test-Path $envFile))
{
    Copy-Item $envExampleFile $envFile
}

$envMap = @{ }
Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -notmatch '=')
    {
        return
    }

    $name, $value = $_ -split '=', 2
    $envMap[$name.Trim()] = $value.Trim()
}

$nexusPort = if ($envMap.ContainsKey('NEXUS_HTTP_PORT') -and $envMap['NEXUS_HTTP_PORT'])
{
    $envMap['NEXUS_HTTP_PORT']
}
else
{
    '8081'
}

$nexusDataDir = Join-Path $dockerDir 'data\nexus'
$passwordFile = Join-Path $nexusDataDir 'admin.password'
$nexusBaseUrl = "http://localhost:$nexusPort"
$timeoutAt = (Get-Date).AddMinutes(6)

Write-Host "Waiting for Nexus at $nexusBaseUrl ..."
while ((Get-Date) -lt $timeoutAt)
{
    if (Test-Path $passwordFile)
    {
        try
        {
            $status = Invoke-RestMethod -Uri "$nexusBaseUrl/service/rest/v1/status" -Method Get -TimeoutSec 5
            if ($status)
            {
                break
            }
        }
        catch
        {
        }
    }

    Start-Sleep -Seconds 5
}

if (-not (Test-Path $passwordFile))
{
    Write-Warning 'Nexus admin password file was not found yet, skipping repository initialization.'
    exit 0
}

$adminPassword = (Get-Content $passwordFile -Raw).Trim()
if (-not $adminPassword)
{
    Write-Warning 'Nexus admin password is empty, skipping repository initialization.'
    exit 0
}

$securePassword = ConvertTo-SecureString $adminPassword -AsPlainText -Force
$credential = New-Object System.Management.Automation.PSCredential('admin', $securePassword)

function Invoke-NexusJson
{
    param(
        [Parameter(Mandatory = $true)]
        [ValidateSet('Get', 'Post')]
        [string] $Method,

        [Parameter(Mandatory = $true)]
        [string] $Path,

        [Parameter()]
        [object] $Body
    )

    $invokeParams = @{
        Uri = "$nexusBaseUrl$Path"
        Method = $Method
        Credential = $credential
        TimeoutSec = 30
    }

    if ($null -ne $Body)
    {
        $invokeParams['ContentType'] = 'application/json'
        $invokeParams['Body'] = ($Body | ConvertTo-Json -Depth 10)
    }

    Invoke-RestMethod @invokeParams
}

$repositories = Invoke-NexusJson -Method Get -Path '/service/rest/v1/repositories'
$repositoryNames = @($repositories | ForEach-Object { $_.name })

function Ensure-NpmProxyRepository
{
    param(
        [string] $Name,
        [string] $RemoteUrl
    )

    if ($repositoryNames -contains $Name)
    {
        Write-Host "Nexus repository '$Name' already exists."
        return
    }

    Invoke-NexusJson -Method Post -Path '/service/rest/v1/repositories/npm/proxy' -Body @{
        name = $Name
        online = $true
        storage = @{
            blobStoreName = 'default'
            strictContentTypeValidation = $true
        }
        proxy = @{
            remoteUrl = $RemoteUrl
            contentMaxAge = 1440
            metadataMaxAge = 1440
        }
        negativeCache = @{
            enabled = $true
            timeToLive = 1440
        }
        httpClient = @{
            blocked = $false
            autoBlock = $true
        }
    } | Out-Null

    $script:repositoryNames += $Name
    Write-Host "Created Nexus repository '$Name'."
}

function Ensure-NpmHostedRepository
{
    param(
        [string] $Name
    )

    if ($repositoryNames -contains $Name)
    {
        Write-Host "Nexus repository '$Name' already exists."
        return
    }

    Invoke-NexusJson -Method Post -Path '/service/rest/v1/repositories/npm/hosted' -Body @{
        name = $Name
        online = $true
        storage = @{
            blobStoreName = 'default'
            strictContentTypeValidation = $true
            writePolicy = 'ALLOW'
        }
    } | Out-Null

    $script:repositoryNames += $Name
    Write-Host "Created Nexus repository '$Name'."
}

function Ensure-NpmGroupRepository
{
    param(
        [string] $Name,
        [string[]] $Members
    )

    if ($repositoryNames -contains $Name)
    {
        Write-Host "Nexus repository '$Name' already exists."
        return
    }

    Invoke-NexusJson -Method Post -Path '/service/rest/v1/repositories/npm/group' -Body @{
        name = $Name
        online = $true
        storage = @{
            blobStoreName = 'default'
            strictContentTypeValidation = $true
        }
        group = @{
            memberNames = $Members
        }
    } | Out-Null

    $script:repositoryNames += $Name
    Write-Host "Created Nexus repository '$Name'."
}

Ensure-NpmProxyRepository -Name 'npm-proxy' -RemoteUrl 'https://registry.npmjs.org'
Ensure-NpmHostedRepository -Name 'npm-hosted'
Ensure-NpmGroupRepository -Name 'npm-public' -Members @('npm-hosted', 'npm-proxy')
