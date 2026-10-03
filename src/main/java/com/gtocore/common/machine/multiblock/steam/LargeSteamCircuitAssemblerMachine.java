package com.gtocore.common.machine.multiblock.steam;

import com.gtocore.config.GTORules;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ItemCell;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
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
    @Nullable
    private Item circuitStackItem;
    private ItemStack circuitStack = ItemStack.EMPTY;

    @SaveToDisk(defaultValue = "0")
    private int count;

    public LargeSteamCircuitAssemblerMachine(MetaMachineBlockEntity holder) {
        super(holder, GTORules.STEAM_CIRCUIT_PARALLELS.get(), 128, 1);
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        if (count < GTORules.STEAM_CIRCUIT_ENGRAVING.get()) {
            IdleReason.CIRCUIT_ENGRAVING.setReason(this, GTORules.STEAM_CIRCUIT_ENGRAVING.get(), count);
            return null;
        }
        if (recipe.itemOutputs.ingredient(0).displayKey() instanceof AEItemKey outputKey && outputKey.getItem() == item) {
            if (isMultiMode) {
                recipe.bake();
                recipe.itemOutputs = recipe.itemOutputs.range(0, 1).scaled(GTORules.STEAM_CIRCUIT_OUTPUT.get());
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
        IdleReason.NOT_APPLICABLE.setReason(this);
        return null;
    }

    @RegisterLanguage(cn = "增产模式 : ", en = "Is Multiply Mode Enabled : ")
    private static final String IS_MULTIPLY = "gtocore.machine.large_steam_circuit_assembler.is_multiply";
    @RegisterLanguage(cn = "电路铭刻", en = "Circuit Engraving")
    private static final String ENGRAVING = "gtocore.machine.large_steam_circuit_assembler.engraving";
    @RegisterLanguage(cn = "已铭刻", en = "Engraved")
    private static final String ENGRAVED = "gtocore.machine.large_steam_circuit_assembler.engraved";
    @RegisterLanguage(cn = "增产模式", en = "Multiply Mode")
    private static final String MULTIPLY_MODE = "gtocore.machine.large_steam_circuit_assembler.multiply_mode";
    @RegisterLanguage(cn = "从输入总线取出电路，铭刻数量达到要求后只生产该电路", en = "Takes circuits from input buses; once enough are engraved, only that circuit is produced")
    private static final String ENGRAVE_HINT = "gtocore.machine.large_steam_circuit_assembler.engrave_hint";
    @RegisterLanguage(cn = "产量与耗时按规则倍增，蒸汽消耗同步增加", en = "Output and duration are multiplied by the rules; steam usage increases accordingly")
    private static final String MULTIPLY_HINT = "gtocore.machine.large_steam_circuit_assembler.multiply_hint";

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("gtocore.machine.large_steam_circuit_assembler.circuit", (item == null ? "null" : Component.translatable(item.getDescriptionId()))));
            if (item != null && count < GTORules.STEAM_CIRCUIT_ENGRAVING.get()) {
                textList.add(Component.translatable("gui.ae2.Missing", GTORules.STEAM_CIRCUIT_ENGRAVING.get() - count));
            }
            textList.add(Component.translatable(IS_MULTIPLY, isMultiMode));
        }
    }

    @Override
    protected void addPageContent(MultiblockPage page) {
        super.addPageContent(page);
        var circuit = ItemCell.of(this::getCircuitStack);
        var progress = ProgressBar.of(LayoutStyle.AUTO, Component.translatable(ENGRAVED), 0,
                () -> new ProgressBar.Progress(count, GTORules.STEAM_CIRCUIT_ENGRAVING.get(), 0)).bindClientColor(UITheme::barProgress);
        progress.layout(l -> l.flex(1));
        var engrave = Button.translatable(UISizes.BUTTON_WIDTH, "gtocore.machine.large_steam_circuit_assembler.engrave_circuit")
                .setOnServerClick(this::engraveCircuit);
        engrave.tooltips(ENGRAVE_HINT);
        var row = UIElement.centeredRow(UISizes.SLOT_SIZE).layout(l -> l.width(LayoutStyle.AUTO))
                .addChildren(circuit, progress, engrave);
        page.addSection(Form.section(ENGRAVING).addChild(row));
    }

    @Override
    public void addControls(ControlPanel controls) {
        super.addControls(controls);
        controls.addToggle(MULTIPLY_MODE, () -> isMultiMode, value -> isMultiMode = value, MULTIPLY_HINT);
    }

    private ItemStack getCircuitStack() {
        if (item != circuitStackItem) {
            circuitStackItem = item;
            circuitStack = item == null ? ItemStack.EMPTY : item.getDefaultInstance();
        }
        return circuitStack;
    }

    private void engraveCircuit() {
        if (!isFormed()) return;
        for (IMultiPart part : getParts()) {
            if (part instanceof ItemBusPartMachine bus) {
                NotifiableInventory<AEItemKey> handler = bus.getInventory();
                IO io = handler.getHandlerIO();
                if (io == IO.IN || io == IO.BOTH) {
                    var inv = handler.storage;
                    for (int i = 0; i < inv.size(); i++) {
                        AEItemKey key = inv.keyAt(i);
                        if (key == null) continue;
                        Item stackItem = key.getItem();
                        for (TagKey<Item> tagKey : stackItem.builtInRegistryHolder().tags().toList()) {
                            if (tagKey.location().toString().contains("gtceu:circuits/")) {
                                long stored = inv.amountAt(i);
                                int c;
                                if (stackItem == item) {
                                    c = (int) Math.min(GTORules.STEAM_CIRCUIT_ENGRAVING.get() - count, stored);
                                    count += c;
                                } else {
                                    c = (int) Math.min(GTORules.STEAM_CIRCUIT_ENGRAVING.get(), stored);
                                    count = c;
                                }
                                item = stackItem;
                                inv.extract(i, key, c, false);
                                onChanged();
                                if (count >= GTORules.STEAM_CIRCUIT_ENGRAVING.get()) return;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
