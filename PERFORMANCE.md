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
4. **`MapEntry` 迭代问题**：开放寻址 Map 或精简 Map 的实现在迭代时会为每个元素创建一个对象，元素量大后会造成 GC 压力。**尽量改用 `forEach`（或 `fastForEach`）/ `fastIterator`**，避免产生中间对象。
5. **原始容器迭代问题**：如 `IntSet`、原始 `Map` 的 `keyset()` / `values()` 返回的原始容器，用 `for (Type e : set) {}` 增强 for 会调用包装方法，迭代时依然会装箱、分配对象。**应手动使用迭代器或 forEach**，杜绝隐式装箱与分配。
6. **优先用 EnumMap / EnumSet 处理枚举键。** 本仓大量逻辑围绕枚举状态/类型分发；凡 `Map<Enum,...>` / `Set<Enum>` 一律用 `EnumMap` / `EnumSet`，它们是按序号的数组实现，速度与内存均优于任何哈希容器，且迭代不产生装箱。
7. **慎用 `Optional`、`stream()`、`.boxed()` 在热路径中。** 热路径（render、tick、机器逻辑高频调用处）**禁止**出现流式链（`stream().map(...).filter(...)`）、`Optional` 链式调用与 `.boxed()`——它们会产生中间对象与装箱。优先用显式 for/while 循环与直接判空。仅在冷路径（配置解析、数据一次性整理）可酌情使用。
8. **`hashCode()` 要与 `equals()` 成对且缓存。** 未重写 `equals` 只当 `Map` key/`Set` 元素时，直接用引用语义容器（见术语「引用容器」），别依赖默认 `Object.hashCode()` 走哈希容器形同虚空。自定义哈希对象的 `hashCode()` 若较贵，应在构造后缓存到字段，避免每次寻址重算。
9. **热路径避免在循环内分配对象。** 高频迭代内禁止 `new`、字符串拼接（用 `StringBuilder`）、创建临时集合。可复用而不复用的局部 `ArrayList`、临时 `Map` 一律提为可复用实例或改用池化/`ThreadLocal`，减少 GC 压力。
10. **不熟悉/不确定容器行为时，先查 FastCollection 与 fastutil 是否已有更优替代**，再决定用 JDK 原生容器；凡是本仓已确立用法的（如 `AEKeyMap`、OpenCache 系列），跟随既有先例，不要另造轮子。

## 非容器规范

11. **热路径避免使用 `synchronized` / 可重入锁做高频互斥。** 若临界区是纯读或只需原子读写，优先用无锁方案（`LongAdder`、`AtomicLong/AtomicReference` 轻量 CAS、不可变快照 + volatile，或读多写少用 `CopyOnWriteArrayList`）。本仓在大量机器/网络逻辑处有并发读写，慎用粗粒度锁阻塞 tick 线程。
12. **字符串拼接禁止在热路径用 `+` 或 `String.format`。** 高频处一律用 `StringBuilder`/`StringBuffer` 复用实例，或预构造缓存；渲染、tick、tooltip 生成处必须按此处理。网络/IO 字符串生成尤其注意。
14. **反射仅用于冷路径（注册、扫描、启动一次）。** 禁止在热路径使用 `Class.forName`、`getMethod`/`getDeclaredMethod`、`getField` 等反射调用。需要重复调用时，缓存反射到的 `Method`/`Field`/构造器并 `setAccessible(true)`，优先用 `MethodHandles`/`VarHandle`。
19. **不要在每帧/每次 tick 内重复计算只依赖静态数据的结果。** 查得到的常量（`Item.EMPTY`、注册表查找、`ResourceLocation`、字符串 key）提到 `static final` 字段或缓存；GTO 的 registry/命名空间高频碰撞时，用编译期已知常量替代字符串查找。

### 网络同步

23. **网络同步不要简单复用磁盘保存的数据。** 直接序列化 NBT 或复用存盘格式做网络包会带进大量与同步无关的字段与对象分配，造成带宽与 GC 浪费。**应为网络通道单独写一套紧凑的流式编码**（可变长/定长原始字段、按需仅含变更字段），并**优先使用 DataSyncLib 相关的 API** 完成字段级增量同步，而非整包序列化。

### DataSyncLib 存盘

24. **非必要不要 `saveNull`。** 只有非默认/非空的值才需要真正写入磁盘；值为空时直接跳过写入，磁盘读写更小、体积更紧凑。
25. **多用默认值，值为默认值时也不写入。** 对大多数取默认值的字段，设定明确的默认值并跳过序列化；仅当字段偏离默认值时写入，进一步压缩存盘体积、减少 IO。
