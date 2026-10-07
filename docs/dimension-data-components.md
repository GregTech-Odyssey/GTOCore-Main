# 维度与天气流式存盘

新维度和新天气系统直接读写紧凑 IO 数据流，当前 schema 为 3。datafix 从本次调整前的 schema 2 起维护，不追溯更早的开发格式或旧 NBT。格式、类型和版本分流位于文件边界，不添加字段类型、元组长度、重复键或载荷完整消费等额外校验层；读取失败仍传播，不能创建空数据替代已有文件。

## 数据范围

| 数据 | 实现与保存位置 |
|---|---|
| 模板、系列、动态强加载候选 | `GTOLib/api/dimension/DimensionCatalog`；主世界 `gtolib/dimensions/catalog.dat` |
| 实例描述、冻结模板、权限、归属、访客与出生点 | `InstanceDescriptor`、`ResolvedTemplate`、`SeriesDefinition`；`instances/<UUID前两位>/<UUID>.dat` |
| 单项实例创建事务 | `InstanceStore`；`instances/pending.dat` |
| 固定维度天气与天气时钟 | `WeatherSystem extends FastSavedData`；主世界 `data/gtocore_galaxy_weather.dat` |
| 动态实例天气 | `InstanceWeatherData extends FastSavedData`；实例 `data/gtocore_instance_weather.dat` |

目录和描述是独立流式文件，没有需要转成 FastSavedData 的自有 SavedData 类。原版/Forge 强加载记录、Ad Astra 空间站、AE2 空间区域以及区块、实体、随机序列仍遵循各自已有协议。玩家返程、坐标卡、旅行系统和监控网络不属于本文的自有存盘范围。

## datafix 基线

- **schema 2**：首次 datafix 基线，固定样例保存在 `src/test/resources/saved-data/schema-2/`，来源是调整前真实服务器产生的文件。`LegacyWeatherNbt`、`LegacyDimensionNbt` 和旧 NBT 读取路径继续删除。
- **schema 3**：直接流式格式。schema 2 的组件树只在 datafix 中读成普通字符串 Map，再还原领域对象；不创建 DataComponentKey、DataComponentMap 或磁盘组件注册表。
- **后续不兼容变更**：提升受影响的存储/schema 版本，为支持的旧版本提供 datafix，覆盖目录、描述、pending 和天气中实际受影响的结构。模板版本与 Minecraft DataVersion 不能代替自有格式版本。
- **迁移边界**：按访问读取并迁移，不扫描历史实例或加载地形。读取本身不改写文件，正常保存以当前格式提交。迁移/写入失败保留原文件并传播错误，不尝试猜测修复。
- **验证**：使用固定基线样例检查身份、种子、冻结定义、天气随机状态和日程，覆盖重复读取、保存后重开与 pending 幂等恢复；样例不能由当前 writer 临时生成替代。

## 流式 codec 与集合

`DimensionDataCodecs` 复用 `IOStreamCodec` / `IOStreamCodecs` 编码领域记录；`WeatherDataIO` 和 `WeatherTimeline` 直接读写天气字段。目录使用现有 `O2OOpenCacheHashMap` / `OpenCacheHashSet`，天气使用 `Reference2ObjectOpenHashMap`。字段顺序由 schema 固定，写入 VarInt 集合长度后逐项编码，不落盘组件名称或键注册表。运行 capability 的无 codec 键仍用于世界生命周期缓存。

生成定义采用 VarInt 字节长度 + UTF-8，不受 `writeUTF` 的 64 KiB 限制。可选字段用标志位，实例 UUID/维度键从逻辑键派生，目录 Map 的键从记录 ID 派生，不重复保存。模板策略与归属类型以固定枚举顺序编码，顺序变更须升级 schema。天气类型保存稳定字符串 ID；网络继续使用独立注册整数 ID，不发送磁盘载荷或历史实例目录。

## 维度文件

`DimensionDataIO` 读取 GZIP 解压后的固定外壳：

```text
magic[int] = "GTDC"
kind[byte] = catalog(1) | instance(2) | pending(3)
storage_version[int] = 1
schema_version[int] = 3
catalog = templates[count + records] + series[count + records] + forced[count + locations]
instance/pending = logical_key + seed + ordinal + template + flags + optional fields
```

读取匹配 magic、kind 和版本。schema 2 按原有 Minecraft DataVersion + 定长载荷长度外壳读取，再执行 datafix；schema 3 没有载荷缓冲、中间 Data 树或 Minecraft DataVersion 字段。旧压缩 NBT 与其他未知格式直接失败。

原子写入顺序是同目录临时文件、关闭编码流、force 文件内容、`ATOMIC_MOVE + REPLACE_EXISTING`。写入失败不降级成覆盖式保存。目录注册、系列创建、强加载候选和权限变更仍保留原有内存回滚。

创建事务顺序：恢复当前格式 pending → 原子写 pending → 原子写描述 → 追加/核对 UUID 索引并 force → 删除 pending → 更新缓存。重启只恢复唯一 pending，不扫描历史实例。

`instances.index` 每条固定 16 字节 UUID。实例描述缓存上限 32、休眠元数据缓存上限 16，分页和按 UUID 查询不加载地形。模板快照、系列基础种子、实例创建序号和现有存盘路径保持稳定。

## 天气 FastSavedData 文件

两种天气存储均通过 `FastSavedData.get` 读取和复用 `DimensionDataStorage.cache` 中的唯一对象，直接读写 `DataIOStream`。文件不使用 GZIP 或 `DataVersion` / `data` NBT 外壳：

```text
magic[int] = "GTWC"
kind[byte] = global(1) | instance(2)
schema_version[int] = 3
global = clock[long] + timelines[count + dimension + timeline]
instance = timeline
timeline = random_state[long] + recovery_present[boolean] + optional recovery_end[long]
           + periods[count + weather_name + start[long] + duration[VarLong]]
```

全局和实例格式不能互换。schema 2 只在 datafix 入口读取组件载荷；新格式直接编码日程，不构造组件树。只有文件不存在时才创建天气，读取失败保留已有文件，不重置随机状态或日程。

全局天气仍缓存于主世界 `ILevel` capability。实例天气独立存放，不并入全局历史表或实例描述。时间线扩展、人工天气变更、随机状态推进和恒星恢复边界变更通知唯一父 FastSavedData 标脏；天气时钟原有标脏频率保持。

运行世界正常调用 FastSavedData 保存。休眠预报使用 `DimensionDataIO.writeFastAtomic`，只读写实例天气元数据；写入成功后才清除 dirty。`FastSavedData.save(File)` 在 `DimensionSaveAttempt` 作用域内也使用该二进制原子写入入口，失败传播并保留 dirty，不经过 SavedData 的 NBT 保存方法。

`DimensionSavedDataMixin` 的 `writeNbtAtomic` 仅服务原版和第三方 SavedData，属于它们的原生协议边界，不是自有维度或天气的兼容层。

## 验证

检查真实二进制磁盘往返、天气随机状态和恒星恢复边界、未来 72000 tick 的天气一致性、父对象标脏、原子写入失败保留 dirty、未知格式/版本及截断文件不被替换。实例存储检查分页与有界缓存、超长 UTF-8 生成定义、种子极值及创建事务各中断点的幂等恢复。基线样例覆盖 schema 2 → 3 的目录、描述、pending、全局天气和实例天气。

使用有效 JDK 21 执行 Spotless、Java/Kotlin 编译及相关测试。修改 GTOLib 后，收尾必须运行 `buildGtolibProtected` 并确认通过，遵守 [预构建要求](gtolib.md)。维度实机探针与重启检查见 [维度文档](dimensions.md)。
