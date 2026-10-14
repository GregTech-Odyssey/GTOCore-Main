package com.gtocore.api.wireless.energy;

import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public sealed interface Provider {

    GlobalPos pos();

    UUID owner();

    record Tower(GlobalPos pos, UUID owner, List<Unit> units, int tier, long throughput) implements Provider {

        public int unitTier() {
            int top = -1;
            for (var unit : units) top = Math.max(top, unit.tier());
            return top >= 0 ? top : Math.max(0, tier - 1);
        }
    }

    record Unit(int tier, int count, BigInteger capacity, int loss) {

        public static final int FIXED = -1;
    }

    record Relay(GlobalPos pos, UUID owner, int tier, int amperage, ResourceKey<Level> target) implements Provider {

        public static final int AMPERAGE = 99999999;

        public boolean connected() {
            return !GridBody.same(target, pos.dimension());
        }
    }
}
