package vazkii.botania.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.List;

/** 植物魔法辞典：右键打开 Patchouli 的植物魔法手册。 */
public class LexicaBotaniaItem extends Item {

    /** 精灵版标记，与原模组同一键名。 */
    public static final String TAG_ELVEN_UNLOCK = "botania:elven_unlock";
    /** 手册本体。复刻内容与本体同在一个 mod，书籍只能挂在 gtocore 命名空间下。 */
    public static final ResourceLocation BOOK = new ResourceLocation("gtocore", "lexicon");

    public LexicaBotaniaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            PatchouliAPI.get().openBookGUI(serverPlayer, BOOK);
            serverPlayer.playSound(BotaniaSounds.lexiconOpen.get(), 1F, (float) (0.7 + Math.random() * 0.4));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        try {
            tooltip.add(PatchouliAPI.get().getSubtitle(BOOK).copy().withStyle(ChatFormatting.GRAY));
        } catch (IllegalArgumentException ignored) {
            // 手册内容尚未加载完成时取不到版本副标题
        }
    }

    public static boolean isElven(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(TAG_ELVEN_UNLOCK);
    }
}
