package com.gtocore.common.data;

import com.gtocore.common.blockentity.HeatPipeBlockEntity;
import com.gtocore.common.blockentity.ManaPipeBlockEntity;
import com.gtocore.common.blockentity.MufflerPipeBlockEntity;
import com.gtocore.common.blockentity.SourceJarBE;

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

    public static final BlockEntityEntry<SourceJarBE> SOURCE_JAR_BE_BLOCK_ENTITY_ENTRY = GTO
            .<SourceJarBE>blockEntity("laureril_source_jar", SourceJarBE::new)
            .validBlocks(GTOBlocks.LAURERIL_JAR)
            .register();
}
