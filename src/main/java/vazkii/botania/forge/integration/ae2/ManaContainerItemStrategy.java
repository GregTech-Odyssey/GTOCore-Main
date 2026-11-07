package vazkii.botania.forge.integration.ae2;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.GenericStack;

import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaItem;

final class ManaContainerItemStrategy implements ContainerItemStrategy<ManaKey, ManaItem> {

    @Nullable
    @Override
    public GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ManaItem manaItem) || manaItem.getMana() <= 0) {
            return null;
        }
        return new GenericStack(ManaKey.KEY, manaItem.getMana());
    }

    @Nullable
    @Override
    public ManaItem findCarriedContext(Player player, AbstractContainerMenu menu) {
        var carried = menu.getCarried();
        return carried.isEmpty() || carried.getCount() != 1 ? null : (carried.getItem() instanceof ManaItem item ? item : null);
    }

    @Nullable
    @Override
    public ManaItem findPlayerSlotContext(Player player, int slot) {
        var stack = player.getInventory().getItem(slot);
        return stack.isEmpty() || stack.getCount() != 1 ? null : (stack.getItem() instanceof ManaItem item ? item : null);
    }

    @Override
    public long extract(ManaItem context, ManaKey what, long amount, Actionable mode) {
        if (context.isNoExport()) {
            return 0;
        }
        int taken = (int) Math.min(amount, Math.max(0, context.getMana()));
        if (taken > 0 && mode == Actionable.MODULATE) {
            context.addMana(-taken);
        }
        return taken;
    }

    @Override
    public long insert(ManaItem context, ManaKey what, long amount, Actionable mode) {
        int accepted = (int) Math.min(amount, Math.max(0, context.getMaxMana() - context.getMana()));
        if (accepted > 0 && mode == Actionable.MODULATE) {
            context.addMana(accepted);
        }
        return accepted;
    }

    @Override
    public void playFillSound(Player player, ManaKey what) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.4F);
    }

    @Override
    public void playEmptySound(Player player, ManaKey what) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 0.9F);
    }

    @Nullable
    @Override
    public GenericStack getExtractableContent(ManaItem context) {
        return context.isNoExport() || context.getMana() <= 0 ? null : new GenericStack(ManaKey.KEY, context.getMana());
    }
}
