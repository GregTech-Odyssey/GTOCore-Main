package com.gtocore.common.machine.tesseract;

import com.gtocore.api.gui.ServerRows;
import com.gtocore.common.item.TesseractTargetMarker;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.data.GTODimensions;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncItem;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ButtonGroup;
import com.gregtechceu.gtceu.uipro.elements.InfoIcon;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.uiwidgets.cover.CoverUIs;
import com.gregtechceu.gtceu.uiwidgets.item.HeldItemPage;
import com.gregtechceu.gtceu.uiwidgets.side.FaceNet;
import com.gregtechceu.gtceu.uiwidgets.side.FacePicker;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import com.gto.datasynclib.datastream.codec.ByteStreamCodec;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@DataGeneratorScanned
public final class TesseractUI {

    public static final int EMPTY = 0;
    public static final int ITEM = 1;
    public static final int FLUID = 2;
    public static final int BOTH = 3;
    public static final int NO_STORAGE = 4;
    public static final int UNLOADED = 5;
    public static final int SELF = 6;
    public static final int MISSING_DIMENSION = 7;

    public static final int LIST_WIDTH = UISizes.CONTENT_WIDTH - 2 * UITheme.PANEL_PADDING;
    public static final int MARKER_PAGE_WIDTH = UISizes.CONTENT_WIDTH + 2 * UISizes.SLOT;
    private static final int PAGE_HEIGHT_LIMIT = Integer.MAX_VALUE / 4;
    private static final int REFRESH_TICKS = 5;
    public static final String FACE_POPUP = "tesseract.face";

    @RegisterLanguage(cn = "已绑定", en = "Bound")
    private static final String LINE_BOUND = "gtocore.tesseract.line.bound";
    @RegisterLanguage(cn = "%s / %s", en = "%s / %s")
    public static final String VALUE_BOUND = "gtocore.tesseract.value.bound";
    @RegisterLanguage(cn = "%s（不限）", en = "%s (no limit)")
    public static final String VALUE_UNLIMITED = "gtocore.tesseract.value.unlimited";
    @RegisterLanguage(cn = "跨维度", en = "Cross-dimension")
    private static final String LINE_CROSS = "gtocore.tesseract.line.cross";
    @RegisterLanguage(cn = "支持", en = "Supported")
    private static final String VALUE_SUPPORTED = "gtocore.tesseract.value.supported";
    @RegisterLanguage(cn = "不支持", en = "Not supported")
    private static final String VALUE_UNSUPPORTED = "gtocore.tesseract.value.unsupported";
    @RegisterLanguage(cn = "目标记录了维度，所在区块加载时即可访问", en = "Targets store their dimension and are reachable while their chunk is loaded")
    private static final String CROSS_SUPPORTED_DETAIL = "gtocore.tesseract.cross.supported";
    @RegisterLanguage(cn = "坐标信息卡只记录坐标，目标按本机所在维度查找", en = "Coordinate cards store only coordinates; targets are looked up in this dimension")
    private static final String CROSS_UNSUPPORTED_DETAIL = "gtocore.tesseract.cross.unsupported";
    @RegisterLanguage(cn = "访问面", en = "Access side")
    private static final String LINE_ACCESS = "gtocore.tesseract.line.access";
    @RegisterLanguage(cn = "随访问方向", en = "Follows accessor")
    private static final String VALUE_FOLLOW = "gtocore.tesseract.value.follow";
    @RegisterLanguage(cn = "各目标指定面", en = "Set per target")
    private static final String VALUE_PER_TARGET = "gtocore.tesseract.value.per_target";
    @RegisterLanguage(cn = "从本机某一面访问时，访问目标的同一面", en = "Accessing a side of this block accesses the same side of the target")
    private static final String ACCESS_FOLLOW_DETAIL = "gtocore.tesseract.access.follow";
    @RegisterLanguage(cn = "无论从本机哪一面访问，都只访问各目标记录的那一面", en = "Whichever side of this block is accessed, each target is accessed only through its recorded side")
    private static final String ACCESS_PER_TARGET_DETAIL = "gtocore.tesseract.access.per_target";

    @RegisterLanguage(cn = "暂存", en = "Buffered")
    public static final String LINE_PENDING = "gtocore.tesseract.line.pending";
    @RegisterLanguage(cn = "无", en = "None")
    public static final String VALUE_PENDING_NONE = "gtocore.tesseract.value.pending_none";
    @RegisterLanguage(cn = "%s 项，暂停接收", en = "%s, intake paused")
    public static final String VALUE_PENDING = "gtocore.tesseract.value.pending";
    @RegisterLanguage(cn = "暂存清空前不接收新的样板推送", en = "No new pattern pushes are accepted until the buffer is empty")
    public static final String PENDING_DETAIL = "gtocore.tesseract.pending.detail";
    @RegisterLanguage(cn = "%s → %s 号 · %s", en = "%s → #%s · %s")
    public static final String PENDING_ROW = "gtocore.tesseract.pending.row";
    @RegisterLanguage(cn = "被样板供应器推送时", en = "When pushed by a pattern provider")
    private static final String SECTION_PUSH = "gtocore.tesseract.section.push";
    @RegisterLanguage(cn = "与普通容器相同：样板原料直接写入绑定目标。", en = "Same as a regular container: pattern ingredients go straight into the bound target.")
    private static final String PUSH_BASIC = "gtocore.tesseract.push.basic";
    @RegisterLanguage(cn = "顺序", en = "Sequential")
    private static final String MODE_SEQUENTIAL = "gtocore.tesseract.mode.sequential";
    @RegisterLanguage(cn = "轮询", en = "Round robin")
    private static final String MODE_ROUND_ROBIN = "gtocore.tesseract.mode.round_robin";
    @RegisterLanguage(cn = "也可用螺丝刀右键本机切换", en = "Can also be toggled with a screwdriver")
    private static final String MODE_TOOLTIP = "gtocore.tesseract.mode.tooltip";
    @RegisterLanguage(cn = "按卡槽顺序写入，前面的目标放满后溢出到后面的目标。", en = "Inserted in slot order; once a target is full, the rest overflows into the next one.")
    private static final String DESC_SEQUENTIAL = "gtocore.tesseract.mode.sequential.desc";
    @RegisterLanguage(cn = "每轮向每个目标各推送一份样板，已含该样板原料的目标跳过，循环至全部推送失败。", en = "Each round pushes one pattern copy into every target, skipping targets that already hold its ingredients, until no target accepts more.")
    private static final String DESC_ROUND_ROBIN = "gtocore.tesseract.mode.round_robin.desc";
    @RegisterLanguage(cn = "样板类型", en = "Pattern type")
    private static final String RULE_PATTERN = "gtocore.tesseract.rule.pattern";
    @RegisterLanguage(cn = "仅处理样板", en = "Processing only")
    private static final String VALUE_PATTERN = "gtocore.tesseract.rule.pattern.value";
    @RegisterLanguage(cn = "分发", en = "Dispatch")
    private static final String RULE_DISPATCH = "gtocore.tesseract.rule.dispatch";
    @RegisterLanguage(cn = "第 i 种原料 → 第 i 个目标", en = "Ingredient i → target i")
    private static final String VALUE_DISPATCH = "gtocore.tesseract.rule.dispatch.value";
    @RegisterLanguage(cn = "原料按样板中的编码顺序编号", en = "Ingredients are numbered in the order they are encoded in the pattern")
    private static final String DISPATCH_DETAIL = "gtocore.tesseract.rule.dispatch.detail";
    @RegisterLanguage(cn = "接收条件", en = "Accepts")
    private static final String RULE_ACCEPT = "gtocore.tesseract.rule.accept";
    @RegisterLanguage(cn = "原料种数 ≤ %s", en = "Up to %s ingredients")
    public static final String VALUE_ACCEPT = "gtocore.tesseract.rule.accept.value";
    @RegisterLanguage(cn = "原料种数多于已绑定目标数的样板不会被推送", en = "Patterns with more ingredients than bound targets are not pushed")
    private static final String ACCEPT_DETAIL = "gtocore.tesseract.rule.accept.detail";
    @RegisterLanguage(cn = "目标已满", en = "Target full")
    private static final String RULE_FULL = "gtocore.tesseract.rule.full";
    @RegisterLanguage(cn = "暂存，每秒重试", en = "Buffered, retried every second")
    private static final String VALUE_FULL = "gtocore.tesseract.rule.full.value";
    @RegisterLanguage(cn = "放不进目标的原料暂存在本机，暂存清空前不接收新的样板推送", en = "Ingredients that do not fit are buffered here; no new pushes are accepted until the buffer is empty")
    private static final String FULL_DETAIL = "gtocore.tesseract.rule.full.detail";

    @RegisterLanguage(cn = "绑定目标（按卡槽顺序）", en = "Targets (in slot order)")
    public static final String SECTION_TARGETS = "gtocore.tesseract.section.targets";
    @RegisterLanguage(cn = "原料序号 → 目标", en = "Ingredient index → target")
    public static final String SECTION_DIRECTED_TARGETS = "gtocore.tesseract.section.directed_targets";
    @RegisterLanguage(cn = "放入写有坐标的坐标信息卡以绑定目标", en = "Insert a coordinate card holding a position to bind a target")
    public static final String TARGETS_TOOLTIP = "gtocore.tesseract.targets.tooltip";
    @RegisterLanguage(cn = "也可用坐标标签枪 Shift + 左键本机写入", en = "A Tesseract Target Marker can also write targets with Shift + left-click")
    public static final String TARGETS_MARKER_TOOLTIP = "gtocore.tesseract.targets.marker_tooltip";
    @RegisterLanguage(cn = "用坐标标签枪编辑目标后，Shift + 左键本机写入", en = "Edit targets with a Tesseract Target Marker, then Shift + left-click this block to write them")
    public static final String DIRECTED_TARGETS_TOOLTIP = "gtocore.tesseract.targets.directed_tooltip";
    @RegisterLanguage(cn = "对本机按中键可将当前目标读入标签枪", en = "Pick-block on this block to copy its targets into the marker")
    public static final String DIRECTED_READ_TOOLTIP = "gtocore.tesseract.targets.directed_read";
    @RegisterLanguage(cn = "尚未绑定目标", en = "No targets bound")
    public static final String DIRECTED_EMPTY = "gtocore.tesseract.targets.directed_empty";
    @RegisterLanguage(cn = "样板第 %s 种原料送往此目标", en = "Receives ingredient %s of the pattern")
    public static final String DIRECTED_BADGE = "gtocore.tesseract.targets.directed_badge";
    @RegisterLanguage(cn = "放入坐标信息卡以绑定", en = "Insert a coordinate card")
    public static final String ROW_EMPTY = "gtocore.tesseract.row.empty";
    @RegisterLanguage(cn = "区块未加载", en = "Chunk not loaded")
    private static final String ROW_UNLOADED = "gtocore.tesseract.row.unloaded";
    @RegisterLanguage(cn = "维度不存在", en = "Unknown dimension")
    private static final String ROW_MISSING_DIMENSION = "gtocore.tesseract.row.missing_dimension";
    @RegisterLanguage(cn = "%s, %s, %s · %s", en = "%s, %s, %s · %s")
    private static final String ROW_LOCATION = "gtocore.tesseract.row.location";
    @RegisterLanguage(cn = "本维度", en = "This dimension")
    private static final String THIS_DIMENSION = "gtocore.tesseract.row.this_dimension";

    @RegisterLanguage(cn = "未绑定", en = "Not bound")
    private static final String LAMP_EMPTY = "gtocore.tesseract.lamp.empty";
    @RegisterLanguage(cn = "已连接：物品", en = "Connected: items")
    private static final String LAMP_ITEM = "gtocore.tesseract.lamp.item";
    @RegisterLanguage(cn = "已连接：流体", en = "Connected: fluids")
    private static final String LAMP_FLUID = "gtocore.tesseract.lamp.fluid";
    @RegisterLanguage(cn = "已连接：物品、流体", en = "Connected: items and fluids")
    private static final String LAMP_BOTH = "gtocore.tesseract.lamp.both";
    @RegisterLanguage(cn = "该面没有物品或流体存储", en = "No item or fluid storage on this side")
    private static final String LAMP_NO_STORAGE = "gtocore.tesseract.lamp.no_storage";
    @RegisterLanguage(cn = "目标所在区块未加载", en = "The target's chunk is not loaded")
    private static final String LAMP_UNLOADED = "gtocore.tesseract.lamp.unloaded";
    @RegisterLanguage(cn = "指向本机，已忽略", en = "Points to this block; ignored")
    private static final String LAMP_SELF = "gtocore.tesseract.lamp.self";
    @RegisterLanguage(cn = "目标维度不存在", en = "The target's dimension does not exist")
    private static final String LAMP_MISSING_DIMENSION = "gtocore.tesseract.lamp.missing_dimension";

    @RegisterLanguage(cn = "标记方式", en = "Marking")
    private static final String MARKER_SECTION_MODE = "gtocore.tesseract.marker.section.mode";
    @RegisterLanguage(cn = "左键方块", en = "Left-click on a block")
    private static final String MARKER_LEFT = "gtocore.tesseract.marker.left";
    @RegisterLanguage(cn = "从末尾往前添加", en = "Add from the end")
    private static final String MARKER_LEFT_TAIL = "gtocore.tesseract.marker.left.tail";
    @RegisterLanguage(cn = "移除标记", en = "Remove marks")
    private static final String MARKER_LEFT_REMOVE = "gtocore.tesseract.marker.left.remove";
    @RegisterLanguage(cn = "从末尾往前添加：目标排在列表末尾，后添加的排在更前", en = "Add from the end: targets go to the end of the list, later ones placed before earlier ones")
    private static final String MARKER_LEFT_TAIL_TOOLTIP = "gtocore.tesseract.marker.left.tail.tooltip";
    @RegisterLanguage(cn = "移除标记：移除该方块上的全部标记", en = "Remove marks: removes every mark on that block")
    private static final String MARKER_LEFT_REMOVE_TOOLTIP = "gtocore.tesseract.marker.left.remove.tooltip";
    @RegisterLanguage(cn = "记录的面", en = "Recorded side")
    private static final String MARKER_FACE = "gtocore.tesseract.marker.face";
    @RegisterLanguage(cn = "点击的面", en = "Clicked side")
    private static final String MARKER_FACE_CLICKED = "gtocore.tesseract.marker.face.clicked";
    @RegisterLanguage(cn = "相对的面", en = "Opposite side")
    private static final String MARKER_FACE_OPPOSITE = "gtocore.tesseract.marker.face.opposite";
    @RegisterLanguage(cn = "相对的面：记录被点击面的背面，用于标记被遮挡的面", en = "Opposite side: records the side facing away from the click, for hidden sides")
    private static final String MARKER_FACE_TOOLTIP = "gtocore.tesseract.marker.face.tooltip";
    @RegisterLanguage(cn = "已记录 %s 个目标", en = "%s target(s) recorded")
    private static final String MARKER_TARGETS = "gtocore.tesseract.marker.targets";
    @RegisterLanguage(cn = "尚未记录目标", en = "No targets recorded")
    private static final String MARKER_EMPTY = "gtocore.tesseract.marker.empty";
    @RegisterLanguage(cn = "清空", en = "Clear")
    private static final String MARKER_CLEAR = "gtocore.tesseract.marker.clear";
    @RegisterLanguage(cn = "右键方块：添加标记", en = "Right-click a block: add a mark")
    private static final String MARKER_HELP_ADD = "gtocore.tesseract.marker.help.add";
    @RegisterLanguage(cn = "Shift + 右键方块：移除该面的标记", en = "Shift + right-click a block: remove the mark on that side")
    private static final String MARKER_HELP_REMOVE = "gtocore.tesseract.marker.help.remove";
    @RegisterLanguage(cn = "Shift + 左键超立方体：写入目标", en = "Shift + left-click a Tesseract: write the targets")
    private static final String MARKER_HELP_WRITE = "gtocore.tesseract.marker.help.write";
    @RegisterLanguage(cn = "中键超立方体：读取其配置", en = "Pick-block on a Tesseract: read its configuration")
    private static final String MARKER_HELP_READ = "gtocore.tesseract.marker.help.read";
    @RegisterLanguage(cn = "写入有向超立方体后为第 %s 个目标，对应样板第 %s 种原料", en = "Becomes target %s of a Directed Tesseract, receiving ingredient %s")
    private static final String MARKER_BADGE = "gtocore.tesseract.marker.badge";
    @RegisterLanguage(cn = "金色序号为左键添加的目标，固定排在末尾", en = "Gold numbers were added by left-click and stay at the end")
    private static final String MARKER_BADGE_TAIL = "gtocore.tesseract.marker.badge.tail";
    @RegisterLanguage(cn = "上移", en = "Move up")
    private static final String MOVE_UP = "gtocore.tesseract.marker.move_up";
    @RegisterLanguage(cn = "下移", en = "Move down")
    private static final String MOVE_DOWN = "gtocore.tesseract.marker.move_down";
    @RegisterLanguage(cn = "移除", en = "Remove")
    private static final String REMOVE = "gtocore.tesseract.marker.remove";
    @RegisterLanguage(cn = "已在最前", en = "Already first")
    private static final String REASON_FIRST = "gtocore.tesseract.marker.reason.first";
    @RegisterLanguage(cn = "已在最后", en = "Already last")
    private static final String REASON_LAST = "gtocore.tesseract.marker.reason.last";
    @RegisterLanguage(cn = "没有记录的目标", en = "No targets recorded")
    private static final String REASON_EMPTY = "gtocore.tesseract.marker.reason.empty";
    @RegisterLanguage(cn = "写入有向", en = "Directed")
    private static final String PREVIEW_DIRECTED = "gtocore.tesseract.marker.preview.directed";
    @RegisterLanguage(cn = "写入进阶", en = "Advanced")
    private static final String PREVIEW_ADVANCED = "gtocore.tesseract.marker.preview.advanced";
    @RegisterLanguage(cn = "写入基础", en = "Basic")
    private static final String PREVIEW_BASIC = "gtocore.tesseract.marker.preview.basic";
    @RegisterLanguage(cn = "%s 个目标", en = "%s target(s)")
    private static final String PREVIEW_TARGETS = "gtocore.tesseract.marker.preview.targets";
    @RegisterLanguage(cn = "%s 个（跳过 %s）", en = "%s (skips %s)")
    private static final String PREVIEW_SKIPPED = "gtocore.tesseract.marker.preview.skipped";
    @RegisterLanguage(cn = "第 1 个目标", en = "Target 1 only")
    private static final String PREVIEW_FIRST = "gtocore.tesseract.marker.preview.first";
    @RegisterLanguage(cn = "无", en = "None")
    private static final String PREVIEW_NONE = "gtocore.tesseract.marker.preview.none";
    @RegisterLanguage(cn = "按列表顺序绑定全部目标，记录的面与维度都生效", en = "Binds every target in list order, using the recorded sides and dimensions")
    private static final String PREVIEW_DIRECTED_DETAIL = "gtocore.tesseract.marker.preview.directed.detail";
    @RegisterLanguage(cn = "最多 20 个，每个目标消耗 1 张坐标信息卡；记录的面不生效，当前维度以外的目标被跳过", en = "Up to 20, one coordinate card per target; recorded sides are ignored and targets outside the current dimension are skipped")
    private static final String PREVIEW_ADVANCED_DETAIL = "gtocore.tesseract.marker.preview.advanced.detail";
    @RegisterLanguage(cn = "只绑定当前维度中的第 1 个目标，消耗 1 张坐标信息卡", en = "Binds only the first target in the current dimension, using one coordinate card")
    private static final String PREVIEW_BASIC_DETAIL = "gtocore.tesseract.marker.preview.basic.detail";
    @RegisterLanguage(cn = "%s 号 · %s", en = "#%s · %s")
    private static final String POPUP_TITLE = "gtocore.tesseract.marker.popup.title";
    @RegisterLanguage(cn = "超立方体只通过此面与目标交互。", en = "The Tesseract interacts with the target only through this side.")
    private static final String POPUP_HINT = "gtocore.tesseract.marker.popup.hint";

    private TesseractUI() {}

    public record Target(GlobalPos pos, @Nullable Direction face) {}

    public record Info(ItemStack icon, Component name, Component location, int faceMask, int state) {}

    public static final class Targets {

        private final Supplier<Level> viewer;
        @Nullable
        private final BlockPos self;
        private final Supplier<List<Target>> source;
        private final IntSupplier signature;
        private final Info empty;
        private Info[] infos = new Info[0];
        private boolean built;
        private long refreshed;
        private int builtSignature;

        public Targets(Supplier<Level> viewer, @Nullable BlockPos self, Supplier<List<Target>> source, IntSupplier signature, Component emptyName) {
            this.viewer = viewer;
            this.self = self;
            this.source = source;
            this.signature = signature;
            this.empty = new Info(ItemStack.EMPTY, emptyName, Component.empty(), 0, EMPTY);
        }

        public Info get(int index) {
            refresh();
            return index >= 0 && index < infos.length ? infos[index] : empty;
        }

        private void refresh() {
            var level = viewer.get();
            if (level == null) return;
            long now = level.getGameTime();
            int current = signature.getAsInt();
            if (built && current == builtSignature && now >= refreshed && now - refreshed < REFRESH_TICKS) return;
            built = true;
            refreshed = now;
            builtSignature = current;
            var list = source.get();
            var result = new Info[list.size()];
            for (int i = 0; i < result.length; i++) {
                var target = list.get(i);
                result[i] = target == null ? empty : resolve(level, self, target);
            }
            infos = result;
        }
    }

    public static Info resolve(Level viewer, @Nullable BlockPos self, Target target) {
        var dimension = target.pos().dimension();
        var pos = target.pos().pos();
        var location = location(pos, dimension, viewer.dimension());
        int mask = FaceNet.maskOf(target.face());
        Level level = viewer.dimension() == dimension ? viewer : viewer.getServer() == null ? null : viewer.getServer().getLevel(dimension);
        if (level == null) return new Info(ItemStack.EMPTY, Component.translatable(ROW_MISSING_DIMENSION), location, mask, MISSING_DIMENSION);
        if (!level.isLoaded(pos)) return new Info(ItemStack.EMPTY, Component.translatable(ROW_UNLOADED), location, mask, UNLOADED);
        var block = level.getBlockState(pos).getBlock();
        var icon = new ItemStack(block);
        Component name = block.getName();
        if (self != null && level == viewer && pos.equals(self)) return new Info(icon, name, location, mask, SELF);
        int state = NO_STORAGE;
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            boolean item = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, target.face()).isPresent();
            boolean fluid = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, target.face()).isPresent();
            if (item && fluid) state = BOTH;
            else if (item) state = ITEM;
            else if (fluid) state = FLUID;
        }
        return new Info(icon, name, location, mask, state);
    }

    private static Component location(BlockPos pos, ResourceKey<Level> dimension, ResourceKey<Level> viewer) {
        var name = dimension == viewer ? Component.translatable(THIS_DIMENSION) : Component.translatable(GTODimensions.getTranslationKey(dimension));
        return Component.translatable(ROW_LOCATION, pos.getX(), pos.getY(), pos.getZ(), name);
    }

    public static UIElement row(Widget lead, Targets targets, int index, FaceNet face, Widget... actions) {
        var entry = new Entry(targets, index);
        var row = new Row(entry).addChildren(lead, entry, face);
        row.addChildren(actions);
        return row;
    }

    private static final class Row extends UIElement {

        private final Entry entry;

        private Row(Entry entry) {
            this.entry = entry;
            layout(l -> l.row().height(UISizes.SLOT).gapAll(UISizes.GAP).alignCenter().paddingLeft(ACCENT + 1).paddingRight(UISizes.GAP));
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPositionX(), y = getPositionY();
            graphics.fill(x, y, x + getSizeWidth(), y + getSizeHeight(), UITheme.LIST_ROW_FILL);
            int accent = entry.accentColor();
            if (accent != 0) graphics.fill(x, y, x + ACCENT, y + getSizeHeight(), accent);
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    public static FaceNet face(Targets targets, int index) {
        return new FaceNet(() -> targets.get(index).faceMask());
    }

    public static TextLine index(Supplier<Component> text, Component... tooltips) {
        var label = TextLine.of(INDEX_WIDTH, text).setColor(UITheme::textSecondary).styled().alignCenter();
        if (tooltips.length > 0) label.setHoverTooltips(tooltips);
        return label;
    }

    private static final int INDEX_WIDTH = 12;
    private static final int ICON = 16;
    private static final int ACCENT = 2;
    private static final int TEXT_X = ICON + 4;

    public static final class Entry extends UIElement {

        private final SyncValue<SyncItem> icon;
        private final SyncValue<Component> name;
        private final SyncValue<Component> location;
        private final SyncValue<Integer> state;

        private Entry(Targets targets, int index) {
            layout(l -> l.width(0).height(UISizes.SLOT).flex(1));
            this.icon = addSyncValue(SyncValue.of(() -> SyncItem.of(targets.get(index).icon()), SyncItem.CODEC, SyncItem.EMPTY));
            this.name = addSyncValue(SyncValue.ofComponent(() -> targets.get(index).name()).onChanged(value -> applyTooltip()));
            this.location = addSyncValue(SyncValue.ofComponent(() -> targets.get(index).location()).onChanged(value -> applyTooltip()));
            this.state = addSyncValue(SyncValue.ofInt(() -> targets.get(index).state(), EMPTY).onChanged(value -> applyTooltip()));
        }

        private void applyTooltip() {
            if (name == null || location == null || state == null) return;
            int current = state.getValue();
            if (current == EMPTY) {
                setHoverTooltips(Collections.emptyList());
                return;
            }
            var lines = new ArrayList<Component>(3);
            lines.add(name.getValue());
            lines.add(location.getValue().copy().withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(STATE_KEYS[current]).withStyle(switch (current) {
                case ITEM, FLUID, BOTH -> ChatFormatting.GREEN;
                case UNLOADED -> ChatFormatting.YELLOW;
                default -> ChatFormatting.RED;
            }));
            setHoverTooltips(lines);
        }

        private int accentColor() {
            return switch (state.getValue()) {
                case EMPTY -> 0;
                case ITEM, FLUID, BOTH -> UITheme.STATUS_ONLINE;
                case UNLOADED -> UITheme.STATUS_WARNING;
                default -> UITheme.STATUS_OFFLINE;
            };
        }

        @Override
        public @Nullable Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
            var stack = icon.getValue().stack();
            if (!stack.isEmpty() && isMouseOver(getPositionX(), getPositionY() + 1, ICON, ICON, mouseX, mouseY)) return stack;
            return super.getXEIIngredientOverMouse(mouseX, mouseY);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            var font = Minecraft.getInstance().font;
            int x = getPositionX(), y = getPositionY(), width = getSizeWidth();
            int current = state.getValue();
            if (current == EMPTY) {
                var text = UITheme.clip(font, name.getValue().getString(), width);
                graphics.drawString(font, text, x, y + (UISizes.SLOT - 8) / 2, UITheme.TEXT_SECONDARY, false);
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                return;
            }
            var stack = icon.getValue().stack();
            if (!stack.isEmpty()) graphics.renderItem(stack, x, y + 1);
            int textWidth = width - TEXT_X;
            graphics.drawString(font, UITheme.clip(font, name.getValue().getString(), textWidth), x + TEXT_X, y + 1, UITheme.PANEL_TEXT, false);
            float scale = UISizes.SMALL_TEXT_SCALE;
            graphics.pose().pushPose();
            graphics.pose().translate(x + TEXT_X, y + 11, 0);
            graphics.pose().scale(scale, scale, 1);
            graphics.drawString(font, UITheme.clip(font, location.getValue().getString(), (int) (textWidth / scale)), 0, 0, UITheme.TEXT_SECONDARY, false);
            graphics.pose().popPose();
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    private static final String[] STATE_KEYS = { LAMP_EMPTY, LAMP_ITEM, LAMP_FLUID, LAMP_BOTH, LAMP_NO_STORAGE, LAMP_UNLOADED, LAMP_SELF, LAMP_MISSING_DIMENSION };

    public static StatusPanel status(Supplier<Component> bound, boolean crossDimension, boolean perTargetFace) {
        var panel = new StatusPanel();
        panel.addLine(LINE_BOUND, bound);
        var cross = Component.translatable(crossDimension ? VALUE_SUPPORTED : VALUE_UNSUPPORTED);
        panel.addLine(LINE_CROSS, () -> cross).level(() -> crossDimension ? StatusLine.Level.GOOD : StatusLine.Level.NORMAL)
                .tooltip(crossDimension ? CROSS_SUPPORTED_DETAIL : CROSS_UNSUPPORTED_DETAIL);
        var access = Component.translatable(perTargetFace ? VALUE_PER_TARGET : VALUE_FOLLOW);
        panel.addLine(LINE_ACCESS, () -> access).tooltip(perTargetFace ? ACCESS_PER_TARGET_DETAIL : ACCESS_FOLLOW_DETAIL);
        return panel;
    }

    public static UIElement listSection(String titleKey, Widget list, String... tooltipKeys) {
        var title = TextLine.translatable(0, titleKey).setColor(UITheme::panelText);
        title.layout(l -> l.flex(1));
        var header = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter()).addChild(title);
        if (tooltipKeys.length > 0) header.addChild(InfoIcon.info(tooltipKeys));
        return UIElement.section().addChildren(header, list);
    }

    public static Widget page(Widget... children) {
        var scroller = new ScrollerView("tesseract.page", UISizes.CONTENT_WIDTH, UISizes.SLOT).adaptiveWidth().setResizable(false)
                .fitPage().adaptiveHeight(PAGE_HEIGHT_LIMIT);
        scroller.addScrollViewChild(CoverUIs.page().addChildren(children));
        return UIElement.column(LayoutStyle.AUTO).addChild(scroller);
    }

    public static UIElement column(List<Widget> rows) {
        var column = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP).paddingTop(1).paddingBottom(1));
        for (var row : rows) column.addChild(row);
        return column;
    }

    public static UIElement basicPushSection() {
        return CoverUIs.section(SECTION_PUSH).addChild(Label.translatable(PUSH_BASIC, LIST_WIDTH).setColor(UITheme::panelText));
    }

    public static UIElement roundRobinSection(BooleanSupplier roundRobin, Consumer<Boolean> setRoundRobin) {
        var modes = ButtonGroup.single(2, i -> Component.translatable(i == 0 ? MODE_SEQUENTIAL : MODE_ROUND_ROBIN),
                () -> roundRobin.getAsBoolean() ? 1 : 0, i -> setRoundRobin.accept(i == 1)).horizontal();
        modes.setHoverTooltips(MODE_TOOLTIP);
        var description = Label.of(() -> Component.translatable(roundRobin.getAsBoolean() ? DESC_ROUND_ROBIN : DESC_SEQUENTIAL), LIST_WIDTH)
                .setColor(UITheme::panelText);
        return CoverUIs.section(SECTION_PUSH).addChildren(modes, new DistributionStrip(roundRobin), description);
    }

    public static UIElement directedPushSection(IntSupplier targetCount, Widget pending) {
        var rules = new StatusPanel();
        var pattern = Component.translatable(VALUE_PATTERN);
        var dispatch = Component.translatable(VALUE_DISPATCH);
        var full = Component.translatable(VALUE_FULL);
        rules.addLine(RULE_PATTERN, () -> pattern);
        rules.addLine(RULE_DISPATCH, () -> dispatch).tooltip(DISPATCH_DETAIL);
        rules.addLine(RULE_ACCEPT, () -> Component.translatable(VALUE_ACCEPT, targetCount.getAsInt())).tooltip(ACCEPT_DETAIL);
        rules.addLine(RULE_FULL, () -> full).tooltip(FULL_DETAIL);
        return CoverUIs.section(SECTION_PUSH).addChildren(rules, pending);
    }

    public static ModularUI markerUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        return HeldItemPage.create(holder, player, window -> markerPage(holder, window));
    }

    private static Widget markerPage(HeldItemUIFactory.HeldItemHolder holder, MachineWindow window) {
        boolean remote = holder.isRemote();
        Supplier<ItemStack> held = holder::getHeld;
        var targets = new Targets(() -> holder.player.level(), null, () -> markerTargets(held.get()),
                () -> TesseractTargetMarker.signature(held.get()), Component.empty());
        window.registerPopup(FACE_POPUP, index -> remote || index < TesseractTargetMarker.count(held.get()) ?
                facePopup(targets, index, () -> TesseractTargetMarker.getFace(held.get(), index), face -> TesseractTargetMarker.setFace(held.get(), index, face)) : null);

        var left = ButtonGroup.single(2, i -> Component.translatable(i == 0 ? MARKER_LEFT_TAIL : MARKER_LEFT_REMOVE),
                () -> TesseractTargetMarker.getLeftMode(held.get()), i -> TesseractTargetMarker.setLeftMode(held.get(), i)).horizontal();
        left.setHoverTooltips(MARKER_LEFT_TAIL_TOOLTIP, MARKER_LEFT_REMOVE_TOOLTIP);
        var face = ButtonGroup.single(2, i -> Component.translatable(i == 0 ? MARKER_FACE_CLICKED : MARKER_FACE_OPPOSITE),
                () -> TesseractTargetMarker.getFaceMode(held.get()), i -> TesseractTargetMarker.setFaceMode(held.get(), i)).horizontal();
        face.setHoverTooltips(MARKER_FACE_TOOLTIP);
        var modes = CoverUIs.section(MARKER_SECTION_MODE).addChildren(
                TextLine.translatable(LayoutStyle.AUTO, MARKER_LEFT).setColor(UITheme::textSecondary), left,
                TextLine.translatable(LayoutStyle.AUTO, MARKER_FACE).setColor(UITheme::textSecondary), face);

        var count = TextLine.of(0, () -> Component.translatable(MARKER_TARGETS, TesseractTargetMarker.count(held.get()))).setColor(UITheme::panelText);
        count.layout(l -> l.flex(1));
        var clear = Button.translatable(UISizes.BUTTON_WIDTH, MARKER_CLEAR).setVariant(UITheme.ButtonVariant.DANGER)
                .setOnServerClick(() -> TesseractTargetMarker.clearAllPatternFaces(held.get()))
                .disabled(() -> TesseractTargetMarker.count(held.get()) == 0, REASON_EMPTY);
        var header = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter())
                .addChildren(count, InfoIcon.info(MARKER_HELP_ADD, MARKER_HELP_REMOVE, MARKER_HELP_WRITE, MARKER_HELP_READ), clear);
        var rows = new ServerRows<>(remote, ByteStreamCodec.INT_CODEC, () -> indices(TesseractTargetMarker.count(held.get())),
                () -> TesseractTargetMarker.count(held.get()), index -> markerRow(window, held, targets, index), Component.translatable(MARKER_EMPTY));
        rows.layout(l -> l.paddingTop(1).paddingBottom(1));
        var list = UIElement.section().addChildren(header, rows);

        var preview = new StatusPanel();
        preview.addLine(PREVIEW_DIRECTED, () -> Component.translatable(PREVIEW_TARGETS, TesseractTargetMarker.count(held.get())))
                .tooltip(PREVIEW_DIRECTED_DETAIL);
        preview.addLine(PREVIEW_ADVANCED, () -> advancedPreview(holder)).level(() -> skipped(holder) > 0 ? StatusLine.Level.WARNING : StatusLine.Level.NORMAL)
                .tooltip(PREVIEW_ADVANCED_DETAIL);
        preview.addLine(PREVIEW_BASIC, () -> Component.translatable(sameDimension(holder) > 0 ? PREVIEW_FIRST : PREVIEW_NONE))
                .tooltip(PREVIEW_BASIC_DETAIL);

        return UIElement.column(LayoutStyle.AUTO).layout(l -> l.minWidth(MARKER_PAGE_WIDTH).gapAll(UISizes.SECTION_GAP))
                .addChildren(modes, list, preview);
    }

    private static UIElement markerRow(MachineWindow window, Supplier<ItemStack> held, Targets targets, int index) {
        var tail = TextColor.fromRgb(UITheme.STATUS_TEXT_WARNING & 0xFFFFFF);
        var badge = index(() -> {
            var number = Component.literal(String.valueOf(index + 1));
            return TesseractTargetMarker.isTail(held.get(), index) ? number.withStyle(style -> style.withColor(tail)) : number;
        }, Component.translatable(MARKER_BADGE, index + 1, index + 1), Component.translatable(MARKER_BADGE_TAIL));
        var face = face(targets, index);
        bindFacePopup(face, window, index);
        var up = Button.icon(UITheme.ARROW_UP).setOnServerClick(() -> TesseractTargetMarker.move(held.get(), index, -1))
                .disabled(() -> index == 0, REASON_FIRST);
        up.setHoverTooltips(MOVE_UP);
        var down = Button.icon(UITheme.ARROW_DOWN).setOnServerClick(() -> TesseractTargetMarker.move(held.get(), index, 1))
                .disabled(() -> index >= TesseractTargetMarker.count(held.get()) - 1, REASON_LAST);
        down.setHoverTooltips(MOVE_DOWN);
        var remove = Button.glyph("×").setVariant(UITheme.ButtonVariant.DANGER)
                .setOnServerClick(() -> TesseractTargetMarker.remove(held.get(), index));
        remove.setHoverTooltips(REMOVE);
        return row(badge, targets, index, face, up, down, remove);
    }

    public static Popup facePopup(Targets targets, int index, Supplier<Direction> current, Consumer<Direction> select) {
        return Popup.of(() -> targets.get(index).icon(), () -> Component.translatable(POPUP_TITLE, index + 1, targets.get(index).name()),
                content -> content.addChildren(FacePicker.grid(current, select),
                        Label.translatable(POPUP_HINT, UISizes.POPUP_CONTENT_WIDTH).setColor(UITheme::panelText)));
    }

    public static void bindFacePopup(FaceNet face, MachineWindow window, int index) {
        face.setOnClientClick(() -> window.togglePopup(FACE_POPUP, index));
        face.setSelected(() -> window.isPopupOpen(FACE_POPUP, index));
    }

    private static List<Target> markerTargets(ItemStack stack) {
        var ordered = TesseractTargetMarker.getOrderedTargets(stack);
        if (ordered.isEmpty()) return Collections.emptyList();
        var result = new ArrayList<Target>(ordered.size());
        for (var target : ordered) result.add(new Target(target.pos(), target.face()));
        return result;
    }

    private static List<Integer> indices(int count) {
        var result = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) result.add(i);
        return result;
    }

    private static int sameDimension(HeldItemUIFactory.HeldItemHolder holder) {
        var dimension = holder.player.level().dimension();
        int count = 0;
        for (var target : TesseractTargetMarker.getOrderedTargets(holder.getHeld())) {
            if (target.pos().dimension() == dimension) count++;
        }
        return count;
    }

    private static int skipped(HeldItemUIFactory.HeldItemHolder holder) {
        return TesseractTargetMarker.count(holder.getHeld()) - sameDimension(holder);
    }

    private static Component advancedPreview(HeldItemUIFactory.HeldItemHolder holder) {
        int bound = Math.min(AdvancedTesseractMachine.MAX_TARGETS, sameDimension(holder));
        int skipped = skipped(holder);
        return skipped > 0 ? Component.translatable(PREVIEW_SKIPPED, bound, skipped) : Component.translatable(PREVIEW_TARGETS, bound);
    }
}
