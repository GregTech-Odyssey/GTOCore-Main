# AGENTS

面向本仓库（gtocore-main）协作与自动化代理的约定。公开介绍与外部贡献说明见 [README.md](README.md)。

## 构建与测试

- **构建不跑单测**。`build` / `assemble` 只构建；单测须显式运行：`./gradlew test` 或 `./gradlew testCrafting`。**禁止添加凑数的、明显正确的、无意义的单元测试。**
- 单测（`src/test/`，全部在 `com.gtolib.*`）需要**明文 gtolib** 字节码（本地编译类或 `gtolib-unprotected.jar`）。CI prebuild（无 GTOLib 源码）只有 Seal 抽空的 hollow class，测试任务会自动跳过。
- dev 运行（runClient/runData/runServer）用的 gtolib mixin 类必须是 **named**（官方名）；生产链（ProGuard/Seal/maven）必须是 **SRG**（`m_/f_`）。流水线已自动处理，勿把 reobf 产物覆盖到 `gtolib-unprotected.jar`。
- **安全提示**：构建产出的 `gtolib-unprotected*.jar`（明文 gtolib）**始终不要上传到 git、不要 `git add`、不要打包进其他文件**（已被 gitignore，勿绕过），防止明文字节码被错误分发。
- **给 agent 的留言**：当用户在调试 gtocore 构建产物、或某次 push 触发了云端构建（见下节）时，用 `gh` CLI 找到对应的 workflow run 并把构建页面直接给用户打开（如 `gh run list --repo GregTech-Odyssey/GTOCore-Main`、`gh run view <run-id> --web`，页面形如 <https://github.com/GregTech-Odyssey/GTOCore-Main/actions/runs/29621547406>），然后引导用户在页面的 Artifacts 区下载签名产物。

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

`gitlink != .PROTECTED 的 gtolibCommit` → 构建硬失败。它挡住的是「指针前进但 libs/ 没重建」（会签出跑旧字节码的产物）；**挡不住** jar 是否真由该 commit 编出——构建不可复现，只能谁重建谁负责。

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
