package com.gtocore.common.recipe;

import com.gtocore.common.data.machines.MultiBlockG;

import com.gregtechceu.gtceu.api.transfer.key.KeyCodecs;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyLongMap;

import com.gto.datasynclib.datastream.codec.JavaValueOps;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class GeneralVaultRecipe extends ShapedRecipe {

    public GeneralVaultRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getWidth(), recipe.getHeight(),
                recipe.getIngredients(), recipe.getResultItem(RegistryAccess.EMPTY));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer container, @NotNull RegistryAccess registryAccess) {
        var merged = new AEKeyLongMap<AEKey>();
        try {
            for (int i = 0; i < container.getContainerSize(); i++) {
                var stack = container.getItem(i);
                if (stack.is(MultiBlockG.ITEM_VAULT.asItem()) || stack.is(MultiBlockG.FLUID_VAULT.asItem())) {
                    mergeKeyMap(stack, merged);
                }
            }
            var result = super.assemble(container, registryAccess);
            if (!merged.isEmpty()) {
                var ops = JavaValueOps.INSTANCE;
                var list = new ArrayList<Object>(merged.size() * 2);
                merged.fastForEach((key, amount) -> {
                    list.add(KeyCodecs.AE_KEY_DATA_CODEC.encode(ops, key));
                    ops.addLong(list, amount);
                });
                result.getOrCreateTag().putByteArray("keymap", ops.toBytes(ops.createList(list)));
            }
            return result;
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static void mergeKeyMap(@NotNull ItemStack stack, AEKeyLongMap<AEKey> merged) {
        var tag = stack.getTag();
        if (tag == null || !tag.contains("keymap")) return;
        if (!(tag.get("keymap") instanceof ByteArrayTag bytes)) throw new IllegalArgumentException("Invalid vault keymap");
        var buffer = Unpooled.wrappedBuffer(bytes.getAsByteArray());
        Object data;
        try {
            data = JavaValueOps.INSTANCE.readValue(buffer);
            if (buffer.isReadable()) throw new IllegalArgumentException("Invalid vault keymap length");
        } finally {
            buffer.release();
        }
        var ops = JavaValueOps.INSTANCE;
        if (ops.isNull(data)) return;
        var list = ops.getList(data);
        if ((list.size() & 1) != 0) throw new IllegalArgumentException("Invalid vault keymap");
        merged.ensureCapacity(merged.size() + list.size() / 2);
        for (int i = 0; i < list.size(); i += 2) {
            var key = KeyCodecs.AE_KEY_DATA_CODEC.decode(ops, list.get(i));
            long amount = ops.getLong(list, i + 1);
            if (key == null || amount <= 0) {
                throw new IllegalArgumentException("Invalid vault keymap entry");
            }
            merged.set(key, Math.addExact(merged.getAmount(key), amount));
        }
    }
}
