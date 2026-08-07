package com.gtocore.api.research;

import com.gtolib.api.lang.CNEN;
import com.gtolib.utils.ColorUtils;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;

import com.gto.datasynclib.datastream.codec.DataCodec;
import com.gto.datasynclib.util.Registry;
import com.gto.fastcollection.O2OOpenCacheHashMap;
import lombok.Getter;

import java.util.Map;

@Getter
public final class ResearchTag {

    private final String name;
    private final int color;
    private final long bytePerPoint;

    public static final Map<String, CNEN> LNAG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;
    /**
     * DataSyncLib 注册器：注册的全部 ResearchTag 以 name 为 key。freeze 后按 name 排序分配稳定整数 id，
     * 供网络流用紧凑 id 编解码（registry.streamCodec()）；持久化走 name key（registry.dataCodec()）。
     */
    public static final Registry<String, ResearchTag> TAGS = createRegistry();

    private static Registry<String, ResearchTag> createRegistry() {
        var registry = new Registry<String, ResearchTag>("research_tag", DataCodec.STRING_CODEC, t -> t.name);
        registry.unfreeze();
        return registry;
    }

    public ResearchTag(String name, String cn, String en, long bytePerPoint) {
        if (LNAG != null) {
            LNAG.put("gtocore.research.tag." + name, new CNEN(cn, en));
        }
        TAGS.register(name, this);
        this.name = name;
        var ran = RandomSource.create(name.hashCode() * 31L);
        this.color = ColorUtils.getInterpolatedColor(ran.nextInt(0xFFFFFF) | 0xFF000000, 0xFFFFFFFF, 0.5f);
        this.bytePerPoint = bytePerPoint;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable("gtocore.research.tag." + name);
    }

    public static final ResearchTag MATERIAL = new ResearchTag("material", "材料", "Material", 32);
    public static final ResearchTag DATA_STORAGE = new ResearchTag("data_storage", "数据存储", "Data Storage", 64);
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

    static {
        TAGS.freeze();
    }
}
