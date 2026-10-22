package com.gtocore.data.transaction.data.trade;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.api.gui.StackTexture;
import com.gtocore.common.data.GTOItems;
import com.gtocore.data.transaction.data.CoinExchange;
import com.gtocore.data.transaction.data.CoinSeries;
import com.gtocore.data.transaction.manager.TradeData;
import com.gtocore.data.transaction.manager.TradeEntry;
import com.gtocore.data.transaction.manager.TradingManager;

import com.gtolib.utils.WalletUtils;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.gui.GuiTextures;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import java.util.List;
import java.util.Set;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTOMachines.*;
import static com.gtocore.data.transaction.data.GTOTrade.limitedTimesCondition;
import static com.gtocore.data.transaction.data.GTOTrade.performAddMultiplier;
import static com.gtocore.data.transaction.data.TradeLang.GAME_COINS;
import static com.gtocore.data.transaction.data.TradeLang.TECH_OPERATOR_COIN;
import static com.gtocore.data.transaction.data.TradeLang.addTradeLang;
import static com.gtocore.data.transaction.data.trade.UnlockTrade.UNLOCK_BASE;
import static com.lowdragmc.lowdraglib.LDLib.random;

public final class WelcomeGroup {

    /**
     * 欢迎来到格雷科技
     * <p>
     * - 币兑区
     * - 会员区
     */
    public static void init() {
        int GroupIndex = TradingManager.INSTANCE.addShopGroup(
                addTradeLang("欢迎来到格雷科技", "Welcome to Gray Technology"),
                GuiTextures.GREGTECH_LOGO,
                GuiTextures.GREGTECH_LOGO);

        int ShopIndex1 = TradingManager.INSTANCE.addShopByGroupIndex(
                GroupIndex,
                addTradeLang("格雷科技销售部兑币区", "Currency Exchange Area of Gray Technology Sales Department"),
                UNLOCK_BASE,
                Set.of(TECH_OPERATOR_COIN, GAME_COINS),
                new ItemStackTexture(ChemicalHelper.get(GTOTagPrefix.COIN, Neutronium)));

        addCoinTrades(GroupIndex, ShopIndex1, CoinExchange.techOperator());
        addCoinTrades(GroupIndex, ShopIndex1, CoinExchange.gameCoins());

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex1, new TradeEntry.Builder()
                .texture(new StackTexture(ChemicalHelper.get(GTOTagPrefix.COIN, Neutronium), GTOItems.GAME_COINS_1.asStack()))
                .addDescription(Component.translatable(addTradeLang("将游戏币兑换为技术员币", "Exchange Game Coin to Tech Operator Coin."))).unlockCondition(UNLOCK_BASE)
                .inputCurrency(GAME_COINS, 1)
                .outputCurrency(TECH_OPERATOR_COIN, 5)
                .condition(limitedTimesCondition(Game_coin_to_tech_coin, Game_coin_to_tech_coin_limit))
                .onExecute((data, entry, multiplier) -> performAddMultiplier(data, entry, multiplier, Game_coin_to_tech_coin, Weekly_time))
                .build());

        int ShopIndex2 = TradingManager.INSTANCE.addShopByGroupIndex(
                GroupIndex,
                addTradeLang("格雷科技销售部会员区", "Membership Area of Gray Technology Sales Department"),
                UNLOCK_BASE,
                Set.of(TECH_OPERATOR_COIN),
                new ResourceTexture("minecraft:textures/mob_effect/luck.png"));

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex2, new TradeEntry.Builder()
                .texture(new ResourceTexture("minecraft:textures/mob_effect/luck.png"))
                .description(List.of(
                        Component.translatable(addTradeLang("每周签到", "Weekly check-in")),
                        Component.translatable(addTradeLang("领取幸运物资", "Claim lucky supplies"))))
                .unlockCondition(UNLOCK_BASE)
                .condition(WelcomeGroup::checkThisWeek,
                        remaining -> Component.translatable(remaining > 0 ?
                                "gtocore.trade.condition.weekly.available" : "gtocore.trade.condition.weekly.used"))
                .onExecute(WelcomeGroup::performCheckIn)
                .build());

        for (int i = 1; i < 8; i++) {
            TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex2, new TradeEntry.Builder()
                    .texture(new StackTexture(TRADING_STATION[1].asStack()))
                    .addDescription(Component.translatable(addTradeLang("升级贸易站", "Upgrade Trading Station")))
                    .addDescription(Component.translatable("gtocore.trade_group.exchanged", "Tier" + (i + 1), "Tier" + i))
                    .unlockCondition(UNLOCK_BASE)
                    .inputItem(TRADING_STATION[i].asStack())
                    .inputCurrency(TECH_OPERATOR_COIN, 4 << (i * 3))
                    .outputItem(TRADING_STATION[i + 1].asStack())
                    .build());
        }

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex2, new TradeEntry.Builder()
                .texture(new StackTexture(SLOT_MACHINE.asStack()))
                .unlockCondition(UNLOCK_BASE)
                .inputCurrency(TECH_OPERATOR_COIN, 5)
                .outputItem(SLOT_MACHINE.asStack())
                .build());

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex2, new TradeEntry.Builder()
                .texture(new StackTexture(WIDE_SLOT_MACHINE.asStack()))
                .unlockCondition(UNLOCK_BASE)
                .inputCurrency(TECH_OPERATOR_COIN, 25)
                .outputItem(WIDE_SLOT_MACHINE.asStack())
                .build());

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex2, new TradeEntry.Builder()
                .texture(new StackTexture(JUST_GET_10.asStack()))
                .unlockCondition(UNLOCK_BASE)
                .inputCurrency(TECH_OPERATOR_COIN, 25)
                .outputItem(JUST_GET_10.asStack())
                .build());

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex2, new TradeEntry.Builder()
                .texture(new StackTexture(GAME_2048.asStack()))
                .unlockCondition(UNLOCK_BASE)
                .inputCurrency(TECH_OPERATOR_COIN, 25)
                .outputItem(GAME_2048.asStack())
                .build());

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex2, new TradeEntry.Builder()
                .texture(new StackTexture(GAME_24.asStack()))
                .unlockCondition(UNLOCK_BASE)
                .inputCurrency(TECH_OPERATOR_COIN, 25)
                .outputItem(GAME_24.asStack())
                .build());

        TradingManager.INSTANCE.addTradeEntryByIndices(GroupIndex, ShopIndex2, new TradeEntry.Builder()
                .texture(new StackTexture(MINESWEEPER_CORE.asStack()))
                .unlockCondition(UNLOCK_BASE)
                .inputCurrency(TECH_OPERATOR_COIN, 25)
                .outputItem(MINESWEEPER_CORE.asStack())
                .build());
    }

    /** 为一种货币登记各面额的「实体币换入钱包」与「钱包换出实体币」交易。 */
    private static void addCoinTrades(int groupIndex, int shopIndex, CoinSeries coins) {
        for (int tier = 0; tier < coins.typeCount(); tier++) {
            TradingManager.INSTANCE.addTradeEntryByIndices(groupIndex, shopIndex, createCoinExchangeTrade(coins, tier));
            TradingManager.INSTANCE.addTradeEntryByIndices(groupIndex, shopIndex, createCoinWithdrawTrade(coins, tier));
        }
    }

    public static TradeEntry createCoinExchangeTrade(CoinSeries coins, int tier) {
        ItemStack coin = coins.itemStack(tier);
        return new TradeEntry.Builder()
                .texture(new ItemStackTexture(coin))
                .description(List.of(Component.translatable("gtocore.trade_group.exchanged", Component.translatable("gtocore.currency." + coins.currencyId()), coin.getDisplayName())))
                .unlockCondition(UNLOCK_BASE)
                .inputItem(coin)
                .outputCurrency(coins.currencyId(), coins.value(tier))
                .build();
    }

    public static TradeEntry createCoinWithdrawTrade(CoinSeries coins, int tier) {
        ItemStack coin = coins.itemStack(tier);
        return new TradeEntry.Builder()
                .texture(new ItemStackTexture(coin))
                .description(List.of(Component.translatable("gtocore.trade_group.exchanged", coin.getDisplayName(), Component.translatable("gtocore.currency." + coins.currencyId()))))
                .unlockCondition(UNLOCK_BASE)
                .inputCurrency(coins.currencyId(), coins.value(tier))
                .outputItem(coin)
                .build();
    }

    private static final String Weekly_check_in = "Weekly check-in";
    private static final long Weekly_time = 140L;

    /** 游戏币换技术员币：交易历史标记与每游戏周（{@link #Weekly_time} 游戏分钟）的兑换上限。 */
    private static final String Game_coin_to_tech_coin = "Game coin -> tech operator coin";
    private static final int Game_coin_to_tech_coin_limit = 50;

    // 前置检查逻辑：检查本周是否签过到
    private static int checkThisWeek(TradeData data, TradeEntry entry) {
        Level level = data.level();
        ServerLevel serverLevel = level instanceof ServerLevel ? (ServerLevel) level : null;
        long time = WalletUtils.getGameMinuteKey(level) / Weekly_time * Weekly_time;
        return WalletUtils.getTransactionMinuteAmount(data.uuid(), serverLevel, Weekly_check_in, time) == 0 ? 1 : 0;
    }

    // 执行逻辑：添加标记，随机给予0-100技术员币
    private static void performCheckIn(TradeData data, TradeEntry entry, int multiplier) {
        Level level = data.level();
        ServerLevel serverLevel = level instanceof ServerLevel ? (ServerLevel) level : null;
        WalletUtils.addTimingCompressionStrategyTransaction(data.uuid(), serverLevel, Weekly_check_in, Weekly_time, 1);
        WalletUtils.addCurrency(data.uuid(), serverLevel, TECH_OPERATOR_COIN, random.nextInt() & 100);
    }
}
