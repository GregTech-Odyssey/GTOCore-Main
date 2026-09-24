package com.gtocore.common.item.misc;

import com.gtocore.common.data.GTOOrganItems;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.player.OrganInventory;
import com.gtolib.api.player.OrganService;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.item.armor.ArmorTooltips;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 器官相关物品的 tooltip，格式与 GTM 电力盔甲一致（{@link ArmorTooltips}）：金色「◆ 分区」、灰色缩进信息行、白色数值；
 * 功能行「▸ 名称 状态 [操作] 消耗」，状态色绿 = 生效、黄 = 缺条件、红 = 损坏；长说明只在按住 Shift 时显示。
 * <p>
 * 状态随玩家当前身上的器官实时变化（客户端的 {@link OrganInventory} 由服务端同步）。数值常量取自 {@link OrganService}。
 */
@DataGeneratorScanned
public final class OrganTooltips {

    private OrganTooltips() {}

    // 等级名

    public static final String[] TIER_CN = { "萌芽级", "标准级 (I)", "强化级 (II)", "战术级 (III)", "原型级 (IV)" };
    public static final String[] TIER_EN = { "Sprout", "Standard (I)", "Reinforced (II)", "Tactical (III)", "Prototype (IV)" };

    @RegisterLanguage(cn = "萌芽级", en = "Sprout")
    public static final String TIER_0 = "gtocore.organ.tier.0";
    @RegisterLanguage(cn = "标准级 (I)", en = "Standard (I)")
    public static final String TIER_1 = "gtocore.organ.tier.1";
    @RegisterLanguage(cn = "强化级 (II)", en = "Reinforced (II)")
    public static final String TIER_2 = "gtocore.organ.tier.2";
    @RegisterLanguage(cn = "战术级 (III)", en = "Tactical (III)")
    public static final String TIER_3 = "gtocore.organ.tier.3";
    @RegisterLanguage(cn = "原型级 (IV)", en = "Prototype (IV)")
    public static final String TIER_4 = "gtocore.organ.tier.4";
    @RegisterLanguage(cn = "不完整", en = "Incomplete")
    public static final String TIER_NONE = "gtocore.organ.tier.none";
    private static final String[] TIER_KEYS = { TIER_0, TIER_1, TIER_2, TIER_3, TIER_4 };

    // 分区

    @RegisterLanguage(cn = "单件效果", en = "Organ Effect")
    private static final String SECTION_PIECE = "gtocore.organ.section.piece";
    @RegisterLanguage(cn = "套装效果", en = "Set Effects")
    private static final String SECTION_SET = "gtocore.organ.section.set";
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
    @RegisterLanguage(cn = "无", en = "None")
    public static final String VALUE_PLANET_NONE = "gtocore.organ.value.planet_none";
    @RegisterLanguage(cn = "%s 级及以下", en = "Tier %s and below")
    public static final String VALUE_PLANET_UP_TO = "gtocore.organ.value.planet_up_to";
    @RegisterLanguage(cn = "%s 小时 %s 分", en = "%sh %smin")
    private static final String VALUE_HOURS_MINUTES = "gtocore.organ.value.hours_minutes";

    // 功能名

    @RegisterLanguage(cn = "移动速度上限 +%s", en = "Speed cap +%s")
    private static final String FEATURE_SPEED = "gtocore.organ.feature.speed";
    @RegisterLanguage(cn = "触及距离 +%s", en = "Reach +%s")
    private static final String FEATURE_REACH = "gtocore.organ.feature.reach";
    @RegisterLanguage(cn = "免疫中毒与凋零", en = "Poison & wither immunity")
    private static final String FEATURE_CLEANSE = "gtocore.organ.feature.cleanse";
    @RegisterLanguage(cn = "水下呼吸", en = "Water breathing")
    private static final String FEATURE_BREATH = "gtocore.organ.feature.breath";
    @RegisterLanguage(cn = "解除负重上限", en = "Unlimited carry weight")
    private static final String FEATURE_LOAD = "gtocore.organ.feature.load";
    @RegisterLanguage(cn = "护甲 +%s  韧性 +%s", en = "Armor +%s  Toughness +%s")
    private static final String FEATURE_ARMOR = "gtocore.organ.feature.armor";
    @RegisterLanguage(cn = "可停留星球 %s", en = "Habitable planets %s")
    private static final String FEATURE_PLANET = "gtocore.organ.feature.planet";
    @RegisterLanguage(cn = "永不饥饿", en = "Never hungry")
    private static final String FEATURE_SATIATION = "gtocore.organ.feature.satiation";
    @RegisterLanguage(cn = "纳米遁墙", en = "Nano phasing")
    private static final String FEATURE_NANO_WALL = "gtocore.organ.feature.nano_wall";
    @RegisterLanguage(cn = "无限飞行", en = "Unlimited flight")
    private static final String FEATURE_FLIGHT = "gtocore.organ.feature.flight";
    @RegisterLanguage(cn = "免疫危害物质", en = "Hazard immunity")
    private static final String FEATURE_HAZARD = "gtocore.organ.feature.hazard";
    @RegisterLanguage(cn = "飞行", en = "Flight")
    private static final String FEATURE_WING_FLIGHT = "gtocore.organ.feature.wing_flight";
    @RegisterLanguage(cn = "修改器官", en = "Modify organs")
    private static final String FEATURE_MODIFY = "gtocore.organ.feature.modify";

    // 状态

    @RegisterLanguage(cn = "单件被动", en = "Passive")
    private static final String STATE_PIECE = "gtocore.organ.state.piece";
    @RegisterLanguage(cn = "需要 %s 级", en = "Needs tier %s")
    private static final String STATE_NEED_TIER = "gtocore.organ.state.need_tier";
    @RegisterLanguage(cn = "双腿被动", en = "Both legs")
    private static final String STATE_LEGS = "gtocore.organ.state.legs";
    @RegisterLanguage(cn = "需要双腿同级", en = "Needs both legs")
    private static final String STATE_NEED_LEGS = "gtocore.organ.state.need_legs";
    @RegisterLanguage(cn = "全套被动", en = "Set passive")
    private static final String STATE_SET = "gtocore.organ.state.set";
    @RegisterLanguage(cn = "需要整套%s", en = "Needs a full %s set")
    private static final String STATE_NEED_SET = "gtocore.organ.state.need_set";
    @RegisterLanguage(cn = "电量不足", en = "Low energy")
    private static final String STATE_NO_ENERGY = "gtocore.organ.state.no_energy";
    @RegisterLanguage(cn = "已损坏", en = "Broken")
    private static final String STATE_BROKEN = "gtocore.organ.state.broken";

    // 消耗与操作

    @RegisterLanguage(cn = "每秒 1 点耐久", en = "1 durability/s")
    private static final String COST_DURABILITY = "gtocore.organ.cost.durability";
    @RegisterLanguage(cn = "[右键]", en = "[Right Click]")
    private static final String HINT_RIGHT_CLICK = "gtocore.organ.hint.right_click";

    // Shift 说明

    @RegisterLanguage(cn = "左右腿都达到该等级才生效，逐级累加；实际速度在「玩家实时属性」中调节", en = "Both legs must reach this tier; stacks per tier. Set the actual speed in Player Realtime Attributes")
    private static final String DETAIL_SPEED = "gtocore.organ.detail.speed";
    @RegisterLanguage(cn = "未解除时，背包中高质量材料总质量超过 %s 会被减速", en = "Otherwise, carrying more than %s mass of heavy materials slows you down")
    private static final String DETAIL_LOAD = "gtocore.organ.detail.load";
    @RegisterLanguage(cn = "整套指九种身体器官都已装上，且都不低于该等级", en = "A full set means all nine body organs are installed at this tier or higher")
    private static final String DETAIL_SET = "gtocore.organ.detail.set";
    @RegisterLanguage(cn = "在等级不足的星球上，伤害逐秒递增，%s 秒后死亡", en = "On planets above your tier, damage grows every second and kills you after %s seconds")
    private static final String DETAIL_PLANET = "gtocore.organ.detail.planet";
    @RegisterLanguage(cn = "飞行时可穿过方块，在「玩家实时属性」中开关", en = "Fly through blocks; toggle it in Player Realtime Attributes")
    private static final String DETAIL_NANO_WALL = "gtocore.organ.detail.nano_wall";
    @RegisterLanguage(cn = "不再受 GT 危害物质影响", en = "GT hazardous materials no longer affect you")
    private static final String DETAIL_HAZARD = "gtocore.organ.detail.hazard";
    @RegisterLanguage(cn = "装进修改器的翅膀槽即可飞行，只在悬空飞行时消耗；多个翅膀按槽位顺序使用", en = "Fly while it sits in a wing slot of the Organ Modifier; only consumed while airborne. Multiple wings are used in slot order")
    private static final String DETAIL_WING = "gtocore.organ.detail.wing";
    @RegisterLanguage(cn = "可由无线电网自动充电", en = "Charged automatically by the wireless energy network")
    private static final String DETAIL_WING_CHARGE = "gtocore.organ.detail.wing_charge";
    @RegisterLanguage(cn = "器官要装进修改器的槽位才生效；每缺一件身体器官，生命上限 %s", en = "Organs only work in the Organ Modifier slots; each missing body organ costs %s max health")
    private static final String DETAIL_MODIFY = "gtocore.organ.detail.modify";
    @RegisterLanguage(cn = "死亡重生时，缺少的身体器官会补上萌芽级", en = "Missing body organs are refilled with Sprout organs on respawn")
    private static final String DETAIL_RESPAWN = "gtocore.organ.detail.respawn";

    /// tooltip 与状态面板里用到的百分比文字，只取决于常量，预先算好（避免每帧 / 每 tick 拼接字符串）
    private static final String MISSING_ONE_TEXT = "-" + percent(OrganService.HEALTH_LOSS_PER_MISSING);
    private static final String[] MISSING_HEALTH_TEXT = new String[OrganType.BODY.length + 1];
    private static final String[] LEG_SPEED_TEXT = new String[TierOrganItem.MAX_TIER + 1];

    static {
        for (int missing = 0; missing < MISSING_HEALTH_TEXT.length; missing++) {
            MISSING_HEALTH_TEXT[missing] = "-" + percent(OrganService.HEALTH_LOSS_PER_MISSING * missing);
        }
        for (int tier = 0; tier < LEG_SPEED_TEXT.length; tier++) {
            // 原版基础移速 0.1
            LEG_SPEED_TEXT[tier] = percent(OrganService.legSpeedBonus(tier) / 0.1F);
        }
    }

    /** 等级名的翻译键；-1（不完整）也有对应的键。 */
    public static String tierName(int tier) {
        return tier < 0 ? TIER_NONE : TIER_KEYS[Math.min(tier, TIER_KEYS.length - 1)];
    }

    /** 「标准级 (I)」；不完整时为「不完整」。 */
    public static MutableComponent tierValue(int tier) {
        return Component.translatable(tierName(tier));
    }

    /** 百分比，最多两位小数，不用 String.format。 */
    public static String percent(double fraction) {
        return ArmorTooltips.amps(fraction * 100) + "%";
    }

    /** 可停留星球的数值文字。 */
    public static MutableComponent planetValue(int setTier) {
        int max = OrganService.maxPlanetTier(setTier);
        return max <= 0 ? Component.translatable(VALUE_PLANET_NONE) : Component.translatable(VALUE_PLANET_UP_TO, max);
    }

    /** 生命上限的数值文字：完整，或「−20% · 缺少 2 件」。 */
    public static MutableComponent healthValue(int missing) {
        if (missing <= 0) return Component.translatable(VALUE_HEALTH_FULL);
        return Component.translatable(VALUE_HEALTH_MISSING, MISSING_HEALTH_TEXT[Math.min(missing, OrganType.BODY.length)], missing);
    }

    public static ChatFormatting healthColor(int missing) {
        return missing <= 0 ? ChatFormatting.GREEN : missing < 3 ? ChatFormatting.YELLOW : ChatFormatting.RED;
    }

    public static ChatFormatting setTierColor(int setTier) {
        return setTier >= 1 ? ChatFormatting.GREEN : setTier == 0 ? ChatFormatting.YELLOW : ChatFormatting.RED;
    }

    // 分级身体器官

    public static void addTierOrganTooltip(ItemStack stack, TierOrganItem item, List<Component> lines) {
        var type = item.getOrganType();
        int tier = item.getTier();
        @Nullable
        OrganInventory organs = localOrgans();
        boolean installed = organs != null && isInstalled(organs, stack);

        lines.add(ArmorTooltips.info(INFO_PART, white(type.translationKey), white(tierName(tier))));
        lines.add(ArmorTooltips.info(INFO_MISSING, ArmorTooltips.value(MISSING_ONE_TEXT, ChatFormatting.RED)));
        // 萌芽级：出生自带、死亡重生补发
        if (tier == 0) lines.add(Component.literal(" ").append(Component.translatable(INFO_SPROUT)).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        List<Component> piece = new ArrayList<>(3);
        switch (type) {
            case LEFT_LEG, RIGHT_LEG -> {
                if (tier >= 1) {
                    boolean active = installed && Math.min(organs.getTier(OrganType.LEFT_LEG), organs.getTier(OrganType.RIGHT_LEG)) >= tier;
                    feature(piece, Component.translatable(FEATURE_SPEED, LEG_SPEED_TEXT[tier]),
                            active ? state(STATE_LEGS, ChatFormatting.GREEN) : state(STATE_NEED_LEGS, ChatFormatting.YELLOW), null);
                    ArmorTooltips.addDetail(piece, DETAIL_SPEED);
                }
            }
            case RIGHT_ARM -> pieceFeature(piece, Component.translatable(FEATURE_REACH, ArmorTooltips.amps(OrganService.REACH_BONUS)), tier, OrganService.RIGHT_ARM_REACH_TIER);
            case LIVER -> pieceFeature(piece, Component.translatable(FEATURE_CLEANSE), tier, OrganService.LIVER_CLEANSE_TIER);
            case LUNG -> pieceFeature(piece, Component.translatable(FEATURE_BREATH), tier, OrganService.LUNG_BREATH_TIER);
            case SPINE -> {
                pieceFeature(piece, Component.translatable(FEATURE_LOAD), tier, OrganService.SPINE_UNLIMITED_LOAD_TIER);
                ArmorTooltips.addDetail(piece, DETAIL_LOAD, OrganService.LIMITED_MASS_CAP);
            }
            default -> {}
        }
        if (!piece.isEmpty()) {
            lines.add(ArmorTooltips.section(SECTION_PIECE));
            lines.addAll(piece);
        }

        // 套装效果：本件装在身上且整套不低于本件等级时为绿
        boolean setActive = installed && organs.getSetTier() >= tier;
        Component setState = setActive ? state(STATE_SET, ChatFormatting.GREEN) :
                Component.translatable(STATE_NEED_SET, Component.translatable(tierName(tier))).withStyle(ChatFormatting.YELLOW);
        lines.add(ArmorTooltips.section(SECTION_SET));
        feature(lines, Component.translatable(FEATURE_PLANET, planetValue(tier)), setState, null);
        ArmorTooltips.addDetail(lines, DETAIL_PLANET, OrganService.PLANET_KILL_SECONDS);
        if (tier >= 1) {
            String armor = String.valueOf(OrganService.ARMOR_PER_SET_TIER * tier);
            feature(lines, Component.translatable(FEATURE_ARMOR, armor, armor), setState, null);
        }
        if (tier >= OrganService.SET_SATIATION_TIER) feature(lines, Component.translatable(FEATURE_SATIATION), setState, null);
        if (tier >= OrganService.SET_NANO_WALL_TIER) {
            feature(lines, Component.translatable(FEATURE_NANO_WALL), setState, null);
            ArmorTooltips.addDetail(lines, DETAIL_NANO_WALL);
        }
        if (tier >= OrganService.SET_FLIGHT_TIER) feature(lines, Component.translatable(FEATURE_FLIGHT), setState, null);
        if (tier >= OrganService.SET_HAZARD_IMMUNE_TIER) {
            feature(lines, Component.translatable(FEATURE_HAZARD), setState, null);
            ArmorTooltips.addDetail(lines, DETAIL_HAZARD);
        }
        ArmorTooltips.addDetail(lines, DETAIL_SET);
        if (!ArmorTooltips.showDetails()) lines.add(ArmorTooltips.SHIFT_HINT);
    }

    /** 本件达到等级即生效的单件效果：达到为绿「单件被动」，未达到为黄「需要 n 级」。 */
    private static void pieceFeature(List<Component> lines, Component name, int tier, int requiredTier) {
        feature(lines, name, tier >= requiredTier ? state(STATE_PIECE, ChatFormatting.GREEN) :
                Component.translatable(STATE_NEED_TIER, requiredTier).withStyle(ChatFormatting.YELLOW), null);
    }

    // 翅膀

    public static void addWingTooltip(ItemStack stack, WingOrganItem item, List<Component> lines) {
        lines.add(ArmorTooltips.info(INFO_WING, white(OrganType.WING.translationKey), ArmorTooltips.value(ArmorTooltips.amps(item.getMaxFlySpeed()))));
        Component state;
        Component cost;
        if (item.isElectric()) {
            var electric = GTCapabilityHelper.getElectricItem(stack);
            boolean hasEnergy = electric != null && electric.getCharge() > 0;
            lines.add(ArmorTooltips.info(INFO_FLIGHT_CAPACITY, ArmorTooltips.value(String.valueOf(GTOOrganItems.MECHANICAL_WING_FLIGHT_HOURS)),
                    GTValues.VNF[GTValues.EV]));
            state = hasEnergy ? state(STATE_PIECE, ChatFormatting.GREEN) : state(STATE_NO_ENERGY, ChatFormatting.YELLOW);
            // 每秒耗 V[EV] EU，即 1/20 A
            cost = ArmorTooltips.ampsPerSecond(1.0 / 20);
        } else {
            int seconds = stack.getMaxDamage() - stack.getDamageValue();
            ChatFormatting color = seconds <= 0 ? ChatFormatting.RED : seconds * 10 < stack.getMaxDamage() ? ChatFormatting.YELLOW : ChatFormatting.WHITE;
            lines.add(ArmorTooltips.info(INFO_FLIGHT_TIME, Component.translatable(VALUE_HOURS_MINUTES, seconds / 3600, seconds % 3600 / 60).withStyle(color)));
            state = seconds > 0 ? state(STATE_PIECE, ChatFormatting.GREEN) : state(STATE_BROKEN, ChatFormatting.RED);
            cost = ArmorTooltips.cost(COST_DURABILITY);
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
            int setTier = organs.getSetTier();
            int missing = organs.getMissingBodyCount();
            lines.add(ArmorTooltips.section(SECTION_BODY));
            lines.add(ArmorTooltips.info(INFO_SET_TIER, tierValue(setTier).withStyle(setTierColor(setTier))));
            lines.add(ArmorTooltips.info(INFO_HEALTH, healthValue(missing).withStyle(healthColor(missing))));
            lines.add(ArmorTooltips.info(INFO_PLANET, planetValue(setTier).withStyle(ChatFormatting.WHITE)));
            lines.add(ArmorTooltips.info(INFO_WINGS, ArmorTooltips.value(String.valueOf(wingCount(organs))), ArmorTooltips.value(String.valueOf(OrganType.WING.slotCount))));
        }
        lines.add(ArmorTooltips.section(SECTION_FEATURES));
        feature(lines, Component.translatable(FEATURE_MODIFY), Component.translatable(HINT_RIGHT_CLICK).withStyle(ChatFormatting.DARK_GRAY), null);
        ArmorTooltips.addDetail(lines, DETAIL_MODIFY, MISSING_ONE_TEXT);
        ArmorTooltips.addDetail(lines, DETAIL_PLANET, OrganService.PLANET_KILL_SECONDS);
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
