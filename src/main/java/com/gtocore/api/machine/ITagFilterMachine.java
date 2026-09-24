package com.gtocore.api.machine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.PhantomFluidSlot;
import com.gregtechceu.gtceu.uipro.elements.PhantomItemSlot;
import com.gregtechceu.gtceu.uipro.elements.RichText;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.tags.TagKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ITagFilterMachine extends IDropSaveMachine {

    String getTagWhite();

    String getTagBlack();

    /** 过滤的是物品（true）还是流体（false），决定配置界面放物品槽还是流体槽。 */
    boolean isItemFilter();

    void setTagWhite(String tagWhite);

    void setTagBlack(String tagBlack);

    @Override
    default void saveToItem(CompoundTag tag) {
        IDropSaveMachine.super.saveToItem(tag);
        tag.putString("TagWhite", getTagWhite());
        tag.putString("TagBlack", getTagBlack());
    }

    @Override
    default void loadFromItem(CompoundTag tag) {
        IDropSaveMachine.super.loadFromItem(tag);
        if (tag.contains("TagWhite")) {
            setTagWhite(tag.getString("TagWhite"));
        }
        if (tag.contains("TagBlack")) {
            setTagBlack(tag.getString("TagBlack"));
        }
    }

    /**
     * 标签过滤配置（机器左侧小组件）：白名单、黑名单两个区块，末尾两行表达式语法说明。
     *
     * <pre>
     *  ┌ 白名单 ─────────────────────┐
     *  │ [ 标签表达式输入框     ] [槽] │
     *  │ ┌ 槽里物品/流体的标签 ─────┐ │  ← 槽里有东西且带标签时才显示
     *  │ │ forge:ingots/iron        │ │     左键填入输入框、右键复制
     *  │ └──────────────────────────┘ │
     *  └──────────────────────────────┘
     *  ┌ 黑名单 …（同上）─────────────┐
     *  * 表示通配符 () 表示优先
     *  &amp; = 逻辑与 | = 逻辑或 ^ = 逻辑异或
     * </pre>
     *
     * 同步全部走组件自带的服务端下发：输入框与机器的标签字符串双向绑定（服务端值变了下发，玩家输入上行给 setter）；
     * 虚拟槽的内容由服务端记录；标签列表由服务端按槽里的内容算好、经 {@link RichText} 下发，点击标签时服务端校验后改机器字段。
     * 虚拟槽只是界面里的临时工具（查标签用），库存属于这一个打开的界面，不进机器。
     */
    @DataGeneratorScanned
    class FilterIFancyConfigurator implements IFancyConfigurator {

        @RegisterLanguage(cn = "白名单", en = "Whitelist")
        private static final String WHITELIST = "gtocore.machine.tag_filter.whitelist";
        @RegisterLanguage(cn = "黑名单", en = "Blacklist")
        private static final String BLACKLIST = "gtocore.machine.tag_filter.blacklist";

        /// 标签列表最多显示几行，再多滚动（行距与状态行相同）
        private static final int TAG_LIST_MAX_LINES = 6;

        private final ITagFilterMachine machine;

        public FilterIFancyConfigurator(ITagFilterMachine machine) {
            this.machine = machine;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtocore.machine.tag_filter.tag_config_title");
        }

        @Override
        public IGuiTexture getIcon() {
            return WidgetIcons.FILTER;
        }

        @Override
        public Widget createConfigurator() {
            boolean isItem = machine.isItemFilter();
            return UIElement.column(UISizes.CONTENT_WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP)).addChildren(
                    filterSection(WHITELIST, "tag_filter.whitelist", isItem, machine::getTagWhite, machine::setTagWhite),
                    filterSection(BLACKLIST, "tag_filter.blacklist", isItem, machine::getTagBlack, machine::setTagBlack),
                    Label.translatable("gtocore.machine.tag_filter.tooltip.0", UISizes.CONTENT_WIDTH).setColor(UITheme.TEXT_SECONDARY),
                    Label.translatable("gtocore.machine.tag_filter.tooltip.1", UISizes.CONTENT_WIDTH).setColor(UITheme.TEXT_SECONDARY));
        }

        /**
         * 一个名单的区块：标题、"输入框 + 虚拟槽"一行、槽里内容的标签列表。
         * {@code scrollerId} 是标签列表滚动区的固定 id（锁定高度按它记）。
         */
        private UIElement filterSection(String titleKey, String scrollerId, boolean isItem, Supplier<String> getter, Consumer<String> setter) {
            var tags = new TagList();
            Widget slot;
            if (isItem) {
                var handler = new ItemStackTransfer(1);
                handler.setOnContentsChanged(tags::markDirty);
                tags.source = () -> handler.getStackInSlot(0).getTags().map(t -> t);
                var itemSlot = new PhantomItemSlot(handler, 0).xeiPhantom();
                // 只用来查标签，记一个就够
                itemSlot.setMaxStackSize(1);
                slot = itemSlot;
            } else {
                var tank = new CustomFluidTank(1);
                tank.setOnContentsChanged(tags::markDirty);
                tags.source = () -> tank.getFluid().getFluid().defaultFluidState().getTags().map(t -> t);
                slot = new PhantomFluidSlot(tank, 0, tank::getFluid, tank::setFluid).xeiPhantom();
            }

            var field = new TextField(0, getter, setter);
            field.layout(l -> l.flexGrow(1));
            var inputRow = UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.GAP).alignCenter()).addChildren(field, slot);

            var text = new RichText();
            // 文字只由服务端取值下发（客户端不自己算，与多方块显示窗相同）
            text.textSupplier(machine.self().isRemote() ? null : tags::appendLines);
            text.clickHandler((tag, click) -> onTagClicked(tags, setter, tag, click));
            var scroller = new ScrollerView(scrollerId, UISizes.CONTENT_WIDTH - 2 * UITheme.PANEL_PADDING, StatusLine.HEIGHT)
                    .adaptiveHeight(TAG_LIST_MAX_LINES * StatusLine.HEIGHT + 2 * UITheme.PANEL_PADDING)
                    .layoutContent(l -> l.paddingAll(UITheme.PANEL_PADDING));
            // 标签可点（左键填入、右键复制），用可操作区块的底，不用只读的状态显示窗
            scroller.setBackground(UITheme.PANEL);
            scroller.addScrollViewChild(text);
            var tagList = UIElement.column(LayoutStyle.AUTO).addChild(scroller);
            // 槽里没东西（或没有标签）时不占位置；是否显示由服务端判定、经区块的同步值下发，两端控件树不变
            tagList.setDisplay(false);

            var section = UIElement.section(LayoutStyle.AUTO);
            section.addSyncValue(SyncValue.of(tags::hasTags, SyncValue.BOOLEAN, false).onChanged(tagList::setDisplay));
            return section.addChildren(TextLine.translatable(LayoutStyle.AUTO, titleKey).setColor(UITheme.PANEL_TEXT), inputRow, tagList);
        }

        /** 点击标签：两端各调用一次。客户端右键复制；服务端左键把标签填进名单（先确认它确实是槽里内容的标签）。 */
        private static void onTagClicked(TagList tags, Consumer<String> setter, String tag, ClickData click) {
            if (click.isRemote) {
                if (click.button == 1) copyToClipboard(tag);
            } else if (click.button == 0 && tags.contains(tag)) {
                setter.accept(tag);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private static void copyToClipboard(String text) {
            Minecraft.getInstance().keyboardHandler.setClipboard(text);
        }

        /** 服务端：虚拟槽里内容的标签，只在槽内容变化后重算一次。 */
        private static final class TagList {

            private Supplier<Stream<TagKey<?>>> source = Stream::empty;
            private boolean dirty = true;
            private List<String> tags = Collections.emptyList();
            private List<Component> lines = Collections.emptyList();

            void markDirty() {
                dirty = true;
            }

            private void refresh() {
                if (!dirty) return;
                dirty = false;
                var names = source.get().map(tag -> tag.location().toString()).toList();
                var hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("cover.tag_filter.tag_entry.tooltip"));
                var newLines = new ArrayList<Component>(names.size());
                for (var name : names) {
                    newLines.add(ComponentPanelWidget.withButton(Component.literal(name), name).copy().withStyle(s -> s.withHoverEvent(hover)));
                }
                tags = names;
                lines = newLines;
            }

            boolean hasTags() {
                refresh();
                return !tags.isEmpty();
            }

            boolean contains(String tag) {
                refresh();
                return tags.contains(tag);
            }

            void appendLines(List<Component> out) {
                refresh();
                out.addAll(lines);
            }
        }
    }
}
