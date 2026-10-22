package com.gtocore.common.machine.electric.beam;

import com.gtolib.api.beam.*;
import com.gtolib.api.machine.SimpleNoEnergyMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;

import net.minecraft.world.phys.Vec3;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/** Two native output ports; the level manager owns all resulting branches. */
@Getter
public class AdjustableSemiReflector extends SimpleNoEnergyMachine implements IBeamOperator {

    @SaveToDisk(defaultValue = "0.5")
    private float reflectivity = 0.5F;
    @SaveToDisk(defaultValue = "0")
    private float thetaRad;
    @SaveToDisk(defaultValue = "0")
    private float phiRad;

    public AdjustableSemiReflector(MetaMachineBlockEntity holder) {
        super(holder, 0, t -> 0);
    }

    private Vec3 normal() {
        return BeamMachineUtils.direction(thetaRad, phiRad);
    }

    @Override
    public void route(BeamRay incoming, Vec3 hit, BeamRoutes outputs) {
        outputs.add(BeamRoute.reflect(incoming, hit, normal()));
        outputs.straight(incoming, hit);
    }

    @Override
    public void evaluate(BeamBatch batch) {
        for (int i = 0; i < batch.size(); i++) {
            var pass = batch.pass(i);
            pass.forward(0, reflectivity);
            pass.forward(1, 1.0D - reflectivity);
        }
    }

    public void setReflectivity(float value) {
        reflectivity = Float.isFinite(value) ? Math.clamp(value, 0.0F, 1.0F) : 0.5F;
        if (getLevel() != null) requestRayBeamUpdate(getLevel(), getPos());
    }

    public void setThetaRad(float value) {
        thetaRad = BeamMachineUtils.normalizeAngle(value);
        pathChanged();
    }

    public void setPhiRad(float value) {
        phiRad = BeamMachineUtils.clampPitch(value);
        pathChanged();
    }

    private void pathChanged() {
        if (getLevel() != null) requestRayBeamPathUpdate(getLevel(), getPos());
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel panel) {
        super.attachConfigurators(panel);
        panel.attachConfigurators(BeamConfigurator.reflectivity(this::getReflectivity, this::setReflectivity));
        panel.attachConfigurators(BeamConfigurator.normal(this::getThetaRad, this::setThetaRad, this::getPhiRad, this::setPhiRad));
    }

    @Override
    public Widget createUIWidget() {
        return BeamMachineUtils.addReflectivityControl(super.createUIWidget(), this::getReflectivity, this::setReflectivity);
    }

    @Override
    public boolean hasAutoOutputFluid() {
        return false;
    }

    @Override
    public boolean hasAutoOutputItem() {
        return false;
    }
}
