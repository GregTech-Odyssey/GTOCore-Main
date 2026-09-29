package com.gtocore.common.machine.multiblock.steam;

import com.gtocore.config.GTORules;

import com.gtolib.api.annotation.Scanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@Scanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LargeSteamCircuitAssemblerMachine extends BaseSteamMultiblockMachine {

    @SaveToDisk(defaultValue = "true")
    private boolean isMultiMode = true;

    @Override
    boolean oc() {
        return true;
    }

    @SaveToDisk
    private Item item;

    @SaveToDisk(defaultValue = "0")
    private int count;

    public LargeSteamCircuitAssemblerMachine(MetaMachineBlockEntity holder) {
        super(holder, GTORules.STEAM_CIRCUIT_PARALLELS.get(), 128, 1);
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        if (count < GTORules.STEAM_CIRCUIT_ENGRAVING.get()) return null;
        var content = recipe.itemOutputs.getFirst();
        if (content.inner.getInnerItemStack().getItem() == item) {
            if (isMultiMode) {
                recipe.itemOutputs = List.of(content.copy(GTORules.STEAM_CIRCUIT_OUTPUT.get()));
                recipe = super.getRealRecipe(unit, recipe);
                if (recipe != null) {
                    recipe.duration = recipe.duration * GTORules.STEAM_CIRCUIT_DURATION.get();
                    recipe.setEUt(recipe.getInputEUt() * GTORules.STEAM_CIRCUIT_STEAM_COST.get());
                }
                return recipe;
            } else {
                return super.getRealRecipe(unit, recipe);
            }
        }
        return null;
    }

    @RegisterLanguage(cn = "增产模式 : ", en = "Is Multiply Mode Enabled : ")
    private static final String IS_MULTIPLY = "gtocore.machine.large_steam_circuit_assembler.is_multiply";

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(ComponentPanelWidget.withButton(Component.translatable("gtocore.machine.large_steam_circuit_assembler.engrave_circuit"), "engraveCircuit"));
            textList.add(Component.translatable("gtocore.machine.large_steam_circuit_assembler.circuit", (item == null ? "null" : Component.translatable(item.getDescriptionId()))));
            if (item != null && count < GTORules.STEAM_CIRCUIT_ENGRAVING.get()) {
                textList.add(Component.translatable("gui.ae2.Missing", GTORules.STEAM_CIRCUIT_ENGRAVING.get() - count));
            }
            if (isMultiMode) textList.add(Component.translatable(IS_MULTIPLY, true).append(ComponentPanelWidget.withButton(Component.translatable("gtocore.machine.on"), "toggleMultiMode")));
            if (!isMultiMode) textList.add(Component.translatable(IS_MULTIPLY, false).append(ComponentPanelWidget.withButton(Component.translatable("gtocore.machine.off"), "toggleMultiMode")));
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        if (!clickData.isRemote && "toggleMultiMode".equals(componentData)) {
            isMultiMode = !isMultiMode;
        }
        if (!clickData.isRemote && "engraveCircuit".equals(componentData)) {
            for (IMultiPart part : getParts()) {
                if (part instanceof ItemBusPartMachine bus) {
                    NotifiableItemStackHandler inv = bus.getInventory();
                    IO io = inv.getHandlerIO();
                    if (io == IO.IN || io == IO.BOTH) {
                        for (int i = 0; i < inv.getSlots(); i++) {
                            ItemStack stack = inv.getStackInSlot(i);
                            for (TagKey<Item> tagKey : stack.getTags().toList()) {
                                if (tagKey.location().toString().contains("gtceu:circuits/")) {
                                    int c = stack.getCount();
                                    if (stack.getItem() == item) {
                                        c = Math.min(GTORules.STEAM_CIRCUIT_ENGRAVING.get() - count, c);
                                        count += c;
                                    } else {
                                        c = Math.min(GTORules.STEAM_CIRCUIT_ENGRAVING.get(), c);
                                        count = c;
                                    }
                                    item = stack.getItem();
                                    inv.extractItemInternal(i, c, false);
                                    if (count >= GTORules.STEAM_CIRCUIT_ENGRAVING.get()) return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
