package com.gtocore.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.hollingsworth.arsnouveau.common.block.SourceJar;
import com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile;

import static com.gtocore.common.data.GTOBlockEntities.SOURCE_JAR_BE_BLOCK_ENTITY_ENTRY;

public class SourceJarBE extends SourceJarTile {

    public SourceJarBE(BlockPos pos, BlockState state) {
        super(SOURCE_JAR_BE_BLOCK_ENTITY_ENTRY.get(), pos, state);
    }

    public SourceJarBE(BlockEntityType<SourceJarBE> blockEntityBlockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityBlockEntityType, blockPos, blockState);
    }

    @Override
    public int getMaxSource() {
        return (int) 1e7;
    }

    @Override
    public boolean updateBlock() {
        BlockState state = level.getBlockState(worldPosition);
        int fillState = 0;
        if (this.getSource() > 0 && this.getSource() < 1e6)
            fillState = 1;
        else if (this.getSource() != 0) {
            fillState = (int) ((this.getSource() / 1e6) + 1);
        }
        if (state.hasProperty(SourceJar.fill))
            level.setBlock(worldPosition, state.setValue(SourceJar.fill, fillState), 3);
        return true;
    }
}
