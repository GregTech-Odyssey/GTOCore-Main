package com.gtocore.common.monitor;

import com.gtocore.common.data.GTOCreativeModeTabs;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.config.GTOConfig;
import com.gtocore.integration.ae.wireless.WirelessPermissions;

import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.monitor.*;
import com.gtolib.monitor.sources.MonitorDataSources;
import com.gtolib.monitor.sources.MonitorInfoSources;
import com.gtolib.monitor.sources.MonitorPersonalSources;
import com.gtolib.utils.register.BlockRegisterUtils;
import com.gtolib.utils.register.ItemRegisterUtils;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;

import com.gto.registrate.util.entry.BlockEntityEntry;
import com.gto.registrate.util.entry.BlockEntry;
import com.gto.registrate.util.entry.ItemEntry;
import com.gto.registrate.util.nullness.NonNullBiConsumer;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.gtolib.api.registries.GTORegistration.GTO;

public final class MonitorRegistries {

    public static final BlockEntry<MonitorBusBlock> BUS = BlockRegisterUtils.block("monitor_bus", "监控总线", MonitorBusBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL).strength(1.5f, 6.0f).sound(SoundType.METAL).noOcclusion().dynamicShape())
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate(NonNullBiConsumer.noop())
            .loot(NonNullBiConsumer.noop())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .register();

    public static final BlockEntityEntry<MonitorBusBlockEntity> BUS_BLOCK_ENTITY = GTO
            .blockEntity("monitor_bus", MonitorBusBlockEntity::new)
            .validBlocks(BUS)
            .register();

    public static final ItemEntry<MonitorPartItem> DISPLAY_PANEL = ItemRegisterUtils.item("monitor_display_panel", "显示器", p -> new MonitorPartItem(p, BusPart.DISPLAY, null))
            .model(NonNullBiConsumer.noop())
            .lang("Display")
            .color(() -> () -> MonitorPartItem::tint)
            .tab(GTOCreativeModeTabs.GTO_MACHINE.getKey())
            .register();

    public static final ItemEntry<MonitorPartItem> ME_PANEL = ItemRegisterUtils.item("monitor_me_panel", "ME控件", p -> new MonitorPartItem(p, BusPart.DATA, MonitorPanels.ME))
            .model(NonNullBiConsumer.noop())
            .lang("ME Control")
            .color(() -> () -> MonitorPartItem::tint)
            .tab(GTOCreativeModeTabs.GTO_MACHINE.getKey())
            .register();

    public static final ItemEntry<MonitorPartItem> MACHINE_PANEL = ItemRegisterUtils.item("monitor_machine_panel", "机器控件", p -> new MonitorPartItem(p, BusPart.DATA, MonitorPanels.MACHINE))
            .model(NonNullBiConsumer.noop())
            .lang("Machine Control")
            .color(() -> () -> MonitorPartItem::tint)
            .tab(GTOCreativeModeTabs.GTO_MACHINE.getKey())
            .register();

    public static final ItemEntry<MonitorPartItem> REDSTONE_PANEL = ItemRegisterUtils.item("monitor_redstone_panel", "红石控件", p -> new MonitorPartItem(p, BusPart.DATA, MonitorPanels.REDSTONE))
            .model(NonNullBiConsumer.noop())
            .lang("Redstone Control")
            .color(() -> () -> MonitorPartItem::tint)
            .tab(GTOCreativeModeTabs.GTO_MACHINE.getKey())
            .register();

    public static final ItemEntry<MonitorPartItem> SHARE_PANEL = ItemRegisterUtils.item("monitor_share_panel", "玩家组件上传控件", p -> new MonitorPartItem(p, BusPart.DATA, MonitorPanels.SHARE))
            .model(NonNullBiConsumer.noop())
            .lang("Player Component Upload Control")
            .color(() -> () -> MonitorPartItem::tint)
            .tab(GTOCreativeModeTabs.GTO_MACHINE.getKey())
            .register();

    public static final ItemEntry<MonitorPartItem> DOWNLOAD_PANEL = ItemRegisterUtils.item("monitor_download_panel", "玩家组件下载控件", p -> new MonitorPartItem(p, BusPart.DATA, MonitorPanels.DOWNLOAD))
            .model(NonNullBiConsumer.noop())
            .lang("Player Component Download Control")
            .color(() -> () -> MonitorPartItem::tint)
            .tab(GTOCreativeModeTabs.GTO_MACHINE.getKey())
            .register();
    public static final ItemEntry<MonitorCableItem> CABLE_ITEM = ItemRegisterUtils.item("monitor_cable", "监控线缆", p -> new MonitorCableItem(BUS.get(), p, BusPart.NO_COLOR))
            .model(NonNullBiConsumer.noop())
            .lang("Monitor Cable")
            .color(() -> () -> MonitorCableItem::tint)
            .tab(GTOCreativeModeTabs.GTO_MACHINE.getKey())
            .register();

    private static final ItemEntry<MonitorCableItem>[] DYED_CABLES = registerDyedCables();

    static {
        MonitorDataSources.init();
        MonitorPersonalSources.init();
        MonitorInfoSources.init();
        MonitorPanels.init();
        MonitorContent.install(new MonitorContent() {

            @Override
            public Block bus() {
                return BUS.get();
            }

            @Override
            public BlockEntityType<MonitorBusBlockEntity> busEntity() {
                return BUS_BLOCK_ENTITY.get();
            }

            @Override
            public Item display() {
                return DISPLAY_PANEL.get();
            }

            @Override
            public Item cable(byte color) {
                return MonitorRegistries.cable(color);
            }

            @Override
            public int maxScreenSize() {
                return GTOConfig.INSTANCE.gamePlay.maxMonitorSize;
            }

            @Override
            public boolean sameTeam(@Nullable UUID a, @Nullable UUID b) {
                return WirelessPermissions.sameTeam(a, b);
            }

            @Override
            public boolean hudEnabled(Player player) {
                return IEnhancedPlayer.of(player).getPlayerData().organs.hasImplant(GTOOrganItems.MIND_IMPLANT.get());
            }

            @Override
            public boolean hasImplant(Player player, Item implant) {
                return IEnhancedPlayer.of(player).getPlayerData().organs.hasImplant(implant);
            }

            @Override
            public Item hudItem() {
                return GTOOrganItems.MIND_IMPLANT.get();
            }

            @Override
            public boolean webImages() {
                return GTOConfig.INSTANCE.gamePlay.monitorWebImages;
            }
        });
    }

    private MonitorRegistries() {}

    public static void init() {}

    @SuppressWarnings("unchecked")
    private static ItemEntry<MonitorCableItem>[] registerDyedCables() {
        var dyes = DyeColor.values();
        var entries = (ItemEntry<MonitorCableItem>[]) new ItemEntry<?>[dyes.length];
        for (var dye : dyes) {
            byte color = (byte) dye.getId();
            entries[color] = ItemRegisterUtils.item("monitor_cable_" + dye.getSerializedName(), dyeChineseName(dye) + "监控线缆", p -> new MonitorCableItem(BUS.get(), p, color))
                    .model(NonNullBiConsumer.noop())
                    .lang(FormattingUtil.toEnglishName(dye.getSerializedName()) + " Monitor Cable")
                    .color(() -> () -> MonitorCableItem::tint)
                    .tab(GTOCreativeModeTabs.GTO_MACHINE.getKey())
                    .register();
        }
        return entries;
    }

    private static String dyeChineseName(DyeColor dye) {
        return switch (dye) {
            case WHITE -> "白色";
            case ORANGE -> "橙色";
            case MAGENTA -> "品红色";
            case LIGHT_BLUE -> "淡蓝色";
            case YELLOW -> "黄色";
            case LIME -> "黄绿色";
            case PINK -> "粉红色";
            case GRAY -> "灰色";
            case LIGHT_GRAY -> "淡灰色";
            case CYAN -> "青色";
            case PURPLE -> "紫色";
            case BLUE -> "蓝色";
            case BROWN -> "棕色";
            case GREEN -> "绿色";
            case RED -> "红色";
            case BLACK -> "黑色";
        };
    }

    public static ItemEntry<MonitorCableItem> dyedCable(DyeColor dye) {
        return DYED_CABLES[dye.getId()];
    }

    public static MonitorCableItem cable(byte color) {
        return color < 0 ? CABLE_ITEM.get() : DYED_CABLES[color].get();
    }
}
