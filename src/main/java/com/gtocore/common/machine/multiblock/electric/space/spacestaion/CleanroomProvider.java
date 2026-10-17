package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.feature.multiblock.IDroneControlCenterMachine;
import com.gtolib.api.machine.impl.part.DroneHatchPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gtocore.common.machine.multiblock.part.maintenance.ModularHatchPartMachine.CLEANROOM_NOT_SET;
import static com.gtocore.common.machine.multiblock.part.maintenance.ModularHatchPartMachine.CURRENT_CLEANROOM;

@DataGeneratorScanned
public class CleanroomProvider extends Extension implements IDroneControlCenterMachine, ISpaceServiceMachine {

    @RegisterLanguage(cn = "超净环境", en = "Cleanroom")
    private static final String CLEANROOM = "gtocore.machine.space_cleanroom_provider.cleanroom";

    private int cleanroomTier;
    private final List<DroneHatchPartMachine> droneHatchPartMachine = new ArrayList<>();

    public CleanroomProvider(MetaMachineBlockEntity metaMachineBlockEntity) {
        super(metaMachineBlockEntity);
    }

    @Override
    public void onStructureFormed() {
        droneHatchPartMachine.clear();
        super.onStructureFormed();
        this.cleanroomTier = getMultiblockState().getMatchContext().getOrDefault(Predicates.DataKey.FILTER_TYPE, 0);
        IIWirelessInteractor.addToNet(this, IDroneControlCenterMachine.class);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.cleanroomTier = 0;
        droneHatchPartMachine.clear();
        IIWirelessInteractor.removeFromNet(this, IDroneControlCenterMachine.class);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        this.cleanroomTier = 0;
        IIWirelessInteractor.removeFromNet(this, IDroneControlCenterMachine.class);
    }

    @Override
    public boolean isActiveState() {
        return isWorkspaceReady();
    }

    public void onPartScan(@NotNull IMultiPart part) {
        super.onPartScan(part);
        if (part instanceof DroneHatchPartMachine machine) {
            droneHatchPartMachine.add(machine);
        }
    }

    @Override
    public long getEUt() {
        if (cleanroomTier == 0) {
            return VA[HV];
        }
        return (long) VA[LuV] * cleanroomTier;
    }

    @Override
    public int getCleanroomTier() {
        return cleanroomTier;
    }

    @Override
    public List<DroneHatchPartMachine> getDroneHatchPartMachine() {
        return droneHatchPartMachine;
    }

    @Override
    public void customText(@NotNull List<Component> list) {
        super.customText(list);
        if (!MultiblockPage.isScreenText()) {
            list.add(Component.translatable(CURRENT_CLEANROOM));
            list.add(getCurrentCleanroom().withStyle(ChatFormatting.GREEN));
        }
        IDroneControlCenterMachine.super.addCustomText(list);
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addLine(CLEANROOM, MultiblockPage.cached(this::getCleanroomTier, tier -> getCurrentCleanroom())).bindLevel(() -> cleanroomTier == 0 ? Level.WARNING : Level.NORMAL);
    }

    private MutableComponent getCurrentCleanroom() {
        if (cleanroomTier == 0) {
            return Component.translatable(CLEANROOM_NOT_SET);
        }
        return ICleanroomProvider.getCleanroomTooltip(cleanroomTier);
    }
}
