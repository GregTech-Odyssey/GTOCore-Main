package com.gtocore.common.machine.noenergy.game.minesweeper;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;
import com.gtocore.common.machine.noenergy.game.milestone.MinesweeperMilestones;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Adjuster;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 扫雷游戏机：在机器旁边铺开一格一个显示方块的盘面，提供难度、重开、展开/收起与取出游戏币。
 *
 * <p>
 * 落子由显示方块转发到 {@link #click}；只有整局通关才结算基础奖励，自定义档不计奖励。
 * 盘面按自定义最大尺寸定长分配，规则只看前 {@code cellCount} 格；每次落子只刷新规则报告的显示方块。
 */
@DataGeneratorScanned
public class MinesweeperCoreMachine extends AbstractGameMachine<MinesweeperCoreMachine.BoardSnapshot> {

    @RegisterLanguage(cn = "初级", en = "Beginner")
    private static final String DIFFICULTY_EASY = "gtocore.machine.minesweeper.difficulty.easy";
    @RegisterLanguage(cn = "中级", en = "Intermediate")
    private static final String DIFFICULTY_NORMAL = "gtocore.machine.minesweeper.difficulty.normal";
    @RegisterLanguage(cn = "高级", en = "Advanced")
    private static final String DIFFICULTY_HARD = "gtocore.machine.minesweeper.difficulty.hard";
    @RegisterLanguage(cn = "专家", en = "Expert")
    private static final String DIFFICULTY_EXPERT = "gtocore.machine.minesweeper.difficulty.expert";
    @RegisterLanguage(cn = "地狱", en = "Hell")
    private static final String DIFFICULTY_HELL = "gtocore.machine.minesweeper.difficulty.hell";
    @RegisterLanguage(cn = "自定义", en = "Custom")
    private static final String DIFFICULTY_CUSTOM = "gtocore.machine.minesweeper.difficulty.custom";
    /** 难度名字，下标与 {@link MinesweeperRules.Difficulty} 的序数一一对应。 */
    private static final String[] DIFFICULTY_NAMES = { DIFFICULTY_EASY, DIFFICULTY_NORMAL, DIFFICULTY_HARD,
            DIFFICULTY_EXPERT, DIFFICULTY_HELL, DIFFICULTY_CUSTOM };

    @RegisterLanguage(cn = "难度", en = "Difficulty")
    private static final String SECTION_DIFFICULTY = "gtocore.machine.minesweeper.section.difficulty";
    @RegisterLanguage(cn = "自定义盘面", en = "Custom board")
    private static final String SECTION_CUSTOM = "gtocore.machine.minesweeper.section.custom";
    @RegisterLanguage(cn = "宽度", en = "Width")
    private static final String LINE_WIDTH = "gtocore.machine.minesweeper.line.width";
    @RegisterLanguage(cn = "高度", en = "Height")
    private static final String LINE_HEIGHT = "gtocore.machine.minesweeper.line.height";
    @RegisterLanguage(cn = "雷数", en = "Mines")
    private static final String LINE_MINES = "gtocore.machine.minesweeper.line.mines";
    @RegisterLanguage(cn = "%s×%s · %s 雷 · %s 币", en = "%s×%s · %s mines · %s coins")
    private static final String TEXT_DIFFICULTY_DETAIL = "gtocore.machine.minesweeper.text.difficulty_detail";
    @RegisterLanguage(cn = "不计奖励", en = "no payout")
    private static final String TEXT_CUSTOM_DETAIL = "gtocore.machine.minesweeper.text.custom_detail";
    @RegisterLanguage(cn = "%s %s×%s · %s雷 · %s币", en = "%s %s×%s · %s mines · %s coins")
    private static final String TEXT_DIFFICULTY = "gtocore.machine.minesweeper.text.difficulty";
    @RegisterLanguage(cn = "自定义 %s×%s · %s 雷 · 不计奖励", en = "Custom %s×%s · %s mines · no payout")
    private static final String TEXT_CUSTOM = "gtocore.machine.minesweeper.text.custom";
    @RegisterLanguage(cn = "当前自定义：%s×%s · %s 雷", en = "Current custom board: %s×%s · %s mines")
    private static final String TEXT_CUSTOM_SUMMARY = "gtocore.machine.minesweeper.text.custom_summary";
    @RegisterLanguage(cn = "自定义模式不计奖励；宽度与高度各 %s~%s，雷数按盘面大小夹紧",
                      en = "Custom mode pays nothing; width and height are %s to %s each, and the mine count is clamped to the board")
    private static final String TEXT_CUSTOM_HINT = "gtocore.machine.minesweeper.text.custom_hint";
    @RegisterLanguage(cn = "盘面还没铺开，点「展开盘面」开始", en = "The board is packed up — press Unfold board to start")
    private static final String TEXT_IDLE = "gtocore.machine.minesweeper.text.idle";
    @RegisterLanguage(cn = "已插旗 %s / %s 雷", en = "%s / %s mines flagged")
    private static final String TEXT_FLAGS = "gtocore.machine.minesweeper.text.flags";
    @RegisterLanguage(cn = "通关！已兑现 %s 游戏币", en = "Cleared! %s Game Coins")
    private static final String TEXT_WON = "gtocore.machine.minesweeper.text.won";
    @RegisterLanguage(cn = "踩到雷了，本局没有奖励", en = "Hit a mine — no reward this round")
    private static final String TEXT_LOST = "gtocore.machine.minesweeper.text.lost";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final String BUTTON_RESTART = "gtocore.machine.minesweeper.button.restart";
    @RegisterLanguage(cn = "展开盘面", en = "Unfold board")
    private static final String BUTTON_UNFOLD = "gtocore.machine.minesweeper.button.unfold";
    @RegisterLanguage(cn = "收起盘面", en = "Pack up board")
    private static final String BUTTON_COLLAPSE = "gtocore.machine.minesweeper.button.collapse";
    @RegisterLanguage(cn = "盘面还没铺开", en = "The board is packed up")
    private static final String REASON_NOT_UNFOLDED = "gtocore.machine.minesweeper.reason.not_unfolded";
    @RegisterLanguage(cn = "盘面已经铺开了", en = "The board is already unfolded")
    private static final String REASON_UNFOLDED = "gtocore.machine.minesweeper.reason.unfolded";
    @RegisterLanguage(cn = "位置被挡住了，先把机器旁边的空地清出来", en = "The area is obstructed — clear the ground next to the machine first")
    private static final String MSG_OBSTRUCTED = "gtocore.machine.minesweeper.msg.obstructed";

    @RegisterLanguage(cn = "战役", en = "Campaign")
    private static final String PAGE_CAMPAIGN = "gtocore.machine.minesweeper.page.campaign";
    @RegisterLanguage(cn = "扫雷战役", en = "Minesweeper campaign")
    private static final String PAGE_CAMPAIGN_TOOLTIP = "gtocore.machine.minesweeper.page.campaign.tooltip";
    @RegisterLanguage(cn = "基础排行榜", en = "Base leaderboard")
    private static final String PAGE_RECORDS = "gtocore.machine.minesweeper.page.records";
    @RegisterLanguage(cn = "基础模式各难度的最快通关榜", en = "Fastest clears for each classic difficulty")
    private static final String PAGE_RECORDS_TOOLTIP = "gtocore.machine.minesweeper.page.records.tooltip";
    @RegisterLanguage(cn = "战役排行榜", en = "Campaign leaderboard")
    private static final String PAGE_CAMPAIGN_RECORDS = "gtocore.machine.minesweeper.page.campaign_records";
    @RegisterLanguage(cn = "战役各章节的总用时与完成进度", en = "Total time and progress for each campaign chapter")
    private static final String PAGE_CAMPAIGN_RECORDS_TOOLTIP = "gtocore.machine.minesweeper.page.campaign_records.tooltip";
    @RegisterLanguage(cn = "新手", en = "Novice")
    private static final String TIER_NOVICE = "gtocore.machine.minesweeper.tier.novice";
    @RegisterLanguage(cn = "老兵", en = "Veteran")
    private static final String TIER_VETERAN = "gtocore.machine.minesweeper.tier.veteran";
    @RegisterLanguage(cn = "精锐", en = "Elite")
    private static final String TIER_ELITE = "gtocore.machine.minesweeper.tier.elite";
    private static final String[] TIER_NAMES = { TIER_NOVICE, TIER_VETERAN, TIER_ELITE };
    @RegisterLanguage(cn = "已通关 %s / %s", en = "%s / %s cleared")
    private static final String TEXT_CAMPAIGN_PROGRESS = "gtocore.machine.minesweeper.text.campaign_progress";
    @RegisterLanguage(cn = "%s×%s · %s 雷 · %s", en = "%s×%s · %s mines · %s")
    private static final String TEXT_CAMPAIGN_LEVEL_DETAIL = "gtocore.machine.minesweeper.text.campaign_level_detail";
    @RegisterLanguage(cn = "已通关 · 最快 %s", en = "Cleared · best %s")
    private static final String TEXT_CAMPAIGN_LEVEL_CLEARED = "gtocore.machine.minesweeper.text.campaign_level_cleared";
    @RegisterLanguage(cn = "已通关", en = "Cleared")
    private static final String TEXT_CAMPAIGN_LEVEL_CLEARED_NO_SPEED = "gtocore.machine.minesweeper.text.campaign_level_cleared_no_speed";
    @RegisterLanguage(cn = "未通关", en = "Not cleared")
    private static final String TEXT_CAMPAIGN_LEVEL_UNCLEARED = "gtocore.machine.minesweeper.text.campaign_level_uncleared";
    @RegisterLanguage(cn = "%s %s/%s · %s×%s · %s 雷",
                      en = "%s %s/%s · %s×%s · %s mines")
    private static final String TEXT_CAMPAIGN = "gtocore.machine.minesweeper.text.campaign";
    @RegisterLanguage(cn = "通关！", en = "Cleared!")
    private static final String TEXT_CAMPAIGN_WON = "gtocore.machine.minesweeper.text.campaign_won";
    @RegisterLanguage(cn = "首次通关奖励已存入机器：%s 游戏币", en = "First-clear reward added to the machine: %s Game Coins")
    private static final String TEXT_CAMPAIGN_REWARD = "gtocore.machine.minesweeper.text.campaign_reward";
    @RegisterLanguage(cn = "先完成上一关", en = "Clear the previous level first")
    private static final String REASON_LEVEL_LOCKED = "gtocore.machine.minesweeper.reason.level_locked";
    @RegisterLanguage(cn = "总时间", en = "Total time")
    private static final String COLUMN_TOTAL_TIME = "gtocore.machine.minesweeper.column.total_time";
    @RegisterLanguage(cn = "参与者：%s", en = "Participants: %s")
    private static final String TOOLTIP_PARTICIPANTS = "gtocore.machine.minesweeper.tooltip.participants";
    @RegisterLanguage(cn = "已完成 %s/%s", en = "%s/%s completed")
    private static final String TOOLTIP_CAMPAIGN_PROGRESS = "gtocore.machine.minesweeper.tooltip.campaign_progress";

    /** 踩雷时每颗雷的爆炸威力：踩中的那颗大一点，其余小一点。 */
    private static final float DEATH_EXPLOSION_POWER = 12.0F;
    private static final float MINE_EXPLOSION_POWER = 6.0F;
    /** 额外补一层爆炸声，避免原版固定音量把冲击感压住。 */
    private static final float DEATH_EXPLOSION_SOUND_VOLUME = 16.0F;
    private static final float MINE_EXPLOSION_SOUND_VOLUME = 10.0F;
    private static final float DEATH_EXPLOSION_SOUND_PITCH = 0.6F;
    private static final float MINE_EXPLOSION_SOUND_PITCH = 0.8F;
    /**
     * 一局最多炸几下：大盘的雷有几百颗，全炸会把服务器与客户端一起拖垮；踩中的那颗必炸，其余按固定间隔抽。
     */
    public static final int MAX_EXPLOSIONS = 24;

    /** 盘面容量：按自定义档的最大边长分配，字段长度固定（换难度只改读到哪里，不换数组引用）。 */
    private static final int CELL_CAPACITY = MinesweeperRules.CUSTOM_MAX_SIDE * MinesweeperRules.CUSTOM_MAX_SIDE;

    /** 盘面边长输入框的步长（默认 / Shift / Ctrl / Ctrl+Shift）。 */
    private static final long[] SIDE_STEPS = { 1, 4, 16, 32 };
    /** 雷数输入框的步长：盘面最大 64×64，雷数能到四千多。 */
    private static final long[] MINE_STEPS = { 1, 16, 128, 1024 };
    /** 配置页里输入框前面那一列名字的宽度：两个汉字足够。 */
    private static final int LABEL_WIDTH = 40;

    /** 模式（{@link MinesweeperRules.Mode} 的序数）；旧存档默认基础档。 */
    @SaveToDisk
    private int mode = MinesweeperRules.Mode.PRESET.ordinal();
    /** 当前选中的战役关 0..59；基础/自定义模式下也保留，切回战役时继续用。 */
    @SaveToDisk
    private int campaignLevel;
    /** 本局种子；与 firstCell 一起决定雷盘，读档时重算。 */
    @SaveToDisk
    private long boardSeed;
    /** 第一次翻开的格子；-1 表示本局还没生成雷盘。 */
    @SaveToDisk
    private int firstCell = -1;
    /** 本局开始时刻（展开盘面时记下）；用于成绩计时。 */
    @SaveToDisk
    private long roundStart;
    /** 本局开启者；= null 表示无法归属成绩。 */
    @SaveToDisk
    @Nullable
    private UUID roundStarter;
    /** 本局其他参与者（翻开过格子的人，不含开启者）。 */
    @SaveToDisk
    private List<UUID> roundParticipants = new ArrayList<>();

    /** 难度（{@link MinesweeperRules.Difficulty} 的序数）。 */
    @SaveToDisk
    private int difficulty = MinesweeperRules.Difficulty.EASY.ordinal();
    /**
     * 自定义档的宽、高与雷数：只在自定义档生效，换到预设档时原样留着。默认值与地狱档一致（30×30、249 雷），
     * 下发到客户端是为了配置页能直接显示这组数（输入框与那行「当前自定义」都读它们）。
     */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int customWidth = MinesweeperRules.Difficulty.HELL.width();
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int customHeight = MinesweeperRules.Difficulty.HELL.height();
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int customMines = MinesweeperRules.Difficulty.HELL.mineCount();
    /** 盘面是否已经铺开（显示方块与边框还在世界里）。 */
    @SaveToDisk
    private boolean unfolded;
    /** 铺开时的盘面宽高：各档尺寸改过之后，读档能凭它认出「地上那片是旧尺寸」，收掉再重铺。 */
    @SaveToDisk
    private int boardWidth;
    @SaveToDisk
    private int boardHeight;
    /** 这一面盘是否已经结过账，一面盘只兑现一次。 */
    @SaveToDisk
    private boolean settled;
    /** 雷盘运行期缓存；新盘由 seed + firstCell 重算，旧档才需要把雷盘本身存下来。 */
    @SaveToDisk(skipWhen = "minesNeedSaving")
    private boolean[] mines = new boolean[CELL_CAPACITY];
    /** 显示盘，取值见 {@link MinesweeperRules}。只在铺开盘面时存盘。 */
    @SaveToDisk(skipWhen = "boardPackedUpCells")
    private int[] cells = new int[CELL_CAPACITY];

    /** 按当前难度建的规则实例；读档与换难度时重建（临时容器跟着盘面尺寸分配）。 */
    private MinesweeperRules rules = MinesweeperRules.create(MinesweeperRules.Difficulty.EASY);
    /** 一次落子改动过的格子，只拿来刷新这几个显示方块。 */
    private final IntArrayList touched = new IntArrayList();

    public MinesweeperCoreMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /** 难度名字的 lang key，工具提示与配置页共用。 */
    public static String difficultyName(MinesweeperRules.Difficulty preset) {
        return DIFFICULTY_NAMES[preset.ordinal()];
    }

    /**
     * 通道里下发的界面快照：难度、盘面尺寸、雷数与奖励、已插旗数、局面阶段，相等性只看版本号。
     *
     * <p>
     * 尺寸与雷数写进快照，是为了自定义档改了宽高之后客户端不用自己再算一遍（服务端夹好的值才作数）。
     */
    public record BoardSnapshot(int revision, int mode, int difficulty, int campaignLevel, long campaignCleared,
                                int width, int height, int mineCount, long maxReward, int flags, int state) {

        /** 盘面收起（还没铺开）。 */
        private static final int STATE_COLLAPSED = 0;
        /** 铺开且还在进行中。 */
        private static final int STATE_PLAYING = 1;
        /** 全部非雷格翻开，已兑现。 */
        private static final int STATE_WON = 2;
        /** 踩到雷，已结算。 */
        private static final int STATE_LOST = 3;

        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, 0, 0, 0, 0L, 0, 0, 0, 0L, 0, STATE_COLLAPSED);
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT, BoardSnapshot::mode,
                ByteBufCodecs.VAR_INT, BoardSnapshot::difficulty,
                ByteBufCodecs.VAR_INT, BoardSnapshot::campaignLevel,
                ByteBufCodecs.VAR_LONG, BoardSnapshot::campaignCleared,
                ByteBufCodecs.VAR_INT, BoardSnapshot::width,
                ByteBufCodecs.VAR_INT, BoardSnapshot::height,
                ByteBufCodecs.VAR_INT, BoardSnapshot::mineCount,
                ByteBufCodecs.VAR_LONG, BoardSnapshot::maxReward,
                ByteBufCodecs.VAR_INT, BoardSnapshot::flags,
                ByteBufCodecs.VAR_INT, BoardSnapshot::state,
                BoardSnapshot::new);

        /** 当前档位；网络来的序数一律夹回合法范围。 */
        MinesweeperRules.Difficulty preset() {
            var all = MinesweeperRules.Difficulty.ALL;
            return all[Math.clamp(difficulty, 0, all.length - 1)];
        }

        boolean isPreset() {
            return mode == MinesweeperRules.Mode.PRESET.ordinal();
        }

        boolean isCampaign() {
            return mode == MinesweeperRules.Mode.CAMPAIGN.ordinal();
        }

        boolean isCustom() {
            return mode == MinesweeperRules.Mode.CUSTOM.ordinal();
        }

        boolean collapsed() {
            return state == STATE_COLLAPSED;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || other instanceof BoardSnapshot snapshot && revision == snapshot.revision;
        }

        @Override
        public int hashCode() {
            return revision;
        }
    }

    // ==================== 局面 ====================

    /** 读档后的收尾：难度与自定义尺寸夹回合法值并按它重建规则。只在服务端调用。 */
    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        rebuildRules();
        sanitizeBoard();
        sanitizeRound();
    }

    /** 盘面尺寸与当前档位不符时收干净，并按未铺开处理。 */
    private void sanitizeBoard() {
        if (!unfolded) return;
        if (boardWidth != rules.width() || boardHeight != rules.height()) {
            removeLayout();
            unfolded = false;
            boardWidth = 0;
            boardHeight = 0;
            newBoard();
            clearRound();
            return;
        }
        if (firstCell >= 0) {
            if (firstCell < rules.cellCount()) {
                regenerateMines();
            } else {
                firstCell = -1;
                boardSeed = 0L;
                Arrays.fill(mines, false);
            }
        } else if (!rules.isPlaced(mines)) {
            Arrays.fill(mines, false);
        }
    }

    /**
     * 按当前模式重建规则：预设档直接用枚举；战役用静态关卡；自定义档把宽高雷数夹进合法范围，
     * 并把夹好的值写回字段，界面上的输入框与状态行才不会跟规则各说一套。
     */
    private void rebuildRules() {
        var selected = MinesweeperRules.Mode.of(mode);
        if (selected == null) selected = MinesweeperRules.Mode.PRESET;
        if (selected == MinesweeperRules.Mode.PRESET) {
            var preset = MinesweeperRules.Difficulty.of(difficulty);
            if (preset == null) preset = MinesweeperRules.Difficulty.EASY;
            if (preset.custom()) {
                selected = MinesweeperRules.Mode.CUSTOM;
            } else {
                difficulty = preset.ordinal();
                rules = MinesweeperRules.create(preset);
                mode = selected.ordinal();
                return;
            }
        }
        mode = selected.ordinal();
        if (selected == MinesweeperRules.Mode.CAMPAIGN) {
            campaignLevel = Math.clamp(campaignLevel, 0, MinesweeperCampaign.LEVEL_COUNT - 1);
            difficulty = -1;
            rules = MinesweeperRules.createCampaign(MinesweeperCampaign.level(campaignLevel));
        } else {
            difficulty = MinesweeperRules.Difficulty.CUSTOM.ordinal();
            rules = MinesweeperRules.createCustom(customWidth, customHeight, customMines);
            customWidth = rules.width();
            customHeight = rules.height();
            customMines = rules.mineCount();
        }
    }

    @Override
    protected BoardSnapshot createView() {
        int snapshotDifficulty = rules.difficulty() == null ?
                (rules.campaign() ? -1 : MinesweeperRules.Difficulty.CUSTOM.ordinal()) :
                rules.difficulty().ordinal();
        return new BoardSnapshot(revision(), mode, snapshotDifficulty, campaignLevel, viewerCleared(),
                rules.width(), rules.height(), rules.mineCount(), rules.maxReward(), rules.flags(cells), stateOrdinal());
    }

    /** 局面阶段：收起 / 进行中 / 通关 / 踩雷，全部从盘面现推。 */
    private int stateOrdinal() {
        if (!unfolded) return BoardSnapshot.STATE_COLLAPSED;
        if (rules.isLost(cells)) return BoardSnapshot.STATE_LOST;
        if (rules.isWon(mines, cells)) return BoardSnapshot.STATE_WON;
        return BoardSnapshot.STATE_PLAYING;
    }

    /** 这一面盘现在能兑现多少币（只有全部完成才非 0；自定义档恒为 0）。 */
    private long payout() {
        return rules.payout(rules.foundMines(mines, cells), rules.isWon(mines, cells));
    }

    /** 铺一片空盘：定长数组原地归零，尾巴一直是「未翻开 / 不是雷」。 */
    private void newBoard() {
        Arrays.fill(mines, false);
        Arrays.fill(cells, MinesweeperRules.UNOPENED);
        firstCell = -1;
        boardSeed = 0L;
        settled = false;
    }

    // ==================== 界面动作 ====================

    /** 铺开盘面：先把要占的空地检查一遍，再一格一个显示方块铺出去，外圈围一圈边框。位置被挡住时给点的人一句提示。 */
    private void unfoldOnServer(@Nullable ServerPlayer player) {
        if (isRemote() || unfolded) return;
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        int width = rules.width();
        int height = rules.height();
        BlockPos core = getPos();
        for (int x = 0; x <= width + 1; x++) {
            for (int z = 0; z <= height + 1; z++) {
                BlockPos pos = gridPos(x, z);
                if (pos.equals(core) || serverLevel.isEmptyBlock(pos)) continue;
                send(player, Component.translatable(MSG_OBSTRUCTED));
                return;
            }
        }
        if (rules.campaign() && (player == null || !isCampaignUnlocked(player.getUUID(), campaignLevel))) {
            send(player, Component.translatable(REASON_LEVEL_LOCKED));
            return;
        }
        newBoard();
        startRound(player);
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                BlockPos pos = cellPos(x, z);
                serverLevel.setBlock(pos, MinesweeperDisplayBlock.displayState(MinesweeperRules.UNOPENED),
                        Block.UPDATE_CLIENTS);
                if (serverLevel.getBlockEntity(pos) instanceof MinesweeperDisplayBlockEntity display) {
                    display.setCore(core);
                }
            }
        }
        BlockState frameState = GTOBlocks.MINESWEEPER_FRAME.get().defaultBlockState();
        for (int x = 0; x <= width + 1; x++) {
            for (int z = 0; z <= height + 1; z++) {
                if (x >= 1 && x <= width && z >= 1 && z <= height) continue;
                BlockPos framePos = gridPos(x, z);
                if (!framePos.equals(core)) serverLevel.setBlock(framePos, frameState, Block.UPDATE_CLIENTS);
            }
        }
        unfolded = true;
        boardWidth = width;
        boardHeight = height;
        syncAndMarkChanged();
    }

    /** 收起盘面：先把这一局按规矩结掉（没通关就是 0 币），再拆掉显示方块、边框并把盘面清空。 */
    private void collapseOnServer(@Nullable ServerPlayer player) {
        if (isRemote() || !unfolded) return;
        settle(player);
        removeLayout();
        unfolded = false;
        boardWidth = 0;
        boardHeight = 0;
        newBoard();
        clearRound();
        syncAndMarkChanged();
    }

    /** 服务端：「重新开始」：结掉当前这一局，再按同一难度重铺一盘。只有铺开时才有意义。 */
    private void restartOnServer(@Nullable ServerPlayer player) {
        if (isRemote() || !unfolded) return;
        if (rules.campaign() && (player == null || !isCampaignUnlocked(player.getUUID(), campaignLevel))) {
            send(player, Component.translatable(REASON_LEVEL_LOCKED));
            return;
        }
        settle(player);
        newBoard();
        startRound(player);
        mirrorAll();
        syncAndMarkChanged();
    }

    /**
     * 服务端：换难度。当前这一局先结账并收摊（难度变了盘面尺寸也对不上），换了之后由玩家自己点「展开盘面」重新开局。
     *
     * @param ordinal 配置页按钮的序号，与 {@link MinesweeperRules.Difficulty} 的序数一一对应
     */
    private void switchDifficulty(int ordinal) {
        if (isRemote()) return;
        var preset = MinesweeperRules.Difficulty.of(ordinal);
        if (preset == null) return;
        var target = preset.custom() ? MinesweeperRules.Mode.CUSTOM : MinesweeperRules.Mode.PRESET;
        if (mode == target.ordinal() && difficulty == ordinal) return;
        collapseOnServer(null);
        difficulty = ordinal;
        mode = target.ordinal();
        rebuildRules();
        newBoard();
        syncAndMarkChanged();
    }

    /** 服务端：选中战役关；只允许点的人已经解锁的关卡。 */
    private void selectCampaignLevel(int level, @Nullable ServerPlayer player) {
        if (isRemote() || player == null) return;
        if (level < 0 || level >= MinesweeperCampaign.LEVEL_COUNT) return;
        if (!isCampaignUnlocked(player.getUUID(), level)) {
            send(player, Component.translatable(REASON_LEVEL_LOCKED));
            return;
        }
        if (mode == MinesweeperRules.Mode.CAMPAIGN.ordinal() && campaignLevel == level) return;
        collapseOnServer(null);
        mode = MinesweeperRules.Mode.CAMPAIGN.ordinal();
        campaignLevel = level;
        rebuildRules();
        newBoard();
        syncAndMarkChanged();
    }

    /** 服务端：改自定义宽度（已由输入框夹过一次，这里再夹一次免得存档/网络来的值越界）。 */
    private void setCustomWidth(int width) {
        if (isRemote()) return;
        int clamped = Math.clamp(width, MinesweeperRules.CUSTOM_MIN_SIDE, MinesweeperRules.CUSTOM_MAX_SIDE);
        if (clamped == customWidth) return;
        customWidth = clamped;
        applyCustomShape();
    }

    private void setCustomHeight(int height) {
        if (isRemote()) return;
        int clamped = Math.clamp(height, MinesweeperRules.CUSTOM_MIN_SIDE, MinesweeperRules.CUSTOM_MAX_SIDE);
        if (clamped == customHeight) return;
        customHeight = clamped;
        applyCustomShape();
    }

    private void setCustomMines(int mines) {
        if (isRemote()) return;
        int clamped = MinesweeperRules.clampMines(customWidth, customHeight, mines);
        if (clamped == customMines) return;
        customMines = clamped;
        applyCustomShape();
    }

    /** 自定义盘面参数变了：雷数按新尺寸重新夹紧；正在玩自定义盘的话先结账收摊，再按新参数重铺。 */
    private void applyCustomShape() {
        customMines = MinesweeperRules.clampMines(customWidth, customHeight, customMines);
        if (mode == MinesweeperRules.Mode.CUSTOM.ordinal()) {
            collapseOnServer(null);
            rebuildRules();
            newBoard();
        }
        syncAndMarkChanged();
    }

    /** 自定义盘面这组数（服务端夹紧后的值，随字段下发），配置页那一行「当前自定义」用它。 */
    private Component customShapeText() {
        return Component.translatable(TEXT_CUSTOM_SUMMARY, customWidth, customHeight, customMines);
    }

    /** 自定义档的雷数上限（输入框用它限制可输入范围）。 */
    private int maxCustomMines() {
        return Math.max(1, customWidth * customHeight - 9);
    }

    // ==================== 战役/成绩/生成 ====================

    private @Nullable MinesweeperMilestones minesweeperMilestones() {
        return getLevel() instanceof ServerLevel level ? MinesweeperMilestones.get(level) : null;
    }

    private boolean isCampaignUnlocked(UUID player, int level) {
        var store = minesweeperMilestones();
        return store != null && store.isCampaignUnlocked(player, level);
    }

    /** 当前界面查看者的战役进度；没有界面查看者时为 0。 */
    private long viewerCleared() {
        var store = minesweeperMilestones();
        var viewer = viewerId();
        return store == null || viewer == null ? 0L : store.campaignCleared(viewer);
    }

    @Override
    protected void onViewerChanged() {
        if (!isRemote() && getLevel() != null) syncAndMarkChanged();
    }

    private void startRound(@Nullable ServerPlayer player) {
        roundStart = gameTime();
        roundStarter = player == null || player instanceof FakePlayer ? null : player.getUUID();
        if (roundParticipants == null) roundParticipants = new ArrayList<>();
        roundParticipants.clear();
    }

    private void clearRound() {
        roundStart = 0L;
        roundStarter = null;
        if (roundParticipants == null) roundParticipants = new ArrayList<>();
        roundParticipants.clear();
    }

    private void sanitizeRound() {
        if (!unfolded) {
            clearRound();
            return;
        }
        if (roundStart <= 0L || roundStart > gameTime()) roundStart = gameTime();
        if (roundParticipants == null) roundParticipants = new ArrayList<>();
        if (roundStarter == null) {
            roundParticipants.clear();
        } else {
            roundParticipants.removeIf(participant -> participant == null || participant.equals(roundStarter));
        }
    }

    /** 新盘只有 seed/firstCell，不存雷盘；旧档没有 firstCell 时才需要把旧雷盘存下来。 */
    private boolean minesNeedSaving(boolean[] mines) {
        return unfolded && firstCell < 0 && rules.isPlaced(mines);
    }

    /** 读档时按 seed+firstCell 重算雷盘；算法与生成器共用同一个布局函数。 */
    private void regenerateMines() {
        Arrays.fill(mines, false);
        boolean[] generated = MinesweeperBoardGenerator.layout(rules.width(), rules.height(),
                rules.mineCount(), boardSeed, firstCell);
        System.arraycopy(generated, 0, mines, 0, Math.min(generated.length, mines.length));
    }

    /** 首次翻开前生成雷盘：基础种子混合世界种子、核心方块位置和本局随机盐；异常时退回混合种子布雷。 */
    private void generateBoard(int safeCell) {
        if (!(getLevel() instanceof ServerLevel level)) return;
        long worldSeed = level.getSeed();
        long corePosition = getPos().asLong();
        try {
            var generated = MinesweeperBoardGenerator.generate(random(), worldSeed, corePosition,
                    rules.width(), rules.height(), rules.mineCount(), safeCell);
            Arrays.fill(mines, false);
            System.arraycopy(generated.mines(), 0, mines, 0, Math.min(generated.mines().length, mines.length));
            boardSeed = generated.seed();
        } catch (RuntimeException failure) {
            boardSeed = MinesweeperBoardGenerator.mixSeed(worldSeed, corePosition, random().nextLong());
            Arrays.fill(mines, false);
            boolean[] generated = MinesweeperBoardGenerator.layout(rules.width(), rules.height(),
                    rules.mineCount(), boardSeed, safeCell);
            System.arraycopy(generated, 0, mines, 0, Math.min(generated.length, mines.length));
        }
        firstCell = safeCell;
    }

    private void noteParticipant(@Nullable ServerPlayer player) {
        if (player == null || player instanceof FakePlayer) return;
        UUID id = player.getUUID();
        if (roundStarter == null) {
            roundStarter = id;
            return;
        }
        if (roundParticipants == null) roundParticipants = new ArrayList<>();
        if (id.equals(roundStarter) || roundParticipants.contains(id)) return;
        roundParticipants.add(id);
    }

    /** 显示方块被点：右击翻开、潜行右击插旗；数字格周围旗数对上了就连开一圈。 */
    public void click(BlockPos displayPos, @Nullable ServerPlayer player, boolean flag) {
        if (isRemote() || !unfolded || settled) return;
        int cell = cellAt(displayPos);
        if (cell < 0) return;
        touched.clear();
        boolean generated = false;
        if (!flag && cells[cell] == MinesweeperRules.UNOPENED && firstCell < 0 && !rules.isPlaced(mines)) {
            generateBoard(cell);
            generated = true;
        }
        boolean changed;
        if (flag) {
            changed = rules.toggleFlag(cells, cell, touched);
        } else if (cells[cell] >= MinesweeperRules.NUMBER_MIN) {
            changed = chord(cell);
        } else {
            changed = rules.open(mines, cells, cell, touched);
        }
        if (!flag && changed) noteParticipant(player);
        if (changed) mirrorTouched();
        // 每次有效点击都检查一次结束条件；没有改动盘面时也要补上可能漏掉的结算。
        if (rules.isOver(mines, cells)) {
            settle(player);
        } else if (!changed && !generated) {
            return;
        }
        syncAndMarkChanged();
    }

    /** 数字格：周围旗数与数字相等时，把周围还扣着的格子一次翻开（可能踩雷，也可能直接通关）。 */
    private boolean chord(int cell) {
        int flagsAround = 0;
        for (int direction = 0; direction < MinesweeperRules.DIRECTIONS; direction++) {
            int next = rules.neighbour(cell, direction);
            if (next >= 0 && cells[next] == MinesweeperRules.FLAGGED) flagsAround++;
        }
        if (flagsAround != cells[cell]) return false;
        boolean changed = false;
        for (int direction = 0; direction < MinesweeperRules.DIRECTIONS; direction++) {
            int next = rules.neighbour(cell, direction);
            if (next < 0 || cells[next] != MinesweeperRules.UNOPENED) continue;
            changed |= rules.open(mines, cells, next, touched);
            if (rules.isLost(cells)) break;
        }
        return changed;
    }

    /**
     * 结算：全部通关（或雷全找到）才把奖励记进余额（同一面盘只给一次），踩雷时放个响并告诉点的人。
     * 中途重开、收起、换难度、拆机器也会调到这里，没通关就是 0 币。
     *
     * @param player 点出这一下的人，界面上没有（拆机器）时为 {@code null}
     */
    private void settle(@Nullable ServerPlayer player) {
        if (settled) return;
        settled = true;
        boolean won = rules.isWon(mines, cells);
        long firstClearReward = won ? recordVictory() : 0L;
        long amount = payout();
        if (amount > 0) addBalance(amount);
        if (rules.isLost(cells)) {
            explode();
            send(player, Component.translatable(TEXT_LOST));
        } else if (won) {
            if (rules.campaign()) {
                send(player, Component.translatable(TEXT_CAMPAIGN_WON));
                if (firstClearReward > 0L) {
                    send(player, Component.translatable(TEXT_CAMPAIGN_REWARD, number(firstClearReward)));
                }
            } else if (amount > 0L) {
                send(player, Component.translatable(TEXT_WON, number(amount)));
            }
        }
    }

    /**
     * 胜利时先落成绩/战役进度；战役首次完成把奖励存进机器余额，返回奖励金额供提示。
     * 用时非法（0 或负）时不写成绩，但战役进度仍按完成推进。
     */
    private long recordVictory() {
        var store = minesweeperMilestones();
        if (store == null || roundStarter == null) return 0L;
        long completedAt = gameTime();
        long ticks = roundStart > 0L && completedAt > roundStart ? completedAt - roundStart : 0L;
        if (rules.campaign()) {
            boolean first = store.recordCampaign(campaignLevel, roundStarter, roundParticipants, ticks, completedAt);
            if (!first) return 0L;
            long reward = MinesweeperCampaign.rewardForLevel(campaignLevel);
            addBalance(reward);
            return reward;
        }
        if (rules.mode() == MinesweeperRules.Mode.PRESET &&
                difficulty >= 0 && difficulty < MinesweeperRules.Difficulty.CUSTOM.ordinal()) {
            store.recordBase(difficulty, roundStarter, roundParticipants, ticks, completedAt);
        }
        return 0L;
    }

    /** 拆除机器时先把盘面收干净，余额由基类按面额掉回世界。 */
    @Override
    public void onMachineRemoved() {
        removeLayout();
        super.onMachineRemoved();
    }

    /** 拆除机器、收起盘面等时机都会调：把还没结账的成绩折进余额。 */
    @Override
    protected void cashOut() {
        settle(null);
    }

    /**
     * 踩雷的动静：踩中的那颗按威力炸一次，其余雷按固定间隔抽到 {@link #MAX_EXPLOSIONS} 为止——每处都有爆炸声、
     * 对生物造成伤害与击退，但不破坏方块（{@link Level.ExplosionInteraction#NONE} 只影响实体、不动方块）。
     */
    private void explode() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        int cellCount = rules.cellCount();
        int deathCell = -1;
        int mineTotal = 0;
        for (int cell = 0; cell < cellCount; cell++) {
            if (!mines[cell]) continue;
            mineTotal++;
            if (cells[cell] == MinesweeperRules.DEATH_BOMB) deathCell = cell;
        }
        if (mineTotal == 0) return;
        int budget = MAX_EXPLOSIONS;
        if (deathCell >= 0) {
            explodeAt(serverLevel, deathCell, DEATH_EXPLOSION_POWER,
                    DEATH_EXPLOSION_SOUND_VOLUME, DEATH_EXPLOSION_SOUND_PITCH);
            budget--;
        }
        int stride = Math.max(1, mineTotal / budget);
        int seen = 0;
        for (int cell = 0; cell < cellCount && budget > 0; cell++) {
            if (!mines[cell] || cell == deathCell) continue;
            if (seen++ % stride != 0) continue;
            explodeAt(serverLevel, cell, MINE_EXPLOSION_POWER,
                    MINE_EXPLOSION_SOUND_VOLUME, MINE_EXPLOSION_SOUND_PITCH);
            budget--;
        }
    }

    private void explodeAt(ServerLevel level, int cell, float power, float volume, float pitch) {
        BlockPos pos = displayPos(cell);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, power,
                Level.ExplosionInteraction.NONE);
        // 原版爆炸声的音量是固定的，这里额外补一层更响、更低沉的爆炸声。
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, volume, pitch);
    }

    // ==================== 盘面方块 ====================

    /** 只拆属于本机的盘面：显示方块按核心归属认领，边框只收外圈；没铺开过则不动。 */
    private void removeLayout() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        if (boardWidth > 0 && boardHeight > 0) {
            removeBlocks(serverLevel, boardWidth, boardHeight, true);
        } else if (unfolded) {
            removeBlocks(serverLevel, MinesweeperRules.CUSTOM_MAX_SIDE, MinesweeperRules.CUSTOM_MAX_SIDE, false);
        }
    }

    /** 扫 {@code width × height} 盘面及外圈；显示方块按核心归属，{@code frames} 控制是否连边框一起收。 */
    private void removeBlocks(ServerLevel level, int width, int height, boolean frames) {
        BlockPos core = getPos();
        for (int x = 0; x <= width + 1; x++) {
            for (int z = 0; z <= height + 1; z++) {
                BlockPos pos = gridPos(x, z);
                if (pos.equals(core)) continue;
                Block block = level.getBlockState(pos).getBlock();
                if (block instanceof MinesweeperDisplayBlock) {
                    if (level.getBlockEntity(pos) instanceof MinesweeperDisplayBlockEntity display &&
                            core.equals(display.getCore())) {
                        level.removeBlock(pos, false);
                    }
                } else if (frames && block instanceof MinesweeperFrameBlock && onRing(x, z, width, height)) {
                    level.removeBlock(pos, false);
                }
            }
        }
    }

    /** 是否位于盘面外圈那一圈边框上（{@code 0} 与 {@code width + 1} / {@code height + 1} 是边框）。 */
    private static boolean onRing(int x, int z, int width, int height) {
        return x == 0 || x == width + 1 || z == 0 || z == height + 1;
    }

    /** 把一次落子改动过的显示方块刷成新状态（{@link #touched} 里可能有重复，重复刷同一个格子无害）。 */
    private void mirrorTouched() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        for (int index = 0; index < touched.size(); index++) {
            mirror(serverLevel, touched.getInt(index));
        }
    }

    /** 把整盘刷一遍：重开一局时所有格子都回到「未翻开」，只能逐格刷。 */
    private void mirrorAll() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        for (int cell = 0; cell < rules.cellCount(); cell++) {
            mirror(serverLevel, cell);
        }
    }

    private void mirror(ServerLevel level, int cell) {
        if (cell < 0 || cell >= rules.cellCount()) return;
        BlockPos pos = displayPos(cell);
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof MinesweeperDisplayBlock)) return;
        int wanted = MinesweeperDisplayBlock.propertyFor(cells[cell]);
        if (state.getValue(MinesweeperDisplayBlock.STATE) != wanted) {
            level.setBlock(pos, state.setValue(MinesweeperDisplayBlock.STATE, wanted), Block.UPDATE_CLIENTS);
        }
    }

    /** 棋盘网格坐标（{@code gx} 向 +X、{@code gz} 向 -Z）对应的世界坐标；机器自己就是 (0, 0)。 */
    private BlockPos gridPos(int gx, int gz) {
        return getPos().offset(gx, 0, -gz);
    }

    /** 第 {@code (x, z)} 格（0 起，棋盘在第 1 圈边框之内）的世界坐标。 */
    private BlockPos cellPos(int x, int z) {
        return gridPos(x + 1, z + 1);
    }

    private BlockPos displayPos(int cell) {
        int width = rules.width();
        return cellPos(cell % width, cell / width);
    }

    /** 显示方块的位置 → 格子下标；不在棋盘范围内返回 -1。 */
    private int cellAt(BlockPos displayPos) {
        int width = rules.width();
        int height = rules.height();
        int x = displayPos.getX() - getPos().getX() - 1;
        int z = getPos().getZ() - displayPos.getZ() - 1;
        if (x < 0 || x >= width || z < 0 || z >= height) return -1;
        return z * width + x;
    }

    // ==================== 界面 ====================

    /** 主页 = 局面面板 + 状态面板；顺序两端必须一致，通道里的条目编号才对得上。 */
    @Override
    public Widget createUIWidget() {
        var page = Form.page();
        page.addChildren(createBoardArea(), createStatusPanel());
        return page;
    }

    /** 主页之外挂配置、基础里程碑、战役、战役里程碑四个子页。 */
    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new BoardConfigPage());
        sideTabs.attachSubTab(new BaseMilestonePage());
        sideTabs.attachSubTab(new CampaignPage());
        sideTabs.attachSubTab(new CampaignMilestonePage());
    }

    /** 局面面板：难度、这一局的进度与操作说明，快照挂在这一层上。 */
    private UIElement createBoardArea() {
        var area = panel();
        registerView(area, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
        area.addChildren(
                TextLine.of(LayoutStyle.AUTO, this::difficultyText).styled().setTextAlign(Horizontal.CENTER),
                TextLine.of(LayoutStyle.AUTO, this::stateText).styled().setTextAlign(Horizontal.CENTER));
        return area;
    }

    /** 状态面板：余额 + 展开/收起 + 重新开始/取出全部。 */
    private StatusPanel createStatusPanel() {
        var status = panel();
        status.addLine(LINE_BALANCE, () -> number(balance()));

        var unfold = Button.translatable(77, BUTTON_UNFOLD)
                .clientDisabled(() -> !view().collapsed(), REASON_UNFOLDED);
        unfold.setOnServerClick(() -> unfoldOnServer(playerOf(unfold)));
        var collapse = Button.translatable(77, BUTTON_COLLAPSE)
                .clientDisabled(() -> view().collapsed(), REASON_NOT_UNFOLDED);
        collapse.setOnServerClick(() -> collapseOnServer(playerOf(collapse)));
        var restart = Button.translatable(77, BUTTON_RESTART)
                .clientDisabled(() -> view().collapsed(), REASON_NOT_UNFOLDED);
        restart.setOnServerClick(() -> restartOnServer(playerOf(restart)));

        status.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(unfold, collapse));
        status.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(restart, createWithdrawButton(77)));
        return status;
    }

    /** 难度行：档位、盘面尺寸、雷数与全找到时的奖励；自定义档换成「不计奖励」。 */
    private Component difficultyText() {
        var snapshot = view();
        if (snapshot.isCampaign()) {
            int tier = MinesweeperCampaign.tierOf(snapshot.campaignLevel());
            var level = MinesweeperCampaign.level(snapshot.campaignLevel());
            return Component.translatable(TEXT_CAMPAIGN, Component.translatable(TIER_NAMES[tier]),
                    level.index(), MinesweeperCampaign.tierSize(tier), snapshot.width(), snapshot.height(),
                    FormattingUtil.formatNumbers(snapshot.mineCount()));
        }
        if (snapshot.isCustom()) {
            return Component.translatable(TEXT_CUSTOM, snapshot.width(), snapshot.height(), snapshot.mineCount());
        }
        return Component.translatable(TEXT_DIFFICULTY, Component.translatable(difficultyName(snapshot.preset())),
                snapshot.width(), snapshot.height(), FormattingUtil.formatNumbers(snapshot.mineCount()),
                FormattingUtil.formatNumbers(snapshot.maxReward()));
    }

    /** 局面行：收起 / 已插旗 / 通关 / 踩雷，全部由服务端状态推出来。 */
    private Component stateText() {
        var snapshot = view();
        return switch (snapshot.state()) {
            case BoardSnapshot.STATE_WON -> snapshot.isCampaign() ? Component.translatable(TEXT_CAMPAIGN_WON) :
                    Component.translatable(TEXT_WON, number(snapshot.maxReward()));
            case BoardSnapshot.STATE_LOST -> Component.translatable(TEXT_LOST);
            case BoardSnapshot.STATE_PLAYING -> Component.translatable(TEXT_FLAGS,
                    number(snapshot.flags()), number(snapshot.mineCount()));
            default -> Component.translatable(TEXT_IDLE);
        };
    }

    /** 按钮背后要的玩家由 {@code AbstractGameMachine#playerOf(Widget)} 提供。 */
    private static void send(@Nullable ServerPlayer player, Component message) {
        if (player != null) player.displayClientMessage(message, true);
    }

    /** 服务端：难度页里这一档是不是当前选中的难度（自定义档单列，和预设档互斥）。 */
    private boolean isDifficultySelected(int ordinal) {
        var preset = MinesweeperRules.Difficulty.ALL[ordinal];
        if (preset.custom()) return mode == MinesweeperRules.Mode.CUSTOM.ordinal();
        return mode == MinesweeperRules.Mode.PRESET.ordinal() && difficulty == ordinal;
    }

    /** 难度页：六个难度（按钮上只写档位名，选中的那个两端带绿色 > <；尺寸/雷数/奖励放悬浮提示）+ 自定义盘面的宽/高/雷数。 */
    private final class BoardConfigPage extends GamePage {

        private BoardConfigPage() {
            super(WidgetIcons.SETTINGS, PAGE_DIFFICULTY, PAGE_DIFFICULTY_TOOLTIP);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();

            var difficulties = Form.section(SECTION_DIFFICULTY);
            for (int ordinal = 0; ordinal < DIFFICULTY_NAMES.length; ordinal++) {
                // 循环变量不是有效 final，先拷一份给 lambda 用
                int index = ordinal;
                var preset = MinesweeperRules.Difficulty.ALL[index];
                var button = choiceButton(LayoutStyle.AUTO, Component.translatable(DIFFICULTY_NAMES[index]),
                        () -> isDifficultySelected(index));
                if (preset.custom()) {
                    button.setHoverTooltips(Component.translatable(TEXT_CUSTOM_DETAIL),
                            Component.translatable(TEXT_CUSTOM_HINT, MinesweeperRules.CUSTOM_MIN_SIDE,
                                    MinesweeperRules.CUSTOM_MAX_SIDE));
                } else {
                    button.setHoverTooltips(Component.translatable(TEXT_DIFFICULTY_DETAIL, preset.width(),
                            preset.height(), FormattingUtil.formatNumbers(preset.mineCount()),
                            FormattingUtil.formatNumbers(preset.maxReward())));
                }
                button.setOnServerClick(() -> switchDifficulty(index));
                difficulties.addChild(button);
            }
            page.addChild(difficulties);

            var custom = Form.section(SECTION_CUSTOM);
            custom.addChild(adjusterRow(LINE_WIDTH, NumberField.ofInt(LayoutStyle.AUTO,
                    () -> customWidth, MinesweeperCoreMachine.this::setCustomWidth,
                    MinesweeperRules.CUSTOM_MIN_SIDE, MinesweeperRules.CUSTOM_MAX_SIDE).setSteps(SIDE_STEPS)));
            custom.addChild(adjusterRow(LINE_HEIGHT, NumberField.ofInt(LayoutStyle.AUTO,
                    () -> customHeight, MinesweeperCoreMachine.this::setCustomHeight,
                    MinesweeperRules.CUSTOM_MIN_SIDE, MinesweeperRules.CUSTOM_MAX_SIDE).setSteps(SIDE_STEPS)));
            custom.addChild(adjusterRow(LINE_MINES, NumberField.ofInt(LayoutStyle.AUTO,
                    () -> customMines, MinesweeperCoreMachine.this::setCustomMines,
                    () -> 1, MinesweeperCoreMachine.this::maxCustomMines).setSteps(MINE_STEPS)));
            custom.addChild(TextLine.of(LayoutStyle.AUTO, MinesweeperCoreMachine.this::customShapeText).styled());
            page.addChild(custom);

            page.addChild(balancePanel());
            return page;
        }
    }

    private static final MilestoneColumn[] BASE_RECORD_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 70), MilestoneColumn.center(COLUMN_DURATION, 56) };
    private static final MilestoneColumn[] CAMPAIGN_RECORD_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 70), MilestoneColumn.center(COLUMN_TOTAL_TIME, 56) };

    private int minesweeperMilestonesVersion() {
        var store = minesweeperMilestones();
        return store == null ? Integer.MIN_VALUE : store.modCount();
    }

    private List<MilestoneCell[]> baseRows(int difficulty) {
        var store = minesweeperMilestones();
        var server = server();
        return store == null || server == null ? List.of() : buildBaseRows(store, server, difficulty);
    }

    private List<MilestoneCell[]> campaignRows(int tierStart, int tierSize) {
        var store = minesweeperMilestones();
        var server = server();
        return store == null || server == null ? List.of() : buildCampaignRows(store, server, tierStart, tierSize);
    }

    /** 战役关按钮的悬浮提示：盘面尺寸、雷数，以及打开界面这名玩家自己的进度与最快用时。 */
    private Component campaignLevelTooltip(int level) {
        var levelData = MinesweeperCampaign.level(level);
        var store = minesweeperMilestones();
        var viewer = viewerId();
        Component progress;
        if (store == null || !store.isCampaignCleared(viewer, level)) {
            progress = Component.translatable(TEXT_CAMPAIGN_LEVEL_UNCLEARED);
        } else {
            long best = store.campaignLevelBestTicks(level, viewer);
            progress = best > 0L ? Component.translatable(TEXT_CAMPAIGN_LEVEL_CLEARED, duration(best)) :
                    Component.translatable(TEXT_CAMPAIGN_LEVEL_CLEARED_NO_SPEED);
        }
        return Component.translatable(TEXT_CAMPAIGN_LEVEL_DETAIL, levelData.width(), levelData.height(),
                FormattingUtil.formatNumbers(levelData.minesCount()), progress);
    }

    private static List<MilestoneCell[]> buildBaseRows(MinesweeperMilestones store, MinecraftServer server,
                                                       int difficulty) {
        var stats = store.baseBoard(difficulty);
        var rows = new ArrayList<MilestoneCell[]>(stats.size());
        for (var stat : stats) {
            var key = stat.key();
            rows.add(new MilestoneCell[] {
                    MilestoneCell.of(Component.literal(MinesweeperMilestones.nameOf(server, key.starter()))),
                    MilestoneCell.of(duration(stat.ticks()), participantHint(server, stat.completedGameTime(),
                            key.participants())) });
        }
        return rows;
    }

    private static List<MilestoneCell[]> buildCampaignRows(MinesweeperMilestones store, MinecraftServer server,
                                                           int tierStart, int tierSize) {
        var stats = store.campaignChapterBoard(tierStart, tierSize);
        var rows = new ArrayList<MilestoneCell[]>(stats.size());
        for (var stat : stats) {
            var key = stat.key();
            var value = stat.completedCount() >= stat.tierSize() ?
                    duration(stat.totalTicks()) :
                    Component.translatable(TOOLTIP_CAMPAIGN_PROGRESS, stat.completedCount(), stat.tierSize());
            rows.add(new MilestoneCell[] {
                    MilestoneCell.of(Component.literal(MinesweeperMilestones.nameOf(server, key.starter()))),
                    MilestoneCell.of(value, campaignHint(server, stat)) });
        }
        return rows;
    }

    private static Component participantHint(MinecraftServer server, long completedGameTime, List<UUID> participants) {
        var hint = Component.translatable(TOOLTIP_ACHIEVED, dayTime(completedGameTime));
        if (participants.isEmpty()) return hint;
        return Component.empty().append(hint).append("\n")
                .append(Component.translatable(TOOLTIP_PARTICIPANTS, Component.literal(participantNames(server, participants))));
    }

    private static Component campaignHint(MinecraftServer server, MinesweeperMilestones.MineCampaignChapterStat stat) {
        var hint = Component.translatable(TOOLTIP_CAMPAIGN_PROGRESS, stat.completedCount(), stat.tierSize());
        var participants = stat.key().participants();
        if (participants.isEmpty()) return hint;
        return Component.empty().append(hint).append("\n")
                .append(Component.translatable(TOOLTIP_PARTICIPANTS, Component.literal(participantNames(server, participants))));
    }

    private static String participantNames(MinecraftServer server, List<UUID> participants) {
        var names = new StringBuilder();
        for (int index = 0; index < participants.size(); index++) {
            if (index > 0) names.append(", ");
            names.append(MinesweeperMilestones.nameOf(server, participants.get(index)));
        }
        return names.toString();
    }

    /** 战役页：三个章节、每关一个按钮；未解锁只看得到、点不动，服务端再校验一次。 */
    private final class CampaignPage implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var scroller = ScrollerView.page("gtocore_minesweeper_campaign", UISizes.CONTENT_WIDTH).adaptiveWidth();
            var page = Form.page();
            page.addChild(TextLine.of(LayoutStyle.AUTO, () -> Component.translatable(TEXT_CAMPAIGN_PROGRESS,
                    MinesweeperCampaign.clearedCount(view().campaignCleared()), MinesweeperCampaign.LEVEL_COUNT)).styled());
            for (int tier = 0; tier < MinesweeperCampaign.TIER_COUNT; tier++) {
                int start = MinesweeperCampaign.tierStart(tier);
                int size = MinesweeperCampaign.tierSize(tier);
                var section = Form.section(TIER_NAMES[tier]);
                UIElement row = null;
                for (int index = start; index < start + size; index++) {
                    final int levelIndex = index;
                    if ((index - start) % 5 == 0) {
                        row = UIElement.row(Button.HEIGHT).layout(style -> style.gapAll(UISizes.GAP));
                        section.addChild(row);
                    }
                    var level = MinesweeperCampaign.level(levelIndex);
                    var button = Button.text(24, Component.literal(String.valueOf(level.index())));
                    button.setHoverTooltips(Component.translatable(TEXT_CAMPAIGN_LEVEL_UNCLEARED));
                    button.addSyncValue(SyncValue.ofComponent(() -> campaignLevelTooltip(levelIndex), Component.empty()))
                            .onChanged(button::setHoverTooltips);
                    button.setSelected(() -> view().isCampaign() && view().campaignLevel() == levelIndex);
                    button.clientDisabled(() -> !MinesweeperCampaign.isUnlocked(view().campaignCleared(), levelIndex),
                            REASON_LEVEL_LOCKED);
                    button.setOnServerClick(() -> selectCampaignLevel(levelIndex, playerOf(button)));
                    row.addChild(button);
                }
                page.addChild(section);
            }
            page.addChild(balancePanel());
            scroller.addScrollViewChild(page);
            return scroller;
        }

        @Override
        public IGuiTexture getTabIcon() {
            return WidgetIcons.MAP_LIGHT;
        }

        @Override
        public Component getTitle() {
            return Component.translatable(PAGE_CAMPAIGN);
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(Component.translatable(PAGE_CAMPAIGN_TOOLTIP));
        }

        @Override
        public boolean hasPlayerInventory() {
            return false;
        }
    }

    /** 基础排行榜页：顶上一排难度选项，下面只显示选中的那张表。 */
    private final class BaseMilestonePage extends LeaderboardTabPage {

        private BaseMilestonePage() {
            super(PAGE_RECORDS, PAGE_RECORDS_TOOLTIP);
        }

        @Override
        protected int boardCount() {
            return MinesweeperRules.Difficulty.CUSTOM.ordinal();
        }

        @Override
        protected Component boardName(int index) {
            return Component.translatable(DIFFICULTY_NAMES[index]);
        }

        @Override
        protected Widget boardContent(int difficulty) {
            var section = Form.section(difficultyName(MinesweeperRules.Difficulty.ALL[difficulty]));
            section.addChild(milestoneTable("minesweeper_base_" + difficulty, BASE_RECORD_COLUMNS,
                    MinesweeperCoreMachine.this::minesweeperMilestonesVersion, () -> baseRows(difficulty)));
            return section;
        }
    }

    /** 战役排行榜页：顶上一排章节选项，下面只显示选中的那张表。 */
    private final class CampaignMilestonePage extends LeaderboardTabPage {

        private CampaignMilestonePage() {
            super(PAGE_CAMPAIGN_RECORDS, PAGE_CAMPAIGN_RECORDS_TOOLTIP);
        }

        @Override
        protected int boardCount() {
            return MinesweeperCampaign.TIER_COUNT;
        }

        @Override
        protected Component boardName(int index) {
            return Component.translatable(TIER_NAMES[index]);
        }

        @Override
        protected Widget boardContent(int tier) {
            int start = MinesweeperCampaign.tierStart(tier);
            int size = MinesweeperCampaign.tierSize(tier);
            var section = Form.section(TIER_NAMES[tier]);
            section.addChild(milestoneTable("minesweeper_campaign_" + start, CAMPAIGN_RECORD_COLUMNS,
                    MinesweeperCoreMachine.this::minesweeperMilestonesVersion, () -> campaignRows(start, size)));
            return section;
        }
    }

    /** 一行「名字 + 输入框」：名字占固定一列，输入框吃掉剩下的横向空间，增减按钮按步长文字自适应。 */
    private static UIElement adjusterRow(String labelKey, Adjuster field) {
        var row = UIElement.row(UISizes.CONTROL_HEIGHT).layout(style -> style.gapAll(UISizes.GAP));
        row.addChildren(UIElement.column(LABEL_WIDTH).addChild(TextLine.translatable(LayoutStyle.AUTO, labelKey)), field);
        return row;
    }

    // ==================== 存盘跳过 ====================

    /** 盘面收起时不写显示盘（全未翻开没有意义）。 */
    private boolean boardPackedUpCells(int[] cells) {
        return !unfolded;
    }
}
