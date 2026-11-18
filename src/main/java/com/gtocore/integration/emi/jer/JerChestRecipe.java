package com.gtocore.integration.emi.jer;

import com.gtolib.GTOCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// 改写自 Just Enough Resources（way2muchnoise，"Don't Be a Jerk" 非商业许可）
// https://github.com/way2muchnoise/JustEnoughResources
final class JerChestRecipe implements EmiRecipe {

    private static final int WIDTH = 160;
    private static final int COLUMNS = 8;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 24;

    private static final Map<String, String> NAMES = Map.ofEntries(
            Map.entry("chests/abandoned_mineshaft", JerEmi.Lang.CHEST_ABANDONED_MINESHAFT),
            Map.entry("chests/desert_pyramid", JerEmi.Lang.CHEST_DESERT_PYRAMID),
            Map.entry("chests/jungle_temple", JerEmi.Lang.CHEST_JUNGLE_TEMPLE),
            Map.entry("chests/igloo_chest", JerEmi.Lang.CHEST_IGLOO),
            Map.entry("chests/stronghold_corridor", JerEmi.Lang.CHEST_STRONGHOLD_CORRIDOR),
            Map.entry("chests/stronghold_library", JerEmi.Lang.CHEST_STRONGHOLD_LIBRARY),
            Map.entry("chests/stronghold_crossing", JerEmi.Lang.CHEST_STRONGHOLD_CROSSING),
            Map.entry("chests/village_blacksmith", JerEmi.Lang.CHEST_VILLAGE_BLACKSMITH),
            Map.entry("chests/spawn_bonus_chest", JerEmi.Lang.CHEST_SPAWN_BONUS),
            Map.entry("chests/simple_dungeon", JerEmi.Lang.CHEST_SIMPLE_DUNGEON),
            Map.entry("chests/nether_bridge", JerEmi.Lang.CHEST_NETHER_BRIDGE),
            Map.entry("chests/end_city_treasure", JerEmi.Lang.CHEST_END_CITY),
            Map.entry("chests/woodland_mansion", JerEmi.Lang.CHEST_WOODLAND_MANSION),
            Map.entry("chests/village/village_weaponsmith", JerEmi.Lang.CHEST_VILLAGE_WEAPONSMITH),
            Map.entry("chests/village/village_toolsmith", JerEmi.Lang.CHEST_VILLAGE_TOOLSMITH),
            Map.entry("chests/village/village_armorer", JerEmi.Lang.CHEST_VILLAGE_ARMORER),
            Map.entry("chests/village/village_cartographer", JerEmi.Lang.CHEST_VILLAGE_CARTOGRAPHER),
            Map.entry("chests/village/village_mason", JerEmi.Lang.CHEST_VILLAGE_MASON),
            Map.entry("chests/village/village_shepherd", JerEmi.Lang.CHEST_VILLAGE_SHEPHERD),
            Map.entry("chests/village/village_butcher", JerEmi.Lang.CHEST_VILLAGE_BUTCHER),
            Map.entry("chests/village/village_fletcher", JerEmi.Lang.CHEST_VILLAGE_FLETCHER),
            Map.entry("chests/village/village_fisher", JerEmi.Lang.CHEST_VILLAGE_FISHER),
            Map.entry("chests/village/village_tannery", JerEmi.Lang.CHEST_VILLAGE_TANNERY),
            Map.entry("chests/village/village_temple", JerEmi.Lang.CHEST_VILLAGE_TEMPLE),
            Map.entry("chests/village/village_desert_house", JerEmi.Lang.CHEST_VILLAGE_DESERT_HOUSE),
            Map.entry("chests/village/village_plains_house", JerEmi.Lang.CHEST_VILLAGE_PLAINS_HOUSE),
            Map.entry("chests/village/village_taiga_house", JerEmi.Lang.CHEST_VILLAGE_TAIGA_HOUSE),
            Map.entry("chests/village/village_snowy_house", JerEmi.Lang.CHEST_VILLAGE_SNOWY_HOUSE),
            Map.entry("chests/village/village_savanna_house", JerEmi.Lang.CHEST_VILLAGE_SAVANNA_HOUSE),
            Map.entry("chests/underwater_ruin_small", JerEmi.Lang.CHEST_UNDERWATER_RUIN_SMALL),
            Map.entry("chests/underwater_ruin_big", JerEmi.Lang.CHEST_UNDERWATER_RUIN_BIG),
            Map.entry("chests/buried_treasure", JerEmi.Lang.CHEST_BURIED_TREASURE),
            Map.entry("chests/shipwreck_map", JerEmi.Lang.CHEST_SHIPWRECK_MAP),
            Map.entry("chests/shipwreck_supply", JerEmi.Lang.CHEST_SHIPWRECK_SUPPLY),
            Map.entry("chests/shipwreck_treasure", JerEmi.Lang.CHEST_SHIPWRECK_TREASURE),
            Map.entry("chests/pillager_outpost", JerEmi.Lang.CHEST_PILLAGER_OUTPOST),
            Map.entry("chests/bastion_treasure", JerEmi.Lang.CHEST_BASTION_TREASURE),
            Map.entry("chests/bastion_other", JerEmi.Lang.CHEST_BASTION_OTHER),
            Map.entry("chests/bastion_bridge", JerEmi.Lang.CHEST_BASTION_BRIDGE),
            Map.entry("chests/bastion_hoglin_stable", JerEmi.Lang.CHEST_BASTION_HOGLIN_STABLE),
            Map.entry("chests/ruined_portal", JerEmi.Lang.CHEST_RUINED_PORTAL),
            Map.entry("chests/ancient_city", JerEmi.Lang.CHEST_ANCIENT_CITY),
            Map.entry("chests/ancient_city_ice_box", JerEmi.Lang.CHEST_ANCIENT_CITY_ICE_BOX));

    private final ResourceLocation id;
    private final ResourceLocation table;
    private final List<JerLoot.Drop> drops;
    private final List<EmiStack> outputs;
    private final int minStacks;
    private final int maxStacks;

    private JerChestRecipe(ResourceLocation table, JerLoot.Table loot) {
        this.id = GTOCore.id("/jer/dungeon/" + table.getNamespace() + "/" + table.getPath());
        this.table = table;
        this.drops = sorted(loot.drops());
        List<EmiStack> out = new ArrayList<>(drops.size());
        for (JerLoot.Drop drop : drops) out.add(EmiStack.of(drop.stack()));
        this.outputs = out;
        this.minStacks = loot.minStacks();
        this.maxStacks = loot.maxStacks();
    }

    static List<JerChestRecipe> create(JerLootSync.Dataset data) {
        List<JerChestRecipe> recipes = new ArrayList<>(data.chests().size());
        for (JerLootSync.Chest chest : data.chests()) recipes.add(new JerChestRecipe(chest.id(), chest.table()));
        return recipes;
    }

    private static List<JerLoot.Drop> sorted(List<JerLoot.Drop> drops) {
        ReferenceOpenHashSet<Item> seen = new ReferenceOpenHashSet<>(drops.size());
        List<JerLoot.Drop> result = new ArrayList<>(drops.size());
        for (JerLoot.Drop drop : drops) {
            if (seen.add(drop.stack().getItem())) result.add(drop);
        }
        result.sort(Comparator.comparingDouble((JerLoot.Drop d) -> -d.sortIndex())
                .thenComparing(d -> BuiltInRegistries.ITEM.getKey(d.stack().getItem())));
        return result;
    }

    private Component name() {
        String key = "minecraft".equals(table.getNamespace()) ? NAMES.get(table.getPath()) : null;
        return key == null ? Component.literal(table.toString()) : Component.translatable(key);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return JerEmi.DUNGEON;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return Collections.emptyList();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return GRID_Y + (drops.size() + COLUMNS - 1) / COLUMNS * 18 + 2;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(name(), 2, 2, 0x404040, false);
        String stacks = minStacks == maxStacks ? Integer.toString(minStacks) : minStacks + " - " + maxStacks;
        widgets.addText(Component.translatable(JerEmi.Lang.STACKS, stacks), 2, 12, 0x404040, false);
        for (int i = 0; i < drops.size(); i++) {
            JerEmi.decorate(widgets.addSlot(outputs.get(i), GRID_X + (i % COLUMNS) * 18, GRID_Y + (i / COLUMNS) * 18).recipeContext(this), drops.get(i));
        }
    }
}
