package com.gtocore.client;

import com.gtocore.api.data.SpaceResourceIndex;
import com.gtocore.api.data.SpaceResourceIndex.Entry;
import com.gtocore.api.data.SpaceResourceIndex.Type;

import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * 为可由太空资源获取类配方（采矿、钻井、浮游物质收集、行星气体抽取、虚空集气）
 * 得到的物品与流体追加获取方式，按住 Ctrl 展开，按配方类型分组。
 * <p>
 * 折叠提示与展开后的全部行都按物品/流体缓存，且由可翻译组件构成，
 * 悬停时不再重复构造，切换语言也无需失效。
 */
@OnlyIn(Dist.CLIENT)
public final class SpaceResourceTooltips {

    private static final String PREFIX = "gtocore.tooltip.space_resource.";
    private static final String HOLD_CTRL_KEY = PREFIX + "hold_ctrl";
    private static final String TITLE_KEY = PREFIX + "title";
    private static final String TITLE_SECONDARY_KEY = PREFIX + "title_secondary";
    private static final String DRONE_KEY = PREFIX + "drone";
    private static final String CIRCUIT_KEY = PREFIX + "circuit";
    private static final String NO_CIRCUIT_KEY = PREFIX + "no_circuit";
    private static final String FUEL_KEY = PREFIX + "fuel";
    private static final String GALAXY_KEY = PREFIX + "galaxy";
    private static final String DIMENSION_KEY = PREFIX + "dimension";
    private static final String CHANCE_KEY = PREFIX + "chance";
    private static final String CHANCE_BOOST_KEY = PREFIX + "chance_boost";

    private static final Component[] TYPE_NAMES = typeNames();
    private static final Component SEPARATOR = Component.literal(" / ").withStyle(ChatFormatting.GRAY);

    /** 第 0 行永远是折叠时显示的那行提示，其余为展开后的内容。 */
    private static final Reference2ObjectOpenHashMap<Item, ObjectList<Component>> ITEM_CACHE = new Reference2ObjectOpenHashMap<>();
    private static final Reference2ObjectOpenHashMap<Fluid, ObjectList<Component>> FLUID_CACHE = new Reference2ObjectOpenHashMap<>();

    private SpaceResourceTooltips() {}

    public static void append(Item item, List<Component> tooltips) {
        ObjectList<Component> lines = ITEM_CACHE.get(item);
        if (lines == null) {
            lines = build(SpaceResourceIndex.getByMaterial(item), SpaceResourceIndex.isSecondaryForm(item), SpaceResourceIndex.getByItem(item));
            ITEM_CACHE.put(item, lines);
        }
        if (lines.isEmpty()) return;
        if (GTUtil.isCtrlDown()) {
            for (int i = 1; i < lines.size(); i++) tooltips.add(lines.get(i));
        } else {
            tooltips.add(lines.get(0));
        }
    }

    public static void append(Fluid fluid, Consumer<Component> tooltips) {
        ObjectList<Component> lines = FLUID_CACHE.get(fluid);
        if (lines == null) {
            lines = build(null, false, SpaceResourceIndex.getByFluid(fluid));
            FLUID_CACHE.put(fluid, lines);
        }
        if (lines.isEmpty()) return;
        if (GTUtil.isCtrlDown()) {
            for (int i = 1; i < lines.size(); i++) tooltips.accept(lines.get(i));
        } else {
            tooltips.accept(lines.get(0));
        }
    }

    /**
     * 同一产物可能既按材料登记（采矿）又按物品登记（浮游物质收集），两边都要算上；
     * 只有按材料查到的那批才可能是二次加工形态（矿粉），按物品查到的永远是直接产出。
     */
    private static ObjectList<Component> build(@Nullable ObjectArrayList<Entry> byMaterial, boolean secondary, @Nullable ObjectArrayList<Entry> byKey) {
        int total = size(byMaterial) + size(byKey);
        if (total == 0) return ObjectLists.emptyList();
        // 折叠提示 1 行，每种类型 1 行标题，每条配方最多 4 行
        ObjectArrayList<Component> lines = new ObjectArrayList<>(2 + (total << 2));
        lines.add(Component.empty());
        MutableComponent hint = Component.empty();
        boolean firstType = true;
        // 按类型分组，同一产物可能同时由多种配方获得（如空气既能行星抽取也能虚空集气）
        for (Type type : SpaceResourceIndex.types()) {
            boolean matched = appendType(type, byMaterial, secondary, lines);
            matched |= appendType(type, byKey, false, lines);
            if (!matched) continue;
            if (!firstType) hint.append(SEPARATOR);
            hint.append(TYPE_NAMES[type.ordinal()]);
            firstType = false;
        }
        lines.set(0, Component.translatable(HOLD_CTRL_KEY, hint).withStyle(ChatFormatting.DARK_GRAY));
        return lines;
    }

    private static int size(@Nullable ObjectArrayList<Entry> entries) {
        return entries == null ? 0 : entries.size();
    }

    private static boolean appendType(Type type, @Nullable ObjectArrayList<Entry> entries, boolean secondary, ObjectArrayList<Component> lines) {
        if (entries == null) return false;
        boolean matched = false;
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (entry.type() != type) continue;
            if (!matched) {
                String titleKey = secondary ? TITLE_SECONDARY_KEY : TITLE_KEY;
                lines.add(Component.translatable(titleKey, TYPE_NAMES[type.ordinal()]).withStyle(ChatFormatting.AQUA));
                matched = true;
            }
            appendEntry(entry, lines);
        }
        return matched;
    }

    private static void appendEntry(Entry entry, ObjectArrayList<Component> lines) {
        Component circuit = Component.literal(String.valueOf(entry.circuit())).withStyle(ChatFormatting.YELLOW);
        Item drone = entry.minDrone();
        if (drone != null) {
            Component name = drone.getDescription().copy().withStyle(ChatFormatting.GREEN);
            lines.add(Component.translatable(DRONE_KEY, name, circuit).withStyle(ChatFormatting.GRAY));
        } else if (entry.circuit() > 0) {
            lines.add(Component.translatable(CIRCUIT_KEY, circuit).withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.translatable(NO_CIRCUIT_KEY).withStyle(ChatFormatting.GRAY));
        }
        FluidStack[] fuels = entry.fuels();
        if (fuels != null) {
            lines.add(Component.translatable(FUEL_KEY, fuels(fuels)).withStyle(ChatFormatting.GRAY));
        }
        Component place = entry.place();
        if (place != null) {
            String key = entry.type().place == SpaceResourceIndex.Place.GALAXY ? GALAXY_KEY : DIMENSION_KEY;
            lines.add(Component.translatable(key, place.copy().withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
        }
        int chance = entry.chance();
        if (chance > 0 && chance < Content.MAX_CHANCE) {
            Component base = percent(chance);
            int boost = entry.chanceBoost();
            lines.add((boost > 0 ? Component.translatable(CHANCE_BOOST_KEY, base, percent(boost)) : Component.translatable(CHANCE_KEY, base))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    /** 万分数转百分比，整数时不带小数点。 */
    private static Component percent(int perTenThousand) {
        return Component.literal(FormattingUtil.formatNumbers(perTenThousand / 100.0D)).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    private static Component fuels(FluidStack[] fuels) {
        MutableComponent component = Component.empty();
        for (int i = 0; i < fuels.length; i++) {
            if (i > 0) component.append(SEPARATOR);
            component.append(fuels[i].getDisplayName().copy().withStyle(ChatFormatting.DARK_AQUA));
        }
        return component;
    }

    private static Component[] typeNames() {
        Type[] types = SpaceResourceIndex.types();
        Component[] names = new Component[types.length];
        for (Type type : types) {
            names[type.ordinal()] = Component.translatable(PREFIX + "type." + type.name().toLowerCase(Locale.ROOT));
        }
        return names;
    }
}
