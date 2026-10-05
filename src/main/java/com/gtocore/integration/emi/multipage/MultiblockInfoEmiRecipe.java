package com.gtocore.integration.emi.multipage;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.item.OrderItem;

import com.gtolib.GTOCore;
import com.gtolib.cache.CacheManager;
import com.gtolib.utils.FileUtils;
import com.gtolib.utils.ItemUtils;
import com.gtolib.utils.iostream.IOStreamDecoder;
import com.gtolib.utils.iostream.IOStreamEncoder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.integration.emi.multipage.MultiblockEmiActions;
import com.gregtechceu.gtceu.integration.emi.multipage.StructurePreviewTrigger;
import com.gregtechceu.gtceu.integration.emi.recipe.EmiPageLayout;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uiwidgets.structure.StructurePlans;
import com.gregtechceu.gtceu.uiwidgets.structure.StructurePreviewWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.fml.loading.FMLLoader;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.emi.ModularForegroundRenderWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.screen.RecipeScreen;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class MultiblockInfoEmiRecipe extends ModularEmiRecipe<Widget> implements EmiPageLayout.Paged, EmiPageLayout.NoSideButtons {

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(GTCEu.id("multiblock_info"), EmiStack.of(GTMultiMachines.ELECTRIC_BLAST_FURNACE.asItem())) {

        @Override
        public Component getName() {
            return Component.translatable("gtceu.jei.multiblock_info");
        }
    };

    private static final Widget STRUCTURE = new Widget(0, 0, StructurePreviewWidget.WIDTH, StructurePreviewWidget.HEIGHT);
    private static final GTRecipeWidget.PageFrame COMPACT_FRAME = new GTRecipeWidget.PageFrame(StructurePreviewWidget.WIDTH, StructurePreviewWidget.HEIGHT, 0, false);

    private static volatile List<MultiblockInfoEmiRecipe> pendingInputs = Collections.emptyList();

    public final MultiblockMachineDefinition definition;
    private volatile boolean inputsCollected;
    private int pagedWidth = -1;
    private GTRecipeWidget.PageFrame frame = COMPACT_FRAME;

    public MultiblockInfoEmiRecipe(MultiblockMachineDefinition definition) {
        super(() -> STRUCTURE);
        this.definition = definition;
        widget = () -> createWidget(frame);
    }

    private Widget createWidget(GTRecipeWidget.PageFrame frame) {
        int width = frame.minWidth(), height = frame.fillHeight();
        var structure = definition.displayStructure();
        if (structure == null) return new Widget(0, 0, width, height);
        var preview = new StructurePreviewWidget(definition, structure, width, height, () -> openFull(structure),
                MultiblockEmiActions.of(this, definition));
        return frame.card() ? new Card(frame, preview) : preview;
    }

    private void openFull(Structure structure) {
        if (Minecraft.getInstance().screen instanceof RecipeScreen recipes && recipes.old != null &&
                EmiRecipeFiller.performFill(this, recipes.old, EmiCraftContext.Type.FILL_BUTTON, EmiCraftContext.Destination.NONE, 1)) {
            return;
        }
        StructurePreviewTrigger.open(definition, structure);
    }

    @Override
    public int getPagedWidth() {
        int width = Math.max(StructurePreviewWidget.WIDTH, EmiPageLayout.minPageWidth());
        pagedWidth = width + (width & 1);
        return pagedWidth;
    }

    @Override
    public int getPagedHeight() {
        int area = EmiPageLayout.recipeAreaHeight(CATEGORY);
        return area > 0 ? area : StructurePreviewWidget.HEIGHT;
    }

    @Override
    public int getDisplayWidth() {
        return StructurePreviewWidget.WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return StructurePreviewWidget.HEIGHT;
    }

    public static void prepareInputsOnTagsUpdate(List<MultiblockInfoEmiRecipe> recipes) {
        if (recipes.isEmpty()) return;
        pendingInputs = List.copyOf(recipes);
        MinecraftForge.EVENT_BUS.addListener(MultiblockInfoEmiRecipe::onTagsUpdated);
    }

    private static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) return;
        var recipes = pendingInputs;
        if (recipes.isEmpty()) return;
        pendingInputs = Collections.emptyList();
        var thread = new Thread(() -> {
            long time = System.currentTimeMillis();
            for (var recipe : recipes) {
                try {
                    recipe.getInputs();
                } catch (Throwable e) {
                    GTOCore.LOGGER.warn("Failed to prepare multiblock EMI inputs for {}", recipe.definition.getId(), e);
                }
            }
            GTOCore.LOGGER.info("Prepared {} multiblock EMI inputs in {}ms", recipes.size(), System.currentTimeMillis() - time);
        }, "GTO Multiblock EMI Inputs");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        if (!inputsCollected) {
            synchronized (this) {
                if (!inputsCollected) {
                    collectInputs();
                    inputsCollected = true;
                }
            }
        }
        return inputs;
    }

    private void collectInputs() {
        var file = CacheManager.getCacheFile("multiblock/" + definition.getName() + "_inputs");
        if (FMLLoader.isProduction() && file.exists()) {
            try {
                var cached = FileUtils.loadFromFile(file, CachedInputs.DECODER);
                addInputs(cached.groups(), cached.parts());
                return;
            } catch (RuntimeException e) {
                GTOCore.LOGGER.warn("Failed to load multiblock inputs cache {}", file, e);
            }
        }
        var cached = computeInputs();
        if (FMLLoader.isProduction()) {
            try {
                FileUtils.saveToFile(cached, file, CachedInputs.ENCODER);
            } catch (RuntimeException e) {
                GTOCore.LOGGER.warn("Failed to save multiblock inputs cache {}", file, e);
            }
        }
        addInputs(cached.groups(), cached.parts());
    }

    private CachedInputs computeInputs() {
        Set<Set<Item>> groups = new ObjectOpenHashSet<>();
        var structure = definition.displayStructure();
        if (structure != null) {
            for (var predicate : structure.predicates()) {
                addGroups(predicate.common, groups);
                addGroups(predicate.limited, groups);
            }
        }
        var layout = structure == null ? null : structure.layout(structure.defaultValues());
        if (layout == null) return new CachedInputs(groups, Collections.emptyList());
        var controller = definition.asItem();
        var parts = new ArrayList<ItemStack>();
        for (var stack : StructurePlans.preview(definition, layout).parts()) {
            if (!stack.isEmpty()) parts.add(stack);
        }
        parts.sort(Comparator.<ItemStack>comparingInt(stack -> stack.getItem() == controller ? 0 : hasBlockEntity(stack.getItem()) ? 1 : 2)
                .thenComparingInt(stack -> -stack.getCount()));
        return new CachedInputs(groups, parts);
    }

    private void addInputs(Collection<? extends Collection<? extends Item>> groups, List<ItemStack> parts) {
        for (var group : groups) inputs.add(EmiIngredient.of(group.stream().map(EmiStack::of).toList(), 1));
        for (var stack : parts) inputs.add(EmiStack.of(stack));
    }

    private record CachedInputs(Collection<? extends Collection<? extends Item>> groups, List<ItemStack> parts) {

        private static final IOStreamEncoder<Collection<? extends Collection<? extends Item>>> GROUPS_ENCODER = IOStreamEncoder.collection(IOStreamEncoder.collection(ItemUtils.IO_CODEC));
        private static final IOStreamEncoder<Collection<? extends ItemStack>> PARTS_ENCODER = IOStreamEncoder.collection(ItemUtils.STACK_IO_CODEC);
        private static final IOStreamDecoder<List<List<Item>>> GROUPS_DECODER = IOStreamDecoder.list(IOStreamDecoder.list(ItemUtils.IO_CODEC));
        private static final IOStreamDecoder<List<ItemStack>> PARTS_DECODER = IOStreamDecoder.list(ItemUtils.STACK_IO_CODEC);

        static final IOStreamEncoder<CachedInputs> ENCODER = (stream, inputs) -> {
            GROUPS_ENCODER.encode(stream, inputs.groups);
            PARTS_ENCODER.encode(stream, inputs.parts);
        };
        static final IOStreamDecoder<CachedInputs> DECODER = stream -> new CachedInputs(GROUPS_DECODER.decode(stream), PARTS_DECODER.decode(stream));
    }

    private static void addGroups(List<SimplePredicate> predicates, Set<Set<Item>> groups) {
        for (var simplePredicate : predicates) {
            if (simplePredicate == null || simplePredicate.candidates == null) continue;
            Set<Item> items = new ReferenceOpenHashSet<>();
            for (var itemStack : simplePredicate.getCandidates()) {
                var item = itemStack.getItem();
                if (item == Items.AIR || item == Items.BARRIER) continue;
                items.add(item);
            }
            if (items.size() > 1) groups.add(items);
        }
    }

    private static boolean hasBlockEntity(Item item) {
        return item instanceof BlockItem blockItem && blockItem.getBlock().defaultBlockState().hasBlockEntity();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return Collections.singletonList(EmiStack.of(OrderItem.setTarget(GTOItems.ORDER.asStack(), definition.asStack())));
    }

    @Override
    public List<Widget> getFlatWidgetCollection(Widget widgetIn) {
        return Collections.emptyList();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return definition.getId();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (pagedWidth > 0 && EmiPageLayout.claimPagedGroup(widgets, pagedWidth)) {
            frame = new GTRecipeWidget.PageFrame(pagedWidth, widgets.getHeight(), 0, true);
        } else {
            frame = COMPACT_FRAME;
        }
        var widget = this.widget.get();
        var modular = new ModularWrapper<>(widget);
        modular.setRecipeWidget(0, 0);

        synchronized (CACHE_OPENED) {
            CACHE_OPENED.add(modular);
        }
        widgets.add(new CustomModularEmiRecipe(modular, Collections.emptyList()));
        widgets.add(new ModularForegroundRenderWidget(modular));
        widgets.add(new SlotWidget(EmiStack.of(OrderItem.setTarget(GTOItems.ORDER.asStack(), definition.asStack())), 1000, 1000).recipeContext(this));
        MultiblockEmiActions.blockFavoriteKey(widgets);
    }

    @Override
    public void addTempWidgets(WidgetHolder widgets) {
        frame = COMPACT_FRAME;
        if (TEMP_CACHE != null) {
            TEMP_CACHE.modularUI.triggerCloseListeners();
            TEMP_CACHE = null;
        }

        var widget = this.widget.get();
        var modular = new ModularWrapper<>(widget);
        modular.setRecipeWidget(0, 0);
        widgets.add(new CustomModularEmiRecipe(modular, Collections.emptyList()));
        TEMP_CACHE = modular;
    }

    private static final class Card extends UIElement implements ILocalUI {

        private final GTRecipeWidget.PageFrame frame;

        private Card(GTRecipeWidget.PageFrame frame, UIElement content) {
            this.frame = frame;
            layout(l -> l.column().size(frame.minWidth(), frame.fillHeight()));
            addChild(content);
        }

        @Override
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            GTRecipeWidget.drawPageCard(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), frame);
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }
    }
}
