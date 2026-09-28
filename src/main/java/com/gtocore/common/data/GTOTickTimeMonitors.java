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
