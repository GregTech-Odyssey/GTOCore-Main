# 07 · 架构与数据流

> [← 返回 00 概要](00-overview.md)

> 讲清楚「注解如何变成字段对象」「一次同步到底经过哪些步骤」「存盘格式长什么样」「线程在哪一侧」。
> 读完后你能自己回答：为什么我的字段没发出去 / 为什么读回来是旧值。

---

## 1. 分层架构

```
┌──────────────────────────────────────────────────────────────┐
│ 应用层：你的类 + 注解                                          │
│   @SyncToClient @SaveToDisk @Access ...                       │
└───────────────────────────┬──────────────────────────────────┘
                            │ 类级反射扫描（每类一次，全局缓存）
┌───────────────────────────▼──────────────────────────────────┐
│ FieldDefinitionStorage                                        │
│   allDefinitions[]        所有被管理字段的定义                 │
│   syncToClientDefinitions[] 按方向分组                         │
│   syncToServerDefinitions[]                                   │
│   saveDefinitions[]                                           │
│   definitionMap           name → definition                   │
│   typeDefinitions         type → [definition]                 │
└───────────────────────────┬──────────────────────────────────┘
                            │ 每实例工厂创建
┌───────────────────────────▼──────────────────────────────────┐
│ FieldDataManager（每 holder 实例一个）                         │
│   allFields / syncToClientFields / syncToServerFields /        │
│   saveFields                                                  │
│   changed                                                     │
│   updating / writing        重入保护                            │
└───────────────────────────┬──────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────┐
│ DataField 体系                                                 │
│   值模式：IntField / LongField / ... / ObjField / ObjCodecField│
│   访问模式：IntArrayAccess / CollectionAccess / MapAccess /    │
│            FieldDataHolderAccess / ChildManagerAccess /        │
│            TagSerializableAccess ...                          │
└───────────────────────────┬──────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────┐
│ DataSyncCodec（统一 codec 注册表）                              │
│   网络半：StreamCodec<FriendlyByteBuf, T>                      │
│   磁盘半：ValueCodec<T>  ← 写 carrier 值                       │
└───────────────────────────────────────────────────────────────┘
```

### 为什么分两层缓存

| 层 | 何时创建 | 作用域 | 成本 |
| --- | --- | --- | --- |
| `FieldDefinitionStorage` | 类第一次被扫描 | **每类一次**（全局） | 反射 + 注解解析（贵） |
| `FieldDataManager` | holder 第一次用 | **每实例一次** | 按定义创建 `DataField`（便宜） |

所以「新建一台机器」不会重复做反射扫描——这是 `LazyFieldDataManager` 存在的意义。

---

## 2. 字段如何被分类

扫描一个字段时，决定它走哪条路（`DataSyncLib` 主类里的工厂注册 + `FieldDefinitionStorage` 的匹配逻辑）：

```
1. 是 primitive / primitive 数组？           → IntField / IntArrayAccess ...
2. 是 Collection / Map / 数组？              → *Access（带 Hash.Strategy）
3. 类型实现了 IFieldDataHolder？             → FieldDataHolderAccess
4. 类型实现了 IDataSerializable？            → SerializableAccess
5. 类型实现了 Forge INBTSerializable？        → TagSerializableAccess
6. DataSyncCodec.get(type) 有 codec？        → ObjCodecField
7. 以上都不行                                 → 扫描期报错「No codec registered for type」
```

**优先级**：访问工厂按优先级排序匹配，更具体的类型先命中
（例如 `IntCollection` 优先级 1000 > `Collection` 100）。

### 值模式 vs 访问模式

| | 值模式 | 访问模式 |
| --- | --- | --- |
| 触发 | 非 final 普通字段 | `final` 字段 或 `@Access` |
| 读 | 取整个值 | 取容器引用 |
| 变更检测 | 值快照比对 / 内容比对 | 容器自身 `detectChange()` + 引用比对 |
| 写回 | 替换字段值 | 就地修改容器内容 |
| `instanceAsValue` | — | `true` 时把实例本身当可空值编码 |

**判断口诀**：**容器引用不变、内容变 → 访问模式**。`List<X> list = new ArrayList<>()` 后一直 `add`，
这就是访问模式的场景。

---

## 3. 同步流程（服务端 → 客户端）

### 3.1 时序

```
[服务端线程]
  你的 tick()
    ├─ 修改字段（energy += 10）
    ├─ setChanged()                                    ← 存档用，与同步无关
    └─ DataSyncNetwork.syncBlockEntityToClient(this)
          │
          ├─ ① hasSyncFields(SERVER) ?                 → 没有 @SyncToClient 字段：直接返回
          │
          ├─ ② updateFieldDirtyFlags(SERVER, autoDetectOnly)
          │     └─ 遍历 syncToClientFields
          │           对每个字段：detectChange()
          │             ├─ autoDetect=false 且未被 markFieldsForSync → 跳过
          │             └─ 与上次快照不同 → 标记 changed
          │     └─ 返回「本轮是否有任何变化」
          │
          ├─ ③ 无变化 → 返回（不发包）
          │
          ├─ ④ writeToNetworkBuffer(SERVER, writeAll=false)
          │     ├─ holder.writeCustomSyncData(buf, false)
          │     └─ 遍历字段，只写 changed 的：
          │           writeVarInt(索引) + field.writeToBuffer(...)
          │           写完立刻 clearChanged()
          │
          └─ ⑤ server.execute(() -> CHANNEL.send(TRACKING_CHUNK, packet))
                 ↑ 快照已在当前线程生成；发包调度到服务端主线程

[客户端线程]
  handleBlockEntity(packet, ctx)
    ├─ 判断 getOriginationSide().isClient() → 是 C2S，走服务端分支
    └─ 否则 enqueueWork(()) -> {
          level = player.level()
          be = level.getBlockEntity(pos)
          be.getFieldDataManager().readFromNetworkBuffer(CLIENT, data)
            ├─ holder.readCustomSyncData(buf)
            ├─ while (buf.readableBytes() > 0) {
            │     index = readVarInt()
            │     fields[index].readFromBuffer(CLIENT, source, buf)
            │     if (definition.scheduleUpdate(CLIENT)) update = true
            │ }
            └─ if (update) holder.scheduleUpdate(CLIENT)
       }
```

### 3.2 关键设计点

| 设计 | 原因 |
| --- | --- |
| **推送式**（无自动 tick 钩子） | 框架不知道你什么时候改值；由你在合适时机调用，避免无意义开销 |
| **脏标记 + 索引寻址** | 只传变更字段；每字段 1 字节索引（VarInt） |
| **无变化直接返回** | 在 `updateFieldDirtyFlags` 返回 false 处短路，不发包 |
| **快照在调用线程生成** | 允许跨线程调用；但意味着调用后再改值不会反映在本次包里 |
| **发包调度到主线程** | `CHANNEL.send` 不是线程安全的 |
| **C2S 应用后自动 `setChanged()`** | 服务端收到客户端值视为「状态变了」，自动标脏区块 |

### 3.3 区块加载时的同步（`field_sync`）

新玩家进入区块时**不走上面的包**，而是走原版 `getUpdateTag()`：

```java
@Override
public @NotNull CompoundTag getUpdateTag() {
    var tag = super.getUpdateTag();
    if (getFieldDataManager().hasSyncFields(LogicalSide.SERVER)) {
        // writeAll = true → 全量，不依赖任何脏标记
        tag.putByteArray("field_sync",
                getFieldDataManager().writeToNetworkBuffer(LogicalSide.SERVER, true));
    }
    return tag;
}
```

客户端加载时：

```java
public void load(@NotNull CompoundTag tag) {
    super.load(tag);
    if (tag.get("field_sync") instanceof ByteArrayTag arr) {
        getFieldDataManager().readFromNetworkBuffer(LogicalSide.CLIENT, arr.getAsByteArray());
    } else if (tag.get("field_save") instanceof ByteArrayTag arr) {
        getFieldDataManager().readFromValue(
                JavaValueOps.INSTANCE.fromBytes(arr.getAsByteArray()),
                JavaValueOps.create(tag.getInt("field_data_dataVersion")));
    }
}
```

> ⚠️ **两条路互斥**：`field_sync` 存在时 `field_save` 完全不读（连版本号都不看）。
> 这就是「服务端刚重启、玩家一进来显示旧值」类问题的根源之一——如果你手工往 NBT 里塞了
> `field_sync` 或未及时更新它。

---

## 4. 持久化流程

### 4.1 存盘

```
BlockEntity.saveAdditional(tag)
  ├─ super.saveAdditional(tag)
  ├─ tag.putInt("field_data_dataVersion", VERSION)
  └─ var ops = JavaValueOps.create(VERSION);
     tag.putByteArray("field_save",
         ops.toBytes(manager.writeToValue(ops)));

manager.writeToValue(ops)
  ├─ holder.writeCustomSaveData(data, ops)
  └─ 遍历 saveFields（按存储键排序）:
        result = field.writeToValue(source, ops)
        ├─ result == NOT_PERSISTED          → 跳过（被 skipWhen 拦下）
        ├─ result 是 null 且 !saveEmpty      → 跳过
        └─ 否则 data.put(key, result)
  └─ data 为空 → ops.createNull()
     否则 → ops.createStringMap(data)
```

### 4.2 读盘

```
BlockEntity.load(tag)（无 field_sync 时）
  └─ manager.readFromValue(ops.fromBytes(field_save), ops.create(dataVersion))

manager.readFromValue(data, ops)
  ├─ ops.isNull(data) → 直接返回（"什么都没存"，不是错误）
  ├─ map = ops.getStringMap(data)
  ├─ holder.readCustomSaveData(map, ops)
  └─ 遍历 saveFields:
        if (!map.containsKey(key)) continue;      ← 旧存档缺字段：安全跳过
        field.readFromValue(source, map.get(key), ops)
        └─ 若定义带 listener 且字段存在于 map → 调用 listener(value)
```

### 4.3 carrier 值是什么

磁盘上不是 NBT 结构，而是一段**自定义二进制**（19 种类型 id，见 [05](05-codecs.md)）：

```
field_save (byte[])
  └─ JavaValueOps.toBytes(Object)
       └─ [type id][payload] ...
```

外层是一个 `STRING_MAP`（id 14），键是字段存储键，值是各字段的 carrier 值。
比 NBT 紧凑：整数用 VarInt，没有 NBT 的 tag 头开销。

**为什么不用 NBT 直接存**：`ValueOps` 的 id 表与历史 `Data` 系统字节兼容，
老存档仍可读；同时避免了 `CompoundTag` 的装箱与嵌套开销。

### 4.4 版本与迁移

```
VERSION (static int)
   │ 存盘时写入 "field_data_dataVersion"
   │ 读盘时通过 JavaValueOps.create(version) 传给 codec
   ▼
codec 内 ops.dataVersion() 可读到它 → 用于分支处理不同版本的数据
```

GTOCore 实例（`MEPatternBufferPartMachine.InternalSlot` 的自定义 codec 里）：

```java
if (ops.dataVersion() < 2) {
    var nbt = LegacyIO.legacyCompound(ops, data);
    if (nbt != null) {
        deserializeNBT(nbt);
        return;
    }
}
fieldDataManager.get().readFromValue(data, ops);
```

**注意**：`FieldDataHolderBlockEntity.VERSION` 是全局静态。想按类型分版本，必须覆盖
`saveAdditional`/`load`（或 Entity 的对应方法）自己写版本号。

---

## 5. 线程模型

| 操作 | 允许的线程 | 说明 |
| --- | --- | --- |
| 读写字段本身 | 你的业务线程（通常主线程） | 框架不加锁 |
| `updateFieldDirtyFlags` | 调用 `syncXxx` 的线程 | 有 `updating` 重入保护 |
| `writeToNetworkBuffer` | 同上 | 有 `writing` 重入保护 |
| `readFromNetworkBuffer` | 网络处理线程（`enqueueWork` 后为主线程） | — |
| `CHANNEL.send` | **必须主线程** | 框架内部用 `server.execute` / `Minecraft.execute` 调度 |
| `writeToValue` / `readFromValue` | 存档线程（通常是服务端主线程或 IO 线程） | 线程安全依赖你的 codec |

### 重入保护

```java
private volatile boolean updating;   // updateFieldDirtyFlags 期间为 true
private volatile boolean writing;    // writeToNetworkBuffer 期间为 true
```

若在 `skipWhen` / `listener` / `detectChange` 里再次触发同一操作，会**直接返回**
（`updateFieldDirtyFlags` 返回 `false`，`writeToNetworkBuffer` 返回空数组）。
这样避免了「listener 里改字段 → 又触发同步 → 死循环」。

> 副作用：如果你在 listener 里改字段，本次同步**不会**包含这次改动。下次同步才会。这是刻意的。

---

## 6. 数据流全景图（一张图记住）

```
                        ┌─────────────┐
      注解字段  ────────►│  存储定义    │（每类一次）
                        └──────┬──────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
        sync 字段集        save 字段集      all 字段集
              │                │                │
    ┌─────────┴────────┐       │        ┌───────┴────────┐
    ▼                  ▼       │        ▼                ▼
  S2C 字段           C2S 字段  │    writeAllToValue   FieldDataCodec
    │                  │       │    readAllFromValue  （POJO 整体）
    ▼                  ▼       ▼
writeToNetworkBuffer  ...   writeToValue
    │                          │
    ▼                          ▼
FriendlyByteBuf          JavaValueOps.toBytes
    │                          │
    ▼                          ▼
datasynclib:sync           "field_save" NBT
(网络包)                    （存档）
    │
    ▼
readFromNetworkBuffer  ──► scheduleUpdate / listener
```

---

## 7. GTOCore 的桥接层（GTCEu `ISync`）

反编译 GTCEu jar（`build/generated/jarJar/META-INF/jarjar/gtceu-...jar`）可见：

```java
public interface ISync extends com.gto.datasynclib.IFieldDataHolder {
    GTBlockEntity getHolder();

    default void syncToClient() {
        DataSyncNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_CHUNK.with(() -> ...),
                new BlockEntitySyncPacket(
                        getHolder().getBlockPos(),
                        getHolder().getFieldDataManager()
                                  .writeToNetworkBuffer(LogicalSide.SERVER, false)));
    }
    default void syncAllToClient() {
        // 同上，writeToNetworkBuffer(LogicalSide.SERVER, true)
    }
    default void syncToServer();
    default void syncAllToServer();
}
```

```java
public abstract class GTBlockEntity extends BlockEntity implements ISync, ITickSubscription {
    private TickableSubscription autoSyncSubscription;
    public boolean sync;                                  // 脏标记
    public boolean needSync();
    protected void autoSync();
    public void loadCustomPersistedData(CompoundTag);
    public void saveCustomPersistedData(CompoundTag, boolean);
    public void scheduleUpdate(LogicalSide);
}
```

### 与裸 DataSyncLib 的差异

| 行为 | 裸 BlockEntity 基类 | GTCEu `GTBlockEntity` / `MetaMachine` |
| --- | --- | --- |
| 字段扫描 | `getFieldDataManager()` 懒加载 | 同样，`MetaMachineBlockEntity` / `MetaMachine` 提供 |
| 存盘钩子 | `saveAdditional` / `load` | `saveCustomPersistedData` / `loadCustomPersistedData` |
| 自动同步 | **无**（必须手动调） | **有**：`autoSync()` 由 tick 驱动 |
| 触发同步 | `DataSyncNetwork.syncBlockEntityToClient(this)` | `requestSync()`（只置标记）/ `syncToClient()`（立即发包） |

### 两条发送路径（反编译确认）

```java
// MetaMachine
public void requestSync() { this.holder.sync = true; }        // 只置标记

// GTBlockEntity
public boolean needSync() {
    if (sync) { sync = false; return true; }                  // one-shot 取走
    return false;
}

protected void autoSync() {
    if (remove) return;
    if (needSync() || (++periodID % 40 == 0)) {
        DataSyncNetwork.syncBlockEntityToClient(this);        // 完整流程：自动检测 + 增量
    }
}

// ISync
default void syncToClient() {
    // 直接发包，跳过 updateFieldDirtyFlags —— 只写「此刻已脏」的字段
    DataSyncNetwork.CHANNEL.send(TRACKING_CHUNK, new BlockEntitySyncPacket(pos,
            getFieldDataManager().writeToNetworkBuffer(LogicalSide.SERVER, false)));
}
```

| 路径 | 脏检测 | 时机 | 适用 |
| --- | --- | --- | --- |
| `requestSync()` → `autoSync()` | ✅ 自动检测（含 `autoDetect` 过滤） | 下一 tick | 常规用法 |
| `syncToClient()` | ❌ 只看已有脏标记 | 立即 | 已 `markFieldsForSync` 后立即发 |
| `syncAllToClient()` | ❌（`writeAll = true` 不看脏） | 立即 | 全量重推 |

**周期性兜底**：`autoSync()` 里有 `periodID % 40 == 0`——即使没人调 `requestSync()`，
每 2 秒也会尝试一次自动检测 + 增量同步（`autoDetect = true` 的字段因此最多延迟 2 秒）。

> 因此 GTOCore 里**几乎看不到 `DataSyncNetwork.*` 调用**——都被 `GTBlockEntity` 封装了。
> 想在 GTOCore 里用 `syncAllToClient()` 做全量重推，直接调机器的该方法即可。

细节与案例见 [08-gtocore-usage.md](08-gtocore-usage.md)。
