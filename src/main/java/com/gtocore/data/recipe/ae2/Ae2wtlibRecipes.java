package com.gtocore.data.recipe.ae2;

import com.gtocore.common.data.GTOItems;
import com.gtocore.integration.ae.wtlib.WFTMenu;
import com.gtocore.integration.ae.wtlib.WRTMenu;

import com.gtolib.GTOCore;
import com.gtolib.api.ae2.me2in1.Wireless;

import com.gregtechceu.gtceu.common.data.GTRecipes;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import appeng.core.definitions.AEItems;

import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import de.mari_023.ae2wtlib.AE2wtlib;
import de.mari_023.ae2wtlib.wut.recipe.Combine;
import de.mari_023.ae2wtlib.wut.recipe.Upgrade;

public final class Ae2wtlibRecipes {

    public static void init() {
        Terminal[] own = {
                new Terminal(Wireless.ID, GTOItems.WIRELESS_ME2IN1),
                new Terminal(WRTMenu.ID, GTOItems.WIRELESS_WRT),
                new Terminal(WFTMenu.ID, GTOItems.WIRELESS_WFT)
        };
        Terminal[] others = {
                new Terminal("crafting", AEItems.WIRELESS_CRAFTING_TERMINAL),
                new Terminal("pattern_encoding", AE2wtlib.PATTERN_ENCODING_TERMINAL),
                new Terminal("pattern_access", AE2wtlib.PATTERN_ACCESS_TERMINAL),
                new Terminal("ex_crafting", EPPItemAndBlock.WIRELESS_EX_CT),
                new Terminal("ex_pattern_access", EPPItemAndBlock.WIRELESS_EX_PAT)
        };
        for (int i = 0; i < own.length; i++) {
            var a = own[i];
            var id = GTOCore.id(a.name);
            GTRecipes.RECIPE_MAP.put(id, new Upgrade(Ingredient.of(a.item), a.name, id));
            for (int j = i + 1; j < own.length; j++) {
                combine(a, own[j]);
            }
            for (var b : others) {
                combine(a, b);
            }
        }
    }

    private static void combine(Terminal a, Terminal b) {
        var id = GTOCore.id(a.name + "_combined_with_" + b.name);
        GTRecipes.RECIPE_MAP.put(id, new Combine(Ingredient.of(a.item), Ingredient.of(b.item), a.name, b.name, id));
    }

    private record Terminal(String name, ItemLike item) {}
}
