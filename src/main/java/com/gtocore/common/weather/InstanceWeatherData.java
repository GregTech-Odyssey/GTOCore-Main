package com.gtocore.common.weather;

import com.gtolib.api.dimension.DimensionManager;
import com.gtolib.api.dimension.InstanceDescriptor;
import com.gtolib.api.dimension.InstanceStore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * 按动态实例独立保存的天气日程，不加入全局历史世界天气表。
 * <p>
 * 运行实例复用世界 SavedData；休眠实例查询只读写其天气元数据，不加载地形或实体。
 */
final class InstanceWeatherData extends SavedData {

    private static final String NAME = "gtocore_instance_weather";
    private final WeatherTimeline timeline;

    private InstanceWeatherData(WeatherTimeline timeline) {
        this.timeline = timeline;
    }

    /**
     * 取得并扩展实例天气日程；休眠时原子写入该实例文件，运行时交由正常世界保存。
     *
     * @param server     所属服务器
     * @param descriptor 实例描述，提供种子和存档地址
     * @param clock      当前天气时钟
     * @param profile    模板天气配置
     * @return 与实例独立种子关联的天气日程
     * @throws UncheckedIOException 休眠天气元数据无法保存
     */
    static WeatherTimeline timeline(MinecraftServer server, InstanceDescriptor descriptor, long clock, WeatherProfile profile) {
        var level = server.getLevel(descriptor.dimension());
        DimensionDataStorage storage = level == null ? new DimensionDataStorage(DimensionManager.get(server).dimensionPath(descriptor.dimension()).resolve("data").toFile(), server.getFixerUpper()) : level.getDataStorage();
        var data = storage.computeIfAbsent(tag -> new InstanceWeatherData(WeatherTimeline.load(tag.getCompound("timeline"))),
                () -> new InstanceWeatherData(new WeatherTimeline(descriptor.seed())), NAME);
        data.timeline.extend(profile, clock);
        data.setDirty();
        if (level == null) {
            var tag = NbtUtils.addCurrentDataVersion(new CompoundTag());
            tag.put("data", data.save(new CompoundTag()));
            try {
                InstanceStore.writeAtomic(storage.getDataFile(NAME).toPath(), tag);
            } catch (IOException exception) {
                throw new UncheckedIOException("Cannot save dormant instance weather", exception);
            }
            data.setDirty(false);
        }
        return data.timeline;
    }

    /**
     * 保存实例天气日程。
     *
     * @param tag SavedData 提供的目标标签
     * @return 写入天气日程后的同一标签
     */
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("timeline", timeline.save());
        return tag;
    }
}
