package com.gtocore.integration.emi.research;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.api.research.scanning.DataScanningManager;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.client.renderer.RenderUtil;
import com.gtocore.common.data.GTOItems;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.api.stacks.AEKey;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.gtocore.integration.emi.research.EmiResearchHelper.*;

public final class DataScanningEmiRecipe implements EmiRecipe {

    private static final ResourceLocation BACKGROUND = GTOCore.id("textures/gui/progress_bar/progress_bar_data_generate_base.png");
    private static final ResourceLocation EUREKA_BACKGROUND = GTOCore.id("textures/gui/progress_bar/progress_bar_eureka_what.png");
    private static final int BASE_WIDTH = 102;
    private static final int EUREKA_DISPLAY_WIDTH = 172;
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
            return List.of();
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

    @Override
    public int getDisplayWidth() {
        return eurekaOutput == null ? BASE_WIDTH : EUREKA_DISPLAY_WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BACKGROUND, 0, 0, BASE_WIDTH, HEIGHT, 0, 0, BASE_WIDTH, HEIGHT, BASE_WIDTH, HEIGHT);
        widgets.add(new SlotWidget(input, CENTER_SLOT_X, CENTER_SLOT_Y) {

            @Override
            public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
                super.drawOverlay(draw, mouseX, mouseY, delta);
                if (TeamResearchSavedDtat.getOrCreateContext(Minecraft.getInstance().player).hasScanned(key)) {
                    RenderUtil.drawRainbowBorder(draw, x, y, 18, 18, 300, 1F);
                }
            }
        }).drawBack(false).appendTooltip(
                () -> ClientTooltipComponent.create((TeamResearchSavedDtat.getOrCreateContext(Minecraft.getInstance().player).hasScanned(key) ?
                        Component.translatable(DOMAIN_DATA_STORAGE_REPEAT, FormattingUtil.formatNumber2Places(DataScanningManager.getRepeatedScanPenalty() * 100)).withStyle(ChatFormatting.RED) :
                        Component.translatable(DOMAIN_DATA_STORAGE_NOT_SCANNED).withStyle(ChatFormatting.GREEN)).getVisualOrderText()));
        for (int i = 0; i < Math.min(researchOutputs.size(), BAR_X.length); i++) {
            EmiStack output = researchOutputs.get(i);
            int color = ((ResearchTagEmiStack) output).tag.getColor();
            widgets.add(new ResearchBarWidget(output, BAR_X[i], BAR_Y[i], color)).recipeContext(this);
        }
        if (eurekaOutput != null) {
            widgets.addTexture(EUREKA_BACKGROUND, EUREKA_X, EUREKA_Y, EUREKA_WIDTH, EUREKA_HEIGHT, 0, 0, EUREKA_WIDTH, EUREKA_HEIGHT, EUREKA_WIDTH, EUREKA_HEIGHT);
            widgets.addSlot(eurekaOutput, EUREKA_SLOT_X, EUREKA_SLOT_Y).appendTooltip(Component.translatable(EUREKA_NAME).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .drawBack(false).recipeContext(this);
        }
    }

    private static int logarithmicFill(long amount) {
        if (amount <= 0L) {
            return 0;
        }
        return Math.min(BAR_INNER_WIDTH, Long.SIZE - Long.numberOfLeadingZeros(amount));
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
        }

        @Override
        public void drawStack(GuiGraphics draw, int mouseX, int mouseY, float delta) {}
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }
}
