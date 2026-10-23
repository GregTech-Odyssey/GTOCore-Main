package com.gtocore.common.block;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.integration.botania.IEntropinnyum;

import com.gtolib.utils.explosion.LargeExplosion;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public enum ChargeBomb {

    NAQUADRIA(GTOBlocks.NAQUADRIA_CHARGE, GTItems.QUANTUM_STAR, 200, 1_000_000),
    LEPTONIC(GTOBlocks.LEPTONIC_CHARGE, GTItems.GRAVI_STAR, 800, 4_000_000),
    QUANTUM_CHROMODYNAMIC(GTOBlocks.QUANTUM_CHROMODYNAMIC_CHARGE, GTOItems.UNSTABLE_STAR, 2000, 10_000_000);

    private static final ChargeBomb[] VALUES = values();

    private final Supplier<? extends Block> block;
    private final Supplier<? extends Item> activator;
    public final int radius;
    public final int absorbedMana;

    ChargeBomb(Supplier<? extends Block> block, Supplier<? extends Item> activator, int radius, int absorbedMana) {
        this.block = block;
        this.activator = activator;
        this.radius = radius;
        this.absorbedMana = absorbedMana;
    }

    public Block block() {
        return block.get();
    }

    public Item activator() {
        return activator.get();
    }

    public static boolean tryDetonate(Level level, BlockPos pos, Item item) {
        for (ChargeBomb bomb : VALUES) {
            if (item != bomb.activator() || level.getBlockState(pos).getBlock() != bomb.block()) continue;
            if (!IEntropinnyum.absorbBomb(level, pos, bomb.absorbedMana)) {
                LargeExplosion.sphere(level, pos, bomb.radius).breakBedrock(true).start();
            }
            return true;
        }
        return false;
    }
}
