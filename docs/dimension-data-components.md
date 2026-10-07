# 维度与天气组件存盘

新维度和新天气系统只支持各自的当前二进制组件格式。未知格式、未知版本、损坏载荷和缺失必需字段直接传播失败，不能创建空数据替代已有文件。

## 数据范围

| 数据 | 实现与保存位置 |
|---|---|
| 模板、系列、动态强加载候选 | `GTOLib/api/dimension/DimensionCatalog`；主世界 `gtolib/dimensions/catalog.dat` |
| 实例描述、冻结模板、权限、归属、访客与出生点 | `InstanceDescriptor`、`ResolvedTemplate`、`SeriesDefinition`；`instances/<UUID前两位>/<UUID>.dat` |
| 单项实例创建事务 | `InstanceStore`；`instances/pending.dat` |
| 固定维度天气与天气时钟 | `WeatherSystem extends FastSavedData`；主世界 `data/gtocore_galaxy_weather.dat` |
| 动态实例天气 | `InstanceWeatherData extends FastSavedData`；实例 `data/gtocore_instance_weather.dat` |

目录和描述是独立组件文件，没有需要转成 FastSavedData 的自有 SavedData 类。原版/Forge 强加载记录、Ad Astra 空间站、AE2 空间区域以及区块、实体、随机序列仍遵循各自已有协议。玩家返程、坐标卡、旅行系统和监控网络不属于本文的自有存盘范围。

## 组件与 codec

`DimensionDataComponents` 分别注册 CATALOG、INSTANCE、TEMPLATE、SERIES；`WeatherDataComponents` 分别注册 TIMELINE、GLOBAL、INSTANCE。字段单例、磁盘注册表及运行 capability 分开，组件 map 仅在保存和读取边界构造，领域对象仍是唯一内存数据源。

持久化调用 `registry.encode(map)` / `registry.decode(data, dataVersion)`，使用稳定名称保存组件和注册对象。网络继续使用独立的紧凑流式 codec，不发送磁盘载荷或历史实例目录。对称嵌套记录使用按序 `ListData`，单字段直接使用对应 Data 类型。

## 维度文件

`DimensionDataIO` 读取 GZIP 解压后的固定外壳：

```text
magic[int] = "GTDC"
kind[byte] = catalog(1) | instance(2) | pending(3)
storage_version[int] = 1
schema_version[int] = 2
minecraft_data_version[int]
payload_length[int]
payload = registry.encode(components).writeToBytes()
```

读取必须匹配 magic、kind 和版本，载荷长度在 1..64 MiB 范围内，并完整消费文件和组件载荷。旧压缩 NBT 与其他格式均被拒绝，没有格式回退入口。

原子写入顺序是同目录临时文件、关闭编码流、force 文件内容、`ATOMIC_MOVE + REPLACE_EXISTING`。写入失败不降级成覆盖式保存。目录注册、系列创建、强加载候选和权限变更仍保留原有内存回滚。

创建事务顺序：恢复当前格式 pending → 原子写 pending → 原子写描述 → 追加/核对 UUID 索引并 force → 删除 pending → 更新缓存。重启只恢复唯一 pending，不扫描历史实例；

`instances.index` 每条固定 16 字节 UUID。实例描述缓存上限 32、休眠元数据缓存上限 16，分页和按 UUID 查询不加载地形。模板快照、系列基础种子、实例创建序号和现有存盘路径保持稳定。

## 天气 FastSavedData 文件

两种天气存储均通过 `FastSavedData.get` 读取和复用 `DimensionDataStorage.cache` 中的唯一对象，直接读写 `DataIOStream`。文件不使用 GZIP 或 `DataVersion` / `data` NBT 外壳：

```text
magic[int] = "GTWC"
kind[byte] = global(1) | instance(2)
schema_version[int] = 2
minecraft_data_version[int]
payload_length[int]
payload = registry.encode(components).writeToBytes()
```

天气文件和内部组件载荷均检查完整消费，长度上限为 64 MiB；全局和实例格式不能互换。只有文件不存在时才创建天气，读取失败保留已有文件，不重置随机状态或日程。`LegacyWeatherNbt`、`LegacyDimensionNbt` 及旧格式固定样例已移除。

全局天气仍缓存于主世界 `ILevel` capability。实例天气独立存放，不并入全局历史表或实例描述。时间线扩展、人工天气变更、随机状态推进和恒星恢复边界变更通知唯一父 FastSavedData 标脏；天气时钟原有标脏频率保持。

运行世界正常调用 FastSavedData 保存。休眠预报使用 `DimensionDataIO.writeFastAtomic`，只读写实例天气元数据；写入成功后才清除 dirty。`FastSavedData.save(File)` 在 `DimensionSaveAttempt` 作用域内也使用该二进制原子写入入口，失败传播并保留 dirty，不经过 SavedData 的 NBT 保存方法。

`DimensionSavedDataMixin` 的 `writeNbtAtomic` 仅服务原版和第三方 SavedData，属于它们的原生协议边界，不是自有维度或天气的兼容层。

## 验证

检查真实二进制磁盘往返、天气随机状态和恒星恢复边界、未来 72000 tick 的天气一致性、父对象标脏、原子写入失败保留 dirty、未知格式/版本及损坏载荷不被替换。实例存储检查分页与有界缓存、超长 UTF-8 生成定义、种子极值、必需字段以及创建事务各中断点的幂等恢复。

使用有效 JDK 21 执行 Spotless、Java/Kotlin 编译及相关测试。修改 GTOLib 后，收尾必须运行 `buildGtolibProtected` 并确认通过，遵守 [预构建要求](gtolib.md)。维度实机探针与重启检查见 [维度文档](dimensions.md)。
