package com.gtocore.integration.emi.research;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.TeamResearchSavedData;
import com.gtocore.api.research.scanning.DataScanningManager;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.client.renderer.RenderUtil;
import com.gtocore.common.data.GTOItems;
import com.gtocore.integration.emi.EmiPageLayout;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.recipe.ui.RecipeSlotLayouts;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.recipe.RecipeSpecPanel;
import com.gregtechceu.gtceu.uiwidgets.recipe.SlotAmountGrid;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEKey;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.emi.ModularForegroundRenderWidget;
import com.lowdragmc.lowdraglib.emi.ModularWrapperWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.vfyjxf.taffy.style.AlignContent;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.V;
import static com.gregtechceu.gtceu.api.GTValues.VN;
import static com.gtocore.integration.emi.research.EmiResearchHelper.*;

public final class DataScanningEmiRecipe implements EmiRecipe, EmiPageLayout.Paged {

    private static final String AMPERAGE = "gtceu.recipe.info.amperage";
    private static final String EU_TIER = "gtceu.recipe.eu.tier";

    private static final int BASE_WIDTH = 102;
    private static final int HEIGHT = 72;
    private static final int EUREKA_X = 103;
    private static final int EUREKA_Y = 17;
    private static final int EUREKA_WIDTH = 68;
    private static final int EUREKA_HEIGHT = 42;
    private static final int EUREKA_SLOT_X = EUREKA_X + 38;
    private static final int EUREKA_SLOT_Y = EUREKA_Y + 12;
    private static final int CENTER_SLOT_X = 19;
    private static final int CENTER_SLOT_Y = 27;
    private static final int BAR_WIDTH = 18;
    private static final int BAR_HEIGHT = 6;
    private static final int BAR_INNER_WIDTH = 16;
    private static final int[] BAR_X = { 19, 41, 63, 84, 84, 63, 41, 19 };
    private static final int[] BAR_Y = { 0, 0, 1, 5, 63, 65, 66, 66 };

    private static final long SCAN_CYCLE = 2400;
    private static final long SCAN_SWEEP = 1200;
    private static final long BAR_FLASH = 600;
    private static final int BEAM_COLOR = 0x33E0FF;
    private static final int BEAM_TRAIL = 5;
    private static final int BEAM_TRAIL_ALPHA = 0x70;

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            GTOCore.id("data_scanning"), EmiStack.of(GTOItems.DATA_CRYSTAL_MK1.asItem())) {

        @Override
        public Component getName() {
            return Component.translatable(CATEGORY_NAME);
        }
    };

    private final AEKey key;
    private final EmiStack input;
    private final List<EmiStack> researchOutputs;
    private final @Nullable TechNode eurekaNode;
    private final @Nullable TechNodeEmiStack eurekaOutput;
    private final List<EmiStack> outputs;
    private final Size[] pagedSizes = new Size[6];
    private @Nullable Size compactSize;
    private int pagedButtons = -1;

    private DataScanningEmiRecipe(AEKey key, EmiStack input, List<EmiStack> researchOutputs, @Nullable TechNode eurekaNode) {
        this.key = key;
        this.input = input;
        this.researchOutputs = researchOutputs;
        this.eurekaNode = eurekaNode;
        this.eurekaOutput = eurekaNode == null ? null : new TechNodeEmiStack(eurekaNode);
        if (eurekaOutput == null) {
            this.outputs = researchOutputs;
        } else {
            List<EmiStack> combinedOutputs = new ArrayList<>(researchOutputs.size() + 1);
            combinedOutputs.addAll(researchOutputs);
            combinedOutputs.add(eurekaOutput);
            this.outputs = List.copyOf(combinedOutputs);
        }
    }

    public static void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(GTOItems.DATA_CRYSTAL_MK1.asItem()));
        registry.addWorkstation(CATEGORY, EmiStack.of(GTOItems.DATA_CRYSTAL_MK2.asItem()));
        registry.addWorkstation(CATEGORY, EmiStack.of(GTOItems.DATA_CRYSTAL_MK3.asItem()));
        registry.addWorkstation(CATEGORY, EmiStack.of(GTOItems.DATA_CRYSTAL_MK4.asItem()));
        registry.addWorkstation(CATEGORY, EmiStack.of(GTOItems.DATA_CRYSTAL_MK5.asItem()));
        for (var scanner : GTMachines.SCANNER) {
            if (scanner != null) registry.addWorkstation(CATEGORY, EmiStack.of(scanner.asItem()));
        }
        registry.addDeferredRecipes(DataScanningEmiRecipe::registerRecipes);
    }

    private static void registerRecipes(Consumer<EmiRecipe> recipeConsumer) {
        List<DataScanningManager.DataScanningEntry> scanningEntries = DataScanningManager.getDataScanningEntries();
        Reference2ObjectMap<AEKey, ResearchPoints> pointsByKey = new Reference2ObjectOpenCustomHashMap<>(ResearchRequirements.AE_KEY_STRATEGY);
        for (var entry : scanningEntries) {
            pointsByKey.put(entry.key(), entry.points());
        }

        Set<AEKey> eurekaKeys = new ObjectOpenCustomHashSet<>(ResearchRequirements.AE_KEY_STRATEGY);
        for (var entry : ResearchRequirements.getEurekaRequirementEntries()) {
            eurekaKeys.add(entry.key());
            var input = EmiResearchHelper.toEmiStack(entry.key());
            if (input == null) {
                continue;
            }
            List<EmiStack> researchOutputs = createResearchOutputs(pointsByKey.get(entry.key()));
            for (var node : entry.nodes()) {
                recipeConsumer.accept(new DataScanningEmiRecipe(entry.key(), input, researchOutputs, node));
            }
        }

        for (var entry : scanningEntries) {
            if (eurekaKeys.contains(entry.key())) {
                continue;
            }
            var input = EmiResearchHelper.toEmiStack(entry.key());
            if (input == null) {
                continue;
            }
            List<EmiStack> researchOutputs = createResearchOutputs(entry.points());
            if (!researchOutputs.isEmpty()) {
                recipeConsumer.accept(new DataScanningEmiRecipe(entry.key(), input, researchOutputs, null));
            }
        }
    }

    private static List<EmiStack> createResearchOutputs(@Nullable ResearchPoints points) {
        if (points == null) {
            return Collections.emptyList();
        }
        return points.reference2LongEntrySet().stream()
                .filter(point -> point.getLongValue() > 0L)
                .sorted(Comparator.comparing(point -> point.getKey().getName()))
                .map(point -> new ResearchTagEmiStack(point.getKey()).setAmount(point.getLongValue()))
                .toList();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        ResourceLocation typeId = key.getType().getId();
        ResourceLocation keyId = key.getId();
        String path = "data_scanning/" + typeId.getNamespace() + "/" + typeId.getPath() + "/" + keyId.getNamespace() + "/" + keyId.getPath();
        if (eurekaNode != null) {
            path += "/eureka/" + eurekaNode.getManager().getId() + "/" + eurekaNode.name;
        }
        return GTOCore.id(path);
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    private GTRecipeWidget.PageFrame pagedFrame(int fillHeight, int buttons) {
        return new GTRecipeWidget.PageFrame(EmiPageLayout.minPageWidth(), fillHeight, buttons, true);
    }

    private Size pagedSize(int buttons) {
        if (buttons >= pagedSizes.length) return measure(pagedFrame(0, buttons));
        var size = pagedSizes[buttons];
        if (size == null) pagedSizes[buttons] = size = measure(pagedFrame(0, buttons));
        return size;
    }

    private Size compactSize() {
        var size = compactSize;
        if (size == null) compactSize = size = measure(GTRecipeWidget.PageFrame.COMPACT);
        return size;
    }

    private Size measure(GTRecipeWidget.PageFrame frame) {
        var page = new Page(frame);
        return new Size(page.getSizeWidth(), page.getSizeHeight());
    }

    private int pagedDisplayWidth(int buttons) {
        return EmiPageLayout.displayWidth(pagedSize(buttons).width, buttons);
    }

    @Override
    public int getPagedWidth() {
        int buttons = EmiPageLayout.sideButtons(this);
        pagedButtons = buttons;
        return pagedDisplayWidth(buttons);
    }

    @Override
    public int getPagedHeight() {
        return pagedSize(Math.max(pagedButtons, 0)).height;
    }

    @Override
    public int getDisplayWidth() {
        return compactSize().width;
    }

    @Override
    public int getDisplayHeight() {
        return compactSize().height;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        GTRecipeWidget.PageFrame frame;
        if (pagedButtons >= 0 && EmiPageLayout.claimPagedGroup(widgets, pagedDisplayWidth(pagedButtons))) {
            frame = pagedFrame(widgets.getHeight(), pagedButtons);
        } else {
            frame = GTRecipeWidget.PageFrame.COMPACT;
        }
        var page = new Page(frame);
        var modular = new ModularWrapper<>(page);
        modular.setRecipeWidget(0, 0);
        synchronized (ModularEmiRecipe.CACHE_OPENED) {
            ModularEmiRecipe.CACHE_OPENED.add(modular);
        }

        var origin = page.art.getPosition();
        List<dev.emi.emi.api.widget.Widget> slots = new ArrayList<>();
        slots.add(new SlotWidget(input, origin.x + CENTER_SLOT_X, origin.y + CENTER_SLOT_Y) {

            @Override
            public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
                super.drawOverlay(draw, mouseX, mouseY, delta);
                if (hasScanned()) {
                    RenderUtil.drawRainbowBorder(draw, x, y, 18, 18, 300, 1F);
                }
            }
        }.drawBack(false).appendTooltip(() -> ClientTooltipComponent.create((hasScanned() ?
                Component.translatable(DOMAIN_DATA_STORAGE_REPEAT, repeatYield()).withStyle(ChatFormatting.RED) :
                Component.translatable(DOMAIN_DATA_STORAGE_NOT_SCANNED).withStyle(ChatFormatting.GREEN)).getVisualOrderText())));
        for (int i = 0; i < Math.min(researchOutputs.size(), BAR_X.length); i++) {
            var output = researchOutputs.get(i);
            int color = ((ResearchTagEmiStack) output).tag.getColor();
            slots.add(new ResearchBarWidget(output, origin.x + BAR_X[i], origin.y + BAR_Y[i], color).recipeContext(this));
        }
        for (int i = 0; i < page.pointSlots.size(); i++) {
            var pos = page.pointSlots.get(i).getPosition();
            slots.add(new SlotWidget(researchOutputs.get(i), pos.x, pos.y) {

                @Override
                public void drawStack(GuiGraphics draw, int mouseX, int mouseY, float delta) {
                    getStack().render(draw, x + 1, y + 1, delta, EmiIngredient.RENDER_ICON);
                }
            }.drawBack(false).recipeContext(this));
        }
        if (eurekaOutput != null) {
            slots.add(new SlotWidget(eurekaOutput, origin.x + EUREKA_SLOT_X, origin.y + EUREKA_SLOT_Y)
                    .appendTooltip(Component.translatable(EUREKA_NAME).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .drawBack(false).recipeContext(this));
        }
        widgets.add(new ModularWrapperWidget(modular, slots));
        slots.forEach(widgets::add);
        widgets.add(new ModularForegroundRenderWidget(modular));
    }

    private boolean hasScanned() {
        var player = Minecraft.getInstance().player;
        return player != null && TeamResearchSavedData.getOrCreateContext(player).hasScanned(key);
    }

    private static String repeatYield() {
        return FormattingUtil.formatNumber2Places(DataScanningManager.getRepeatedScanPenalty() * 100);
    }

    private static long cycleTime() {
        return Util.getMillis() % SCAN_CYCLE;
    }

    private static int logarithmicFill(long amount) {
        if (amount <= 0L) {
            return 0;
        }
        return Math.min(BAR_INNER_WIDTH, Long.SIZE - Long.numberOfLeadingZeros(amount));
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }

    private static final class ResearchBarWidget extends SlotWidget {

        private final int color;

        private ResearchBarWidget(EmiStack stack, int x, int y, int color) {
            super(stack, x, y);
            this.color = color;
        }

        @Override
        public Bounds getBounds() {
            return new Bounds(x, y, BAR_WIDTH, BAR_HEIGHT);
        }

        @Override
        public void drawBackground(GuiGraphics draw, int mouseX, int mouseY, float delta) {
            int fill = logarithmicFill(getStack().getAmount());
            draw.fill(x + 1, y + 1, x + 1 + fill, y + BAR_HEIGHT - 1, color);
            long time = cycleTime() - SCAN_SWEEP;
            if (time >= 0 && time < BAR_FLASH) {
                int alpha = (int) (0xC0 * (BAR_FLASH - time) / BAR_FLASH);
                draw.fill(x + 1, y + 1, x + 1 + fill, y + BAR_HEIGHT - 1, alpha << 24 | 0xFFFFFF);
            }
        }

        @Override
        public void drawStack(GuiGraphics draw, int mouseX, int mouseY, float delta) {}
    }

    private final class Page extends UIElement implements ILocalUI {

        private final GTRecipeWidget.PageFrame frame;
        private final UIElement art;
        private final List<Widget> pointSlots = new ArrayList<>();

        private Page(GTRecipeWidget.PageFrame frame) {
            this.frame = frame;
            int artWidth = eurekaOutput == null ? BASE_WIDTH : EUREKA_X + EUREKA_WIDTH;
            int width = Math.max(artWidth + 2 * RecipeSlotLayouts.PADDING + 2 * UITheme.PANEL_PADDING, frame.minWidth());
            int pageWidth = width + (width & 1);
            layout(l -> l.column().width(pageWidth).minHeight(frame.fillHeight()).gapAll(UISizes.SECTION_GAP));
            setClientSideWidget();

            art = RecipeSlotLayouts.canvas(artWidth, HEIGHT);
            RecipeSlotLayouts.image(art, GTOGuiTextures.PROGRESS_BAR_DATA_GENERATE_BASE, 0, 0, BASE_WIDTH, HEIGHT);
            if (eurekaOutput != null) {
                RecipeSlotLayouts.image(art, GTOGuiTextures.PROGRESS_BAR_EUREKA, EUREKA_X, EUREKA_Y, EUREKA_WIDTH, EUREKA_HEIGHT);
            }
            RecipeSlotLayouts.place(art, new ScanBeam(), CENTER_SLOT_X, CENTER_SLOT_Y);
            var slotArea = new UIElement().layout(l -> l.row().paddingAll(RecipeSlotLayouts.PADDING)).addChild(art);

            var stage = new UIElement().layout(l -> l.column().flexGrow(1).alignCenter().justifyContent(AlignContent.CENTER)
                    .paddingHorizontal(UITheme.PANEL_PADDING));
            stage.setBackground(UITheme.PANEL);
            stage.addChild(slotArea);
            addChild(stage);

            if (!researchOutputs.isEmpty()) {
                var entries = new ArrayList<SlotAmountGrid.Entry>(researchOutputs.size());
                for (var output : researchOutputs) {
                    var hole = new ImageWidget(0, 0, UISizes.SLOT, UISizes.SLOT, UITheme.ITEM_SLOT);
                    pointSlots.add(hole);
                    int color = ((ResearchTagEmiStack) output).tag.getColor();
                    entries.add(new SlotAmountGrid.Entry(hole, Component.literal(FormattingUtil.formatNumbers(output.getAmount())), color));
                }
                addChild(new SlotAmountGrid(pageWidth, entries));
            }

            var left = new UIElement().layout(l -> l.column().flexGrow(1).flexShrink(1).minWidth(0).gapAll(UISizes.SECTION_GAP));
            left.addChild(createPanel());
            var lower = new UIElement().layout(l -> l.row().gapAll(UISizes.SECTION_GAP).minHeight(frame.notchHeight()));
            lower.addChild(left);
            if (frame.sideButtons() > 0) lower.addChild(UIElement.spacer(GTRecipeWidget.PageFrame.NOTCH_WIDTH, 0));
            addChild(lower);
        }

        private Widget createPanel() {
            long eut = EmiResearchHelper.getScannerEUt(key);
            int tier = GTUtil.getTierByVoltage(eut);
            var power = Component.literal(FormattingUtil.formatNumbers(eut) + " EU/t");
            var amperage = Component.translatable(EU_TIER, FormattingUtil.formatNumber2Places((float) eut / V[tier]), VN[tier]);
            var scanned = Component.translatable(SCAN_STATE_REPEAT, repeatYield());
            var notScanned = Component.translatable(SCAN_STATE_NEW);
            var panel = new RecipeSpecPanel();
            panel.layout(l -> l.flexGrow(1));
            panel.value(Component.translatable(SCAN_POWER), () -> power);
            panel.value(Component.translatable(AMPERAGE), () -> amperage);
            panel.value(Component.translatable(SCAN_STATE), () -> hasScanned() ? scanned : notScanned);
            if (eurekaNode != null && eurekaOutput != null) {
                panel.divider();
                var eureka = Component.translatable(EUREKA_UNLOCK, eurekaOutput.getName());
                var node = eurekaNode;
                panel.link(() -> eureka, () -> EmiResearchHelper.openTechNode(node));
            }
            return panel;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            GTRecipeWidget.drawPageCard(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), frame);
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    private static final class ScanBeam extends UIElement {

        private ScanBeam() {
            layout(l -> l.size(UISizes.SLOT, UISizes.SLOT));
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            long time = cycleTime();
            if (time >= SCAN_SWEEP) return;
            int x = getPositionX() + 1, width = UISizes.SLOT - 2;
            int top = getPositionY() + 1, span = UISizes.SLOT - 2;
            int beam = top + (int) (span * time / SCAN_SWEEP);
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(0, 0, UITheme.OVERLAY_Z);
            for (int i = 1; i <= BEAM_TRAIL && beam - i >= top; i++) {
                int alpha = BEAM_TRAIL_ALPHA * (BEAM_TRAIL + 1 - i) / (BEAM_TRAIL + 1);
                graphics.fill(x, beam - i, x + width, beam - i + 1, alpha << 24 | BEAM_COLOR);
            }
            graphics.fill(x - 1, beam, x + width + 1, beam + 1, 0xE0000000 | BEAM_COLOR);
            pose.popPose();
        }
    }
}
