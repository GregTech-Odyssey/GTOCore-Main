package com.gtocore.common.machine.noenergy.game;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;

import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import org.jetbrains.annotations.Nullable;

/**
 * 「按局结算 + 分档棋盘」的游戏机：一局里攒积分，玩不动了或玩家重新开始时按当前档位的比例兑现成游戏币；盘面分几档，
 * 格子越多越好合、同一份积分越不值钱。
 *
 * <p>
 * 2048 与合成十共用这一层，两者的差别只有盘面规则、档位名字与兑现参数。子类要实现：
 * <ul>
 * <li>{@link #dealBoard()}：按当前规则发一局；</li>
 * <li>{@link #redemptionBaseline()} / {@link #pointsPerCoin(int)}：兑现规则；</li>
 * <li>{@link #applyBoardSize(int)} / {@link #isValidBoardSize(int)} / {@link #boardSizeNames()} /
 * {@link #boardSizeSectionTitle()}：档位；</li>
 * <li>{@link #cellSizeMax()} / {@link #createBoardArea()} / {@link #sanitizeRound()}：盘面；</li>
 * </ul>
 * 换档位、开局、结账、格子边长与档位配置页都由这一层统一，子类不必各写一遍。
 *
 * @param <VIEW> 这类机器下发给客户端的界面快照类型
 */
@DataGeneratorScanned
public abstract class AbstractRoundGameMachine<VIEW> extends AbstractGameMachine<VIEW> {

    @RegisterLanguage(cn = "本局积分", en = "Current score")
    protected static final String LINE_SCORE = "gtocore.machine.game.line.score";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    protected static final String BUTTON_RESTART = "gtocore.machine.game.button.restart";
    @RegisterLanguage(cn = "%s 分 / 1 游戏币", en = "%s points per Game Coin")
    protected static final String TEXT_POINTS_PER_COIN = "gtocore.machine.game.text.points_per_coin";

    /** 整盘能占的宽度：格子按它摊薄，保证最大档也放得进页面。 */
    private static final int BOARD_MAX_WIDTH = 156;

    /** 本局累计积分。 */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int score;
    /** 局面阶段；只有服务端看它，界面按盘面自己判断能不能动。 */
    @SaveToDisk
    private int gameStatus = Status.IDLE.ordinal();
    /** 棋盘档位（子类自己那个 BoardSize 枚举的序数）。 */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int boardSize;

    /** 局面阶段：还没开局 / 进行中 / 已结束（结束的那一局已经兑现过）。 */
    protected enum Status {
        IDLE,
        PLAYING,
        GAME_OVER
    }

    protected AbstractRoundGameMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    // ==================== 子类接口 ====================

    /** 发一局新牌：按当前规则把盘面铺出来。清零成绩与进入进行中由 {@link #startRound()} 统一做。 */
    protected abstract void dealBoard();

    /** 子类按自己的规则整理盘面与档位；盘面作废时调 {@link #setIdle()}，下次打开界面就会重发。 */
    protected abstract void sanitizeRound();

    /** 兑现基线：一局积分超过它的部分才折算游戏币（公开给工具提示复用）。 */
    public abstract int redemptionBaseline();

    /** 某档位的兑现比例：超过基线后每多少分折 1 枚游戏币（公开给工具提示复用）。 */
    public abstract int pointsPerCoin(int ordinal);

    /** 换档位后把规则实例换成新档位（{@link #boardSize} 已经写好）。 */
    protected abstract void applyBoardSize(int ordinal);

    /** 网络来的档位序数是否合法。 */
    protected abstract boolean isValidBoardSize(int ordinal);

    /** 档位按钮上的名字，下标就是档位序数。 */
    protected abstract String[] boardSizeNames();

    /** 档位区块的标题 lang key。 */
    protected abstract String boardSizeSectionTitle();

    /** 数字材质原尺寸：小盘就用它，大盘按 {@link #cellSize(int)} 摊薄。 */
    protected abstract int cellSizeMax();

    /** 盘面本体：外层那块居中的底板由 {@link #createGameArea()} 统一包。 */
    protected abstract UIElement createBoardArea();

    /** 读档后的收尾：先把局面阶段与档位夹回合法值，再让子类整盘面（子类因此可以认定档位一定合法）。 */
    @Override
    protected final void sanitizeState() {
        if (gameStatus < 0 || gameStatus >= Status.values().length) setIdle();
        if (!isValidBoardSize(boardSize)) boardSize = 0;
        sanitizeRound();
    }

    /** 当前档位的兑现比例。 */
    public final int pointsPerCoin() {
        return pointsPerCoin(boardSize);
    }

    // ==================== 局面状态 ====================

    protected final int score() {
        return score;
    }

    /** 当前档位序数。 */
    protected final int boardSize() {
        return boardSize;
    }

    /** 把本局成绩归零。 */
    protected final void resetScore() {
        score = 0;
    }

    /** 加上一次合并的得分；超过 {@code int} 上限就饱和。 */
    protected final void addScore(int gained) {
        score = (int) Math.min(Integer.MAX_VALUE, (long) score + gained);
    }

    protected final boolean isPlaying() {
        return gameStatus == Status.PLAYING.ordinal();
    }

    protected final boolean isGameOver() {
        return gameStatus == Status.GAME_OVER.ordinal();
    }

    protected final void setPlaying() {
        gameStatus = Status.PLAYING.ordinal();
    }

    protected final void setIdle() {
        gameStatus = Status.IDLE.ordinal();
    }

    /** 玩不动了：记下结束并把这局积分兑现掉。 */
    protected final void endRound() {
        gameStatus = Status.GAME_OVER.ordinal();
        cashOut();
    }

    /** 同一局只兑现一次：已经结束的那一局在 {@link #endRound()} 里结过了。 */
    @Override
    protected void cashOut() {
        if (isGameOver()) return;
        long reward = (score - redemptionBaseline()) / pointsPerCoin();
        if (reward > 0) addBalance(reward);
    }

    /** 服务端：结掉当前这一局——兑现本局积分并回到「没开局」，成绩清零。换档位前先调它，才能按旧档位结账。 */
    protected final void settleRound() {
        cashOut();
        resetScore();
        setIdle();
    }

    /** 服务端：开新一局（清零本局成绩并进入进行中）。 */
    protected final void startRound() {
        dealBoard();
        resetScore();
        setPlaying();
    }

    /** 服务端：「重新开始」按钮：结掉当前这一局再开一局。没有进行中的一局时按下也无副作用。 */
    protected void restartRoundOnServer() {
        if (isRemote()) return;
        settleRound();
        startRound();
        syncAndMarkChanged();
    }

    /**
     * 服务端：换棋盘档位。先按旧档位结账，再用新档位重建规则、重新发牌（本局成绩不带过去）；调用前先
     * {@link #isValidBoardSize(int)} 校验。
     */
    protected final void switchBoardSize(int ordinal) {
        if (isRemote() || ordinal == boardSize) return;
        settleRound();
        boardSize = ordinal;
        applyBoardSize(ordinal);
        startRound();
        syncAndMarkChanged();
    }

    @Override
    protected void ensureStarted() {
        if (gameStatus == Status.IDLE.ordinal()) startRound();
    }

    /** 某档位的格子边长：小盘用材质原尺寸，大盘按整盘可用宽度摊薄。 */
    protected final int cellSize(int size) {
        return Math.min(cellSizeMax(), BOARD_MAX_WIDTH / size);
    }

    // ==================== 界面 ====================

    @Override
    protected void addStatusLines(StatusPanel status) {
        status.addLine(LINE_SCORE, () -> number(score));
    }

    @Override
    protected UIElement createStatusActions() {
        return UIElement.centeredRow(Button.HEIGHT).addChildren(
                Button.translatable(77, BUTTON_RESTART).setOnServerClick(this::restartRoundOnServer),
                createWithdrawButton(77));
    }

    /** 盘面区：外层面板当底并把盘面居中，盘面本体由子类给。 */
    @Override
    protected final UIElement createGameArea() {
        return new StatusPanel().layout(LayoutStyle::alignCenter).addChild(createBoardArea());
    }

    /** 档位配置页：标题与名字表由子类给，比例文案、当前项与切换动作都在这一层。 */
    @Override
    protected final IFancyUIProvider createSettingsPage() {
        return new BoardSizeConfigPage();
    }

    private final class BoardSizeConfigPage extends SettingsPage {

        @Override
        protected String settingsTitle() {
            return boardSizeSectionTitle();
        }

        @Override
        protected String[] optionNames() {
            return boardSizeNames();
        }

        @Override
        protected @Nullable Component optionDetail(int ordinal) {
            return Component.translatable(TEXT_POINTS_PER_COIN, number(pointsPerCoin(ordinal)));
        }

        @Override
        protected int currentOption() {
            return boardSize;
        }

        @Override
        protected void applyOption(int ordinal) {
            if (isValidBoardSize(ordinal)) switchBoardSize(ordinal);
        }
    }
}
