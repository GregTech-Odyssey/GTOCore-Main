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

## 两条混淆轨道

| 产物 | 用途 | 名字 |
|---|---|---|
| `build/gtolib/gtolib-protected.jar`（dev 轨） | 本地 userdev 运行与缓存命中时的编译 classpath | 保留全部类名/包名（成员名也只做方法体内优化），含 named `.dev.bin` |
| `build/gtolib/gtolib-release.jar`（发行轨） | 嵌入玩家用的 gtocore jar | 内部类名、包名、成员名混淆，无 `.dev.bin` |

两条轨道都是直链，任务名见括号：

- dev 轨：named 明文（`packageGtolibJar`）→ ProGuard `GTOLib/protect/proguard-rules.pro`（`proguardGtolibDev`）→ Seal（`sealGtolibDev`）。
- 发行轨：named 明文 → ART reobf 成 SRG（`reobfGtolib`）→ ProGuard `GTOLib/protect/proguard-release-rules.pro` + gtocore 引用面（`proguardGtolibRelease`）→ Seal，剥除 `.dev.bin` 并压缩空壳（`sealGtolibRelease`）。

dev 轨的 `proguard-rules.pro` 保留全部类名/包名（Seal 路径键与 `SecureJar` 资源路径依赖这个约定），只有发行轨参与改名。发行轨的混淆字典是 `GTOLib/protect/my_dict.txt`（`U+3164` 加 28 个组合符的 `28³ = 21952` 个全枚举名），每个任务用 `SecureRandom` 重新洗牌，所以同一份源码两次构建的名字都不同，构建不可复现。

保名面只有：`com.gtolib.api.capability` / `com.gtolib.api.machine.feature` / `com.gtolib.api.machine.mana.feature` 下的接口（类名+成员名）、`com.gtolib.mixin.**`、入口（`MixinConfigPlugin` / `MathUtil`）、`@KeepName` 显式声明的类与成员、枚举、`@LDLRegister`、DataSyncLib 注解字段，以及 `com.gtolib.api.ae2.me2in1.*` 包名。**gtocore 对 `com.gtolib` 的静态引用不再保名**：打包时 `prepareGtolibRefRemapper` 按 `build/gtolib/gtolib-release-mapping.txt` 改写 gtocore 字节码里的类、字段、方法与继承成员引用（含覆盖方法），两边一起混淆。

两条轨道的 ProGuard 都必须把**主工程已编译类**（`com/gtolib` 除外）当 library 传进去：dev 轨用 named 的 `build/gtolib/gtocore-named-library.jar`，发行轨用 ART reobf 后的 `build/gtolib/gtocore-srg-library.jar`。不能省：ProGuard 对「owner 既不在 program 也不在 library」的成员引用**不会改写其描述符里的程序类名**，`com.gtolib.*` 的原始名字会残留在 gtolib 自己的常量池里（例如 `ICrossRecipeElectricMachine` 读 gtocore 的 `GTORules.TIME_TWISTER_ENERGY:Lcom/gtolib/api/rule/IntRule;`），发行包运行到该路径就 `NoClassDefFoundError`。

打包期 remap 覆盖的引用面（漏一个就是运行时链接错误）：类/字段/方法引用、继承与覆盖成员、成员描述符、类名字符串，以及 **`invokedynamic` 的 SAM 名**——LambdaMetafactory 生成的类按名字实现 gtolib 函数式接口的方法，ASM 的 `ClassRemapper` 拿不到 `bsmArgs`，必须由 `resolveSamName` 单独改（例：gtocore 给 `CraftAction.craft` 注册 lambda，漏改就是 `AbstractMethodError`）。

还有一类**改不了、只能保名**的：gtolib 接口的抽象方法被改名，而它的实现来自 library（gtceu/AE2 等）里同名同签名的方法——Mixin 把 gtolib 接口挂到 library 类上时实现继承自父类，library 的名字不会被混淆，于是接口方法成了"没有实现的抽象方法"（`IFluidDrillLogic.getMachine()` 继承自 gtceu `MachineTrait.getMachine()` 就是这类）。给它加 `@KeepName` 即可；`verifyLibraryBackedInterfaceMethods` 会在打包时扫 jar+library 全量类层次把这一类拦下来。

四道自检在打包时拦下残留：`verifyNoStaleGtolibRefs`（ProGuard 输出里不许出现已改名类的原始名）、`verifyNoOriginalGtolibNames`（最终 jar 的引用与字符串）、`verifyGtolibSamTargets`（每个指向 gtolib 接口的 lambda，SAM 名必须在混淆后的接口层次里解析得到）、`verifyLibraryBackedInterfaceMethods`（接口方法改名后必须仍能在 jar+library 里找到实现）。

dev 轨保留真名，所以 GTOLib 源码指纹命中（不编译 GTOLib 源码）时，gtocore 直接拿 dev 轨 jar 编译/运行；发行轨的混淆名只出现在玩家包里，由打包时的 remap 与 gtocore 对齐。

## 按名访问：@KeepName

`com.gtolib.api.annotation.KeepName`（`@Retention(CLASS)`，不产生运行时依赖）用来显式声明按名访问，替代人肉补 keep：

- 标注在类上：类名与包名不混淆，成员名照常混淆；
- 标注在字段、方法或构造器上：该成员名与所属类的类名（含包名）都不混淆。

反射、注解扫描、存盘键、配置或脚本里写死类名/成员名时都要标注。流水线会扫描该轨的 ProGuard 输入 jar，把注解展开成**具体类名**的 `-keepnames`（`build/gtolib/keepname-dev.pro` / `keepname-release.pro`）——不要在规则文件里写 `-keepnames class * { @KeepName <fields>; }`，ProGuard 7.8 实测会把「`class *` + 成员说明」当成命中全部类，混淆类数直接变 0。

GTOLib 把**能力/特性接口整体保名**（规则里的 `-keepnames interface com.gtolib.api.capability.**`、`...api.machine.feature.**`、`...api.machine.mana.feature.**`，共约 38 个接口）：这些是实现方（含 gtocore）主要对接的接口，只有声明没有方法体，泄名换来的稳定性划算，也顺带消掉「两个接口里描述符相同、名字不同的成员被改成同一个名字、实现类因此出现重复方法」的冲突（`IVacuumMachine.getVacuumTier()` 与 `IGravityPartMachine.getCurrentGravity()` 就是这类）。其它包的接口按普通类参与混淆；一旦出现同类冲突，流水线 remap 会自检重复方法/字段并直接报错，此时给其中一个成员加 `@KeepName` 即可（非接口的父类/接口冲突同理）。

第三方或 GTOSeal Runtime 写死、改不了源码的名字，仍在 `GTOLib/protect/proguard-release-rules.pro` 里补 `-keepnames`。漏标只会在生产包运行到该路径时报错，开发环境（dev 轨保留真名）测不出来。

两条轨道使用同一套 ProGuard 优化，只开方法体内部的 `code/*` 类别（不改类、字段、方法的名字与签名），因此默认 `runClient` 运行的就是与发行包同样优化过的代码。不要开启 `class/*`、`field/*`、`method/*` 及 `code/*/advanced`：它们会改变成员或依赖值推导，反射与存盘写入的字段会被悄悄改坏。

GTOLib 的 Mixin 只写注入点与一行转调，复杂逻辑放 mixin 包外的 `com.gtolib.<领域>.XxxHooks`：mixin 包在发行包里保持明文。

混淆映射 `build/gtolib/gtolib-release-mapping.txt` 既是打包时 gtocore 引用面对齐的依据，也用于反混淆崩溃栈，禁止公开。云端构建只上传用 `GTOLIB_MAPPING_KEY` 加密后的映射（artifact `gtolib-mapping-encrypted`）。该密钥是映射专用的仓库 secret，与签名密码 `GTO_KEY` 分开；向维护者索取后解密：

```bash
openssl enc -d -aes-256-cbc -pbkdf2 -iter 600000 -in gtolib-release-mapping.txt.gz.enc -pass env:GTOLIB_MAPPING_KEY | gunzip > mapping.txt
```

2026-10-14 之前的构建产物仍用 `GTO_KEY` 加密，解密时把 `-pass` 换成 `env:GTO_KEY`。

## 多人同时修改 GTOLib

先在 GTOLib 仓按序合并所有改动、解决源码冲突并确定最终 SHA，再让主仓集成分支指向该 SHA，运行 `.\gradlew.bat build` 确认主仓能通过新 GTOLib 编译。

## 相关路径

| 路径 | 说明 |
|---|---|
| `GTOLib/` | gtocore-gtolib 子模块，需要权限才能初始化 |
| `GTOLib/protect/` | ProGuard 规则、混淆字典与 Seal 打包器 |
| `build/gtolib/` | GTOLib 构建产物与缓存（`inputs.stamp`），禁止提交 |
| `gradle/scripts/gtolib-pipeline.gradle.kts` | 加密流水线、reobf / Seal 与校验逻辑 |
