package com.gtocore.common.wireless.energy;

import com.gtocore.api.wireless.energy.GridScheduler;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;

import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@DataGeneratorScanned
public final class PortPriorityUI {

    @RegisterLanguage(cn = "电网优先级", en = "Grid Priority")
    private static final String LABEL = "gtocore.wireless_energy.priority";
    @RegisterLanguage(cn = "能源库存取电时，优先级高的设备先被服务；同一优先级的设备轮流服务", en = "Higher-priority devices are served first when storing or drawing energy; devices of equal priority take turns")
    private static final String TIP = "gtocore.wireless_energy.priority.tooltip";
    @RegisterLanguage(cn = "最低", en = "Lowest")
    private static final String LOWEST = "gtocore.wireless_energy.priority.0";
    @RegisterLanguage(cn = "低", en = "Low")
    private static final String LOW = "gtocore.wireless_energy.priority.1";
    @RegisterLanguage(cn = "普通", en = "Normal")
    private static final String NORMAL = "gtocore.wireless_energy.priority.2";
    @RegisterLanguage(cn = "高", en = "High")
    private static final String HIGH = "gtocore.wireless_energy.priority.3";
    @RegisterLanguage(cn = "最高", en = "Highest")
    private static final String HIGHEST = "gtocore.wireless_energy.priority.4";

    @RegisterLanguage(cn = "仅在无线模式下生效", en = "Only applies in wireless mode")
    public static final String WIRELESS_ONLY = "gtocore.wireless_energy.priority.wireless_only";

    private static final String[] OPTIONS = { LOWEST, LOW, NORMAL, HIGH, HIGHEST };

    private PortPriorityUI() {}

    public static Component option(int priority) {
        return Component.translatable(OPTIONS[GridScheduler.clampPriority(priority)]);
    }

    public static UIElement addTo(ControlPanel controls, IntSupplier getter, IntConsumer setter) {
        return controls.addSegments(LABEL, OPTIONS.length, PortPriorityUI::option, getter, setter, TIP);
    }

    public static UIElement row(IntSupplier getter, IntConsumer setter) {
        return Form.segmentRow(LABEL, OPTIONS.length, PortPriorityUI::option, getter, setter, TIP);
    }
}
