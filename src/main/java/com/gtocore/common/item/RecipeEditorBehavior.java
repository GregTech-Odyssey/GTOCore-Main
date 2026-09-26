package com.gtocore.common.item;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTORecipeTypes;

import com.gtolib.GTOCore;
import com.gtolib.api.machine.DummyMachine;
import com.gtolib.utils.FluidUtils;
import com.gtolib.utils.ItemUtils;
import com.gtolib.utils.StringConverter;
import com.gtolib.utils.StringIndex;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeSlotLayouts;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeSlots;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.PhantomFluidSlot;
import com.gregtechceu.gtceu.uipro.elements.PhantomItemSlot;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.cover.CoverUIs;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fluids.FluidStack;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;
import dev.vfyjxf.taffy.style.AlignItems;
import it.unimi.dsi.fastutil.objects.Reference2CharLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.ArrayList;

public final class RecipeEditorBehavior implements IItemUIFactory, IFancyUIProvider {

    public static final RecipeEditorBehavior INSTANCE = new RecipeEditorBehavior();

    private static final Map<MetaMachine, DummyMachine> CACHE = new Reference2ObjectOpenHashMap<>();
    private static final Map<BlockPos, DummyMachine> POS_CACHE = new O2OOpenCacheHashMap<>();

    private boolean isGT;
    private DummyMachine machine;

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) {
            Set<BiCache> cache = new OpenCacheHashSet<>();
            for (GTRecipeType recipeType : GTRegistries.RECIPE_TYPES.values()) {
                if (recipeType.isNoSearch()) continue;
                if (recipeType == GTRecipeTypes.BREWING_RECIPES) continue;
                if (recipeType == GTRecipeTypes.SCANNER_RECIPES) continue;
                if (recipeType == GTORecipeTypes.LARGE_GAS_COLLECTOR_RECIPES) continue;
                if (recipeType == GTORecipeTypes.SPACE_STATION_CONSTRUCTION_RECIPES) continue;
                var recipes = new ArrayList<>(recipeType.recipes.values());
                recipeType.getProxyRecipes().forEach((t) -> {
                    if (t instanceof GTRecipeType type) {
                        recipes.addAll(type.recipes.values());
                    }
                });
                var stringSetMap = new O2OOpenCacheHashMap<ResourceLocation, Set<String>>(recipes.size());
                for (var recipe : recipes) {
                    var id = recipe.id;
                    var input = new OpenCacheHashSet<String>();
                    if (!recipe.itemInputs.isEmpty()) {
                        for (var content : recipe.itemInputs) {
                            var ingredient = content.inner;
                            Ingredient inner = ingredient.inner;
                            a:
                            for (Ingredient.Value value : inner.values) {
                                if (value instanceof Ingredient.ItemValue itemValue) {
                                    Collection<ItemStack> stacks = itemValue.getItems();
                                    if (stacks.isEmpty()) {
                                        GTOCore.LOGGER.error("配方 {} 存在空物品输入", id);
                                        continue;
                                    }
                                    for (ItemStack stack : stacks) {
                                        if (stack.isEmpty()) continue;
                                        if (stack.is(GTItems.PROGRAMMED_CIRCUIT.get())) {
                                            input.add("c" + IntCircuitBehaviour.getCircuitConfiguration(stack));
                                        } else {
                                            String s = ItemUtils.getId(stack);
                                            if (stack.getTag() != null) {
                                                s = s + stack.getTag();
                                            }
                                            input.add(s);
                                        }
                                        break a;
                                    }
                                } else if (value instanceof Ingredient.TagValue tagValue) {
                                    input.add(tagValue.tag.location().toString());
                                    break;
                                }
                            }
                        }
                    }
                    if (!recipe.fluidInputs.isEmpty()) {
                        for (var content : recipe.fluidInputs) {
                            FluidStack[] stacks = content.inner.getStacks();
                            if (stacks.length == 0) {
                                GTOCore.LOGGER.error("配方 {} 存在空流体输入", id);
                                continue;
                            }
                            String s = FluidUtils.getId(stacks[0].getFluid());
                            if (stacks[0].getTag() != null) {
                                s = s + stacks[0].getTag();
                            }
                            input.add(s);
                        }
                    }
                    if (input.isEmpty()) continue;
                    stringSetMap.put(id, input);
                }
                stringSetMap.forEach((id, set) -> {
                    var map = new O2OOpenCacheHashMap<>(stringSetMap);
                    map.remove(id);
                    map.forEach((k, v) -> {
                        var object = new BiCache(id, k);
                        if (cache.contains(object)) return;
                        if (set.containsAll(v)) {
                            cache.add(object);
                            GTOCore.LOGGER.error("\n{} 与 {} 冲突\n{}\n{}", id, k, set, v);
                        }
                    });
                });
            }
            return InteractionResult.CONSUME;
        }
        MetaMachine metaMachine = MetaMachine.getMachine(context.getLevel(), context.getClickedPos());
        if (metaMachine instanceof IRecipeLogicMachine) {
            isGT = true;
            machine = CACHE.computeIfAbsent(metaMachine, DummyMachine::createDummyMachine);
        } else if (context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof CraftingTableBlock) {
            isGT = false;
            machine = POS_CACHE.computeIfAbsent(context.getClickedPos(), p -> DummyMachine.createDummyMachine(BlockEntityType.CHEST, p, GTMachines.ASSEMBLER[1].defaultBlockState(), GTRecipeTypes.ASSEMBLER_RECIPES));
        } else {
            return InteractionResult.PASS;
        }
        IItemUIFactory.super.use(context.getItemInHand().getItem(), context.getLevel(), context.getPlayer(), context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack heldItem = player.getItemInHand(usedHand);
        return InteractionResultHolder.success(heldItem);
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return new ModularUI(176, 166, holder, entityPlayer).widget(new MachineWindow(this));
    }

    /**
     * 编辑页（新式界面框架）：上方是按配方类型排好的虚拟槽（可从 EMI 拖入），GT 机器下方再有配方参数区块，
     * 最后一个整行按钮把当前内容导出成配方代码写进日志。
     */
    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var page = UIElement.column(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH).gapAll(UISizes.SECTION_GAP));
        page.addChild(slotArea());
        if (isGT) {
            var section = UIElement.section();
            section.addChildren(
                    textRow("ID", new TextField(UISizes.BUTTON_WIDTH * 2, () -> machine.id, id -> machine.id = id)),
                    CoverUIs.inlineNumberRow("Circuit", NumberField.of(LayoutStyle.AUTO, () -> machine.circuit, value -> machine.circuit = (int) value, 0, 32)),
                    CoverUIs.inlineNumberRow("EUt", NumberField.of(LayoutStyle.AUTO, () -> machine.eut, value -> machine.eut = value, -Long.MAX_VALUE, Long.MAX_VALUE)),
                    CoverUIs.inlineNumberRow("Duration", NumberField.of(LayoutStyle.AUTO, () -> machine.duration, value -> machine.duration = (int) value, 0, Integer.MAX_VALUE)),
                    CoverUIs.inlineNumberRow("FurnaceTemp", NumberField.of(LayoutStyle.AUTO, () -> machine.temp, value -> machine.temp = (int) value, 0, Integer.MAX_VALUE)),
                    CoverUIs.inlineNumberRow("MANAt", NumberField.of(LayoutStyle.AUTO, () -> machine.manat, value -> machine.manat = (int) value, Integer.MIN_VALUE, Integer.MAX_VALUE)));
            page.addChild(section);
        }
        page.addChild(Button.text(LayoutStyle.AUTO, () -> "Export").setOnServerClick(this::exportRecipe));
        return page;
    }

    /** 槽位区：输入虚拟槽 → 配方类型的进度箭头 → 输出虚拟槽；工作台只有物品。 */
    /**
     * 槽位区，排法同默认配方排布（{@link RecipeSlotLayouts#DEFAULT}）：两侧等宽、箭头居中，槽上叠配方类型的角标；
     * 槽换成可从 EMI 拖入的虚拟槽。进度箭头只作装饰，只在客户端循环播放，不同步。
     */
    private UIElement slotArea() {
        var inputs = slotSide(machine.importItems, machine.importFluids, false);
        var outputs = slotSide(machine.exportItems, machine.exportFluids, true);
        int sideWidth = Math.max(inputs.getLayoutStyle().declaredWidth(), outputs.getLayoutStyle().declaredWidth());
        inputs.layout(l -> l.width(sideWidth));
        outputs.layout(l -> l.width(sideWidth));
        var progress = new ProgressWidget(ProgressWidget.JEIProgress, 0, 0, RecipeSlots.PROGRESS_SIZE, RecipeSlots.PROGRESS_SIZE, machine.recipeType.getRecipeUI().getProgressBarTexture());
        progress.setClientSideWidget();
        return new UIElement()
                .layout(l -> l.row().paddingAll(RecipeSlotLayouts.PADDING).gapAll(RecipeSlotLayouts.PROGRESS_MARGIN).alignCenter().alignSelf(AlignItems.CENTER))
                .addChildren(inputs, progress, outputs);
    }

    /** 一侧的虚拟槽：物品一个网格、流体另起一个网格，每行最多 {@link RecipeSlotLayouts#SIDE_COLUMNS} 格；宽度声明为较宽网格的宽度。 */
    private UIElement slotSide(CustomItemStackHandler items, NotifiableFluidTank fluids, boolean output) {
        var ui = machine.recipeType.getRecipeUI();
        var itemSlots = new ArrayList<Widget>(items.getSlots());
        var transfer = ItemTransferHelperImpl.toItemTransfer(items);
        for (int i = 0; i < items.getSlots(); i++) {
            var slot = new PhantomItemSlot(transfer, i).xeiPhantom();
            addOverlay(slot, ui.getSlotOverlay(output, ItemRecipeInfo.INSTANCE, i == items.getSlots() - 1));
            itemSlots.add(slot);
        }
        var fluidSlots = new ArrayList<Widget>(isGT ? fluids.getTanks() : 0);
        if (isGT) {
            for (int i = 0; i < fluids.getTanks(); i++) {
                int tank = i;
                var slot = new PhantomFluidSlot(fluids, tank, () -> fluids.getFluidInTank(tank), fluid -> fluids.setFluidInTank(tank, fluid)).xeiPhantom();
                addOverlay(slot, ui.getSlotOverlay(output, FluidRecipeInfo.INSTANCE, i == fluids.getTanks() - 1));
                fluidSlots.add(slot);
            }
        }
        int columns = Math.min(RecipeSlotLayouts.SIDE_COLUMNS, Math.max(itemSlots.size(), fluidSlots.size()));
        var side = new UIElement().layout(l -> l.column().width(columns * UISizes.SLOT).alignCenter());
        if (!itemSlots.isEmpty()) side.addChild(RecipeSlotLayouts.grid(itemSlots, RecipeSlotLayouts.SIDE_COLUMNS));
        if (!fluidSlots.isEmpty()) side.addChild(RecipeSlotLayouts.grid(fluidSlots, RecipeSlotLayouts.SIDE_COLUMNS));
        return side;
    }

    private static void addOverlay(Widget slot, @Nullable IGuiTexture overlay) {
        if (overlay != null) slot.setBackground(new GuiTextureGroup(slot.getBackgroundTexture(), overlay));
    }

    /** 区块里一行"名称 …… [输入框]"（开发工具，名称不翻译）。 */
    private static UIElement textRow(String label, TextField field) {
        var name = TextLine.constant(0, Component.literal(label)).setColor(UITheme.PANEL_TEXT);
        name.layout(l -> l.flex(1));
        return UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter()).addChildren(name, field);
    }

    /** 把当前内容导出成配方代码（GT 配方构建器或工作台有序合成），写进日志。只在服务端执行。 */
    private void exportRecipe() {
        StringBuilder stringBuilder = new StringBuilder();
        if (isGT) {
            String recipeType;
            if (StringIndex.RECIPETYPE_MAP.containsKey(machine.recipeType)) {
                recipeType = StringIndex.RECIPETYPE_MAP.get(machine.recipeType);
            } else {
                recipeType = machine.recipeType.registryName.getPath().toUpperCase() + "_RECIPES";
            }
            String id = machine.id;
            if (id.isEmpty()) {
                for (int i = 0; i < machine.exportItems.getSlots(); i++) {
                    if (!id.isEmpty()) break;
                    ItemStack stack = machine.exportItems.getStackInSlot(i);
                    if (stack.isEmpty()) continue;
                    id = ItemUtils.getIdLocation(stack.getItem()).getPath();
                }
                for (int i = 0; i < machine.exportFluids.getTanks(); i++) {
                    if (!id.isEmpty()) break;
                    FluidStack stack = machine.exportFluids.getFluidInTank(i);
                    if (stack.isEmpty()) continue;
                    id = FluidUtils.getIdLocation(stack.getFluid()).getPath();
                }
            }
            stringBuilder.append("\n").append(recipeType).append(".builder(\"").append(id).append("\")\n");
            for (int i = 0; i < machine.importItems.getSlots(); i++) {
                ItemStack stack = machine.importItems.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                String stringItem = StringConverter.fromItem(getItemIngredient(stack), 1);
                stringBuilder.append(".inputItems(").append(stringItem).append(")").append("\n");
            }
            for (int i = 0; i < machine.exportItems.getSlots(); i++) {
                ItemStack stack = machine.exportItems.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                String stringItem = StringConverter.fromItem(ItemIngredient.of(stack), 1);
                stringBuilder.append(".outputItems(").append(stringItem).append(")").append("\n");
            }
            for (int i = 0; i < machine.importFluids.getTanks(); i++) {
                FluidStack stack = machine.importFluids.getFluidInTank(i);
                if (stack.isEmpty()) continue;
                String stringFluid = StringConverter.fromFluid(FluidIngredient.of(stack), true);
                stringBuilder.append(".inputFluids(").append(stringFluid).append(")").append("\n");
            }
            for (int i = 0; i < machine.exportFluids.getTanks(); i++) {
                FluidStack stack = machine.exportFluids.getFluidInTank(i);
                if (stack.isEmpty()) continue;
                String stringFluid = StringConverter.fromFluid(FluidIngredient.of(stack), true);
                stringBuilder.append(".outputFluids(").append(stringFluid).append(")").append("\n");
            }
            if (machine.circuit > 0) {
                stringBuilder.append(".circuitMeta(").append(machine.circuit).append(")\n");
            }
            if (machine.eut != 0) {
                stringBuilder.append(".EUt(").append(machine.eut).append(")\n");
            }
            if (machine.temp != 0) {
                stringBuilder.append(".blastFurnaceTemp(").append(machine.temp).append(")\n");
            }
            if (machine.duration == 0) {
                GTOCore.LOGGER.error("无时间");
                return;
            } else {
                stringBuilder.append(".duration(").append(machine.duration).append(")\n");
            }
            if (machine.manat != 0) {
                stringBuilder.append(".MANAt(").append(machine.manat).append(")\n");
            }
            stringBuilder.append(".save();\n");
        } else {
            String id = machine.id;
            if (id.isEmpty())
                id = ItemUtils.getIdLocation(machine.exportItems.getStackInSlot(0).getItem()).getPath();
            stringBuilder.append("\nVanillaRecipeHelper.addShapedRecipe(");
            stringBuilder.append("GTOCore.id(\"").append(id).append("\"), ");
            stringBuilder.append(StringConverter.fromItem(ItemIngredient.of(machine.exportItems.getStackInSlot(0)), 0)).append(",\n\"");
            char c = 'A';
            Reference2CharLinkedOpenHashMap<Item> map = new Reference2CharLinkedOpenHashMap<>();
            for (int i = 0, j = 0; i < machine.importItems.getSlots(); i++, j++) {
                Item item = machine.importItems.getStackInSlot(i).getItem();
                if (item != Items.AIR && !map.containsKey(item)) {
                    map.put(item, c);
                    c++;
                }
                char d = item == Items.AIR ? ' ' : map.getChar(item);
                if (j > 2) {
                    stringBuilder.append("\",\n\"").append(d);
                    j = 0;
                } else {
                    stringBuilder.append(d);
                }
            }
            stringBuilder.append("\",\n");
            map.forEach((k, v) -> stringBuilder.append("'").append(v).append("', ").append(StringConverter.fromItem(getItemIngredient(k.getDefaultInstance()), 2)).append(","));
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append(");");
        }
        GTOCore.LOGGER.info(stringBuilder.toString());
    }

    private static ItemIngredient getItemIngredient(ItemStack stack) {
        if (ItemMap.UNIVERSAL_CIRCUITS.contains(stack.getItem())) {
            for (int tier : GTMachineUtils.ALL_TIERS) {
                if (GTOItems.UNIVERSAL_CIRCUIT[tier].is(stack.getItem())) {
                    return ItemIngredient.of(CustomTags.CIRCUITS_ARRAY[tier], stack.getCount());
                }
            }
        }
        return ItemIngredient.of(stack);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(GTOItems.ULV_ROBOT_ARM.get());
    }

    @Override
    public Component getTitle() {
        return Component.translatable("item.gtocore.recipe_editor");
    }

    private record BiCache(Object a, Object b) {

        @Override
        public boolean equals(Object o) {
            if (o instanceof BiCache(Object a1, Object b1)) {
                if (a.equals(a1) && b.equals(b1)) return true;
                return a.equals(b1) && b.equals(a1);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return a.hashCode() + b.hashCode();
        }
    }
}
