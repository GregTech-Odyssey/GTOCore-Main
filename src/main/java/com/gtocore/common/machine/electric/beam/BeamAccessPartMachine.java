package com.gtocore.common.machine.electric.beam;

import com.gtocore.common.data.GTOTickTimeMonitors;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.beam.*;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.List;

@DataGeneratorScanned
public class BeamAccessPartMachine extends MultiblockPartMachine implements IBeamOperator {

    private final BeamReceiverHistory history = new BeamReceiverHistory();
    private TickableSubscription tickSubscription;
    private final TickTimeMonitor beamMonitor = holder.monitorTick(GTOTickTimeMonitors.BEAM, this::sampleCurrentTick);

    public BeamAccessPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean batchInputs() {
        return true;
    }

    @Override
    public Vec3 receiverDirection() {
        return Vec3.atLowerCornerOf(getFrontFacing().getOpposite().getNormal());
    }

    @Override
    public void route(BeamRay incoming, Vec3 hit, BeamRoutes outputs) {}

    @Override
    public void evaluate(BeamBatch batch) {}

    @Override
    public void commitBeamInputs(BeamBatch batch) {
        history.begin(getLevel().getGameTime());
        for (int i = 0; i < batch.size(); i++) {
            var pass = batch.pass(i);
            history.add(pass.ray().wavelength(), pass.detectedInput());
        }
        history.finish();
        notifyControllers();
    }

    @Override
    public void clearBeamInputs() {
        if (getLevel() != null) history.clear(getLevel().getGameTime());
        notifyControllers();
    }

    public double averageReceivedPower(int min, int max, double analyzerAngle) {
        return history.average(getLevel().getGameTime(), min, max, analyzerAngle);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) tickSubscription = subscribeServerTick(tickSubscription, beamMonitor, 5);
    }

    @Override
    public void onUnload() {
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
        clearBeamInputs();
        super.onUnload();
    }

    @Override
    public Widget createUIWidget() {
        return MachineDisplay.page(this, this::addDisplayText);
    }

    private void addDisplayText(List<Component> lines) {
        lines.add(Component.translatable(RECEIVED_BEAMS).withStyle(ChatFormatting.GOLD));
        boolean received = false;
        for (var iterator = history.channels().int2ObjectEntrySet().fastIterator(); iterator.hasNext();) {
            var entry = iterator.next();
            var signal = entry.getValue().current;
            if (signal.power <= 0) continue;
            received = true;
            lines.add(Component.translatable(RECEIVED_BEAM, entry.getIntKey(),
                    Math.round(Math.toDegrees(signal.angle()) * 100) / 100.0,
                    Math.round(signal.linearDegree() * 10000) / 100.0,
                    FormattingUtil.formatNumbers(signal.integerPower())).withStyle(ChatFormatting.AQUA));
        }
        if (!received) lines.add(Component.translatable(NO_RECEIVED_BEAM).withStyle(ChatFormatting.GRAY));
    }

    private void sampleCurrentTick() {
        boolean recent = history.hasRecentPower();
        history.advance(getLevel().getGameTime());
        if (recent) notifyControllers();
    }

    private void notifyControllers() {
        for (var controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine machine) machine.getRecipeLogic().updateTickSubscription();
        }
    }

    //
    // "gtocore.machine.ray_beam_part.no_received_beam": "未接收到光束",
    // "gtocore.machine.ray_beam_part.received_beam": "波长：%s nm，偏振：%s°，强度：%s a.u.",
    // "gtocore.machine.ray_beam_part.received_beams": "当前接收的光束",
    @RegisterLanguage(cn = "未接收到光束", en = "No received beam")
    public static final String NO_RECEIVED_BEAM = "gtocore.machine.ray_beam_part.no_received_beam";
    @RegisterLanguage(cn = "波长：%s nm，偏振：%s°（线偏振度：%s%%），强度：%s a.u.", en = "Wavelength: %s nm, Polarization: %s° (linear polarization: %s%%), Intensity: %s a.u.")
    public static final String RECEIVED_BEAM = "gtocore.machine.ray_beam_part.received_beam";
    @RegisterLanguage(cn = "当前接收的光束", en = "Current received beams")
    public static final String RECEIVED_BEAMS = "gtocore.machine.ray_beam_part.received_beams";
}
