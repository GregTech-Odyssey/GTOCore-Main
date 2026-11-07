package vazkii.botania.common.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.BotaniaReplica;

/**
 * 复刻盔甲的材质定义。
 *
 * <p>
 * 防御点与韧性按原模组量级给出：植物钢接近铁、元素与泰拉钢接近钻石、精灵钢更高，
 * 织物与额外植物学各套装为轻甲。
 * </p>
 */
public final class BotaniaArmorMaterials {

    public static final ArmorMaterial MANASTEEL = create("manasteel", 15, 2, 6, 5, 2, 0F, 0F, 18, "manasteel_ingot",
            SoundEvents.ARMOR_EQUIP_IRON);
    public static final ArmorMaterial ELEMENTIUM = create("elementium", 20, 3, 8, 6, 3, 1F, 0F, 20, "elementium_ingot",
            SoundEvents.ARMOR_EQUIP_DIAMOND);
    public static final ArmorMaterial TERRASTEEL = create("terrasteel", 30, 3, 9, 7, 3, 2F, 0.1F, 25, "terrasteel_ingot",
            SoundEvents.ARMOR_EQUIP_NETHERITE);
    public static final ArmorMaterial MANAWEAVE = create("manaweave", 8, 1, 3, 2, 1, 0F, 0F, 15, "manaweave_cloth",
            SoundEvents.ARMOR_EQUIP_LEATHER);
    public static final ArmorMaterial ALFSTEEL = create("alfsteel", 33, 3, 9, 7, 3, 3F, 0.1F, 30, "alfsteel_ingot",
            SoundEvents.ARMOR_EQUIP_NETHERITE);
    public static final ArmorMaterial STARRY_IDOL = create("starry_idol", 20, 2, 6, 5, 2, 1F, 0F, 20, "photonium_ingot",
            SoundEvents.ARMOR_EQUIP_IRON);
    public static final ArmorMaterial PLEIADES = create("pleiades_combat_maid", 20, 2, 6, 5, 2, 1F, 0F, 20, "shadowium_ingot",
            SoundEvents.ARMOR_EQUIP_IRON);
    public static final ArmorMaterial GOBLIN_SLAYER = create("goblin_slayer", 20, 2, 6, 5, 2, 1F, 0F, 20,
            "orichalcos_ingot", SoundEvents.ARMOR_EQUIP_IRON);
    public static final ArmorMaterial SHADOW_WARRIOR = create("shadow_warrior", 20, 2, 6, 5, 2, 1F, 0F, 20,
            "shadowium_ingot", SoundEvents.ARMOR_EQUIP_IRON);

    private BotaniaArmorMaterials() {}

    private static ArmorMaterial create(String name, int baseDurability, int helmet, int chestplate, int leggings,
                                        int boots, float toughness, float knockbackResistance, int enchantmentValue, String repairItem,
                                        SoundEvent equipSound) {
        return new ArmorMaterial() {

            @Override
            public int getDurabilityForType(@NotNull ArmorItem.Type type) {
                int multiplier = switch (type) {
                    case HELMET -> 11;
                    case CHESTPLATE -> 16;
                    case LEGGINGS -> 15;
                    case BOOTS -> 13;
                };
                return baseDurability * multiplier;
            }

            @Override
            public int getDefenseForType(@NotNull ArmorItem.Type type) {
                return switch (type) {
                    case HELMET -> helmet;
                    case CHESTPLATE -> chestplate;
                    case LEGGINGS -> leggings;
                    case BOOTS -> boots;
                };
            }

            @Override
            public int getEnchantmentValue() {
                return enchantmentValue;
            }

            @NotNull
            @Override
            public SoundEvent getEquipSound() {
                return equipSound;
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

            @NotNull
            @Override
            public String getName() {
                return name;
            }

            @Override
            public float getToughness() {
                return toughness;
            }

            @Override
            public float getKnockbackResistance() {
                return knockbackResistance;
            }
        };
    }
}
