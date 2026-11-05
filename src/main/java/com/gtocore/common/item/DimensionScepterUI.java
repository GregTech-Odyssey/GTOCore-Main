package com.gtocore.common.item;

import com.gtocore.common.item.DimensionScepterBookmarks.Bookmark;
import com.gtocore.eio_travel.implementations.AnchorTravelTarget;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.data.GTODimensions;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.InfoIcon;
import com.gregtechceu.gtceu.uipro.elements.ServerList;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.datastream.codec.ByteBufCodecs;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 维度权杖的旅行锚收藏与附近锚点列表。收藏项保存在权杖物品 NBT 中，UI 只负责增删和选择。
 */
@DataGeneratorScanned
public final class DimensionScepterUI {

    @RegisterLanguage(cn = "收藏的旅行锚", en = "Saved Travel Anchors")
    private static final String BOOKMARKS = "gtocore.dimension_scepter.bookmarks";
    @RegisterLanguage(cn = "点击选中收藏的锚，再右键权杖传送到该锚；选中后不受瞄准角度和传送距离限制，跨维度同样可用", en = "Select a saved anchor, then right-click the scepter to teleport there. The selected anchor ignores aim angle and item range, including across dimensions")
    private static final String BOOKMARKS_TIP = "gtocore.dimension_scepter.bookmarks.tip";
    @RegisterLanguage(cn = "已选中：%s", en = "Selected: %s")
    private static final String BOOKMARKS_SELECTED = "gtocore.dimension_scepter.bookmarks.selected";
    @RegisterLanguage(cn = "未选中，右键不会传送", en = "Nothing selected; right-click does nothing")
    private static final String BOOKMARKS_NONE_SELECTED = "gtocore.dimension_scepter.bookmarks.none_selected";
    @RegisterLanguage(cn = "尚未收藏旅行锚", en = "No saved travel anchors")
    private static final String BOOKMARKS_EMPTY = "gtocore.dimension_scepter.bookmarks.empty";
    @RegisterLanguage(cn = "选中 / 取消选中", en = "Select / deselect")
    private static final String BOOKMARK_SELECT = "gtocore.dimension_scepter.bookmark.select";
    @RegisterLanguage(cn = "传送到该收藏", en = "Teleport to this anchor")
    private static final String BOOKMARK_TELEPORT = "gtocore.dimension_scepter.bookmark.teleport";
    @RegisterLanguage(cn = "没有选中的收藏锚", en = "No saved anchor selected")
    private static final String BOOKMARK_NO_SELECTION = "gtocore.dimension_scepter.bookmark.no_selection";
    @RegisterLanguage(cn = "移除收藏", en = "Remove bookmark")
    private static final String BOOKMARK_REMOVE = "gtocore.dimension_scepter.bookmark.remove";
    @RegisterLanguage(cn = "未命名", en = "Unnamed")
    private static final String BOOKMARK_UNNAMED = "gtocore.dimension_scepter.bookmark.unnamed";
    @RegisterLanguage(cn = "该旅行锚已不可用", en = "This travel anchor is no longer available")
    private static final String BOOKMARK_UNAVAILABLE = "gtocore.dimension_scepter.bookmark.unavailable";
    @RegisterLanguage(cn = "收藏已满（最多 %s 个）", en = "Bookmark list full (up to %s)")
    private static final String BOOKMARK_FULL = "gtocore.dimension_scepter.bookmark.full";
    @RegisterLanguage(cn = "收藏", en = "Save")
    private static final String BOOKMARK_ADD = "gtocore.dimension_scepter.bookmark.add";
    @RegisterLanguage(cn = "附近的旅行锚", en = "Nearby Travel Anchors")
    private static final String NEARBY = "gtocore.dimension_scepter.nearby";
    @RegisterLanguage(cn = "当前维度、权杖传送范围内已命名的旅行锚", en = "Named travel anchors in the current dimension and within the scepter's travel range")
    private static final String NEARBY_TIP = "gtocore.dimension_scepter.nearby.tip";
    @RegisterLanguage(cn = "从附近已命名的旅行锚中选择要收藏的锚", en = "Pick a named travel anchor nearby to save it")
    private static final String NEARBY_HINT = "gtocore.dimension_scepter.nearby.hint";
    @RegisterLanguage(cn = "附近没有可收藏的旅行锚", en = "No travel anchors nearby to save")
    private static final String NEARBY_EMPTY = "gtocore.dimension_scepter.nearby.empty";
    @RegisterLanguage(cn = "%s · %s, %s, %s", en = "%s · %s, %s, %s")
    private static final String LOCATION = "gtocore.dimension_scepter.location";
    @RegisterLanguage(cn = "%s, %s, %s · %s 格", en = "%s, %s, %s · %s blocks")
    private static final String NEARBY_LOCATION = "gtocore.dimension_scepter.nearby.location";

    private static final Component BOOKMARKS_LABEL = Component.translatable(BOOKMARKS);
    private static final Component BOOKMARKS_TIP_C = Component.translatable(BOOKMARKS_TIP);
    private static final Component BOOKMARKS_NONE_SELECTED_C = Component.translatable(BOOKMARKS_NONE_SELECTED);
    private static final Component BOOKMARKS_EMPTY_C = Component.translatable(BOOKMARKS_EMPTY);
    private static final Component BOOKMARK_SELECT_C = Component.translatable(BOOKMARK_SELECT);
    private static final Component BOOKMARK_TELEPORT_C = Component.translatable(BOOKMARK_TELEPORT);
    private static final Component BOOKMARK_NO_SELECTION_C = Component.translatable(BOOKMARK_NO_SELECTION);
    private static final Component BOOKMARK_REMOVE_C = Component.translatable(BOOKMARK_REMOVE);
    private static final Component BOOKMARK_UNNAMED_C = Component.translatable(BOOKMARK_UNNAMED);
    private static final Component BOOKMARK_UNAVAILABLE_C = Component.translatable(BOOKMARK_UNAVAILABLE);
    private static final Component NEARBY_LABEL = Component.translatable(NEARBY);
    private static final Component NEARBY_TIP_C = Component.translatable(NEARBY_TIP);
    private static final Component NEARBY_HINT_C = Component.translatable(NEARBY_HINT);
    private static final Component NEARBY_EMPTY_C = Component.translatable(NEARBY_EMPTY);
    private static final Component BOOKMARK_ADD_C = Component.translatable(BOOKMARK_ADD);
    private static final Component BOOKMARK_FULL_C = Component.translatable(BOOKMARK_FULL, DimensionScepterBookmarks.MAX_BOOKMARKS);

    private DimensionScepterUI() {}

    public static UIElement bookmarksSection(HeldItemUIFactory.HeldItemHolder holder) {
        var section = Form.section(BOOKMARKS_LABEL);
        var selection = TextLine.of(0, () -> {
            Bookmark bookmark = DimensionScepterBookmarks.selected(holder.getHeld());
            return bookmark == null ? BOOKMARKS_NONE_SELECTED_C : Component.translatable(BOOKMARKS_SELECTED, bookmarkName(holder.player.level(), bookmark));
        }).bindClientColor(UITheme::panelText);
        selection.layout(l -> l.flex(1));
        var teleport = Button.icon(UITheme.ARROW_RIGHT)
                .setOnServerClick(() -> {
                    int index = DimensionScepterBookmarks.selectedIndex(holder.getHeld());
                    if (index >= 0) DimensionScepterBehavior.teleportToBookmark(holder.player, holder.getHeld(), index);
                })
                .disabled(() -> DimensionScepterBookmarks.selected(holder.getHeld()) == null, BOOKMARK_NO_SELECTION_C);
        teleport.setHoverTooltips(BOOKMARK_TELEPORT_C);
        section.addChild(UIElement.centeredRow(UISizes.CONTROL_HEIGHT)
                .addChildren(selection, InfoIcon.info(BOOKMARKS_TIP_C), teleport));

        var list = ServerList.of(ByteBufCodecs.VAR_INT, () -> indices(DimensionScepterBookmarks.count(holder.getHeld())),
                index -> bookmarkRow(holder, index))
                .version(() -> DimensionScepterBookmarks.signature(holder.getHeld()))
                .emptyText(BOOKMARKS_EMPTY_C);
        list.rowHeight(UISizes.SLOT_SIZE);
        section.addChild(list);
        return section;
    }

    public static UIElement nearbySection(HeldItemUIFactory.HeldItemHolder holder) {
        var section = Form.section(NEARBY_LABEL);
        var hint = TextLine.constant(0, NEARBY_HINT_C).bindClientColor(UITheme::textSecondary);
        hint.layout(l -> l.flex(1));
        section.addChild(UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(hint, InfoIcon.info(NEARBY_TIP_C)));

        var list = ServerList.of(ByteBufCodecs.VAR_LONG, () -> candidateKeys(holder),
                pos -> candidateRow(holder, BlockPos.of(pos)))
                .version(() -> DimensionScepterBookmarks.signature(holder.getHeld()))
                .rescanEvery(20)
                .emptyText(NEARBY_EMPTY_C);
        list.rowHeight(UISizes.SLOT_SIZE);
        section.addChild(list);
        return section;
    }

    private static UIElement bookmarkRow(HeldItemUIFactory.HeldItemHolder holder, int index) {
        var mark = TextLine.of(10, () -> Component.literal(DimensionScepterBookmarks.selectedIndex(holder.getHeld()) == index ? "▶" : " "))
                .bindClientColor(UITheme::panelText);
        var name = TextLine.of(LayoutStyle.AUTO, () -> {
            Bookmark bookmark = DimensionScepterBookmarks.get(holder.getHeld(), index);
            return bookmark == null ? BOOKMARK_UNAVAILABLE_C : bookmarkName(holder.player.level(), bookmark);
        }).bindClientColor(UITheme::panelText);
        var location = TextLine.of(LayoutStyle.AUTO, () -> {
            Bookmark bookmark = DimensionScepterBookmarks.get(holder.getHeld(), index);
            return bookmark == null ? Component.empty() : location(bookmark);
        }).bindClientColor(UITheme::textSecondary);
        var text = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(0).flex(1)).addChildren(name, location);

        var select = Button.glyph("✓")
                .setOnServerClick(() -> DimensionScepterBookmarks.toggleSelected(holder.getHeld(), index));
        select.setHoverTooltips(BOOKMARK_SELECT_C);
        var teleport = Button.icon(UITheme.ARROW_RIGHT)
                .setOnServerClick(() -> DimensionScepterBehavior.teleportToBookmark(holder.player, holder.getHeld(), index));
        teleport.setHoverTooltips(BOOKMARK_TELEPORT_C);
        var remove = Button.glyph("×").setVariant(UITheme.ButtonVariant.DANGER)
                .setOnServerClick(() -> DimensionScepterBookmarks.remove(holder.getHeld(), index));
        remove.setHoverTooltips(BOOKMARK_REMOVE_C);
        return row(mark, text, select, teleport, remove);
    }

    private static UIElement candidateRow(HeldItemUIFactory.HeldItemHolder holder, BlockPos pos) {
        var name = TextLine.of(LayoutStyle.AUTO, () -> {
            AnchorTravelTarget target = DimensionScepterBookmarks.findCandidate(holder.player.level(), holder.getHeld(), holder.player, pos);
            return target == null ? BOOKMARK_UNAVAILABLE_C : Component.literal(target.getName());
        }).bindClientColor(UITheme::panelText);
        var location = TextLine.of(LayoutStyle.AUTO, () -> {
            AnchorTravelTarget target = DimensionScepterBookmarks.findCandidate(holder.player.level(), holder.getHeld(), holder.player, pos);
            return target == null ? Component.empty() : nearbyLocation(holder, target);
        }).bindClientColor(UITheme::textSecondary);
        var text = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(0).flex(1)).addChildren(name, location);

        var add = Button.glyph("+")
                .setOnServerClick(() -> addCandidate(holder, pos))
                .disabled(() -> DimensionScepterBookmarks.isFull(holder.getHeld()), BOOKMARK_FULL_C);
        add.setHoverTooltips(BOOKMARK_ADD_C);
        return row(null, text, add);
    }

    private static UIElement row(@Nullable UIElement mark, UIElement text, Button... buttons) {
        var row = UIElement.centeredRow(UISizes.SLOT_SIZE).layout(l -> l.gapAll(UISizes.GAP));
        if (mark != null) row.addChild(mark);
        row.addChild(text);
        if (buttons != null) row.addChildren(buttons);
        return row;
    }

    private static void addCandidate(HeldItemUIFactory.HeldItemHolder holder, BlockPos pos) {
        AnchorTravelTarget target = DimensionScepterBookmarks.findCandidate(holder.player.level(), holder.getHeld(), holder.player, pos);
        if (target != null) DimensionScepterBookmarks.add(holder.getHeld(), holder.player.level(), target);
    }

    private static List<Integer> indices(int count) {
        if (count <= 0) return Collections.emptyList();
        var result = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) result.add(i);
        return result;
    }

    private static List<Long> candidateKeys(HeldItemUIFactory.HeldItemHolder holder) {
        var candidates = DimensionScepterBookmarks.candidates(holder.player.level(), holder.getHeld(), holder.player.blockPosition());
        if (candidates.isEmpty()) return Collections.emptyList();
        var result = new ArrayList<Long>(candidates.size());
        for (AnchorTravelTarget candidate : candidates) result.add(candidate.getPos().asLong());
        return result;
    }

    public static Component bookmarkName(@Nullable Level level, Bookmark bookmark) {
        String name = DimensionScepterBookmarks.displayName(level, bookmark);
        return name.isBlank() ? BOOKMARK_UNNAMED_C : Component.literal(name);
    }

    private static Component location(Bookmark bookmark) {
        return Component.translatable(LOCATION, Component.translatable(GTODimensions.getTranslationKey(bookmark.dimension())),
                bookmark.pos().getX(), bookmark.pos().getY(), bookmark.pos().getZ());
    }

    private static Component nearbyLocation(HeldItemUIFactory.HeldItemHolder holder, AnchorTravelTarget target) {
        BlockPos pos = target.getPos();
        int distance = (int) Math.round(Math.sqrt(pos.distSqr(holder.player.blockPosition())));
        return Component.translatable(NEARBY_LOCATION, pos.getX(), pos.getY(), pos.getZ(), distance);
    }
}
