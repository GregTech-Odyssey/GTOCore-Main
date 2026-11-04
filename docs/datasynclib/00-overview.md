# 00 · 概要

> DataSyncLib 功能参考文档的入口。本文件提供**一份概要、一份目录**，不承载细节。
> 需要具体参数、API 签名或案例时，按目录跳转。
>
> 源码根：`F:\GitHub\DataSyncLib\src\main\java\com\gto\datasynclib\`
> 版本基线：`26.10.11`（`GTOCore-Main/gradle.properties` → `datasynclib_version`）

---

## 1. 概要

### 1.1 它是什么

DataSyncLib 是 Minecraft 1.20.1 / Forge 上的**字段级同步与持久化框架**。
给字段加注解，它负责三件事：

| 职责 | 注解 | 落点 |
| --- | --- | --- |
| 服务端 → 客户端增量同步 | `@SyncToClient` | `FieldDataManager.writeToNetworkBuffer` |
| 客户端 → 服务端同步 | `@SyncToServer` | 同上（反方向） |
| 磁盘持久化（自定义二进制，非裸 NBT） | `@SaveToDisk` | `FieldDataManager.writeToValue` |

它替掉的三类手写样板：

1. `saveAdditional` / `load` 里逐字段 `tag.putInt(...)` / `tag.getInt(...)`；
2. 自维护 dirty 标记 + 手写 `FriendlyByteBuf` 编解码 + 自注册网络包；
3. 自维护「存档版本号 → 字段迁移」的 if/else 链。

### 1.2 五维能力模型

任何功能都是这五个维度的组合：

| 维度 | 问题 | 选项 |
| --- | --- | --- |
| 方向 | 往哪走？ | S2C / C2S / 磁盘 / 不传 |
| 粒度 | 整个字段还是容器内容？ | 值模式 / 访问模式 |
| 编码 | 用什么字节？ | `StreamCodec` / `ValueCodec` |
| 时机 | 什么时候传？ | 手动推送 / 脏标记 / 条件跳过 |
| 载体 | 放在哪？ | BlockEntity / Entity / 任意对象 |

### 1.3 三条铁律

| 铁律 | 说明 |
| --- | --- |
| **同步是推送式的** | 没有自动 tick 钩子。必须调 `DataSyncNetwork.syncXxx()`（GTCEu 桥接层例外，有 40 tick 兜底） |
| **存盘要 `setChanged()`** | `@SaveToDisk` 决定"写什么"，不决定"什么时候写" |
| **`autoDetect = false` 要手动置脏** | 不参与自动比对，必须 `markFieldsForSync("字段名")` |

### 1.4 最小可用形态

只有三行是必须记住的：

```java
public class MyBlockEntity extends FieldDataHolderBlockEntity {

    @SyncToClient @SaveToDisk
    private int energy;

    public void serverTick() {
        energy++;
        setChanged();                                   // ① 标记写盘
        DataSyncNetwork.syncBlockEntityToClient(this);  // ② 标记同步（增量）
    }
}
```

非 BlockEntity 对象实现 `IFieldDataHolder` + 持有 `LazyFieldDataManager` 即可；
GTOCore 机器继承 GTCEu `MetaMachineBlockEntity`，用 `requestSync()` 代替第 ② 步。
三种形态详见 [03 §1](03-core-api.md#1-基类与核心接口) 与 [08 §0](08-gtocore-usage.md#0-先搞懂-gtocore-的接入方式)。

---

## 2. 目录

### 2.1 文档清单

| 文件 | 覆盖范围 | 主要读者 |
| --- | --- | --- |
| **00-overview.md**（本文件） | 概要、目录、能力矩阵、全局约束、版本基线 | 所有人 |
| [01-capabilities.md](01-capabilities.md) | 全部能力清单与落点类、字段分类规则、时机控制机制、已知限制 | 想知道"这个库能干什么" |
| [02-annotations.md](02-annotations.md) | 11 个注解的完整参数表、内部映射、回调签名、边界行为 | 写注解时查参数 |
| [03-core-api.md](03-core-api.md) | 基类与接口、`FieldDataManager`、`DataSyncNetwork`、远程调用、监听器 Holder、`IDataSerializable`、工具类 | 调 API 时查签名 |
| [04-extension-points.md](04-extension-points.md) | 四层解析链、factory / access / 策略注册、优先级体系、自定义 `DataField` | 让框架认识新类型 |
| [05-codecs.md](05-codecs.md) | 三层 codec、内置类型全表、`ValueOps` 完整 API、`ValueConverters`、`CustomTypes`、枚举策略、Buffer 工具 | 处理序列化 |
| [06-data-models.md](06-data-models.md) | `Registry` 完整 API、`DataComponent` 三件套、`FileUtil` 版本化文件 | 处理成组数据与文件 |
| [07-architecture.md](07-architecture.md) | 分层架构、字段分类、同步与持久化流程、线程模型、线格式 | 理解内部机制 |
| [08-gtocore-usage.md](08-gtocore-usage.md) | GTOCore / GTOLib 集成（`ISync` 桥接层）+ 18 个真实案例（带行号）+ 本仓未使用功能清单 | 在 GTOCore 里写代码 |
| [09-pitfalls.md](09-pitfalls.md) | 按症状索引的排错手册 + 一页纸急救表 | 排查问题 |

### 2.2 按主题检索

| 我想查…… | 去哪 |
| --- | --- |
| 某个注解有什么参数 | [02](02-annotations.md) |
| 某个类/方法的签名 | [03](03-core-api.md) |
| 内置类型支持哪些 | [05 §2](05-codecs.md#2-内置类型全表) |
| 自定义类型怎么注册 | [05 §3](05-codecs.md#3-自定义类型接入方式) |
| 让框架认识新的容器/泛型 | [04](04-extension-points.md) |
| 同步/存盘的内部流程 | [07](07-architecture.md) |
| GTOCore 里该怎么写 | [08](08-gtocore-usage.md) |
| 字段不同步 / 存档丢失 | [09](09-pitfalls.md) |
| 有哪些功能、分别在哪个类 | [01 §2](01-capabilities.md#2-功能清单按模块) |

---

## 3. 能力矩阵

### 3.1 README 列出的核心特性 → 落点

| # | 能力 | 实现类 / 注解 | 详见 |
| --- | --- | --- | --- |
| 1 | 服务端→客户端字段同步 | `@SyncToClient`、`writeToNetworkBuffer` | [02 §1](02-annotations.md#1-synctoclient) |
| 2 | 客户端→服务端字段同步 | `@SyncToServer` | [02 §2](02-annotations.md#2-synctoserver) |
| 3 | 磁盘持久化 | `@SaveToDisk`、`writeToValue` | [02 §3](02-annotations.md#3-savetodisk) |
| 4 | 增量同步（脏标记 + 索引寻址） | `updateFieldDirtyFlags` | [07 §3](07-architecture.md#3-同步流程服务端--客户端) |
| 5 | 可扩展编解码（30+ 内置类型） | `DataSyncCodec` | [05 §2](05-codecs.md#2-内置类型全表) |
| 6 | 字段级变更通知 | `@SyncTo*.listener`、`@SaveToDisk.listener` | [02](02-annotations.md#回调方法签名总表) |
| 7 | 响应式 Holder | `listener/*NotifiableHolder`、`ISyncNotifiable` | [03 §5](03-core-api.md#5-监听器-holder-体系) |
| 8 | DataComponent 系统 | `DataComponentKey` / `Map` / `Registry` | [06 §2](06-data-models.md#2-datacomponent-三件套) |
| 9 | Registry 工具 | `util/Registry` | [06 §1](06-data-models.md#1-registry) |
| 10 | 高性能（MethodHandle / FastUtil / VarInt） | `DataFieldDefinition.source`、`VarInts` | [07 §1](07-architecture.md#1-分层架构) |
| 11 | 19 种载体值类型 + `ValueConverters` | `ValueOps`、`ValueConverters` | [05 §4](05-codecs.md#4-valueops-carrier-模型) |
| 12 | 开箱即用基类 | `FieldDataHolderBlockEntity` / `Entity` | [03 §1](03-core-api.md#1-基类与核心接口) |
| 13 | 嵌套 Holder（扁平化 / 子管理器） | `@AdditionalHolder(childManager)`、`ChildFieldDataHolder` | [02 §5](02-annotations.md#5-additionalholder) |
| 14 | 泛型层级解析 | `ReflectUtil`、`@Generic` | [04 §5](04-extension-points.md#5-泛型解析与-generic-注解) |
| 15 | Registry 全局 codec 自动注册 | `Registry.registerToGlobalCodecs` | [06 §1.6](06-data-models.md#16-全局-codec-自动注册) |
| 16 | 自定义变更检测策略 | `@Strategy`、`registerStrategy` | [04 §6](04-extension-points.md#6-策略注册) |

### 3.2 README 未列出但源码存在的能力

| 能力 | 实现类 | 详见 |
| --- | --- | --- |
| 远程方法调用（跨端 fire-and-forget） | `@RemoteCall`、`RemoteInvoker`、`RemoteNetwork` | [03 §4](03-core-api.md#4-远程调用) |
| 自管理序列化对象 | `IDataSerializable`、`AbstractDataSerializable` | [03 §6](03-core-api.md#6-idataserializable) |
| 字段工厂扩展体系 | `FieldDefinitionStorage.register*Factory` | [04 §2](04-extension-points.md#2-值模式工厂注册registerfactory) |
| 容器访问器家族（13 种 + 11 种数组） | `field/access/*` | [04 §9](04-extension-points.md#9-已注册的访问器与服务覆盖范围) |
| 条件存盘 / 默认值省略 | `@SaveToDisk(skipWhen/defaultValue/saveEmpty)` | [02 §3](02-annotations.md#3-savetodisk) |
| 类型转换存取 | `@Conversion` | [02 §7](02-annotations.md#7-conversion) |
| 强制泛型工厂 | `@Generic` | [02 §9](02-annotations.md#9-generic) |
| 仅纳入管理 | `@AddToManager` | [02 §10](02-annotations.md#10-addtomanager) |
| 强制访问模式 | `@Access(instanceAsValue)` | [02 §4](02-annotations.md#4-access) |
| 部分字段存取（子集序列化） | `writeFieldsToValue` / `readFieldsFromValue` | [03 §2.6](03-core-api.md#26-磁盘编解码) |
| 单字段读写 | `writeFieldToValue` / `readFieldFromValue` | [03 §2.6](03-core-api.md#26-磁盘编解码) |
| 自定义同步/存盘数据块 | `IFieldDataHolder` 的 4 个 custom data 钩子 | [03 §1.1](03-core-api.md#11-ifielddataholder) |
| 版本化文件读写 | `FileUtil.writeVersioned` / `readVersioned` | [06 §3](06-data-models.md#3-版本化文件) |
| JSON / NBT 作为 custom type | `JsonUtils`、`NbtUtil` | [05 §6](05-codecs.md#6-customtypes库内部-registry) |
| 枚举序列化策略（fixed vs 名字） | `EnumUtil` | [05 §7](05-codecs.md#7-枚举序列化) |
| 数组哈希策略包装 | `HashUtil.arrayStrategy` | [04 §6.4](04-extension-points.md#64-为自定义数组派生策略) |
| Buffer 工具（VarInt / 版本号） | `VarInts`、`VersionedFriendlyByteBuf` | [05 §8](05-codecs.md#8-buffer-工具) |
| 缓存实现（3 种） | `util/cache/*` | [03 §7](03-core-api.md#7-工具类速查) |
| `DataField.mustDetect()` 强制检测 | `DataField` 接口 | [04 §8.2](04-extension-points.md#82-mustdetect-的语义) |

---

## 4. 全局约束

所有功能共享的前提。违反任一条，功能不生效。

| 约束 | 说明 |
| --- | --- |
| 宿主必须实现 `IFieldDataHolder` | 否则字段不被扫描 |
| 注解只对**实例字段**生效 | `static` / `transient` 被跳过 |
| 字段类型必须有 codec | 否则扫描期抛 `No codec registered for type` |
| 注册必须早于首次扫描 | `DataSyncCodec.register` / factory 注册应在模组构造期 |
| 同步是**推送式** | 裸基类无自动 tick 钩子（GTCEu 桥接层有 40 tick 兜底） |
| 存盘需要 `setChanged()` | BlockEntity 系必须；GTCEu 机器由框架处理 |
| `VERSION` 是全局静态 | `FieldDataHolderBlockEntity.VERSION` 被所有 holder 共享 |

---

## 5. 版本基线与破坏性变更

| 项 | 值 |
| --- | --- |
| DataSyncLib | `26.10.11` |
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 21（字节码目标） |
| 同步通道 | `datasynclib:sync`，协议版本 `"2"` |

### 26.9.4 重命名（旧代码/旧文档不再适用）

| 旧名 | 新名 |
| --- | --- |
| `condition` | `skipWhen` |
| `autoUpdate` | `autoDetect` |
| `notifyUpdate` | `scheduleUpdate` |
| `saveNull` | `saveEmpty` |
| `getFunction` | `toManaged` |
| `setFunction` | `toField` |
| `createInstance` | `instanceAsValue` |
| `DataFieldDefinition#autoUpdate` | `#autoDetect` |
| `DataFieldDefinition#notifyUpdate` | `#scheduleUpdate` |
| `DataFieldDefinition.createInstance` | `instanceAsValue` |
| 参数名 `autoOnly` | `autoDetectOnly` |

### 26.9.2 协议变更

消息索引从 4 条（0/1 = BE 双向，2/3 = Entity 双向）改为 2 条
（0 = BE，1 = Entity，各自双向）。`PROTOCOL_VERSION` 因此升到 `"2"`。

### 26.8.1 新增

- `@AdditionalHolder(childManager = true)` 子管理器模式
- `ReflectUtil` 泛型层级解析
- `Registry` 全局 codec 自动注册

---

## 6. 与 DataSyncLib 自带文档的分工

| 文档 | 定位 |
| --- | --- |
| `DataSyncLib/docs/index.html` | 上游原文档（架构 + API，含英文版） |
| `DataSyncLib/README_zh.md` | 上游特性清单与示例 |
| **本目录** | 功能参考：逐模块覆盖 + 扩展点 + GTOCore 集成 + 排错 |

本目录的差异点：

1. 覆盖了上游 README 未列出的模块（`IDataSerializable`、factory 注册体系、远程调用、工具类）；
2. 每个功能标注**实现类**，可按代码定位；
3. GTOCore 独有的 `ISync` 桥接层与两条同步路径（反编译验证）；
4. 按症状组织的排错手册。

若与上游文档冲突，以本仓源码为准。
