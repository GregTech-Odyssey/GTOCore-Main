package com.gtocore.common.machine.multiblock.noenergy;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.part.NeutronAcceleratorPartMachine;
import com.gtocore.common.machine.multiblock.part.SensorPartMachine;

import com.gtolib.api.machine.multiblock.NoEnergyMultiblockMachine;
import com.gtolib.api.recipe.GTORecipeModifiers;
import com.gtolib.api.recipe.IdleReason;
import com.gtolib.utils.MachineUtils;
import com.gtolib.utils.NumberUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.uipro.window.WindowAnchor;
import com.gregtechceu.gtceu.uiwidgets.display.DetailsTab;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NeutronActivatorMachine extends NoEnergyMultiblockMachine implements IExplosionMachine {

    static final int EXPLOSION_EV = 1200000000;
    static final int DISPLAY_MAX_EV = 1400000000;
    static final int DECAY_EV_PER_SECOND = 72 * 1000;
    static final int MODERATOR_EV = 10 * 1000000;
    static final int MAX_ACCELERATORS = 2;
    private static final Item dustBeryllium = ChemicalHelper.getItem(TagPrefix.dust, GTMaterials.Beryllium);
    private static final Item dustGraphite = ChemicalHelper.getItem(TagPrefix.dust, GTMaterials.Graphite);
    int height;
    int targetMin;
    int targetMax;
    int targetEvt;
    @Getter
    @SaveToDisk(defaultValue = "0")
    protected int eV;
    private final ConditionalSubscriptionHandler neutronEnergySubs;
    private SensorPartMachine sensorMachine;
    private final List<ItemBusPartMachine> busMachines = new ArrayList<>(2);
    private final List<NeutronAcceleratorPartMachine> acceleratorMachines = new ArrayList<>(2);

    public NeutronActivatorMachine(MetaMachineBlockEntity holder) {
        super(holder);
        neutronEnergySubs = new ConditionalSubscriptionHandler(this, this::neutronEnergyUpdate, 0, () -> isFormed || eV > 0);
    }

    @Override
    public void onPartScan(IMultiPart part) {
        super.onPartScan(part);
        switch (part) {
            case ItemBusPartMachine itemBusPart -> {
                IO io = itemBusPart.getInventory().getHandlerIO();
                if (io == IO.IN || io == IO.BOTH) {
                    busMachines.add(itemBusPart);
                    for (var handler : itemBusPart.getRecipeHandlers()) {
                        traitSubscriptions.add(handler.subscribe(this::absorptionUpdate));
                    }
                }
            }
            case NeutronAcceleratorPartMachine neutronAccelerator -> acceleratorMachines.add(neutronAccelerator);
            case SensorPartMachine sensorPartMachine -> sensorMachine = sensorPartMachine;
            default -> {}
        }
    }

    @Override
    public void onStructureFormed() {
        acceleratorMachines.clear();
        busMachines.clear();
        super.onStructureFormed();
        var container = getMultiblockState().getMatchContext().get(GTOPredicates.DataKeys.SPEED_PIPE);
        if (container != null) {
            height = container;
        }
        neutronEnergySubs.initialize(getLevel());
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        height = 0;
        sensorMachine = null;
        busMachines.clear();
        acceleratorMachines.clear();
    }

    double getEfficiencyFactor() {
        return Math.pow(0.95, Math.max(height - 4, 0));
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        recordTarget(recipe);
        if ((eV > recipe.data.getInt(GTORecipeDataKeys.EV_MIN) * 1000000 && eV < recipe.data.getInt(GTORecipeDataKeys.EV_MAX) * 1000000)) {
            recipe = GTORecipeModifiers.parallel(this, unit, recipe);
            if (recipe == null) return null;
            recipe.duration = (int) Math.round(Math.max(recipe.duration * getEfficiencyFactor(), 1));
            return recipe;
        }
        setIdleReason(IdleReason.NEUTRON_KINETIC_ENERGY_NOT_SATISFIES);
        return null;
    }

    void recordTarget(GTRecipe recipe) {
        targetMin = recipe.data.getInt(GTORecipeDataKeys.EV_MIN);
        targetMax = recipe.data.getInt(GTORecipeDataKeys.EV_MAX);
        targetEvt = recipe.data.getInt(GTORecipeDataKeys.EVT);
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        int evt = (int) (recipe.data.getInt(GTORecipeDataKeys.EVT) * 1000 * getEVtMultiplier());
        if (eV < evt) {
            setIdleReason(IdleReason.NEUTRON_KINETIC_ENERGY_NOT_SATISFIES);
            return false;
        } else {
            eV -= evt;
        }
        return true;
    }

    double getEVtMultiplier() {
        return Math.max(1, Math.pow(MachineUtils.getHatchParallel(this), 1.2) * getEfficiencyFactor());
    }

    void neutronEnergyUpdate() {
        boolean active = false;
        if (isFormed) {
            for (var accelerator : acceleratorMachines) {
                long increase = accelerator.consumeEnergy();
                if (increase > 0) {
                    active = true;
                    eV += (int) Math.round(Math.max(increase * getEfficiencyFactor(), 1));
                }
            }
            if (eV > EXPLOSION_EV) doExplosion(6);
        }
        if (getOffsetTimer() % 20 == 0) {
            getRecipeLogic().updateTickSubscription();
            if (!active) eV = Math.max(eV - DECAY_EV_PER_SECOND, 0);
        }
        if (eV < 0) eV = 0;
        if (sensorMachine == null) return;
        sensorMachine.update((float) eV / 1000000);
        neutronEnergySubs.updateSubscription();
    }

    private void absorptionUpdate() {
        for (ItemBusPartMachine bus : busMachines) {
            var inv = bus.getInventory();
            for (int i = 0; i < inv.getSlots(); i++) {
                var stack = inv.getStackInSlot(i);
                if (isModerator(stack)) {
                    int consume = Math.clamp(eV / MODERATOR_EV, 1, stack.getCount());
                    inv.extractItemInternal(i, consume, false);
                    eV -= MODERATOR_EV * consume;
                }
            }
        }
    }

    static boolean isModerator(ItemStack stack) {
        return stack.is(dustBeryllium) || stack.is(dustGraphite);
    }

    List<ItemBusPartMachine> getModeratorBuses() {
        return busMachines;
    }

    List<NeutronAcceleratorPartMachine> getAccelerators() {
        return acceleratorMachines;
    }

    int getMaxAccelerators() {
        return MAX_ACCELERATORS;
    }

    @Nullable
    SensorPartMachine getSensor() {
        return sensorMachine;
    }

    boolean consumesKineticEnergy() {
        return true;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(0, new DetailsTab(DetailsTab.visitKey(this), widget -> NeutronActivatorFlowPage.details(this)));
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return NeutronActivatorFlowPage.create(this, widget);
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    @Override
    public boolean windowHasPlayerInventory() {
        return true;
    }

    @Override
    public WindowAnchor windowAnchor() {
        return WindowAnchor.CENTER;
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        textList.add(Component.translatable("gtocore.machine.neutron_activator.ev", NumberUtils.formatLong(eV)));
        textList.add(Component.translatable("gtocore.machine.neutron_activator.efficiency", FormattingUtil.formatNumbers(getEVtMultiplier())));
        textList.add(Component.translatable("gtocore.machine.height", height));
        textList.add(Component.translatable("gtocore.machine.duration_multiplier.tooltip", FormattingUtil.formatNumbers(getEfficiencyFactor() * 100)).append("%"));
    }
}
