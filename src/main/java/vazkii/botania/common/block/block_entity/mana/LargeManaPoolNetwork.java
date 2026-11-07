package vazkii.botania.common.block.block_entity.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * 相邻大型魔力池组成的并池网络。
 *
 * <p>
 * 按单元记录的核心位置收集同一 3×3 的单元，容量与储量按单元汇总；
 * 注入与抽取先在单元自己身上进行，不够时再在单元之间搬运。
 * 单个单元容量为整组规格除以单元数，合并后总容量与原模组一致。
 * </p>
 */
public final class LargeManaPoolNetwork {

    private LargeManaPoolNetwork() {}

    /** 收集与 {@code start} 属于同一 3×3 的所有大型池单元。 */
    public static List<LargeManaPoolBlockEntity> collect(Level level, BlockPos start) {
        if (!(level.getBlockEntity(start) instanceof LargeManaPoolBlockEntity unit)) {
            return List.of();
        }
        BlockPos core = unit.getCorePos();
        List<LargeManaPoolBlockEntity> units = new ArrayList<>(9);
        for (BlockPos pos : BlockPos.betweenClosed(core.offset(-1, 0, -1), core.offset(1, 0, 1))) {
            if (level.getBlockEntity(pos) instanceof LargeManaPoolBlockEntity part && core.equals(part.getCorePos())) {
                units.add(part);
            }
        }
        if (units.isEmpty()) {
            units.add(unit);
        }
        return units;
    }

    /** 单元数量。 */
    public static int units(List<LargeManaPoolBlockEntity> units) {
        return units.size();
    }

    /** 网络总容量。 */
    public static long capacity(List<LargeManaPoolBlockEntity> units) {
        long total = 0;
        for (int i = 0; i < units.size(); i++) {
            total += units.get(i).unitCapacity();
        }
        return total;
    }

    /** 网络已存魔力。 */
    public static long stored(List<LargeManaPoolBlockEntity> units) {
        long total = 0;
        for (int i = 0; i < units.size(); i++) {
            total += units.get(i).getCurrentManaLong();
        }
        return total;
    }

    /** 网络剩余空间。 */
    public static long room(List<LargeManaPoolBlockEntity> units) {
        long total = 0;
        for (int i = 0; i < units.size(); i++) {
            LargeManaPoolBlockEntity unit = units.get(i);
            total += Math.max(0, unit.unitCapacity() - unit.getCurrentManaLong());
        }
        return total;
    }
}
