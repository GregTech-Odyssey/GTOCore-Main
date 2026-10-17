package com.gtocore.data.transaction.data;

import com.gtolib.api.lang.CNEN;

import com.gregtechceu.gtceu.GTCEu;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

import java.util.Map;

public final class TradeLang {

    public static final Map<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;

    private static void addTradeLang(String key, String cn, String en) {
        if (LANG != null) LANG.put(key, new CNEN(cn, en));
    }

    public static String addTradeLang(String cn, String en) {
        int hash = cn.hashCode();
        String key = "gtocore.trade." + hash;
        if (TradeLang.LANG != null) TradeLang.LANG.put(key, new CNEN(cn, en));
        return key;
    }

    /// - 技术员币：Tech Operator Coin
    public static final String TECH_OPERATOR_COIN = "tech_operator_coin";
    /// - 研究员币：Research Pioneer Coin
    public static final String RESEARCH_PIONEER_COIN = "research_pioneer_coin";
    /// - 工程师币：Tech Transformer Coin
    public static final String TECH_TRANSFORMER_COIN = "tech_transformer_coin";
    /// - 引航员币：Voyager Navigator Coin
    public static final String VOYAGER_NAVIGATOR_COIN = "voyager_navigator_coin";
    /// - 科技执政官币：Tech Administrator Coin
    public static final String TECH_ADMINISTRATOR_COIN = "tech_administrator_coin";
    /// - 首席科学家币：Chief Science Steward Coin
    public static final String CHIEF_SCIENCE_STEWARD_COIN = "chief_science_steward_coin";

    /// - 能量币：Energy Coin
    /// - 致密能量币：Compact Energy Coin
    /// 比例 524288：1
    public static final String ENERGY_COIN = "energy_coin";
    public static final String COMPACT_ENERGY_COIN = "compact_energy_coin";

    /// - 游戏币：Game Coins
    /// - 游戏里程碑币：Game Milestone Coins
    public static final String GAME_COINS = "game_coins";
    public static final String GAME_MILESTONE_COINS = "game_milestone_coins";

    static {
        addTradeLang("gtocore.currency." + TECH_OPERATOR_COIN, "技术员币", "Tech Operator Coin");
        addTradeLang("gtocore.currency." + RESEARCH_PIONEER_COIN, "研究员币", "Research Pioneer Coin");
        addTradeLang("gtocore.currency." + TECH_TRANSFORMER_COIN, "工程师币", "Tech Transformer Coin");
        addTradeLang("gtocore.currency." + VOYAGER_NAVIGATOR_COIN, "引航员币", "Voyager Navigator Coin");
        addTradeLang("gtocore.currency." + TECH_ADMINISTRATOR_COIN, "科技执政官币", "Tech Administrator Coin");
        addTradeLang("gtocore.currency." + CHIEF_SCIENCE_STEWARD_COIN, "首席科学家币", "Chief Science Steward Coin");

        addTradeLang("gtocore.currency." + ENERGY_COIN, "能量币", "Energy Coin");
        addTradeLang("gtocore.currency." + COMPACT_ENERGY_COIN, "致密能量币", "Compact Energy Coin");

        addTradeLang("gtocore.currency." + GAME_COINS, "游戏币", "Game Coin");
        addTradeLang("gtocore.currency." + GAME_MILESTONE_COINS, "游戏里程碑币", "Game Milestone Coin");

        addTradeLang("gtocore.currency.denomination", "面值: %s", "Denomination : %s");

        addTradeLang("gtocore.trade.quest_id", "任务 ID：%s", "Quest ID: %s");

        addTradeLang("gtocore.palm_sized_bank.textList.1", "欢迎使用掌上银行！", "Welcome to Mobile Banking!");
        addTradeLang("gtocore.palm_sized_bank.textList.2", "在这里, 您可以方便地管理您的虚拟资产", "Here, you can conveniently manage your virtual assets");
        addTradeLang("gtocore.palm_sized_bank.textList.3", "请注意保护您的账户信息, 避免泄露给他人", "Please be careful to protect your account information and avoid disclosing it to others");
        addTradeLang("gtocore.palm_sized_bank.textList.4", "祝您使用愉快！", "Wish you a pleasant experience!");
        addTradeLang("gtocore.palm_sized_bank.textList.5", "无法获取玩家信息", "Failed to obtain player information");
        addTradeLang("gtocore.palm_sized_bank.textList.6", "当前用户: %s", "Current user: %s");
        addTradeLang("gtocore.palm_sized_bank.textList.7", "用户 UUID: %s", "User UUID: %s");
        addTradeLang("gtocore.palm_sized_bank.textList.8", "创建钱包", "Create a wallet");
        addTradeLang("gtocore.palm_sized_bank.textList.9", "钱包不存在", "Wallet does not exist");

        addTradeLang("gtocore.palm_sized_bank.textList.10", "资产总览", "Asset Overview");
        addTradeLang("gtocore.palm_sized_bank.textList.11", "货币种类", "Currency Type");
        addTradeLang("gtocore.palm_sized_bank.textList.12", "持有数量", "Amount");

        addTradeLang("gtocore.palm_sized_bank.textList.20", "交易记录", "Transaction records");
        addTradeLang("gtocore.palm_sized_bank.textList.21", "交易主键", "Transaction Key");
        addTradeLang("gtocore.palm_sized_bank.textList.22", "交易总量", "Total transaction volume");
        addTradeLang("gtocore.palm_sized_bank.textList.23", "策略类型标识", "Strategy type identifier");
        addTradeLang("gtocore.palm_sized_bank.textList.24", "本分钟交易量", "Trading volume this minute");
        addTradeLang("gtocore.palm_sized_bank.textList.25", "上一分钟交易量", "Previous minute's trading volume");
        addTradeLang("gtocore.palm_sized_bank.textList.26", "交易明细", "Transaction details");

        addTradeLang("gtocore.palm_sized_bank.textList.30", "标签组", "Tag Group");
        addTradeLang("gtocore.palm_sized_bank.textList.31", "标签列表", "Tag list");

        addTradeLang("gtocore.palm_sized_bank.textList.40", "需要%s%s", "need%s%s");
        addTradeLang("gtocore.palm_sized_bank.textList.41", "[获取会员卡]", "[Get membership card]");
        addTradeLang("gtocore.palm_sized_bank.textList.42", "添加到共享名单", "Add to shared list");
        addTradeLang("gtocore.palm_sized_bank.textList.43", "共享名单", "Shared list");

        addTradeLang("gtocore.palm_sized_bank.textList.50", "转账给此账户", "Transfer to this account");
        addTradeLang("gtocore.palm_sized_bank.textList.51", "转账此货币", "Transfer this currency");
        addTradeLang("gtocore.palm_sized_bank.textList.52", "转账金额", "Transfer amount");
        addTradeLang("gtocore.palm_sized_bank.textList.53", "[转账]", "[Transfer]");
        addTradeLang("gtocore.palm_sized_bank.textList.54", "[确认转账]", "[Transfer Confirmed]");

        addTradeLang("gtocore.gray_membership_card.hover_text.1", "主人: ", "Owner: ");
        addTradeLang("gtocore.gray_membership_card.hover_text.2", "共享者: ", "Shared by: ");

        addTradeLang("gtocore.trade_group.true", "价格", "Price");
        addTradeLang("gtocore.trade_group.false", "商品", "Commodity");
        addTradeLang("gtocore.trade_group.description", "说明", "Description");
        addTradeLang("gtocore.trade_group.unlock", "未解锁, 需要解锁 %s", "Not unlocked, needs to be unlocked %s");
        addTradeLang("gtocore.trade_group.unsatisfied", "不满足额外条件", "Additional conditions not met");
        addTradeLang("gtocore.trade_group.amount", "可交易次数：%s", "Available trades: %s");
        addTradeLang("gtocore.trade_group.unlimited", "无限", "Unlimited");
        addTradeLang("gtocore.trade_group.repeatedly.1", "[%s + 点击] 最多交易 10 次", "[%s + Click] Trade up to 10 times");
        addTradeLang("gtocore.trade_group.repeatedly.2", "[%s + 点击] 最多交易 100 次", "[%s + Click] Trade up to 100 times");
        addTradeLang("gtocore.trade_group.exchanged", "将%2$s兑换为%1$s", "Exchange %1$s for %2$s");

        addTradeLang("gtocore.trading_station.unlock_shop", "解锁商店", "Unlock Store");
        addTradeLang("gtocore.trading_station.item_storage", "物品存储", "Item Storage");
        addTradeLang("gtocore.trading_station.fluid_storage", "流体存储", "Fluid Storage");
        addTradeLang("gtocore.trading_station.help", "使用说明", "Guide");
        addTradeLang("gtocore.trading_station.open_help", "查看使用说明", "View Guide");
        addTradeLang("gtocore.trading_station.help.tour", "界面导览", "Interface Tour");
        addTradeLang("gtocore.trading_station.help.tour.1", "1. 主控制面板：这里是你的指挥中心。左侧显示你的会员信息、共享权限列表，并有一个刷新按钮。右侧是商店组的快捷切换区，点击图标即可瞬间切换到不同的商品目录。", "1. Main Control Panel: This is your command center. The left side displays your membership info, shared permissions list, and a refresh button. The right side is a quick-switch area for shop groups; click an icon to instantly change to a different product catalog.");
        addTradeLang("gtocore.trading_station.help.tour.2", "2. 库存管理：在这里，你可以看到所有的物品和流体存储。左侧是物品槽，右侧是流体槽（输入在左，输出在右）。你可以直接拖拽物品进行存取，非常直观。随着等级提升，这里的格子会越来越多。", "2. Inventory Management: Here, you can see all your item and fluid storage. The left side is for item slots, and the right side is for fluid tanks (input on the left, output on the right). You can directly drag and drop items to deposit or withdraw them, which is very intuitive. As you tier up, more slots will become available here.");
        addTradeLang("gtocore.trading_station.help.tour.3", "3. 交易解锁：想知道如何开启更高级的交易吗？来这里看看。左侧显示当前贸易站等级和可解锁的贸易组，右侧则详细列出了每个贸易组的解锁条件和所需资源。满足条件后，新的贸易就会自动为你开启。", "3. Trade Unlocks: Want to know how to unlock more advanced trades? Check here. The left side shows your current station tier and available trade groups to unlock. The right side details the unlock conditions and required resources for each group. Once met, new trades will automatically become available to you.");
        addTradeLang("gtocore.trading_station.help.tour.4", "4. 商店货架：这是你进行交易的地方。每个商店组都有多个货架标签，每个标签下陈列着16种交易商品。将鼠标悬停在商品图标上，可以查看详细的交易说明和解锁状态。找到你想要的，点击即可完成交易。", "4. Shop Shelves: This is where you perform trades. Each shop group has multiple shelf tabs, and each tab displays 16 tradeable goods. Hover your mouse over an item icon to see detailed trade descriptions and unlock status. Find what you want, click, and the trade is done.");
        addTradeLang("gtocore.trading_station.help.start", "快速上手指南", "Quick Start Guide");
        addTradeLang("gtocore.trading_station.help.start.1", "第一步：找个好地方放下你的贸易站，然后右键点击打开界面。将你的格雷会员卡放入左侧的卡槽中，贸易站即刻激活，并加载你的个人数据和基础贸易目录。", "Step 1: Find a good spot to place your Trading Station, then right-click to open its interface. Insert your Greg Membership Card into the left card slot. The station will activate immediately, loading your personal data and basic trade catalogs.");
        addTradeLang("gtocore.trading_station.help.start.2", "第二步：切换到“库存”标签页，将你想要交易的原料（比如一堆矿石或一桶流体）放入左侧的“输入”槽中。放心，产出的成品会自动进入右侧的“输出”槽，无需手动分配。", "Step 2: Switch to the \"Inventory\" tab. Place the raw materials you want to trade (like a stack of ore or a bucket of fluid) into the left \"Input\" slots. Don't worry, the finished products will automatically go into the right \"Output\" slots; no manual sorting needed.");
        addTradeLang("gtocore.trading_station.help.start.3", "第三步：去“交易解锁”标签页看看有什么新东西可以解锁。如果你满足了某个贸易组的解锁条件（比如 station 等级够了，或者完成了前置交易），它就会自动解锁。", "Step 3: Head to the \"Trade Unlocks\" tab to see what new things you can unlock. If you meet the conditions for a trade group (like having a high enough station tier or completing prerequisite trades), it will unlock automatically.");
        addTradeLang("gtocore.trading_station.help.start.4", "第四步：在主面板切换到你刚解锁的商店组，浏览货架，找到你想执行的交易。确认输入槽里的原料充足后，点击交易图标即可。如果想批量处理，记得使用Ctrl或Shift快捷键。", "Step 4: Switch to the newly unlocked shop group on the main panel, browse the shelves, and find the trade you want to execute. After confirming you have enough raw materials in the input slots, click the trade icon. Remember to use Ctrl or Shift for bulk transactions.");
        addTradeLang("gtocore.trading_station.help.start.5", "第五步：当你的贸易站等级足够高时，别忘了设置“自动输出”。在方向配置界面设定好输出管道的方向，开启自动输出后，你的成品就会源源不断地自动运送到你的仓库里，实现真正的无人值守贸易。", "Step 5: When your trading station is tier high enough, don't forget to set up \"Auto-Output\". Configure the output direction for your pipes in the direction settings interface. Once auto-output is enabled, your finished products will be continuously and automatically delivered to your warehouse, achieving true unattended trading.");
        addTradeLang("gtocore.trading_station.help.notes", "温馨提示", "Helpful Tips");
        addTradeLang("gtocore.trading_station.help.notes.1", "1. 会员卡是你的命脉！请务必妥善保管。建议提前设置好共享权限，以防万一卡片丢失，你的伙伴还能帮你管理。", "1. Your Membership Card is your lifeline! Please keep it safe. It's advisable to set up shared permissions in advance, so in case the card is lost, your teammates can still help you manage the station.");
        addTradeLang("gtocore.trading_station.help.notes.2", "2. 注意容量限制！在进行大规模批量交易前，确保你的输出槽有足够的空间容纳成品，以免因空间不足导致交易失败。", "2. Watch your capacity limits! Before performing large-scale bulk transactions, ensure your output slots have enough space for the finished products to avoid transaction failure due to insufficient space.");
        addTradeLang("gtocore.trading_station.help.notes.3", "3. 自动输出需要配合！确保你设置的输出方向上有管道或其他可接收的设备（如箱子、ME接口），否则物品会无法送出。", "3. Auto-Output needs cooperation! Ensure there are pipes or other receivable devices (like chests, ME Interfaces) in the direction you've set; otherwise, items won't be able to be sent out.");
        addTradeLang("gtocore.trading_station.help.notes.4", "4. 幸运商店每日刷新！记得每天都来看看，也许能淘到平时买不到的稀有宝贝。高等级的贸易站刷新的商品会更好哦！", "4. Lucky Merchant refreshes daily! Remember to check back every day; you might find rare treasures that are not normally available. Higher-tier stations offer even better items!");

        addTradeLang("gtocore.trading_station.textList.2", "⇦ 请放入会员卡", "⇦ Please insert your membership card.");
        addTradeLang("gtocore.trading_station.textList.3", "欢迎「 %s 」", "Welcome「 %s 」");
        addTradeLang("gtocore.trading_station.textList.4", "共享给: ", "Share with: ");
        addTradeLang("gtocore.trading_station.textList.8", "[刷新]", "[Refresh]");

        addTradeLang("gtocore.trading_station.textList.11", "请从掌上银行获取会员卡以使用交易站", "Please obtain a membership card from your mobile banking app to use the trading platform.");

        addTradeLang("gtocore.trading_station.textList.20", "商店未解锁, 需要解锁 %s", "Store locked, needs to be unlocked %s");
        addTradeLang("gtocore.trading_station.textList.21", "交易解锁", "Transaction unlock");
        addTradeLang("gtocore.trading_station.textList.22", "解锁 %s", "Unlock %s");

        addTradeLang("gtocore.trade.condition.header", "限制条件", "Restrictions");
        addTradeLang("gtocore.trade.condition.single.available", "购买限制：仅限 1 次（未购买）", "Purchase limit: once only (not purchased)");
        addTradeLang("gtocore.trade.condition.single.used", "购买限制：仅限 1 次（已购买）", "Purchase limit: once only (purchased)");
        addTradeLang("gtocore.trade.condition.periodic", "周期限制：本周期最多交易 %s 次，已交易 %s 次", "Periodic limit: at most %s trades this period, %s used");
        addTradeLang("gtocore.trade.condition.weekly.available", "签到限制：本周期只能领取 1 次（未领取）", "Check-in limit: once this period (not claimed)");
        addTradeLang("gtocore.trade.condition.weekly.used", "签到限制：本周期只能领取 1 次（已领取）", "Check-in limit: once this period (claimed)");
        addTradeLang("gtocore.trade.condition.unlock.available", "解锁限制：只能执行 1 次（未执行）", "Unlock limit: once only (not executed)");
        addTradeLang("gtocore.trade.condition.unlock.used", "解锁限制：只能执行 1 次（已执行）", "Unlock limit: once only (executed)");
        addTradeLang("gtocore.trade.condition.quest", "前置任务：%s（%s）", "Quest prerequisite: %s (%s)");
        addTradeLang("gtocore.trade.condition.met", "已完成", "Completed");
        addTradeLang("gtocore.trade.condition.unmet", "未完成", "Incomplete");

        addTradeLang("gtocore.trade_lottery.weight", " [权重: %s]", " [Weight: %s]");
    }
}
