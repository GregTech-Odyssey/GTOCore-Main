package com.gtocore.common.machine.electric.beam;

import com.gtolib.api.beam.Beam;
import com.gtolib.api.beam.BeamManager;
import com.gtolib.api.beam.BeamNode;
import com.gtolib.api.beam.BeamPassContext;
import com.gtolib.api.beam.BeamPassKey;
import com.gtolib.api.beam.BeamProperties;
import com.gtolib.api.beam.IBeamOperator;
import com.gtolib.api.machine.SimpleNoEnergyMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;

import net.minecraft.world.phys.Vec3;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class AdjustableSemiReflector extends SimpleNoEnergyMachine implements IBeamOperator {

    @Getter
    @SaveToDisk(defaultValue = "0.5")
    @SyncToClient
    private float reflectivity = 0.5F;

    private final Object2ObjectOpenHashMap<BeamPassKey, Incident> incidents = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<TransmittedKey, TransmittedBeam> transmittedBeams = new Object2ObjectOpenHashMap<>();
    private boolean transmittedDirty;
    private TickableSubscription tickSubscription;

    public AdjustableSemiReflector(MetaMachineBlockEntity holder) {
        super(holder, 0, t -> 0);
    }

    @Override
    public BeamNode operate(BeamPassContext context, BeamNode lastNode, Vec3 exactPos) {
        if (context.beamId() <= 0) return reflectedNode(lastNode, exactPos);
        var incident = incidents.computeIfAbsent(context.key(), ignored -> new Incident());
        incident.context = context;
        incident.node = lastNode;
        incident.exactPos = exactPos;
        transmittedDirty = true;
        return reflectedNode(lastNode, exactPos);
    }

    @Override
    public void onRayBeamPassRemoved(BeamPassContext context) {
        if (incidents.remove(context.key()) != null) transmittedDirty = true;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = Math.clamp(reflectivity, 0.0F, 1.0F);
        transmittedDirty = true;
        if (getLevel() != null) requestRayBeamUpdate(getLevel(), getPos());
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
        configuratorPanel.attachConfigurators(BeamConfigurator.reflectivity(this::getReflectivity, this::setReflectivity));
    }

    @Override
    public Widget createUIWidget() {
        return BeamMachineUtils.addReflectivityControl(super.createUIWidget(), this::getReflectivity, this::setReflectivity);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) tickSubscription = subscribeServerTick(tickSubscription, this::tick, 1);
    }

    @Override
    public void onUnload() {
        clearTransmittedBeams();
        incidents.clear();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
        super.onUnload();
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        clearTransmittedBeams();
        incidents.clear();
    }

    private void tick() {
        var manager = BeamManager.get(getLevel());
        for (var iter = incidents.object2ObjectEntrySet().fastIterator(); iter.hasNext();) {
            var it = iter.next();
            var incident = it.getValue();
            if (incidents.get(it.getKey()) != incident) continue;
            if (!manager.isPassActive(incident.context)) {
                iter.remove();
                transmittedDirty = true;
            }
        }
        if (!transmittedDirty) return;
        transmittedDirty = false;

        var aggregates = collectTransmittedBeams();
        for (var iter = transmittedBeams.object2ObjectEntrySet().fastIterator(); iter.hasNext();) {
            var entry = iter.next();
            if (aggregates.containsKey(entry.getKey())) continue;
            if (entry.getValue() == null) {
                iter.remove();
                continue;
            }
            if (entry.getValue().beamId >= 0) manager.unregister(entry.getValue().beamId);
            iter.remove();
        }
        for (var entry : aggregates.entrySet()) {
            var aggregate = entry.getValue();
            if (!manager.isPassActive(aggregate.ownerContext)) {
                transmittedDirty = true;
                continue;
            }
            var snapshot = aggregate.snapshot();
            var transmitted = transmittedBeams.computeIfAbsent(entry.getKey(), ignored -> new TransmittedBeam());
            if (transmitted.beamId < 0 || manager.getBeam(transmitted.beamId) == null) {
                transmitted.beamId = manager.registerDependent(aggregate.ownerContext, new Beam(snapshot.node()));
                transmitted.snapshot = snapshot;
            } else if (!snapshot.equals(transmitted.snapshot)) {
                manager.update(transmitted.beamId, new Beam(snapshot.node()));
                transmitted.snapshot = snapshot;
            }
        }
    }

    private Map<TransmittedKey, TransmittedAggregate> collectTransmittedBeams() {
        var aggregates = new HashMap<TransmittedKey, TransmittedAggregate>();
        for (var incident : incidents.values()) {
            var properties = incident.node.propertiesSnapshot();
            long reflected = BeamMachineUtils.scaled(properties.intensity, reflectivity);
            long transmitted = (long) ((properties.intensity - reflected) * incident.context.decay());
            if (transmitted <= 0) continue;

            Vec3 direction = properties.getVector().normalize();
            if (direction.lengthSqr() <= 1.0E-12D) continue;
            var key = TransmittedKey.of(direction, properties.waveLength, properties.polarization);
            aggregates.computeIfAbsent(key, TransmittedAggregate::new).add(incident, transmitted);
        }
        return aggregates;
    }

    private BeamNode reflectedNode(BeamNode lastNode, Vec3 exactPos) {
        var properties = lastNode.propertiesSnapshot().copy();
        long reflected = BeamMachineUtils.scaled(properties.intensity, reflectivity);
        Vec3 direction = properties.getVector().normalize().scale(-1.0D);
        properties.vx = direction.x;
        properties.vy = direction.y;
        properties.vz = direction.z;
        properties.intensity = reflected;
        return new BeamNode(exactPos.x, exactPos.y, exactPos.z, properties);
    }

    private void clearTransmittedBeams() {
        if (getLevel() != null && !isRemote()) {
            var manager = BeamManager.getIfPresent(getLevel());
            if (manager != null) {
                for (var transmitted : transmittedBeams.values()) {
                    if (transmitted.beamId >= 0) manager.unregister(transmitted.beamId);
                }
            }
        }
        transmittedBeams.clear();
    }

    private static final class Incident {

        private BeamPassContext context;
        private BeamNode node;
        private Vec3 exactPos;
    }

    private static final class TransmittedAggregate {

        private final TransmittedKey key;
        private BeamPassContext ownerContext;
        private Vec3 exactPos;
        private long intensity;

        private TransmittedAggregate(TransmittedKey key) {
            this.key = key;
        }

        private void add(Incident incident, long addedIntensity) {
            intensity = intensity > Long.MAX_VALUE - addedIntensity ? Long.MAX_VALUE : intensity + addedIntensity;
            if (ownerContext == null || comparePass(incident.context, ownerContext) < 0) {
                ownerContext = incident.context;
                exactPos = incident.exactPos;
            }
        }

        private TransmittedSnapshot snapshot() {
            return new TransmittedSnapshot(key, exactPos, intensity);
        }
    }

    private static final class TransmittedBeam {

        private int beamId = -1;
        private TransmittedSnapshot snapshot;
    }

    private record TransmittedKey(double vx, double vy, double vz, int waveLength, float polarization) {

        private static TransmittedKey of(Vec3 direction, int waveLength, float polarization) {
            return new TransmittedKey(canonicalZero(direction.x), canonicalZero(direction.y), canonicalZero(direction.z),
                    waveLength, polarization == 0.0F ? 0.0F : polarization);
        }
    }

    private record TransmittedSnapshot(TransmittedKey key, Vec3 exactPos, long intensity) {

        private BeamNode node() {
            var properties = new BeamProperties();
            properties.vx = key.vx;
            properties.vy = key.vy;
            properties.vz = key.vz;
            properties.waveLength = key.waveLength;
            properties.polarization = key.polarization;
            properties.intensity = intensity;
            return new BeamNode(exactPos.x, exactPos.y, exactPos.z, properties);
        }
    }

    private static int comparePass(BeamPassContext first, BeamPassContext second) {
        int beamComparison = Integer.compare(first.beamId(), second.beamId());
        return beamComparison != 0 ? beamComparison : Integer.compare(first.passIndex(), second.passIndex());
    }

    private static double canonicalZero(double value) {
        return value == 0.0D ? 0.0D : value;
    }
}
