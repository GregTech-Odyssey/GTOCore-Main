package com.gtocore.common.item;

import com.gtocore.common.block.SteamPipeBlock;

import com.gregtechceu.gtceu.api.item.PipeBlockItem;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SteamPipeBlockItem extends PipeBlockItem implements IItemRendererProvider {

    public SteamPipeBlockItem(SteamPipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public IRenderer getRenderer(ItemStack stack) {
        return getBlock().getRenderer(getBlock().defaultBlockState());
    }
}
