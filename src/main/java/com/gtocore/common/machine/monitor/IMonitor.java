package com.gtocore.common.machine.monitor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public interface IMonitor {

    BlockPos getPos();

    Level getLevel();

    Direction getFrontFacing();
}
