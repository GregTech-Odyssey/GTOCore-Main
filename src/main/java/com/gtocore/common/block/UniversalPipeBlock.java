package com.gtocore.common.block;

import com.gtocore.common.blockentity.UniversalPipeBlockEntity;
import com.gtocore.common.data.GTOBlockEntities;
import com.gtocore.common.pipe.universal.LevelUniversalPipeNet;
import com.gtocore.common.pipe.universal.UniversalPipeProperties;
import com.gtocore.common.pipe.universal.UniversalPipeType;
import com.gtocore.common.pipe.universal.UniversalStorages;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.block.PipeBlockRenderer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class UniversalPipeBlock extends PipeBlock<UniversalPipeType, UniversalPipeProperties, LevelUniversalPipeNet> {

    public final PipeBlockRenderer renderer;
    @Getter
    public final PipeModel pipeModel;
    private final UniversalPipeType pipeType;
    private final UniversalPipeProperties properties;

    public UniversalPipeBlock(Properties properties, UniversalPipeType pipeType) {
        super(properties, pipeType);
        this.pipeType = pipeType;
        this.properties = UniversalPipeProperties.INSTANCE;
        // 大型管道口径：与 GT 大型管道同一套贴图与厚度
        this.pipeModel = new PipeModel(pipeType.getThickness(), () -> GTCEu.id("block/pipe/pipe_side"), () -> GTCEu.id("block/pipe/pipe_large_in"), null, null);
        this.renderer = new PipeBlockRenderer(this.pipeModel);
    }

    @Override
    public @NotNull LevelUniversalPipeNet getWorldPipeNet(ServerLevel level) {
        return LevelUniversalPipeNet.getOrCreate(level);
    }

    /** 与 GT 物品/流体管道同款：把本档的每秒速率写在工具提示里。 */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        var properties = pipeType.properties;
        tooltip.add(Component.translatable("gtocore.universal_pipe.item_transfer_rate", properties.itemThroughput()));
        tooltip.add(Component.translatable("gtocore.universal_pipe.fluid_transfer_rate", pipeType.bucketsPerSecond()));
        tooltip.add(Component.translatable("gtocore.universal_pipe.pull_mode").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public @NotNull BlockEntityType<? extends PipeBlockEntity<UniversalPipeType, UniversalPipeProperties>> getBlockEntityType() {
        return GTOBlockEntities.UNIVERSAL_PIPE.get();
    }

    @Override
    public @NotNull UniversalPipeProperties createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return this.properties;
    }

    @Override
    public @NotNull UniversalPipeProperties createProperties(PipeBlockEntity<UniversalPipeType, UniversalPipeProperties> pipeTile) {
        return this.pipeType.modifyProperties(properties);
    }

    @Override
    public @NotNull UniversalPipeProperties getFallbackType() {
        return this.properties;
    }

    @Override
    @Nullable
    public PipeBlockRenderer getRenderer(BlockState state) {
        return renderer;
    }

    @Override
    public boolean canPipesConnect(PipeBlockEntity<UniversalPipeType, UniversalPipeProperties> selfTile, Direction side,
                                   PipeBlockEntity<UniversalPipeType, UniversalPipeProperties> sideTile) {
        return selfTile instanceof UniversalPipeBlockEntity && sideTile instanceof UniversalPipeBlockEntity;
    }

    /** 只连具体存储：整网聚合的 NetworkStorage 不算接口，所以对着 AE 线缆不会接上。 */
    @Override
    public boolean canPipeConnectToBlock(PipeBlockEntity<UniversalPipeType, UniversalPipeProperties> selfTile, Direction side, @Nullable BlockEntity tile) {
        return UniversalStorages.getStorage(tile, side.getOpposite()) != null;
    }
}
