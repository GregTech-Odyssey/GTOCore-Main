package com.gtocore.common.machine.noenergy.PlatformDeployment;

import com.gtocore.client.forge.ForgeClientEvent;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOTickTimeMonitors;
import com.gtocore.common.data.translation.GTOMachineTooltips;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.network.NetworkPack;
import com.gtolib.utils.ServerUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.forge.MenuItemAdapter;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ButtonGroup;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uipro.elements.Stepper;
import com.gregtechceu.gtceu.uipro.elements.SwitchedContent;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.util.holder.IntObjectHolder;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.AlignContent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gtocore.common.item.CoordinateCardBehavior.getStoredCoordinates;
import static com.gtocore.common.machine.noenergy.PlatformDeployment.PlatformCreators.PlatformCreationAsync;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlatformDeploymentMachine extends MetaMachine implements IFancyUIMachine, IMachineLife {

    @RegisterLanguage(cn = "材料与坐标卡", en = "Materials & Coordinate Cards")
    private static final String ITEMS = "gtocore.machine.industrial_platform_deployment_tools.ui.items";

    private static final NetworkPack HIGHLIGHT_REGION = NetworkPack.registerS2C("platformDeploymentMachineHighlight", (p, b) -> {
        var dimension = b.readResourceKey(Registries.DIMENSION);
        var start = b.readBlockPos();
        var end = b.readBlockPos();
        var color = b.readInt();
        var durationTicks = b.readInt();
        ForgeClientEvent.highlightRegion(dimension, start, end, color, durationTicks);
    });

    private static final NetworkPack STOP_HIGHLIGHT = NetworkPack.registerS2C("platformDeploymentMachineStopHighlight", (p, b) -> {
        var start = b.readBlockPos();
        var end = b.readBlockPos();
        ForgeClientEvent.stopHighlight(start, end);
    });

    public static void highlightRegion(ResourceKey<Level> dimension, BlockPos start, BlockPos end, int color, int durationTicks) {
        HIGHLIGHT_REGION.send(buf -> {
            buf.writeResourceKey(dimension);
            buf.writeBlockPos(start);
            buf.writeBlockPos(end);
            buf.writeInt(color);
            buf.writeInt(durationTicks);
        }, ServerUtils.getServer());
    }

    public static void stopHighlight(BlockPos start, BlockPos end) {
        STOP_HIGHLIGHT.send(buf -> {
            buf.writeBlockPos(start);
            buf.writeBlockPos(end);
        }, ServerUtils.getServer());
    }

    @SaveToDisk
    private final NotifiableInventory<AEItemKey> inventory;

    public PlatformDeploymentMachine(MetaMachineBlockEntity holder) {
        super(holder);
        inventory = NotifiableInventory.items(this, 27, IO.NONE, IO.BOTH);
        inventory.addChangedListener(this::examineMaterial);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        inventory.notifyListeners();
        posChanged();
    }

    @Override
    public void onMachineRemoved() {
        unloadingMaterial();
        clearInventory(inventory.storage);
    }

    /////////////////////////////////////
    // *********** 信息存储 *********** //
    /// //////////////////////////////////

    // 当前所处的步骤
    private int step = 0;

    // 总步骤数
    private static final int totalStep = 3;

    // 步骤编号常量定义
    private static final int Introduction = 0;        // 第零步：简介
    private static final int PresetSelection = 1;     // 第一步：选择预设
    private static final int ConfirmConsumables = 2;  // 第二步：确认耗材
    private static final int AdjustSettings = 3;      // 第三步：调整设置

    // ------------------- 第一步：选择预设 -------------------
    // 是否已完成预设选择
    @SaveToDisk(defaultValue = "false")
    private boolean presetConfirm = false;
    // 当前查看的预设组索引
    @SaveToDisk(defaultValue = "0")
    private int checkGroup = 0;
    // 显示的预设编号
    @SaveToDisk(defaultValue = "0")
    private int checkId = 0;
    // 保存的预设组编号
    @SaveToDisk(defaultValue = "0")
    private int saveGroup = 0;
    // 保存的预设编号
    @SaveToDisk(defaultValue = "0")
    private int saveId = 0;
    // 是否显示预览
    @SaveToDisk(defaultValue = "false")
    private boolean preview = false;
    // 是否高亮
    @SaveToDisk(defaultValue = "false")
    private boolean highlight = false;

    // ------------------- 第二步：选择偏移 -------------------
    // X方向区块偏移
    @SaveToDisk(defaultValue = "0")
    private int offsetX = 0;
    // Z方向区块偏移
    @SaveToDisk(defaultValue = "0")
    private int offsetZ = 0;
    // Y方向高度偏移
    @SaveToDisk(defaultValue = "-1")
    private int offsetY = -1;

    // 坐标点
    @SaveToDisk
    private BlockPos pos1 = new BlockPos(0, 0, 0);
    @SaveToDisk
    private BlockPos pos2 = new BlockPos(0, 0, 0);

    // ------------------- 第三步：确认放置 -------------------
    // 库存的原料量
    @SaveToDisk
    private final int[] materialInventory = new int[] { 0, 0, 0 };
    // 库存是否充足
    @SaveToDisk(defaultValue = "false")
    private boolean insufficient = false;
    // 原料物品
    private static final List<List<IntObjectHolder<Item>>> ITEM_VALUE_HOLDERS = List.of(
            List.of(
                    new IntObjectHolder<>(5000, GTOItems.INDUSTRIAL_COMPONENTS[0][2].asItem()),
                    new IntObjectHolder<>(1000, GTOItems.INDUSTRIAL_COMPONENTS[0][1].asItem()),
                    new IntObjectHolder<>(200, GTOItems.INDUSTRIAL_COMPONENTS[0][0].asItem())),
            List.of(
                    new IntObjectHolder<>(5000, GTOItems.INDUSTRIAL_COMPONENTS[1][2].asItem()),
                    new IntObjectHolder<>(1000, GTOItems.INDUSTRIAL_COMPONENTS[1][1].asItem()),
                    new IntObjectHolder<>(200, GTOItems.INDUSTRIAL_COMPONENTS[1][0].asItem())),
            List.of(
                    new IntObjectHolder<>(5000, GTOItems.INDUSTRIAL_COMPONENTS[2][2].asItem()),
                    new IntObjectHolder<>(1000, GTOItems.INDUSTRIAL_COMPONENTS[2][1].asItem()),
                    new IntObjectHolder<>(200, GTOItems.INDUSTRIAL_COMPONENTS[2][0].asItem())));

    // ------------------- 第四步：运行中 -------------------
    // 任务是否完成
    @SaveToDisk(defaultValue = "true")
    private boolean taskCompleted = true;

    /** 正在跑的部署任务；tick 由下面的监控转进来（监控的 task 必须固定，而 placer 每次都是新的）。 */
    private PlatformStructurePlacer activePlacer;
    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    protected final TickTimeMonitor platformPlacementMonitor = holder.monitorTick(GTOTickTimeMonitors.PLATFORM_PLACEMENT, this::tickPlatformPlacement);

    private void tickPlatformPlacement() {
        PlatformStructurePlacer placer = activePlacer;
        if (placer != null) placer.placeBatch();
    }

    // 跳过空气
    @SaveToDisk(defaultValue = "true")
    private boolean skipAir = true;
    // 光照更新
    @SaveToDisk(defaultValue = "true")
    private boolean updateLight = true;
    // 速度
    @SaveToDisk(defaultValue = "50")
    private int speed = 50;
    private int lastExportTimer = Integer.MIN_VALUE;
    // X轴对称
    @SaveToDisk(defaultValue = "false")
    private boolean xMirror = false;
    // Z轴对称
    @SaveToDisk(defaultValue = "false")
    private boolean zMirror = false;
    // Y轴旋转
    @SaveToDisk(defaultValue = "0")
    private int rotation = 0;
    // 可导出
    @SaveToDisk(defaultValue = "false")
    private boolean canExport = false;

    private int progress = 0;

    /////////////////////////////////////
    // ************ UI组件 ************ //
    /////////////////////////////////////
    @RegisterLanguage(cn = "部署步骤", en = "Deployment Steps")
    private static final String STEP_SECTION = "gtocore.machine.industrial_platform_deployment_tools.ui.steps";
    @RegisterLanguage(cn = "预设库", en = "Library")
    private static final String PRESET_GROUP = "gtocore.machine.industrial_platform_deployment_tools.ui.preset_group";
    @RegisterLanguage(cn = "切换预设库后，蓝图回到该库的第一个", en = "Switching libraries returns to the first blueprint of the library")
    private static final String PRESET_GROUP_TIP = "gtocore.machine.industrial_platform_deployment_tools.ui.preset_group_tip";
    @RegisterLanguage(cn = "蓝图", en = "Blueprint")
    private static final String BLUEPRINT = "gtocore.machine.industrial_platform_deployment_tools.ui.blueprint";
    @RegisterLanguage(cn = "向前 10 个", en = "Back 10")
    private static final String PREVIOUS_10 = "gtocore.machine.industrial_platform_deployment_tools.ui.previous_10";
    @RegisterLanguage(cn = "向后 10 个", en = "Forward 10")
    private static final String NEXT_10 = "gtocore.machine.industrial_platform_deployment_tools.ui.next_10";
    @RegisterLanguage(cn = "向前 5 个", en = "Back 5")
    private static final String PREVIOUS_5 = "gtocore.machine.industrial_platform_deployment_tools.ui.previous_5";
    @RegisterLanguage(cn = "向后 5 个", en = "Forward 5")
    private static final String NEXT_5 = "gtocore.machine.industrial_platform_deployment_tools.ui.next_5";
    @RegisterLanguage(cn = "选用当前蓝图", en = "Use This Blueprint")
    private static final String CHOOSE = "gtocore.machine.industrial_platform_deployment_tools.ui.choose";
    @RegisterLanguage(cn = "选用", en = "Select")
    private static final String CHOOSE_BUTTON = "gtocore.machine.industrial_platform_deployment_tools.ui.choose_button";
    @RegisterLanguage(cn = "将正在查看的蓝图设为部署目标，并重新计算材料与放置范围", en = "Sets the viewed blueprint as the deployment target and recalculates materials and placement range")
    private static final String CHOOSE_TIP = "gtocore.machine.industrial_platform_deployment_tools.ui.choose_tip";
    @RegisterLanguage(cn = "显示预览图", en = "Show Preview")
    private static final String PREVIEW = "gtocore.machine.industrial_platform_deployment_tools.ui.preview";
    @RegisterLanguage(cn = "该蓝图没有预览图", en = "This blueprint has no preview image")
    private static final String PREVIEW_UNAVAILABLE = "gtocore.machine.industrial_platform_deployment_tools.ui.preview_unavailable";
    @RegisterLanguage(cn = "高亮放置范围", en = "Highlight Area")
    private static final String HIGHLIGHT = "gtocore.machine.industrial_platform_deployment_tools.ui.highlight";
    @RegisterLanguage(cn = "在世界中标出已选蓝图的放置范围；放入两张坐标卡时标出两卡之间的区域", en = "Marks the placement area of the selected blueprint in the world; with two coordinate cards inserted, marks the region between them")
    private static final String HIGHLIGHT_TIP = "gtocore.machine.industrial_platform_deployment_tools.ui.highlight_tip";
    @RegisterLanguage(cn = "装载材料", en = "Load Materials")
    private static final String LOAD = "gtocore.machine.industrial_platform_deployment_tools.ui.load";
    @RegisterLanguage(cn = "装载", en = "Load")
    private static final String LOAD_BUTTON = "gtocore.machine.industrial_platform_deployment_tools.ui.load_button";
    @RegisterLanguage(cn = "将物品槽中的工业组件折算为材料储量", en = "Converts industrial components in the slots into material reserves")
    private static final String LOAD_TIP = "gtocore.machine.industrial_platform_deployment_tools.ui.load_tip";
    @RegisterLanguage(cn = "卸载材料", en = "Unload Materials")
    private static final String UNLOAD = "gtocore.machine.industrial_platform_deployment_tools.ui.unload";
    @RegisterLanguage(cn = "卸载", en = "Unload")
    private static final String UNLOAD_BUTTON = "gtocore.machine.industrial_platform_deployment_tools.ui.unload_button";
    @RegisterLanguage(cn = "将材料储量换回工业组件，放入空物品槽", en = "Converts material reserves back into industrial components in empty slots")
    private static final String UNLOAD_TIP = "gtocore.machine.industrial_platform_deployment_tools.ui.unload_tip";
    @RegisterLanguage(cn = "X 偏移（区块）", en = "X Offset (chunks)")
    private static final String OFFSET_X = "gtocore.machine.industrial_platform_deployment_tools.ui.offset_x";
    @RegisterLanguage(cn = "Y 偏移（格）", en = "Y Offset (blocks)")
    private static final String OFFSET_Y = "gtocore.machine.industrial_platform_deployment_tools.ui.offset_y";
    @RegisterLanguage(cn = "Z 偏移（区块）", en = "Z Offset (chunks)")
    private static final String OFFSET_Z = "gtocore.machine.industrial_platform_deployment_tools.ui.offset_z";
    @RegisterLanguage(cn = "跳过空气", en = "Skip Air")
    private static final String SKIP_AIR = "gtocore.machine.industrial_platform_deployment_tools.ui.skip_air";
    @RegisterLanguage(cn = "蓝图中的空气不清除原有方块", en = "Air in the blueprint does not clear existing blocks")
    private static final String SKIP_AIR_TIP = "gtocore.machine.industrial_platform_deployment_tools.ui.skip_air_tip";
    @RegisterLanguage(cn = "光照更新", en = "Update Lighting")
    private static final String UPDATE_LIGHT = "gtocore.machine.industrial_platform_deployment_tools.ui.update_light";
    @RegisterLanguage(cn = "放置时重新计算光照", en = "Recalculates lighting while placing")
    private static final String UPDATE_LIGHT_TIP = "gtocore.machine.industrial_platform_deployment_tools.ui.update_light_tip";
    @RegisterLanguage(cn = "X 轴对称", en = "Mirror X")
    private static final String X_MIRROR = "gtocore.machine.industrial_platform_deployment_tools.ui.x_mirror";
    @RegisterLanguage(cn = "Z 轴对称", en = "Mirror Z")
    private static final String Z_MIRROR = "gtocore.machine.industrial_platform_deployment_tools.ui.z_mirror";
    @RegisterLanguage(cn = "绕 Y 轴旋转", en = "Rotation (Y axis)")
    private static final String ROTATION = "gtocore.machine.industrial_platform_deployment_tools.ui.rotation";
    @RegisterLanguage(cn = "部署速度", en = "Deployment Speed")
    private static final String SPEED = "gtocore.machine.industrial_platform_deployment_tools.ui.speed";
    @RegisterLanguage(cn = "每刻放置 速度×1000 个方块", en = "Places speed × 1000 blocks per tick")
    private static final String SPEED_TIP = "gtocore.machine.industrial_platform_deployment_tools.ui.speed_tip";
    @RegisterLanguage(cn = "部署", en = "Deployment")
    private static final String DEPLOY_SECTION = "gtocore.machine.industrial_platform_deployment_tools.ui.deploy";
    @RegisterLanguage(cn = "部署平台", en = "Deploy Platform")
    private static final String START = "gtocore.machine.industrial_platform_deployment_tools.ui.start";
    @RegisterLanguage(cn = "开始", en = "Start")
    private static final String START_BUTTON = "gtocore.machine.industrial_platform_deployment_tools.ui.start_button";
    @RegisterLanguage(cn = "消耗材料，按当前偏移、旋转与对称设置放置已选蓝图", en = "Consumes materials and places the selected blueprint with the current offset, rotation and mirror settings")
    private static final String START_TIP = "gtocore.machine.industrial_platform_deployment_tools.ui.start_tip";
    @RegisterLanguage(cn = "需要已选择蓝图、材料充足且没有进行中的部署", en = "Requires a selected blueprint, sufficient materials and no deployment in progress")
    private static final String START_UNAVAILABLE = "gtocore.machine.industrial_platform_deployment_tools.ui.start_unavailable";
    @RegisterLanguage(cn = "已放入两张坐标卡，当前为导出模式", en = "Two coordinate cards are inserted; export mode is active")
    private static final String EXPORT_MODE = "gtocore.machine.industrial_platform_deployment_tools.ui.export_mode";
    @RegisterLanguage(cn = "可以开始部署", en = "Ready to deploy")
    private static final String READY = "gtocore.machine.industrial_platform_deployment_tools.ui.ready";
    @RegisterLanguage(cn = "导出区域", en = "Export Region")
    private static final String EXPORT = "gtocore.machine.industrial_platform_deployment_tools.ui.export";
    @RegisterLanguage(cn = "导出", en = "Export")
    private static final String EXPORT_BUTTON = "gtocore.machine.industrial_platform_deployment_tools.ui.export_button";
    @RegisterLanguage(cn = "将两张坐标卡之间的区域导出为结构文件，保存在 logs/platform 目录", en = "Exports the region between the two coordinate cards as a structure file in the logs/platform folder")
    private static final String EXPORT_TIP = "gtocore.machine.industrial_platform_deployment_tools.ui.export_tip";
    @RegisterLanguage(cn = "需要放入两张已记录坐标的坐标卡", en = "Requires two coordinate cards with stored positions")
    private static final String EXPORT_UNAVAILABLE = "gtocore.machine.industrial_platform_deployment_tools.ui.export_unavailable";

    private static final String AT_MIN = "gtceu.uipro.stepper.at_min";
    private static final String AT_MAX = "gtceu.uipro.stepper.at_max";
    private static final String UNSELECTED = "gtocore.machine.industrial_platform_deployment_tools.text.unselected";
    private static final String INSUFFICIENT = "gtocore.machine.industrial_platform_deployment_tools.material.insufficient";
    private static final String DOING = "gtocore.machine.industrial_platform_deployment_tools.doing";
    private static final String[] STEP_TITLES = {
            "gtocore.machine.industrial_platform_deployment_tools.title.0",
            "gtocore.machine.industrial_platform_deployment_tools.title.1",
            "gtocore.machine.industrial_platform_deployment_tools.title.2",
            "gtocore.machine.industrial_platform_deployment_tools.title.3" };
    private static final String[] MATERIAL_KEYS = {
            "gtocore.machine.industrial_platform_deployment_tools.material.0",
            "gtocore.machine.industrial_platform_deployment_tools.material.1",
            "gtocore.machine.industrial_platform_deployment_tools.material.2" };
    private static final Component[] ROTATION_OPTIONS = {
            Component.literal("0°"), Component.literal("90°"), Component.literal("180°"), Component.literal("270°") };

    private static final String PAGE_SCROLLER = "platform_deployment.page";
    private static final int EXPORT_COOLDOWN = 100;

    private UIElement itemPanel() {
        var controls = ControlPanel.of(this);
        var adapter = new MenuItemAdapter(inventory.storage);
        controls.addGrid(ITEMS, SlotGrid.of(9, inventory.storage.size(), i -> ItemSlot.of(adapter, i)));
        return controls.build();
    }

    private static final int PREVIEW_SIZE = 100;
    private static final int INDEX_WIDTH = 36;
    private static final int MAX_CHUNK_OFFSET = 1875000;
    private static final int MAX_Y_OFFSET = 4096;
    private static final int MIN_SPEED = 10;
    private static final int MAX_SPEED = 100;

    @Override
    public Widget createUIWidget() {
        var steps = ButtonGroup.single(totalStep + 1, i -> Component.literal(String.valueOf(i + 1)), () -> step, i -> step = i)
                .horizontal()
                .optionTooltips(i -> Collections.singletonList(Component.translatable(STEP_TITLES[i])));
        var page = MachineDisplay.column()
                .addChild(Form.section(STEP_SECTION).addChild(steps))
                .addChild(MachineDisplay.display(this, this::addDisplayText, null))
                .addChild(new SwitchedContent(() -> step, (key, remote) -> createStepControls(key)))
                .addChild(createDeployControls())
                .addChild(itemPanel());
        var scroller = ScrollerView.page(PAGE_SCROLLER, UISizes.CONTENT_WIDTH).adaptiveWidth();
        scroller.addScrollViewChild(page);
        return scroller;
    }

    private static UIElement centered(Widget widget) {
        return new UIElement().layout(l -> l.row().justifyContent(AlignContent.CENTER)).addChild(widget);
    }

    @Nullable
    private Widget createStepControls(int key) {
        var controls = ControlPanel.of(this);
        switch (key) {
            case PresetSelection -> {
                int groups = PlatformTemplateStorage.preset.size();
                var groupStepper = Stepper.of(INDEX_WIDTH, () -> checkGroup, this::setCheckGroup, 0, () -> PlatformTemplateStorage.preset.size() - 1)
                        .setFormatter(i -> (i + 1) + "/" + groups);
                controls.add(Form.controlRow(PRESET_GROUP, navigator(
                        navButton("«", PREVIOUS_10, () -> changeGroup(-10), () -> checkGroup <= 0, AT_MIN),
                        groupStepper,
                        navButton("»", NEXT_10, () -> changeGroup(10), () -> checkGroup >= groups - 1, AT_MAX)), PRESET_GROUP_TIP));
                var blueprintStepper = Stepper.of(INDEX_WIDTH, () -> checkId, this::setCheckId, 0, () -> structureCount() - 1);
                var blueprintRow = Form.controlRow(BLUEPRINT, navigator(
                        navButton("«", PREVIOUS_5, () -> changeId(-5), () -> checkId <= 0, AT_MIN),
                        blueprintStepper,
                        navButton("»", NEXT_5, () -> changeId(5), () -> checkId >= structureCount() - 1, AT_MAX)));
                var blueprintCount = blueprintRow.addSyncValue(SyncValue.ofInt(this::structureCount, 0));
                blueprintStepper.setFormatter(i -> (i + 1) + "/" + blueprintCount.getValue());
                controls.add(blueprintRow);
                controls.addServerButton(CHOOSE, CHOOSE_BUTTON, () -> {
                    saveGroup = checkGroup;
                    saveId = checkId;
                    presetConfirm = true;
                    examineMaterial();
                    posChanged();
                }, CHOOSE_TIP);
                controls.addToggle(PREVIEW, () -> preview, value -> preview = value)
                        .disabled(() -> !getPlatformBlockStructure(checkGroup, checkId).preview(), PREVIEW_UNAVAILABLE);
                controls.addToggle(HIGHLIGHT, () -> highlight, value -> {
                    highlight = value;
                    highlightArea(value);
                }, HIGHLIGHT_TIP);
                controls.add(new SwitchedContent(this::previewKey, (previewKey, remote) -> createPreview(previewKey)));
            }
            case ConfirmConsumables -> {
                controls.addServerButton(LOAD, LOAD_BUTTON, () -> {
                    loadingMaterial();
                    examineMaterial();
                }, LOAD_TIP);
                controls.addServerButton(UNLOAD, UNLOAD_BUTTON, () -> {
                    unloadingMaterial();
                    examineMaterial();
                }, UNLOAD_TIP);
            }
            case AdjustSettings -> {
                controls.addInt(OFFSET_X, () -> offsetX, value -> {
                    offsetX = value;
                    posChanged();
                }, -MAX_CHUNK_OFFSET, MAX_CHUNK_OFFSET);
                controls.addInt(OFFSET_Y, () -> offsetY, value -> {
                    offsetY = value;
                    posChanged();
                }, -MAX_Y_OFFSET, MAX_Y_OFFSET);
                controls.addInt(OFFSET_Z, () -> offsetZ, value -> {
                    offsetZ = value;
                    posChanged();
                }, -MAX_CHUNK_OFFSET, MAX_CHUNK_OFFSET);
                controls.addToggle(SKIP_AIR, () -> skipAir, value -> skipAir = value, SKIP_AIR_TIP);
                controls.addToggle(UPDATE_LIGHT, () -> updateLight, value -> updateLight = value, UPDATE_LIGHT_TIP);
                controls.addToggle(X_MIRROR, () -> xMirror, value -> xMirror = value);
                controls.addToggle(Z_MIRROR, () -> zMirror, value -> zMirror = value);
                controls.addChoice(ROTATION, ROTATION_OPTIONS.length, i -> ROTATION_OPTIONS[i], () -> rotation / 90, i -> rotation = i * 90);
                controls.addInt(SPEED, () -> speed, value -> speed = value, MIN_SPEED, MAX_SPEED, SPEED_TIP);
            }
            default -> {
                return null;
            }
        }
        return controls.build();
    }

    private UIElement createDeployControls() {
        var status = TextLine.of(LayoutStyle.AUTO, MultiblockPage.cached(this::deployState, PlatformDeploymentMachine::deployStateText))
                .bindClientColor(UITheme::panelText);
        var start = Button.translatable(UISizes.BUTTON_WIDTH, START_BUTTON)
                .setOnServerClick(() -> {
                    start();
                    onChanged();
                })
                .disabled(() -> !presetConfirm || !insufficient || !taskCompleted, START_UNAVAILABLE);
        var startRow = Form.controlRow(START, start, START_TIP).disabled(() -> canExport, EXPORT_MODE);
        var export = Button.translatable(UISizes.BUTTON_WIDTH, EXPORT_BUTTON)
                .setOnServerClick(this::requestExport)
                .disabled(() -> !canExport, EXPORT_UNAVAILABLE);
        return Form.section(DEPLOY_SECTION).addChildren(status, startRow, Form.controlRow(EXPORT, export, EXPORT_TIP));
    }

    private long deployState() {
        if (canExport) return -1;
        if (!presetConfirm) return -2;
        if (!insufficient) return -3;
        if (!taskCompleted) return progress;
        return -4;
    }

    private static Component deployStateText(long state) {
        if (state == -1) return Component.translatable(EXPORT_MODE);
        if (state == -2) return Component.translatable(UNSELECTED);
        if (state == -3) return Component.translatable(INSUFFICIENT);
        if (state == -4) return Component.translatable(READY);
        return Component.translatable(DOING, state);
    }

    private static UIElement navigator(Widget... children) {
        return UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(children);
    }

    private Button navButton(String glyph, String tooltipKey, Runnable action, BooleanSupplier atEdge, String edgeKey) {
        var button = Button.glyph(glyph).setOnServerClick(() -> {
            action.run();
            onChanged();
        }).disabled(atEdge, edgeKey);
        button.tooltips(tooltipKey);
        return button;
    }

    private int structureCount() {
        return getPlatformPreset(checkGroup).structures().size();
    }

    private void changeGroup(int delta) {
        checkGroup = Mth.clamp(checkGroup + delta, 0, PlatformTemplateStorage.preset.size() - 1);
        checkId = 0;
    }

    private void changeId(int delta) {
        checkId = Mth.clamp(checkId + delta, 0, structureCount() - 1);
    }

    private void setCheckGroup(int group) {
        if (group == checkGroup) return;
        checkGroup = group;
        checkId = 0;
        onChanged();
    }

    private void setCheckId(int id) {
        if (id == checkId) return;
        checkId = id;
        onChanged();
    }

    private int previewKey() {
        if (step != PresetSelection || !preview) return -1;
        if (!getPlatformBlockStructure(checkGroup, checkId).preview()) return -1;
        return checkGroup << 16 | checkId;
    }

    @Nullable
    private Widget createPreview(int key) {
        if (key < 0) return null;
        PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(key >>> 16, key & 0xFFFF);
        var texture = new ResourceTexture(GTOCore.id("textures/gui/industrial_platform_deployment_tools/" + structure.name() + ".png"));
        return centered(new ImageWidget(0, 0, PREVIEW_SIZE, PREVIEW_SIZE, texture));
    }

    private static final Component empty = Component.empty();

    private void addDisplayText(List<Component> textList) {
        textList.add(Component.translatable(STEP_TITLES[step]));
        switch (step) {
            case Introduction -> GTOMachineTooltips.IndustrialPlatformDeploymentToolsIntroduction.apply(textList);
            case PresetSelection -> {
                PlatformBlockType.PlatformPreset group = getPlatformPreset(checkGroup);
                PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(checkGroup, checkId);

                textList.add(presetConfirm ?
                        Component.translatable("gtocore.machine.industrial_platform_deployment_tools.text.selected", saveGroup + 1, saveId + 1) :
                        Component.translatable(UNSELECTED));
                textList.add(Component.translatable("gtocore.machine.industrial_platform_deployment_tools.text.size", structure.xSize(), structure.ySize(), structure.zSize(),
                        structure.xSize() >> 4, structure.zSize() >> 4));

                String displayName = group.displayName();
                String description = group.description();
                String source = group.source();
                if (displayName != null) textList.add(Component.translatable(displayName));
                if (description != null) textList.add(Component.translatable(description));
                if (source != null) textList.add(Component.translatable("gtocore.machine.industrial_platform_deployment_tools.text.source", source));

                String structDisplayName = structure.displayName();
                String type = structure.type();
                String structDescription = structure.description();
                String structSource = structure.source();
                if (structDisplayName != null) textList.add(Component.translatable(structDisplayName));
                if (type != null) textList.add(Component.translatable(type));
                if (structDescription != null) textList.add(Component.translatable(structDescription));
                if (structSource != null) textList.add(Component.translatable("gtocore.machine.industrial_platform_deployment_tools.text.source", structSource));
            }
            case ConfirmConsumables -> {
                textList.add(Component.translatable("gtocore.machine.industrial_platform_deployment_tools.material.reserves"));
                for (int i = 0; i < MATERIAL_KEYS.length; i++) {
                    textList.add(Component.translatable(MATERIAL_KEYS[i]).append(String.valueOf(materialInventory[i])));
                }
                textList.add(empty);

                if (!presetConfirm) textList.add(Component.translatable(UNSELECTED));
                else {
                    PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(saveGroup, saveId);
                    int[] costMaterial = structure.materials();

                    textList.add(Component.translatable("gtocore.machine.industrial_platform_deployment_tools.material.demand"));
                    for (int i = 0; i < MATERIAL_KEYS.length; i++) {
                        textList.add(Component.translatable(MATERIAL_KEYS[i]).append(String.valueOf(costMaterial[i])));
                    }
                    textList.add(empty);

                    List<IntObjectHolder<ItemStack>> extraMaterials = structure.extraMaterials();
                    if (!extraMaterials.isEmpty()) {
                        textList.add(Component.translatable("gtocore.machine.industrial_platform_deployment_tools.material.extra_demand"));
                        extraMaterials.forEach(e -> textList.add(
                                Component.literal("[").append(e.value.getDisplayName()).append("×").append(String.valueOf(e.priority)).append("]")));
                    }
                    if (!insufficient) textList.add(Component.translatable(INSUFFICIENT));
                    else textList.add(Component.translatable("gtocore.machine.industrial_platform_deployment_tools.material.adequate"));
                }
            }
            case AdjustSettings -> {
                if (!presetConfirm) textList.add(Component.translatable(UNSELECTED));
                else {
                    textList.add(Component.translatable("gtocore.machine.industrial_platform_deployment_tools.boundary"));
                    textList.add(Component.translatable("gtocore.machine.industrial_platform_deployment_tools.offset.x", pos1.getX())
                            .append(" ~ ").append(String.valueOf(pos2.getX())));
                    textList.add(Component.translatable("gtocore.machine.industrial_platform_deployment_tools.offset.y", pos1.getY())
                            .append(" ~ ").append(String.valueOf(pos2.getY())));
                    textList.add(Component.translatable("gtocore.machine.industrial_platform_deployment_tools.offset.z", pos1.getZ())
                            .append(" ~ ").append(String.valueOf(pos2.getZ())));
                }
            }
        }
    }

    /////////////////////////////////////
    // *********** 辅助方法 *********** //
    /////////////////////////////////////

    private PlatformBlockType.PlatformPreset getPlatformPreset(int group) {
        try {
            return PlatformTemplateStorage.preset.get(group);
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            checkGroup = 0;
            saveGroup = 0;
            return PlatformTemplateStorage.preset.getFirst();
        }
    }

    private PlatformBlockType.PlatformBlockStructure getPlatformBlockStructure(int group, int id) {
        try {
            return getPlatformPreset(group).structures().get(id);
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            checkId = 0;
            saveId = 0;
            return getPlatformPreset(group).structures().getFirst();
        }
    }

    private void loadingMaterial() {
        var storage = inventory.storage;
        for (int i = 0; i < 9; i++) {
            long count = storage.amountAt(i);
            if (count <= 0) continue;
            Item item = storage.keyAt(i).getItem();
            for (int k = 0; k < ITEM_VALUE_HOLDERS.size(); k++) {
                for (IntObjectHolder<Item> holder : ITEM_VALUE_HOLDERS.get(k)) {
                    if (holder.value.equals(item)) {
                        materialInventory[k] += holder.priority * (int) count;
                        storage.set(i, null, 0);
                        break;
                    }
                }
            }
        }
    }

    private void unloadingMaterial() {
        var storage = inventory.storage;
        for (int i = 0; i < 9; i++) {
            if (storage.amountAt(i) > 0) continue;
            boolean filled = false;
            for (int k = 0; k < ITEM_VALUE_HOLDERS.size() && !filled; k++) {
                for (IntObjectHolder<Item> holder : ITEM_VALUE_HOLDERS.get(k)) {
                    int count = Math.min(materialInventory[k] / holder.priority, 64);
                    if (count > 0) {
                        storage.set(i, AEItemKey.of(holder.value), count);
                        materialInventory[k] -= holder.priority * count;
                        filled = true;
                        break;
                    }
                }
            }
        }
    }

    private void posChanged() {
        BlockPos pos = getPos();
        if (highlight) {
            highlight = false;
            highlightArea(false);
        }
        PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(saveGroup, saveId);

        int sizeX = structure.xSize();
        int sizeZ = structure.zSize();
        int sizeY = structure.ySize();

        int chunkMinX = (pos.getX() >> 4) << 4;
        int chunkMinZ = (pos.getZ() >> 4) << 4;

        int centerOffsetX = (sizeX - 1) / 32;
        int centerOffsetZ = (sizeZ - 1) / 32;

        int startX = chunkMinX - centerOffsetX * 16 + offsetX * 16;
        int startZ = chunkMinZ - centerOffsetZ * 16 + offsetZ * 16;
        int startY = pos.getY() + offsetY;

        int maxX = startX + sizeX - 1;
        int maxZ = startZ + sizeZ - 1;
        int maxY = startY + sizeY - 1;

        pos1 = new BlockPos(startX, startY, startZ);
        pos2 = new BlockPos(maxX, maxY, maxZ);
    }

    private void examineMaterial() {
        if (!presetConfirm) {
            insufficient = false;
            return;
        }
        PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(saveGroup, saveId);
        int[] costMaterial = structure.materials();
        boolean materialsSufficient = true;
        canExport = false;
        for (int i = 0; i < materialInventory.length; i++) {
            if (materialInventory[i] < costMaterial[i]) {
                materialsSufficient = false;
                break;
            }
        }
        List<IntObjectHolder<ItemStack>> extraMaterials = structure.extraMaterials();
        Map<Item, Integer> inventoryCount = new HashMap<>();
        int coordinateCards = 0;
        var storage = inventory.storage;
        for (int i = 0; i < storage.size(); i++) {
            long count = storage.amountAt(i);
            if (count <= 0) continue;
            var key = storage.keyAt(i);
            Item item = key.getItem();
            inventoryCount.put(item, inventoryCount.getOrDefault(item, 0) + (int) count);
            if (item == GTOItems.COORDINATE_CARD.asItem()) {
                if (coordinateCards == 0) pos1 = getStoredCoordinates(Keys.displayStack(key));
                else pos2 = getStoredCoordinates(Keys.displayStack(key));
                coordinateCards++;
            }
        }
        for (IntObjectHolder<ItemStack> holder : extraMaterials) {
            Item item = holder.value.getItem();
            int required = holder.priority;
            int available = inventoryCount.getOrDefault(item, 0);
            if (available < required) {
                materialsSufficient = false;
                break;
            }
        }
        canExport = coordinateCards > 1;
        insufficient = materialsSufficient;
    }

    private boolean consumeResources() {
        if (!presetConfirm || !insufficient) return false;
        PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(saveGroup, saveId);
        int[] costMaterial = structure.materials();
        for (int i = 0; i < materialInventory.length; i++) materialInventory[i] -= costMaterial[i];
        List<IntObjectHolder<ItemStack>> extraMaterials = structure.extraMaterials();
        for (IntObjectHolder<ItemStack> holder : extraMaterials) {
            Item item = holder.value.getItem();
            int remaining = holder.priority;
            var storage = inventory.storage;
            for (int i = 0; i < storage.size() && remaining > 0; i++) {
                long count = storage.amountAt(i);
                if (count <= 0) continue;
                var key = storage.keyAt(i);
                if (key.getItem() == item) {
                    remaining -= (int) storage.extract(i, key, Math.min(count, remaining), false);
                }
            }
            if (remaining > 0) {
                GTOCore.LOGGER.error("Failed to consume all required resources for platform deployment");
                return false;
            }
        }
        return true;
    }

    private static int[] transform(int lx, int ly, int lz, int sx, int sz, int rot, boolean zMir, boolean xMir) {
        int rx = lx, rz = lz;
        switch (rot) {
            case 90 -> {
                int t = rx;
                rx = sz - 1 - rz;
                rz = t;
            }
            case 180 -> {
                rx = sx - 1 - rx;
                rz = sz - 1 - rz;
            }
            case 270 -> {
                int t = rx;
                rx = rz;
                rz = sx - 1 - t;
            }
        }
        if (xMir) rx = sx - 1 - rx;
        if (zMir) rz = sz - 1 - rz;
        return new int[] { rx, ly, rz };
    }

    private static int[] calcOffsetsBy8Points(int sx, int sy, int sz, int rot, boolean zMir, boolean xMir) {
        int[][] corners = { { 0, 0, 0 }, { sx - 1, 0, 0 }, { 0, sy - 1, 0 }, { sx - 1, sy - 1, 0 }, { 0, 0, sz - 1 }, { sx - 1, 0, sz - 1 }, { 0, sy - 1, sz - 1 }, { sx - 1, sy - 1, sz - 1 } };
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        for (int[] c : corners) {
            int[] t = transform(c[0], c[1], c[2], sx, sz, rot, zMir, xMir);
            minX = Math.min(minX, t[0]);
            minY = Math.min(minY, t[1]);
            minZ = Math.min(minZ, t[2]);
        }
        return new int[] { -minX, -minY, -minZ };
    }

    private void highlightArea(boolean light) {
        if (!(getLevel() instanceof ServerLevel)) return;
        ResourceKey<Level> dimension = getLevel().dimension();

        if (canExport) {
            BlockPos p1 = null, p2 = null;
            var storage = inventory.storage;
            for (int i = 0; i < storage.size() && (p1 == null || p2 == null); i++) {
                if (storage.amountAt(i) <= 0) continue;
                var key = storage.keyAt(i);
                if (key.getItem() == GTOItems.COORDINATE_CARD.asItem()) {
                    if (p1 == null) p1 = getStoredCoordinates(Keys.displayStack(key));
                    else p2 = getStoredCoordinates(Keys.displayStack(key));
                }
            }
            if (p1 != null && p2 != null) {
                BlockPos min = new BlockPos(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.min(p1.getZ(), p2.getZ()));
                BlockPos max = new BlockPos(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()), Math.max(p1.getZ(), p2.getZ()));
                if (light) highlightRegion(dimension, min, max, 0x660099CC, 1200);
                else stopHighlight(min, max);
            }
            return;
        }

        if (!presetConfirm) return;
        PlatformBlockType.PlatformBlockStructure struct = getPlatformBlockStructure(saveGroup, saveId);
        int sx = struct.xSize(), sy = struct.ySize(), sz = struct.zSize();
        BlockPos start = pos1;

        boolean zMir = this.xMirror, xMir = this.zMirror;
        int rot = this.rotation;

        int[] offsets = calcOffsetsBy8Points(sx, sy, sz, rot, zMir, xMir);
        int ox = offsets[0], oy = offsets[1], oz = offsets[2];

        int[][] corners = { { 0, 0, 0 }, { sx - 1, 0, 0 }, { 0, sy - 1, 0 }, { sx - 1, sy - 1, 0 }, { 0, 0, sz - 1 }, { sx - 1, 0, sz - 1 }, { 0, sy - 1, sz - 1 }, { sx - 1, sy - 1, sz - 1 } };

        int minWX = Integer.MAX_VALUE, minWY = Integer.MAX_VALUE, minWZ = Integer.MAX_VALUE;
        int maxWX = Integer.MIN_VALUE, maxWY = Integer.MIN_VALUE, maxWZ = Integer.MIN_VALUE;
        for (int[] c : corners) {
            int[] t = transform(c[0], c[1], c[2], sx, sz, rot, zMir, xMir);
            int wx = start.getX() + t[0] + ox;
            int wy = start.getY() + t[1] + oy;
            int wz = start.getZ() + t[2] + oz;
            minWX = Math.min(minWX, wx);
            maxWX = Math.max(maxWX, wx);
            minWY = Math.min(minWY, wy);
            maxWY = Math.max(maxWY, wy);
            minWZ = Math.min(minWZ, wz);
            maxWZ = Math.max(maxWZ, wz);
        }

        BlockPos minPos = new BlockPos(minWX, minWY, minWZ);
        BlockPos maxPos = new BlockPos(maxWX, maxWY, maxWZ);
        if (light) highlightRegion(dimension, minPos, maxPos, 0x2277FF77, 600);
        else stopHighlight(minPos, maxPos);
    }

    private void start() {
        if (!taskCompleted) return;
        Level level = getLevel();
        if (level == null) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!consumeResources()) return;
        posChanged();
        PlatformBlockType.PlatformBlockStructure structure = getPlatformBlockStructure(saveGroup, saveId);
        progress = 0;
        taskCompleted = false;
        activePlacer = null;
        try {
            activePlacer = PlatformStructurePlacer.placeStructureAsync(
                    serverLevel,
                    pos1,
                    structure,
                    speed * 1000,
                    true,
                    skipAir,
                    updateLight,
                    zMirror,
                    xMirror,
                    rotation,
                    progress -> this.progress = progress,
                    () -> {
                        activePlacer = null;
                        taskCompleted = true;
                    },
                    platformPlacementMonitor);
        } catch (IOException e) {
            GTOCore.LOGGER.error("The industrial platform deployment tool cannot deploy the platform, platform error {} {}, file location {}",
                    getPlatformPreset(saveGroup).name(),
                    structure.name(),
                    structure.resource());
            taskCompleted = true;
        }
        examineMaterial();
    }

    private void requestExport() {
        int now = getOffsetTimer();
        if (lastExportTimer != Integer.MIN_VALUE && now - lastExportTimer >= 0 && now - lastExportTimer < EXPORT_COOLDOWN) return;
        lastExportTimer = now;
        getPlatform();
    }

    private void getPlatform() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        BlockPos pos1 = null;
        BlockPos pos2 = null;
        var storage = inventory.storage;
        for (int i = 0; i < storage.size(); i++) {
            if (storage.amountAt(i) <= 0) continue;
            var key = storage.keyAt(i);
            if (key.getItem() == GTOItems.COORDINATE_CARD.asItem()) {
                if (pos1 == null) pos1 = getStoredCoordinates(Keys.displayStack(key));
                else pos2 = getStoredCoordinates(Keys.displayStack(key));
            }
        }
        if (pos1 != null && pos2 != null) {
            PlatformCreationAsync(serverLevel, pos1, pos2, xMirror, zMirror, rotation);
        }
    }
}
