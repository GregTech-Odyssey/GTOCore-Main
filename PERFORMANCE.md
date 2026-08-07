# 性能规范

> **要求与本仓所有编码改动一并遵守。** 本 Mod 是高密度、高频逻辑的性能敏感项目，容器与迭代的写法会直接决定 GC 压力与运行帧率。所有由 AI 生成或修改的代码，在提交前必须按下述规范自查。

本文件被 [AGENTS.md](AGENTS.md) 强制引用，AI 与开发者操作本仓库代码时必须一并遵循。

## 术语

| 术语 | 定义 |
|------|------|
| **哈希对象** | 重写了 `hashCode()` 与 `equals()` 的对象 |
| **引用对象** | 未重写哈希语义（即非哈希）的对象 |
| **哈希方式** | 对象作为 `Map` 的 key 或放进 `Set` 时，若它是哈希对象即按哈希处理；否则按引用处理 |
| **哈希缓存容器** | 寻址后缓存计算出的 hash，查找时用该 hash 优先比较、快速剔除不相等对象的容器。如 JDK 标准库的 `HashMap`/`HashSet` 及其父类，以及 FastCollection 的容器 |
| **非哈希缓存容器** | 寻址后直接使用 `equals()` 比较，无 hash 预判。如 fastutil 的开放寻址容器（`ObjectOpenHashSet` 等） |
| **原始容器** | 使用原始类型（无装箱）的容器，如 fastutil 的各种原始容器（`IntSet`、`Int2ObjectMap` 等） |

## 容器规范

1. **尽量使用原始集合**，尤其是对象作为 `Set` 或 `Map` 的 key 时，优先选原始类型对应的 fastutil 容器。
2. **根据对象类型选择引用容器。** 属「注册对象/标识稳定」的（如 `Item`、`Fluid`，以及专门的 `AEKeyMap`），用引用容器；**`AEKey` 是引用对象，必须使用专门的 `AEKeyMap`**。
3. **除非哈希特别轻、计算极快，一律使用哈希缓存容器。** 即便对象本身支持 `equals`，也别用开放寻址的「非哈希缓存容器」裸跑；尤其 `HashSet` 要用 OpenCacheHashSet 替代——速度更快、内存占用更低。
4. **`MapEntry` 迭代问题**：开放寻址 Map 或精简 Map（含 fastutil / FastCollection）的实现在**普通迭代**（`for (var e : map.entrySet())`）时会为每个元素创建一个对象，元素量大后会造成 GC 压力。**尽量改用 `forEach`（或 `fastForEach`）/ `fastIterator`**，避免产生中间对象。
   - **⚠️ `fastIterator()` 每次 `next()` 返回的是同一个被复用的 Entry 对象**（只改变其 key/value 内容）。因此**循环体内绝不能把 `entry` 对象本身收集进集合、列表、数组、map 或任何长生命周期结构**——否则收集到的全是同一引用的最终状态。需要保存时，只能保存 `entry.getKey()` / `entry.getValue()` 返回的真实引用，或立刻 `new` 一个新对象再收集。
   - 若循环体需要把条目收集起来（如 `.toList()`、`stream().sorted().collect(...)`、`list.add(entry)`），**必须保留普通迭代**，不可替换为 `fastIterator`。
   - 循环体内的 `break` / `continue` / `return` 直接可用；在同一循环体内多次调用 `it.next()` 需注意每个 `next()` 才推进一次。
5. **原始容器迭代问题**：如 `IntSet`、原始 `Map` 的 `keyset()` / `values()` 返回的原始容器，用 `for (Type e : set) {}` 增强 for 会调用包装方法，迭代时依然会装箱、分配对象。**应手动使用迭代器或 forEach**，杜绝隐式装箱与分配。
6. **优先用 EnumMap / EnumSet 处理枚举键。** 本仓大量逻辑围绕枚举状态/类型分发；凡 `Map<Enum,...>` / `Set<Enum>` 一律用 `EnumMap` / `EnumSet`，它们是按序号的数组实现，速度与内存均优于任何哈希容器，且迭代不产生装箱。
7. **慎用 `Optional`、`stream()`、`.boxed()` 在热路径中。** 热路径（render、tick、机器逻辑高频调用处）**禁止**出现流式链（`stream().map(...).filter(...)`）、`Optional` 链式调用与 `.boxed()`——它们会产生中间对象与装箱。优先用显式 for/while 循环与直接判空。仅在冷路径（配置解析、数据一次性整理）可酌情使用。
8. **`hashCode()` 要与 `equals()` 成对且缓存。** 未重写 `equals` 只当 `Map` key/`Set` 元素时，直接用引用语义容器（见术语「引用容器」），别依赖默认 `Object.hashCode()` 走哈希容器形同虚空。自定义哈希对象的 `hashCode()` 若较贵，应在构造后缓存到字段，避免每次寻址重算。
9. **热路径避免在循环内分配对象。** 高频迭代内禁止 `new`、字符串拼接（用 `StringBuilder`）、创建临时集合。可复用而不复用的局部 `ArrayList`、临时 `Map` 一律提为可复用实例或改用池化/`ThreadLocal`，减少 GC 压力。
10. **不熟悉/不确定容器行为时，先查 FastCollection 与 fastutil 是否已有更优替代**，再决定用 JDK 原生容器；凡是本仓已确立用法的（如 `AEKeyMap`、OpenCache 系列），跟随既有先例，不要另造轮子。
11. **复制 fastutil / FastCollection（及其哈希缓存容器）时优先用 `clone()`，不要用 `new X(set)` 复制构造或迭代 `add`。** fastutil 容器的 `clone()` 直接对底层数组 `Arrays.copyOf` 做**一次批量拷贝**（含哈希缓存容器的内部 hashcache），不经过逐元素迭代、不重排不重哈希、不触发 `equals`/`hashCode`，常数开销远低于复制构造/`addAll` 的"逐一 reopen、重算 hash、重新扩容"。热路径里遇到"复制一份容器"时，先把底层类型能 `clone()` 的就 `clone()`。
   - ⚠️ fastutil 的 `clone()` 返回的是**具体实现类**（如 `IntOpenHashSet` 不带泛型），不是接口 `Set<Integer>`。若字段/返回值声明为接口，需按其签名处理类型（fastutil 约定如此，勿当作 bug 改坏）。
   - **浅拷贝**：`clone()` 只复制容器骨架，元素仍为共享引用（与第 8 条"引用对象"语义同理）。若元素本身是可变的哈希对象、且需要隔离副本各自的元素，才需额外深拷贝元素——否则直接共享即可，别白白分配。
   - 仅当确实需要**独立结构副本**时才用 `clone()`（如缓存一份快照、返回一份可写的副本）；若只是只读引用同一份容器，直接复用原引用即可，连拷贝都不必做（见第 13 条空单例/复用原则，避免无谓分配）。
12. **新建/落地的容器按可预知的元素数预分配容量，避免默认容量起家的多次扩容 rehash。** 凡是能预先估计元素个数的构造点都要一次性给足容量：从已有容器**逐项变换/过滤**而来（无法 `clone()` 的情形，如要算权重、改值、删条目）用 `new X(source.size())` 之类按源大小预分；批量 `put`/`add` 前若知道总数也先分配足额。fastutil/开放寻址容器按 expectedSize 用负载因子算出合适容量，可省去多轮 `rehash` 与扩容时的反复数组拷贝。这是 `clone()` 不可用时的兜底原则（与第 11 条互补），同受第 1/9 条约束；仅当冷路径或元素量极小（个位数，总扩容成本可忽略）时才可省去。
13. **空容器直接用专用空单例，禁止用 `X.of()` 工厂的空参。** 返回/赋值/传入空容器时，用 `Collections.emptyList()` / `emptySet()` / `emptyMap()`（或 fastutil 的 `ObjectLists.EMPTY_LIST` / `ObjectSets.EMPTY_SET` 等，视返回类型而定）——它们是**预先创建的共享单例**，每次引用零分配、零内存。而 `List.of()` / `Set.of()` / `Map.of()` 是通用工厂：**非空**时必然 `new` 分配；即使**空参**，语义也非"专用空单例"，应避免。仅当确需 `List.of()` 家族（如 varargs 场景）或指单个元素 `List.of(x)` 时才用它们。**Guava 的 `ImmutableList.of()` 等属于另一套不可变实现，不在此列**，按既定用法保留。

## 非容器规范

14. **热路径避免使用 `synchronized` / 可重入锁做高频互斥。** 若临界区是纯读或只需原子读写，优先用无锁方案（`LongAdder`、`AtomicLong/AtomicReference` 轻量 CAS、不可变快照 + volatile，或读多写少用 `CopyOnWriteArrayList`）。本仓在大量机器/网络逻辑处有并发读写，慎用粗粒度锁阻塞 tick 线程。
15. **字符串拼接禁止在热路径用 `+` 或 `String.format`。** 高频处一律用 `StringBuilder`/`StringBuffer` 复用实例，或预构造缓存；渲染、tick、tooltip 生成处必须按此处理。网络/IO 字符串生成尤其注意。
16. **反射仅用于冷路径（注册、扫描、启动一次）。** 禁止在热路径使用 `Class.forName`、`getMethod`/`getDeclaredMethod`、`getField` 等反射调用。需要重复调用时，缓存反射到的 `Method`/`Field`/构造器并 `setAccessible(true)`，优先用 `MethodHandles`/`VarHandle`。
17. **不要在每帧/每次 tick 内重复计算只依赖静态数据的结果。** 查得到的常量（`Item.EMPTY`、注册表查找、`ResourceLocation`、字符串 key）提到 `static final` 字段或缓存；GTO 的 registry/命名空间高频碰撞时，用编译期已知常量替代字符串查找。

### DataSyncLib 编解码

18. **对称编解码器用 `ListData`，不要用 `IntMapData` / `StringMapData` 等 map 结构。** 凡 encode 与 decode 一一对应、字段不会缺少的对称编解码（如 `DataCodec`），用 `ListData` 按序存储：encode 用 `new ListData(n)` + `add(...)`，decode 用 `data.asListData()` 直接取。**因为对称结构不会缺字段，不需要 map 的 key 索引，也无须类型判断**（去掉 `instanceof XxxMapData` 与 `Objects.requireNonNull`，直接 `asListData()` / `getString(i)` / `getLong(i+1)`）。
   - **单字段**直接用对应 `Data` 类型（如 `StringData.valueOf(name)` / `data.getString()`），**不要再包一层 `ListData`**。
   - 仅当**非对称**持久化（需容忍缺失字段、有版本号，如 `save`/`load` 兼容旧存档）才保留 map（`StringMapData` 等按 key 定位）。

19. **网络流与持久化要各自用专用编解码器，不要共用一个字符串格式。** 网络通道用紧凑的原始/整数 id 编码，持久化用自描述、跨版本稳定的 key 编码——两者独立，见下条与「网络同步」。
20. **注册对象必须用 DataSyncLib 注册器得到网络专用编解码器，不要和保存共用字符串。** 凡"标识稳定、一次性注册"的对象（如 `ResearchTag`、`TechNode`、`AttributeDefinition`），应注册进 DataSyncLib 的 `Registry`（`com.gto.datasynclib.util.Registry`，`GTRegistry` 即继承它）：
   - `unfreeze()` → 注册所有实例（构造时 `register(name, obj)`）→ **在全部注册完成后** `freeze()`（按 key 排序分配稳定整数 id）。
   - **网络流**用 `registry.streamCodec()` —— 按**紧凑整数 id** 编码（`writeVarInt(id)` / `readVarInt`）。
   - **持久化**用 `registry.dataCodec(keyCodec)`（如 `STRING_CODEC`）—— 按 **key 字符串**编码，自描述、跨版本稳定。
   - **不可共用**：网络与保存各配专用 codec，网络不直接用保存的字符串格式（同 21 条原则）。
   - **注意**：`freeze()` 的 static 块必须放在所有注册对象的静态字段之后，先注册、后 freeze；id 由排序决定，客户端/服务器注册集合一致即保证 id 一致。

### 网络同步

21. **网络同步不要简单复用磁盘保存的数据。** 直接序列化 NBT 或复用存盘格式做网络包会带进大量与同步无关的字段与对象分配，造成带宽与 GC 浪费。**应为网络通道单独写一套紧凑的流式编码**（可变长/定长原始字段、按需仅含变更字段），并**优先使用 DataSyncLib 相关的 API** 完成字段级增量同步，而非整包序列化。

### DataSyncLib 存盘

22. **非必要不要 `saveNull`。** 只有非默认/非空的值才需要真正写入磁盘；值为空时直接跳过写入，磁盘读写更小、体积更紧凑。
23. **多用默认值，值为默认值时也不写入。** 对大多数取默认值的字段，设定明确的默认值并跳过序列化；仅当字段偏离默认值时写入，进一步压缩存盘体积、减少 IO。
