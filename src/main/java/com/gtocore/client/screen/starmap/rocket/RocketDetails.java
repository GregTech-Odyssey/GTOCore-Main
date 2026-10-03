package com.gtocore.client.screen.starmap.rocket;

import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.client.screen.starmap.base.StarSprites;

import com.gtolib.api.adastra.IAdDisplayTagName;

import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import earth.terrarium.adastra.common.constants.ConstantComponents;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * 星球详情卡片：环境信息、前往、所在轨道的空间站（前往 / 建造）。动作经 {@link RocketMapHost} 发 Ad Astra 的网络包。
 */
@OnlyIn(Dist.CLIENT)
final class RocketDetails {

    private static final int BADGE = 36;

    private RocketDetails() {}

    static void build(UIElement column, RocketMapHost host, int body) {
        var menu = host.planets();
        var catalog = host.catalog();
        var decorator = host.decorator();
        var planet = catalog.planet(body);
        var dimension = catalog.dimension(body);
        var orbit = catalog.orbit(body);
        if (planet == null || dimension == null || orbit == null) return;
        column.addChild(new Header(host, body));

        var info = UIElement.section();
        boolean reachable = decorator.reachable(body);
        info.addChild(row(RocketLang.LABEL_TIER, Component.translatable(RocketLang.TIER_VALUE, decorator.requiredTier(body)), reachable ? UITheme::panelText : () -> UITheme.STATUS_TEXT_ERROR));
        if (!reachable) {
            info.addChild(row(RocketLang.LABEL_CURRENT_ROCKET, Component.translatable(RocketLang.TIER_VALUE, decorator.rocketTier()), () -> UITheme.STATUS_TEXT_ERROR));
        }
        info.addChild(row(RocketLang.LABEL_GRAVITY, Component.translatable(RocketLang.GRAVITY_VALUE, String.format("%.2f", planet.gravity()))));
        info.addChild(row(RocketLang.LABEL_ATMOSPHERE, Component.translatable(planet.oxygen() ? RocketLang.BREATHABLE : RocketLang.NO_OXYGEN),
                planet.oxygen() ? () -> UITheme.STATUS_TEXT_GOOD : UITheme::panelText));
        info.addChild(row(RocketLang.LABEL_TEMPERATURE, Component.translatable(RocketLang.TEMPERATURE_VALUE, planet.temperature())));
        info.addChild(row(RocketLang.LABEL_SOLAR, Component.literal(String.valueOf(planet.solarPower()))));
        column.addChild(info);

        boolean here = catalog.current() == body;
        var landing = menu.getLandingPos(dimension, true);
        var land = Button.translatable(LayoutStyle.AUTO, RocketLang.LAND)
                .setVariant(UITheme.ButtonVariant.CONFIRM)
                .disabled(() -> here || !reachable || !decorator.unlocked(body),
                        here ? RocketLang.HERE : !reachable ? RocketLang.ROCKET_TOO_LOW : RocketLang.LOCKED)
                .setOnClientClick(() -> host.land(body));
        land.tooltips(Component.translatable("tooltip.ad_astra.land", catalog.name(body), landing.getX(), landing.getZ()).withStyle(ChatFormatting.AQUA));
        column.addChild(land);
        column.addChild(stations(host, body, orbit));
    }

    private static UIElement stations(RocketMapHost host, int body, ResourceKey<Level> orbit) {
        var menu = host.planets();
        var stations = UIElement.section();
        stations.addChild(TextLine.translatable(LayoutStyle.AUTO, RocketLang.STATIONS).bindClientColor(UITheme::panelText));
        var owned = menu.getOwnedAndTeamSpaceStations(orbit);
        if (owned.isEmpty()) {
            stations.addChild(TextLine.translatable(LayoutStyle.AUTO, RocketLang.NO_STATION).bindClientColor(UITheme::textSecondary));
        }
        for (var entry : owned) {
            var station = entry.getSecond();
            var position = station.position();
            var button = Button.text(LayoutStyle.AUTO, station.name()).setOnClientClick(() -> host.landOnStation(orbit, position));
            button.tooltips(
                    Component.translatable("tooltip.ad_astra.space_station_land", menu.getPlanetName(orbit), position.getMiddleBlockX(),
                            position.getMiddleBlockZ()).withStyle(ChatFormatting.AQUA),
                    Component.translatable("tooltip.ad_astra.space_station_owner", entry.getFirst()).withStyle(ChatFormatting.GOLD));
            stations.addChild(button);
        }
        var construct = Button.translatable(LayoutStyle.AUTO, RocketLang.CONSTRUCT)
                .disabled(() -> !menu.canConstruct(orbit) || menu.isInSpaceStation(orbit), RocketLang.CONSTRUCT_UNAVAILABLE)
                .setOnClientClick(() -> host.construct(body));
        construct.bindTooltips(() -> constructTooltip(menu, orbit));
        stations.addChild(construct);
        return stations;
    }

    static void buildRealm(UIElement column, RocketMapHost host, int realm) {
        column.addChild(new RealmHeader(host.catalog(), realm));
        var info = UIElement.section();
        var dimension = host.catalog().dimensionData(realm);
        if (dimension != null) {
            info.addChild(row(RocketLang.LABEL_TEMPERATURE, Component.translatable(RocketLang.KELVIN_VALUE, Math.round(dimension.getTemperature()))));
            column.addChild(info);
        }
        column.addChild(TextLine.translatable(LayoutStyle.AUTO, RocketLang.NOT_TRAVELABLE).bindClientColor(UITheme::textSecondary));
    }

    private static UIElement row(String label, Component value) {
        return row(label, value, UITheme::panelText);
    }

    private static UIElement row(String label, Component value, IntSupplier color) {
        int width = UIText.width(value) + 2;
        return UIElement.row(TextLine.HEIGHT).layout(l -> l.gapAll(UISizes.GAP))
                .addChildren(TextLine.translatable(LayoutStyle.AUTO, label).bindClientColor(UITheme::textSecondary).layout(l -> l.flex(1)),
                        TextLine.constant(width, value).setTextAlign(Horizontal.RIGHT).bindClientColor(color));
    }

    private static void badge(GuiGraphics graphics, StarSprites.Sprite sprite, int x, int y) {
        StarSprites.draw(graphics, sprite, x + BADGE / 2, y + BADGE / 2, sprite.size() * 2, StarSprites.NO_TINT);
    }

    private static final class Header extends UIElement {

        private final StarCatalog catalog;
        private final RocketDecorator decorator;
        private final int body;
        private final Component subtitle;
        private final Component here = Component.translatable(RocketLang.CURRENT);
        private final Component unlocked = Component.translatable(RocketLang.UNLOCKED);
        private final Component locked = Component.translatable(RocketLang.LOCKED_SHORT);

        Header(RocketMapHost host, int body) {
            this.catalog = host.catalog();
            this.decorator = host.decorator();
            this.body = body;
            var parent = catalog.parentName(body);
            this.subtitle = parent != null ? Component.translatable(RocketLang.SATELLITE_OF, parent) : catalog.systemName(catalog.system(body));
            layout(l -> l.height(BADGE));
        }

        @Override
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPositionX(), y = getPositionY();
            badge(graphics, StarSprites.badge(catalog, body), x, y);
            int textX = x + BADGE + 6;
            graphics.drawString(UIText.font(), subtitle, textX, y + 7, catalog.systemColor(catalog.system(body)), true);
            boolean open = decorator.unlocked(body);
            var status = catalog.current() == body ? here : open ? unlocked : locked;
            int statusY = y + 21;
            UIDraw.lamp(graphics, textX, statusY, 6, open ? UITheme.STATUS_ONLINE : UITheme.STATUS_OFFLINE);
            UIText.drawLeft(graphics, status, textX + 9, statusY - 1, open ? UITheme.STATUS_TEXT_GOOD : UITheme.STATUS_TEXT_ERROR);
        }
    }

    private static final class RealmHeader extends UIElement {

        private final StarCatalog catalog;
        private final int realm;
        private final Component title = Component.translatable(RocketLang.REALMS);
        private final Component status;

        RealmHeader(StarCatalog catalog, int realm) {
            this.catalog = catalog;
            this.realm = realm;
            this.status = Component.translatable(catalog.current() == realm ? RocketLang.CURRENT : RocketLang.NOT_TRAVELABLE);
            layout(l -> l.height(BADGE));
        }

        @Override
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPositionX(), y = getPositionY();
            badge(graphics, StarSprites.badge(catalog, realm), x, y);
            int textX = x + BADGE + 6;
            graphics.drawString(UIText.font(), title, textX, y + 7, UITheme.MAP_REALM, true);
            boolean here = catalog.current() == realm;
            int statusY = y + 21;
            UIDraw.lamp(graphics, textX, statusY, 6, here ? UITheme.STATUS_ONLINE : UITheme.STATUS_OFFLINE);
            UIText.drawLeft(graphics, status, textX + 9, statusY - 1, here ? UITheme.STATUS_TEXT_GOOD : UITheme.PANEL_TEXT);
        }
    }

    private static List<Component> constructTooltip(PlanetsMenu menu, ResourceKey<Level> orbit) {
        var tooltip = new ArrayList<Component>();
        var pos = menu.getLandingPos(orbit, false);
        tooltip.add(Component.translatable("tooltip.ad_astra.construct_space_station_at", menu.getPlanetName(orbit), pos.getX(), pos.getZ())
                .withStyle(ChatFormatting.AQUA));
        if (menu.isInSpaceStation(orbit) || menu.isClaimed(orbit)) {
            tooltip.add(ConstantComponents.SPACE_STATION_ALREADY_EXISTS);
            return tooltip;
        }
        tooltip.add(ConstantComponents.CONSTRUCTION_COST.copy().withStyle(ChatFormatting.AQUA));
        var ingredients = ((IAdDisplayTagName) menu).gtocore$getAdastraDisplayTagNames();
        var needs = ingredients == null ? null : ingredients.get(orbit);
        if (needs == null) return tooltip;
        boolean free = menu.player().isCreative() || menu.player().isSpectator();
        for (var ingredient : needs) {
            boolean enough = free || ingredient.count() >= ingredient.holderCount();
            tooltip.add(Component.translatable("tooltip.ad_astra.requirement", ingredient.count(), ingredient.holderCount(),
                    ingredientName(ingredient.ingredient()).withStyle(ChatFormatting.DARK_AQUA)).withStyle(enough ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        return tooltip;
    }

    private static MutableComponent ingredientName(Ingredient ingredient) {
        var values = ingredient.values;
        if (values.length > 0 && values[0] instanceof Ingredient.TagValue tag) {
            var key = tagTranslationKey(ResourceLocation.parse(tag.serialize().get("tag").getAsString()));
            if (key != null) return Component.translatable(key);
            return Component.translatable("tooltip.ad_astra.unknown_tag", tag.serialize().get("tag"));
        }
        var items = ingredient.getItems();
        if (items.length > 0) return items[0].getHoverName().copy();
        return Component.translatable("tooltip.ad_astra.unknown_ingredient");
    }

    @Nullable
    private static String tagTranslationKey(ResourceLocation id) {
        var key = translatePrefix("tag.item.", id);
        return key != null ? key : translatePrefix("tag.", id);
    }

    @Nullable
    private static String translatePrefix(String prefix, ResourceLocation id) {
        var key = EmiUtil.translateId(prefix, id);
        if (I18n.exists(key)) return key;
        if (id.getNamespace().equals("forge")) {
            key = EmiUtil.translateId(prefix, EmiPort.id("c", id.getPath()));
            if (I18n.exists(key)) return key;
        }
        return null;
    }
}
