package com.gtocore.common.data;

import com.gtolib.api.ae2.infinite.InfiniteResourceRegistry;
import com.gtolib.utils.TagUtils;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public final class GTOInfiniteResources {

    private GTOInfiniteResources() {}

    public static void init() {
        InfiniteResourceRegistry.registerFluid(Fluids.WATER);
        InfiniteResourceRegistry.registerItem(Blocks.COBBLESTONE);
        InfiniteResourceRegistry.registerItem(Blocks.STONE);
        InfiniteResourceRegistry.registerItemTag(TagUtils.createForgeItemTag("concrete"));
    }
}
