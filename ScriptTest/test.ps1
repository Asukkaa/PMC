param(
    [string]$Param1,
    [string]$Param2,
    [string]$Param3
)

# 切换到 UTF-8 代码页，并设置控制台输出编码
chcp 65001 > $null
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# ========== 版本信息 ==========
$psVersion = $PSVersionTable.PSVersion.ToString()
$hostInfo = $Host.Name
Write-Host "=== PowerShell 脚本测试 ==="
Write-Host "PowerShell 版本: $psVersion ($hostInfo)"

# ========== 工作目录 ==========
$workingDir = Get-Location
Write-Host "工作目录: $workingDir"

# ========== 参数 ==========
Write-Host "接收到的参数:"
Write-Host "  Param1: $Param1"
Write-Host "  Param2: $Param2"
Write-Host "  Param3: $Param3"

# ========== 当前目录文件列表 ==========
Write-Host "`n当前目录文件列表:"
Get-ChildItem -Force | ForEach-Object {
    $type = if ($_.PSIsContainer)
    {
        "[目录]"
    }
    else
    {
        "[文件]"
    }
    Write-Host "  $type $( $_.Name )"
}

# ========== 3 秒倒计时 ==========
Write-Host "`n开始 3 秒倒计时..."
for ($i = 3; $i -gt 0; $i--) {
    Write-Host "倒计时: $i 秒"
    Start-Sleep -Seconds 1
}
Write-Host "倒计时结束!"

Write-Host "`n脚本执行完成!"
