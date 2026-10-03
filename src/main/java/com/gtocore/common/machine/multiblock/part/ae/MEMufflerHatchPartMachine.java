package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.config.GTORules;
import com.gtocore.data.CraftingComponents;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.IGTOMufflerMachine;
import com.gtolib.api.machine.trait.MEOutputItemHandler;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeListener;
import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@DataGeneratorScanned
public class MEMufflerHatchPartMachine extends StatusTrackedMEPartMachine implements IGTOMufflerMachine {

    @SaveToDisk
    private final KeyStorage internalBuffer;
    @SaveToDisk
    private final NotifiableInventory<AEItemKey> mufflerHatchInv;
    @SaveToDisk
    private final NotifiableInventory<AEItemKey> amplifierInv;
    private final MEOutputItemHandler handler;

    @SyncToClient
    private int recoveryChance = 0;

    private int muffler_tier = 0;

    public MEMufflerHatchPartMachine(@NotNull MetaMachineBlockEntity holder) {
        super(holder, IO.NONE);
        internalBuffer = new KeyStorage();
        handler = new MEOutputItemHandler(this, internalBuffer);
        mufflerHatchInv = NotifiableInventory.items(this, KeyInventory.items(1, 1, true), IO.NONE, IO.BOTH);
        mufflerHatchInv.setFilter(key -> key instanceof AEItemKey k && Wrapper.MUFFLER_HATCH.containsKey(k.getItem()));
        mufflerHatchInv.addChangedListener(this::onMufflerChange);
        amplifierInv = NotifiableInventory.items(this, KeyInventory.items(1, GTORules.ME_MUFFLER_MAX.get(), true), IO.NONE, IO.BOTH);
        amplifierInv.setFilter(key -> key instanceof AEItemKey k && Wrapper.AMPLIFIER_TIER_MAP.containsKey(k.getItem()));
        amplifierInv.addChangedListener(this::onMufflerChange);
    }

    private void onMufflerChange() {
        var amplifierKey = amplifierInv.storage.keyAt(0);
        var amplifierItem = amplifierKey == null ? Items.AIR : amplifierKey.getItem();
        int amplifierCount = (int) amplifierInv.storage.amountAt(0);
        var mufflerKey = mufflerHatchInv.storage.keyAt(0);
        var item = mufflerKey == null ? Items.AIR : mufflerKey.getItem();
        recoveryChance = 0;
        muffler_tier = tier;
        if (Wrapper.MUFFLER_HATCH.containsKey(item)) {
            muffler_tier = Wrapper.MUFFLER_HATCH.get(item);
        }
        if (Objects.equals(Wrapper.AMPLIFIER_TIER_MAP.get(amplifierItem), Wrapper.MUFFLER_HATCH.get(item))) {
            var recoveryChanceMin = muffler_tier * 10;
            var recoveryChanceMax = recoveryChanceMin * muffler_tier;
            recoveryChance = (recoveryChanceMax - recoveryChanceMin) * (amplifierCount - GTORules.ME_MUFFLER_MIN.get()) / Math.max(1, GTORules.ME_MUFFLER_MAX.get() - GTORules.ME_MUFFLER_MIN.get());
            recoveryChance += recoveryChanceMin;
            recoveryChance = Math.max(recoveryChance, recoveryChanceMin);
        } else {
            recoveryChance = muffler_tier * 10;
        }
    }

    @Override
    public @NotNull RecipeHandlerUnit getHandlerUnit() {
        return RecipeHandlerUnit.NO_DATA;
    }

    @Override
    public void gtolib$insertAsh(MultiblockControllerMachine controller, GTRecipe lastRecipe) {
        TaskHandler.enqueueAsyncTask(getLevel(), () -> IGTOMufflerMachine.super.gtolib$insertAsh(controller, lastRecipe), 0);
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        onMufflerChange();
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        handler.updateAutoOutputSubscription();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        handler.updateAutoOutputSubscription();
    }

    @Override
    public void onMachineRemoved() {
        var grid = getMainNode().getGrid();
        if (grid != null && !internalBuffer.isEmpty()) {
            for (var entry : internalBuffer) {
                grid.getStorageService().getInventory().insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE, getActionSourceField());
            }
        }
        clearInventory(mufflerHatchInv);
        clearInventory(amplifierInv);
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.superAttachConfigurators(configuratorPanel);
    }

    @RegisterLanguage(cn = "放入消声仓", en = "Insert a Muffler Hatch")
    private static final String MUFFLER_TOOLTIP_KEY = "gtocore.machine.me_muffler_part.muffler_tooltip";
    @RegisterLanguage(cn = "放入相同等级的消声仓以启用", en = "Insert a Muffler Hatch of the same level to enable")
    private static final String MUFFLER_TOOLTIP_KEY_EXPERT = "gtocore.machine.me_muffler_part.muffler_tooltip_expert";
    @RegisterLanguage(cn = "放入相同等级的集控核心以增幅概率", en = "Insert a Control Core of the same level to increase the probability")
    private static final String AMPLIFIER_TOOLTIP_KEY = "gtocore.machine.me_muffler_part.apm_tooltip";
    @RegisterLanguage(cn = "消声仓", en = "Muffler Hatch")
    private static final String MUFFLER_SLOT = "gtocore.machine.me_muffler_part.muffler_slot";
    @RegisterLanguage(cn = "集控核心", en = "Control Core")
    private static final String AMPLIFIER_SLOT = "gtocore.machine.me_muffler_part.amplifier_slot";

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return MEPartUI.mainPage(this, widget, buildPage());
    }

    @Override
    public Widget createUIWidget() {
        return buildPage();
    }

    /**
     * 页面：回收几率（状态面板）、消声器槽与增幅槽（悬停看说明）、"等待输出"网格。
     */
    private UIElement buildPage() {
        var status = new StatusPanel();
        status.addSentence(() -> Component.translatable("gtceu.muffler.recovery_tooltip", recoveryChance));
        var muffler = ItemSlot.of(mufflerHatchInv.storage, 0);
        muffler.tooltips(GTORules.MUFFLER_TIER.isExpert() ? MUFFLER_TOOLTIP_KEY_EXPERT : MUFFLER_TOOLTIP_KEY);
        var amplifier = ItemSlot.of(amplifierInv.storage, 0);
        amplifier.tooltips(AMPLIFIER_TOOLTIP_KEY);
        var controls = ControlPanel.of(this);
        controls.addSlot(muffler, MUFFLER_SLOT, ControlPanel.contentName(mufflerHatchInv.storage, 0));
        controls.addSlot(amplifier, AMPLIFIER_SLOT, ControlPanel.contentName(amplifierInv.storage, 0));
        return Form.page().addChildren(status, controls.build(), MEPartUI.waitingList("me.muffler.waiting", this.internalBuffer, false, null));
    }

    @Override
    public void recoverItemsTable(ItemStack recoveryItems) {
        if (!workingEnabled) return;
        var key = Keys.item(recoveryItems);
        if (key != null) handler.insert(key, recoveryItems.getCount(), false);
    }

    @Override
    public boolean isFrontFaceFree() {
        return recoveryChance != 0;
    }

    @Override
    public int gtolib$getRecoveryChance() {
        return recoveryChance;
    }

    @Override
    public int getTier() {
        return Math.max(tier, muffler_tier);
    }

    static class Wrapper {

        public static final Map<Item, Integer> MUFFLER_HATCH;
        public static final Map<Item, Integer> AMPLIFIER_TIER_MAP;
        static {
            var mufflerMap = new HashMap<Item, Integer>();
            for (var i : GTMachines.MUFFLER_HATCH) {
                if (i != null && (i.getTier() >= GTValues.LuV || GTORules.MUFFLER_TIER.isExpert())) {
                    mufflerMap.put(i.asItem(), i.getTier());
                }
            }
            MUFFLER_HATCH = Map.copyOf(mufflerMap);
            var amplifierTierMap = new HashMap<Item, Integer>();
            for (int i = GTValues.UV; i <= GTValues.OpV; ++i) {
                amplifierTierMap.put(((Item) CraftingComponents.INTEGRATED_CONTROL_CORE.get(i)), i);
            }
            AMPLIFIER_TIER_MAP = Map.copyOf(amplifierTierMap);
        }
    }
}
