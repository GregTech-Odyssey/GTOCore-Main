package com.gtocore.common.machine.mana;

import com.gtocore.common.machine.multiblock.part.ae.MEPartUI;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternPartUI;
import com.gtocore.utils.ManaUnification;

import com.gtolib.api.machine.mana.feature.IWirelessManaContainerHolder;
import com.gtolib.api.wireless.WirelessManaContainer;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.uipro.UIElement;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;

import appbot.ae2.ManaKey;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.hepdd.gtmthings.utils.BigIntegerUtils;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import gripe._90.arseng.me.key.SourceKey;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;

public final class MEManaInterface extends MetaMachine implements
                                   IGridConnectedMachine,
                                   MEStorage,
                                   IMachineLife,
                                   IWirelessManaContainerHolder,
                                   IStorageProvider,
                                   IFancyUIMachine {

    @Getter
    @Setter
    private WirelessManaContainer wirelessManaContainerCache;
    @SaveToDisk
    private final GridNodeHolder nodeHolder;
    @SyncToClient
    @Getter
    @Setter
    private boolean isOnline;
    @SaveToDisk(defaultValue = "0")
    @Getter
    private int priority = 0;

    public MEManaInterface(MetaMachineBlockEntity holder) {
        super(holder);
        this.nodeHolder = new GridNodeHolder(this);
        getMainNode().addService(IStorageProvider.class, this);
    }

    @Override
    public Object getResourceIdentity() {
        return getWirelessManaContainer();
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public Component getDescription() {
        return Component.translatable("ars_nouveau.category.source").append("&").append(Component
                .translatable("ftbquests.task.ftbquests.botania_mana"));
    }

    @Override
    public @Nullable UUID getUUID() {
        return getOwnerUUID();
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return what == ManaKey.KEY || what == SourceKey.KEY;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if ((what != ManaKey.KEY && what != SourceKey.KEY) || getWirelessManaContainer() == null) {
            return 0;
        }
        var converted = convert(what, amount);
        if (mode == Actionable.MODULATE) {
            getWirelessManaContainer().setStorage(getWirelessManaContainer().getStorage().add(BigInteger.valueOf(converted)));
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if ((what != ManaKey.KEY && what != SourceKey.KEY) || getWirelessManaContainer() == null) {
            return 0;
        }
        var converted = convert(what, amount);
        long stored = BigIntegerUtils.getLongValue(getWirelessManaContainer().getStorage());
        long toExtract = Math.min(stored, converted);
        if (mode == Actionable.MODULATE) {
            getWirelessManaContainer().setStorage(getWirelessManaContainer().getStorage().subtract(BigInteger.valueOf(toExtract)));
        }
        return toExtract;
    }

    private long convert(AEKey what, long amount) {
        if (what == ManaKey.KEY) {
            return amount;
        } else if (what == SourceKey.KEY) {
            return ManaUnification.sourceToMana(amount);
        } else {
            return 0;
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter toAdd) {
        if (getWirelessManaContainer() == null) {
            return;
        }
        long stored = BigIntegerUtils.getLongValue(getWirelessManaContainer().getStorage());
        if (stored > 0) {
            toAdd.add(ManaKey.KEY, stored);
        }
        long storedSource = ManaUnification.manaToSource(stored);
        if (storedSource > 0) {
            toAdd.add(SourceKey.KEY, storedSource);
        }
    }

    @Override
    public void onMachinePlaced(@org.jetbrains.annotations.Nullable LivingEntity player, ItemStack stack) {
        if (player != null) {
            setOwnerUUID(player.getUUID());
        }
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storageMounts.mount(this);
    }

    /// 优先级范围
    private static final int PRIORITY_LIMIT = 100000000;

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return MEPartUI.mainPage(this::isOnline, getTitle(), widget, buildPage());
    }

    @Override
    public Widget createUIWidget() {
        return buildPage();
    }

    /** 页面：AE 优先级（悬停看提取 / 存入优先级说明）。 */
    private UIElement buildPage() {
        var section = UIElement.section();
        section.addChild(MEPartUI.numberRow("gui.ae2.Priority",
                MEPatternPartUI.intField(0, this::getPriority, value -> setPriority(Math.min(value, PRIORITY_LIMIT)), -PRIORITY_LIMIT),
                "gui.ae2.PriorityExtractionHint", "gui.ae2.PriorityInsertionHint"));
        return MEPartUI.page().addChild(section);
    }

    private void setPriority(int integer) {
        if (integer != this.priority) {
            this.priority = integer;
            if (getMainNode() != null) IStorageProvider.requestUpdate(getMainNode());
        }
    }
}
