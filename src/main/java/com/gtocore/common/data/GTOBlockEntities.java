package com.gtocore.common.data;

import com.gtocore.client.renderer.HappyWaterGodStatueRenderer;
import com.gtocore.common.block.HappyWaterGodStatue;
import com.gtocore.common.block.LaurerilManaPools;
import com.gtocore.common.block.LaurerilSourceJars;
import com.gtocore.common.blockentity.HeatPipeBlockEntity;
import com.gtocore.common.blockentity.ManaPipeBlockEntity;
import com.gtocore.common.blockentity.MufflerPipeBlockEntity;
import com.gtocore.common.blockentity.SteamPipeBlockEntity;
import com.gtocore.common.blockentity.UniversalPipeBlockEntity;
import com.gtocore.common.blockentity.WirelessEnergyUnitBlockEntity;
import com.gtocore.common.machine.noenergy.minesweeper.MinesweeperControlBlockEntity;
import com.gtocore.common.machine.noenergy.minesweeper.MinesweeperCoreBlockEntity;
import com.gtocore.common.machine.noenergy.minesweeper.MinesweeperDisplayBlockEntity;

import com.gto.registrate.util.entry.BlockEntityEntry;
import com.hollingsworth.arsnouveau.client.renderer.tile.LargeSourceJarRenderer;
import vazkii.botania.client.render.block_entity.LargeManaPoolBlockEntityRenderer;
import vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer;

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

    public static final BlockEntityEntry<LaurerilSourceJars.Legacy> SOURCE_JAR_BE_BLOCK_ENTITY_ENTRY = GTO.<LaurerilSourceJars.Legacy>blockEntity("laureril_source_jar", LaurerilSourceJars.Legacy::new)
            .validBlocks(GTOBlocks.LAURERIL_JAR)
            .register();

    public static final BlockEntityEntry<LaurerilSourceJars.Standard> STANDARD_LAURERIL_SOURCE_JAR = GTO.<LaurerilSourceJars.Standard>blockEntity("standard_laureril_source_jar", LaurerilSourceJars.Standard::new)
            .validBlocks(GTOBlocks.STANDARD_LAURERIL_JAR)
            .register();

    public static final BlockEntityEntry<LaurerilSourceJars.Large> LARGE_LAURERIL_SOURCE_JAR = GTO.<LaurerilSourceJars.Large>blockEntity("large_laureril_source_jar", LaurerilSourceJars.Large::new)
            .validBlocks(GTOBlocks.LARGE_LAURERIL_JAR)
            .renderer(() -> LargeSourceJarRenderer::new)
            .register();

    public static final BlockEntityEntry<LaurerilManaPools.Pool> LAURERIL_MANA_POOL = GTO.<LaurerilManaPools.Pool>blockEntity("laureril_mana_pool", LaurerilManaPools.Pool::new)
            .validBlocks(GTOBlocks.LAURERIL_MANA_POOL)
            .renderer(() -> ManaPoolBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<LaurerilManaPools.Large> LARGE_LAURERIL_MANA_POOL = GTO.<LaurerilManaPools.Large>blockEntity("large_laureril_mana_pool", LaurerilManaPools.Large::new)
            .validBlocks(GTOBlocks.LARGE_LAURERIL_MANA_POOL)
            .renderer(() -> LargeManaPoolBlockEntityRenderer::new)
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
