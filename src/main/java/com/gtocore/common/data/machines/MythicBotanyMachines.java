package com.gtocore.common.data.machines;

import com.gtocore.client.renderer.machine.RuneHolderMachineRenderer;
import com.gtocore.common.machine.magic.rune.CentralRuneHolderMachine;
import com.gtocore.common.machine.magic.rune.RuneHolderMachine;

import com.gtolib.api.registries.GTORegistration;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 神话植物学的机器定义：符文支托与大师符文支托。
 */
public final class MythicBotanyMachines {

    private MythicBotanyMachines() {}

    public static void init() {}

    private static final VoxelShape HOLDER_SHAPE = Shapes.or(
            Shapes.box(6.5 / 16.0, 1 / 16.0, 6.5 / 16.0, 9.5 / 16.0, 3 / 16.0, 9.5 / 16.0),
            Shapes.box(5 / 16.0, 0, 5 / 16.0, 11 / 16.0, 1 / 16.0, 11 / 16.0));

    public static final MachineDefinition RUNE_HOLDER = GTORegistration.MYTHICBOTANY
            .machine("rune_holder", RuneHolderMachine::new)
            .noneRotation()
            .shape(HOLDER_SHAPE)
            .blockProp(BlockBehaviour.Properties::noOcclusion)
            .renderer(() -> new RuneHolderMachineRenderer(new ResourceLocation("mythicbotany", "block/rune_holder")))
            .hasTESR(true)
            .register();

    public static final MachineDefinition CENTRAL_RUNE_HOLDER = GTORegistration.MYTHICBOTANY
            .machine("central_rune_holder", CentralRuneHolderMachine::new)
            .noneRotation()
            .shape(HOLDER_SHAPE)
            .blockProp(BlockBehaviour.Properties::noOcclusion)
            .renderer(() -> new RuneHolderMachineRenderer(new ResourceLocation("mythicbotany", "block/central_rune_holder")))
            .hasTESR(true)
            .register();
}
