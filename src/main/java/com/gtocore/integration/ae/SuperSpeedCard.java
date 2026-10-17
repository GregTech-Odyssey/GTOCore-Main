package com.gtocore.integration.ae;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.utils.register.ItemRegisterUtils;

import com.gregtechceu.gtceu.api.tooltip.TipRole;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.Upgrades;
import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.localization.GuiText;
import appeng.items.materials.UpgradeCardItem;
import appeng.parts.automation.IOBusPart;

import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import com.gto.registrate.util.entry.ItemEntry;

import java.util.function.IntToLongFunction;

/**
 * 超级加速卡：单张等效加速卡的 8 倍，与加速卡合计不超过该机器的加速卡上限、超级卡优先计入；适用 IO 总线一族与 IO 端口。
 */
@DataGeneratorScanned
public final class SuperSpeedCard {

    private static final int MULTIPLIER = 8;

    @RegisterLanguage(cn = "每张效果为加速卡的 %s 倍", en = "Each card is %sx as fast as an Acceleration Card")
    public static final String DESC = "gtocore.super_speed_card.desc";
    @RegisterLanguage(cn = "与加速卡合计不超过机器的加速卡上限，超级加速卡优先计入", en = "Counts toward the machine's Acceleration Card limit; Super cards count first")
    public static final String LIMIT = "gtocore.super_speed_card.limit";
    @RegisterLanguage(cn = "仅支持搬运类AE总线", en = "Only supports transfer-type AE buses")
    public static final String SCOPE = "gtocore.super_speed_card.scope";

    private static final TipSpec TIPS = TipSpec.builder().lines(l -> {
        l.line(TipRole.FEATURE, Component.translatable(DESC, MULTIPLIER));
        l.line(TipRole.FEATURE, Component.translatable(LIMIT));
        l.line(TipRole.FEATURE, Component.translatable(SCOPE));
    }).build();

    public static final ItemEntry<UpgradeCardItem> ITEM = ItemRegisterUtils.item("super_speed_card", "超级加速卡", UpgradeCardItem::new)
            .tips(TIPS)
            .lang("Super Acceleration Card")
            .register();

    private SuperSpeedCard() {}

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SuperSpeedCard::onLoadComplete);
    }

    private static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(SuperSpeedCard::registerUpgrades);
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
        if (tooltipGroup == null) Upgrades.add(ITEM.get(), machine, max);
        else Upgrades.add(ITEM.get(), machine, max, tooltipGroup);
    }

    public static long scale(IntToLongFunction table, int maxCards, int superCards, int speedCards) {
        superCards = Math.min(maxCards, superCards);
        speedCards = Math.min(maxCards - superCards, speedCards);
        int cards = superCards + speedCards;
        long base = table.applyAsLong(cards);
        if (cards == 0) return base;
        return base * (speedCards + (long) MULTIPLIER * superCards) / cards;
    }

    private static long scale(IntToLongFunction table, IUpgradeInventory upgrades) {
        return scale(table, upgrades.getMaxInstalled(AEItems.SPEED_CARD), upgrades.getInstalledUpgrades(ITEM.get()), upgrades.getInstalledUpgrades(AEItems.SPEED_CARD));
    }

    public static int busOperations(IUpgradeInventory upgrades) {
        return (int) scale(IOBusPart::getOperationsForSpeedCards, upgrades);
    }

    public static long ioPortOperations(IUpgradeInventory upgrades) {
        return scale(IOPortBlockEntity::getOperationsForSpeedCards, upgrades);
    }

    public static long exIoPortOperations(IUpgradeInventory upgrades) {
        return scale(AccelerationRates::exIoPortOperations, upgrades);
    }

    public static void recipes() {
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("super_speed_card"), new ItemStack(ITEM.get()),
                " A ",
                "ACA",
                " A ",
                'A', AEItems.SPEED_CARD.asItem(), 'C', CustomTags.EV_CIRCUITS);
    }
}
