package com.gtocore.common.machine.electric.beam;

import com.gtolib.api.beam.BeamBatch;
import com.gtolib.api.beam.BeamRay;
import com.gtolib.api.beam.BeamRoute;
import com.gtolib.api.beam.BeamRoutes;
import com.gtolib.api.beam.IBeamOperator;
import com.gtolib.api.machine.SimpleNoEnergyMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;

import net.minecraft.world.phys.Vec3;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class BeamRedirector extends SimpleNoEnergyMachine implements IBeamOperator {

    @SaveToDisk(defaultValue = "0")
    private float thetaRad;
    @SaveToDisk(defaultValue = "0")
    private float phiRad;

    public BeamRedirector(MetaMachineBlockEntity holder) {
        super(holder, 0, t -> 0);
    }

    @Override
    public void route(BeamRay incoming, Vec3 hit, BeamRoutes outputs) {
        outputs.add(BeamRoute.transmit(incoming, hit, BeamMachineUtils.direction(thetaRad, phiRad), incoming.wavelength(), incoming.refractiveIndex()));
    }

    @Override
    public void evaluate(BeamBatch batch) {
        for (int i = 0; i < batch.size(); i++) batch.pass(i).forward(0, 1);
    }

    public void setThetaRad(float thetaRad) {
        this.thetaRad = BeamMachineUtils.normalizeAngle(thetaRad);
        requestUpdate();
    }

    public void setPhiRad(float phiRad) {
        this.phiRad = BeamMachineUtils.clampPitch(phiRad);
        requestUpdate();
    }

    @Override
    public boolean hasAutoOutputFluid() {
        return false;
    }

    @Override
    public boolean hasAutoOutputItem() {
        return false;
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(BeamConfigurator.angles(this::getThetaRad, this::setThetaRad, this::getPhiRad, this::setPhiRad));
    }

    @Override
    public Widget createUIWidget() {
        return BeamMachineUtils.addAngleControls(super.createUIWidget(),
                this::getThetaRad, this::setThetaRad, this::getPhiRad, this::setPhiRad);
    }

    private void requestUpdate() {
        if (getLevel() != null) requestRayBeamPathUpdate(getLevel(), getPos());
    }
}
