package com.gtocore.common.recipe;

import com.gtocore.common.data.GTOCodecs;
import com.gtocore.common.data.machines.MultiBlockG;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;

import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.datastream.data.ListData;
import com.gto.datasynclib.datastream.data.LongData;
import com.gto.datasynclib.datastream.data.NullData;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

public final class GeneralVaultRecipe extends ShapedRecipe {

    public GeneralVaultRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getWidth(), recipe.getHeight(),
                recipe.getIngredients(), recipe.getResultItem(RegistryAccess.EMPTY));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer container, @NotNull RegistryAccess registryAccess) {
        var merged = new AEKeyMap<AEKey>();
        try {
            for (int i = 0; i < container.getContainerSize(); i++) {
                var stack = container.getItem(i);
                if (stack.is(MultiBlockG.ITEM_VAULT.asItem()) || stack.is(MultiBlockG.FLUID_VAULT.asItem())) {
                    mergeKeyMap(stack, merged);
                }
            }
            var result = super.assemble(container, registryAccess);
            if (!merged.isEmpty()) {
                var data = new ListData(merged.size() * 2);
                merged.fastForEach((key, amount) -> {
                    data.add(GTOCodecs.AE_KEY_DATA_CODEC.encode(key));
                    data.add(LongData.valueOf(amount));
                });
                result.getOrCreateTag().putByteArray("keymap", data.writeToBytes());
            }
            return result;
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static void mergeKeyMap(@NotNull ItemStack stack, AEKeyMap<AEKey> merged) {
        var tag = stack.getTag();
        if (tag == null || !tag.contains("keymap")) return;
        if (!(tag.get("keymap") instanceof ByteArrayTag bytes)) throw new IllegalArgumentException("Invalid vault keymap");
        var buffer = Unpooled.wrappedBuffer(bytes.getAsByteArray());
        Data data;
        try {
            data = Data.readData(buffer);
            if (buffer.isReadable()) throw new IllegalArgumentException("Invalid vault keymap length");
        } finally {
            buffer.release();
        }
        if (data == NullData.INSTANCE) return;
        var list = data.asListData();
        if ((list.size() & 1) != 0) throw new IllegalArgumentException("Invalid vault keymap");
        merged.ensureCapacity(merged.size() + list.size() / 2);
        for (int i = 0; i < list.size(); i += 2) {
            var key = GTOCodecs.AE_KEY_DATA_CODEC.decode(list.get(i), 0);
            long amount = list.getLong(i + 1);
            if (key == null || amount <= 0) {
                throw new IllegalArgumentException("Invalid vault keymap entry");
            }
            merged.set(key, Math.addExact(merged.getAmount(key), amount));
        }
    }
}
