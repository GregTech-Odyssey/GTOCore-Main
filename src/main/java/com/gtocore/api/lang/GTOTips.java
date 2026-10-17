package com.gtocore.api.lang;

import com.gtocore.common.data.translation.ComponentSlang;

import com.gregtechceu.gtceu.api.tooltip.StatKind;
import com.gregtechceu.gtceu.api.tooltip.TipRole;
import com.gregtechceu.gtceu.api.tooltip.TipSlot;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * GTO 提示的顺序式写法：每加一行行号加一，翻译键为 gtocore.lang.前缀.行号.英文哈希，与历史语言文件一致。
 */
public final class GTOTips {

    private final String prefix;
    private final TipSpec.Builder builder = TipSpec.builder();
    private int line;
    @Nullable
    private TipSpec.Lines current;
    @Nullable
    private TipSpec.Lines lore;

    private GTOTips(String prefix) {
        this.prefix = prefix;
    }

    public static TipSpec of(String prefix, Consumer<GTOTips> body) {
        GTOTips tips = new GTOTips(prefix);
        body.accept(tips);
        return tips.builder.build();
    }

    public static TipSpec of(Consumer<GTOTips> body) {
        return of("", body);
    }

    public static Component global(String cn, String en) {
        if (cn.equals(en)) return Component.literal(cn);
        return Component.translatable(TipKeys.key(cn, en));
    }

    public Component tr(String cn, String en) {
        if (cn.equals(en)) return Component.literal(cn);
        return Component.translatable(TipKeys.key(cn, en, keyPrefix()));
    }

    public Component tr(String cn, String en, Object... args) {
        if (cn.equals(en)) return Component.literal(cn);
        return Component.translatable(TipKeys.key(cn, en, keyPrefix()), args);
    }

    private String keyPrefix() {
        return prefix.isEmpty() ? TipKeys.PREFIX + "." + line : TipKeys.PREFIX + "." + prefix + "." + line;
    }

    public int line() {
        return line;
    }

    public GTOTips heading(Component title) {
        return heading(TipSlot.FEATURES, title);
    }

    public GTOTips heading(TipSlot slot, Component title) {
        current = builder.open(slot, title, false);
        line++;
        return this;
    }

    public GTOTips group() {
        current = builder.open(TipSlot.FEATURES, null, false);
        return this;
    }

    public GTOTips stats() {
        current = builder.open(TipSlot.STATS, null, false);
        return this;
    }

    public GTOTips details(@Nullable Component title) {
        current = builder.open(TipSlot.DETAILS, title, true);
        if (title != null) line++;
        return this;
    }

    public GTOTips keepOrder() {
        lines().keepOrder();
        return this;
    }

    private TipSpec.Lines lines() {
        if (current == null) current = builder.open(TipSlot.FEATURES, null, false);
        return current;
    }

    public GTOTips line(TipRole role, Component body) {
        lines().line(role, body);
        line++;
        return this;
    }

    public GTOTips dynamic(TipRole role, Supplier<Component> body) {
        lines().line(role, Tips.dynamic(body));
        line++;
        return this;
    }

    public GTOTips feature(Component body) {
        return line(TipRole.FEATURE, body);
    }

    public GTOTips requirement(Component body) {
        return line(TipRole.REQUIREMENT, body);
    }

    public GTOTips attention(Component body) {
        return line(TipRole.ATTENTION, body);
    }

    public GTOTips bonus(Component body) {
        return line(TipRole.BONUS, body);
    }

    public GTOTips penalty(Component body) {
        return line(TipRole.PENALTY, body);
    }

    public GTOTips warning(Component body) {
        return line(TipRole.WARNING, body);
    }

    public GTOTips supported(Component body) {
        return line(TipRole.SUPPORTED, body);
    }

    public GTOTips unsupported(Component body) {
        return line(TipRole.UNSUPPORTED, body);
    }

    public GTOTips detail(Component body) {
        return line(TipRole.DETAIL, body);
    }

    public GTOTips formula(Component body) {
        return line(TipRole.FORMULA, body);
    }

    public GTOTips control(Component body) {
        return line(TipRole.CONTROL, body);
    }

    public GTOTips leftClick(Component body) {
        return line(TipRole.CONTROL_LEFT, body);
    }

    public GTOTips rightClick(Component body) {
        return line(TipRole.CONTROL_RIGHT, body);
    }

    public GTOTips middleClick(Component body) {
        return line(TipRole.CONTROL_MIDDLE, body);
    }

    public GTOTips plain(Component body) {
        return line(TipRole.PLAIN, body);
    }

    public GTOTips statText(Component body) {
        return line(TipRole.STAT, body);
    }

    public GTOTips stat(StatKind kind, Object... args) {
        return line(TipRole.STAT, kind.line(args));
    }

    public GTOTips lore(Component body) {
        if (lore == null) lore = builder.open(TipSlot.LORE, null, false);
        lore.line(TipRole.LORE, body);
        line++;
        return this;
    }

    public GTOTips edition() {
        return lore(ComponentSlang.GTOSignal_Edition_Text);
    }

    public GTOTips editionRemix() {
        return lore(ComponentSlang.GTOSignal_Edition_Remix_Text);
    }

    public GTOTips include(TipSpec other) {
        builder.include(other);
        return this;
    }

    public GTOTips skip() {
        line++;
        return this;
    }
}
