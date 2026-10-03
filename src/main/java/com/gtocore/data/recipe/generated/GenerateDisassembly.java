package com.gtocore.data.recipe.generated;

import com.gtocore.common.data.GTOItems;
import com.gtocore.data.tag.Tags;

import com.gtolib.GTOCore;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.ItemUtils;

import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.recipe.GTRecipeBuilder;
import com.gregtechceu.gtceu.api.recipe.content.ContentList;
import com.gregtechceu.gtceu.api.recipe.content.KeyIngredient;
import com.gregtechceu.gtceu.api.transfer.key.Keys;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.stacks.AEItemKey;

import com.gto.fastcollection.fastutil.OpenCacheHashSet;

import java.util.Collection;
import java.util.Set;

import static com.gtocore.common.data.GTORecipeTypes.*;

public final class GenerateDisassembly {

    public static final Set<ResourceLocation> DISASSEMBLY_RECORD = new OpenCacheHashSet<>();

    public static final Set<ResourceLocation> DISASSEMBLY_BLACKLIST = new OpenCacheHashSet<>();

    private static final String[] outputItem = { "_frame", "_fence", "_electric_motor",
            "_electric_pump", "_conveyor_module", "_electric_piston", "_robot_arm", "_field_generator",
            "_emitter", "_sensor", "smd_", "_lamp", "_integrated_control_core", "ae2:blank_pattern",
            "gtocore:carbon_nanites", "gtmthings:virtual_item_provider", "gtocore:me_wildcard_pattern_buffer",
            "enriched_naquadah_trinium_europium_duranide_single_wire",
            "ruthenium_trinium_americium_neutronate_single_wire" };

    private static boolean isExcludeItems(String id) {
        for (String pattern : outputItem) {
            if (id.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    public static void generateDisassembly(GTRecipeBuilder recipeBuilder) {
        long eut = recipeBuilder.EUt();
        if (eut < 1) return;
        var c = recipeBuilder.getItemOutputs();
        if (c.isEmpty()) {
            GTOCore.LOGGER.error("配方{}没有输出", recipeBuilder.getId());
            return;
        }
        var outIng = c.ingredient(0);
        if (outIng.kind != KeyIngredient.BASE || !(outIng.key() instanceof AEItemKey outKey)) return;
        var item = outKey.getItem();
        var amount = Keys.saturatedInt(c.amount(0));
        if (recipeBuilder.getRecipeType() == LASER_WELDER_RECIPES && !(item instanceof MetaMachineItem)) {
            return;
        }
        ResourceLocation id = ItemUtils.getIdLocation(item);
        if (DISASSEMBLY_BLACKLIST.contains(id)) return;
        boolean cal = recipeBuilder.getRecipeType() == CIRCUIT_ASSEMBLY_LINE_RECIPES;
        ResourceLocation typeid = RecipeBuilder.getTypeID(id, DISASSEMBLY_RECIPES);
        if (cal && RecipeBuilder.get(typeid) != null) return;
        if ((!cal && DISASSEMBLY_RECORD.remove(id)) || isExcludeItems(id.toString())) {
            DISASSEMBLY_BLACKLIST.add(id);
            RecipeBuilder.remove(typeid);
            return;
        }
        RecipeBuilder builder = DISASSEMBLY_RECIPES.recipeBuilder(id)
                .inputItems(item, amount)
                .duration(recipeBuilder.getDuration())
                .EUt(eut);
        boolean hasOutput = false;
        var itemList = recipeBuilder.getItemInputs();
        var fluidList = recipeBuilder.getFluidInputs();
        for (int i = 0, n = itemList.size(); i < n; i++) {
            if (itemList.chance(i) != ContentList.MAX_CHANCE) continue;
            var input = itemList.ingredient(i);
            long count = itemList.amount(i);
            if (input.kind == KeyIngredient.BASE) {
                builder.outputItems(input, count);
                hasOutput = true;
            } else if (input.kind == KeyIngredient.TAG && input.tag() != null) {
                Integer c1 = Tags.CIRCUITS_ARRAY.get(input.itemTagKey());
                if (c1 != null) builder.outputItems(GTOItems.UNIVERSAL_CIRCUIT[c1].get(), Keys.saturatedInt(count));
            } else if (input.source() != null) {
                a:
                for (Ingredient.Value value : input.source().values) {
                    if (value instanceof Ingredient.ItemValue itemValue) {
                        Collection<ItemStack> stacks = itemValue.getItems();
                        if (stacks.size() == 1) {
                            for (ItemStack stack : stacks) {
                                if (!stack.isEmpty() && !stack.hasTag()) {
                                    builder.outputItems(input, count);
                                    hasOutput = true;
                                    break a;
                                }
                            }
                        }
                    } else if (value instanceof Ingredient.TagValue tagValue) {
                        Integer c1 = Tags.CIRCUITS_ARRAY.get(tagValue.tag);
                        if (c1 != null) {
                            builder.outputItems(GTOItems.UNIVERSAL_CIRCUIT[c1].get(), Keys.saturatedInt(count));
                            break;
                        }
                    }
                }
            }
        }
        for (int i = 0, n = fluidList.size(); i < n; i++) {
            if (fluidList.chance(i) == ContentList.MAX_CHANCE) {
                builder.outputFluids(fluidList.ingredient(i), fluidList.amount(i));
                hasOutput = true;
            }
        }
        if (hasOutput) builder.save();
        DISASSEMBLY_RECORD.add(id);
    }
}
