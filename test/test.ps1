# test_script_no_pause.ps1 - No pause version
param(
    [string]$Param1,
    [string]$Param2,
    [string]$Param3
)

Write-Host "=== PowerShell Script Test ==="

# Output working directory
$workingDir = Get-Location
Write-Host "Working Directory: $workingDir"

# Output parameters
Write-Host "Parameters received:"
Write-Host "  Param1: $Param1"
Write-Host "  Param2: $Param2"
Write-Host "  Param3: $Param3"

# 3-second countdown
Write-Host "Starting 3-second countdown..."
for ($i = 3; $i -gt 0; $i--) {
    Write-Host "Countdown: $i seconds"
    Start-Sleep -Seconds 1
}
Write-Host "Countdown finished!"

Write-Host "Script execution completed!"