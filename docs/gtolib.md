# GTOLib 子模块与预构建

仅在任务涉及 `GTOLib/`、`GTOSeal/`、预构建 jar、`.PROTECTED`、gitlink、子模块状态或相关流水线时读取本文。Gradle 命令必须遵守仓库根目录 [AGENTS.md](../AGENTS.md) 的 JDK 21 前置要求。

## 基本模型

`GTOLib/` 与 `GTOSeal/` 都是私有且相互独立的 Git 仓库。只有具备对应仓库权限的人员或执行环境才能初始化、拉取和更新这些子模块；拥有主仓权限不代表同时拥有子模块权限。无权限时子模块目录缺失、未初始化或拉取失败是预期状态，不要反复重试、绕过权限或要求提供凭据。

主仓只记录它们应处于哪个 commit（gitlink，模式 `160000`），不会直接记录子模块内的文件；`.gitmodules` 只保存路径与远端地址。只涉及主仓的任务应继续使用 `libs/` 中的预构建产物完成可行的构建、检查或修改；只有任务确实需要查看或修改私有源码时，缺少子模块权限才构成阻塞。

`git submodule update` 后出现 detached HEAD 是正常现象，因为主仓锁定的是 commit，而不是子模块分支。

dev 运行（`runClient` / `runData` / `runServer`）使用的 GTOLib Mixin 类必须是 named；生产链（ProGuard / Seal / Maven）必须是 SRG（`m_` / `f_`）。流水线已自动处理，不要把 reobf 产物覆盖到 `gtolib-unprotected.jar`。

构建产出的 `gtolib-unprotected*.jar` 是明文字节码，始终不得上传到 Git、不得 `git add`、不得打包进其他文件。它已被 gitignore，禁止绕过。

## 初始化与切换主仓分支

具备两个私有仓库的相应权限时，首次克隆可递归初始化：

```powershell
git clone --recurse-submodules <repo-url>
# 已经克隆主仓时：
git submodule update --init --recursive
```

每个本地 clone 建议启用递归操作，并在切换主仓分支时显式带上子模块：

```powershell
git config submodule.recurse true
git switch --recurse-submodules <main-branch>
```

如果主仓分支已经切换、子模块仍停在旧 commit，先确认子模块没有要保留的改动，再按主仓 gitlink 对齐：

```powershell
git -C GTOLib status --short --branch
git submodule update --init --recursive --checkout GTOLib
```

`submodule.recurse=true` 只让支持递归的 Git 命令同步处理子模块；它不会自动提交、推送子模块，也不会保证主仓与 GTOLib 分支同名。

## 诊断 `M GTOLib`

主仓中的 `M GTOLib` 可能表示子模块 HEAD 与 gitlink 不同，也可能表示子模块内部存在未提交文件。使用以下命令区分：

```powershell
git status --short
git submodule status --recursive
git diff --submodule=log -- GTOLib
git ls-tree HEAD GTOLib
git -C GTOLib rev-parse HEAD
git -C GTOLib status --short --branch
```

如果只是误切子模块，且已确认不保留由错误 GTOLib commit 生成的预构建，可精确恢复相关目标：

```powershell
git submodule update --init --recursive --checkout GTOLib
git restore --source=HEAD -- `
  libs/gtolib-protected.jar libs/gtolib-protected.PROTECTED `
  libs/gtolib-release.jar libs/gtolib-release.PROTECTED
```

只恢复已确认的子模块与生成物。不要使用 `git clean -fd`，避免删除 `docs/` 等无关未跟踪文件；不要随意使用 `git submodule update --remote`，它会尝试推进远端分支，而不是恢复主仓记录的 gitlink。

## 有意修改 GTOLib

子模块与主仓需要分别提交。先在 GTOLib 的同名分支提交并推送，再让主仓记录新 gitlink：

```powershell
git -C GTOLib switch <same-name-branch>
git -C GTOLib add <paths>
git -C GTOLib commit -m "<message>"
git -C GTOLib push
git -C GTOLib status --porcelain  # 必须为空

.\gradlew.bat buildGtolibProtected
git add GTOLib libs/gtolib-protected.jar libs/gtolib-protected.PROTECTED
```

必须先推送子模块 commit，避免主仓指向其他人无法取得的 SHA。主仓中的 `git add GTOLib` 只暂存 gitlink；若流水线同时刷新 release jar 与侧车，也须按 `git status` 将二者成对处理。

## 分支与预构建

| 改动范围 | 要求 |
|---|---|
| 修改了 `GTOLib/` | GTOLib 与主仓使用同名分支并都推送；主仓提交包含子模块指针、新的 `libs/gtolib-protected.jar` 和 `.PROTECTED` |
| 只修改主仓且 protected 未变 | 仅主仓开分支；不必修改 GTOLib 分支，也不必重建预构建 |

CI 或无子模块环境只读取 `libs/gtolib-protected.jar`。只推进源码指针却不刷新预构建会运行旧字节码，因此 jar 与 `.PROTECTED`（含 `jarSha256`）必须成对提交。

有 GTOLib 源码时，默认走加密流水线；`build`、`runClient`、`runData` 会在源码指纹变化后自动刷新 `libs/gtolib-protected.jar` 与 `.PROTECTED`。也可显式执行：

```powershell
.\gradlew.bat buildGtolibProtected
# 全量重建：.\gradlew.bat -PgtolibRebuild=true build
# 明文调试（仅本地，不写 libs）：.\gradlew.bat runClient -PgtolibUnprotected=true
```

若当前任务修改了 `GTOLib/` 代码，完成任务前必须运行一次 `buildGtolibProtected`，并确认编译与预构建重建通过。只在收尾时重建一次；不要为预构建命令添加 `-PgtolibUnprotected` 或 `-PgtolibDebug`。若构建失败，应明确任务尚未验证完成，不得作为正常完成结果收尾。

## 三件套与一致性校验

`GTOLib` 指针、`libs/gtolib-protected.jar` 与 `.PROTECTED` 是一套生成物，同进同出，不单独 merge。

带未提交 GTOLib 改动重建时，侧车会写成 `gtolibCommit=<sha>-dirty`，云端会拒绝；本地 `runClient` 迭代不受影响。

`.PROTECTED` 有两个身份字段：

| 字段 | 内容 | 校验环境 |
|---|---|---|
| `fingerprint` | `GTOLib/src` 与 `GTOLib/protect` 内容哈希 | 本地有源码时 |
| `gtolibCommit` | 重建时的 `git -C GTOLib rev-parse HEAD` | 云端或任何无源码环境 |

云端使用 `submodules: false`：`GTOLib` 与 `GTOSeal` 是私有仓库，而 `pull_request_target` 会执行 PR 侧代码，因此不得向该工作流注入私有子模块凭据。云端无法计算 `fingerprint`，改为比对主仓 tree 中的 gitlink：

```powershell
git ls-tree HEAD GTOLib
```

`gtolibCommit` 与 gitlink 不一致、带 `-dirty`，或整个字段缺失时，构建必须失败并重建 `libs/`。该闸门防止“指针新、jar 旧”，但不能证明 jar 真由对应 commit 构建；重建者仍需负责。

## 多人同时修改 GTOLib

指针与 jar 的冲突不要手工 merge，应丢弃冲突版本并统一重建：

1. 先在 GTOLib 仓按序合并所有改动，解决源码冲突并确定最终 SHA。
2. 主仓集成分支只合并各改动中三件套以外的内容；三件套暂取主仓旧值。
3. 将子模块切到最终 SHA，运行 `.\gradlew.bat buildGtolibProtected`，把三件套作为一个提交。
4. 运行 `.\gradlew.bat build`，确认主仓源码能通过新 GTOLib 编译，日志出现 `GTOLib 一致性 OK`。
5. 将集成分支一次性合入主分支，避免出现指针新而 jar 旧的破碎窗口。

`.PROTECTED` 是文本文件，Git 可能逐行拼出一方的 `jarSha256` 与另一方的 `size`。永远整体重新生成，不要手工编辑。

## 相关路径

| 路径 | 说明 |
|---|---|
| `GTOLib/` | gtocore-gtolib 子模块，需要权限才能初始化 |
| `libs/gtolib-protected.jar` 与 `.PROTECTED` | 预构建 GTOLib 与一致性记录，成对提交 |
| `gradle/scripts/gtolib-pipeline.gradle` | 有无源码流水线、reobf / Seal 与校验逻辑 |
