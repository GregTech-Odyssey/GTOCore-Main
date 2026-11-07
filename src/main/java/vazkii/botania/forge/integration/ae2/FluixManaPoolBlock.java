package vazkii.botania.forge.integration.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.mana.ManaPoolBlock;

import java.util.Collections;
import java.util.List;

/** 福鲁伊克斯魔力池方块：挂在 ME 网络上时池内魔力即网络魔力。 */
public class FluixManaPoolBlock extends ManaPoolBlock {

    public FluixManaPoolBlock(Properties properties) {
        super(Variant.DEFAULT, properties);
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FluixManaPoolBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != BotaniaBlockEntities.FLUIX_MANA_POOL.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (BlockEntityTicker<T>) (l, p, s, be) -> ManaPoolBlockEntity.clientTick(l, p, s,
                    (ManaPoolBlockEntity) be);
        }
        return (BlockEntityTicker<T>) (l, p, s, be) -> FluixManaPoolBlockEntity.serverTick(l, p, s,
                (FluixManaPoolBlockEntity) be);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof FluixManaPoolBlockEntity pool) {
            pool.setOwner(player);
        }
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FluixManaPoolBlockEntity pool ? (int) pool.getComparatorSignal() : 0;
    }

    @NotNull
    @Override
    public List<ItemStack> getDrops(@NotNull BlockState state, LootParams.Builder params) {
        return Collections.singletonList(new ItemStack(this));
    }
}
