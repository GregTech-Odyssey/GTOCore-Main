package com.gtocore.common.machine.noenergy;

import com.gtocore.eio_travel.api.ITravelTarget;
import com.gtocore.eio_travel.implementations.AnchorTravelTarget;
import com.gtocore.eio_travel.logic.TravelSavedData;
import com.gtocore.eio_travel.logic.TravelUtils;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.PhantomItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;

import java.util.Optional;

@DataGeneratorScanned
public class TravelAnchorMachine extends MetaMachine implements IFancyUIMachine, IMachineLife {

    private static final int NAME_MAX_LENGTH = 32;
    private static final String PAGE_SCROLLER = "travel_anchor.page";

    @RegisterLanguage(cn = "锚点设置", en = "Anchor Settings")
    private static final String SETTINGS = "gtocore.travel_anchor.settings";
    @RegisterLanguage(cn = "名称", en = "Name")
    private static final String NAME = "gtocore.travel_anchor.name";
    @RegisterLanguage(cn = "手持旅行手杖时显示在锚点上方，留空则不显示", en = "Shown above the anchor while holding a Travel Staff; leave empty to hide")
    private static final String NAME_TIP = "gtocore.travel_anchor.name.tip";
    @RegisterLanguage(cn = "未命名", en = "Unnamed")
    private static final String UNNAMED = "gtocore.travel_anchor.unnamed";
    @RegisterLanguage(cn = "图标", en = "Icon")
    private static final String ICON = "gtocore.travel_anchor.icon";
    @RegisterLanguage(cn = "手持旅行手杖时显示在锚点前方，可从 EMI 拖入", en = "Shown in front of the anchor while holding a Travel Staff; can be dragged in from EMI")
    private static final String ICON_TIP = "gtocore.travel_anchor.icon.tip";
    @RegisterLanguage(cn = "在旅行网络中显示", en = "Show in Travel Network")
    private static final String VISIBLE = "gtocore.travel_anchor.visible";
    @RegisterLanguage(cn = "关闭后不再作为传送目标，开启了“激活隐藏节点”的旅行手杖除外", en = "When off, the anchor is no longer a teleport target, except for Travel Staffs with Include Hidden Nodes on")
    private static final String VISIBLE_TIP = "gtocore.travel_anchor.visible.tip";

    @SaveToDisk
    private final ItemStackTransfer itemTransfer;

    public TravelAnchorMachine(MetaMachineBlockEntity holder) {
        super(holder);
        itemTransfer = new ItemStackTransfer();
        itemTransfer.setOnContentsChanged(this::onInventoryContentsChanged);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isServerSide() && findTravelTarget().isEmpty()) {
            getOrCreateTravelTarget();
            requestTravelResync();
        }
    }

    @Override
    public void onMachineRemoved() {
        IMachineLife.super.onMachineRemoved();
        if (getLevel() != null) {
            TravelSavedData.getTravelData(getLevel()).removeTravelTargetAt(getLevel(), getPos());
        }
    }

    @Override
    public Widget createUIWidget() {
        var name = TextField.of(UISizes.INLINE_ADJUSTER_WIDTH, this::getName, this::setName).setPlaceholder(Component.translatable(UNNAMED));
        name.getInput().setMaxStringLength(NAME_MAX_LENGTH);
        name.tooltips(NAME_TIP);
        var icon = PhantomItemSlot.of(itemTransfer, 0).xeiPhantom();
        icon.setMaxStackSize(1);
        var section = Form.section(SETTINGS).addChildren(
                Form.controlRow(NAME, name, NAME_TIP),
                Form.controlRow(UISizes.SLOT_SIZE, ICON, icon, ICON_TIP),
                Form.controlRow(VISIBLE, Switch.of(this::getVisibility, this::setVisibility), VISIBLE_TIP));
        var scroller = ScrollerView.page(PAGE_SCROLLER, UISizes.CONTENT_WIDTH).adaptiveWidth();
        scroller.addScrollViewChild(Form.page().addChild(section));
        return scroller;
    }

    private void onInventoryContentsChanged() {
        if (isServerSide()) setIcon(itemTransfer.getStackInSlot(0).getItem());
    }

    public String getName() {
        return findTravelTarget().map(AnchorTravelTarget::getName).orElse("");
    }

    public void setName(String name) {
        getOrCreateTravelTarget().setName(name);
        requestTravelResync();
    }

    public void setIcon(Item icon) {
        getOrCreateTravelTarget().setIcon(icon);
        requestTravelResync();
    }

    public boolean getVisibility() {
        return findTravelTarget().map(AnchorTravelTarget::getVisibility).orElse(false);
    }

    public void setVisibility(boolean visible) {
        getOrCreateTravelTarget().setVisibility(visible);
        requestTravelResync();
    }

    private boolean isServerSide() {
        return getLevel() != null && !getLevel().isClientSide;
    }

    private Optional<AnchorTravelTarget> findTravelTarget() {
        Optional<ITravelTarget> travelTarget = getTravelData().getTravelTarget(getPos());
        return travelTarget.filter(AnchorTravelTarget.class::isInstance).map(AnchorTravelTarget.class::cast);
    }

    private AnchorTravelTarget getOrCreateTravelTarget() {
        var existing = findTravelTarget();
        if (existing.isPresent()) {
            return existing.get();
        }
        AnchorTravelTarget anchorTravelTarget = new AnchorTravelTarget(getPos(), "", Items.AIR, false);
        getTravelData().addTravelTarget(getLevel(), anchorTravelTarget);
        return anchorTravelTarget;
    }

    public TravelSavedData getTravelData() {
        return TravelSavedData.getTravelData(getLevel());
    }

    private void requestTravelResync() {
        if (isServerSide()) {
            TravelUtils.requireResync(getLevel());
        }
    }
}
