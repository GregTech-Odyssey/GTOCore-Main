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

import net.minecraft.ChatFormatting;
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
                        .addDescription(Component.translatable(addTradeLang("以后会添加更多购买项以及钱币获取途径",
                                "More purchase options and ways to obtain currency will be added in the future")))
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
                    new TradeEntry.Builder()
                            .texture(new StackTexture(stack))
                            .unlockCondition(UNLOCK_BASE)
                            .addDescription(Component.translatable(addTradeLang("主线送了一个，如果你丢了可以在这里买",
                                    "The main quest gave you one, you can buy it here if you lost it.")))
                            .inputCurrency(TECH_OPERATOR_COIN, 16)
                            .outputItem(stack)
                            .build());

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

        {
            ItemStack stack = GTOItems.PRECISION_STEAM_MECHANISM.asStack();
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    new TradeEntry.Builder()
                            .texture(new StackTexture(stack))
                            .unlockCondition(UNLOCK_BASE)
                            .addDescription(Component.translatable(addTradeLang("提前造出大蒸汽机器",
                                    "Build large steam machines in advance")))
                            .inputCurrency(TECH_OPERATOR_COIN, 128)
                            .outputItem(stack)
                            .build());
        }

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

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                questGatedItemTrading(UNLOCK_BASE, GTMachines.SUPER_CHEST[LV].asStack(), TECH_OPERATOR_COIN, 16, LV_CIRCUIT_QUEST));

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
                                "Jumping up and down on the small machine after drinking Coke can speed up its operation")),
                                Component.translatable(addTradeLang("可乐等级/机械之神附身的等级代表其最高能加速的小机器的等级",
                                        "Cola Level / Possession of the Machine God Level represents the highest level of small machines it can accelerate."))),
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

        if (!GTOCore.isExpert()) {
            var titaniumTrade = new TradeEntry.Builder()
                    .texture(new StackTexture(ChemicalHelper.get(ingot, Titanium, 3)))
                    .description(List.of(
                            Component.translatable(addTradeLang("进入HV之后再来兑换吧", "Come back to redeem after entering HV.")),
                            Component.translatable(addTradeLang("能让你提前做出少量的HV整体框架", "Allows you to craft a small amount of HV Integral Framework in advance."))))
                    .inputItem(ChemicalHelper.get(ingot, Nichrome, 32))
                    .inputCurrency(TECH_OPERATOR_COIN, 64)
                    .outputItem(ChemicalHelper.get(ingot, Titanium, 3));
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1, titaniumTrade.build());

            var fieldGeneratorTrade = new TradeEntry.Builder()
                    .texture(new StackTexture(GTItems.FIELD_GENERATOR_HV.asStack()))
                    .description(List.of(
                            Component.translatable(addTradeLang("进入HV之后再来兑换吧", "Come back to redeem after entering HV.")),
                            Component.translatable(addTradeLang("能让你提前做出金刚杵和普通难度下的HV输入输出总成", "Allows you to craft the Vajra in advance and the HV Importing/Exporting Dual Output Hatch on Normal difficulty."))))
                    .inputItem(GTItems.FIELD_GENERATOR_MV.asStack(8))
                    .inputCurrency(TECH_OPERATOR_COIN, 64)
                    .outputItem(GTItems.FIELD_GENERATOR_HV.asStack());
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1, fieldGeneratorTrade.build());
        }

        // ----------------------------------------------------------------

        int ShopIndex2 = TradingManager.INSTANCE.addShopByGroupIndex(
                GroupIndex,
                addTradeLang("福利兑换 贰", "Welfare Redemption 2"),
                UNLOCK_BASE,
                Set.of(TECH_OPERATOR_COIN),
                GuiTextures.GREGTECH_LOGO);

        String factoryBlocksDescription = addTradeLang("不想手动点也可以制作建筑方块的无限AE元件",
                "Don't want to click manually? You can also craft an Infinite AE Component for building blocks.");
        String factoryBlocksSubtitle = addTradeLang("建筑师必备", "Essential for Architects");
        if (Mods.FACTORY_BLOCKS.isLoaded()) {
            ItemStack stack = RegistriesUtils.getItemStack("factory_blocks:factory", 64);
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1,
                    new TradeEntry.Builder()
                            .texture(new StackTexture(stack))
                            .unlockCondition(UNLOCK_BASE)
                            .addDescription(Component.translatable(factoryBlocksDescription))
                            .addDescription(Component.translatable(factoryBlocksSubtitle))
                            .outputItem(stack)
                            .build());
        }

        for (String color : List.of("white", "light_gray", "gray", "black", "brown", "red", "orange", "yellow",
                "lime", "green", "cyan", "light_blue", "blue", "purple", "magenta", "pink")) {
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex2,
                    simpleItemTrading(true, UNLOCK_BASE,
                            RegistriesUtils.getItemStack("botania:" + color + "_mystical_flower", 16), TECH_OPERATOR_COIN, 16));
        }
        for (String color : List.of("white", "light_gray", "gray", "black", "brown", "red", "orange", "yellow",
                "lime", "green", "cyan", "light_blue", "blue", "purple", "magenta", "pink")) {
            ItemStack stack = RegistriesUtils.getItemStack("minecraft:" + color + "_concrete", 64);
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex2,
                    new TradeEntry.Builder()
                            .texture(new StackTexture(stack))
                            .unlockCondition(UNLOCK_BASE)
                            .addDescription(Component.translatable(addTradeLang("不想手动点也可以制作混凝土的无限AE元件",
                                    "Don't want to click manually? You can also craft an Infinite AE component for Concrete.")))
                            .addDescription(Component.translatable(addTradeLang("免费续杯", "Free Refills")))
                            .outputItem(stack)
                            .build());
        }
    }
}
