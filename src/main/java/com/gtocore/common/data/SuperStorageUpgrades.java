package com.gtocore.common.data;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.item.StorageUpgradeItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.gto.registrate.util.entry.ItemEntry;

import static com.gtolib.utils.register.ItemRegisterUtils.item;

@DataGeneratorScanned
public final class SuperStorageUpgrades {

    @RegisterLanguage(cn = "将超级箱/缸的容量提升 %s 倍", en = "Increases the capacity of Super Chest/Tank by %s times")
    private static final String CAPACITY = "gtocore.super_storage.upgrade.capacity";

    public static final ItemEntry<StorageUpgradeItem> BRONZE = upgrade("bronze", "青铜升级", "Bronze Upgrade", 2);
    public static final ItemEntry<StorageUpgradeItem> STEEL = upgrade("steel", "钢升级", "Steel Upgrade", 4);
    public static final ItemEntry<StorageUpgradeItem> ALUMINIUM = upgrade("aluminium", "铝升级", "Aluminium Upgrade", 8);
    public static final ItemEntry<StorageUpgradeItem> TITANIUM = upgrade("titanium", "钛升级", "Titanium Upgrade", 16);
    public static final ItemEntry<StorageUpgradeItem> TUNGSTEN_STEEL = upgrade("tungsten_steel", "钨钢升级", "Tungsten Steel Upgrade", 32);

    public static final ItemEntry[] UPGRADES = new ItemEntry[] { BRONZE, STEEL, ALUMINIUM, TITANIUM, TUNGSTEN_STEEL };

    private SuperStorageUpgrades() {}

    private static ItemEntry<StorageUpgradeItem> upgrade(String material, String cn, String en, int multiplier) {
        return item(material + "_super_storage_upgrade", cn, p -> new StorageUpgradeItem(p, multiplier))
                .tips(Component.translatable(CAPACITY, multiplier).withStyle(ChatFormatting.GRAY))
                .lang(en).register();
    }

    public static void init() {}
}
