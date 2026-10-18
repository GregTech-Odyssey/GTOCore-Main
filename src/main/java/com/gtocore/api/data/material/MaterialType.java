package com.gtocore.api.data.material;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterEnumLang;
import com.gtolib.api.data.chemical.material.GTOMaterial;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import lombok.Getter;

/**
 * 按 ForEachMaterial 的入口和材料加工分支分类，不按化学成分、名称或图标分类。
 * 局部禁用熔炼、加工或分解的标记不等于整个材料退出自动配方生成。
 */
@DataGeneratorScanned
@RegisterEnumLang(keyPrefix = "gtocore.material.base_type")
@Getter
public enum MaterialType {

    INGOT("非热加工锭型材料", "Ingot"),
    SMELTING_INGOT("热加工锭型材料", "Ingot (Furnace Smelting)"),
    BLAST_INGOT("熔铸锭型材料", "Ingot (Blast Smelting)"),
    POLYMER("高分子材料", "Polymer"),
    GEM("宝石/晶体材料", "Gem/Crystal"),
    CERAMIC("陶瓷材料", "Ceramic"),
    COMPOSITE("复合材料", "Composite"),
    WOOD("木质材料", "Wood"),
    DUST("一般粉体材料", "General Dust"),
    FLUID("纯流体材料", "Fluid Only"),
    OTHER("其他材料", "Other");

    @RegisterEnumLang.CnValue("name")
    private final String cn;
    @RegisterEnumLang.EnValue("name")
    private final String en;

    MaterialType(String cn, String en) {
        this.cn = cn;
        this.en = en;
    }

    public static MaterialType get(Material material) {
        return ((GTOMaterial) material).gtocore$getBaseType();
    }

    /** 在 ForEachMaterial 的已有遍历中，按完成注册后的属性刷新材料字段。 */
    public static void init(Material material) {
        ((GTOMaterial) material).gtocore$setBaseType(classify(material));
    }

    public static MaterialType classify(Material material) {
        if (material.isNull() || material.hasFlag(MaterialFlags.NO_UNIFICATION)) return OTHER;

        // 与 ForEachMaterial 一致：没有粉体属性的材料只进入分解分支。
        if (!material.hasProperty(PropertyKey.DUST)) {
            return material.hasProperty(PropertyKey.FLUID) ? FLUID : OTHER;
        }

        // 专门的材料族优先于它们共用的 INGOT / GEM 属性。
        if (material.hasFlag(GTOMaterialFlags.GENERATE_CERAMIC)) return CERAMIC;
        if (material.hasFlag(GTOMaterialFlags.COMPOSITE_MATERIAL)) return COMPOSITE;
        if (material.hasProperty(PropertyKey.WOOD)) return WOOD;
        if (material.hasProperty(PropertyKey.POLYMER)) return POLYMER;

        // 对应 processDust 的 GEM -> INGOT -> 一般粉体分支。
        if (material.hasProperty(PropertyKey.GEM) && !material.hasFlag(GTOMaterialFlags.DISABLE_GEM_RECIPES)) return GEM;
        if (material.hasProperty(PropertyKey.INGOT)) {
            if (material.hasFlag(MaterialFlags.FLAMMABLE) || material.hasFlag(MaterialFlags.NO_SMELTING)) return INGOT;
            if (material.getBlastTemperature() > 0) return BLAST_INGOT;
            return material.hasFlag(MaterialFlags.IS_MAGNETIC) ? INGOT : SMELTING_INGOT;
        }
        return DUST;
    }
}
