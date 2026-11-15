# 08 · GTOCore 集成与用法例证

> [← 返回 00 概要](00-overview.md)

> **本章是各功能在 GTOCore 中的落地例证。** 全部例子取自本仓真实代码，带文件路径与行号。
> 功能本身的设计与参数请查 [02](02-annotations.md) / [03](03-core-api.md)；本章只回答「本仓怎么用」。
> 每个案例给：使用场景 → 代码 → 为什么要这样写 → 容易踩的坑。

---

## 目录

| # | 案例 | 涉及能力 |
| --- | --- | --- |
| [0](#0-先搞懂-gtocore-的接入方式) | **GTOCore 的接入方式（先读）** | `ISync` 桥接层 |
| [1](#1-最基础存盘字段fissionreactormachine) | 基础存盘字段 | `@SaveToDisk` + `defaultValue` |
| [2](#2-存盘--同步vacuumpumpmachine) | 存盘 + 同步 | `@SaveToDisk` + `@SyncToClient` |
| [3](#3-渲染可见的同步字段manaheatermachine) | 渲染可见的同步字段 | `@SyncToClient(scheduleUpdate)` |
| [4](#4-接收端回调mestoragehatch--nanophagocytosisplantmachine) | 接收端回调 | `@SyncToClient(listener)` |
| [5](#5-手动置脏大量容器pigmentmixer--advancedtesseractmachine) | 手动置脏（大量容器） | `autoDetect = false` + `markFieldsForSync` |
| [6](#6-非-final-容器的访问模式monitoreu) | 非 final 容器的访问模式 | `@Access` |
| [7](#7-条件存盘abstractrecipeinternalslot) | 条件存盘 | `@SaveToDisk(skipWhen)` |
| [8](#8-自定义-holder-对象mepatternbufferpartmachine) | 自定义 holder 对象 | `IFieldDataHolder` + `LazyFieldDataManager` |
| [9](#9-部分字段存取物品-nbtresonanceflowermachine) | 部分字段存取（物品 NBT） | `writeFieldsToValue` / `readFieldsFromValue` |
| [10](#10-直接读写单字段multiblockmestorageMachine) | 直接读写单字段 | `writeFieldToValue` / `readFieldFromValue` |
| [11](#11-registry--全局-codecresearchtag) | Registry + 全局 codec | `Registry` + `DataSyncCodec.register` |
| [12](#12-手写-valuecodecicrossrecipemachine.thread) | 手写 ValueCodec | `ValueCodec` + `ValueOps` |
| [13](#13-数据版本迁移mepattermbufferpartmachine) | 数据版本迁移 | `ops.dataVersion()` |
| [14](#14-纯二进制-carrier-存取particlecontainmentitem) | 纯二进制 carrier 存取 | `JavaValueOps` |
| [15](#15-机器级同步-requestsync-的正确用法) | `requestSync()` 正确用法 | GTCEu 桥接 |
| [16](#16-gametest-里模拟一次同步) | GameTest 里模拟同步 | `updateFieldDirtyFlags` + buffer |
| [17](#17-idisataserializable-作为外层壳) | `IDataSerializable` 作为外层壳 | 自管理序列化 + 版本迁移 |
| [18](#18-gtocore-未使用的功能清单) | **本仓未使用的功能** | 备查 |

---

## 0. 先搞懂 GTOCore 的接入方式

### 0.1 机器不是 `FieldDataHolderBlockEntity` 子类

```java
// 典型 GTOCore 机器
public final class VacuumPumpMachine extends SimpleTieredMachine { ... }
```

继承链最终到 GTCEu 的 `MetaMachineBlockEntity` → `GTBlockEntity` → `BlockEntity`。
`GTBlockEntity` 实现了 `ISync`，`ISync extends com.gto.datasynclib.IFieldDataHolder`。

反编译 GTCEu jar（`build/generated/jarJar/META-INF/jarjar/gtceu-1.20.1-forge-1.20.1-26.9.31.jar`）：

```java
public interface ISync extends com.gto.datasynclib.IFieldDataHolder {
    GTBlockEntity getHolder();
    default void syncToServer();
    default void syncToClient();       // writeToNetworkBuffer(SERVER, false) → TRACKING_CHUNK
    default void syncAllToServer();
    default void syncAllToClient();    // writeToNetworkBuffer(SERVER, true)  → TRACKING_CHUNK
}

public abstract class GTBlockEntity extends BlockEntity implements ISync, ITickSubscription {
    private TickableSubscription autoSyncSubscription;
    public boolean sync;                                       // 脏标记
    public boolean needSync();
    protected void autoSync();                                 // tick 驱动
    public void loadCustomPersistedData(CompoundTag);
    public void saveCustomPersistedData(CompoundTag, boolean);
    public void scheduleUpdate(LogicalSide);
}
```

### 0.2 结论对照表

| 你想做的事 | 裸 DataSyncLib | GTOCore 机器里 |
| --- | --- | --- |
| 标脏（等下一 tick 发） | `manager.markAsChanged()` | **`requestSync()`** |
| 立刻同步 | `DataSyncNetwork.syncBlockEntityToClient(this)` | `syncToClient()` |
| 立刻全量同步 | `syncBlockEntityToClient(this, true, true)` | `syncAllToClient()` |
| 客户端 → 服务端 | `syncBlockEntityToServer(this)` | `syncToServer()` / `syncAllToServer()` |
| 手动指定字段脏 | `markFieldsForSync("x")` | 同名（`IFieldDataHolder` 默认方法） |
| 存盘 | `setChanged()` | GTCEu 自己管理（`saveCustomPersistedData`） |

### 0.3 两个同步路径（反编译确认）

GTCEu 有**两条**发送路径，行为不同：

**路径 1：`ISync.syncToClient()` — 直接发包，跳过脏检测**

```java
default void syncToClient() {
    DataSyncNetwork.CHANNEL.send(
        PacketDistributor.TRACKING_CHUNK.with(() -> ...),
        new BlockEntitySyncPacket(getHolder().getBlockPos(),
            getHolder().getFieldDataManager().writeToNetworkBuffer(LogicalSide.SERVER, false)));
}
```

它直接 `writeToNetworkBuffer(SERVER, false)`——只写**当前已标脏**的字段，
**不调用** `updateFieldDirtyFlags`。所以 `autoDetect = true` 在此期间检测出的变化**不会被包含**。

**路径 2：`requestSync()` → `autoSync()` — 走完整流程**

反编译结果：

```java
// MetaMachine
public void requestSync() {
    this.holder.sync = true;          // 只置脏标记
}

// GTBlockEntity
public boolean needSync() {
    if (sync) { sync = false; return true; }   // 取走标记（one-shot）
    return false;
}

protected void autoSync() {
    if (remove) return;
    if (needSync() || (++periodID % 40 == 0)) {
        DataSyncNetwork.syncBlockEntityToClient(this);   // ← 三参数便捷版的等价：自动检测 + 增量
    }
}
```

`DataSyncNetwork.syncBlockEntityToClient(be)` 展开为 `(be, false, true)`，内部**会**调用
`updateFieldDirtyFlags(SERVER, true)`——即包含 `autoDetect = true` 字段的自动比对。

**这条路径还带 40 tick 的周期性兜底**（`periodID % 40 == 0`）：即使没人调 `requestSync()`，
每 2 秒也会尝试一次自动检测 + 增量同步。

### 0.4 因此的推论

| 场景 | 结果 |
| --- | --- |
| `autoDetect = true` 字段改值 + `requestSync()` | ✅ 下一 tick 自动检测到并发送 |
| `autoDetect = true` 字段改值，**什么都不调** | ✅ 最多 2 秒内被周期性兜底捕获 |
| `autoDetect = false` 字段改值 + 只调 `requestSync()` | ❌ 自动检测跳过它 → 不发该字段 |
| `autoDetect = false` 字段 + `markFieldsForSync("x")` + `requestSync()` | ✅ 已脏，两条路径都会带上它 |
| 需要立即发送（不等 tick） | 用 `syncToClient()`（但记得先置脏） |
| 需要把当前全部状态推给客户端 | `syncAllToClient()`（`writeAll = true`） |

---

## 1. 最基础存盘字段（`FissionReactorMachine`）

**场景**：机器运行状态，只需跨存档保留，客户端不需要知道。

```java
// src/main/java/com/gtocore/common/machine/multiblock/electric/FissionReactorMachine.java:65
@SaveToDisk(defaultValue = "298")
private int heat = 298;
@SaveToDisk(defaultValue = "0")
private int damaged;
@SaveToDisk(defaultValue = "0")
private int parallel;
@SaveToDisk(defaultValue = "0")
private int recipeHeat;
```

**为什么这样写**

| 细节 | 理由 |
| --- | --- |
| 每个字段都配 `defaultValue` | 绝大多数机器停在默认值，不写盘能显著缩小存档 |
| `heat` 初值 `298` 与 `defaultValue = "298"` 一致 | 否则新建机器第一次保存就写一条冗余记录 |
| 没有 `@SyncToClient` | 客户端渲染不需要；少传一份数据 |

**坑**

- `defaultValue` 是**字符串**，要能被解析成字段类型。`int` 写 `"298"`，`boolean` 写 `"false"`，
  `double` 写 `"0.0"`。
- 字段**初值**和 `defaultValue` 不一致 → 不报错，但存档会多一条。

---

## 2. 存盘 + 同步（`VacuumPumpMachine`）

**场景**：值既要在存档里，又要让客户端（渲染/UI）看到。

```java
// src/main/java/com/gtocore/common/machine/electric/VacuumPumpMachine.java:30
@SaveToDisk(defaultValue = "0")
@SyncToClient
private int vacuumTier;

@Getter
@SaveToDisk(defaultValue = "0")
private int totalEU;
```

**为什么这样写**

- `vacuumTier` 客户端要读（显示当前真空等级）→ 双注解。
- `totalEU` 只服务端统计 → 单 `@SaveToDisk`。
- 两个注解**互不影响**，可自由组合。

**坑**

- 只写 `@SyncToClient` 不写 `@SaveToDisk`：重启后值丢失（客户端拿到的是重新计算的默认值）。
- 只写 `@SaveToDisk`：客户端永远看到默认值（除非你在 `onLoad` 里主动推）。

---

## 3. 渲染可见的同步字段（`ManaHeaterMachine`）

**场景**：字段变化后客户端需要重新渲染。

```java
// src/main/java/com/gtocore/common/machine/mana/ManaHeaterMachine.java:30
/// an indicator used to determine if the salamander input is present
/// **used by client renderer**
@SaveToDisk(defaultValue = "false")
@SyncToClient(scheduleUpdate = true)
private boolean salamanderInput = false;
```

**`scheduleUpdate = true` 的实际效果**

1. 客户端收到该字段 → `FieldDataManager.readFromNetworkBuffer` 记录 `update = true`；
2. 本轮读取结束后调一次 `holder.scheduleUpdate(LogicalSide.CLIENT)`；
3. GTCEu 的 `GTBlockEntity.scheduleUpdate(side)` 会触发机器的渲染/模型刷新。

**注意**

- **一轮只调一次**：即使 5 个 `scheduleUpdate = true` 的字段同时变，也只调一次 `scheduleUpdate`。
- 字段没有变 → 不会触发。所以「每 tick 强制刷新」不适用此机制，那是 `requestSync()`+渲染循环的事。

---

## 4. 接收端回调（`MEStorageHatch` / `NanoPhagocytosisPlantMachine`）

**场景**：客户端收到新值后需要清理缓存、重算索引。

### 4.1 `MEStorageHatch`：能力可用性变化时清方向缓存

```java
// src/main/java/com/gtocore/common/machine/multiblock/part/MEStorageHatch.java:79
@SyncToClient(listener = "onStorageCapabilityAvailabilityChanged")
private boolean storageCapabilityAvailable;

private void onStorageCapabilityAvailabilityChanged(boolean newValue, boolean oldValue) {
    if (newValue != oldValue) {
        clearDirectionCache();
    }
}
```

### 4.2 `NanoPhagocytosisPlantMachine`：模块变化时清配方类型缓存

```java
// src/main/java/com/gtocore/common/machine/multiblock/electric/nano/NanoPhagocytosisPlantMachine.java:16
@SyncToClient(listener = "onGrindingModuleChanged")
private boolean grindingModule;

private void onGrindingModuleChanged(boolean newValue, boolean oldValue) {
    if (newValue != oldValue) setAvailableRecipeTypesCache(null);
}
```

**为什么用 `listener` 而不用 `scheduleUpdate`**

| | `scheduleUpdate = true` | `listener` |
| --- | --- | --- |
| 控制粒度 | 整台机器（调 `holder.scheduleUpdate`） | 精确到字段，能拿到 old/new |
| 典型用途 | 触发渲染刷新 | 清缓存 / 重算 / 精确比较 |

**签名硬要求**

```java
(T newValue, T oldValue) -> void    // 非静态，声明在同一声明类
```

`boolean` 字段就是 `(boolean, boolean)`。写成 `(Boolean, Boolean)` **不匹配**。

**注意**：`listener` **只在网络接收路径**触发。磁盘加载不会触发（用 `@SaveToDisk(listener=...)`）。
所以如果客户端也可能从存档加载，两处都要处理。

---

## 5. 手动置脏：大量容器（`PigmentMixer` / `AdvancedTesseractMachine`）

### 5.1 `PigmentMixer`：结构成型/失效时整体重发

```java
// src/main/java/com/gtocore/common/machine/multiblock/electric/PigmentMixer.java:26
@SyncToClient(autoDetect = false)
final Set<BlockPos> cachedYellowOffsets = new OpenCacheHashSet<>();
@SyncToClient(autoDetect = false)
final Set<BlockPos> cachedCyanOffsets = new OpenCacheHashSet<>();
// ...:28/30/32/34 依次为 Magenta / Black / White

@Override
public void onStructureFormed() {            // :42
    super.onStructureFormed();
    cachedYellowOffsets.addAll(...);
    // ...
    markFieldsForSync("cachedYellowOffsets", "cachedYellowOffsets",     // :49
            "cachedCyanOffsets", "cachedMagentaOffsets", "cachedWhiteOffsets");
}

@Override
public void onStructureInvalid() {           // :53
    super.onStructureInvalid();
    cachedYellowOffsets.clear();
    // ...
    markFieldsForSync("cachedYellowOffsets", "cachedYellowOffsets",     // :60
            "cachedCyanOffsets", "cachedMagentaOffsets", "cachedWhiteOffsets");
}
```

**为什么 `autoDetect = false`**

- 这些集合可能有上百个 `BlockPos`；每 tick 比对代价高。
- 它们的**变化时机是确定的**（结构成型/失效），显式置脏更便宜也更可控。

**为什么这些 `Set` 不需要 `@Access`**

- 字段是 `final` → 自动走访问模式。
- 且逻辑上「整体替换语义」更清晰（成型时全量更新）。

**坑（本仓现存缺陷，值得修）**

上面两个方法里的 `markFieldsForSync` 参数列表是：

```java
markFieldsForSync("cachedYellowOffsets", "cachedYellowOffsets",
        "cachedCyanOffsets", "cachedMagentaOffsets", "cachedWhiteOffsets");
//                 ↑ 重复                                    ↑ 缺 cachedBlackOffsets
```

- **重复名不报错**：`markFieldsForSync` 对已解析的名字重复标记是无害的（幂等）。
- **漏掉 `cachedBlackOffsets` 是实质缺陷**：该字段在 `onStructureFormed` 里
  `addAll(...)`、在 `onStructureInvalid` 里 `clear()`，但**从未被置脏**。
  由于它是 `autoDetect = false`，自动检测永久跳过它 → **黑色偏移量永远同步不到客户端**，
  客户端渲染会缺黑色液体层。
- 反过来说，`markFieldsForSync` 对**拼错**的名字会抛 `createFieldNotFoundException`，
  所以「拼错」能立刻发现，「漏写」则静默失效——这类问题只靠代码审查或渲染对比才能发现。

修改建议：

```java
markFieldsForSync("cachedYellowOffsets", "cachedCyanOffsets",
        "cachedMagentaOffsets", "cachedBlackOffsets", "cachedWhiteOffsets");
```

### 5.2 `AdvancedTesseractMachine`：卡片变化时重算目标列表

```java
// src/main/java/com/gtocore/common/machine/tesseract/AdvancedTesseractMachine.java:72
@SyncToClient(autoDetect = false)
private final List<TesseractDirectedTarget> locations = new ArrayList<>(MAX_TARGETS);

// :118 在重算逻辑末尾
markFieldsForSync("locations");
```

**模式总结**

```java
@SyncToClient(autoDetect = false)
final List<X> cache = new ArrayList<>();

private void recompute() {
    cache.clear();
    cache.addAll(...);          // 就地修改
    markFieldsForSync("cache"); // 显式置脏
    requestSync();              // GTCEu：触发自动同步链
}
```

**注意**：`markFieldsForSync` 只是**标脏**，不发送。GTOCore 里还需要 `requestSync()`
让 `autoSync()` 在下一 tick 走完整流程发出（它内部会调 `syncBlockEntityToClient`，
已脏字段一定会被写入）；或直接 `syncToClient()` 立即发（它只写脏字段，所以前置的
`markFieldsForSync` 是**必须的**）。

---

## 6. 非 final 容器的访问模式

**场景**：`ArrayList` 字段被就地修改（`clear()` / `addAll()`），需要内容变化被检测到。

```java
@SyncToClient
@Access
private ArrayList<String> EnergyInputHistoryDay = new ArrayList<>();

@SyncToClient
@Access
private ArrayList<String> EnergyOutputHistoryDay = new ArrayList<>();

@SyncToClient
@Access
private ArrayList<String> EnergyInputHistoryHour = new ArrayList<>();
// ... 共 6 个

@SyncToClient
private float energyFullness = 0.0f;

@SyncToClient
private Component[] bufferCache = new Component[0];
```

**为什么需要 `@Access`**

- 字段**非 final** → 默认走值模式。
- 值模式对 `ArrayList` 理论上做内容比对，但显式 `@Access` 让语义明确：
  「按容器访问模式处理，追踪就地修改」。
- 更重要的是：这些列表**引用始终不变**，只是内容变。访问模式是对这种模式的正确声明。

**`Component[]` 为什么不用 `@Access`**

- 它是**整体替换**语义（每次算出新数组赋值），值模式更合适。
- 值模式对数组做内容比对（`Arrays.equals`），能检测到替换。

**判断口诀**

```
引用不变、内容变 → 访问模式（@Access 或 final）
引用会换        → 值模式（默认）
```

---

## 7. 条件存盘（`AbstractRecipeInternalSlot`）

**场景**：字段只在特定状态下值得存，其他时候跳过以省空间。

```java
// src/main/java/com/gtocore/common/machine/multiblock/part/ae/AbstractRecipeInternalSlot.java
@SaveToDisk(defaultValue = "false")
boolean editingPattern;

@SaveToDisk(skipWhen = "skipEditorStacks")
GenericStack[] editorInputs = EMPTY_EDITOR_STACKS;

@SaveToDisk(skipWhen = "skipEditorStacks")
GenericStack[] editorOutputs = EMPTY_EDITOR_STACKS;

private boolean skipEditorStacks(GenericStack[] stacks) {
    return !editingPattern;        // 不在编辑模式时不存编辑器槽位
}
```

**语义**

- `skipWhen` 返回 `true` → **不写盘**。
- `skipWhen` 是**每个字段每轮**调用的（含 `@SaveToDisk` 与 `@SyncTo*`）。
- 方法签名 `(T) -> boolean`，`T` 是字段声明类型（这里是 `GenericStack[]`，不是 `Object`）。

**与 `defaultValue` 的分工**

| 机制 | 判定依据 | 适用 |
| --- | --- | --- |
| `defaultValue` | 值**等于**默认值 | 「默认状态很常见」 |
| `skipWhen` | 任意谓词 | 「依赖其他字段状态」 |
| `saveEmpty` | 空值（null/空集合/空数组） | 「空值不值得存」 |

三者可以叠加，先 `skipWhen` 后 `defaultValue`。

**注意**：被 `skipWhen` 跳过的字段，读取时**键不存在** → 值保持默认，
且 `@SaveToDisk(listener=...)` **不会触发**。

---

## 8. 自定义 holder 对象（`MEPatternBufferPartMachine`）

**场景**：一个内部类需要独立管理自己的字段（有独立的 `FieldDataManager`），
而不是让它被外部机器扁平化。

```java
// src/main/java/com/gtocore/common/machine/multiblock/part/ae/MEPatternBufferPartMachine.java:700
public static final class InternalSlot extends AbstractRecipeInternalSlot implements IFieldDataHolder {

    @SaveToDisk(skipWhen = "isLock")           // :717
    public final NotifiableNotConsumableItemHandler shareInventory;
    @SaveToDisk(skipWhen = "isLock")           // :719
    public final NotifiableNotConsumableFluidHandler shareTank;
    @SaveToDisk(skipWhen = "isLock")           // :721
    public final NotifiableInventory<AEItemKey> circuitInventory;

    @Getter
    @SaveToDisk(defaultValue = "false", listener = "setLock")   // :726
    private boolean lock;

    @Setter
    private boolean shouldLockRecipe = true;

    private final LazyFieldDataManager fieldDataManager = new LazyFieldDataManager(this);  // :730

    // :911
    @Override
    public FieldDataManager getFieldDataManager() {
        return fieldDataManager.get();
    }
}
```

**关键点**

| 点 | 说明 |
| --- | --- |
| `LazyFieldDataManager` 是 `private final` | 每实例一个，懒加载 |
| `getFieldDataManager()` 返回 `fieldDataManager.get()` | 固定写法 |
| 内部类**不继承** BlockEntity | 任何对象都能当 holder |
| 由外层机器手动驱动 | 见案例 9/10 |

**这个 holder 怎么被存盘**（同一文件里的 `IDataSerializable` 风格实现）：

```java
// :892 writeValue —— 直接把 @SaveToDisk 字段集写出
@Override
public @NotNull Object writeValue(@NotNull ValueOps ops) {
    return fieldDataManager.get().writeToValue(ops);
}

// :897 readValue —— 先处理旧格式，再走注解字段
@Override
public void readValue(@NotNull Object data, @NotNull ValueOps ops) {
    if (ops.isNull(data)) return;
    if (ops.dataVersion() < 2) {                    // :900
        var nbt = LegacyIO.legacyCompound(ops, data);
        if (nbt != null) {
            deserializeNBT(nbt);
            return;
        }
    }
    fieldDataManager.get().readFromValue(data, ops);
}
```

即：**外层负责调用 `fieldDataManager.get().readFromValue(data, ops)`**，
内部字段由它自己管。这就是「自定义 holder」的核心模式。

**与 `@AdditionalHolder(childManager = true)` 的区别**

| | `childManager = true` | 显式 `IFieldDataHolder` |
| --- | --- | --- |
| 子对象要实现接口吗 | 不需要 | 需要 |
| 编解码入口 | 外层字段自动 | 外层手动调 |
| 版本迁移控制 | 框架固定 | 完全自定义（如上例可插 legacy 分支） |
| GTOCore 偏好 | 少用 | **主用**（因为要版本迁移） |

---

## 9. 部分字段存取（物品 NBT）（`ResonanceFlowerMachine`）

**场景**：物品形态（block item）要携带机器状态，但只想存**部分字段**，
且不想手搓第二套 NBT 格式。

```java
// src/main/java/com/gtocore/common/machine/mana/multiblock/ResonanceFlowerMachine.java:265
/**
 * 物品形式保存：直接复用 {@code @SaveToDisk} 那套字段编解码（{@link JavaValueOps} 二进制），
 * 不再自己手搓一份 NBT 格式——同一份数据只留一条持久化路径。
 */
@Override
public void saveToItem(CompoundTag tag) {
    if (recipeProgresEntrys.isEmpty() && lastRecipe == null) return;
    byte[] data = JavaValueOps.INSTANCE.toBytes(
            getFieldDataManager().writeFieldsToValue(
                    JavaValueOps.INSTANCE, "recipeProgress", "lastRecipe"));
    tag.put(NBT_KEY_RECIPE_PROGRESS, new ByteArrayTag(data));
}

@Override
public void loadFromItem(CompoundTag tag) {
    if (tag.get(NBT_KEY_RECIPE_PROGRESS) instanceof ByteArrayTag data) {
        getFieldDataManager().readFieldsFromValue(
                JavaValueOps.INSTANCE.fromBytes(data.getAsByteArray()),
                JavaValueOps.INSTANCE, "recipeProgress", "lastRecipe");
    }
}
```

**为什么这样写**

| 点 | 说明 |
| --- | --- |
| `writeFieldsToValue(ops, names...)` | 只序列化指定字段，**用同一套 codec** |
| 存成 `ByteArrayTag` | 二进制紧凑，避免 NBT 结构与 Java 类型来回转换 |
| 复用 `@SaveToDisk` 的字段定义 | 字段在类上只声明一次，两条持久化路径共享 |

**返回值语义**

- `writeFieldsToValue` 返回值：`ops.createStringMap(map)`，或全部跳过时 `ops.createNull()`。
- `readFieldsFromValue`：**缺失的键直接跳过** → 旧物品没有该字段也安全。

---

## 10. 直接读写单字段（`MultiblockMEStorageMachine`）

**场景**：物品 NBT 只需携带一个字段。

```java
// src/main/java/com/gtocore/common/machine/multiblock/storage/MultiblockMEStorageMachine.java:342
@Override
public void loadFromItem(CompoundTag tag) {
    if (tag.get("keymap") instanceof ByteArrayTag byteArrayTag) {
        getFieldDataManager().readFieldFromValue(
                JavaValueOps.INSTANCE.fromBytes(byteArrayTag.getAsByteArray()),
                JavaValueOps.INSTANCE, "keyMap");
        recalculateStorage();
    }
}

@Override
public void saveToItem(CompoundTag tag) {
    tag.putByteArray("keymap", JavaValueOps.INSTANCE.toBytes(
            getFieldDataManager().writeFieldToValue("keyMap", JavaValueOps.INSTANCE)));
}
```

**注意**

- `writeFieldToValue` 返回的是**字段的 carrier 值**（不是 map），所以 `toBytes` 后是单个值。
- 若字段值是 `NOT_PERSISTED`（被 `skipWhen` 拦下），写入的是 null carrier → 读回得到默认值。
- 字段名必须与 `@SaveToDisk(key = ...)` 一致（若自定义了 `key`，**这里要用 `key` 还是字段名？
  用 `definitionMap` 的查找键，即 `key` 属性或字段名**——`writeFieldToValue` 走的是
  `storage.definitionMap.get(field)`，即**声明名/存储键**；两者不同时以 `definitionMap` 为准）。

---

## 11. Registry + 全局 codec（`ResearchTag`）

**场景**：一组固定的研究领域标签，需要紧凑网络 id 与稳定磁盘 key。

```java
// src/main/java/com/gtocore/api/research/ResearchTag.java
public static final Registry<String, ResearchTag> TAGS = createRegistry();

private static Registry<String, ResearchTag> createRegistry() {
    var registry = new Registry<String, ResearchTag>("research_tag", ValueCodec.STRING, t -> t.name);
    registry.unfreeze();
    return registry;
}

public ResearchTag(String name, String cn, String en, long bytePerPoint) {
    ...
    TAGS.register(name, this);       // 构造即注册
    ...
}

public static final ResearchTag MATERIAL = new ResearchTag("material", "材料", "Material", 16);
// ... 16 个标签

static {
    TAGS.freeze();                   // 类初始化末尾冻结（按 name 排序分配 id）
}
```

取出两个半侧并注册全局 codec：

```java
// src/main/java/com/gtocore/common/data/GTOCodecs.java:91-93, 150
public final StreamCodec<FriendlyByteBuf, ResearchTag> RESEARCH_TAG_STREAM_CODEC =
        ResearchTag.TAGS.streamCodec();
public final ValueCodec<ResearchTag> RESEARCH_TAG_DATA_CODEC =
        ResearchTag.TAGS.valueCodec();

public final DataSyncCodec<ResearchTag> RESEARCH_TAG_SYNC_CODEC =
        DataSyncCodec.register(ResearchTag.class,
                RESEARCH_TAG_STREAM_CODEC, RESEARCH_TAG_DATA_CODEC);
```

**为什么这样写**

| 点 | 说明 |
| --- | --- |
| `freeze()` 按 key 排序 | id 稳定（同一组 key 顺序永远一样） |
| 网络用 `streamCodec()` | VarInt id，1~2 字节 |
| 磁盘用 `valueCodec()` | 字符串 key，改序不破坏存档 |
| `DataSyncCodec.register` | 之后任何 `ResearchTag` 字段自动可用 |

**坑**

- `freeze()` 之后不能再 `register`（会抛异常/忽略，取决于实现）。
  所以所有 `ResearchTag` 常量必须在 `static {}` 之前声明完毕——这正是为什么它们都写成
  `public static final` 字段且在 `static` 块之前。
- 新增标签会改变 id 分配，**网络协议不兼容**——服务端客户端必须同步更新。

---

## 12. 手写 ValueCodec（`ICrossRecipeMachine.Thread`）

**场景**：一个内部类需要持久化，但不想为它做完整 `DataSyncCodec`。

```java
// GTOLib/src/main/java/com/gtolib/api/machine/feature/multiblock/ICrossRecipeMachine.java:372
@Getter
class Thread {

    public static final ValueCodec<Thread> DATA_CODECS = new ValueCodec<>() {

        @Override
        public @NotNull Object encode(ValueOps ops, Thread obj) {
            var list = new ArrayList<Object>(3);
            list.add(GTRecipe.DATA_CODEC.encode(ops, obj.recipe));
            ops.addInt(list, obj.progress);
            ops.addInt(list, obj.use);
            return ops.createList(list);
        }

        @Override
        public Thread decode(ValueOps ops, @NotNull Object data) {
            var list = ops.getList(data);
            var t = new Thread(GTRecipe.DATA_CODEC.decode(ops, list.get(0)), ops.getInt(list, 2));
            t.progress = ops.getInt(list, 1);
            return t;
        }
    };

    protected int progress;
    protected final GTRecipe recipe;
    protected final int duration;
    protected final int use;
}
```

注册（网络半自动派生）：

```java
// GTOLib/src/main/java/com/gtolib/GTOCore.java:64
DataSyncCodec.register(ICrossRecipeMachine.Thread.class, GTDataFixer.VERSION,
        ICrossRecipeMachine.Thread.DATA_CODECS);
```

**为什么这样写**

| 写法 | 理由 |
| --- | --- |
| 返回 `ops.createList(...)` | 用一个 list 打包 3 个值，结构简单、可读 |
| 用 `ops.addInt(list, ...)` | 避免 `ops.createInt` + `add` 两步 |
| 解码用 `ops.getInt(list, i)` | 带索引的捷径方法 |
| 字段 `recipe` 是 `final` | 构造时注入，所以解码时先构造再补 `progress` |

**注意**：`Thread` 字段本身用 `@SaveToDisk` 时，`DataSyncCodec.register` 必须早于字段扫描。
`GTOCore.bootstrap()` 在模组构造期执行，满足这个条件。

---

## 13. 数据版本迁移（`MEPatternBufferPartMachine`）

**场景**：字段存储格式变了，旧存档要能读。

```java
// src/main/java/com/gtocore/common/machine/multiblock/part/ae/MEPatternBufferPartMachine.java:897
@Override
public void readValue(@NotNull Object data, @NotNull ValueOps ops) {
    if (ops.isNull(data)) return;
    if (ops.dataVersion() < 2) {                  // :900
        var nbt = LegacyIO.legacyCompound(ops, data);
        if (nbt != null) {
            deserializeNBT(nbt);          // 旧格式：整块 NBT
            return;
        }
    }
    fieldDataManager.get().readFromValue(data, ops);   // 新格式：注解字段
}
```

**机制**

- `field_data_dataVersion` 在存盘时写入（`saveAdditional`）。
- 读取时通过 `JavaValueOps.create(version)` 传给 codec。
- codec 里 `ops.dataVersion()` 拿到版本 → 分支迁移。

**迁移策略模板**

```java
public Object encode(ValueOps ops, T obj) {
    // 总是写最新格式
    return ...;
}

public T decode(ValueOps ops, Object data) {
    if (ops.dataVersion() < NEWEST) {
        return upgradeFrom(ops.dataVersion(), ops, data);
    }
    return decodeCurrent(ops, data);
}
```

**坑**

- `VERSION` 是**全局静态**（`FieldDataHolderBlockEntity.VERSION`）。改它会影响所有 holder。
  想按类型分别版本化 → 覆盖 `saveAdditional`/`load` 自己写版本键。
- 在 `field_sync` 路径上没有版本概念（网络数据永远按当前格式解释），
  所以**不要指望同步路径做迁移**。

---

## 14. 纯二进制 carrier 存取（`ParticleContainmentItem`）

**场景**：物品 NBT 里存一个自定义对象，只需二进制、不需要字段框架。

```java
// src/main/java/com/gtocore/api/accelerator/item/ParticleContainmentItem.java:35
public static @Nullable ParticleBeam getContained(ItemStack stack) {
    var tag = stack.getTag();
    if (tag == null) return null;
    if (!tag.contains(NBT_KEY_PARTICLE)) return null;
    var particleTag = tag.getByteArray(NBT_KEY_PARTICLE);
    // 物品 NBT 里的束流是持久化数据：读的时候显式声明版本，解码器不会替它猜。
    var ops = JavaValueOps.create(GTDataFixer.VERSION);
    var particle = ParticleBeam.DATA_CODEC.decode(ops, ops.fromBytes(particleTag));
    if (particle.isEmpty()) return null;
    return particle;
}

public static final String NBT_KEY_PARTICLE = "contained_particle";   // :47
```

**关键点**

- `JavaValueOps.create(version)` 用于**读**（需要版本）；`INSTANCE` 用于**写**普通数据。
- 注释点破了原则：**读持久化数据时显式声明版本**，不要让解码器猜。
- 写入侧对应写法（同仓惯例）：

```java
var ops = JavaValueOps.create(GTDataFixer.VERSION);
tag.putByteArray(NBT_KEY_PARTICLE, ops.toBytes(ParticleBeam.DATA_CODEC.encode(ops, beam)));
```

> 本文件只实现了读半侧（写入方在别处），这里给的是同仓一致写法。

---

## 15. 机器级同步：`requestSync()` 的正确用法

### 15.1 三种触发方式

```java
// 方式 1：标脏，等 autoSync 走完整流程（推荐，最省心）
private void onSomethingChanged() {
    this.field = newValue;
    requestSync();       // holder.sync = true → 下个 tick autoSync() →
                         //   syncBlockEntityToClient(this)（自动检测 + 增量）
}

// 方式 2：立即同步（增量，只发已标脏字段）
private void flushNow() {
    markFieldsForSync("field");   // 必须：autoDetect=true 的变化此时还没被检测
    syncToClient();               // 直接发包，不跑自动检测
}

// 方式 3：立即全量同步（不看脏标记）
private void fullResync() {
    syncAllToClient();
}
```

> 方式 2 的前提很重要：`syncToClient()` **不做自动检测**，它只写「此刻已经是脏的」字段。
> 若字段是 `autoDetect = true` 且你刚改完，它**还没被标记**——直接调 `syncToClient()`
> 会发出空 payload。要么先用 `markFieldsForSync`，要么走方式 1。

### 15.2 真实调用点

```java
// src/main/java/com/gtocore/common/machine/generator/WindMillTurbineMachine.java:199
} else {
    if (hasRotor) {
        this.requestSync();
        inventory.storage.setStackInSlot(0, ItemStack.EMPTY);
    }
    spinSpeed = 0;
    hasRotor = false;
}
```

```java
// src/main/java/com/gtocore/common/machine/multiblock/part/MEStorageHatch.java:100
private void updateStorageCapabilityAvailability() {
    boolean available = storage != null;
    if (storageCapabilityAvailable != available) {
        storageCapabilityAvailable = available;
        clearDirectionCache();
        requestSync();
    }
}
```

```java
// src/main/java/com/gtocore/common/machine/multiblock/part/NeutronIrradiationPartMachine.java:183
if (getOffsetTimer() % 10 == 0) requestSync();   // 周期性刷新
```

### 15.3 决策

| 场景 | 用什么 |
| --- | --- |
| 值改变，不急于这一 tick（`autoDetect = true`） | `requestSync()` |
| 值改变，必须立即让客户端看到 | `markFieldsForSync(...)` + `syncToClient()` |
| 客户端状态可能不同步（新玩家/重连） | `syncAllToClient()` |
| 周期性刷新（如每 10 tick） | `requestSync()`（自动检测只发变更字段，便宜） |
| 忘了调任何方法 | 40 tick 周期兜底会尝试一次（`autoSync` 的 `periodID % 40`） |

### 15.4 常见错误

| 错误 | 后果 |
| --- | --- |
| 只在服务端改字段，从不 `requestSync()` | 最多延迟 2 秒（周期兜底），但依赖兜底不可控；显式调用更好 |
| `autoDetect = false` 字段只 `requestSync()` 不 `markFieldsForSync` | 自动检测跳过它 → 不发该字段 |
| `autoDetect = true` 字段刚改完就调 `syncToClient()` | 尚未标脏 → 发出空 payload |
| 客户端也调 `requestSync()` | 置的是客户端侧标记；`autoSync` 在服务端才发，客户端无效 |
| 在 `scheduleUpdate` 里再 `requestSync()` | 客户端侧无意义，可能造成回环 |

---

## 16. GameTest 里模拟一次同步

**场景**：写测试时验证「同步后目标状态正确」，不想起真客户端。

```java
// src/gametest/java/com/gtocore/gametest/storage/StorageUpgradeGameTests.java:95
private static void sync(MetaMachineBlockEntity source, MetaMachineBlockEntity target, boolean full) {
    source.getFieldDataManager().updateFieldDirtyFlags(LogicalSide.SERVER, false);
    target.getFieldDataManager().readFromNetworkBuffer(
            LogicalSide.CLIENT,
            source.getFieldDataManager().writeToNetworkBuffer(LogicalSide.SERVER, full));
}
```

**为什么有效**

- 同步的本质就是「写 buffer → 读 buffer」，网络只是传输。
- `updateFieldDirtyFlags(SERVER, false)` 先让自动检测跑一遍（`false` = 检查所有 sync 字段，
  包括 `autoDetect = false` 的——测试里通常想要全量对比）。
- `writeToNetworkBuffer(SERVER, full)` → `readFromNetworkBuffer(CLIENT, ...)` 完成应用。

**同样的模式也在 `StorageAppearanceGameTests` 里**

```java
// src/gametest/java/com/gtocore/gametest/storage/StorageAppearanceGameTests.java:57
source.getFieldDataManager().updateFieldDirtyFlags(LogicalSide.SERVER, false);
target.getFieldDataManager().readFromNetworkBuffer(
        LogicalSide.CLIENT,
        source.getFieldDataManager().writeToNetworkBuffer(LogicalSide.SERVER, full));
```

**注意**

- `target` 必须是**真实的** `MetaMachineBlockEntity`（同类型），否则字段索引对不上。
- `readFromNetworkBuffer` 会触发 `scheduleUpdate` / `listener`——测试里要意识到这些副作用。

---

## 17. `IDataSerializable` 作为外层壳

**场景**：一个内部类需要**完全控制**序列化字节布局，同时希望内部字段仍由框架管理，
并且要处理旧格式迁移。

### 代码（`MEPatternBufferPartMachine.InternalSlot`）

```java
// src/main/java/com/gtocore/common/machine/multiblock/part/ae/MEPatternBufferPartMachine.java:700
public static final class InternalSlot extends AbstractRecipeInternalSlot implements IFieldDataHolder {

    private final LazyFieldDataManager fieldDataManager = new LazyFieldDataManager(this);   // :730

    // ===== 网络：不同步（注释明说「无同步，不实现」）=====
    @Override
    public void readBuffer(LogicalSide logicalSide, FriendlyByteBuf friendlyByteBuf) {
        // 无同步，不实现
    }

    // ===== 磁盘写：内部字段直接委托 =====
    @Override
    public @NotNull Object writeValue(@NotNull ValueOps ops) {           // :892
        return fieldDataManager.get().writeToValue(ops);
    }

    // ===== 磁盘读：先试旧格式，再走注解字段 =====
    @Override
    public void readValue(@NotNull Object data, @NotNull ValueOps ops) {  // :897
        if (ops.isNull(data)) return;
        if (ops.dataVersion() < 2) {                                      // :900
            var nbt = LegacyIO.legacyCompound(ops, data);
            if (nbt != null) {
                deserializeNBT(nbt);
                return;
            }
        }
        fieldDataManager.get().readFromValue(data, ops);
    }

    @Override
    public FieldDataManager getFieldDataManager() {                       // :911
        return fieldDataManager.get();
    }
}
```

### 为什么这样写

| 点 | 说明 |
| --- | --- |
| 同时实现 `IFieldDataHolder` 与 `IDataSerializable` | 前者让内部字段被扫描；后者接管**外层**字节布局 |
| `writeValue` 全权委托 | 内部字段用 `@SaveToDisk` 声明，不自造格式 |
| `readValue` 分版本 | 旧存档是整块 NBT，新存档是字段 carrier map |
| `readBuffer` 空实现 | 该内部类不参与网络同步（只在服务端/存档时用） |
| `ops.isNull(data)` 早返回 | 空载荷不是错误 |

### 与 `@AdditionalHolder(childManager = true)` 的对比

| | `childManager = true` | 本例（显式双实现） |
| --- | --- | --- |
| 迁移控制 | 框架固定 | **完全自定义**（可插 legacy 分支） |
| 需要写 `readValue`/`writeValue` | 否 | **是** |
| 需要实现 `IFieldDataHolder` | 否 | **是**（为了内部字段扫描） |
| GTOCore 选择 | 不用 | **用** |

> 结论：需要**版本迁移**或**旧格式兼容**时，显式 `IDataSerializable` + 内部 `FieldDataManager`
> 是唯一可行的组合。这是 GTOCore 不用 `childManager` 的根本原因。

---

## 18. GTOCore 未使用的功能清单

以下功能在 DataSyncLib 中存在且可用，但**本仓源码中完全没有使用**。
新增代码时若需要，按对应章节接入即可。

| 功能 | 章节 | 本仓未用原因（推测） |
| --- | --- | --- |
| `@SyncToServer` | [02 §2](02-annotations.md#2-synctoserver) | GTCEu 有自有 C2S 包体系（`ISync.syncToServer`） |
| `@AdditionalHolder`（两种模式） | [02 §5](02-annotations.md#5-additionalholder) | 需版本迁移 → 改用显式 `IDataSerializable` |
| `@Codec` | [02 §6](02-annotations.md#6-codec) | 自定义类型统一走 `DataSyncCodec.register` |
| `@Conversion` | [02 §7](02-annotations.md#7-conversion) | 无「换类型管理」的需求 |
| `@Strategy` | [02 §8](02-annotations.md#8-strategy) | 内置 `ItemStack`/`FluidStack` 策略已够用 |
| `@Generic` | [02 §9](02-annotations.md#9-generic) | 泛型字段都写成了显式泛型 |
| `@AddToManager` | [02 §10](02-annotations.md#10-addtomanager) | 未使用 `FieldDataCodec` 包 POJO |
| `@RemoteCall` | [02 §11](02-annotations.md#11-remotecall) | GTCEu 有自有网络体系 |
| `@Access(instanceAsValue)` | [02 §4](02-annotations.md#4-access) | 无可空容器字段 |
| `FieldDefinitionStorage.register*Factory` | [04](04-extension-points.md) | 类型都走 `DataSyncCodec.register` |
| `IDataSerializable`（作为字段类型） | [03 §6](03-core-api.md#6-idataserializable) | 只用作**外层壳**（案例 17），未当字段类型 |
| `DataComponent` 三件套 | [06 §2](06-data-models.md#2-datacomponent-三件套) | 无动态组件需求 |
| `FileUtil` 版本化文件 | [06 §3](06-data-models.md#3-版本化文件) | 数据在 NBT 里，不写独立文件 |
| `ValueConverters` | [05 §5](05-codecs.md#5-valueconverters) | 未处理 SELF 值 |
| `CustomTypes`（正确做法是不用） | [05 §6](05-codecs.md#6-customtypes库内部-registry) | 按设计不对下游开放 |
| `Registry` 4 参自动注册 | [06 §1](06-data-models.md#1-registry) | 用了 3 参 + 显式注册（案例 11） |
| `*NotifiableHolder` 家族 | [03 §5](03-core-api.md#5-监听器-holder-体系) | 用 `@SyncTo*.listener` 替代 |
| `FieldDataCodec` | [03 §1.6](03-core-api.md#16-fielddatacodect) | 未用 POJO 整体序列化 |

---

## 附：GTOCore 用法统计（供参考）

在 `src` + `GTOLib/src` 上统计：

| 注解 | 出现次数 | 说明 |
| --- | --- | --- |
| `@SaveToDisk` | **564** | 绝对主力，几乎每个机器都有 |
| `@SyncToClient` | **185** | 渲染/UI 相关字段 |
| `@Access` | 23（DataSyncLib 的已无使用，全部是 Lombok `@Accessors`/Mixin `@Accessor`） | 无 |
| `@SyncToServer` | 0 | GTCEu 体系下未使用 |
| `@AdditionalHolder` | 0 | 改用显式 `IFieldDataHolder` |
| `@Codec` / `@Conversion` / `@Strategy` / `@Generic` / `@AddToManager` | 0 | 主仓未使用 |
| `@RemoteCall` | 0 | 主仓未使用 |

**结论**：GTOCore 的用法高度集中在 `@SaveToDisk` + `@SyncToClient(autoDetect/scheduleUpdate/listener)`
+ `markFieldsForSync`。其余能力见 [§18 清单](#18-gtocore-未使用的功能清单)。
