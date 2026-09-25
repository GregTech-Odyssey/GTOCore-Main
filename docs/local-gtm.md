# 使用本地最新 GTM 联调

仅在任务需要使用本地 GTM 源码、Maven Local 或尚未发布的 GTM 改动时读取本文。Gradle 命令必须遵守仓库根目录 [AGENTS.md](../AGENTS.md) 的 JDK 21 前置要求。

## 依赖方式

主仓不会直接读取本地 GTM 工作区，而是通过 `dependencies.gradle` 中的 Maven 坐标 `com.gregtechceu.gtceu:gtceu-1.20.1-forge-1.20.1:${gtceu_version}` 使用 GTM。GTM 源码位于 [GregTech-Odyssey/GregTech-Modern](https://github.com/GregTech-Odyssey/GregTech-Modern)。主仓正常构建不要求本地检出 GTM；确需本地联调时，默认查找与主仓同级的 `GregTech-Modern/`。若未找到，停止联调并请用户提供已有检出的路径或决定是否克隆，不自行克隆。

联调前确认：

- GTM `gradle.properties` 的 `mod_version` 与主仓 `gradle.properties` 的 `gtceu_version` 完全一致；
- 主仓 `gradle/scripts/repositories.gradle` 已将 `mavenLocal()` 放在远端仓库之前；
- 不要把 jar 复制到 `libs/`，也不要改成文件依赖。

## 发布与刷新

```powershell
$gtocoreRepoPath = (Get-Location).Path
$gtmRepoPath = Join-Path (Split-Path -Parent $gtocoreRepoPath) 'GregTech-Modern'
if (-not (Test-Path -LiteralPath (Join-Path $gtmRepoPath 'gradlew.bat'))) { throw '未找到同级 GTM 仓库；停止并向用户确认实际路径' }
Set-Location -LiteralPath $gtmRepoPath
$env:JAVA_HOME = '<JDK_21_HOME>'
if (-not (Test-Path -LiteralPath "$env:JAVA_HOME\bin\java.exe")) { throw 'Valid JDK 21 required' }
.\gradlew.bat publishToMavenLocal

Set-Location -LiteralPath $gtocoreRepoPath
$env:JAVA_HOME = '<JDK_21_HOME>'
.\gradlew.bat --refresh-dependencies <task>
```

发布成功后，应能在 `$HOME\.m2\repository\com\gregtechceu\gtceu\gtceu-1.20.1-forge-1.20.1\<version>\` 找到 jar、sources jar、POM 与 Gradle module metadata。

同一版本反复发布时，主仓第一次解析必须带 `--refresh-dependencies`；不要通过删除整个 Gradle 缓存解决。

## 验证与发布边界

- 只需确认主仓编译可用时运行 `compileJava`。
- 需要实际联调时运行 `runClient`。
- 若 GTM API、映射或 Mixin 目标变化会影响 GTOLib，在收尾时执行一次 `-PgtolibRebuild=true buildGtolibProtected`，再运行主仓任务。
- Maven Local 只对当前机器生效。主仓若依赖尚未公开发布的 GTM 改动，只能用于本地联调。
- 推送给 CI 或其他开发者前，必须先推送 GTM commit，将对应版本发布到共享 Maven，再更新主仓的 `gtceu_version`。
- 不得把本地 Maven 产物、Gradle 缓存或临时 GTM jar 提交进主仓。
