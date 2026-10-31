# 01 · 能力总览

> [← 返回 00 概要](00-overview.md)

> 本库的**完整能力面**：每个功能解决什么问题、落在哪个类、有什么限制。
> 需要参数细节时跳转到对应章节。本文件不教"怎么做"，只回答"有什么"。

---

## 1. 功能全景

DataSyncLib 把「对象状态的跨端/跨存档传递」拆成 5 个正交维度。任意功能都是这 5 个维度的组合。

```
                     ┌──────────────────────────────┐
   维度 1：方向       │  哪个字段，往哪个方向走？      │
                     │  S2C / C2S / 磁盘 / 不传      │
                     └──────────────┬───────────────┘
                                    ▼
                     ┌──────────────────────────────┐
   维度 2：粒度       │  整个字段？还是容器内容？      │
                     │  值模式 / 访问模式            │
                     └──────────────┬───────────────┘
                                    ▼
                     ┌──────────────────────────────┐
   维度 3：编码       │  用什么字节表示？              │
                     │  StreamCodec / ValueCodec     │
                     └──────────────┬───────────────┘
                                    ▼
                     ┌──────────────────────────────┐
   维度 4：时机       │  什么时候传/存？              │
                     │  手动推送 / 脏标记 / 条件跳过  │
                     └──────────────┬───────────────┘
                                    ▼
                     ┌──────────────────────────────┐
   维度 5：载体       │  放在哪？                     │
                     │  BlockEntity / Entity / 任意  │
                     └──────────────────────────────┘
```

---

## 2. 功能清单（按模块）

### 2.1 声明层：注解

| 注解 | 维度 1（方向） | 关键能力 | 章节 |
| --- | --- | --- | --- |
| `@SyncToClient` | S2C | 增量同步、`autoDetect`、`scheduleUpdate`、`skipWhen`、`listener` | [02 §1](02-annotations.md#1-synctoclient) |
| `@SyncToServer` | C2S | 同上（方向相反） | [02 §2](02-annotations.md#2-synctoserver) |
| `@SaveToDisk` | 磁盘 | `key`、`skipWhen`、`saveEmpty`、`defaultValue`、`defaultValueGetter`、`listener` | [02 §3](02-annotations.md#3-savetodisk) |
| `@Access` | 维度 2 | 强制访问模式；`instanceAsValue` | [02 §4](02-annotations.md#4-access) |
| `@AdditionalHolder` | 维度 5 | 扁平化 / `childManager` 子管理器 | [02 §5](02-annotations.md#5-additionalholder) |
| `@Codec` | 维度 3 | 静态 codec 字段 / 4 个实例方法 | [02 §6](02-annotations.md#6-codec) |
| `@Conversion` | 维度 3 | `toManaged` / `toField` 类型变换 | [02 §7](02-annotations.md#7-conversion) |
| `@Strategy` | 维度 4 | 自定义相等性 | [02 §8](02-annotations.md#8-strategy) |
| `@Generic` | 维度 3 | 强制泛型工厂链 | [02 §9](02-annotations.md#9-generic) |
| `@AddToManager` | 维度 1 | 纳入管理，不自动同步/增量存盘 | [02 §10](02-annotations.md#10-addtomanager) |
| `@RemoteCall` | 方法级 | 跨端 fire-and-forget 调用 | [02 §11](02-annotations.md#11-remotecall) |

**注意 `@AddToManager` 的边界**：它管理的字段会参与 `writeAllToValue` / `readAllFromValue`，
但**不**参与 `writeToValue`（增量存盘）与任何自动同步。用途是喂给 `FieldDataCodec` 整体序列化。

### 2.2 载体层：三类宿主

| 宿主形态 | 实现方式 | 自动获得 | 需手动做 |
| --- | --- | --- | --- |
| `FieldDataHolderBlockEntity` | 继承 | 区块同步 (`field_sync`)、存档 (`field_save`)、版本号 | `setChanged()` + 推送 |
| `FieldDataHolderEntity` | 继承 | 存档 (`field_save`)、版本号 | 推送（实体无区块同步载荷） |
| 任意对象 | 实现 `IFieldDataHolder` | 无 | 全部（编解码入口由外层驱动） |

三者共享同一套字段扫描与编解码，区别只在"钩子接在哪"。

### 2.3 管理层：`FieldDataManager`

一个 holder 实例对应一个 manager，提供 4 组操作：

| 组 | 方法 | 用途 |
| --- | --- | --- |
| 查询 | `getFieldDefinition(...)` ×4 重载 | 反查字段定义 |
| 置脏 | `markFieldsForSync`、`markAsChanged`、`clearAllChangeMarks` | 手动控制脏状态 |
| 网络 | `writeToNetworkBuffer`、`readFromNetworkBuffer`、`updateFieldDirtyFlags`、`hasSyncFields` | 编解码字段集 |
| 磁盘 | `writeToValue` / `writeAllToValue` / `writeFieldsToValue` / `writeFieldToValue` + 对应读 | 编解码字段集（全量/子集/单字段） |

详见 [03 §2](03-core-api.md#2-fielddatamanager)。

### 2.4 传输层：`DataSyncNetwork`

| 能力 | 方法 |
| --- | --- |
| BlockEntity 推送 | `syncBlockEntityToClient` / `syncBlockEntityToServer`（各 3 个重载） |
| Entity 推送 | `syncEntityToClient` / `syncEntityToServer`（各 3 个重载） |
| 远程调用 | `callBlockEntityOn*` / `callEntityOn*` / `sendBlockEntityTo*` / `sendEntityTo*` |
| 通道管理 | `CHANNEL`、`init()` |

详见 [03 §3](03-core-api.md#3-datasyncnetwork)。

### 2.5 编码层：codec 三层

| 抽象 | 介质 | 用途 |
| --- | --- | --- |
| `StreamCodec<B,T>` | `FriendlyByteBuf` | 网络 |
| `ValueCodec<T>` | `ValueOps` carrier | 磁盘 |
| `DataSyncCodec<T>` | 两者合一 | 注册表条目 |

`ValueOps` 提供 19 种载体类型 id，与历史 `Data` 系统字节兼容。
`ValueConverters` 让 SELF 值降级为普通载体值。

详见 [05](05-codecs.md)。

### 2.6 数据模型层

| 模块 | 类 | 能力 |
| --- | --- | --- |
| 注册表 | `Registry<K,V>` | 稳定 id 分配、3 种 codec、全局自动注册 |
| 组件系统 | `DataComponentKey` / `Map` / `Registry` | 身份键、合并语义、30+ 预置 builder |
| 版本化文件 | `FileUtil` | 带版本头的读写、旧格式回退 |

详见 [06](06-data-models.md)。

### 2.7 扩展层

四个可注册维度，决定"框架认识什么东西"：

| 注册点 | 注册方法 | 决定 |
| --- | --- | --- |
| 字段工厂（精确类型） | `registerFactory(Class, Factory)` | 某类型用哪个 `DataField` |
| 字段工厂（接口/谓词） | `registerInterfaceFactory` / `registerCustomFactory` | 按接口/谓词匹配，带优先级 |
| 泛型工厂 | `registerGenericFactory` / `registerCustomGenericFactory` | 带泛型参数的解析 |
| 哈希策略 | `registerStrategy(Class, Hash.Strategy)` | 变更检测的相等性 |

访问器另有 4 个对应的 `register*Access*` 方法。详见 [04](04-extension-points.md)。

### 2.8 自管理序列化：`IDataSerializable`

不走字段扫描，对象**自己**实现 6 个方法：`detectChange` / `markAsChanged` / `clearChanged` /
`isChanged` / `writeBuffer`+`readBuffer` / `writeValue`+`readValue`。

字段类型实现该接口时，框架自动识别（`SerializableAccess`）。详见 [03 §6](03-core-api.md#6-idataserializable)。

### 2.9 监听与响应式

| 机制 | 位置 | 触发时机 |
| --- | --- | --- |
| `@SyncTo*.listener` | 注解 | 网络接收并应用后 |
| `@SaveToDisk.listener` | 注解 | 磁盘加载后（字段存在时） |
| `IFieldDataHolder.scheduleUpdate` | 接口 | 收到 `scheduleUpdate=true` 字段后 |
| `*NotifiableHolder` | `listener/` | holder 自身收发时 |
| `ISyncNotifiable` | 接口 | 上述 holder 的 setter |

详见 [03 §5](03-core-api.md#5-监听器-holder-体系)。

### 2.10 远程调用

`@RemoteCall` + `RemoteInvoker` + `RemoteNetwork`：方法级跨端调用。

能力边界：
- 只支持 `void` 方法（无返回通道）；
- 注解即白名单；
- 支持父类、接口 default、嵌套 holder 上的方法；
- 线上开销 2 字节索引 + 参数；
- 端由 `side()` 强制。

详见 [03 §4](03-core-api.md#4-远程调用)。

---

## 3. 字段处理规则（决定功能是否生效）

扫描一个字段时走的分支：

```
1. primitive / primitive 数组        → IntField / IntArrayAccess ...
2. Collection / Map / 数组           → *Access（配 Hash.Strategy）
3. 实现 IFieldDataHolder             → FieldDataHolderAccess
4. 实现 IDataSerializable            → SerializableAccess
5. 实现 Forge INBTSerializable       → TagSerializableAccess
6. DataSyncCodec.get(type) 命中      → ObjCodecField
7. 都不行                            → 扫描期抛异常
```

### 值模式 vs 访问模式

| | 值模式 | 访问模式 |
| --- | --- | --- |
| 触发 | 非 final 普通字段 | `final` 或 `@Access` |
| 变更检测 | 快照/内容比对 | 容器自身 + 引用比对 |
| 写回 | 替换字段值 | 就地修改 |
| 适用于 | 整体替换语义 | 引用不变、内容变 |

### 访问器家族（13 种容器 + 11 种数组）

| 类别 | 类 |
| --- | --- |
| 集合 | `CollectionAccess`、`IntCollectionAccess`、`LongCollectionAccess` |
| Map | `MapAccess`、`Object2IntMapAccess`、`Object2LongMapAccess`、`Reference2IntMapAccess`、`Reference2LongMapAccess` |
| Holder | `FieldDataHolderAccess`、`ChildManagerAccess`、`SerializableAccess` |
| Forge | `TagSerializableAccess` |
| 数组 | `ArrayAccess`、`BooleanArrayAccess`、`ByteArrayAccess`、`CharArrayAccess`、`ShortArrayAccess`、`IntArrayAccess`、`LongArrayAccess`、`FloatArrayAccess`、`DoubleArrayAccess`、`FieldDataHolderArrayAccess`、`SerializableArrayAccess`、`TagSerializableArrayAccess` |

---

## 4. 时机控制机制汇总

| 机制 | 作用域 | 语义 |
| --- | --- | --- |
| `autoDetect` | `@SyncTo*` | `false` = 不自动比对，需手动置脏 |
| `markFieldsForSync` | 手动 | 立即标脏（按名字/定义） |
| `updateFieldDirtyFlags` | 手动 | 跑一轮比对 |
| `skipWhen` | 注解 | 谓词返回 `true` = 本轮跳过 |
| `defaultValue` / `defaultValueGetter` | `@SaveToDisk` | 等于默认值 = 不写盘 |
| `saveEmpty` | `@SaveToDisk` | 空值是否写入 |
| `writeAll` / `all` 参数 | 网络/编解码 | 全量 vs 增量 |
| `autoDetectOnly` 参数 | 网络/编解码 | 是否越过 `autoDetect=false` |

**四者的组合语义**（写盘时的实际判断顺序）：

```
if (skipWhen(value))            → NOT_PERSISTED（不写）
else if (value == defaultValue) → NOT_PERSISTED（不写）
else if (isEmpty && !saveEmpty) → 不写
else                            → 写入
```

---

## 5. 限制与已知边界

| 限制 | 说明 |
| --- | --- |
| 同步无自动 tick 钩子 | 裸基类必须手动推送；GTCEu 桥接层有 40 tick 兜底 |
| 字段索引与类结构绑定 | 两端字段集合不同 → 索引错位（详见排错 §9） |
| `VERSION` 全局静态 | 无法按类型分别版本化（需覆盖 save/load） |
| 远程调用无返回值 | 只能用字段同步回传 |
| `@Conversion.toField` 缺失静默忽略 | 不报错，易漏 |
| `Registry.freeze()` 后不可注册 | 常量必须在 `static {}` 之前声明完 |
| `CustomTypes` 不对下游开放 | id 空间已占满，应改用 `ValueConverters` |
| `FieldDataCodec` 的 ThreadLocal | 同线程嵌套 encode/decode 会污染 |
| 枚举按 ordinal 存盘（fixed） | 重排常量会破坏旧存档 |
| `static` / `transient` 字段跳过 | 不参与任何管理 |

---

## 6. 模块 → 源码路径对照

| 模块 | 包路径 |
| --- | --- |
| 注解 | `annotations/` |
| 核心（holder/manager/定义） | 根包 |
| 字段实现 | `field/`、`field/access/`、`field/object/` |
| codec | `datastream/codec/` |
| 数据模型 | `datastream/`（Component）、`util/Registry` |
| 网络 | `network/` |
| 远程调用 | `remote/` |
| 监听/Holder | `listener/`、`util/holder/` |
| 工具 | `util/`、`util/cache/` |
| Forge 集成 | `forge/` |
| 载体基类 | `blockentity/`、`entity/` |
