/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.block_entity.mana.LargeManaPoolBlockEntity;
import vazkii.botania.common.block.mana.LargeManaPoolBlock;

import java.util.List;
import java.util.Locale;

public class LargeManaPoolItem extends BlockItem {

    public LargeManaPoolItem(Block block, Properties props) {
        super(block, props);
    }

    @NotNull
    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        if (!ctx.canPlace()) {
            return InteractionResult.FAIL;
        }
        Level level = ctx.getLevel();
        BlockPos center = ctx.getClickedPos();
        LargeManaPoolBlock block = (LargeManaPoolBlock) getBlock();
        BlockState base = block.defaultBlockState();
        CollisionContext collision = ctx.getPlayer() == null ? CollisionContext.empty() : CollisionContext.of(ctx.getPlayer());
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
            if (!level.isInWorldBounds(pos) || !level.getBlockState(pos).canBeReplaced(ctx) || !level.isUnobstructed(base, pos, collision)) {
                return InteractionResult.FAIL;
            }
        }
        if (!level.isClientSide) {
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
                BlockPos placed = pos.immutable();
                level.setBlock(placed, base.setValue(LargeManaPoolBlock.CORE, placed.equals(center)), Block.UPDATE_ALL);
                if (level.getBlockEntity(placed) instanceof LargeManaPoolBlockEntity part) {
                    part.setCoreOffset(center.getX() - placed.getX(), center.getZ() - placed.getZ());
                }
            }
            block.refreshConnections(level, center, 2);
            SoundType sound = base.getSoundType();
            level.playSound(null, center, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            level.gameEvent(GameEvent.BLOCK_PLACE, center, GameEvent.Context.of(ctx.getPlayer(), base));
        }
        Player player = ctx.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            ctx.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        var spec = ((LargeManaPoolBlock) getBlock()).poolSpec();
        tooltip.add(Component.translatable("botaniamisc.largePool0", spec.width(), spec.depth(), String.format(Locale.ROOT, "%,d", spec.capacity())).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("botaniamisc.largePool1").withStyle(ChatFormatting.GRAY));
    }
}
