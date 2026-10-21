package com.gtocore.client.guide;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.uipro.window.PopupCard;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import dev.vfyjxf.taffy.style.AlignContent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * 物品攻略的两张卡片：首次同意页与攻略选择列表。纯客户端界面，关闭时回到打开前的界面。
 */
@OnlyIn(Dist.CLIENT)
@DataGeneratorScanned
public final class ItemGuidesScreen extends ModularUIGuiContainer {

    @RegisterLanguage(cn = "物品攻略", en = "Item Guides")
    static final String CONSENT_TITLE = "gtocore.item_guides.consent.title";
    @RegisterLanguage(cn = "GTO官网gtodyssey.com上的攻略可以关联到具体物品。启用后，悬停在已关联攻略的物品上时显示攻略数量，按%s选择攻略并在浏览器中打开。",
                      en = "Guides on the GTO website gtodyssey.com can be linked to specific items. When enabled, hovering an item that has guides shows how many there are; press %s to choose one and open it in your browser.")
    static final String CONSENT_INTRO = "gtocore.item_guides.consent.intro";
    @RegisterLanguage(cn = "下载内容", en = "What is downloaded")
    static final String DOWNLOAD_HEADING = "gtocore.item_guides.consent.download";
    @RegisterLanguage(cn = "·\u00a0每次启动游戏时，在后台从https://gtodyssey.com获取一次攻略目录：文章标题、发布时间、关联的物品及版本。",
                      en = "· Each time the game starts, the guide catalogue is fetched once in the background from https://gtodyssey.com: article titles, publication dates, and the linked items and versions.")
    static final String DOWNLOAD_1 = "gtocore.item_guides.consent.download.1";
    @RegisterLanguage(cn = "·\u00a0不包含文章正文。目录约30KB；内容未变化时仅核对版本，几乎不产生流量。",
                      en = "· Article text is not included. The catalogue is about 30 KB; when it has not changed only its version is checked, which uses almost no traffic.")
    static final String DOWNLOAD_2 = "gtocore.item_guides.consent.download.2";
    @RegisterLanguage(cn = "·\u00a0下载在后台线程进行，不影响启动与游戏运行。网络不可用时跳过，沿用上次的目录。",
                      en = "· The download runs on a background thread and does not slow down loading or play. Without a connection it is skipped and the previous catalogue is kept.")
    static final String DOWNLOAD_3 = "gtocore.item_guides.consent.download.3";
    @RegisterLanguage(cn = "不会上传", en = "What is never sent")
    static final String UPLOAD_HEADING = "gtocore.item_guides.consent.upload";
    @RegisterLanguage(cn = "·\u00a0不发送玩家名、UUID、存档、模组列表、设备信息或任何游戏内操作。",
                      en = "· No player name, UUID, save data, mod list, device information or in-game activity is sent.")
    static final String UPLOAD_1 = "gtocore.item_guides.consent.upload.1";
    @RegisterLanguage(cn = "·\u00a0与访问普通网页相同，网站只能看到本机的公网IP地址。",
                      en = "· As with visiting any web page, the website only sees this computer's public IP address.")
    static final String UPLOAD_2 = "gtocore.item_guides.consent.upload.2";
    @RegisterLanguage(cn = "·\u00a0打开攻略时由系统浏览器访问网页，与手动打开该网址相同。",
                      en = "· Opening a guide visits the page in your system browser, the same as opening the address yourself.")
    static final String UPLOAD_3 = "gtocore.item_guides.consent.upload.3";
    @RegisterLanguage(cn = "本地文件", en = "Local files")
    static final String FILES_HEADING = "gtocore.item_guides.consent.files";
    @RegisterLanguage(cn = "·\u00a0目录缓存保存在.minecraft/gtocore/item-guides/，可随时删除。",
                      en = "· The catalogue is cached in .minecraft/gtocore/item-guides/ and may be deleted at any time.")
    static final String FILES_1 = "gtocore.item_guides.consent.files.1";
    @RegisterLanguage(cn = "更改选择", en = "Changing this later")
    static final String CHANGE_HEADING = "gtocore.item_guides.consent.change";
    @RegisterLanguage(cn = "·\u00a0在聊天栏输入/gtocorec guides可重新打开本页。",
                      en = "· Type /gtocorec guides in chat to open this page again.")
    static final String CHANGE_1 = "gtocore.item_guides.consent.change.1";
    @RegisterLanguage(cn = "当前状态：%s", en = "Current setting: %s")
    static final String STATE = "gtocore.item_guides.consent.state";
    @RegisterLanguage(cn = "尚未选择", en = "Not chosen yet")
    static final String STATE_UNSET = "gtocore.item_guides.consent.state.unset";
    @RegisterLanguage(cn = "已启用", en = "Enabled")
    static final String STATE_ON = "gtocore.item_guides.consent.state.on";
    @RegisterLanguage(cn = "未启用", en = "Disabled")
    static final String STATE_OFF = "gtocore.item_guides.consent.state.off";
    @RegisterLanguage(cn = "启用", en = "Enable")
    static final String ENABLE = "gtocore.item_guides.consent.enable";
    @RegisterLanguage(cn = "不启用", en = "Do not enable")
    static final String DISABLE = "gtocore.item_guides.consent.disable";
    @RegisterLanguage(cn = "%s的相关攻略", en = "Guides about %s")
    static final String CHOOSER_TITLE = "gtocore.item_guides.chooser.title";
    @RegisterLanguage(cn = "打开", en = "Open")
    static final String OPEN = "gtocore.item_guides.chooser.open";
    @RegisterLanguage(cn = "关联于 %s", en = "Linked in %s")
    static final String LINKED_IN = "gtocore.item_guides.chooser.linked_in";
    @RegisterLanguage(cn = "在浏览器中打开gtodyssey.com的网页 · 目录更新于%s", en = "Opens a page on gtodyssey.com in your browser · catalogue from %s")
    static final String FOOTER = "gtocore.item_guides.chooser.footer";
    @RegisterLanguage(cn = "无法连接gtodyssey.com，使用%s的目录", en = "Could not reach gtodyssey.com; using the catalogue from %s")
    static final String FOOTER_OFFLINE = "gtocore.item_guides.chooser.footer.offline";

    private static final int CONTENT_WIDTH = 2 * UISizes.CONTENT_WIDTH;
    private static final int ACTION_WIDTH = 2 * UISizes.BUTTON_WIDTH;
    private static final int MARGIN = 8;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final Host host;
    @Nullable
    private final Screen previous;
    @Nullable
    private final ItemGuides.Guide[] guides;

    private ItemGuidesScreen(Host host, ModularUI ui, @Nullable Screen previous, @Nullable ItemGuides.Guide[] guides) {
        super(ui, -1);
        this.host = host;
        this.previous = previous;
        this.guides = guides;
        ui.initWidgets();
    }

    static void openConsent(@Nullable Screen previous) {
        open(previous, null, close -> Popup.of(() -> Component.translatable(CONSENT_TITLE), content -> consent(content, close))
                .setContentWidth(CONTENT_WIDTH));
    }

    static void openChooser(Screen previous, ItemStack icon, Component name, ItemGuides.Guide[] guides) {
        open(previous, guides, close -> Popup.of(() -> icon, () -> Component.translatable(CHOOSER_TITLE, name), content -> chooser(content, guides, close))
                .setContentWidth(CONTENT_WIDTH));
    }

    private static void open(@Nullable Screen previous, @Nullable ItemGuides.Guide[] guides, Function<Runnable, Popup> popup) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        var window = minecraft.getWindow();
        int width = window.getGuiScaledWidth(), height = window.getGuiScaledHeight();
        var screen = new ItemGuidesScreen[1];
        Runnable close = () -> {
            if (minecraft.screen == screen[0]) minecraft.setScreen(previous);
        };
        var host = new Host(popup.apply(close), width, height, close);
        var ui = new ModularUI(width, height, IUIHolder.EMPTY, minecraft.player).widget(host);
        screen[0] = new ItemGuidesScreen(host, ui, previous, guides);
        minecraft.setScreen(screen[0]);
    }

    private static void consent(UIElement content, Runnable close) {
        content.addChild(Label.of(CONTENT_WIDTH, () -> Component.translatable(CONSENT_INTRO, ItemGuides.keyName())));
        paragraph(content, DOWNLOAD_HEADING, DOWNLOAD_1, DOWNLOAD_2, DOWNLOAD_3);
        paragraph(content, UPLOAD_HEADING, UPLOAD_1, UPLOAD_2, UPLOAD_3);
        paragraph(content, FILES_HEADING, FILES_1);
        paragraph(content, CHANGE_HEADING, CHANGE_1);
        content.addChild(TextLine.constant(CONTENT_WIDTH, Component.translatable(STATE, Component.translatable(switch (ItemGuides.consent()) {
            case UNSET -> STATE_UNSET;
            case GRANTED -> STATE_ON;
            case DENIED -> STATE_OFF;
        }))).bindClientColor(UITheme::textSecondary));
        var enable = Button.translatable(ACTION_WIDTH, ENABLE).setVariant(UITheme.ButtonVariant.CONFIRM).setOnClientClick(() -> {
            ItemGuides.decide(true);
            close.run();
        });
        var disable = Button.translatable(ACTION_WIDTH, DISABLE).setOnClientClick(() -> {
            ItemGuides.decide(false);
            close.run();
        });
        content.addChild(UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(UIElement.flexSpacer(), enable, disable));
    }

    private static void paragraph(UIElement content, String heading, String... lines) {
        var section = UIElement.column(CONTENT_WIDTH).layout(l -> l.gapAll(UISizes.GAP));
        section.addChild(TextLine.constant(CONTENT_WIDTH, Component.translatable(heading).withStyle(ChatFormatting.BOLD)));
        for (String line : lines) section.addChild(Label.translatable(CONTENT_WIDTH, line));
        content.addChild(section);
    }

    private static void chooser(UIElement content, ItemGuides.Guide[] guides, Runnable close) {
        for (var guide : guides) {
            var title = TextLine.constant(0, Component.literal(ItemGuides.title(guide))).layout(l -> l.flex(1));
            var open = Button.translatable(UISizes.BUTTON_WIDTH, OPEN).setOnClientClick(() -> {
                ItemGuides.openPage(guide);
                close.run();
            });
            var row = UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(title, open);
            var meta = TextLine.constant(CONTENT_WIDTH, meta(guide)).bindClientColor(UITheme::textSecondary);
            content.addChild(UIElement.column(CONTENT_WIDTH).addChildren(row, meta));
        }
        long synced = ItemGuides.syncedAt();
        Component when = Component.literal(synced > 0 ? MINUTE.format(Instant.ofEpochMilli(synced)) : "-");
        content.addChild(TextLine.constant(CONTENT_WIDTH, Component.translatable(ItemGuides.offline() ? FOOTER_OFFLINE : FOOTER, when))
                .bindClientColor(UITheme::textSecondary));
    }

    private static Component meta(ItemGuides.Guide guide) {
        var line = Component.translatable(LINKED_IN, guide.version());
        var voltage = guide.article().voltage();
        if (voltage != null) line.append(" · ").append(voltage);
        return line.append(" · ").append(DAY.format(Instant.ofEpochMilli(guide.article().published())));
    }

    @Override
    public void init() {
        modularUI.setSize(width, height);
        host.resize(width, height);
        super.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (guides != null && keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            int chosen = keyCode - GLFW.GLFW_KEY_1;
            if (chosen < guides.length) {
                ItemGuides.openPage(guides[chosen]);
                onClose();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null && minecraft.screen == this) minecraft.setScreen(previous);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class Host extends UIElement implements ILocalUI {

        private Host(Popup popup, int width, int height, Runnable close) {
            setClientSideWidget();
            layout(l -> l.size(width, height).column().alignCenter().justifyContent(AlignContent.CENTER));
            addChild(new PopupCard("item_guides", popup, height - 2 * MARGIN, close).setResizable(false));
        }

        private void resize(int width, int height) {
            layout(l -> l.size(width, height));
        }
    }
}
