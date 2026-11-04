package com.gtocore.common.machine.noenergy.game.klotski;

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
import com.gregtechceu.gtceu.uipro.render.UIDraw;
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

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.AlignContent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 经典华容道游戏机：6×6 盘面，把火车从左上滑到右侧出口。
 *
 * <p>
 * 关卡分两套：{@link KlotskiRules#MODE_CAMPAIGN} 1000 关按 50 关一章分 20 章，整章按累计星数解锁、
 * 章内完成前一关才解锁下一关；{@link KlotskiRules#MODE_CHALLENGE} 另外 1000 关，单独一页，从第 1 关起
 * 完成前一关解锁下一关。两边都按最少步数评 1~3 星，星级存在 {@link GameMilestones} 里，
 * 累计每 50 星发一次游戏币。
 *
 * <p>
 * 拖动在客户端完成：按下某个滑块后只在它允许的方向上跟手渲染，松手时把目标格发给服务端；
 * 服务端校验路径后落子，位置没变就不计步数。
 */
@DataGeneratorScanned
public final class KlotskiMachine extends AbstractGameMachine<KlotskiMachine.BoardSnapshot> {

    public static final int MODE_CAMPAIGN = KlotskiRules.MODE_CAMPAIGN;
    public static final int MODE_CHALLENGE = KlotskiRules.MODE_CHALLENGE;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PLAYING = 1;
    private static final int STATE_WON = 2;

    /** 盘面绘制参数：6×6 玩区外再包一圈，整盘 8×8、宽 154，正好放得进界面。 */
    private static final int CELL_SIZE = 17;
    private static final int GRID = 2;
    private static final int CELL_PITCH = CELL_SIZE + GRID;
    /** 外圈一格厚。 */
    private static final int BORDER = CELL_PITCH;
    /** 6×6 玩区的像素边长（含格线）。 */
    private static final int INNER_PIXELS = KlotskiRules.BOARD_SIZE * CELL_SIZE +
            (KlotskiRules.BOARD_SIZE + 1) * GRID;
    private static final int BOARD_PIXELS = INNER_PIXELS + 2 * BORDER;

    private static final String TEXTURE_PATH = "gtocore:textures/gui/game/klotski/";
    private static final IGuiTexture WALL_TEXTURE = new ResourceTexture(TEXTURE_PATH + "klotski_wall.png");
    private static final IGuiTexture BOARD_TEXTURE = new ResourceTexture(TEXTURE_PATH + "klotski_board.png");
    /** 按滑块类型取图：1 是火车，2/3 是横向 2/3 格，4/5 是纵向 2/3 格。 */
    private static final IGuiTexture[] BLOCK_TEXTURES = new IGuiTexture[6];

    static {
        BLOCK_TEXTURES[KlotskiRules.TRAIN_TYPE] = new ResourceTexture(TEXTURE_PATH + "klotski_train.png");
        BLOCK_TEXTURES[2] = new ResourceTexture(TEXTURE_PATH + "klotski_block_h2.png");
        BLOCK_TEXTURES[3] = new ResourceTexture(TEXTURE_PATH + "klotski_block_h3.png");
        BLOCK_TEXTURES[4] = new ResourceTexture(TEXTURE_PATH + "klotski_block_v2.png");
        BLOCK_TEXTURES[5] = new ResourceTexture(TEXTURE_PATH + "klotski_block_v3.png");
    }

    private static final int COLOR_SHADOW = 0x66000000;
    private static final int COLOR_WIN_OVERLAY = 0x552E7D32;

    @RegisterLanguage(cn = "关卡", en = "Levels")
    private static final Component PAGE_CAMPAIGN = Component.translatable("gtocore.machine.klotski.page.campaign");
    @RegisterLanguage(cn = "挑战", en = "Challenge")
    private static final Component PAGE_CHALLENGE = Component.translatable("gtocore.machine.klotski.page.challenge");
    /** 聚合页的模式选项翻译键。 */
    public static final Component[] MODE_KEYS = { PAGE_CAMPAIGN, PAGE_CHALLENGE };
    /** 聚合页的“模式”列标题。 */
    public static final Component COLUMN_MODE = Component.empty().append(PAGE_CAMPAIGN).append(" · ").append(PAGE_CHALLENGE);

    @RegisterLanguage(cn = "关卡", en = "Level")
    private static final String LINE_LEVEL = "gtocore.machine.klotski.line.level";
    @RegisterLanguage(cn = "步数", en = "Moves")
    private static final String LINE_MOVES = "gtocore.machine.klotski.line.moves";
    @RegisterLanguage(cn = "评价", en = "Rating")
    private static final String LINE_STARS = "gtocore.machine.klotski.line.stars";
    @RegisterLanguage(cn = "第 %s 章 · 第 %s 关", en = "Chapter %s · Level %s")
    private static final String TEXT_CAMPAIGN_LEVEL = "gtocore.machine.klotski.text.campaign_level";
    @RegisterLanguage(cn = "挑战 %s", en = "Challenge %s")
    private static final String TEXT_CHALLENGE_LEVEL = "gtocore.machine.klotski.text.challenge_level";
    @RegisterLanguage(cn = "累计星星", en = "Total stars")
    private static final String LINE_TOTAL_STARS = "gtocore.machine.klotski.line.total_stars";

    @RegisterLanguage(cn = "完成！", en = "Solved!")
    private static final String TEXT_SOLVED = "gtocore.machine.klotski.text.solved";
    @RegisterLanguage(cn = "重开", en = "Restart")
    private static final String BUTTON_RESTART = "gtocore.machine.klotski.button.restart";
    @RegisterLanguage(cn = "下一关", en = "Next level")
    private static final String BUTTON_NEXT = "gtocore.machine.klotski.button.next";
    @RegisterLanguage(cn = "未解锁", en = "Locked")
    private static final String REASON_LEVEL_LOCKED = "gtocore.machine.klotski.reason.locked";
    @RegisterLanguage(cn = "第 %s 章 · %s", en = "Chapter %s · %s")
    private static final String TEXT_CHAPTER_TITLE = "gtocore.machine.klotski.text.chapter_title";
    @RegisterLanguage(cn = "挑战 %s - %s", en = "Challenge %s - %s")
    private static final String TEXT_CHALLENGE_RANGE = "gtocore.machine.klotski.text.challenge_range";
    @RegisterLanguage(cn = "%s · 最少 %s 步", en = "%s · best %s moves")
    private static final String TEXT_LEVEL_TOOLTIP = "gtocore.machine.klotski.text.level_tooltip";
    @RegisterLanguage(cn = "星星", en = "Stars")
    private static final String COLUMN_STARS = "gtocore.machine.klotski.column.stars";

    /** 章节名（与解包数据里的 20 章一致）。 */
    @RegisterLanguage(cn = "森林 1", en = "Forest 1")
    private static final Component CHAPTER_0 = Component.translatable("gtocore.machine.klotski.chapter.0");
    @RegisterLanguage(cn = "森林 2", en = "Forest 2")
    private static final Component CHAPTER_1 = Component.translatable("gtocore.machine.klotski.chapter.1");
    @RegisterLanguage(cn = "山地 1", en = "Mountain 1")
    private static final Component CHAPTER_2 = Component.translatable("gtocore.machine.klotski.chapter.2");
    @RegisterLanguage(cn = "山地 2", en = "Mountain 2")
    private static final Component CHAPTER_3 = Component.translatable("gtocore.machine.klotski.chapter.3");
    @RegisterLanguage(cn = "火山 1", en = "Volcano 1")
    private static final Component CHAPTER_4 = Component.translatable("gtocore.machine.klotski.chapter.4");
    @RegisterLanguage(cn = "火山 2", en = "Volcano 2")
    private static final Component CHAPTER_5 = Component.translatable("gtocore.machine.klotski.chapter.5");
    @RegisterLanguage(cn = "雪山 1", en = "Snow 1")
    private static final Component CHAPTER_6 = Component.translatable("gtocore.machine.klotski.chapter.6");
    @RegisterLanguage(cn = "雪山 2", en = "Snow 2")
    private static final Component CHAPTER_7 = Component.translatable("gtocore.machine.klotski.chapter.7");
    @RegisterLanguage(cn = "雪山 3", en = "Snow 3")
    private static final Component CHAPTER_8 = Component.translatable("gtocore.machine.klotski.chapter.8");
    @RegisterLanguage(cn = "雪山 4", en = "Snow 4")
    private static final Component CHAPTER_9 = Component.translatable("gtocore.machine.klotski.chapter.9");
    @RegisterLanguage(cn = "雪山 5", en = "Snow 5")
    private static final Component CHAPTER_10 = Component.translatable("gtocore.machine.klotski.chapter.10");
    @RegisterLanguage(cn = "森林 3", en = "Forest 3")
    private static final Component CHAPTER_11 = Component.translatable("gtocore.machine.klotski.chapter.11");
    @RegisterLanguage(cn = "森林 4", en = "Forest 4")
    private static final Component CHAPTER_12 = Component.translatable("gtocore.machine.klotski.chapter.12");
    @RegisterLanguage(cn = "森林 5", en = "Forest 5")
    private static final Component CHAPTER_13 = Component.translatable("gtocore.machine.klotski.chapter.13");
    @RegisterLanguage(cn = "山地 3", en = "Mountain 3")
    private static final Component CHAPTER_14 = Component.translatable("gtocore.machine.klotski.chapter.14");
    @RegisterLanguage(cn = "山地 4", en = "Mountain 4")
    private static final Component CHAPTER_15 = Component.translatable("gtocore.machine.klotski.chapter.15");
    @RegisterLanguage(cn = "山地 5", en = "Mountain 5")
    private static final Component CHAPTER_16 = Component.translatable("gtocore.machine.klotski.chapter.16");
    @RegisterLanguage(cn = "火山 3", en = "Volcano 3")
    private static final Component CHAPTER_17 = Component.translatable("gtocore.machine.klotski.chapter.17");
    @RegisterLanguage(cn = "火山 4", en = "Volcano 4")
    private static final Component CHAPTER_18 = Component.translatable("gtocore.machine.klotski.chapter.18");
    @RegisterLanguage(cn = "火山 5", en = "Volcano 5")
    private static final Component CHAPTER_19 = Component.translatable("gtocore.machine.klotski.chapter.19");

    private static final Component[] CHAPTERS = {
            CHAPTER_0, CHAPTER_1, CHAPTER_2, CHAPTER_3, CHAPTER_4, CHAPTER_5, CHAPTER_6, CHAPTER_7, CHAPTER_8,
            CHAPTER_9, CHAPTER_10, CHAPTER_11, CHAPTER_12, CHAPTER_13, CHAPTER_14, CHAPTER_15, CHAPTER_16,
            CHAPTER_17, CHAPTER_18, CHAPTER_19
    };

    /** 聚合榜与机器排行榜共用的两列。 */
    private static final MilestoneColumn[] LEADERBOARD_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 70),
            MilestoneColumn.right(COLUMN_STARS, 56)
    };

    @SaveToDisk(defaultValue = "0")
    private int mode;
    @SaveToDisk(defaultValue = "0")
    private int level;
    /** 当前局面里每个滑块的左上角，按 {@code x, y} 两个一组。 */
    @SaveToDisk
    private int[] positions = new int[0];
    @SaveToDisk(defaultValue = "0")
    private int moves;
    @SaveToDisk(defaultValue = "0")
    private int status;

    public KlotskiMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    // ==================== 关卡 ====================

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        mode = Math.clamp(mode, MODE_CAMPAIGN, MODE_CHALLENGE);
        level = Math.clamp(level, 0, KlotskiRules.LEVEL_COUNT - 1);
        moves = Math.max(0, moves);
        status = Math.clamp(status, STATE_IDLE, STATE_WON);
        if (positions == null || positions.length != currentLevel().blocks().length * 2) {
            status = STATE_IDLE;
            positions = new int[0];
            moves = 0;
        }
    }

    private KlotskiRules.LevelData currentLevel() {
        return KlotskiRules.level(mode, level);
    }

    /** 打开界面时如果还没开局（刚放下或读档），就从头开始当前关。 */
    private void ensureStarted() {
        if (isRemote() || status != STATE_IDLE) return;
        startLevel(mode, level, null);
    }

    private void startLevel(int nextMode, int nextLevel, @Nullable ServerPlayer starter) {
        mode = nextMode == MODE_CHALLENGE ? MODE_CHALLENGE : MODE_CAMPAIGN;
        level = Math.clamp(nextLevel, 0, KlotskiRules.LEVEL_COUNT - 1);
        positions = currentLevel().initialPositions();
        moves = 0;
        status = STATE_PLAYING;
        beginSession(starter);
        syncAndMarkChanged();
    }

    /** 关卡页点选：两种模式都按进度校验，未解锁的关卡不允许直接开始。 */
    private void selectLevel(int nextMode, int nextLevel, @Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        int clampedMode = nextMode == MODE_CHALLENGE ? MODE_CHALLENGE : MODE_CAMPAIGN;
        int clampedLevel = Math.clamp(nextLevel, 0, KlotskiRules.LEVEL_COUNT - 1);
        if (player != null) {
            UUID playerId = player.getUUID();
            var store = gameMilestones();
            int prevStars = clampedLevel == 0 ? 0 : store.klotskiLevelStars(clampedMode, playerId, clampedLevel - 1);
            if (!unlocked(clampedMode, clampedLevel, store.klotskiTotal(MODE_CAMPAIGN, playerId), prevStars)) {
                player.displayClientMessage(Component.translatable(REASON_LEVEL_LOCKED), true);
                return;
            }
        }
        startLevel(clampedMode, clampedLevel, player);
    }

    private void restartLevel(@Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        startLevel(mode, level, player);
    }

    private void nextLevel(@Nullable ServerPlayer player) {
        if (isRemote() || status != STATE_WON || !guard(player)) return;
        if (level + 1 >= KlotskiRules.LEVEL_COUNT) return;
        selectLevel(mode, level + 1, player);
    }

    private GameMilestones gameMilestones() {
        MinecraftServer server = server();
        if (server == null) throw new IllegalStateException("Klotski machine without a server");
        return GameMilestones.get(server);
    }

    // ==================== 落子 ====================

    /**
     * 服务端落子：校验方向、边界与路径；位置变了才加一步，火车到出口即结算。
     */
    private void moveBlock(int blockIndex, int targetX, int targetY, @Nullable ServerPlayer player) {
        if (isRemote() || status != STATE_PLAYING || !guard(player)) return;
        KlotskiRules.LevelData data = currentLevel();
        if (blockIndex < 0 || blockIndex >= data.blocks().length) return;
        int startX = positions[blockIndex * 2];
        int startY = positions[blockIndex * 2 + 1];
        if (targetX == startX && targetY == startY) return;
        if (!KlotskiRules.canSlide(data, positions, blockIndex, targetX, targetY)) return;
        positions[blockIndex * 2] = targetX;
        positions[blockIndex * 2 + 1] = targetY;
        moves++;
        if (KlotskiRules.isSolved(data, positions)) finishLevel(data);
        syncAndMarkChanged();
    }

    /** 结算：按步数评星，写进独立存档；跨过 50 星整数倍的部分换成游戏币。 */
    private void finishLevel(KlotskiRules.LevelData data) {
        status = STATE_WON;
        if (!sessionCounts()) return;
        int stars = KlotskiRules.starsFor(moves, data.minMoves());
        int reward = gameMilestones().recordKlotski(mode, level, stars, sessionStarter());
        addBalance(reward);
    }

    // ==================== 快照 ====================

    @Override
    protected BoardSnapshot createView() {
        MinecraftServer server = server();
        var store = server == null ? null : GameMilestones.get(server);
        UUID viewer = viewerId();
        long[] campaign = store == null ? new long[GameMilestones.KLOTSKI_WORDS] :
                store.klotskiStars(MODE_CAMPAIGN, viewer);
        long[] challenge = store == null ? new long[GameMilestones.KLOTSKI_WORDS] :
                store.klotskiStars(MODE_CHALLENGE, viewer);
        return new BoardSnapshot(revision(), mode, level, moves, status, positions.clone(), campaign, challenge);
    }

    @Override
    protected void onViewerChanged() {
        if (!isRemote()) syncAndMarkChanged();
    }

    /** 界面快照：盘面 + 当前进度 + 查看者在两种模式下的星级表。 */
    public record BoardSnapshot(int revision, int mode, int level, int moves, int status, int[] positions,
                                long[] campaignStars, long[] challengeStars) {

        public static final BoardSnapshot EMPTY = new BoardSnapshot(0, MODE_CAMPAIGN, 0, 0, STATE_IDLE,
                new int[0], new long[GameMilestones.KLOTSKI_WORDS],
                new long[GameMilestones.KLOTSKI_WORDS]);

        public static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT, BoardSnapshot::mode,
                ByteBufCodecs.VAR_INT, BoardSnapshot::level,
                ByteBufCodecs.VAR_INT, BoardSnapshot::moves,
                ByteBufCodecs.VAR_INT, BoardSnapshot::status,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::positions,
                ByteBufCodecs.LONG_ARRAY, BoardSnapshot::campaignStars,
                ByteBufCodecs.LONG_ARRAY, BoardSnapshot::challengeStars,
                BoardSnapshot::new);

        public boolean playing() {
            return status == STATE_PLAYING;
        }

        public boolean won() {
            return status == STATE_WON;
        }

        public int stars() {
            return GameMilestones.klotskiStarAt(mode == MODE_CHALLENGE ? challengeStars : campaignStars, level);
        }

        public int campaignTotal() {
            return GameMilestones.klotskiTotal(campaignStars);
        }

        public int challengeTotal() {
            return GameMilestones.klotskiTotal(challengeStars);
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
        tabs.attachSubTab(new CampaignPage());
        tabs.attachSubTab(new ChallengePage());
        tabs.attachSubTab(new MilestonePage());
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
        panel.addLine(Component.translatable(LINE_LEVEL), this::levelName);
        panel.addLine(Component.translatable(LINE_MOVES), () -> number(view().moves()));
        panel.addLine(Component.translatable(LINE_STARS), () -> starsText(view().stars()));
        var row = UIElement.row(Button.HEIGHT).layout(layout -> layout.gapAll(UISizes.GAP)
                .justifyContent(AlignContent.CENTER));
        int width = (UISizes.CONTENT_WIDTH - 2 * UISizes.GAP) / 3;
        var restart = Button.text(width, Component.translatable(BUTTON_RESTART));
        restart.setOnServerClick(() -> restartLevel(playerOf(restart)));
        var next = Button.text(width, Component.translatable(BUTTON_NEXT));
        next.clientDisabled(() -> {
            var snapshot = view();
            if (!snapshot.won() || snapshot.level() + 1 >= KlotskiRules.LEVEL_COUNT) return true;
            return snapshot.mode() == MODE_CAMPAIGN && !KlotskiRules.chapterUnlocked(snapshot.campaignTotal(),
                    (snapshot.level() + 1) / KlotskiRules.CHAPTER_SIZE);
        }, Component.translatable(REASON_LEVEL_LOCKED));
        next.setOnServerClick(() -> nextLevel(playerOf(next)));
        row.addChildren(restart, next, createWithdrawButton(width));
        panel.addChild(row);
        return panel;
    }

    private Component levelName() {
        var snapshot = view();
        int index = snapshot.level();
        if (snapshot.mode() == MODE_CHALLENGE) {
            return Component.translatable(TEXT_CHALLENGE_LEVEL, number(index + 1));
        }
        return Component.translatable(TEXT_CAMPAIGN_LEVEL, index / KlotskiRules.CHAPTER_SIZE + 1,
                index % KlotskiRules.CHAPTER_SIZE + 1);
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

    /**
     * 解锁规则：挑战模式从第 1 关起顺序解锁；分章节模式先看整章星数，再要求章内完成前一关。
     */
    private static boolean unlocked(int mode, int level, int campaignTotal, int prevStars) {
        if (mode == MODE_CHALLENGE) return level == 0 || prevStars > 0;
        return KlotskiRules.chapterUnlocked(campaignTotal, level / KlotskiRules.CHAPTER_SIZE) &&
                (level % KlotskiRules.CHAPTER_SIZE == 0 || prevStars > 0);
    }

    // ==================== 关卡页 ====================

    /** 关卡页的公共部分：50 关一行一节，按钮上带 1~3 星的悬停说明。 */
    private abstract class LevelPage extends GamePage {

        private final int pageMode;

        private LevelPage(int pageMode, IGuiTexture icon, Component title) {
            super(icon, title);
            this.pageMode = pageMode;
        }

        @Override
        public final Widget createMainPage(FancyMachineUIWidget widget) {
            var scroller = ScrollerView.page("gtocore_klotski_levels_" + pageMode, UISizes.CONTENT_WIDTH)
                    .adaptiveWidth();
            var page = Form.page();
            page.addChild(starsSummary());
            page.addChild(new SectionSwitch());
            page.addChild(balancePanel());
            scroller.addScrollViewChild(page);
            return scroller;
        }

        /** 累计星数：左边名称、右边数字，中间空白把两边分开。 */
        private UIElement starsSummary() {
            var row = panel();
            row.addLine(Component.translatable(LINE_TOTAL_STARS), () -> number(totalStars()));
            return row;
        }

        /**
         * 分节浏览：顶部 20 个分节按钮（5 个一排共 4 排），下方只渲染当前节的 50 关。
         * <p>
         * 当前节号是一个结构值：客户端点分节按钮时只发请求，服务端校验后下发，两端按同一节号重建，
         * 控件树才不会和服务端走岔。
         */
        private final class SectionSwitch extends UIElement {

            private static final int COLUMNS = 5;

            /** 一节里每行放满按钮：按内容宽减去区块内边距和按钮间距均分。 */
            private final int cellWidth = (UISizes.CONTENT_WIDTH - 2 * UISizes.PANEL_PADDING - (COLUMNS - 1) * UISizes.GAP) / COLUMNS;
            private final UIStructure<Integer> state;
            /** 打开页面时默认停在当前关卡所在的那一节。 */
            private int section = view().level() / KlotskiRules.CHAPTER_SIZE;

            private SectionSwitch() {
                layout(LayoutStyle::column);
                state = addStructure(ByteBufCodecs.INT, () -> section)
                        .validate(value -> value >= 0 && value < KlotskiRules.CHAPTER_COUNT)
                        .apply(this::applySection);
                rebuild();
            }

            private void applySection(int value) {
                section = value;
                rebuild();
            }

            private void rebuild() {
                clearAllWidgets();
                addChild(sectionNav());
                addChild(chapterBody());
            }

            /** 分节按钮的区块：5 个一排、共 4 排，悬停显示节名，选中项高亮。 */
            private UIElement sectionNav() {
                var container = panel();
                for (int rowIndex = 0; rowIndex < KlotskiRules.CHAPTER_COUNT / COLUMNS; rowIndex++) {
                    var row = UIElement.row(Button.HEIGHT).layout(layout -> layout.gapAll(UISizes.GAP));
                    for (int columnIndex = 0; columnIndex < COLUMNS; columnIndex++) {
                        final int sectionIndex = rowIndex * COLUMNS + columnIndex;
                        var button = Button.text(cellWidth, Component.literal(String.valueOf(sectionIndex + 1)));
                        button.setHoverTooltips(sectionTitle(sectionIndex, sectionIndex * KlotskiRules.CHAPTER_SIZE));
                        button.setSelected(() -> section == sectionIndex);
                        button.bindClientVariant(() -> section == sectionIndex ?
                                UITheme.ButtonVariant.CONFIRM : UITheme.ButtonVariant.DEFAULT);
                        button.setOnClientClick(() -> state.request(sectionIndex));
                        row.addChild(button);
                    }
                    container.addChild(row);
                }
                return container;
            }

            /** 章节区块：节名 + 当前节的 50 关（5 个一排、共 10 排）。 */
            private UIElement chapterBody() {
                var container = panel();
                container.addChild(TextLine.of(LayoutStyle.AUTO,
                        () -> sectionTitle(section, section * KlotskiRules.CHAPTER_SIZE))
                        .bindClientColor(UITheme::panelText));
                var grid = UIElement.column(LayoutStyle.AUTO).layout(layout -> layout.gapAll(UISizes.GAP));
                int start = section * KlotskiRules.CHAPTER_SIZE;
                for (int rowIndex = 0; rowIndex < KlotskiRules.CHAPTER_SIZE / COLUMNS; rowIndex++) {
                    var row = UIElement.row(Button.HEIGHT).layout(layout -> layout.gapAll(UISizes.GAP));
                    for (int columnIndex = 0; columnIndex < COLUMNS; columnIndex++) {
                        row.addChild(levelButton(start + rowIndex * COLUMNS + columnIndex));
                    }
                    grid.addChild(row);
                }
                container.addChild(grid);
                return container;
            }

            private Button levelButton(int levelIndex) {
                int label = pageMode == MODE_CHALLENGE ? levelIndex + 1 :
                        levelIndex % KlotskiRules.CHAPTER_SIZE + 1;
                int minMoves = KlotskiRules.level(pageMode, levelIndex).minMoves();
                var button = Button.text(cellWidth, Component.literal(String.valueOf(label)));
                button.setHoverTooltips(Component.translatable(TEXT_LEVEL_TOOLTIP, starsText(0), number(minMoves)));
                button.addSyncValue(SyncValue.ofComponent(() -> Component.translatable(TEXT_LEVEL_TOOLTIP,
                        starsText(levelStars(levelIndex)), number(minMoves)), Component.empty()))
                        .onChanged(button::setHoverTooltips);
                button.setSelected(() -> view().mode() == pageMode && view().level() == levelIndex);
                button.bindClientVariant(() -> levelStars(levelIndex) >= 3 ?
                        UITheme.ButtonVariant.CONFIRM : UITheme.ButtonVariant.DEFAULT);
                button.clientDisabled(() -> !levelUnlocked(levelIndex), Component.translatable(REASON_LEVEL_LOCKED));
                button.setOnServerClick(() -> selectLevel(pageMode, levelIndex, playerOf(button)));
                return button;
            }
        }

        private Component sectionTitle(int sectionIndex, int start) {
            if (pageMode == MODE_CHALLENGE) {
                return Component.translatable(TEXT_CHALLENGE_RANGE, number(start + 1),
                        number(start + KlotskiRules.CHAPTER_SIZE));
            }
            return Component.translatable(TEXT_CHAPTER_TITLE, sectionIndex + 1, CHAPTERS[sectionIndex]);
        }

        private int totalStars() {
            return pageMode == MODE_CHALLENGE ? view().challengeTotal() : view().campaignTotal();
        }

        /**
         * 关卡是否已解锁：挑战模式从第 1 关起必须依次完成前一关；
         * 分章节模式整章要攒够星数，章内还要先完成前一关（每章第一关只需章解锁）。
         */
        private boolean levelUnlocked(int levelIndex) {
            var snapshot = view();
            long[] table = pageMode == MODE_CHALLENGE ? snapshot.challengeStars() : snapshot.campaignStars();
            int prevStars = levelIndex == 0 ? 0 : GameMilestones.klotskiStarAt(table, levelIndex - 1);
            return unlocked(pageMode, levelIndex, snapshot.campaignTotal(), prevStars);
        }

        /** 当前查看者在这一关的星数；服务端用存档，客户端用下发的星级表。 */
        private int levelStars(int levelIndex) {
            if (isRemote()) {
                var snapshot = view();
                return GameMilestones.klotskiStarAt(pageMode == MODE_CHALLENGE ? snapshot.challengeStars() :
                        snapshot.campaignStars(), levelIndex);
            }
            return gameMilestones().klotskiLevelStars(pageMode, viewerId(), levelIndex);
        }
    }

    private final class CampaignPage extends LevelPage {

        private CampaignPage() {
            super(MODE_CAMPAIGN, WidgetIcons.MAP_LIGHT, PAGE_CAMPAIGN);
        }
    }

    private final class ChallengePage extends LevelPage {

        private ChallengePage() {
            super(MODE_CHALLENGE, WidgetIcons.MAP_LIGHT, PAGE_CHALLENGE);
        }
    }

    /** 两个榜：分章节与挑战，各自按总星数降序。 */
    private final class MilestonePage extends LeaderboardTabPage {

        private MilestonePage() {
            super(PAGE_LEADERBOARD, PAGE_LEADERBOARD_TOOLTIP);
        }

        @Override
        protected int boardCount() {
            return 2;
        }

        @Override
        protected Component boardName(int index) {
            return index == MODE_CHALLENGE ? PAGE_CHALLENGE : PAGE_CAMPAIGN;
        }

        @Override
        protected Widget boardContent(int index) {
            return milestoneTable("gtocore_klotski_board_" + index, LEADERBOARD_COLUMNS,
                    () -> {
                        var store = server() == null ? null : GameMilestones.get(server());
                        return store == null ? Integer.MIN_VALUE : store.modCount();
                    },
                    () -> {
                        var server = server();
                        return server == null ? List.of() :
                                buildLeaderboardRows(GameMilestones.get(server), server, index);
                    });
        }
    }

    // ==================== 盘面 ====================

    /** 盘面控件：拖动跟手只发生在客户端，松手才把目标格发给服务端。 */
    private final class BoardView extends UIElement {

        private final RPC<int[]> move;

        /** 正在拖的滑块下标；-1 表示没在拖。 */
        private int dragIndex = -1;
        private int dragCellX;
        private int dragCellY;
        private double dragMouseX;
        private double dragMouseY;
        /** 拖动时滑块相对拖动起点的格子偏移（只在允许的方向上变化）。 */
        private double dragOffset;
        /** 客户端显示用的棋盘与对应的快照版本：松手先落位，等服务端快照回来再对齐。 */
        private int shownRevision = -1;
        private int[] shownPositions = new int[0];
        /** 已发给服务端、还没被快照确认；等待期间不接受新的拖动。 */
        private boolean pendingMove;
        /** 提前落位等待快照的帧数；超过上限仍没确认就回落到服务端位置。 */
        private int pendingFrames;

        private BoardView() {
            layout(layout -> layout.size(BOARD_PIXELS, BOARD_PIXELS));
            registerView(this, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
            move = addRPC(ByteBufCodecs.VAR_INT_ARRAY,
                    (player, payload) -> {
                        if (payload.length < 3) return;
                        moveBlock(payload[0], payload[1], payload[2],
                                player instanceof ServerPlayer serverPlayer ? serverPlayer : null);
                    });
        }

        /** 与服务端快照对齐；版本没变时保留客户端提前落位的位置，避免松手后闪回。 */
        private void syncShown() {
            if (view().revision() != shownRevision) resetShown();
        }

        /** 丢掉提前落位，直接回到服务端位置。 */
        private void resetShown() {
            var snapshot = view();
            shownRevision = snapshot.revision();
            shownPositions = snapshot.positions().clone();
            pendingMove = false;
            pendingFrames = 0;
        }

        /** 当前应当渲染的棋子位置。 */
        private int[] positions() {
            syncShown();
            return shownPositions;
        }

        /** 当前关卡的静态数据。 */
        private KlotskiRules.LevelData data() {
            var snapshot = view();
            return KlotskiRules.level(snapshot.mode(), snapshot.level());
        }

        /** 第 {@code column} 格的左边缘（坐标相对 6×6 玩区）。 */
        private int cellX(int column) {
            return getPositionX() + BORDER + GRID + column * CELL_PITCH;
        }

        /** 第 {@code row} 格的上边缘（坐标相对 6×6 玩区）。 */
        private int cellY(int row) {
            return getPositionY() + BORDER + GRID + row * CELL_PITCH;
        }

        /** 鼠标下的滑块下标；落在格线槽里或盘面外为 -1。 */
        private int blockAt(double mouseX, double mouseY) {
            int[] positions = positions();
            return blockAt(mouseX, mouseY, data(), positions);
        }

        private int blockAt(double mouseX, double mouseY, KlotskiRules.LevelData data, int[] positions) {
            if (!isMouseOverElement(mouseX, mouseY)) return -1;
            if (positions.length != data.blocks().length * 2) return -1;
            int localX = (int) (mouseX - getPositionX()) - BORDER - GRID;
            int localY = (int) (mouseY - getPositionY()) - BORDER - GRID;
            if (localX < 0 || localY < 0) return -1;
            int column = localX / CELL_PITCH;
            int row = localY / CELL_PITCH;
            if (column >= KlotskiRules.BOARD_SIZE || row >= KlotskiRules.BOARD_SIZE) return -1;
            if (localX % CELL_PITCH >= CELL_SIZE || localY % CELL_PITCH >= CELL_SIZE) return -1;
            for (int index = 0; index < data.blocks().length; index++) {
                KlotskiRules.Block block = data.blocks()[index];
                int x = positions[index * 2];
                int y = positions[index * 2 + 1];
                if (column >= x && column < x + block.width() && row >= y && row < y + block.height()) return index;
            }
            return -1;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            syncShown();
            if (button != 0 || !view().playing() || pendingMove) return super.mouseClicked(mouseX, mouseY, button);
            int index = blockAt(mouseX, mouseY);
            if (index < 0) return super.mouseClicked(mouseX, mouseY, button);
            dragIndex = index;
            dragCellX = shownPositions[index * 2];
            dragCellY = shownPositions[index * 2 + 1];
            dragMouseX = mouseX;
            dragMouseY = mouseY;
            dragOffset = 0;
            capturePointer(button);
            playButtonClickSound();
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (dragIndex < 0) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            dragOffset = offsetFor(mouseX, mouseY, data());
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (dragIndex < 0) return super.mouseReleased(mouseX, mouseY, button);
            var data = data();
            if (dragIndex >= data.blocks().length) {
                dragIndex = -1;
                dragOffset = 0;
                releasePointer();
                return true;
            }
            KlotskiRules.Block block = data.blocks()[dragIndex];
            int steps = (int) Math.round(offsetFor(mouseX, mouseY, data));
            int targetX = dragCellX + (block.horizontal() ? steps : 0);
            int targetY = dragCellY + (block.vertical() ? steps : 0);
            if (targetX != dragCellX || targetY != dragCellY) {
                // 客户端先落位，等服务端快照回来再对齐；否则会先闪回原位再跳到新位
                int[] shown = positions();
                if (shown.length == data.blocks().length * 2) {
                    shown[dragIndex * 2] = targetX;
                    shown[dragIndex * 2 + 1] = targetY;
                    pendingMove = true;
                    pendingFrames = 0;
                }
                move.send(new int[] { dragIndex, targetX, targetY });
            }
            dragIndex = -1;
            dragOffset = 0;
            releasePointer();
            return true;
        }

        /** 拖动偏移：横滑块只跟 x，竖滑块只跟 y，并按其他滑块与边界限位。 */
        private double offsetFor(double mouseX, double mouseY, KlotskiRules.LevelData data) {
            if (dragIndex < 0 || dragIndex >= data.blocks().length) return 0;
            KlotskiRules.Block block = data.blocks()[dragIndex];
            double raw = block.horizontal() ? (mouseX - dragMouseX) / CELL_PITCH :
                    block.vertical() ? (mouseY - dragMouseY) / CELL_PITCH : 0;
            int[] positions = positions();
            int forward = legalSteps(data, block, 1, positions);
            int backward = legalSteps(data, block, -1, positions);
            return Math.clamp(raw, -backward, forward);
        }

        /** 从拖动起点沿方向最多能滑几格：碰到其他滑块或棋盘边界就停。 */
        private int legalSteps(KlotskiRules.LevelData data, KlotskiRules.Block block, int direction, int[] positions) {
            int steps = 0;
            while (true) {
                int nextX = dragCellX + (block.horizontal() ? direction * (steps + 1) : 0);
                int nextY = dragCellY + (block.vertical() ? direction * (steps + 1) : 0);
                if (!KlotskiRules.canSlide(data, positions, dragIndex, nextX, nextY)) return steps;
                steps++;
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            // 服务端一直没确认（比如旁观者不能操作）时，提前落位等一会儿就回落到快照位置
            if (pendingMove && ++pendingFrames > 40) resetShown();
            int[] positions = positions();
            var data = data();
            int left = getPositionX();
            int top = getPositionY();
            // 棋盘底层先铺在玩区，外圈墙体再压上去；墙体纹理中间透明，右侧第三行已留出口缺口
            BOARD_TEXTURE.draw(graphics, mouseX, mouseY, left + BORDER, top + BORDER, INNER_PIXELS, INNER_PIXELS);
            WALL_TEXTURE.draw(graphics, mouseX, mouseY, left, top, BOARD_PIXELS, BOARD_PIXELS);
            drawBlocks(graphics, data, positions, mouseX, mouseY);
            if (view().won()) {
                graphics.fill(left, top, left + BOARD_PIXELS, top + BOARD_PIXELS, COLOR_WIN_OVERLAY);
                UIText.drawCentered(graphics, Component.translatable(TEXT_SOLVED).getString(),
                        left + BOARD_PIXELS / 2, top + BOARD_PIXELS / 2 - 4, BOARD_PIXELS, 0xFFFFFFFF);
            } else {
                int hovered = dragIndex < 0 ? blockAt(mouseX, mouseY, data, positions) : -1;
                if (hovered >= 0) {
                    KlotskiRules.Block block = data.blocks()[hovered];
                    UIDraw.hoverOverlay(graphics, cellX(positions[hovered * 2]),
                            cellY(positions[hovered * 2 + 1]),
                            block.width() * CELL_SIZE + (block.width() - 1) * GRID,
                            block.height() * CELL_SIZE + (block.height() - 1) * GRID);
                }
            }
        }

        /** 先画普通滑块，拖动的那个最后画（压在最上面并带阴影）。 */
        @OnlyIn(Dist.CLIENT)
        private void drawBlocks(GuiGraphics graphics, KlotskiRules.LevelData data, int[] positions, int mouseX,
                                int mouseY) {
            if (positions.length != data.blocks().length * 2) return;
            for (int index = 0; index < data.blocks().length; index++) {
                if (index == dragIndex) continue;
                drawBlock(graphics, data.blocks()[index], positions[index * 2], positions[index * 2 + 1], false,
                        mouseX, mouseY);
            }
            if (dragIndex >= 0 && dragIndex < data.blocks().length) {
                drawBlock(graphics, data.blocks()[dragIndex], dragCellX, dragCellY, true, mouseX, mouseY);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void drawBlock(GuiGraphics graphics, KlotskiRules.Block block, int column, int row, boolean lifted,
                               int mouseX, int mouseY) {
            int width = block.width() * CELL_SIZE + (block.width() - 1) * GRID;
            int height = block.height() * CELL_SIZE + (block.height() - 1) * GRID;
            int left = cellX(column) + (lifted && block.horizontal() ? (int) Math.round(dragOffset * CELL_PITCH) : 0);
            int top = cellY(row) + (lifted && block.vertical() ? (int) Math.round(dragOffset * CELL_PITCH) : 0);
            if (lifted) graphics.fill(left + GRID, top + GRID, left + width + GRID, top + height + GRID, COLOR_SHADOW);
            BLOCK_TEXTURES[block.type()].draw(graphics, mouseX, mouseY, left, top, width, height);
        }
    }

    // ==================== 注册信息 ====================

    /** 两个榜汇总到 `/gtocore leaderboard` 的聚合页：一级选项是模式。 */
    public static GameBoardCatalog.Board leaderboardBoard() {
        return GameBoardCatalog.board(LEADERBOARD_COLUMNS, server -> GameMilestones.get(server).modCount(),
                (server, mode, unused) -> buildLeaderboardRows(GameMilestones.get(server), server, mode));
    }

    private static List<MilestoneCell[]> buildLeaderboardRows(GameMilestones store, MinecraftServer server,
                                                              int mode) {
        var rows = new ArrayList<MilestoneCell[]>();
        for (var stat : store.klotskiBoard(mode)) {
            rows.add(new MilestoneCell[] {
                    MilestoneCell.of(Component.literal(GameMilestones.nameOf(server, stat.player()))),
                    MilestoneCell.of(number(stat.stars()))
            });
        }
        return rows;
    }
}
