package com.gtocore.client.guide;

import com.gtocore.client.website.WebsiteConsent;
import com.gtocore.client.website.WebsitePopupScreen;
import com.gtocore.utils.GTOWebsite;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.tooltip.TipTheme;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.screen.EmiScreenManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

/**
 * 物品攻略：征得同意后每次启动在后台下载 gtodyssey.com 的攻略目录，悬停已关联攻略的物品时提示并按键打开选择列表。
 */
@OnlyIn(Dist.CLIENT)
@DataGeneratorScanned
public final class ItemGuides {

    @RegisterLanguage(cn = "查看物品攻略", en = "View item guides")
    public static final String KEY_NAME = "key.gtocore.item_guides";
    @RegisterLanguage(cn = "查看相关攻略（%s 篇）", en = "View related guides (%s)")
    static final String TIP_VIEW = "gtocore.item_guides.tip";

    public record Guide(ItemGuideCatalogue.Article article, String version) {}

    public static final KeyMapping KEY = new KeyMapping(KEY_NAME, KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.keybinding.gtocore");

    private static final String USER_AGENT = "GTOCore-ItemGuides/1";
    private static final int MAX_BYTES = 16 << 20;
    private static final Comparator<Ranked> ORDER = Comparator.comparingInt(Ranked::rank)
            .thenComparing(Comparator.comparingLong((Ranked ranked) -> ranked.article().published()).reversed())
            .thenComparing(ranked -> ranked.article().id());
    @Nullable
    private static final boolean NO_BROWSER = devProperty("gtocore.itemGuides.noBrowser") != null;
    private static final Path DIR = GTOWebsite.DIR.resolve("item-guides");
    private static final Path STATE = DIR.resolve("state.json");
    private static final Path CACHE = DIR.resolve("index.json");
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
        var thread = new Thread(runnable, "GTO-ItemGuides");
        thread.setDaemon(true);
        return thread;
    });
    private static final Int2ObjectOpenHashMap<Component> TIP_LINES = new Int2ObjectOpenHashMap<>();

    private static volatile Index index = Index.EMPTY;
    private static volatile long syncedAt;
    private static volatile boolean offline;
    @Nullable
    private static volatile String chosenLocale;
    @Nullable
    private static String etag;
    @Nullable
    private static InputConstants.Key tipKey;
    @Nullable
    private static KeyModifier tipModifier;
    @Nullable
    private static String tipLanguage;

    private ItemGuides() {}

    public static void init(IEventBus modBus) {
        KeyMappingRegistry.register(KEY);
        modBus.addListener(ItemGuides::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(ItemGuides::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(ItemGuides::onKey);
    }

    public static long syncedAt() {
        return syncedAt;
    }

    public static boolean offline() {
        return offline;
    }

    public static void consentChanged(boolean granted) {
        if (!granted) index = Index.EMPTY;
        WORKER.execute(() -> {
            readState();
            if (granted) {
                if (index.isEmpty()) loadCache();
                fetch();
            }
        });
    }

    public static Component keyName() {
        if (KEY.isUnbound()) return Component.translatable("gtceu.tip.key.unbound").withStyle(Style.EMPTY.withColor(TipTheme.ATTENTION));
        return KEY.getTranslatedKeyMessage().copy().withStyle(Style.EMPTY.withColor(TipTheme.VALUE));
    }

    static List<ItemGuideCatalogue.Locale> locales() {
        return index.locales;
    }

    static String siteLocale() {
        var locales = index.locales;
        String chosen = chosenLocale;
        if (chosen != null && ItemGuideCatalogue.indexOf(locales, chosen) >= 0) return chosen;
        String language = Minecraft.getInstance().getLanguageManager().getSelected().toLowerCase(Locale.ROOT);
        for (var locale : locales) {
            if (language.startsWith(locale.id())) return locale.id();
        }
        return "en";
    }

    static void chooseLocale(String locale) {
        chosenLocale = locale;
        WORKER.execute(ItemGuides::writeState);
    }

    @Nullable
    static String localeName(String id) {
        var locales = index.locales;
        int found = ItemGuideCatalogue.indexOf(locales, id);
        return found < 0 ? null : locales.get(found).name();
    }

    static String title(Guide guide) {
        var titles = guide.article().titles();
        String own = titles.get(siteLocale());
        if (own != null) return own;
        String english = titles.get("en");
        return english != null ? english : titles.get(guide.article().sourceLocale());
    }

    static String pageUrl(Guide guide) {
        var article = guide.article();
        String locale = siteLocale();
        if (!article.hasLocale(locale)) locale = article.sourceLocale();
        return GTOWebsite.origin() + "/articles/" + locale + "/" + article.id();
    }

    static void openPage(Guide guide) {
        String url = pageUrl(guide);
        if (NO_BROWSER) {
            GTOCore.LOGGER.info("[ItemGuides] open {}", url);
            return;
        }
        Util.getPlatform().openUri(URI.create(url));
    }

    @Nullable
    public static Guide[] guidesOf(Item item) {
        return index.items.get(item);
    }

    @Nullable
    public static Guide[] guidesOf(Fluid fluid) {
        return index.fluids.get(fluid);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        WORKER.execute(ItemGuides::boot);
    }

    private static void boot() {
        readState();
        if (!WebsiteConsent.granted()) return;
        loadCache();
        fetch();
    }

    private static void onTooltip(ItemTooltipEvent event) {
        var current = index;
        if (current.isEmpty()) return;
        Guide[] guides = current.items.get(event.getItemStack().getItem());
        if (guides != null) event.getToolTip().add(tipLine(guides.length));
    }

    private static Component tipLine(int count) {
        var key = KEY.getKey();
        var modifier = KEY.getKeyModifier();
        String language = Minecraft.getInstance().getLanguageManager().getSelected();
        if (!key.equals(tipKey) || modifier != tipModifier || !language.equals(tipLanguage)) {
            TIP_LINES.clear();
            tipKey = key;
            tipModifier = modifier;
            tipLanguage = language;
        }
        var line = TIP_LINES.get(count);
        if (line == null) {
            line = Tips.control(Tips.keyAction(keyName(), Component.translatable(TIP_VIEW, count)));
            TIP_LINES.put(count, line);
        }
        return line;
    }

    private static void onKey(ScreenEvent.KeyPressed.Pre event) {
        if (index.isEmpty()) return;
        if (!KEY.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) return;
        Screen screen = event.getScreen();
        if (WebsitePopupScreen.isOpen("item_guides") || typing(screen)) return;
        var target = hovered(screen);
        if (target == null) return;
        event.setCanceled(true);
        Minecraft.getInstance().tell(() -> ItemGuidesScreen.openChooser(screen, target.icon(), target.name(), target.guides()));
    }

    private record Target(ItemStack icon, Component name, Guide[] guides) {}

    @Nullable
    private static Target hovered(Screen screen) {
        var interaction = EmiApi.getHoveredStack(true);
        if (!interaction.isEmpty()) {
            for (var stack : interaction.getStack().getEmiStacks()) {
                Item item = stack.getKeyOfType(Item.class);
                if (item != null) {
                    Guide[] guides = guidesOf(item);
                    if (guides != null) return new Target(stack.getItemStack().copyWithCount(1), stack.getName(), guides);
                }
                Fluid fluid = stack.getKeyOfType(Fluid.class);
                if (fluid != null && fluid != Fluids.EMPTY) {
                    Guide[] guides = guidesOf(fluid);
                    if (guides != null) return new Target(fluid.getBucket().getDefaultInstance(), fluid.getFluidType().getDescription(), guides);
                }
            }
            return null;
        }
        if (screen instanceof AbstractContainerScreen<?> container) {
            var slot = container.getSlotUnderMouse();
            if (slot != null && slot.hasItem()) {
                var stack = slot.getItem();
                Guide[] guides = guidesOf(stack.getItem());
                if (guides != null) return new Target(stack.copyWithCount(1), stack.getHoverName(), guides);
            }
        }
        return null;
    }

    private static boolean typing(Screen screen) {
        if (EmiScreenManager.search.isFocused()) return true;
        return screen.getFocused() instanceof EditBox box && box.isFocused();
    }

    private static void readState() {
        if (!Files.isRegularFile(STATE)) return;
        try {
            var root = JsonParser.parseString(Files.readString(STATE, StandardCharsets.UTF_8)).getAsJsonObject();
            chosenLocale = root.has("locale") ? root.get("locale").getAsString() : null;
            etag = root.has("etag") ? root.get("etag").getAsString() : null;
            syncedAt = root.has("syncedAt") ? root.get("syncedAt").getAsLong() : 0;
        } catch (Exception e) {
            GTOCore.LOGGER.warn("[ItemGuides] unreadable state: {}", e.toString());
        }
    }

    private static void writeState() {
        var root = new JsonObject();
        String locale = chosenLocale;
        if (locale != null) root.addProperty("locale", locale);
        if (etag != null) root.addProperty("etag", etag);
        if (syncedAt > 0) root.addProperty("syncedAt", syncedAt);
        try {
            GTOWebsite.writeAtomically(STATE, root.toString());
        } catch (IOException e) {
            GTOCore.LOGGER.warn("[ItemGuides] cannot save state: {}", e.toString());
        }
    }

    private static void loadCache() {
        if (!Files.isRegularFile(CACHE)) return;
        try {
            if (Files.size(CACHE) > MAX_BYTES) throw new IOException("cache too large");
            publish(ItemGuideCatalogue.parse(Files.readString(CACHE, StandardCharsets.UTF_8)), "cache");
        } catch (Exception e) {
            GTOCore.LOGGER.warn("[ItemGuides] discarding unreadable cache: {}", e.toString());
            etag = null;
            try {
                Files.deleteIfExists(CACHE);
            } catch (IOException ignored) {}
        }
    }

    private static void fetch() {
        long started = System.nanoTime();
        try {
            var request = HttpRequest.newBuilder(URI.create(GTOWebsite.origin() + "/api/item-guides/v" + ItemGuideCatalogue.SCHEMA))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "gzip");
            if (etag != null && Files.isRegularFile(CACHE)) request.header("If-None-Match", etag);
            var response = GTOWebsite.http().send(request.GET().build(), HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                int status = response.statusCode();
                if (status == 304) {
                    synced();
                    GTOCore.LOGGER.info("[ItemGuides] catalogue unchanged ({} ms)", (System.nanoTime() - started) / 1_000_000);
                    return;
                }
                if (status != 200) throw new IOException("HTTP " + status);
                boolean gzip = response.headers().firstValue("Content-Encoding").map(value -> value.equalsIgnoreCase("gzip")).orElse(false);
                String json = readCapped(gzip ? new GZIPInputStream(body) : body);
                var catalogue = ItemGuideCatalogue.parse(json);
                GTOWebsite.writeAtomically(CACHE, json);
                etag = response.headers().firstValue("ETag").orElse(null);
                synced();
                publish(catalogue, "server");
                GTOCore.LOGGER.info("[ItemGuides] catalogue downloaded: {} bytes, {} articles, {} links ({} ms)", json.length(),
                        catalogue.articles().size(), catalogue.links().size(), (System.nanoTime() - started) / 1_000_000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            offline = true;
            GTOCore.LOGGER.warn("[ItemGuides] sync failed, keeping the cached catalogue: {}", e.toString());
        }
    }

    private static void synced() {
        syncedAt = System.currentTimeMillis();
        offline = false;
        writeState();
    }

    private static void publish(ItemGuideCatalogue catalogue, String source) {
        Minecraft.getInstance().execute(() -> {
            if (!WebsiteConsent.granted()) return;
            index = Index.build(catalogue);
            GTOCore.LOGGER.info("[ItemGuides] index from {}: {} items, {} fluids", source, index.items.size(), index.fluids.size());
        });
    }

    private static String readCapped(InputStream in) throws IOException {
        var out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
            if (out.size() > MAX_BYTES) throw new IOException("catalogue too large");
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    @Nullable
    private static String devProperty(String name) {
        return FMLLoader.isProduction() ? null : System.getProperty(name);
    }

    private record Ranked(ItemGuideCatalogue.Article article, String version, int rank) {}

    private record Index(List<ItemGuideCatalogue.Locale> locales, Reference2ObjectOpenHashMap<Item, Guide[]> items, Reference2ObjectOpenHashMap<Fluid, Guide[]> fluids) {

        static final Index EMPTY = new Index(Collections.emptyList(), new Reference2ObjectOpenHashMap<>(), new Reference2ObjectOpenHashMap<>());

        boolean isEmpty() {
            return items.isEmpty() && fluids.isEmpty();
        }

        static Index build(ItemGuideCatalogue catalogue) {
            var items = new Reference2ObjectOpenHashMap<Item, ObjectArrayList<Ranked>>();
            var fluids = new Reference2ObjectOpenHashMap<Fluid, ObjectArrayList<Ranked>>();
            for (var link : catalogue.links()) {
                var id = ResourceLocation.tryParse(link.id());
                if (id == null) continue;
                var ranked = new Ranked(catalogue.articles().get(link.article()), catalogue.versions().get(link.version()), link.version());
                if (link.fluid()) {
                    if (ForgeRegistries.FLUIDS.containsKey(id)) add(fluids.computeIfAbsent(ForgeRegistries.FLUIDS.getValue(id), k -> new ObjectArrayList<>()), ranked);
                } else if (ForgeRegistries.ITEMS.containsKey(id)) {
                    add(items.computeIfAbsent(ForgeRegistries.ITEMS.getValue(id), k -> new ObjectArrayList<>()), ranked);
                }
            }
            return new Index(catalogue.locales(), freeze(items), freeze(fluids));
        }

        private static void add(ObjectArrayList<Ranked> list, Ranked ranked) {
            for (int i = 0; i < list.size(); i++) {
                var other = list.get(i);
                if (other.article() != ranked.article()) continue;
                if (ranked.rank() < other.rank()) list.set(i, ranked);
                return;
            }
            list.add(ranked);
        }

        private static <K> Reference2ObjectOpenHashMap<K, Guide[]> freeze(Reference2ObjectOpenHashMap<K, ObjectArrayList<Ranked>> grouped) {
            var out = new Reference2ObjectOpenHashMap<K, Guide[]>(grouped.size());
            for (var entry : grouped.reference2ObjectEntrySet()) {
                var list = entry.getValue();
                list.sort(ORDER);
                var guides = new Guide[list.size()];
                for (int i = 0; i < guides.length; i++) guides[i] = new Guide(list.get(i).article(), list.get(i).version());
                out.put(entry.getKey(), guides);
            }
            return out;
        }
    }
}
