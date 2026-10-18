package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.api.data.material.MaterialType;
import com.gtocore.integration.emi.WildcardRecipeTooltip;
import com.gtocore.integration.jech.PinYinUtils;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.recipe.RecipeType;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.data.UICodecs;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.elements.ae.PhantomAEKeySlot;
import com.gregtechceu.gtceu.uipro.render.FittedText;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.PageOverlay;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.hooks.IUnique;

import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.gto.datasynclib.util.ByteBufCodecExtends;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.TaffyPosition;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.Collator;
import java.util.*;
import java.util.function.BooleanSupplier;

/** 一个控件绘制材料网格；服务端扫描与校验，搜索文字和显示顺序只影响本端。 */
@DataGeneratorScanned
final class WildcardMaterialGrid extends UIElement {

    @RegisterLanguage(cn = "搜索材料名称或注册名", en = "Search material name or registry ID")
    private static final String SEARCH = "gtocore.ae.wildcard_materials.search";
    @RegisterLanguage(cn = "拖入材料物品、流体或机器；右键/中键清空", en = "Drop a material item, fluid or machine; right/middle click to clear")
    private static final String SLOT_SEARCH = "gtocore.ae.wildcard_materials.slot_search";
    @RegisterLanguage(cn = "机器按当前扫描配方的实际输出形式筛选材料；材料物品或流体只筛选对应材料。", en = "Machines filter materials by the actual output forms of scanned recipes; material items and fluids select their material.")
    private static final String SLOT_SEARCH_DETAIL = "gtocore.ae.wildcard_materials.slot_search.detail";
    @RegisterLanguage(cn = "全部材料", en = "All materials")
    private static final String ALL = "gtocore.ae.wildcard_materials.all";
    @RegisterLanguage(cn = "已启用材料", en = "Enabled materials")
    private static final String ENABLED_ONLY = "gtocore.ae.wildcard_materials.enabled_only";
    @RegisterLanguage(cn = "已禁用材料", en = "Disabled materials")
    private static final String DISABLED_ONLY = "gtocore.ae.wildcard_materials.disabled_only";
    @RegisterLanguage(cn = "筛选材料", en = "Filters")
    private static final String FILTER = "gtocore.ae.wildcard_materials.filter";
    @RegisterLanguage(cn = "材料状态", en = "Material state")
    private static final String MATERIAL_STATE = "gtocore.ae.wildcard_materials.material_state";
    @RegisterLanguage(cn = "材料类型", en = "Material type")
    private static final String MATERIAL_TYPE = "gtocore.ae.wildcard_materials.material_type";
    @RegisterLanguage(cn = "按字母表", en = "Alphabetical")
    private static final String ALPHABETICAL = "gtocore.ae.wildcard_materials.alphabetical";
    @RegisterLanguage(cn = "按颜色色相", en = "Color hue")
    private static final String HUE = "gtocore.ae.wildcard_materials.hue";
    @RegisterLanguage(cn = "已启用优先", en = "Enabled first")
    private static final String ENABLED_FIRST = "gtocore.ae.wildcard_materials.enabled_first";
    @RegisterLanguage(cn = "按材料类型", en = "Material type")
    private static final String BASE_TYPE = "gtocore.ae.wildcard_materials.base_type";
    @RegisterLanguage(cn = "勾选本页全部", en = "Blacklist all on this page")
    private static final String SELECT_PAGE = "gtocore.ae.wildcard_materials.select_page";
    @RegisterLanguage(cn = "清除本页全部", en = "Clear blacklist on this page")
    private static final String CLEAR_PAGE = "gtocore.ae.wildcard_materials.clear_page";
    @RegisterLanguage(cn = "仅修改当前搜索、搜索槽和筛选条件下的全部材料（含滚动区域外的结果）。", en = "Applies to all materials matching the current search, search slot and filters, including results outside the scroll viewport.")
    private static final String PAGE_SELECTION_HINT = "gtocore.ae.wildcard_materials.page_selection_hint";
    @RegisterLanguage(cn = "反转排序方向", en = "Reverse sort direction")
    private static final String REVERSE = "gtocore.ae.wildcard_materials.reverse";
    @RegisterLanguage(cn = "没有匹配的可运行材料", en = "No matching runnable materials")
    private static final String EMPTY = "gtocore.ae.wildcard_materials.empty";
    @RegisterLanguage(cn = "已启用；单击禁用此材料", en = "Enabled; click to disable this material")
    private static final String ENABLED = "gtocore.ae.wildcard_materials.enabled";
    @RegisterLanguage(cn = "已禁用；单击启用此材料", en = "Disabled; click to enable this material")
    private static final String DISABLED = "gtocore.ae.wildcard_materials.disabled";
    @RegisterLanguage(cn = "按住 Shift 查看对应样板的实际配方", en = "Hold Shift to preview the actual recipes behind this material's patterns")
    private static final String HOLD_SHIFT = "gtocore.ae.wildcard_materials.hold_shift";
    @RegisterLanguage(cn = "正在读取样板配方…", en = "Loading pattern recipes...")
    private static final String LOADING_PREVIEW = "gtocore.ae.wildcard_materials.loading_preview";
    @RegisterLanguage(cn = "样板配方 %s / %s（Shift + 滚轮切换）", en = "Pattern recipe %s / %s (Shift + scroll to switch)")
    private static final String PREVIEW_PAGE = "gtocore.ae.wildcard_materials.preview_page";
    @RegisterLanguage(cn = "当前样板配方的预览不可用", en = "The current pattern recipe preview is unavailable")
    private static final String NO_PREVIEW = "gtocore.ae.wildcard_materials.no_preview";

    private static final String[] SORTS = { ALPHABETICAL, HUE, ENABLED_FIRST, BASE_TYPE };
    private static final MaterialType[] BASE_TYPES = MaterialType.values();
    private static final AEKey[] NO_OUTPUTS = new AEKey[0];
    private static final ResourceLocation[] NO_RECIPES = new ResourceLocation[0];
    private static final PatternPreview EMPTY_PREVIEW = new PatternPreview(null, NO_RECIPES);
    private static final StreamCodec<FriendlyByteBuf, BitSet> BITSET = StreamCodec.convert(ByteBufCodecs.LONG_ARRAY, BitSet::toLongArray, BitSet::valueOf);
    // 网络只传材料注册整数 id，实际产物留在服务端供机器搜索使用。
    private static final StreamCodec<FriendlyByteBuf, ArrayList<MEWildcardPatternBufferPartMachine.ScannedMaterial>> MATERIALS = StreamCodec.of((buf, entries) -> {
        buf.writeVarInt(entries.size());
        for (var entry : entries) GTCEuAPI.materialManager.streamCodec().encode(buf, entry.material());
    }, buf -> {
        int size = buf.readVarInt();
        var entries = new ArrayList<MEWildcardPatternBufferPartMachine.ScannedMaterial>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new MEWildcardPatternBufferPartMachine.ScannedMaterial(GTCEuAPI.materialManager.streamCodec().decode(buf), NO_OUTPUTS, NO_RECIPES));
        }
        return entries;
    });
    private static final StreamCodec<FriendlyByteBuf, PatternPreview> PREVIEW = StreamCodec.of((buf, preview) -> {
        buf.writeBoolean(preview.material() != null);
        if (preview.material() == null) return;
        GTCEuAPI.materialManager.streamCodec().encode(buf, preview.material());
        buf.writeVarInt(preview.recipes().length);
        for (var id : preview.recipes()) ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.encode(buf, id);
    }, buf -> {
        if (!buf.readBoolean()) return EMPTY_PREVIEW;
        var material = GTCEuAPI.materialManager.streamCodec().decode(buf);
        var recipes = new ResourceLocation[buf.readVarInt()];
        for (int i = 0; i < recipes.length; i++) recipes[i] = ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.decode(buf);
        return new PatternPreview(material, recipes);
    });

    private record PatternPreview(@Nullable Material material, ResourceLocation[] recipes) {}

    private record PageSelection(List<Material> materials, boolean disabled) {}

    private final MEWildcardPatternBufferPartMachine machine;
    private final boolean remote;
    private final SyncValue<BitSet> disabled;
    private final SyncValue<BitSet> matches;
    private final RPC<Material> toggle;
    private final RPC<PageSelection> selectPage;
    private final RPC<Material> requestPreview;
    private final Button pageSelectionButton;

    // 服务端快照只在扫描结果、黑名单或搜索槽变化时重算。
    private ArrayList<MEWildcardPatternBufferPartMachine.ScannedMaterial> flagMaterials;
    private ArrayList<MEWildcardPatternBufferPartMachine.ScannedMaterial> matchMaterials;
    private int flagRevision = -1;
    private BitSet serverDisabled = new BitSet();
    private BitSet serverMatches = new BitSet();
    @Nullable
    private AEKey serverSearchKey;
    @Nullable
    private AEKey matchedKey;
    @Nullable
    private Material previewMaterial;
    private ArrayList<MEWildcardPatternBufferPartMachine.ScannedMaterial> previewMaterials;
    private PatternPreview serverPreview = EMPTY_PREVIEW;

    // 客户端只为条目缓存名称、图标、提示，不给每个材料建立独立控件。
    private final ArrayList<Entry> entries = new ArrayList<>();
    private final IntArrayList visible = new IntArrayList();
    private final EnumSet<MaterialType> materialTypes = EnumSet.allOf(MaterialType.class);
    private final String[] filterLabels = new String[2];
    private final Component[] pageSelectionTooltips = { Component.translatable(SELECT_PAGE), Component.translatable(CLEAR_PAGE) };
    private final Component pageSelectionHint = Component.translatable(PAGE_SELECTION_HINT);
    private final String[] sortLabels = SORTS.clone();
    private String emptyLabel = "";
    private String search = "";
    private boolean showEnabled = true;
    private boolean showDisabled = true;
    private boolean allVisibleDisabled;
    private byte sort;
    private boolean reversed;
    private Collator alphabetical;
    @Nullable
    private Material lastPreviewRequest;

    private WildcardMaterialGrid(MEWildcardPatternBufferPartMachine machine) {
        this.machine = machine;
        remote = machine.isRemote();
        if (remote) {
            String filterLabel = Component.translatable(FILTER).getString();
            filterLabels[0] = filterLabel + " ▼";
            filterLabels[1] = filterLabel + " ▲";
            for (int i = 0; i < SORTS.length; i++) sortLabels[i] = Component.translatable(SORTS[i]).getString();
            emptyLabel = UIText.fit(Component.translatable(EMPTY).getString(), UISizes.SLOT_ROW_WIDTH);
        }
        layout(l -> l.size(UISizes.SLOT_ROW_WIDTH, UISizes.SLOT_SIZE));
        addSyncValue(SyncValue.of(machine::getScannedMaterials, MATERIALS, new ArrayList<>(0)).onChanged(this::collectEntries));
        disabled = addSyncValue(SyncValue.of(this::disabledFlags, BITSET, new BitSet()).onChanged(value -> refilter()));
        matches = addSyncValue(SyncValue.of(this::matchingFlags, BITSET, new BitSet()).onChanged(value -> refilter()));
        toggle = addRPC(GTCEuAPI.materialManager.streamCodec(), (player, material) -> machine.toggleMaterial(material))
                .validate(Objects::nonNull);
        selectPage = addRPC(StreamCodec.composite(
                UICodecs.list(GTCEuAPI.materialManager.streamCodec(), GTCEuAPI.materialManager.getRegisteredMaterials().size()), PageSelection::materials,
                ByteBufCodecs.BOOL, PageSelection::disabled, PageSelection::new),
                (player, selection) -> machine.setMaterialsDisabled(selection.materials(), selection.disabled()));
        requestPreview = addRPC(GTCEuAPI.materialManager.streamCodec(), (player, material) -> previewMaterial = material)
                .validate(Objects::nonNull)
                .onRejected(() -> lastPreviewRequest = null);
        addSyncValue(SyncValue.of(this::currentPreview, PREVIEW, EMPTY_PREVIEW).onChanged(this::receivePreview));
        pageSelectionButton = Button.glyph("✓").bindClientText(() -> allVisibleDisabled ? "×" : "✓")
                .setOnClientClick(this::selectVisible)
                .clientDisabled(visible::isEmpty);
        pageSelectionButton.setHoverTooltips(pageSelectionTooltips[0], pageSelectionHint);
    }

    /** 只同步 Shift 悬停的材料；扫描结果或请求变化时才重建预览。 */
    private PatternPreview currentPreview() {
        var current = machine.getScannedMaterials();
        if (current == previewMaterials && serverPreview.material() == previewMaterial) return serverPreview;
        previewMaterials = current;
        serverPreview = previewMaterial == null ? EMPTY_PREVIEW : new PatternPreview(previewMaterial, NO_RECIPES);
        for (var entry : current) {
            if (entry.material() == previewMaterial) {
                serverPreview = new PatternPreview(previewMaterial, entry.recipes());
                break;
            }
        }
        return serverPreview;
    }

    private void receivePreview(PatternPreview preview) {
        if (!remote) return;
        for (var entry : entries) {
            if (entry.material != preview.material()) continue;
            entry.recipes = preview.recipes();
            entry.previewIndex = 0;
            preparePreview(entry);
            break;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void preparePreview(Entry entry) {
        entry.preview = entry.recipes.length == 0 ? null : WildcardRecipeTooltip.of(entry.recipes[entry.previewIndex]);
        var enabled = Component.translatable(ENABLED).withStyle(ChatFormatting.GREEN);
        var disabled = Component.translatable(DISABLED).withStyle(ChatFormatting.RED);
        var state = Component.translatable(NO_PREVIEW).withStyle(ChatFormatting.GRAY);
        if (entry.preview == null) {
            entry.previewEnabledTooltip = List.of(entry.displayName, state, enabled);
            entry.previewDisabledTooltip = List.of(entry.displayName, state, disabled);
        } else {
            var page = Component.translatable(PREVIEW_PAGE, entry.previewIndex + 1, entry.recipes.length).withStyle(ChatFormatting.GRAY);
            entry.previewEnabledTooltip = List.of(entry.displayName, entry.preview.categoryName(), page, enabled);
            entry.previewDisabledTooltip = List.of(entry.displayName, entry.preview.categoryName(), page, disabled);
        }
    }

    static void build(MEWildcardPatternBufferPartMachine machine, UIElement column) {
        var grid = new WildcardMaterialGrid(machine);
        var field = TextField.of(LayoutStyle.AUTO, () -> grid.search, text -> {
            grid.search = text;
            grid.refilter();
        }).bindClientPlaceholder(() -> Component.translatable(SEARCH));
        field.layout(l -> l.flex(1));
        field.tooltips(SEARCH);
        field.getInput().setMaxStringLength(64);
        field.setClientSideWidget();
        column.addChild(UIElement.centeredRow(UISizes.SLOT_SIZE).addChildren(field, new SearchSlot(grid)));
        var filterOptions = UIElement.section();
        var checklist = new ScrollerView("ae.wildcard_material_filters", UISizes.SLOT_ROW_WIDTH + ScrollerView.SCROLL_BAR_SPACE,
                ScrollerView.heightFor(6, Button.HEIGHT, UISizes.GAP), UISizes.GAP).setResizable(false);
        checklist.addScrollViewChild(new FilterOption(Component.translatable(ALL),
                () -> grid.showEnabled && grid.showDisabled && grid.materialTypes.size() == BASE_TYPES.length, () -> {
                    boolean selectAll = !grid.showEnabled || !grid.showDisabled || grid.materialTypes.size() != BASE_TYPES.length;
                    grid.showEnabled = selectAll;
                    grid.showDisabled = selectAll;
                    grid.materialTypes.clear();
                    if (selectAll) Collections.addAll(grid.materialTypes, BASE_TYPES);
                    grid.refilter();
                }));
        checklist.addScrollViewChild(TextLine.translatable(LayoutStyle.AUTO, MATERIAL_STATE));
        checklist.addScrollViewChild(new FilterOption(Component.translatable(ENABLED_ONLY), () -> grid.showEnabled, () -> {
            grid.showEnabled = !grid.showEnabled;
            grid.refilter();
        }));
        checklist.addScrollViewChild(new FilterOption(Component.translatable(DISABLED_ONLY), () -> grid.showDisabled, () -> {
            grid.showDisabled = !grid.showDisabled;
            grid.refilter();
        }));
        checklist.addScrollViewChild(TextLine.translatable(LayoutStyle.AUTO, MATERIAL_TYPE));
        for (var type : BASE_TYPES) {
            checklist.addScrollViewChild(new FilterOption(Component.translatable("gtocore.material.base_type.name." + type.name()),
                    () -> grid.materialTypes.contains(type), () -> {
                        if (!grid.materialTypes.remove(type)) grid.materialTypes.add(type);
                        grid.refilter();
                    }));
        }
        filterOptions.addChild(checklist);
        var dropdown = new FilterDropdown(grid, filterOptions);
        var filter = Button.of(LayoutStyle.AUTO).layout(l -> l.flex(1))
                .bindClientText(() -> grid.filterLabels[0]);
        filter.setOnClientClick(() -> dropdown.open(filter));
        var sort = Button.of(LayoutStyle.AUTO).layout(l -> l.flex(1))
                .bindClientText(() -> grid.sortLabels[grid.sort])
                .setOnClientClick(() -> {
                    grid.sort = (byte) ((grid.sort + 1) % SORTS.length);
                    grid.refilter();
                });
        var reverse = Button.glyph("↑").bindClientText(() -> grid.reversed ? "↓" : "↑").setOnClientClick(() -> {
            grid.reversed = !grid.reversed;
            grid.refilter();
        });
        reverse.tooltips(REVERSE);
        column.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(filter, sort, reverse, grid.pageSelectionButton, dropdown));
        var scroller = new ScrollerView("ae.wildcard_materials", UISizes.SLOT_ROW_WIDTH + ScrollerView.SCROLL_BAR_SPACE, 4 * UISizes.SLOT_SIZE)
                .setVerticalScrollDisplay(ScrollerView.ScrollDisplay.ALWAYS);
        scroller.addScrollViewChild(grid);
        column.addChild(scroller);
    }

    private void selectVisible() {
        if (visible.isEmpty()) return;
        var materials = new ArrayList<Material>(visible.size());
        for (int i = 0; i < visible.size(); i++) materials.add(entries.get(visible.getInt(i)).material);
        selectPage.send(new PageSelection(materials, !allVisibleDisabled));
    }

    private BitSet disabledFlags() {
        var current = machine.getScannedMaterials();
        int revision = machine.getMaterialBlacklistRevision();
        if (current != flagMaterials || revision != flagRevision) {
            flagMaterials = current;
            flagRevision = revision;
            var flags = new BitSet(current.size());
            for (int i = 0; i < current.size(); i++) flags.set(i, machine.isMaterialDisabled(current.get(i).material()));
            serverDisabled = flags;
        }
        return serverDisabled;
    }

    private BitSet matchingFlags() {
        var current = machine.getScannedMaterials();
        if (current != matchMaterials || matchedKey != serverSearchKey) {
            matchMaterials = current;
            matchedKey = serverSearchKey;
            var flags = new BitSet(current.size());
            var material = materialOf(serverSearchKey);
            var machineItem = serverSearchKey instanceof AEItemKey item && item.getItem() instanceof MetaMachineItem meta ? meta : null;
            var types = machineItem == null ? null : machineItem.getDefinition().getRecipeTypes();
            for (int i = 0; i < current.size(); i++) {
                var entry = current.get(i);
                boolean match = serverSearchKey == null || material == entry.material();
                if (machineItem != null && types != null) {
                    for (var type : types) {
                        if (!(type instanceof RecipeType recipeType)) continue;
                        for (var output : entry.outputs()) {
                            if (materialOf(output) != entry.material()) continue;
                            if (output instanceof AEItemKey item && recipeType.itemsCanBeProduced.contains(((IUnique) item.getItem()).ae2$getUid()) ||
                                    output instanceof AEFluidKey fluid && recipeType.fluidsCanBeProduced.contains(((IUnique) fluid.getFluid()).ae2$getUid())) {
                                match = true;
                                break;
                            }
                        }
                        if (match) break;
                    }
                }
                flags.set(i, match);
            }
            serverMatches = flags;
        }
        return serverMatches;
    }

    private void collectEntries(ArrayList<MEWildcardPatternBufferPartMachine.ScannedMaterial> materials) {
        if (!remote) return;
        entries.clear();
        lastPreviewRequest = null;
        alphabetical = Collator.getInstance(Locale.forLanguageTag(Minecraft.getInstance().getLanguageManager().getSelected().replace('_', '-')));
        entries.ensureCapacity(materials.size());
        for (var entry : materials) entries.add(new Entry(entry.material()));
        refilter();
    }

    private void refilter() {
        if (!remote) return;
        visible.clear();
        visible.ensureCapacity(entries.size());
        var flags = disabled.getValue();
        var matching = matches.getValue();
        allVisibleDisabled = true;
        String query = search.strip().toLowerCase(Locale.ROOT);
        for (int i = 0; i < entries.size(); i++) {
            if (!matching.get(i) || (flags.get(i) ? !showDisabled : !showEnabled)) continue;
            var entry = entries.get(i);
            if (!materialTypes.contains(entry.materialType)) continue;
            if (!query.isEmpty() && !PinYinUtils.match(entry.name, query) && !entry.id.contains(query)) continue;
            visible.add(i);
            if (!flags.get(i)) allVisibleDisabled = false;
        }
        if (visible.isEmpty()) allVisibleDisabled = false;
        pageSelectionButton.setHoverTooltips(pageSelectionTooltips[allVisibleDisabled ? 1 : 0], pageSelectionHint);
        IntArrays.stableSort(visible.elements(), 0, visible.size(), (a, b) -> {
            int result = switch (sort) {
                case 1 -> Float.compare(entries.get(a).hue, entries.get(b).hue);
                case 2 -> Boolean.compare(flags.get(a), flags.get(b));
                case 3 -> Integer.compare(entries.get(a).materialType.ordinal(), entries.get(b).materialType.ordinal());
                default -> 0;
            };
            if (result == 0) result = alphabetical.compare(entries.get(a).name, entries.get(b).name);
            if (result == 0) result = entries.get(a).id.compareTo(entries.get(b).id);
            return reversed ? -Integer.signum(result) : result;
        });
        layout(l -> l.height(Math.max(1, (visible.size() + UISizes.SLOTS_PER_ROW - 1) / UISizes.SLOTS_PER_ROW) * UISizes.SLOT_SIZE));
    }

    private int entryAt(double mouseX, double mouseY) {
        if (!isMouseOverElement(mouseX, mouseY)) return -1;
        for (Widget parent = getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof ScrollerView && !parent.isMouseOverElement(mouseX, mouseY)) return -1;
        }
        int column = (int) (mouseX - getPositionX()) / UISizes.SLOT_SIZE;
        int row = (int) (mouseY - getPositionY()) / UISizes.SLOT_SIZE;
        if (column >= UISizes.SLOTS_PER_ROW) return -1;
        int index = row * UISizes.SLOTS_PER_ROW + column;
        return index < visible.size() ? visible.getInt(index) : -1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        int x0 = getPositionX(), y0 = getPositionY();
        if (visible.isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, emptyLabel,
                    x0, UIText.centerY(y0, UISizes.SLOT_SIZE), UITheme.TEXT_SECONDARY, false);
            return;
        }
        int top = Integer.MIN_VALUE, bottom = Integer.MAX_VALUE;
        for (Widget parent = getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof ScrollerView) {
                top = Math.max(top, parent.getPositionY());
                bottom = Math.min(bottom, parent.getPositionY() + parent.getSizeHeight());
            }
        }
        int hovered = entryAt(mouseX, mouseY);
        int firstRow = top <= y0 ? 0 : (top - y0) / UISizes.SLOT_SIZE;
        int from = Math.min(visible.size(), firstRow * UISizes.SLOTS_PER_ROW);
        for (int k = from; k < visible.size(); k++) {
            int x = x0 + k % UISizes.SLOTS_PER_ROW * UISizes.SLOT_SIZE;
            int y = y0 + k / UISizes.SLOTS_PER_ROW * UISizes.SLOT_SIZE;
            if (y >= bottom) break;
            int index = visible.getInt(k);
            UITheme.ITEM_SLOT.draw(graphics, mouseX, mouseY, x, y, UISizes.SLOT_SIZE, UISizes.SLOT_SIZE);
            entries.get(index).icon.draw(graphics, mouseX, mouseY, x + 1, y + 1, 16, 16);
            if (disabled.getValue().get(index)) UIDraw.disabledHatch(graphics, x, y, UISizes.SLOT_SIZE, UISizes.SLOT_SIZE);
            graphics.fill(x + 13, y + 2, x + 16, y + 5, disabled.getValue().get(index) ? 0xFFB64040 : 0xFF4E9F45);
            if (index == hovered) UIDraw.hoverOverlay(graphics, x, y, UISizes.SLOT_SIZE, UISizes.SLOT_SIZE);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        int index = entryAt(mouseX, mouseY);
        if (index < 0 || gui == null || gui.getModularUIGui() == null) return;
        var entry = entries.get(index);
        boolean isDisabled = disabled.getValue().get(index);
        if (Screen.hasShiftDown()) {
            if (lastPreviewRequest != entry.material) {
                lastPreviewRequest = entry.material;
                requestPreview.send(entry.material);
            }
            gui.getModularUIGui().setHoverTooltip(isDisabled ? entry.previewDisabledTooltip : entry.previewEnabledTooltip, ItemStack.EMPTY, null, entry.preview);
        } else {
            gui.getModularUIGui().setHoverTooltip(isDisabled ? entry.disabledTooltip : entry.enabledTooltip, ItemStack.EMPTY, null, null);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        int index = entryAt(mouseX, mouseY);
        if (!Screen.hasShiftDown() || index < 0 || wheelDelta == 0) return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        var entry = entries.get(index);
        if (entry.recipes != null && entry.recipes.length > 1) {
            entry.previewIndex = Math.floorMod(entry.previewIndex + (wheelDelta > 0 ? -1 : 1), entry.recipes.length);
            preparePreview(entry);
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int index = entryAt(mouseX, mouseY);
        if (button != 0 || index < 0) return super.mouseClicked(mouseX, mouseY, button);
        toggle.send(entries.get(index).material);
        playButtonClickSound();
        return true;
    }

    private static @NotNull Material materialOf(@Nullable AEKey key) {
        return switch (key) {
            case AEItemKey item -> ChemicalHelper.getMaterialStack(item.getReadOnlyStack()).material();
            case AEFluidKey fluid -> ChemicalHelper.getMaterial(fluid.getFluid());
            case null, default -> GTMaterials.NULL;
        };
    }

    private static boolean isSearchKey(AEKey key) {
        return key instanceof AEItemKey item && item.getItem() instanceof MetaMachineItem || materialOf(key) != GTMaterials.NULL;
    }

    private static final class Entry {

        final Material material;
        final String name;
        final String id;
        final MaterialType materialType;
        final float hue;
        final IGuiTexture icon;
        final List<Component> enabledTooltip;
        final List<Component> disabledTooltip;
        final Component displayName;
        @Nullable
        ResourceLocation[] recipes;
        int previewIndex;
        @Nullable
        WildcardRecipeTooltip preview;
        List<Component> previewEnabledTooltip;
        List<Component> previewDisabledTooltip;

        Entry(Material material) {
            this.material = material;
            var localized = material.getLocalizedName();
            displayName = localized;
            name = localized.getString().toLowerCase(Locale.ROOT);
            id = material.getResourceLocation().toString();
            materialType = MaterialType.get(material);
            int rgb = material.getMaterialRGB();
            hue = java.awt.Color.RGBtoHSB(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, null)[0];
            icon = new GuiTextureGroup(
                    new ResourceTexture("gtceu:textures/item/material_sets/dull/ingot.png").setColor(material.getMaterialARGB()),
                    new ResourceTexture("gtceu:textures/item/material_sets/dull/ingot_secondary.png").setColor(material.getLayerARGB(1)),
                    new ResourceTexture("gtceu:textures/item/material_sets/dull/ingot_overlay.png"));
            var registryName = Component.literal(id).withStyle(ChatFormatting.DARK_GRAY);
            var enabled = Component.translatable(ENABLED).withStyle(ChatFormatting.GREEN);
            var disabled = Component.translatable(DISABLED).withStyle(ChatFormatting.RED);
            var hint = Component.translatable(HOLD_SHIFT).withStyle(ChatFormatting.GRAY);
            var loading = Component.translatable(LOADING_PREVIEW).withStyle(ChatFormatting.GRAY);
            enabledTooltip = List.of(localized, registryName, enabled, hint);
            disabledTooltip = List.of(localized, registryName, disabled, hint);
            previewEnabledTooltip = List.of(localized, registryName, enabled, loading);
            previewDisabledTooltip = List.of(localized, registryName, disabled, loading);
        }
    }

    /** 下拉列表由窗口浮层接管绘制和鼠标；标题也在浮层内，单击标题或外部都可收起。 */
    private static final class FilterDropdown extends PageOverlay {

        private final Button header;
        private final UIElement options;

        FilterDropdown(WildcardMaterialGrid grid, UIElement options) {
            this.options = options;
            header = Button.of(LayoutStyle.AUTO).bindClientText(() -> grid.filterLabels[1])
                    .setOnClientClick(() -> setDisplay(false));
            layout(l -> l.positionType(TaffyPosition.ABSOLUTE).left(0).top(0).column()
                    .width(UISizes.SLOT_ROW_WIDTH + ScrollerView.SCROLL_BAR_SPACE).gapAll(UISizes.GAP));
            addChildren(UIElement.row(Button.HEIGHT).addChild(header), options);
            setDisplay(false);
        }

        void open(Button anchor) {
            header.layout(l -> l.width(anchor.getSizeWidth()));
            setDisplay(true);
        }

        @Override
        public boolean isMouseOverElement(double mouseX, double mouseY) {
            return header.isMouseOverElement(mouseX, mouseY) || options.isMouseOverElement(mouseX, mouseY);
        }

        @Override
        protected void onOutsideClick() {
            setDisplay(false);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected void drawOverlayBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            // 标题按钮与选项区块各自绘制背景。
        }
    }

    /** 筛选仅在本端生效；复用按钮外观和多选标记，不为筛选项建立服务端绑定。 */
    private static final class FilterOption extends Button {

        private final Component label;
        private final BooleanSupplier checked;
        @Nullable
        private FittedText fitted;

        FilterOption(Component label, BooleanSupplier checked, Runnable toggle) {
            super(LayoutStyle.AUTO, Button.HEIGHT, null, null);
            this.label = label;
            this.checked = checked;
            bindClientVariant(() -> checked.getAsBoolean() ? UITheme.ButtonVariant.CONFIRM : UITheme.ButtonVariant.DEFAULT);
            setOnClientClick(toggle);
            tooltips(label);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            int x = getPositionX(), y = getPositionY(), faceHeight = getSizeHeight() - UISizes.BUTTON_LIP_HEIGHT;
            int padding = UISizes.TEXT_PADDING;
            UIDraw.optionMark(graphics, x + padding, y + (faceHeight - UISizes.OPTION_MARK_SIZE) / 2, true, checked.getAsBoolean());
            int textX = x + 2 * padding + UISizes.OPTION_MARK_SIZE;
            if (fitted == null) fitted = new FittedText();
            UIText.drawLeft(graphics, fitted.fit(label, x + getSizeWidth() - padding - textX), textX,
                    UIText.centerY(y, faceHeight), getTextColor(isMouseOverElement(mouseX, mouseY), true));
        }
    }

    /** 搜索槽仅保留右键/中键清空条件；资源槽行为与同步由 GTM 提供。 */
    private static final class SearchSlot extends UIElement {

        private final RPC<Unit> clear;

        SearchSlot(WildcardMaterialGrid grid) {
            layout(l -> l.size(UISizes.SLOT_SIZE, UISizes.SLOT_SIZE));
            clear = addRPC(UICodecs.UNIT, (player, ignored) -> grid.serverSearchKey = null);
            var slot = PhantomAEKeySlot.of(() -> grid.serverSearchKey, key -> grid.serverSearchKey = key, WildcardMaterialGrid::isSearchKey).xeiPhantom();
            slot.setHoverTooltips(Component.translatable(SLOT_SEARCH), Component.translatable(SLOT_SEARCH_DETAIL));
            addChild(slot);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if ((button == 1 || button == 2) && isMouseOverElement(mouseX, mouseY)) {
                if (!isDisabled()) clear.send(Unit.INSTANCE);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
