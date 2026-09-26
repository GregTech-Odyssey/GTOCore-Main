package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.gui.ServerRows;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.api.research.techtree.ui.TechNodeDetails;
import com.gtocore.api.research.techtree.ui.TechNodeLine;
import com.gtocore.api.research.techtree.ui.TechTreePage;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.item.DataCenterAggregationTerminal;
import com.gtocore.common.item.DataCenterAggregationTerminal.Binding;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.InfoIcon;
import com.gregtechceu.gtceu.uipro.elements.ItemView;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.FlexWrap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@DataGeneratorScanned
public final class DataCenterAggregationUI {

    private static final int CARD_WIDTH = UISizes.CONTENT_WIDTH;
    private static final int GRID_WIDTH = 2 * CARD_WIDTH + UISizes.SECTION_GAP;
    private static final int SERVER_HEIGHT_LIMIT = Integer.MAX_VALUE / 4;
    private static final int HEAVY_REFRESH_TICKS = 20;
    private static final int CONFIRM_TICKS = 60;
    private static final Component NONE = Component.literal(DataCenter.NO_VALUE);

    @RegisterLanguage(cn = "数据中心总览", en = "Data Center Overview")
    private static final String TITLE = "gtocore.data_center_aggregation.title";
    @RegisterLanguage(cn = "已绑定", en = "Bound")
    private static final String LINE_BOUND = "gtocore.data_center_aggregation.line.bound";
    @RegisterLanguage(cn = "研究中", en = "Researching")
    private static final String LINE_RESEARCHING = "gtocore.data_center_aggregation.line.researching";
    @RegisterLanguage(cn = "空闲可用", en = "Idle")
    private static final String LINE_IDLE = "gtocore.data_center_aggregation.line.idle";
    @RegisterLanguage(cn = "算力上限合计", en = "Total max CWU")
    private static final String LINE_TOTAL_CWU = "gtocore.data_center_aggregation.line.total_cwu";
    @RegisterLanguage(cn = "发起人", en = "Initiator")
    private static final String LINE_REQUESTER = "gtocore.data_center_aggregation.line.requester";
    @RegisterLanguage(cn = "%s / %s", en = "%s / %s")
    private static final String VALUE_BOUND = "gtocore.data_center_aggregation.value.bound";
    @RegisterLanguage(cn = "%s 台", en = "%s")
    private static final String VALUE_UNITS = "gtocore.data_center_aggregation.value.units";
    @RegisterLanguage(cn = "其中 %s 台暂停", en = "%s paused")
    private static final String DETAIL_PAUSED = "gtocore.data_center_aggregation.detail.paused";
    @RegisterLanguage(cn = "玻璃 %s · 数据槽位 %s · 已存配方 %s", en = "Glass %s · Data slots %s · Recipes %s")
    private static final String DETAIL_CAPACITY = "gtocore.data_center_aggregation.detail.capacity";
    @RegisterLanguage(cn = "已绑定的数据中心", en = "Bound Data Centers")
    private static final String HEADER_CARDS = "gtocore.data_center_aggregation.header.cards";
    @RegisterLanguage(cn = "没有绑定的数据中心", en = "No bound Data Centers")
    private static final String EMPTY = "gtocore.data_center_aggregation.empty";
    @RegisterLanguage(cn = "全部解除", en = "Unbind all")
    private static final String UNBIND_ALL = "gtocore.data_center_aggregation.unbind_all";
    @RegisterLanguage(cn = "再次点击确认", en = "Click to confirm")
    private static final String UNBIND_ALL_CONFIRM = "gtocore.data_center_aggregation.unbind_all.confirm";
    @RegisterLanguage(cn = "解除终端与所有数据中心的绑定，不影响它们正在进行的研究", en = "Unbind every Data Center from this terminal; their ongoing research is not affected")
    private static final String UNBIND_ALL_TIP = "gtocore.data_center_aggregation.unbind_all.tip";
    @RegisterLanguage(cn = "没有绑定的数据中心", en = "No bound Data Centers")
    private static final String NOTHING_BOUND = "gtocore.data_center_aggregation.nothing_bound";
    @RegisterLanguage(cn = "解除绑定（不影响正在进行的研究）", en = "Unbind (ongoing research is not affected)")
    private static final String UNBIND_TIP = "gtocore.data_center_aggregation.unbind.tip";
    @RegisterLanguage(cn = "研究任务存在各台数据中心上：它们各自记录研究节点与发起人，消耗电力与冷却液、从算力网络获取 CWU，并累加到团队进度",
                      en = "Research runs on each Data Center: each keeps its own node and initiator, consumes power and coolant, draws CWU from its computation network and adds it to team progress")
    private static final String HELP_TASK = "gtocore.data_center_aggregation.help.task";
    @RegisterLanguage(cn = "每台一次研究一个节点，多台可同时研究不同节点", en = "Each researches one node at a time; several can research different nodes at once")
    private static final String HELP_PARALLEL = "gtocore.data_center_aggregation.help.parallel";
    @RegisterLanguage(cn = "算力上限取决于玻璃等级、数据访问仓的槽位数与已存配方数", en = "The CWU cap depends on glass tier, Data Access Hatch slots and stored recipes")
    private static final String HELP_CAPACITY = "gtocore.data_center_aggregation.help.capacity";
    @RegisterLanguage(cn = "启动研究时交给空闲且算力上限最高的一台", en = "Launching research goes to the idle one with the highest CWU cap")
    private static final String HELP_DISPATCH = "gtocore.data_center_aggregation.help.dispatch";

    @RegisterLanguage(cn = "研究中", en = "Researching")
    private static final String STATE_RESEARCHING = "gtocore.data_center_aggregation.state.researching";
    @RegisterLanguage(cn = "研究暂停", en = "Paused")
    private static final String STATE_PAUSED = "gtocore.data_center_aggregation.state.paused";
    @RegisterLanguage(cn = "空闲", en = "Idle")
    private static final String STATE_IDLE = "gtocore.data_center_aggregation.state.idle";
    @RegisterLanguage(cn = "区块未加载", en = "Chunk unloaded")
    private static final String STATE_UNLOADED = "gtocore.data_center_aggregation.state.unloaded";
    @RegisterLanguage(cn = "已不存在", en = "Missing")
    private static final String STATE_MISSING = "gtocore.data_center_aggregation.state.missing";
    @RegisterLanguage(cn = "无权访问", en = "No access")
    private static final String STATE_NO_ACCESS = "gtocore.data_center_aggregation.state.no_access";
    @RegisterLanguage(cn = "数据中心所在区块未加载，无法读取状态，研究也不会推进", en = "The chunk is not loaded; its status cannot be read and research does not progress")
    private static final String DETAIL_UNLOADED = "gtocore.data_center_aggregation.detail.unloaded";
    @RegisterLanguage(cn = "该位置已没有数据中心，可以解除绑定", en = "There is no Data Center at this position any more; you can unbind it")
    private static final String DETAIL_MISSING = "gtocore.data_center_aggregation.detail.missing";

    @RegisterLanguage(cn = "正在研究中（%s 台）", en = "Researching (%s)")
    private static final String RESEARCHING_ON = "gtocore.data_center_aggregation.research.researching_on";
    @RegisterLanguage(cn = "点击取消这些数据中心对该节点的研究", en = "Click to cancel this node on those Data Centers")
    private static final String TIP_CANCEL = "gtocore.data_center_aggregation.research.tip_cancel";
    @RegisterLanguage(cn = "交给空闲且算力上限最高的数据中心研究", en = "Assign to the idle Data Center with the highest CWU cap")
    private static final String TIP_LAUNCH = "gtocore.data_center_aggregation.research.tip_launch";
    @RegisterLanguage(cn = "没有空闲的数据中心", en = "No idle Data Center")
    private static final String NO_IDLE = "gtocore.data_center_aggregation.research.no_idle";

    private static final SyncValue.Codec<Binding> BINDING_CODEC = new SyncValue.Codec<>() {

        @Override
        public void write(FriendlyByteBuf buf, Binding value) {
            buf.writeResourceLocation(value.dimension().location());
            buf.writeLong(value.pos().asLong());
        }

        @Override
        public Binding read(FriendlyByteBuf buf) {
            return new Binding(ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()), BlockPos.of(buf.readLong()));
        }
    };

    private DataCenterAggregationUI() {}

    public static ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        var context = new Context(holder, player);
        var window = TechTreePage.window(new TechTreePage.Options()
                .home(focus -> new Overview(context, focus))
                .extra(context::attachResearchSection)
                .researchingAll(context::researchingNodes));
        return new ModularUI(176, 166, holder, player).widget(window);
    }

    private enum State {

        RESEARCHING(STATE_RESEARCHING, StatusLine.Level.GOOD),
        PAUSED(STATE_PAUSED, StatusLine.Level.WARNING),
        IDLE(STATE_IDLE, StatusLine.Level.NORMAL),
        UNFORMED(DataCenter.LANG_STATE_UNFORMED, StatusLine.Level.ERROR),
        UNLOADED(STATE_UNLOADED, StatusLine.Level.WARNING),
        MISSING(STATE_MISSING, StatusLine.Level.ERROR),
        NO_ACCESS(STATE_NO_ACCESS, StatusLine.Level.ERROR);

        private final Component text;
        private final StatusLine.Level level;

        State(String key, StatusLine.Level level) {
            this.text = Component.translatable(key);
            this.level = level;
        }
    }

    private static final class Entry {

        private final Binding binding;
        @Nullable
        private DataCenter machine;
        private State state = State.UNLOADED;
        @Nullable
        private TechNode node;
        @Nullable
        private UUID requester;
        private long maxCwu;
        private int slots;
        private int recipes;
        private int glassTier;
        private int heavyTick = Integer.MIN_VALUE;
        @Nullable
        private UUID requesterTextFor;
        private Component requesterText = NONE;
        private long maxCwuTextFor = -1;
        private Component maxCwuText = NONE;
        private Component capacityText = Component.empty();
        private Component detailText = Component.empty();

        private Entry(Binding binding) {
            this.binding = binding;
        }

        private void update(MinecraftServer server, Player player, int tick) {
            machine = null;
            node = null;
            requester = null;
            var level = server.getLevel(binding.dimension());
            var pos = binding.pos();
            if (level == null) {
                state = State.MISSING;
            } else if (!level.isLoaded(pos)) {
                state = State.UNLOADED;
            } else if (!(MetaMachine.getMachine(level, pos) instanceof DataCenter center)) {
                state = State.MISSING;
            } else if (!MachineOwner.canOpenOwnerMachine(player, center)) {
                state = State.NO_ACCESS;
            } else {
                machine = center;
                node = center.selectedNode;
                requester = node == null ? null : center.researchRequester;
                if (!center.isFormed()) state = State.UNFORMED;
                else if (node == null) state = State.IDLE;
                else state = center.isActive() && !center.isResearchBlocked() ? State.RESEARCHING : State.PAUSED;
                if (tick < heavyTick || tick - heavyTick >= HEAVY_REFRESH_TICKS) {
                    heavyTick = tick;
                    boolean formed = center.isFormed();
                    maxCwu = formed ? center.getCWUInputLimit() : 0L;
                    int newSlots = center.getTotalDataSlots();
                    int newRecipes = formed ? center.getExistRecipes().size() : 0;
                    int newGlass = Math.max(0, Math.min(center.getCasingTier(GTORecipeDataKeys.GLASS_TIER), GTValues.VNF.length - 1));
                    if (newSlots != slots || newRecipes != recipes || newGlass != glassTier || capacityText.getContents() == Component.empty().getContents()) {
                        slots = newSlots;
                        recipes = newRecipes;
                        glassTier = newGlass;
                        capacityText = Component.translatable(DETAIL_CAPACITY, GTValues.VNF[glassTier], FormattingUtil.formatNumbers(slots), FormattingUtil.formatNumbers(recipes));
                    }
                }
            }
            if (requester == null ? requesterTextFor != null : !requester.equals(requesterTextFor)) {
                requesterTextFor = requester;
                requesterText = requester == null ? NONE : Component.literal(server.getProfileCache() == null ? requester.toString() :
                        server.getProfileCache().get(requester).map(profile -> profile.getName()).orElse(requester.toString()));
            }
            long shownCwu = machine == null ? -1 : maxCwu;
            if (shownCwu != maxCwuTextFor) {
                maxCwuTextFor = shownCwu;
                maxCwuText = shownCwu < 0 ? NONE : Component.literal(FormattingUtil.formatNumbers(shownCwu) + " CWU/t");
            }
            detailText = switch (state) {
                case PAUSED -> Component.translatable(machine != null && machine.isResearchBlocked() ? DataCenter.LANG_PREREQUISITES_NOT_RESEARCHED : DataCenter.LANG_RESEARCH_IDLE);
                case UNLOADED -> Component.translatable(DETAIL_UNLOADED);
                case MISSING -> Component.translatable(DETAIL_MISSING);
                default -> Component.empty();
            };
        }

        private boolean isIdle() {
            return machine != null && state == State.IDLE;
        }
    }

    private static final class Context {

        private final HeldItemUIFactory.HeldItemHolder holder;
        private final Player player;
        @Nullable
        private final MinecraftServer server;
        private final O2OOpenCacheHashMap<Binding, Entry> entries = new O2OOpenCacheHashMap<>();
        private final ArrayList<Entry> current = new ArrayList<>();
        private final ArrayList<TechNode> researching = new ArrayList<>();
        private List<Binding> bindings = Collections.emptyList();
        @Nullable
        private Tag bindingsTag;
        private boolean bindingsRead;
        private int version;
        private int refreshedTick = Integer.MIN_VALUE;
        private int confirmUntil = Integer.MIN_VALUE;
        private int researchingCount;
        private int pausedCount;
        private int idleCount;
        private long totalCwu = -1;
        private Component totalCwuText = NONE;
        private int boundTextFor = -1;
        private Component boundText = NONE;
        private int researchingTextFor = -1;
        private Component researchingText = NONE;
        private int idleTextFor = -1;
        private Component idleText = NONE;

        private Context(HeldItemUIFactory.HeldItemHolder holder, Player player) {
            this.holder = holder;
            this.player = player;
            this.server = player.level().isClientSide ? null : player.getServer();
        }

        private ItemStack stack() {
            return holder.held;
        }

        private void refresh() {
            if (server == null) return;
            int tick = server.getTickCount();
            if (tick == refreshedTick) return;
            refreshedTick = tick;
            var tag = stack().getTag();
            var latestTag = tag == null ? null : tag.get(DataCenterAggregationTerminal.TAG_BINDINGS);
            if (!bindingsRead || latestTag != bindingsTag) {
                bindingsRead = true;
                bindingsTag = latestTag;
                bindings = DataCenterAggregationTerminal.getBindings(stack());
                version++;
            }
            current.clear();
            researching.clear();
            researchingCount = 0;
            pausedCount = 0;
            idleCount = 0;
            long cwu = 0;
            for (var binding : bindings) {
                var entry = entries.computeIfAbsent(binding, Entry::new);
                entry.update(server, player, tick);
                current.add(entry);
                if (entry.node != null && entry.machine != null) {
                    researchingCount++;
                    if (entry.state == State.PAUSED) pausedCount++;
                    if (!researching.contains(entry.node)) researching.add(entry.node);
                }
                if (entry.isIdle()) idleCount++;
                if (entry.machine != null) cwu += entry.maxCwu;
            }
            if (cwu != totalCwu) {
                totalCwu = cwu;
                totalCwuText = Component.literal(FormattingUtil.formatNumbers(cwu) + " CWU/t");
            }
        }

        private void invalidate() {
            refreshedTick = Integer.MIN_VALUE;
        }

        private List<Binding> bindings() {
            refresh();
            return bindings;
        }

        private int version() {
            refresh();
            return version;
        }

        private List<TechNode> researchingNodes() {
            refresh();
            return researching;
        }

        @Nullable
        private Entry entry(Binding binding) {
            refresh();
            return server == null ? null : entries.get(binding);
        }

        private boolean isConfirming() {
            return server != null && server.getTickCount() < confirmUntil;
        }

        private void unbindAll() {
            if (server == null) return;
            if (isConfirming()) {
                confirmUntil = Integer.MIN_VALUE;
                DataCenterAggregationTerminal.unbindAll(stack());
                invalidate();
            } else if (!bindings().isEmpty()) {
                confirmUntil = server.getTickCount() + CONFIRM_TICKS;
            }
        }

        private void unbind(Binding binding) {
            if (server == null) return;
            if (DataCenterAggregationTerminal.unbind(stack(), binding)) invalidate();
        }

        private int researchingCount(TechNode node) {
            refresh();
            int count = 0;
            for (var entry : current) {
                if (entry.machine != null && entry.node == node) count++;
            }
            return count;
        }

        @Nullable
        private DataCenter bestIdle() {
            refresh();
            Entry best = null;
            for (var entry : current) {
                if (entry.isIdle() && (best == null || entry.maxCwu > best.maxCwu)) best = entry;
            }
            return best == null ? null : best.machine;
        }

        private void toggleResearch(TechNode node) {
            if (server == null) return;
            invalidate();
            if (researchingCount(node) > 0) {
                for (var entry : current) {
                    if (entry.machine != null && entry.node == node) entry.machine.cancelResearch(player);
                }
            } else {
                var best = bestIdle();
                if (best != null) best.startResearch(player, node);
            }
            invalidate();
        }

        private void attachResearchSection(UIElement section, TechNode node) {
            var count = section.addSyncValue(SyncValue.ofInt(() -> researchingCount(node), 0));
            var button = Button.text(LayoutStyle.AUTO, () -> count.getValue() > 0 ? Component.translatable(RESEARCHING_ON, count.getValue()).getString() :
                    Component.translatable(DataCenter.LANG_DATA_ACCESS_LAUNCH_RESEARCH).getString())
                    .setVariant(() -> count.getValue() > 0 ? UITheme.ButtonVariant.DANGER : UITheme.ButtonVariant.CONFIRM)
                    .setOnServerClick(() -> toggleResearch(node))
                    .bindTooltip(() -> Component.translatable(researchingCount(node) > 0 ? TIP_CANCEL : TIP_LAUNCH));
            button.disabled(() -> TechTreeSavedData.isUnlocked(TechTreeSavedData.getTeamUUID(player), node), TechNodeDetails.ALREADY_UNLOCKED);
            var gate = UIElement.column(LayoutStyle.AUTO).addChild(button);
            gate.disabled(() -> researchingCount(node) == 0 && bestIdle() == null, NO_IDLE);
            section.disabled(() -> researchingCount(node) == 0 && !TechTreeSavedData.isPrerequisitesUnlocked(TechTreeSavedData.getTeamUUID(player), node),
                    DataCenter.LANG_PREREQUISITES_NOT_RESEARCHED);
            section.addChild(gate);
        }

        private Component boundText() {
            refresh();
            int count = bindings.size();
            if (count != boundTextFor) {
                boundTextFor = count;
                boundText = Component.translatable(VALUE_BOUND, count, DataCenterAggregationTerminal.MAX_BINDINGS);
            }
            return boundText;
        }

        private Component researchingText() {
            refresh();
            if (researchingCount != researchingTextFor) {
                researchingTextFor = researchingCount;
                researchingText = Component.translatable(VALUE_UNITS, researchingCount);
            }
            return researchingText;
        }

        private Component idleText() {
            refresh();
            if (idleCount != idleTextFor) {
                idleTextFor = idleCount;
                idleText = Component.translatable(VALUE_UNITS, idleCount);
            }
            return idleText;
        }
    }

    private record Overview(Context context, Consumer<TechNode> focus) implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            boolean remote = widget.isRemote();
            var page = UIElement.column(GRID_WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP));

            var summary = new StatusPanel();
            summary.addLine(LINE_BOUND, context::boundText);
            summary.addLine(LINE_RESEARCHING, context::researchingText)
                    .level(() -> {
                        context.refresh();
                        if (context.pausedCount > 0) return StatusLine.Level.WARNING;
                        return context.researchingCount > 0 ? StatusLine.Level.GOOD : StatusLine.Level.NORMAL;
                    })
                    .detail(() -> {
                        context.refresh();
                        return context.pausedCount > 0 ? Component.translatable(DETAIL_PAUSED, context.pausedCount) : Component.empty();
                    });
            summary.addLine(LINE_IDLE, context::idleText);
            summary.addLine(LINE_TOTAL_CWU, () -> {
                context.refresh();
                return context.totalCwuText;
            });
            page.addChild(summary);

            var confirming = SyncValue.of(context::isConfirming, SyncValue.BOOLEAN, false);
            var unbindAll = Button.text(LayoutStyle.AUTO, () -> Component.translatable(confirming.getValue() ? UNBIND_ALL_CONFIRM : UNBIND_ALL).getString())
                    .setVariant(UITheme.ButtonVariant.DANGER)
                    .setOnServerClick(context::unbindAll)
                    .disabled(() -> context.bindings().isEmpty(), NOTHING_BOUND);
            unbindAll.layout(l -> l.width(UISizes.BUTTON_WIDTH * 3 / 2));
            unbindAll.setHoverTooltips(Component.translatable(UNBIND_ALL_TIP));
            var header = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter());
            header.addSyncValue(confirming);
            var title = TextLine.translatable(0, HEADER_CARDS);
            title.layout(l -> l.flex(1));
            header.addChildren(title, InfoIcon.info(HELP_TASK, HELP_PARALLEL, HELP_CAPACITY, HELP_DISPATCH), unbindAll);
            page.addChild(header);

            var cards = new ServerRows<>(remote, BINDING_CODEC, context::bindings, context::version,
                    binding -> card(context, binding, remote, focus), Component.translatable(EMPTY));
            cards.layout(l -> l.row().flexWrap(FlexWrap.WRAP).width(GRID_WIDTH).gapAll(UISizes.SECTION_GAP));
            page.addChild(cards);

            var scroller = new ScrollerView("data_center_aggregation.page", GRID_WIDTH, UISizes.SLOT).adaptiveWidth().setResizable(false);
            scroller.addScrollViewChild(page);
            scroller.adaptiveHeight(remote ? MachineWindow.clientPageHeightLimit(false) : SERVER_HEIGHT_LIMIT);
            return UIElement.column(LayoutStyle.AUTO).addChild(scroller);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ItemStackTexture(ExResearchMachines.DATA_CENTER.asStack());
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(Component.translatable(TITLE));
        }

        @Override
        public Component getTitle() {
            return Component.translatable(TITLE);
        }

        @Override
        public boolean hasPlayerInventory() {
            return false;
        }
    }

    private static UIElement card(Context context, Binding binding, boolean remote, Consumer<TechNode> focus) {
        var card = UIElement.section(CARD_WIDTH);

        var unbind = Button.glyph("×").setOnServerClick(() -> context.unbind(binding));
        unbind.setHoverTooltips(Component.translatable(UNBIND_TIP));
        var location = TextLine.constant(0, DataCenterAggregationTerminal.location(binding));
        location.layout(l -> l.flex(1));
        var header = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter());
        header.addChildren(ItemView.of(ExResearchMachines.DATA_CENTER.asStack()), location, unbind);
        card.addChild(header);

        var status = new StatusPanel();
        status.addLine(DataCenter.LANG_LINE_STATE, () -> {
            var entry = context.entry(binding);
            return entry == null ? NONE : entry.state.text;
        }).level(() -> {
            var entry = context.entry(binding);
            return entry == null ? StatusLine.Level.NORMAL : entry.state.level;
        }).detail(() -> {
            var entry = context.entry(binding);
            return entry == null ? Component.empty() : entry.detailText;
        });
        status.addLine(TechNodeLine.client(DataCenter.LANG_LINE_RESEARCH, () -> {
            var entry = context.entry(binding);
            return entry == null ? null : entry.node;
        }, focus));
        status.addLine(LINE_REQUESTER, () -> {
            var entry = context.entry(binding);
            return entry == null ? NONE : entry.requesterText;
        });
        status.addLine(DataCenter.LANG_LINE_MAX_CWU, () -> {
            var entry = context.entry(binding);
            return entry == null ? NONE : entry.maxCwuText;
        }).detail(() -> {
            var entry = context.entry(binding);
            return entry == null || entry.machine == null ? Component.empty() : entry.capacityText;
        });
        card.addChild(status);

        var progress = new ProgressBar(LayoutStyle.AUTO, Component.translatable(DataCenter.LANG_RESEARCH_PROGRESS), UITheme.STATUS_ONLINE, () -> {
            var entry = context.entry(binding);
            return entry == null || entry.machine == null || entry.node == null ? ProgressBar.Progress.EMPTY : entry.machine.researchProgress();
        });
        progress.layout(l -> l.flex(1));
        var cancel = Button.translatable(UISizes.BUTTON_WIDTH, DataCenter.LANG_CANCEL_BUTTON)
                .setVariant(UITheme.ButtonVariant.DANGER)
                .setOnServerClick(() -> {
                    var entry = context.entry(binding);
                    if (entry != null && entry.machine != null && entry.node != null) {
                        entry.machine.cancelResearch(context.player);
                        context.invalidate();
                    }
                });
        var research = new UIElement().layout(l -> l.row().gapAll(UISizes.GAP).alignCenter());
        research.addChildren(progress, cancel);
        boolean researching = false;
        if (!remote) {
            var entry = context.entry(binding);
            researching = entry != null && entry.machine != null && entry.node != null;
        }
        research.setDisplay(researching);
        card.addSyncValue(SyncValue.of(() -> {
            var entry = context.entry(binding);
            return entry != null && entry.machine != null && entry.node != null;
        }, SyncValue.BOOLEAN, researching).onChanged(research::setDisplay));
        card.addChild(research);
        return card;
    }
}
