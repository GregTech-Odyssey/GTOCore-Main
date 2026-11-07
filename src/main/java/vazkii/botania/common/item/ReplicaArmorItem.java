package vazkii.botania.common.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * 复刻盔甲物品。
 *
 * <p>
 * 仅重写客户端接入点：按材质与装备槽返回原模组的盔甲模型，服务端行为与普通 {@link ArmorItem} 一致。
 * </p>
 */
public class ReplicaArmorItem extends ArmorItem {

    public ReplicaArmorItem(ArmorMaterial material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,
                                                                                     ItemStack stack, EquipmentSlot slot, net.minecraft.client.model.HumanoidModel<?> original) {
                var model = vazkii.botania.client.model.armor.ArmorModels.get(stack);
                return model == null ? original : model;
            }
        });
    }
}
