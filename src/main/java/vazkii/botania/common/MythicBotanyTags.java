package vazkii.botania.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * 神话植物学复刻的物品标签。
 */
public final class MythicBotanyTags {

    private MythicBotanyTags() {}

    /** 可以放进符文支托的物品：植物魔法的所有符文与菲姆布尔的符文牌。 */
    public static final TagKey<Item> RITUAL_RUNES = TagKey.create(Registries.ITEM, new ResourceLocation("mythicbotany", "ritual_runes"));
}
