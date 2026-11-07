package vazkii.botania.common.integration.corporea;

import net.minecraft.world.level.Level;

import vazkii.botania.api.corporea.CorporeaNode;
import vazkii.botania.api.corporea.CorporeaSpark;
import vazkii.botania.common.impl.corporea.DummyCorporeaNode;

/** 节点探测：按需求不实现 Corporea 机制，统一返回占位节点。 */
public final class CorporeaNodeDetectors {

    private CorporeaNodeDetectors() {}

    public static CorporeaNode findNode(Level level, CorporeaSpark spark) {
        return new DummyCorporeaNode(level, spark.getAttachPos(), spark);
    }
}
