# AGENTS

面向本仓库（gtocore-main）协作与自动化代理的约定。公开介绍与外部贡献说明见 [README.md](README.md)。本文件只保留常驻规则与任务路由；专题说明仅在任务命中时读取，不要在每次工作前加载全部文档。

## 任务路由

先按实际改动选择需要读取的说明。一个任务命中多项时合并读取；范围不明确时先检查相关文件和 diff，再决定是否加载专题文档。

| 任务或改动 | 开始工作前读取 |
|---|---|
| 创建、修改或审查 Java/Kotlin 代码 | [CODING_GUIDELINES.md](CODING_GUIDELINES.md) 中与改动相关的章节 |
| 修改 `src/generated/resources`、语言、模型、标签、配方等生成结果 | [生成资源](docs/generated-resources.md) |
| 使用本地 GTM 源码联调、发布到 Maven Local 或刷新本地依赖 | [本地 GTM 联调](docs/local-gtm.md) |
| 操作 `GTOLib/`、`GTOSeal/`、预构建 jar、`.PROTECTED`、gitlink 或诊断 `M GTOLib` | [GTOLib 子模块与预构建](docs/gtolib.md) |
| 调试 gtocore 构建产物，或触发、排查、下载 Build and Sign 产物 | [云端构建与签名](docs/build-signing.md) |
| 提交/推送、`runData` 空转或崩溃、Gradle 产物被占用 | [Agent 常见坑与协作约定](docs/agent-pitfalls.md) |

纯文档、提示词或其他不涉及代码语义的修改不要求读取编码规范，也不要求运行 Gradle。

## 编码规范

- 首次编辑或审查 Java/Kotlin 代码前，必须打开 [CODING_GUIDELINES.md](CODING_GUIDELINES.md) 的相关章节；完成修改后按实际 diff 再检查一次。它是本仓性能与正确性约束，不是可选建议。
- 集合、Map/Set、遍历、复制、热路径分配或并发改动：读取“术语”“容器规范”“非容器规范”。
- `CustomItemStackHandler`：读取“物品库存 IO”。
- DataSyncLib、codec、持久化或网络同步：读取对应的编解码、网络与存盘章节。
- `createCustomRecipe` 或 `RecipeHandlerUnit`：读取“配方逻辑”。
- Level/维度级缓存、连接表或注册表：读取“世界级数据存储”。
- 改动跨越多个领域或无法判断适用范围时，读取全文。优先跟随仓库已有实现和容器类型，不另造平行模式。

## 构建与验证

- 代码推送到远程仓库前，或在本地执行 `build` / `assemble` 前，必须先按下方要求设置有效的 JDK 21，再使用 Spotless 应用并检查格式：

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat spotlessCheck
```

- **Windows agent 第一次调用 Gradle 前必须显式设置有效的 JDK 21**，并先确认 `java.exe` 存在：

```powershell
$env:JAVA_HOME = '<JDK_21_HOME>'
if (-not (Test-Path -LiteralPath "$env:JAVA_HOME\bin\java.exe")) { throw 'Valid JDK 21 required' }
.\gradlew.bat <task>
```

- 主仓、子模块和本地 GTM 仓库（如有）均遵守上述要求。禁止先裸跑 Gradle 等待 `JAVA_HOME` 报错后再重试；仓库文档与命令示例禁止写入具体用户名或用户目录绝对路径。
- Gradle 会写入用户级缓存。若当前宿主限制实际缓存路径，按宿主权限机制在第一次调用时处理；不要先运行一个已知会因缓存权限失败的试跑。
- `build` / `assemble` 只构建，不会自动运行单测。单测须显式执行 `test` 或 `testCrafting`；禁止添加凑数、明显正确或与行为无关的测试。
- `src/test/` 下的 `com.gtolib.*` 测试需要明文 GTOLib 字节码；只有 Seal hollow class 的 CI prebuild 会自动跳过这些测试。
- 验证应与改动范围相称。通常只需 `compileJava`、`compileKotlin`、相关测试或专题文档要求的 `runData` / `buildGtolibProtected`，不要为纯说明修改运行 Gradle。
- 任务报「另一个程序正在使用此文件」（如 `libs/gto-seal-runtime-1.0.jar`）时先 `.\gradlew.bat --stop`，再单独跑目标任务；`runData` 至少 1~3 分钟且可能因 datagen 崩溃偶发失败，先重试再排查。详见 [Agent 常见坑与协作约定](docs/agent-pitfalls.md)。
- 用 PowerShell 管道接 `gradlew` 输出会让 `$LASTEXITCODE` 失真（可能拿到 -1）：先把输出存进变量，再读退出码。

## 生成资源

- `src/generated/resources` 是 data generator 输出。修改生成内容时必须改真实生成源并运行 `runData`；手工编辑或脚本改写生成文件只能用于诊断，不能作为最终结果。
- 语言文件的来源、简繁转换与 HashCache 强制回写方法见 [生成资源](docs/generated-resources.md)。
- 用 `git reset` / `git checkout --` / 合并 / 手工改写动过生成文件后再跑 `runData` 可能空转（成功但文件没变）：按 `docs/generated-resources.md` 删掉含 `Registrate Provider for gtocore` 的 `.cache` 清单并加 `--rerun-tasks`；跑完对比文件 mtime 确认真的写了。

## GTOLib 与预构建安全

- `GTOLib/` 与 `GTOSeal/` 都是私有子模块，只有具备对应仓库权限的人员或执行环境才能初始化、拉取和更新。无权限时子模块不可用是预期状态；不要反复重试、绕过权限或要求提供凭据。只涉及主仓的任务应继续使用仓库内预构建产物完成可行工作。
- `GTOLib/` 与 `GTOSeal/` 是独立 Git 仓库；主仓记录的是 gitlink。修改、切换或恢复它们前读取 [GTOLib 子模块与预构建](docs/gtolib.md)。
- 修改 `GTOLib/` 代码时，完成当前任务前必须使用 JDK 21 运行一次 `buildGtolibProtected` 并确认通过。一个任务在收尾时重建一次即可，不要每改一处都重建；若构建失败，应明确任务尚未验证完成，不得作为正常完成结果收尾。
- GTOLib 指针、`libs/gtolib-protected.jar` 与对应 `.PROTECTED` 是不可拆分的一套生成物；不得单独合并、手工拼接侧车或只更新其中一项。
- `gtolib-unprotected*.jar` 始终不得提交、上传或打包分发；不要绕过 gitignore。开发运行需要 named 类，生产链使用 SRG，勿用 reobf 产物覆盖明文调试 jar。

## 云端签名

- 本地 jar 仅用于开发调试；进入整合包必须使用 Build and Sign 产生的签名产物。
- `pull_request_target` 会执行 PR 侧代码并注入签名 secrets。成员评论 `/build` 即代表已审阅并接受签名风险；触发条件与产物查找见 [云端构建与签名](docs/build-signing.md)。

## 收尾

- 保留用户已有和无关改动，只处理当前任务范围内的文件。
- 提交只带自己的路径（`git commit --only <paths>` 或 `git commit -F msg -- <paths>`）：同一工作区里可能有别人并行 `git add`，不带路径的 `git commit` 与 `git commit --amend` 会把对方的暂存一起提交。误提交后用 `git reset --mixed <上一个好提交>` 重做，再把对方原本暂存的文件原样 `git add` 回去。
- 推送前先 `git fetch` 并用 `git rev-list --left-right --count origin/<branch>...HEAD` 看差距：远端可能已有内容等价的提交，这种情况不要 force，reset 到远端后只补真正的增量。详细流程与其它坑见 [Agent 常见坑与协作约定](docs/agent-pitfalls.md)。
- 报告实际执行的验证；若某项必要检查不可运行，说明具体原因，不用无关检查代替。
- 若本次修改新增了可长期复用的仓库约束，将简短触发条件放在本文件，将详细流程放进 `docs/`，避免再次把全部背景塞回常驻上下文。
