# 02 · 注解功能参考

> [← 返回 00 概要](00-overview.md)

> DataSyncLib 的声明层：11 个注解的**完整参数语义、生效条件、回调签名、边界行为**。
> 源码：`annotations/`。每条都标注了它最终落到 `DataFieldDefinition` 的哪个字段。

---

## 注解 → DataFieldDefinition 映射

扫描器把注解解析成 `DataFieldDefinition` 的公开字段（`DataFieldDefinition.java`）：

| 注解属性 | `DataFieldDefinition` 字段 | 类型 |
| --- | --- | --- |
| `@SaveToDisk` 存在 | `isSave` | `boolean` |
| `@SaveToDisk(key)` | `key` | `String` |
| `@SaveToDisk(saveEmpty)` | `saveEmpty` | `boolean` |
| `@SyncToClient` 存在 | `isSyncToClient` | `boolean` |
| `@SyncToServer` 存在 | `isSyncToServer` | `boolean` |
| `@Access` / `final` | `isFinal` + 工厂选择 | `boolean` |
| `@Access(instanceAsValue)` | `instanceAsValue` | `boolean` |
| `@Strategy` | `strategy` | `Hash.Strategy<T>` |
| `@Generic` 或泛型字段 | `genericType` / `genericCodecs` | `Class<?>[]` / `DataSyncCodec<?>[]` |
| `@Conversion(toManaged)` | `conversionGet` | `Function<Object,Object>` |
| `@Conversion(toField)` | `conversionSet` | `Function<T,Object>` |
| 字段本身 | `field` / `isFinal` / `factory` / `source` | — |

`DataFieldDefinition` 还暴露了运行期判定方法（供自定义 `DataField` 使用）：

```java
boolean hasDefaultValue();
T       getDefaultValue(Object source);            // 对象类型
boolean getDefaultBooleanValue(Object source);     // 原始类型各有一份
int     getDefaultIntValue(Object source);
// byte/short/long/float/double/char 同理

boolean skipSync(LogicalSide side, Object source, T value);   // 原始类型各有重载
boolean skipSave(Object source, T value);                     // 同上
```

**`skipSync` / `skipSave` 的行为**：内部先判 `skipWhen` 谓词，再判 `defaultValue`。
这就是为什么"默认值省略"对同步与存盘是**分开**的两个入口。

---

## 1. `@SyncToClient`

**源码**：`SyncToClient.java` ｜ **方向**：服务端 → 客户端（S2C）

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface SyncToClient {
    boolean scheduleUpdate() default false;
    boolean autoDetect()     default true;
    String  skipWhen()       default "";
    String  listener()       default "";
}
```

### 参数

| 参数 | 默认 | 语义 | 内部落点 |
| --- | --- | --- | --- |
| `autoDetect` | `true` | `true`：每轮 sync 自动比对快照。<br>`false`：**不参与自动比对**，只能靠 `markFieldsForSync` 置脏。 | `DataFieldDefinition#autoDetect(LogicalSide)` |
| `scheduleUpdate` | `false` | 接收端应用该字段后触发 `holder.scheduleUpdate(side)`。 | `DataFieldDefinition#scheduleUpdate(LogicalSide)` |
| `skipWhen` | `""` | 方法 `(T)->boolean`；返回 `true` = 本轮不发。 | `DataFieldDefinition#skipSync(...)` |
| `listener` | `""` | 方法 `(T new, T old)->void`；网络接收路径应用后调用。 | 字段实现的 `readFromBuffer` 内 |

### 行为细节

**1. 增量协议**：`writeToNetworkBuffer` 只为 `isChanged()` 的字段写
`VarInt(索引) + payload`。`writeAll=true` 时才全写。

**2. 三处短路**（决定了"什么都没发生"的原因）：

```
hasSyncFields(SERVER)                        → 无 @SyncToClient 字段：直接返回
updateFieldDirtyFlags(SERVER, auto) == false → 无脏字段：不发包
writeToNetworkBuffer 里 isChanged() == false → 该字段不写入
```

**3. `scheduleUpdate` 的聚合语义**：一轮读取中只要有**任意一个**字段的
`scheduleUpdate(side)` 为 true，末尾调**一次** `holder.scheduleUpdate(side)`。
不是每字段一次。

**4. `listener` 只在网络路径触发**。磁盘加载不触发（用 `@SaveToDisk(listener)`）。
区块同步（`field_sync`）也不触发——它走 `readFromNetworkBuffer`，**会**触发 `listener` 吗？
会：`field_sync` 走的就是 `readFromNetworkBuffer(CLIENT, ...)`，所以 `@SyncToClient.listener`
在区块加载同步时**也会**触发。这是与 `@SaveToDisk.listener` 的重要区别。

**5. 与 `@SaveToDisk` 可自由组合**，两者互不影响。

### 实例（GTOCore）

```java
// src/main/java/com/gtocore/common/machine/electric/VacuumPumpMachine.java:30
@SaveToDisk(defaultValue = "0")
@SyncToClient
private int vacuumTier;
```

```java
@SyncToClient
@Access
private ArrayList<String> EnergyInputHistoryDay = new ArrayList<>();
```

```java
// src/main/java/com/gtocore/common/machine/multiblock/part/MEStorageHatch.java:79
@SyncToClient(listener = "onStorageCapabilityAvailabilityChanged")
private boolean storageCapabilityAvailable;
```

---

## 2. `@SyncToServer`

**源码**：`SyncToServer.java` ｜ **方向**：客户端 → 服务端（C2S）

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface SyncToServer {
    boolean scheduleUpdate() default false;
    boolean autoDetect()     default true;
    String  skipWhen()       default "";
    String  listener()       default "";
}
```

参数语义与 `@SyncToClient` **完全相同**，仅方向相反。

### 方向相关的行为差异

| 项 | `@SyncToClient` | `@SyncToServer` |
| --- | --- | --- |
| 触发方法 | `syncXxxToClient`（服务端调） | `syncXxxToServer`（客户端调） |
| 会遍历的字段集 | `syncToClientFields` | `syncToServerFields` |
| `LogicalSide` 参数 | `SERVER` | `CLIENT` |
| 接收后自动标脏 | 否（客户端） | **是**：`if (side.isServer()) be.setChanged()` |
| 区块同步载荷 `field_sync` | 包含 | 不包含 |

### 安全边界

框架**不做来源校验**。客户端可写任何 `@SyncToServer` 字段。服务端必须在消费处校验：
数值范围、枚举合法性、目标是否存在。

### 设计定位

服务端权威。`@SyncToServer` 只把值送到服务端，**不广播给其他玩家**。
要让改动对他人可见，服务端处理后走 `@SyncToClient` 下发。

### GTOCore 现状

主仓 `@SyncToServer` 使用数 **0**（GTCEu 体系下客户端输入多走自有包）。
如需使用，确认 GTCEu 的 `ISync.syncToServer()` 调用链已接入；否则手动调
`DataSyncNetwork.syncBlockEntityToServer(this, ...)`。

---

## 3. `@SaveToDisk`

**源码**：`SaveToDisk.java` ｜ **方向**：磁盘

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface SaveToDisk {
    String  key()                 default "";
    String  skipWhen()            default "";
    boolean saveEmpty()           default false;
    String  defaultValue()        default "";
    String  defaultValueGetter()  default "";
    String  listener()            default "";
}
```

### 参数

| 参数 | 默认 | 语义 | 内部落点 |
| --- | --- | --- | --- |
| `key` | `""` | 存档键名；空则用字段名 | `DataFieldDefinition#key` |
| `skipWhen` | `""` | 方法 `(T)->boolean`；`true` = 不写 | `#skipSave(...)` |
| `saveEmpty` | `false` | 空值（null/空集合/空数组）是否写 | `#saveEmpty` |
| `defaultValue` | `""` | 字符串形式的默认值；等于则不写 | `#getDefaultXxxValue(...)` |
| `defaultValueGetter` | `""` | 方法名，动态默认值（优先于 `defaultValue`） | 同上 |
| `listener` | `""` | 方法 `(T)->void`；磁盘加载后调用 | `#getSaveListener()` |

### 写入判定顺序（精确）

```
writeToValue(source, ops):
  1. skipWhen(value) == true            → 返回 NOT_PERSISTED
  2. value.equals(defaultValue)         → 返回 NOT_PERSISTED
  3. value == null && !saveEmpty        → 返回 null（不写入 map）
  4. value 是空集合/数组 && !saveEmpty   → 返回 null（不写入 map）
  5. 否则                                → 正常编码
```

`NOT_PERSISTED` 是**哨兵对象**（`DataField.NOT_PERSISTED`），identity 比较，永不参与序列化。

### `defaultValue` 的类型解析

| 字段类型 | 字符串形式 |
| --- | --- |
| `boolean` | `"true"` / `"false"` |
| `byte` / `short` / `int` / `long` | 整数字面量 |
| `float` / `double` | 浮点字面量 |
| `char` | 单字符 |
| `String` | 原样 |
| 枚举 | 枚举常量名 |
| 其他对象 | **不支持**（用 `defaultValueGetter`） |

`defaultValueGetter` 指向**非静态**方法，签名 `() -> T`（无参）。它在**每个字段每次写盘时**调用。

### `listener` 触发矩阵

| 事件 | 是否触发 |
| --- | --- |
| 磁盘加载（`field_save`），字段键存在 | ✅ |
| 磁盘加载，字段键缺失（被跳过/旧存档） | ❌ |
| 网络同步（`readFromBuffer`） | ❌ |
| 区块加载同步（`field_sync`） | ❌ |
| `readFieldsFromValue`（子集读） | ✅（字段在子集内且键存在时） |

签名要求：**恰好一个参数，类型精确匹配**（`int` 不是 `Integer`），**非静态**，
声明在字段所在类。

### 实例

```java
// src/main/java/com/gtocore/common/machine/multiblock/electric/FissionReactorMachine.java:65
@SaveToDisk(defaultValue = "298")
private int heat = 298;
@SaveToDisk(defaultValue = "0")
private int damaged;

// src/main/java/com/gtocore/common/machine/multiblock/part/ae/AbstractRecipeInternalSlot.java:13
@SaveToDisk(skipWhen = "skipEditorStacks")
GenericStack[] editorInputs = EMPTY_EDITOR_STACKS;

private boolean skipEditorStacks(GenericStack[] stacks) {
    return !editingPattern;
}

// src/main/java/com/gtocore/common/machine/multiblock/part/ae/MEPatternBufferPartMachine.java:726
@SaveToDisk(defaultValue = "false", listener = "setLock")
private boolean lock;
```

---

## 4. `@Access`

**源码**：`Access.java` ｜ **作用**：容器类型的处理模式

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface Access {
    boolean instanceAsValue() default false;
}
```

### 模式选择的完整规则

| 字段声明 | 无 `@Access` | 有 `@Access` |
| --- | --- | --- |
| 非 final 普通值 | 值模式 | 访问模式 |
| 非 final 容器 | 值模式（内容比对） | 访问模式 |
| `final` 普通值 | 访问模式 | 访问模式 |
| `final` 容器 | 访问模式 | 访问模式 |

> 注意：`final` 字段**无论是否加 `@Access`** 都走访问模式。

### 两种模式的内部差异

| | 值模式（`AbstractField` / `ObjField`） | 访问模式（`AbstractFieldAccess`） |
| --- | --- | --- |
| 读值 | 直接取字段 | 取字段引用 |
| 快照 | 保存 `lastValue` + `lastHash` | 保存 `hashCode`（部分实现） |
| 变更检测 | `hashCode` 快速失败 → `strategy.equals` | 容器自身哈希/内容 |
| 写回 | `field.set(source, newValue)` | 就地 `add`/`put`/`set` |
| null 处理 | `false` 布尔前缀 | 视 `instanceAsValue` |

### `instanceAsValue`

| 值 | 语义 |
| --- | --- |
| `false`（默认） | 假定实例永远存在，只序列化**内容**。字段为 null 时行为未定义（依赖具体实现）。 |
| `true` | 把实例本身当**可空值**编码：先写存在标记，再写内容。允许 `null` ↔ 实例的往返。 |

**何时必须开 `true`**：字段可能在某个时刻为 `null`，且需要把这个 `null` 状态持久化/同步。

### 实例

```java
// GTOCore：非 final ArrayList 就地修改 → 需要访问模式
@SyncToClient
@Access
private ArrayList<String> EnergyInputHistoryDay = new ArrayList<>();
```

---

## 5. `@AdditionalHolder`

**源码**：`AdditionalHolder.java` ｜ **作用**：嵌套对象的字段托管模式

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface AdditionalHolder {
    boolean childManager() default false;
}
```

### 两种模式的能力对照

| 维度 | 扁平化（默认） | `childManager = true` |
| --- | --- | --- |
| 扫描范围 | 递归扫描子对象类的注解字段 | 不扫描；子对象自建 manager |
| 字段归属 | 全部注册到**父** manager | 子对象**独立** manager |
| 访问路径 | 组合 getter 链（`DataFieldDefinition.source`） | 整个对象当**一个字段** |
| 子对象需实现 `IFieldDataHolder` | 否 | 否（用 `ChildFieldDataHolder` 适配） |
| 增量同步粒度 | 每个子字段独立 | 整个对象原子 |
| 命名冲突 | 子对象同名字段会冲突 | 不冲突 |
| 版本迁移 | 子字段各自版本化 | 子对象内部自定义 |
| 嵌套 | 支持递归 | 支持递归（子对象内再标 `childManager`） |

### 扁平化的实现机制

`DataFieldDefinition.source` 是 `Function<Object, Object>`——组合的 getter 链。
`IFieldDataHolder.getSource(definition)` 默认实现：

```java
default Object getSource(DataFieldDefinition<?> definition) {
    var source = definition.source;
    if (source == null) return this;
    return source.apply(this);
}
```

所以扁平化后，父 manager 里的字段定义带着"如何从父对象走到子对象"的路径。

### 子管理器的实现机制

`ChildFieldDataHolder`（`ChildFieldDataHolder.java`）：

```java
public final class ChildFieldDataHolder implements IFieldDataHolder {
    private final Object target;
    private final LazyFieldDataManager fieldDataManager;   // 用 target.getClass() 扫描

    @Override public Object getSource(DataFieldDefinition<?> def) {
        var source = def.source;
        if (source == null) return target;   // 子 manager 的顶层字段无父链
        return source.apply(target);
    }

    // 4 个 custom data 钩子 + scheduleUpdate 全部转发给 target（若 target 也是 holder）
}
```

### 字段声明示例

```java
// 扁平化
@AdditionalHolder
private InventoryData inventory = new InventoryData();

// 子管理器
@SaveToDisk
@SyncToClient
@AdditionalHolder(childManager = true)
private final TestEntitySub sub = new TestEntitySub();
```

> `childManager` 字段本身通常还要加 `@SaveToDisk`/`@SyncToClient`——因为它作为**一个字段**
> 参与父 manager 的同步/存盘。

### GTOCore 现状

主仓使用数 **0**。倾向显式 `IFieldDataHolder`（需要控制版本迁移，见 [08 §8](08-gtocore-usage.md#8-自定义-holder-对象mepatternbufferpartmachine)）。

---

## 6. `@Codec`

**源码**：`Codec.java` ｜ **作用**：覆盖默认 codec 查找

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface Codec {
    String saveCodec()     default "";   // 磁盘：ValueCodec/CombinedCodec 静态字段名
    String syncCodec()     default "";   // 网络：StreamCodec/CombinedCodec 静态字段名
    String writeToValue()  default "";   // 实例方法 (ValueOps, T) -> Object
    String readFromValue() default "";   // 实例方法 (ValueOps, Object) -> T
    String writeToBuffer() default "";   // 实例方法 (FriendlyByteBuf, T) -> void
    String readFromBuffer()default "";   // 实例方法 (FriendlyByteBuf) -> T
}
```

### 两种模式（互斥）

**模式 1：静态 codec 字段**

| 属性 | 查找位置 | 类型 |
| --- | --- | --- |
| `saveCodec` | 同声明类的静态字段 | `ValueCodec<T>` 或 `CombinedCodec<T>` |
| `syncCodec` | 同上 | `StreamCodec<FriendlyByteBuf,T>` 或 `CombinedCodec<T>` |

规则：
- 只给 `saveCodec` → `syncCodec` 从它派生（`CombinedCodec` 提供两侧）。
- 名字查找走 `getDeclaredField`（**不搜父类**）。
- 字段必须 `static`。

**模式 2：实例方法**

| 属性 | 签名 | 用途 |
| --- | --- | --- |
| `writeToValue` | `(ValueOps ops, T value) -> Object` | 磁盘编码 |
| `readFromValue` | `(ValueOps ops, Object data) -> T` | 磁盘解码 |
| `writeToBuffer` | `(FriendlyByteBuf buf, T value) -> void` | 网络编码 |
| `readFromBuffer` | `(FriendlyByteBuf buf) -> T` | 网络解码 |

在**持有者实例**上调用（无 source 参数），可访问其他成员。
触发条件：`saveCodec` 为空时启用。

### 与 `DataSyncCodec.register` 的关系

| | `@Codec` | `DataSyncCodec.register` |
| --- | --- | --- |
| 作用范围 | 单个字段 | 全局（所有该类型的字段） |
| 时机 | 注解解析时 | 注册时（必须早于扫描） |
| 适用 | 类型只在个别字段用/需要实例上下文 | 类型普遍使用 |

### 字段实现的选择

| 情况 | 使用的 `DataField` |
| --- | --- |
| `@Codec` 指定静态 codec | `CustomObjCodecField`（用提供的 codec） |
| 无 `@Codec`，类型有全局 codec | `ObjCodecField`（用 `DataFieldDefinition` 的 codec） |
| 无匹配 codec | 扫描期异常 |

### 实例：`FieldDataCodec` 作为静态 codec

```java
private static final FieldDataCodec<MyConfig> CONFIG_CODEC =
        FieldDataManager.createCodec(MyConfig.class, MyConfig::new);

@SaveToDisk
@SyncToClient
@Codec(saveCodec = "CONFIG_CODEC", syncCodec = "CONFIG_CODEC")
private MyConfig config = new MyConfig();
```

`FieldDataCodec<T>` 实现 `CombinedCodec<T>` + `IFieldDataHolder`，内部用 `writeAllToValue` /
`readAllFromValue`（因此 POJO 内部字段可以只标 `@AddToManager`）。

### 实例：实例方法

```java
@Codec(writeToValue = "myWrite", readFromValue = "myRead")
private MyObj data;

private Object myWrite(ValueOps ops, MyObj value) { return ops.createString(value.toString()); }
private MyObj  myRead(ValueOps ops, Object data)   { return MyObj.parse(ops.getString(data)); }
```

---

## 7. `@Conversion`

**源码**：`Conversion.java` ｜ **作用**：字段声明一种类型，框架按另一种类型管理

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface Conversion {
    String toManaged();          // 必填：静态 Function<A, B>
    String toField() default ""; // 可选：静态 Function<B, A>
}
```

### 机制

1. `toManaged` → 同声明类的 `static Function<A, B>` 字段（`getDeclaredField`，**不搜父类**）。
2. `A` 必须等于字段声明类型；`B` 成为**被管理类型**——codec/strategy/factory 全按 `B` 解析。
3. `toField` → 反向 `Function<B, A>`，`DataFieldDefinition#set` 时调用。

### 何时可以省略 `toField`

| 情况 | 可省略 |
| --- | --- |
| 字段是 `final` | ✅（setter 不会被调用） |
| 字段加了 `@Access` | ✅（访问模式就地修改容器） |
| 字段非 final 且值模式 | ❌（写回需要反向转换） |

### 边界行为

| 情况 | 结果 |
| --- | --- |
| `toField` 写了但方法不存在 | **静默忽略**（不抛异常） |
| `toManaged` 的入参类型不匹配字段类型 | 运行期 `ClassCastException` |
| 函数字段非 static | 解析失败 |

### 实例：只读视图（`CompoundTag` 按 `Map<String,Tag>` 管理）

```java
// src/main/java/com/gto/datasynclib/test/TestBlockEntity.java:303
static final Function<CompoundTag, Map<String, Tag>> COMPOUND_TAG_MAP_FUNCTION =
        NbtUtil.COMPOUND_TAG_MAP;

@SyncToClient
@SaveToDisk
@Conversion(toManaged = "COMPOUND_TAG_MAP_FUNCTION")
final CompoundTag tagData = new CompoundTag();
```

`final` 字段 → 无需 `toField`，`CompoundTag` 通过自身 API 就地修改。

### 实例：双向转换

```java
private static final Function<MyStorage, MyView> TO_VIEW   = MyStorage::toView;
private static final Function<MyView, MyStorage> FROM_VIEW = MyView::toStorage;

@SyncToClient
@Conversion(toManaged = "TO_VIEW", toField = "FROM_VIEW")
private MyStorage data = new MyStorage();
```

---

## 8. `@Strategy`

**源码**：`Strategy.java` ｜ **作用**：自定义变更检测的相等性

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface Strategy {
    String value();  // 同声明类的 static Hash.Strategy<T> 字段名
}
```

### 默认行为

无 `@Strategy` 且无类型级注册时，使用 `DataFieldDefinition.OBJECT_STRATEGY`
（标准 `hashCode()` / `equals()`）。

### 三级查找顺序

```
1. 字段上的 @Strategy（最高优先级）
2. FieldDefinitionStorage.registerStrategy(Class, Strategy) 注册的类型级策略
3. DataFieldDefinition.OBJECT_STRATEGY（默认）
```

### 内置已注册策略（`DataSyncLib` 主类构造期）

| 类型 | 策略 |
| --- | --- |
| `ItemStack` | `ItemStackHashStrategy` |
| `FluidStack` | `FluidStackHashStrategy` |
| `ItemStack[]` | `ItemStackArrayHashStrategy` |
| `FluidStack[]` | `FluidStackArrayHashStrategy` |

因此这些类型的字段**开箱即用**，无需 `@Strategy`。

### 数组字段的特殊要求

数组策略必须针对**数组类型**，用 `HashUtil.arrayStrategy(elementStrategy)` 派生：

```java
public static final Hash.Strategy<MyKey[]> KEYS = HashUtil.arrayStrategy(MyKeyHashStrategy.ALL);

@Strategy("KEYS")
private MyKey[] keys = new MyKey[4];
```

### 自定义策略实现

```java
public static final Hash.Strategy<ItemStack> ITEM_ONLY = new Hash.Strategy<>() {
    public int hashCode(ItemStack s) { return Item.getId(s.getItem()); }
    public boolean equals(ItemStack a, ItemStack b) {
        return (a == b) || (a != null && b != null && a.getItem() == b.getItem());
    }
};

@Strategy("ITEM_ONLY")
@SyncToClient
private ItemStack stack;
```

### 字段查找规则

- `getDeclaredField` → **不搜父类**。
- 任意访问修饰符均可（框架 `setAccessible(true)`）。
- 字段必须 `static`。

---

## 9. `@Generic`

**源码**：`Generic.java` ｜ **作用**：强制走泛型工厂链

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface Generic {}
```

### 默认解析顺序 vs `@Generic`

```
默认：FIELDS 缓存（精确类型）→ 失败才 → GENERIC 缓存（泛型）
@Generic：直接走 GENERIC 缓存
```

### 适用场景

| 场景 | 是否需要 |
| --- | --- |
| `List<String> names`（显式泛型） | ❌ 默认就能解析 |
| `List names`（裸类型，但想按 `List<String>` 处理） | ✅ |
| 类型有多个泛型参数，需明确实参 | ✅ |

### 约束

字段必须能解析出泛型实参，否则扫描期抛异常。
泛型编码查 `DataSyncCodec.get(Class, Class...)`，走 `GENERIC_CODECS`
（按泛型实参**身份**匹配，需传与声明一致的 `Class` 实例）。

---

## 10. `@AddToManager`

**源码**：`AddToManager.java` ｜ **作用**：纳入管理，不自动同步/增量存盘

```java
@Retention(RUNTIME) @Target(FIELD)
public @interface AddToManager {}
```

### 精确能力边界

| 操作 | 是否参与 |
| --- | --- |
| `FieldDefinitionStorage.allDefinitions` | ✅ |
| `definitionMap` / `typeDefinitions` 查询 | ✅ |
| `writeAllToValue` / `readAllFromValue` | ✅ |
| `writeToValue` / `readFromValue`（增量存盘） | ❌ |
| `writeToNetworkBuffer` / `readFromNetworkBuffer` | ❌ |
| `markFieldsForSync` | ✅（能标脏，但没有同步路径消费它） |

### 用途

1. **`FieldDataCodec` 的 POJO 内部字段**：`FieldDataCodec` 用 `writeAllToValue`，
   所以内部只需 `@AddToManager`。
2. **全量导出/导入**：希望字段参与 `writeAll` 但不污染自动流程。
3. **复用 codec**：让某字段被管理（能反查定义）但不需要自动行为。

### 实例

```java
// src/main/java/com/gto/datasynclib/test/TestBlockEntity.java:467
static class A {
    @AddToManager
    int a;
}
// A 由 A_CODEC（FieldDataCodec）整体编解码
```

---

## 11. `@RemoteCall`

**源码**：`RemoteCall.java` ｜ **作用**：方法级跨端调用

```java
@Retention(RUNTIME) @Target(METHOD)
public @interface RemoteCall {
    String value() default "";                    // 线上方法名
    LogicalSide side() default LogicalSide.BOTH;  // 允许执行的端
}
```

### 完整规则表

| 规则 | 细节 |
| --- | --- |
| 返回类型 | 必须 `void`；有返回值 → 类扫描时**拒绝** |
| 白名单 | 只有注解的方法可达；未注解的永远不可调 |
| 线上名唯一性 | 每类唯一；有重载必须显式 `value = "..."` |
| 参数 codec | 每个参数类型都必须有 `DataSyncCodec`（走网络半） |
| 端限制 | `side` 不符 → **执行前**拒绝 |
| 静态方法 | 支持（无需目标对象） |
| 父类方法 | 支持（扫描类层级） |
| 接口 default | 支持（扫描接口） |
| 嵌套 holder | 支持（按扁平树路径索引寻址） |
| 重写 | 类方法覆盖接口 default（Java 优先级） |
| 异常 | 记录日志，不传播 |
| 来源校验 | **无**（框架不校验发送者身份） |

### 寻址机制

```
payload = [目标节点索引 VarInt][方法索引 VarInt][参数...]
```

- **目标节点索引**：调用目标在**被寻址对象的扁平 holder 树**中的位置。
  `0` = 被寻址对象自身。
- **方法索引**：该类排序后的方法名表下标。
- 两级索引让「嵌套 holder 上的方法」可直达，调用方无需知道自己嵌在第几层。

### 端判定依据

调用到达时 `side` 取自**包寻址对象的 level**：
- block entity / entity 在某个 level → 有端；
- 目标不在 level（如单元测试）→ 无端，跳过检查。

### 发送 API 与接收 API

```java
// 编码
byte[] p = FieldDataManager.writeRemoteCall(root, target, "method", args);
byte[] p = holder.writeRemoteCall(target, "method", args);       // 便捷：this 为 root
byte[] p = RemoteInvoker.write(target, "method", args);          // 从 target 自行找 root

// 发送（内置通道）
DataSyncNetwork.callBlockEntityOnServer(be, "method", args);
DataSyncNetwork.callBlockEntityOnClients(be, "method", args);
DataSyncNetwork.callEntityOnServer(e, "method", args);
DataSyncNetwork.callEntityOnClients(e, "method", args);

// 接收（RemoteNetwork 自动调用）
IFieldDataHolder.handleRemoteCall(target, payload);
IFieldDataHolder.handleRemoteCall(target, payload, side);        // 显式端
```

`writeRemoteCall` 会**先校验**：target 在 root 树内、方法在白名单、参数匹配。
所以拼错方法名在发送端就失败。

### 实例

```java
@RemoteCall
public void requestUpgrade(int tier) { ... }                 // BOTH

@RemoteCall(side = LogicalSide.CLIENT)
public void onStructureFormedClient() { ... }                // 仅客户端

@RemoteCall(value = "configure", side = LogicalSide.SERVER)
private void configure(String key, int value) { ... }        // 仅服务端，线上名 configure
```

### GTOCore 现状

主仓使用数 **0**（GTCEu 有自有网络体系）。`RemoteNetwork.init()` 已由
DataSyncLib 主类调用，无需重复注册。

---

## 回调方法签名总表

所有回调都要求：**非静态**、声明在**字段所在类**（`getDeclaredField`/`getDeclaredMethod` 语义）、
参数类型**精确匹配**声明类型。

| 注解属性 | 触发时机 | 签名 |
| --- | --- | --- |
| `@SyncToClient.listener` | 网络接收并应用后 | `(T newValue, T oldValue) -> void` |
| `@SyncToServer.listener` | 同上（C2S 方向） | `(T newValue, T oldValue) -> void` |
| `@SaveToDisk.listener` | 磁盘加载后（键存在时） | `(T value) -> void` |
| `@SyncToClient.skipWhen` | 发送前 | `(T value) -> boolean`（true = 跳过） |
| `@SyncToServer.skipWhen` | 同上 | `(T value) -> boolean` |
| `@SaveToDisk.skipWhen` | 写盘前 | `(T value) -> boolean`（true = 跳过） |
| `@SaveToDisk.defaultValueGetter` | 写盘前 | `() -> T` |

### 接口钩子（`IFieldDataHolder`）

| 方法 | 时机 | 签名 |
| --- | --- | --- |
| `scheduleUpdate` | 收到 `scheduleUpdate=true` 字段后 | `(LogicalSide) -> void` |
| `writeCustomSyncData` | 字段写入网络前 | `(FriendlyByteBuf, boolean writeAll)` |
| `readCustomSyncData` | 字段读取网络前 | `(FriendlyByteBuf)` |
| `writeCustomSaveData` | 字段写入存档 map 前 | `(Map<String,Object>, ValueOps)` |
| `readCustomSaveData` | 字段读取存档 map 后 | `(Map<String,Object>, ValueOps)` |

---

## 参数速查总表

| 注解 | 参数 | 类型 | 默认 |
| --- | --- | --- | --- |
| `@SyncToClient` / `@SyncToServer` | `scheduleUpdate` | `boolean` | `false` |
| | `autoDetect` | `boolean` | `true` |
| | `skipWhen` | `String` | `""` |
| | `listener` | `String` | `""` |
| `@SaveToDisk` | `key` | `String` | `""` |
| | `skipWhen` | `String` | `""` |
| | `saveEmpty` | `boolean` | `false` |
| | `defaultValue` | `String` | `""` |
| | `defaultValueGetter` | `String` | `""` |
| | `listener` | `String` | `""` |
| `@Access` | `instanceAsValue` | `boolean` | `false` |
| `@AdditionalHolder` | `childManager` | `boolean` | `false` |
| `@Codec` | `saveCodec` / `syncCodec` / `writeToValue` / `readFromValue` / `writeToBuffer` / `readFromBuffer` | `String` | `""` |
| `@Conversion` | `toManaged`（必填） | `String` | — |
| | `toField` | `String` | `""` |
| `@Strategy` | `value`（必填） | `String` | — |
| `@RemoteCall` | `value` | `String` | `""`（用 Java 方法名） |
| | `side` | `LogicalSide` | `BOTH` |
| `@Generic` / `@AddToManager` | 无参数 | — | — |
