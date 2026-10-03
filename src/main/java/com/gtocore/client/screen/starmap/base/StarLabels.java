package com.gtocore.client.screen.starmap.base;

import com.gregtechceu.gtceu.uipro.render.UIText;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

@OnlyIn(Dist.CLIENT)
public final class StarLabels {

    private static final int MAX_ENTRIES = 4096;

    private final Reference2IntOpenHashMap<Component> widths = new Reference2IntOpenHashMap<>();
    private int generation = Integer.MIN_VALUE;

    public StarLabels() {
        widths.defaultReturnValue(-1);
    }

    public int width(Component text) {
        if (generation != UIText.generation() || widths.size() >= MAX_ENTRIES) {
            widths.clear();
            generation = UIText.generation();
        }
        int width = widths.getInt(text);
        if (width < 0) {
            width = UIText.width(text);
            widths.put(text, width);
        }
        return width;
    }
}
