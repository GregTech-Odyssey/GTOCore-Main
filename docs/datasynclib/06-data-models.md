# 06 · 数据模型参考

> [← 返回 00 概要](00-overview.md)

> 三个成组数据设施：`Registry`（稳定 id 分配）、`DataComponent`（身份键组件）、
> `FileUtil`（版本化文件）。

---

## 1. `Registry`

**源码**：`util/Registry.java`

### 1.1 用途

给「一组有限的值」分配**稳定的整数 id**：
- 网络传 id（紧凑，1~2 字节）；
- 磁盘传 key（自描述、抗改序）。

### 1.2 类型参数与构造

```java
public class Registry<K extends Comparable<K>, V> implements Iterable<V> {
    // 3 参：不自动注册全局 codec
    public Registry(String name, ValueCodec<K> keyCodec, Function<? super V, ? extends K> keyGetter);
    // 4 参：freeze() 时自动注册全局 codec
    public Registry(String name, ValueCodec<K> keyCodec,
                    @Nullable Function<? super V, ? extends K> keyGetter,
                    @Nullable Class<V> valueType);
}
```

| 参数 | 作用 |
| --- | --- |
| `name` | 注册表名（用于异常信息与 `DataComponentRegistry` 的派生名） |
| `keyCodec` | key 的 `ValueCodec`（如 `ValueCodec.STRING`） |
| `keyGetter` | 从值反查 key；`null` 时走内部反查 map |
| `valueType` | **值的运行时类型**；非 null 则 `freeze()` 自动 `DataSyncCodec.register` |

**`K` 必须 `Comparable`**：`freeze()` 按 key 排序分配 id。

### 1.3 生命周期

```
构造（frozen = true）
  │
  ├─ unfreeze()   → 清空，frozen = false（可注册）
  │     │
  │     ├─ register(key, value)
  │     └─ replace(key, value)
  │
  └─ freeze()     → 按 key 排序 → 分配 0..n-1 → frozen = true
                    └─ 若 valueType 非 null：registerToGlobalCodecs()
```

**约束**：

| 操作 | 前置状态 | 违规行为 |
| --- | --- | --- |
| `unfreeze()` | 已 frozen | 抛 `IllegalStateException("already unfrozen")` |
| `freeze()` | 未 frozen | 抛 `IllegalStateException("already frozen")` |
| `register(key, v)` | 未 frozen | 抛 `IllegalStateException("has been frozen")` |
| `register(key, v)` | key 不存在 | key 已存在 → 抛 `IllegalStateException` |
| `replace(key, v)` | 未 frozen 且 key 存在 | key 不存在 → 抛 `IllegalStateException` |

> `register`/`replace` 同步；`freeze`/`unfreeze` **不同步**（应在单线程初始化期调用）。

### 1.4 id 分配规则

```java
protected void build() {
    var list = new ArrayList<>(keyValues.entrySet());
    list.sort(Map.Entry.comparingByKey());       // ← 按 key 自然序
    for (int i = 0; i < size; i++) {
        valueKeys.put(v, new IntObjectHolder<>(i, key));   // 值 → (id, key)
        idValues.set(i, v);                                // id → 值
    }
}
```

**同一组 key → 永远相同的 id 分配**。这是网络兼容性的基础。

### 1.5 完整 API

#### 状态与生命周期

```java
String getName();
boolean isFrozen();
void unfreeze();
void freeze();
boolean isContextValid();        // 默认 true，子类可覆盖以限制上下文
void registerToGlobalCodecs();   // 手动触发全局注册（valueType 非 null 时有效）
```

#### 注册与修改

```java
<T extends V> T register(K key, T value);
<T extends V> T replace(K key, T value);
```

#### 查询

```java
boolean containsKey(K key);
boolean containsValue(V value);      // 身份查找
V get(int id);
V get(K key);
V getOrDefault(K key, V defaultValue);
int getId(V value);
K getKey(V value);
Set<V> values();                     // id 顺序（live view）
Set<K> keys();                       // 插入顺序（live view）
```

#### 迭代

```java
Iterator<V> iterator();              // id 顺序
void forEach(Consumer<? super V>);   // id 顺序
Spliterator<V> spliterator();
void forEachKey(Consumer<? super K>);        // 插入顺序
void forEachKeyValue(BiConsumer<? super K, ? super V>);   // 插入顺序
```

#### 三种 codec

```java
StreamCodec<FriendlyByteBuf, V> streamCodec();   // 按整数 id（VarInt）
ValueCodec<V> valueCodec();                      // 按 key（自描述）
DataSyncCodec<V> combinedCodec();                // 两者组合
Codec<V> codec(Codec<K> keyCodec);               // Mojang DFU（key xmap）
```

| codec | 编码内容 | 适用 |
| --- | --- | --- |
| `streamCodec()` | `writeVarInt(getId(v))` | 网络（紧凑，要求两端注册表一致） |
| `valueCodec()` | `keyCodec.encode(keyGetter(v))` | 磁盘（改序不破坏、可读） |
| `combinedCodec()` | 组合 | 需要 `DataSyncCodec` 的地方 |
| `codec(keyCodec)` | `keyCodec.xmap(keyValues::get, this::getKey)` | Mojang DFU 集成 |

### 1.6 全局 codec 自动注册

**触发**：`freeze()` 时 `valueType != null` → 调 `registerToGlobalCodecs()`。

```java
public final void registerToGlobalCodecs() {
    if (valueType == null) return;
    DataSyncCodec.register(valueType, streamCodec(), valueCodec());
}
```

**效果**：之后任何 `valueType` 类型的字段都能自动编解码，无需手动注册。

**注意**：

| 点 | 说明 |
| --- | --- |
| 只能在 `freeze()` 后 | 否则 id 未分配，codec 无意义 |
| 只注册一次 | 再次 `freeze()` 会抛异常（已 frozen） |
| 3 参构造器不触发 | `valueType` 为 null |

### 1.7 线程安全

| 成员 | 保护 |
| --- | --- |
| `frozen` | `volatile` |
| `register` / `replace` | `synchronized (this)` |
| `freeze` / `unfreeze` | **无同步**（应在拥有线程调用） |
| 冻结后的读操作 | 无锁 |

### 1.8 `Phase` 枚举

```java
public enum Phase { PRE, OPEN, CLOSED, FROZEN }
```

**声明但当前未使用**（状态只用 `frozen` 布尔表达）。保留给未来的四态生命周期。

### 1.9 实例（GTOCore）

```java
// src/main/java/com/gtocore/api/research/ResearchTag.java
public static final Registry<String, ResearchTag> TAGS = createRegistry();

private static Registry<String, ResearchTag> createRegistry() {
    var registry = new Registry<String, ResearchTag>("research_tag", ValueCodec.STRING, t -> t.name);
    registry.unfreeze();
    return registry;
}

public ResearchTag(String name, ...) {
    ...
    TAGS.register(name, this);       // 构造即注册
}

public static final ResearchTag MATERIAL = new ResearchTag("material", ...);
// ... 16 个常量

static {
    TAGS.freeze();                   // 类初始化末尾冻结
}
```

取出两个半侧并注册：

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

> 这里用了 **3 参构造器 + 显式 `DataSyncCodec.register`**，而不是 4 参自动注册。
> 两种都可行；显式注册让注册点集中在 `GTOCodecs`，便于审计。

### 1.10 已知陷阱

| 陷阱 | 后果 | 避免 |
| --- | --- | --- |
| `freeze()` 后新增常量 | `register` 抛异常 | 常量必须在 `static {}` **之前**声明完 |
| 新增 key 改变排序 | **所有后续 id 位移** → 网络不兼容 | 服务端/客户端必须同步更新 |
| 用 4 参构造器但期望手动注册 | 自动注册已发生 | 二选一，不要重复 |
| `keyGetter` 返回 null | `valueCodec` 编码时 NPE | 保证 keyGetter 对已注册值非 null |

---

## 2. `DataComponent` 三件套

**源码**：`datastream/`

一套**基于身份（identity）的组件模型**：对象挂若干可选数据块，每块可独立合并。

### 2.1 三者关系

| 类 | 角色 | 关键特性 |
| --- | --- | --- |
| `DataComponentKey<T>` | 键（名字 + codec + 合并/创建函数） | 身份哈希；同名字不同注册表的键互不相等 |
| `DataComponentMap` | 键 → 值的 map | 类型安全访问器 + 合并语义 |
| `DataComponentRegistry` | 键的注册表 | **同时是 `DataComponentMap` 的 codec**（Stream + Value + Mojang） |

### 2.2 `DataComponentKey<T>`

```java
public class DataComponentKey<T> {
    public final String name;
    public final DataSyncCodec<T> codec;
    public final int mixCode;                        // = HashCommon.mix(identityHashCode(this))
    public final BiFunction<T, T, T> mergeFunction;
    public final Function<T, T> createFunction;

    public static <T> DataComponentKey<T> createNoCodec(String name);
    public static <T> DataComponentKey<T> create(String name, Consumer<Builder<T>> builder);

    // 30+ 预置 builder（见 §2.5）
}
```

#### 身份哈希的意义

`mixCode` 由 `System.identityHashCode(this)` 混合而来。`DataKey2ObjectMap` 用它做键查找，
因此：

```java
DataComponentKey<Integer> k1 = DataComponentKey.createNoCodec("x");
DataComponentKey<Integer> k2 = DataComponentKey.createNoCodec("x");
k1 != k2          // 同名但不同实例 → 不同键
```

**用途**：多注册表共存时，同名键不会互相串。

#### 合并与创建函数

| 函数 | 何时调用 | 默认 |
| --- | --- | --- |
| `mergeFunction(old, new) -> merged` | 合并两个 component map 时 | `(o, n) -> n`（新覆盖旧） |
| `createFunction(value) -> storedCopy` | 存储前 | `n -> n`（原样） |

`createFunction` 常用于**防御性拷贝**（集合/Map）。

### 2.3 `DataComponentMap`

```java
public final class DataComponentMap extends DataKey2ObjectMap<DataComponentKey<?>, Object> {
    public DataComponentMap();
    public DataComponentMap(int size);
    public DataComponentMap(DataComponentMap map);      // 拷贝构造

    public void fastForEach(BiConsumer<DataComponentKey, Object> consumer);

    public <T> T getData(DataComponentKey<T> key);
    public <T> T getOrDefaultData(DataComponentKey<T> key, T defaultValue);
    public <T> T getOrCreateData(DataComponentKey<T> key, Supplier<T> supplier);
    public <T> T getOrPut(DataComponentKey<T> key, T value);
    public <T> T merge(DataComponentKey<T> key, T value);       // 用 key 的 merge/create
    public void merge(DataComponentMap other);                  // 批量

    // 类型便利访问器（缺失时返回安全默认值）
    public boolean getBoolean(DataComponentKey<Boolean> key);   // 默认 false
    public int getInt(DataComponentKey<Integer> key);           // 默认 0
    public double getDouble(...);  public float getFloat(...);
    public long getLong(...);      public String getString(...); // 默认 ""
}
```

#### 合并语义

| 方法 | 行为 |
| --- | --- |
| `merge(key, value)` | 键已存在 → `mergeFunction(old, value)`；不存在 → `createFunction(value)` |
| `merge(otherMap)` | 对 other 的每项执行上面逻辑 |

### 2.4 `DataComponentRegistry`

```java
public final class DataComponentRegistry
        extends Registry<String, DataComponentKey<?>>
        implements CombinedCodec<DataComponentMap> {

    public DataComponentRegistry(String name);

    public <T> DataComponentKey<T> register(String name, DataSyncCodec<T> codec);
    public <T> DataComponentKey<T> register(String name, Consumer<DataComponentKey.Builder<T>> builder);
    public <T, K extends DataComponentKey<T>> K register(K key);

    public <T extends DataComponentKey<?>> T getDataComponentKey(String name);
    public <T extends DataComponentKey<?>> T getDataComponentKey(int id);

    // CombinedCodec<DataComponentMap>
    public void encode(FriendlyByteBuf buf, DataComponentMap obj);
    public DataComponentMap decode(FriendlyByteBuf buf);
    public Object encode(ValueOps ops, DataComponentMap obj);
    public DataComponentMap decode(ValueOps ops, Object d);
}
```

#### 编码规则

| 点 | 行为 |
| --- | --- |
| 无 codec 的键 | **静默跳过** |
| 每项格式 | 长度前缀（便于未知键安全跳过） |
| 未知键 | 解码时跳过，不破坏 buffer 位置 |

#### 三个 codec 接口同时实现

| 接口 | 用途 |
| --- | --- |
| `StreamCodec<FriendlyByteBuf, DataComponentMap>` | 网络同步 |
| `ValueCodec<DataComponentMap>` | 磁盘（carrier string map） |
| `Codec<DataComponentMap>` | Mojang DFU |

### 2.5 预置 builder 全表

`DataComponentKey` 提供 30+ 静态工厂，为常见 FastUtil 类型预配置合并/创建行为。

| 类别 | builder |
| --- | --- |
| 通用 | `collectionBuilder`、`mapBuilder` |
| `IntSet` | `intSetBuilder` |
| `Int2*Map` | `int2IntMapBuilder`、`int2LongMapBuilder`、`int2ObjectMapBuilder`、`int2FloatMapBuilder`、`int2DoubleMapBuilder`、`int2BooleanMapBuilder`、`int2CharMapBuilder`、`int2ShortMapBuilder`、`int2ByteMapBuilder` |
| `LongSet` | `longSetBuilder` |
| `Long2*Map` | `long2IntMapBuilder`、`long2LongMapBuilder`、`long2ObjectMapBuilder`、`long2FloatMapBuilder`、`long2DoubleMapBuilder`、`long2BooleanMapBuilder`、`long2CharMapBuilder`、`long2ShortMapBuilder`、`long2ByteMapBuilder` |
| `DoubleSet` | `doubleSetBuilder` |
| `Double2*Map` | `double2IntMapBuilder`、`double2LongMapBuilder`、`double2ObjectMapBuilder`、`double2FloatMapBuilder`、`double2DoubleMapBuilder`、`double2BooleanMapBuilder`、`double2CharMapBuilder`、`double2ShortMapBuilder`、`double2ByteMapBuilder` |
| `FloatSet` | `floatSetBuilder` |
| `Float2*Map` | （同上家族） |

每个 builder 的参数是 `Function<C, C> createFunction`（防御性拷贝）。

### 2.6 与字段注解的关系

**两套独立机制**：

| | 注解字段 | DataComponent |
| --- | --- | --- |
| 形态 | 固定 schema 的字段集合 | 动态可选的组件集合 |
| 驱动 | 框架自动扫描 | 手动编解码 |
| 声明 | 类上注解字段 | 运行期组装的 map |
| 适用 | 常规状态 | 可选的、按需出现的附加数据 |

**接入方式**：把 `DataComponentRegistry` 当 codec 用，在 `writeCustomSyncData` /
`writeCustomSaveData` 里编解码：

```java
@Override
public void writeCustomSaveData(Map<String, Object> data, ValueOps ops) {
    data.put("components", COMPONENT_REGISTRY.encode(ops, myComponents));
}

@Override
public void readCustomSaveData(Map<String, Object> data, ValueOps ops) {
    if (data.containsKey("components"))
        myComponents = COMPONENT_REGISTRY.decode(ops, data.get("components"));
}
```

GTOCore 主仓未使用该模块。

---

## 3. 版本化文件

**源码**：`util/FileUtil.java`

### 3.1 API

```java
public static final int MAX_VERSION = 1 << 16;      // 65536

// 带版本头
public static <T> void writeVersioned(Path path, int version, StreamCodec<...> codec, T value);
public static <T> void writeVersioned(Path path, int version, ValueCodec<T> codec, T value);
public static <T> T readVersioned(Path path, StreamCodec<...> codec);
public static <T> T readVersioned(Path path, ValueCodec<T> codec);

// 无版本头
public static <T> T read(Path path, ...codec);
public static <T> void write(Path path, ...codec, T value);
```

### 3.2 文件布局

```
[VarInt version] [payload]
```

- 版本必须在 `1 .. MAX_VERSION`；越界抛 `IllegalArgumentException`。
- **`0` 是本库永不写出的值** → 用于识别「版本号机制之前」的旧文件。

### 3.3 编解码器看到的版本

| codec 类型 | 取版本方式 |
| --- | --- |
| `StreamCodec` | 参数是 `VersionedFriendlyByteBuf`，用 `buf.version()` |
| `ValueCodec` | 用 `ops.dataVersion()` |

### 3.4 旧格式回退模板

```java
T value;
try {
    value = FileUtil.readVersioned(path, codec);
} catch (RuntimeException e) {
    value = FileUtil.read(path, legacyCodec);      // 版本号机制之前写的
}
```

**迁移策略**：
1. 从此写带版本号的；
2. 旧读取放进 `catch`；
3. 等没有文件再需要它时删掉旧 codec。

### 3.5 何时**不**加版本头

| 文件类型 | 原因 |
| --- | --- |
| 随包资源 | 与游戏版本绑定，不需要用户侧迁移 |
| 给人看的导出文本 | 格式对外可见，版本头污染 |
| 可丢弃的缓存 | 直接重建即可 |

> 判据：**这个文件的字节是不是本库自己写的、且需要在版本间读回来**。
> 只有这种才加版本头。

### 3.6 `VersionedFriendlyByteBuf`

```java
public class VersionedFriendlyByteBuf extends FriendlyByteBuf {
    public int version();
}
```

`FileUtil.writeVersioned` 内部用它包装 buffer，让 stream codec 能读到写入版本。

---

## 4. 决策清单

| 你的需求 | 用哪个 |
| --- | --- |
| 一组固定值，要紧凑网络 id | `Registry`（4 参构造 + `freeze` 自动注册） |
| 一组固定值，想显式控制注册点 | `Registry`（3 参 + 手动 `DataSyncCodec.register`） |
| 动态可选的数据块，各块合并规则不同 | `DataComponentKey` + `Map` + `Registry` |
| 写一个自定义文件格式 | `FileUtil.writeVersioned` |
| 读一个可能是旧格式的文件 | `readVersioned` + `catch` 回退 |
| NBT / JSON 作为值 | `NbtUtil` / `JsonUtils` 的 `CustomTypes.Type` |

---

## 5. GTOCore 使用现状

| 模块 | 使用情况 |
| --- | --- |
| `Registry` | ✅ `ResearchTag.TAGS`（16 个研究标签） |
| `DataComponent*` | ❌ 未使用 |
| `FileUtil` | ❌ 主仓未直接使用（GTCEu 侧有 `GTDataFixer.VERSION` 作为数据版本常量） |
