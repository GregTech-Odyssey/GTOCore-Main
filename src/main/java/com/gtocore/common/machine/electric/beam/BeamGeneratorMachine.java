package com.gtocore.common.machine.electric.beam;

import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.beam.Beam;
import com.gtolib.api.beam.BeamManager;
import com.gtolib.api.beam.BeamNode;
import com.gtolib.api.beam.BeamProperties;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.world.phys.Vec3;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class BeamGeneratorMachine extends SimpleTieredMachine {

    @SaveToDisk
    @SyncToClient
    private long currentIntensity;
    @SaveToDisk
    @SyncToClient
    private int currentWaveLength;
    @SaveToDisk
    @SyncToClient
    private int ocLevel;
    @SaveToDisk
    @SyncToClient
    private float thetaRad;
    @SaveToDisk
    @SyncToClient
    private float phiRad;

    private TickableSubscription tickSubscription;
    private long beamId = -1;
    private long emittedIntensity = -1;
    private int emittedWaveLength = -1;
    private int emittedThetaBits;
    private int emittedPhiBits;

    public BeamGeneratorMachine(MetaMachineBlockEntity holder, int tier) {
        super(holder, tier, t -> 16000);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) tickSubscription = subscribeServerTick(tickSubscription, this::tick, 5);
    }

    @Override
    public void onUnload() {
        removeBeam();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
        super.onUnload();
    }

    @Override
    public void onMachineRemoved() {
        removeBeam();
        super.onMachineRemoved();
    }

    private void tick() {
        if (getRecipeLogic().isWorking() && currentIntensity > 0 && currentWaveLength > 0) {
            refreshBeam();
        } else {
            removeBeam();
        }
    }

    @Override
    public void beforeWorking(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        super.beforeWorking(unit, recipe);
        currentIntensity = scaledIntensity(recipe.data.getInt(GTORecipeDataKeys.RAY_INTENSITY), recipe.ocLevel);
        currentWaveLength = recipe.data.getInt(GTORecipeDataKeys.RAY_WAVELENGTH);
        ocLevel = recipe.ocLevel;
        refreshBeam();
    }

    @Override
    public void afterWorking() {
        currentIntensity = 0;
        currentWaveLength = 0;
        ocLevel = 0;
        super.afterWorking();
    }

    public void setThetaRad(float thetaRad) {
        this.thetaRad = BeamMachineUtils.normalizeAngle(thetaRad);
        refreshBeam();
    }

    public void setPhiRad(float phiRad) {
        this.phiRad = BeamMachineUtils.clampPitch(phiRad);
        refreshBeam();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(BeamConfigurator.angles(this::getThetaRad, this::setThetaRad, this::getPhiRad, this::setPhiRad));
    }

    private void refreshBeam() {
        if (isRemote() || getLevel() == null || !getRecipeLogic().isWorking() || currentIntensity <= 0 || currentWaveLength <= 0) return;
        var manager = BeamManager.get(getLevel());
        int thetaBits = Float.floatToIntBits(thetaRad);
        int phiBits = Float.floatToIntBits(phiRad);
        if (beamId >= 0 && manager.getBeam(beamId) != null &&
                emittedIntensity == currentIntensity && emittedWaveLength == currentWaveLength &&
                emittedThetaBits == thetaBits && emittedPhiBits == phiBits)
            return;
        var beam = createBeam();
        if (beamId < 0 || manager.getBeam(beamId) == null) beamId = manager.register(beam);
        else if (emittedThetaBits != thetaBits || emittedPhiBits != phiBits) manager.updatePath(beamId, beam);
        else manager.update(beamId, beam);
        emittedIntensity = currentIntensity;
        emittedWaveLength = currentWaveLength;
        emittedThetaBits = thetaBits;
        emittedPhiBits = phiBits;
    }

    private Beam createBeam() {
        Vec3 direction = BeamMachineUtils.direction(thetaRad, phiRad);
        Vec3 start = BeamMachineUtils.cubeFrameIntersection(Vec3.atCenterOf(getPos()), direction);
        var properties = new BeamProperties();
        properties.vx = direction.x;
        properties.vy = direction.y;
        properties.vz = direction.z;
        properties.intensity = currentIntensity;
        properties.waveLength = currentWaveLength;
        return new Beam(new BeamNode(start.x, start.y, start.z, properties));
    }

    private void removeBeam() {
        if (isRemote() || getLevel() == null || beamId < 0) return;
        var manager = BeamManager.getIfPresent(getLevel());
        if (manager != null) manager.unregister(beamId);
        beamId = -1;
        emittedIntensity = -1;
        emittedWaveLength = -1;
    }

    private static long scaledIntensity(int intensity, int overclockLevel) {
        if (intensity <= 0) return 0;
        if (overclockLevel >= 63 || intensity > (Long.MAX_VALUE >> overclockLevel)) return Long.MAX_VALUE;
        return (long) intensity << overclockLevel;
    }

    public static final RecipeModifier BEAM_OVERCLOCK = (holder, unit, recipe) -> {
        if (holder instanceof IOverclockMachine overclockMachine) {
            var eutm = GTUtil.getTierByVoltage(overclockMachine.getOverclockVoltage());
            var eutr = GTUtil.getTierByVoltage(recipe.eut);
            var ocLevel = eutm - eutr;
            recipe.ocLevel = ocLevel;
            recipe.eut = recipe.eut << (ocLevel * 2);
            return recipe;
        }
        return null;
    };
}
