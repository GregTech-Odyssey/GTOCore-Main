package com.gtocore.client.guide;

import com.gtocore.client.website.WebsitePopupScreen;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ButtonGroup;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.Popup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * 物品攻略选择列表：一篇攻略一个区块，数字键 1~9 直接打开。联网授权见 WebsiteConsent。
 */
@OnlyIn(Dist.CLIENT)
@DataGeneratorScanned
public final class ItemGuidesScreen {

    @RegisterLanguage(cn = "%s的相关攻略", en = "Guides about %s")
    static final String CHOOSER_TITLE = "gtocore.item_guides.chooser.title";
    @RegisterLanguage(cn = "打开", en = "Open")
    static final String OPEN = "gtocore.item_guides.chooser.open";
    @RegisterLanguage(cn = "用%s显示标题并打开攻略", en = "Show titles and open guides in %s")
    static final String LANGUAGE_TIP = "gtocore.item_guides.chooser.language";
    @RegisterLanguage(cn = "仅有%s版", en = "%s only")
    static final String ONLY_IN = "gtocore.item_guides.chooser.only_in";
    @RegisterLanguage(cn = "在浏览器中打开gtodyssey.com的网页 · 目录更新于%s", en = "Opens a page on gtodyssey.com in your browser · catalogue from %s")
    static final String FOOTER = "gtocore.item_guides.chooser.footer";
    @RegisterLanguage(cn = "无法连接gtodyssey.com，使用%s的目录", en = "Could not reach gtodyssey.com; using the catalogue from %s")
    static final String FOOTER_OFFLINE = "gtocore.item_guides.chooser.footer.offline";

    private static final int CONTENT_WIDTH = 2 * UISizes.CONTENT_WIDTH;
    private static final int ENTRY_HEIGHT = 2 * TextLine.HEIGHT + UISizes.GAP;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    static void openChooser(Screen previous, ItemStack icon, Component name, ItemGuides.Guide[] guides) {
        Runnable reopen = () -> Minecraft.getInstance().tell(() -> openChooser(previous, icon, name, guides));
        WebsitePopupScreen.open("item_guides", previous, close -> Popup.of(() -> icon, () -> Component.translatable(CHOOSER_TITLE, name), content -> chooser(content, guides, close, reopen))
                .setContentWidth(CONTENT_WIDTH), keyCode -> {
                    if (keyCode < GLFW.GLFW_KEY_1 || keyCode > GLFW.GLFW_KEY_9) return false;
                    int chosen = keyCode - GLFW.GLFW_KEY_1;
                    if (chosen >= guides.length) return false;
                    ItemGuides.openPage(guides[chosen]);
                    return true;
                });
    }

    private static void chooser(UIElement content, ItemGuides.Guide[] guides, Runnable close, Runnable reopen) {
        var languages = languages(reopen);
        if (languages != null) content.addChild(languages);
        long synced = ItemGuides.syncedAt();
        var catalogue = Component.literal(synced > 0 ? MINUTE.format(Instant.ofEpochMilli(synced)) : "-");
        if (ItemGuides.offline()) {
            content.addChild(Label.of(CONTENT_WIDTH, () -> Component.translatable(FOOTER_OFFLINE, catalogue)).bindClientColor(() -> UITheme.STATUS_TEXT_WARNING));
        }
        var source = Component.translatable(FOOTER, catalogue);
        for (var guide : guides) content.addChild(entry(guide, source, close));
    }

    private static UIElement entry(ItemGuides.Guide guide, Component source, Runnable close) {
        var title = TextLine.constant(LayoutStyle.AUTO, Component.literal(ItemGuides.title(guide)));
        var details = UIElement.row(TextLine.HEIGHT);
        var article = guide.article();
        var version = Component.literal(guide.version());
        if (article.voltage() != null) version.append(" · ").append(article.voltage());
        boolean foreign = !article.hasLocale(ItemGuides.siteLocale());
        if (foreign) version.append(" · ");
        details.addChild(fitted(version).bindClientColor(UITheme::textSecondary));
        if (foreign) {
            String name = ItemGuides.localeName(article.sourceLocale());
            details.addChild(fitted(Component.translatable(ONLY_IN, name != null ? name : article.sourceLocale())).bindClientColor(() -> UITheme.STATUS_TEXT_WARNING));
        }
        details.addChild(UIElement.flexSpacer());
        details.addChild(fitted(Component.literal(DAY.format(Instant.ofEpochMilli(article.published())))).bindClientColor(UITheme::textSecondary));
        var text = UIElement.column(LayoutStyle.AUTO).layout(l -> l.flex(1).gapAll(UISizes.GAP)).addChildren(title, details);
        var open = Button.translatable(UISizes.BUTTON_WIDTH, OPEN).setOnClientClick(() -> {
            ItemGuides.openPage(guide);
            close.run();
        });
        open.tooltips(source);
        var entry = UIElement.section(CONTENT_WIDTH).layout(l -> l.paddingHorizontal(UISizes.SECTION_GAP));
        entry.addChild(UIElement.centeredRow(ENTRY_HEIGHT).layout(l -> l.gapAll(UISizes.SECTION_GAP)).addChildren(text, open));
        return entry;
    }

    private static TextLine fitted(Component text) {
        return TextLine.constant(UIText.width(text), text);
    }

    @Nullable
    private static UIElement languages(Runnable reopen) {
        var locales = ItemGuides.locales();
        if (locales.size() < 2) return null;
        int current = Math.max(0, ItemGuideCatalogue.indexOf(locales, ItemGuides.siteLocale()));
        var languages = ButtonGroup.single(locales.size(), i -> Component.literal(locales.get(i).name()), () -> current, i -> {
            ItemGuides.chooseLocale(locales.get(i).id());
            reopen.run();
        }).compact().optionTooltips(i -> Collections.singletonList(Component.translatable(LANGUAGE_TIP, locales.get(i).name())));
        languages.layout(l -> l.flexGrow(1));
        int index = 0;
        for (var option : languages.widgets) {
            if (option instanceof Button button) {
                int width = UIText.width(locales.get(index++).name()) + 2 * UISizes.TEXT_PADDING;
                button.layout(l -> l.minWidth(width).flex(1));
            }
        }
        return UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChild(languages);
    }
}
