# AGENTS

面向本仓库（gtocore-main）协作与自动化代理的约定。公开介绍与外部贡献说明见 [README.md](README.md)。

## 构建与测试

- **Windows agent 运行 Gradle 时，第一次调用就必须显式设置有效的 JDK 21**：先从现有环境、工作区依赖配置或本机已安装运行时中确定 JDK 21，再执行 `$env:JAVA_HOME='<JDK_21_HOME>'; .\gradlew.bat <task>`；调用前应确认 `$env:JAVA_HOME\bin\java.exe` 存在。禁止先裸跑 `gradlew.bat` 等待 `JAVA_HOME` 报错后再重试；主仓、子模块和同级 GTM 仓库均遵守。仓库文档与命令示例中禁止写入具体用户名或用户目录绝对路径。
- **Gradle 会写入用户级缓存**（`GRADLE_USER_HOME`；未显式设置时通常位于 `$HOME\.gradle`，包括 wrapper 的 `.lck` 文件）。若当前沙箱权限不包含实际缓存路径，且任务已明确要求执行 Gradle，应在第一次调用时直接申请所需权限并运行上述完整命令；禁止先执行一个已知会因缓存写权限而失败的沙箱内试跑。
- **构建不跑单测**。`build` / `assemble` 只构建；单测须显式运行：`./gradlew test` 或 `./gradlew testCrafting`。**禁止添加凑数的、明显正确的、无意义的单元测试。**
- 单测（`src/test/`，全部在 `com.gtolib.*`）需要**明文 gtolib** 字节码（本地编译类或 `gtolib-unprotected.jar`）。CI prebuild（无 GTOLib 源码）只有 Seal 抽空的 hollow class，测试任务会自动跳过。
- dev 运行（runClient/runData/runServer）用的 gtolib mixin 类必须是 **named**（官方名）；生产链（ProGuard/Seal/maven）必须是 **SRG**（`m_/f_`）。流水线已自动处理，勿把 reobf 产物覆盖到 `gtolib-unprotected.jar`。
- **安全提示**：构建产出的 `gtolib-unprotected*.jar`（明文 gtolib）**始终不要上传到 git、不要 `git add`、不要打包进其他文件**（已被 gitignore，勿绕过），防止明文字节码被错误分发。
- **给 agent 的留言**：当用户在调试 gtocore 构建产物、或某次 push 触发了云端构建（见下节）时，用 `gh` CLI 找到对应的 workflow run 并把构建页面直接给用户打开（如 `gh run list --repo GregTech-Odyssey/GTOCore-Main`、`gh run view <run-id> --web`，页面形如 <https://github.com/GregTech-Odyssey/GTOCore-Main/actions/runs/29621547406>），然后引导用户在页面的 Artifacts 区下载签名产物。

## 使用本地最新 GTM 联调

- 主仓不会直接读取同级的 GTM 工作区，而是通过 `dependencies.gradle` 中的 Maven 坐标 `com.gregtechceu.gtceu:gtceu-1.20.1-forge-1.20.1:${gtceu_version}` 使用 GTM。联调前确认 GTM `gradle.properties` 的 `mod_version` 与主仓 `gradle.properties` 的 `gtceu_version` 完全一致；主仓的 `gradle/scripts/repositories.gradle` 已将 `mavenLocal()` 放在远端仓库之前，无须复制 jar 到 `libs/` 或改成文件依赖。
- 在同级 GTM 仓库用 JDK 21 发布到本机 Maven，再回到主仓刷新同版本依赖并执行需要的任务：

```powershell
Set-Location ..\GregTech-Modern
$env:JAVA_HOME='<JDK_21_HOME>'
.\gradlew.bat publishToMavenLocal

Set-Location ..\GTOCore-Main
$env:JAVA_HOME='<JDK_21_HOME>'
.\gradlew.bat --refresh-dependencies <task>
```

- `publishToMavenLocal` 成功后，应能在 `$HOME\.m2\repository\com\gregtechceu\gtceu\gtceu-1.20.1-forge-1.20.1\<version>\` 找到 jar、sources jar、POM 与 Gradle module metadata。同一版本反复本地发布时，主仓第一次解析必须带 `--refresh-dependencies`，不要靠删除整个 Gradle 缓存解决。
- 验证任务按改动范围选择：只需确认主仓编译可用时运行 `compileJava`；需要实际联调时运行 `runClient`；若 GTM 的 API、映射或 mixin 目标变化会影响 GTOLib，则按本文 GTOLib 规则在收尾时执行一次 `-PgtolibRebuild=true buildGtolibProtected`，再运行主仓任务。
- 本地 Maven 只对当前机器生效。主仓代码若依赖尚未公开发布的 GTM 改动，只能用于本地联调；推送给 CI 或其他开发者前，必须先推送 GTM commit，并把对应版本发布到共享 Maven，再更新主仓的 `gtceu_version`。不得把本地 Maven 产物、Gradle 缓存或临时 GTM jar 提交进主仓。

## 语言数据生成

- `src/generated/resources/assets/gtocore/lang/en_us.json`、`zh_cn.json`、`zh_tw.json` 均为 data generator 的输出，**禁止直接编辑或用脚本改写**。需要调整文案时，应修改 Java/Kotlin 中对应的翻译源，再运行 `./gradlew runData` 生成并检查 JSON；若生成结果不符合预期，继续修正翻译源，不以手改 JSON 作为最终结果。
- 注册入口是 `src/main/java/com/gtocore/data/Datagen.java`，语言数据由 `src/main/java/com/gtocore/data/lang/LangHandler.java` 聚合。其中 GTOLib 注解与动态翻译分别经 `ScanningClass.LANG`、`DynamicInitialData.LANG`、`TranslationKeyProvider.LANG` 汇入。
- provider 按 `enInitialize()` → `cnInitialize()` → `twInitialize()` 的注册顺序运行：`enInitialize()` 负责初始化完整的共享语言表并输出英文，`cnInitialize()` 输出简体中文，`twInitialize()` 使用 `ChineseConverter` 将同一份简体中文源转换为繁体中文。因此新增或修改中文文案时只维护翻译源中的简体中文，不单独维护 `zh_tw.json`。
- `runData --rerun-tasks` 只强制 Gradle 任务重跑，**不会绕过 Minecraft `HashCache`**。HashCache 比较本次生成哈希与 `src/generated/resources/.cache` 的清单，不校验磁盘上生成文件的实际内容；如果语言 JSON 曾被 `git restore`、合并或外部工具改写，缓存仍可能误判为无需写入。需要强制回写时，只删除首行包含 `Registrate Provider for gtocore` 的那一个缓存清单，再运行 `./gradlew runData --rerun-tasks`；不要清空整个 `.cache`，也不要删除其他 provider 的清单。

## 云端构建与签名（进整合包的唯一途径）

触发 **Build and Sign** 云端工作流：

| 触发 | 条件 | 所需标记 / 命令 |
|------|------|-----------------|
| `push` | commit message 含构建标记（单独成词，如 `release --build`） | **需要** |
| PR **opened** | 提起人为**组织成员** → 自动构建一次（按 PR head） | **不需要** |
| PR **synchronize**（新提交） | head commit 含构建标记，且推送者为组织成员 | **需要** |
| PR **评论** | 组织成员在 PR 下评论，第一行必须精确为 `/build` | `/build` |

- 产出**签名版** jar——只有这个版本可以放进整合包使用。
- 仅**组织成员**（或本仓 write 级 collaborator）能触发签名构建；bot 与外部人员会被拒绝。
- **本地构建的 jar 未经签名，无法放进整合包**；本地构建只用于开发调试（runClient 等）。
- **外部 fork PR**：工作流使用 `pull_request_target`，在 base 仓上下文运行，**可以**使用 org secrets 签名。外部贡献者开 PR 不会自动构建；组织成员审阅后在 PR 下以 `/build` 作为第一行评论，即可触发对 **PR head（含 fork）** 的签名构建。
- **PR 状态反馈**：构建开始时会在 PR 下发一条 bot 评论（⏳ 正在构建 + Actions 链接）；完成/失败/取消后会**原地更新**同一条评论。`issue_comment` 触发的 run **不会**出现在 PR Checks 页，请看这条评论或 [Actions](https://github.com/GregTech-Odyssey/GTOCore-Main/actions)。
- **安全**：`pull_request_target` 会执行 PR 侧代码（`build.gradle` 等）并注入签名 secrets。成员评论 `/build` 即表示已审阅并愿意签名；勿对未审代码轻易触发。

## 子模块日常操作

`GTOLib/` 与 `GTOSeal/` 都是独立 Git 仓库。主仓只记录它们应处于哪个 commit（gitlink，模式 `160000`），不会直接记录子模块内的文件；`.gitmodules` 只保存路径与远端地址。`git submodule update` 后出现 detached HEAD 是正常现象，因为主仓锁定的是 commit，不是子模块分支。

### 初始化与切换主仓分支

首次克隆应递归初始化：

```bash
git clone --recurse-submodules <repo-url>
# 已经克隆主仓时：
git submodule update --init --recursive
```

每个本地 clone 建议启用递归操作，并在切换主仓分支时显式带上子模块：

```bash
git config submodule.recurse true
git switch --recurse-submodules <main-branch>
```

如果主仓分支已经切换、子模块仍停在旧 commit，先确认子模块没有要保留的改动，再按主仓 gitlink 对齐：

```bash
git -C GTOLib status --short --branch
git submodule update --init --recursive --checkout GTOLib
```

`submodule.recurse=true` 只让支持递归的 Git 命令同步处理子模块；它不会替你提交、推送子模块，也不会自动保证 main 与 GTOLib 分支同名。

### 诊断 `M GTOLib`

主仓中的 `M GTOLib` 可能表示“子模块 HEAD 与 gitlink 不同”，也可能表示“子模块内部有未提交文件”。用下面几条命令区分：

```bash
git status --short
git submodule status --recursive
git diff --submodule=log -- GTOLib
git ls-tree HEAD GTOLib
git -C GTOLib rev-parse HEAD
git -C GTOLib status --short --branch
```

如果只是误切子模块、且确认不保留由错误 GTOLib commit 生成的预构建，可精确恢复相关目标：

```bash
git submodule update --init --recursive --checkout GTOLib
git restore --source=HEAD -- \
  libs/gtolib-protected.jar libs/gtolib-protected.PROTECTED \
  libs/gtolib-release.jar libs/gtolib-release.PROTECTED
```

清理这种错位时只恢复已确认的子模块与生成物，**不要使用 `git clean -fd`**，避免删除 `doc/` 等无关的未跟踪文件。也不要随意使用 `git submodule update --remote`；它会尝试推进到子模块远端分支，而不是恢复主仓记录的 gitlink。

### 有意修改 GTOLib

子模块与主仓需要分别提交。先在 GTOLib 的同名分支提交并推送，再让主仓记录新 gitlink：

```bash
git -C GTOLib switch <same-name-branch>
# 修改后：
git -C GTOLib add <paths>
git -C GTOLib commit -m "<message>"
git -C GTOLib push
git -C GTOLib status --porcelain  # 必须为空

./gradlew buildGtolibProtected
git add GTOLib libs/gtolib-protected.jar libs/gtolib-protected.PROTECTED
```

必须先推送子模块 commit，避免主仓指向其他人无法取得的 SHA。主仓中的 `git add GTOLib` 只暂存 gitlink；若流水线同时刷新 release jar 与侧车，也须按 `git status` 将二者成对处理。完整生成物规则见下文。

## 分支与 gtolib 预构建

| 改动范围 | 要求 |
|----------|------|
| 改了 `GTOLib/`（submodule） | gtolib 与 main 用**同名分支**并都推远端；main 提交须包含 submodule 指针 + 新的 `libs/gtolib-protected.jar` + `.PROTECTED`（三者一起 add/push） |
| 只改 main（protected 未变） | 仅 main 开分支即可；不必动 gtolib 分支，也不必重建预构建 |

原因：CI / 无 submodule 环境只认 `libs/gtolib-protected.jar`；只推源码指针不刷新预构建会跑旧字节码（现由下节的 `gtolibCommit` 闸门拦截）。jar 与 `.PROTECTED`（含 jarSha256）必须成对提交。

有 GTOLib 源码时，默认始终走**加密**流水线；`build` / `runClient` / `runData` 会在源码指纹变化后自动刷新 `libs/gtolib-protected.jar` + `.PROTECTED`。也可显式：

```bash
./gradlew buildGtolibProtected
# 全量重建：./gradlew -PgtolibRebuild=true build
# 明文调试（仅本地，不写 libs）：./gradlew runClient -PgtolibUnprotected=true
```

**Agent 要求**：若本次 Prompt 修改了 `GTOLib/` 下的代码，须在**完成该 Prompt 前**运行一次 `./gradlew buildGtolibProtected` 尝试编译并重建 gtolib 预构建，确认通过后再收尾。不要每改一处就重建一次——一个 Prompt 只在收尾时重建这一次。**不要**加 `-PgtolibUnprotected` / `-PgtolibDebug`（明文不会写入 libs，且不符合预构建要求）。

## 三件套：提交、校验与多人合并

`GTOLib` 指针 + `libs/gtolib-protected.jar` + `.PROTECTED` 是**一套生成物**，同进同出，不单独 merge。

### 开发者怎么提交

改了 `GTOLib/`：先在 GTOLib 提交并**推送**（工作区必须干净），回主仓重建，三者一个 commit：

```bash
./gradlew buildGtolibProtected
git add GTOLib libs/gtolib-protected.jar libs/gtolib-protected.PROTECTED
```

带未提交改动重建时侧车会写成 `gtolibCommit=<sha>-dirty`，云端直接拒绝；本地 runClient 迭代不受影响。

### SHA 校验是怎么做的

`.PROTECTED` 有两个身份字段，各管一种环境：

| 字段 | 内容 | 谁校验 |
|------|------|--------|
| `fingerprint` | `GTOLib/src` + `GTOLib/protect` 内容哈希 | 本地**有**源码时 |
| `gtolibCommit` | 重建时的 `git -C GTOLib rev-parse HEAD` | 云端 / 任何**无**源码环境 |

云端是 `submodules: false`（私有仓，且 `pull_request_target` 会执行 PR 侧代码，**不能**注入 GTOLib 凭据），算不出 `fingerprint`，改比对主仓 tree 里的 gitlink：

```bash
git ls-tree HEAD GTOLib   # → 160000 commit <sha>	GTOLib
```

`gtolibCommit` 与 gitlink 不一致、带 `-dirty`、或**整个字段缺失**（旧流水线产物）→ 构建一律硬失败，必须重建 `libs/`。它挡住的是「指针前进但 libs/ 没重建」（会签出跑旧字节码的产物）；**挡不住** jar 是否真由该 commit 编出——构建不可复现，只能谁重建谁负责。

### 多人同时改 gtolib 时怎么合

指针和 jar 的冲突**不要手工 merge**，一律丢弃后统一重建一次：

1. **先在 GTOLib 仓**按序合完所有 PR，源码冲突在那边解干净，记下 main 最终 SHA。
2. 主仓开集成分支，只合各 PR 中**三件套以外**的改动；三件套一律取 main 旧值。
3. submodule 切到该 SHA → `./gradlew buildGtolibProtected` → 三者一个 commit。
4. `./gradlew build` 确认主仓源码能编过新 gtolib（日志出现 `GTOLib 一致性 OK`）。
5. 集成分支一次性合入 main —— main 不经历「指针新、jar 旧」的破碎窗口。

`.PROTECTED` 是文本文件，git 会逐行 merge，可能拼出 A 的 `jarSha256` 配 B 的 `size`。**永远整体重新生成，不要手改。**

## 相关路径

| 路径 | 说明 |
|------|------|
| `GTOLib/` | gtocore-gtolib submodule（需权限才 init） |
| `libs/gtolib-protected.jar` + `.PROTECTED` | 预构建 gtolib 及一致性记录（成对提交） |
| `gradle/scripts/gtolib-pipeline.gradle` | 有/无源码流水线、reobf/Seal 与校验逻辑 |

## 性能规范

> **强制必读。** 本仓所有 AI 生成/修改的代码，提交前必须按 [CODING_GUIDELINES.md](CODING_GUIDELINES.md) 自查。容器与迭代及类型/配方/编解码等的写法直接决定 GC 压力与帧率，属硬性要求。
