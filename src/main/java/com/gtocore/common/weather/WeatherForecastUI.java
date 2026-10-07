package com.gtocore.common.weather;

import com.gtocore.common.saved.DysonSphereSavaedData;
import com.gtocore.common.saved.VoidWorldTimeSavedData;

import com.gtolib.GTOCore;
import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.GTODimensions;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.datastream.codec.ByteStreamCodec;
import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import earth.terrarium.adastra.api.planets.PlanetApi;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/** Server-authoritative planet tabs; only the visible page synchronizes its rolling forecast. */
public final class WeatherForecastUI extends UIFactory<WeatherForecastUI.Target> {

    private static final WeatherForecastUI FACTORY = new WeatherForecastUI();
    private static final int WIDTH = 360;
    private static final Component NO_CHANGE = Component.translatable("gtocore.weather.forecast_no_change");
    private static final Component RUNNING = Component.translatable("gtocore.weather.forecast_running");
    private static final Component PAUSED = Component.translatable("gtocore.weather.forecast_paused");
    private static final Component SUNRISE = Component.translatable("gtocore.weather.forecast_sunrise");
    private static final Component SUNSET = Component.translatable("gtocore.weather.forecast_sunset");

    private WeatherForecastUI() {
        super(GTOCore.id("weather_forecast"));
    }

    public static void init() {
        UIFactory.register(FACTORY);
    }

    public static void open(ServerPlayer player) {
        FACTORY.openUI(new Target(WeatherSystem.get(player.server).forecastDimensions(player)), player);
    }

    @Override
    protected @Nullable ModularUI createUITemplate(@Nullable Target holder, Player player) {
        return holder == null ? null : holder.createUI(player);
    }

    @Override
    protected Target readHolderFromSyncData(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        var dimensions = new ObjectArrayList<ResourceKey<Level>>(count);
        for (int i = 0; i < count; i++) {
            dimensions.add(ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()));
        }
        return new Target(dimensions);
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf buf, Target holder) {
        buf.writeVarInt(holder.dimensions.size());
        for (var key : holder.dimensions) {
            buf.writeResourceLocation(key.location());
        }
    }

    public static final class Target implements IUIHolder {

        private final ObjectArrayList<ResourceKey<Level>> dimensions;

        private Target(ObjectArrayList<ResourceKey<Level>> dimensions) {
            this.dimensions = dimensions;
        }

        @Override
        public ModularUI createUI(Player player) {
            var tabs = new ObjectArrayList<PlanetTab>(dimensions.size());
            for (var key : dimensions) {
                tabs.add(new PlanetTab(key, player, tabs));
            }
            var window = new MachineWindow(tabs.getFirst()).setCentered(true).setTitleFollowsTab(true);
            return new ModularUI(WIDTH + 2 * UISizes.WINDOW_PADDING_X, 180, this, player).widget(window);
        }

        @Override
        public boolean isInvalid() {
            return false;
        }

        @Override
        public boolean isRemote() {
            return GTCEu.isClientThread();
        }

        @Override
        public void markAsDirty() {}
    }

    private static final class PlanetTab implements IFancyUIProvider {

        private final ResourceKey<Level> dimension;
        private final Player player;
        private final ObjectArrayList<PlanetTab> tabs;
        private final Component title;
        private final IGuiTexture icon;
        private final List<Component> tabTooltips;

        private PlanetTab(ResourceKey<Level> dimension, Player player, ObjectArrayList<PlanetTab> tabs) {
            this.dimension = dimension;
            this.player = player;
            this.tabs = tabs;
            var planet = Dimension.getIncludingOrbits(dimension);
            title = planet == null ? Component.literal(dimension.location().toString()) :
                    Component.translatable(planet.getOrbit() == dimension ?
                            "planet." + dimension.location().getNamespace() + "." + dimension.location().getPath() :
                            planet.getKey());
            icon = planet == null ? new ItemStackTexture(Items.CLOCK) : new ResourceTexture(planet.getIcon());
            tabTooltips = List.of(title);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var source = new ForecastSource(dimension, player);
            var page = UIElement.column(WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP));
            var status = new StatusPanel(WIDTH);
            status.addLine("gtocore.weather.forecast_current", source::current);
            status.addLine("gtocore.weather.forecast_next", source::next);
            status.addLine("gtocore.weather.forecast_change_in", source::changeIn);
            status.addLine("gtocore.weather.forecast_cycle", () -> source.get().paused() ? PAUSED : RUNNING);
            page.addChild(status);
            var axis = UIElement.row(TextLine.HEIGHT).layout(l -> l.width(WIDTH));
            axis.addChild(TextLine.translatable(WIDTH / 2, "gtocore.weather.forecast_now"));
            axis.addChild(
                    TextLine.constant(WIDTH / 2, time(WeatherTimeline.FORECAST_TICKS)).setTextAlign(Horizontal.RIGHT));
            page.addChild(axis);
            page.addChild(new ForecastBar(source));
            page.addChild(TextLine.translatable(WIDTH, "gtocore.weather.forecast_bar_hint"));
            page.addChild(TextLine.translatable(WIDTH, "gtocore.weather.forecast_scale_hint"));
            var legend = UIElement.column(WIDTH).layout(l -> l.gapAll(UISizes.GAP));
            UIElement row = null;
            int index = 0;
            for (var weather : possibleWeather(dimension)) {
                if (index++ % 3 == 0) {
                    row = UIElement.row(TextLine.HEIGHT).layout(l -> l.width(WIDTH).alignCenter());
                    legend.addChild(row);
                }
                var entry = UIElement.row(TextLine.HEIGHT)
                        .layout(l -> l.width(WIDTH / 3).alignCenter().gapAll(UISizes.GAP));
                entry.addChild(
                        new UIElement().layout(l -> l.size(6, 6)).setBackground(new ColorRectTexture(color(weather))));
                entry.addChild(TextLine.constant(LayoutStyle.AUTO, weather.displayName()).layout(l -> l.flex(1)));
                row.addChild(entry);
            }
            page.addChild(legend);
            return page;
        }

        @Override
        public void attachSideTabs(TabsWidget sideTabs) {
            sideTabs.setMainTab(tabs.getFirst());
            for (int i = 1; i < tabs.size(); i++) {
                sideTabs.attachSubTab(tabs.get(i));
            }
        }

        @Override
        public boolean hasPlayerInventory() {
            return false;
        }

        @Override
        public Component getTitle() {
            return title;
        }

        @Override
        public IGuiTexture getTabIcon() {
            return icon;
        }

        @Override
        public List<Component> getTabTooltips() {
            return tabTooltips;
        }
    }

    private static ObjectArrayList<WeatherType> possibleWeather(ResourceKey<Level> dimension) {
        var profile = WeatherSystem.profile(dimension);
        var result = new ObjectArrayList<WeatherType>(profile.entries().length + 2);
        for (var entry : profile.entries()) {
            result.add(entry.weather());
        }
        if (profile.stellarInfluence()) {
            if (!result.contains(WeatherTypes.CLEAR)) {
                result.add(WeatherTypes.CLEAR);
            }
            if (!result.contains(WeatherTypes.THUNDER)) {
                result.add(WeatherTypes.THUNDER);
            }
        }
        return result;
    }

    private record Forecast(long clock, ObjectArrayList<WeatherTimeline.Period> periods, boolean paused,
                            long gameTime, long dayTime, boolean daylightCycle, boolean hasDayNight) {

        private static final Forecast EMPTY = new Forecast(0, new ObjectArrayList<>(0), false, 0, 0, false, false);
        private static final ByteStreamCodec<Forecast> CODEC = new ByteStreamCodec<>() {

            @Override
            public void encode(FriendlyByteBuf buf, Forecast value) {
                buf.writeVarLong(value.clock());
                buf.writeBoolean(value.paused());
                buf.writeVarLong(value.gameTime());
                buf.writeVarLong(value.dayTime());
                buf.writeByte((value.daylightCycle() ? 1 : 0) | (value.hasDayNight() ? 2 : 0));
                buf.writeVarInt(value.periods().size());
                for (var period : value.periods()) {
                    WeatherTypes.REGISTRY.streamCodec().encode(buf, period.weather());
                    buf.writeVarInt((int) (period.end() - value.clock()));
                }
            }

            @Override
            public Forecast decode(FriendlyByteBuf buf) {
                long clock = buf.readVarLong();
                boolean paused = buf.readBoolean();
                long gameTime = buf.readVarLong();
                long dayTime = buf.readVarLong();
                int flags = buf.readUnsignedByte();
                int count = buf.readVarInt();
                var periods = new ObjectArrayList<WeatherTimeline.Period>(count);
                long start = clock;
                for (int i = 0; i < count; i++) {
                    var weather = WeatherTypes.REGISTRY.streamCodec().decode(buf);
                    long end = clock + buf.readVarInt();
                    periods.add(new WeatherTimeline.Period(weather, start, end));
                    start = end;
                }
                return new Forecast(clock, periods, paused, gameTime, dayTime, (flags & 1) != 0, (flags & 2) != 0);
            }
        };
    }

    private static final class ForecastSource {

        private final ResourceKey<Level> dimension;
        private final Player player;
        private int sampledTick = Integer.MIN_VALUE;
        private Forecast forecast = Forecast.EMPTY;
        private Component current = Component.empty();
        private Component next = NO_CHANGE;
        private Component changeIn = NO_CHANGE;

        private ForecastSource(ResourceKey<Level> dimension, Player player) {
            this.dimension = dimension;
            this.player = player;
        }

        private Forecast get() {
            var server = player.getServer();
            int tick = server.getTickCount();
            if (sampledTick == Integer.MIN_VALUE || tick - sampledTick >= 20) {
                sampledTick = tick;
                var system = WeatherSystem.get(server);
                var level = server.getLevel(dimension);
                boolean hasDayNight = level != null && level.dimensionType().hasSkyLight() &&
                        !level.dimensionType().hasFixedTime() &&
                        dimension != GTODimensions.SOLAR_SURFACE && !GTODimensions.isOrbit(dimension) &&
                        !PlanetApi.API.isSpace(level) &&
                        !(GTODimensions.isVoid(dimension) && VoidWorldTimeSavedData.INSTANCE.isFixedTime()) &&
                        DysonSphereSavaedData.getDimensionLaunchData(dimension) <= 100;
                var timeLevel = level == null ? server.overworld() : level;
                forecast = new Forecast(system.clock(), system.forecast(dimension),
                        !server.overworld().getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE),
                        timeLevel.getGameTime(), timeLevel.getDayTime(),
                        timeLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT), hasDayNight);
                current = forecast.periods().getFirst().weather().displayName();
                next = forecast.periods().size() > 1 ? forecast.periods().get(1).weather().displayName() : NO_CHANGE;
                changeIn = forecast.periods().size() > 1 ?
                        time(forecast.periods().getFirst().end() - forecast.clock()) : NO_CHANGE;
            }
            return forecast;
        }

        private Component current() {
            get();
            return current;
        }

        private Component next() {
            get();
            return next;
        }

        private Component changeIn() {
            get();
            return changeIn;
        }
    }

    private static final class ForecastBar extends UIElement {

        private static final int MARKER_HEIGHT = 14;
        private static final int BAR_HEIGHT = 24;
        private static final int TICK_INTERVAL = 3000;
        private static final int MAJOR_INTERVAL = 12000;
        private static final int SUNRISE_COLOR = 0xFFFFAD32;
        private static final int SUNSET_COLOR = 0xFF526BC9;
        private static final int ICON_SIZE = 12;
        private static final ResourceLocation SUNRISE_SOURCE = ResourceLocation
                .parse("ad_astra:textures/environment/sun.png");
        private static final ResourceLocation SUNSET_SOURCE = ResourceLocation
                .parse("ad_astra:textures/environment/moon.png");
        private static final ResourceTexture SUNRISE_TEXTURE = new ResourceTexture(GTOCore.id("weather_forecast/sun"));
        private static final ResourceTexture SUNSET_TEXTURE = new ResourceTexture(GTOCore.id("weather_forecast/moon"));

        private record DayMarker(long gameTime, boolean sunrise, List<Component> tooltip) {}

        private final SyncValue<Forecast> forecast;
        private final ObjectArrayList<List<Component>> tooltips = new ObjectArrayList<>(16);
        private final ObjectArrayList<DayMarker> dayMarkers = new ObjectArrayList<>(8);
        private final List<Component> scaleTooltip = List
                .of(Component.translatable("gtocore.weather.forecast_scale_hint"));
        private double frameElapsed;
        private double animationGameTime;
        private long animationNanos;
        private double pixelScale = 1;
        private double pixelOrigin;

        private ForecastBar(ForecastSource source) {
            layout(l -> l.size(WIDTH, MARKER_HEIGHT + BAR_HEIGHT));
            forecast = addSyncValue(
                    SyncValue.of(source::get, Forecast.CODEC, Forecast.EMPTY).onChanged(this::updateTooltips));
        }

        private void updateTooltips(Forecast value) {
            tooltips.clear();
            for (var period : value.periods()) {
                tooltips.add(
                        List.of(period.weather().displayName(), Component.translatable("gtocore.weather.forecast_range",
                                time(period.start() - value.clock()), time(period.end() - value.clock()))));
            }
            dayMarkers.clear();
            if (value.hasDayNight() && value.daylightCycle()) {
                for (long offset = Math.floorMod(-value.dayTime(), MAJOR_INTERVAL); offset <=
                        WeatherTimeline.FORECAST_TICKS; offset += MAJOR_INTERVAL) {
                    boolean sunrise = Math.floorMod(value.dayTime() + offset, 24000) == 0;
                    dayMarkers.add(new DayMarker(value.gameTime() + offset, sunrise,
                            List.of(sunrise ? SUNRISE : SUNSET, time(offset))));
                }
            }
        }

        private double weatherElapsed() {
            return forecast.getValue().paused() ? 0 : frameElapsed;
        }

        private double advanceAnimation(double targetGameTime, long now, boolean paused) {
            long elapsed = now - animationNanos;
            if (animationNanos == 0 || elapsed > 1_000_000_000L || paused) {
                animationGameTime = targetGameTime;
            } else {
                double seconds = elapsed / 1_000_000_000.0;
                double predicted = animationGameTime + seconds * 20;
                double error = targetGameTime - predicted;
                animationGameTime = Math.abs(error) > 40 ? targetGameTime :
                        predicted + error * -Math.expm1(-8 * seconds);
            }
            animationNanos = now;
            return animationGameTime;
        }

        private float position(long time) {
            return Math.clamp(offsetPosition(time - forecast.getValue().clock() - weatherElapsed()), getPositionX() + 1,
                    getPositionX() + getSizeWidth() - 1);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            int trackTop = getPositionY() + MARKER_HEIGHT;
            UITheme.PROGRESS_TRACK.draw(graphics, mouseX, mouseY, getPositionX(), trackTop, getSizeWidth(), BAR_HEIGHT);
            var value = forecast.getValue();
            var periods = value.periods();
            if (periods.isEmpty()) {
                return;
            }
            var minecraft = Minecraft.getInstance();
            var level = minecraft.level;
            frameElapsed = level == null ? 0 : Math.max(0, advanceAnimation(level.getGameTime() + (double) partialTicks,
                    System.nanoTime(), minecraft.isPaused()) - value.gameTime());
            var matrix = graphics.pose().last().pose();
            pixelScale = minecraft.getWindow().getGuiScale() * matrix.m00();
            pixelOrigin = minecraft.getWindow().getGuiScale() * matrix.m30();
            int top = trackTop + 1, bottom = trackTop + BAR_HEIGHT - 1;
            int left = getPositionX() + 1, right = getPositionX() + getSizeWidth() - 1;
            graphics.enableScissor(left, top, right, bottom);
            drawWeather(graphics, periods, top, bottom);
            drawTicks(graphics, value, top);
            for (int i = 0; i < dayMarkers.size(); i++) {
                var marker = dayMarkers.get(i);
                double offset = marker.gameTime() - value.gameTime() - frameElapsed;
                if (offset < 0 || offset > WeatherTimeline.FORECAST_TICKS) {
                    continue;
                }
                float x = offsetPosition(offset);
                int markerColor = marker.sunrise() ? SUNRISE_COLOR : SUNSET_COLOR;
                fillBarAA(graphics, x - 0.5f, top, x + 0.5f, bottom, markerColor);
            }
            // Submit the floating-point quads while the bar's scissor is still active.
            graphics.flush();
            graphics.disableScissor();
            graphics.enableScissor(left, getPositionY(), right, trackTop);
            for (int i = 0; i < dayMarkers.size(); i++) {
                var marker = dayMarkers.get(i);
                double offset = marker.gameTime() - value.gameTime() - frameElapsed;
                if (offset < 0 || offset > WeatherTimeline.FORECAST_TICKS) {
                    continue;
                }
                var texture = markerTexture(minecraft.getTextureManager(), marker.sunrise());
                texture.draw(graphics, mouseX, mouseY, offsetPosition(offset) - ICON_SIZE / 2f, getPositionY() + 1,
                        ICON_SIZE, ICON_SIZE);
            }
            graphics.disableScissor();
        }

        private float offsetPosition(double offset) {
            return getPositionX() + 1 + (float) (offset * (getSizeWidth() - 2) / WeatherTimeline.FORECAST_TICKS);
        }

        @OnlyIn(Dist.CLIENT)
        private static ResourceTexture markerTexture(TextureManager manager, boolean sunrise) {
            var texture = sunrise ? SUNRISE_TEXTURE : SUNSET_TEXTURE;
            // The one-argument lookup would load the runtime alias as a nonexistent resource file.
            if (!(manager.getTexture(texture.imageLocation, null) instanceof LinearTexture)) {
                manager.register(texture.imageLocation, new LinearTexture(sunrise ? SUNRISE_SOURCE : SUNSET_SOURCE));
            }
            return texture;
        }

        @OnlyIn(Dist.CLIENT)
        private static final class LinearTexture extends SimpleTexture {

            private LinearTexture(ResourceLocation source) {
                super(source);
            }

            @Override
            public void load(ResourceManager resources) throws IOException {
                super.load(resources);
                setFilter(true, false);
            }
        }

        private float pixelPosition(int pixel) {
            return (float) ((pixel - pixelOrigin) / pixelScale);
        }

        @OnlyIn(Dist.CLIENT)
        private void drawWeather(GuiGraphics graphics, ObjectArrayList<WeatherTimeline.Period> periods, int top,
                                 int bottom) {
            int pendingPixel = 0;
            double weight = 0, red = 0, green = 0, blue = 0;
            for (int i = 0; i < periods.size(); i++) {
                var period = periods.get(i);
                double start = pixelOrigin + (i == 0 ? getPositionX() + 1 : position(period.start())) * pixelScale;
                double end = pixelOrigin +
                        (i == periods.size() - 1 ? getPositionX() + getSizeWidth() - 1 : position(period.end())) *
                                pixelScale;
                if (start >= end) {
                    continue;
                }
                int first = (int) Math.floor(start), last = (int) Math.floor(end);
                int color = color(period.weather());
                int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
                double coverage = Math.min(end, first + 1.0) - start;
                if (weight > 0 && pendingPixel != first) {
                    fillWeatherPixel(graphics, pendingPixel, top, bottom, red, green, blue, weight);
                    weight = red = green = blue = 0;
                }
                pendingPixel = first;
                weight += coverage;
                red += r * coverage;
                green += g * coverage;
                blue += b * coverage;
                if (first == last) {
                    continue;
                }
                fillWeatherPixel(graphics, first, top, bottom, red, green, blue, weight);
                fillBar(graphics, pixelPosition(first + 1), top, pixelPosition(last), bottom, color);
                pendingPixel = last;
                weight = end - last;
                red = r * weight;
                green = g * weight;
                blue = b * weight;
            }
            if (weight > 0) {
                fillWeatherPixel(graphics, pendingPixel, top, bottom, red, green, blue, weight);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void fillWeatherPixel(GuiGraphics graphics, int pixel, int top, int bottom, double red, double green,
                                      double blue, double weight) {
            int color = 0xFF000000 | (int) Math.round(red / weight) << 16 | (int) Math.round(green / weight) << 8 |
                    (int) Math.round(blue / weight);
            fillBar(graphics, pixelPosition(pixel), top, pixelPosition(pixel + 1), bottom, color);
        }

        @OnlyIn(Dist.CLIENT)
        private void fillBarAA(GuiGraphics graphics, float left, int top, float right, int bottom, int color) {
            double start = pixelOrigin + Math.max(left, getPositionX() + 1) * pixelScale;
            double end = pixelOrigin + Math.min(right, getPositionX() + getSizeWidth() - 1) * pixelScale;
            if (start >= end) {
                return;
            }
            int first = (int) Math.floor(start), last = (int) Math.floor(end);
            double coverage = Math.min(end, first + 1.0) - start;
            fillBar(graphics, pixelPosition(first), top, pixelPosition(first + 1), bottom,
                    coverageColor(color, coverage));
            if (first == last) {
                return;
            }
            fillBar(graphics, pixelPosition(first + 1), top, pixelPosition(last), bottom, color);
            if (end > last) {
                fillBar(graphics, pixelPosition(last), top, pixelPosition(last + 1), bottom,
                        coverageColor(color, end - last));
            }
        }

        private static int coverageColor(int color, double coverage) {
            return color & 0xFFFFFF | (int) Math.round((color >>> 24) * coverage) << 24;
        }

        @OnlyIn(Dist.CLIENT)
        private void fillBar(GuiGraphics graphics, float left, int top, float right, int bottom, int color) {
            left = Math.max(left, getPositionX() + 1);
            right = Math.min(right, getPositionX() + getSizeWidth() - 1);
            if (left >= right) {
                return;
            }
            var matrix = graphics.pose().last().pose();
            var vertices = graphics.bufferSource().getBuffer(RenderType.gui());
            vertices.vertex(matrix, left, top, 0).color(color).endVertex();
            vertices.vertex(matrix, left, bottom, 0).color(color).endVertex();
            vertices.vertex(matrix, right, bottom, 0).color(color).endVertex();
            vertices.vertex(matrix, right, top, 0).color(color).endVertex();
        }

        @OnlyIn(Dist.CLIENT)
        private void drawTicks(GuiGraphics graphics, Forecast value, int top) {
            boolean dayScale = value.hasDayNight() && value.daylightCycle();
            long clock = dayScale ? value.dayTime() : value.clock();
            double elapsed = dayScale ? frameElapsed : weatherElapsed();
            long first = clock - Math.floorMod(clock, TICK_INTERVAL);
            long horizon = clock + WeatherTimeline.FORECAST_TICKS + TICK_INTERVAL;
            for (long tick = first; tick <= horizon; tick += TICK_INTERVAL) {
                double offset = tick - clock - elapsed;
                float x = offsetPosition(offset);
                int height = tick % MAJOR_INTERVAL == 0 ? BAR_HEIGHT - 2 : tick % 6000 == 0 ? 8 : 4;
                fillBarAA(graphics, x - 0.5f, top, x + 0.5f, top + height, 0xFF252525);
            }
        }

        @Override
        public boolean hasOwnTooltip(int mouseX, int mouseY) {
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            if (gui == null || gui.getModularUIGui() == null || !isMouseOverElement(mouseX, mouseY)) {
                return;
            }
            var value = forecast.getValue();
            if (mouseY < getPositionY() + MARKER_HEIGHT) {
                for (int i = 0; i < dayMarkers.size(); i++) {
                    var marker = dayMarkers.get(i);
                    double offset = marker.gameTime() - value.gameTime() - frameElapsed;
                    if (offset >= 0 && offset <= WeatherTimeline.FORECAST_TICKS &&
                            Math.abs(mouseX - offsetPosition(offset)) <= ICON_SIZE / 2f) {
                        gui.getModularUIGui().setHoverTooltip(marker.tooltip(), ItemStack.EMPTY, null, null);
                        return;
                    }
                }
                gui.getModularUIGui().setHoverTooltip(scaleTooltip, ItemStack.EMPTY, null, null);
                return;
            }
            var periods = value.periods();
            for (int i = 0; i < periods.size(); i++) {
                var period = periods.get(i);
                float start = i == 0 ? getPositionX() + 1 : position(period.start());
                float end = i == periods.size() - 1 ? getPositionX() + getSizeWidth() - 1 : position(period.end());
                if (mouseX >= start && mouseX < end) {
                    gui.getModularUIGui().setHoverTooltip(tooltips.get(i), ItemStack.EMPTY, null, null);
                    return;
                }
            }
        }
    }

    private static Component time(long ticks) {
        return Component.translatable("gtocore.weather.forecast_time", ticks / 1000, ticks % 1000 * 60 / 1000);
    }

    private static int color(WeatherType weather) {
        return switch (weather.id()) {
            case "clear" -> 0xFFF2C94C;
            case "rain" -> 0xFF4C9BE8;
            case "thunder" -> 0xFF8B6CCF;
            case "calm" -> 0xFF78C9C3;
            case "solar_storm" -> 0xFFFF7043;
            case "acid_rain" -> 0xFF8DBF45;
            case "methane_rain" -> 0xFF32A89D;
            case "dust_storm" -> 0xFFC18B52;
            case "snow" -> 0xFFDCEEF5;
            default -> UITheme.textSecondary();
        };
    }
}
