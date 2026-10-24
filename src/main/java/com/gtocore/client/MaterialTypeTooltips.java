package com.gtocore.client;

import com.gtocore.api.data.material.MaterialType;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MaterialTypeTooltips {

    private static final ResourceLocation ICON_FONT = GTOCore.id("material_type");
    private static final Component[] LINES = buildLines();

    private MaterialTypeTooltips() {}

    public static Component of(Material material) {
        return LINES[MaterialType.get(material).ordinal()];
    }

    private static Component[] buildLines() {
        var types = MaterialType.values();
        var lines = new Component[types.length];
        for (var type : types) {
            // 图集自上而下与 MaterialType 的枚举顺序一致，每格 10×10。
            var icon = Component.literal(String.valueOf((char) ('\uE000' + type.ordinal())))
                    .withStyle(style -> style.withFont(ICON_FONT).withColor(ChatFormatting.WHITE));
            lines[type.ordinal()] = Tips.stat(Component.empty().append(icon).append(" ")
                    .append(Component.translatable("gtocore.material.base_type.name." + type.name())));
        }
        return lines;
    }
}
