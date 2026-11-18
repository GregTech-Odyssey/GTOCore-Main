# EMI recipe pages rewritten from JEI integrations / 由 JEI 集成改写的 EMI 配方页

GTOCore provides native EMI pages for several mods whose recipe pages were previously shown through their JEI integrations. Code derived from those integrations keeps its original license; the project's own code license does not replace it.

GTOCore 为若干模组提供原生 EMI 页面，这些页面此前经各模组的 JEI 集成显示。由 JEI 集成改写的代码继续适用其原始许可证，不改用本项目自有代码的许可证。

## Derived code / 改写的代码

Each file under these packages names its origin in a two-line header above the class declaration. / 以下各包中的每个文件都在类声明前用两行注明出处。

| Package under `com.gtocore.integration.emi` | Upstream / 上游 | Author / 作者 | Revision / 版本 | License / 许可证 |
| --- | --- | --- | --- | --- |
| `apotheosis` | https://github.com/Shadows-of-Fire/Apotheosis | Shadows_of_Fire (Copyright (c) 2023 Brennan Ward) | branch `1.20`, `785d26e` (7.4.8) | MIT, `licenses/apotheosis-MIT.txt` |
| `arsnouveau` | https://github.com/baileyholl/Ars-Nouveau | Bailey Hollingsworth | branch `1.20`, as carried by [Ars-Nouveau-gto](https://github.com/GregTech-Odyssey/Ars-Nouveau-gto) `dd90ba32e` (4.12.29) | LGPL-3.0, full text in [`licenses/LGPL-3.0.txt`](https://github.com/GregTech-Odyssey/GTOCore-Main/tree/0.6.0/licenses) |
| `mythicbotany` | https://github.com/noeppi-noeppi/MythicBotany | noeppi_noeppi | branch `master`, `f3e9c94` (4.0) | Apache-2.0, `licenses/mythicbotany-Apache-2.0.txt` |
| `jer` | https://github.com/way2muchnoise/JustEnoughResources | way2muchnoise | branch `1.20.1`, `a2dff20` (1.4.0.247) | "Don't Be a Jerk" non-commercial care-free license, `licenses/jeresources-LICENSE.md` |

Changes / 改动：

- All four: the JEI categories, recipe wrappers and transfer handlers are rewritten against the EMI API; textures and language entries of the original mods are referenced at runtime and not copied, except for Just Enough Resources. / 四者的 JEI 类别、配方包装与配方转移处理器均按 EMI 接口重写；除 Just Enough Resources 外，原模组的贴图与语言条目在运行时引用，不复制。
- Apotheosis: potion charms are compared by potion and their crafting page lists one recipe per potion. / 药水护符按药水区分，合成页按药水逐条列出。
- Ars Nouveau: armor upgrades list one recipe per armor piece; the scry ritual category is not included. / 护甲升级按护甲逐件列出；不含占卜仪式类别。
- Just Enough Resources: the mob, chest and plant pages and the loot table conversion are carried over; the enchantment, villager and world generation pages, the API and the textures are not. Texts are taken from its language files. Chest tables cover every `chests/` loot table. / 保留生物、箱子、植物三页与战利品表换算；不含附魔、村民、世界生成页、对外接口与贴图。文字取自其语言文件。箱子清单扩展到全部 `chests/` 战利品表。

## Independent implementations / 独立实现

The following integrations call the public classes of the respective mods and contain no code from them. / 以下接入只调用对应模组的公开类，不含其代码。

| Package | Mod / 模组 | Author / 作者 | Link / 链接 | Mod license / 模组许可证 |
| --- | --- | --- | --- | --- |
| `com.gtocore.integration.emi.farmersrespite` | Farmer's Respite | Umpaz, Probleyes | https://www.curseforge.com/minecraft/mc-mods/farmers-respite | Marked MIT in the mod file, All Rights Reserved on its release pages / 模组文件标注 MIT，发布页标注保留所有权利 |
| `com.gtocore.integration.jade.JadeRecipeKeys` | Jade | Snownee | https://github.com/Snownee/Jade | CC BY-NC-SA 4.0 |
| `com.gtocore.integration.ftbquests.emi` | FTB Quests, FTB XMod Compat | FTB Team | https://github.com/FTBTeam/FTB-Quests , https://github.com/FTBTeam/FTB-XMod-Compat | All Rights Reserved |

The Farmer's Respite brewing page follows the layout of the original page and uses the textures shipped in that mod at runtime. / Farmer's Respite 冲泡页沿用原页面的布局，贴图在运行时取自该模组。
