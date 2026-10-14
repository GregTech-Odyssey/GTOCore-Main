# Agent 常见坑与协作约定

本文记录在 gtocore-main 里实际踩过的坑与对应的处理办法，以及若干与用户协作时定下的约定。命中「提交/推送、`runData` 空转或崩溃、Gradle 产物被占用、多方块 UI」这类任务时先读本文。Gradle 命令仍须遵守根目录 [AGENTS.md](../AGENTS.md) 的 JDK 21 前置要求。

## 数据生成（runData）

### 外部替换过生成文件后 runData 会空转

`git reset --hard`、`git checkout -- <生成文件>`、合并、手工改写之后，Minecraft 的 `HashCache` 可能认定「无需写入」：`runData` 返回成功，但生成文件一个字节没变。

- 判断：跑之前后对比 `Get-Item <lang>.LastWriteTime`，或直接看文件内容有没有变。
- 处理（同 [生成资源](generated-resources.md)）：在 `src/generated/resources/.cache` 里找首行含 `Registrate Provider for gtocore` 的那个清单（文件名是哈希，会随构建变化），只删它，再 `runData --rerun-tasks`。
- 不要为了图快直接改生成文件当结果。

### `syncSealRuntimeToLibs` 报「另一个程序正在使用此文件」

`libs/gto-seal-runtime-1.0.jar` 被别的进程持有（Gradle 守护进程、游戏、IDE 都可能），同步任务无法覆盖。

- 处理：`.\gradlew.bat --stop`，然后**单独**跑 `runData`（同一个命令里先跑别的 Gradle 任务会让新守护进程重新打开这个 jar，照样失败）。

### datagen 偶发崩溃

`GTOFluids.<clinit>` 注册时 `FastCollection` 的 `MultiMap.put` → fastutil `ReferenceLinkedOpenHashSet.rehash` 抛 `ArrayIndexOutOfBoundsException`（依赖 identity hash，同一份代码重跑常常就过）。先重试 1~2 次；连续失败再当真实错误查。

### 配置阶段「Remote host terminated the handshake」

网络不通时，配置阶段「正在从 Mojang API 验证 MC 版本」会失败，报 `Failed to notify project evaluation listener` / `Remote host terminated the handshake`，和改的代码无关。加 `--offline` 走本地缓存即可：`.\gradlew.bat runData --offline`（`spotlessApply` 同理）。

### 语言键扫描

- `@RegisterLanguage` 只对**带 `@DataGeneratorScanned` / `@Scanned` 的类**生效；嵌套类里的语言常量要**单独**给那个嵌套类加注解，否则键不会生成。
- 注解值是编译期常量：把 `public static final int` 拼进文案里没问题。
- 改了文案（Kotlin 工具提示或注解）都要重跑 `runData`，键会跟着内容变哈希。

## Git 与提交

- **`git commit`（不带路径）与 `git commit --amend` 都会提交索引里的全部内容**。同一工作区里可能有别人并行 `git add`：在你 `git diff --cached` 之后、`git commit` 之前就可能多出内容，`--amend` 尤其容易把对方的暂存一起卷进自己的提交。
  - 提交只带自己的路径：`git commit --only <paths>`（或 `git commit -F msg -- <paths>`）。
  - 已误提交：`git reset --mixed <上一个好提交>`，重新只提交自己的文件，再把对方原本暂存的文件按原样 `git add` 回暂存区（不要动对方工作区内容）。
- **推送前先 `git fetch`**：远端很可能已经有内容等价的提交（信息不同、或由对方从 IDE 推送）。用 `git rev-list --left-right --count origin/<branch>...HEAD` 看差距；远端已含你的改动时不要 force，`git reset --hard origin/<branch>` 后只补真正的增量再推。
- 需要保留本地旧提交时先建备份分支（例如 `backup/local-before-push`），收尾时提醒用户可以删。
- 涉及子模块指针：确认目标提交在子模块自己的远端上（`git -C GTOLib branch -r --contains <sha>`），否则推出去别人拉不到。
- 提交信息用中文 `feat:` / `fix:` / `chore:`；写文件给 git 用时用无 BOM UTF-8。

## 构建与验证

- Windows 上第一次调用 Gradle 前显式设置 JDK 21 并确认 `java.exe` 存在（见 AGENTS.md）。
- 推送前 `spotlessApply` + `spotlessCheck`；只覆盖 Java 时用 `spotlessJavaCheck`（`spotlessCheck` 会连 Kotlin 一起查，可能报别人正在改的文件）。
- **依赖版本升级（`gradle.properties`）会静默换掉 API**：例如 AE2 `15.269.2` 去掉了 `appeng.api.implementations.items.IStorageComponent`，`StorageComponentItem` 从「实现该接口、`int getBytes(ItemStack)`」变成「无参 `long getBytes()`」。升版本后先 `compileJava`，按编译错误逐个适配，并把版本号与适配代码放进**同一个提交**（否则提交出去的状态编译不过）。
- `gradlew.bat ... | Select-String ...` 这类管道会让 `$LASTEXITCODE` 失真（可能拿到 -1）：把输出先存变量，再读 `$LASTEXITCODE`。
- `runData` 至少要 1~3 分钟，崩溃/占用都要重试，别把它当秒级任务。