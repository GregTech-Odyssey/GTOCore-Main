package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.elements.Button;
import com.gtocore.api.gui.ui.elements.StatusLine;
import com.gtocore.api.gui.ui.elements.StatusPanel;
import com.gtocore.api.gui.ui.styletemplate.UISizes;
import com.gtocore.api.gui.ui.styletemplate.UITheme;
import com.gtocore.api.gui.ui.window.MachineWindow;
import com.gtocore.api.gui.ui.window.Popup;
import com.gtocore.config.GTOConfig;

import com.gtolib.GTOCore;
import com.gtolib.api.ae2.MyPatternDetailsHelper;
import com.gtolib.api.ae2.pattern.IParallelPatternDetails;
import com.gtolib.api.ae2.stacks.TagPrefixKey;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.recipe.RecipeType;
import com.gtolib.api.recipe.lookup.IIngredientConvertible;
import com.gtolib.utils.GTOUtils;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.*;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.hooks.IUnique;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.gto.recipesearch.IntLongMap;
import com.hepdd.gtmthings.common.item.VirtualFluidProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.data.CustomItems;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@DataGeneratorScanned
public class MEWildcardPatternBufferPartMachine extends MEPatternBufferPartMachine {

    private List<IPatternDetails> cachedPatterns;
    private boolean dirty = true;
    private boolean lock = false;
    private int scannedPatterns = 0;
    @Getter
    @SaveToDisk(defaultValue = "0")
    private int patternPriority = 0;
    @Getter
    @SaveToDisk(defaultValue = "1")
    private int maxFluidsOutput = 1;
    @Getter
    @SaveToDisk(defaultValue = "1")
    private int maxItemsOutput = 1;
    @SaveToDisk
    private final CustomItemStackHandler blacklistedItems;
    @SaveToDisk
    private final ItemStackTransfer blacklistedItemsStorageTransfer;
    @SaveToDisk
    private final CustomFluidTank[] blacklistedFluids;
    @SaveToDisk
    private final CustomItemStackHandler blacklistedAltProcessableMachines;
    @SaveToDisk
    private final ItemStackTransfer blacklistedAltProcessableMachinesStorageTransfer;
    private final Int2ReferenceOpenHashMap<Material> blacklistedMaterials = new Int2ReferenceOpenHashMap<>();
    private final ReferenceOpenHashSet<Material> blacklistedMaterialSet = new ReferenceOpenHashSet<>();
    private final IntSet blacklistedAltProcessableItemIds = new IntOpenHashSet();
    private final IntSet blacklistedAltProcessableFluidIds = new IntOpenHashSet();
    private final SearchRecipeHandlerUnit searchHolder = new SearchRecipeHandlerUnit();
    private final RecipeHandlerUnit sharedSearchHandlers;

    public MEWildcardPatternBufferPartMachine(@NotNull MetaMachineBlockEntity holder) {
        super(holder, 1);

        blacklistedItems = new CustomItemStackHandler(18);
        blacklistedItemsStorageTransfer = new ItemStackTransfer(36);
        blacklistedFluids = new CustomFluidTank[18];
        blacklistedAltProcessableMachines = new CustomItemStackHandler(6);
        blacklistedAltProcessableMachinesStorageTransfer = new ItemStackTransfer(6);
        Arrays.setAll(blacklistedFluids, i -> new CustomFluidTank(1));

        shareInventory.addChangedListener(this::requestPatternUpdate);
        circuitInventorySimulated.addChangedListener(this::requestPatternUpdate);
        shareTank.addChangedListener(this::requestPatternUpdate);
        getInternalInventory()[0].shareTank.addChangedListener(this::requestPatternUpdate);
        Runnable requestPatternUpdateIfUnlocked = () -> {
            if (!getInternalInventory()[0].isLock()) {
                requestPatternUpdate();
            }
        };
        getInternalInventory()[0].circuitInventory.addChangedListener(requestPatternUpdateIfUnlocked);
        getInternalInventory()[0].shareInventory.addChangedListener(requestPatternUpdateIfUnlocked);
        getInternalInventory()[0].setShouldLockRecipe(false);
        var slot = getInternalInventory()[0];
        sharedSearchHandlers = RecipeHandlerUnit.of(IO.IN,
                slot.circuitInventory, slot.shareInventory, slot.shareTank,
                circuitInventorySimulated, shareInventory, shareTank);
    }

    @Override
    public boolean patternFilter(@NotNull ItemStack stack) {
        var f = stack.getItem() instanceof ProcessingPatternItem;
        if (!f) return false;
        return checkDuplicatedPattern(this, stack);
    }

    @Override
    public @Nullable IPatternDetails decodePattern(@NotNull ItemStack stack, int index) {
        var pattern = MyPatternDetailsHelper.decodePattern(stack, holder, getGrid());
        if (pattern == null) return null;
        return IParallelPatternDetails.of(pattern, getLevel(), 1);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        loadBlacklistData();
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        requestPatternUpdate();
    }

    @Override
    public void onDetailsPostInit() {
        requestPatternUpdate();
    }

    @Override
    public void onPatternChange(int index) {
        super.onPatternChange(index);
        requestPatternUpdate();
    }

    private void requestPatternUpdate() {
        if (lock) return;
        lock = true;
        this.dirty = true;
        ICraftingProvider.requestUpdate(getMainNode());
        lock = false;
    }

    @Override
    public boolean pushPattern(@NotNull IPatternDetails patternDetails, KeyCounter @NotNull [] inputHolder) {
        try {
            lock = true;
            return getInternalInventory()[0].pushPattern(patternDetails, inputHolder);
        } finally {
            lock = false;
        }
    }

    @Override
    public void clearMachineRecipeCache() {}

    @Override
    public void clearPatternRecipeCache() {}

    // 设置项改动都要 onChanged() 标记存盘，否则区块不脏、改动在重启后丢失
    public void setPatternPriority(int priority) {
        if (patternPriority == priority) return;
        patternPriority = priority;
        onChanged();
    }

    private void setMaxFluidsOutput(int integer) {
        final int last = this.maxFluidsOutput;
        maxFluidsOutput = Math.max(0, integer);
        if (last != this.maxFluidsOutput) {
            onChanged();
            requestPatternUpdate();
        }
    }

    private void setMaxItemsOutput(int integer) {
        final int last = this.maxItemsOutput;
        maxItemsOutput = Math.max(0, integer);
        if (last != this.maxItemsOutput) {
            onChanged();
            requestPatternUpdate();
        }
    }

    private void loadBlacklistData() {
        blacklistedMaterials.clear();
        blacklistedMaterialSet.clear();
        blacklistedAltProcessableItemIds.clear();
        blacklistedAltProcessableFluidIds.clear();
        int i = 0;
        for (; i < blacklistedItems.getSlots(); i++) {
            var stack = blacklistedItems.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            var mat = ChemicalHelper.getMaterialStack(stack).material();
            if (mat != GTMaterials.NULL) {
                blacklistedMaterials.put(i, mat);
                blacklistedMaterialSet.add(mat);
            }
        }
        for (; i < blacklistedItems.getSlots() + blacklistedFluids.length; i++) {
            var tank = blacklistedFluids[i - blacklistedItems.getSlots()];
            if (tank.isEmpty()) continue;
            var mat = ChemicalHelper.getMaterial(tank.getFluid().getFluid());
            if (mat != GTMaterials.NULL) {
                blacklistedMaterials.put(i, mat);
                blacklistedMaterialSet.add(mat);
            }
        }
        for (var entry : blacklistedAltProcessableMachines.stacks) {
            if (!(entry.getItem() instanceof MetaMachineItem item)) continue;
            var definition = item.getDefinition();
            var recipeTypes = definition.getRecipeTypes();
            if (recipeTypes == null) continue;
            for (var rt : recipeTypes) {
                var recipeType = (RecipeType) rt;
                blacklistedAltProcessableItemIds.addAll(recipeType.itemsCanBeProduced);
                blacklistedAltProcessableFluidIds.addAll(recipeType.fluidsCanBeProduced);
            }
        }
        requestPatternUpdate();
    }

    /**
     * 开销大约为1.5ms~2ms每次调用
     */
    private void rebuildCacheIfNeeded(List<IPatternDetails> patterns) {
        if (dirty || cachedPatterns == null) {
            dirty = false;
            // profiler start
            long nanos = System.nanoTime();
            AtomicLong substitutingIngredients = new AtomicLong();
            AtomicLong validatingPatterns = new AtomicLong();

            // var patterns = super.getAvailablePatterns();
            var newPatterns = new ArrayList<IPatternDetails>();
            var templates = new WildcardPatternTemplate[patterns.size()];
            var searchContext = new SearchContext();

            long startSubstituting = System.nanoTime();
            for (int i = 0; i < patterns.size(); i++) {
                var p = patterns.get(i);
                if (p instanceof AEProcessingPattern processingPattern) {
                    templates[i] = new WildcardPatternTemplate(processingPattern);
                }
            }
            substitutingIngredients.addAndGet(System.nanoTime() - startSubstituting);

            GTCEuAPI.materialManager.getRegisteredMaterials().forEach(material -> {
                if (blacklistedMaterialSet.contains(material)) return;
                for (int i = 0; i < patterns.size(); i++) {
                    var template = templates[i];
                    if (template == null) continue;

                    long startSubstituting1 = System.nanoTime();
                    var inputMap = template.tryBuildSearchMap(material, searchContext);
                    var output = template.tryResolveOutputs(material);
                    substitutingIngredients.addAndGet(System.nanoTime() - startSubstituting1);
                    if (inputMap == null || output == null) continue;

                    long startValidating1 = System.nanoTime();
                    var detail = validatePattern(inputMap, output);
                    if (detail != null) {
                        var converted = IParallelPatternDetails.of(convertPattern(detail, 0), getLevel(), 1);
                        newPatterns.add(converted);
                    }
                    validatingPatterns.addAndGet(System.nanoTime() - startValidating1);
                }
            });
            cachedPatterns = newPatterns;
            scannedPatterns = cachedPatterns.size();
            if (GTOConfig.INSTANCE.devMode.aeLog) {
                GTOCore.LOGGER.info("MEWildcardPatternBufferPartMachine recalculated patterns: {} patterns in {} ms",
                        scannedPatterns, (System.nanoTime() - nanos) / 1_000_000.0);
                GTOCore.LOGGER.info("  substituting ingredients took {} ms ({})%",
                        substitutingIngredients.get() / 1_000_000.0, substitutingIngredients.get() * 100.0 / (System.nanoTime() - nanos));
                GTOCore.LOGGER.info("  validating patterns took {} ms ({})%",
                        validatingPatterns.get() / 1_000_000.0, validatingPatterns.get() * 100.0 / (System.nanoTime() - nanos));
            }
            // profiler end
        }
    }

    @Override
    public @NotNull List<@NotNull IPatternDetails> getAvailablePatterns() {
        var patterns = super.getAvailablePatterns();
        if (patterns.isEmpty()) {
            patterns = readInv();
            if (patterns.isEmpty()) {
                scannedPatterns = 0;
                return patterns;
            }
        }
        rebuildCacheIfNeeded(patterns);
        return cachedPatterns;
    }

    @Override
    public void loadFromItem(@NotNull CompoundTag tag) {
        super.loadFromItem(tag);
        patternPriority = tag.getInt("patternPriority");
        maxFluidsOutput = tag.getInt("maxFluidsOutput");
        maxItemsOutput = tag.getInt("maxItemsOutput");
        blacklistedItemsStorageTransfer.deserializeNBT(tag.getCompound("blacklistedItems"));
        var fluidsTag = tag.getList("blacklistedFluids", 10);
        for (int i = 0; i < fluidsTag.size(); i++) {
            blacklistedFluids[i].deserializeNBT(fluidsTag.getCompound(i));
        }
        blacklistedAltProcessableMachinesStorageTransfer.deserializeNBT(tag.getCompound("blacklistedAltProcessableMachines"));
        loadBlacklistData();
    }

    @Override
    public void saveToItem(@NotNull CompoundTag tag) {
        super.saveToItem(tag);
        tag.putInt("patternPriority", patternPriority);
        tag.putInt("maxFluidsOutput", maxFluidsOutput);
        tag.putInt("maxItemsOutput", maxItemsOutput);
        tag.put("blacklistedItems", blacklistedItemsStorageTransfer.serializeNBT());
        var fluidsTag = new ListTag();
        for (var tank : blacklistedFluids) {
            fluidsTag.add(tank.serializeNBT());
        }
        tag.put("blacklistedFluids", fluidsTag);
        tag.put("blacklistedAltProcessableMachines", blacklistedAltProcessableMachinesStorageTransfer.serializeNBT());
    }

    private @NotNull List<@NotNull IPatternDetails> readInv() {
        var pattern = getPatternInventory().getStackInSlot(0);
        var details = decodePattern(pattern, 0);
        return details == null ? Collections.emptyList() : List.of(details);
    }

    public static void onMultiblockRecipeTypeChange(@NotNull MultiblockControllerMachine machine) {
        Arrays.stream(machine.getParts())
                .filter(MEWildcardPatternBufferPartMachine.class::isInstance)
                .map(MEWildcardPatternBufferPartMachine.class::cast)
                .forEach(MEWildcardPatternBufferPartMachine::requestPatternUpdate);
    }

    // ========== Pattern Validation ==========

    private AEProcessingPattern validatePattern(IntLongMap inputMap, GenericStack[] sparseOutput) {
        ObjHolder<GTRecipeDefinition> valid = new ObjHolder<>();
        if (recipeType == null) {
            if (!getRecipeTypes().isEmpty()) {
                for (var rt : getRecipeTypes()) {
                    if (searchRecipe(rt, inputMap, (u, r) -> {
                        if (checkProb(r)) {
                            valid.value = r;
                            recipeType = r.recipeType;
                            return true;
                        }
                        return false;
                    })) break;
                }
            }
        } else {
            searchRecipe(recipeType, inputMap, (u, r) -> {
                if (checkProb(r)) {
                    valid.value = r;
                    return true;
                }
                return false;
            });
        }
        var outPattern = MyPatternDetailsHelper.convertFromGTRecipe(valid.value, maxItemsOutput, maxFluidsOutput);
        // outPattern的output 需要包含 sparseOutput的所有东西
        if (outPattern != null) {
            var outSparse = outPattern.getSparseOutputs();
            for (var reqStack : sparseOutput) {
                boolean found = false;
                for (var outStack : outSparse) {
                    if (reqStack.what() == outStack.what() && reqStack.amount() <= outStack.amount()) {
                        found = true;
                    }
                    switch (outStack.what()) {
                        case AEItemKey itemKey -> {
                            int id = ((IUnique) itemKey.getItem()).ae2$getUid();
                            if (blacklistedAltProcessableItemIds.contains(id)) {
                                return null;
                            }
                        }
                        case AEFluidKey fluidKey -> {
                            int id = ((IUnique) fluidKey.getFluid()).ae2$getUid();
                            if (blacklistedAltProcessableFluidIds.contains(id)) {
                                return null;
                            }
                        }
                        default -> {}
                    }
                }
                if (!found) {
                    return null;
                }
            }
            return outPattern;
        }
        return null;
    }

    private boolean checkProb(GTRecipeDefinition recipe) {
        for (var ingredient : recipe.itemInputs) {
            if (ingredient.chance != 10000 && ingredient.chance != 0) return false;
        }
        for (var ingredient : recipe.fluidInputs) {
            if (ingredient.chance != 10000 && ingredient.chance != 0) return false;
        }
        return true;
    }

    private boolean searchRecipe(GTRecipeType type, IntLongMap inputMap, java.util.function.BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle) {
        searchHolder.use(inputMap);
        try {
            return searchHolder.findRecipe(type, canHandle);
        } finally {
            searchHolder.clear();
        }
    }

    private static void addSearchKey(IntLongMap map, AEKey key) {
        var normalized = normalizeSearchKey(key);
        if (normalized != null) {
            ((IIngredientConvertible) normalized).gtolib$convert(Integer.MAX_VALUE, map);
        }
    }

    private static @Nullable Object normalizeSearchKey(AEKey key) {
        if (key instanceof AEItemKey what && MEPatternVirtualInputHelper.isVirtualProvider(what)) {
            if (what.getItem() == CustomItems.VIRTUAL_ITEM_PROVIDER.get()) {
                ItemStack virtualItem = VirtualItemProviderBehavior.getVirtualItem(what.getReadOnlyStack());
                return virtualItem.isEmpty() ? null : AEItemKey.of(virtualItem);
            } else if (what.getItem() == CustomItems.VIRTUAL_FLUID_PROVIDER.get()) {
                FluidStack virtualFluid = VirtualFluidProviderBehavior.getVirtualFluid(what.getReadOnlyStack());
                return virtualFluid.isEmpty() ? null : AEFluidKey.of(virtualFluid);
            }
        }
        return key;
    }

    private static final class SearchRecipeHandlerUnit extends RecipeHandlerUnit {

        private IntLongMap inputMap = IntLongMap.EMPTY;

        private SearchRecipeHandlerUnit() {
            super(IO.IN, null);
        }

        private void use(IntLongMap inputMap) {
            this.inputMap = inputMap;
        }

        private void clear() {
            this.inputMap = IntLongMap.EMPTY;
        }

        @Override
        public IntLongMap getSearchMap(@NotNull GTRecipeType type) {
            return inputMap;
        }
    }

    private final class SearchContext {

        private final Reference2ObjectOpenHashMap<GTRecipeType, IntLongMap> sharedInputCache = new Reference2ObjectOpenHashMap<>();
        private final IntLongMap workingInput = new IntLongMap();

        private IntLongMap prepareWorkingInput(GTRecipeType type, IntLongMap wildcardInput) {
            workingInput.clear();
            var cachedInput = sharedInputCache.get(type);
            if (cachedInput == null) {
                var cached = new IntLongMap();
                sharedSearchHandlers.getSearchMap(type).addTo(cached);
                sharedInputCache.put(type, cached);
                cachedInput = cached;
            }
            cachedInput.addTo(workingInput);
            wildcardInput.addTo(workingInput);
            return workingInput;
        }
    }

    private record TagPrefixRequirement(TagPrefixKey key, long amount) {

    }

    private final class WildcardPatternTemplate {

        private final GenericStack[] fixedInputKeys;
        private final TagPrefixRequirement[] tagInputs;
        private final GenericStack[] resolvedOutputs;
        private final TagPrefixRequirement[] tagOutputs;
        private final int fixedOutputCount;
        private final Reference2ObjectOpenHashMap<GTRecipeType, IntLongMap> fixedInputCache = new Reference2ObjectOpenHashMap<>();

        private WildcardPatternTemplate(AEProcessingPattern pattern) {
            var fixedInputs = new ArrayList<GenericStack>();
            var inputTags = new ArrayList<TagPrefixRequirement>();
            for (var stack : pattern.getSparseInputs()) {
                if (stack.what() instanceof TagPrefixKey tagPrefixKey) {
                    inputTags.add(new TagPrefixRequirement(tagPrefixKey, stack.amount()));
                } else {
                    fixedInputs.add(stack);
                }
            }
            fixedInputKeys = fixedInputs.toArray(GenericStack[]::new);
            tagInputs = inputTags.toArray(TagPrefixRequirement[]::new);

            var fixedOutputs = new ArrayList<GenericStack>();
            var outputTags = new ArrayList<TagPrefixRequirement>();
            for (var stack : pattern.getSparseOutputs()) {
                if (stack.what() instanceof TagPrefixKey tagPrefixKey) {
                    outputTags.add(new TagPrefixRequirement(tagPrefixKey, stack.amount()));
                } else {
                    fixedOutputs.add(stack);
                }
            }
            tagOutputs = outputTags.toArray(TagPrefixRequirement[]::new);
            fixedOutputCount = fixedOutputs.size();
            resolvedOutputs = new GenericStack[fixedOutputCount + tagOutputs.length];
            for (int i = 0; i < fixedOutputCount; i++) {
                resolvedOutputs[i] = fixedOutputs.get(i);
            }
        }

        private @Nullable IntLongMap tryBuildSearchMap(Material material, SearchContext context) {
            var activeType = getCurrentSearchType();
            if (activeType == null) {
                return null;
            }
            var wildcardInput = fixedInputCache.computeIfAbsent(activeType, this::buildFixedInputMap);
            var working = context.prepareWorkingInput(activeType, wildcardInput);
            for (var requirement : tagInputs) {
                var what = requirement.key.getFromMaterial(material);
                if (what == null) {
                    return null;
                }
                addSearchKey(working, what);
            }
            return working;
        }

        private @Nullable GenericStack[] tryResolveOutputs(Material material) {
            for (int i = 0; i < tagOutputs.length; i++) {
                var requirement = tagOutputs[i];
                var what = requirement.key.getFromMaterial(material);
                if (what == null) {
                    return null;
                }
                resolvedOutputs[fixedOutputCount + i] = new GenericStack(what, requirement.amount);
            }
            return resolvedOutputs;
        }

        private IntLongMap buildFixedInputMap(GTRecipeType type) {
            var map = new IntLongMap();
            for (var stack : fixedInputKeys) {
                addSearchKey(map, stack.what());
            }
            return map;
        }
    }

    private @Nullable GTRecipeType getCurrentSearchType() {
        if (recipeType != null) {
            return recipeType;
        }
        var recipeTypes = getRecipeTypes();
        return recipeTypes.isEmpty() ? null : recipeTypes.getFirst();
    }

    // ==================== 界面 ====================

    /// 黑名单弹出面板的键
    private static final String BLACKLIST_POPUP = "wildcard_blacklist";
    /// 界面上开放的黑名单槽数：物品、流体各两行，机器一行
    private static final int BLACKLIST_ITEM_SLOTS = 18;
    private static final int BLACKLIST_FLUID_SLOTS = 18;
    private static final int BLACKLIST_MACHINE_SLOTS = 6;
    private static final IGuiTexture BLACKLIST_SLOT_BACKGROUND = new GuiTextureGroup(UITheme.ITEM_SLOT, GuiTextures.CONFIG_ARROW_DARK);

    @Override
    protected void buildGridHeader(@NotNull UIElement page) {
        var status = new StatusPanel(page.getContentWidth());
        status.addLine(LANG_WILDCARD_PATTERN_BUFFER_LOADED_PATTERNS, () -> Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_LOADED_PATTERNS_VALUE, scannedPatterns))
                .level(() -> scannedPatterns > 0 ? StatusLine.Level.GOOD : StatusLine.Level.NORMAL);
        page.addChild(status);
    }

    @Override
    protected int gridHeaderHeight() {
        return StatusPanel.heightFor(1) + UISizes.GAP;
    }

    /** 网格下方：三项数值设置 + 打开材料黑名单弹出面板的按钮。 */
    @Override
    protected void buildUI(@NotNull UIElement root) {
        super.buildUI(root);
        int width = root.getContentWidth();
        var settings = UIElement.section(width);
        root.addChild(settings);
        int inner = settings.getContentWidth();
        settings.addChildren(
                MEPatternPartUI.labeledRow(inner, LANG_WILDCARD_PATTERN_BUFFER_PRIORITY,
                        MEPatternPartUI.intField(UISizes.BUTTON_WIDTH, this::getPatternPriority, this::setPatternPriority, Integer.MIN_VALUE),
                        LANG_WILDCARD_PATTERN_BUFFER_PRIORITY_DESC),
                MEPatternPartUI.labeledRow(inner, LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES,
                        MEPatternPartUI.intField(UISizes.BUTTON_WIDTH, this::getMaxFluidsOutput, this::setMaxFluidsOutput, 0),
                        LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES_DESC, LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES_EXAMPLE),
                MEPatternPartUI.labeledRow(inner, LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES,
                        MEPatternPartUI.intField(UISizes.BUTTON_WIDTH, this::getMaxItemsOutput, this::setMaxItemsOutput, 0),
                        LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES_DESC, LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES_EXAMPLE),
                MEPatternPartUI.labeledRow(inner, LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST,
                        Button.translatable(UISizes.BUTTON_WIDTH, LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_OPEN)
                                .setOnClientClick(() -> {
                                    var window = MachineWindow.of(root);
                                    if (window != null) window.togglePopup(BLACKLIST_POPUP, 0);
                                }),
                        LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_DESC));
    }

    @Override
    protected void registerPopups(@NotNull MachineWindow window) {
        super.registerPopups(window);
        window.registerPopup(BLACKLIST_POPUP, ignored -> Popup.of(() -> Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST), this::buildBlacklist));
    }

    /** 材料黑名单弹出面板：物品、流体、机器三个区块。 */
    private void buildBlacklist(UIElement column) {
        MEPatternPartUI.slotRows(MEPatternPartUI.section(column, LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_ITEMS), BLACKLIST_ITEM_SLOTS, this::createItemBlacklistSlot);
        MEPatternPartUI.slotRows(MEPatternPartUI.section(column, LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_FLUIDS), BLACKLIST_FLUID_SLOTS, this::createFluidBlacklistSlot);
        MEPatternPartUI.slotRows(MEPatternPartUI.section(column, LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_MACHINES), BLACKLIST_MACHINE_SLOTS, this::createMachineBlacklistSlot);
    }

    private Widget createItemBlacklistSlot(int finalIndex) {
        return new PhantomSlotWidget(blacklistedItemsStorageTransfer, finalIndex, 0, 0) {

            @Override
            public ItemStack slotClickPhantom(Slot slot, int mouseButton, ClickType clickTypeIn, ItemStack stackHeld) {
                ItemStack stack = ItemStack.EMPTY;
                ItemStack stackSlot = slot.getItem();
                if (!stackSlot.isEmpty()) {
                    stack = stackSlot.copy();
                }

                Material materialSlot = ChemicalHelper.getMaterialStack(stackSlot).material();
                Material materialHeld = ChemicalHelper.getMaterialStack(stackHeld).material();

                if (materialHeld == GTMaterials.NULL || mouseButton == 2 || mouseButton == 1) {
                    // held is empty,right click,middle click
                    // -> clear slot
                    fillPhantomSlot(slot, ItemStack.EMPTY);
                    blacklistedItems.setStackInSlot(finalIndex, ItemStack.EMPTY);
                    loadBlacklistData();
                } else if (materialSlot == GTMaterials.NULL) {   // slot is empty
                    if (!blacklistedMaterials.containsValue(materialHeld)) {
                        // held is not empty and item not in other slot
                        // -> add to slot
                        fillPhantomSlot(slot, stackHeld);
                        var itemStack = stackHeld.copy();
                        blacklistedItems.setStackInSlot(finalIndex, itemStack);
                        loadBlacklistData();
                    }
                } else {
                    if (materialSlot != materialHeld) {
                        // slot item not equal to held item
                        if (!blacklistedMaterials.containsValue(materialHeld)) {
                            // item not in other slot
                            // -> change the slot
                            fillPhantomSlot(slot, stackHeld);
                            var itemStack = stackHeld.copy();
                            blacklistedItems.setStackInSlot(finalIndex, itemStack);
                            loadBlacklistData();
                        }
                    }
                }
                return stack;
            }

            @Override
            public List<Component> getFullTooltipTexts() {
                var superText = super.getFullTooltipTexts();
                if (this.slotReference != null) {
                    var mat = ChemicalHelper.getMaterialStack(this.slotReference.getItem()).material();
                    if (mat != GTMaterials.NULL) {
                        superText.addFirst(Component.translatable("metaitem.tool.tooltip.primary_material", mat.getLocalizedName()));
                    }
                }
                return superText;
            }
        }.setClearSlotOnRightClick(false).setChangeListener(this::onChanged).setBackgroundTexture(BLACKLIST_SLOT_BACKGROUND);
    }

    private Widget createFluidBlacklistSlot(int fluidIndex) {
        int shift = blacklistedItems.getSlots();
        return new PhantomFluidWidget(
                this.blacklistedFluids[fluidIndex], fluidIndex,
                0, 0, UISizes.SLOT, UISizes.SLOT,
                () -> this.blacklistedFluids[fluidIndex].getFluid(),
                (fluid -> {
                    int shiftedIndex = fluidIndex + shift;
                    if (fluid.isEmpty()) {
                        this.blacklistedFluids[fluidIndex].setFluid(fluid);
                        if (!blacklistedMaterials.isEmpty() && blacklistedMaterials.containsKey(shiftedIndex)) {
                            blacklistedMaterials.remove(shiftedIndex);
                        }
                        loadBlacklistData();
                        return;
                    }
                    Material fluidMaterial = ChemicalHelper.getMaterial(fluid.getFluid());
                    for (var entry : blacklistedMaterials.int2ReferenceEntrySet()) {
                        int i = entry.getIntKey() - shift;
                        Material f = entry.getValue();
                        if (i != fluidIndex && f == fluidMaterial) {
                            return;
                        } else if (i == fluidIndex && f != fluidMaterial) {
                            setFluid(fluidIndex, fluid);
                            return;
                        }
                    }
                    setFluid(fluidIndex, fluid);
                })) {

            @Override
            public List<Component> getFullTooltipTexts() {
                var superTexts = super.getFullTooltipTexts();
                var mat = ChemicalHelper.getMaterial(getFluid().getFluid());
                if (mat != GTMaterials.NULL) {
                    superTexts.addFirst(Component.translatable("metaitem.tool.tooltip.primary_material", mat.getLocalizedName()));
                }
                return superTexts;
            }
        }.setShowAmount(false).setBackground(UITheme.FLUID_SLOT);
    }

    private Widget createMachineBlacklistSlot(int finalIndex) {
        return new PhantomSlotWidget(blacklistedAltProcessableMachinesStorageTransfer, finalIndex, 0, 0) {

            @Override
            public ItemStack slotClickPhantom(Slot slot, int mouseButton, ClickType clickTypeIn, ItemStack stackHeld) {
                ItemStack stack = ItemStack.EMPTY;
                ItemStack stackSlot = slot.getItem();
                if (!stackSlot.isEmpty()) {
                    stack = stackSlot.copy();
                }
                boolean heldIsMachine = checkIsMachine(stackHeld);

                if (stackHeld.isEmpty() || mouseButton == 2 || mouseButton == 1) {
                    // held is empty,right click,middle click
                    // -> clear slot
                    fillPhantomSlot(slot, ItemStack.EMPTY);
                    blacklistedAltProcessableMachines.setStackInSlot(finalIndex, ItemStack.EMPTY);
                    loadBlacklistData();
                } else if (stackSlot.isEmpty()) {   // slot is empty
                    if (heldIsMachine &&
                            Arrays.stream(blacklistedAltProcessableMachines.stacks).noneMatch(s -> areItemsEqual(s, stackHeld))) {
                        // held is not empty and item not in other slot
                        // -> add to slot
                        fillPhantomSlot(slot, stackHeld);
                        var itemStack = stackHeld.copy();
                        blacklistedAltProcessableMachines.setStackInSlot(finalIndex, itemStack);
                        loadBlacklistData();
                    }
                } else {
                    if (heldIsMachine &&
                            !areItemsEqual(stackSlot, stackHeld)) {
                        // slot item not equal to held item
                        // item not in other slot
                        // -> change the slot
                        fillPhantomSlot(slot, stackHeld);
                        var itemStack = stackHeld.copy();
                        blacklistedAltProcessableMachines.setStackInSlot(finalIndex, itemStack);
                        loadBlacklistData();
                    }
                }
                return stack;
            }

            @Override
            public boolean areItemsEqual(ItemStack itemStack1, ItemStack itemStack2) {
                return itemStack1.getItem() == itemStack2.getItem(); // no nbt comparison
            }

            @Override
            public List<Component> getFullTooltipTexts() {
                var superText = super.getFullTooltipTexts();
                if (this.slotReference != null) {
                    var item = this.slotReference.getItem();
                    if (item.getItem() instanceof MetaMachineItem metaMachineItem) {
                        var definition = metaMachineItem.getDefinition();
                        var recipeTypes = definition.getRecipeTypes();
                        if (recipeTypes != null && recipeTypes.length > 0) {
                            var finalComp = Arrays.stream(definition.getRecipeTypes())
                                    .filter(Objects::nonNull)
                                    .map(r -> Component.translatable("gtceu." + r.registryName.getPath()))
                                    .collect(GTOUtils.joiningComponent(ComponentUtils.DEFAULT_SEPARATOR));
                            superText.addFirst(Component.translatable("gtocore.lang.template.recipes_type.-1712405057", finalComp));
                        }
                    }
                }
                return superText;
            }

            @Override
            public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
                if (this.slotReference != null && this.drawHoverTips && this.isMouseOverElement(mouseX, mouseY) && this.getHoverElement(mouseX, mouseY) == this) {
                    ItemStack stack = this.slotReference.getItem();
                    if (stack.isEmpty() && this.gui != null) {
                        this.gui.getModularUIGui().setHoverTooltip(List.of(Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS).withStyle(ChatFormatting.WHITE),
                                Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_1).withStyle(ChatFormatting.GRAY),
                                Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_2).withStyle(ChatFormatting.GRAY),
                                Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_3).withStyle(ChatFormatting.GRAY)),
                                stack, null, stack.getTooltipImage().orElse(null));
                    }
                }
            }
        }.setClearSlotOnRightClick(false).setChangeListener(this::onChanged).setBackgroundTexture(BLACKLIST_SLOT_BACKGROUND);
    }

    private void setFluid(int index, FluidStack fs) {
        var newFluid = fs.copy();
        newFluid.setAmount(1);
        this.blacklistedFluids[index].setFluid(newFluid);
        loadBlacklistData();
    }

    private static boolean checkIsMachine(ItemStack stack) {
        return stack.getItem() instanceof MetaMachineItem;
    }

    private static void fillPhantomSlot(Slot slot, ItemStack stackHeld) {
        if (stackHeld.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            ItemStack phantomStack = stackHeld.copy();
            phantomStack.setCount(1);
            slot.set(phantomStack);
        }
    }

    // ========== Localization ==========

    @RegisterLanguage(cn = "样板优先级：", en = "Pattern Priority: ")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_PRIORITY = "gtocore.ae.appeng.pattern.priority";
    @RegisterLanguage(cn = "此样板总成提供的样板优先级。合成计算将优先考虑优先级最高的样板。", en = "The priority for the patterns offered by this provider. The crafting calculation will prioritize patterns with the highest priority.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_PRIORITY_DESC = "gtocore.ae.appeng.pattern.priority.desc";
    @RegisterLanguage(cn = "通配符样板总成材料黑名单", en = "Wildcard Pattern Provider Material Blacklist")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST = "gtocore.ae.appeng.wildcard_pattern_buffer.blacklist";
    @RegisterLanguage(cn = "添加到黑名单中的材料将不会被通配符样板总成所使用。", en = "Materials added to the blacklist will not be used by the Wildcard Pattern Provider.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_DESC = "gtocore.ae.appeng.wildcard_pattern_buffer.blacklist.desc";
    @RegisterLanguage(cn = "编辑", en = "Edit")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_OPEN = "gtocore.ae.appeng.wildcard_pattern_buffer.blacklist.open";
    @RegisterLanguage(cn = "物品黑名单（按材料）", en = "Item Blacklist (by material)")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_ITEMS = "gtocore.ae.appeng.wildcard_pattern_buffer.blacklist.items";
    @RegisterLanguage(cn = "流体黑名单（按材料）", en = "Fluid Blacklist (by material)")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_FLUIDS = "gtocore.ae.appeng.wildcard_pattern_buffer.blacklist.fluids";
    @RegisterLanguage(cn = "机器黑名单", en = "Machine Blacklist")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_MACHINES = "gtocore.ae.appeng.wildcard_pattern_buffer.blacklist.machines";
    @RegisterLanguage(cn = "最大物品输出种数：", en = "Max Item Output Types: ")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES = "gtocore.ae.appeng.wildcard_pattern_buffer.max_item_output_types";
    @RegisterLanguage(cn = "自动生成的样板中产物允许的最大物品种类数。", en = "The maximum number of item types allowed in the outputs of auto-generated patterns.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES_DESC = "gtocore.ae.appeng.wildcard_pattern_buffer.max_item_output_types.desc";
    @RegisterLanguage(cn = "例如，生成的样板中，若配方输出既含有物品，又含有流体，将此项设为0，则仅允许流体作为样板的产物。", en = "For example, in generated patterns, if the recipe outputs both items and fluids, setting this to 0 will only allow fluids as outputs of the pattern.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_ITEM_OUTPUT_TYPES_EXAMPLE = "gtocore.ae.appeng.wildcard_pattern_buffer.max_item_output_types.example";
    @RegisterLanguage(cn = "最大流体输出种数：", en = "Max Fluid Output Types: ")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES = "gtocore.ae.appeng.wildcard_pattern_buffer.max_fluid_output_types";
    @RegisterLanguage(cn = "自动生成的样板中产物允许的最大流体种类数。", en = "The maximum number of fluid types allowed in the outputs of auto-generated patterns.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES_DESC = "gtocore.ae.appeng.wildcard_pattern_buffer.max_fluid_output_types.desc";
    @RegisterLanguage(cn = "例如，生成的样板中，若配方输出含有多种流体，将此项设为1，则仅允许配方中的第一种流体作为样板的产物。", en = "For example, in generated patterns, if the recipe outputs multiple fluids, setting this to 1 will only allow the first fluid in the recipe as the output of the pattern.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MAX_FLUID_OUTPUT_TYPES_EXAMPLE = "gtocore.ae.appeng.wildcard_pattern_buffer.max_fluid_output_types.example";
    @RegisterLanguage(cn = "已加载通配符样板", en = "Loaded wildcard patterns")
    static final String LANG_WILDCARD_PATTERN_BUFFER_LOADED_PATTERNS = "gtocore.ae.appeng.wildcard_pattern_buffer.loaded_patterns";
    @RegisterLanguage(cn = "%s 种", en = "%s")
    static final String LANG_WILDCARD_PATTERN_BUFFER_LOADED_PATTERNS_VALUE = "gtocore.ae.appeng.wildcard_pattern_buffer.loaded_patterns.value";
    @RegisterLanguage(cn = "机器黑名单过滤槽", en = "Machine Blacklist Filter Slots")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS = "gtocore.ae.appeng.wildcard_pattern_buffer.machine_filter_slots";
    @RegisterLanguage(cn = "在此处放入机器的物品形态以添加机器黑名单过滤槽。", en = "Place the item form of a machine here to add a machine blacklist filter slot.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_1 = "gtocore.ae.appeng.wildcard_pattern_buffer.machine_filter_slots.desc.1";
    @RegisterLanguage(cn = "当生成的样板的产物与黑名单中过滤槽中的机器物品形态对应的机器能够生产时，生成的样板将被禁止使用。", en = "When the output of a generated pattern can be produced by a machine corresponding to the item form in the blacklist filter slot, the generated pattern will be prohibited from being used.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_2 = "gtocore.ae.appeng.wildcard_pattern_buffer.machine_filter_slots.desc.2";
    @RegisterLanguage(cn = "例如，石墨烯箔可以同时使用多辊压机和化学气相沉积系统来生产，如果在机器黑名单过滤槽中放入化学气相沉积机器的物品形态，则多辊轧机中生成箔的配方样板在此总成中，不会生成石墨烯材料相关。",
                      en = "For example, graphene foils can be produced by both the cluster rolling and the CVD system. If the item form of the CVD machine is placed in the machine blacklist filter slot, then recipe patterns for producing foils in the cluster rolling that involve graphene materials will not be generated in this buffer.")
    private static final String LANG_WILDCARD_PATTERN_BUFFER_MACHINE_FILTER_SLOTS_DESC_3 = "gtocore.ae.appeng.wildcard_pattern_buffer.machine_filter_slots.desc.3";
}
