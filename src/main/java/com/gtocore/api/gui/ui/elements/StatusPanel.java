package com.gtocore.api.gui.ui.elements;

import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.styletemplate.UITheme;

import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * 机器的状态显示面板：若干 {@link StatusLine} 逐行紧排在一块下凹的"显示窗"（{@link UITheme#STATUS_PANEL}）里。
 * 与设置区块（{@link UITheme#PANEL}，平边框）外观不同，玩家一眼能认出"这是状态，不是可操作的设置"。
 * 机器的所有者、成员数、已加载数量、运行状态这类只读信息都放进这里，不要用散落的文字。
 *
 * <pre>
 * var status = new StatusPanel(width);
 * status.addLine(KEY_OWNER, () -&gt; ownerName);                                   // 纯信息：无灯
 * status.addLine(KEY_STATE, () -&gt; shortState)                                   // 表示好坏：灯 + 上色
 *         .level(() -&gt; online ? StatusLine.Level.GOOD : StatusLine.Level.ERROR)
 *         .detail(() -&gt; fullReason);                                          // 悬停看完整原因
 * </pre>
 */
public class StatusPanel extends UIElement {

    public StatusPanel(int width) {
        layout(l -> l.column().width(width).paddingAll(UITheme.PANEL_PADDING).paddingBottom(UITheme.PANEL_PADDING_BOTTOM));
        setBackground(UITheme.STATUS_PANEL);
    }

    /** 加一行"名称 …… 数值"，返回该行以便继续设置等级、说明。 */
    public StatusLine addLine(String labelKey, Supplier<Component> value) {
        return addLine(StatusLine.of(getContentWidth(), labelKey, value));
    }

    /** 加一行整句状态（没有名称）。 */
    public StatusLine addSentence(Supplier<Component> text) {
        return addLine(StatusLine.sentence(getContentWidth(), text));
    }

    public StatusLine addLine(StatusLine line) {
        addChild(line);
        return line;
    }

    /** {@code lines} 行时面板的总高度（建页时预留空间用）。 */
    public static int heightFor(int lines) {
        return UITheme.PANEL_PADDING + UITheme.PANEL_PADDING_BOTTOM + lines * StatusLine.HEIGHT;
    }
}
