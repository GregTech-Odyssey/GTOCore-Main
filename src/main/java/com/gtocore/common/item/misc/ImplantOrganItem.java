package com.gtocore.common.item.misc;

import com.gtolib.implant.AutoRestock;
import com.gtolib.implant.BeamEmitter;
import com.gtolib.implant.ChargeCannon;
import com.gtolib.implant.CurrentVision;
import com.gtolib.implant.FaultVision;
import com.gtolib.implant.Implants;
import com.gtolib.implant.RemoteTouch;
import com.gtolib.implant.StockSense;
import com.gtolib.implant.TeamMarkers;
import com.gtolib.implant.Tether;
import com.gtolib.implant.ToolArm;
import com.gtolib.monitor.MonitorPartItem;

import com.gregtechceu.gtceu.api.tooltip.StatKind;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.api.tooltip.TipTheme;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class ImplantOrganItem extends OrganItemBase {

    private static final Component INTERACT_KEY = Component.keybind("key.gtocore.hud_interact");
    private static final Component LAYOUT_KEY = Component.keybind("key.gtocore.hud_layout");
    private static final Component ACTION_KEY = Component.keybind("key.gtocore.implant_action");
    private static final Component WHEEL_KEY = Component.keybind("key.gtocore.implant_wheel");
    private static final StatKind IDLE = new StatKind("implant_idle", "gtocore.implant.stat.idle", TipTheme.ENERGY_AMPERAGE, null);
    private static final StatKind ACTIVE = new StatKind("implant_active", "gtocore.implant.stat.active", TipTheme.ENERGY_AMPERAGE, null);
    private static final StatKind USE = new StatKind("implant_use", "gtocore.implant.stat.use", TipTheme.ENERGY_STORAGE, null);
    private static final int TICKS_PER_SECOND = 20;

    private final TipSpec tips;

    private ImplantOrganItem(Properties properties, TipSpec tips) {
        super(properties, OrganType.IMPLANT);
        this.tips = tips;
    }

    public static ImplantOrganItem mind(Properties properties) {
        return new ImplantOrganItem(properties, TipSpec.builder()
                .summary(MonitorPartItem.SERIES)
                .summary(Component.translatable("gtocore.implant.mind.desc"))
                .lines(l -> l.feature(Component.translatable("gtocore.implant.slot"))
                        .feature(Component.translatable("gtocore.implant.mind.grant"))
                        .feature(Component.translatable("gtocore.implant.mind.sources"))
                        .control(Tips.keyAction(INTERACT_KEY, Component.translatable("gtocore.implant.mind.interact")))
                        .control(Tips.keyAction(LAYOUT_KEY, Component.translatable("gtocore.implant.mind.layout"))))
                .build());
    }

    public static ImplantOrganItem drone(Properties properties) {
        return new ImplantOrganItem(properties, TipSpec.builder()
                .summary(MonitorPartItem.SERIES)
                .summary(Component.translatable("gtocore.implant.drone.desc"))
                .lines(l -> l.feature(Component.translatable("gtocore.implant.slot"))
                        .feature(Component.translatable("gtocore.implant.drone.panels"))
                        .requirement(Component.translatable("gtocore.implant.drone.requires_mind"))
                        .control(Tips.keyAction(INTERACT_KEY, Component.translatable("gtocore.implant.mind.interact")))
                        .control(Tips.keyAction(LAYOUT_KEY, Component.translatable("gtocore.implant.mind.layout"))))
                .build());
    }

    public static ImplantOrganItem faultVision(Properties properties) {
        return powered(properties, Implants.FAULT_VISION, Component.translatable("gtocore.implant.fault_vision.desc"),
                l -> l.feature(Component.translatable("gtocore.implant.fault_vision.outline", FaultVision.RADIUS))
                        .feature(Component.translatable("gtocore.implant.fault_vision.hud"))
                        .requirement(Component.translatable("gtocore.implant.requires_mind_hud")));
    }

    public static ImplantOrganItem stockSense(Properties properties) {
        return powered(properties, Implants.STOCK_SENSE, Component.translatable("gtocore.implant.stock_sense.desc"),
                l -> l.feature(Component.translatable("gtocore.implant.stock_sense.hud", StockSense.REACH))
                        .requirement(Component.translatable("gtocore.implant.requires_terminal"))
                        .requirement(Component.translatable("gtocore.implant.requires_mind_hud")));
    }

    public static ImplantOrganItem currentVision(Properties properties) {
        return powered(properties, Implants.CURRENT_VISION, Component.translatable("gtocore.implant.current_vision.desc"),
                l -> l.feature(Component.translatable("gtocore.implant.current_vision.label", CurrentVision.RADIUS))
                        .feature(Component.translatable("gtocore.implant.current_vision.color")));
    }

    public static ImplantOrganItem remoteTouch(Properties properties) {
        return powered(properties, Implants.REMOTE_TOUCH, Component.translatable("gtocore.implant.remote_touch.desc"),
                l -> l.feature(Component.translatable("gtocore.implant.remote_touch.lock", RemoteTouch.RANGE))
                        .feature(Component.translatable("gtocore.implant.remote_touch.hud"))
                        .requirement(Component.translatable("gtocore.implant.requires_mind_hud"))
                        .control(Tips.keyAction(INTERACT_KEY, Component.translatable("gtocore.implant.remote_touch.interact"))));
    }

    public static ImplantOrganItem teamMarker(Properties properties) {
        return powered(properties, Implants.TEAM_MARKER, Component.translatable("gtocore.implant.team_marker.desc"),
                l -> l.feature(Component.translatable("gtocore.implant.team_marker.rule", TeamMarkers.RANGE, TeamMarkers.LIFE / TICKS_PER_SECOND, TeamMarkers.PER_PLAYER))
                        .feature(Component.translatable("gtocore.implant.team_marker.team"))
                        .control(Tips.keyAction(WHEEL_KEY, Component.translatable("gtocore.implant.control.wheel")))
                        .control(Tips.keyAction(ACTION_KEY, Component.translatable("gtocore.implant.team_marker.act"))));
    }

    public static ImplantOrganItem restock(Properties properties) {
        return powered(properties, Implants.RESTOCK, Component.translatable("gtocore.implant.restock.desc"),
                l -> l.feature(Component.translatable("gtocore.implant.restock.rule", AutoRestock.PERIOD / TICKS_PER_SECOND))
                        .feature(Component.translatable("gtocore.implant.restock.cost"))
                        .requirement(Component.translatable("gtocore.implant.requires_terminal")));
    }

    public static ImplantOrganItem tool(Properties properties) {
        return powered(properties, Implants.TOOL, Component.translatable("gtocore.implant.tool.desc"),
                l -> l.feature(Component.translatable("gtocore.implant.tool.kinds", ToolArm.RANGE))
                        .control(Tips.keyAction(WHEEL_KEY, Component.translatable("gtocore.implant.control.wheel")))
                        .control(Tips.keyAction(ACTION_KEY, Component.translatable("gtocore.implant.tool.act"))));
    }

    public static ImplantOrganItem beam(Properties properties) {
        return powered(properties, Implants.BEAM, Component.translatable("gtocore.implant.beam.desc"),
                l -> l.feature(Component.translatable("gtocore.implant.beam.rule", BeamEmitter.RANGE, TICKS_PER_SECOND / BeamEmitter.HIT_PERIOD, (int) BeamEmitter.DAMAGE))
                        .control(Tips.keyAction(WHEEL_KEY, Component.translatable("gtocore.implant.control.wheel")))
                        .control(Tips.keyAction(ACTION_KEY, Component.translatable("gtocore.implant.beam.act"))));
    }

    public static ImplantOrganItem cannon(Properties properties) {
        return powered(properties, Implants.CANNON, Component.translatable("gtocore.implant.cannon.desc"),
                l -> l.feature(Component.translatable("gtocore.implant.cannon.rule", ChargeCannon.RANGE, ChargeCannon.FULL_CHARGE / TICKS_PER_SECOND))
                        .feature(Component.translatable("gtocore.implant.cannon.damage", (int) ChargeCannon.MIN_DAMAGE, (int) ChargeCannon.MAX_DAMAGE,
                                (int) ChargeCannon.MIN_RADIUS, (int) ChargeCannon.MAX_RADIUS))
                        .feature(Component.translatable("gtocore.implant.cannon.cost", ChargeCannon.MIN_COST_DIVISOR))
                        .control(Tips.keyAction(WHEEL_KEY, Component.translatable("gtocore.implant.control.wheel")))
                        .control(Tips.keyAction(ACTION_KEY, Component.translatable("gtocore.implant.cannon.act"))));
    }

    public static ImplantOrganItem tether(Properties properties) {
        return powered(properties, Implants.TETHER, Component.translatable("gtocore.implant.tether.desc"),
                l -> l.feature(Component.translatable("gtocore.implant.tether.rule", Tether.RANGE, Tether.MAX_TICKS / TICKS_PER_SECOND))
                        .feature(Component.translatable("gtocore.implant.tether.limit"))
                        .control(Tips.keyAction(WHEEL_KEY, Component.translatable("gtocore.implant.control.wheel")))
                        .control(Tips.keyAction(ACTION_KEY, Component.translatable("gtocore.implant.tether.act")))
                        .leftClick(Component.translatable("gtocore.implant.tether.fling")));
    }

    private static ImplantOrganItem powered(Properties properties, Implants.Spec spec, Component desc, Consumer<TipSpec.Lines> body) {
        return new ImplantOrganItem(properties, TipSpec.builder()
                .summary(desc)
                .stats(s -> {
                    if (spec.idle() > 0) s.power(IDLE, spec.idle(), spec.tier());
                    if (spec.active() > 0) s.power(ACTIVE, spec.active(), spec.tier());
                    if (spec.use() > 0) s.energy(USE, spec.use(), spec.tier());
                })
                .lines(l -> {
                    l.feature(Component.translatable("gtocore.implant.slot"));
                    body.accept(l);
                    l.feature(Component.translatable("gtocore.implant.power.supply"));
                })
                .build());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.addAll(tips.get());
    }
}
