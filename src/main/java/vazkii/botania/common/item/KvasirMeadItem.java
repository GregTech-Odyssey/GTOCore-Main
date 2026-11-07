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

import vazkii.botania.common.BotaniaItems;
import vazkii.botania.common.MythicBotanyPlayerData;

/** 诗之蜜酒：饮下后获得诗之知识，是进入亚尔夫海姆的条件之一。 */
public class KvasirMeadItem extends Item {

    public KvasirMeadItem(Properties properties) {
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
            if (MythicBotanyPlayerData.grantKvasirKnowledge(player)) {
                player.sendSystemMessage(Component.translatable("message.mythicbotany.kvasir_knowledge").withStyle(ChatFormatting.GRAY));
            } else {
                player.sendSystemMessage(Component.translatable("message.mythicbotany.kvasir_known").withStyle(ChatFormatting.GRAY));
            }
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                return new ItemStack(BotaniaItems.VIAL.get());
            }
            player.getInventory().add(new ItemStack(BotaniaItems.VIAL.get()));
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
