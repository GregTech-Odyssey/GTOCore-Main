package com.gtocore.common.machine.electric.beam;

import com.gtolib.api.beam.BeamNode;
import com.gtolib.api.beam.BeamPassContext;
import com.gtolib.api.beam.IBeamOperator;
import com.gtolib.api.machine.SimpleNoEnergyMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;

import net.minecraft.world.phys.Vec3;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;

@Getter
public class BeamRedirector extends SimpleNoEnergyMachine implements IBeamOperator {

    @SaveToDisk
    @SyncToClient
    private float thetaRad;
    @SaveToDisk
    @SyncToClient
    private float phiRad;

    public BeamRedirector(MetaMachineBlockEntity holder) {
        super(holder, 0, t -> 0);
    }

    @Override
    public BeamNode operate(BeamPassContext context, BeamNode lastNode, Vec3 exactPos) {
        var properties = lastNode.propertiesSnapshot().copy();
        Vec3 direction = BeamMachineUtils.direction(thetaRad, phiRad);
        properties.vx = direction.x;
        properties.vy = direction.y;
        properties.vz = direction.z;
        properties.intensity = (long) (properties.intensity * context.decay());
        return new BeamNode(exactPos.x, exactPos.y, exactPos.z, properties);
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
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
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
