package com.gtocore.common.machine.multiblock.noenergy;

import com.gtocore.api.data.NeutronSeries;
import com.gtocore.common.machine.multiblock.part.NeutronIrradiationPartMachine;
import com.gtocore.common.machine.multiblock.part.SensorPartMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.NoEnergyMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

@DataGeneratorScanned
public class NeutronIrradiationChamber extends NoEnergyMultiblockMachine {

    @SaveToDisk(defaultValue = "true")
    @Getter
    @Setter
    private boolean reportsMinFlux = true;
    private NeutronIrradiationPartMachine[] parts;
    private SensorPartMachine sensorMachineNeutronFlux;
    @Nullable
    private TickableSubscription tickSubscription;

    public NeutronIrradiationChamber(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        parts = Arrays.stream(getParts()).filter(p -> p instanceof NeutronIrradiationPartMachine)
                .map(p -> (NeutronIrradiationPartMachine) p)
                .toArray(NeutronIrradiationPartMachine[]::new);
    }

    @Override
    public void onPartScan(@NotNull IMultiPart part) {
        super.onPartScan(part);
        if (part instanceof SensorPartMachine sensor) {
            sensorMachineNeutronFlux = sensor;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        sensorMachineNeutronFlux = null;
        parts = null;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        sensorMachineNeutronFlux = null;
        parts = null;
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tickSubscription = subscribeServerTick(tickSubscription, this::tick, 20);
    }

    private void tick() {
        if (isFormed()) {

            final AtomicLong neutronFluxkeV = new AtomicLong(0);
            fastForEachItems(true, (stack, amount) -> {
                var neutron_sources = NeutronSeries.NEUTRON_SOURCES.get(stack.getItem());
                if (neutron_sources != null) {
                    neutronFluxkeV.addAndGet((long) neutron_sources * amount);
                    inputItem(stack.getItem(), amount);
                }
            });
            if (parts == null || parts.length == 0) return;

            var fluxTotal = neutronFluxkeV.get() * 1000; // keV to eV
            var fluxHatch = fluxTotal / parts.length;
            long sumFlux = 0;
            long minFlux = Long.MAX_VALUE;
            for (var part : parts) {
                var flux = Math.min(part.getNeutronFlux() + fluxHatch, 10_000_000L); // 10,000 keV = 10 MeV
                part.setNeutronFlux(flux);
                sumFlux += flux;
                if (flux < minFlux) minFlux = flux;
            }

            if (sensorMachineNeutronFlux != null) {
                var reportFlux = reportsMinFlux ? minFlux : (sumFlux / parts.length);
                sensorMachineNeutronFlux.update(reportFlux / 1000_000f); // MeV
            }

        }
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0.5, 1, 0.5),
                        GuiTextures.DISTRIBUTION_MODE.getSubTexture(0, 0, 1, 1 / 3d)),
                new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0.5, 1, 0.5),
                        GuiTextures.DISTRIBUTION_MODE.getSubTexture(0, 2 / 3d, 1, 1 / 3d)),
                this::isReportsMinFlux, (cd, b) -> setReportsMinFlux(b)).setTooltipsSupplier(b -> Collections.singletonList(Component.translatable(b ? LANG_REPORTS_MIN_FLUX : LANG_REPORTS_AVG_FLUX))));
    }

    @RegisterLanguage(cn = "传感器采用最低水平仓室的中子通量", en = "Sensor reports the minimum neutron flux of the chambers")
    public static final String LANG_REPORTS_MIN_FLUX = "gtceu.gui.neutron_irradiation_chamber.reports_min_flux";
    @RegisterLanguage(cn = "传感器采用平均水平仓室的中子通量", en = "Sensor reports the average neutron flux of the chambers")
    public static final String LANG_REPORTS_AVG_FLUX = "gtceu.gui.neutron_irradiation_chamber.reports_avg_flux";
}
