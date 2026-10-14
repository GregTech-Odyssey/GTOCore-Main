package com.gtocore.api.electrochemistry;

import com.gtolib.GTOCore;
import com.gtolib.api.lang.CNEN;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistry;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

public class Ions {

    public static final O2OOpenCacheHashMap<String, CNEN> NAME_TO_transkey = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;

    public static final GTRegistry.RL<IonDefinition> REGISTRY_KEY;

    static {
        // Recipe codecs are initialized from GTCEu's addon registration hook.
        REGISTRY_KEY = new GTRegistry.RL<>(GTOCore.id("ions"), false);
        REGISTRY_KEY.unfreeze();
    }

    private static IonDefinition register(String name, String cn, int charge, String symbol, int color) {
        var definition = new IonDefinition(name, charge, symbol, color);
        REGISTRY_KEY.register(GTOCore.id(name), definition);
        if (GTCEu.isDataGen()) {
            NAME_TO_transkey.put(definition.translationKey(), new CNEN(cn, name.replace('_', ' ')));
        }
        return definition;
    }

    public static void init() {}

    public static final IonDefinition PROTON = register("proton", "质子", 1, "H+", 0xF28C8C);
    public static final IonDefinition CHLORIDE = register("chloride", "氯离子", -1, "Cl-", 0x8BCC70);
    public static final IonDefinition SODIUM = register("sodium", "钠离子", 1, "Na+", 0xE9BE65);
    public static final IonDefinition RHODIUM = register("rhodium", "铑离子", 3, "Rh3+", 0xD7A9B5);
    public static final IonDefinition BERYLLIUM = register("beryllium", "铍离子", 2, "Be2+", 0x9EBDA6);
    public static final IonDefinition CUPRIC = register("cupric", "铜离子", 2, "Cu2+", 0x529DDF);
    public static final IonDefinition TITANIUM3 = register("titanium3", "钛离子(III)", 3, "Ti3+", 0xA88BDA);
    public static final IonDefinition HYDROXIDE = register("hydroxide", "氢氧根离子", -1, "OH-", 0x65C7CF);
    public static final IonDefinition GOLD_CHLORIDE = register("gold_chloride", "四氯金酸根离子", -1, "AuCl4-", 0xE8B84B);
    public static final IonDefinition SUPERCRITICAL_OXYGEN = register("supercritical_oxygen", "超临界氧离子", -2, "O2-", 0x8C8CF2);
    public static final IonDefinition ANIMIUM = register("animium", "灵铵离子", -1, "N(anima)4-", 0xD6B0E8);

    static {
        REGISTRY_KEY.freeze();
    }
}
