# 04 · 扩展点参考

> [← 返回 00 概要](00-overview.md)

> 决定「框架认识什么」的四层注册体系：字段工厂（值模式/访问模式/泛型）+ 哈希策略。
> 全部入口在 `FieldDefinitionStorage`（`FieldDefinitionStorage.java`）。
>
> **通用前提**：所有注册必须在**该类型的字段被首次扫描之前**完成。
> 解析结果按类型缓存在 `ClassValue` / map 中，晚到的注册会被忽略。

---

## 1. 四层解析链

字段扫描时按顺序尝试四个层级。任一命中即停止。

```
层级 1  EXACT_ACCESS     精确类型（访问模式）      ← 优先级最高
层级 2  EXACT_FIELDS     精确类型（值模式）
层级 3  FIELDS           值模式谓词链（按 priority 降序）
层级 4  ACCESS           访问模式谓词链（按 priority 降序）
层级 5  GENERIC_FIELDS / GENERIC_FIELDS_CACHE   泛型（`@Generic` 或精确层失败时）
```

> 精确层（EXACT）永远胜过谓词层。这解释了为什么 `BlockPos` 有自己的 codec 时
> 不会被 `Vec3i` 的谓词抢走——两者都是精确注册，按类型精确匹配。

### 各层的数据结构

| 层 | 结构 | 匹配方式 |
| --- | --- | --- |
| `EXACT_ACCESS` | `Map<Class, Factory>` | 精确类相等 |
| `EXACT_FIELDS` | `Map<Class, Factory>` | 精确类相等 |
| `FIELDS` | `List<CustomFactory>`（按 priority 降序） | 谓词 `Predicate<Class>` |
| `ACCESS` | 同上 | 谓词 |
| `GENERIC_FIELDS_CACHE` | `Map<rawClass, Map<泛型实参wrapper, Factory>>` | 泛型实参**身份**匹配 |
| `GENERIC_FIELDS` | `List<CustomGenericFactory>`（降序） | `BiPredicate<rawClass, Class[]>` |

---

## 2. 值模式工厂注册（`register*Factory`）

值模式的字段实现基类是 `AbstractField` / `ObjField`。

### 2.1 精确类型

```java
public static <T> void registerFactory(Class<T> type, DataField.Factory<T> factory);
```

**用途**：为某个具体类型指定专用 `DataField`，绕过所有谓词。

```java
FieldDefinitionStorage.registerFactory(MyType.class, def -> new MyCustomField(def));
```

**优先级**：高于谓词链，也高于泛型与访问层。

### 2.2 接口 / 超类匹配

```java
public static <T> void registerInterfaceFactory(
        Class<T> interfaceType,
        Function<Class<?>, DataField.Factory<T>> factory,
        int priority);
```

内部转调 `registerCustomFactory(interfaceType::isAssignableFrom, ...)`。

```java
FieldDefinitionStorage.registerInterfaceFactory(IMySerializable.class,
        concreteType -> def -> new MySerializableField(def, concreteType), 200);
```

**注意**：`factory` 是 `Function<Class<?>, Factory<T>>`——接收**具体字段类型**，
返回该类型的工厂。这样同一注册能服务整个接口家族。

### 2.3 任意谓词

```java
public static <T> void registerCustomFactory(
        Predicate<Class<?>> type,
        Function<Class<?>, DataField.Factory<T>> factory,
        int priority);
```

**用途**：`registerCustomFactory` 是「让自定义类型可序列化」的主入口。
默认库已注册「有 codec 的对象」在 **priority 100**。

```java
FieldDefinitionStorage.registerCustomFactory(
        cls -> cls.isAnnotationPresent(MyMarker.class),
        cls -> def -> new MyField(def),
        150);          // > 100，先于默认的 codec 谓词
```

### 优先级语义

| 值 | 含义 |
| --- | --- |
| 越高 | 越先被检查 |
| 相同 | 保持注册顺序（`List.sort` 稳定） |
| 内置参考 | `IntCollection` = 1000，`Collection` = 100 |

内置注册（`DataSyncLib` 主类构造期）：

| 类型 / 谓词 | 层级 | 优先级 |
| --- | --- | --- |
| 原始类型（`int.class` …） | EXACT_FIELDS | — |
| `IntCollection` / `LongCollection` | ACCESS | 1000 |
| FastUtil `Object2IntMap` 等 | ACCESS | 高 |
| `IFieldDataHolder` | ACCESS | 高 |
| `IDataSerializable` | ACCESS | 高 |
| Forge `INBTSerializable` | ACCESS | 高 |
| `Collection` / `Map` / 数组 | ACCESS | 100 左右 |
| 有 codec 的对象 | FIELDS | 100 |

---

## 3. 访问模式工厂注册（`register*AccessFactory`）

访问模式的字段实现基类是 `AbstractFieldAccess`。

| 方法 | 匹配方式 |
| --- | --- |
| `registerAccessFactory(Class, Factory)` | 精确类型 |
| `registerAccessInterfaceFactory(Class, Function, int)` | 接口/超类 |
| `registerAccessCustomFactory(Predicate, Function, int)` | 任意谓词 |

**语义与值模式完全对称**，只是落在访问层。

### 何时选访问层

```
字段是容器（就地修改语义）？
├─ 是 → 访问层（registerAccess*）
└─ 否 → 值层（register*）
```

### 实例：给自定义容器加访问器

```java
// 一个自定义的缓存容器，内容就地变化
public final class MyCache<K, V> {
    private final Map<K, V> map = new HashMap<>();
    public void put(K k, V v) { map.put(k, v); }
    public Map<K, V> view() { return map; }
}

FieldDefinitionStorage.registerAccessCustomFactory(
        cls -> MyCache.class.isAssignableFrom(cls),
        cls -> def -> new MyCacheAccess(def),
        500);
```

---

## 4. 泛型工厂注册

### 4.1 精确泛型

```java
public static <T> void registerGenericFactory(
        Class<?> type, DataField.Factory<T> factory, Class<?>... genericType);
```

**用途**：为 `MyType<String, Integer>` 这类参数化类型精确注册。

**匹配方式**：泛型实参按**身份**（`==`）比对，不是 `equals`。
必须传与字段声明中**相同的 `Class` 实例**（Class 对象天然单例，正常写法即可）。

```java
// 对应字段声明：MyType<String, Integer> field;
FieldDefinitionStorage.registerGenericFactory(
        MyType.class, def -> new MyGenericField(def),
        String.class, Integer.class);
```

### 4.2 谓词泛型

```java
public static <T> void registerCustomGenericFactory(
        BiPredicate<Class<?>, Class<?>[]> type,
        BiFunction<Class<?>, Class<?>[], DataField.Factory<T>> factory,
        int priority);
```

**用途**：无法精确枚举的泛型家族（如任意 `Map<K,V>`、任意 `List<T>`）。

```java
FieldDefinitionStorage.registerCustomGenericFactory(
        (raw, args) -> Map.class.isAssignableFrom(raw) && args.length == 2,
        (raw, args) -> def -> new MyMapField(def, args[0], args[1]),
        200);
```

### 与 `@Generic` 的关系

| 情况 | 走哪条 |
| --- | --- |
| 显式泛型字段（`List<String>`） | 精确层失败 → 泛型层自动 |
| 裸类型 + `@Generic` | **直接**走泛型层 |
| 裸类型无 `@Generic` | 只走精确层（可能失败） |

---

## 5. 泛型解析与 `@Generic` 注解

### 5.1 `ReflectUtil` 的解析能力

| 方法 | 作用 |
| --- | --- |
| `getFieldGenericType(Type)` | 取字段泛型实参（`Type[]`） |
| `getFieldGenericTypeClasses(Type)` | 同上，转成 `Class<?>[]` |
| `getRawType(Type)` | 取原始类 |
| `getResolvedSuperType(fieldType, targetRaw)` | 沿父类/接口解析到目标类型的参数化形态 |
| `getResolvedGenericArguments(fieldType, targetRaw)` | 取解析后的实参 `Class<?>[]` |
| `resolveSuperTypeBindings(slot, raw, out)` | 累积类型变量绑定 |
| `resolveType(type, bindings)` | 用绑定替换类型变量 |

### 5.2 多层继承解析示例

```java
// class A<T> extends HashMap<String, T>
// 字段 A<Integer> value;
Type ft = A.class.getDeclaredField("value").getGenericType();
Class<?>[] args = ReflectUtil.getResolvedGenericArguments(ft, HashMap.class);
// → [String.class, Integer.class]
```

支持：父类链、泛型接口、多层继承、接口间接继承。

### 5.3 解析失败的行为

| 情况 | 结果 |
| --- | --- |
| 泛型实参含类型变量 `T` 且无法绑定 | `DataSyncCodec.get(null)` → `IllegalArgumentException` |
| `@Generic` 但泛型不可解析 | 扫描期抛异常 |
| 通配符 `?` | 同上 |

错误信息：`"Cannot resolve a codec for a null type (unresolved wildcard or type variable?)"`

---

## 6. 策略注册

### 6.1 API

```java
public static <T> void registerStrategy(Class<T> type, Hash.Strategy<T> strategy);
```

**用途**：为类型注册**默认**变更检测策略（不需要每字段写 `@Strategy`）。

### 6.2 查找优先级

```
1. 字段上的 @Strategy（最高）
2. STRATEGIES map（registerStrategy 注册）
3. DataFieldDefinition.OBJECT_STRATEGY（默认 equals/hashCode）
```

### 6.3 内置已注册

| 类型 | 策略 |
| --- | --- |
| `ItemStack` | `ItemStackHashStrategy` |
| `FluidStack` | `FluidStackHashStrategy` |
| `ItemStack[]` | `ItemStackArrayHashStrategy` |
| `FluidStack[]` | `FluidStackArrayHashStrategy` |

> **数组不继承元素策略**：数组字段查的是自己精确类型的策略。
> 所以 `ItemStack[]` 单独注册了一份。

### 6.4 为自定义数组派生策略

```java
// 元素策略 → 数组策略
Hash.Strategy<MyKey[]> keys = HashUtil.arrayStrategy(MyKeyHashStrategy.ALL);

FieldDefinitionStorage.registerStrategy(MyKey[].class, keys);
```

`HashUtil.arrayStrategy` 会正确处理：长度、逐元素、null 数组/元素。

### 6.5 其他 `HashUtil` 工具

| 成员 | 用途 |
| --- | --- |
| `ARRAY_IDENTITY_STRATEGY` | 按元素身份（`==`）比较的 `Object[]` 策略 |
| `arrayStrategy(elementStrategy)` | 由元素策略派生数组策略 |
| `arrayIdentityHashCode(Object[])` | 身份哈希 |
| `arrayIdentityEquals(Object[], Object[])` | 身份比较 |
| `arrayIdentityWrapper(T[])` / `arrayWrapper(T[])` | 把数组包成 `Supplier<T[]>`（用于 map 键） |

`arrayIdentityWrapper` 用于泛型实参 map 的键（`DataSyncCodec.GENERIC_CODECS`）。

---

## 7. 注册时机与顺序陷阱

### 7.1 为什么必须早

`FieldDefinitionStorage` 用 `ClassValue<FieldDefinitionStorage>` 缓存**每类**的扫描结果：

```java
protected FieldDefinitionStorage computeValue(Class<?> clazz) { ... }   // 只跑一次
```

字段的工厂在扫描时**一次性解析并固化**进 `DataFieldDefinition.factory`。
之后的注册**不会**影响已扫描的类。

### 7.2 正确的注册位置

```java
// ✅ 模组构造期（@Mod 构造 / FMLCommonSetupEvent 之前）
DataSyncCodec.register(MyType.class, STREAM, VALUE);
FieldDefinitionStorage.registerCustomFactory(...);
FieldDefinitionStorage.registerStrategy(MyType.class, MY_STRATEGY);
```

```java
// ❌ 太晚
// - 首次 tick 里
// - 某个机器的 onLoad 里（此时字段已扫描）
// - 收到第一个网络包时
```

### 7.3 库自身的注册顺序（参考）

`DataSyncLib` 主类构造期按此顺序：

```
1. DataSyncCodec 类初始化（所有 codec 常量注册进 CODECS）
2. DataSyncCodec.init()（primitive 别名）
3. DataSyncNetwork.init()
4. RemoteNetwork.init()
5. 原始类型 DataField 工厂注册（EXACT_FIELDS）
6. codec 对象工厂注册（FIELDS 谓词，priority 100）
7. 访问器工厂注册（ACCESS，含优先级）
8. 哈希策略注册（ItemStack / FluidStack / 数组）
9. 枚举 fixed 标记（LogicalSide、Direction）
10. 测试内容（仅开发环境）
```

> 下游模组应在**第 5 步开始之前**（即自己的构造期）完成注册，或至少在任何字段扫描之前。

### 7.4 注册的线程安全

| 方法族 | 同步方式 |
| --- | --- |
| `register*Factory`（精确） | `synchronized (EXACT_*)` |
| `register*Custom*Factory` | `synchronized (FIELDS/ACCESS)` + `sort` |
| `registerGenericFactory` | `synchronized (cache)` |
| `registerStrategy` | `synchronized (STRATEGIES)` |

**读侧无锁**。因此注册应集中在单线程初始化阶段完成。

---

## 8. `DataField.Factory` 与自定义 `DataField`

### 8.1 接口

```java
public interface DataField<T> {
    DataFieldDefinition<T> getDefinition();
    void markAsChanged(@NotNull Object source);
    boolean isChanged(@NotNull Object source);
    void clearChanged(@NotNull Object source);
    boolean detectChange(@NotNull LogicalSide side, @NotNull Object source, boolean autoDetectOnly);
    void writeToBuffer(@NotNull LogicalSide side, @NotNull Object source,
                       @NotNull FriendlyByteBuf data, boolean writeAll);
    void readFromBuffer(@NotNull LogicalSide side, @NotNull Object source,
                        @NotNull FriendlyByteBuf data);
    void readFromValue(@NotNull Object source, @NotNull Object value, @NotNull ValueOps ops);
    @NotNull Object writeToValue(@NotNull Object source, @NotNull ValueOps ops);

    Object NOT_PERSISTED = new Object();      // 哨兵：不写盘

    default boolean mustDetect() { return false; }

    interface Factory<T> {
        DataField<T> create(DataFieldDefinition definition);
    }

    record CustomFactory<T>(Predicate<Class<?>> predicate,
                            Function<Class<?>, Factory<T>> factory,
                            int priority) { }

    record CustomGenericFactory<T>(BiPredicate<Class<?>, Class<?>[]> predicate,
                                   BiFunction<Class<?>, Class<?>[], Factory<T>> factory,
                                   int priority) { }
}
```

### 8.2 `mustDetect()` 的语义

| 值 | 行为 |
| --- | --- |
| `false`（默认） | 字段既未标脏、又不满足 `autoDetect` → **完全跳过**，只能靠 `markAsChanged` |
| `true` | 每轮同步都尝试 `detectChange`，忽略 `autoDetect` |

**使用者**：容器访问器（`FieldDataHolderAccess` 等）——它们有自己的内部变更追踪，
必须每轮询问。

### 8.3 `writeToValue` 的三种返回

| 返回 | 含义 |
| --- | --- |
| 值本身 | 正常写入 |
| `null` | 显式 null（仅 `saveEmpty` 时写入 map） |
| `DataField.NOT_PERSISTED` | 什么都不写（被 skip/default/empty 拦下） |

判断用**身份比较**（`result == DataField.NOT_PERSISTED`）。

### 8.4 实现自定义 `DataField` 的最小骨架

```java
public class MyField<T> implements DataField<T> {
    private final DataFieldDefinition<T> def;
    private boolean changed;

    public MyField(DataFieldDefinition<T> def) { this.def = def; }

    @Override public DataFieldDefinition<T> getDefinition() { return def; }
    @Override public void markAsChanged(Object src) { changed = true; }
    @Override public boolean isChanged(Object src) { return changed; }
    @Override public void clearChanged(Object src) { changed = false; }

    @Override public boolean detectChange(LogicalSide side, Object src, boolean autoOnly) {
        return false;                     // 或自己的比对逻辑
    }

    @Override public void writeToBuffer(LogicalSide side, Object src,
                                        FriendlyByteBuf buf, boolean writeAll) {
        // 写 payload（索引由 manager 负责）
    }

    @Override public void readFromBuffer(LogicalSide side, Object src, FriendlyByteBuf buf) {
        // 读 payload；需要时调用 def.getSyncListener()
    }

    @Override public Object writeToValue(Object src, ValueOps ops) {
        if (def.skipSave(src, value)) return NOT_PERSISTED;
        return ops.createString(String.valueOf(value));
    }

    @Override public void readFromValue(Object src, Object data, ValueOps ops) {
        value = ops.getString(data);
    }
}
```

### 8.5 继承现成基类

多数情况下不必从零实现：

| 基类 | 适用 |
| --- | --- |
| `AbstractField<T>` | 值模式，含默认快照比对 |
| `ObjField<T>` | 对象值模式（hashCode 快筛 + strategy.equals） |
| `ObjCodecField<T>` | 用 `DataFieldDefinition` 的 codec |
| `CustomObjCodecField<T>` | 用**外部提供**的 codec |
| `AbstractFieldAccess<C>` | 访问模式基类（容器） |
| `ArrayAccess<E,C>` | 数组访问模式基类 |
| `{Boolean,Byte,Short,Char,Int,Long,Float,Double}Field` | 原始类型值模式 |
| `{Boolean,Byte,Short,Char,Int,Long,Float,Double}ArrayAccess` | 原始数组访问模式 |

---

## 9. 已注册的访问器与服务覆盖范围

### 9.1 集合与 Map

| 字段类型 | 访问器 | 元素 codec 来源 |
| --- | --- | --- |
| `Collection<E>` | `CollectionAccess<E>` | `definition.genericCodecs[0]` |
| `IntCollection` | `IntCollectionAccess` | 不装箱 |
| `LongCollection` | `LongCollectionAccess` | 不装箱 |
| `Map<K,V>` | `MapAccess<K,V>` | `genericCodecs[0..1]` |
| `Object2IntMap<K>` | `Object2IntMapAccess` | key codec + int |
| `Object2LongMap<K>` | `Object2LongMapAccess` | key codec + long |
| `Reference2IntMap<K>` | `Reference2IntMapAccess` | 引用键 |
| `Reference2LongMap<K>` | `Reference2LongMapAccess` | 引用键 |

### 9.2 Holder 与 Forge

| 字段类型 | 访问器 | 机制 |
| --- | --- | --- |
| `IFieldDataHolder` | `FieldDataHolderAccess` | 整个对象当一字段，用其 manager |
| `@AdditionalHolder(childManager=true)` | `ChildManagerAccess` | 同上，但用 `ChildFieldDataHolder` 包装 |
| `IDataSerializable` | `SerializableAccess` | 调对象的 4 个方法 |
| Forge `INBTSerializable` | `TagSerializableAccess` | NBT 往返 |

### 9.3 数组

| 字段类型 | 访问器 |
| --- | --- |
| `T[]`（非 primitive） | `ArrayAccess`（元素 codec 从 `genericCodecs[0]` 或 `DataSyncCodec.get(componentType)`） |
| `boolean[]` / `byte[]` / `short[]` / `char[]` | 对应 `*ArrayAccess` |
| `int[]` / `long[]` / `float[]` / `double[]` | 对应 `*ArrayAccess` |
| `IFieldDataHolder[]` | `FieldDataHolderArrayAccess` |
| `IDataSerializable[]` | `SerializableArrayAccess` |
| `INBTSerializable[]` | `TagSerializableArrayAccess` |

### 9.4 集合元素的 codec 解析失败

`CollectionAccess` 构造时：

```java
if (definition.genericCodecs.length == 0) throw new IllegalArgumentException("Collection type not found");
if (elementCodec == null) throw new IllegalArgumentException("Codec not found for type " + definition.genericType[0]);
```

因此 `Collection<UnregisteredType>` 会在**扫描期**报错。

---

## 10. 扩展清单速查

| 你想做的事 | 用哪个 API |
| --- | --- |
| 让一个自定义类型���序列化 | `DataSyncCodec.register`（首选）+ 默认 codec 谓词即可 |
| 给自定义类型专用 `DataField` | `registerFactory(Class, Factory)` |
| 给某个接口的所有实现统一处理 | `registerInterfaceFactory(Class, Function, int)` |
| 按注解/特征匹配类型 | `registerCustomFactory(Predicate, Function, int)` |
| 自定义容器类型（就地修改） | `registerAccessCustomFactory(...)` 或 `registerAccessInterfaceFactory(...)` |
| 参数化类型精确注册 | `registerGenericFactory(Class, Factory, Class...)` |
| 参数化类型家族 | `registerCustomGenericFactory(BiPredicate, BiFunction, int)` |
| 自定义变更检测 | `registerStrategy(Class, Hash.Strategy)` 或字段上 `@Strategy` |
| 自定义数组策略 | `HashUtil.arrayStrategy(elementStrategy)` + `registerStrategy` |
| 完全自管理序列化 | 实现 `IDataSerializable`（无需注册） |
