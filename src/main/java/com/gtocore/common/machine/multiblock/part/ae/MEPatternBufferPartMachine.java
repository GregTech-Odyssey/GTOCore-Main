package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.api.gui.configurators.MultiMachineModeFancyConfigurator;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.data.GTORecipes;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.machine.trait.InternalSlotRecipeHandler;
import com.gtocore.integration.ae.PatternContainerGroupHelper;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.trait.NotifiableNotConsumableFluidHandler;
import com.gtolib.api.machine.trait.NotifiableNotConsumableItemHandler;
import com.gtolib.api.network.NetworkPack;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.RecipeType;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IWailaDisplayProvider;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.ButtonConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.FancyInvConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.FancyTankConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.CircuitHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IFilteredHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.transfer.item.LockableItemStackHandler;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.jade.GTElementHelper;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;

import com.gto.datasynclib.FieldDataManager;
import com.gto.datasynclib.IFieldDataHolder;
import com.gto.datasynclib.LazyFieldDataManager;
import com.gto.datasynclib.LogicalSide;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.util.DataCodecs;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gto.recipesearch.IntLongMap;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElementHelper;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternBufferPartMachine extends MEPatternPartMachine<MEPatternBufferPartMachine.InternalSlot> implements IDataStickInteractable, IWailaDisplayProvider {

    @RegisterLanguage(cn = "此槽已缓存配方", en = "Recipe cached in this slot")
    private static final String CACHE = "gtocore.pattern_buffer.cache";

    @RegisterLanguage(cn = "此样板物品输入槽", en = "The item input slots of this pattern")
    public static final String ITEM_SPECIAL = "gtceu.ae.pattern_part_machine.ITEM_SPECIAL";
    @RegisterLanguage(cn = "此样板流体输入槽", en = "The fluid input slots of this pattern")
    public static final String FLUID_SPECIAL = "gtceu.ae.pattern_part_machine.FLUID_SPECIAL";
    @RegisterLanguage(cn = "此样板电路输入槽", en = "The circuit input slot of this pattern")
    public static final String CIRCUIT_SPECIAL = "gtceu.ae.pattern_part_machine.CIRCUIT_SPECIAL";
    @RegisterLanguage(cn = "此样板记录的配方", en = "The recipe recorded by this pattern")
    public static final String RECIPE_SPECIAL = "gtceu.ae.pattern_part_machine.RECIPE_SPECIAL";
    @RegisterLanguage(cn = "点此查看配方详情", en = "Click to see recipe details")
    public static final String VIEW_RECIPE = "gtceu.ae.pattern_part_machine.VIEW_RECIPE";
    @RegisterLanguage(cn = "当前并没有记录任何配方", en = "No recipe is recorded currently")
    public static final String NO_RECIPE = "gtceu.ae.pattern_part_machine.NO_RECIPE";
    @RegisterLanguage(cn = "EMI 中找不到配方 %s", en = "Recipe %s is not available in EMI")
    public static final String RECIPE_NOT_IN_EMI = "gtocore.pattern_buffer.recipe_not_in_emi";
    @RegisterLanguage(cn = "解除当前机器的配方锁定", en = "Clear the recipe lock of this machine")
    public static final String CLEAR_RECIPE_SLOT = "gtceu.ae.pattern_part_machine.clear_recipe";
    @RegisterLanguage(cn = "当前机器的配方锁定已清除", en = "The recipe lock of this machine has been cleared")
    public static final String CLEAR_RECIPE_SLOT_MSG = "gtceu.ae.pattern_part_machine.clear_recipe_msg";
    @RegisterLanguage(cn = "打开emi页面后，选择一个配方，用“+”按钮将其添加到样板中。", en = "After opening the emi page, select a recipe and use the \"+\" button to add it to the pattern.")
    public static final String ADD_RECIPE_MSG = "gtceu.ae.pattern_part_machine.clear_recipe_msg2";
    @RegisterLanguage(cn = "此样板物品与流体配置", en = "The item and fluid configuration of this pattern")
    public static final String PATTERN_CONFIGURATION = "gtceu.ae.pattern_part_machine.PATTERN_CONFIGURATION";
    @RegisterLanguage(cn = "发信合成模式", en = "Emitting crafting mode")
    public static final String EMITTING_CRAFTING_MODE = "gtceu.ae.pattern_part_machine.EMITTING_CRAFTING_MODE";
    @RegisterLanguage(cn = "物品不够时请求合成", en = "Request crafting when items are insufficient")
    public static final String REQUEST_CRAFTING_WHEN_INSUFFICIENT = "gtceu.ae.pattern_part_machine.REQUEST_CRAFTING_WHEN_INSUFFICIENT";
    @RegisterLanguage(cn = "已锁定，由样板内的配方自动拉取虚拟成分进行合成", en = "Locked, automatically pull virtual ingredients for crafting according to the recipe in the pattern")
    public static final String ITEM_LOCKED = "gtceu.ae.pattern_part_machine.locked_emitting_crafting_mode";
    @RegisterLanguage(cn = "该模式与标准发信器的合成卡功能相似，在下单请求该物品后，机器会使用样板中的配方被动持续向机器内输入",
                      en = "This mode is similar to the crafting card function of a standard emitter. " +
                              "After placing an order for the item, the machine will passively and continuously input items into the machine using the recipe in the pattern.")
    public static final String EMITTING_CRAFTING_MODE_TOOLTIP = "gtceu.ae.pattern_part_machine.EMITTING_CRAFTING_MODE_TOOLTIP";
    @RegisterLanguage(cn = "需要先在此槽放入处理样板（发信合成监听样板产物的下单请求）",
                      en = "Put a processing pattern in this slot first (emitting crafting watches orders for the pattern's output)")
    public static final String EMITTING_CRAFTING_MODE_NEED_PATTERN = "gtocore.pattern_buffer.emitting_crafting_mode.need_pattern";
    @RegisterLanguage(cn = "需要先在此槽放入样板", en = "Put a pattern in this slot first")
    public static final String PATTERN_REQUIRED = "gtocore.pattern_buffer.pattern_required";
    @RegisterLanguage(cn = "低存量触发模式", en = "Low stock triggering mode")
    public static final String LOW_STOCK_TRIGGERING_MODE = "gtceu.ae.pattern_part_machine.LOW_STOCK_TRIGGERING_MODE";
    @RegisterLanguage(cn = "该模式会在网络库存量低于设定数量时触发持续被动配方输入，直到库存量满足要求。",
                      en = "This mode will trigger continuous passive recipe input when the network inventory is below the set quantity, until the inventory meets the requirements.")
    public static final String LOW_STOCK_TRIGGERING_MODE_TOOLTIP = "gtceu.ae.pattern_part_machine.LOW_STOCK_TRIGGERING_MODE_TOOLTIP";
    @RegisterLanguage(cn = "低存量库存触发阈值", en = "Low stock triggering threshold")
    public static final String LOW_STOCK_TRIGGERING_THRESHOLD = "gtceu.ae.pattern_part_machine.LOW_STOCK_TRIGGERING_THRESHOLD";
    @RegisterLanguage(cn = "被动输入乘数", en = "Passive input multiplier")
    public static final String PASSIVE_INPUT_MULTIPLIER = "gtceu.ae.pattern_part_machine.PASSIVE_INPUT_MULTIPLIER";
    @RegisterLanguage(cn = "按照设定的乘数调整被动输入的数量。例如，设定为10时，按样板配置的数量×10进行被动输入。",
                      en = "Adjust the quantity of passive input according to the set multiplier. For example, when set to 10, passive input will be performed according to the quantity configured in the pattern x10.")
    public static final String PASSIVE_INPUT_MULTIPLIER_TOOLTIP = "gtceu.ae.pattern_part_machine.PASSIVE_INPUT_MULTIPLIER_TOOLTIP";

    /// EMI 配方页的"+"按钮：把选中的配方写进指定样板槽（客户端发往服务端）
    public static final NetworkPack SET_ID_CHANNEL = NetworkPack.registerC2S("me_pattern_buffer_set_id_channel", (player, buf) -> {
        var blockPos = buf.readBlockPos();
        var slot = buf.readVarInt();
        var recipeId = buf.readResourceLocation();
        if (player.level().getBlockEntity(blockPos) instanceof MetaMachineBlockEntity blockEntity &&
                blockEntity.getMetaMachine() instanceof MEPatternBufferPartMachine machine) {
            machine.setSlotRecipeId(slot, recipeId);
        }
    });

    @Override
    public @Nullable GTRecipeType gto$getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable Collection<GTRecipeType> gto$getRecipeTypes() {
        return recipeTypes;
    }

    @SaveToDisk
    @SyncToClient
    @Getter
    private final ArrayList<GTRecipeType> recipeTypes = new ArrayList<>();
    @SaveToDisk
    @SyncToClient
    @Getter
    public GTRecipeType recipeType = null;

    @SyncToClient
    private final boolean[] caches;
    @SaveToDisk
    public final NotifiableNotConsumableItemHandler shareInventory;
    @SaveToDisk
    public final NotifiableNotConsumableFluidHandler shareTank;
    @SaveToDisk
    public final NotifiableItemStackHandler circuitInventorySimulated;

    @SaveToDisk
    private final Set<BlockPos> proxies = new OpenCacheHashSet<>();
    private final Set<MEPatternBufferProxyPartMachine> proxyMachines = new ReferenceOpenHashSet<>();
    public final InternalSlotRecipeHandler internalRecipeHandler;

    protected ConfiguratorPanel configuratorPanel;

    @Getter
    @SaveToDisk(defaultValue = "0")
    private int priority = 0;

    public MEPatternBufferPartMachine(MetaMachineBlockEntity holder, int maxPatternCount) {
        super(holder, maxPatternCount);
        this.caches = new boolean[maxPatternCount];
        this.shareInventory = createShareInventory();
        this.shareTank = new NotifiableNotConsumableFluidHandler(this, 9, 64000);
        this.circuitInventorySimulated = CircuitHandler.create(this);
        this.internalRecipeHandler = new InternalSlotRecipeHandler(this, getInternalInventory());
    }

    private void setPriority(int priority) {
        if (priority == Integer.MIN_VALUE) return;
        this.priority = priority;
        circuitInventorySimulated.setPriority(priority);
        RecipeHandlerUnit.notify(this);
    }

    NotifiableNotConsumableItemHandler createShareInventory() {
        var h = new NotifiableNotConsumableItemHandler(this, 9, IO.NONE);
        h.setFilter(stack -> !(stack.getItem() instanceof EncodedPatternItem));
        return h;
    }

    @Override
    public InternalSlot[] createInternalSlotArray() {
        return new InternalSlot[getMaxPatternCount()];
    }

    @Override
    public boolean patternFilter(ItemStack stack) {
        if (stack.getOrCreateTag().tags.get("recipe") instanceof StringTag stringTag) {
            var recipe = RecipeBuilder.get(RLUtils.parse(stringTag.getAsString()));
            if (recipe != null) {
                if (recipeType == null) {
                    if (!recipeTypes.isEmpty() && !RecipeType.available(recipe.recipeType, recipeTypes.toArray(new GTRecipeType[0]))) return false;
                } else if (!RecipeType.available(recipe.recipeType, recipeType)) {
                    return false;
                }
            }
        }
        var f = stack.getItem() instanceof ProcessingPatternItem;
        if (!f) return false;
        return checkDuplicatedPattern(this, stack);
    }

    @Override
    public InternalSlot createInternalSlot(int i) {
        return new InternalSlot(this, i);
    }

    @Override
    public List<RecipeHandlerUnit> getRecipeHandlers() {
        return internalRecipeHandler.getSlotHandlers();
    }

    @Override
    public boolean canShared() {
        return true;
    }

    void addProxy(MEPatternBufferProxyPartMachine proxy) {
        proxies.add(proxy.getPos());
        proxyMachines.add(proxy);
    }

    void removeProxy(MEPatternBufferProxyPartMachine proxy) {
        proxies.remove(proxy.getPos());
        proxyMachines.remove(proxy);
    }

    /// 镜像所在区块卸载：保留绑定位置，只是暂时拿不到它那台机器
    void unloadProxy(MEPatternBufferProxyPartMachine proxy) {
        proxyMachines.remove(proxy);
    }

    /// 缓存配方的类型是否还有机器能跑（主机器或任一镜像的机器）；有镜像未加载或未成型时无法判断，视为能跑
    public boolean isRecipeTypeUsable(GTRecipeType type) {
        if (recipeTypes.isEmpty() || recipeTypes.contains(type)) return true;
        if (proxyMachines.size() < proxies.size()) return true;
        for (var proxy : proxyMachines) {
            var controllers = proxy.getControllers();
            if (controllers.isEmpty()) return true;
            for (var controller : controllers) {
                if (controller instanceof IRecipeLogicMachine machine && GTRecipeType.available(type, machine.getAvailableRecipeTypes())) return true;
            }
        }
        return false;
    }

    private Set<MEPatternBufferProxyPartMachine> getProxies() {
        return proxyMachines;
    }

    private void refundAll(ClickData clickData) {
        if (!clickData.isRemote) {
            for (InternalSlot internalSlot : getInternalInventory()) {
                internalSlot.refund();
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (recipeType == GTORecipeTypes.DUMMY_RECIPES || recipeType == GTORecipeTypes.HATCH_COMBINED) {
            recipeType = null;
        }
        MultiMachineModeFancyConfigurator.verify(recipeTypes, recipeType, () -> recipeType = null);
        circuitInventorySimulated.setPriority(priority);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new MultiMachineModeFancyConfigurator(recipeTypes, recipeType, this::setRecipeType));
        sideTabs.attachSubTab(IFilteredHandler.createPriorityConfigurator(this::getPriority, this::setPriority));
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        this.recipeTypes.clear();
        this.recipeTypes.addAll(MultiMachineModeFancyConfigurator.extractRecipeTypes(this.getController()));
        MultiMachineModeFancyConfigurator.verify(recipeTypes, recipeType, () -> recipeType = null);
        for (InternalSlot internalSlot : getInternalInventory()) {
            internalSlot.verify();
        }
    }

    @Override
    public void setAvailableRecipeTypes(@NotNull GTRecipeType[] types) {
        this.recipeTypes.clear();
        this.recipeTypes.addAll(Arrays.asList(types));
        MultiMachineModeFancyConfigurator.verify(recipeTypes, recipeType, () -> recipeType = null);
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        this.recipeTypes.clear();
    }

    public void setRecipeType(GTRecipeType type) {
        if (type != recipeType) {
            recipeType = type;
            if (!isRemote()) {
                // 总成模式覆盖机器模式：丢掉与新模式不符的槽位缓存，再按新模式重读样板配方
                var slots = getInternalInventory();
                for (int i = 0; i < slots.length; i++) {
                    slots[i].setRecipe(slots[i].recipe);
                    if (slots[i].recipe == null) reloadPatternRecipe(i);
                }
            }
            for (var c : getControllers()) {
                if (c instanceof IRecipeLogicMachine machine) {
                    machine.getRecipeLogic().markLastRecipeDirty();
                    machine.getRecipeLogic().updateTickSubscription();
                }
            }
        }
    }

    @Override
    public void onPatternChange(int index) {
        getInternalInventory()[index].setLock(false);
        super.onPatternChange(index);
        // super 里 decodePattern 读到的样板配方会被随后的 InternalSlot.onPatternChange 清掉，这里补读
        if (!isRemote()) reloadPatternRecipe(index);
    }

    private void reloadPatternRecipe(int index) {
        var stack = getInternalPatternInventory().getStackInSlot(index);
        if (!stack.isEmpty()) MEPatternVirtualInputHelper.readRecipeTag(stack, getInternalInventory()[index]::setRecipe);
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        var slot = getDetailsSlotMap().get(patternDetails);
        if (slot != null) {
            return slot.pushPattern(patternDetails, inputHolder);
        }
        return false;
    }

    @Override
    public @Nullable IPatternDetails decodePattern(ItemStack stack, int index) {
        var pattern = super.decodePattern(stack, index);
        if (pattern == null) return null;
        if (!caches[index]) {
            MEPatternVirtualInputHelper.readRecipeTag(stack, getInternalInventory()[index]::setRecipe);
        }
        return pattern;
    }

    @Override
    public IPatternDetails convertPattern(IPatternDetails pattern, int index) {
        var slot = getInternalInventory()[index];
        return MEPatternVirtualInputHelper.convertPattern(pattern, this::getGrid, this::getActionSource,
                slot.circuitInventory, slot.shareInventory.storage, slot.shareTank.getStorages(), null,
                slot.virtualInputAvailability, () -> {
                    slot.setLock(true);
                    return true;
                });
    }

    @Override
    @Nullable
    public Component appendHoverTooltips(int index) {
        if (caches[index]) {
            return Component.translatable(CACHE);
        }
        return null;
    }

    // ==================== 单槽配方 ====================

    /** 样板槽记录的配方：优先取已缓存的配方，否则读样板物品上的 {@code recipe} 标签；槽号越界或都没有时返回 null。 */
    @Nullable
    public ResourceLocation getSlotRecipeId(int slot) {
        if (slot < 0 || slot >= getMaxPatternCount()) return null;
        var recipe = getInternalInventory()[slot].recipe;
        if (recipe != null) return recipe.id;
        var stack = getPatternInventory().getStackInSlot(slot);
        if (stack.isEmpty() || stack.getTag() == null) return null;
        var recipeId = stack.getTag().getString("recipe");
        // 空串会被解析成 "minecraft:"
        return recipeId.isEmpty() ? null : ResourceLocation.tryParse(recipeId);
    }

    /** 把配方写进样板物品的 {@code recipe} 标签并缓存到该槽；{@code recipeId} 为空或查无此配方时清除。槽里没有样板时不做任何事。 */
    public void setSlotRecipeId(int slot, @Nullable ResourceLocation recipeId) {
        if (slot < 0 || slot >= getMaxPatternCount()) return;
        var stack = getPatternInventory().getStackInSlot(slot);
        if (stack.isEmpty()) return;
        var recipe = recipeId == null ? null : RecipeBuilder.get(recipeId);
        if (recipe == null) stack.getOrCreateTag().remove("recipe");
        else stack.getOrCreateTag().putString("recipe", recipe.id.toString());
        getInternalInventory()[slot].setRecipe(recipe);
        // 改样板物品的 NBT 不会触发物品栏回调，要自己标记存盘
        onChanged();
    }

    @Override
    protected boolean supportsSlotConfig() {
        return true;
    }

    /** 单槽配置：物品输入、流体输入、电路、记录的配方，各占一个面板区块。 */
    @Override
    protected void buildSlotConfig(UIElement column, int index) {
        var slot = getInternalInventory()[index];

        var items = MEPatternPartUI.section(column, ITEM_SPECIAL);
        MEPatternPartUI.slotRows(items, slot.lockableInventory.getSlots(),
                // 样板被虚拟输入锁定时只读（锁定状态由服务端判定下发）
                i -> MEPatternPartUI.missingVirtualInputOverlay(ItemSlot.of(slot.lockableInventory, i).disabled(slot::isLock, ITEM_LOCKED),
                        () -> slot.isMissingVirtualItemSlot(i)));

        var fluids = MEPatternPartUI.section(column, FLUID_SPECIAL);
        MEPatternPartUI.fluidSlots(fluids, slot.shareTank.getStorages(),
                (i, tank) -> MEPatternPartUI.missingVirtualInputOverlay(tank, () -> slot.isMissingVirtualFluidSlot(i)));

        var circuitStorage = slot.circuitInventory.storage;
        MEPatternPartUI.section(column, CIRCUIT_SPECIAL).addChild(MEPatternPartUI.circuitRow(
                MEPatternPartUI.missingVirtualInputOverlay(MEPatternPartUI.readOnlyCircuitSlot(circuitStorage), slot::isMissingVirtualCircuit),
                () -> MEPatternPartUI.circuitOf(circuitStorage.getStackInSlot(0)),
                circuit -> circuitStorage.setStackInSlot(0, MEPatternPartUI.circuitStack(circuit))));

        var recipe = MEPatternPartUI.section(column, RECIPE_SPECIAL);
        var recipeField = new TextField(0, () -> {
            var recipeId = getSlotRecipeId(index);
            return recipeId == null ? "" : recipeId.toString();
        }, text -> setSlotRecipeId(index, ResourceLocation.tryParse(text))).setRightClickClear(true);
        recipeField.layout(l -> l.flexGrow(1));
        recipeField.setHoverTooltips(Component.translatable(ADD_RECIPE_MSG));
        // 样板槽为空时整行只读（没有样板物品可写配方）：查看、输入、清除三个控件一起叠斜纹，悬停说明原因
        recipe.addChild(UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.width(recipe.getContentWidth()).gapAll(UISizes.GAP).alignCenter())
                .disabled(() -> getPatternInventory().getStackInSlot(index).isEmpty(), PATTERN_REQUIRED).addChildren(
                        Button.glyph("?")
                                .bindTooltip(() -> Component.translatable(getSlotRecipeId(index) != null ? VIEW_RECIPE : NO_RECIPE))
                                .setOnClientClick(() -> {
                                    var recipeId = getSlotRecipeId(index);
                                    if (recipeId != null) displayEmiRecipe(recipeId);
                                }),
                        recipeField,
                        Button.glyph("×").setVariant(UITheme.ButtonVariant.DANGER)
                                .setOnServerClick(() -> {
                                    if (getPatternInventory().getStackInSlot(index).isEmpty()) return;
                                    slot.setRecipe(null);
                                    onChanged();
                                })
                                .setHoverTooltips(CLEAR_RECIPE_SLOT)));
    }

    @OnlyIn(Dist.CLIENT)
    private static void displayEmiRecipe(ResourceLocation recipeId) {
        // 先按 id 在 EMI 里找这条配方
        var emiRecipe = EmiApi.getRecipeManager().getRecipe(recipeId);
        if (emiRecipe == null) {
            for (var recipe : GTORecipes.EMI_RECIPES) {
                if (recipeId.equals(recipe.getId())) {
                    emiRecipe = recipe;
                    break;
                }
            }
        }
        if (emiRecipe != null) {
            EmiApi.displayRecipe(emiRecipe);
            return;
        }
        // EMI 里没有这条配方（如所在类别不在 EMI 展示）：退而打开它第一个物品产物的全部配方
        var definition = RecipeBuilder.get(recipeId);
        if (definition != null) {
            for (var output : definition.itemOutputs) {
                var stack = output.inner.getInnerItemStack();
                if (!stack.isEmpty()) {
                    EmiApi.displayRecipes(EmiStack.of(stack));
                    return;
                }
            }
        }
        var player = Minecraft.getInstance().player;
        if (player != null) player.displayClientMessage(Component.translatable(RECIPE_NOT_IN_EMI, recipeId.toString()), true);
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        if (isFormed()) {
            IMultiController controller = getController();
            Collection<GTRecipeType> availableRecipeTypes = controller instanceof IRecipeLogicMachine recipeMachine ?
                    Arrays.asList(recipeMachine.getAvailableRecipeTypes()) : Collections.emptyList();
            return PatternContainerGroupHelper.forPatternBuffer(
                    controller.self(), this, getCustomName(), recipeType, availableRecipeTypes);
        } else {
            if (!getCustomName().isEmpty()) {
                return new PatternContainerGroup(AEItemKey.of(GTAEMachines.ME_PATTERN_BUFFER.asItem()), Component.literal(getCustomName()), Collections.emptyList());
            } else {
                return new PatternContainerGroup(AEItemKey.of(GTAEMachines.ME_PATTERN_BUFFER.asItem()), GTAEMachines.ME_PATTERN_BUFFER.get().getDefinition().asItem().getDescription(), Collections.emptyList());
            }
        }
    }

    @Override
    protected GTRecipeType groupRecipeType() {
        return recipeType;
    }

    @Override
    public Component gto$getTerminalGroupSearchName() {
        if (!isFormed()) {
            return getTerminalGroup().name();
        }
        if (!getCustomName().isEmpty() && !getCustomName().startsWith("+")) {
            return Component.literal(getCustomName());
        }
        IMultiController controller = getController();
        Collection<GTRecipeType> availableRecipeTypes = controller instanceof IRecipeLogicMachine recipeMachine ?
                Arrays.asList(recipeMachine.getAvailableRecipeTypes()) : Collections.emptyList();
        String extraSuffix = getCustomName().startsWith("+") ? getCustomName().substring(1).strip() : "";
        return PatternContainerGroupHelper.getSearchName(controller.self(), extraSuffix, recipeType, availableRecipeTypes);
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        this.configuratorPanel = configuratorPanel;
        configuratorPanel.attachConfigurators(new ButtonConfigurator(WidgetIcons.REFUND, this::refundAll).setTooltips(List.of(Component.translatable("gui.gtceu.refund_all.desc"))));
        configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventorySimulated.storage));
        configuratorPanel.attachConfigurators(new FancyInvConfigurator(shareInventory.storage, Component.translatable("gui.gtceu.share_inventory.title")).setTooltips(List.of(Component.translatable("gui.gtceu.share_inventory.desc.0"), Component.translatable("gui.gtceu.share_inventory.desc.1"))));
        configuratorPanel.attachConfigurators(new FancyTankConfigurator(shareTank.getStorages(), Component.translatable("gui.gtceu.share_tank.title")).setTooltips(List.of(Component.translatable("gui.gtceu.share_tank.desc.0"), Component.translatable("gui.gtceu.share_inventory.desc.1"))));
        super.attachConfigurators(configuratorPanel);
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        super.saveToItem(tag);
        tag.put("si", shareInventory.storage.serializeNBT());
        ListTag tanks = new ListTag();
        for (var tank : shareTank.getStorages()) {
            tanks.add(tank.serializeNBT());
        }
        tag.put("st", tanks);
        tag.put("ci", circuitInventorySimulated.storage.serializeNBT());
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        super.loadFromItem(tag);
        shareInventory.storage.deserializeNBT(tag.get("si"));
        ListTag tanks = tag.getList("st", Tag.TAG_COMPOUND);
        for (int i = 0; i < tanks.size(); i++) {
            shareTank.getStorages()[i].deserializeNBT(tanks.getCompound(i));
        }
        circuitInventorySimulated.storage.deserializeNBT(tag.get("ci"));
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        dataStick.getOrCreateTag().putIntArray("pos", new int[] { getPos().getX(), getPos().getY(), getPos().getZ() });
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendWailaTooltip(CompoundTag data, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (!data.getBoolean("formed")) return;
        var proxies = data.getInt("proxies");
        if (proxies > 0) iTooltip.add(Component.translatable("gtceu.top.proxies_bound", data.getInt("proxies")).withStyle(TooltipHelper.RAINBOW_HSL_SLOW));
        readBufferTag(iTooltip, data);
    }

    @Override
    public void appendWailaData(CompoundTag data, BlockAccessor blockAccessor) {
        if (!isFormed()) {
            data.putBoolean("formed", false);
            return;
        }
        data.putBoolean("formed", true);
        var proxies = getProxies().size();
        if (proxies > 0) data.putInt("proxies", proxies);
        writeBufferTag(data, this);
    }

    @Override
    public void clearMachineRecipeCache() {
        var slots = getInternalInventory();
        for (int i = 0; i < slots.length; i++) {
            slots[i].setRecipe(null);
            reloadPatternRecipe(i);
        }
        getControllers().forEach(controller -> {
            if (controller instanceof IRecipeLogicMachine rlm) {
                rlm.getRecipeLogic().updateTickSubscription();
            }
        });
    }

    @Override
    public void clearPatternRecipeCache() {
        for (var pattern : getInternalPatternInventory()) {
            pattern.getOrCreateTag().remove("recipe");
        }
        ICraftingProvider.requestUpdate(getMainNode());
        clearMachineRecipeCache();
    }

    static void writeBufferTag(CompoundTag data, MEPatternBufferPartMachine buffer) {
        var items = new AEKeyMap<AEItemKey>();
        var fluids = new AEKeyMap<AEFluidKey>();
        for (InternalSlot slot : buffer.getInternalInventory()) {
            slot.itemInventory.fastForEach(items::insert);
            slot.fluidInventory.fastForEach(fluids::insert);
        }

        ListTag itemsTag = new ListTag();
        for (var entry : items) {
            var ct = entry.getKey().toTag();
            ct.putLong("real", entry.getLongValue());
            itemsTag.add(ct);
        }
        if (!itemsTag.isEmpty()) data.put("items", itemsTag);

        ListTag fluidsTag = new ListTag();
        for (var entry : fluids) {
            var ct = entry.getKey().toTag();
            ct.putLong("real", entry.getLongValue());
            fluidsTag.add(ct);
        }
        if (!fluidsTag.isEmpty()) data.put("fluids", fluidsTag);
    }

    static void readBufferTag(ITooltip iTooltip, CompoundTag data) {
        IElementHelper helper = iTooltip.getElementHelper();

        ListTag itemsTag = data.getList("items", Tag.TAG_COMPOUND);
        for (Tag t : itemsTag) {
            if (!(t instanceof CompoundTag ct)) continue;
            var stack = AEItemKey.fromTag(ct);
            if (stack == null) continue;
            var amount = ct.getLong("real");
            if (amount > 0) {
                iTooltip.add(helper.smallItem(stack.getReadOnlyStack()));
                Component text = Component.literal(" ")
                        .append(Component.literal(String.valueOf(amount)).withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("× ").withStyle(ChatFormatting.WHITE))
                        .append(stack.getDisplayName().copy().withStyle(ChatFormatting.GOLD));
                iTooltip.append(text);
            }
        }
        ListTag fluidsTag = data.getList("fluids", Tag.TAG_COMPOUND);
        for (Tag t : fluidsTag) {
            if (!(t instanceof CompoundTag ct)) continue;
            var stack = AEFluidKey.fromTag(ct);
            if (stack == null) continue;
            var amount = ct.getLong("real");
            if (amount > 0) {
                iTooltip.add(GTElementHelper.smallFluid(JadeFluidObject.of(stack.getFluid())));
                Component text = Component.literal(" ")
                        .append(Component.literal(FormattingUtil.formatBuckets(amount)))
                        .withStyle(ChatFormatting.DARK_PURPLE)
                        .append(Component.literal(" ").withStyle(ChatFormatting.WHITE))
                        .append(stack.getDisplayName().copy().withStyle(ChatFormatting.DARK_AQUA));
                iTooltip.append(text);
            }
        }
    }

    public static final class InternalSlot extends AbstractRecipeInternalSlot implements IFieldDataHolder {

        @SaveToDisk(listener = "setRecipe")
        public GTRecipeDefinition recipe;
        public final MEPatternBufferPartMachine machine;
        public final int index;
        private final InputSink inputSink;
        public final IntLongMap ingredientMap = new IntLongMap();
        @SaveToDisk
        public final AEKeyMap<AEItemKey> itemInventory = new AEKeyMap<>();
        @SaveToDisk
        public final AEKeyMap<AEFluidKey> fluidInventory = new AEKeyMap<>();

        @SaveToDisk(skipWhen = "isLock")
        public final NotifiableNotConsumableItemHandler shareInventory;
        @SaveToDisk(skipWhen = "isLock")
        public final NotifiableNotConsumableFluidHandler shareTank;
        @SaveToDisk(skipWhen = "isLock")
        public final NotifiableItemStackHandler circuitInventory;
        final LockableItemStackHandler lockableInventory;
        private final MEVirtualInputAvailability virtualInputAvailability = new MEVirtualInputAvailability();
        @Getter
        @SaveToDisk(defaultValue = "false", listener = "setLock")
        private boolean lock;
        @Setter
        private boolean shouldLockRecipe = true;
        private final LazyFieldDataManager fieldDataManager = new LazyFieldDataManager(this);

        private InternalSlot(MEPatternBufferPartMachine machine, int index) {
            this.machine = machine;
            this.index = index;
            this.shareInventory = machine.createShareInventory();
            this.shareTank = new NotifiableNotConsumableFluidHandler(machine, 9, 64000);
            this.circuitInventory = CircuitHandler.create(machine);
            this.inputSink = new InputSink(this);
            this.lockableInventory = new LockableItemStackHandler(shareInventory.storage);
        }

        private boolean isLock(NotifiableItemStackHandler circuitInventory) {
            return lock;
        }

        private boolean isLock(NotifiableNotConsumableFluidHandler shareTank) {
            return lock;
        }

        private boolean isLock(NotifiableNotConsumableItemHandler shareInventory) {
            return lock;
        }

        public void verify() {
            if (recipe != null && !machine.isRecipeTypeUsable(recipe.recipeType)) {
                setRecipe(null);
            }
        }

        public void setLock(boolean lock) {
            if (this.lock) {
                circuitInventory.storage.setStackInSlot(0, ItemStack.EMPTY);
                for (int i = 0; i < 9; i++) {
                    shareInventory.setStackInSlot(i, ItemStack.EMPTY);
                }
                for (var tank : shareTank.getStorages()) {
                    tank.setFluid(FluidStack.EMPTY);
                }
            }
            if (!lock) virtualInputAvailability.clear();
            this.lock = lock;
            lockableInventory.setLock(lock);
        }

        public boolean isMissingVirtualItemSlot(int slot) {
            return virtualInputAvailability.isItemMissing(slot);
        }

        public boolean isMissingVirtualFluidSlot(int slot) {
            return virtualInputAvailability.isFluidMissing(slot);
        }

        public boolean isMissingVirtualCircuit() {
            return virtualInputAvailability.isCircuitMissing();
        }

        public void setRecipe(@Nullable GTRecipeDefinition recipe) {
            if (!shouldLockRecipe) return;
            if (recipe != null && recipe.registered && (machine.recipeType == null || GTRecipeType.available(recipe.recipeType, machine.recipeType))) {
                this.recipe = recipe;
                machine.caches[index] = true;
            } else {
                this.recipe = null;
                machine.caches[index] = false;
            }
        }

        public boolean isEmpty() {
            return itemInventory.isEmpty() && fluidInventory.isEmpty();
        }

        private void refund() {
            var network = machine.getMainNode().getGrid();
            if (network != null) {
                MEStorage networkInv = network.getStorageService().getInventory();
                var energy = network.getEnergyService();
                for (var it = itemInventory.iterator(); it.hasNext();) {
                    var entry = it.next();

                    var count = entry.getLongValue();
                    if (count == 0) {
                        it.remove();
                        continue;
                    }
                    var key = entry.getKey();
                    if (key == null) continue;
                    long inserted = StorageHelper.poweredInsert(energy, networkInv, key, count, machine.getActionSourceField());
                    if (inserted > 0) {
                        count -= inserted;
                        if (count == 0) it.remove();
                        else entry.setValue(count);
                    }
                }
                for (var it = fluidInventory.iterator(); it.hasNext();) {
                    var entry = it.next();
                    var amount = entry.getLongValue();
                    if (amount == 0) {
                        it.remove();
                        continue;
                    }
                    var key = entry.getKey();
                    if (key == null) continue;
                    long inserted = StorageHelper.poweredInsert(energy, networkInv, key, amount, machine.getActionSourceField());
                    if (inserted > 0) {
                        amount -= inserted;
                        if (amount == 0) it.remove();
                        else entry.setValue(amount);
                    }
                }
                markContentsChanged();
            }
        }

        @Override
        public void onPatternChange() {
            setRecipe(null);
            refund();
        }

        @Override
        public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
            patternDetails.pushInputsToExternalInventory(inputHolder, inputSink);
            markContentsChanged();
            return true;
        }

        public boolean handleItemInternal(List<Content<ItemIngredient>> items, boolean simulate) {
            boolean changed = false;
            for (var it = items.iterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }
                for (var it2 = itemInventory.iterator(); it2.hasNext();) {
                    var entry = it2.next();
                    if (!ingredient.inner.testAeKay(entry.getKey())) continue;
                    var count = entry.getLongValue();
                    long extracted = Math.min(count, ingredient.amount);
                    if (extracted > 0) {
                        if (!simulate) {
                            changed = true;
                            count -= extracted;
                            if (count < 1) it2.remove();
                            else entry.setValue(count);
                        }
                        ingredient.shrink(extracted);
                        if (ingredient.amount < 1) {
                            it.remove();
                            break;
                        }
                    }
                }
            }
            if (changed) {
                markContentsChanged();
            }
            return items.isEmpty();
        }

        public boolean handleFluidInternal(List<Content<FluidIngredient>> fluids, boolean simulate) {
            boolean changed = false;
            for (var it = fluids.iterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }
                for (var it2 = fluidInventory.iterator(); it2.hasNext();) {
                    var entry = it2.next();
                    if (!ingredient.inner.testAeKay(entry.getKey())) continue;
                    var count = entry.getLongValue();
                    long extracted = Math.min(count, ingredient.amount);
                    if (extracted > 0) {
                        if (!simulate) {
                            changed = true;
                            count -= extracted;
                            if (count < 1) it2.remove();
                            else entry.setValue(count);
                        }
                        ingredient.shrink(extracted);
                        if (ingredient.amount < 1) {
                            it.remove();
                            break;
                        }
                    }
                }
            }
            if (changed) {
                markContentsChanged();
            }
            return fluids.isEmpty();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.get("recipe") instanceof ByteArrayTag byteArrayTag) setRecipe(GTRecipeDefinition.DATA_CODEC.decode(Data.readData(byteArrayTag.getAsByteArray())));
            ListTag items = tag.getList("inventory", Tag.TAG_COMPOUND);
            for (Tag t : items) {
                if (!(t instanceof CompoundTag ct)) continue;
                var stack = AEItemKey.fromTag(ct);
                if (stack == null) continue;
                var amount = ct.getLong("real");
                if (amount > 0) {
                    itemInventory.set(stack, amount);
                }
            }
            ListTag fluids = tag.getList("fluidInventory", Tag.TAG_COMPOUND);
            for (Tag t : fluids) {
                if (!(t instanceof CompoundTag ct)) continue;
                var stack = AEFluidKey.fromTag(ct);
                if (stack == null) continue;
                var amount = ct.getLong("real");
                if (amount > 0) {
                    fluidInventory.set(stack, amount);
                }
            }
            shareInventory.storage.deserializeNBT(tag.tags.get("inv"));
            if (tag.tags.get("tank") instanceof ListTag tanks) {
                for (int i = 0; i < tanks.size(); i++) {
                    var t = tanks.getCompound(i);
                    if (t.isEmpty()) continue;
                    var tank = shareTank.getStorages()[i];
                    tank.deserializeNBT(t);
                }
            }
            var c = tag.getInt("c");
            if (c > 0) circuitInventory.storage.setStackInSlot(0, IntCircuitBehaviour.stack(c));
            setLock(tag.getBoolean("l"));
        }

        @Override
        public void writeBuffer(LogicalSide logicalSide, FriendlyByteBuf friendlyByteBuf) {
            // 无同步，不实现
        }

        @Override
        public void readBuffer(LogicalSide logicalSide, FriendlyByteBuf friendlyByteBuf) {
            // 无同步，不实现
        }

        @Override
        public Data writeData() {
            return fieldDataManager.get().writeToData();
        }

        @Override
        public void readData(Data data, int dataVersion) {
            if (data.isNull()) return;
            if (dataVersion < 2) {
                var nbt = DataCodecs.TAG_CODEC.decode(data, dataVersion);
                if (nbt instanceof CompoundTag compoundTag) {
                    deserializeNBT(compoundTag);
                    return;
                }
            }
            fieldDataManager.get().readFromData(data, dataVersion);
        }

        @Override
        public FieldDataManager getFieldDataManager() {
            return fieldDataManager.get();
        }
    }

    private record InputSink(InternalSlot slot) implements IPatternDetails.PatternInputSink {

        @Override
        public void pushInput(AEKey key, long amount) {
            if (amount < 1) return;
            if (key instanceof AEItemKey itemKey) {
                if (MEPatternVirtualInputHelper.isVirtualProvider(itemKey)) return;
                slot.itemInventory.insert(itemKey, amount);
            } else if (key instanceof AEFluidKey fluidKey) {
                slot.fluidInventory.insert(fluidKey, amount);
            }
        }
    }
}
