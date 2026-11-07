package vazkii.botania.forge.integration.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

import java.util.List;

public final class ManaKey extends AEKey {

    public static final ManaKey KEY = new ManaKey();

    private ManaKey() {}

    @Override
    public AEKeyType<ManaKey> getType() {
        return ManaKeyType.TYPE;
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Override
    public CompoundTag toTag() {
        return new CompoundTag();
    }

    @Override
    public Object getPrimaryKey() {
        return this;
    }

    @Override
    public ResourceLocation getId() {
        return ManaKeyType.ID;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf data) {}

    @Override
    protected Component computeDisplayName() {
        return Component.translatable(ManaKeyType.NAME_KEY);
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {}
}
