# 05 · 编解码功能参考

> [← 返回 00 概要](00-overview.md)

> 三层 codec 抽象、内置类型全表、`ValueOps` 载体模型、`ValueConverters`、
> `CustomTypes`、枚举策略、Buffer 工具。

---

## 1. 三层 codec 抽象

| 抽象 | 介质 | 方法 | 用途 |
| --- | --- | --- | --- |
| `StreamCodec<B, T>` | `FriendlyByteBuf` | `encode(B, T)` / `decode(B) -> T` | 网络 |
| `ValueCodec<T>` | `ValueOps` carrier | `encode(ValueOps, T) -> Object` / `decode(ValueOps, Object) -> T` | 磁盘 |
| `DataSyncCodec<T>` | 两者 | 实现 `CombinedCodec<T>` | 注册表条目 |

### `DataSyncCodec` 的结构

```java
public final class DataSyncCodec<T> implements CombinedCodec<T> {
    public final StreamEncoder<? super FriendlyByteBuf, ? super T> streamWriter;
    public final StreamDecoder<? super FriendlyByteBuf, ? extends T> streamReader;
    public final ValueEncoder<? super T> valueWriter;
    public final ValueDecoder<? extends T> valueReader;

    public ValueCodec<T>  toValueCodec();
    public StreamCodec<? super FriendlyByteBuf, T> toStreamCodec();
    public DataSyncCodec<T> toDataSyncCodec();     // 返回 this
}
```

### 构造与注册 API

```java
// 构造（不注册）
public static <T> DataSyncCodec<T> of(StreamCodec<? super FriendlyByteBuf,T> s, ValueCodec<T> v);
public static <T> DataSyncCodec<T> of(int version, ValueCodec<T> v);           // 网络半自动派生
public static <T> DataSyncCodec<T> of(StreamCodec<? super FriendlyByteBuf,T> s); // 磁盘存 BYTE_ARRAY

// 注册（写入全局表）
public static <T> DataSyncCodec<T> register(Class<T> type, StreamCodec<...> s, ValueCodec<T> v);
public static <T> DataSyncCodec<T> register(Class<T> type, int version, ValueCodec<T> v);
public static <T> DataSyncCodec<T> register(Class<T> type, StreamCodec<...> s, Codec<T> mojang);
public static <T> DataSyncCodec<T> register(Class<T> type, Registry<T> registry);
public static <T> DataSyncCodec<T> register(Class<?> type, StreamCodec<...> s, ValueCodec<T> v,
                                            Class<?>... genericTypes);
public DataSyncCodec<T> register(Class<T> type);                                // 实例方法

// 查询
public static <T> DataSyncCodec<T> get(Class<T> type);
public static <T> DataSyncCodec<T> get(Class<?> type, Class<?>... genericTypes);
public static boolean contains(Class<?> type);
public static boolean contains(Class<?> type, Class<?>... genericTypes);

// 自动生成
public static <T extends Enum<T>> DataSyncCodec<T> ofEnum(Class<T> enumClass);
public static <T> DataSyncCodec<T> ofArray(Class<T> componentClass);

// 初始化（补 primitive 别名）
public static void init();
```

### 四种"造 codec"的方式对比

| 方式 | 网络半 | 磁盘半 | 体积 | 适用 |
| --- | --- | --- | --- | --- |
| `of(stream, value)` | 提供 | 提供 | 最优 | 两者都有 |
| `of(version, value)` | 自动派生（写 value 的 id+payload） | 提供 | 较优 | 只有 value |
| `of(stream)` | 提供 | 自动派生（存 `BYTE_ARRAY`） | 较大 | 只有 stream |
| `register(type, stream, mojang)` | 提供 | `ValueCodecs.fromCodec` | 中等 | 有 Mojang Codec |

### 查找顺序（`get(Class)`）

```
1. 枚举 → ENUM_CACHE（ClassValue，自动生成）
2. 非 primitive 数组 → ARRAY_CACHE（ClassValue，自动生成）
3. CODECS 精确匹配
   ↑ 没有 assignable-from 回退：子类型必须显式注册
```

`get(int.class)` 与 `get(Integer.class)` 命中同一实例（`init()` 补别名）。

### 泛型查找

```java
DataSyncCodec<T> get(Class<?> type, Class<?>... genericTypes)
```

走 `GENERIC_CODECS`，键是**泛型实参的身份 wrapper**（`HashUtil.arrayIdentityWrapper`）。
必须传与字段声明相同的 `Class` 实例。

### 常量列表（`DataSyncCodec` 公开字段）

见 §2 内置类型全表。

---

## 2. 内置类型全表

### 2.1 原始类型与包装

| 类型 | 常量 | 网络编码 |
| --- | --- | --- |
| `boolean` / `Boolean` | `BOOLEAN_CODEC` | `writeBoolean` |
| `byte` / `Byte` | `BYTE_CODEC` | `writeByte` |
| `short` / `Short` | `SHORT_CODEC` | `writeShort` |
| `int` / `Integer` | `INT_CODEC` | `writeVarInt` |
| `long` / `Long` | `LONG_CODEC` | `writeLong` |
| `float` / `Float` | `FLOAT_CODEC` | `writeFloat` |
| `double` / `Double` | `DOUBLE_CODEC` | `writeDouble` |
| `char` / `Character` | `CHAR_CODEC` | `writeChar` |
| `String` | `STRING_CODEC` | UTF-8 |
| `UUID` | `UUID_CODEC` | 两个 long |
| `BigInteger` | `BIG_INTEGER_CODEC` | 字节数组 |

### 2.2 原始数组

| 类型 | 常量 |
| --- | --- |
| `boolean[]` | `BOOLEANS_CODEC` |
| `byte[]` | `BYTES_CODEC` |
| `short[]` | `SHORTS_CODEC` |
| `char[]` | `CHARS_CODEC` |
| `int[]` | `INTS_CODEC`（VarInt 数组） |
| `long[]` | `LONGS_CODEC` |
| `float[]` | `FLOATS_CODEC` |
| `double[]` | `DOUBLES_CODEC` |

### 2.3 FastUtil 原始集合

| 类型 | 常量 | 特点 |
| --- | --- | --- |
| `IntList` | `INT_LIST_CODEC` | 元素不装箱 |
| `IntSet` | `INT_SET_CODEC` | 元素不装箱 |
| `LongList` | `LONG_LIST_CODEC` | 元素不装箱 |
| `LongSet` | `LONG_SET_CODEC` | 元素不装箱 |

> `List<Integer>` 会装箱；大集合优先用 `IntList`。

### 2.4 Minecraft 注册表条目

网络写**注册 id**，磁盘写**注册 key**。

| 类型 | 常量 | 注册表 |
| --- | --- | --- |
| `Item` | `ITEM_CODEC` | `BuiltInRegistries.ITEM` |
| `Block` | `BLOCK_CODEC` | `BuiltInRegistries.BLOCK` |
| `Fluid` | `FLUID_CODEC` | `BuiltInRegistries.FLUID` |
| `EntityType<?>` | `ENTITY_TYPE_CODEC` | `ENTITY_TYPE` |
| `BlockEntityType<?>` | `BLOCK_ENTITY_TYPE_CODEC` | `BLOCK_ENTITY_TYPE` |
| `MobEffect` | `MOB_EFFECT_CODEC` | `MOB_EFFECT` |
| `Enchantment` | `ENCHANTMENT_CODEC` | `ENCHANTMENT` |
| `SoundEvent` | `SOUND_EVENT_CODEC` | `SOUND_EVENT` |
| `Attribute` | `ATTRIBUTE_CODEC` | `ATTRIBUTE` |
| `ParticleType<?>` | `PARTICLE_TYPE_CODEC` | `PARTICLE_TYPE` |
| `MenuType<?>` | `MENU_TYPE_CODEC` | `MENU` |
| `RecipeType<?>` | `RECIPE_TYPE_CODEC` | `RECIPE_TYPE` |

### 2.5 Minecraft 值类型

| 类型 | 常量 | 备注 |
| --- | --- | --- |
| `ResourceLocation` | `RESOURCE_LOCATION_CODEC` | |
| `Vec2` | `VEC2_CODEC` | |
| `Vec3` | `VEC3_CODEC` | 3 个 double |
| `Vec3i` | `VEC3I_CODEC` | 3 个 VarInt（手写，免装箱） |
| `BlockPos` | `BLOCK_POS_CODEC` | 更紧凑的专用编码 |
| `ChunkPos` | `CHUNK_POS_CODEC` | |
| `SectionPos` | `SECTION_POS_CODEC` | packed long |
| `AABB` | `AABB_CODEC` | 6 个 double（手写，免装箱） |
| `GlobalPos` | `GLOBAL_POS_CODEC` | `CombinedCodec.composite` 组合而成 |

### 2.6 NBT / 物品 / 文本

| 类型 | 常量 |
| --- | --- |
| `Tag` | `TAG_CODEC` |
| `CompoundTag` | `COMPOUND_TAG_CODEC` |
| `ListTag` | `LIST_TAG_CODEC` |
| `ItemStack` | `ITEM_STACK_CODEC` |
| `FluidStack` | `FLUID_STACK_CODEC` |
| `Component` | `COMPONENT_CODEC`（网络：二叉树；磁盘：JSON 字符串） |
| `BlockState` | `BLOCK_STATE_CODEC`（Mojang Codec 派生） |

### 2.7 自动生成

#### 枚举（`ENUM_CACHE`）

| 项 | 网络 | 磁盘 |
| --- | --- | --- |
| 编码 | `writeVarInt(ordinal())` | **fixed**：ordinal；**非 fixed**：名字字符串 |
| 解码 | `constants[readVarInt()]` | 按 int 或按名字查 |

**`fixed` 判定**（`EnumUtil.isFixed`）：
- 显式 `addFixedEnum(Class)` / `addFixedEnum(Predicate)`
- 库内置标记：`LogicalSide`、`Direction`

**风险**：fixed 枚举按 ordinal 存 → **重排常量会破坏旧存档**。

#### 对象数组（`ARRAY_CACHE`）

| 项 | 网络 | 磁盘 |
| --- | --- | --- |
| 空数组 | `writeVarInt(0)` | `ops.createNull()` |
| null 元素 | `writeBoolean(false)` | `ops.createNull()` 元素 |
| 解码 | 长度 + 逐元素 | 非 list → 零长数组；null 元素 → null 槽 |

---

## 3. 自定义类型接入方式

### 方式 A：全局注册（推荐）

```java
// GTOCore: src/main/java/com/gtocore/common/data/GTOCodecs.java:148
DataSyncCodec.register(ResearchPoints.class, RESEARCH_POINTS_STREAM_CODEC, RESEARCH_POINTS_DATA_CODEC);

// GTOCore: src/main/java/com/gtocore/common/CommonProxy.java:193（Mojang Codec 作磁盘半）
DataSyncCodec.register(TesseractDirectedTarget.class,
        TesseractDirectedTarget.STREAM_CODEC, TesseractDirectedTarget.CODEC);

// GTOLib: GTOLib/src/main/java/com/gtolib/GTOCore.java:64（只有 ValueCodec）
DataSyncCodec.register(ICrossRecipeMachine.Thread.class, GTDataFixer.VERSION,
        ICrossRecipeMachine.Thread.DATA_CODECS);
```

**时机**：模组构造期。晚于字段扫描 → 被缓存遮蔽。

### 方式 B：`@Codec` 局部覆盖

```java
private static final FieldDataCodec<MyConfig> CONFIG_CODEC =
        FieldDataManager.createCodec(MyConfig.class, MyConfig::new);

@SaveToDisk @SyncToClient
@Codec(saveCodec = "CONFIG_CODEC", syncCodec = "CONFIG_CODEC")
private MyConfig config = new MyConfig();
```

详见 [02 §6](02-annotations.md#6-codec)。

### 方式 C：`Registry` 成组值

见 [06 §1](06-data-models.md#1-registry)。

### 方式 D：`ValueConverters`（SELF 值降级）

见 §5。

### 方式 E：自定义 `DataField` 工厂

见 [04](04-extension-points.md)。

---

## 4. `ValueOps` carrier 模型

`ValueOps` 是「不知道值物理形态」的抽象。codec 只跟它打交道。

**实现**：`JavaValueOps`（`INSTANCE` = 无版本；`create(int)` = 带版本）。
carrier 就是普通 Java 对象（`Integer`、`String`、`List`、`Map`…），可直接 `toString` 调试。

### 4.1 类型 id 表

| id | 类型 | carrier | 说明 |
| --- | --- | --- | --- |
| 0 | `NULL` | `Null.INSTANCE` | 单一共享对象；Java `null` 也接受 |
| 1 | `BOOLEAN` | 保留 | 布尔存为 byte 0/1 |
| 2 | `BYTE` | `Byte` | |
| 3 | `SHORT` | `Short` | |
| 4 | `CHAR` | `Character` | |
| 5 | `INT` | `Integer` | |
| 6 | `LONG` | `Long` | |
| 7 | `FLOAT` | `Float` | |
| 8 | `DOUBLE` | `Double` | |
| 9 | `BYTE_ARRAY` | `byte[]` | |
| 10 | `INT_ARRAY` | `int[]` | |
| 11 | `LONG_ARRAY` | `long[]` | |
| 12 | `STRING` | `String` | |
| 13 | `LIST` | `List<Object>` | |
| 14 | `STRING_MAP` | `Map<String,Object>` | |
| 15 | `CUSTOM` | `CustomTypes.Type` 的值 | |
| 16 | *retired* | — | 曾为 `OBJECT_MAP`（已移除） |
| 17 | `INT_MAP` | `Int2ObjectMap<Object>` | |
| 18 | `LONG_MAP` | `Long2ObjectMap<Object>` | |
| 19 | `SELF` | 值本身 | 不透明，需 `ValueConverters` 或 codec 处理 |

### 4.2 完整 API 清单

#### 类型判定（`instanceof`）

```java
isNull, isBoolean, isByte, isShort, isChar, isInt, isLong, isFloat, isDouble,
isString, isByteArray, isIntArray, isLongArray, isList, isStringMap, isIntMap,
isLongMap, isSelf, isCustom
```

#### 类型读取（不检查，直接 cast）

```java
getBoolean, getByte, getShort, getChar, getInt, getLong, getFloat, getDouble,
getString, getByteArray, getIntArray, getLongArray, getList, getStringMap,
getIntMap, getLongMap
```

#### 装箱读取

```java
getBooleanBoxed, getByteBoxed, getShortBoxed, getCharBoxed,
getIntBoxed, getLongBoxed, getFloatBoxed, getDoubleBoxed
```

#### 创建（原始）

```java
createNull, createBoolean, createByte, createShort, createChar, createInt,
createLong, createFloat, createDouble, createString, createByteArray,
createIntArray, createLongArray, createList, createStringMap, createIntMap,
createLongMap
```

#### 创建（装箱）

```java
createBooleanBoxed, createByteBoxed, createShortBoxed, createCharBoxed,
createIntBoxed, createLongBoxed, createFloatBoxed, createDoubleBoxed
```

#### 创建（变长参数 / 派生）

```java
createList(Object... values)          // 方便写法
createBooleanArray(boolean[])         // 派生：包成 BYTE_ARRAY
createShortArray(short[])             // 派生：包成 INT_ARRAY
createCharArray(char[])               // 派生：INT_ARRAY
createFloatArray(float[])             // 派生：INT_ARRAY
createDoubleArray(double[])           // 派生：LONG_ARRAY
createUUID(UUID)                      // 派生：LONG_ARRAY
createBigInteger(BigInteger)          // 派生：BYTE_ARRAY
createBigDecimal(BigDecimal)          // 派生：BYTE_ARRAY
```

对应读取：`getBooleanArray`、`getShortArray`、`getCharArray`、`getFloatArray`、
`getDoubleArray`、`getUUID`、`getBigInteger`、`getBigDecimal`。

#### 容器元素捷径

**List 读取（带索引）**：

```java
getBoolean(list, i), getByte(list, i), getShort(list, i), getChar(list, i),
getInt(list, i), getLong(list, i), getFloat(list, i), getDouble(list, i),
getString(list, i), getByteArray(list, i), getIntArray(list, i),
getLongArray(list, i), getList(list, i), getStringMap(list, i)
```

**StringMap 读取（带 key）**：同上，第二参数为 `String key`。

**IntMap / LongMap 读取（带 key）**：同上，第二参数为 `int`/`long`。

**List 追加**：

```java
addBoolean(list, v), addByte, addShort, addChar, addInt, addLong, addFloat,
addDouble, addString, addByteArray, addIntArray, addLongArray, addList,
addStringMap
```

**Map 写入**：

```java
putBoolean(map, key, v), putByte, putShort, putChar, putInt, putLong,
putFloat, putDouble, putString, putByteArray, putIntArray, putLongArray,
putList, putStringMap
```

对 `StringMap`（key 为 `String`）、`Int2ObjectMap`（key 为 `int`）、
`Long2ObjectMap`（key 为 `long`）各有重载。

#### 版本与降级

```java
int dataVersion();                        // 当前读/写的数据版本
<V> V getSelf(Object data, Class<V> type);  // 取 SELF 值（原来是该类型直接返回，转换过则用转换器还原）
String describe(Object data);             // 调试用描述
```

#### 二进制

```java
byte getTypeId(Object data);
void writeValue(Object data, ByteBuf buf);   // 只写 payload（id 由调用方写）
Object readValue(ByteBuf buf);               // 按给定 id 读
```

与历史 `Data` 系统字节兼容 → 老存档可读。

### 4.3 `JavaValueOps` 特有

```java
public static final JavaValueOps INSTANCE;              // dataVersion = 0
public static JavaValueOps create(int dataVersion);     // version 0 时返回 INSTANCE
public static void init();                              // 注册内置 ValueConverters

byte[] toBytes(Object carrier);
Object fromBytes(byte[] data);
int dataVersion();
byte getTypeId(Object data);
```

---

## 5. `ValueConverters`

### 5.1 解决的问题

当自定义类型到达 carrier 却**没有 codec** 时，默认走 `SELF`（Java 序列化）：
体积大、跨版本脆弱、不可读。注册转换器后它变成普通 carrier 值。

### 5.2 API

```java
ValueConverters.Converter.<UUID>builder(ValueOps.Type.LONG_ARRAY, UUID.class)
        .write((ops, uuid) -> ops.createUUID(uuid))       // 编码：值 → payload
        .read((ops, data) -> ops.getUUID(data))           // 解码：payload → 值
        .build();                                         // build() 完成注册
```

| 参数 | 语义 |
| --- | --- |
| 第一个参数 | **payload 的 `ValueOps.Type` id**（不是转换器编号） |
| 第二个参数 | 转换器服务的类 |

**可选谓词**（覆盖整族类型）：

```java
ValueConverters.Converter.<Tagged>builder(ValueOps.Type.STRING, Tagged.class)
        .when(Tagged.class::isAssignableFrom)      // 覆盖所有实现类
        .write((ops, t) -> ops.createString(t.tag()))
        .read((ops, d) -> Tagged.of(ops.getString(d)))
        .build();
```

谓词按**注册顺序**匹配，先注册优先 → **窄的放前面**。

### 5.3 读取侧

```java
ops.getSelf(data, Type.class)
```

- 本来就是该类型 → 直接返回；
- 是转换过的 → 用对应转换器还原；
- 都不成立 → **报错**（不猜）。

### 5.4 内置已注册（`JavaValueOps.init()`）

| 类型 | 存成 |
| --- | --- |
| `UUID` | `LONG_ARRAY` |
| `BigInteger` | `BYTE_ARRAY` |
| `BigDecimal` | `BYTE_ARRAY`（scale 是 VarInt） |
| `boolean[]` | `BYTE_ARRAY` |
| `short[]` / `char[]` / `float[]` | `INT_ARRAY` |
| `double[]` | `LONG_ARRAY` |
| `IntList` / `LongList` / `IntSet` / `LongSet` | 原始数组 |

### 5.5 故意不转换的类型

| 类型 | 原因 |
| --- | --- |
| **有序集合**（`LinkedHashSet` 等） | payload 无顺序，重建后相等性语义会变 |
| **枚举（作为裸值）** | 要么放进字段（`DataSyncCodec` 按 ordinal/名字存），要么放进元素类型为该枚举的集合 |

### 5.6 与 `CustomTypes` 的分工

| | `ValueConverters` | `CustomTypes` |
| --- | --- | --- |
| 面向 | **模组作者** | 库内部 |
| id 空间 | 不占 id（用 payload 的类型 id） | 全局 int 表 `0..15` 已占 |
| 冲突 | 同类型只能注册一次；同类 payload 共用 id | 撞 id **直接报错** |
| 对端兼容 | 普通 carrier 值，任何端都能读 | 对端不认识 → **拒绝整个值** |
| 用途 | 让自定义对象变小、可读 | 库自己的 payload 格式 |

**结论**：模组对象走 `ValueConverters`，**不要**用 `CustomTypes`。

---

## 6. `CustomTypes`（库内部 registry）

**源码**：`datastream/codec/CustomTypes.java`

### 6.1 API

```java
public record Type<T>(int id, BiConsumer<T, ByteBuf> write, Function<ByteBuf, T> read) {
    public static <T> Builder<T> builder(int id);
    public static <T> Builder<T> builder(String name);
}

public static synchronized void register(Type<?> type);
public static Type<?> type(int id);

// Builder
.write(BiConsumer<T, ByteBuf>)
.read(Function<ByteBuf, T>)
.build()   // 构造并注册
```

### 6.2 已占用的 id

| id | 类型 | 注册者 |
| --- | --- | --- |
| 0 | `Tag` | `NbtUtil.TAG_TYPE` |
| 1 | `CompoundTag` | `NbtUtil.COMPOUND_TAG_TYPE` |
| 2 | `ListTag` | `NbtUtil.LIST_TAG_TYPE` |
| 3 | `JsonElement` | `JsonUtils.JSON_TYPE` |
| 4 | `JsonObject` | `JsonUtils.JSON_OBJECT_TYPE` |
| 5 | `JsonArray` | `JsonUtils.JSON_ARRAY_TYPE` |

**`0..15` 全部属于库内部**，无下游空间。

### 6.3 为什么不对下游开放

| 问题 | 说明 |
| --- | --- |
| id 空间 | `0..15` 已占满，无区间分配 |
| 冲突处理 | 撞 id **直接报错**，不合并 |
| 不可回收 | id 一旦发出永久占用 |
| 线格式渗透 | 该 id 会出现在**所有**存档与网络流中 |
| 对端要求 | 对端必须认识该 type，否则**拒绝**而非降级 |

### 6.4 读取自定义值（对所有代码开放）

```java
ops.isCustom(data)                       // 是否 custom
ops.getCustomId(data)                    // 取 id
ops.getCustom(CustomTypes.Type<V>, data) // 按类型读
```

这是安全的——那是库自己的 payload 在回来。

---

## 7. 枚举序列化

### 7.1 `EnumUtil` API

```java
boolean isFixed(Class<? extends Enum<?>> type);
void addFixedEnum(Class<? extends Enum<?>> type);
void addFixedEnum(Predicate<Class<? extends Enum<?>>> predicate);
String getSerializedName(Enum<?> enumValue);
<T extends Enum<T>> T getSerializedEnum(Class<T> type, String name);
<T extends Enum<T>> T getEnum(Class<T> type, String name);
```

### 7.2 fixed vs 非 fixed

| | fixed | 非 fixed（默认） |
| --- | --- | --- |
| 网络 | ordinal VarInt | ordinal VarInt |
| 磁盘 | **ordinal** 整数 | **名字**字符串 |
| 体积 | 小 | 较大 |
| 重排常量风险 | **会破坏旧存档** | 安全 |
| 改名风险 | 安全 | **会破坏旧存档** |

### 7.3 库内置的 fixed 枚举

`LogicalSide`、`Direction`（`DataSyncLib` 主类构造期标记）。

### 7.4 自定义

```java
EnumUtil.addFixedEnum(MyStableEnum.class);                    // 指定类
EnumUtil.addFixedEnum(cls -> cls.getName().startsWith("com.example.stable"));  // 谓词
```

**只有确定永不重排的枚举才应标 fixed。**

---

## 8. Buffer 工具

### 8.1 `VarInts`

```java
void write(ByteBuf buf, int value);
int read(ByteBuf buf);
void writeVarLong(ByteBuf buf, long value);
long readVarLong(ByteBuf buf);
```

### 8.2 `ByteBufCodecExtends`

预置 `StreamCodec` 集合：`RESOURCE_LOCATION_CODEC`、`VEC2_CODEC`、`VEC3_CODEC`、
`VEC3I_CODEC`、`BLOCK_POS_CODEC`、`CHUNK_POS_CODEC`、`SECTION_POS_CODEC`、`AABB_CODEC`、
`TAG_CODEC`、`COMPOUND_TAG_CODEC`、`LIST_TAG_CODEC`、`ITEM_STACK_CODEC`、
`FLUID_STACK_CODEC`、`COMPONENT_CODEC`。

另有 `of(Registry)` 用于 MC 注册表类型。

### 8.3 `ByteBufCodecs`

通用 buffer codec：`fromValueCodec(version, valueCodec)`、`fromCodec(mojangCodec)`、
原始类型 / 数组 / 集合编解码器。

### 8.4 `ComponentStreamCodecs`

`Component` 的网络半：**二叉树**格式（比 JSON 字符串紧凑）。

```java
ComponentStreamCodecs.register(Class<? extends ComponentContents>, StreamCodec)
```

给模组自己的 `ComponentContents` 分配 wire kind `7`：类名作 key + VarInt 帧。
对端缺该类时跳过帧、每 key 警告一次、该节点解码为空（style 与兄弟仍可读）。

### 8.5 `DiskByteBufCodecs`

磁盘半：`COMPONENT_CODEC` 走 **JSON 字符串**（可读、稳定）。

### 8.6 `VersionedFriendlyByteBuf`

```java
int version();
```

带版本号的 `FriendlyByteBuf`，由 `FileUtil.writeVersioned` 使用。

---

## 9. 决策清单

| 你的情况 | 做法 |
| --- | --- |
| MC 原版/Forge 类型 | 查 [§2](#2-内置类型全表) |
| 自己写的 POJO，字段都能注解 | `FieldDataManager.createCodec` + `@Codec` |
| 有 `StreamCodec` + `ValueCodec` | `DataSyncCodec.register(T, stream, value)` |
| 有 `StreamCodec` + Mojang `Codec` | `DataSyncCodec.register(T, stream, mojangCodec)` |
| 只有 `ValueCodec` | `DataSyncCodec.register(T, version, valueCodec)` |
| 只有 `StreamCodec`，体积不敏感 | `DataSyncCodec.of(streamCodec)` |
| 一组有限值，要稳定 id | `Registry` + `freeze()` |
| 只想让 SELF 值变小 | `ValueConverters.Converter` |
| 枚举，常量永不重排 | `EnumUtil.addFixedEnum` |
| 动态可选数据块 | `DataComponentKey/Map/Registry` |

**共同要求**：注册早于字段首次扫描（模组构造期）。
