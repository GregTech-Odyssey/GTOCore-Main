package com.gtocore.common.pipe.mana;

import com.gtocore.common.data.GTOMaterials;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * 魔力管道的等级：按魔法金属分级，方块 id 与显示名都取材料名。
 * <p>
 * 顺序跟本包的魔法材料阶梯一致（活木 → 原始青铜 → 魔力钢 → 泰拉钢 → 源质钢 → 精灵钢 → 盖亚钢），流量按池算：
 * 活木 0.5 池/秒，原始青铜 1 池/秒，之后每档翻倍（一池 = 1000000 魔力）。
 */
public enum ManaPipeType implements IPipeType<ManaPipeProperties>, StringRepresentable {

    LIVINGWOOD("活木", GTOMaterials.Livingwood, 0.5D),
    ORIGINAL_BRONZE("原始青铜", GTOMaterials.OriginalBronze, 1D),
    MANASTEEL("魔力钢", GTOMaterials.Manasteel, 2D),
    TERRASTEEL("泰拉钢", GTOMaterials.Terrasteel, 4D),
    ELEMENTIUM("源质钢", GTOMaterials.Elementium, 8D),
    ALFSTEEL("精灵钢", GTOMaterials.Alfsteel, 16D),
    GAIASTEEL("盖亚钢", GTOMaterials.Gaiasteel, 32D);

    public static final ResourceLocation TYPE = GTCEu.id("mana");

    /** 一池魔力。 */
    private static final long MANA_PER_POOL = 1_000_000;

    public final String cnName;
    public final Material material;
    private final double poolsPerSecond;
    /** 本档共享的节点数据，管内所有同档管道共用同一份。 */
    public final ManaPipeProperties properties;

    ManaPipeType(String cnName, Material material, double poolsPerSecond) {
        this.cnName = cnName;
        this.material = material;
        this.poolsPerSecond = poolsPerSecond;
        this.properties = new ManaPipeProperties((long) (MANA_PER_POOL * poolsPerSecond));
    }

    @Override
    public float getThickness() {
        return 0.375F;
    }

    /** 本档每秒过多少池，供工具提示显示。 */
    public double poolsPerSecond() {
        return poolsPerSecond;
    }

    @Override
    public ManaPipeProperties modifyProperties(ManaPipeProperties baseProperties) {
        return properties;
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }

    @Override
    public @NotNull String getSerializedName() {
        return material.getName();
    }
}
