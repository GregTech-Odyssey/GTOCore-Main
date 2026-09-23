package com.gtocore.mixin.ae2.menu;

import com.gtocore.api.ae2.pattern.IEncodingLogic;
import com.gtocore.client.Message;
import com.gtocore.common.machine.multiblock.electric.SuperMolecularAssemblerMachine;
import com.gtocore.common.machine.multiblock.part.ae.MECraftPatternPartMachine;
import com.gtocore.common.machine.multiblock.part.ae.MEPartInv;
import com.gtocore.integration.ae.hooks.IExtendedPatternContainer;
import com.gtocore.integration.ae.hooks.IExtendedPatternEncodingTerm;

import com.gtolib.api.ae2.IPatterEncodingTermMenu;
import com.gtolib.api.ae2.pattern.PatternUtils;
import com.gtolib.utils.ClientUtil;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.storage.ITerminalHost;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AEPatternDecoder;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.helpers.IMenuCraftingPacket;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.PatternEncodingLogic;
import appeng.util.ConfigInventory;
import appeng.util.inv.AppEngInternalInventory;

import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(PatternEncodingTermMenu.class)
public abstract class PatternEncodingTermMenuMixin extends MEStorageMenu implements IMenuCraftingPacket, IPatterEncodingTermMenu, IExtendedPatternEncodingTerm.Menu {

    @Unique
    private static final String TITLE_ENABLED = "gtocore.pattern.recipeInfoButton.title.enabled";
    @Unique
    private static final String TITLE_DISABLED = "gtocore.pattern.recipeInfoButton.title.disabled";
    @Unique
    private static final String CLICK_TO_ENABLE = "gtocore.pattern.recipeInfoButton.clickToEnable";
    @Unique
    private static final String CLICK_TO_DISABLE = "gtocore.pattern.recipeInfoButton.clickToDisable";
    @Unique
    private static final String CLICK_TO_CLEAR = "gtocore.pattern.recipeInfoButton.clickToClear";
    // 单个目的地最多同步的样板产物数，防止大型装配矩阵集群撑爆发包
    @Unique
    private static final int GTO$MAX_OUTPUTS_PER_DESTINATION = 1024;

    @Shadow(remap = false)
    @Final
    private ConfigInventory encodedInputsInv;
    @Shadow(remap = false)
    @Final
    private ConfigInventory encodedOutputsInv;
    @Shadow(remap = false)
    @Final
    private PatternEncodingLogic encodingLogic;

    @Unique
    @GuiSync(122)
    public boolean gtolib$extraInfoEnabled = true;
    @Unique
    @GuiSync(120)
    public String gtocore$recipe = "";
    @Unique
    private GTRecipeType gto$lastRecipeType = null;
    @Unique
    private boolean gto$isCraft = false;
    @Unique
    private List<IExtendedPatternContainer> gto$currentContainers = null;
    @Unique
    private ItemStack gto$patternStack;
    // 本次编码样板的主产物，用于标记「已有相同样板」的目的地
    @Unique
    private Object gto$primaryOutput;
    // 每次下发目的地列表加一；客户端发送样板、请求产物时带回，用来拒绝过期请求
    @Unique
    private int gto$destinationRequestId;
    // 已经下发过产物的目的地列表编号，同一份列表只响应一次产物请求
    @Unique
    private int gto$outputsSentRequestId = -1;
    @Unique
    private UUID gtocore$UUID;

    protected PatternEncodingTermMenuMixin(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host);
    }

    @Unique
    private IEncodingLogic gtolib$logic() {
        return ((IEncodingLogic) encodingLogic);
    }

    @Override
    public void gtolib$addRecipe(String id) {
        if (isClientSide()) {
            sendClientAction("addRecipe", id);
        } else {
            gtolib$logic().gtocore$setRecipe(id);
        }
        gto$lastRecipeType = GTRegistries.RECIPE_TYPES.get(RLUtils.parse(id.split("/")[0]));
    }

    @Override
    public void gtolib$addUUID(UUID id) {
        if (isClientSide()) {
            sendClientAction("addUUID", id);
        } else gtocore$UUID = id;
    }

    @Override
    public void gtolib$clickRecipeInfo() {
        if (isClientSide()) {
            sendClientAction("clickRecipeInfo");
            return;
        }
        if (this.gtolib$extraInfoEnabled && !gtolib$logic().gtocore$getRecipe().isEmpty()) {
            gtolib$logic().gtocore$clearExtraRecipeInfo();
            return;
        }
        gtolib$logic().gtocore$clearExtraRecipeInfo();
        this.gtolib$extraInfoEnabled = !this.gtolib$extraInfoEnabled;
    }

    @Override
    public Component gtolib$getRecipeInfoTooltip() {
        var title = Component.empty();
        title.append(this.gtolib$extraInfoEnabled ? Component.translatable(TITLE_ENABLED) : Component.translatable(TITLE_DISABLED));
        title.append("\n");
        if (!this.gtolib$extraInfoEnabled) {
            return title.append(Component.translatable(CLICK_TO_ENABLE));
        }
        if (!gtocore$recipe.isEmpty()) {
            var tooltip = Component.empty();
            tooltip.append(Component.translatable("gtocore.pattern.recipe")).append("\n");
            var key = RLUtils.parse(gtocore$recipe.split("/")[0]).toLanguageKey();
            tooltip.append(Component.translatable("gtocore.pattern.type", Component.translatable(key))).append("\n");
            return title.append(tooltip.append(Component.translatable(CLICK_TO_CLEAR)));
        } else {
            return title.append(Component.translatable(CLICK_TO_DISABLE));
        }
    }

    @Inject(method = "encodeProcessingPattern", at = @At("RETURN"), remap = false)
    private void encodeProcessingPatternHook(CallbackInfoReturnable<ItemStack> cir) {
        if (gtolib$extraInfoEnabled) {
            if (!gtolib$logic().gtocore$getRecipe().isEmpty()) {
                cir.getReturnValue().getOrCreateTag().putString("recipe", gtolib$logic().gtocore$getRecipe());
            }
        }
        if (gtocore$UUID != null) {
            cir.getReturnValue().getOrCreateTag().putUUID("uuid", gtocore$UUID);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPatternTerminalMenuHost;Z)V",
            at = @At("TAIL"),
            remap = false)
    private void initHooks(MenuType<?> menuType, int id, Inventory ip, IPatternTerminalMenuHost host, boolean bindInventory, CallbackInfo ci) {
        registerClientAction("modifyPatter", Integer.class, this::gtolib$modifyPatter);
        registerClientAction("clearSecOutput", this::gtolib$clearSecOutput);
        registerClientAction("addRecipe", String.class, this::gtolib$addRecipe);
        registerClientAction("clickRecipeInfo", this::gtolib$clickRecipeInfo);
        registerClientAction("addUUID", UUID.class, this::gtolib$addUUID);
        registerClientAction("sendPattern", int[].class, this::gto$onSendPatternAction);
        registerClientAction("requestPatternOutputs", Integer.class, requestId -> {
            if (requestId != null) gtolib$requestPatternOutputs(requestId);
        });
        registerClientAction("sendPatternRequest", String.class, this::gtolib$sendEncodeRequest);
    }

    @Override
    public void gtolib$modifyPatter(Integer data) {
        if (isClientSide()) {
            sendClientAction("modifyPatter", data);
        } else {
            // modify
            PatternUtils.mulPatternEncodingArea(encodedInputsInv, encodedOutputsInv, data);
        }
    }

    @Unique
    public void gtolib$clearSecOutput() {
        if (isClientSide()) {
            sendClientAction("clearSecOutput");
        } else {
            for (int i = 1; i <= 8; i++) {
                encodedOutputsInv.setStack(i, null);
            }
        }
    }

    @Inject(method = "encode", at = @At(value = "INVOKE", target = "Lappeng/menu/me/items/PatternEncodingTermMenu;sendClientAction(Ljava/lang/String;)V"), remap = false)
    private void encode(CallbackInfo ci) {
        gtolib$addUUID(ClientUtil.getUUID());
    }

    @Inject(method = "encodePattern", at = @At(value = "RETURN"), remap = false)
    private void onEncodeSucceeded(CallbackInfoReturnable<ItemStack> cir) {
        var stack = cir.getReturnValue();
        if (stack == null || stack.isEmpty()) return;
        gto$isCraft = !(stack.getItem() instanceof ProcessingPatternItem);
    }

    @Inject(method = "broadcastChanges", at = @At("TAIL"))
    public void broadcastChanges(CallbackInfo ci) {
        if (isServerSide()) {
            this.gtocore$recipe = gtolib$logic().gtocore$getRecipe();
        }
    }

    @Shadow(remap = false)
    @Nullable
    protected abstract ItemStack encodePattern();

    @Unique
    private List<IExtendedPatternContainer> gto$getPatternContainers(String recipeLocName) {
        var gridNode = getActionHost().getActionableNode();
        if (gridNode == null) {
            return Collections.emptyList();
        }
        var grid = gridNode.getGrid();
        if (grid == null) {
            return Collections.emptyList();
        }
        var stack = gto$patternStack;
        if (stack == null) return Collections.emptyList();
        ArrayList<IExtendedPatternContainer> machines = new ArrayList<>(grid.size() / 2 + 1);
        for (var machineClass : grid.getMachineClasses()) {
            if (IExtendedPatternContainer.class.isAssignableFrom(machineClass)) {
                machines.addAll((Collection<? extends IExtendedPatternContainer>) grid.getActiveMachines(machineClass));
            }
        }
        var thisPatternDetails = AEPatternDecoder.INSTANCE.decodePattern(stack, getPlayer().level(), false);
        if (thisPatternDetails == null) return Collections.emptyList();
        gto$primaryOutput = thisPatternDetails.getPrimaryOutput().what();
        Set<Object> sameCluster = new OpenCacheHashSet<>();

        machines.removeIf(container -> gto$shouldRemoveContainer(container, stack, sameCluster));
        var containerComparator = (gto$isCraft ? gto$craftFirst(stack) : gto$recipeFirst(gto$lastRecipeType, recipeLocName, stack)).reversed();

        machines.sort(containerComparator);
        return machines;
    }

    @Unique
    private static Comparator<IExtendedPatternContainer> gto$craftFirst(ItemStack patternStack) {
        return Comparator.comparing((IExtendedPatternContainer p) -> gto$canAddPattern(p, patternStack))
                .thenComparing(IExtendedPatternContainer::gto$isCraftingContainer);
    }

    @Unique
    private static Comparator<IExtendedPatternContainer> gto$recipeFirst(GTRecipeType recipeType, String recipeLocName, ItemStack patternStack) {
        if (recipeType == null) {
            return Comparator.comparing((IExtendedPatternContainer p) -> gto$canAddPattern(p, patternStack));
        }
        var c = Comparator.comparing((IExtendedPatternContainer p) -> gto$canAddPattern(p, patternStack))
                .thenComparing((IExtendedPatternContainer p) -> p.gto$supportsRecipeType(recipeType));
        if (recipeLocName != null && !recipeLocName.isEmpty()) {
            c = c.thenComparing((IExtendedPatternContainer p) -> gto$matchesRecipeName(p, recipeLocName));
        }
        return c;
    }

    @Unique
    private static boolean gto$matchesRecipeName(IExtendedPatternContainer container, String recipeType) {
        var recipeNames = new ObjectLinkedOpenHashSet<String>();
        recipeNames.add(recipeType.toLowerCase(Locale.ROOT));
        var name = container.gto$getTerminalGroupSearchName().getString().toLowerCase(Locale.ROOT);
        for (var recipeName : recipeNames) {
            if (!recipeName.isEmpty() && name.contains(recipeName)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean gto$canAddPattern(IExtendedPatternContainer container, ItemStack patternStack) {
        return container.getTerminalPatternInventory().simulateAdd(patternStack).isEmpty();
    }

    /**
     * 只去掉终端里不可见的目的地，以及集群（装配矩阵 / 超分子装配器）中除代表成员外的其余成员；
     * 已有相同样板的目的地不再隐藏，改由 {@link #gto$hasSamePattern} 标记后在列表中置灰。
     */
    @Unique
    private boolean gto$shouldRemoveContainer(IExtendedPatternContainer container, ItemStack patternStack,
                                              Set<Object> sameCluster) {
        if (!container.isVisibleInTerminal()) {
            return true;
        }

        var patternInv = container.getTerminalPatternInventory();
        var hasSpace = gto$canAddPattern(container, patternStack);
        if (patternInv instanceof AppEngInternalInventory aeInv &&
                aeInv.getHost() instanceof TileAssemblerMatrixPattern matrixPattern) {
            var matrix = matrixPattern.getCluster();
            if (matrix == null) {
                return false;
            }
            return gto$shouldRemoveClusterMember(matrix, gto$getMatrixContainers(matrixPattern), patternStack, sameCluster, hasSpace);
        }
        if (patternInv instanceof MEPartInv inv &&
                inv.getMachine() instanceof MECraftPatternPartMachine mecppm &&
                mecppm.getController() instanceof SuperMolecularAssemblerMachine smaMachine) {
            return gto$shouldRemoveClusterMember(smaMachine, gto$getSmaContainers(smaMachine), patternStack, sameCluster, hasSpace);
        }
        return false;
    }

    /**
     * 集群在列表中只保留一个代表成员：优先选有空位的成员，其余成员去掉。
     */
    @Unique
    private static boolean gto$shouldRemoveClusterMember(Object cluster, List<IExtendedPatternContainer> clusterContainers,
                                                         ItemStack patternStack, Set<Object> sameCluster, boolean hasSpace) {
        if (sameCluster.contains(cluster)) {
            return true;
        }
        var clusterHasSpace = clusterContainers.stream().anyMatch(p -> gto$canAddPattern(p, patternStack));
        if (!hasSpace && clusterHasSpace) {
            return true;
        }
        sameCluster.add(cluster);
        return false;
    }

    @Unique
    private static List<IExtendedPatternContainer> gto$getMatrixContainers(TileAssemblerMatrixPattern matrixPattern) {
        var matrix = matrixPattern.getCluster();
        if (matrix == null) return Collections.emptyList();
        return matrix.getPatterns().stream()
                .filter(IExtendedPatternContainer.class::isInstance)
                .map(IExtendedPatternContainer.class::cast)
                .toList();
    }

    @Unique
    private static List<IExtendedPatternContainer> gto$getSmaContainers(SuperMolecularAssemblerMachine smaMachine) {
        return Arrays.stream(smaMachine.getParts())
                .filter(IExtendedPatternContainer.class::isInstance)
                .map(IExtendedPatternContainer.class::cast)
                .toList();
    }

    /**
     * 目的地（集群则为整个集群）是否已有与本次样板主产物相同的样板。
     */
    @Unique
    private static boolean gto$hasSamePattern(IExtendedPatternContainer container, Object primaryOutput, Level level) {
        var patternInv = container.getTerminalPatternInventory();
        List<IExtendedPatternContainer> members = null;
        if (patternInv instanceof AppEngInternalInventory aeInv &&
                aeInv.getHost() instanceof TileAssemblerMatrixPattern matrixPattern &&
                matrixPattern.getCluster() != null) {
            members = gto$getMatrixContainers(matrixPattern);
        } else if (patternInv instanceof MEPartInv inv &&
                inv.getMachine() instanceof MECraftPatternPartMachine mecppm &&
                mecppm.getController() instanceof SuperMolecularAssemblerMachine smaMachine) {
                    members = gto$getSmaContainers(smaMachine);
                }
        if (members == null) {
            return gto$containsPrimaryOutput(container, primaryOutput, level);
        }
        for (int i = 0, size = members.size(); i < size; i++) {
            if (gto$containsPrimaryOutput(members.get(i), primaryOutput, level)) return true;
        }
        return false;
    }

    @Unique
    private static boolean gto$containsPrimaryOutput(IExtendedPatternContainer container, Object primaryOutput,
                                                     Level level) {
        var patterns = container.gto$getAvailablePatterns(level);
        for (int i = 0, size = patterns.size(); i < size; i++) {
            if (patterns.get(i).getPrimaryOutput().what() == primaryOutput) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean gto$isFull(IExtendedPatternContainer container, ItemStack patternStack) {
        var patternInv = container.getTerminalPatternInventory();
        if (patternInv instanceof AppEngInternalInventory aeInv &&
                aeInv.getHost() instanceof TileAssemblerMatrixPattern matrixPattern) {
            var matrix = matrixPattern.getCluster();
            if (matrix == null) {
                return !gto$canAddPattern(container, patternStack);
            }
            return matrix.getPatterns().stream()
                    .filter(IExtendedPatternContainer.class::isInstance)
                    .map(IExtendedPatternContainer.class::cast)
                    .noneMatch(p -> gto$canAddPattern(p, patternStack));
        }
        if (patternInv instanceof MEPartInv inv &&
                inv.getMachine() instanceof MECraftPatternPartMachine mecppm &&
                mecppm.getController() instanceof SuperMolecularAssemblerMachine smaMachine) {
            return Arrays.stream(smaMachine.getParts())
                    .filter(IExtendedPatternContainer.class::isInstance)
                    .map(IExtendedPatternContainer.class::cast)
                    .noneMatch(p -> gto$canAddPattern(p, patternStack));
        }
        return !gto$canAddPattern(container, patternStack);
    }

    @Override
    public void gtolib$sendPattern(int requestId, int index) {
        if (isClientSide()) {
            sendClientAction("sendPattern", new int[] { requestId, index });
            return;
        }
        // 客户端点的是旧列表（期间又右键编码过一次）时拒绝，避免按下标发到新列表里的另一个目的地
        if (requestId != gto$destinationRequestId) return;
        var gridNode = getActionHost().getActionableNode();
        if (gridNode == null) {
            return;
        }
        var grid = gridNode.getGrid();
        if (grid == null) {
            return;
        }
        var containers = gto$currentContainers;
        if (containers == null) {
            return;
        }
        if (index < 0 || index >= containers.size()) {
            return;
        }
        var container = containers.get(index);
        if (container.isOutOfService()) return;

        var patternStack = gto$patternStack;
        if (patternStack == null) return;
        if (!gto$canAddPattern(container, patternStack)) return;

        var me = grid.getStorageService().getInventory();
        var blank = AEItemKey.of(AEItems.BLANK_PATTERN);
        var extractedBlankPatterns = me.extract(blank, 1, Actionable.MODULATE, getActionSource());
        if (extractedBlankPatterns == 0) {
            return;
        }
        // 如果没有成功插入样板则回滚
        var remainder = container.getTerminalPatternInventory().addItems(patternStack);
        if (!remainder.isEmpty()) {
            me.insert(blank, extractedBlankPatterns, Actionable.MODULATE, getActionSource());
        }
    }

    @Unique
    private void gto$onSendPatternAction(int[] args) {
        if (args == null || args.length != 2) return;
        gtolib$sendPattern(args[0], args[1]);
    }

    @Unique
    private void gtolib$sendEncodeRequest(String recipeLocName) {
        if (isClientSide()) {
            sendClientAction("sendPatternRequest", recipeLocName);
            return;
        }
        var patternStack = encodePattern();
        if (patternStack == null) return;
        gto$patternStack = patternStack;
        gto$currentContainers = gto$getPatternContainers(recipeLocName);
        gto$destinationRequestId++;
        if (gto$currentContainers.isEmpty()) return;
        var level = getPlayer().level();
        var destinations = new Message.PatternDestination[gto$currentContainers.size()];
        for (int i = 0; i < destinations.length; i++) {
            var container = gto$currentContainers.get(i);
            destinations[i] = new Message.PatternDestination(
                    container.gto$getMachineGroup(),
                    container.gto$getPlainCustomName(),
                    container.gto$getProviderIcon(),
                    gto$isFull(container, patternStack),
                    gto$hasSamePattern(container, gto$primaryOutput, level));
        }
        Message.sendPatternDestination((ServerPlayer) getPlayer(), gto$destinationRequestId, destinations);
    }

    /**
     * 客户端打开「匹配样板产物」时才请求各目的地已有样板的产物；同一份目的地列表只响应一次。
     */
    @Override
    public void gtolib$requestPatternOutputs(int requestId) {
        if (isClientSide()) {
            sendClientAction("requestPatternOutputs", requestId);
            return;
        }
        var containers = gto$currentContainers;
        if (containers == null || requestId != gto$destinationRequestId || requestId == gto$outputsSentRequestId) return;
        gto$outputsSentRequestId = requestId;
        var level = getPlayer().level();
        var outputs = new AEKey[containers.size()][];
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = gto$collectPatternOutputs(containers.get(i), level);
        }
        Message.sendPatternOutputs((ServerPlayer) getPlayer(), requestId, outputs);
    }

    /**
     * 收集目的地已有样板的全部产物（去重），集群（装配矩阵 / 超分子装配器）按整个集群收集，与列表中一行代表一个集群一致。
     */
    @Unique
    private static AEKey[] gto$collectPatternOutputs(IExtendedPatternContainer container, Level level) {
        var outputs = new AEKeyMap<AEKey>();
        var patternInv = container.getTerminalPatternInventory();
        if (patternInv instanceof AppEngInternalInventory aeInv &&
                aeInv.getHost() instanceof TileAssemblerMatrixPattern matrixPattern &&
                matrixPattern.getCluster() != null) {
            for (var member : matrixPattern.getCluster().getPatterns()) {
                if (member instanceof IExtendedPatternContainer c) gto$collectPatternOutputs(c, level, outputs);
            }
        } else if (patternInv instanceof MEPartInv inv &&
                inv.getMachine() instanceof MECraftPatternPartMachine mecppm &&
                mecppm.getController() instanceof SuperMolecularAssemblerMachine smaMachine) {
                    for (var part : smaMachine.getParts()) {
                        if (part instanceof IExtendedPatternContainer c) gto$collectPatternOutputs(c, level, outputs);
                    }
                } else {
                    gto$collectPatternOutputs(container, level, outputs);
                }
        return outputs.keySet().toArray(new AEKey[outputs.size()]);
    }

    @Unique
    private static void gto$collectPatternOutputs(IExtendedPatternContainer container, Level level, AEKeyMap<AEKey> outputs) {
        var patterns = container.gto$getAvailablePatterns(level);
        for (int i = 0, size = patterns.size(); i < size; i++) {
            if (outputs.size() >= GTO$MAX_OUTPUTS_PER_DESTINATION) return;
            for (var output : patterns.get(i).getOutputs()) {
                if (output != null) outputs.put(output.what(), 1);
            }
        }
    }

    @Override
    public void gtolib$sendEncodeRequest() {
        if (isClientSide()) {
            if (gto$lastRecipeType == null) {
                sendClientAction("sendPatternRequest", "");
            } else {
                sendClientAction("sendPatternRequest",
                        Component.translatable("gtceu." + gto$lastRecipeType.registryName.getPath()).getString());
            }
        }
    }
}
