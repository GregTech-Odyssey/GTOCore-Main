package com.gtocore.api.research;

import com.gtolib.api.lang.CNEN;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;

import com.gto.fastcollection.O2OOpenCacheHashMap;
import lombok.Getter;

import java.util.Map;

@Getter
public final class ResearchTag {

    private final String name;
    private final int color;

    public static final Map<String, CNEN> LNAG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
    public static final O2OOpenCacheHashMap<String, ResearchTag> TAGS = new O2OOpenCacheHashMap<>();

    public ResearchTag(String name, String cn, String en) {
        if (LNAG != null) {
            LNAG.put("gtocore.research.tag." + name, new CNEN(cn, en));
        }
        TAGS.put(name, this);
        this.name = name;
        var ran = RandomSource.create(name.hashCode());
        this.color = ran.nextInt(0xFFFFFF) | 0xFF000000;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable("gtocore.research.tag." + name);
    }

    public static final ResearchTag MATERIAL = new ResearchTag("material", "材料", "Material");
    public static final ResearchTag COMPUTATION = new ResearchTag("computation", "计算", "Computation");
    public static final ResearchTag THERMODYNAMICS = new ResearchTag("thermodynamics", "热力学", "Thermodynamics");
    public static final ResearchTag ENERGY = new ResearchTag("energy", "能源", "Energy");
    public static final ResearchTag MECHANICS = new ResearchTag("mechanics", "机械", "Mechanics");
    public static final ResearchTag ALCHEMY = new ResearchTag("alchemy", "炼金", "Alchemy");
    public static final ResearchTag ALFHEIMY = new ResearchTag("alfheimy", "精灵", "Alfheimy");
    public static final ResearchTag ASTRONOMY = new ResearchTag("astronomy", "天文学", "Astronomy");
    public static final ResearchTag QUANTUM = new ResearchTag("quantum", "量子", "Quantum");
    public static final ResearchTag CATALYSIS = new ResearchTag("catalysis", "催化", "Catalysis");
    public static final ResearchTag PARTICLE = new ResearchTag("particle", "粒子", "Particle");
    public static final ResearchTag BIOLOGY = new ResearchTag("biology", "生物", "Biology");
    public static final ResearchTag OPTICS = new ResearchTag("optics", "光学", "Optics");
    public static final ResearchTag EXOTIC = new ResearchTag("exotic", "奇异", "Exotic");
    public static final ResearchTag SUPRACAUSAL = new ResearchTag("supracausal", "超因果", "Supracausal");
}
