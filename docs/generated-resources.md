# 生成资源

仅在任务涉及 `src/generated/resources`、语言、模型、标签、配方或其他 data generator 输出时读取本文。Gradle 命令必须遵守仓库根目录 [AGENTS.md](../AGENTS.md) 的 JDK 21 前置要求。

## 通用规则

- `src/generated/resources` 下的文件是生成结果。需要调整内容时修改 Java/Kotlin 中的生成源，再运行 `.\gradlew.bat runData` 并检查输出。
- 不要直接编辑或用脚本改写生成文件作为最终实现。若生成结果不符合预期，继续修正生成源。
- `runData --rerun-tasks` 只强制 Gradle 任务执行，不保证 Minecraft `HashCache` 重写磁盘上被外部修改过的文件。

## 语言数据

以下文件均由 data generator 生成：

- `src/generated/resources/assets/gtocore/lang/en_us.json`
- `src/generated/resources/assets/gtocore/lang/zh_cn.json`
- `src/generated/resources/assets/gtocore/lang/zh_tw.json`

注册入口是 `src/main/java/com/gtocore/data/Datagen.java`，语言数据由 `src/main/java/com/gtocore/data/lang/LangHandler.java` 聚合。GTOLib 注解与动态翻译分别经 `ScanningClass.LANG`、`DynamicInitialData.LANG`、`TranslationKeyProvider.LANG` 汇入。

provider 按 `enInitialize()` → `cnInitialize()` → `twInitialize()` 的顺序运行：

- `enInitialize()` 初始化完整共享语言表并输出英文；
- `cnInitialize()` 输出简体中文；
- `twInitialize()` 使用 `ChineseConverter` 将同一份简体中文源转换为繁体中文。

新增或修改中文文案时只维护翻译源中的简体中文，不单独维护 `zh_tw.json`。

## HashCache 强制回写

HashCache 比较本次生成哈希与 `src/generated/resources/.cache` 中的清单，不校验生成文件的当前磁盘内容。如果语言 JSON 曾被 `git restore`、合并或外部工具改写，缓存可能误判为无需写入。

需要强制回写时：

1. 在 `.cache` 中找到首行包含 `Registrate Provider for gtocore` 的那一个清单；
2. 只删除该清单；
3. 运行 `.\gradlew.bat runData --rerun-tasks`。

不要清空整个 `.cache`，也不要删除其他 provider 的清单。
