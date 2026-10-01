package com.gtocore.common.item.misc;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.player.OrganEffect;
import com.gtolib.api.player.OrganEffects;
import com.gtolib.api.player.OrganInventory;
import com.gtolib.api.player.OrganScope;
import com.gtolib.api.player.OrganService;
import com.gtolib.api.player.OrganTier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.item.armor.ArmorTooltips;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 器官相关物品的 tooltip，格式与 GTM 电力盔甲一致（{@link ArmorTooltips}）：金色「◆ 分区」、灰色缩进信息行、白色数值；
 * 功能行「▸ 名称 状态 [操作] 消耗」，状态色绿 = 生效、黄 = 缺条件、红 = 损坏；长说明只在按住 Shift 时显示。
 * <p>
 * 状态随玩家当前身上的器官实时变化（客户端的 {@link OrganInventory} 由服务端同步）。
 */
@DataGeneratorScanned
public final class OrganTooltips {

    private OrganTooltips() {}

    // 等级名

    @RegisterLanguage(cn = "不完整", en = "Incomplete")
    public static final String TIER_NONE = "gtocore.organ.tier.none";

    // 分区

    @RegisterLanguage(cn = "功能", en = "Features")
    private static final String SECTION_FEATURES = "gtocore.organ.section.features";
    @RegisterLanguage(cn = "当前身体", en = "Current Body")
    private static final String SECTION_BODY = "gtocore.organ.section.body";

    // 信息行

    @RegisterLanguage(cn = "部位 %s · 等级 %s", en = "Part %s · Tier %s")
    private static final String INFO_PART = "gtocore.organ.info.part";
    @RegisterLanguage(cn = "缺少此部位时，生命上限 %s", en = "Max health %s while this part is missing")
    private static final String INFO_MISSING = "gtocore.organ.info.missing";
    @RegisterLanguage(cn = "下辈子或许可以再领一套...", en = "Maybe you can get another set in your next life...")
    private static final String INFO_SPROUT = "gtocore.organ.info.sprout";
    @RegisterLanguage(cn = "部位 %s · 最高飞行速度 %s", en = "Part %s · Max flight speed %s")
    private static final String INFO_WING = "gtocore.organ.info.wing";
    @RegisterLanguage(cn = "剩余飞行时间 %s", en = "Flight time left %s")
    private static final String INFO_FLIGHT_TIME = "gtocore.organ.info.flight_time";
    @RegisterLanguage(cn = "满电可飞行 %s 小时（%s）", en = "%s hours of flight when full (%s)")
    private static final String INFO_FLIGHT_CAPACITY = "gtocore.organ.info.flight_capacity";
    @RegisterLanguage(cn = "套装等级 %s", en = "Set tier %s")
    private static final String INFO_SET_TIER = "gtocore.organ.info.set_tier";
    @RegisterLanguage(cn = "生命上限 %s", en = "Max health %s")
    private static final String INFO_HEALTH = "gtocore.organ.info.health";
    @RegisterLanguage(cn = "可停留星球 %s", en = "Habitable planets %s")
    private static final String INFO_PLANET = "gtocore.organ.info.planet";
    @RegisterLanguage(cn = "翅膀 %s / %s", en = "Wings %s / %s")
    private static final String INFO_WINGS = "gtocore.organ.info.wings";

    // 数值

    @RegisterLanguage(cn = "完整", en = "Full")
    public static final String VALUE_HEALTH_FULL = "gtocore.organ.value.health_full";
    @RegisterLanguage(cn = "%s · 缺少 %s 件", en = "%s · %s missing")
    public static final String VALUE_HEALTH_MISSING = "gtocore.organ.value.health_missing";
    @RegisterLanguage(cn = "%s 小时 %s 分", en = "%sh %smin")
    private static final String VALUE_HOURS_MINUTES = "gtocore.organ.value.hours_minutes";

    // 功能名

    @RegisterLanguage(cn = "飞行", en = "Flight")
    private static final String FEATURE_WING_FLIGHT = "gtocore.organ.feature.wing_flight";
    @RegisterLanguage(cn = "修改器官", en = "Modify organs")
    private static final String FEATURE_MODIFY = "gtocore.organ.feature.modify";

    // 状态

    @RegisterLanguage(cn = "电量不足", en = "Low energy")
    private static final String STATE_NO_ENERGY = "gtocore.organ.state.no_energy";
    @RegisterLanguage(cn = "已损坏", en = "Broken")
    private static final String STATE_BROKEN = "gtocore.organ.state.broken";

    // 消耗与操作

    @RegisterLanguage(cn = "每秒 %s 点耐久", en = "%s durability/s")
    private static final String COST_DURABILITY = "gtocore.organ.cost.durability";
    @RegisterLanguage(cn = "[右键]", en = "[Right Click]")
    private static final String HINT_RIGHT_CLICK = "gtocore.organ.hint.right_click";

    // Shift 说明

    @RegisterLanguage(cn = "装进修改器的翅膀槽即可飞行，只在悬空飞行时消耗；多个翅膀按槽位顺序使用", en = "Fly while it sits in a wing slot of the Organ Modifier; only consumed while airborne. Multiple wings are used in slot order")
    private static final String DETAIL_WING = "gtocore.organ.detail.wing";
    @RegisterLanguage(cn = "可由无线电网自动充电", en = "Charged automatically by the wireless energy network")
    private static final String DETAIL_WING_CHARGE = "gtocore.organ.detail.wing_charge";
    @RegisterLanguage(cn = "器官要装进修改器的槽位才生效；每缺一件身体器官，生命上限 %s", en = "Organs only work in the Organ Modifier slots; each missing body organ costs %s max health")
    private static final String DETAIL_MODIFY = "gtocore.organ.detail.modify";
    @RegisterLanguage(cn = "死亡重生时，缺少的身体器官会补上萌芽级", en = "Missing body organs are refilled with Sprout organs on respawn")
    private static final String DETAIL_RESPAWN = "gtocore.organ.detail.respawn";

    /// tooltip 与状态面板里用到的百分比文字，只取决于常量，预先算好（避免每帧 / 每 tick 拼接字符串）
    private static final String MISSING_ONE_TEXT = "-" + OrganEffects.percent(OrganService.HEALTH_LOSS_PER_MISSING);
    private static final String[] MISSING_HEALTH_TEXT = new String[OrganType.BODY.length + 1];

    static {
        for (int missing = 0; missing < MISSING_HEALTH_TEXT.length; missing++) {
            MISSING_HEALTH_TEXT[missing] = "-" + OrganEffects.percent(OrganService.HEALTH_LOSS_PER_MISSING * missing);
        }
    }

    public static String tierName(@Nullable OrganTier tier) {
        return tier == null ? TIER_NONE : tier.getTranslationKey();
    }

    /** 「标准级 (I)」；不完整时为「不完整」。 */
    public static MutableComponent tierValue(@Nullable OrganTier tier) {
        return Component.translatable(tierName(tier));
    }

    /** 生命上限的数值文字：完整，或「−20% · 缺少 2 件」。 */
    public static MutableComponent healthValue(int missing) {
        if (missing <= 0) return Component.translatable(VALUE_HEALTH_FULL);
        return Component.translatable(VALUE_HEALTH_MISSING, MISSING_HEALTH_TEXT[Math.min(missing, OrganType.BODY.length)], missing);
    }

    public static ChatFormatting healthColor(int missing) {
        return missing <= 0 ? ChatFormatting.GREEN : missing < 3 ? ChatFormatting.YELLOW : ChatFormatting.RED;
    }

    public static ChatFormatting setTierColor(@Nullable OrganTier setTier) {
        return setTier == null ? ChatFormatting.RED : setTier.atLeast(OrganTier.STANDARD) ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
    }

    // 分级身体器官

    public static void addTierOrganTooltip(ItemStack stack, TierOrganItem item, List<Component> lines) {
        var type = item.getOrganType();
        OrganTier tier = item.getTier();
        @Nullable
        OrganInventory organs = localOrgans();
        @Nullable
        OrganInventory installedIn = organs != null && isInstalled(organs, stack) ? organs : null;

        lines.add(ArmorTooltips.info(INFO_PART, white(type.translationKey), white(tier.getTranslationKey())));
        lines.add(ArmorTooltips.info(INFO_MISSING, ArmorTooltips.value(MISSING_ONE_TEXT, ChatFormatting.RED)));
        // 萌芽级：出生自带、死亡重生补发
        if (tier == OrganTier.SPROUT) lines.add(Component.literal(" ").append(Component.translatable(INFO_SPROUT)).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        @Nullable
        OrganScope section = null;
        var effects = OrganEffect.all();
        for (int i = 0, size = effects.size(); i < size; i++) {
            var effect = effects.get(i);
            var scope = effect.getScope();
            if (effect.getFeature() == null || !scope.covers(type) || !scope.isListedFor(tier, effect.getTier())) continue;
            if (section == null || !section.getSection().equals(scope.getSection())) {
                closeSection(lines, section);
                lines.add(ArmorTooltips.section(scope.getSection()));
            }
            section = scope;
            feature(lines, effect.featureName(tier), scope.state(tier, effect.getTier(), installedIn), null);
            if (effect.getDetail() != null) ArmorTooltips.addDetail(lines, effect.getDetail(), effect.getDetailArgs());
        }
        closeSection(lines, section);
        if (!ArmorTooltips.showDetails()) lines.add(ArmorTooltips.SHIFT_HINT);
    }

    private static void closeSection(List<Component> lines, @Nullable OrganScope scope) {
        if (scope != null && scope.getFooter() != null) ArmorTooltips.addDetail(lines, scope.getFooter());
    }

    // 翅膀

    public static void addWingTooltip(ItemStack stack, WingOrganItem item, List<Component> lines) {
        lines.add(ArmorTooltips.info(INFO_WING, white(OrganType.WING.translationKey), ArmorTooltips.value(ArmorTooltips.amps(item.getMaxFlySpeed()))));
        Component state;
        Component cost;
        if (item.isElectric()) {
            var electric = GTCapabilityHelper.getElectricItem(stack);
            boolean hasEnergy = electric != null && electric.getCharge() > 0;
            lines.add(ArmorTooltips.info(INFO_FLIGHT_CAPACITY, ArmorTooltips.value(String.valueOf(item.getFlightHours())),
                    GTValues.VNF[item.getVoltage()]));
            state = hasEnergy ? state(OrganScope.STATE_PIECE, ChatFormatting.GREEN) : state(STATE_NO_ENERGY, ChatFormatting.YELLOW);
            // 每秒耗 V[EV] EU，即 1/20 A
            cost = ArmorTooltips.ampsPerSecond(1.0 / 20);
        } else {
            int seconds = stack.getMaxDamage() - stack.getDamageValue();
            ChatFormatting color = seconds <= 0 ? ChatFormatting.RED : seconds * 10 < stack.getMaxDamage() ? ChatFormatting.YELLOW : ChatFormatting.WHITE;
            lines.add(ArmorTooltips.info(INFO_FLIGHT_TIME, Component.translatable(VALUE_HOURS_MINUTES, seconds / 3600, seconds % 3600 / 60).withStyle(color)));
            state = seconds > 0 ? state(OrganScope.STATE_PIECE, ChatFormatting.GREEN) : state(STATE_BROKEN, ChatFormatting.RED);
            cost = Component.translatable(COST_DURABILITY, WingOrganItem.DURABILITY_PER_SECOND).withStyle(ChatFormatting.GRAY);
        }
        lines.add(ArmorTooltips.section(SECTION_FEATURES));
        feature(lines, Component.translatable(FEATURE_WING_FLIGHT), state, cost);
        ArmorTooltips.addDetail(lines, DETAIL_WING);
        if (item.isElectric()) ArmorTooltips.addDetail(lines, DETAIL_WING_CHARGE);
        if (!ArmorTooltips.showDetails()) lines.add(ArmorTooltips.SHIFT_HINT);
    }

    // 器官修改器：显示玩家当前身体状态

    public static void addModifierTooltip(List<Component> lines) {
        @Nullable
        OrganInventory organs = localOrgans();
        if (organs != null) {
            OrganTier setTier = organs.getSetTier();
            int missing = organs.getMissingBodyCount();
            lines.add(ArmorTooltips.section(SECTION_BODY));
            lines.add(ArmorTooltips.info(INFO_SET_TIER, tierValue(setTier).withStyle(setTierColor(setTier))));
            lines.add(ArmorTooltips.info(INFO_HEALTH, healthValue(missing).withStyle(healthColor(missing))));
            lines.add(ArmorTooltips.info(INFO_PLANET, OrganEffects.planetValue(setTier).withStyle(ChatFormatting.WHITE)));
            lines.add(ArmorTooltips.info(INFO_WINGS, ArmorTooltips.value(String.valueOf(wingCount(organs))), ArmorTooltips.value(String.valueOf(OrganType.WING.slotCount))));
        }
        lines.add(ArmorTooltips.section(SECTION_FEATURES));
        feature(lines, Component.translatable(FEATURE_MODIFY), Component.translatable(HINT_RIGHT_CLICK).withStyle(ChatFormatting.DARK_GRAY), null);
        ArmorTooltips.addDetail(lines, DETAIL_MODIFY, MISSING_ONE_TEXT);
        ArmorTooltips.addDetail(lines, OrganEffects.DETAIL_PLANET, OrganEffects.PLANET_KILL_SECONDS);
        ArmorTooltips.addDetail(lines, DETAIL_RESPAWN);
        if (!ArmorTooltips.showDetails()) lines.add(ArmorTooltips.SHIFT_HINT);
    }

    public static int wingCount(OrganInventory organs) {
        int count = 0;
        for (int i = 0; i < OrganType.WING.slotCount; i++) {
            if (!organs.getStack(OrganType.WING, i).isEmpty()) count++;
        }
        return count;
    }

    // 通用

    /** 功能行「 ▸ 名称 状态 消耗」，名称可带参数。 */
    private static void feature(List<Component> lines, Component name, Component state, @Nullable Component cost) {
        MutableComponent line = Component.literal(" ▸ ").withStyle(ChatFormatting.DARK_GRAY)
                .append(name.copy().withStyle(ChatFormatting.WHITE))
                .append("  ").append(state);
        if (cost != null) line.append("  ").append(cost);
        lines.add(line);
    }

    private static Component white(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.WHITE);
    }

    private static Component state(String key, ChatFormatting color) {
        return ArmorTooltips.state(key, color);
    }

    @Nullable
    private static OrganInventory localOrgans() {
        return GTCEu.isClientSide() ? OrganTooltipsClient.localOrgans() : null;
    }

    /** 这件物品是否就是身上装着的那件（修改器界面里的槽直接引用玩家器官库存的物品）。 */
    private static boolean isInstalled(OrganInventory organs, ItemStack stack) {
        for (int slot = 0; slot < organs.getSlots(); slot++) {
            if (organs.getStackInSlot(slot) == stack) return true;
        }
        return false;
    }
}
