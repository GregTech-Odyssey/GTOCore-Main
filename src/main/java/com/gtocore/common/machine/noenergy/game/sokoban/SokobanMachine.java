package com.gtocore.common.machine.noenergy.game.sokoban;

import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;
import com.gtocore.common.machine.noenergy.game.GameBoardCatalog;
import com.gtocore.common.machine.noenergy.game.milestone.GameMilestones;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.data.UIStructure;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.AlignContent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * 推箱子游戏机：把每一关的全部箱子推到目标点上。
 *
 * <p>
 * 关卡取自解包数据，共 {@link SokobanRules#LEVEL_COUNT} 关，按每 100 关一章、8 章一个大章组织；
 * 累计星数每满 {@link SokobanRules#STARS_PER_CHAPTER} 星解锁下一章，章内完成前一关才解锁下一关。
 * 按参考步数评 1~3 星，星级存进 {@link GameMilestones}；累计每 50 星发 100 游戏币。
 *
 * <p>
 * 方向键或 WASD 经 {@link RPC} 上行，服务端校验后落子；推不动的方向不计步数。没有 tick 逻辑。
 */
@DataGeneratorScanned
public final class SokobanMachine extends AbstractGameMachine<SokobanMachine.BoardSnapshot> {

    private static final int STATE_IDLE = 0;
    private static final int STATE_PLAYING = 1;
    private static final int STATE_WON = 2;

    /** 盘面绘制区域边长：宽高按关卡尺寸等比缩放后居中。 */
    private static final int BOARD_BOX = 170;
    private static final int MAX_CELL = 17;
    private static final int MIN_CELL = 3;

    private static final int COLOR_BOARD = 0xFF12161E;
    private static final int COLOR_WALL = 0xFF5C6470;
    private static final int COLOR_WALL_LIGHT = 0xFF7C8794;
    private static final int COLOR_FLOOR = 0xFF2C3542;
    private static final int COLOR_FLOOR_ALT = 0xFF323C4B;
    private static final int COLOR_GOAL = 0xFFD9A441;
    private static final int COLOR_BOX = 0xFFC98A3C;
    private static final int COLOR_BOX_EDGE = 0xFF7A4E1C;
    private static final int COLOR_BOX_DONE = 0xFF4CAF50;
    private static final int COLOR_BOX_DONE_EDGE = 0xFF2E7D32;
    private static final int COLOR_PLAYER = 0xFF4FD8FF;
    private static final int COLOR_PLAYER_EDGE = 0xFF16697A;
    private static final int COLOR_WIN_OVERLAY = 0x552E7D32;

    @RegisterLanguage(cn = "关卡", en = "Levels")
    private static final Component PAGE_LEVELS = Component.translatable("gtocore.machine.sokoban.page.levels");
    @RegisterLanguage(cn = "移动 | 推动", en = "Move | Pushes")
    private static final Component LINE_MOVE_PUSHES = Component.translatable("gtocore.machine.sokoban.line.move_pushes");
    @RegisterLanguage(cn = "第 %s 大章 · 第 %s 章 · 第 %s 关", en = "Chapter %s · Part %s · Level %s")
    private static final String TEXT_LEVEL = "gtocore.machine.sokoban.text.level";
    @RegisterLanguage(cn = "第 %s 大章 · 第 %s 章", en = "Chapter %s · Part %s")
    private static final String TEXT_CHAPTER_TITLE = "gtocore.machine.sokoban.text.chapter_title";
    @RegisterLanguage(cn = "第 %s 大章", en = "Chapter %s")
    private static final String TOOLTIP_MAJOR = "gtocore.machine.sokoban.tooltip.major";
    @RegisterLanguage(cn = "第 %s 章", en = "Part %s")
    private static final String TOOLTIP_CHAPTER = "gtocore.machine.sokoban.tooltip.chapter";
    @RegisterLanguage(cn = "%s · 参考 %s 步 / %s 推", en = "%s · reference %s moves / %s pushes")
    private static final String TEXT_LEVEL_TOOLTIP = "gtocore.machine.sokoban.text.level_tooltip";
    @RegisterLanguage(cn = "重开", en = "Restart")
    private static final Component BUTTON_RESTART = Component.translatable("gtocore.machine.sokoban.button.restart");

    private static final MilestoneColumn[] LEADERBOARD_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 70),
            MilestoneColumn.right(COLUMN_STARS, 56) };

    /** 关卡。 */
    @SaveToDisk(defaultValue = "0")
    private int level;
    /** 小人所在格；-1 表示还没开局。 */
    @SaveToDisk(defaultValue = "-1")
    private int playerCell = -1;
    /** 每个箱子的格子下标。 */
    @SaveToDisk
    private int[] boxes = new int[0];
    @SaveToDisk(defaultValue = "0")
    private int moves;
    /** 其中推动箱子的次数。 */
    @SaveToDisk(defaultValue = "0")
    private int pushes;
    @SaveToDisk(defaultValue = "0")
    private int status;

    public SokobanMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    // ==================== 关卡 ====================

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        level = Math.clamp(level, 0, SokobanRules.LEVEL_COUNT - 1);
        moves = Math.max(0, moves);
        pushes = Math.max(0, pushes);
        status = Math.clamp(status, STATE_IDLE, STATE_WON);
        var data = currentLevel();
        if (boxes == null || boxes.length != data.boxes().length || playerCell < 0 ||
                playerCell >= data.width() * data.height() || SokobanRules.wall(data, playerCell)) {
            status = STATE_IDLE;
            playerCell = -1;
            boxes = new int[0];
            moves = 0;
            pushes = 0;
        }
    }

    private SokobanRules.LevelData currentLevel() {
        return SokobanRules.level(level);
    }

    private void ensureStarted() {
        if (isRemote() || status != STATE_IDLE) return;
        startLevel(level, null);
    }

    private void startLevel(int nextLevel, @Nullable ServerPlayer starter) {
        level = Math.clamp(nextLevel, 0, SokobanRules.LEVEL_COUNT - 1);
        var data = currentLevel();
        playerCell = data.playerCell();
        boxes = data.boxes().clone();
        moves = 0;
        pushes = 0;
        status = STATE_PLAYING;
        beginSession(starter);
        syncAndMarkChanged();
    }

    /** 关卡页点选：章要攒够星数，章内要完成前一关。 */
    private void selectLevel(int nextLevel, @Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        int index = Math.clamp(nextLevel, 0, SokobanRules.LEVEL_COUNT - 1);
        if (player != null) {
            var store = gameMilestones();
            int chapter = SokobanRules.chapterOf(index);
            int prevStars = index == 0 ? 0 : store.sokobanLevelStars(player.getUUID(), index - 1);
            int previousChapterLastStars = chapter == 0 ? 0 :
                    store.sokobanLevelStars(player.getUUID(), SokobanRules.chapterStart(chapter) - 1);
            if (!SokobanRules.levelUnlocked(index, store.sokobanTotal(player.getUUID()), prevStars,
                    previousChapterLastStars)) {
                player.displayClientMessage(REASON_LEVEL_LOCKED, true);
                return;
            }
        }
        startLevel(index, player);
    }

    private void restartLevel(@Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        startLevel(level, player);
    }

    private void nextLevel(@Nullable ServerPlayer player) {
        if (isRemote() || status != STATE_WON || !guard(player)) return;
        if (level + 1 >= SokobanRules.LEVEL_COUNT) return;
        selectLevel(level + 1, player);
    }

    private GameMilestones gameMilestones() {
        MinecraftServer server = server();
        if (server == null) throw new IllegalStateException("Sokoban machine without a server");
        return GameMilestones.get(server);
    }

    // ==================== 落子 ====================

    /** 服务端走一步：方向不成立就不动、不计步；把所有箱子推上目标点即结算。 */
    private void step(int direction, @Nullable ServerPlayer player) {
        if (isRemote() || status != STATE_PLAYING || !guard(player)) return;
        var data = currentLevel();
        boolean pushed = SokobanRules.pushesBox(data, playerCell, boxes, direction);
        int next = SokobanRules.step(data, playerCell, boxes, direction);
        if (next < 0) return;
        playerCell = next;
        moves++;
        if (pushed) pushes++;
        if (SokobanRules.isSolved(data, boxes)) finishLevel(data);
        syncAndMarkChanged();
    }

    /** 结算：按参考移动/推动步数评星，写进存档；跨过 50 星整数倍的部分换成游戏币。 */
    private void finishLevel(SokobanRules.LevelData data) {
        status = STATE_WON;
        if (!sessionCounts()) return;
        int stars = SokobanRules.starsFor(moves, pushes, data.bestMoves(), data.bestPushes());
        int reward = gameMilestones().recordSokoban(level, stars, sessionStarter());
        addBalance(reward);
    }

    // ==================== 快照 ====================

    @Override
    protected BoardSnapshot createView() {
        MinecraftServer server = server();
        var store = server == null ? null : GameMilestones.get(server);
        long[] stars = store == null ? new long[GameMilestones.SOKOBAN_WORDS] : store.sokobanStars(viewerId());
        return new BoardSnapshot(revision(), level, playerCell, boxes.clone(), moves, pushes, status, stars);
    }

    @Override
    protected void onViewerChanged() {
        if (!isRemote()) syncAndMarkChanged();
    }

    /** 界面快照：关卡、小人、箱子、移动/推动步数、阶段与查看者的星级表。 */
    public record BoardSnapshot(int revision, int level, int playerCell, int[] boxes, int moves, int pushes,
                                int status, long[] stars) {

        public static final BoardSnapshot EMPTY = new BoardSnapshot(0, 0, -1, new int[0], 0, 0, STATE_IDLE,
                new long[GameMilestones.SOKOBAN_WORDS]);

        public static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT, BoardSnapshot::level,
                ByteBufCodecs.VAR_INT, BoardSnapshot::playerCell,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::boxes,
                ByteBufCodecs.VAR_INT, BoardSnapshot::moves,
                ByteBufCodecs.VAR_INT, BoardSnapshot::pushes,
                ByteBufCodecs.VAR_INT, BoardSnapshot::status,
                ByteBufCodecs.LONG_ARRAY, BoardSnapshot::stars,
                BoardSnapshot::new);

        public boolean playing() {
            return status == STATE_PLAYING;
        }

        public boolean won() {
            return status == STATE_WON;
        }

        public int starsAt() {
            return GameMilestones.sokobanStarAt(stars, level);
        }

        public int total() {
            return GameMilestones.sokobanTotal(stars);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof BoardSnapshot snapshot && snapshot.revision == revision;
        }

        @Override
        public int hashCode() {
            return revision;
        }
    }

    // ==================== 机器界面 ====================

    @Override
    public void attachSideTabs(TabsWidget tabs) {
        super.attachSideTabs(tabs);
        tabs.attachSubTab(new LevelsPage());
        tabs.attachSubTab(new LeaderboardPage());
    }

    @Override
    public Widget createUIWidget() {
        if (!isRemote()) ensureStarted();
        var page = Form.page();
        var boardPanel = panel().layout(LayoutStyle::alignCenter).addChild(new BoardView());
        page.addChild(boardPanel);
        page.addChild(createStatusPanel());
        return page;
    }

    /** 状态面板：关卡、步数、评价、累计星数与操作按钮。 */
    private StatusPanel createStatusPanel() {
        var panel = panel();
        panel.addLine(LINE_LEVEL, this::levelName);
        panel.addLine(LINE_MOVE_PUSHES, () -> Component.empty().append(number(view().moves())).append(" | ").append(number(view().pushes())));
        panel.addLine(LINE_STARS, () -> starsText(view().starsAt()));
        var row = UIElement.row(Button.HEIGHT).layout(layout -> layout.gapAll(UISizes.GAP)
                .justifyContent(AlignContent.CENTER));
        int width = (UISizes.CONTENT_WIDTH - 2 * UISizes.GAP) / 3;
        var restart = Button.text(width, BUTTON_RESTART);
        restart.setOnServerClick(() -> restartLevel(playerOf(restart)));
        var next = Button.text(width, BUTTON_NEXT);
        next.clientDisabled(() -> {
            var snapshot = view();
            int target = snapshot.level() + 1;
            if (!snapshot.won() || target >= SokobanRules.LEVEL_COUNT) return true;
            int chapter = SokobanRules.chapterOf(target);
            int previousChapterLastStars = chapter == 0 ? 0 :
                    GameMilestones.sokobanStarAt(snapshot.stars(), SokobanRules.chapterStart(chapter) - 1);
            return !SokobanRules.levelUnlocked(target, snapshot.total(),
                    GameMilestones.sokobanStarAt(snapshot.stars(), snapshot.level()), previousChapterLastStars);
        }, REASON_LEVEL_LOCKED);
        next.setOnServerClick(() -> nextLevel(playerOf(next)));
        row.addChildren(restart, next, createWithdrawButton(width));
        panel.addChild(row);
        return panel;
    }

    private Component levelName() {
        int index = view().level();
        int major = SokobanRules.majorOf(index);
        int chapter = SokobanRules.chapterOf(index);
        return Component.translatable(TEXT_LEVEL, number(major + 1), number(chapter % SokobanRules.CHAPTERS_PER_MAJOR + 1),
                number(index + 1));
    }

    /** 0~3 星的文字预先建好，界面每帧刷新时不再反复分配。 */
    private static final Component[] STAR_TEXT = {
            Component.literal("☆☆☆").withStyle(ChatFormatting.GOLD),
            Component.literal("★☆☆").withStyle(ChatFormatting.GOLD),
            Component.literal("★★☆").withStyle(ChatFormatting.GOLD),
            Component.literal("★★★").withStyle(ChatFormatting.GOLD)
    };

    private static Component starsText(int stars) {
        return STAR_TEXT[Math.clamp(stars, 0, 3)];
    }

    // ==================== 关卡页 ====================

    /** 关卡页：顶部大章与小章选择，下方是当前小章的 50 关。 */
    private final class LevelsPage extends GamePage {

        private LevelsPage() {
            super(WidgetIcons.MAP_LIGHT, PAGE_LEVELS);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var scroller = ScrollerView.page("gtocore_sokoban_levels", UISizes.CONTENT_WIDTH).adaptiveWidth();
            var page = Form.page();
            var summary = panel();
            summary.addLine(LINE_TOTAL_STARS, () -> number(view().total()));
            page.addChild(summary);
            page.addChild(new ChapterSwitch());
            page.addChild(balancePanel());
            scroller.addScrollViewChild(page);
            return scroller;
        }

        /**
         * 两层章节切换：一行 8 个大章，每个大章的 16 个小章分两行，下方渲染当前小章的关卡。
         * 当前小章号是结构值，客户端点按钮只发请求，服务端校验后下发，两端按同一个小章重建。
         */
        private final class ChapterSwitch extends UIElement {

            private static final int COLUMNS = 5;
            private static final int SUB_COLUMNS = 6;

            private final int cellWidth = (UISizes.CONTENT_WIDTH - 2 * UISizes.PANEL_PADDING -
                    (COLUMNS - 1) * UISizes.GAP) / COLUMNS;
            private final int chapterWidth = (UISizes.CONTENT_WIDTH - 2 * UISizes.PANEL_PADDING -
                    (SUB_COLUMNS - 1) * UISizes.GAP) / SUB_COLUMNS;
            private final int majorWidth = (UISizes.CONTENT_WIDTH - 2 * UISizes.PANEL_PADDING -
                    (SokobanRules.MAJOR_COUNT - 1) * UISizes.GAP) / SokobanRules.MAJOR_COUNT;
            private final UIStructure<Integer> state;
            /** 打开页面时默认停在当前关卡所在的小章。 */
            private int chapter = SokobanRules.chapterOf(view().level());

            private ChapterSwitch() {
                layout(LayoutStyle::column);
                state = addStructure(ByteBufCodecs.INT, () -> chapter)
                        .validate(value -> value >= 0 && value < SokobanRules.CHAPTER_COUNT)
                        .apply(this::applyChapter);
                rebuild();
            }

            private void applyChapter(int value) {
                chapter = value;
                rebuild();
            }

            private void rebuild() {
                clearAllWidgets();
                addChild(majorNav());
                addChild(chapterNav());
                addChild(chapterBody());
            }

            /** 大章选择：8 个按钮一排。 */
            private UIElement majorNav() {
                var container = panel();
                var row = UIElement.row(Button.HEIGHT).layout(layout -> layout.gapAll(UISizes.GAP));
                int major = chapter / SokobanRules.CHAPTERS_PER_MAJOR;
                for (int index = 0; index < SokobanRules.MAJOR_COUNT; index++) {
                    final int majorIndex = index;
                    var button = Button.text(majorWidth, Component.literal(String.valueOf(index + 1)));
                    button.setHoverTooltips(Component.translatable(TOOLTIP_MAJOR, number(index + 1)));
                    button.setSelected(() -> major == majorIndex);
                    button.bindClientVariant(() -> major == majorIndex ?
                            UITheme.ButtonVariant.CONFIRM : UITheme.ButtonVariant.DEFAULT);
                    button.setOnClientClick(() -> state.request(majorIndex * SokobanRules.CHAPTERS_PER_MAJOR +
                            chapter % SokobanRules.CHAPTERS_PER_MAJOR));
                    row.addChild(button);
                }
                container.addChild(row);
                return container;
            }

            /** 小章选择：当前大章的 16 个小章每行 6 个，占三行。 */
            private UIElement chapterNav() {
                var container = panel();
                int major = chapter / SokobanRules.CHAPTERS_PER_MAJOR;
                int rows = (SokobanRules.CHAPTERS_PER_MAJOR + SUB_COLUMNS - 1) / SUB_COLUMNS;
                for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
                    var row = UIElement.row(Button.HEIGHT).layout(layout -> layout.gapAll(UISizes.GAP));
                    for (int columnIndex = 0; columnIndex < SUB_COLUMNS; columnIndex++) {
                        int sub = rowIndex * SUB_COLUMNS + columnIndex;
                        if (sub >= SokobanRules.CHAPTERS_PER_MAJOR) {
                            row.addChild(Button.text(chapterWidth, Component.literal("-")).disabled(() -> true, null));
                            continue;
                        }
                        final int subIndex = sub;
                        int chapterIndex = major * SokobanRules.CHAPTERS_PER_MAJOR + sub;
                        var button = Button.text(chapterWidth, Component.literal(String.valueOf(sub + 1)));
                        button.setHoverTooltips(Component.translatable(TOOLTIP_CHAPTER, number(chapterIndex + 1)));
                        button.setSelected(() -> chapter % SokobanRules.CHAPTERS_PER_MAJOR == subIndex);
                        button.bindClientVariant(() -> chapter % SokobanRules.CHAPTERS_PER_MAJOR == subIndex ?
                                UITheme.ButtonVariant.CONFIRM : UITheme.ButtonVariant.DEFAULT);
                        button.setOnClientClick(() -> state.request(chapterIndex));
                        row.addChild(button);
                    }
                    container.addChild(row);
                }
                return container;
            }

            /** 当前小章的关卡：5 个一排，每章 50 关共 10 排，按钮上写全局关卡号。 */
            private UIElement chapterBody() {
                var container = panel();
                container.addChild(TextLine.of(LayoutStyle.AUTO,
                        () -> Component.translatable(TEXT_CHAPTER_TITLE,
                                number(chapter / SokobanRules.CHAPTERS_PER_MAJOR + 1),
                                number(chapter % SokobanRules.CHAPTERS_PER_MAJOR + 1)))
                        .bindClientColor(UITheme::panelText));
                var grid = UIElement.column(LayoutStyle.AUTO).layout(layout -> layout.gapAll(UISizes.GAP));
                int start = chapter * SokobanRules.CHAPTER_SIZE;
                int count = SokobanRules.chapterLevels(chapter);
                for (int rowIndex = 0; rowIndex * COLUMNS < count; rowIndex++) {
                    var row = UIElement.row(Button.HEIGHT).layout(layout -> layout.gapAll(UISizes.GAP));
                    for (int columnIndex = 0; columnIndex < COLUMNS; columnIndex++) {
                        int offset = rowIndex * COLUMNS + columnIndex;
                        if (offset >= count) break;
                        row.addChild(levelButton(start + offset));
                    }
                    grid.addChild(row);
                }
                container.addChild(grid);
                return container;
            }

            private Button levelButton(int levelIndex) {
                int label = levelIndex + 1;
                var data = SokobanRules.level(levelIndex);
                int bestMoves = data.bestMoves();
                int bestPushes = data.bestPushes();
                var button = Button.text(cellWidth, Component.literal(String.valueOf(label)));
                button.setHoverTooltips(Component.translatable(TEXT_LEVEL_TOOLTIP, starsText(0), number(bestMoves),
                        number(bestPushes)));
                button.addSyncValue(SyncValue.ofComponent(() -> Component.translatable(TEXT_LEVEL_TOOLTIP,
                        starsText(levelStars(levelIndex)), number(bestMoves), number(bestPushes)), Component.empty()))
                        .onChanged(button::setHoverTooltips);
                button.setSelected(() -> view().level() == levelIndex);
                button.bindClientVariant(() -> levelStars(levelIndex) >= 3 ?
                        UITheme.ButtonVariant.CONFIRM : UITheme.ButtonVariant.DEFAULT);
                button.clientDisabled(() -> !levelUnlocked(levelIndex), REASON_LEVEL_LOCKED);
                button.setOnServerClick(() -> selectLevel(levelIndex, playerOf(button)));
                return button;
            }
        }

        /** 关卡是否已解锁：章要攒够星数或上一章已通关，章内还要先完成前一关。 */
        private boolean levelUnlocked(int levelIndex) {
            var snapshot = view();
            int chapter = SokobanRules.chapterOf(levelIndex);
            int prevStars = levelIndex == 0 ? 0 : GameMilestones.sokobanStarAt(snapshot.stars(), levelIndex - 1);
            int previousChapterLastStars = chapter == 0 ? 0 :
                    GameMilestones.sokobanStarAt(snapshot.stars(), SokobanRules.chapterStart(chapter) - 1);
            return SokobanRules.levelUnlocked(levelIndex, snapshot.total(), prevStars, previousChapterLastStars);
        }

        /** 当前查看者在这一关的星数；服务端用存档，客户端用下发的星级表。 */
        private int levelStars(int levelIndex) {
            if (isRemote()) return GameMilestones.sokobanStarAt(view().stars(), levelIndex);
            return gameMilestones().sokobanLevelStars(viewerId(), levelIndex);
        }
    }

    /** 排行榜页：按总星数降序。 */
    private final class LeaderboardPage extends SingleLeaderboardPage {

        private LeaderboardPage() {
            super(PAGE_LEADERBOARD, PAGE_LEADERBOARD_TOOLTIP);
        }

        @Override
        protected Widget boardContent() {
            var section = Form.section(SECTION_MILESTONES);
            section.addChild(milestoneTable("game_milestones_sokoban", LEADERBOARD_COLUMNS,
                    SokobanMachine::buildMilestoneRows));
            return section;
        }
    }

    // ==================== 盘面 ====================

    /** 盘面控件：自绘地图、箱子与小人，同时接收方向键。 */
    private final class BoardView extends UIElement {

        private final RPC<Integer> move;

        private BoardView() {
            layout(layout -> layout.size(BOARD_BOX, BOARD_BOX));
            registerView(this, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
            move = addRPC(ByteBufCodecs.VAR_INT,
                    (player, direction) -> step(direction,
                            player instanceof ServerPlayer serverPlayer ? serverPlayer : null));
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            int direction = directionOf(keyCode);
            if (direction < 0) return super.keyPressed(keyCode, scanCode, modifiers);
            move.send(direction);
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            var snapshot = view();
            var data = SokobanRules.level(snapshot.level());
            int left = getPositionX();
            int top = getPositionY();
            graphics.fill(left, top, left + BOARD_BOX, top + BOARD_BOX, COLOR_BOARD);

            int cell = cellSize(data);
            int originX = left + (BOARD_BOX - data.width() * cell) / 2;
            int originY = top + (BOARD_BOX - data.height() * cell) / 2;
            char[] tiles = data.tiles();
            boolean[] inside = data.inside();
            for (int row = 0; row < data.height(); row++) {
                for (int column = 0; column < data.width(); column++) {
                    int index = row * data.width() + column;
                    if (!inside[index]) continue;
                    int x = originX + column * cell;
                    int y = originY + row * cell;
                    if (tiles[index] == SokobanRules.WALL) {
                        graphics.fill(x, y, x + cell, y + cell, COLOR_WALL);
                        graphics.fill(x, y, x + cell, y + Math.min(2, cell), COLOR_WALL_LIGHT);
                    } else {
                        graphics.fill(x, y, x + cell, y + cell,
                                ((row + column) & 1) == 0 ? COLOR_FLOOR : COLOR_FLOOR_ALT);
                        if (tiles[index] == SokobanRules.GOAL) {
                            int inset = Math.max(1, cell / 3);
                            graphics.fill(x + inset, y + inset, x + cell - inset, y + cell - inset, COLOR_GOAL);
                        }
                    }
                }
            }

            int[] boxes = snapshot.boxes();
            for (int index = 0; index < boxes.length; index++) {
                int box = boxes[index];
                if (box < 0 || box >= tiles.length) continue;
                boolean done = tiles[box] == SokobanRules.GOAL;
                drawBlock(graphics, originX + (box % data.width()) * cell,
                        originY + (box / data.width()) * cell, cell,
                        done ? COLOR_BOX_DONE : COLOR_BOX, done ? COLOR_BOX_DONE_EDGE : COLOR_BOX_EDGE);
            }

            int player = snapshot.playerCell();
            if (player >= 0 && player < tiles.length) {
                drawBlock(graphics, originX + (player % data.width()) * cell,
                        originY + (player / data.width()) * cell, cell, COLOR_PLAYER, COLOR_PLAYER_EDGE);
            }

            if (snapshot.won()) {
                graphics.fill(left, top, left + BOARD_BOX, top + BOARD_BOX, COLOR_WIN_OVERLAY);
                UIText.drawCentered(graphics, TEXT_SOLVED.getString(),
                        left + BOARD_BOX / 2, top + BOARD_BOX / 2 - 4, BOARD_BOX, 0xFFFFFFFF);
            }
        }

        /** 关卡越大格子越小：24×24 仍能塞进 {@link #BOARD_BOX}。 */
        private int cellSize(SokobanRules.LevelData data) {
            int size = BOARD_BOX / Math.max(data.width(), data.height());
            return Math.clamp(size, MIN_CELL, MAX_CELL);
        }

        @OnlyIn(Dist.CLIENT)
        private void drawBlock(GuiGraphics graphics, int x, int y, int cell, int color, int edge) {
            int inset = Math.max(1, cell / 8);
            graphics.fill(x + inset, y + inset, x + cell - inset, y + cell - inset, color);
            int border = Math.max(1, cell / 6);
            graphics.fill(x + inset, y + inset, x + cell - inset, y + inset + border, edge);
            graphics.fill(x + inset, y + cell - inset - border, x + cell - inset, y + cell - inset, edge);
            graphics.fill(x + inset, y + inset, x + inset + border, y + cell - inset, edge);
            graphics.fill(x + cell - inset - border, y + inset, x + cell - inset, y + cell - inset, edge);
        }
    }

    /** 键盘到方向的映射：方向键与 WASD。 */
    private static int directionOf(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> SokobanRules.DIR_LEFT;
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> SokobanRules.DIR_RIGHT;
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> SokobanRules.DIR_UP;
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> SokobanRules.DIR_DOWN;
            default -> -1;
        };
    }

    // ==================== 注册信息 ====================

    /** 汇总到 `/gtocore leaderboard` 的推箱子榜。 */
    public static GameBoardCatalog.Board leaderboardBoard() {
        return GameBoardCatalog.board(LEADERBOARD_COLUMNS, server -> GameMilestones.get(server).modCount(),
                (server, first, second) -> buildMilestoneRows(GameMilestones.get(server), server));
    }

    private static List<MilestoneCell[]> buildMilestoneRows(GameMilestones store, MinecraftServer server) {
        var rows = new ArrayList<MilestoneCell[]>();
        for (var stat : store.sokobanBoard()) {
            rows.add(new MilestoneCell[] {
                    MilestoneCell.of(Component.literal(GameMilestones.nameOf(server, stat.player()))),
                    MilestoneCell.of(number(stat.stars()))
            });
        }
        return rows;
    }
}
