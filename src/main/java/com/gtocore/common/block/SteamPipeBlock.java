package com.gtocore.common.block;

import com.gtocore.common.blockentity.SteamPipeBlockEntity;
import com.gtocore.common.data.GTOBlockEntities;
import com.gtocore.common.pipe.steam.LevelSteamPipeNet;
import com.gtocore.common.pipe.steam.SteamPipeProperties;
import com.gtocore.common.pipe.steam.SteamPipeType;
import com.gtocore.common.pipe.steam.SteamStorages;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SteamPipeBlock extends PipeBlock<SteamPipeType, SteamPipeProperties, LevelSteamPipeNet> {

    public final PipeBlockRenderer renderer;
    @Getter
    public final PipeModel pipeModel;
    private final SteamPipeType pipeType;

    public SteamPipeBlock(Properties properties, SteamPipeType pipeType) {
        super(properties, pipeType);
        this.pipeType = pipeType;
        this.pipeModel = new PipeModel(pipeType.getThickness(), () -> GTOCore.id("block/pipe/steam_pipe_side"), () -> GTOCore.id("block/pipe/steam_pipe_in"), null, null);
        this.renderer = new PipeBlockRenderer(this.pipeModel);
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor() {
        return (blockState, level, blockPos, index) -> {
            if (blockPos != null && level != null && level.getBlockEntity(blockPos) instanceof PipeBlockEntity<?, ?> pipe) {
                if (!pipe.getFrameMaterial().isNull()) {
                    if (index == 3) return pipe.getFrameMaterial().getMaterialRGB();
                    if (index == 4) return pipe.getFrameMaterial().getMaterialSecondaryRGB();
                }
                if (pipe.isPainted()) return pipe.getRealColor();
            }
            return 0xFFFFFF;
        };
    }

    @Override
    public @NotNull LevelSteamPipeNet getWorldPipeNet(ServerLevel level) {
        return LevelSteamPipeNet.getOrCreate(level);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Tips.feature(Component.translatable("gtocore.steam_pipe.only_steam")));
        tooltip.add(Tips.bonus(Component.translatable("gtocore.steam_pipe.unlimited")));
        tooltip.add(Tips.feature(Component.translatable("gtocore.universal_pipe.pull_mode")));
    }

    @Override
    public @NotNull BlockEntityType<? extends PipeBlockEntity<SteamPipeType, SteamPipeProperties>> getBlockEntityType() {
        return GTOBlockEntities.STEAM_PIPE.get();
    }

    @Override
    public @NotNull SteamPipeProperties createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return SteamPipeProperties.INSTANCE;
    }

    @Override
    public @NotNull SteamPipeProperties createProperties(PipeBlockEntity<SteamPipeType, SteamPipeProperties> pipeTile) {
        return this.pipeType.modifyProperties(SteamPipeProperties.INSTANCE);
    }

    @Override
    public @NotNull SteamPipeProperties getFallbackType() {
        return SteamPipeProperties.INSTANCE;
    }

    @Override
    @Nullable
    public PipeBlockRenderer getRenderer(BlockState state) {
        return renderer;
    }

    @Override
    public boolean canPipesConnect(PipeBlockEntity<SteamPipeType, SteamPipeProperties> selfTile, Direction side, PipeBlockEntity<SteamPipeType, SteamPipeProperties> sideTile) {
        return selfTile instanceof SteamPipeBlockEntity && sideTile instanceof SteamPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(PipeBlockEntity<SteamPipeType, SteamPipeProperties> selfTile, Direction side, @Nullable BlockEntity tile) {
        return SteamStorages.hasStorage(tile, side.getOpposite());
    }
}
