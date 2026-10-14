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
 * 顺序跟本包的魔法材料阶梯一致（魔力钢 → 泰拉钢 → 源质钢 → 精灵钢 → 盖亚钢），流量按池算：
 * 魔力钢 0.1 池/秒起步，每档翻倍（一池 = 1000000 魔力）。
 */
public enum ManaPipeType implements IPipeType<ManaPipeProperties>, StringRepresentable {

    MANASTEEL("魔力钢", GTOMaterials.Manasteel),
    TERRASTEEL("泰拉钢", GTOMaterials.Terrasteel),
    ELEMENTIUM("源质钢", GTOMaterials.Elementium),
    ALFSTEEL("精灵钢", GTOMaterials.Alfsteel),
    GAIASTEEL("盖亚钢", GTOMaterials.Gaiasteel);

    public static final ResourceLocation TYPE = GTCEu.id("mana");

    /** 一池魔力。 */
    private static final long MANA_PER_POOL = 1_000_000;
    /** 第一档的每秒流量：0.1 池。 */
    private static final long BASE_MANA = MANA_PER_POOL / 10;

    public final String cnName;
    public final Material material;
    /** 本档共享的节点数据，管内所有同档管道共用同一份。 */
    public final ManaPipeProperties properties;

    ManaPipeType(String cnName, Material material) {
        this.cnName = cnName;
        this.material = material;
        this.properties = new ManaPipeProperties(BASE_MANA << ordinal());
    }

    @Override
    public float getThickness() {
        return 0.375F;
    }

    /** 本档每秒过多少池，供工具提示显示。 */
    public double poolsPerSecond() {
        return properties.manaPerSecond() / (double) MANA_PER_POOL;
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
