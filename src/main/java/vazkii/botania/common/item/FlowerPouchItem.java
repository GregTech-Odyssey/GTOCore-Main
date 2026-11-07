package vazkii.botania.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.menu.FlowerPouchContainer;

/**
 * 采花袋。
 *
 * <p>
 * 容量 32 格，按颜色分别存放 16 种神秘花与 16 种高神秘花；捡起地上的花时会自动收进袋里，
 * 右键打开界面，潜行右键可以把袋里的花放进容器。
 * </p>
 */
public class FlowerPouchItem extends Item {

    public static final int DYE_COUNT = 16;
    public static final int SIZE = 2 * DYE_COUNT;

    private static final String TAG_ITEMS = "Items";

    public FlowerPouchItem(Properties properties) {
        super(properties);
    }

    /** 袋子第 {@code slot} 格对应的花：前 16 格是神秘花，后 16 格是高神秘花。 */
    public static Item getFlowerForSlot(int slot) {
        DyeColor color = DyeColor.byId(Math.floorMod(slot, DYE_COUNT));
        String suffix = slot < DYE_COUNT ? "_mystical_flower" : "_double_flower";
        return BuiltInRegistries.ITEM.get(new ResourceLocation("botania", color.getName() + suffix));
    }

    /** 神秘花或高神秘花在袋内的槽位，其他物品返回 -1。 */
    public static int slotFor(ItemStack stack) {
        Block block = Block.byItem(stack.getItem());
        if (block == Blocks.AIR) {
            return -1;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null || !"botania".equals(id.getNamespace())) {
            return -1;
        }
        String path = id.getPath();
        if (path.endsWith("_mystical_flower")) {
            DyeColor color = DyeColor.byName(path.substring(0, path.length() - "_mystical_flower".length()), null);
            return color == null ? -1 : color.getId();
        }
        if (path.endsWith("_double_flower")) {
            DyeColor color = DyeColor.byName(path.substring(0, path.length() - "_double_flower".length()), null);
            return color == null ? -1 : DYE_COUNT + color.getId();
        }
        return -1;
    }

    /** 袋子物品的库存，内容保存在物品 NBT 里。 */
    public static SimpleContainer getInventory(ItemStack bag) {
        return new PouchInventory(bag);
    }

    /** 捡起地上的花时自动收进背包里的采花袋。 */
    public static boolean onPickupItem(ItemEntity entity, Player player) {
        ItemStack entityStack = entity.getItem();
        int slot = slotFor(entityStack);
        if (slot < 0 || entityStack.isEmpty() || entity.level().isClientSide) {
            return false;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (i == player.getInventory().selected) {
                continue;
            }
            ItemStack bag = player.getInventory().getItem(i);
            if (bag.isEmpty() || !(bag.getItem() instanceof FlowerPouchItem)) {
                continue;
            }
            SimpleContainer inventory = getInventory(bag);
            ItemStack existing = inventory.getItem(slot);
            int max = Math.min(existing.isEmpty() ? entityStack.getMaxStackSize() : existing.getMaxStackSize(),
                    inventory.getMaxStackSize());
            int newCount = Math.min(existing.getCount() + entityStack.getCount(), max);
            int pickedUp = newCount - existing.getCount();
            if (pickedUp > 0) {
                if (existing.isEmpty()) {
                    inventory.setItem(slot, entityStack.split(pickedUp));
                } else {
                    existing.grow(pickedUp);
                    entityStack.shrink(pickedUp);
                }
                inventory.setChanged();
                if (entityStack.isEmpty()) {
                    entity.discard();
                }
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 1.6F);
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((windowId, inventory, p) -> new FlowerPouchContainer(windowId, inventory, bag),
                            bag.getHoverName()),
                    buffer -> buffer.writeBoolean(hand == InteractionHand.MAIN_HAND));
        }
        return InteractionResultHolder.sidedSuccess(bag, level.isClientSide());
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return InteractionResult.PASS;
        }
        var handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, ctx.getClickedFace()).resolve();
        if (handler.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        IItemHandler itemHandler = handler.get();
        SimpleContainer inventory = getInventory(ctx.getItemInHand());
        boolean moved = false;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack remaining = stack;
            for (int slot = 0; slot < itemHandler.getSlots() && !remaining.isEmpty(); slot++) {
                remaining = itemHandler.insertItem(slot, remaining, false);
            }
            if (remaining.getCount() != stack.getCount()) {
                moved = true;
            }
            inventory.setItem(i, remaining);
        }
        return moved ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity entity) {
        SimpleContainer inventory = getInventory(entity.getItem());
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                Block.popResource(entity.level(), entity.blockPosition(), stack);
            }
        }
        inventory.clearContent();
    }

    private static final class PouchInventory extends SimpleContainer {

        private final ItemStack bag;
        private boolean loading;

        private PouchInventory(ItemStack bag) {
            super(SIZE);
            this.bag = bag;
            CompoundTag tag = bag.getTag();
            if (tag != null && tag.contains(TAG_ITEMS, Tag.TAG_LIST)) {
                loading = true;
                ListTag list = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag entry = list.getCompound(i);
                    int slot = entry.getByte("Slot") & 255;
                    if (slot < SIZE) {
                        super.setItem(slot, ItemStack.of(entry));
                    }
                }
                loading = false;
            }
        }

        @Override
        public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
            return !stack.isEmpty() && stack.is(getFlowerForSlot(slot));
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (!loading) {
                save();
            }
        }

        private void save() {
            ListTag list = new ListTag();
            for (int i = 0; i < SIZE; i++) {
                ItemStack stack = getItem(i);
                if (!stack.isEmpty()) {
                    CompoundTag entry = new CompoundTag();
                    entry.putByte("Slot", (byte) i);
                    stack.save(entry);
                    list.add(entry);
                }
            }
            bag.getOrCreateTag().put(TAG_ITEMS, list);
        }
    }
}
