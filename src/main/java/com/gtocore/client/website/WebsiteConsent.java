package com.gtocore.client.website;

import com.gtocore.client.guide.ItemGuides;
import com.gtocore.utils.GTOWebsite;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;

/**
 * 官网联网授权：物品攻略与 /bug 共用一份同意书，同意全开、不同意全关；记录同意时同意书内容的哈希，内容一变进游戏就重新询问。
 */
@OnlyIn(Dist.CLIENT)
@DataGeneratorScanned
public final class WebsiteConsent {

    @RegisterLanguage(cn = "官网联网功能", en = "Website features")
    static final String TITLE = "gtocore.website.consent.title";
    @RegisterLanguage(cn = "以下功能需要连接GTO官网gtodyssey.com。启用即表示你同意下列内容；不启用时这些功能均不可用。",
                      en = "The features below connect to the GTO website gtodyssey.com. Enabling them means you agree to what is described here; without it none of them are available.")
    static final String INTRO = "gtocore.website.consent.intro";
    @RegisterLanguage(cn = "物品攻略", en = "Item guides")
    static final String GUIDES_HEADING = "gtocore.website.consent.guides";
    @RegisterLanguage(cn = "· 每次启动游戏时在后台获取一次攻略目录（标题、发布时间、关联物品，约30KB，不含正文）。悬停在已关联攻略的物品上时按%s选择攻略并在浏览器中打开。",
                      en = "· Each time the game starts, the guide catalogue (titles, dates and linked items, about 30 KB, no article text) is fetched once in the background. Hovering an item with guides, press %s to choose one and open it in your browser.")
    static final String GUIDES_1 = "gtocore.website.consent.guides.1";
    @RegisterLanguage(cn = "bug反馈（/bug）", en = "Bug reports (/bug)")
    static final String BUG_HEADING = "gtocore.website.consent.bug";
    @RegisterLanguage(cn = "· 登录官网账号后，输入/bug将以你的账号领取反馈链接，自动填写整合包版本、游戏环境与难度，并上传本机的日志与24小时内的崩溃报告。",
                      en = "· After signing in with your website account, /bug opens a report link in your name, fills in the pack version, environment and difficulty, and uploads this computer's log and any crash report from the last 24 hours.")
    static final String BUG_1 = "gtocore.website.consent.bug.1";
    @RegisterLanguage(cn = "· 在服务器中游玩时，服务器也会上传服务端日志（服主可关闭）。",
                      en = "· When playing on a server, the server also uploads its log (the server owner can turn this off).")
    static final String BUG_2 = "gtocore.website.consent.bug.2";
    @RegisterLanguage(cn = "· 日志将随issue公开，可能包含玩家名与聊天内容；IP地址会自动打码。超过5MB的日志会截取到3MB以内再上传。",
                      en = "· Logs are published with the issue and may contain player names and chat; IP addresses are masked. Logs over 5 MB are cut down to under 3 MB before upload.")
    static final String BUG_3 = "gtocore.website.consent.bug.3";
    @RegisterLanguage(cn = "其他", en = "Otherwise")
    static final String OTHER_HEADING = "gtocore.website.consent.other";
    @RegisterLanguage(cn = "· 除上述内容外，不发送存档、模组列表、设备信息或任何游戏内操作。攻略目录缓存与官网登录凭据保存在.minecraft/gtocore/，可随时删除。",
                      en = "· Nothing else is sent: no save data, mod list, device information or in-game activity. The guide cache and the website sign-in are kept in .minecraft/gtocore/ and may be deleted at any time.")
    static final String OTHER_1 = "gtocore.website.consent.other.1";
    @RegisterLanguage(cn = "· 输入/gtocorec website可重新打开本页；同意书内容更新后会再次询问。",
                      en = "· Type /gtocorec website to open this page again. You will be asked again whenever this text changes.")
    static final String OTHER_2 = "gtocore.website.consent.other.2";
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

    private static final String[] TERMS = { INTRO, GUIDES_HEADING, GUIDES_1, BUG_HEADING, BUG_1, BUG_2, BUG_3, OTHER_HEADING, OTHER_1, OTHER_2 };
    private static final Path STATE_FILE = GTOWebsite.DIR.resolve("website").resolve("consent.json");
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
        paragraph(content, width, Component.translatable(GUIDES_HEADING), Component.translatable(GUIDES_1, ItemGuides.keyName()));
        paragraph(content, width, Component.translatable(BUG_HEADING), Component.translatable(BUG_1), Component.translatable(BUG_2), Component.translatable(BUG_3));
        paragraph(content, width, Component.translatable(OTHER_HEADING), Component.translatable(OTHER_1), Component.translatable(OTHER_2));
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

    private static void paragraph(UIElement content, int width, Component heading, Component... lines) {
        var section = UIElement.column(width).layout(l -> l.gapAll(UISizes.GAP));
        section.addChild(TextLine.constant(width, heading.copy().withStyle(ChatFormatting.BOLD)));
        for (Component line : lines) section.addChild(Label.of(width, () -> line));
        content.addChild(section);
    }

    private static String termsHash() {
        var fields = new ObjectArrayList<RegisterLanguage>();
        var keys = new ObjectArrayList<String>();
        for (Field field : WebsiteConsent.class.getDeclaredFields()) {
            var language = field.getAnnotation(RegisterLanguage.class);
            if (language == null || !Modifier.isStatic(field.getModifiers()) || field.getType() != String.class) continue;
            try {
                field.setAccessible(true);
                String key = (String) field.get(null);
                for (String term : TERMS) {
                    if (term.equals(key)) {
                        fields.add(language);
                        keys.add(key);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        var order = new Integer[keys.size()];
        for (int i = 0; i < order.length; i++) order[i] = i;
        java.util.Arrays.sort(order, Comparator.comparing(keys::get));
        var text = new StringBuilder();
        for (int i : order) text.append(keys.get(i)).append('\n').append(fields.get(i).cn()).append('\n').append(fields.get(i).en()).append('\n');
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
