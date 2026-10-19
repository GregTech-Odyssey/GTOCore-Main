package com.gtocore.common.data;

import com.gtocore.client.renderer.HappyWaterGodStatueRenderer;
import com.gtocore.common.block.HappyWaterGodStatue;
import com.gtocore.common.blockentity.HeatPipeBlockEntity;
import com.gtocore.common.blockentity.ManaPipeBlockEntity;
import com.gtocore.common.blockentity.MufflerPipeBlockEntity;
import com.gtocore.common.blockentity.SourceJarBE;
import com.gtocore.common.blockentity.SteamPipeBlockEntity;
import com.gtocore.common.blockentity.UniversalPipeBlockEntity;
import com.gtocore.common.blockentity.WirelessEnergyUnitBlockEntity;
import com.gtocore.common.machine.noenergy.minesweeper.MinesweeperControlBlockEntity;
import com.gtocore.common.machine.noenergy.minesweeper.MinesweeperCoreBlockEntity;
import com.gtocore.common.machine.noenergy.minesweeper.MinesweeperDisplayBlockEntity;

import com.gto.registrate.util.entry.BlockEntityEntry;

import static com.gtolib.api.registries.GTORegistration.GTO;

public class GTOBlockEntities {

    public static void init() {}

    public static final BlockEntityEntry<HeatPipeBlockEntity> HEAT_PIPE = GTO
            .blockEntity("heat_pipe", HeatPipeBlockEntity::new)
            .validBlocks(GTOBlocks.HEAT_PIPES)
            .register();

    public static final BlockEntityEntry<ManaPipeBlockEntity> MANA_PIPE = GTO
            .blockEntity("mana_pipe", ManaPipeBlockEntity::new)
            .validBlocks(GTOBlocks.MANA_PIPES)
            .register();

    public static final BlockEntityEntry<MufflerPipeBlockEntity> MUFFLER_PIPE = GTO
            .blockEntity("muffler_pipe", MufflerPipeBlockEntity::new)
            .validBlocks(GTOBlocks.MUFFLER_PIPES)
            .register();

    public static final BlockEntityEntry<SteamPipeBlockEntity> STEAM_PIPE = GTO
            .blockEntity("steam_pipe", SteamPipeBlockEntity::new)
            .validBlocks(GTOBlocks.STEAM_PIPES)
            .register();

    public static final BlockEntityEntry<UniversalPipeBlockEntity> UNIVERSAL_PIPE = GTO
            .blockEntity("universal_pipe", UniversalPipeBlockEntity::new)
            .validBlocks(GTOBlocks.UNIVERSAL_PIPES)
            .register();

    public static final BlockEntityEntry<SourceJarBE> SOURCE_JAR_BE_BLOCK_ENTITY_ENTRY = GTO
            .<SourceJarBE>blockEntity("laureril_source_jar", SourceJarBE::new)
            .validBlocks(GTOBlocks.LAURERIL_JAR)
            .register();

    public static final BlockEntityEntry<WirelessEnergyUnitBlockEntity> WIRELESS_ENERGY_UNIT = GTO
            .blockEntity("wireless_energy_unit", WirelessEnergyUnitBlockEntity::new)
            .validBlocks(GTOBlocks.LV_WIRELESS_ENERGY_UNIT, GTOBlocks.MV_WIRELESS_ENERGY_UNIT, GTOBlocks.HV_WIRELESS_ENERGY_UNIT, GTOBlocks.EV_WIRELESS_ENERGY_UNIT,
                    GTOBlocks.IV_WIRELESS_ENERGY_UNIT, GTOBlocks.LUV_WIRELESS_ENERGY_UNIT, GTOBlocks.ZPM_WIRELESS_ENERGY_UNIT, GTOBlocks.UV_WIRELESS_ENERGY_UNIT,
                    GTOBlocks.UHV_WIRELESS_ENERGY_UNIT, GTOBlocks.UEV_WIRELESS_ENERGY_UNIT, GTOBlocks.UIV_WIRELESS_ENERGY_UNIT, GTOBlocks.UXV_WIRELESS_ENERGY_UNIT,
                    GTOBlocks.OPV_WIRELESS_ENERGY_UNIT, GTOBlocks.MAX_WIRELESS_ENERGY_UNIT)
            .register();

    public static final BlockEntityEntry<MinesweeperCoreBlockEntity> MINESWEEPER_CORE = GTO
            .blockEntity("minesweeper_core", MinesweeperCoreBlockEntity::new)
            .validBlocks(GTOBlocks.MINESWEEPER_CORE)
            .register();

    public static final BlockEntityEntry<MinesweeperDisplayBlockEntity> MINESWEEPER_DISPLAY = GTO
            .blockEntity("minesweeper_display", MinesweeperDisplayBlockEntity::new)
            .validBlocks(GTOBlocks.MINESWEEPER_DISPLAY)
            .register();

    public static final BlockEntityEntry<MinesweeperControlBlockEntity> MINESWEEPER_CONTROL = GTO
            .blockEntity("minesweeper_control", MinesweeperControlBlockEntity::new)
            .validBlocks(GTOBlocks.MINESWEEPER_CONTROL)
            .register();

    public static final BlockEntityEntry<HappyWaterGodStatue.Entity> HAPPY_WATER_GOD_STATUE = GTO.<HappyWaterGodStatue.Entity>blockEntity("happy_water_god_statue", HappyWaterGodStatue.Entity::new)
            .validBlocks(GTOBlocks.HAPPY_WATER_GOD_STATUE)
            .renderer(() -> HappyWaterGodStatueRenderer::new)
            .register();
}
