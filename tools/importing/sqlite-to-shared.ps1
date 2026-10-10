param(
    [Parameter(Mandatory = $true)][ValidateSet('inspect', 'apply')][string]$Action,
    [Parameter(Mandatory = $true)][string]$KiteMarketJar,
    [Parameter(Mandatory = $true)][string]$SourceCopy,
    [Parameter(Mandatory = $true)][Guid]$Network,
    [Parameter(Mandatory = $true)][string]$Report,
    [string]$TargetConfig,
    [string]$ExpectedSourceSha256,
    [string]$Java = 'java',
    [switch]$SourceStopped,
    [switch]$TargetStopped,
    [switch]$TargetEmpty
)
$ErrorActionPreference = 'Stop'
if (-not $SourceStopped) { throw '-SourceStopped is required after normal shutdown of every source node' }
$taskMigrationJar = (Resolve-Path -LiteralPath $KiteMarketJar).Path
$taskMigrationSource = (Resolve-Path -LiteralPath $SourceCopy).Path
$taskMigrationReport = [IO.Path]::GetFullPath($Report)
$taskMigrationArgs = @(
    '-cp', $taskMigrationJar, 'com.kitemc.market.core.importing.OfflineMigrationCli',
    $Action, '--source-copy', $taskMigrationSource, '--network', $Network.ToString(),
    '--report', $taskMigrationReport, '--source-stopped'
)
if ($Action -eq 'apply') {
    if (-not $TargetStopped -or -not $TargetEmpty) { throw '-TargetStopped and -TargetEmpty are required' }
    if ($ExpectedSourceSha256 -cnotmatch '^[a-f0-9]{64}$') { throw 'Use the SHA-256 from the reviewed inspect report' }
    $taskMigrationTarget = (Resolve-Path -LiteralPath $TargetConfig).Path
    $taskMigrationArgs += @('--target-config', $taskMigrationTarget,
        '--expected-source-sha256', $ExpectedSourceSha256, '--target-stopped', '--target-empty')
}
& $Java @taskMigrationArgs
if ($LASTEXITCODE -ne 0) { throw "Offline migration failed (exit $LASTEXITCODE); inspect retained evidence before retrying" }
