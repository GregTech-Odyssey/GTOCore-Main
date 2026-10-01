package com.gtocore.common.machine.mana;

import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.data.GTODimensions;

import net.minecraft.world.level.Level;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.DataComponentKey;
import earth.terrarium.adastra.api.planets.PlanetApi;

public record CelestialHandler(long maxCapacity) {

    public static void init() {}

    public static final DataComponentKey<Integer> SOLARIS = GTORecipeDataKeys.register("solaris", DataSyncCodec.INT_CODEC);

    public static final DataComponentKey<Integer> LUNARA = GTORecipeDataKeys.register("lunara", DataSyncCodec.INT_CODEC);
    public static final DataComponentKey<Integer> VOIDFLUX = GTORecipeDataKeys.register("voidflux", DataSyncCodec.INT_CODEC);
    public static final DataComponentKey<Integer> STELLARM = GTORecipeDataKeys.register("stellarm", DataSyncCodec.INT_CODEC);
    public static final DataComponentKey<Integer> ANY = GTORecipeDataKeys.register("any", DataSyncCodec.INT_CODEC);

    public enum Mode {
        VOID,
        OTHERSIDE,
        SPACE,
        ALFHEIM,
        END,
        OVERWORLD
    }

    private long clampToMaxCapacity(long value) {
        return Math.min(maxCapacity, value);
    }

    public Mode initMode(Level world) {
        if (world == null) {
            return Mode.OVERWORLD;
        }
        var dim = world.dimension();

        if (PlanetApi.API.isSpace(world)) {
            return Mode.SPACE;
        } else if (GTODimensions.isVoid(dim)) {
            return Mode.VOID;
        }
        if (dim == GTODimensions.OTHERSIDE) return Mode.OTHERSIDE;
        if (dim == GTODimensions.ALFHEIM) return Mode.ALFHEIM;
        if (dim == GTODimensions.THE_END) return Mode.END;
        return Mode.OVERWORLD;
    }

    public Resource increase(Level world, int multiple, long solaris, long lunara, long voidflux, long stellarm, Mode mode) {
        if (world == null) return new Resource(solaris, lunara, voidflux, stellarm);

        switch (mode) {
            case SPACE -> stellarm = clampToMaxCapacity(stellarm + 40L * multiple);
            case VOID -> {
                solaris = clampToMaxCapacity(solaris + 5L * multiple);
                lunara = clampToMaxCapacity(lunara + 5L * multiple);
            }
            case ALFHEIM -> {
                if (world.isDay()) {
                    solaris = clampToMaxCapacity(solaris + 20L * multiple);
                } else if (world.isNight()) {
                    lunara = clampToMaxCapacity(lunara + 20L * multiple);
                }
            }
            case OTHERSIDE -> voidflux = clampToMaxCapacity(voidflux + 50L * multiple);
            case END -> voidflux = clampToMaxCapacity(voidflux + 10L * multiple);
            case OVERWORLD -> {
                if (world.isDay()) {
                    solaris = clampToMaxCapacity(solaris + 10L * multiple);
                } else if (world.isNight()) {
                    lunara = clampToMaxCapacity(lunara + 10L * multiple);
                }
            }
        }
        return new Resource(solaris, lunara, voidflux, stellarm);
    }

    public ResourceResult deductResource(DataComponentKey<Integer> type, int cost, long parallel, long solaris, long lunara, long voidflux, long stellarm) {
        long totalCost = (long) cost * parallel;
        if (totalCost <= 0) return new ResourceResult(true, solaris, lunara, voidflux, stellarm);
        if (type == SOLARIS) {
            if (solaris < totalCost) return new ResourceResult(false, solaris, lunara, voidflux, stellarm);
            solaris = Math.max(0L, solaris - totalCost);
        } else if (type == LUNARA) {
            if (lunara < totalCost) return new ResourceResult(false, solaris, lunara, voidflux, stellarm);
            lunara = Math.max(0L, lunara - totalCost);
        } else if (type == VOIDFLUX) {
            if (voidflux < totalCost) return new ResourceResult(false, solaris, lunara, voidflux, stellarm);
            voidflux = Math.max(0L, voidflux - totalCost);
        } else if (type == STELLARM) {
            if (stellarm < totalCost) return new ResourceResult(false, solaris, lunara, voidflux, stellarm);
            stellarm = Math.max(0L, stellarm - totalCost);
        } else if (type == ANY) {
            long remainingCost = totalCost;
            if (solaris > 0) {
                long deduct = Math.min(solaris, remainingCost);
                solaris = Math.max(0, solaris - deduct);
                remainingCost -= deduct;
            }
            if (remainingCost > 0 && lunara > 0) {
                long deduct = Math.min(lunara, remainingCost);
                lunara = Math.max(0, lunara - deduct);
                remainingCost -= deduct;
            }
            if (remainingCost > 0 && voidflux > 0) {
                long deduct = Math.min(voidflux, remainingCost);
                voidflux = Math.max(0, voidflux - deduct);
                remainingCost -= deduct;
            }
            if (remainingCost > 0 && stellarm > 0) {
                long deduct = Math.min(stellarm, remainingCost);
                stellarm = Math.max(0, stellarm - deduct);
                remainingCost -= deduct;
            }
            if (remainingCost > 0) return new ResourceResult(false, solaris, lunara, voidflux, stellarm);
        } else {
            return new ResourceResult(false, solaris, lunara, voidflux, stellarm);
        }
        return new ResourceResult(true, solaris, lunara, voidflux, stellarm);
    }

    public static long cost(int solarisCost, int lunaraCost, int voidfluxCost, int stellarmCost, int anyCost) {
        if (solarisCost > 0) return solarisCost;
        if (lunaraCost > 0) return lunaraCost;
        if (voidfluxCost > 0) return voidfluxCost;
        if (stellarmCost > 0) return stellarmCost;
        return anyCost;
    }

    public static long available(int solarisCost, int lunaraCost, int voidfluxCost, int stellarmCost, long solaris, long lunara, long voidflux, long stellarm) {
        if (solarisCost > 0) return solaris;
        if (lunaraCost > 0) return lunara;
        if (voidfluxCost > 0) return voidflux;
        if (stellarmCost > 0) return stellarm;
        return solaris + lunara + voidflux + stellarm;
    }

    public record ResourceResult(boolean success, long solaris, long lunara, long voidflux, long stellarm) {}

    public record Resource(long solaris, long lunara, long voidflux, long stellarm) {}
}
