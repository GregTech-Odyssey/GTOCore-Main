package com.gtocore.common.item;

import com.gtocore.api.wireless.energy.ProviderRegistry;
import com.gtocore.common.block.WirelessEnergyTowerUnit;
import com.gtocore.common.blockentity.WirelessEnergyUnitBlockEntity;
import com.gtocore.common.machine.multiblock.storage.WirelessEnergySubstationMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

import java.math.BigInteger;

public class WirelessEnergyUnitBlockItem extends BlockItem {

    public WirelessEnergyUnitBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
            if (getBlock() instanceof WirelessEnergyTowerUnit unit &&
                    context.getLevel() instanceof ServerLevel serverLevel &&
                    context.getPlayer() instanceof ServerPlayer serverPlayer &&
                    serverLevel.getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity mbe &&
                    mbe.getMetaMachine() instanceof WirelessEnergySubstationMachine machine) {
                var substituted = machine.substituteBlocks(unit, stack.getCount(), serverPlayer);
                if (substituted > 0) {
                    var stored = WirelessEnergyUnitBlockEntity.stored(stack);
                    if (stored != null) ProviderRegistry.chargeTower(machine, stored.multiply(BigInteger.valueOf(substituted)));
                    stack.shrink(substituted);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.CONSUME;
        }
        return super.onItemUseFirst(stack, context);
    }
}
