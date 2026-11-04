# 依赖模组 fork 的修改位置

仅在定位或修复 GTM、AE2、ExtendedAE、EMI、Botania、Ars Nouveau 行为、考虑为其添加 GTOCore Mixin，或调整这些依赖时读取本文。

本项目维护以下模组的 fork：

| 模组 | fork 仓库 |
| --- | --- |
| GTM | [GregTech-Modern](https://github.com/GregTech-Odyssey/GregTech-Modern) |
| AE2 | [Applied-Energistics-2-gto](https://github.com/GregTech-Odyssey/Applied-Energistics-2-gto) |
| ExtendedAE | [ExtendedAE](https://github.com/GregTech-Odyssey/ExtendedAE) |
| EMI | [EMI-gto](https://github.com/GregTech-Odyssey/EMI-gto) |
| Botania | [Botania-gto](https://github.com/GregTech-Odyssey/Botania-gto) |
| Ars Nouveau | [Ars-Nouveau-gto](https://github.com/GregTech-Odyssey/Ars-Nouveau-gto) |

主仓通过 `build.gradle.kts` 依赖块中的 Maven 坐标引用 fork，不会自动读取任何本地 fork 工作区。其中 GTM、AE2、ExtendedAE 的版本分别由 `gradle.properties` 中的 `gtceu_version`、`ae2_version`、`extendedae_version` 控制。需要访问源码时，默认在主仓同一父目录下查找与上表仓库同名的目录。

ExtendedAE 使用 `com.glodblock.github.extendedae:extendedae-forge-1.20.1`，依赖项目的 AE2、EMI fork。其通用兼容逻辑直接在 ExtendedAE 源码维护，不在 AE2 或 Core 重复添加 Mixin；Core 的分级接口、GT 机器规则、传送与专属界面等集成仍由 Core 维护。

## 选择修改位置

- 先沿调用链确认问题的实际归属，并检查主仓已有兼容逻辑。若缺陷或目标行为属于上述模组自身，优先考虑直接修改对应 fork；不因任务从 GTOCore 发起就默认在主仓新增 Mixin 或重复修复。
- 若行为仅属于 GTOCore 的配方、集成或适配，则在主仓修改。若两边都需改动，分别处理并验证组合效果。
- 按影响范围、与 fork 其他使用者的兼容性、性能、维护成本和版本发布时机权衡。Mixin 可以用于确需主仓局部拦截、fork 行为不宜改变或暂时无法交付 fork 改动的情况；说明选择原因及后续是否需移除。
- 确认修复需要改 fork 时，只检查本次需要的对应仓库，先确认上述同级目录是该 fork 的可用检出。若找不到，不要自行克隆；停下并向用户报告缺少哪个仓库，请用户给出已有检出的实际路径，或决定是否需要克隆。等待路径时，可继续完成不依赖该源码的主仓工作。

## 跨仓库交付

- fork 与 GTOCore 是独立仓库。修改 fork 后，先按该仓库的说明构建和验证，再确认主仓实际解析的是包含该改动的版本；本地未发布的结果只用于本机联调。
- 主仓若依赖新 fork 行为，向他人交付前须确保对应源码提交及依赖产物可获取，并同步主仓版本。GTM 的 Maven Local 联调与共享发布要求见 [本地 GTM 联调](local-gtm.md)；AE2 的构建与发布方式应以其 fork 当前配置为准，不沿用 GTM 命令。
