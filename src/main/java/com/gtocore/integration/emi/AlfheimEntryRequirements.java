package com.gtocore.integration.emi;

import com.gtocore.integration.mythic_botany.AlfheimHelper;

import com.gtolib.GTOCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.MythicBotanyItems;
import vazkii.botania.common.alfheim.AlfheimAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 亚尔夫海姆准入条件：饮下诗之蜜酒并集齐十二遗物。
 *
 * <p>
 * 槽位底色表示该条件的完成情况：绿色为全部条件已满足，蓝色为这一项已满足，黄色为尚未满足。
 * 状态在绘制与悬停时按玩家当前情况读取，不随页面缓存。
 * </p>
 */
public class AlfheimEntryRequirements implements EmiRecipe {

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(GTOCore.id("alfheim_entry_requirements"),
            EmiStack.of(MythicBotanyItems.MB_KVASIR_MEAD.get()));

    private static final int NO_MET = 0;
    private static final int HALF_MET = 1;
    private static final int FULL_MET = 2;

    private static final int SLOT_COLOR = 0xFF8B8B8B;
    private static final int FULL_MET_COLOR = 0xFF00FF00;
    private static final int HALF_MET_COLOR = 0xFF0000FF;
    private static final int NO_MET_COLOR = 0xFFFFFF00;

    private static final int TEXT_TOP = 54;

    private static final EmiStack MEAD = EmiStack.of(MythicBotanyItems.MB_KVASIR_MEAD.get());
    private static final List<EmiIngredient> INPUTS;

    static {
        var relics = AlfheimAccess.REQUIRED_ITEMS;
        var inputs = new ArrayList<EmiIngredient>(1 + relics.size());
        inputs.add(MEAD);
        for (Item relic : relics) {
            inputs.add(EmiStack.of(relic));
        }
        INPUTS = List.copyOf(inputs);
    }

    @Nullable
    private List<FormattedCharSequence> text;
    private Set<Item> obtained = Set.of();
    @Nullable
    private Player obtainedPlayer;
    private int obtainedTick = Integer.MIN_VALUE;

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return GTOCore.id("alfheim_entry_requirements");
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return INPUTS;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 160;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.add(new RequirementSlot(MEAD, 8, 18,
                this::meadStatus,
                () -> Component.translatable("gtocore.entry_alfheim.1.c"),
                () -> Component.translatable("gtocore.entry_alfheim.1")).recipeContext(this));

        var relics = AlfheimAccess.REQUIRED_ITEMS;
        for (int i = 0; i < relics.size(); i++) {
            Item relic = relics.get(i);
            widgets.add(new RequirementSlot(EmiStack.of(relic), 36 + 18 * (i % 6), 9 + 18 * (i / 6),
                    () -> relicStatus(relic),
                    () -> Component.translatable("gtocore.entry_alfheim.2.c"),
                    () -> Component.translatable("gtocore.entry_alfheim.2")).recipeContext(this));
        }

        var font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight + 2;
        int lineCount = Math.max(1, (widgets.getHeight() - TEXT_TOP - 4) / lineHeight);
        var lines = text();
        var pages = new PageManager(lines, lineCount);
        if (lineCount < lines.size()) {
            widgets.addButton(2, 2, 12, 12, 0, 0, () -> true, (mouseX, mouseY, button) -> pages.scroll(-1));
            widgets.addButton(widgets.getWidth() - 14, 2, 12, 12, 12, 0, () -> true, (mouseX, mouseY, button) -> pages.scroll(1));
        }
        widgets.addDrawable(0, TEXT_TOP, 0, 0, (gui, mouseX, mouseY, delta) -> {
            int start = pages.start();
            for (int i = 0; i < lineCount; i++) {
                int index = start + i;
                if (index >= lines.size()) {
                    return;
                }
                gui.drawString(font, lines.get(index), 4, i * lineHeight, 0xFF000000);
            }
        });
    }

    private int meadStatus() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return NO_MET;
        }
        return metStatus(AlfheimHelper.clientCanPlayerUsePortal(player), AlfheimHelper.clientPlayerHasKnowledge(player));
    }

    private int relicStatus(Item relic) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return NO_MET;
        }
        return metStatus(AlfheimHelper.clientCanPlayerUsePortal(player), obtained(player).contains(relic));
    }

    private static int metStatus(boolean fullMet, boolean halfMet) {
        return fullMet ? FULL_MET : halfMet ? HALF_MET : NO_MET;
    }

    /** 十二个槽位每帧都要判定，整理背包每个游戏刻至多一次。 */
    private Set<Item> obtained(Player player) {
        if (player != obtainedPlayer || player.tickCount != obtainedTick) {
            obtainedPlayer = player;
            obtainedTick = player.tickCount;
            obtained = AlfheimHelper.itemObtained(player);
        }
        return obtained;
    }

    private List<FormattedCharSequence> text() {
        if (text == null) {
            text = Minecraft.getInstance().font.split(Component.translatable("gtocore.entry_alfheim.3"), getDisplayWidth() - 4);
        }
        return text;
    }

    private static int blendColor(int from, int to, float delta) {
        int alpha = lerpChannel(from >>> 24, to >>> 24, delta);
        int red = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, delta);
        int green = lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, delta);
        int blue = lerpChannel(from & 0xFF, to & 0xFF, delta);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int lerpChannel(int from, int to, float delta) {
        return from + Math.round((to - from) * delta);
    }

    /** 条件槽位：底色随完成情况脉动，提示按当前状态给出。 */
    private static final class RequirementSlot extends SlotWidget {

        private final IntSupplier status;
        private final Supplier<Component> halfMetTooltip;
        private final Supplier<Component> noMetTooltip;

        private RequirementSlot(EmiIngredient stack, int x, int y, IntSupplier status,
                                Supplier<Component> halfMetTooltip, Supplier<Component> noMetTooltip) {
            super(stack, x, y);
            this.status = status;
            this.halfMetTooltip = halfMetTooltip;
            this.noMetTooltip = noMetTooltip;
        }

        @Override
        public void drawBackground(GuiGraphics gui, int mouseX, int mouseY, float delta) {
            super.drawBackground(gui, mouseX, mouseY, delta);
            float pulse = (float) (Math.sin((System.currentTimeMillis() % 1000L) / 1000F * Math.PI * 2) * 0.1F + 0.9F);
            int color = switch (status.getAsInt()) {
                case FULL_MET -> blendColor(FULL_MET_COLOR, SLOT_COLOR, pulse);
                case HALF_MET -> blendColor(HALF_MET_COLOR, SLOT_COLOR, pulse);
                default -> blendColor(NO_MET_COLOR, SLOT_COLOR, pulse);
            };
            Bounds bounds = getBounds();
            gui.fill(bounds.x() + 1, bounds.y() + 1, bounds.x() + 17, bounds.y() + 17, color);
        }

        @Override
        public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
            List<ClientTooltipComponent> tooltip = new ArrayList<>(super.getTooltip(mouseX, mouseY));
            tooltip.add(ClientTooltipComponent.create(statusComponent().getVisualOrderText()));
            return tooltip;
        }

        private Component statusComponent() {
            return switch (status.getAsInt()) {
                case FULL_MET -> Component.translatable("gtocore.entry_alfheim.0.c");
                case HALF_MET -> halfMetTooltip.get();
                default -> noMetTooltip.get();
            };
        }
    }

    private static final class PageManager {

        private final List<FormattedCharSequence> lines;
        private final int pageSize;
        private int currentPage;

        private PageManager(List<FormattedCharSequence> lines, int pageSize) {
            this.lines = lines;
            this.pageSize = pageSize;
        }

        private void scroll(int delta) {
            int totalPages = (lines.size() - 1) / pageSize + 1;
            currentPage += delta;
            if (currentPage < 0) {
                currentPage = totalPages - 1;
            }
            if (currentPage >= totalPages) {
                currentPage = 0;
            }
        }

        private int start() {
            return currentPage * pageSize;
        }
    }
}
