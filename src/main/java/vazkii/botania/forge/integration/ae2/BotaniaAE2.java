package vazkii.botania.forge.integration.ae2;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegisterEvent;

import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageCells;

/** AE2 联动：魔力键类型与魔力磁盘。 */
public final class BotaniaAE2 {

    public static final ResourceKey<Registry<AEKeyType<?>>> KEY_TYPES = ResourceKey
            .createRegistryKey(new ResourceLocation("ae2", "keytypes"));

    private BotaniaAE2() {}

    public static void init(IEventBus modBus) {
        vazkii.botania.forge.BotaniaForgeCapabilities.register(modBus);
        modBus.addListener(BotaniaAE2::onRegister);
        modBus.addListener(BotaniaAE2::setup);
        // AE2 要求在预初始化阶段注册 part 模型，晚于 FMLCommonSetupEvent 会抛异常
        appeng.api.parts.PartModels.registerModels(
                appeng.items.parts.PartModelsHelper.createModels(ManaP2PTunnelPart.class));
    }

    private static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(KEY_TYPES)) {
            event.register(KEY_TYPES, ManaKeyType.ID, () -> ManaKeyType.TYPE);
        }
    }

    private static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            StorageCells.addCellHandler(ManaCellHandler.INSTANCE);
            appeng.api.behaviors.ExternalStorageStrategy.register(ManaKeyType.TYPE, ManaStorageStrategies::external);
            appeng.api.behaviors.StackImportStrategy.register(ManaKeyType.TYPE, ManaStorageStrategies::importer);
            appeng.api.behaviors.StackExportStrategy.register(ManaKeyType.TYPE, ManaStorageStrategies::exporter);
            appeng.api.behaviors.ContainerItemStrategy.register(ManaKeyType.TYPE, ManaKey.class,
                    new ManaContainerItemStrategy());
            appeng.api.behaviors.GenericSlotCapacities.register(ManaKeyType.TYPE, 1_000_000L);
            appeng.api.client.StorageCellModels.registerModel(
                    vazkii.botania.common.BotaniaItems.MANA_STORAGE_CELL_1K.get(),
                    new ResourceLocation("botania", "block/drive/mana_cell_1k"));
            appeng.api.client.StorageCellModels.registerModel(
                    vazkii.botania.common.BotaniaItems.MANA_STORAGE_CELL_4K.get(),
                    new ResourceLocation("botania", "block/drive/mana_cell_4k"));
            appeng.api.client.StorageCellModels.registerModel(
                    vazkii.botania.common.BotaniaItems.MANA_STORAGE_CELL_16K.get(),
                    new ResourceLocation("botania", "block/drive/mana_cell_16k"));
            appeng.api.client.StorageCellModels.registerModel(
                    vazkii.botania.common.BotaniaItems.MANA_STORAGE_CELL_64K.get(),
                    new ResourceLocation("botania", "block/drive/mana_cell_64k"));
            appeng.api.client.StorageCellModels.registerModel(
                    vazkii.botania.common.BotaniaItems.MANA_STORAGE_CELL_256K.get(),
                    new ResourceLocation("botania", "block/drive/mana_cell_256k"));
            appeng.api.client.StorageCellModels.registerModel(
                    vazkii.botania.common.BotaniaItems.CREATIVE_MANA_CELL.get(),
                    new ResourceLocation("botania", "block/drive/creative_mana_cell"));
        });
    }
}
