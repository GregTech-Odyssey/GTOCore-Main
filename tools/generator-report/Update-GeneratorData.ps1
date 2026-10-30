#requires -Version 7.0
[CmdletBinding()]
param(
    [string]$RepoPath = (Join-Path $PSScriptRoot '../..'),
    [string]$GtmPath,
    [string]$OutputDirectory,
    [string]$RuntimeRoot,
    [ValidateSet('Easy', 'Normal', 'Expert')]
    [string]$Difficulty = 'Normal',
    [switch]$RenderPreviews
)

$ErrorActionPreference = 'Stop'
$RepoPath = (Resolve-Path -LiteralPath $RepoPath).Path
if (-not $GtmPath) {
    $GtmPath = Join-Path (Split-Path -Parent $RepoPath) 'GregTech-Modern'
}
$GtmPath = (Resolve-Path -LiteralPath $GtmPath).Path
if (-not $OutputDirectory) {
    $OutputDirectory = Join-Path $RepoPath 'outputs/generator-report'
}
$OutputDirectory = [System.IO.Path]::GetFullPath($OutputDirectory)

# 核对手工建模的机器机制。燃料配方的数量、时长等由提取器每次重新读取。
$baseline = Get-Content -Raw -LiteralPath (Join-Path $PSScriptRoot 'mechanism_baseline.json') | ConvertFrom-Json -AsHashtable
$changedSources = [System.Collections.Generic.List[string]]::new()
foreach ($group in @(@{ Root = $RepoPath; Hashes = $baseline.gtocore }, @{ Root = $GtmPath; Hashes = $baseline.gtm })) {
    foreach ($entry in $group.Hashes.GetEnumerator()) {
        $sourceFile = Join-Path $group.Root $entry.Key
        if (-not (Test-Path -LiteralPath $sourceFile -PathType Leaf)) {
            $changedSources.Add($sourceFile)
            continue
        }
        if ((Get-FileHash -LiteralPath $sourceFile -Algorithm SHA256).Hash -ne $entry.Value) {
            $changedSources.Add($sourceFile)
        }
    }
}
if ($changedSources.Count -gt 0) {
    throw ('以下机制源码与计算模板基线不同，需要复核并更新模板；本次未生成表格：' + [Environment]::NewLine + ($changedSources -join [Environment]::NewLine))
}

if (-not $RuntimeRoot) {
    $runtimeCandidates = [System.Collections.Generic.List[string]]::new()
    if ($env:CODEX_HOME) {
        $runtimeCandidates.Add((Join-Path (Split-Path -Parent $env:CODEX_HOME) '.cache/codex-runtimes/codex-primary-runtime/dependencies'))
    }
    $runtimeCandidates.Add((Join-Path ([Environment]::GetFolderPath('UserProfile')) '.cache/codex-runtimes/codex-primary-runtime/dependencies'))
    foreach ($candidate in $runtimeCandidates) {
        if (Test-Path -LiteralPath (Join-Path $candidate 'node/node_modules/@oai/artifact-tool')) {
            $RuntimeRoot = $candidate
            break
        }
    }
}
if (-not $RuntimeRoot) {
    throw '未找到Codex文档运行时。请使用-RuntimeRoot指定dependencies目录；脚本不会下载或安装依赖。'
}
$RuntimeRoot = (Resolve-Path -LiteralPath $RuntimeRoot).Path
$nodeExecutable = Join-Path $RuntimeRoot 'node/bin/node.exe'
$pythonExecutable = Join-Path $RuntimeRoot 'python/python.exe'
$nodePackages = Join-Path $RuntimeRoot 'node/node_modules'
foreach ($requiredPath in @($nodeExecutable, $pythonExecutable, (Join-Path $nodePackages '@oai/artifact-tool'))) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw ('缺少运行时依赖：' + $requiredPath)
    }
}

$temporaryRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
$jobDirectory = Join-Path $temporaryRoot ('gtocore-generator-report-' + [Guid]::NewGuid().ToString('N'))
$packageLink = Join-Path $jobDirectory 'node_modules'
New-Item -ItemType Directory -Path $jobDirectory | Out-Null
try {
    New-Item -ItemType Junction -Path $packageLink -Target $nodePackages | Out-Null
    $builderPath = Join-Path $jobDirectory 'build_report.mjs'
    $dataPath = Join-Path $jobDirectory 'extracted.json'
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'build_report.mjs') -Destination $builderPath
    Write-Host '正在提取当前工作区的燃料与制造配方……'
    & $pythonExecutable -X utf8 (Join-Path $PSScriptRoot 'extract_data.py') --repo $RepoPath --gtm $GtmPath --output $dataPath
    if ($LASTEXITCODE -ne 0) { throw '源码数据提取失败，未生成新的表格。' }

    New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
    $difficultyNumber = @{ Easy = 1; Normal = 2; Expert = 3 }[$Difficulty]
    $builderArguments = @($builderPath, $dataPath, $OutputDirectory, $difficultyNumber)
    if ($RenderPreviews) { $builderArguments += (Join-Path $OutputDirectory '预览') }
    Write-Host '正在计算、校验并生成中文版Excel……'
    & $nodeExecutable @builderArguments
    if ($LASTEXITCODE -ne 0) { throw '表格计算或导出失败，请检查上方具体错误。' }
    $workbookPath = Join-Path $OutputDirectory '发电机与燃料数据表_中文版.xlsx'
    if (-not (Test-Path -LiteralPath $workbookPath -PathType Leaf)) { throw '未找到导出的工作簿。' }
    [pscustomobject]@{ Workbook = $workbookPath; Difficulty = $Difficulty }
} finally {
    # 先删除目录联接，保证清理临时目录时不会遍历运行时依赖。
    $resolvedJob = [System.IO.Path]::GetFullPath($jobDirectory)
    $rootPrefix = $temporaryRoot.TrimEnd([System.IO.Path]::DirectorySeparatorChar) + [System.IO.Path]::DirectorySeparatorChar
    if (-not $resolvedJob.StartsWith($rootPrefix, [StringComparison]::OrdinalIgnoreCase) -or
        -not ([System.IO.Path]::GetFileName($resolvedJob)).StartsWith('gtocore-generator-report-', [StringComparison]::Ordinal)) {
        throw '临时目录路径校验失败，已停止自动清理。'
    }
    if (Test-Path -LiteralPath $packageLink) {
        $linkItem = Get-Item -LiteralPath $packageLink
        if (-not ($linkItem.Attributes -band [System.IO.FileAttributes]::ReparsePoint)) {
            throw '依赖路径不是目录联接，已停止自动清理。'
        }
        Remove-Item -LiteralPath $packageLink -Force
    }
    Remove-Item -LiteralPath $resolvedJob -Recurse -Force
}
