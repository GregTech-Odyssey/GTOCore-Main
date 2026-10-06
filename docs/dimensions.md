# 维度生命周期与实例

维度模板登记生成规则；实例描述保存身份、归属、种子和解析后的生成定义；`DimensionManager` 只为运行中的实例持有 `ServerLevel`。动态实例不注册到数据包 `LEVEL_STEM`。原版三个世界常驻，其他世界默认在空闲 1200 tick 后于服务器 tick 末尾保存和关闭。关闭保留定义和原存档目录。

## 配置与指令

`GTOConfig.dimensions` 提供 `enabled`、`idleTicks` 和 `residentDimensions`。关闭懒加载时恢复数据包/代码世界的启动加载；动态历史仍按地址加载。额外常驻列表填写完整维度 ID。

所有管理指令要求权限等级 2，根入口为 `/gtocore dimensions`：

| 指令参数 | 用途 |
|---|---|
| `templates` | 列出模板及版本 |
| `private create player\|team <UUID> <模板> [槽位 [种子]]` | 创建描述，默认槽位 `main`，此时不生成地形 |
| `series create <系列ID> [模板 [基础种子 [player\|team <UUID>]]]` | 登记系列，默认主世界噪声模板，无归属时公开 |
| `series enter <系列ID> <long序号> [玩家选择器]` | 授权后取得或创建地址，并进入安全出生点 |
| `info <维度ID>`、`list [页码]` | 查看状态、种子、归属、保活原因；每页 10 条历史记录 |
| `tp <维度ID> [玩家选择器]` | 通过安全出生策略传送 |
| `grant\|revoke <维度ID> <访客UUID>` | 持久化访客授权，撤销后立即检查在线玩家 |
| `load\|unload <维度ID>` | 显式加载或申请安全卸载；占用、常驻或已休眠时拒绝卸载 |

示例模板为 `gtocore:private_void`、`gtocore:private_flat` 和 `gtocore:overworld_noise`。虚空模板首次进入时创建 5×5 平台；已保存的出生点不可站立时重新选择安全位置。指令返回的动态维度 ID 形如 `gtolib:instance/<UUID>`。

## 调用约定

管理器绑定服务器实例，通过 `DimensionManager.get(server)` 取得。模板注册、实例创建、权限修改、加载和卸载操作在服务器线程执行。`load(key)` 可从其他线程调用，它将工作提交给服务器，并返回共享的 `CompletableFuture<ServerLevel>`。加载回调中的重入请求使用同一个 future；保存中的加载请求等待保存结束并取消关闭。`loadNow` 不能等待一个尚未完成的重入请求。

```java
var manager = DimensionManager.get(server);
var owner = new OwnerRef(OwnerRef.Kind.PLAYER, player.getUUID());
var instance = manager.getOrCreatePrivate(owner, DimensionTemplates.VOID, "main", null);
manager.teleport(player, instance.dimension());

var address = new SeriesAddress(new ResourceLocation("example", "journey"), -2L);
if (manager.canEnterSeries(player, address)) {
    var destination = manager.getOrCreateSeries(address);
    manager.teleport(player, destination.dimension());
}
```

模板使用已注册的 `DimensionType` key 和生成器工厂。变更规则必须提升模板版本；同一版本的不同定义会被拒绝。私人实例身份只包含归属种类、UUID、模板 ID、槽位，系列实例身份只包含系列 ID 和序号。规范 UTF-8 长度前缀编码后使用 `UUID.nameUUIDFromBytes`，读取时核对逻辑键，防止碰撞。`SeriesAddress.offset` 使用精确加法，溢出抛出异常。查询相邻地址不会创建实例。

`getLevel` 只查询运行引用，`getAllLevels` 返回仅包含初始化完成世界的稳定快照。元数据查询用 `descriptor`、分页 `instances().page`、`metadataStorage`、`DimensionStations.query` 或天气预报接口。需要区块/实体时才调用授权加载入口 `loadFor(entity, key)`；玩家传送优先使用 `teleport`。底层玩家跨维度方法也校验权限和加载状态。

底层传送同时核对目标对象是否就是当前运行的 `ServerLevel`。实例重开后，缓存中已经关闭的旧对象会在移动玩家、创建着陆器或发送维度包前被拒绝；调用方应重新按维度键取得目标。

跨 tick 的实际操作可持有 `keepAlive(key, reason)` 返回的租约，并在结束时 `close`。建造、传送及 AE2 空间交换使用作用域租约。飞行火箭与未落地着陆器使用实体租约和随位置刷新、40 tick 过期的临时区块票据；着陆或移除时释放。普通实体保存后随世界休眠。需要离线运行的设备应采用有效的区块强加载。

## 权限与兼容

个人实例默认允许本人及明确访客；队伍实例按 FTB Teams 当前队伍身份校验。加入队伍不改变个人实例归属。离队事件立即复核，队伍删除立即送回在线非管理员；删除队伍后即使有访客授权，也只有管理员可处理该实例。管理员等级 2 可以访问。私人授权与星球解锁分别保存。

登录、重生、维度参数指令、统一传送工具、私人/系列指令、虚空设备和 Ad Astra 星球入口显式加载目标。空间站建造和着陆在加载前检查私人权限、原有星球规则及站点归属；站点列表和归属查询读取 SavedData。AE2 空间存储世界通过统一管理器登记，列表查询不创建世界，实际空间交换才加载并保活。FTB Chunks 的加票据操作只加载其自身规则已经判定有效的目标，移除票据不唤醒世界。

原版和 Forge 保存的强加载由加载流程恢复，并执行 Forge 注册的票据校验回调，包含 AE2、FTB Chunks 的有效性判断。动态实例的强加载索引随加减票据持久化；启动只遍历该索引。无效强加载的启动候选在校验后可回到休眠。固定数据包世界允许读取小型 `chunks` 元数据来寻找启动候选。

主仓和 GTOLib 全局存档初始化只在主世界执行。监控运行状态存于 `ILevel` capability；卸载不会清空其他世界的监控。跨维度设备的目标只保存维度键和位置，卸载时解除 Level、方块实体及处理器引用，查询不会加载目标。动态环境存入实例并在进入前同步客户端；动态天气时间线按实例保存，休眠预报只读取该实例，tick 不维护全部历史世界。

## 存盘与失败处理

主世界目录下 `gtolib/dimensions/catalog.dat` 保存模板、系列及动态强加载索引。`instances/<UUID前两位>/<UUID>.dat` 保存各实例，`instances.index` 以固定 16 字节 UUID 分页寻址；`pending.dat` 是单项创建事务日志，恢复时不扫描历史。描述缓存上限 32，休眠 SavedData 缓存上限 16。未访问的序号没有描述或地形文件。生成定义使用 NBT 字节数组保存 UTF-8，支持超过 64 KiB 的完整噪声设置和生物群系参数定义。

实例首次创建即冻结生成定义和种子；模板升级不覆盖已有描述。实例种子参与 `getSeed`、`StructureCheck`、噪声/结构生成状态、随机序列及服务端/客户端混淆种子。数据包和既有代码维度保持原主世界种子及生成器。动态出生位置、角度、实体、区块、SavedData 和随机序列在重新加载时恢复。

每 20 tick 只检查已加载世界，tick 末尾重新检查玩家、强加载、未过期临时票据及租约。保存失败保留世界运行引用，并保留失败数据的 dirty 状态；后续可以重试。关闭失败将实例标为 `POISONED`，禁止同一次服务器运行重新打开并停止服务器。正常关闭发布 Forge 生命周期事件，移除世界边界委托监听器，刷新 Forge 世界数组和计时缓存；不删除或移动存档。

## 验证

使用有效 JDK 21，按根目录 `AGENTS.md` 设置 `JAVA_HOME`。迭代测试命令：

```powershell
.\gradlew.bat spotlessApply spotlessCheck test --tests 'com.gtolib.api.dimension.*' -PgtolibUnprotected=true
.\gradlew.bat runData -PgtolibUnprotected=true
```

Forge 探针位于 `src/dimensionTest`，通过 `-I gradle/scripts/dimensions-probe.gradle` 显式加入测试运行，标准打包不包含它。默认在 `build/dimensions-test/server` 新建隔离测试目录；启动前由测试人员确认该目录的 `eula.txt` 和开发配置。可用 `-PdimensionProbeDir=<测试根目录>` 指定其他隔离位置。服务端端口为 25577；`online-mode=false` 只用于本地测试。

```powershell
.\gradlew.bat runServer -I gradle/scripts/dimensions-probe.gradle -PgtolibUnprotected=true
# 联机探针：先启动服务器，再在两个终端启动不同名字的客户端
.\gradlew.bat runServer -I gradle/scripts/dimensions-probe.gradle -PgtolibUnprotected=true -PdimensionNetworkProbe=true
.\gradlew.bat runClient -I gradle/scripts/dimensions-probe.gradle -PgtolibUnprotected=true -PdimensionNetworkProbe=true -PdimensionClientName=DimensionA
.\gradlew.bat runClient -I gradle/scripts/dimensions-probe.gradle -PgtolibUnprotected=true -PdimensionNetworkProbe=true -PdimensionClientName=DimensionB
```

断言结果以测试目录的 `result.txt`、`network-server.txt` 和各客户端结果文件为准，Gradle 的退出码不能代替探针结果。服务端探针故意注入一次保存失败，相关错误日志属于验证预期。联机探针自动处理隔离客户端的首次启动界面、连接、传送和重连，完成后关闭测试进程。

联机探针还执行完整管理员指令、实际放置的虚空设备传送处理、太空电梯/火箭星球菜单着陆、空间站结构建造与休眠后授权着陆、AE2 方块交换，以及动态世界的强制出生点重生。站点查询和拒绝着陆均检查没有唤醒世界。单服探针可增加 `-PdimensionProbeFTB=false` 验证不安装可选 FTB Chunks 的启动与生命周期。

`-PdimensionRestartProbe=true` 使用两次独立服务器启动：第一次写入原版、AE2 锚点与 FTB 强加载，第二次验证恢复和无效票据清理。此模式将隔离测试世界的 FTB `force_load_mode` 设置为 `always`，并在测试中另外检查 `never`、过期和访问拒绝不会唤醒实例。`-PdimensionCloseProbe=true` 在卸载事件阶段注入不可恢复异常，验证停服、`POISONED` 状态、拒绝重开及保存保留；结果为 `close-result.txt`。这些故障开关只存在于探针，不进入生产包。

交付时仍按 `docs/gtolib.md` 在干净的 GTOLib 提交上收尾运行一次 `buildGtolibProtected`，并核对 gitlink、protected jar 和侧车。不上传明文调试 jar。
