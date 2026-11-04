# 03 · 核心 API 参考

> [← 返回 00 概要](00-overview.md)

> 运行期 API：宿主基类、`FieldDataManager`、`DataSyncNetwork`、远程调用、监听器 Holder、
> `IDataSerializable`、工具类。每条给签名 + 语义 + 约束。
> 源码根：`F:\GitHub\DataSyncLib\src\main\java\com\gto\datasynclib\`

---

## 目录

1. [基类与核心接口](#1-基类与核心接口)
2. [FieldDataManager](#2-fielddatamanager)
3. [DataSyncNetwork](#3-datasynclibnetwork)
4. [远程调用](#4-远程调用)
5. [监听器 Holder 体系](#5-监听器-holder-体系)
6. [IDataSerializable](#6-idisataserializable)
7. [工具类速查](#7-工具类速查)

---

## 1. 基类与核心接口

### 1.1 `IFieldDataHolder`

**源码**：`IFieldDataHolder.java`

```java
public interface IFieldDataHolder {
    // ===== 必须实现 =====
    FieldDataManager getFieldDataManager();

    // ===== 可选重写 =====
    @Nullable default IFieldDataHolder getParentHolder();
    default Object getSource(DataFieldDefinition<?> definition);
    default void markFieldsForSync(String... names);
    default void scheduleUpdate(LogicalSide side);
    default void writeCustomSyncData(FriendlyByteBuf buf, boolean writeAll);
    default void readCustomSyncData(FriendlyByteBuf buf);
    default void writeCustomSaveData(Map<String, Object> data, ValueOps ops);
    default void readCustomSaveData(Map<String, Object> data, ValueOps ops);

    // ===== 远程调用 =====
    default byte[] writeRemoteCall(Object target, String method, Object... args);
    static void handleRemoteCall(Object target, byte[] payload);
    static void handleRemoteCall(Object target, byte[] payload, @Nullable LogicalSide side);
}
```

#### 各成员语义

| 成员 | 默认实现 | 何时重写 |
| --- | --- | --- |
| `getFieldDataManager()` | 抽象 | **必须**：返回 `LazyFieldDataManager.get()` |
| `getParentHolder()` | 返回 `null` | 嵌套 holder 需要把 `scheduleUpdate` 冒泡给外层时 |
| `getSource(definition)` | 走 `definition.source` 组合链，`null` 则返回 `this` | 需要自定义字段宿主解析时 |
| `markFieldsForSync(names)` | 委托 `getFieldDataManager().markFieldsForSync(names)` | 一般不需要 |
| `scheduleUpdate(side)` | 转发给 `getParentHolder()` | 有 `scheduleUpdate=true` 字段时要响应 |
| `writeCustomSyncData` | 空 | 需要在字段之外额外同步数据 |
| `readCustomSyncData` | 空 | 配对读取 |
| `writeCustomSaveData` | 空 | 需要存字段覆盖不到的数据（map value 是 carrier 值） |
| `readCustomSaveData` | 空 | 配对读取，可用 `ops.dataVersion()` 做迁移 |

#### `getSource` 的作用

扁平化 `@AdditionalHolder` 时，父 manager 里的字段定义带 `source` 路径。
`getSource` 负责把「父对象」解析成「真正持有该字段的对象」：

```java
default Object getSource(DataFieldDefinition<?> definition) {
    var source = definition.source;
    if (source == null) return this;      // 字段直接声明在 this 上
    return source.apply(this);            // 沿 getter 链走进子对象
}
```

#### custom data 钩子的用途与位置

```
网络写： writeCustomSyncData(buf, writeAll) → [字段们] 
网络读： readCustomSyncData(buf) → [字段们] 
磁盘写： writeCustomSaveData(map, ops) → [字段们] → ops.createStringMap(map)
磁盘读： [map] → readCustomSaveData(map, ops) → [字段们]
```

即 custom 数据在**字段之前**（写）或**之后**（读 map 已构建）。

`writeCustomSaveData` 的 map value 是 **carrier 值**，用 `ops.createInt(3)` 等构造。

### 1.2 `FieldDataHolderBlockEntity`

**源码**：`blockentity/FieldDataHolderBlockEntity.java`

```java
public class FieldDataHolderBlockEntity extends BlockEntity implements IFieldDataHolder {
    public static int VERSION;                                  // 全局存档格式版本
    protected final LazyFieldDataManager fieldDataManager;

    public FieldDataManager getFieldDataManager();
    public @NotNull CompoundTag getUpdateTag();                 // 写 "field_sync"
    public void load(@NotNull CompoundTag tag);                 // field_sync 优先
    protected void saveAdditional(@NotNull CompoundTag tag);    // field_save + version
}
```

#### NBT 键与行为

| 键 | 写入者 | 读取者 | 内容 |
| --- | --- | --- | --- |
| `"field_sync"` | `getUpdateTag()` | `load()`（优先） | `writeToNetworkBuffer(SERVER, true)` 的全量字节 |
| `"field_save"` | `saveAdditional()` | `load()`（次选） | `ops.toBytes(writeToValue(ops))` |
| `"field_data_dataVersion"` | `saveAdditional()` | `load()` | `VERSION` 的 `int` 值 |

#### `load()` 的互斥语义

```java
if (tag.get("field_sync") instanceof ByteArrayTag arr) {
    readFromNetworkBuffer(LogicalSide.CLIENT, arr.getAsByteArray());
    // ← field_save 与 field_data_dataVersion 都被完全忽略
} else if (tag.get("field_save") instanceof ByteArrayTag arr) {
    readFromValue(JavaValueOps.INSTANCE.fromBytes(arr.getAsByteArray()),
                  JavaValueOps.create(tag.getInt("field_data_dataVersion")));
}
```

#### `VERSION` 的全局性

`VERSION` 是 **`static`** 字段，被所有 holder 共享，默认 `0`。
子类声明同名字段**不影响**写入值（`saveAdditional` 编译期绑定基类字段）。

```java
FieldDataHolderBlockEntity.VERSION = 1;   // 模组初始化期，全局生效一次
```

按类型分版本：覆盖 `saveAdditional` / `load` 自行管理。

### 1.3 `FieldDataHolderEntity`

**源码**：`entity/FieldDataHolderEntity.java`

```java
public class FieldDataHolderEntity extends Entity implements IFieldDataHolder {
    public static int VERSION;
    protected final LazyFieldDataManager fieldDataManager;

    public FieldDataManager getFieldDataManager();
    protected void defineSynchedData();                          // 默认空
    public void addAdditionalSaveData(@NotNull CompoundTag tag);
    public void readAdditionalSaveData(@NotNull CompoundTag tag);
}
```

| 项 | BlockEntity 版 | Entity 版 |
| --- | --- | --- |
| 区块同步载荷 | `getUpdateTag()` → `field_sync` | **无** |
| 存盘 | `saveAdditional` / `load` | `addAdditionalSaveData` / `readAdditionalSaveData` |
| NBT 键 | `field_save` / `field_data_dataVersion` | 同名 |
| 版本字段 | `VERSION`（static） | `VERSION`（static，独立的一份） |

**实体没有区块同步**：首次进入视野必须显式推送一次（或依赖 `TRACKING_ENTITY` 时机）。

### 1.4 `LazyFieldDataManager`

**源码**：`LazyFieldDataManager.java`

```java
public final class LazyFieldDataManager {
    public LazyFieldDataManager(IFieldDataHolder holder);
    public LazyFieldDataManager(IFieldDataHolder holder, Class<?> holderClass);
    public FieldDataManager get();     // 双检锁，线程安全
}
```

- 第二个构造器允许**扫描指定类**（而非 `holder.getClass()`）。
  `ChildFieldDataHolder` 用它扫描子对象类。
- `volatile` + 双检锁保证可见性与单实例。

### 1.5 `ChildFieldDataHolder`

**源码**：`ChildFieldDataHolder.java`

```java
public final class ChildFieldDataHolder implements IFieldDataHolder {
    public ChildFieldDataHolder(@NotNull Object target);
    public FieldDataManager getFieldDataManager();     // 扫描 target.getClass()
    public <T> T target();                            // 取回被包装的对象
    public Object getSource(DataFieldDefinition<?> def);  // source==null 时返回 target
    // 4 个 custom data 钩子 + scheduleUpdate 全部转发给 target（若 target 也是 holder）
}
```

用途：`@AdditionalHolder(childManager = true)` 的内部适配器，让**任意对象**获得独立 manager。

### 1.6 `FieldDataCodec<T>`

**源码**：`util/FieldDataCodec.java`

```java
public class FieldDataCodec<T> implements CombinedCodec<T>, IFieldDataHolder {
    public FieldDataCodec(Class<T> objClass, Supplier<T> constructor);
    public FieldDataCodec(Class<T> objClass, Supplier<T> constructor,
                          StreamEncoder<FriendlyByteBuf, ? super T> extraWriter,
                          StreamDecoder<FriendlyByteBuf, ? extends T> extraReader);
}
```

把 POJO 的注解字段打包成一个 codec。

| 特性 | 说明 |
| --- | --- |
| 序列化范围 | `writeAllToValue` / `readAllFromValue`（含 `@AddToManager`） |
| 网络形态 | `buf.writeByteArray(内部 writeToNetworkBuffer(BOTH, true))` |
| 当前实例 | `ThreadLocal<T>` |
| 线程安全 | 跨线程安全；**同线程嵌套会污染** |
| `extraWriter`/`extraReader` | 在字段之外追加自定义网络数据 |

创建方式：

```java
FieldDataCodec<MyConfig> C = FieldDataManager.createCodec(MyConfig.class, MyConfig::new);
```

---

## 2. `FieldDataManager`

**源码**：`FieldDataManager.java` ｜ 每 holder 实例一个。

### 2.1 构造与静态工厂

```java
public FieldDataManager(IFieldDataHolder holder);                 // = holder.getClass()
public FieldDataManager(IFieldDataHolder holder, Class<?> holderClass);

public static <T> FieldDataCodec<T> createCodec(Class<T> objClass, Supplier<T> constructor);
public static <T> FieldDataCodec<T> createCodec(Class<T> objClass, Supplier<T> constructor,
        StreamEncoder<FriendlyByteBuf, ? super T> extraStreamWriter,
        StreamDecoder<FriendlyByteBuf, ? extends T> extraStreamReader);
```

构造时按 `FieldDefinitionStorage.get(holderClass)` 创建全部 `DataField` 实例，
并分组成 4 个数组：`allFields` / `syncToClientFields` / `syncToServerFields` / `saveFields`。

### 2.2 字段查询

```java
DataFieldDefinition<?> getFieldDefinition(Object fieldObject);
DataFieldDefinition<?> getFieldDefinition(Class<?> type, Object fieldObject);
DataFieldDefinition<?> getFieldDefinition(Field field);
DataFieldDefinition<?> getFieldDefinition(String fieldName);
```

| 重载 | 查找方式 |
| --- | --- |
| `(Object)` | 按字段类型的候选定义，用 `definition.get(source) == fieldObject` 匹配 |
| `(Class, Object)` | 同上，指定类型 |
| `(Field)` | 按 `field.getType()` 找候选，比对 `definition.field.equals(field)` |
| `(String)` | 走 `storage.definitionMap`（键 = 存储键） |

### 2.3 变更标记

```java
void markFieldsForSync(@NotNull DataFieldDefinition<?>... fields);
void markFieldsForSync(@NotNull String... fields);      // 未知名 → 抛异常
void clearAllChangeMarks();
void markAsChanged();
void clearChanged();
boolean isChanged();
```

**`markFieldsForSync(String...)` 的键规则**：走 `storage.definitionMap`，
键是**存储键**（`@SaveToDisk(key=...)` 的值，否则字段名）。

| 情况 | 结果 |
| --- | --- |
| 名字不存在 | 抛 `IllegalArgumentException`（`createFieldNotFoundException`） |
| 名字重复 | 幂等，无害 |
| 字段是 `@AddToManager` | 能标脏，但没有同步路径消费 |

### 2.4 变更检测

```java
boolean updateFieldDirtyFlags(LogicalSide side, boolean autoDetectOnly);
```

| 参数 | 语义 |
| --- | --- |
| `side` | `SERVER` → 检查 `syncToClientFields`；`CLIENT` → `syncToServerFields`；`BOTH` → 全部 |
| `autoDetectOnly` | `true`：只检查 `autoDetect=true`；`false`：所有 sync 字段都检查（**越过 `autoDetect=false`**） |

**内部逻辑**（逐字段）：

```java
if (!field.mustDetect() && field.isChanged(source)) {
    hasChange = true;                                    // 已手动标脏
} else if ((!autoDetectOnly || def.autoDetect(side))
           && field.detectChange(side, source, autoDetectOnly)) {
    hasChange = true;                                    // 自动检测到变化
}
```

`mustDetect()` 为 `true` 的字段（容器访问器）**每轮都尝试检测**。

**返回值**：`changed = hasChange || changed`（累积之前 `markAsChanged()` 的标记）。

**重入保护**：`updating` 为 true 时直接返回 `false`。

### 2.5 网络编解码

```java
byte @NotNull [] writeToNetworkBuffer(LogicalSide side, boolean writeAll);
void readFromNetworkBuffer(LogicalSide side, byte @NotNull [] data);
boolean hasSyncFields(LogicalSide side);
```

**字段集选择**：

| side | 写入时 | 读取时 |
| --- | --- | --- |
| `SERVER` | `syncToClientFields` | （读取通常用 CLIENT） |
| `CLIENT` | `syncToServerFields` | `syncToClientFields` |
| `BOTH` | `allFields` | `allFields` |

> 注意读取端的映射：发送方用 `SERVER` 写 `syncToClientFields`，接收方用 `CLIENT` 读
> **同一个** `syncToClientFields`（`side.isClient()` 分支）。索引因此对齐。

**线格式**：

```
[custom sync data]
重复直到 buffer 耗尽：
  VarInt(在字段集中的索引)
  [字段 payload]
```

**`writeToNetworkBuffer` 的副作用**：写完每个字段立刻 `clearChanged(source)`；
全部写完 `changed = false`。

**`readFromNetworkBuffer` 的副作用**：末尾若 `update == true`（有 `scheduleUpdate` 字段）
则调一次 `holder.scheduleUpdate(side)`。

**重入保护**：`writing` 为 true 时返回空数组。

### 2.6 磁盘编解码

```java
// 字段集
Object writeToValue(ValueOps ops);                             // saveFields
Object writeAllToValue(ValueOps ops);                          // allFields（含 @AddToManager）
void   readFromValue(Object data, ValueOps ops);               // saveFields
void   readAllFromValue(Object data, ValueOps ops);            // allFields

// 子集（按名字）
Object writeFieldsToValue(ValueOps ops, String... fields);
void   readFieldsFromValue(Object data, ValueOps ops, String... fields);

// 单字段
Object writeFieldToValue(String field, ValueOps ops);
void   readFieldFromValue(Object data, ValueOps ops, String field);
```

**返回值形态**：

| 方法 | 返回 |
| --- | --- |
| `writeToValue` / `writeAllToValue` / `writeFieldsToValue` | `ops.createStringMap(map)` 或 `ops.createNull()` |
| `writeFieldToValue` | 该字段的 carrier 值，或 `DataField.NOT_PERSISTED` |

**写入顺序**：按存储键排序 → 同一状态字节可复现。

**读取语义**：
- `data` 是 null carrier → **直接返回**（"什么都没存"，不是错误）；
- map 中缺某键 → **跳过该字段**（旧存档安全）；
- `@SaveToDisk.listener` 仅在键存在时触发。

**子集方法的行为**：

```java
writeFieldsToValue(ops, "a", "b")
  → map 只含 a、b（若它们通过 skip 判定）
  → 全被跳过时返回 ops.createNull()

readFieldsFromValue(data, ops, "a", "b")
  → 只读 a、b；缺失的跳过
```

### 2.7 远程调用相关

```java
Object2ReferenceOpenHashMap<String, RemoteMethod> getRemoteMethods();
@Nullable RemoteMethod getRemoteMethod(String method);
int indexIn(Object root);
static byte[] writeRemoteCall(Object root, Object target, String method, Object... args);
```

| 方法 | 语义 |
| --- | --- |
| `getRemoteMethods()` | 本 holder 的 `@RemoteCall` 方法表（懒加载，类级缓存）。**共享实例，只读** |
| `getRemoteMethod(name)` | 按线上名查一个 |
| `indexIn(root)` | 本 holder 在 root 扁平树中的索引（`(root, index)` 成对缓存） |
| `writeRemoteCall(...)` | 编码并**校验**（target 在树内、方法存在、参数匹配） |

`writeRemoteCall` 的异常：`IllegalArgumentException`，附带可用节点/方法信息。

### 2.8 内部状态与重入

```java
private volatile boolean updating;   // updateFieldDirtyFlags 期间
private volatile boolean writing;    // writeToNetworkBuffer 期间
@Getter private boolean changed;     // 累积脏状态
```

重入时的行为：

| 入口 | 重入时返回 |
| --- | --- |
| `updateFieldDirtyFlags` | `false` |
| `writeToNetworkBuffer` | 空 `byte[]` |

**含义**：在 `listener` / `skipWhen` / `detectChange` 里再次触发同步会被静默丢弃。

---

## 3. `DataSyncNetwork`

**源码**：`network/DataSyncNetwork.java` ｜ `@UtilityClass`，全 static。

### 3.1 通道与常量

```java
public final SimpleChannel CHANNEL;    // "datasynclib:sync"
public void init();                    // 幂等；由 DataSyncLib 主类调用
```

协议版本 `"2"`（私有常量）。版本不匹配 → Forge 拒绝连接。

**注册的消息**：

| 索引 | 消息 | 载荷 | 注册者 |
| --- | --- | --- | --- |
| 0 | `BlockEntitySyncPacket` | `BlockPos` + `byte[]` | `DataSyncNetwork.init()` |
| 1 | `EntitySyncPacket` | `int entityId` + `byte[]` | `DataSyncNetwork.init()` |
| 2 | `RemoteBlockEntityPacket` | `BlockPos` + `byte[]` | `RemoteNetwork.init()` |
| 3 | `RemoteEntityPacket` | `int entityId` + `byte[]` | `RemoteNetwork.init()` |

**方向判定**：`NetworkEvent.Context.getDirection().getOriginationSide()`。
每个消息**双向共用**一个 handler。

### 3.2 同步方法（4 组 × 3 重载）

```java
// BlockEntity → 客户端（服务端调用）
void syncBlockEntityToClient(BlockEntity be);                              // (be, false, true)
void syncBlockEntityToClient(BlockEntity be, boolean all);                 // (be, all, true)
void syncBlockEntityToClient(BlockEntity be, boolean all, boolean autoDetectOnly);

// BlockEntity ← 客户端（客户端调用）
void syncBlockEntityToServer(BlockEntity be);
void syncBlockEntityToServer(BlockEntity be, boolean all);
void syncBlockEntityToServer(BlockEntity be, boolean all, boolean autoDetectOnly);

// Entity
void syncEntityToClient(Entity entity);                                    // (e, false, true)
void syncEntityToClient(Entity entity, boolean all);
void syncEntityToClient(Entity entity, boolean all, boolean autoDetectOnly);
void syncEntityToServer(Entity entity);
void syncEntityToServer(Entity entity, boolean all);
void syncEntityToServer(Entity entity, boolean all, boolean autoDetectOnly);
```

#### 调用约束

| 方法族 | 必须在 | 内部检查 | 发包调度 |
| --- | --- | --- | --- |
| `syncXxxToClient` | 服务端 | `be.getLevel() instanceof ServerLevel` | `level.getServer().execute(...)` |
| `syncXxxToServer` | 客户端 | `level.isClientSide()` | `Minecraft.getInstance().execute(...)` |

> 侧不对 → **静默返回**（不抛异常）。这是"什么都没发生"的常见原因。

#### 完整执行链

```
syncBlockEntityToClient(be, all, autoDetectOnly):
  1. be instanceof IFieldDataHolder ?
  2. be.getLevel() instanceof ServerLevel ?
  3. manager.hasSyncFields(SERVER) ?
  4. manager.updateFieldDirtyFlags(SERVER, autoDetectOnly) ?   ← false 直接返回
  5. data = manager.writeToNetworkBuffer(SERVER, all)
  6. server.execute(() -> CHANNEL.send(TRACKING_CHUNK, packet))
```

第 6 步的 distributor：

| 方法 | distributor |
| --- | --- |
| `syncBlockEntityToClient` | `TRACKING_CHUNK` |
| `syncEntityToClient` | `TRACKING_ENTITY` |
| `syncXxxToServer` | `CHANNEL.sendToServer` |

#### 异步安全性

「异步安全」= **快照在调用线程立刻生成**，发包在主线程。
因此调用后再改字段**不会**反映在本次包里。

### 3.3 远程调用发送

```java
// BlockEntity
void callBlockEntityOnServer(BlockEntity be, String method, Object... args);
void sendBlockEntityToServer(BlockEntity be, byte[] payload);
void callBlockEntityOnClients(BlockEntity be, String method, Object... args);
void sendBlockEntityToClients(BlockEntity be, byte[] payload);

// Entity
void callEntityOnServer(Entity entity, String method, Object... args);
void sendEntityToServer(Entity entity, byte[] payload);
void callEntityOnClients(Entity entity, String method, Object... args);
void sendEntityToClients(Entity entity, byte[] payload);
```

| 前缀 | 行为 |
| --- | --- |
| `call*` | 编码（`RemoteInvoker.write`）+ 发送 |
| `send*` | 只发送（payload 自备） |

**侧检查**：`*OnServer` 要求客户端，`*OnClients` 要求 `ServerLevel`；不符则静默返回。

---

## 4. 远程调用

### 4.1 `RemoteInvoker`

**源码**：`remote/RemoteInvoker.java`

```java
static Object2ReferenceOpenHashMap<String, RemoteMethod> methods(Class<?> clazz);
static byte[] write(Object target, String method, Object... args);       // 从 target 找 root
static byte[] write(RemoteMethod method, int targetIndex, Object... args);
static void handle(Object target, byte[] payload);                       // 无端校验
static void handle(Object target, byte[] payload, @Nullable LogicalSide side);
```

| 方法 | 说明 |
| --- | --- |
| `methods(Class)` | 扫描并缓存类的 `@RemoteCall` 方法表（含父类、接口，重写覆盖） |
| `write(...)` | 编码：目标节点索引 + 方法索引 + 参数 |
| `handle(...)` | 解码并调用；异常记录日志不传播 |

**扫描规则**（`methods(Class)`）：

1. 沿类层级收集（父类 + 接口）；
2. 接口 `default` 方法也算；
3. 重写**替换**继承条目（类方法 > 接口 default）；
4. 有返回值的方法 → 抛异常（扫描期拒绝）；
5. 方法名排序后建索引表。

### 4.2 `RemoteNetwork`

**源码**：`remote/RemoteNetwork.java`

```java
void init();                                        // 注册索引 2/3（已由主类调用）
void handleReceived(Object target, byte[] payload); // 带日志的分发入口
```

接收流程：

```
handleBlockEntity(packet, ctx):
  ctx.getDirection().getOriginationSide().isClient() ?
    → enqueueWork: sender.level() 找 BE，side = SERVER
    → enqueueWork: player.level() 找 BE，side = CLIENT
  两条路都走 applyBlockEntityCall(level, packet, side)
    → IFieldDataHolder.handleRemoteCall(be, payload, side)
```

### 4.3 包与路由

| 类 | 职责 |
| --- | --- |
| `RemoteBlockEntityPacket` | record `(BlockPos pos, byte[] data)` + encode/decode |
| `RemoteEntityPacket` | record `(int entityId, byte[] data)` + encode/decode |
| `RemoteRouting` | 扁平 holder 树：`indexOf(root, holder)`、`holderNodeNames(clazz)` |
| `RemoteMethod` | 已解析方法元数据（参数 codec、端限制、索引） |

**寻址开销**：目标索引 + 方法索引，各 1~2 字节 VarInt。

### 4.4 端校验

| 场景 | 端来源 | 不匹配时 |
| --- | --- | --- |
| 目标在 level 中 | 目标所在 level 的侧 | 拒绝，方法体不执行 |
| 目标不在 level（测试） | `null` | 跳过检查 |
| 显式传 `side` | 调用方指定 | 按指定值检查 |

---

## 5. 监听器 Holder 体系

**源码**：`listener/`、`util/holder/`

### 5.1 两个家族

#### 纯值 Holder（`util/holder/`）

| 类 | 值类型 |
| --- | --- |
| `BooleanHolder` / `ByteHolder` / `ShortHolder` / `CharHolder` | 原始类型 |
| `IntHolder` / `LongHolder` / `FloatHolder` / `DoubleHolder` | 原始类型 |
| `ObjHolder<T>` | 对象（可 null） |
| `IntObjectHolder` | int 键 → 对象 |

```java
public class ObjHolder<T> {
    public T value;
    public final T get();
    public final void set(T value);
}
```

#### 可序列化 Holder（`listener/`）

| 类 | 父类 | 实现接口 |
| --- | --- | --- |
| `ObjSerializableHolder<T>` | `ObjHolder<T>` | `IDataSerializable` |
| `ObjNotifiableHolder<T>` | `ObjSerializableHolder<T>` | + `ISyncNotifiable` |
| `{Boolean,Byte,Short,Int,Long,Float,Double}NotifiableHolder` | 对应值 Holder | `IDataSerializable` + `ISyncNotifiable` |

### 5.2 `ObjSerializableHolder<T>` 的机制

```java
protected T lastValue;
protected int lastHash;
private boolean changed;
protected final DataSyncCodec<T> codec;

public static <T> ObjSerializableHolder<T> create(DataSyncCodec<T> codec);
public static <T> ObjSerializableHolder<T> create(DataSyncCodec<T> codec, T value);

// IDataSerializable
boolean detectChange();   // hashCode 快速失败 → Objects.equals(lastValue, value)
void markAsChanged(); clearChanged(); boolean isChanged();
void writeBuffer(LogicalSide, FriendlyByteBuf);   // false 布尔 | true + codec
void readBuffer(LogicalSide, FriendlyByteBuf);
Object writeValue(ValueOps);                      // ops.createNull() | codec.encode
void readValue(Object, ValueOps);
```

**null 处理**：网络用 presence 布尔前缀；磁盘用 carrier null。

### 5.3 `ISyncNotifiable`

```java
public interface ISyncNotifiable<T, LISTENER> {
    T setReceiverListener(@NotNull LISTENER listener);   // 接收端，新值已赋值后
    T setSenderListener(@NotNull LISTENER listener);     // 发送端，值已写入 buffer 后
}
```

### 5.4 触发顺序（`ObjNotifiableHolder`）

```java
writeBuffer(side, buf):
    写 presence 布尔
    写 codec payload
    senderListener.onSync(side, lastValue, value)     // ← 发送端回调
    lastValue = value

readBuffer(side, buf):
    oldValue = value
    读 presence → 读 codec
    receiverListener.onSync(side, oldValue, value)    // ← 接收端回调
```

### 5.5 用法

```java
ObjNotifiableHolder<String> holder =
        ObjNotifiableHolder.create(DataSyncCodec.STRING_CODEC)
                .setReceiverListener((side, oldVal, newVal) -> updateUI(newVal))
                .setSenderListener((side, oldVal, newVal) -> logChange(newVal));
```

`listener` 函数类型：`ObjSyncListener<T>`、`IntSyncListener` … 各有 `EMPTY` 常量。

### 5.6 与其他通知机制的对比

| 机制 | 位置 | 发送端回调 | 接收端回调 | 磁盘回调 |
| --- | --- | --- | --- | --- |
| `@SyncTo*.listener` | 注解答 | ❌ | ✅ | ❌ |
| `@SaveToDisk.listener` | 注解答 | — | — | ✅ |
| `holder.scheduleUpdate` | 接口 | ❌ | ✅（聚合） | ❌ |
| `*NotifiableHolder` | holder 自身 | ✅ | ✅ | ❌ |

---

## 6. `IDataSerializable`

**源码**：`IDataSerializable.java`、`AbstractDataSerializable.java`

**自管理序列化**：对象自己实现全部编解码，不走字段扫描。

```java
public interface IDataSerializable {
    // 变更追踪
    boolean detectChange();
    void markAsChanged();
    void clearChanged();
    boolean isChanged();

    // 网络
    void writeBuffer(LogicalSide side, @NotNull FriendlyByteBuf buf);
    void readBuffer(LogicalSide side, @NotNull FriendlyByteBuf buf);

    // 磁盘（carrier 值）
    @NotNull Object writeValue(@NotNull ValueOps ops);
    void readValue(@NotNull Object data, @NotNull ValueOps ops);
}
```

### 与 `IFieldDataHolder` 的分工

| | `IFieldDataHolder` | `IDataSerializable` |
| --- | --- | --- |
| 字段管理 | 框架扫描注解 | **自己实现** |
| 变更检测 | 框架比对快照 | **自己的 `detectChange()`** |
| 编解码 | 框架调各字段 codec | **自己的 4 个方法** |
| 粒度 | 字段级 | 对象级（原子） |
| 适用 | 常规字段集合 | 特殊结构、需要完全控制字节布局 |

### 接入方式

字段类型实现 `IDataSerializable` → 框架自动用 `SerializableAccess`（数组用 `SerializableArrayAccess`）。

```java
public class MyState implements IDataSerializable {
    private int a;
    private final List<String> b = new ArrayList<>();
    private boolean changed;

    @Override public boolean detectChange() { return changed; }
    @Override public void markAsChanged() { changed = true; }
    @Override public void clearChanged() { changed = false; }
    @Override public boolean isChanged() { return changed; }

    @Override public void writeBuffer(LogicalSide side, FriendlyByteBuf buf) {
        buf.writeVarInt(a);
        buf.writeCollection(b, FriendlyByteBuf::writeUtf);
    }
    @Override public void readBuffer(LogicalSide side, FriendlyByteBuf buf) {
        a = buf.readVarInt();
        b.clear();
        buf.readCollection(b::add, FriendlyByteBuf::readUtf);
    }

    @Override public Object writeValue(ValueOps ops) {
        var list = new ArrayList<Object>(2);
        ops.addInt(list, a);
        list.add(ops.createList(new ArrayList<>(b)));
        return ops.createList(list);
    }
    @Override public void readValue(Object data, ValueOps ops) {
        var list = ops.getList(data);
        a = ops.getInt(list, 0);
        b.clear();
        for (var e : ops.getList(list.get(1))) b.add(ops.getString(e));
    }
}
```

### `AbstractDataSerializable`

```java
public abstract class AbstractDataSerializable implements IDataSerializable {
    protected boolean changed;

    public void markAsChanged() { changed = true; }
    public void clearChanged()  { changed = false; }
    public boolean isChanged()  { return changed; }
    public boolean detectChange() { return changed; }   // 只看手动标记
}
```

只提供变更追踪的默认实现；4 个编解码方法仍需自己写。

### 常见实现

| 类 | 说明 |
| --- | --- |
| `ObjSerializableHolder<T>` | 用 codec 做编解码（见 §5） |
| `*NotifiableHolder` | 同上 + 收发回调 |

### GTOCore 用法

`MEPatternBufferPartMachine.InternalSlot` 是典型：

```java
// :892 / :897
@Override public @NotNull Object writeValue(@NotNull ValueOps ops) {
    return fieldDataManager.get().writeToValue(ops);      // 内部字段交给 manager
}
@Override public void readValue(@NotNull Object data, @NotNull ValueOps ops) {
    if (ops.isNull(data)) return;
    if (ops.dataVersion() < 2) {                          // 版本迁移
        var nbt = LegacyIO.legacyCompound(ops, data);
        if (nbt != null) { deserializeNBT(nbt); return; }
    }
    fieldDataManager.get().readFromValue(data, ops);
}
```

即：`IDataSerializable` 作为**外层壳**，内部字段委托给 `FieldDataManager`，
外壳负责版本迁移与旧格式兼容。

---

## 7. 工具类速查

### 7.1 序列化相关

| 类 | API |
| --- | --- |
| `JavaValueOps` | `INSTANCE`、`create(int version)`、`toBytes(Object)`、`fromBytes(byte[])`、`dataVersion()` |
| `ValueOps` | 见 [05 §4](05-codecs.md#4-valueops-carrier-模型) |
| `VarInts` | `write(ByteBuf, int)`、`read(ByteBuf)`、`writeVarLong`、`readVarLong` |
| `VersionedFriendlyByteBuf` | `version()` |
| `ByteBufCodecExtends` | 预置 `StreamCodec`（`RESOURCE_LOCATION_CODEC`、`VEC3_CODEC`、`COMPOUND_TAG_CODEC`、`ITEM_STACK_CODEC` 等） |
| `ComponentStreamCodecs` | `Component` 的二叉树线格式 + `register(Class, codec)` |
| `DiskByteBufCodecs` | 磁盘半（`COMPONENT_CODEC` 走 JSON 字符串） |
| `ByteBufCodecs` | 通用 buffer codec（`fromValueCodec` 等） |

### 7.2 反射与类型

| 类 | API |
| --- | --- |
| `ReflectUtil` | `getFieldGenericTypeClasses`、`getResolvedGenericArguments`、`getResolvedSuperType`、`resolveSuperTypeBindings`、`getAccessibleMethod`、`createFieldNotFoundException` |
| `EnumUtil` | `isFixed`、`addFixedEnum`、`getSerializedName`、`getSerializedEnum`、`getEnum` |

### 7.3 哈希与集合

| 类 | API |
| --- | --- |
| `HashUtil` | `ARRAY_IDENTITY_STRATEGY`、`arrayStrategy(strategy)`、`arrayIdentityHashCode`、`arrayIdentityEquals`、`arrayIdentityWrapper` |
| `ItemStackHashStrategy` | `ItemStack` 标量策略 |
| `FluidStackHashStrategy` | `FluidStack` 标量策略 |
| `ItemStackArrayHashStrategy` / `FluidStackArrayHashStrategy` | 数组策略 |

### 7.4 NBT / JSON

| 类 | API |
| --- | --- |
| `NbtUtil` | `COMPOUND_TAG_MAP`（`Function<CompoundTag, Map<String,Tag>>`）、`TAG_TYPE` / `COMPOUND_TAG_TYPE` / `LIST_TAG_TYPE`（CustomTypes）、`read` / `write` |
| `JsonUtils` | `JSON_TYPE` / `JSON_OBJECT_TYPE` / `JSON_ARRAY_TYPE`（CustomTypes）、`write` / `read` 及分项方法 |

### 7.5 文件

| 类 | API |
| --- | --- |
| `FileUtil` | `MAX_VERSION`、`writeVersioned`、`readVersioned`、`read`、`write` |

详见 [06 §3](06-data-models.md#3-版本化文件)。

### 7.6 缓存

| 类 | 特点 |
| --- | --- |
| `MapCache<K,V>` | 接口 |
| `HashMapCache` | `HashMap` 实现 |
| `ConcurrentHashMapCache` | 并发安全 |
| `IdentityHashMapCache` | 身份比较（`==`） |

用于 `FieldDefinitionStorage` 的 `ClassValue` 内部与工厂缓存。

### 7.7 注册表与定义层

| 类 | API |
| --- | --- |
| `FieldDefinitionStorage` | `get(Class)`、`getFieldDefinition(Field)`、4 组 `register*Factory`、`registerStrategy` |
| `Registry<K,V>` | 见 [06 §1](06-data-models.md#1-registry) |
| `DataComponentRegistry` | 见 [06 §2](06-data-models.md#2-datacomponent-三件套) |
| `CustomTypes` | `register`、`type(int)`、`Type.builder` |
| `ValueConverters` | `Converter.builder`、`Converter.build` |

---

## 附：类型关系总图

```
IFieldDataHolder ──── 有 ────► FieldDataManager ──── 持有 ────► DataField[]
      ▲                              │                              │
      │                              │                              ├─ AbstractField（值模式）
      │                              │                              │    └─ IntField / ObjField / ObjCodecField / CustomObjCodecField
      │                              │                              └─ AbstractFieldAccess（访问模式）
      │                              │                                   └─ CollectionAccess / MapAccess / *ArrayAccess / FieldDataHolderAccess / SerializableAccess
      │                              │
      ├─ FieldDataHolderBlockEntity  └─ 使用 ► FieldDefinitionStorage ── 缓存 ──► DataFieldDefinition[]
      ├─ FieldDataHolderEntity                                                      │
      ├─ FieldDataCodec                          DataSyncCodec ◄───────────────────┘
      ├─ ChildFieldDataHolder                     ├─ streamWriter/Reader
      └─ 你的类                                   └─ valueWriter/Reader

IDataSerializable ── 独立于字段扫描，由 SerializableAccess 适配
```
