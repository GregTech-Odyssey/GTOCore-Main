package com.gtocore.integration.botania;

import com.gregtechceu.gtceu.api.pattern.MultiblockWorldData;

import net.minecraft.server.level.ServerLevel;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

public final class BotaniaFlowerTicking {

    private BotaniaFlowerTicking() {}

    public static void init() {
        SpecialFlowerBlockEntity.registerSlowTickCondition(BotaniaFlowerTicking::isInMultiblock);
    }

    private static boolean isInMultiblock(SpecialFlowerBlockEntity flower) {
        if (!(flower.getLevel() instanceof ServerLevel serverLevel)) return false;
        var cache = MultiblockWorldData.get(serverLevel);
        if (cache == null) return false;
        var pos = flower.getBlockPos();
        var states = cache.getControllersInChunk(serverLevel.getChunkAt(pos).getPos().toLong());
        if (states == null) return false;
        long p = pos.asLong();
        for (var structure : states) {
            if (structure.blockEntityCache.contains(p)) return true;
        }
        return false;
    }
}
