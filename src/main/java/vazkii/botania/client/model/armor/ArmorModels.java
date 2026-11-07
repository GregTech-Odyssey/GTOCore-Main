package vazkii.botania.client.model.armor;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.item.BotaniaArmorMaterials;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 已烘焙的盔甲模型，按材质名与装备槽索引。
 *
 * <p>
 * 原模组按物品类型区分四套盔甲；复刻盔甲共用同一个物品类，因此按材质名匹配。
 * </p>
 */
public final class ArmorModels {

    private static final Map<String, Map<EquipmentSlot, ArmorModel>> MODELS = new HashMap<>();

    private ArmorModels() {}

    public static void init(EntityRendererProvider.Context context) {
        MODELS.clear();
        put(context, BotaniaArmorMaterials.MANASTEEL.getName(), ArmorModelLayers.MANASTEEL_INNER, ArmorModelLayers.MANASTEEL_OUTER);
        put(context, BotaniaArmorMaterials.ELEMENTIUM.getName(), ArmorModelLayers.ELEMENTIUM_INNER, ArmorModelLayers.ELEMENTIUM_OUTER);
        put(context, BotaniaArmorMaterials.TERRASTEEL.getName(), ArmorModelLayers.TERRASTEEL_INNER, ArmorModelLayers.TERRASTEEL_OUTER);
        put(context, BotaniaArmorMaterials.MANAWEAVE.getName(), ArmorModelLayers.MANAWEAVE_INNER, ArmorModelLayers.MANAWEAVE_OUTER);
    }

    private static void put(EntityRendererProvider.Context context, String name, ModelLayerLocation inner,
                            ModelLayerLocation outer) {
        Map<EquipmentSlot, ArmorModel> bySlot = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ModelPart part = context.bakeLayer(slot == EquipmentSlot.LEGS ? inner : outer);
            bySlot.put(slot, new ArmorModel(part, slot));
        }
        MODELS.put(name, bySlot);
    }

    @Nullable
    public static ArmorModel get(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armor)) {
            return null;
        }
        Map<EquipmentSlot, ArmorModel> bySlot = MODELS.get(armor.getMaterial().getName());
        return bySlot == null ? null : bySlot.get(armor.getEquipmentSlot());
    }
}
