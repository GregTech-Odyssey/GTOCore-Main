package com.gtocore.integration.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IEntropinnyum {

    int RANGE = 12;
    int EXPLODE_EFFECT_EVENT = 0;
    int ANGRY_EFFECT_EVENT = 1;

    static boolean absorbBomb(Level level, BlockPos center, int amount) {
        // if (level.isClientSide) {
        // return false;
        // }
        // List<EntropinnyumBlockEntity> flowers = new ArrayList<>();
        // GTOBlockEntityIndexes.ENTROPINNYUMS.query(level, center, BlockRange.box(RANGE + 1), f ->
        // f.getBlockState().getBlock() == vazkii.botania.common.BotaniaBlocks.getBlock("entropinnyum") && f.getMana()
        // == 0 && inScanBox(center, f.getBlockPos()), flowers);
        // if (!flowers.isEmpty()) {
        // BlockOrder.sortBetweenClosed(flowers, EntropinnyumBlockEntity::getBlockPos);
        // flowers.get(0).fillExceedingMaxMana(amount);
        // level.playSound(null, center, BotaniaSounds.entropinnyumHappy, SoundSource.BLOCKS);
        // level.removeBlock(center, false);
        // return true;
        // }
        return false;
    }

    private static boolean inScanBox(BlockPos center, BlockPos pos) {
        int dx = pos.getX() - center.getX(), dy = pos.getY() - center.getY(), dz = pos.getZ() - center.getZ();
        return dx >= -RANGE && dx <= RANGE + 1 && dy >= -RANGE && dy <= RANGE + 1 && dz >= -RANGE && dz <= RANGE + 1;
    }
}
