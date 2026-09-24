package com.gtocore.common.machine.noenergy;

import com.gtocore.integration.jade.provider.AEGridProvider;

import com.gtolib.api.ae2.IExpandedGrid;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.parts.AEBasePart;

import com.glodblock.github.extendedae.client.render.EAEHighlightHandler;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

@DataGeneratorScanned
public final class PerformanceMonitorMachine extends MetaMachine implements IFancyUIMachine {

    @RegisterLanguage(cn = "ME网络", en = "ME Grid")
    private static final String GRID = "gtocore.performance_monitor.grid";

    private static final Pattern PATTERN = Pattern.compile(", ");

    private List<Component> textListCache;
    private boolean grid = false;

    public PerformanceMonitorMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    private void handleDisplayClick(String componentData, ClickData clickData) {
        if (componentData.equals("grid")) {
            grid = !grid;
            textListCache = null;
        } else if (clickData.isRemote) {
            if (componentData.isEmpty()) return;
            String[] parts = PATTERN.split(componentData);
            if (parts.length == 4) {
                BlockPos pos = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                EAEHighlightHandler.highlight(pos, GTODimensions.getDimensionKey(RLUtils.parse(parts[3])), System.currentTimeMillis() + 15000);
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        return MachineDisplay.page(this, this::addDisplayText, this::handleDisplayClick);
    }

    private void addDisplayText(@NotNull List<Component> textList) {
        if (isRemote()) return;
        textList.add(Component.translatable("gtocore.digital_miner.show_range").append(ComponentPanelWidget.withButton(Component.translatable(grid ? GRID : "config.gtceu.option.machines"), "grid")));
        if (grid) {
            AEGridProvider.OBSERVE = true;
            if (textListCache == null || holder.getOffsetTimer() % 80 == 0) {
                textListCache = new ArrayList<>();
                Map<IExpandedGrid, Long> sortedMap = new TreeMap<>(Comparator.comparing(IExpandedGrid::getLatency).reversed());
                sortedMap.putAll(IExpandedGrid.PERFORMANCE_MAP);
                IExpandedGrid.PERFORMANCE_MAP.clear();
                for (Map.Entry<IExpandedGrid, Long> entry : sortedMap.entrySet()) {
                    IExpandedGrid key = entry.getKey();
                    Object o;
                    String pos = "";
                    Level level = null;
                    if (key.getPivot() != null) {
                        level = key.getPivot().getLevel();
                        if ((o = key.getPivot().getOwner()) != null) {
                            switch (o) {
                                case AEBasePart part -> {
                                    var host = part.getHost();
                                    if (host != null) {
                                        pos = host.getBlockEntity().getBlockPos().toShortString();
                                    }
                                }
                                case BlockEntity be -> pos = be.getBlockPos().toShortString();
                                case MetaMachine machine -> pos = machine.getPos().toShortString();
                                default -> {}
                            }
                        }
                    }
                    textListCache.add(Component.translatable(key.toString()).append(" ")
                            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("recipe.condition.dimension.tooltip", level == null ? " " : level.dimension().location()).append(" [").append(pos).append("] "))))
                            .append(Component.translatable(AEGridProvider.LATENCY, entry.getValue()).append(" μs"))
                            .append(ComponentPanelWidget.withButton(Component.literal(" [ ] "), pos + ", " + (level == null ? "" : level.dimension().location()))));
                }
            }
            textList.addAll(textListCache);
        }
    }
}
