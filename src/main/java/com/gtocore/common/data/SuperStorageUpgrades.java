package com.gtocore.common.data;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.item.DrawerStorageUpgradeItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.gto.registrate.util.entry.ItemEntry;

import static com.gtolib.utils.register.ItemRegisterUtils.item;

@DataGeneratorScanned
public final class SuperStorageUpgrades {

    @RegisterLanguage(cn = "用于抽屉存储器，将每种类容量提升 %s 倍", en = "Only for Drawer Storage: increases capacity per type by %s times")
    private static final String CAPACITY = "gtocore.super_storage.upgrade.capacity";

    @RegisterLanguage(cn = "使输入总线每个槽位仅按一个超级箱/缸统计种类数来换取更高的平均容量", en = "Makes the Input Bus count only one Super Chest/Tank per slot for type counting in exchange for higher average capacity")
    private static final String AGGREGATION_EFFECT = "gtocore.super_storage.upgrade.aggregation";

    public static final ItemEntry<DrawerStorageUpgradeItem> BRONZE = upgrade("bronze", "青铜升级", "Bronze Upgrade", 2);
    public static final ItemEntry<DrawerStorageUpgradeItem> STEEL = upgrade("steel", "钢升级", "Steel Upgrade", 4);
    public static final ItemEntry<DrawerStorageUpgradeItem> ALUMINIUM = upgrade("aluminium", "铝升级", "Aluminium Upgrade", 8);
    public static final ItemEntry<DrawerStorageUpgradeItem> TITANIUM = upgrade("titanium", "钛升级", "Titanium Upgrade", 12);
    public static final ItemEntry<DrawerStorageUpgradeItem> TUNGSTEN_STEEL = upgrade("tungsten_steel", "钨钢升级", "Tungsten Steel Upgrade", 16);
    public static final ItemEntry<DrawerStorageUpgradeItem> AGGREGATION = item("aggregation_super_storage_upgrade", "聚合升级", p -> new DrawerStorageUpgradeItem(p, 2, true))
            .tips(Component.translatable(CAPACITY, 2).withStyle(ChatFormatting.GRAY),
                    Component.translatable(AGGREGATION_EFFECT).withStyle(ChatFormatting.GRAY))
            .lang("Aggregation Upgrade").register();

    @SuppressWarnings("unchecked")
    public static final ItemEntry<DrawerStorageUpgradeItem>[] UPGRADES = new ItemEntry[] { BRONZE, STEEL, ALUMINIUM, TITANIUM, TUNGSTEN_STEEL };

    private SuperStorageUpgrades() {}

    private static ItemEntry<DrawerStorageUpgradeItem> upgrade(String material, String cn, String en, int multiplier) {
        return item(material + "_super_storage_upgrade", cn, p -> new DrawerStorageUpgradeItem(p, multiplier))
                .tips(Component.translatable(CAPACITY, multiplier).withStyle(ChatFormatting.GRAY))
                .lang(en).register();
    }

    public static void init() {}
}
