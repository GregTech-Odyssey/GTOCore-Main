package com.gtocore.api.gui.ui.elements;

import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.data.SyncValue;
import com.gtocore.api.gui.ui.styletemplate.UISizes;
import com.gtocore.api.gui.ui.styletemplate.UITheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * 状态行：机器状态显示的一行，与一行文字同高（{@link #HEIGHT}）。
 *
 * <pre>
 * 名称 ………………………………… 数值          （{@link Level#NORMAL}：纯信息，没有任何标记）
 * 名称 ……………………………… ● 数值          （其他等级：数值前一颗状态灯，灯与数值同色：正常绿、注意黄、错误红）
 * </pre>
 *
 * 名称用次要文字色，数值右对齐；名称为空时整行是一句状态（有灯时灯在句首）。文字放不下时截断加省略号。
 * <p>
 * 状态行<strong>不用</strong> {@link InfoIcon}：信息图标的含义是"悬停看说明"，放在每一行会让玩家分不清哪里能悬停，
 * 行也会被撑高。需要解释的行用 {@link #detail}（服务端下发的悬停说明），没有说明时悬停只在文字被截断时显示全文。
 * <p>
 * 数值、等级、说明都由服务端取值下发（{@link SyncValue}）；构造时<strong>不会</strong>调用 getter，可以依赖服务端独有的数据。
 * 通常放进 {@link StatusPanel} 里纵向堆叠，而不是单独散放。
 */
public class StatusLine extends UIElement {

    public static final int HEIGHT = UISizes.STATUS_LINE_HEIGHT;
    /// 状态灯边长（含 1 像素深色描边）与灯和数值之间的空隙
    private static final int LAMP = 6;
    private static final int LAMP_GAP = 3;

    /** 这一项的好坏。{@link #NORMAL} 是纯信息，不画灯、不上色。 */
    public enum Level {

        NORMAL(0, UITheme.TEXT),
        GOOD(UITheme.STATUS_ONLINE, UITheme.STATUS_TEXT_GOOD),
        WARNING(UITheme.STATUS_WARNING, UITheme.STATUS_TEXT_WARNING),
        ERROR(UITheme.STATUS_OFFLINE, UITheme.STATUS_TEXT_ERROR);

        private final int lampColor;
        private final int textColor;

        Level(int lampColor, int textColor) {
            this.lampColor = lampColor;
            this.textColor = textColor;
        }

        public boolean hasLamp() {
            return this != NORMAL;
        }
    }

    private static final Level[] LEVELS = Level.values();

    private final Component label;
    private final SyncValue<Component> value;
    private final SyncValue<Integer> level;
    private Supplier<Level> levelGetter = () -> Level.NORMAL;
    @Nullable
    private SyncValue<Component> detail;

    public StatusLine(int width, Component label, Supplier<Component> value) {
        this.label = label;
        layout(l -> l.size(width, HEIGHT));
        this.value = addSyncValue(SyncValue.of(value, SyncValue.COMPONENT, Component.empty()));
        this.level = addSyncValue(SyncValue.ofInt(() -> levelGetter.get().ordinal(), Level.NORMAL.ordinal()));
    }

    /** 名称取翻译键的一行，默认 {@link Level#NORMAL}。 */
    public static StatusLine of(int width, String labelKey, Supplier<Component> value) {
        return new StatusLine(width, Component.translatable(labelKey), value);
    }

    /** 没有名称、整行一句话的状态。 */
    public static StatusLine sentence(int width, Supplier<Component> text) {
        return new StatusLine(width, Component.empty(), text);
    }

    /** 等级随状态变化（服务端取值下发）。只有表示好坏的项才设，纯信息项保持默认。 */
    public StatusLine level(Supplier<Level> level) {
        this.levelGetter = level;
        return this;
    }

    /**
     * 悬停说明（服务端取值下发），例如行内显示短句"未绑定所有者"，悬停看完整原因。
     * 两端建页时都要以同样顺序调用（它会注册一个同步值）。
     */
    public StatusLine detail(Supplier<Component> detail) {
        this.detail = addSyncValue(SyncValue.of(detail, SyncValue.COMPONENT, Component.empty()));
        return this;
    }

    /** 固定的悬停说明（翻译键）。 */
    public StatusLine tooltip(String... translationKeys) {
        var tooltips = new Component[translationKeys.length];
        for (int i = 0; i < translationKeys.length; i++) tooltips[i] = Component.translatable(translationKeys[i]);
        setHoverTooltips(tooltips);
        return this;
    }

    /** 最近一次同步到的数值。 */
    public Component getValue() {
        return value.getValue();
    }

    /** 最近一次同步到的等级。 */
    public Level getLevel() {
        int index = level.getValue();
        return index >= 0 && index < LEVELS.length ? LEVELS[index] : Level.NORMAL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        int x = getPositionX(), y = getPositionY(), width = getSizeWidth();
        var font = Minecraft.getInstance().font;
        var current = getLevel();
        int textY = y + (HEIGHT - 8) / 2;
        int lampSpace = current.hasLamp() ? LAMP + LAMP_GAP : 0;
        String valueText = value.getValue().getString();
        String labelText = label.getString();
        if (labelText.isEmpty()) {
            if (current.hasLamp()) drawLamp(graphics, x, y, current);
            graphics.drawString(font, UITheme.clip(font, valueText, width - lampSpace), x + lampSpace, textY, current.textColor, false);
            return;
        }
        // 数值（连同灯）优先完整显示，但最多占一半；名称用剩下的宽度
        int valueWidth = Math.min(font.width(valueText) + lampSpace, width / 2);
        String shownLabel = UITheme.clip(font, labelText, width - valueWidth - UISizes.TEXT_PADDING);
        graphics.drawString(font, shownLabel, x, textY, UITheme.TEXT_SECONDARY, false);
        String shownValue = UITheme.clip(font, valueText, width - font.width(shownLabel) - UISizes.TEXT_PADDING - lampSpace);
        int valueX = x + width - font.width(shownValue);
        graphics.drawString(font, shownValue, valueX, textY, current.textColor, false);
        if (current.hasLamp()) drawLamp(graphics, valueX - lampSpace, y, current);
    }

    /** 状态灯：1 像素深色描边的小方块，左上一点高光。 */
    @OnlyIn(Dist.CLIENT)
    private static void drawLamp(GuiGraphics graphics, int x, int y, Level level) {
        int top = y + (HEIGHT - LAMP) / 2;
        graphics.fill(x, top, x + LAMP, top + LAMP, UITheme.STATUS_LAMP_OUTLINE);
        graphics.fill(x + 1, top + 1, x + LAMP - 1, top + LAMP - 1, level.lampColor);
        graphics.fill(x + 1, top + 1, x + 2, top + 2, 0x80FFFFFF);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        // 固定说明优先；其次服务端下发的说明；都没有且文字被截断时，悬停显示全文
        if (!tooltipTexts.isEmpty() || gui == null || gui.getModularUIGui() == null || !isMouseOverElement(mouseX, mouseY)) return;
        if (detail != null && !detail.getValue().getString().isEmpty()) {
            gui.getModularUIGui().setHoverTooltip(List.of(detail.getValue()), ItemStack.EMPTY, null, null);
            return;
        }
        var font = Minecraft.getInstance().font;
        String labelText = label.getString(), valueText = value.getValue().getString();
        int lampSpace = getLevel().hasLamp() ? LAMP + LAMP_GAP : 0;
        int gap = labelText.isEmpty() ? 0 : UISizes.TEXT_PADDING;
        if (font.width(labelText) + gap + lampSpace + font.width(valueText) > getSizeWidth()) {
            var full = labelText.isEmpty() ? value.getValue() : label.copy().append(" ").append(value.getValue());
            gui.getModularUIGui().setHoverTooltip(List.of(full), ItemStack.EMPTY, null, null);
        }
    }
}
