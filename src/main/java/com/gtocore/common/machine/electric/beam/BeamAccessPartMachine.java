package com.gtocore.common.machine.electric.beam;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.beam.BeamManager;
import com.gtolib.api.beam.BeamNode;
import com.gtolib.api.beam.BeamPassContext;
import com.gtolib.api.beam.BeamPassKey;
import com.gtolib.api.beam.BeamProperties;
import com.gtolib.api.beam.IBeamOperator;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@DataGeneratorScanned
public class BeamAccessPartMachine extends MultiblockPartMachine implements IBeamOperator {

    public static final int AVERAGE_WINDOW_TICKS = 20;

    private final Object2ObjectOpenHashMap<BeamPassKey, ActiveBeam> activeBeams = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<BeamKey, IntensityHistory> intensityHistories = new Object2ObjectOpenHashMap<>();
    /** Packed as [wavelength, Float.floatToIntBits(polarization), ...]. */
    @SyncToClient
    private int[] receivedBeamProperties = new int[0];
    @SyncToClient
    private long[] receivedBeamIntensities = new long[0];
    private TickableSubscription tickSubscription;
    private long sampledTick = Long.MIN_VALUE;
    private boolean activeBeamsDirty = true;

    public BeamAccessPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public BeamNode operate(BeamPassContext context, BeamNode lastNode, Vec3 exactPos) {
        if (context.beamId() > 0) {
            var properties = lastNode.propertiesSnapshot().copy();
            properties.intensity = BeamMachineUtils.scaled(properties.intensity, context.decay());
            activeBeams.put(context.key(), new ActiveBeam(context, properties));
            activeBeamsDirty = true;
        }
        return new BeamNode(exactPos.x, exactPos.y, exactPos.z, BeamProperties.NO_INTENSITY);
    }

    @Override
    public void onRayBeamPassRemoved(BeamPassContext context) {
        if (activeBeams.remove(context.key()) != null) activeBeamsDirty = true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) tickSubscription = subscribeServerTick(tickSubscription, this::sampleCurrentTick, 5);
    }

    @Override
    public void onUnload() {
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
        activeBeams.clear();
        intensityHistories.clear();
        sampledTick = Long.MIN_VALUE;
        activeBeamsDirty = true;
        super.onUnload();
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 125);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(GuiTextures.DISPLAY)
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(170)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private void addDisplayText(List<Component> textList) {
        textList.add(Component.translatable("gtocore.machine.ray_beam_part.received_beams").withStyle(ChatFormatting.GOLD));
        int beamCount = Math.min(receivedBeamProperties.length / 2, receivedBeamIntensities.length);
        if (beamCount == 0) {
            textList.add(Component.translatable("gtocore.machine.ray_beam_part.no_received_beam").withStyle(ChatFormatting.GRAY));
            return;
        }
        for (int i = 0; i < beamCount; i++) {
            var key = new BeamKey(receivedBeamProperties[i * 2], Float.intBitsToFloat(receivedBeamProperties[i * 2 + 1]));
            textList.add(Component.translatable("gtocore.machine.ray_beam_part.received_beam",
                    key.waveLength(),
                    String.format(Locale.ROOT, "%.2f", Math.toDegrees(key.polarization())),
                    FormattingUtil.formatNumbers(receivedBeamIntensities[i]))
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    /** Adds this receiver's last-second samples to arrays ordered from oldest to newest. */
    public void collectRecentIntensitySamples(Map<BeamKey, long[]> samples) {
        sampleCurrentTick();
        var level = getLevel();
        if (level == null || level.isClientSide()) return;

        long now = level.getGameTime();
        long firstTick = now - AVERAGE_WINDOW_TICKS + 1L;
        for (var iter = intensityHistories.object2ObjectEntrySet().fastIterator(); iter.hasNext();) {
            var entry = iter.next();
            long[] target = samples.computeIfAbsent(entry.getKey(), ignored -> new long[AVERAGE_WINDOW_TICKS]);
            var history = entry.getValue();
            for (int i = 0; i < AVERAGE_WINDOW_TICKS; i++) {
                target[i] = saturatedAdd(target[i], history.get(firstTick + i));
            }
        }
    }

    private void sampleCurrentTick() {
        var level = getLevel();
        if (level == null || level.isClientSide()) return;
        long now = level.getGameTime();
        if (sampledTick == now && !activeBeamsDirty) return;

        var manager = BeamManager.getIfPresent(level);
        if (manager != null) {
            activeBeams.values().removeIf(active -> !manager.isPassActive(active.context));
        }

        var currentIntensities = new Object2ObjectOpenHashMap<BeamKey, Long>();
        for (var active : activeBeams.values()) {
            var properties = active.properties;
            if (properties.intensity <= 0) continue;
            currentIntensities.merge(BeamKey.of(properties), properties.intensity, BeamAccessPartMachine::saturatedAdd);
        }

        boolean hadHistories = !intensityHistories.isEmpty();
        for (var iter = intensityHistories.object2ObjectEntrySet().fastIterator(); iter.hasNext();) {
            var entry = iter.next();
            entry.getValue().set(now, currentIntensities.getOrDefault(entry.getKey(), 0L));
        }
        for (var iter = currentIntensities.object2ObjectEntrySet().fastIterator(); iter.hasNext();) {
            var entry = iter.next();
            intensityHistories.computeIfAbsent(entry.getKey(), ignored -> new IntensityHistory()).set(now, entry.getValue());
        }
        intensityHistories.values().removeIf(history -> !history.hasRecentSample(now));
        updateReceivedBeamDisplay(currentIntensities);

        sampledTick = now;
        activeBeamsDirty = false;
        if (hadHistories || !intensityHistories.isEmpty()) notifyControllers();
    }

    private void updateReceivedBeamDisplay(Object2ObjectOpenHashMap<BeamKey, Long> currentIntensities) {
        var entries = new ArrayList<DisplayBeam>(currentIntensities.size());
        for (var iter = currentIntensities.object2ObjectEntrySet().fastIterator(); iter.hasNext();) {
            var entry = iter.next();
            if (entry.getValue() > 0) entries.add(new DisplayBeam(entry.getKey(), entry.getValue()));
        }
        entries.sort(Comparator.comparingInt((DisplayBeam entry) -> entry.key.waveLength())
                .thenComparingDouble(entry -> entry.key.polarization()));
        var properties = new int[entries.size() * 2];
        var intensities = new long[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            properties[i * 2] = entry.key.waveLength();
            properties[i * 2 + 1] = Float.floatToIntBits(entry.key.polarization());
            intensities[i] = entry.intensity;
        }
        if (!Arrays.equals(receivedBeamProperties, properties) || !Arrays.equals(receivedBeamIntensities, intensities)) {
            receivedBeamProperties = properties;
            receivedBeamIntensities = intensities;
            requestSync();
        }
    }

    private void notifyControllers() {
        for (var controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine machine) machine.getRecipeLogic().updateTickSubscription();
        }
    }

    private static long saturatedAdd(long first, long second) {
        if (first <= 0) return Math.max(0L, second);
        if (second <= 0) return first;
        return first > Long.MAX_VALUE - second ? Long.MAX_VALUE : first + second;
    }

    private record ActiveBeam(BeamPassContext context, BeamProperties properties) {}

    public record BeamKey(int waveLength, float polarization) {

        private static BeamKey of(BeamProperties properties) {
            float polarization = properties.polarization == 0.0F ? 0.0F : properties.polarization;
            return new BeamKey(properties.waveLength, polarization);
        }
    }

    private record DisplayBeam(BeamKey key, long intensity) {}

    private static final class IntensityHistory {

        private final long[] ticks = new long[AVERAGE_WINDOW_TICKS];
        private final long[] intensities = new long[AVERAGE_WINDOW_TICKS];

        private IntensityHistory() {
            Arrays.fill(ticks, Long.MIN_VALUE);
        }

        private void set(long tick, long intensity) {
            int index = Math.floorMod(tick / 5, AVERAGE_WINDOW_TICKS);
            ticks[index] = tick / 5;
            intensities[index] = intensity;
        }

        private long get(long tick) {
            int index = Math.floorMod(tick / 5, AVERAGE_WINDOW_TICKS);
            return ticks[index] == tick / 5 ? intensities[index] : 0L;
        }

        private boolean hasRecentSample(long now) {
            long oldest = now / 5 - AVERAGE_WINDOW_TICKS + 1L;
            for (int i = 0; i < ticks.length; i++) {
                if (ticks[i] >= oldest && ticks[i] <= now / 5 && intensities[i] > 0) return true;
            }
            return false;
        }
    }

    //
    // "gtocore.machine.ray_beam_part.no_received_beam": "未接收到光束",
    // "gtocore.machine.ray_beam_part.received_beam": "波长：%s nm，偏振：%s°，强度：%s a.u.",
    // "gtocore.machine.ray_beam_part.received_beams": "当前接收的光束",
    @RegisterLanguage(cn = "未接收到光束", en = "No received beam")
    public static final String NO_RECEIVED_BEAM = "gtocore.machine.ray_beam_part.no_received_beam";
    @RegisterLanguage(cn = "波长：%s nm，偏振：%s°，强度：%s a.u.", en = "Wavelength: %s nm, Polarization: %s°, Intensity: %s a.u.")
    public static final String RECEIVED_BEAM = "gtocore.machine.ray_beam_part.received_beam";
    @RegisterLanguage(cn = "当前接收的光束", en = "Current received beams")
    public static final String RECEIVED_BEAMS = "gtocore.machine.ray_beam_part.received_beams";
}
