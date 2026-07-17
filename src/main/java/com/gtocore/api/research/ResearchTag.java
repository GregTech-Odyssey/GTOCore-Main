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
    private final long bytePerPoint;

    public static final Map<String, CNEN> LNAG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
    public static final O2OOpenCacheHashMap<String, ResearchTag> TAGS = new O2OOpenCacheHashMap<>();

    public ResearchTag(String name, String cn, String en, long bytePerPoint) {
        if (LNAG != null) {
            LNAG.put("gtocore.research.tag." + name, new CNEN(cn, en));
        }
        TAGS.put(name, this);
        this.name = name;
        var ran = RandomSource.create(name.hashCode());
        this.color = ran.nextInt(0xFFFFFF) | 0xFF000000;
        this.bytePerPoint = bytePerPoint;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable("gtocore.research.tag." + name).withStyle(style -> style.withColor(color));
    }

    public static final ResearchTag MATERIAL = new ResearchTag("material", "材料", "Material", 32);
    public static final ResearchTag DATA_ENGINEERING = new ResearchTag("data_engineering", "数据工程", "Data Engineering", 64);
    public static final ResearchTag COMPUTATION = new ResearchTag("computation", "计算", "Computation", 128);
    public static final ResearchTag THERMODYNAMICS = new ResearchTag("thermodynamics", "热力学", "Thermodynamics", 128);
    public static final ResearchTag ENERGY = new ResearchTag("energy", "能源", "Energy", 128);
    public static final ResearchTag ASSEMBLY = new ResearchTag("assembly", "组装", "Assembly", 128);
    public static final ResearchTag ALFHEIMY = new ResearchTag("alfheimy", "精灵", "Alfheimy", 256);
    public static final ResearchTag CATALYSIS = new ResearchTag("catalysis", "催化", "Catalysis", 256);
    public static final ResearchTag INTERSTELLAR_ENGINEERING = new ResearchTag("interstellar_engineering", "星际工程", "Interstellar Engineering", 512);
    public static final ResearchTag MECHANICS = new ResearchTag("mechanics", "机械", "Mechanics", 1024);
    public static final ResearchTag PARTICLE = new ResearchTag("particle", "粒子", "Particle", 4096);
    public static final ResearchTag BIOLOGY = new ResearchTag("biology", "生物", "Biology", 4096);
    public static final ResearchTag OPTICS = new ResearchTag("optics", "光学", "Optics", 2L << 15);
    public static final ResearchTag QUANTUM = new ResearchTag("quantum", "量子", "Quantum", 2L << 20);
    public static final ResearchTag EXOTIC = new ResearchTag("exotic", "奇异", "Exotic", 2L << 25);
    public static final ResearchTag SUPRACAUSAL = new ResearchTag("supracausal", "超因果", "Supracausal", 2L << 30);
}
