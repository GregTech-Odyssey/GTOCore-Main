package vazkii.botania.common.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.BotaniaReplica;

/**
 * 复刻工具与武器的材质等级。
 *
 * <p>
 * 数值按原模组量级给出：植物钢接近铁、元素接近钻石、泰拉钢与精灵钢更高。
 * 修复材料按注册名指向对应锭或材料物品。
 * </p>
 */
public enum BotaniaTiers implements Tier {

    MANASTEEL(200, 6.5F, 1.5F, 2, 18, "manasteel_ingot"),
    ELEMENTIUM(800, 8.0F, 2.0F, 3, 20, "elementium_ingot"),
    TERRASTEEL(2500, 9.0F, 3.0F, 4, 25, "terrasteel_ingot"),
    ALFSTEEL(3000, 10.0F, 3.5F, 4, 30, "alfsteel_ingot"),
    GAIA(2600, 9.5F, 3.5F, 4, 26, "gaia_ingot"),
    PHOTONIUM(2800, 9.5F, 3.0F, 4, 26, "photonium_ingot"),
    SHADOWIUM(2800, 9.5F, 3.0F, 4, 26, "shadowium_ingot"),
    AERIALITE(2800, 9.5F, 3.0F, 4, 26, "aerialite_ingot"),
    ORICHALCOS(2800, 9.5F, 3.0F, 4, 26, "orichalcos_ingot"),
    RHEIN(3200, 10.0F, 4.0F, 4, 30, "das_rheingold");

    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int level;
    private final int enchantmentValue;
    private final String repairItem;

    BotaniaTiers(int uses, float speed, float attackDamageBonus, int level, int enchantmentValue, String repairItem) {
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.level = level;
        this.enchantmentValue = enchantmentValue;
        this.repairItem = repairItem;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamageBonus;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @NotNull
    @Override
    public Ingredient getRepairIngredient() {
        var botania = BotaniaReplica.BOTANIA.getItem(repairItem);
        if (botania != null) {
            return Ingredient.of(botania);
        }
        var mythic = BotaniaReplica.MYTHICBOTANY.getItem(repairItem);
        if (mythic != null) {
            return Ingredient.of(mythic);
        }
        var extra = BotaniaReplica.EXTRABOTANY.getItem(repairItem);
        return extra != null ? Ingredient.of(extra) : Ingredient.EMPTY;
    }
}
