package com.gtocore.client.website;

import com.gtocore.client.guide.ItemGuides;
import com.gtocore.utils.GTOWebsite;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * 官网联网授权：物品攻略与 /bug 共用一份同意书，同意全开、不同意全关；记录同意时同意书内容的哈希，内容一变进游戏就重新询问。
 */
@OnlyIn(Dist.CLIENT)
@DataGeneratorScanned
public final class WebsiteConsent {

    @RegisterLanguage(cn = "官网联网功能", en = "Website features")
    static final String TITLE = "gtocore.website.consent.title";
    @RegisterLanguage(cn = "以下功能需连接GTO官网（gtodyssey.com），同意后方可使用。",
                      en = "These features connect to the GTO website (gtodyssey.com) and need your consent.")
    static final String INTRO = "gtocore.website.consent.intro";
    @RegisterLanguage(cn = "物品攻略", en = "Item guides")
    static final String GUIDES_HEADING = "gtocore.website.consent.guides";
    @RegisterLanguage(cn = "每次启动游戏时，在后台获取一次攻略目录（约30KB，不含正文）。",
                      en = "Fetches the guide list once per launch (about 30 KB, no article text).")
    static final String GUIDES_1 = "gtocore.website.consent.guides.1";
    @RegisterLanguage(cn = "悬停在已关联攻略的物品上时，按%s选择并在浏览器中打开攻略。",
                      en = "Hover over an item with guides and press %s to open one in your browser.")
    static final String GUIDES_2 = "gtocore.website.consent.guides.2";
    @RegisterLanguage(cn = "问题反馈（/bug）", en = "Bug reports (/bug)")
    static final String BUG_HEADING = "gtocore.website.consent.bug";
    @RegisterLanguage(cn = "使用前需登录官网账号；多人游戏中仅OP可以使用此指令。",
                      en = "Needs a website account; in multiplayer, only operators can use it.")
    static final String BUG_1 = "gtocore.website.consent.bug.1";
    @RegisterLanguage(cn = "以你的账号生成反馈链接，并自动填写整合包版本、游戏环境与难度。",
                      en = "Creates a report link with the pack version, environment and difficulty.")
    static final String BUG_2 = "gtocore.website.consent.bug.2";
    @RegisterLanguage(cn = "同时上传本机日志与24小时内的崩溃报告。",
                      en = "Uploads this computer's log and any crash report from the last 24 hours.")
    static final String BUG_3 = "gtocore.website.consent.bug.3";
    @RegisterLanguage(cn = "在服务器中游玩时，服务端日志也会一并上传。",
                      en = "On a server, the server log is uploaded as well.")
    static final String BUG_4 = "gtocore.website.consent.bug.4";
    @RegisterLanguage(cn = "日志随反馈公开，可能包含玩家名与聊天内容；IP地址会自动打码。",
                      en = "Logs are public and may include player names and chat; IPs are masked.")
    static final String BUG_5 = "gtocore.website.consent.bug.5";
    @RegisterLanguage(cn = "单个日志超过5MB时，只上传开头1MB与末尾2MB。",
                      en = "A log over 5 MB is cut to its first 1 MB and last 2 MB.")
    static final String BUG_6 = "gtocore.website.consent.bug.6";
    @RegisterLanguage(cn = "其他说明", en = "Notes")
    static final String OTHER_HEADING = "gtocore.website.consent.other";
    @RegisterLanguage(cn = "除上述内容外，不发送存档、模组列表、设备信息或任何游戏内操作。",
                      en = "Nothing else is sent: no saves, mod list, device information or in-game activity.")
    static final String OTHER_1 = "gtocore.website.consent.other.1";
    @RegisterLanguage(cn = "攻略缓存与登录凭据保存在.minecraft/gtocore/，可随时删除。",
                      en = "Guide cache and sign-in are kept in .minecraft/gtocore/ (safe to delete).")
    static final String OTHER_2 = "gtocore.website.consent.other.2";
    @RegisterLanguage(cn = "输入/gtocorec website可重新打开本页；内容更新后将再次询问。",
                      en = "Use /gtocorec website to reopen; you will be asked again if this changes.")
    static final String OTHER_3 = "gtocore.website.consent.other.3";
    @RegisterLanguage(cn = "当前状态：%s", en = "Current setting: %s")
    static final String STATE = "gtocore.website.consent.state";
    @RegisterLanguage(cn = "尚未选择", en = "Not chosen yet")
    static final String STATE_UNSET = "gtocore.website.consent.state.unset";
    @RegisterLanguage(cn = "同意书已更新，请重新选择", en = "This text has changed; please choose again")
    static final String STATE_CHANGED = "gtocore.website.consent.state.changed";
    @RegisterLanguage(cn = "已启用", en = "Enabled")
    static final String STATE_ON = "gtocore.website.consent.state.on";
    @RegisterLanguage(cn = "未启用", en = "Disabled")
    static final String STATE_OFF = "gtocore.website.consent.state.off";
    @RegisterLanguage(cn = "同意并启用", en = "Agree and enable")
    static final String ENABLE = "gtocore.website.consent.enable";
    @RegisterLanguage(cn = "不启用", en = "Do not enable")
    static final String DISABLE = "gtocore.website.consent.disable";

    private static final String[] TERMS = { INTRO, GUIDES_HEADING, GUIDES_1, GUIDES_2, BUG_HEADING, BUG_1, BUG_2, BUG_3, BUG_4, BUG_5, BUG_6, OTHER_HEADING, OTHER_1, OTHER_2, OTHER_3 };
    private static final Path STATE_FILE = GTOWebsite.DIR.resolve("website").resolve("consent.json");
    private static final int BULLET_WIDTH = 5;
    private static final Component BULLET = Component.literal("·");
    private static final int PROMPT_DELAY_TICKS = 60;
    private static final int PROMPT_RETRY_TICKS = 20;
    private static final int PROMPT_RETRIES = 30;
    public static final String HASH = termsHash();

    private static volatile boolean granted;
    private static volatile boolean decided;
    private static volatile boolean changed;
    private static boolean prompted;

    private WebsiteConsent() {}

    public static void init() {
        readState();
        MinecraftForge.EVENT_BUS.addListener(WebsiteConsent::onLoggingIn);
    }

    public static boolean granted() {
        return granted;
    }

    public static void open(@Nullable Screen previous) {
        Minecraft.getInstance().tell(() -> WebsitePopupScreen.open("website_consent", previous, close -> Popup.of(() -> Component.translatable(TITLE), content -> content(content, close))
                .setContentWidth(2 * UISizes.CONTENT_WIDTH)));
    }

    private static void decide(boolean value) {
        granted = value;
        decided = true;
        changed = false;
        var root = new JsonObject();
        root.addProperty("hash", HASH);
        root.addProperty("granted", value);
        root.addProperty("decidedAt", System.currentTimeMillis());
        try {
            GTOWebsite.writeAtomically(STATE_FILE, root.toString());
        } catch (Exception e) {
            GTOCore.LOGGER.warn("[Website] cannot save consent: {}", e.toString());
        }
        ItemGuides.consentChanged(value);
    }

    private static void readState() {
        if (!Files.isRegularFile(STATE_FILE)) return;
        try {
            var root = JsonParser.parseString(Files.readString(STATE_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            boolean same = root.has("hash") && HASH.equals(root.get("hash").getAsString());
            decided = same;
            changed = !same;
            granted = same && root.has("granted") && root.get("granted").getAsBoolean();
        } catch (Exception e) {
            GTOCore.LOGGER.warn("[Website] unreadable consent, asking again: {}", e.toString());
        }
    }

    private static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (prompted || decided) return;
        prompted = true;
        promptLater(PROMPT_DELAY_TICKS, PROMPT_RETRIES);
    }

    private static void promptLater(int delay, int retries) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        TaskHandler.enqueueTask(level, () -> {
            var minecraft = Minecraft.getInstance();
            if (minecraft.player == null || decided) return;
            if (minecraft.screen != null) {
                if (retries > 0) promptLater(PROMPT_RETRY_TICKS, retries - 1);
                return;
            }
            open(null);
        }, delay);
    }

    private static void content(UIElement content, Runnable close) {
        int width = 2 * UISizes.CONTENT_WIDTH;
        content.addChild(Label.translatable(width, INTRO));
        content.addChild(section(width, GUIDES_HEADING, Component.translatable(GUIDES_1), Component.translatable(GUIDES_2, ItemGuides.keyName())));
        content.addChild(section(width, BUG_HEADING, Component.translatable(BUG_1), Component.translatable(BUG_2), Component.translatable(BUG_3),
                Component.translatable(BUG_4), Component.translatable(BUG_5), Component.translatable(BUG_6)));
        content.addChild(section(width, OTHER_HEADING, Component.translatable(OTHER_1), Component.translatable(OTHER_2), Component.translatable(OTHER_3)));
        String state = !decided ? (changed ? STATE_CHANGED : STATE_UNSET) : granted ? STATE_ON : STATE_OFF;
        content.addChild(TextLine.constant(width, Component.translatable(STATE, Component.translatable(state))).bindClientColor(UITheme::textSecondary));
        int action = 2 * UISizes.BUTTON_WIDTH;
        var disable = Button.translatable(action, DISABLE).setOnClientClick(() -> {
            decide(false);
            close.run();
        });
        var enable = Button.translatable(action, ENABLE).setVariant(UITheme.ButtonVariant.CONFIRM).setOnClientClick(() -> {
            decide(true);
            close.run();
        });
        content.addChild(UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(disable, UIElement.flexSpacer(), enable));
    }

    private static UIElement section(int width, String heading, Component... items) {
        var section = Form.section(heading).layout(l -> l.width(width));
        int text = width - 2 * UISizes.PANEL_PADDING - BULLET_WIDTH - UISizes.GAP;
        for (Component item : items) {
            var row = UIElement.row(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP));
            row.addChildren(Label.of(BULLET_WIDTH, () -> BULLET).bindClientColor(UITheme::panelText), Label.of(text, () -> item).bindClientColor(UITheme::panelText));
            section.addChild(row);
        }
        return section;
    }

    private static String termsHash() {
        var cn = langFile("zh_cn");
        var en = langFile("en_us");
        var keys = TERMS.clone();
        Arrays.sort(keys);
        var text = new StringBuilder();
        for (String key : keys) text.append(key).append('\n').append(langValue(cn, key)).append('\n').append(langValue(en, key)).append('\n');
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static JsonObject langFile(String locale) {
        try (var in = WebsiteConsent.class.getResourceAsStream("/assets/gtocore/lang/" + locale + ".json")) {
            if (in == null) return new JsonObject();
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String langValue(JsonObject lang, String key) {
        var value = lang.get(key);
        return value == null ? "" : value.getAsString();
    }
}
