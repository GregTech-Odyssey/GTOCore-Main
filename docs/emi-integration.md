# EMI 与 AE 的职责边界

EMI 由独立的 fork [EMI GTO Edition](https://github.com/GregTech-Odyssey/EMI-gto)（同级目录 `EMI-gto`，坐标 `dev.emi:emi-forge-1.20.1`，版本见 `gradle.properties` 的 `emi_version`）提供，模组 ID 仍为 `emi`。依赖方向是单向的：EMI 不认识 AE、GTM、GTOCore、GTOLib；AE fork 从 `15.2610.31` 起依赖 EMI-gto（`mods.toml` 中为必需前置），与官方 AE2 的 `appeng.integration.modules.emi` 接法相同。

`15.2610.18`～`15.2610.30` 期间 EMI 源码曾内置在 AE fork 中（同一个 jar 声明 `ae2`、`emi`、`emitrades` 三个模组），这些源码与改动已全部移入 EMI-gto。主仓直接依赖 EMI-gto，不再排除 `dev.emi:emi-forge` 与 EMI Trades 的传递依赖；运行依赖里只能出现一份 `emi`，不能再装官方 EMI。

EMI-gto 以上游 `1.1.24+1.20.1`（提交 `23d251ea8ea3a5fd7d760948f36014b185eac69f`）为基础，只保留 Forge/Mojang 映射以及通用的优化与修复。Crazerium 分支的规划器、收藏分组/分页、配方筛选与分享、批量拖放和自动合成扩展及其 GTO 界面适配均已移除。

EMI Trades（`io.github.prismwork.emitrades`、`assets/emitrades`）随 EMI-gto 一起发布，保留 `emitrades` 模组标识、类别和配置 `emitrades.json5`。整合包不安装独立 EMI Trades jar。GTO 的插件白名单在开发和生产环境都注册内置 `EMITradesPlugin`；未使用 GTO 白名单时由 `@EmiEntrypoint` 自动发现。村民和流浪商人交易展示、原版工厂字段适配、有上限的自定义交易采样，以及不持有世界的实体快照均由 EMI-gto 维护；显示实体缓存在 `ClientLevel` 实例，随世界释放。

EMI-gto 的 README、jar 内 `NOTICE`、`LICENSE-EMI` 和 `LICENSE-EMITrades` 注明 EMI 原作者 Emi（emilyploszaj）、fork 作者 Crazerium、EMI Trades 原作者 Prismwork / Flamarine 及其 MIT 许可。

## 实现位置

只依赖 Minecraft/Forge 的 EMI 逻辑维护在 EMI-gto；需要 AE 的逻辑维护在 AE fork 的 `appeng.integration.modules.emi`：

| 能力 | 位置 |
|---|---|
| 配方输入、催化剂、输出、类别和 ID 索引 | EMI-gto `dev.emi.emi.registry.EmiRecipeIndex` |
| 库存合成判定、可合成列表及库存比较 | EMI-gto `dev.emi.emi.api.recipe.EmiPlayerInventory` |
| 合成余物发现 | EMI-gto `dev.emi.emi.recipe.EmiShapedRecipe` |
| 自定义物品列表的过滤及索引烘焙 | EMI-gto `dev.emi.emi.registry.EmiStackList.bake(List)` |
| 物品/流体连同 NBT 的驻留键、NBT 比较 | 装有 AE 时为 AE 的 `AEItemKey`/`AEFluidKey`（EMI-gto `StackKeys` 选择）、`Comparison` |
| 全局/服务器分组 JSON 读写与旧格式兼容 | EMI-gto `dev.emi.emi.runtime.EmiPersistentData` |
| 工作站多列布局 | EMI-gto `RecipeScreen` |
| 异步搜索烘焙、搜索就绪状态与进度显示 | EMI-gto `EmiSearch`、`EmiReloadManager`、`EmiScreenManager` |
| 延迟启动 JEI 的 JEMI 侧状态与消费入口 | EMI-gto `dev.emi.emi.jemi.JemiPlugin` |
| 收藏对应的 AE key 缓存 | AE `EmiFavoriteKeyCache` |
| AE/EMI 转换 | AE `EmiStackHelper` |
| 大数量按 AE 格式渲染、AE 包装流体的配方查询、收藏变动回调 | AE `AppEngEmiHooks`（接 EMI-gto 的 `EmiHooks`） |
| 无槽位的 AE 合成确认/合成 CPU 界面显示 EMI | AE `AppEngEmiPlugin`（通用界面范围提供器） |

`ItemEmiStack` / `FluidEmiStack` 内部持有驻留键（内容相同即同一对象，按 `==` 比较），`getStackKey()` 提供该引用。装有 AE 时 EMI 直接使用 AE 的 `AEItemKey` / `AEFluidKey`，与 AE 共用一套键，`EmiStackHelper.toAEKey` 直接取用、`toEmiStack` 用 `ItemEmiStack.ofKey` / `FluidEmiStack.ofKey` 包回；没装 AE 时 EMI 才用自带的键。`getKey()` 继续返回 `Item` / `Fluid`，以维持注册表适配、配方比较及第三方集成的既有语义。自定义 EMI 资源没有驻留键，仍走自身的 key/NBT 逻辑。

### 资源专属比较器

严格比较通过 `EmiStack.getStrictComparison()` 选择资源专属的共享比较器，调用方用 `isStrictEqual`；严格索引的 hash 也必须取同一个比较器，不能另行读取 NBT。物品/流体使用 `COMPARE_STACK_KEY`，搜索文本使用 `COMPARE_STRING`，材料形态、研究标签、科技节点和离子使用各自的注册对象引用。装有 AE 时物品/流体的驻留键就是 AEKey，`COMPARE_STACK_KEY` 即按 AEKey 引用比较；需要区分数据的 GTO 物品默认比较也使用它。

新增自定义资源必须覆写 `getStrictComparison()`，比较与 hash 使用同一业务标识；不要为了匹配而临时序列化 NBT。旧 `COMPARE_NBT`、`Comparison.compareNbt()` 与无资源参数的 `EmiPort.compareStrict()` 保留并标记弃用，仅供尚未迁移的第三方二进制兼容，本仓及维护中的 EMI/AE 调用不再使用。

`EmiHooks` 的三个回调由 AE 客户端初始化时（`AppEngClient` 构造器调用 `AppEngEmiHooks.register()`）注册一次。收藏缓存由 EMI 的收藏变更路径（磁盘加载、添加和删除）经 `EmiHooks.onFavoritesChanged` 更新；GTO 不再额外用 Mixin 重算同一份缓存。

## GTO 保留的适配

`GTOLib/src/main/java/com/gtolib/emi` 保留 GT/GTO 专用行为：GT 物品和配方收集、机器等级筛选、TagPrefix/通用电路、研究提示、玩家 AE 数据、合成站库存、GT 流体单元，以及 FTB 界面返回行为。

服务器收藏配置与服务器身份仍由 GTO 决定，再调用 EMI 的分组存储入口。身份尚未同步时，GTO 跳过写入并加载空数据，不能把未知身份写进全局收藏。Spark 启动分析的停止策略与 JEI 自身启动拦截也保留在 GTO。

新增通用逻辑时不要让 EMI-gto 依赖 AE、GTM、`com.gtolib`、`com.gtocore`，也不要让 AE fork 反向依赖 `com.gtolib`、`com.gtocore` 或 GT 机器类；需要整合包参与的行为，在 EMI-gto 开放通用钩子，由对方注册。已迁入本体的实现不要再用 GTO overwrite 覆盖。

## 性能与参数约束

EMI 加载/重载中的逐配方、逐原料、逐标签操作按局部高频路径处理，不能因为入口低频而保留内部的 stream pipeline、装箱或重复构建集合。索引直接构建最终列表，注册对象与驻留键使用引用容器，哈希对象使用哈希缓存容器；AE 侧以 AEKey 为键的容器用 `AEKeySet`/`AEKeyObjectMap`/`AEKeyIntMap` 等专用容器。排序先缓存比较所需的数据，避免在 comparator 中反复遍历。

对外 EMI/JEI 协议保留必要的接口类型、Stream/Optional 返回值；内部使用具体容器、原始类型与显式循环。参数和空值约束通过 JetBrains `@NotNull`、`@Nullable`、`@Contract`、`@UnmodifiableView` 表达，直接共享只读缓存，不增加运行时防御检查或不可变包装。

## 跨仓验证与交付

各仓用 JDK 21。先在 EMI-gto 编译、跑单元测试并 `publishToMavenLocal`，再在 AE fork 编译、跑 `appeng.integration.modules.emi` 测试并 `publishToMavenLocal`，最后在主仓把 `emi_version`、`ae2_version` 改成同一版本号验证 `compileJava`、`compileKotlin`，修改 GTOLib 后必须完成 `buildGtolibProtected`。同时检查运行依赖里只有一份 `emi`（`dev.emi:emi-forge-1.20.1`）。

Maven Local 产物仅供本机验证。对其他环境交付前按 EMI-gto → AE → GTOCore 的顺序发布；只有修改 GTOLib 时才需按子模块流程提交、推送并更新主仓 gitlink。客户端工作站布局、流体查询、收藏、搜索与村民交易页仍需游戏内回归。
