package vazkii.botania.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.BotaniaReplica;

/**
 * 神秘花瓣。
 *
 * <p>
 * 点在泥土标签方块上时，在点击面的相邻位置铺一层同色的埋藏花瓣；相邻面同样可放。
 * </p>
 */
public class MysticalPetalItem extends Item {

    public final DyeColor color;

    public MysticalPetalItem(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        if (!level.getBlockState(pos).canBeReplaced()) {
            return InteractionResult.PASS;
        }
        if (!level.getBlockState(pos.below()).is(BlockTags.DIRT)) {
            return InteractionResult.PASS;
        }
        if (ctx.getPlayer() != null && !ctx.getPlayer().mayUseItemAt(pos, ctx.getClickedFace(), ctx.getItemInHand())) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Block petals = BotaniaReplica.BOTANIA.getBlock(color.getName() + "_buried_petals");
        if (petals == null) {
            return InteractionResult.PASS;
        }
        BlockState state = petals.defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        SoundType sound = state.getSoundType();
        level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1F) / 2F,
                sound.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(ctx.getPlayer(), state));
        if (ctx.getPlayer() == null || !ctx.getPlayer().getAbilities().instabuild) {
            ctx.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
