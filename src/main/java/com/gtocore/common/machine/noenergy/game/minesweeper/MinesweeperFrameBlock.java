package com.gtocore.common.machine.noenergy.game.minesweeper;

import net.minecraft.world.level.block.Block;

/**
 * 扫雷边框方块：铺盘时在棋盘外圈围一圈，纯装饰（照参考的 vertical_frame），没有方块实体、不掉落（棋盘由核心方块收走）。
 */
public class MinesweeperFrameBlock extends Block {

    public MinesweeperFrameBlock(Properties properties) {
        super(properties);
    }
}
