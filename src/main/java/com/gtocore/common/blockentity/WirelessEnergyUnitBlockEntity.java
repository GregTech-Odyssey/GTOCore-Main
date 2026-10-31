package com.gtocore.common.blockentity;

import com.gtocore.common.block.WirelessEnergyTowerUnit;
import com.gtocore.common.block.WirelessEnergyUnitBlock;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.wireless.energy.ProviderRegistry;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.IDataComponentItem;
import com.gregtechceu.gtceu.api.item.ItemDataComponentKey;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.tooltip.ElectricText;
import com.gregtechceu.gtceu.api.tooltip.StatKind;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import com.gto.datasynclib.datastream.codec.JavaValueOps;
import com.gto.datasynclib.datastream.codec.ValueCodec;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;

/**
 * 无线能源塔存储单元里的电量：塔结构失效时把本塔分得的电量按单元等级由高到低写入，拆下后随物品保留，塔重新成型时并入电网。
 */
@DataGeneratorScanned
public final class WirelessEnergyUnitBlockEntity extends BlockEntity {

    @RegisterLanguage(cn = "能源塔结构失效时存入；装回能源塔并成型后并入电网", en = "Stored when the tower's structure breaks; returns to the grid once the tower forms again")
    private static final String STORED_DETAIL = "gtocore.block.wireless_energy_unit.stored.detail";

    public static final ItemDataComponentKey<BigInteger, Tag> ENERGY = ItemDataComponentKey.create("gtocore_wireless_unit_energy", WirelessEnergyUnitBlockEntity::write, WirelessEnergyUnitBlockEntity::read);
    private static final String ENERGY_TAG = "energy";
    private static final double TICKS_PER_HOUR = 20 * 3600;

    private BigInteger energy = BigInteger.ZERO;

    public WirelessEnergyUnitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static Tag write(BigInteger value) {
        return new ByteArrayTag(JavaValueOps.INSTANCE.toBytes(ValueCodec.BIG_INTEGER.encode(JavaValueOps.INSTANCE, value)));
    }

    @Nullable
    private static BigInteger read(Tag tag) {
        if (!(tag instanceof ByteArrayTag bytes)) return null;
        var value = ValueCodec.BIG_INTEGER.decode(JavaValueOps.INSTANCE, JavaValueOps.INSTANCE.fromBytes(bytes.getAsByteArray()));
        return value != null && value.signum() > 0 ? value : null;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        var value = tag.contains(ENERGY_TAG) ? read(tag.get(ENERGY_TAG)) : null;
        energy = value == null ? BigInteger.ZERO : value;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (energy.signum() > 0) tag.put(ENERGY_TAG, write(energy));
    }

    public static void onPlaced(Level level, BlockPos pos, ItemStack stack) {
        if (level.isClientSide) return;
        var value = IDataComponentItem.get(stack, ENERGY);
        if (value != null && level.getBlockEntity(pos) instanceof WirelessEnergyUnitBlockEntity unit) {
            unit.energy = value;
            unit.setChanged();
        }
    }

    public static List<ItemStack> withEnergy(List<ItemStack> drops, @Nullable BlockEntity blockEntity, Item item) {
        if (!(blockEntity instanceof WirelessEnergyUnitBlockEntity unit) || unit.energy.signum() <= 0) return drops;
        for (var stack : drops) {
            if (stack.is(item)) {
                IDataComponentItem.set(stack, ENERGY, unit.energy);
                break;
            }
        }
        return drops;
    }

    @Nullable
    public static BigInteger stored(ItemStack stack) {
        return IDataComponentItem.get(stack, ENERGY);
    }

    public static void appendTooltip(ItemStack stack, int tier, List<Component> tooltip) {
        var value = IDataComponentItem.get(stack, ENERGY);
        if (value == null) return;
        tooltip.add(value.bitLength() < 63 ? Tips.energy(StatKind.CHARGE, value.longValue(), tier) :
                Tips.stat(StatKind.CHARGE.line(ElectricText.energy(value, tier))));
        tooltip.add(Tips.detail(Component.translatable(STORED_DETAIL)));
    }

    public record Snapshot(Multimap<Integer, BlockPos> units, int glassTier, int multiplierHalf) {

        public static Snapshot of(Multimap<Integer, BlockPos> units, int glassTier, int multiplierHalf) {
            return new Snapshot(ArrayListMultimap.create(units), glassTier, multiplierHalf);
        }
    }

    public static void absorb(MetaMachine tower, Multimap<Integer, BlockPos> units, int glassTier) {
        var level = tower.getLevel();
        if (level == null) return;
        var charged = new ObjectArrayList<WirelessEnergyUnitBlockEntity>();
        var total = BigInteger.ZERO;
        for (int tier = 1; tier <= Math.min(glassTier, GTValues.MAX); tier++) {
            for (var pos : units.get(tier)) {
                if (level.getChunkAt(pos).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK) instanceof WirelessEnergyUnitBlockEntity unit && unit.energy.signum() > 0) {
                    charged.add(unit);
                    total = total.add(unit.energy);
                }
            }
        }
        if (charged.isEmpty() || !ProviderRegistry.chargeTower(tower, total)) return;
        for (var unit : charged) level.removeBlockEntity(unit.worldPosition);
    }

    public static void release(MetaMachine tower, @Nullable Snapshot snapshot, @Nullable BlockPos first) {
        var charge = snapshot != null && tower.getLevel() instanceof ServerLevel level ? ProviderRegistry.towerCharge(tower) : null;
        if (charge == null || charge.energy().signum() <= 0) {
            ProviderRegistry.releaseTower(tower, BigInteger.ZERO);
            return;
        }
        var level = (ServerLevel) tower.getLevel();
        var left = charge.energy();
        if (first != null && level.getBlockState(first).getBlock() instanceof WirelessEnergyUnitBlock block && block.getTier() <= snapshot.glassTier() && snapshot.units().containsEntry(block.getTier(), first)) {
            left = fill(level, first, block, unitCapacity(block, snapshot, charge), left);
        }
        for (int tier = Math.min(snapshot.glassTier(), GTValues.MAX); tier >= 1 && left.signum() > 0; tier--) {
            var block = WirelessEnergyUnitBlock.get(tier);
            var positions = snapshot.units().get(tier);
            if (block == null || positions.isEmpty()) continue;
            var capacity = unitCapacity(block, snapshot, charge);
            for (var pos : positions) {
                left = fill(level, pos, block, capacity, left);
                if (left.signum() <= 0) break;
            }
        }
        ProviderRegistry.releaseTower(tower, charge.energy().subtract(left));
    }

    private static BigInteger unitCapacity(WirelessEnergyUnitBlock block, Snapshot snapshot, ProviderRegistry.TowerCharge charge) {
        return WirelessEnergyTowerUnit.boostCapacity(block.getCapacity(), snapshot.multiplierHalf()).multiply(BigInteger.valueOf(block.isGridScaled() ? charge.scale(block.getTier()) : 1));
    }

    private static BigInteger fill(ServerLevel level, BlockPos pos, WirelessEnergyUnitBlock block, BigInteger capacity, BigInteger left) {
        if (left.signum() <= 0 || !level.isLoaded(pos) || level.getBlockState(pos).getBlock() != block || !(level.getBlockEntity(pos) instanceof WirelessEnergyUnitBlockEntity unit)) return left;
        var room = capacity.subtract(unit.energy);
        if (room.signum() <= 0) return left;
        var put = room.min(left);
        unit.energy = unit.energy.add(put);
        unit.setChanged();
        return left.subtract(put);
    }
}
