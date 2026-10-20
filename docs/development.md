# 团队开发说明

本文面向有 gtolib 权限的团队成员；公开介绍见 [README](../README.md)，自动化代理的约定见 [AGENTS.md](../AGENTS.md)。

## 开发环境

- **Java 25**（运行 Gradle / IDE 的 JDK）
- 模组字节码目标为 **Java 21**（依赖与 reobf 兼容，构建脚本已自动设置；请勿用 25 作为 `--release`）
- Git
- 可访问 Gradle 和项目依赖仓库的网络环境
- 足够的可用内存；当前 Gradle 配置的最大堆内存为 8 GiB

请确认 `java -version` 指向 Java 25，并在 IDE 和终端中使用同一个 JDK。

## 克隆与构建

```bash
git clone https://github.com/GregTech-Odyssey/GTOCore-Main.git
cd GTOCore-Main
git submodule update --init GTOLib
bash ./gradlew build
```

Windows PowerShell 或命令提示符：

```bat
gradlew.bat build
```

首次构建需要下载 Gradle 和项目依赖，耗时会明显长于后续构建。

## IDE 导入

使用 IntelliJ IDEA 或其他支持 Gradle 的 IDE 打开仓库根目录：

1. 将项目 JDK 和 Gradle JVM 设置为 Java 25。
2. 以 Gradle 项目导入并等待依赖同步完成。
3. 不要把 `build/`、`run/` 或 IDE 本地配置加入提交。

## 常用任务

| 目的 | macOS / Linux 命令 |
|------|--------------------|
| 格式化源码与资源 | `bash ./gradlew spotlessApply` |
| 完整构建 | `bash ./gradlew build` |
| 启动开发客户端 | `bash ./gradlew runClient` |
| 启动开发服务端 | `bash ./gradlew runServer` |
| 生成数据 | `bash ./gradlew runData` |

`runData` 会写入 `src/generated/resources/`。提交前请检查生成结果，只保留与本次改动有关的文件。

## 推荐开发流程

1. 从当前活跃的开发分支（如 `0.6.0`）创建独立开发分支。
2. 在开始大型功能或架构调整前先提交 Issue 与维护者确认方向。
3. 让每个提交和 Pull Request 保持单一、明确的目标。
4. 遵循现有代码风格，避免无关格式化和大范围重构。
5. 运行 `spotlessApply`，然后执行与改动相符的运行验证。
6. 至少运行一次完整 `build`。
7. 使用 `git diff` 检查改动，排除生成物、日志、缓存、凭据和其他本地文件。
8. 推送分支并创建 Pull Request。

## 分支与 submodule

请按下列规则处理分支与提交，避免主仓与 submodule 指针不一致。

1. **修改了 gtocore-gtolib（`GTOLib/` submodule）时**  
   提交与推送时，请将 **gtocore-gtolib** 与 **gtocore-main** 都上传到**同名**的各自分支（例如两边都使用 `ae-rework`）。  
   主仓提交中需包含对 `GTOLib` submodule 指针的更新，并指向已推送到远端同名分支上的 gtolib 提交。

2. **未修改 gtocore-gtolib，只修改 gtocore-main 时**  
   可以只在 **gtocore-main** 迁出新分支并推送，不必同步新建 gtolib 分支。

## Pull Request 要求

Pull Request 请包含：

- 改动目的和实现摘要
- 主要影响范围
- 已完成的构建与运行验证
- 关联 Issue（如有）
- 涉及界面或游戏内表现时的截图或录像
- 已知限制或尚未覆盖的情况

维护者可能会要求调整实现、补充验证或缩小改动范围。请不要提交来源不明或许可证不兼容的代码与资源。

提交即表示同意按 [GregTech Odyssey 许可证说明](https://github.com/GregTech-Odyssey/GregTech-Odyssey/blob/main/README_zh.md#许可证) 发布相应内容。
