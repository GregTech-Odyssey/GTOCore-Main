# Functional Storage assets / Functional Storage 素材来源

- Upstream / 上游: https://github.com/Buuz135/FunctionalStorage
- Revision / 固定提交: [`36dcd33b31ed7675eba733df6d1829bc7ac4196f`](https://github.com/Buuz135/FunctionalStorage/tree/36dcd33b31ed7675eba733df6d1829bc7ac4196f) (`main`, Minecraft 1.20.1).
- Copyright (c) 2021 Buuz135, Rid.
- License / 许可证: MIT. The complete, unchanged license is included in `licenses/functional-storage-MIT.txt` in this resource pack / JAR.
- These third-party assets retain their MIT license; the project's own code/artwork license does not replace it. / 这些第三方素材继续使用 MIT 许可证，不改用本项目自有代码或美术的许可证。

## Models / 模型

Source prefix: `src/main/resources/assets/functionalstorage/` at the revision above. [Browse original models](https://github.com/Buuz135/FunctionalStorage/tree/36dcd33b31ed7675eba733df6d1829bc7ac4196f/src/main/resources/assets/functionalstorage/models/block).

| Included file under `assets/gtocore/` | Upstream file |
| --- | --- |
| `models/block/wooden_super_storage/chest.json` | `models/block/base_x_1.json` |
| `models/block/wooden_super_storage/tank.json` | `models/block/fluid_1.json` |

Changes / 改动：资源引用迁入 `gtocore`；两种模型移除 Functional Storage 静态锁图标，由 GTCEu 显示实时锁定状态；正面内凹从 0.5 像素改为 1 像素，与 GTCEu 数量/锁定显示面对齐。流体模型的侧面、顶底和内壁共用各木材的 `side` 材质，正面保留上游玻璃材质。几何结构、UV 和物品展示变换来自上述模型。木材的 front/side 绑定参考上游 `models/block/<wood>_1.json`。

## Textures / 材质

[Browse original textures](https://github.com/Buuz135/FunctionalStorage/tree/36dcd33b31ed7675eba733df6d1829bc7ac4196f/src/main/resources/assets/functionalstorage/textures/block). All textures are copied byte-for-byte without modification. / 以下材质均原样复制，未改动像素。

Included directory: `assets/gtocore/textures/block/wooden_super_storage/`. Each file maps to `src/main/resources/assets/functionalstorage/textures/block/<same filename>` upstream.

| Filename / 文件名 | SHA-256 |
| --- | --- |
| `oak_side.png` | `bc9b91881bc27426182052c821b66e3789ffb2c1838e7c048d34614c6f61fa3b` |
| `oak_front_1.png` | `608b83b79ddf60849156395e738168493c5c30416a98e922e8f9e554c7600883` |
| `spruce_side.png` | `145f24da227a053ef6dc77a4f10bee8ca84084629c5a98a86ba05412ddfc1e2f` |
| `spruce_front_1.png` | `55c8abc567982b30c99002df2b5298fa96695a28f02e2a5c85d4f63657b854ae` |
| `birch_side.png` | `7aa5c73e883c0c525f2dab012509cb470781b701c02c2abeb240e4be03e196de` |
| `birch_front_1.png` | `d79c0fec9084f3380f1c26228bc16e3b5a5bcf1508badfaccbe4b0e0d7978bfd` |
| `jungle_side.png` | `6489baed14064f21e798109f8011e7a6fb1416afdfbf428d3aa98316a86cc449` |
| `jungle_front_1.png` | `1b5fafcc02edf9f3f4fd2d7f5d7ed107de86dc130175963386337786f9947de1` |
| `acacia_side.png` | `b22607a7637e4b1fbcc9b3b89b7cccaa72c116b615d174f354430fa2d4367a65` |
| `acacia_front_1.png` | `3a398728d59684dfd82ce0aad26f18dc372abab3b59452b74aad15af2e6ce34e` |
| `dark_oak_side.png` | `a29f5045d1ca82d3b0af75e505c3f57581721b883b25e8aa6c2776eef7c1dff8` |
| `dark_oak_front_1.png` | `26cc5d92528991d74ffb8faca6438a24bb07d1200e2714a15827c2938cfb30b8` |
| `crimson_side.png` | `45803f0e716e7f9693d57b199ae7ab7e4f51a75cf5914d2ac172b58dc9aab379` |
| `crimson_front_1.png` | `0920619d824ec97c282c288d19e7e6dd21761a0fae64e54c0cfe703f4079baa1` |
| `warped_side.png` | `0b30dc91bcc3856f4037d7f9a14afdcd3afd26957b5cb9e8254c854acca26b88` |
| `warped_front_1.png` | `cedaf902b796d9308ea8e41a63fa616bcd0c5bb467190631ed3c3ad0168d64c6` |
| `mangrove_side.png` | `4c7837e4bfad4aa245041ac21cbd23cdd0c5adf4fb83b67ce70efe838128e777` |
| `mangrove_front_1.png` | `1a7213df11da419a237dad3ac1bc8cf1bb2386e21ff9aa854507af7fd6c7e310` |
| `cherry_side.png` | `1cc7d2374835b63edbc5807dc444fa8f9a85fcd074c8a8db0fa76d8ad340442b` |
| `cherry_front_1.png` | `0ec770e2866844296723aa84a7052148c955dc3a4e6d4750a67640927fbc0651` |
| `fluid_front.png` | `8630fc667550b07f88b1c04f827a5b0152c80797be084f0bcc5c1f34f5baa756` |

## Partitioned storage models / 分格存储模型

`models/block/compartment_storage/wood_2.json` and `metal_2.json` derive from `models/block/fluid_2.json`; `wood_4.json` and `metal_4.json` derive from `models/block/fluid_4.json`, all at the pinned revision above. The 1x2 layout uses two rows; 2x2 uses four independent windows. Static lock elements and unused groups are removed. Glass is a front-only plane recessed by one pixel; frames, dividers and inner walls use the original wood or voltage casing texture with projected UVs. The metal 2x2 front reuses GTCEu screen glass. The 1x2 fronts use the project-supplied `textures/block/overlay/machine/overlay_screen_glass_16_32.png`, `overlay_qchest_emissive16_32.png` and `overlay_qtank_emissive16_32.png`; each 32x16 emissive frame supplies an 8x8 mark drawn separately from the glass. / 两格上下排列、四格为2x2。移除静态锁图标和未使用的分组；玻璃仅保留内凹一像素的正面平面，外框、分隔条和内壁沿用原木材或电压外壳材质，UV 按实际坐标投影。金属 2x2 版沿用 GTCEu 玻璃；1x2 版使用项目提供的上述三张纹理，从每个 32x16 发光帧截取 8x8 图案并独立绘制。
