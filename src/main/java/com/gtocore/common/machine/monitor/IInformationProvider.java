package com.gtocore.common.machine.monitor;

import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public interface IInformationProvider extends IMonitor, IFancyUIMachine {

    /// 这个将会被客户端所用到！！！
    default DisplayComponentList provideInformation() {
        return new DisplayComponentList(getSortedRLs());
    }

    /// 提供一个默认的可用ResourceLocation列表
    /// 如果没有自定义的显示顺序更改，则会使用这个默认列表的顺序。
    default List<ResourceLocation> getAvailableRLs() {
        return new ArrayList<>();
    }

    /// 返回一个排序后的ResourceLocation列表，可以修改或删除部分默认的ResourceLocation，但不能增添新的
    List<ResourceLocation> getSortedRLs();

    /// 实现此方法以同步需要显示的信息到客户端
    /// 每半秒执行一次
    void syncInfoFromServer();

    default long getPriority() {
        return 0; // Default priority, can be overridden
    }

    default void setPriority(long current) {
        // Default implementation does nothing, can be overridden
    }
}
