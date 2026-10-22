package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.config.GTOConfig;

import com.gtolib.GTOCore;
import com.gtolib.api.ae2.MyPatternDetailsHelper;
import com.gtolib.api.ae2.pattern.IParallelPatternDetails;
import com.gtolib.api.ae2.stacks.TagPrefixKey;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.recipe.lookup.MapIngredient;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.ContentList;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.uipro.window.PopupHost;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.*;
import appeng.api.stacks.AEItemKey;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.ProcessingPatternItem;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.gto.recipesearch.IntLongMap;
import com.hepdd.gtmthings.common.item.VirtualFluidProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.data.CustomItems;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@DataGeneratorScanned
public class MEWildcardPatternBufferPartMachine extends MEPatternBufferPartMachine {

    private static final GenericStack[] EMPTY_EDITOR_STACKS = AbstractRecipeInternalSlot.EMPTY_EDITOR_STACKS;

    private List<IPatternDetails> cachedPatterns;
    @Nullable
    private GTRecipeType resolvedRecipeType;
    private boolean dirty = true;
    private boolean lock = false;
    private int scannedPatterns = 0;
    @Getter
    @SaveToDisk(defaultValue = "0")
    private int patternPriority = 0;
    @SaveToDisk
    private final StackInventory blacklistedItems;
    @SaveToDisk
    private final KeyInventory<AEFluidKey> blacklistedFluids;
    @SaveToDisk
    private String[] blacklistedMaterialNames = new String[0];
    private final ReferenceOpenHashSet<Material> blacklistedMaterialSet = new ReferenceOpenHashSet<>();
    private ArrayList<ScannedMaterial> scannedMaterials = new ArrayList<>(0);
    private int materialBlacklistRevision;
    private final RecipeHandlerUnit sharedSearchHandlers;
    // 兼容旧存档中的单槽草稿，加载后迁移到内部槽位。
    @SaveToDisk(defaultValue = "false")
    private boolean editingTemplate;
    @SaveToDisk(skipWhen = "skipLegacyEditorStacks")
    private GenericStack[] editorInputs = EMPTY_EDITOR_STACKS;
    @SaveToDisk(skipWhen = "skipLegacyEditorStacks")
    private GenericStack[] editorOutputs = EMPTY_EDITOR_STACKS;

    public MEWildcardPatternBufferPartMachine(@NotNull MetaMachineBlockEntity holder, @NotNull PatternBufferType type) {
        super(holder, type);

        blacklistedItems = new StackInventory(18);
        blacklistedFluids = KeyInventory.fluids(18, 1);

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
        if (hasIncompleteEditorPattern(index)) return null;
        var pattern = MyPatternDetailsHelper.decodePattern(stack, holder, getGrid());
        if (pattern == null) return null;
        return IParallelPatternDetails.of(pattern, getLevel(), 1);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        migrateEditorTemplate();
        loadBlacklistData();
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        requestPatternUpdate();
    }

    @Override
    public void setAvailableRecipeTypes(@NotNull GTRecipeType[] types) {
        super.setAvailableRecipeTypes(types);
        requestPatternUpdate();
    }

    @Override
    public void setRecipeType(GTRecipeType type) {
        if (recipeType == type) return;
        super.setRecipeType(type);
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

    @Override
    protected void onEditorPatternChanged() {
        getAvailablePatterns();
    }

    /** 旧版把唯一槽位的草稿保存在机器上；迁移到公共的逐槽位存储。 */
    private void migrateEditorTemplate() {
        if (!editingTemplate) return;
        getInternalInventory()[0].setEditorTemplate(editorInputs, editorOutputs);
        editingTemplate = false;
        editorInputs = EMPTY_EDITOR_STACKS;
        editorOutputs = EMPTY_EDITOR_STACKS;
        onChanged();
    }

    private boolean skipLegacyEditorStacks(GenericStack[] stacks) {
        return !editingTemplate;
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

    private void loadBlacklistData() {
        blacklistedMaterialSet.clear();
        for (var name : blacklistedMaterialNames) {
            var material = GTCEuAPI.materialManager.getMaterial(name);
            if (material != null && material != GTMaterials.NULL) blacklistedMaterialSet.add(material);
        }
        // 旧版物品/流体槽只作为迁移来源；新黑名单按材料保存，不再受槽数限制。
        boolean migrated = false;
        for (int i = 0; i < blacklistedItems.getSlots(); i++) {
            var stack = blacklistedItems.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            var mat = ChemicalHelper.getMaterialStack(stack).material();
            if (mat != GTMaterials.NULL) blacklistedMaterialSet.add(mat);
            blacklistedItems.setStackInSlot(i, ItemStack.EMPTY);
            migrated = true;
        }
        for (int i = 0; i < blacklistedFluids.size(); i++) {
            var fluid = blacklistedFluids.keyAt(i);
            if (fluid == null) continue;
            var mat = ChemicalHelper.getMaterial(fluid.getFluid());
            if (mat != GTMaterials.NULL) blacklistedMaterialSet.add(mat);
            blacklistedFluids.set(i, null, 0);
            migrated = true;
        }
        if (migrated) saveMaterialBlacklist();
        materialBlacklistRevision++;
        requestPatternUpdate();
    }

    private void saveMaterialBlacklist() {
        blacklistedMaterialNames = new String[blacklistedMaterialSet.size()];
        int index = 0;
        for (var material : blacklistedMaterialSet) blacklistedMaterialNames[index++] = material.getResourceLocation().toString();
        onChanged();
    }

    ArrayList<ScannedMaterial> getScannedMaterials() {
        if (dirty) getAvailablePatterns();
        return scannedMaterials;
    }

    int getMaterialBlacklistRevision() {
        return materialBlacklistRevision;
    }

    boolean isMaterialDisabled(Material material) {
        return blacklistedMaterialSet.contains(material);
    }

    void toggleMaterial(Material material) {
        for (var entry : getScannedMaterials()) {
            if (entry.material() != material) continue;
            if (!blacklistedMaterialSet.remove(material)) blacklistedMaterialSet.add(material);
            saveMaterialBlacklist();
            materialBlacklistRevision++;
            requestPatternUpdate();
            return;
        }
    }

    /** 只接受当前扫描仍可运行的材料；整批修改只保存并通知一次。 */
    void setMaterialsDisabled(List<Material> materials, boolean disabled) {
        var selected = new ReferenceOpenHashSet<Material>(materials.size());
        selected.addAll(materials);
        boolean changed = false;
        for (var entry : getScannedMaterials()) {
            var material = entry.material();
            if (!selected.contains(material)) continue;
            changed |= disabled ? blacklistedMaterialSet.add(material) : blacklistedMaterialSet.remove(material);
        }
        if (!changed) return;
        saveMaterialBlacklist();
        materialBlacklistRevision++;
        requestPatternUpdate();
    }

    record ScannedMaterial(Material material, AEKey[] outputs, ResourceLocation[] recipes) {}

    /**
     * 开销大约为1.5ms~2ms每次调用
     */
    private void rebuildCacheIfNeeded(List<IPatternDetails> patterns) {
        if (!MapIngredient.isReady()) return;
        if (!dirty && cachedPatterns != null) return;
        dirty = false;
        resolvedRecipeType = null;
        boolean profile = GTOConfig.INSTANCE.devMode.aeLog;
        long nanos = profile ? System.nanoTime() : 0;
        var materials = GTCEuAPI.materialManager.getRegisteredMaterials();
        var newPatterns = new ArrayList<IPatternDetails>(Math.max(scannedPatterns, materials.size()));
        var newMaterials = new ArrayList<ScannedMaterial>(materials.size());
        var templates = new WildcardPatternTemplate[patterns.size()];
        var searchContext = new SearchContext();
        var validRecipe = new ObjHolder<GTRecipeDefinition>();
        for (int i = 0; i < patterns.size(); i++) {
            if (patterns.get(i) instanceof AEProcessingPattern processingPattern) {
                templates[i] = new WildcardPatternTemplate(processingPattern);
            }
        }
        for (var material : materials) {
            AEKeySet<AEKey> outputs = null;
            ArrayList<ResourceLocation> recipes = null;
            for (var template : templates) {
                if (template == null) continue;
                var inputMap = template.tryBuildSearchMap(material, searchContext);
                var output = template.tryResolveOutputs(material);
                if (inputMap == null || output == null) continue;
                var detail = validatePattern(inputMap, output, validRecipe);
                if (detail == null) continue;
                if (outputs == null) {
                    outputs = new AEKeySet<>(detail.getSparseOutputs().length);
                    recipes = new ArrayList<>(patterns.size());
                }
                recipes.add(validRecipe.value.id);
                for (var stack : detail.getSparseOutputs()) outputs.add(stack.what());
                if (!blacklistedMaterialSet.contains(material)) {
                    newPatterns.add(IParallelPatternDetails.of(convertPattern(detail, 0), getLevel(), 1));
                }
            }
            if (outputs != null) newMaterials.add(new ScannedMaterial(material, outputs.toArray(AEKey[]::new), recipes.toArray(ResourceLocation[]::new)));
        }
        if (!sameScannedMaterials(newMaterials)) scannedMaterials = newMaterials;
        cachedPatterns = newPatterns;
        scannedPatterns = newPatterns.size();
        if (profile) {
            GTOCore.LOGGER.info("MEWildcardPatternBufferPartMachine recalculated patterns: {} patterns in {} ms",
                    scannedPatterns, (System.nanoTime() - nanos) / 1_000_000.0);
        }
    }

    private boolean sameScannedMaterials(ArrayList<ScannedMaterial> materials) {
        if (materials.size() != scannedMaterials.size()) return false;
        for (int i = 0; i < materials.size(); i++) {
            var next = materials.get(i);
            var previous = scannedMaterials.get(i);
            if (next.material() != previous.material() || !Arrays.equals(next.outputs(), previous.outputs()) || !Arrays.equals(next.recipes(), previous.recipes())) return false;
        }
        return true;
    }

    @Override
    public @NotNull List<@NotNull IPatternDetails> getAvailablePatterns() {
        if (hasIncompleteEditorPattern(0)) {
            clearScannedPatterns();
            return Collections.emptyList();
        }
        var patterns = super.getAvailablePatterns();
        if (patterns.isEmpty()) {
            patterns = readInv();
            if (patterns.isEmpty()) {
                clearScannedPatterns();
                return patterns;
            }
        }
        rebuildCacheIfNeeded(patterns);
        return cachedPatterns;
    }

    private void clearScannedPatterns() {
        scannedPatterns = 0;
        if (!scannedMaterials.isEmpty()) scannedMaterials = new ArrayList<>(0);
        cachedPatterns = Collections.emptyList();
        dirty = false;
    }

    @Override
    public void loadFromItem(@NotNull CompoundTag tag) {
        super.loadFromItem(tag);
        editingTemplate = tag.getBoolean("editingTemplate");
        editorInputs = editingTemplate ? loadEditorStacks(tag.getList("editorInputs", 10), AEProcessingPattern.MAX_INPUT_SLOTS) : EMPTY_EDITOR_STACKS;
        editorOutputs = editingTemplate ? loadEditorStacks(tag.getList("editorOutputs", 10), AEProcessingPattern.MAX_OUTPUT_SLOTS) : EMPTY_EDITOR_STACKS;
        migrateEditorTemplate();
        patternPriority = tag.getInt("patternPriority");
        var materialNames = tag.getList("blacklistedMaterialNames", 8);
        blacklistedMaterialNames = new String[materialNames.size()];
        for (int i = 0; i < materialNames.size(); i++) blacklistedMaterialNames[i] = materialNames.getString(i);
        loadStacks(blacklistedItems, tag.getCompound("blacklistedItems"));
        var fluidsTag = tag.getList("blacklistedFluids", 10);
        int fluidCount = Math.min(fluidsTag.size(), blacklistedFluids.size());
        blacklistedFluids.clear();
        for (int i = 0; i < fluidCount; i++) {
            var fluid = FluidStack.loadFluidStackFromNBT(fluidsTag.getCompound(i));
            blacklistedFluids.set(i, Keys.fluid(fluid), fluid.getAmount());
        }
        loadBlacklistData();
    }

    @Override
    public void saveToItem(@NotNull CompoundTag tag) {
        super.saveToItem(tag);
        tag.remove("editingTemplate");
        tag.remove("editorInputs");
        tag.remove("editorOutputs");
        tag.putInt("patternPriority", patternPriority);
        var materialNames = new ListTag();
        for (var name : blacklistedMaterialNames) materialNames.add(StringTag.valueOf(name));
        tag.put("blacklistedMaterialNames", materialNames);
        tag.put("blacklistedItems", saveStacks(blacklistedItems));
        var fluidsTag = new ListTag();
        for (int i = 0; i < blacklistedFluids.size(); i++) {
            var fluid = blacklistedFluids.keyAt(i);
            var stack = fluid == null ? FluidStack.EMPTY : Keys.toFluidStack(fluid, blacklistedFluids.amountAt(i));
            fluidsTag.add(stack.writeToNBT(new CompoundTag()));
        }
        tag.put("blacklistedFluids", fluidsTag);
    }

    private static GenericStack[] loadEditorStacks(ListTag tag, int limit) {
        var stacks = new GenericStack[Math.min(tag.size(), limit)];
        for (int i = 0; i < stacks.length; i++) stacks[i] = GenericStack.readTag(tag.getCompound(i));
        return stacks;
    }

    private static CompoundTag saveStacks(StackInventory handler) {
        var stacks = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) stacks.set(i, handler.getStackInSlot(i));
        return ContainerHelper.saveAllItems(new CompoundTag(), stacks);
    }

    private static void loadStacks(StackInventory handler, CompoundTag tag) {
        var stacks = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, stacks);
        for (int i = 0; i < stacks.size(); i++) handler.setStackInSlot(i, stacks.get(i));
    }

    private @NotNull List<@NotNull IPatternDetails> readInv() {
        var pattern = getPatternInventory().getStackInSlot(0);
        var details = decodePattern(pattern, 0);
        return details == null ? Collections.emptyList() : List.of(details);
    }

    public static void onMultiblockRecipeTypeChange(@NotNull MultiblockControllerMachine machine) {
        Arrays.stream(machine.getParts(MEWildcardPatternBufferPartMachine.class))
                .forEach(MEWildcardPatternBufferPartMachine::requestPatternUpdate);
    }

    // ========== Pattern Validation ==========

    private AEProcessingPattern validatePattern(IntLongMap inputMap, GenericStack[] sparseOutput, ObjHolder<GTRecipeDefinition> valid) {
        valid.value = null;
        var searchType = getEffectiveRecipeType();
        if (searchType == null) {
            if (!getRecipeTypes().isEmpty()) {
                for (var rt : getRecipeTypes()) {
                    if (rt.search(inputMap, r -> {
                        if (checkProb(r)) {
                            valid.value = r;
                            resolvedRecipeType = r.recipeType;
                            return true;
                        }
                        return false;
                    })) break;
                }
            }
        } else {
            searchType.search(inputMap, r -> {
                if (checkProb(r)) {
                    valid.value = r;
                    return true;
                }
                return false;
            });
        }
        var recipe = valid.value;
        if (recipe == null) return null;
        // 完整保留产物；超过 AE2 原生样板容量的配方无法编码。
        if (recipe.itemOutputs.size() + recipe.fluidOutputs.size() > AEProcessingPattern.MAX_OUTPUT_SLOTS) return null;
        var outPattern = MyPatternDetailsHelper.convertFromGTRecipe(recipe, recipe.itemOutputs.size(), recipe.fluidOutputs.size());
        // outPattern的output 需要包含 sparseOutput的所有东西
        if (outPattern != null) {
            var outSparse = outPattern.getSparseOutputs();
            for (var reqStack : sparseOutput) {
                boolean found = false;
                for (var outStack : outSparse) {
                    if (reqStack.what() == outStack.what() && reqStack.amount() <= outStack.amount()) {
                        found = true;
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
        var items = recipe.itemInputs;
        for (int i = 0; i < items.size(); i++) {
            int chance = items.chance(i);
            if (chance != ContentList.MAX_CHANCE && chance != 0) return false;
        }
        var fluids = recipe.fluidInputs;
        for (int i = 0; i < fluids.size(); i++) {
            int chance = fluids.chance(i);
            if (chance != ContentList.MAX_CHANCE && chance != 0) return false;
        }
        return true;
    }

    private static void addSearchKey(GTRecipeType type, IntLongMap map, AEKey key) {
        var normalized = normalizeSearchKey(key);
        if (normalized != null) {
            type.convertKey(normalized, Integer.MAX_VALUE, map);
        }
    }

    private static @Nullable AEKey normalizeSearchKey(AEKey key) {
        if (key instanceof AEItemKey what && MEPatternVirtualInputHelper.isVirtualProvider(what)) {
            if (what.getItem() == CustomItems.VIRTUAL_ITEM_PROVIDER.get()) {
                ItemStack virtualItem = VirtualItemProviderBehavior.getVirtualItem(what.getReadOnlyStack());
                return virtualItem.isEmpty() ? null : AEItemKey.of(virtualItem);
            } else if (what.getItem() == CustomItems.VIRTUAL_FLUID_PROVIDER.get()) {
                FluidStack virtualFluid = VirtualFluidProviderBehavior.getVirtualFluid(what.getReadOnlyStack());
                return virtualFluid.isEmpty() ? null : Keys.fluid(virtualFluid);
            }
        }
        return key;
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
        private int fixedInputGeneration;

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
            int generation = GTRecipeType.searchGeneration();
            if (generation != fixedInputGeneration) {
                fixedInputCache.clear();
                fixedInputGeneration = generation;
            }
            var wildcardInput = fixedInputCache.computeIfAbsent(activeType, this::buildFixedInputMap);
            var working = context.prepareWorkingInput(activeType, wildcardInput);
            for (var requirement : tagInputs) {
                var what = requirement.key.getFromMaterial(material);
                if (what == null) {
                    return null;
                }
                addSearchKey(activeType, working, what);
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
                addSearchKey(type, map, stack.what());
            }
            return map;
        }
    }

    @Override
    public @Nullable GTRecipeType getEffectiveRecipeType() {
        return recipeType != null ? recipeType : resolvedRecipeType;
    }

    private @Nullable GTRecipeType getCurrentSearchType() {
        var type = getEffectiveRecipeType();
        if (type != null) {
            return type;
        }
        var recipeTypes = getRecipeTypes();
        return recipeTypes.isEmpty() ? null : recipeTypes.getFirst();
    }

    // ==================== 界面 ====================

    /// 两块独立开关的面板放在同一滚动列，避免屏幕较矮时被 PopupHost 分到两列。
    private static final String TOOLS_POPUP = "wildcard_tools";
    private static final int MATERIALS_PANEL = 1;
    private static final int EDITOR_PANEL = 2;

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(UISizes.WINDOW_WIDTH, UISizes.WINDOW_WIDTH, this, entityPlayer).widget(new WildcardWindow(this));
    }

    /** 仅此机器将主窗口和展开的侧栏作为一个整体居中，沿用外壳的布局动画与面板滚动。 */
    private static final class WildcardWindow extends MachineWindow {

        @Nullable
        private PopupHost sidebar;

        private WildcardWindow(MEWildcardPatternBufferPartMachine machine) {
            super(machine);
        }

        @Override
        public void onContentResized(Widget root) {
            if (root instanceof PopupHost host) sidebar = host;
            super.onContentResized(root);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected void setWindowBasePosition(int x, int y) {
            if (sidebar == null || sidebar.getSizeWidth() == 0 || !isPopupOpen(TOOLS_POPUP)) {
                super.setWindowBasePosition(x, y);
                return;
            }
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int top = (screenHeight - getGui().getHeight()) / 2 + y;
            // 与外壳即将应用的侧栏纵向偏移一致，按包含顶部标签的实际外接矩形居中。
            int sidebarY = Math.max(UISizes.SCREEN_MARGIN - top,
                    Math.min(0, screenHeight - UISizes.SCREEN_MARGIN - top - sidebar.getSizeHeight()));
            int tabsHeight = getSideTabsWidget().getSubTabs().isEmpty() ? 0 : UISizes.PAGE_TAB_HEIGHT + UISizes.PAGE_TAB_RAISE;
            int groupTop = Math.min(-tabsHeight, sidebarY);
            int groupBottom = Math.max(getSizeHeight(), sidebarY + sidebar.getSizeHeight());
            getGui().setSize(getSizeWidth() + UISizes.POPUP_GAP + sidebar.getSizeWidth(), groupBottom - groupTop);
            super.setWindowBasePosition(0, -groupTop);
        }
    }

    @Override
    protected void buildGridHeader(@NotNull UIElement page) {
        var status = new StatusPanel(page.getContentWidth());
        status.addLine(LANG_WILDCARD_PATTERN_BUFFER_LOADED_PATTERNS, () -> Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_LOADED_PATTERNS_VALUE, scannedPatterns))
                .bindLevel(() -> scannedPatterns > 0 ? Level.GOOD : Level.NORMAL);
        page.addChild(status);
    }

    @Override
    protected int gridHeaderHeight() {
        return StatusPanel.heightFor(1) + UISizes.GAP;
    }

    /** 网格下方：样板优先级、材料管理与样板编辑面板入口。 */
    @Override
    protected void buildUI(@NotNull UIElement root) {
        super.buildUI(root);
        int width = root.getContentWidth();
        var settings = UIElement.section(width);
        root.addChild(settings);
        int inner = settings.getContentWidth();
        settings.addChildren(
                Form.numberRow(LANG_WILDCARD_PATTERN_BUFFER_PRIORITY,
                        MEPatternPartUI.intField(0, this::getPatternPriority, this::setPatternPriority, Integer.MIN_VALUE),
                        LANG_WILDCARD_PATTERN_BUFFER_PRIORITY_DESC),
                MEPatternPartUI.labeledRow(inner, LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST,
                        toolPanelButton(root, MATERIALS_PANEL),
                        LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_DESC),
                MEPatternPartUI.labeledRow(inner, PatternEditor.TITLE,
                        toolPanelButton(root, EDITOR_PANEL),
                        PatternEditor.HINT));
    }

    private static Button toolPanelButton(UIElement root, int panel) {
        String label = Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST_OPEN).getString();
        String collapsed = label + " ▶";
        String expanded = label + " ◀";
        return Button.of(UISizes.BUTTON_WIDTH).bindClientText(() -> {
            var window = MachineWindow.of(root);
            return window != null && window.isPopupOpen(TOOLS_POPUP) && (window.getPopupArgument(TOOLS_POPUP) & panel) != 0 ? expanded : collapsed;
        }).setOnClientClick(() -> {
            var window = MachineWindow.of(root);
            if (window != null) toggleToolPanel(window, panel);
        });
    }

    @Override
    protected void registerPopups(@NotNull MachineWindow window) {
        super.registerPopups(window);
        window.registerPopup(TOOLS_POPUP, panels -> {
            if (panels < 1 || panels > (MATERIALS_PANEL | EDITOR_PANEL)) return null;
            return Popup.of(() -> Component.translatable(LANG_WILDCARD_TOOLS), column -> {
                int width = PatternEditor.CONTENT_WIDTH;
                if ((panels & MATERIALS_PANEL) != 0) {
                    column.addChild(PopupHost.standalonePanel("wildcard.materials",
                            Popup.of(() -> Component.translatable(LANG_WILDCARD_PATTERN_BUFFER_BLACKLIST), content -> WildcardMaterialGrid.build(this, content)).setContentWidth(width),
                            () -> toggleToolPanel(window, MATERIALS_PANEL)));
                }
                if ((panels & EDITOR_PANEL) != 0) {
                    column.addChild(PopupHost.standalonePanel("wildcard.editor",
                            Popup.of(() -> Component.translatable(PatternEditor.TITLE), content -> PatternEditor.build(this, 0, content)).setContentWidth(width),
                            () -> toggleToolPanel(window, EDITOR_PANEL)));
                }
            }).setContentWidth(PatternEditor.CONTENT_WIDTH + 2 * UISizes.POPUP_PADDING);
        });
    }

    private static void toggleToolPanel(MachineWindow window, int panel) {
        int panels = window.isPopupOpen(TOOLS_POPUP) ? window.getPopupArgument(TOOLS_POPUP) : 0;
        panels ^= panel;
        if (panels == 0) window.closePopup(TOOLS_POPUP);
        else window.openPopup(TOOLS_POPUP, panels);
    }

    // ========== Localization ==========

    @RegisterLanguage(cn = "通配符样板侧栏", en = "Wildcard Pattern Sidebar")
    private static final String LANG_WILDCARD_TOOLS = "gtocore.ae.appeng.wildcard_pattern_buffer.tools";

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
    @RegisterLanguage(cn = "已加载通配符样板", en = "Loaded wildcard patterns")
    static final String LANG_WILDCARD_PATTERN_BUFFER_LOADED_PATTERNS = "gtocore.ae.appeng.wildcard_pattern_buffer.loaded_patterns";
    @RegisterLanguage(cn = "%s 种", en = "%s")
    static final String LANG_WILDCARD_PATTERN_BUFFER_LOADED_PATTERNS_VALUE = "gtocore.ae.appeng.wildcard_pattern_buffer.loaded_patterns.value";
}
