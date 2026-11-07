package vazkii.botania.common.item.relic;

import com.gtocore.common.item.RewardBagItem;
import com.gtocore.utils.PlayerNameUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.handler.ExtraBotanySounds;

import java.util.List;
import java.util.UUID;

/**
 * 潘多拉魔盒：盖亚守护者 III 的遗物奖励，开启后获得各档奖励袋与英雄勋章。
 *
 * <p>
 * 与原额外植物学一致，魔盒在掉落时与击杀者灵魂绑定；其他玩家无法开启，持有者每 10 tick 受到伤害。
 * </p>
 */
public class PandorasBoxItem extends RewardBagItem {

    private static final String TAG_SOULBIND_UUID = "soulbindUUID";

    public PandorasBoxItem(Item.Properties properties, ResourceLocation lootTable) {
        super(properties, lootTable, 1, () -> ExtraBotanySounds.PANDORAS_BOX_OPEN.get());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        UUID soulbind = getSoulbindUUID(stack);
        if (soulbind == null) {
            if (!level.isClientSide) {
                bindToUUID(stack, player.getUUID());
            }
        } else if (!soulbind.equals(player.getUUID())) {
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        UUID soulbind = getSoulbindUUID(stack);
        if (soulbind == null) {
            bindToUUID(stack, player.getUUID());
        } else if (!soulbind.equals(player.getUUID()) && player.tickCount % 10 == 0) {
            player.hurt(player.damageSources().magic(), 2F);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        UUID soulbind = getSoulbindUUID(stack);
        if (soulbind == null) {
            tooltip.add(Component.translatable("botaniamisc.relicUnbound"));
        } else {
            tooltip.add(Component.translatable("botaniamisc.relicSoulbound", PlayerNameUtils.getLastKnownName(level, soulbind)));
        }
    }

    public static void bindToUUID(ItemStack stack, UUID uuid) {
        stack.getOrCreateTag().putString(TAG_SOULBIND_UUID, uuid.toString());
    }

    @Nullable
    public static UUID getSoulbindUUID(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_SOULBIND_UUID, net.minecraft.nbt.Tag.TAG_STRING)) {
            return null;
        }
        try {
            return UUID.fromString(stack.getTag().getString(TAG_SOULBIND_UUID));
        } catch (IllegalArgumentException exception) {
            stack.getTag().remove(TAG_SOULBIND_UUID);
            return null;
        }
    }
}
