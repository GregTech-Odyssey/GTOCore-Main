package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.common.BotaniaBlocks;

/**
 * 符文祭坛方块实体。
 *
 * <p>
 * 投入材料后匹配符文配方并记录所需魔力，随后从相邻魔力池抽取魔力，
 * 达到需求后产出符文并清空祭坛。
 * </p>
 */
public class RunicAltarBlockEntity extends BotaniaBlockEntity implements ManaReceiver {

    private static final int SIZE = 16;
    private static final String TAG_MANA = "mana";
    private static final String TAG_MANA_TO_GET = "manaToGet";

    private final SimpleContainer inventory = new SimpleContainer(SIZE);
    private long mana;
    private int manaToGet;

    public RunicAltarBlockEntity(BlockPos pos, BlockState state) {
        this(BotaniaBlockEntities.RUNIC_ALTAR.get(), pos, state);
    }

    public RunicAltarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Container getInventory() {
        return inventory;
    }

    public int getManaToGet() {
        return manaToGet;
    }

    /** 玩家投入物品。 */
    public boolean addItem(Player player, ItemStack stack) {
        if (stack.isEmpty() || manaToGet != 0) {
            return false;
        }
        if (stack.is(BotaniaBlocks.LIVINGROCK.get().asItem())) {
            return true;
        }
        for (int i = 0; i < SIZE; i++) {
            if (inventory.getItem(i).isEmpty()) {
                inventory.setItem(i, stack.copyWithCount(1));
                if (player != null && !player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                markDispatchable();
                return true;
            }
        }
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RunicAltarBlockEntity self) {
        if (self.manaToGet <= 0 || !(level instanceof net.minecraft.server.level.ServerLevel server)) {
            return;
        }
        if (level.getGameTime() % 5 != 0) {
            return;
        }
        server.sendParticles(
                vazkii.botania.client.fx.SparkleParticleData.sparkle(1F, 0.6F, 0.9F, 0.4F, 4),
                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 1, 0.2, 0.1, 0.2, 0.02);
    }

    @Override
    public Level getManaReceiverLevel() {
        return level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return worldPosition;
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(Integer.MAX_VALUE, mana);
    }

    @Override
    public long getCurrentManaLong() {
        return mana;
    }

    @Override
    public boolean isFull() {
        return manaToGet <= 0 || mana >= manaToGet;
    }

    @Override
    public void receiveMana(int amount) {
        if (amount != 0) {
            mana = Math.max(0, mana + amount);
            markDispatchable();
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    @Override
    protected void readNBT(CompoundTag tag) {
        mana = tag.getLong(TAG_MANA);
        manaToGet = tag.getInt(TAG_MANA_TO_GET);
        inventory.fromTag(tag.getList("Items", 10));
    }

    @Override
    protected void writeNBT(CompoundTag tag) {
        tag.putLong(TAG_MANA, mana);
        tag.putInt(TAG_MANA_TO_GET, manaToGet);
        tag.put("Items", inventory.createTag());
    }
}
