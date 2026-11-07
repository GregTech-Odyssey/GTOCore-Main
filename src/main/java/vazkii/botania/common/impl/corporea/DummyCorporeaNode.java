package vazkii.botania.common.impl.corporea;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.corporea.CorporeaNode;
import vazkii.botania.api.corporea.CorporeaRequest;
import vazkii.botania.api.corporea.CorporeaSpark;

import java.util.Collections;
import java.util.List;

/** 占位节点：没有可连接容器的火花使用它。 */
public class DummyCorporeaNode implements CorporeaNode {

    private final Level world;
    private final BlockPos pos;
    private final CorporeaSpark spark;

    public DummyCorporeaNode(Level world, BlockPos pos, CorporeaSpark spark) {
        this.world = world;
        this.pos = pos;
        this.spark = spark;
    }

    @Override
    public Level getWorld() {
        return world;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public CorporeaSpark getSpark() {
        return spark;
    }

    @Override
    public List<ItemStack> countItems(CorporeaRequest request) {
        return Collections.emptyList();
    }

    @Override
    public List<ItemStack> extractItems(CorporeaRequest request) {
        return Collections.emptyList();
    }
}
