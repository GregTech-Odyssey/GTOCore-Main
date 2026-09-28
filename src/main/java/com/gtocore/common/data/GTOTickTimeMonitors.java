package com.gtocore.common.data;

import com.gtolib.api.lang.CNEN;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class GTOTickTimeMonitors {

    public final Map<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;

    public final TickTimeMonitor.Entry THREAD_RECIPE_LOGIC = create("thread_recipe_logic", "多线程配方逻辑", RecipeLogic.SEARCH_MAX_INTERVAL);

    public final TickTimeMonitor.Entry HEAT_CONDUCTION = create("heat_conduction", "热传导");
    public final TickTimeMonitor.Entry WIRELESS_ENERGY = create("wireless_energy", "无线能量抽取");
    // 物品 / 流体分开两个 entry：MEDualOutputPartMachine 同一个方块实体上两个 trait 都要监控，
    // 共用一个 entry 的话后注册的会拿到前一个的监控器（task 是前一个的，自己那份永远不跑）。
    public final TickTimeMonitor.Entry ME_OUTPUT_ITEM = create("me_output_item", "ME 物品输出");
    public final TickTimeMonitor.Entry ME_OUTPUT_FLUID = create("me_output_fluid", "ME 流体输出");
    public final TickTimeMonitor.Entry SPACE_STATION = create("space_station", "空间站");

    public final TickTimeMonitor.Entry PLATFORM_PLACEMENT = create("platform_placement", "平台部署");
    public final TickTimeMonitor.Entry ME_INPUT = create("me_input", "ME 输入");
    public final TickTimeMonitor.Entry ME_ENERGY = create("me_energy", "ME 能量");
    public final TickTimeMonitor.Entry ME_STORAGE = create("me_storage", "ME 存储访问");
    public final TickTimeMonitor.Entry ME_PULL = create("me_pull", "ME 拉取样板");
    public final TickTimeMonitor.Entry BEAM = create("beam", "束流");
    public final TickTimeMonitor.Entry RADIATION = create("radiation", "辐射");
    public final TickTimeMonitor.Entry REACTOR_HEAT = create("reactor_heat", "反应堆热系统");
    public final TickTimeMonitor.Entry RESEARCH_COMPUTATION = create("research_computation", "算力计算");
    public final TickTimeMonitor.Entry CWUT_MODIFICATION = create("cwut_modification", "算力上限刷新");
    public final TickTimeMonitor.Entry GENERATOR_INTAKE = create("generator_intake", "发电机进气");
    public final TickTimeMonitor.Entry MANA = create("mana", "魔力");
    public final TickTimeMonitor.Entry CRAFTING_INTERFACE = create("crafting_interface", "合成接口");

    public TickTimeMonitor.Entry create(String name, String cn, int window) {
        if (LANG != null) LANG.put("gtceu.top.tick_time." + name, CNEN.createToEnglishName(name, cn));
        return TickTimeMonitor.create(name, window);
    }

    public TickTimeMonitor.Entry create(String name, String cn) {
        if (LANG != null) LANG.put("gtceu.top.tick_time." + name, CNEN.createToEnglishName(name, cn));
        return TickTimeMonitor.create(name, TickTimeMonitor.TICK_TIME_WINDOW);
    }

    public static void init() {}
}
