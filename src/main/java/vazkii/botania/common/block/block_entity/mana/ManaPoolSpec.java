package vazkii.botania.common.block.block_entity.mana;

import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaReceiver;

/**
 * 单个魔力池单元的参数。
 *
 * <p>
 * 合并后的大型池按单元数累加容量，但每单元的物品注入速率与火花倍率不随合并放大。
 * </p>
 */
public final class ManaPoolSpec {

    public static final int DEFAULT_ITEM_TRANSFER_RATE = 1000;

    public static final ManaPoolSpec POOL = builder().capacity(ManaPoolBlockEntity.MAX_MANA).build();
    public static final ManaPoolSpec DILUTED = builder().capacity(10_000).build();
    public static final ManaPoolSpec LARGE = builder().footprint(3, 3).capacity(36_000_000L).build();

    private final int width;
    private final int depth;
    private final long capacity;
    private final int sparkFactor;
    private final int itemTransferRate;
    private final int infusionRate;

    private ManaPoolSpec(int width, int depth, long capacity, int sparkFactor, int itemTransferRate, int infusionRate) {
        this.width = width;
        this.depth = depth;
        this.capacity = capacity;
        this.sparkFactor = sparkFactor;
        this.itemTransferRate = itemTransferRate;
        this.infusionRate = infusionRate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder().footprint(width, depth).capacity(capacity).sparkFactor(sparkFactor)
                .itemTransferRate(itemTransferRate).infusionRate(infusionRate);
    }

    public int width() {
        return width;
    }

    public int depth() {
        return depth;
    }

    public int cells() {
        return width * depth;
    }

    public long capacity() {
        return capacity;
    }

    public int sparkFactor() {
        return sparkFactor > 0 ? sparkFactor : cells();
    }

    public int itemTransferRate() {
        return itemTransferRate > 0 ? itemTransferRate : DEFAULT_ITEM_TRANSFER_RATE * cells();
    }

    public int infusionRate() {
        return infusionRate > 0 ? infusionRate : cells();
    }

    public static ManaPoolSpec of(Block block, ManaPoolSpec fallback) {
        return block instanceof Holder holder ? holder.poolSpec() : fallback;
    }

    public static int sparkFactorOf(@Nullable ManaReceiver receiver) {
        return receiver instanceof Holder holder ? holder.poolSpec().sparkFactor() : 1;
    }

    public interface Holder {

        ManaPoolSpec poolSpec();
    }

    public static final class Builder {

        private int width = 1;
        private int depth = 1;
        private long capacity;
        private int sparkFactor;
        private int itemTransferRate;
        private int infusionRate;

        private Builder() {}

        public Builder footprint(int width, int depth) {
            this.width = width;
            this.depth = depth;
            return this;
        }

        public Builder capacity(long capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder sparkFactor(int sparkFactor) {
            this.sparkFactor = sparkFactor;
            return this;
        }

        public Builder itemTransferRate(int itemTransferRate) {
            this.itemTransferRate = itemTransferRate;
            return this;
        }

        public Builder infusionRate(int infusionRate) {
            this.infusionRate = infusionRate;
            return this;
        }

        public ManaPoolSpec build() {
            if (width <= 0 || depth <= 0 || capacity <= 0 || itemTransferRate < 0 || sparkFactor < 0 || infusionRate < 0) {
                throw new IllegalArgumentException("invalid mana pool spec");
            }
            return new ManaPoolSpec(width, depth, capacity, sparkFactor, itemTransferRate, infusionRate);
        }
    }
}
