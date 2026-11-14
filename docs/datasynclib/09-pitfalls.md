# 09 · 排错手册

> [← 返回 00 概要](00-overview.md)

> 按**症状**查表。每条给：可能原因 → 如何确认 → 修复。
> 案例都对应本仓真实代码或 DataSyncLib 源码行为。

---

## 快速诊断流程图

```
字段值没同步到客户端？
  │
  ├─ 该类是 IFieldDataHolder 吗？ ── 否 → 根本不会被管理
  │
  ├─ 字段有 @SyncToClient 吗？ ── 否 → 加注解
  │
  ├─ GTOCore 机器：调了 requestSync()/syncToClient() 吗？
  │                                    ── 否 → 加调用
  │
  ├─ autoDetect = false 且没 markFieldsForSync？
  │                                    ── 是 → 置脏（见 §2）
  │
  ├─ 字段是可变容器，引用没变而内容变了？
  │                                    ── 是 → @Access 或 final（见 §3）
  │
  └─ 值是「相等」的（strategy 认为没变）？ ── 是 → 检查 @Strategy（见 §4）

字段没存进存档？
  │
  ├─ 有 @SaveToDisk 吗？
  ├─ 调了 setChanged() / 机器被标脏吗？（见 §5）
  ├─ 被 skipWhen / defaultValue / saveEmpty 拦下了吗？（见 §6）
  └─ 该字段所在类被正确扫描了吗？（嵌套对象要 @AdditionalHolder 或显式 holder）
```

---

## 1. 字段完全没被管理（注解无效）

### 症状
- 改值后既不存盘也不同步，没报错。
- `getFieldDefinition("字段名")` 返回 `null`。

### 原因与修复

| 原因 | 确认方式 | 修复 |
| --- | --- | --- |
| 类没实现 `IFieldDataHolder` | 看类声明 / 继承链 | 继承基类或自行实现并返回 `FieldDataManager` |
| 字段是 `static` | 看声明 | 去掉 `static`（静态字段不被管理） |
| 字段是 `transient` | 看声明 | 去掉 `transient` |
| 只标了 `@AddToManager` | 看注解 | 加 `@SaveToDisk` / `@SyncToClient`（`@AddToManager` 不参与自动同步/增量存盘） |
| 嵌套对象没标 `@AdditionalHolder` | 看字段 | 加 `@AdditionalHolder`，或让子对象自己实现 `IFieldDataHolder` 并由外层驱动 |
| 注解在**父类字段**上，但扫描的是子类 | — | 扫描会走父类，正常；但要确认父类也在同一个 holder 上（不是独立的非 holder 对象） |

### 验证代码

```java
var def = getFieldDataManager().getFieldDefinition("myField");
System.out.println(def);   // null = 没被管理
```

---

## 2. `autoDetect = false` 的字段永远不同步

### 症状
- 字段有 `@SyncToClient(autoDetect = false)`。
- 值改了，客户端看不到。
- 断点打在 `writeToNetworkBuffer` 里，发现**字段根本没被写入**。

### 根因链（源码）

```
syncBlockEntityToClient(be, all=false, autoDetectOnly=true)
  → updateFieldDirtyFlags(SERVER, autoDetectOnly = true)
      → if ((!autoDetectOnly || d.autoDetect(side)) && field.detectChange(...))
               ↑ autoDetectOnly=true 且 d.autoDetect=false → 条件为 false → 跳过检测
  → 若没有任何字段变脏 → 返回，不发包
```

即便绕过检测（`autoDetectOnly = false`），`writeToNetworkBuffer` 仍只写**已标脏**字段：

```java
if (writeAll || field.isChanged(source)) { writeVarInt(i); field.writeToBuffer(...); }
```

GTOCore 机器还有一层陷阱：`syncToClient()`（`ISync` 默认方法）**直接调
`writeToNetworkBuffer(SERVER, false)`**，完全不跑 `updateFieldDirtyFlags`。
所以 `autoDetect = true` 的字段刚改完立刻 `syncToClient()` 也会发空包——它此刻还不是脏的。

### 修复

```java
// 三选一
markFieldsForSync("myField");                          // ① 手工置脏
DataSyncNetwork.syncBlockEntityToClient(be, false, false);  // ② 强制检测所有字段
DataSyncNetwork.syncBlockEntityToClient(be, true, true);    // ③ 全量发送
```

GTOCore 机器：

```java
markFieldsForSync("myField");
requestSync();          // 下一 tick autoSync() → syncBlockEntityToClient(this)（已脏字段必被写入）
// 或
markFieldsForSync("myField");
syncToClient();         // 立即发（此刻它已经是脏的）
```

### 拼错名字会怎样

`markFieldsForSync(String...)` 走 `storage.definitionMap.get(name)`；
`definitionMap` 的键是**存储键**（`@SaveToDisk(key=...)` 或字段名）。找不到 → **抛异常**
（`createFieldNotFoundException`）。所以拼错能立刻发现；**漏写**才是静默失效。

> ⚠️ 注意 `@SaveToDisk(key = "custom")` 会让存储键变成 `"custom"`，
> 此时 `markFieldsForSync("字段名")` **找不到**。用 `key` 的值。

---

## 3. 容器内容变了但没检测到

### 症状
- `final List<X> list`，`list.add(...)` 后不同步。
- 或者非 final 的 `Set`，`add` 后不同步。

### 原因

| 场景 | 默认行为 | 结果 |
| --- | --- | --- |
| 非 final 容器 + 就地修改 | 值模式 | 内容比对**可能**检测到（依赖容器 `equals`），但语义不如访问模式明确 |
| 嵌套子对象就地修改 | 值模式快照比对 | **漏检**（快照指向同一对象） |
| `final` 容器 + 就地修改 | 访问模式 | ✅ 正常 |

### 修复

```java
// 方案 A：加 @Access（推荐用于明确的容器语义）
@SyncToClient
@Access
private ArrayList<String> history = new ArrayList<>();

// 方案 B：声明为 final
@SaveToDisk
@SyncToClient
private final Set<BlockPos> offsets = new HashSet<>();
```

GTOCore 目前只用后一种：`PigmentMixer` 用 `final Set` + `autoDetect = false`。

### 嵌套对象特例

```java
class Outer {
    @AdditionalHolder              // 扁平化：递归扫描 Inner 的字段
    Inner inner = new Inner();
}
class Inner {
    @SyncToClient int x;           // 就地改 inner.x → 能检测到（因为是独立字段）
}
```

若 `inner` 是**普通字段**（无 `@AdditionalHolder`）且类型有 codec → 整体值模式，
需要重新赋值才检测到。

---

## 4. 值明明变了却判定「没变」

### 症状
- 打印出来值确实不同，但 `detectChange()` 返回 false。

### 原因

| 原因 | 说明 |
| --- | --- |
| `equals()` 被重写成「忽略某些字段」 | 框架默认用 `OBJECT_STRATEGY`（标准 `hashCode/equals`） |
| `hashCode()` 与 `equals()` 不一致 | 快照比对失效 |
| `@Strategy` 指向的策略过于宽松 | 例如只比 item id，不比数量/NBT |
| 字段类型是**枚举**且比较的是 ordinal | 应当一致，检查是否被 `@Conversion` 换了类型 |

### 修复

```java
// 自定义策略：明确「什么算变化」
public static final Hash.Strategy<ItemStack> FULL = new Hash.Strategy<>() {
    public int hashCode(ItemStack s) { return ItemStack.hashItemAndComponents(s); }
    public boolean equals(ItemStack a, ItemStack b) {
        return (a == b) || (a != null && b != null && ItemStack.isSameItemSameTags(a, b));
    }
};

@Strategy("FULL")
@SyncToClient
private ItemStack stack;
```

或全局注册：

```java
FieldDefinitionStorage.registerStrategy(MyType.class, FULL);
```

**内置已处理**：`ItemStack` / `FluidStack` 及其数组有专用策略（`ItemStackHashStrategy` 等），
不需要额外配置。

---

## 5. 存档里没有字段（重启后值丢失）

### 症状
- 游戏内存里值正确，退出重进变默认。

### 检查清单

| # | 检查项 | 说明 |
| --- | --- | --- |
| 1 | 有 `@SaveToDisk` 吗 | 只有 `@SyncToClient` 不会写盘 |
| 2 | 调了 `setChanged()` 吗 | BlockEntity 必须标脏区块才会保存 |
| 3 | GTOCore 机器：`saveCustomPersistedData` 链路通吗 | 由 GTCEu 在区块保存时调用；确认机器真的在已加载区块里 |
| 4 | 字段被 `skipWhen` 拦下了吗 | 见 §6 |
| 5 | 字段值等于 `defaultValue` 吗 | 等于则不写盘（设计行为，不是 bug） |
| 6 | 字段是空值且 `saveEmpty = false` | null / 空集合 / 空数组不写 |
| 7 | 嵌套对象有 `@AdditionalHolder` 或显式 holder 吗 | 否则内部字段不被扫描 |
| 8 | `key` 属性与读取时用的名字一致吗 | `@SaveToDisk(key="x")` 后读取要用 `"x"` |

### 关键区分：`setChanged()` 管「存不存」，`@SaveToDisk` 管「存什么」

```java
// ❌ 只加注解，从不标脏 → 区块不会保存 → 字段丢失
@SaveToDisk private int energy;

// ✅
energy = 100;
setChanged();       // ← 必须有
```

GTOCore 机器不需要手动 `setChanged()`——GTCEu 的机器保存流程会处理，
但**你需要确保机器的字段值在保存时刻是正确的**（异步写入场景要注意）。

---

## 6. 字段被条件跳过

### 症状
- 字段有时存、有时不存。

### 三个跳过机制

| 机制 | 谓词 | 触发条件 |
| --- | --- | --- |
| `skipWhen = "method"` | `(T) -> boolean` 返回 `true` | 跳过 |
| `defaultValue = "x"` | 值等于 `x` | 跳过 |
| `saveEmpty`（默认 false） | 值是 null / 空集合 / 空数组 | 跳过 |

### 确认方式

```java
// 手工验证某个字段会写出什么
Object v = getFieldDataManager().writeFieldToValue("energy", JavaValueOps.INSTANCE);
System.out.println(v);   // DataField.NOT_PERSISTED = 被 skipWhen 拦下
```

### 修复

- 想让空值也存 → `saveEmpty = true`。
- `skipWhen` 谓词写反了（返回 `true` 是跳过）→ 检查语义。这是 26.9.4 改名时特别强调的点：
  旧名 `condition` 容易被理解成「满足条件才保存」，新名 `skipWhen` 才说清方向。

GTOCore 正确示例：

```java
// 不在编辑模式时，编辑器槽位不值得存
@SaveToDisk(skipWhen = "skipEditorStacks")
GenericStack[] editorInputs = EMPTY_EDITOR_STACKS;

private boolean skipEditorStacks(GenericStack[] stacks) {
    return !editingPattern;       // 返回 true = 跳过
}
```

---

## 7. `listener` / `skipWhen` 不触发或报错

### 症状
- 回调方法从不执行。
- 或者扫描期抛方法找不到异常。

### 原因表

| 原因 | 表现 | 修复 |
| --- | --- | --- |
| 参数类型不精确 | 静默不匹配 | `int` 字段必须 `(int)`，不能 `(Integer)` |
| 方法不是非静态 | 找不到 | 去掉 `static` |
| 方法不在同一声明类 | 找不到 | 移到字段所在类（`getDeclaredField` 不搜父类） |
| `@SaveToDisk.listener` 期望磁盘加载 | 网络同步不触发 | 改用 `@SyncToClient(listener=...)` |
| 字段被跳过 | 不触发 | listener 只在**字段存在于载荷**时触发 |
| 同名重载 | 解析歧义 | 方法名唯一 |

### 两条 listener 的触发矩阵

| 事件 | `@SyncTo*.listener` | `@SaveToDisk.listener` |
| --- | --- | --- |
| 收到网络同步 | ✅ | ❌ |
| 区块加载（`field_sync`） | ❌ | ❌ |
| 存档加载（`field_save`） | ❌ | ✅（仅字段存在时） |
| 发送数据 | ❌ | ❌ |

**因此**：如果一个字段既需要「客户端收到时刷新」又需要「存档加载后重建缓存」，
**两个注解都要配 listener**：

```java
@SyncToClient(listener = "onClientUpdate")     // (T,T)
@SaveToDisk(listener = "onDiskLoaded")         // (T)
private int energy;

private void onClientUpdate(int newValue, int oldValue) { ... }
private void onDiskLoaded(int value) { ... }
```

---

## 8. 抛 `No codec registered for type` / `IllegalArgumentException: Cannot resolve a codec for a null type`

### 症状（扫描期，通常是类首次使用时）

```
java.lang.RuntimeException: No codec registered for type com.example.MyType
```

### 原因

| 原因 | 修复 |
| --- | --- |
| 类型未注册 | `DataSyncCodec.register(...)` 或 `@Codec` |
| 注册**晚于**字段首次扫描 | 提前到模组构造期 |
| 字段声明用了通配符/类型变量（`List<?>`、`T`） | 用具体类型，或 `@Generic` + 可解析泛型 |
| 是子类型，但只注册了父类型 | 注册必须**精确匹配**（没有 assignable-from 回退） |
| `void` 类型字段 | 不可能，属于声明错误 |

### 关键：注册时机

```java
// ✅ 模组构造期（CommonProxy 构造 / @Mod 构造）
DataSyncCodec.register(MyType.class, STREAM_CODEC, VALUE_CODEC);

// ❌ 在某个字段首次被扫描之后（例如 FMLCommonSetupEvent 里、或首次 tick）
```

`FieldDefinitionStorage` 会缓存每个字段解析出的 codec 工厂，**后注册会被已有的缓存遮蔽**。

GTOCore 的正确位置：

```java
// src/main/java/com/gtocore/common/CommonProxy.java:193
DataSyncCodec.register(TesseractDirectedTarget.class,
        TesseractDirectedTarget.STREAM_CODEC, TesseractDirectedTarget.CODEC);

// GTOLib/src/main/java/com/gtolib/GTOCore.java:64
DataSyncCodec.register(ICrossRecipeMachine.Thread.class, GTDataFixer.VERSION,
        ICrossRecipeMachine.Thread.DATA_CODECS);
```

---

## 9. 反序列化时 `Field index N out of bounds`

### 症状（客户端/服务端收到包后）

```
IndexOutOfBoundsException: Field index 7 out of bounds. Expected 0-5 for holder com.example.MyBlockEntity
```

### 原因

**字段索引表不一致**：发送端和接收端的类**字段集合不同**（数量或顺序）。

| 原因 | 说明 |
| --- | --- |
| 客户端/服务端版本不一致 | 最常见：一方加了字段，另一方没更新 |
| 服务端热重载了类，客户端没有 | 开发环境常见 |
| 字段集合因条件不同而不同 | 注解扫描是**静态**的，不应有运行期差异；若有 → 说明用了两套不同的类 |

### 修复

- 保证客户端与服务端使用**同一个 mod jar**。
- 开发环境改了字段/注解后**两端都要重启**。
- 线上版本升级必须同时更新两端（协议版本 `"2"` 会拦不同协议，但**同一协议版本下的字段变更检测不到**）。
- 若要兼容演进，**只在末尾追加字段**（索引稳定），不要删/重排。

### 为什么索引会错位

`storage.syncToClientDefinitions` 的顺序由扫描决定（按字段名排序或声明顺序）。
一旦两端字段集合不同，同一索引指向不同字段，除了越界还会有**类型错乱**（更隐蔽）。

---

## 10. 协议版本不匹配（连不上服务器）

### 症状

```
This server is incompatible with your client! (datasynclib:sync)
```

### 原因

`DataSyncNetwork.PROTOCOL_VERSION` 客户端与服务端不一致。该常量当前为 `"2"`
（26.9.2 因为消息索引重排而提升）。

### 修复

更新两端到同一 DataSyncLib 版本。这不是你能「绕开」的——`NetworkRegistry.newSimpleChannel`
的版本检查就是双向强制。

**你新增自定义消息时**：注册到 `DataSyncNetwork.CHANNEL` 会增加消息索引。
若在 0~3 之外追加是安全的；若中间插入，必须 bump `PROTOCOL_VERSION`。

---

## 11. 客户端显示旧值 / 新玩家进服看不到状态

### 症状
- 单个玩家刷新正常，但新加入的玩家看到默认值。

### 根因

新玩家进入区块走 `getUpdateTag()` → 读 `field_sync`。检查：

| 检查项 | 说明 |
| --- | --- |
| 你重写过 `getUpdateTag()` 吗 | 若重写且**没调 `super`**，`field_sync` 不会写入 |
| 你重写过 `saveAdditional`/`load` 吗 | 若绕过基类实现，`field_save`/`field_sync` 键可能缺失 |
| `load()` 里 `field_sync` 优先 | 若手工往 NBT 塞了 `field_sync`，会覆盖 `field_save` |

### 基类的正确实现（不要改坏）

```java
@Override
public @NotNull CompoundTag getUpdateTag() {
    var tag = super.getUpdateTag();                       // ← 必须调 super
    if (getFieldDataManager().hasSyncFields(LogicalSide.SERVER)) {
        tag.putByteArray("field_sync",
                getFieldDataManager().writeToNetworkBuffer(LogicalSide.SERVER, true));  // 全量
    }
    return tag;
}

@Override
public void load(@NotNull CompoundTag tag) {
    super.load(tag);                                      // ← 必须调 super
    if (tag.get("field_sync") instanceof ByteArrayTag arr) {
        getFieldDataManager().readFromNetworkBuffer(LogicalSide.CLIENT, arr.getAsByteArray());
    } else if (tag.get("field_save") instanceof ByteArrayTag arr) {
        getFieldDataManager().readFromValue(...);
    }
}
```

GTOCore 机器由 GTCEu 的 `loadCustomPersistedData` / `saveCustomPersistedData` 处理，
一般不需要你干预；但**如果你覆盖了机器的 NBT 保存方法，记得调用父类实现**。

---

## 12. 存档读回来是旧格式 / 迁移没生效

### 症状
- 明明改了字段结构，旧存档读出来还是旧值。
- `ops.dataVersion()` 分支走不到。

### 检查

| 检查项 | 说明 |
| --- | --- |
| 版本号真的写进去了吗 | `saveAdditional` 里 `tag.putInt("field_data_dataVersion", VERSION)` |
| `VERSION` 是全局静态 | 改子类自己的 `VERSION` 字段**无效**（编译期绑定基类字段） |
| 读取时传了版本吗 | `JavaValueOps.create(tag.getInt("field_data_dataVersion"))`，而不是 `INSTANCE` |
| 是否走了 `field_sync` 分支 | 该分支**没有版本概念**，不会走迁移逻辑 |
| 迁移代码在 codec 的 `decode` 里吗 | `ops.dataVersion()` 只有 codec 能读到 |

### 修复模板

```java
// 全局设一次（模组初始化期）
FieldDataHolderBlockEntity.VERSION = 2;
FieldDataHolderEntity.VERSION = 2;

// codec 里分支
public MyType decode(ValueOps ops, Object data) {
    if (ops.dataVersion() < 2) return migrateV1(ops, data);
    return decodeV2(ops, data);
}
```

GTOCore 的实例（`MEPatternBufferPartMachine`）：

```java
if (ops.dataVersion() < 2) {
    var nbt = LegacyIO.legacyCompound(ops, data);
    if (nbt != null) { deserializeNBT(nbt); return; }
}
fieldDataManager.get().readFromValue(data, ops);
```

---

## 13. 同步/存档出现「一半新一半旧」

### 症状
- 多个字段中只有部分更新了。

### 原因

| 原因 | 说明 |
| --- | --- |
| `autoDetect` 不一致 | 有的字段 `autoDetect = true`，有的 `false` 且未置脏 |
| 部分字段被 `skipWhen` 跳过 | 本轮不发送 |
| 重入保护吞掉了本次操作 | 在 listener 里再次触发同步会直接返回 |
| 在 sync 调用**之后**才改的字段 | 快照已在调用线程生成，后续改动属于下一轮 |

### 重入保护的细节

```java
private volatile boolean updating;   // updateFieldDirtyFlags 期间
private volatile boolean writing;    // writeToNetworkBuffer 期间
```

若你的 `skipWhen` / `detectChange` / `listener` 里再调 `syncXxx`：
`updateFieldDirtyFlags` 返回 `false`（视作没变化），`writeToNetworkBuffer` 返回空数组。
**本次操作被静默丢弃**。

**不要在回调里再触发同步**。改为「置一个标记，下个 tick 再同步」。

---

## 14. GTOCore 特有：机器字段同步了但没人看到

### 症状
- 字段有 `@SyncToClient`，客户端也有值，但渲染/UI 不更新。

### 检查

| 检查项 | 说明 |
| --- | --- |
| 需要 `scheduleUpdate = true` 吗 | 纯字段更新不会自动触发渲染刷新 |
| 渲染代码读的是哪个字段 | 可能读的是缓存的派生值，需要 `listener` 清缓存 |
| 是否只在客户端改了字段 | 客户端改 `@SyncToClient` 字段无意义（会被服务端值覆盖） |
| 机器是否 `isRemote()` | GTCEu 侧判断；客户端机器是 remote |

### 正确的组合

```java
// 值会变 + 需要渲染 → scheduleUpdate
@SyncToClient(scheduleUpdate = true)
private boolean active;

// 值会变 + 需要清派生缓存 → listener
@SyncToClient(listener = "onModeChanged")
private int mode;

private void onModeChanged(int newMode, int oldMode) {
    if (newMode != oldMode) rebuildCache();
}
```

GTOCore 实例：

```java
// ManaHeaterMachine.java:33 —— 客户端渲染器要用
@SyncToClient(scheduleUpdate = true)
private boolean salamanderInput = false;

// NanoPhagocytosisPlantMachine.java:16 —— 需要清配方类型缓存
@SyncToClient(listener = "onGrindingModuleChanged")
private boolean grindingModule;
```

---

## 15. 性能问题：同步太频繁 / 存档太大

### 同步太频繁

| 症状 | 修复 |
| --- | --- |
| 每 tick 都发包 | 只在值真正变化时调 `requestSync()`；`autoDetect` 会自动过滤无变化 |
| 大集合每次全量 | 用 `autoDetect = false` + 只在结构变化时置脏（见 `PigmentMixer`） |
| 网络带宽高 | 检查字段类型：`List<Integer>` → `IntList`；`String` 大文本考虑压缩或拆字段 |

### 存档太大

| 症状 | 修复 |
| --- | --- |
| 大量默认值被写入 | 配 `defaultValue`（GTOCore 大量使用） |
| 空集合被写入 | 保持 `saveEmpty = false`（默认） |
| 临时状态被写入 | `@SaveToDisk(skipWhen = "...")` |
| 大量重复结构 | 考虑 `Registry` + 存 id，而不是存完整对象 |

### 具体收益参考

`FissionReactorMachine` 4 个字段全部配 `defaultValue`；`VacuumPumpMachine` 2 个字段同样。
GTOCore 主仓 564 个 `@SaveToDisk` 中绝大多数都带 `defaultValue`——这是仓库的既定风格，
新代码应遵循。

---

## 16. 开发期的类加载/热重载怪象

### 症状
- 开发环境改字段后行为诡异（值错乱、索引越界、listener 不触发）。

### 原因

`FieldDefinitionStorage` 是**类级全局缓存**，`ClassValue` / 静态 map 持有扫描结果。
热重载替换了 `Class` 对象，但旧缓存可能仍被引用。

### 处理

- **改字段/注解后重启游戏**，不要只热重载。
- 若必须热重载，避免在同一个 JVM 里反复改**字段集合**。

---

## 附录：调试技巧

### A. 打印一个 holder 的全部定义

```java
var storage = FieldDefinitionStorage.get(getClass());
System.out.println("all: " + Arrays.toString(storage.allDefinitions));
System.out.println("syncToClient: " + Arrays.toString(storage.syncToClientDefinitions));
System.out.println("save: " + Arrays.toString(storage.saveDefinitions));
```

### B. 手工序列化看内容

```java
// 存档形态
Object carrier = getFieldDataManager().writeToValue(JavaValueOps.INSTANCE);
System.out.println(carrier);              // carrier 是普通 Java 对象，可直接打印
byte[] bytes = JavaValueOps.INSTANCE.toBytes(carrier);
System.out.println("bytes = " + bytes.length);

// 单个字段
System.out.println(getFieldDataManager().writeFieldToValue("energy", JavaValueOps.INSTANCE));

// 网络形态
byte[] net = getFieldDataManager().writeToNetworkBuffer(LogicalSide.SERVER, true);
System.out.println("net bytes = " + net.length);
```

### C. 检查某个字段是否变脏

```java
boolean dirty = getFieldDataManager()
        .updateFieldDirtyFlags(LogicalSide.SERVER, false);
System.out.println("any change: " + dirty);
```

### D. 验证远程调用方法表

```java
System.out.println(getFieldDataManager().getRemoteMethods().keySet());
```

### E. 常见日志关键字

在游戏日志里搜这些能快速定位：

| 关键字 | 含义 |
| --- | --- |
| `No codec registered for type` | 类型未注册（§8） |
| `Field index` / `out of bounds` | 字段索引不一致（§9） |
| `incompatible with your client` | 协议版本不匹配（§10） |
| `createFieldNotFoundException` | `markFieldsForSync` 名字错了（§2） |

---

## 一页纸急救表

| 症状 | 最可能原因 | 一行修复 |
| --- | --- | --- |
| 字段没同步 | 没调同步方法 | GTOCore: `requestSync()`；裸基类: `syncBlockEntityToClient(this)` |
| `autoDetect=false` 不同步 | 没置脏 | `markFieldsForSync("x")` |
| 刚改完就 `syncToClient()` 发空包 | 该路径不跑自动检测 | 先 `markFieldsForSync("x")`，或改用 `requestSync()` |
| 容器内容变但没反应 | 值模式漏检 | 加 `@Access` 或 `final` |
| 值变了判定没变 | equals 过宽 | 配 `@Strategy` |
| 重启丢值 | 没 `setChanged()` | 改值后调 `setChanged()` |
| 有时存有时不存 | `skipWhen`/`defaultValue` | 检查谓词方向（`true`=跳过） |
| listener 不触发 | 参数类型/路径不对 | 精确类型 + 区分网络/磁盘 |
| `No codec registered` | 未注册/注册太晚 | 模组构造期 `DataSyncCodec.register` |
| `Field index out of bounds` | 两端字段集不同 | 两端同版本 |
| 连不上服务器 | 协议版本不符 | 更新 DataSyncLib |
| 新玩家看不到状态 | `getUpdateTag` 没调 super | 恢复基类实现 |
| 迁移不生效 | 版本没传 / 全局 VERSION | `JavaValueOps.create(version)` |
| 字段同步了但界面不刷新 | 缺 `scheduleUpdate` | 加 `scheduleUpdate = true` 或 listener 清缓存 |

---

## 相关文档

- 注解参数细节 → [02-annotations.md](02-annotations.md)
- 同步内部流程 → [07-architecture.md](07-architecture.md)
- codec 与注册时机 → [05-codecs.md](05-codecs.md)
- GTOCore 正确写法 → [08-gtocore-usage.md](08-gtocore-usage.md)
