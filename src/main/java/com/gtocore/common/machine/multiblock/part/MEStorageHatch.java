package com.gtocore.common.machine.multiblock.part;

import com.gtocore.api.ae2.stacks.AEFluidKeyStackHandler;
import com.gtocore.api.ae2.stacks.AEItemKeyStackHandler;
import com.gtocore.api.ae2.stacks.AEManaKeyHandler;
import com.gtocore.common.machine.multiblock.storage.MultiblockMEStorageMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.SingleCustomItemStackHandler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaReceiver;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@DataGeneratorScanned
public final class MEStorageHatch extends MultiblockPartMachine {

    @RegisterLanguage(cn = "标记物品", en = "Marked Item")
    private static final String MARK_ITEM = "gtocore.machine.vault_hatch.mark_item";
    @RegisterLanguage(cn = "标记流体", en = "Marked Fluid")
    private static final String MARK_FLUID = "gtocore.machine.vault_hatch.mark_fluid";

    private static final int MARK_FLUID_AMOUNT = 1000;

    @NotNull
    private final AEManaKeyHandler manaHandler;

    /** 标记的物品，非空时本仓的物品 IO 只针对它 */
    @SaveToDisk
    private final SingleCustomItemStackHandler itemMark = new SingleCustomItemStackHandler(1);
    /** 标记的流体，非空时本仓的流体 IO 只针对它 */
    @SaveToDisk
    private final CustomFluidTank fluidMark = new CustomFluidTank(MARK_FLUID_AMOUNT);
    // 处理器本身就是本仓的物品/流体能力（MachineTrait 构造时自动挂上），每个仓体各持一份才有各自的标记
    @NotNull
    private final AEItemKeyStackHandler itemHandler;
    @NotNull
    private final AEFluidKeyStackHandler fluidHandler;

    @NotNull
    private LazyOptional<MEStorage> capabilityStorage = LazyOptional.empty();
    @NotNull
    private LazyOptional<ManaReceiver> capabilityMana = LazyOptional.empty();
    @SyncToClient(listener = "onStorageCapabilityAvailabilityChanged")
    private boolean storageCapabilityAvailable;

    public MEStorageHatch(MetaMachineBlockEntity holder) {
        super(holder);
        this.manaHandler = new AEManaKeyHandler();
        this.itemHandler = new AEItemKeyStackHandler(this);
        this.fluidHandler = new AEFluidKeyStackHandler(this);
        this.itemHandler.setCapabilityValidator(d -> isStorageCapabilityAvailable());
        this.fluidHandler.setCapabilityValidator(d -> isStorageCapabilityAvailable());
    }

    private boolean isStorageCapabilityAvailable() {
        return isRemote() ? storageCapabilityAvailable : capabilityStorage.isPresent();
    }

    private void updateStorageCapabilityAvailability() {
        boolean available = capabilityStorage.isPresent();
        if (storageCapabilityAvailable != available) {
            storageCapabilityAvailable = available;
            clearDirectionCache();
            requestSync();
        }
    }

    @SuppressWarnings("unused")
    private void onStorageCapabilityAvailabilityChanged(boolean newValue, boolean oldValue) {
        if (newValue != oldValue) {
            clearDirectionCache();
        }
    }

    @Override
    public @Nullable <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (capabilityStorage.isPresent()) {
            if (cap == Capabilities.STORAGE) {
                if (side == null || side == getFrontFacing()) {
                    return capabilityStorage.cast();
                }
            } else if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
                if (side == null || side == getFrontFacing()) {
                    return capabilityMana.cast();
                }
                return LazyOptional.empty();
            }
        }
        return null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        applyMarks();
        manaHandler.setPos(getPos());
        manaHandler.setLevel(getLevel());
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unbindHandlers();
        manaHandler.setLevel(null);
        capabilityStorage.invalidate();
        capabilityStorage = LazyOptional.empty();
        capabilityMana.invalidate();
        capabilityMana = LazyOptional.empty();
        storageCapabilityAvailable = false;
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof MultiblockMEStorageMachine machine) {
            bindHandlers(machine);
            var mana = machine.getCapability(BotaniaForgeCapabilities.MANA_RECEIVER, null);
            if (mana != null && mana.isPresent()) {
                manaHandler.setMap(machine.getKeyMap());
                manaHandler.setStorageSupplier(machine.getStorageSupplier());
                manaHandler.setCapacity(machine.getCapacity());
                manaHandler.setOnChange(machine.getOnChange());
                capabilityMana = LazyOptional.of(() -> manaHandler);
            }
            capabilityStorage = LazyOptional.of(() -> machine);
            updateStorageCapabilityAvailability();
            this.notifyNeighborsUpdate();
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        unbindHandlers();
        manaHandler.setMap(null);
        manaHandler.setStorageSupplier(null);
        manaHandler.setCapacity(0);
        manaHandler.setOnChange(null);
        capabilityStorage.invalidate();
        capabilityStorage = LazyOptional.empty();
        capabilityMana.invalidate();
        capabilityMana = LazyOptional.empty();
        updateStorageCapabilityAvailability();
        this.notifyNeighborsUpdate();
    }

    /**
     * 控制器容量变化后重读一次绑定（如抽屉存储器增减抽屉时会变）。
     * 处理器里的容量只是写入前的预检上限，真正的限制在控制器的写入逻辑里。
     */
    public void refreshStorageBinding() {
        for (var controller : getControllers()) {
            if (!(controller instanceof MultiblockMEStorageMachine machine)) continue;
            bindHandlers(machine);
            manaHandler.setCapacity(machine.getCapacity());
        }
    }

    /**
     * 绑定本仓的物品/流体处理器：它们与控制器共用同一份存储和容量，但各自带本仓的标记。
     * 保险库只支持物品或流体中的一种，另一种不绑也不当其能力，行为与改造前一致。
     */
    private void bindHandlers(MultiblockMEStorageMachine machine) {
        if (machine.getItemHandlerCap(null, false) != null) {
            itemHandler.setStorage(machine);
            itemHandler.setMap(machine.getKeyMap());
            itemHandler.setStorageSupplier(machine.getStorageSupplier());
            itemHandler.setOnChange(machine.getOnChange());
            itemHandler.setCapacity(machine.getCapacity());
            applyMarks();
        }
        if (machine.getFluidHandlerCap(null, false) != null) {
            fluidHandler.setStorage(machine);
            fluidHandler.setMap(machine.getKeyMap());
            fluidHandler.setStorageSupplier(machine.getStorageSupplier());
            fluidHandler.setOnChange(machine.getOnChange());
            fluidHandler.setCapacity(machine.getCapacity());
            applyMarks();
        }
    }

    /**
     * 解除绑定：处理器清空后外界拿到的是 0 槽/0 罐，也不再留已经拆掉的控制器引用。
     * 容量随机械方块数量变化，重新成型时 {@link #addedToController} 会重新绑定。
     */
    private void unbindHandlers() {
        clearHandler(itemHandler);
        clearHandler(fluidHandler);
    }

    private static void clearHandler(AEItemKeyStackHandler handler) {
        handler.setStorage(null);
        handler.setMap(null);
        handler.setStorageSupplier(null);
        handler.setOnChange(null);
        handler.setCapacity(0);
    }

    private static void clearHandler(AEFluidKeyStackHandler handler) {
        handler.setStorage(null);
        handler.setMap(null);
        handler.setStorageSupplier(null);
        handler.setOnChange(null);
        handler.setCapacity(0);
    }

    /** 把界面上的标记同步到处理器；标记为空即取消标记，恢复透传。 */
    private void applyMarks() {
        var stack = itemMark.getStackInSlot(0);
        itemHandler.setMark(stack.isEmpty() ? null : AEItemKey.of(stack));
        var fluid = fluidMark.getFluid();
        fluidHandler.setMark(fluid.isEmpty() ? null : AEFluidKey.of(fluid));
    }

    private void onMarkChanged() {
        if (isRemote()) return;
        applyMarks();
        onChanged();
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 152, 46);
        group.addWidget(new PhantomSlotWidget(itemMark, 0, 4, 4)
                .setChangeListener(this::onMarkChanged)
                .setBackground(GuiTextures.SLOT));
        group.addWidget(new LabelWidget(26, 9, () -> MARK_ITEM));
        group.addWidget(new PhantomFluidWidget(fluidMark, 0, 4, 26, 18, 18,
                fluidMark::getFluid,
                fluid -> fluidMark.setFluid(fluid.isEmpty() ? FluidStack.EMPTY : ICustomFluidStackHandler.copy(fluid, MARK_FLUID_AMOUNT)))
                .setChangeListener(this::onMarkChanged)
                .setShowAmount(false)
                .setBackground(GuiTextures.FLUID_SLOT));
        group.addWidget(new LabelWidget(26, 31, () -> MARK_FLUID));
        return group;
    }

    @Override
    public boolean canShared() {
        return false;
    }
}
