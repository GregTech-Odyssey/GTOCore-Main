# GTOLib 子模块与构建

仅在任务涉及 `GTOLib/`、`GTOSeal/`、GTOLib 构建缓存（`build/gtolib/`）、gitlink、子模块状态或相关流水线时读取本文。Gradle 命令必须遵守仓库根目录 [AGENTS.md](../AGENTS.md) 的 JDK 21 前置要求。

## 基本模型

`GTOLib/` 与 `GTOSeal/` 都是私有且相互独立的 Git 仓库。只有具备对应仓库权限的人员或执行环境才能初始化、拉取和更新这些子模块；拥有主仓权限不代表同时拥有子模块权限。无权限时子模块目录缺失、未初始化或拉取失败是预期状态，不要反复重试、绕过权限或要求提供凭据。

主仓只记录它们应处于哪个 commit（gitlink，模式 `160000`），不会直接记录子模块内的文件；`.gitmodules` 只保存路径与远端地址。**构建 gtocore 必须有 GTOLib 权限**：缺少 `GTOLib/` 源码时配置阶段直接报错（提示 `git submodule update --init GTOLib`）；非明文模式下 `GTOLib/protect/` 的 ProGuard 规则与 Seal Packager 缺失同样报错。公开仓不提交任何 GTOLib 产物，GTOLib 只在 `build/gtolib/` 构建；`libs/` 不再存放 GTOLib 产物，残留的旧 `libs/gtolib-*` 由 gitignore 兜底，禁止提交或上传。

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

如果只是误切子模块，确认子模块没有要保留的改动后按 gitlink 恢复；`build/gtolib/` 缓存会在下次构建时按源码指纹自动重建：

```powershell
git submodule update --init --recursive --checkout GTOLib
```

不要使用 `git clean -fd`，避免删除 `docs/` 等无关未跟踪文件；不要随意使用 `git submodule update --remote`，它会尝试推进远端分支，而不是恢复主仓记录的 gitlink。

## 有意修改 GTOLib

子模块与主仓需要分别提交。先在 GTOLib 的同名分支提交并推送，再让主仓记录新 gitlink：

```powershell
git -C GTOLib switch <same-name-branch>
git -C GTOLib add <paths>
git -C GTOLib commit -m "<message>"
git -C GTOLib push
git -C GTOLib status --porcelain  # 必须为空

.\gradlew.bat buildGtolibProtected
git add GTOLib
```

必须先推送子模块 commit，避免主仓指向其他人和 CI 无法取得的 SHA。主仓只提交 gitlink，不提交任何 GTOLib 构建产物。

## 分支与本地缓存

| 改动范围 | 要求 |
|---|---|
| 修改了 `GTOLib/` | GTOLib 与主仓使用同名分支并都推送；主仓提交只包含子模块指针 |
| 只修改主仓 | 仅主仓开分支，不必动 GTOLib |

默认走加密流水线，产物只在 `build/gtolib/`。缓存是否有效只看源码指纹（`GTOLib/src` 与 `GTOLib/protect`）与 `build/gtolib/inputs.stamp` 记录的指纹和 jar 哈希；指纹变化、产物缺失或本次调用含 `clean` 时，`build`、`runClient`、`runData` 会自动完整重建。也可显式执行：

```powershell
.\gradlew.bat buildGtolibProtected
# 全量重建：.\gradlew.bat -PgtolibRebuild=true build
# 明文调试（仅本地，不动加密缓存）：.\gradlew.bat runClient -PgtolibUnprotected=true
# 明文模式只用于本地 runClient / test；jar、build 会直接拒绝，发行包只能嵌入加密的发行轨
```

若当前任务修改了 `GTOLib/` 代码，完成任务前必须运行一次 `buildGtolibProtected`，并确认编译与加密通过。只在收尾时重建一次；不要为该命令添加 `-PgtolibUnprotected` 或 `-PgtolibDebug`。若构建失败，应明确任务尚未验证完成，不得作为正常完成结果收尾。

## 两条加密轨道

| 产物 | 用途 | 名字 |
|---|---|---|
| `build/gtolib/gtolib-protected.jar`（dev 轨） | 本地 userdev 运行与缓存命中时的编译 classpath | keepnames，含 named `.dev.bin` |
| `build/gtolib/gtolib-release.jar`（发行轨） | 嵌入玩家用的 gtocore jar | 内部类名、包名、成员名混淆，无 `.dev.bin` |

两条轨道都是直链，任务名见括号：

- dev 轨：named 明文（`packageGtolibJar`）→ ProGuard `proguard-rules.pro`（`proguardGtolibDev`）→ Seal（`sealGtolibDev`）。
- 发行轨：named 明文 → ART reobf 成 SRG（`reobfGtolib`）→ ProGuard `proguard-release-rules.pro` + gtocore 引用面（`proguardGtolibRelease`）→ Seal，剥除 `.dev.bin` 并压缩空壳（`sealGtolibRelease`）。

dev 轨保留全部真实名字，**只能留在本机**。发行轨的混淆字典每次构建随机打乱，同一份源码两次构建的混淆名也不同；发行混淆规则见 `GTOLib/protect/proguard-release-rules.pro`，保名面只包括 gtocore 实际引用的类、Mixin 与入口、以及按名反射/扫描的类与成员。新增按名反射或注解扫描时须同步补 keep，否则只在生产包运行时报错。

两条轨道使用同一套 ProGuard 优化，只开方法体内部的 `code/*` 类别（不改类、字段、方法的名字与签名），因此默认 `runClient` 运行的就是与发行包同样优化过的代码。不要开启 `class/*`、`field/*`、`method/*` 及 `code/*/advanced`：它们会改变成员或依赖值推导，反射与存盘写入的字段会被悄悄改坏。

GTOLib 的 Mixin 只写注入点与一行转调，复杂逻辑放 mixin 包外的 `com.gtolib.<领域>.XxxHooks`：mixin 包在发行包里保持明文。

混淆映射 `build/gtolib/gtolib-release-mapping.txt` 用于反混淆崩溃栈，禁止公开。云端构建只上传用 `GTO_KEY` 加密后的映射（artifact `gtolib-mapping-encrypted`），解密：

```bash
openssl enc -d -aes-256-cbc -pbkdf2 -iter 600000 -in gtolib-release-mapping.txt.gz.enc -pass env:GTO_KEY | gunzip > mapping.txt
```

## 多人同时修改 GTOLib

先在 GTOLib 仓按序合并所有改动、解决源码冲突并确定最终 SHA，再让主仓集成分支指向该 SHA，运行 `.\gradlew.bat build` 确认主仓能通过新 GTOLib 编译。

## 相关路径

| 路径 | 说明 |
|---|---|
| `GTOLib/` | gtocore-gtolib 子模块，需要权限才能初始化 |
| `GTOLib/protect/` | ProGuard 规则、混淆字典与 Seal 打包器 |
| `build/gtolib/` | GTOLib 构建产物与缓存（`inputs.stamp`），禁止提交 |
| `gradle/scripts/gtolib-pipeline.gradle.kts` | 加密流水线、reobf / Seal 与校验逻辑 |
