package com.gtocore.mixin.gtm;

import com.gtocore.common.cover.RainFilmCover;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import com.suried.gtoutils.level.LoadedChunks;
import com.suried.gtoutils.schedule.Schedulers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IExplosionMachine.class, remap = false)
public interface IExplosionMachineMixin {

    /**
     * @author GTO
     * @reason Preserve terrain hazards while protecting covered machines from rain.
     */
    @Overwrite
    default void checkWeatherOrTerrainExplosion(ServerLevel level, float explosionPower, double additionalFireChance) {
        var machine = (IExplosionMachine) this;
        if (!machine.shouldWeatherOrTerrainExplosion()) return;
        try {
            var pos = machine.self().getPos();
            if (GTValues.RNG.nextInt(10) == 0) {
                for (Direction side : GTUtil.DIRECTIONS) {
                    var fluidState = LoadedChunks.blockState(level, pos.relative(side)).getFluidState();
                    if (!fluidState.isEmpty()) {
                        machine.executeExplosion(level, explosionPower);
                        return;
                    }
                }
            }
            if (GTValues.RNG.nextInt(10) == 0 &&
                    !(machine.self().getCoverContainer().getCoverAtSide(Direction.UP) instanceof RainFilmCover)) {
                if (level.isRainingAt(pos) || level.isRainingAt(pos.east()) || level.isRainingAt(pos.west()) ||
                        level.isRainingAt(pos.north()) || level.isRainingAt(pos.south())) {
                    if (level.isThundering() && GTValues.RNG.nextInt(3) == 0) {
                        machine.executeExplosion(level, explosionPower);
                    } else if (GTValues.RNG.nextInt(10) == 0) {
                        machine.executeExplosion(level, explosionPower);
                    } else Schedulers.of(level).schedule(() -> machine.setOnFire(additionalFireChance));
                }
            }
        } catch (Exception ignored) {}
    }
}
