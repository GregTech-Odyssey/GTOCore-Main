package com.gtocore.client.website;

import com.gtocore.common.website.BugLogs;
import com.gtocore.utils.GTOWebsite;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.Popup;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * 官网登录：玩家在官网"游戏连接"页复制游戏 token，在游戏里从剪贴板粘贴，核对后保存在 .minecraft/gtocore/website/account.json。
 */
@OnlyIn(Dist.CLIENT)
@DataGeneratorScanned
public final class WebsiteAccount {

    @RegisterLanguage(cn = "登录GTO官网", en = "Sign in to the GTO website")
    static final String TITLE = "gtocore.website.account.title";
    @RegisterLanguage(cn = "/bug以你的官网账号提交反馈，需要先登录。", en = "/bug files reports with your website account, so you need to sign in first.")
    static final String INTRO = "gtocore.website.account.intro";
    @RegisterLanguage(cn = "1. 点击“打开官网”，登录后在“游戏连接”页生成并复制token。", en = "1. Press \"Open website\", sign in, then generate and copy the token on the Game connection page.")
    static final String STEP_1 = "gtocore.website.account.step.1";
    @RegisterLanguage(cn = "2. 回到游戏，点击“粘贴token”。", en = "2. Come back to the game and press \"Paste token\".")
    static final String STEP_2 = "gtocore.website.account.step.2";
    @RegisterLanguage(cn = "3. 登录成功后再次输入/bug。", en = "3. Once signed in, type /bug again.")
    static final String STEP_3 = "gtocore.website.account.step.3";
    @RegisterLanguage(cn = "打开官网", en = "Open website")
    static final String OPEN = "gtocore.website.account.open";
    @RegisterLanguage(cn = "粘贴token", en = "Paste token")
    static final String PASTE = "gtocore.website.account.paste";
    @RegisterLanguage(cn = "剪贴板里不是游戏token，请在官网“游戏连接”页点击复制后重试。", en = "The clipboard does not hold a game token. Copy it on the Game connection page and try again.")
    static final String NOT_TOKEN = "gtocore.website.account.not_token";
    @RegisterLanguage(cn = "正在核对token……", en = "Checking the token…")
    static final String CHECKING = "gtocore.website.account.checking";
    @RegisterLanguage(cn = "已登录为%s。再次输入/bug即可提交反馈。", en = "Signed in as %s. Type /bug again to file a report.")
    static final String SIGNED_IN = "gtocore.website.account.signed_in";
    @RegisterLanguage(cn = "登录失败：%s", en = "Sign-in failed: %s")
    static final String FAILED = "gtocore.website.account.failed";
    @RegisterLanguage(cn = "官网登录已失效，请重新登录。", en = "Your website sign-in is no longer valid. Please sign in again.")
    static final String EXPIRED = "gtocore.website.account.expired";

    private static final Pattern TOKEN = Pattern.compile("^gtog_[0-9a-f]{64}$");
    private static final Path FILE = GTOWebsite.DIR.resolve("website").resolve("account.json");
    static final String USER_AGENT = "GTOCore-Bug/1";

    @Nullable
    private static volatile String token;
    private static volatile Component status = Component.empty();
    private static volatile int statusColor;

    private WebsiteAccount() {}

    public static void init() {
        if (!Files.isRegularFile(FILE)) return;
        try {
            var root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            String stored = root.has("token") ? root.get("token").getAsString() : null;
            if (stored != null && TOKEN.matcher(stored).matches()) token = stored;
        } catch (Exception e) {
            GTOCore.LOGGER.warn("[Website] unreadable account: {}", e.toString());
        }
    }

    @Nullable
    public static String token() {
        return token;
    }

    public static void signOut(boolean expired) {
        token = null;
        try {
            Files.deleteIfExists(FILE);
        } catch (Exception e) {
            GTOCore.LOGGER.warn("[Website] cannot remove account: {}", e.toString());
        }
        if (expired) status(Component.translatable(EXPIRED), UITheme.STATUS_TEXT_WARNING);
    }

    public static void open() {
        Minecraft.getInstance().tell(() -> WebsitePopupScreen.open("website_account", null, close -> Popup.of(() -> Component.translatable(TITLE), content -> content(content, close))
                .setContentWidth(2 * UISizes.CONTENT_WIDTH)));
    }

    private static void content(UIElement content, Runnable close) {
        int width = 2 * UISizes.CONTENT_WIDTH;
        content.addChild(Label.translatable(width, INTRO));
        content.addChild(Label.translatable(width, STEP_1));
        content.addChild(Label.translatable(width, STEP_2));
        content.addChild(Label.translatable(width, STEP_3));
        content.addChild(Label.of(width, () -> status).bindClientColor(() -> statusColor));
        int action = 2 * UISizes.BUTTON_WIDTH;
        var website = Button.translatable(action, OPEN).setOnClientClick(() -> Util.getPlatform().openUri(GTOWebsite.origin() + "/account/game"));
        var paste = Button.translatable(action, PASTE).setVariant(UITheme.ButtonVariant.CONFIRM).setOnClientClick(() -> paste(close));
        content.addChild(UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(website, UIElement.flexSpacer(), paste));
    }

    private static void paste(Runnable close) {
        String pasted = Minecraft.getInstance().keyboardHandler.getClipboard().trim();
        if (!TOKEN.matcher(pasted).matches()) {
            status(Component.translatable(NOT_TOKEN), UITheme.STATUS_TEXT_ERROR);
            return;
        }
        status(Component.translatable(CHECKING), UITheme.textSecondary());
        CompletableFuture.runAsync(() -> verify(pasted, close));
    }

    private static void verify(String candidate, Runnable close) {
        try {
            var request = HttpRequest.newBuilder(URI.create(GTOWebsite.origin() + "/api/game/v1/account"))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + candidate)
                    .GET().build();
            var response = GTOWebsite.http().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                status(Component.translatable(FAILED, BugLogs.failure(response)), UITheme.STATUS_TEXT_ERROR);
                return;
            }
            var root = JsonParser.parseString(response.body()).getAsJsonObject();
            String name = root.has("displayName") ? root.get("displayName").getAsString() : "";
            var stored = new JsonObject();
            stored.addProperty("token", candidate);
            GTOWebsite.writeAtomically(FILE, stored.toString());
            token = candidate;
            Minecraft.getInstance().execute(() -> {
                close.run();
                var player = Minecraft.getInstance().player;
                if (player != null) player.sendSystemMessage(Component.translatable(SIGNED_IN, name).withStyle(s -> s.withColor(UITheme.STATUS_TEXT_GOOD)));
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            status(Component.translatable(FAILED, String.valueOf(e.getMessage())), UITheme.STATUS_TEXT_ERROR);
        }
    }

    private static void status(Component text, int color) {
        status = text;
        statusColor = color;
    }
}
