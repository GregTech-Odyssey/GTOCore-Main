package vazkii.botania.common;

import com.gtocore.data.lang.LangHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * 复刻内容在注册时登记的中英文名，由 GTOCore 的语言生成器统一写出。
 */
public final class BotaniaLang {

    private static final Object2ObjectOpenHashMap<String, String[]> LANG = new Object2ObjectOpenHashMap<>();

    static {
        add("botania.ae2.mana", "Mana", "Mana");
        add("botania.ae2.cell_stored", "Stored: %s / %s Mana", "已存：%s / %s 魔力");
        add("item.botania.mana_cell_housing", "ME Mana Cell Housing", "ME 魔力元件外壳");
        add("item.botania.mana_storage_cell_1k", "1k ME Mana Storage Cell", "1k ME 魔力存储元件");
        add("item.botania.mana_storage_cell_4k", "4k ME Mana Storage Cell", "4k ME 魔力存储元件");
        add("item.botania.mana_storage_cell_16k", "16k ME Mana Storage Cell", "16k ME 魔力存储元件");
        add("item.botania.mana_storage_cell_64k", "64k ME Mana Storage Cell", "64k ME 魔力存储元件");
        add("item.botania.mana_storage_cell_256k", "256k ME Mana Storage Cell", "256k ME 魔力存储元件");
        add("item.botania.creative_mana_cell", "Creative ME Mana Cell", "创造 ME 魔力存储元件");
        LANG.put("itemGroup.botania.gto", new String[] { "植物魔法复刻", "Botania Replica" });
        LANG.put("entity.botania.doppleganger", new String[] { "盖亚守护者", "Gaia Guardian" });
        LANG.put("botaniamisc.gaiaRitualPylons", new String[] { "需要 4 个盖亚水晶围绕召唤点", "Requires 4 Gaia Pylons around the ritual site" });
    }

    private BotaniaLang() {}

    static void add(String key, String cn, String en) {
        LANG.put(key, new String[] { cn, en });
    }

    public static void init() {
        LANG.forEach((key, value) -> LangHandler.addCNEN(key, value[0], value[1]));
    }
}
