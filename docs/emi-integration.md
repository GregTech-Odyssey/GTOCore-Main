# EMI 与 AE 的职责边界

GTO AE fork 从 `15.2610.18` 起承载 `dev.emi.emi` 源码，并在同一个产物中声明 `ae2`、`emi` 两个模组。GTO 通过 AE 依赖获取 EMI，不能再额外加载独立的 `dev.emi:emi-forge`；主仓对传递依赖也做了排除。

从 `15.2610.24` 起，内置 EMI 以原始上游 `1.1.24+1.20.1`（提交 `23d251ea8ea3a5fd7d760948f36014b185eac69f`）为基础，仅保留 Forge/Mojang 映射以及 AE/GTO 自有集成和优化。Crazerium 分支的规划器、收藏分组/分页、配方筛选与分享、批量拖放和自动合成扩展及其 GTO 界面适配均已移除。

从 `15.2610.20` 起，AE fork 同时内置 EMI Trades（`io.github.prismwork.emitrades`、`assets/emitrades`），保留 `emitrades` 模组标识、类别和配置 `emitrades.json5`。整合包不再安装独立 EMI Trades jar，主仓也不再编译依赖该 jar。GTO 的插件白名单在开发和生产环境都注册内置 `EMITradesPlugin`；未使用 GTO 白名单时由 `@EmiEntrypoint` 自动发现。

村民和流浪商人交易展示、原版工厂字段适配、有上限的自定义交易采样，以及不持有世界的实体快照均由 AE fork 维护。显示实体缓存在 `ClientLevel` 实例，随世界释放；原先 GTO 的 `emitrades` Mixin 和快照桥接已移除。模组交易工厂只在注册时调用，不保存在配方索引中。

AE fork 的 README、jar 内 `NOTICE`、`LICENSE-EMI` 和 `LICENSE-EMITrades` 分别注明 EMI 原作者 Emi（emilyploszaj）、EMI Trades 原作者 Prismwork / Flamarine 及其 MIT 许可；AE2 本身与资源仍遵循各自原有协议。

## 实现位置

仅依赖 Minecraft/Forge/EMI，或仅增加 AE 依赖的逻辑，直接维护在同级 `Applied-Energistics-2-gto` 的源码中：

| 能力 | AE fork 中的位置 |
|---|---|
| 配方输入、催化剂、输出、类别和 ID 索引 | `dev.emi.emi.registry.EmiRecipeIndex` |
| 库存合成判定、可合成列表及库存比较 | `dev.emi.emi.api.recipe.EmiPlayerInventory` |
| 合成余物发现 | `dev.emi.emi.recipe.EmiShapedRecipe` |
| 自定义物品列表的过滤及索引烘焙 | `dev.emi.emi.registry.EmiStackList.bake(List)` |
| 收藏对应的 AE key 缓存 | `appeng.integration.modules.emi.EmiFavoriteKeyCache` |
| AE/EMI 转换、NBT 比较及大数量渲染 | `EmiStackHelper`、`EmiStack`、`Comparison`、`EmiRenderHelper` |
| 桶、流体容器与 AE 包装流体的配方查询 | `dev.emi.emi.api.EmiApi` |
| 全局/服务器分组 JSON 读写与旧格式兼容 | `dev.emi.emi.runtime.EmiPersistentData` |
| 工作站多列布局、无槽位 AE 合成界面支持 | `RecipeScreen`、`EmiScreenBase` |
| 异步搜索烘焙、搜索就绪状态与进度显示 | `EmiSearch`、`EmiReloadManager`、`EmiScreenManager` |
| 延迟启动 JEI 的 JEMI 侧状态与消费入口 | `dev.emi.emi.jemi.JemiPlugin` |

`ItemEmiStack` / `FluidEmiStack` 内部复用规范化的 AE key，`getAEKey()` 提供该引用。`getKey()` 继续返回 `Item` / `Fluid`，以维持注册表适配、配方比较及第三方集成的既有语义。自定义 EMI 资源默认没有 AE key，仍走自身的 key/NBT 逻辑。

收藏缓存由 EMI 的收藏变更路径更新，包含磁盘加载、添加和删除；GTO 不再额外用 Mixin 重算同一份缓存。库存合成判定与配方填充恢复上游的原料数量语义，继续使用 GTO 的具体容器和可复用计数表。

## GTO 保留的适配

`GTOLib/src/main/java/com/gtolib/emi` 保留 GT/GTO 专用行为：GT 物品和配方收集、机器等级筛选、TagPrefix/通用电路、研究提示、玩家 AE 数据、合成站库存、GT 流体单元，以及 FTB 界面返回行为。Crazerium 分支的 GT 工具耐久与可合成次数注册入口已随对应扩展移除。

服务器收藏配置与服务器身份仍由 GTO 决定，再调用 EMI 的分组存储入口。身份尚未同步时，GTO 跳过写入并加载空数据，不能把未知身份写进全局收藏。Spark 启动分析的停止策略与 JEI 自身启动拦截也保留在 GTO。

新增通用逻辑时不要让 AE fork 反向依赖 `com.gtolib`、`com.gtocore` 或 GT 机器类；已迁入本体的实现不要再用 GTO overwrite 覆盖。

## 性能与参数约束

EMI 加载/重载中的逐配方、逐原料、逐标签操作按局部高频路径处理，不能因为入口低频而保留内部的 stream pipeline、装箱或重复构建集合。索引直接构建最终列表，注册对象使用引用容器，哈希对象使用哈希缓存容器；排序先缓存比较所需的数据，避免在 comparator 中反复遍历。

对外 EMI/JEI 协议保留必要的接口类型、Stream/Optional 返回值；内部使用具体容器、原始类型与显式循环。参数和空值约束通过 JetBrains `@NotNull`、`@Nullable`、`@Contract`、`@UnmodifiableView` 表达，直接共享只读缓存，不增加运行时防御检查或不可变包装。

## 跨仓验证与交付

先按各仓规则设置 JDK 21，验证 AE fork 的编译、聚焦测试和资源打包，再通过 Maven Local 做本机联调。主仓验证 `compileJava`、`compileKotlin`，修改 GTOLib 后必须完成 `buildGtolibProtected`。同时检查运行依赖里没有独立 `emi-forge`。

Maven Local 产物仅供本机验证。对其他环境交付前，需要发布含对应改动的 AE 版本；只有修改 GTOLib 时才需按子模块流程提交、推送并更新主仓 gitlink。客户端工作站布局、流体查询、收藏和搜索仍需游戏内回归。
