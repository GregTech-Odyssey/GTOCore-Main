package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 花药台方块实体。
 *
 * <p>
 * 用水桶填充后，投掷的花瓣进入内部容器；最后投掷试剂时匹配配方并产出成品，同时清空容器与水。
 * </p>
 */
public class PetalApothecaryBlockEntity extends BotaniaBlockEntity {

    public enum Fluid {
        EMPTY,
        WATER,
        LAVA
    }

    private static final String TAG_FLUID = "fluid";
    private static final int SIZE = 16;

    private final SimpleContainer inventory = new SimpleContainer(SIZE);
    private Fluid fluid = Fluid.EMPTY;

    public PetalApothecaryBlockEntity(BlockPos pos, BlockState state) {
        this(BotaniaBlockEntities.PETAL_APOTHECARY.get(), pos, state);
    }

    public PetalApothecaryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Container getInventory() {
        return inventory;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public void setFluid(Fluid fluid) {
        this.fluid = fluid;
        markDispatchable();
    }

    /** 物品实体落入时的处理。 */
    public boolean collideEntityItem(ItemEntity item) {
        if (level == null || level.isClientSide || !item.isAlive()) {
            return false;
        }
        ItemStack stack = item.getItem();
        if (fluid == Fluid.EMPTY) {
            if (stack.is(Items.WATER_BUCKET)) {
                setFluid(Fluid.WATER);
                item.setItem(new ItemStack(Items.BUCKET));
                return true;
            }
            if (stack.is(Items.LAVA_BUCKET)) {
                setFluid(Fluid.LAVA);
                item.setItem(new ItemStack(Items.BUCKET));
                return true;
            }
            return false;
        }
        if (fluid == Fluid.LAVA) {
            item.setSecondsOnFire(100);
            return true;
        }
        if (stack.isEmpty()) {
            return false;
        }
        int slot = -1;
        for (int i = 0; i < SIZE; i++) {
            if (inventory.getItem(i).isEmpty()) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            return false;
        }
        inventory.setItem(slot, stack.copy());
        item.setItem(ItemStack.EMPTY);
        markDispatchable();
        return true;
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
                                  PetalApothecaryBlockEntity self) {
        if (self.fluid == Fluid.EMPTY || !(level instanceof net.minecraft.server.level.ServerLevel server)) {
            return;
        }
        if (level.getGameTime() % 8 != 0) {
            return;
        }
        server.sendParticles(
                vazkii.botania.client.fx.SparkleParticleData.sparkle(1F, 0.45F, 0.75F, 1F, 3),
                pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 1, 0.15, 0.05, 0.15, 0.01);
    }

    @Override
    protected void readNBT(CompoundTag tag) {
        int ordinal = tag.getInt(TAG_FLUID);
        Fluid[] values = Fluid.values();
        fluid = values[Math.max(0, Math.min(values.length - 1, ordinal))];
        inventory.fromTag(tag.getList("Items", 10));
    }

    @Override
    protected void writeNBT(CompoundTag tag) {
        tag.putInt(TAG_FLUID, fluid.ordinal());
        tag.put("Items", inventory.createTag());
    }
}
