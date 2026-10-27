package com.gtocore.api.ae2.stacks;

import com.gtolib.utils.MathUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyLongMap;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.forge.integration.ae2.ManaKey;

import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.function.ObjLongConsumer;

public final class AEManaKeyHandler implements ManaPool {

    @Setter
    private AEKeyLongMap<AEKey> map;
    @Setter
    private long capacity;
    @Setter
    private LongSupplier storageSupplier;
    /** 每次实际变动后回调 (key, 变动量)：正为存入、负为取出，调用方据此增量记账。 */
    @Setter
    private ObjLongConsumer<AEKey> onChange;

    @Setter
    @Nullable
    private Level level;

    @Setter
    @Nullable
    private BlockPos pos;

    @Override
    public Level getManaReceiverLevel() {
        return this.level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return this.pos;
    }

    @Override
    public int getCurrentMana() {
        if (map == null) return 0;
        return MathUtil.saturatedCast(map.getAmount(ManaKey.KEY));
    }

    @Override
    public boolean isFull() {
        if (storageSupplier == null) return true;
        return storageSupplier.getAsLong() >= capacity;
    }

    @Override
    public void receiveMana(int mana) {
        if (map == null || mana == 0) return;
        long applied;
        if (mana > 0) {
            applied = map.insert(ManaKey.KEY, mana, Long.MAX_VALUE);
        } else {
            applied = -map.extract(ManaKey.KEY, -mana);
        }
        onChange.accept(ManaKey.KEY, applied);
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        if (storageSupplier == null) return false;
        return storageSupplier.getAsLong() < capacity;
    }

    @Override
    public boolean isOutputtingPower() {
        return false;
    }

    @Override
    public int getMaxMana() {
        return MathUtil.saturatedCast(getMaxManaLong());
    }

    @Override
    public long getMaxManaLong() {
        if (storageSupplier == null) return 0;
        long free = Math.max(0, capacity - storageSupplier.getAsLong());
        return free > Long.MAX_VALUE / 62 ? Long.MAX_VALUE : free * 62;
    }

    @Override
    public long getCurrentManaLong() {
        return map == null ? 0 : map.getAmount(ManaKey.KEY);
    }

    @Override
    public void receiveManaLong(long mana) {
        if (map == null || mana == 0) return;
        long applied;
        if (mana > 0) {
            applied = map.insert(ManaKey.KEY, mana, Long.MAX_VALUE);
        } else {
            applied = -map.extract(ManaKey.KEY, -mana);
        }
        onChange.accept(ManaKey.KEY, applied);
    }

    @Override
    public Optional<DyeColor> getColor() {
        return Optional.of(DyeColor.GRAY);
    }

    @Override
    public void setColor(Optional<DyeColor> color) {}
}
