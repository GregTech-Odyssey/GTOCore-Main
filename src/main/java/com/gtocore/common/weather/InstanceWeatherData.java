package com.gtocore.common.weather;

import com.gtolib.api.dimension.DimensionDataIO;
import com.gtolib.api.dimension.DimensionManager;
import com.gtolib.api.dimension.InstanceDescriptor;
import com.gtolib.api.misc.FastSavedData;
import com.gtolib.utils.iostream.DataIOStream;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * 按动态实例独立保存的天气日程，不加入全局历史世界天气表。
 * <p>
 * 运行实例复用世界 SavedData；休眠实例查询只读写其天气元数据，不加载地形或实体。
 */
final class InstanceWeatherData extends FastSavedData {

    private static final String NAME = "gtocore_instance_weather";
    private final WeatherTimeline timeline;

    private InstanceWeatherData(WeatherTimeline timeline) {
        this.timeline = timeline;
        timeline.onChange(this::setDirty);
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
    static WeatherTimeline timeline(MinecraftServer server, InstanceDescriptor descriptor, long clock,
                                    WeatherProfile profile) {
        var level = server.getLevel(descriptor.dimension());
        DimensionDataStorage storage = level == null ?
                DimensionManager.get(server).metadataStorage(descriptor.dimension()) : level.getDataStorage();
        var data = FastSavedData.get(NAME, storage, InstanceWeatherData::load,
                () -> new InstanceWeatherData(new WeatherTimeline(descriptor.seed())));
        data.timeline.extend(profile, clock);
        data.setDirty();
        if (level == null) {
            try {
                DimensionDataIO.writeFastAtomic(storage.getDataFile(NAME).toPath(), data);
            } catch (IOException exception) {
                throw new UncheckedIOException("Cannot save dormant instance weather", exception);
            }
            data.setDirty(false);
        }
        return data.timeline;
    }

    static InstanceWeatherData load(DataIOStream stream) throws IOException {
        return new InstanceWeatherData(WeatherDataIO.readInstance(stream));
    }

    /**
     * 保存实例天气日程。
     *
     * @param stream FastSavedData 提供的二进制输出流
     * @throws IOException 天气日程无法写入
     */
    @Override
    public void save(DataIOStream stream) throws IOException {
        WeatherDataIO.writeInstance(stream, timeline);
    }
}
