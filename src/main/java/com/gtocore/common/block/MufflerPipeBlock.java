package com.gtocore.common.block;

import com.gtocore.common.blockentity.MufflerPipeBlockEntity;
import com.gtocore.common.data.GTOBlockEntities;
import com.gtocore.common.pipe.muffler.IMufflerConduction;
import com.gtocore.common.pipe.muffler.LevelMufflerPipeNet;
import com.gtocore.common.pipe.muffler.MufflerPipeProperties;
import com.gtocore.common.pipe.muffler.MufflerPipeType;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MufflerPipeBlock extends PipeBlock<MufflerPipeType, MufflerPipeProperties, LevelMufflerPipeNet> {

    public final PipeBlockRenderer renderer;
    @Getter
    public final PipeModel pipeModel;
    private final MufflerPipeType pipeType;
    private final MufflerPipeProperties properties;

    public MufflerPipeBlock(Properties properties, MufflerPipeType pipeType) {
        super(properties, pipeType);
        this.pipeType = pipeType;
        this.properties = MufflerPipeProperties.INSTANCE;
        this.pipeModel = new PipeModel(
                pipeType.getThickness(),
                () -> GTCEu.id("block/pipe/pipe_side"),
                () -> GTCEu.id("block/pipe/pipe_normal_in"),
                null,
                null);
        this.renderer = new PipeBlockRenderer(this.pipeModel);
    }

    @Override
    public @NotNull LevelMufflerPipeNet getWorldPipeNet(ServerLevel level) {
        return LevelMufflerPipeNet.getOrCreate(level);
    }

    @Override
    public @NotNull BlockEntityType<? extends PipeBlockEntity<MufflerPipeType, MufflerPipeProperties>> getBlockEntityType() {
        return GTOBlockEntities.MUFFLER_PIPE.get();
    }

    @Override
    public @NotNull MufflerPipeProperties createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return MufflerPipeProperties.INSTANCE;
    }

    @Override
    public @NotNull MufflerPipeProperties createProperties(PipeBlockEntity<MufflerPipeType, MufflerPipeProperties> pipeTile) {
        return this.pipeType.modifyProperties(properties);
    }

    @Override
    public @NotNull MufflerPipeProperties getFallbackType() {
        return MufflerPipeProperties.INSTANCE;
    }

    @Override
    @Nullable
    public PipeBlockRenderer getRenderer(BlockState state) {
        return renderer;
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor(Material material) {
        return (blockState, level, blockPos, index) -> {
            if (blockPos != null && level != null && level.getBlockEntity(blockPos) instanceof PipeBlockEntity<?, ?> pipe) {
                if (!pipe.getFrameMaterial().isNull()) {
                    if (index == 3) {
                        return pipe.getFrameMaterial().getMaterialRGB();
                    } else if (index == 4) {
                        return pipe.getFrameMaterial().getMaterialSecondaryRGB();
                    }
                }
                if (pipe.isPainted()) {
                    return pipe.getRealColor();
                }
            }
            return material.getMaterialRGB();
        };
    }

    @Override
    public boolean canPipesConnect(PipeBlockEntity<MufflerPipeType, MufflerPipeProperties> selfTile, Direction side,
                                   PipeBlockEntity<MufflerPipeType, MufflerPipeProperties> sideTile) {
        return selfTile instanceof MufflerPipeBlockEntity && sideTile instanceof MufflerPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(PipeBlockEntity<MufflerPipeType, MufflerPipeProperties> selfTile,
                                         Direction side, @Nullable BlockEntity tile) {
        return GTCapabilityHelper.getBlockEntityGTCapability(IMufflerConduction.class, tile, side.getOpposite()) != null;
    }
}
