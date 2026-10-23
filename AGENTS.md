# AGENTS

面向本仓库（gtocore-main）协作与自动化代理的约定。公开介绍见 [README.md](README.md)，团队开发环境、构建命令与分支规则见 [docs/development.md](docs/development.md)。README 是对外文件，只写项目介绍、闭源声明与参与方式（想法走 Issue、代码由团队完成、公开环境不提供构建），不要写入构建步骤、内部流程或权限细节。本文件只保留常驻规则与任务路由；专题说明仅在任务命中时读取，不要在每次工作前加载全部文档。

## 任务路由

先按实际改动选择需要读取的说明。一个任务命中多项时合并读取；范围不明确时先检查相关文件和 diff，再决定是否加载专题文档。

| 任务或改动 | 开始工作前读取 |
|---|---|
| 创建、修改或审查 Java/Kotlin 代码 | [CODING_GUIDELINES.md](CODING_GUIDELINES.md) 中与改动相关的章节 |
| 修改 `src/generated/resources`、语言、模型、标签、配方等生成结果 | [生成资源](docs/generated-resources.md) |
| 修改 GTORules 的世界创建、读取或持久化流程 | [世界规则只读约定](docs/world-rules.md)；规则仅在创建世界时确定，已有世界禁止补写、迁移回写或修复改写 |
| 定位或修复 GTM/AE2 行为、考虑在主仓增加 Mixin 或调整这两个依赖 | [GTM 与 AE2 fork 的修改位置](docs/forked-dependencies.md) |
| 修改 EMI 实现、AE/EMI 转换或 EMI 依赖 | [EMI 与 AE 的职责边界](docs/emi-integration.md) |
| EMI 启动兼容性、配方/搜索性能基准或大规模目录回归 | [EMI benchmark](docs/emi-benchmark.md) |
| 使用本地 GTM 源码联调、发布到 Maven Local 或刷新本地依赖 | [本地 GTM 联调](docs/local-gtm.md) |
| 操作 `GTOLib/`、`GTOSeal/`、GTOLib 构建缓存（`build/gtolib/`）、gitlink 或诊断 `M GTOLib` | [GTOLib 子模块与构建](docs/gtolib.md) |
| 调试 gtocore 构建产物，或触发、排查、下载 Build and Sign 产物 | [云端构建与签名](docs/build-signing.md) |
| 报错日志或崩溃报告里 `com.gtocore` / `com.gtolib` 的栈帧是代码保护后的名字，需要对回源码名 | [代码保护报错栈对回源码名](GTOLib/docs/deobfuscate-stacktrace.md)（GTOLib 仓） |
| 提交/推送、`runData` 空转或崩溃、Gradle 产物被占用、LeanObject/AE 启动兼容性 | [Agent 常见坑与协作约定](docs/agent-pitfalls.md) |
| 搭建开发环境、IDE 导入、分支与 PR 要求，或修改 README | [团队开发说明](docs/development.md) |

纯文档、提示词或其他不涉及代码语义的修改不要求读取编码规范，也不要求运行 Gradle。

GTM 与 AE2 均有本项目维护的 fork。修复涉及它们内部行为时，先判断是否应直接改对应 fork，不默认在 GTOCore 添加 Mixin；根据 [修改位置说明](docs/forked-dependencies.md) 权衡。

## 编码规范

- 首次编辑或审查 Java/Kotlin 代码前，必须打开 [CODING_GUIDELINES.md](CODING_GUIDELINES.md) 的相关章节；完成修改后按实际 diff 再检查一次。它是本仓性能与正确性约束，不是可选建议。
- 集合、Map/Set、遍历、复制、热路径分配或并发改动：读取“术语”“容器规范”“非容器规范”。
- 异常处理、同步执行的状态切换或公共方法/API 设计：读取“非容器规范”。
- `CustomItemStackHandler`：读取“物品库存 IO”。
- DataSyncLib、codec、持久化或网络同步：读取对应的编解码、网络与存盘章节。
- 配方搜索、机器配方扩展、`RecipeLogic` / `RecipeLogicExt`、`createCustomRecipe` 或 `RecipeHandlerUnit`：读取“配方逻辑”。
- Level/维度级缓存、连接表或注册表：读取“世界级数据存储”。
- 由注册表、配方、标签派生的缓存或索引，或考虑数据包重载：读取“数据包与重载”。
- 注册 Forge / mod 事件总线监听（新写处理器、`@SubscribeEvent`、`EventBusSubscriber`、`addListener`），或需要给已有处理器补接线入口：读取“事件注册”。`com.gtocore` 包（`mixin` 除外）已禁用 `@SubscribeEvent`，发行构建会直接失败。
- 改动跨越多个领域或无法判断适用范围时，读取全文。优先跟随仓库已有实现和容器类型，不另造平行模式。

## 依赖源码与性能（硬性）

- **只以本仓 fork 的实际源码为准**：GTM 读 `../GregTech-Modern`，AE2 读 GTO 的 AE fork（`ae2_version` 指向的 `appeng:appliedenergistics2-forge-1.20.1:*`，本地解 jar/`javap` 或用仓库里的调用点）。不要读上游 AE/GT 官方源码，不要凭记忆或"看起来是这么回事"写实现；方法是否存在、签名与语义以实际字节码/源码为准（见 [GTM 与 AE2 fork 的修改位置](docs/forked-dependencies.md)）。
- **性能优先，不写自造遍历**：能在既有容器/API 上一次查到的，不写 `for` 全扫。典型例：模糊匹配用 GTO AE fork 在 `KeyCounter` 上的 `findFuzzyValue`（只要数量）/ `findFuzzyKey`（要具体 key）/ `containsFuzzy`（先判定再决定是否分配），语义与 `IExpandedStorageService#fuzzyExtract`、`crafting2` 取料一致；精确命中优先，未命中再走模糊。掉耐久物品（GT 工具、桶、流体容器）必须按"同类物品"匹配，`AEKey` 是引用类型，普通物品按引用比较。
- **魔力、魔源的转移只走标准传输**（2026-10-23，福鲁伊克斯魔力池虚增复制事故后统一）：两个持有者之间搬魔力一律 `ManaEndpoint.transfer(from, to, amount)`（植物魔法 API）；魔源一律 `ISourceTile.transfer(from, to, amount)`（新生魔艺 API）；GT 魔力容器和魔源方块按魔力单位接入用 `ManaUnification.of(container, 速率倍数)` / `ManaUnification.of(sourceTile)`。`transfer` 先模拟目标能收多少，再从来源扣，只把实际扣到的给目标，没给出去的退回来源。禁止手写"先给后扣"（`receiver.receiveMana(x)` 后再 `pool.receiveMana(-x)`）、禁止拿读数（`getCurrentMana`/`getSource`）代替实际变化量记账；单边加减用 `addMana/removeMana(amount, simulate)`、`addSource/removeSource(amount, simulate)`，按返回值记账。机器里的端点对象建一次存字段，不在 tick 里新建。改动这类代码要跑守恒探针（`tools/probe-templates/mana-transfer/2026-10-23/`）。
- **热路径零无谓分配**：先精确查 `get`/`contains`，需要集合时用仓库既有类型（`AEKeyLongMap`/`KeyCounter`/`ObjectArrayList`/`ReferenceOpenHashSet`），不要 streams、`Optional`、临时 `List` 装箱。
- **DataSyncLib 持久化/同步**：自定义类型字段用 `@SaveToDisk` 时**不加 `final`**（final 会走 `@Access` 工厂，定义建不起来），编解码必须在 `GTOCodecs.init()` 里注册（早于机器注册）；机器字段不要拿 `@SyncToClient` 当界面同步用，界面走 uipro 的 `SyncValue`/`RPC`，可变的列表类数据要给"内容变了就换新快照实例"的 getter（`SyncValue` 用 `Objects.equals` 判变化）。
- **uipro 界面**：尺寸按 `UISizes` 常量给死（例如 3×3 槽位所在行高必须是 `3 * SLOT_SIZE`，否则会压到下一行）；主页面用 `Form.page()` + `column/row/centeredRow` 竖排，不要给主页面套没必要的 `ScrollerView`；列表页用 `ScrollerView` + `VirtualList`/`AEStackGrid`。

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
- `src/test/` 下的 `com.gtolib.*` 测试需要明文 GTOLib 字节码，只在 `-PgtolibUnprotected=true` 下运行，加密构建会自动跳过。
- 验证应与改动范围相称。通常只需 `compileJava`、`compileKotlin`、相关测试或专题文档要求的 `runData` / `buildGtolibProtected`，不要为纯说明修改运行 Gradle。
- 任务报「另一个程序正在使用此文件」（如 `libs/gto-seal-runtime-1.0.jar`）时先 `.\gradlew.bat --stop`，再单独跑目标任务；`runData` 至少 1~3 分钟且可能因 datagen 崩溃偶发失败，先重试再排查。详见 [Agent 常见坑与协作约定](docs/agent-pitfalls.md)。
- 用 PowerShell 管道接 `gradlew` 输出会让 `$LASTEXITCODE` 失真（可能拿到 -1）：先把输出存进变量，再读退出码。

## 生成资源

- `src/generated/resources` 是 data generator 输出。修改生成内容时必须改真实生成源并运行 `runData`；手工编辑或脚本改写生成文件只能用于诊断，不能作为最终结果。
- 语言文件的来源、简繁转换与 HashCache 强制回写方法见 [生成资源](docs/generated-resources.md)。
- 用 `git reset` / `git checkout --` / 合并 / 手工改写动过生成文件后再跑 `runData` 可能空转（成功但文件没变）：按 `docs/generated-resources.md` 删掉含 `Registrate Provider for gtocore` 的 `.cache` 清单并加 `--rerun-tasks`；跑完对比文件 mtime 确认真的写了。

## GTOLib 与构建产物安全

- `GTOLib/` 与 `GTOSeal/` 都是私有子模块，只有具备对应仓库权限的人员或执行环境才能初始化、拉取和更新。无权限时子模块不可用是预期状态；不要反复重试、绕过权限或要求提供凭据。构建 gtocore 必须有 GTOLib 源码（缺失时配置阶段直接报错），公开仓不带任何 GTOLib 预构建。
- `GTOLib/` 与 `GTOSeal/` 是独立 Git 仓库；主仓记录的是 gitlink。修改、切换或恢复它们前读取 [GTOLib 子模块与构建](docs/gtolib.md)。
- 修改 `GTOLib/` 代码时，完成当前任务前必须使用 JDK 21 运行一次 `buildGtolibProtected` 并确认通过。一个任务在收尾时重建一次即可，不要每改一处都重建；若构建失败，应明确任务尚未验证完成，不得作为正常完成结果收尾。
- GTOLib 产物只生成在 `build/gtolib/`（按源码指纹缓存），始终不得提交、上传或打包分发；dev 轨保留类名/包名（`GTOLib/protect/proguard-rules.pro`），只有发行轨改名（`proguard-release-rules.pro`，字典每次构建重新打乱）。`libs/` 不再存放 GTOLib 产物，残留的旧 `libs/gtolib-*` 由 gitignore 兜底，不要绕过。开发运行需要 named 类，生产链使用 SRG，勿用 reobf 产物覆盖明文调试 jar。
- GTOLib 新增按名反射、注解扫描、存盘键或配置里写类名时，必须用 `com.gtolib.api.annotation.KeepName` 标注（类级保类名+包名，成员级保成员名+所属类名）；第三方或 GTOSeal Runtime 写死、改不了源码的名字再补 `GTOLib/protect/proguard-release-rules.pro` 的 `-keepnames`。开发环境测不出漏补。

## 云端签名

- 本地 jar 仅用于开发调试；进入整合包必须使用 Build and Sign 产生的签名产物。
- `pull_request_target` 会执行 PR 侧代码并注入签名 secrets 与 GTOLib 只读凭据，只构建本仓分支。成员评论 `/build` 即代表已审阅并接受签名风险；触发条件与产物查找见 [云端构建与签名](docs/build-signing.md)。

## 收尾

- 保留用户已有和无关改动，只处理当前任务范围内的文件。
- 提交只带自己的路径（`git commit --only <paths>` 或 `git commit -F msg -- <paths>`）：同一工作区里可能有别人并行 `git add`，不带路径的 `git commit` 与 `git commit --amend` 会把对方的暂存一起提交。误提交后用 `git reset --mixed <上一个好提交>` 重做，再把对方原本暂存的文件原样 `git add` 回去。
- 推送前先 `git fetch` 并用 `git rev-list --left-right --count origin/<branch>...HEAD` 看差距：远端可能已有内容等价的提交，这种情况不要 force，reset 到远端后只补真正的增量。详细流程与其它坑见 [Agent 常见坑与协作约定](docs/agent-pitfalls.md)。
- 报告实际执行的验证；若某项必要检查不可运行，说明具体原因，不用无关检查代替。
- 若本次修改新增了可长期复用的仓库约束，将简短触发条件放在本文件，将详细流程放进 `docs/`，避免再次把全部背景塞回常驻上下文。
