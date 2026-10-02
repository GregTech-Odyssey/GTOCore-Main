package com.gtocore.api.gui.overview;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.factory.MachineSubWindowFactory;
import com.gregtechceu.gtceu.api.gui.fancy.SubWindowButton;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IMachineSubWindows;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblockpro.BuildUpload;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Layout;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PlayerSupply;
import com.gregtechceu.gtceu.api.machine.multiblockpro.StructureBuild;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.network.RequestThrottle;
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.data.UIChannel;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.gto.datasynclib.datastream.codec.ByteStreamCodec;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@DataGeneratorScanned
public final class OverviewWidget extends WidgetGroup implements UIChannel.Host {

    @RegisterLanguage(cn = "正常运行", en = "Connected")
    public static final String LANG_LEGEND_OK = "gtocore.overview.legend.ok";
    @RegisterLanguage(cn = "未成型或已断开", en = "Unformed or detached")
    public static final String LANG_LEGEND_DETACHED = "gtocore.overview.legend.detached";
    @RegisterLanguage(cn = "空接口（未被遮挡时可点击）", en = "Free port (clickable when not hidden)")
    public static final String LANG_LEGEND_ANCHOR = "gtocore.overview.legend.anchor";
    @RegisterLanguage(cn = "预览中的结构", en = "Structure being previewed")
    public static final String LANG_LEGEND_GHOST = "gtocore.overview.legend.ghost";
    @RegisterLanguage(cn = "这个接口不能接入这一类", en = "This port does not accept this kind")
    public static final String LANG_CATEGORY_UNAVAILABLE = "gtocore.overview.category_unavailable";
    @RegisterLanguage(cn = "没有可接入的结构", en = "Nothing fits this port")
    public static final String LANG_NONE = "gtocore.overview.none";
    @RegisterLanguage(cn = "朝向 %s / %s", en = "Orientation %s / %s")
    public static final String LANG_ORIENTATION = "gtocore.overview.orientation";
    @RegisterLanguage(cn = "旋转", en = "Rotate")
    public static final String LANG_ROTATE = "gtocore.overview.rotate";
    @RegisterLanguage(cn = "换一个控制器位置或绕对接轴旋转", en = "Move the controller to another port position or turn around the docking axis")
    public static final String LANG_ROTATE_TOOLTIP = "gtocore.overview.rotate.tooltip";
    @RegisterLanguage(cn = "这里放不下：会与已有方块重叠", en = "Does not fit here: it would overlap existing blocks")
    public static final String LANG_BLOCKED = "gtocore.overview.blocked";
    @RegisterLanguage(cn = "这个接口放不下该结构：会与已有方块重叠，或该结构不允许这个朝向", en = "This structure does not fit here: it would overlap existing blocks or cannot face this way")
    public static final String LANG_NO_FIT = "gtocore.overview.no_fit";
    @RegisterLanguage(cn = "只有一种可行朝向", en = "Only one orientation fits")
    public static final String LANG_SINGLE = "gtocore.overview.single";
    @RegisterLanguage(cn = "返回机器界面", en = "Back to the machine")
    public static final String LANG_BACK = "gtocore.overview.back";
    @RegisterLanguage(cn = "缺少控制器：%s", en = "Missing controller: %s")
    public static final String LANG_NO_CONTROLLER = "gtocore.overview.no_controller";
    @RegisterLanguage(cn = "缺少方块：%s", en = "Missing block: %s")
    public static final String LANG_MISSING_BLOCK = "gtocore.overview.missing_block";
    @RegisterLanguage(cn = "接口已被占用或已失效，请重新选择", en = "The port is taken or no longer valid; choose again")
    public static final String LANG_STALE = "gtocore.overview.stale";
    @RegisterLanguage(cn = "结构规模超出上限，仅显示部分模块", en = "The structure exceeds the display limit; only some modules are shown")
    public static final String LANG_TRUNCATED = "gtocore.overview.truncated";
    @RegisterLanguage(cn = "方块数据超出上限，仅显示结构外形", en = "The block data exceeds the display limit; only structure outlines are shown")
    public static final String LANG_COARSE = "gtocore.overview.coarse";

    private static final int SNAPSHOT = 3;
    private static final int MAX_VALUES = 64;
    private static final RequestThrottle BUILD_THROTTLE = new RequestThrottle(20);
    private static final ByteStreamCodec<BuildRequest> BUILD_REQUEST = ByteStreamCodec.of((buf, request) -> {
        buf.writeVarInt(request.anchor());
        buf.writeResourceLocation(request.definition());
        buf.writeVarIntArray(request.values());
        buf.writeBlockPos(request.port());
        buf.writeEnum(request.front());
        buf.writeEnum(request.up());
        buf.writeVarInt(request.upload());
    }, buf -> new BuildRequest(buf.readVarInt(), buf.readResourceLocation(), buf.readVarIntArray(MAX_VALUES), buf.readBlockPos(),
            buf.readEnum(Direction.class), buf.readEnum(Direction.class), buf.readVarInt()));

    private record BuildRequest(int anchor, ResourceLocation definition, int[] values, BlockPos port, Direction front, Direction up, int upload) {}

    private final MultiblockControllerMachine host;
    private final OverviewAdapter adapter;
    @Nullable
    private OverviewScan scan;
    private int sentVersion = -1;
    @Nullable
    private OverviewSnapshot snapshot;
    @Nullable
    private Consumer<OverviewSnapshot> sink;
    private final UIChannel channel = new UIChannel(this);
    private final RPC<BuildRequest> buildRequest;
    private final RPC<Unit> backRequest;

    public OverviewWidget(MultiblockControllerMachine host, OverviewAdapter adapter) {
        super(0, 0, 320, 240);
        this.host = host;
        this.adapter = adapter;
        buildRequest = addRPC(BUILD_REQUEST, this::serverBuild);
        backRequest = addRPC(player -> {
            if (player instanceof ServerPlayer serverPlayer) MachineSubWindowFactory.openMachine(serverPlayer, host);
        });
    }

    @Override
    public UIChannel getChannel() {
        return channel;
    }

    public static SubWindowButton button(IMachineSubWindows machine, String key, MultiblockMachineDefinition definition, OverviewAdapter adapter) {
        return new SubWindowButton(machine, key, new ItemStackTexture(definition.asStack()), Component.translatable(adapter.titleKey()),
                Component.translatable(adapter.openKey()).withStyle(ChatFormatting.GRAY));
    }

    public MultiblockControllerMachine getHost() {
        return host;
    }

    public OverviewAdapter getAdapter() {
        return adapter;
    }

    public void setSink(Consumer<OverviewSnapshot> sink) {
        this.sink = sink;
        if (snapshot != null) sink.accept(snapshot);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        channel.prime();
        if (isRemote()) OverviewView.attach(this);
        else if (gui != null) gui.registerCloseListener(this::releaseScan);
    }

    @Override
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        setSize(new Size(screenWidth, screenHeight));
        super.onScreenSizeUpdate(screenWidth, screenHeight);
    }

    private OverviewScan scan() {
        if (scan == null) scan = OverviewScan.acquire(host, adapter);
        return scan;
    }

    private void releaseScan() {
        if (scan != null) scan.release();
        scan = null;
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        channel.writeInitialData(buffer);
        var current = scan();
        sentVersion = current.version();
        current.payload().write(buffer);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        channel.readInitialData(buffer);
        receive(OverviewSnapshot.read(buffer));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        channel.detectAndSendChanges();
        var current = scan();
        current.tick();
        if (current.version() == sentVersion) return;
        sentVersion = current.version();
        writeUpdateInfo(SNAPSHOT, current.payload()::write);
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == SNAPSHOT) receive(OverviewSnapshot.read(buffer));
        else if (!channel.readUpdateInfo(id, buffer)) super.readUpdateInfo(id, buffer);
    }

    private void receive(OverviewSnapshot next) {
        snapshot = next;
        if (sink != null) sink.accept(next);
    }

    public void requestBuild(int anchor, MultiblockMachineDefinition definition, int[] values, OverviewDocking.DockPose pose, int upload) {
        buildRequest.send(new BuildRequest(anchor, definition.getId(), values, pose.port(), pose.front(), pose.up(), upload));
    }

    public void requestBack() {
        backRequest.send(Unit.INSTANCE);
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (!channel.handleClientAction(id, buffer)) super.handleClientAction(id, buffer);
    }

    private void serverBuild(@Nullable Player player, BuildRequest request) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!BUILD_THROTTLE.tryAcquire(serverPlayer) || !serverPlayer.mayBuild()) return;
        build(serverPlayer, request.anchor(), request.definition(), request.values(), request.port(), request.front(), request.up(), request.upload());
    }

    private void build(ServerPlayer player, int anchorIndex, ResourceLocation id, int[] values, BlockPos port, Direction front, Direction up, int upload) {
        var level = player.serverLevel();
        if (level != host.getLevel()) return;
        var current = scan().snapshot();
        if (current == null || anchorIndex < 0 || anchorIndex >= current.anchors().size()) {
            player.sendSystemMessage(Component.translatable(LANG_STALE));
            return;
        }
        var anchor = current.anchors().get(anchorIndex);
        if (!anchor.cells().contains(port) || !(GTRegistries.MACHINES.get(id) instanceof MultiblockMachineDefinition definition) ||
                !adapter.members(anchor).contains(definition)) {
            player.sendSystemMessage(Component.translatable(LANG_STALE));
            return;
        }
        var structure = definition.displayStructure();
        var layout = structure == null ? null : structure.layout(values);
        var choices = layout == null ? null : BuildUpload.take(player, upload, definition, values, layout.cells().size());
        if (choices == null) {
            player.sendSystemMessage(Component.translatable(StructureBuild.INVALID));
            return;
        }
        if (OverviewCapture.occupied(level, anchor.cells())) {
            player.sendSystemMessage(Component.translatable(LANG_STALE));
            return;
        }
        if (!level.isLoaded(port) || !level.mayInteract(player, port) || !adapter.acceptsPortBlock(level.getBlockState(port))) {
            player.sendSystemMessage(Component.translatable(LANG_BLOCKED));
            return;
        }
        var controllerItem = definition.asItem();
        var supply = player.isCreative() ? null : PlayerSupply.of(player, true);
        if (supply != null && supply.count().getLong(controllerItem) <= 0) {
            player.sendSystemMessage(Component.translatable(LANG_NO_CONTROLLER, definition.asStack().getHoverName()));
            return;
        }
        restrictToCandidates(layout, choices);
        OverviewDocking.DockPose matched = null;
        for (var pose : adapter.orientations(definition, layout, choices, anchor, level)) {
            if (pose.same(port, front, up)) {
                matched = pose;
                break;
            }
        }
        if (matched == null) {
            player.sendSystemMessage(Component.translatable(LANG_BLOCKED));
            return;
        }
        if (supply != null && !supply.take(controllerItem)) {
            player.sendSystemMessage(Component.translatable(LANG_NO_CONTROLLER, definition.asStack().getHoverName()));
            return;
        }
        adapter.prepare(player, supply, definition, layout, choices, matched, level);
        var existing = level.getBlockState(port);
        if (supply != null && !existing.isAir()) {
            for (var drop : Block.getDrops(existing, level, port, level.getBlockEntity(port), player, ItemStack.EMPTY)) {
                player.getInventory().placeItemBackInInventory(drop);
            }
        }
        var state = OverviewDocking.controllerState(definition, front, up);
        level.setBlock(port, state, 3);
        state.getBlock().setPlacedBy(level, port, state, player, new ItemStack(controllerItem));
        scan().invalidate();
        if (MetaMachine.getMachine(level, port) instanceof IMultiController controller) {
            StructureBuild.build(player, controller, layout, choices);
        }
    }

    private static void restrictToCandidates(Layout layout, Item[] choices) {
        var cells = layout.cells();
        for (int i = 0; i < choices.length; i++) {
            var item = choices[i];
            if (item != null && !cells.get(i).predicate().candidateItems().contains(item)) choices[i] = null;
        }
    }
}
