package com.gtocore.common.monitor;

import com.gtolib.monitor.MonitorPanelType;
import com.gtolib.monitor.MonitorPanelTypes;
import com.gtolib.monitor.MonitorPartItem;
import com.gtolib.monitor.sources.MonitorDataSources;
import com.gtolib.monitor.sources.MonitorPersonalSources;

import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.network.chat.Component;

import java.util.List;

public final class MonitorPanels {

    public static final MonitorPanelType ME = MonitorPanelTypes.register("me", MonitorDataSources.ME, "gtocore.monitor.group.me_panel", MonitorRegistries.ME_PANEL, false,
            MonitorPartItem.dataTips(Component.translatable("gtocore.monitor.tip.me"), Component.translatable("gtocore.monitor.tip.me.channels"),
                    Component.translatable("gtocore.monitor.tip.key.place_me")),
            Component.translatable("gtocore.monitor.control.requirement.me"), List.of("me.cpu"),
            List.of(Component.translatable("gtocore.monitor.control.how.me.read"), Component.translatable("gtocore.monitor.control.how.me.item")));
    public static final MonitorPanelType MACHINE = MonitorPanelTypes.register("machine", MonitorDataSources.MACHINE, "gtocore.monitor.group.machine_panel", MonitorRegistries.MACHINE_PANEL, false,
            MonitorPartItem.dataTips(Component.translatable("gtocore.monitor.tip.machine"), Component.translatable("gtocore.monitor.tip.machine.channels"),
                    Component.translatable("gtocore.monitor.tip.key.place_machine")),
            Component.translatable("gtocore.monitor.control.requirement.machine"), List.of("machine.running", "machine.switch"),
            List.of(Component.translatable("gtocore.monitor.control.how.machine.read"), Component.translatable("gtocore.monitor.control.how.machine.switch")));
    public static final MonitorPanelType REDSTONE = MonitorPanelTypes.register("redstone", MonitorDataSources.REDSTONE, "gtocore.monitor.group.redstone_panel", MonitorRegistries.REDSTONE_PANEL, true,
            MonitorPartItem.dataTips(Component.translatable("gtocore.monitor.tip.redstone"), Component.translatable("gtocore.monitor.tip.redstone.channels"),
                    Component.translatable("gtocore.monitor.tip.key.place_redstone"), Component.translatable("gtocore.monitor.tip.redstone.dust"),
                    Component.translatable("gtocore.monitor.tip.redstone.switch"), Component.translatable("gtocore.monitor.tip.redstone.slider")),
            Component.translatable("gtocore.monitor.control.requirement.redstone"), List.of("redstone.signal", "redstone.output_level"),
            List.of(Component.translatable("gtocore.monitor.control.how.redstone.read"), Component.translatable("gtocore.monitor.control.how.redstone.write")));

    public static final MonitorPanelType SHARE = MonitorPanelTypes.registerShare("share", MonitorRegistries.SHARE_PANEL,
            TipSpec.builder()
                    .summary(MonitorPartItem.SERIES)
                    .summary(Component.translatable("gtocore.monitor.tip.share"))
                    .lines(l -> l.feature(Component.translatable("gtocore.monitor.tip.share.members"))
                            .feature(Component.translatable("gtocore.monitor.tip.share.dimension"))
                            .feature(Component.translatable("gtocore.monitor.tip.share.hud"))
                            .rightClick(Tips.keyAction(Component.translatable("gtocore.monitor.tip.key.control"), Component.translatable("gtocore.monitor.tip.share.open")))
                            .rightClick(Tips.keyAction(Component.translatable("gtocore.monitor.tip.key.wrench"), Component.translatable("gtocore.monitor.tip.remove_panel"))))
                    .build(),
            List.of(Component.translatable("gtocore.monitor.control.how.share.list"), Component.translatable("gtocore.monitor.control.how.share.hud"),
                    Component.translatable("gtocore.monitor.control.how.share.unloaded")));

    public static final MonitorPanelType DOWNLOAD = MonitorPanelTypes.registerDownload("download", MonitorPersonalSources.PLAYER, "gtocore.monitor.group.player_download",
            MonitorRegistries.DOWNLOAD_PANEL,
            TipSpec.builder()
                    .summary(MonitorPartItem.SERIES)
                    .summary(Component.translatable("gtocore.monitor.tip.download"))
                    .lines(l -> l.feature(Component.translatable("gtocore.monitor.tip.download.bind"))
                            .feature(Component.translatable("gtocore.monitor.tip.share.dimension"))
                            .feature(Component.translatable("gtocore.monitor.tip.data.link"))
                            .rightClick(Tips.keyAction(Component.translatable("gtocore.monitor.tip.key.control"), Component.translatable("gtocore.monitor.tip.download.open")))
                            .rightClick(Tips.keyAction(Component.translatable("gtocore.monitor.tip.key.wrench"), Component.translatable("gtocore.monitor.tip.remove_panel"))))
                    .build(),
            List.of(Component.translatable("gtocore.monitor.control.how.download.read"), Component.translatable("gtocore.monitor.control.how.download.operate"),
                    Component.translatable("gtocore.monitor.control.how.download.limits")));

    private MonitorPanels() {}

    public static void init() {}
}
