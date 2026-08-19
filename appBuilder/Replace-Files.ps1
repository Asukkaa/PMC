# ======================================================
# 用户配置区域
# ======================================================
$sourceJarPath = "C:\Users\asus\.m2\repository\org\bytedeco"
$internalPath = ""
$destinationDir = Join-Path $PSScriptRoot "win"   # 可改为 "mac" 或 "mac_arm"

# ======================================================
# 脚本主体
# ======================================================
Add-Type -AssemblyName System.IO.Compression.FileSystem

# ---------- 根据目标目录名称决定平台关键词和扩展名 ----------
$destFolderName = Split-Path $destinationDir -Leaf
if ($destFolderName -match 'win')
{
    $platformKeyword = "windows-x86_64"
    $allowedExtensions = @(".dll")
}
elseif ($destFolderName -match 'mac_arm')
{
    $platformKeyword = "macosx-arm64"
    $allowedExtensions = @(".dylib")
}
elseif ($destFolderName -match 'mac')
{
    $platformKeyword = "macosx-x86_64"
    $allowedExtensions = @(".dylib")
}
else
{
    $platformKeyword = $null
    $allowedExtensions = @(".dll", ".dylib")
}

$allPlatformKeywords = @("windows-x86_64", "macosx-x86_64", "macosx-arm64", "linux-x86_64", "linux-arm64")

# ---------- 辅助函数：提取库名和版本号 ----------
function Get-LibAndVersion($jarPath)
{
    $parts = $jarPath -split '\\'
    if ($parts.Count -ge 3)
    {
        $libName = $parts[-3]
        $versionStr = $parts[-2]
        return $libName, $versionStr
    }
    return $null, $null
}

# ---------- 获取所有 JAR 文件 ----------
if (Test-Path $sourceJarPath -PathType Container)
{
    $allJarFiles = Get-ChildItem -Path $sourceJarPath -Recurse -Filter "*.jar" | ForEach-Object { $_.FullName }
}
elseif (Test-Path $sourceJarPath -PathType Leaf)
{
    $allJarFiles = @($sourceJarPath)
}
else
{
    Write-Error "源路径不存在: $sourceJarPath"
    exit 1
}

if ($allJarFiles.Count -eq 0)
{
    Write-Host "未找到任何 JAR 文件。"
    exit 0
}

# ---------- 平台筛选 + 按库名分组，优先选择平台包 ----------
Write-Host "目标目录: $destinationDir"
Write-Host "平台关键词: $( if ($platformKeyword)
{
    $platformKeyword
}
else
{
    '无限制'
} )"
Write-Host ""

$libGroups = @{ }
foreach ($jar in $allJarFiles)
{
    $lib, $ver = Get-LibAndVersion $jar
    if (-not $lib -or -not $ver)
    {
        continue
    }
    if (-not $libGroups.ContainsKey($lib))
    {
        $libGroups[$lib] = @()
    }
    $libGroups[$lib] += @{ Path = $jar; Version = $ver }
}

$latestJars = @{ }
foreach ($lib in $libGroups.Keys)
{
    $jars = $libGroups[$lib]
    $platformJars = @()
    $genericJars = @()
    foreach ($jarInfo in $jars)
    {
        $path = $jarInfo.Path
        if ($platformKeyword)
        {
            if ($path -match $platformKeyword)
            {
                $platformJars += $jarInfo
            }
            else
            {
                $hasPlatform = $false
                foreach ($kw in $allPlatformKeywords)
                {
                    if ($path -match $kw)
                    {
                        $hasPlatform = $true
                        break
                    }
                }
                if (-not $hasPlatform)
                {
                    $genericJars += $jarInfo
                }
            }
        }
        else
        {
            $genericJars += $jarInfo
        }
    }

    $selectedJar = $null
    if ($platformJars.Count -gt 0)
    {
        $selectedJar = $platformJars | Sort-Object -Property {
            $ver = $_.Version
            if ($ver -match '^(\d+\.\d+\.\d+)')
            {
                [Version]$matches[1]
            }
            else
            {
                [Version]"0.0.0"
            }
        } -Descending | Select-Object -First 1
    }
    elseif ($genericJars.Count -gt 0)
    {
        $selectedJar = $genericJars | Sort-Object -Property {
            $ver = $_.Version
            if ($ver -match '^(\d+\.\d+\.\d+)')
            {
                [Version]$matches[1]
            }
            else
            {
                [Version]"0.0.0"
            }
        } -Descending | Select-Object -First 1
    }

    if ($selectedJar)
    {
        $latestJars[$lib] = @{ Path = $selectedJar.Path; Version = $selectedJar.Version }
    }
}

$jarFiles = $latestJars.Values | ForEach-Object { $_.Path }
Write-Host "筛选后保留 $( $jarFiles.Count ) 个 JAR（优先平台包）。`n"

if ($jarFiles.Count -eq 0)
{
    Write-Host "未找到可处理的 JAR 文件。"
    exit 0
}

# ---------- 检查目标目录 ----------
if (-not (Test-Path $destinationDir))
{
    Write-Error "目标目录不存在: $destinationDir"
    exit 1
}

$existingFiles = Get-ChildItem -Path $destinationDir -File
if ($existingFiles.Count -eq 0)
{
    Write-Host "目标目录为空，无需操作。"
    exit 0
}

$patterns = @()
$patternExample = @{ }
foreach ($file in $existingFiles)
{
    $baseName = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    if ($baseName -match '\d+$')
    {
        $trimmed = $baseName -replace '\d+$', ''
        if ($trimmed -eq "")
        {
            $trimmed = $baseName
        }
        $pattern = "$trimmed*"
    }
    else
    {
        $pattern = $baseName
    }
    if ($patterns -notcontains $pattern)
    {
        $patterns += $pattern
        $patternExample[$pattern] = $file.Name
    }
}

Write-Host "目标目录匹配模式："
$patterns | ForEach-Object { Write-Host "  $_" }

if ($internalPath -and $internalPath -ne "")
{
    $normalizedInternal = $internalPath.TrimEnd('/') + '/'
    Write-Host "JAR 内部目录过滤: $normalizedInternal"
}
else
{
    $normalizedInternal = $null
    Write-Host "JAR 内部目录过滤: 无（查找整个 JAR）"
}
Write-Host "提取扩展名: $( $allowedExtensions -join ', ' )`n"

# ========== 第一阶段：扫描所有 JAR，收集匹配条目 ==========
Write-Host "正在扫描所有 JAR 中的匹配文件..."
$allCandidates = @()

foreach ($jarPath in $jarFiles)
{
    try
    {
        $zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
        $entries = $zip.Entries

        foreach ($entry in $entries)
        {
            $entryPath = $entry.FullName -replace '\\', '/'

            if ($normalizedInternal)
            {
                if (-not $entryPath.StartsWith($normalizedInternal))
                {
                    continue
                }
            }

            $ext = [System.IO.Path]::GetExtension($entry.Name)
            if ($allowedExtensions -notcontains $ext)
            {
                continue
            }

            $entryBase = [System.IO.Path]::GetFileNameWithoutExtension($entry.Name)
            $matched = $false
            $matchedPattern = $null
            foreach ($pattern in $patterns)
            {
                if ($pattern -like '*')
                {
                    $patternBase = $pattern -replace '\*$', ''
                    if ($entryBase -like "$patternBase*")
                    {
                        $rest = $entryBase.Substring($patternBase.Length)
                        if ($rest -eq "" -or $rest -match '^\d+$')
                        {
                            $matched = $true
                            $matchedPattern = $pattern
                            break
                        }
                    }
                }
                else
                {
                    if ($entryBase -eq $pattern)
                    {
                        $matched = $true
                        $matchedPattern = $pattern
                        break
                    }
                }
            }
            if (-not $matched)
            {
                continue
            }

            $oldFileName = $patternExample[$matchedPattern]
            $groupKey = $entryBase -replace '\d+$', ''
            if ($groupKey -eq "")
            {
                $groupKey = $entryBase
            }

            $lastWrite = $entry.LastWriteTime
            $lastWriteLocal = if ($lastWrite.HasValue)
            {
                $lastWrite.Value.LocalDateTime
            }
            else
            {
                $null
            }

            $allCandidates += [PSCustomObject]@{
                JarPath = $jarPath
                EntryFullName = $entry.FullName
                EntryName = $entry.Name
                EntryPath = $entryPath
                BaseName = $entryBase
                GroupKey = $groupKey
                LastWriteTime = $lastWriteLocal
                MatchedPattern = $matchedPattern
                OldFileName = $oldFileName
                Entry = $entry  # 保留对 ZipArchiveEntry 的引用用于提取（但之后会重新打开）
            }
        }
        $zip.Dispose()
    }
    catch
    {
        Write-Error "扫描 $jarPath 时出错: $_"
        continue
    }
}

Write-Host "共扫描到 $( $allCandidates.Count ) 个候选文件。`n"

if ($allCandidates.Count -eq 0)
{
    Write-Host "警告：未匹配到任何文件。"
    Write-Host "当前匹配模式："
    $patterns | ForEach-Object { Write-Host "  $_" }
    Write-Host "筛选后的 JAR 列表："
    $jarFiles | ForEach-Object { Write-Host "  $( Split-Path $_ -Leaf )" }
    exit 0
}

# ========== 第二阶段：手动分组，每组只保留最新的 ==========
$grouped = @{ }
foreach ($candidate in $allCandidates)
{
    $key = $candidate.GroupKey
    if (-not $grouped.ContainsKey($key))
    {
        $grouped[$key] = @()
    }
    $grouped[$key] += $candidate
}

Write-Host "分组情况："
foreach ($key in $grouped.Keys)
{
    Write-Host "  GroupKey: '$key', 成员数: $( $grouped[$key].Count )"
}

$selectedEntries = @()
foreach ($key in $grouped.Keys)
{
    $items = $grouped[$key]
    $latest = $items | Sort-Object -Property LastWriteTime -Descending | Select-Object -First 1
    if ($latest)
    {
        $selectedEntries += $latest
    }
}

Write-Host "`n按基础名分组后，将提取 $( $selectedEntries.Count ) 个文件（每组最新）。`n"

if ($selectedEntries.Count -eq 0)
{
    Write-Host "警告：未提取任何文件，请检查配置。"
    exit 0
}

# ========== 第三阶段：按 JAR 分组，重新打开提取 ==========
$extractedByJar = @{ }
foreach ($sel in $selectedEntries)
{
    $jar = $sel.JarPath
    if (-not $extractedByJar.ContainsKey($jar))
    {
        $extractedByJar[$jar] = @()
    }
    $extractedByJar[$jar] += $sel
}

$totalExtracted = 0

foreach ($jar in $extractedByJar.Keys)
{
    $list = $extractedByJar[$jar]
    Write-Host "从 JAR: $jar"
    Write-Host "提取了以下 $( $list.Count ) 个文件："
    try
    {
        $zip = [System.IO.Compression.ZipFile]::OpenRead($jar)
        foreach ($sel in $list)
        {
            $entry = $zip.GetEntry($sel.EntryFullName)
            if ($entry -eq $null)
            {
                Write-Warning "  找不到条目: $( $sel.EntryFullName )"
                continue
            }
            $destFile = Join-Path $destinationDir $sel.EntryName
            [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $destFile, $true)

            # ---------- 保留原始修改时间 ----------
            if ($entry.LastWriteTime.HasValue)
            {
                $destFileObj = Get-Item $destFile
                $destFileObj.LastWriteTime = $entry.LastWriteTime.Value.LocalDateTime
            }

            Write-Host "    $( $sel.OldFileName ) -> $( $sel.EntryName ) (内部路径: $( $sel.EntryPath ))"
            $totalExtracted++
        }
        $zip.Dispose()
    }
    catch
    {
        Write-Error "提取 $jar 时出错: $_"
    }
    Write-Host ""
}

Write-Host "`n全部完成！共提取 $totalExtracted 个文件（按基础名分组后只保留最新版本）。"
Write-Host "同名文件已覆盖，不同版本已按最新时间筛选。"
Write-Host "所有提取的文件已保留 JAR 中的原始修改时间。"