<!--
公开文件：本 README 会对外发布。
禁止写入私有仓库名称、内部架构、保护或加密实现、凭据、密钥、内部构建流程及权限细节。
这里只允许保留项目公开介绍、经确认的闭源声明和参与方式说明；团队开发流程写在 docs/development.md。
-->

# GTOCore-Main

[English](README_EN.md)

## 项目介绍

GTOCore 是 GregTech Odyssey 的统一核心模组，面向 Minecraft 1.20.1 和 Forge 47.4.20，模组 ID 为 `gtocore`。

本仓库公开 GTOCore 的源码与资源，方便大家阅读、了解和审阅实现。

本仓库内容的许可以 [GregTech Odyssey 许可证说明](https://github.com/GregTech-Odyssey/GregTech-Odyssey/blob/main/README_zh.md#许可证) 为准；gtolib 仍为闭源组件。

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

## 参与方式

#### 想法与反馈

如果你有好的想法、建议，或者在游玩中发现了问题，欢迎到 [Issue](https://github.com/GregTech-Odyssey/GregTech-Odyssey/issues) 留言。每一条反馈我们都会认真阅读，合适的想法会由维护者整理进后续版本。

#### 关于代码修改

GTOCore 的功能设计、数值平衡与版本节奏需要整体把控，因此代码改动目前统一由开发团队完成。比起直接提交代码，一条描述清楚的 Issue 往往更能帮到我们——请放心把想法交给我们来实现。

#### 关于构建与测试

源码公开是为了透明，便于大家查阅与审阅。完整构建依赖团队内部的组件，公开环境无法直接编译或运行，也无需自行搭建开发环境进行测试；如果想体验最新内容，请使用整合包发布的正式版本或测试版本。

#### 构建与发布标记（维护者）

推送到本仓库时，在 commit message 里写单独成词的标记来触发签名构建：

| 标记 | 作用 | 谁可以用 |
|---|---|---|
| `-b` | 只构建签名 jar，不发布 | 组织成员 |
| `-p` | 构建并只发布到 Maven，用于 dev 版整合包 | 发布者账号 |
| `-cf` | 构建并发布到 Maven 与 CurseForge，用于 CurseForge 版整合包 | 发布者账号 |

其他账号写的 `-p`、`-cf` 会被忽略。完整规则见 [签名构建说明](docs/build-signing.md)。

## 许可证

见 [GregTech Odyssey 许可证说明](https://github.com/GregTech-Odyssey/GregTech-Odyssey/blob/main/README_zh.md#许可证)。
