package vazkii.botania.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import vazkii.botania.common.MythicBotanyItems;
import vazkii.botania.common.MythicBotanyPlayerData;

/**
 * 装有智慧之泉水的海姆达尔号角：饮下后获得密米尔的知识，喝完退回空号角。
 */
public class FullGjallarHornItem extends Item {

    public FullGjallarHornItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (!(living instanceof Player player)) {
            return stack;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!level.isClientSide) {
            if (MythicBotanyPlayerData.grantMimirKnowledge(player)) {
                player.sendSystemMessage(Component.translatable("message.mythicbotany.mimir_knowledge").withStyle(ChatFormatting.GRAY));
            } else {
                player.sendSystemMessage(Component.translatable("message.mythicbotany.mimir_known").withStyle(ChatFormatting.GRAY));
            }
        }
        if (!player.isCreative()) {
            stack.shrink(1);
            ItemStack empty = MythicBotanyItems.MB_GJALLAR_HORN_EMPTY.asStack();
            if (stack.isEmpty()) {
                return empty;
            }
            player.getInventory().add(empty);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 48;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
}
