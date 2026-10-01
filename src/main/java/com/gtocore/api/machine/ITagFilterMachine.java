package com.gtocore.api.machine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.InfoIcon;
import com.gregtechceu.gtceu.uipro.elements.RichText;
import com.gregtechceu.gtceu.uiwidgets.filter.TagLookupView;

import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;
import java.util.function.Supplier;

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
     * 标签过滤配置：白名单、黑名单两个区块。
     *
     * <pre>
     *  ┌ 白名单 ─────────────────────┐
     *  │ [ 标签表达式输入框     ] [槽] │
     *  │ ┌ 槽里物品/流体的标签 ─────┐ │  ← 槽里有东西且带标签时才显示
     *  │ │ forge:ingots/iron        │ │     左键填入输入框、右键复制
     *  │ └──────────────────────────┘ │
     *  └──────────────────────────────┘
     *  ┌ 黑名单 …（同上）─────────────┐
     * </pre>
     *
     * 同步全部走组件自带的服务端下发：输入框与机器的标签字符串双向绑定（服务端值变了下发，玩家输入上行给 setter）；
     * 虚拟槽的内容由服务端记录；标签列表由服务端按槽里的内容算好、经 {@link RichText} 下发，点击标签时服务端校验后改机器字段。
     * 虚拟槽只是界面里的临时工具（查标签用），库存属于这一个打开的界面，不进机器。
     */
    @DataGeneratorScanned
    final class TagFilterUI {

        @RegisterLanguage(cn = "白名单", en = "Whitelist")
        private static final String WHITELIST = "gtocore.machine.tag_filter.whitelist";
        @RegisterLanguage(cn = "黑名单", en = "Blacklist")
        private static final String BLACKLIST = "gtocore.machine.tag_filter.blacklist";
        @RegisterLanguage(cn = "放入物品以查看其标签", en = "Insert an item to view its tags")
        private static final String LOOKUP_ITEM = "gtocore.machine.tag_filter.lookup_item";
        @RegisterLanguage(cn = "从EMI拖入流体以查看其标签", en = "Drag a fluid from EMI to view its tags")
        private static final String LOOKUP_FLUID = "gtocore.machine.tag_filter.lookup_fluid";
        @RegisterLanguage(cn = "仅用于查询，不影响过滤结果", en = "For lookup only; does not affect filtering")
        private static final String LOOKUP_ONLY = "gtocore.machine.tag_filter.lookup_only";

        private TagFilterUI() {}

        public static UIElement create(ITagFilterMachine machine) {
            boolean isItem = machine.isItemFilter();
            return Form.page().addChildren(
                    filterSection(WHITELIST, "tag_filter.whitelist", isItem, machine::getTagWhite, machine::setTagWhite),
                    filterSection(BLACKLIST, "tag_filter.blacklist", isItem, machine::getTagBlack, machine::setTagBlack));
        }

        /**
         * 一个名单的区块：标题、"输入框 + 虚拟槽"一行、槽里内容的标签列表。
         * {@code scrollerId} 是标签列表滚动区的固定 id（锁定高度按它记）。
         */
        private static UIElement filterSection(String titleKey, String scrollerId, boolean isItem, Supplier<String> getter, Consumer<String> setter) {
            var lookup = isItem ? TagLookupView.items(scrollerId, getter, setter, setter, LOOKUP_ITEM, LOOKUP_ONLY) :
                    TagLookupView.fluids(scrollerId, getter, setter, setter, LOOKUP_FLUID, LOOKUP_ONLY);
            var titleRow = Form.controlRow(titleKey, InfoIcon.info("gtocore.machine.tag_filter.tooltip.0", "gtocore.machine.tag_filter.tooltip.1"));
            return UIElement.section(LayoutStyle.AUTO).addChildren(titleRow, lookup);
        }
    }
}
