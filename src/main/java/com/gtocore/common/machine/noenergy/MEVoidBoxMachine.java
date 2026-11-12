package com.gtocore.common.machine.noenergy;

import com.gtolib.api.ae2.storage.VoidBoxCondensation;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.PhantomFluidSlot;
import com.gregtechceu.gtceu.uipro.elements.PhantomItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeySet;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.core.definitions.AEItems;

import com.gto.datasynclib.annotations.AdditionalHolder;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.function.IntFunction;

/**
 * ME 虚空箱子：接入 ME 网络后把送进来的物品与流体销毁，标了什么就只销毁什么，什么都没标记就全部销毁。
 * <p>
 * 默认优先级拉满，入网内容先落到本机，避免被别的存储收下；优先级可以在界面里改。
 */
@DataGeneratorScanned
public final class MEVoidBoxMachine extends MetaMachine implements IGridConnectedMachine, MEStorage, IStorageProvider, IFancyUIMachine {

    private static final int MARK_SLOTS = UISizes.SLOTS_PER_ROW << 3;
    private static final int MARK_FLUID_AMOUNT = 1000;
    private static final String PAGE_SCROLLER = "me_void_box.page";
    private static final int PRIORITY_MIN = Integer.MIN_VALUE + 1;
    private static final int PRIORITY_MAX = Integer.MAX_VALUE - 1;
    private static final Lazy<AEItemKey> MATTER_BALL = Lazy.of(() -> AEItemKey.of(AEItems.MATTER_BALL.asItem()));
    private static final Lazy<AEItemKey> SINGULARITY = Lazy.of(() -> AEItemKey.of(AEItems.SINGULARITY.asItem()));

    @RegisterLanguage(cn = "产出模式", en = "Output Mode")
    private static final Component OUTPUT_MODE = Component.translatable("gtocore.machine.me_void_box.output_mode");
    @RegisterLanguage(cn = "仅销毁", en = "Void Only")
    private static final Component VOID_ONLY = Component.translatable("gtocore.machine.me_void_box.void_only");
    @RegisterLanguage(cn = "本机存储：%s 个物质球，%s 个奇点", en = "Stored here: %s matter balls, %s singularities")
    private static final String STORED = "gtocore.machine.me_void_box.stored";

    private final KeyCounter outputs = new KeyCounter();
    private boolean outputsDirty = true;
    @AdditionalHolder
    private final VoidBoxCondensation condensation = new VoidBoxCondensation(this::onOutputChanged, AEKeyTypes.FLUIDS.getAmountPerOperation());

    @RegisterLanguage(cn = "销毁标记", en = "Void Filter")
    private static final Component MARKS = Component.translatable("gtocore.machine.me_void_box.marks");
    @RegisterLanguage(cn = "标记物品", en = "Marked Items")
    private static final Component ITEM_MARK = Component.translatable("gtocore.machine.me_void_box.item_mark");
    @RegisterLanguage(cn = "标记流体", en = "Marked Fluids")
    private static final Component FLUID_MARK = Component.translatable("gtocore.machine.me_void_box.fluid_mark");
    @RegisterLanguage(cn = "空着就销毁全部", en = "Void everything when nothing is marked")
    private static final Component VOID_ALL = Component.translatable("gtocore.machine.me_void_box.void_all");

    private static final Component PRIORITY = Component.translatable("gui.ae2.Priority");
    private static final Component PRIORITY_EXTRACTION_HINT = Component.translatable("gui.ae2.PriorityExtractionHint");
    private static final Component PRIORITY_INSERTION_HINT = Component.translatable("gui.ae2.PriorityInsertionHint");

    @SaveToDisk
    private final KeyInventory<AEItemKey> itemMarks;

    @SaveToDisk
    private final KeyInventory<AEFluidKey> fluidMarks;
    @SaveToDisk
    private final GridNodeHolder nodeHolder;
    @SaveToDisk(defaultValue = "-2147483646")
    private int priority = PRIORITY_MIN;
    @SyncToClient
    private boolean isOnline;
    private final AEKeySet<AEKey> marked = new AEKeySet<>();

    public MEVoidBoxMachine(MetaMachineBlockEntity holder) {
        super(holder);
        this.nodeHolder = new GridNodeHolder(this);
        this.itemMarks = KeyInventory.items(MARK_SLOTS, 1, true);
        this.fluidMarks = KeyInventory.fluids(MARK_SLOTS, MARK_FLUID_AMOUNT);
        getMainNode().addService(IStorageProvider.class, this);
    }

    @Override
    public MEStorage getStorageCap(net.minecraft.core.Direction side) {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        outputsDirty = true;
        rebuildMarks();
    }

    private void setItemMark(int slot, ItemStack stack) {
        if (isRemote()) return;
        itemMarks.set(slot, stack.isEmpty() ? null : AEItemKey.of(stack), stack.isEmpty() ? 0 : 1);
        rebuildMarks();
    }

    private void setFluidMark(int slot, FluidStack stack) {
        if (isRemote()) return;
        fluidMarks.set(slot, stack.isEmpty() ? null : Keys.fluid(stack), stack.isEmpty() ? 0 : 1);
        rebuildMarks();
    }

    private void rebuildMarks() {
        marked.clear();
        collect(itemMarks, marked);
        collect(fluidMarks, marked);
        onChanged();
    }

    private static void collect(KeyInventory<? extends AEKey> marks, AEKeySet<AEKey> out) {
        for (int i = 0; i < marks.size(); i++) {
            var key = marks.keyAt(i);
            if (key != null) out.add(key);
        }
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storageMounts.mount(this, priority);
    }

    @Override
    public Component getDescription() {
        return getDefinition().asItem().getDescription();
    }

    @Override
    public boolean supportsKeyType(AEKeyType<?> aeKeyType) {
        return aeKeyType == AEKeyTypes.ITEMS || aeKeyType == AEKeyTypes.FLUIDS;
    }

    @Override
    public int operationTime() {
        return 1;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0 || !supportsKeyType(what.getType()) || !marked.isEmpty() && !marked.containKey(what)) return 0;
        if (mode == Actionable.MODULATE) condensation.add(amount, what.getAmountPerOperation());
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0 || what != MATTER_BALL.get() && what != SINGULARITY.get()) return 0;
        return condensation.extract(what == SINGULARITY.get(), amount, mode == Actionable.SIMULATE);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        out.addAll(getAvailableStacks());
    }

    @Override
    public KeyCounter getAvailableStacks() {
        if (outputsDirty) {
            outputsDirty = false;
            outputs.clear();
            collectOutputs(outputs);
        }
        return outputs;
    }

    private void collectOutputs(KeyCounter out) {
        long balls = condensation.getAmount(false);
        long singularities = condensation.getAmount(true);
        if (balls > 0) out.add(MATTER_BALL.get(), balls);
        if (singularities > 0) out.add(SINGULARITY.get(), singularities);
    }

    private void onOutputChanged() {
        outputsDirty = true;
        onChanged();
    }

    private int getPriority() {
        return priority;
    }

    private void setPriority(int value) {
        if (value == priority) return;
        priority = value;
        if (!isRemote()) IStorageProvider.requestUpdate(getMainNode());
        onChanged();
    }

    @Override
    public Widget createUIWidget() {
        var page = Form.page();
        page.addChild(Form.choiceRow(OUTPUT_MODE, 3, i -> switch (i) {
            case 1 -> AEItems.MATTER_BALL.asItem().getDescription();
            case 2 -> AEItems.SINGULARITY.asItem().getDescription();
            default -> VOID_ONLY;
        }, condensation::getMode, condensation::setMode));
        page.addChild(Form.label(() -> Component.translatable(STORED, condensation.getAmount(false), condensation.getAmount(true))));
        page.addChild(Form.numberRow(PRIORITY, NumberField.ofInt(LayoutStyle.AUTO, this::getPriority, this::setPriority, PRIORITY_MIN, PRIORITY_MAX),
                PRIORITY_EXTRACTION_HINT, PRIORITY_INSERTION_HINT));
        var section = Form.section(MARKS);
        section.addChild(Form.label(ITEM_MARK));
        addSlots(section, MARK_SLOTS, i -> PhantomItemSlot.of(() -> Keys.displayStack(itemMarks.keyAt(i)),
                stack -> setItemMark(i, stack), stack -> true).xeiPhantom());
        section.addChild(Form.label(FLUID_MARK));
        addSlots(section, MARK_SLOTS, i -> PhantomFluidSlot.of(null, 0, () -> Keys.displayFluid(fluidMarks.keyAt(i)),
                stack -> setFluidMark(i, stack)).xeiPhantom());
        section.addChild(Form.label(VOID_ALL));
        page.addChild(section);
        var scroller = ScrollerView.page(PAGE_SCROLLER, UISizes.CONTENT_WIDTH).adaptiveWidth();
        scroller.addScrollViewChild(page);
        return scroller;
    }

    private static void addSlots(UIElement section, int count, IntFunction<Widget> factory) {
        for (int start = 0; start < count; start += UISizes.SLOTS_PER_ROW) {
            var row = UIElement.row(UISizes.SLOT_SIZE);
            for (int i = start; i < Math.min(count, start + UISizes.SLOTS_PER_ROW); i++) {
                row.addChild(factory.apply(i));
            }
            section.addChild(row);
        }
    }
}
