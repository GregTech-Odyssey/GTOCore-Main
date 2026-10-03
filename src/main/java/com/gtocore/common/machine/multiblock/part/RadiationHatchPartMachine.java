package com.gtocore.common.machine.multiblock.part;

import com.gtocore.api.machine.part.IRadiationHatch;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTOTickTimeMonitors;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RadiationHatchPartMachine extends MultiblockPartMachine implements IMachineLife, IRadiationHatch {

    @RegisterLanguage(cn = "放射源", en = "Radiation Source")
    private static final String SOURCE = "gtocore.machine.radiation_hatch.source";

    @RegisterLanguage(cn = "抑制量（Sv）", en = "Inhibition Amount (Sv)")
    private static final String INHIBITION = "gtocore.machine.radiation_hatch.inhibition";

    @SaveToDisk
    private final NotifiableInventory<AEItemKey> inventory;
    @Getter
    @SaveToDisk(defaultValue = "0")
    private int radioactivity;
    @SaveToDisk(defaultValue = "0")
    private int initialRadioactivity;
    @SaveToDisk(defaultValue = "0")
    private int count;
    @SaveToDisk(defaultValue = "0")
    private int time;
    @SaveToDisk(defaultValue = "0")
    private int inhibitionDose;
    @SaveToDisk(defaultValue = "0")
    private int initialTime;
    @SaveToDisk(defaultValue = "false")
    private boolean signalPowered;

    private TickableSubscription radiationSubs;

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor radiationMonitor = holder.monitorTick(GTOTickTimeMonitors.RADIATION, this::checkRadiation);
    private final RecipeHandlerUnit handlerList;

    public RadiationHatchPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
        inventory = NotifiableInventory.items(this, 1, IO.IN, IO.BOTH);
        handlerList = RecipeHandlerUnit.of(IO.IN, inventory);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        radiationSubs = subscribeServerTick(radiationSubs, radiationMonitor);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (radiationSubs != null) {
            radiationSubs.unsubscribe();
            radiationSubs = null;
        }
    }

    private void checkRadiation() {
        if (time > 0) {
            if (count < 1) {
                radioactivity = initialRadioactivity * (initialTime + time) / (initialTime << 1);
            }
            time--;
        } else if (getOffsetTimer() % 20 == 0) {
            radioactivity = 0;
            GTRecipeType[] recipeTypes = getDefinition().getRecipeTypes();
            if (recipeTypes != null) {
                GTRecipeType recipeType = recipeTypes[0];
                handlerList.findRecipe(recipeType, (u, r) -> {
                    if (consumeInputs(r)) {
                        count = (int) inventory.storage.amountAt(0);
                        initialRadioactivity = (int) ((r.data.getInt(GTORecipeDataKeys.RADIOACTIVITY) - inhibitionDose) * (1 + ((double) count / 64)));
                        initialTime = r.duration * (inhibitionDose + 200) / 200;
                        time = initialTime;
                        radioactivity = initialRadioactivity;
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    private boolean consumeInputs(GTRecipeDefinition recipe) {
        var inputs = recipe.itemInputs;
        int size = inputs.size();
        for (int i = 0; i < size; i++) {
            if (!handlerList.consume(inputs.ingredient(i), inputs.amount(i), true)) return false;
        }
        for (int i = 0; i < size; i++) {
            if (inputs.isConsumable(i)) handlerList.consume(inputs.ingredient(i), inputs.amount(i), false);
        }
        return true;
    }

    @Override
    public Widget createUIWidget() {
        return MachineDisplay.page(this, this::addDisplayText, controls -> {
            controls.addSlot(inventory.storage, 0, SOURCE);
            controls.addInt(INHIBITION, () -> inhibitionDose, value -> inhibitionDose = value, 0, 40);
        });
    }

    private void addDisplayText(List<Component> textList) {
        textList.add(Component.translatable("gtocore.machine.radiation_hatch.inhibition_dose", inhibitionDose));
        textList.add(Component.translatable("gtocore.recipe.radioactivity", radioactivity));
        textList.add(Component.translatable("gtocore.machine.radiation_hatch.time", time, initialTime));
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(inventory.storage);
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public boolean canConnectRedstone(Direction side) {
        return true;
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        if (getLevel().hasNeighborSignal(fromPos) && !signalPowered) {
            initialRadioactivity = 0;
            time = 0;
            radioactivity = 0;
            signalPowered = true;
        } else if (!getLevel().hasNeighborSignal(fromPos)) {
            signalPowered = false;
        }
    }
}
