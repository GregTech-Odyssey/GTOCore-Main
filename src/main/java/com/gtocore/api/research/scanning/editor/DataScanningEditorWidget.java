package com.gtocore.api.research.scanning.editor;

import com.gtocore.api.research.ResearchTag;

import com.gtolib.GTOCore;
import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.SwitchedContent;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.elements.ae.PhantomAEKeySlot;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.network.chat.Component;

import appeng.api.identity.IdentityKeySet;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.FlexWrap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/** 扫描编辑草稿只保存在当前界面的服务端实例里，控件值和可变长结构均由 uipro 同步。 */
final class DataScanningEditorWidget extends UIElement {

    private static final int WIDTH = 2 * UISizes.SLOT_ROW_WIDTH;

    private final ObjectArrayList<AEKey> targets = new ObjectArrayList<>();
    private final ObjectArrayList<PointEntry> points = new ObjectArrayList<>();
    private final ResearchTag[] tags = new ResearchTag[ResearchTag.TAGS.keys().size()];

    DataScanningEditorWidget() {
        layout(l -> l.column().width(WIDTH));
        for (int i = 0; i < tags.length; i++) tags[i] = ResearchTag.TAGS.get(i);

        var page = Form.page().layout(l -> l.width(WIDTH));
        page.addChild(section("Scan targets", new SwitchedContent(targets::size, (count, remote) -> targetSlots(count))));

        var add = Button.of(LayoutStyle.AUTO).bindClientText(() -> "Add research points")
                .setOnServerClick(this::addResearchPoint).disabled(() -> points.size() >= tags.length, null);
        page.addChild(section("Research points", add, new SwitchedContent(points::size, (count, remote) -> pointRows(count))));
        page.addChild(Button.of(LayoutStyle.AUTO).bindClientText(() -> "Export Java to log")
                .setVariant(UITheme.ButtonVariant.CONFIRM).setOnServerClick(this::exportToLog));
        addChild(page);
    }

    private static UIElement section(String title, Widget... children) {
        return UIElement.section().addChild(TextLine.constant(LayoutStyle.AUTO, Component.literal(title))).addChildren(children);
    }

    private UIElement targetSlots(int count) {
        var slots = new UIElement().layout(l -> l.row().flexWrap(FlexWrap.WRAP));
        // 最后一个槽永远为空；清空已有槽时从列表删除，后续槽位按顺序补齐。
        for (int i = 0; i <= count; i++) {
            int index = i;
            slots.addChild(PhantomAEKeySlot.of(() -> index < targets.size() ? targets.get(index) : null,
                    key -> setTarget(index, key), key -> true).xeiPhantom());
        }
        return slots;
    }

    private void setTarget(int index, @Nullable AEKey key) {
        if (index < targets.size()) {
            if (key == null) targets.remove(index);
            else targets.set(index, key);
        } else if (key != null) {
            targets.add(key);
        }
    }

    private UIElement pointRows(int count) {
        var rows = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP));
        for (int i = 0; i < count; i++) {
            int index = i;
            var row = UIElement.centeredRow(UISizes.CONTROL_HEIGHT);
            var tag = new ResearchTagSelector(tags, () -> index < points.size() ? points.get(index).tag : tags[0], value -> {
                if (index < points.size()) points.get(index).tag = value;
            });
            var amount = NumberField.ofLong(LayoutStyle.AUTO, () -> index < points.size() ? points.get(index).amount : 1,
                    value -> {
                        if (index < points.size()) points.get(index).amount = value;
                    }, 1, Long.MAX_VALUE).layout(l -> l.flex(1));
            var remove = Button.glyph("×").setVariant(UITheme.ButtonVariant.DANGER).setOnServerClick(() -> {
                if (index < points.size()) points.remove(index);
            });
            rows.addChild(row.addChildren(tag, amount, remove));
        }
        return rows;
    }

    private void addResearchPoint() {
        var used = new IdentityKeySet<ResearchTag>(points.size());
        for (int i = 0; i < points.size(); i++) used.add(points.get(i).tag);
        for (var tag : tags) {
            if (!used.contains(tag)) {
                points.add(new PointEntry(tag));
                return;
            }
        }
    }

    private void exportToLog() {
        String error = validate();
        if (error != null) {
            GTOCore.LOGGER.error("Data scanning editor export failed: {}", error);
            return;
        }
        GTOCore.LOGGER.error("Generated data scanning Java code:\n{}", generateCode());
    }

    private @Nullable String validate() {
        if (targets.isEmpty()) return "at least one scan target is required";
        if (points.isEmpty()) return "at least one ResearchPoint entry is required";
        var used = new IdentityKeySet<ResearchTag>(points.size());
        for (int i = 0; i < points.size(); i++) {
            var point = points.get(i);
            if (!used.add(point.tag)) return "duplicate ResearchTag: " + point.tag.getName();
            if (point.amount <= 0) return "ResearchPoint amounts must be positive";
        }
        return null;
    }

    private String generateCode() {
        var researchPoints = new StringBuilder("ResearchPoints.of(");
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) researchPoints.append(", ");
            var point = points.get(i);
            researchPoints.append("ResearchTag.").append(point.tag.getName().toUpperCase(Locale.ROOT))
                    .append(", ").append(point.amount).append('L');
        }
        researchPoints.append(')');
        var code = new StringBuilder();
        for (int i = 0; i < targets.size(); i++) {
            if (i > 0) code.append('\n');
            code.append("DataScanningManager.registerDataScanning(").append(ingredientExpression(targets.get(i)))
                    .append(", ").append(researchPoints).append(");");
        }
        return code.toString();
    }

    private static String ingredientExpression(AEKey key) {
        if (key instanceof AEItemKey item && !item.hasTag()) {
            return "RegistriesUtils.getItem(\"" + escape(RegistriesUtils.getIdLocation(item.getItem()).toString()) + "\")";
        }
        if (key instanceof AEFluidKey fluid && !fluid.hasTag()) {
            return "RegistriesUtils.getFluid(\"" + escape(RegistriesUtils.getIdLocation(fluid.getFluid()).toString()) + "\")";
        }
        return "AEKey.fromTagGeneric(RecipeEditorBehavior.parseTemplateTag(\"" + escape(key.toTagGeneric().toString()) + "\"))";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }

    private static final class PointEntry {

        private ResearchTag tag;
        private long amount = 1;

        private PointEntry(ResearchTag tag) {
            this.tag = tag;
        }
    }
}
