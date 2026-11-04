package com.gtocore.integration.ae;

import com.gtocore.mixin.mc.ItemAccessor;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.item.IItem;
import com.gtolib.utils.register.ItemRegisterUtils;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.tooltip.TipRole;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import appeng.api.parts.IPartHost;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.Upgrades;
import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.items.materials.UpgradeCardItem;
import appeng.parts.automation.IOBusPart;
import appeng.util.InteractionUtil;

import com.gto.registrate.util.entry.ItemEntry;

import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.IntToLongFunction;

/**
 * 加速卡分级：(I) 即 AE 加速卡，(II)~(V) 每级为前一级的 8 倍；各级合计不超过机器的加速卡上限，高等级优先计入，适用 IO 总线一族与 IO 端口。
 */
@DataGeneratorScanned
public final class SpeedCards {

    public static final int TIERS = 5;
    private static final String[] ROMAN = { "I", "II", "III", "IV", "V" };
    private static final long[] MULTIPLIERS = { 1, 8, 64, 512, 4096 };
    private static final TagKey<Item>[] CIRCUITS = new TagKey[] { CustomTags.MV_CIRCUITS, CustomTags.EV_CIRCUITS, CustomTags.LuV_CIRCUITS, CustomTags.UV_CIRCUITS, CustomTags.UEV_CIRCUITS };

    @RegisterLanguage(cn = "每张效果为加速卡(I)的 %s 倍", en = "Each card is %sx as fast as an Acceleration Card (I)")
    public static final String DESC = "gtocore.speed_card.desc";
    @RegisterLanguage(cn = "与其他等级的加速卡合计不超过机器的加速卡上限，高等级优先计入", en = "Shares the machine's Acceleration Card limit with other tiers; higher tiers count first")
    public static final String LIMIT = "gtocore.speed_card.limit";
    @RegisterLanguage(cn = "仅支持搬运类AE总线与IO端口", en = "Only supports transfer-type AE buses and IO ports")
    public static final String SCOPE = "gtocore.speed_card.scope";
    @RegisterLanguage(cn = "潜行右键已达加速卡上限的机器，替换其中最低级的加速卡", en = "Sneak right-click a machine at its card limit to replace its lowest-tier Acceleration Card")
    public static final String REPLACE = "gtocore.speed_card.replace";
    @RegisterLanguage(cn = "更高等级的加速卡(II)~(V)每级效果为前一级的 8 倍，可用于搬运类AE总线与IO端口", en = "Acceleration Cards (II)-(V) are each 8x the previous tier and work in transfer-type AE buses and IO ports")
    public static final String HIGHER = "gtocore.speed_card.higher";
    @RegisterLanguage(cn = "加速卡(I)", en = "Acceleration Card (I)")
    public static final String NAME_I = "item.gtocore.speed_card_i";

    private static final ItemEntry<CardItem>[] CARDS = new ItemEntry[] { null, register(1), register(2), register(3), register(4) };

    private SpeedCards() {}

    private static ItemEntry<CardItem> register(int tier) {
        var tips = TipSpec.builder().lines(l -> {
            l.line(TipRole.FEATURE, Component.translatable(DESC, MULTIPLIERS[tier]));
            l.line(TipRole.FEATURE, Component.translatable(LIMIT));
            l.line(TipRole.FEATURE, Component.translatable(SCOPE));
            l.line(TipRole.FEATURE, Component.translatable(REPLACE));
        }).build();
        return ItemRegisterUtils.item(id(tier), "加速卡(" + ROMAN[tier] + ")", p -> new CardItem(p, tier))
                .tips(tips)
                .lang("Acceleration Card (" + ROMAN[tier] + ")")
                .register();
    }

    private static String id(int tier) {
        return "speed_card_" + ROMAN[tier].toLowerCase(Locale.ROOT);
    }

    public static ItemLike card(int tier) {
        return tier == 0 ? AEItems.SPEED_CARD : CARDS[tier].get();
    }

    public static String roman(int tier) {
        return ROMAN[tier];
    }

    public static void init() {
        ((ItemAccessor) AEItems.SPEED_CARD.asItem()).gtocore$setDescriptionId(NAME_I);
        ((IItem) AEItems.SPEED_CARD.asItem()).gtolib$setTips(TipSpec.builder().lines(l -> l.line(TipRole.FEATURE, Component.translatable(HIGHER))).build());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpeedCards::onLoadComplete);
    }

    private static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(SpeedCards::registerUpgrades);
    }

    private static void registerUpgrades() {
        var ioBuses = GuiText.IOBuses.getTranslationKey();
        register(AEParts.IMPORT_BUS, ioBuses);
        register(AEParts.EXPORT_BUS, ioBuses);
        register(EPPItemAndBlock.EX_IMPORT_BUS, "group.ex_io_bus_part");
        register(EPPItemAndBlock.EX_EXPORT_BUS, "group.ex_io_bus_part");
        register(EPPItemAndBlock.TAG_EXPORT_BUS, null);
        register(EPPItemAndBlock.MOD_EXPORT_BUS, null);
        register(EPPItemAndBlock.PRECISE_EXPORT_BUS, null);
        register(EPPItemAndBlock.THRESHOLD_EXPORT_BUS, null);
        register(AEBlocks.IO_PORT, null);
        register(EPPItemAndBlock.EX_IO_PORT, null);
    }

    private static void register(ItemLike machine, String tooltipGroup) {
        int max = Upgrades.getMaxInstallable(AEItems.SPEED_CARD, machine);
        if (max <= 0) return;
        for (int tier = 1; tier < TIERS; tier++) {
            if (tooltipGroup == null) Upgrades.add(card(tier), machine, max);
            else Upgrades.add(card(tier), machine, max, tooltipGroup);
        }
    }

    public static long scale(IntToLongFunction table, int tier, int cards) {
        return table.applyAsLong(cards) * MULTIPLIERS[tier];
    }

    private static long scale(IntToLongFunction table, IUpgradeInventory upgrades) {
        int remaining = upgrades.getMaxInstalled(AEItems.SPEED_CARD);
        int cards = 0;
        long weight = 0;
        for (int tier = TIERS - 1; tier >= 0 && remaining > 0; tier--) {
            int count = Math.min(remaining, upgrades.getInstalledUpgrades(card(tier)));
            remaining -= count;
            cards += count;
            weight += count * MULTIPLIERS[tier];
        }
        long base = table.applyAsLong(cards);
        return cards == 0 ? base : base * weight / cards;
    }

    public static int busOperations(IUpgradeInventory upgrades) {
        return (int) Math.min(Integer.MAX_VALUE, scale(IOBusPart::getOperationsForSpeedCards, upgrades));
    }

    public static long ioPortOperations(IUpgradeInventory upgrades) {
        return scale(IOPortBlockEntity::getOperationsForSpeedCards, upgrades);
    }

    public static long exIoPortOperations(IUpgradeInventory upgrades) {
        return scale(AccelerationRates::exIoPortOperations, upgrades);
    }

    public static void baseRecipe() {
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("speed_card"), new ItemStack(AEItems.SPEED_CARD.asItem(), 2),
                "ABF",
                "CDB",
                "EBF",
                'A', new MaterialEntry(TagPrefix.wireFine, GTMaterials.RedAlloy), 'B', new MaterialEntry(TagPrefix.plate, GTMaterials.StainlessSteel),
                'C', new ItemStack(AEItems.ENGINEERING_PROCESSOR.asItem()), 'D', CIRCUITS[0], 'E', new MaterialEntry(TagPrefix.wireFine, GTMaterials.Silver),
                'F', ConventionTags.ALL_FLUIX);
    }

    public static void recipes() {
        for (int tier = 1; tier < TIERS; tier++) {
            VanillaRecipeHelper.addShapedRecipe(GTOCore.id(id(tier)), new ItemStack(card(tier)),
                    " A ",
                    "ACA",
                    " A ",
                    'A', AEItems.SPEED_CARD.asItem(), 'C', CIRCUITS[tier]);
        }
    }

    private static int tierOf(Item item) {
        if (item == AEItems.SPEED_CARD.asItem()) return 0;
        return item instanceof CardItem card ? card.tier : -1;
    }

    @Nullable
    private static IUpgradeInventory upgradesAt(UseOnContext context) {
        var be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (be instanceof IPartHost host) {
            return host.selectPartWorld(context.getClickLocation()).part instanceof IUpgradeableObject part ? part.getUpgrades() : null;
        }
        return be instanceof IUpgradeableObject object ? object.getUpgrades() : null;
    }

    private static final int BELOW_LIMIT = -1;
    private static final int NOTHING_LOWER = -2;

    private static int replaceableSlot(IUpgradeInventory upgrades, int tier) {
        int installed = 0;
        boolean full = true;
        int slot = NOTHING_LOWER;
        int lowest = tier;
        for (int i = 0; i < upgrades.size(); i++) {
            var stack = upgrades.getStackInSlot(i);
            if (stack.isEmpty()) {
                full = false;
                continue;
            }
            int t = tierOf(stack.getItem());
            if (t < 0) continue;
            installed += stack.getCount();
            if (t < lowest) {
                lowest = t;
                slot = i;
            }
        }
        return full || installed >= upgrades.getMaxInstalled(AEItems.SPEED_CARD) ? slot : BELOW_LIMIT;
    }

    @Nullable
    public static InteractionResult refuseAtLimit(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || stack.getItem() != AEItems.SPEED_CARD.asItem() || !InteractionUtil.isInAlternateUseMode(player)) return null;
        var upgrades = upgradesAt(context);
        if (upgrades == null || upgrades.getMaxInstalled(AEItems.SPEED_CARD) <= 0 || replaceableSlot(upgrades, 0) != NOTHING_LOWER) return null;
        return refuse(player);
    }

    private static InteractionResult refuse(Player player) {
        player.displayClientMessage(PlayerMessages.MaxUpgradesOfTypeInstalled.text(), true);
        return InteractionResult.FAIL;
    }

    public static final class CardItem extends UpgradeCardItem {

        private final int tier;

        private CardItem(Properties properties, int tier) {
            super(properties);
            this.tier = tier;
        }

        @Override
        public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
            var player = context.getPlayer();
            if (player == null || !InteractionUtil.isInAlternateUseMode(player)) return super.onItemUseFirst(stack, context);
            var upgrades = upgradesAt(context);
            if (upgrades == null || upgrades.getMaxInstalled(this) <= 0) return super.onItemUseFirst(stack, context);
            int slot = replaceableSlot(upgrades, tier);
            if (slot == BELOW_LIMIT) return super.onItemUseFirst(stack, context);
            if (slot == NOTHING_LOWER) return refuse(player);
            var level = context.getLevel();
            if (!level.isClientSide()) {
                var old = upgrades.getStackInSlot(slot).copy();
                upgrades.setItemDirect(slot, stack.copyWithCount(1));
                if (!player.getAbilities().instabuild) stack.shrink(1);
                player.getInventory().placeItemBackInInventory(old);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    public static void remap(MissingMappingsEvent event) {
        var legacy = GTOCore.id("super_speed_card");
        for (var mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, GTOCore.MOD_ID)) {
            if (mapping.getKey().equals(legacy)) mapping.remap(CARDS[1].get());
        }
    }
}
