package com.gtocore.client.screen.starmap;

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
import net.minecraft.client.Minecraft;
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

/**
 * 星球详情卡片：环境信息、前往、所在轨道的空间站（前往 / 建造）。动作经 {@link StarMapHost} 发 Ad Astra 的网络包。
 */
@OnlyIn(Dist.CLIENT)
final class StarMapDetails {

    private static final int BADGE = 36;

    private StarMapDetails() {}

    static void build(UIElement column, StarMapHost host, StarMapModel.Body body) {
        var menu = host.planets();
        var planet = body.planet();
        column.addChild(new Header(host, body));

        var info = UIElement.section();
        boolean reachable = host.model().reachable(body);
        info.addChild(row(StarMapLang.LABEL_TIER, Component.translatable(StarMapLang.TIER_VALUE, body.requiredTier()), reachable ? UITheme.PANEL_TEXT : UITheme.STATUS_TEXT_ERROR));
        if (!reachable) {
            info.addChild(row(StarMapLang.LABEL_CURRENT_ROCKET, Component.translatable(StarMapLang.TIER_VALUE, host.model().rocketTier), UITheme.STATUS_TEXT_ERROR));
        }
        info.addChild(row(StarMapLang.LABEL_GRAVITY, Component.translatable(StarMapLang.GRAVITY_VALUE, String.format("%.2f", planet.gravity()))));
        info.addChild(row(StarMapLang.LABEL_ATMOSPHERE, Component.translatable(planet.oxygen() ? StarMapLang.BREATHABLE : StarMapLang.NO_OXYGEN),
                planet.oxygen() ? UITheme.STATUS_TEXT_GOOD : UITheme.PANEL_TEXT));
        info.addChild(row(StarMapLang.LABEL_TEMPERATURE, Component.translatable(StarMapLang.TEMPERATURE_VALUE, planet.temperature())));
        info.addChild(row(StarMapLang.LABEL_SOLAR, Component.literal(String.valueOf(planet.solarPower()))));
        column.addChild(info);

        var landing = menu.getLandingPos(body.dimensionKey(), true);
        var land = Button.translatable(LayoutStyle.AUTO, StarMapLang.LAND)
                .setVariant(UITheme.ButtonVariant.CONFIRM)
                .disabled(() -> host.model().current == body || !reachable || !StarMapScene.unlocked(body),
                        host.model().current == body ? StarMapLang.HERE : !reachable ? StarMapLang.ROCKET_TOO_LOW : StarMapLang.LOCKED)
                .setOnClientClick(() -> host.land(body));
        land.tooltips(Component.translatable("tooltip.ad_astra.land", body.name(), landing.getX(), landing.getZ()).withStyle(ChatFormatting.AQUA));
        column.addChild(land);

        var orbit = body.orbit();
        var stations = UIElement.section();
        stations.addChild(TextLine.translatable(LayoutStyle.AUTO, StarMapLang.STATIONS).bindClientColor(UITheme::panelText));
        var owned = menu.getOwnedAndTeamSpaceStations(orbit);
        if (owned.isEmpty()) {
            stations.addChild(TextLine.translatable(LayoutStyle.AUTO, StarMapLang.NO_STATION).bindClientColor(UITheme::textSecondary));
        }
        for (var entry : owned) {
            var station = entry.getSecond();
            var position = station.position();
            var button = Button.text(LayoutStyle.AUTO, station.name())
                    .setOnClientClick(() -> host.landOnStation(orbit, position));
            button.tooltips(
                    Component.translatable("tooltip.ad_astra.space_station_land", menu.getPlanetName(orbit), position.getMiddleBlockX(),
                            position.getMiddleBlockZ()).withStyle(ChatFormatting.AQUA),
                    Component.translatable("tooltip.ad_astra.space_station_owner", entry.getFirst()).withStyle(ChatFormatting.GOLD));
            stations.addChild(button);
        }
        var construct = Button.translatable(LayoutStyle.AUTO, StarMapLang.CONSTRUCT)
                .disabled(() -> !menu.canConstruct(orbit) || menu.isInSpaceStation(orbit), StarMapLang.CONSTRUCT_UNAVAILABLE)
                .setOnClientClick(() -> host.construct(body));
        construct.bindTooltips(() -> constructTooltip(menu, orbit));
        stations.addChild(construct);
        column.addChild(stations);
    }

    private static UIElement row(String label, Component value) {
        return row(label, value, UITheme.PANEL_TEXT);
    }

    private static UIElement row(String label, Component value, int color) {
        int width = UIText.width(value) + 2;
        return UIElement.row(TextLine.HEIGHT).layout(l -> l.gapAll(UISizes.GAP))
                .addChildren(TextLine.translatable(LayoutStyle.AUTO, label).bindClientColor(UITheme::textSecondary).layout(l -> l.flex(1)),
                        TextLine.constant(width, value).setTextAlign(Horizontal.RIGHT).setColor(color));
    }

    private static final class Header extends UIElement {

        private final StarMapHost host;
        private final StarMapModel.Body body;

        Header(StarMapHost host, StarMapModel.Body body) {
            this.host = host;
            this.body = body;
            layout(l -> l.height(BADGE));
        }

        @Override
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPositionX(), y = getPositionY();
            var sprite = StarMapDraw.planet(StarMapDraw.texture(body.icon(), StarMapModel.fallbackIcon()), StarMapScene.PLANET_TEXELS);
            StarMapDraw.sprite(graphics, sprite, x + BADGE / 2, y + BADGE / 2, sprite.size() * 2, 1);
            var font = Minecraft.getInstance().font;
            int textX = x + BADGE + 6;
            var parent = host.model().parentName(body);
            var subtitle = parent != null ? Component.translatable(StarMapLang.SATELLITE_OF, parent) : body.system().name;
            graphics.drawString(font, subtitle, textX, y + 7, StarMapScene.color(body.system()), true);
            boolean unlocked = StarMapScene.unlocked(body);
            boolean here = host.model().current == body;
            var status = Component.translatable(here ? StarMapLang.CURRENT : unlocked ? StarMapLang.UNLOCKED : StarMapLang.LOCKED_SHORT);
            int statusY = y + 21;
            UIDraw.lamp(graphics, textX, statusY, 6, unlocked ? UITheme.STATUS_ONLINE : UITheme.STATUS_OFFLINE);
            UIText.drawLeft(graphics, status, textX + 9, statusY - 1, unlocked ? UITheme.STATUS_TEXT_GOOD : UITheme.STATUS_TEXT_ERROR);
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
