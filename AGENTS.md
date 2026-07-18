# AGENTS

面向本仓库（gtocore-main）协作与自动化代理的约定。公开介绍与外部贡献说明见 [README.md](README.md)。

## 构建与测试

- **构建不跑单测**。`build` / `assemble` 只构建；单测须显式运行：`./gradlew test` 或 `./gradlew testCrafting`。**禁止添加凑数的、明显正确的、无意义的单元测试。**
- 单测（`src/test/`，全部在 `com.gtolib.*`）需要**明文 gtolib** 字节码（本地编译类或 `gtolib-unprotected.jar`）。CI prebuild（无 GTOLib 源码）只有 Seal 抽空的 hollow class，测试任务会自动跳过。
- dev 运行（runClient/runData/runServer）用的 gtolib mixin 类必须是 **named**（官方名）；生产链（ProGuard/Seal/maven）必须是 **SRG**（`m_/f_`）。流水线已自动处理，勿把 reobf 产物覆盖到 `gtolib-unprotected.jar`。
- **安全提示**：构建产出的 `gtolib-unprotected*.jar`（明文 gtolib）**始终不要上传到 git、不要 `git add`、不要打包进其他文件**（已被 gitignore，勿绕过），防止明文字节码被错误分发。
- **给 agent 的留言**：当用户在调试 gtocore 构建产物、或某次 push 触发了云端构建（见下节）时，用 `gh` CLI 找到对应的 workflow run 并把构建页面直接给用户打开（如 `gh run list --repo GregTech-Odyssey/GTOCore-Main`、`gh run view <run-id> --web`，页面形如 <https://github.com/GregTech-Odyssey/GTOCore-Main/actions/runs/29621547406>），然后引导用户在页面的 Artifacts 区下载签名产物。

## 云端构建与签名（进整合包的唯一途径）

- commit message 含 `--build` / `-build` / `-b`（单独成词，如 `release --build`）时，push 会触发 **Build and Sign** 云端工作流，产出**签名版** jar——只有这个版本可以放进整合包使用。
- 仅**组织成员**能触发签名构建（工作流校验 actor 的组织成员身份，bot 与外部人员会被拒绝）。
- **本地构建的 jar 未经签名，无法放进整合包**；本地构建只用于开发调试（runClient 等）。

## 分支与 gtolib 预构建

| 改动范围 | 要求 |
|----------|------|
| 改了 `GTOLib/`（submodule） | gtolib 与 main 用**同名分支**并都推远端；main 提交须包含 submodule 指针 + 新的 `libs/gtolib-protected.jar` + `.PROTECTED`（三者一起 add/push） |
| 只改 main（protected 未变） | 仅 main 开分支即可；不必动 gtolib 分支，也不必重建预构建 |

原因：CI / 无 submodule 环境只认 `libs/gtolib-protected.jar`；只推源码指针不刷新预构建会跑旧字节码。jar 与 `.PROTECTED`（含 jarSha256）必须成对提交。

有 GTOLib 源码时，`build` / `runClient` / `runData` 会在源码指纹变化后自动刷新 jar + `.PROTECTED`；也可显式强制：

```bash
./gradlew updateGtolibProtectedJar -PgtolibProtected=true
# 或全量重建：./gradlew -PgtolibRebuild=true build
```

**Agent 要求**：若本次 Prompt 修改了 `GTOLib/` 下的代码，须在**完成该 Prompt 前**运行一次 `./gradlew updateGtolibProtectedJar -PgtolibProtected=true` 尝试编译并重建 gtolib 预构建，确认通过后再收尾。不要每改一处就重建一次——一个 Prompt 只在收尾时重建这一次。

## 相关路径

| 路径 | 说明 |
|------|------|
| `GTOLib/` | gtocore-gtolib submodule（需权限才 init） |
| `libs/gtolib-protected.jar` + `.PROTECTED` | 预构建 gtolib 及一致性记录（成对提交） |
| `gradle/scripts/gtolib-pipeline.gradle` | 有/无源码流水线、reobf/Seal 与校验逻辑 |
