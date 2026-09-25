package com.gtocore.data.transaction.data.trade;

import com.gtocore.api.gui.StackTexture;
import com.gtocore.common.data.GTOItems;
import com.gtocore.data.transaction.manager.TradeEntry;
import com.gtocore.data.transaction.manager.TradingManager;
import com.gtocore.integration.Mods;

import com.gtolib.GTOCore;
import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import appeng.core.definitions.AEItems;

import vazkii.botania.common.item.BotaniaItems;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.block;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.ingot;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.api.data.tag.GTOTagPrefix.COIN;
import static com.gtocore.data.transaction.data.GTOTrade.*;
import static com.gtocore.data.transaction.data.TradeLang.TECH_OPERATOR_COIN;
import static com.gtocore.data.transaction.data.TradeLang.addTradeLang;
import static com.gtocore.data.transaction.data.trade.UnlockTrade.UNLOCK_BASE;
import static com.gtocore.utils.PlayerHeadUtils.itemStackAddNbtString;

public final class WelfareGroup {

    private static final long LV_ASSEMBLER_QUEST = 0x33B01E889EE7E990L;
    private static final long MOON_QUEST = 0x310210D2AD9515E3L;
    private static final long LV_CIRCUIT_QUEST = 0x0FBE7318DFBA700EL;

    /**
     * 员工福利兑换中心
     * <p>
     * - 福利兑换 壹
     * - 福利兑换 贰
     */
    public static void init() {
        int GroupIndex = TradingManager.INSTANCE.addShopGroup(
                addTradeLang("员工福利兑换中心", "Employee Benefits Redemption Center"),
                GuiTextures.GREGTECH_LOGO,
                GuiTextures.GREGTECH_LOGO);

        int ShopIndex1 = TradingManager.INSTANCE.addShopByGroupIndex(
                GroupIndex,
                addTradeLang("格雷科技™员工会员店", "Gray Tech™ Employee Membership Store"),
                UNLOCK_BASE,
                Set.of(TECH_OPERATOR_COIN),
                GuiTextures.GREGTECH_LOGO);

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                new TradeEntry.Builder()
                        .texture(new StackTexture(BotaniaItems.manaCookie))
                        .addDescription(Component.translatable(addTradeLang(
                                "欢迎来到格雷科技™员工会员专卖店",
                                "Welcome to the Grey Tech™ Employee Membership Store")))
                        .addDescription(Component.translatable(addTradeLang("希望你喜欢", "Hope you like it")))
                        .outputItem(new ItemStack(BotaniaItems.manaCookie))
                        .outputItem(ChemicalHelper.get(ingot, Bronze, 64))
                        .preCheck((a, b) -> checkTag(a, b, "Welcome to the Grey Tech™ Employee Membership Store"))
                        .onExecute((a, b, c) -> performTag(a, b, c, "Welcome to the Grey Tech™ Employee Membership Store"))
                        .build());

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                simpleSingleTimesItemTrading(true, UNLOCK_BASE, new ItemStack(BotaniaItems.manaCookie, 64),
                        TECH_OPERATOR_COIN, 16, "WelfareGroup.ManaCookiePurchase"));

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                SimpleLotteryTrading(UNLOCK_BASE, TECH_OPERATOR_COIN, 32,
                        List.of(Component.translatable(addTradeLang("切勿沉迷", "Avoid excessive indulgence"))),
                        List.of(lotteryItem(1, ChemicalHelper.get(COIN, Copper, 6400)),
                                lotteryItem(10, ChemicalHelper.get(COIN, Copper, 640)),
                                lotteryItem(5000, ChemicalHelper.get(COIN, Copper, 64)),
                                lotteryItem(34459, ChemicalHelper.get(COIN, Copper, 32)),
                                lotteryItem(60530, ChemicalHelper.get(COIN, Copper, 16)))));

        {
            ItemStack stack = itemStackAddNbtString(GTOItems.PROSPECTOR_MANA_ULV.asStack(), "{mana:500000}");
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    simpleItemTrading(true, UNLOCK_BASE, stack, TECH_OPERATOR_COIN, 16));

            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    new TradeEntry.Builder()
                            .texture(new StackTexture(stack))
                            .addDescription(Component.translatable(addTradeLang(
                                    "建议完全用完了魔力再来这里兑换，当然如果你有自己的植物魔法产线就最好了",
                                    "It is recommended to redeem rewards here only after using up all your mana. Of course, it would be best if you have your own plant magic production line")))
                            .inputItem(GTOItems.PROSPECTOR_MANA_ULV.asStack())
                            .inputItem(ChemicalHelper.get(ingot, Bronze, 32))
                            .outputItem(stack)
                            .build());
        }

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                simpleItemTrading(true, UNLOCK_BASE, ChemicalHelper.get(block, Bronze, 8), TECH_OPERATOR_COIN, 10));

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                simpleItemTrading(true, UNLOCK_BASE, GTOItems.PRECISION_STEAM_MECHANISM.asStack(), TECH_OPERATOR_COIN, 128));

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                simpleItemTrading(true, UNLOCK_BASE, GTItems.STICKY_RESIN.asStack(128), TECH_OPERATOR_COIN, 16));

        {
            ItemStack stack = itemStackAddNbtString(AEItems.PORTABLE_ITEM_CELL16K.stack(), "{internalCurrentPower:20000.0d}");
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    simpleItemTrading(true, UNLOCK_BASE, stack, TECH_OPERATOR_COIN, 128));
        }

        {
            ItemStack stack = itemStackAddNbtString(AEItems.PORTABLE_FLUID_CELL16K.stack(), "{internalCurrentPower:20000.0d}");
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    simpleItemTrading(true, UNLOCK_BASE, stack, TECH_OPERATOR_COIN, 128));

        }
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                questGatedItemTrading(UNLOCK_BASE, GTMachines.SUPER_TANK[LV].asStack(), TECH_OPERATOR_COIN, 24, LV_ASSEMBLER_QUEST));

        {
            ItemStack stack = itemStackAddNbtString(Objects.requireNonNull(GTMaterialItems.TOOL_ITEMS.get(DamascusSteel, GTToolType.MINING_HAMMER)).asStack(),
                    "{DisallowContainerItem:0b,Enchantments:[{id:\"minecraft:unbreaking\",lvl:3s}],GT.Behaviours:{AoEColumn:1,AoELayer:0,AoERow:1},GT.Tool:{Damage:0},HideFlags:2}");
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    simpleItemTrading(true, UNLOCK_BASE, stack, TECH_OPERATOR_COIN, 48));
        }

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                SimpleLotteryTrading(UNLOCK_BASE, TECH_OPERATOR_COIN, 8,
                        List.of(Component.translatable(addTradeLang(
                                "喝了可乐之后在小机器上跳来跳去可以加速小机器工作",
                                "Jumping up and down on the small machine after drinking Coke can speed up its operation"))),
                        List.of(lotteryItem(25, GTOItems.MYSTERIOUS_BOOST_DRINK[2].asStack()),
                                lotteryItem(75, GTOItems.MYSTERIOUS_BOOST_DRINK[1].asStack()),
                                lotteryItem(900, GTOItems.MYSTERIOUS_BOOST_DRINK[0].asStack()))));

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                SimpleLotteryTrading(UNLOCK_BASE, TECH_OPERATOR_COIN, 256,
                        List.of(Component.translatable(addTradeLang(
                                "喝了可乐之后在小机器上跳来跳去可以加速小机器工作",
                                "Jumping up and down on the small machine after drinking Coke can speed up its operation"))),
                        List.of(lotteryItem(25, GTOItems.MYSTERIOUS_BOOST_DRINK[5].asStack()),
                                lotteryItem(75, GTOItems.MYSTERIOUS_BOOST_DRINK[4].asStack()),
                                lotteryItem(900, GTOItems.MYSTERIOUS_BOOST_DRINK[3].asStack()))));

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                new TradeEntry.Builder()
                        .texture(new StackTexture(new ItemStack(Blocks.BLAST_FURNACE)))
                        .addDescription(Component.translatable(addTradeLang("炼金大师套装", "Alchemy Master Set")))
                        .inputCurrency(TECH_OPERATOR_COIN, 24)
                        .outputItem(new ItemStack(Blocks.BLAST_FURNACE, 4))
                        .outputItem(RegistriesUtils.getItemStack("jumbofurnace:jumbo_furnace", 8))
                        .outputItem(new ItemStack(Items.COAL, 128))
                        .build());

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                simpleItemTrading(true, UNLOCK_BASE, ChemicalHelper.get(ingot, Steel, 32), TECH_OPERATOR_COIN, 64));

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                simpleItemTrading(true, UNLOCK_BASE, new ItemStack(Items.ENDER_PEARL, 4), TECH_OPERATOR_COIN, 8));

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                simpleItemTrading(true, UNLOCK_BASE, new ItemStack(Blocks.CLAY, 64), TECH_OPERATOR_COIN, 16));

        {
            ItemStack sword = RegistriesUtils.getItemStack("minecraft:netherite_sword", 1,
                    "{Damage:0,Enchantments:[{id:\"minecraft:smite\",lvl:7s},{id:\"minecraft:looting\",lvl:3s},{id:\"minecraft:mending\",lvl:1s},{id:\"minecraft:sharpness\",lvl:7s}],RepairCost:31,display:{Name:'{\"text\":\"勇气之剑\"}'}}");
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    new TradeEntry.Builder()
                            .texture(new StackTexture(sword))
                            .addDescription(Component.translatable(addTradeLang("地牢猎手套装", "Dungeon Hunter Set")))
                            .inputCurrency(TECH_OPERATOR_COIN, 64)
                            .outputItem(sword)
                            .outputItem(RegistriesUtils.getItemStack("minecraft:shield", 1,
                                    "{Damage:0,Enchantments:[{id:\"minecraft:unbreaking\",lvl:5s},{id:\"apotheosis:reflective\",lvl:7s}],RepairCost:3,display:{Name:'{\"text\":\"豪迈之盾\"}'}}"))
                            .outputItem(RegistriesUtils.getItemStack("minecraft:bow", 1,
                                    "{Damage:0,Enchantments:[{id:\"minecraft:power\",lvl:8s},{id:\"minecraft:infinity\",lvl:1s},{id:\"minecraft:flame\",lvl:1s},{id:\"minecraft:punch\",lvl:3s}],RepairCost:15,display:{Name:'{\"text\":\"无畏之弓\"}'}}"))
                            .outputItem(new ItemStack(Items.ARROW))
                            .build());
        }

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                simpleItemTrading(true, UNLOCK_BASE, RegistriesUtils.getItemStack("minecraft:enchanted_book", 1,
                        "{StoredEnchantments:[{id:\"minecraft:mending\",lvl:1s}]}"), TECH_OPERATOR_COIN, 128));

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                questGatedItemTrading(UNLOCK_BASE, RegistriesUtils.getItemStack("ad_astra:large_gas_tank", 1,
                        "{BotariumData:{StoredFluids:[{Amount:3000L,Fluid:\"ad_astra:oxygen\"}]}}"), TECH_OPERATOR_COIN, 64, MOON_QUEST));

        if (!GTOCore.isExpert()) {
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    new TradeEntry.Builder()
                            .texture(new StackTexture(ChemicalHelper.get(ingot, Titanium, 3)))
                            .addDescription(Component.translatable(addTradeLang("钛锭", "Titanium Ingots")))
                            .inputItem(ChemicalHelper.get(ingot, Nichrome, 32))
                            .inputCurrency(TECH_OPERATOR_COIN, 64)
                            .outputItem(ChemicalHelper.get(ingot, Titanium, 3))
                            .build());

            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    new TradeEntry.Builder()
                            .texture(new StackTexture(GTItems.FIELD_GENERATOR_HV.asStack()))
                            .addDescription(Component.translatable(addTradeLang("HV力场发生器", "HV Field Generator")))
                            .inputItem(GTItems.FIELD_GENERATOR_MV.asStack(8))
                            .inputCurrency(TECH_OPERATOR_COIN, 64)
                            .outputItem(GTItems.FIELD_GENERATOR_HV.asStack())
                            .build());
        }

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                questGatedItemTrading(UNLOCK_BASE, GTMachines.SUPER_CHEST[LV].asStack(), TECH_OPERATOR_COIN, 16, LV_CIRCUIT_QUEST));

        if (Mods.FACTORY_BLOCKS.isLoaded()) {
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    freeItemTrading(UNLOCK_BASE, RegistriesUtils.getItemStack("factory_blocks:factory", 64)));
        }

        for (String color : List.of("white", "light_gray", "gray", "black", "brown", "red", "orange", "yellow",
                "lime", "green", "cyan", "light_blue", "blue", "purple", "magenta", "pink")) {
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    simpleItemTrading(true, UNLOCK_BASE,
                            RegistriesUtils.getItemStack("botania:" + color + "_mystical_flower", 16), TECH_OPERATOR_COIN, 16));
        }

        for (String ore : List.of("iron", "copper", "tin", "coal")) {
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    simpleItemTrading(true, UNLOCK_BASE, RegistriesUtils.getItemStack("gtceu:" + ore + "_ore", 64), TECH_OPERATOR_COIN, 24));
        }

        for (ItemStack stack : List.of(new ItemStack(Items.HONEYCOMB, 16), new ItemStack(Items.CHORUS_FRUIT),
                new ItemStack(Items.RABBIT_FOOT, 16), RegistriesUtils.getItemStack("apotheosis:gem_dust", 64),
                new ItemStack(Items.GLOW_INK_SAC, 8), RegistriesUtils.getItemStack("apotheosis:mythic_material"))) {
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    simpleItemTrading(true, UNLOCK_BASE, stack, TECH_OPERATOR_COIN, 32));
        }

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.WHITE_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.ORANGE_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.MAGENTA_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.LIGHT_BLUE_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.YELLOW_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.LIME_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.PINK_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.GRAY_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.LIGHT_GRAY_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.CYAN_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.PURPLE_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.BLUE_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.BROWN_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.GREEN_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.RED_CONCRETE, 64)));
        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                freeItemTrading(UNLOCK_BASE, new ItemStack(Items.BLACK_CONCRETE, 64)));

        int ShopIndex2 = TradingManager.INSTANCE.addShopByGroupIndex(
                GroupIndex,
                addTradeLang("福利兑换 贰", "Welfare Redemption 2"),
                UNLOCK_BASE,
                Set.of(TECH_OPERATOR_COIN),
                GuiTextures.GREGTECH_LOGO);
    }
}
