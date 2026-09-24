package com.gtocore.common.machine.mana.part;

import com.gtocore.common.machine.multiblock.part.ae.MEPartUI;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternPartUI;
import com.gtocore.utils.ManaUnification;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.mana.ManaAmplifierPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.ButtonConfigurator;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;

import appbot.ae2.ManaKey;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import gripe._90.arseng.me.key.SourceKey;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@DataGeneratorScanned
public final class MEManaAmplifierPartMachine extends ManaAmplifierPartMachine implements IGridConnectedMachine {

    @RegisterLanguage(cn = "从ME网络拉取魔力", en = "Pull Mana from ME Network")
    public static final String LANG_USE_SOURCE = "gtceu.machine.mana_amplifier.use_source";
    @RegisterLanguage(cn = "从ME网络拉取魔源", en = "Pull Source from ME Network")
    public static final String LANG_USE_MANA = "gtceu.machine.mana_amplifier.use_mana";
    @SaveToDisk
    private final GridNodeHolder nodeHolder;
    @SyncToClient
    @Getter
    @Setter
    private boolean isOnline;
    private final ConditionalSubscriptionHandler updateSubs;

    private boolean useMana = true;
    private boolean useSource = true;

    public MEManaAmplifierPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
        this.nodeHolder = new GridNodeHolder(this);
        this.updateSubs = new ConditionalSubscriptionHandler(this, this::updateTick, 20, this::isWorkingEnabled);
        manaContainer.setAcceptDistributor(false);
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateSubs.initialize(getLevel());
    }

    /// 基类"最大魔力"设置的翻译键（GTOLib 里声明为私有，这里写完整字面量）
    private static final String LANG_MAX_MANA = "gtocore.machine.mana_amplifier_part.max_mana";

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return MEPartUI.mainPage(this::isOnline, getTitle(), widget, buildPage());
    }

    /** 页面：一个设置区块——最大魔力、是否从 ME 取魔力、是否从 ME 取源质。 */
    @Override
    public Widget createUIWidget() {
        return buildPage();
    }

    private UIElement buildPage() {
        var section = UIElement.section();
        section.addChildren(
                MEPartUI.numberRow(LANG_MAX_MANA, MEPatternPartUI.longField(0, this::getCurrent, this::setMaxMana, min)),
                MEPartUI.controlRow(LANG_USE_MANA, Switch.of(() -> useMana, enabled -> useMana = enabled)),
                MEPartUI.controlRow(LANG_USE_SOURCE, Switch.of(() -> useSource, enabled -> useSource = enabled)));
        return MEPartUI.page().addChild(section);
    }

    private void setMaxMana(long amount) {
        current = Math.max(min, amount);
        onAmountChange(current);
        onChanged();
    }

    private void updateTick() {
        this.updateSubs.updateSubscription();
        if (getActionableNode() != null && getActionableNode().isActive()) {
            var meStorage = getActionableNode().getGrid().getStorageService().getInventory();
            long canInsert = manaContainer.getMaxMana() - manaContainer.getCurrentMana();
            if (canInsert > 0 && useMana) {
                long canExtract = meStorage.extract(ManaKey.KEY, canInsert, Actionable.SIMULATE, IActionSource.ofMachine(this));
                if (canExtract > 0) {
                    long extracted = meStorage.extract(ManaKey.KEY, canExtract, Actionable.MODULATE, IActionSource.ofMachine(this));
                    manaContainer.addMana(extracted, 1, false);
                }
            }
            canInsert = ManaUnification.manaToSource(manaContainer.getMaxMana() - manaContainer.getCurrentMana());
            if (canInsert > 0 && useSource) {
                long canExtract = meStorage.extract(SourceKey.KEY, canInsert, Actionable.SIMULATE, IActionSource.ofMachine(this));
                if (canExtract > 0) {
                    long extracted = meStorage.extract(SourceKey.KEY, canExtract, Actionable.MODULATE, IActionSource.ofMachine(this));
                    manaContainer.addMana(ManaUnification.sourceToMana(extracted), 1, false);
                }
            }
        }
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        updateSubs.updateSubscription();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new ButtonConfigurator(WidgetIcons.REFUND, this::refundAll).setTooltips(List.of(Component.translatable("gui.gtceu.refund_all.desc"))));
    }

    private void refundAll(ClickData clickData) {
        if (clickData.isRemote) return;
        setWorkingEnabled(false);
        if (getActionableNode() != null && getActionableNode().isActive()) {
            var meStorage = getActionableNode().getGrid().getStorageService().getInventory();
            long currentMana = manaContainer.getCurrentMana();
            if (currentMana > 0) {
                manaContainer.removeMana(
                        meStorage.insert(
                                ManaKey.KEY,
                                currentMana,
                                Actionable.MODULATE,
                                IActionSource.ofMachine(this)),
                        1, false);
            }
        }
    }
}
