# 木质超级箱与超级缸

注册位于 `GTOLib/src/main/java/com/gtolib/api/data/WoodenSuperStorageRegistration.java`，由现有 `SuperStorageRegistration.init()` 加载。使用 `gtceu` 命名空间：

- 超级箱：`gtceu:<wood>_super_chest`，每个最多存放 **8192 个物品**，不按物品堆叠上限缩减容量。
- 超级缸：`gtceu:<wood>_super_tank`，每个最多存放 **256 桶（256000 mB）**；方块和手持物品的容量一致。
- `<wood>` 为 `oak`、`spruce`、`birch`、`jungle`、`acacia`、`dark_oak`、`crimson`、`warped`、`mangrove`、`cherry`，与 Functional Storage 1.20.1 的十种木材一致。

机器沿用 GTM fork 的 `QuantumChestMachine` / `QuantumTankMachine`，支持原有锁定、溢出销毁、自动输出、拆除保存及超级箱控制器连接。最大容量也作为界面容量上限。超级缸使用 `QuantumTankMachineItem` 并登记 `TANK_CAPACITY`，以保持物品流体能力与放置后的容量一致。

超级箱用对应木板包围箱子合成；超级缸用对应木板、玻璃和空桶合成。配方由 `MetaTileEntityLoader` 调用注册。

模型和材质来自 [Functional Storage](https://github.com/Buuz135/FunctionalStorage)，上游提交、原始文件映射、模型适配说明和 PNG 校验值见 [素材来源清单](../src/main/resources/licenses/functional-storage-assets.md)，完整 MIT 许可证见 [functional-storage-MIT.txt](../src/main/resources/licenses/functional-storage-MIT.txt)。两份文件随模组资源一起打包，第三方素材保留 MIT 许可。

`WoodenSuperStorageGameTests` 覆盖全部木材的斧头采掘标签、方块模拟/实际插入、容量溢出拒绝、手持超级缸容量及控制器聚合访问。修改后按仓库 JDK 21 约定执行 `runData`、`runGameTestServer`、`spotlessCheck` 和 `buildGtolibProtected`。
