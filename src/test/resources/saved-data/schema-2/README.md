# Schema 2 存档样本

这四个文件来自 2026-10-07 的隔离 Forge 维度探针存档，保留当时实际写出的字节，不通过当前编码器重建。

- `catalog.dat`：维度模板、系列和强加载候选目录。
- `instance.dat`：实例 `89ce535d-ef11-301b-aeef-044637837f7a`，包含种子、归属和出生点。
- `global-weather.dat`：主世界天气时钟和固定维度日程。
- `instance-weather.dat`：上述实例的独立天气日程。

维度文件使用 GZIP 和 `GTDC` 信封；天气文件使用未压缩的 `GTWC` 信封。迁移测试验证读取不改写原文件、保存升级为 schema 3，以及 pending 各提交边界的恢复。
