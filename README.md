<!--
公开文件：本 README 会对外发布。
禁止写入私有仓库名称、内部架构、保护或加密实现、凭据、密钥、内部构建流程及权限细节。
这里只允许保留项目公开介绍、经确认的闭源声明和面向外部的贡献指南。
-->

# GTOCore-Main

[English](README_EN.md)

## 项目介绍

GTOCore 是 GregTech Odyssey 的统一核心模组，面向 Minecraft 1.20.1 和 Forge 47.4.20，模组 ID 为 `gtocore`。

本仓库提供 GTOCore 的公开源码、资源、数据生成入口和构建配置。外部贡献者可以使用仓库中已提供的依赖完成编译、数据生成和本地运行，不需要访问私有仓库，也不需要初始化私有 submodule。

`com.gtocore` 范围内的公开代码依据本仓库 [LICENSE](LICENSE) 发布；gtolib 仍为闭源组件。

## gtolib 闭源声明

#### 为什么 gtocore 内的 gtolib 选择闭源

我们深感遗憾地宣布，由于持续的代码剽窃问题，我们不得不将 gtolib 的核心代码暂时闭源。

**具体情况：**

- 多个个人或团体直接**抄袭，侵占**我们的开源库
- 进行批量替换操作，将所有 "gto" 标识替换为他们自己的标识
- 完全剽窃我们的研发成果，并重新包装发布
- 声称是他们自主开发的整合包，完全无视原创者的权益
- 面对我们的抗议和沟通，以「学习用途，日后会修改」为借口拒绝停止侵权行为

**证据保存：**

我们及时保存了相关证据，其中一个剽窃者的仓库备份可见：
[https://github.com/wanggugu197/Pillar_of_shame](https://github.com/wanggugu197/Pillar_of_shame)

在多次沟通无效后的几天内，我们正式开始对核心代码进行闭源处理。

**我们的立场：**

这一决定是为了保护我们团队两年来的心血结晶。我们相信真正的开发者会理解并支持原创者的权益保护行为。

感谢您的理解与支持。

## 外部贡献指南

### 可贡献范围

欢迎提交以下改动：

- `src/main/java/com/gtocore/` 下的公开源码
- `src/main/resources/` 和 `src/generated/resources/` 中与公开源码直接相关的资源与数据
- 面向公开项目的构建脚本和文档
- 错误修复、兼容性改进、性能优化和可维护性改进

如需参与私有模块的开发，请先联系维护人员了解当前情况并取得相应授权。

请不要直接修改或替换闭源组件及仓库内的预构建二进制依赖。相关问题请提交 Issue，由维护者判断是否需要内部处理。

### 开发环境

- Java 21
- Git
- 可访问 Gradle 和项目依赖仓库的网络环境
- 足够的可用内存；当前 Gradle 配置的最大堆内存为 8 GiB

请确认 `java -version` 指向 Java 21，并在 IDE 和终端中使用同一个 JDK。

### 克隆与构建

外部贡献者只需普通 clone，不要初始化私有 submodule：

```bash
git clone https://github.com/GregTech-Odyssey/GTOCore-Main.git
cd GTOCore-Main
bash ./gradlew build
```

若有 gtolib 权限：在 `cd` 之后先执行 `git submodule update --init GTOLib`，再 `bash ./gradlew build`。

Windows PowerShell 或命令提示符：

```bat
gradlew.bat build
```

首次构建需要下载 Gradle 和项目依赖，耗时会明显长于后续构建。

### IDE 导入

使用 IntelliJ IDEA 或其他支持 Gradle 的 IDE 打开仓库根目录：

1. 将项目 JDK 和 Gradle JVM 设置为 Java 21。
2. 以 Gradle 项目导入并等待依赖同步完成。
3. 不要把 `build/`、`run/` 或 IDE 本地配置加入提交。

### 常用任务

| 目的 | macOS / Linux 命令 |
|------|--------------------|
| 格式化源码与资源 | `bash ./gradlew spotlessApply` |
| 完整构建 | `bash ./gradlew build` |
| 启动开发客户端 | `bash ./gradlew runClient` |
| 启动开发服务端 | `bash ./gradlew runServer` |
| 生成数据 | `bash ./gradlew runData` |

`runData` 会写入 `src/generated/resources/`。提交前请检查生成结果，只保留与本次改动有关的文件。

### 推荐贡献流程

1. 从最新的 `main` 分支创建独立开发分支。
2. 在开始大型功能或架构调整前先提交 Issue 与维护者确认方向。
3. 让每个提交和 Pull Request 保持单一、明确的目标。
4. 遵循现有代码风格，避免无关格式化和大范围重构。
5. 运行 `spotlessApply`，然后执行与改动相符的运行验证。
6. 至少运行一次完整 `build`。
7. 使用 `git diff` 检查改动，排除生成物、日志、缓存、凭据和其他本地文件。
8. 推送分支并创建 Pull Request。

### Pull Request 要求

Pull Request 请包含：

- 改动目的和实现摘要
- 主要影响范围
- 已完成的构建与运行验证
- 关联 Issue（如有）
- 涉及界面或游戏内表现时的截图或录像
- 已知限制或尚未覆盖的情况

维护者可能会要求调整实现、补充验证或缩小改动范围。请不要提交来源不明或许可证不兼容的代码与资源。

### 许可证

提交贡献即表示你同意按本仓库 [LICENSE](LICENSE) 中的许可证发布相应内容。
