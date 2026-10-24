package com.gtocore.common.machine.noenergy.game.minesweeper;

import net.minecraft.world.level.block.Block;

/** 扫雷边框方块：铺盘时围在棋盘外圈，纯装饰；没有方块实体，也不掉落。 */
public class MinesweeperFrameBlock extends Block {

    public MinesweeperFrameBlock(Properties properties) {
        super(properties);
    }
}
