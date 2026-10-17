package com.gtocore.common.machine.noenergy.minesweeper;

import com.gtocore.common.data.GTOBlockEntities;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 扫雷核心方块：把盘面铺在自己旁边（+X/+Z 方向各留一格边距），一格一个 {@link MinesweeperDisplayBlock}。
 *
 * <p>
 * 交互全在方块的某一面上：<b>顶面正中</b>点一下——还没铺盘就铺，已经铺了就重开一局（重开前按当前进度兑现）；
 * <b>顶面四角</b>点一下——铺盘前可以前后切难度。踩雷或把非雷格全部翻开后这一局结束，按「插对的旗占雷数的比例」
 * 把游戏币直接给点的那位（见 {@link MinesweeperRules#payout}），踩雷还会在原地炸一下（不破坏方块）。
 */
@DataGeneratorScanned
public class MinesweeperCoreBlock extends Block implements EntityBlock {

    /** 盘面是否已经铺开；铺开后四角不再响应切难度。 */
    public static final BooleanProperty UNFOLDED = BooleanProperty.create("unfolded");
    /** 当前难度（{@link MinesweeperRules.Difficulty} 的序数）：只用来换正面的那张图，规则在方块实体里。 */
    public static final IntegerProperty DIFFICULTY = IntegerProperty.create("difficulty", 0,
            MinesweeperRules.Difficulty.ALL.length - 1);

    @RegisterLanguage(cn = "初级 9×9", en = "Beginner 9×9")
    static final String DIFFICULTY_EASY = "gtocore.machine.minesweeper.difficulty.easy";
    @RegisterLanguage(cn = "中级 16×16", en = "Intermediate 16×16")
    static final String DIFFICULTY_NORMAL = "gtocore.machine.minesweeper.difficulty.normal";
    @RegisterLanguage(cn = "高级 16×30", en = "Advanced 16×30")
    static final String DIFFICULTY_HARD = "gtocore.machine.minesweeper.difficulty.hard";
    @RegisterLanguage(cn = "专家 30×64", en = "Expert 30×64")
    static final String DIFFICULTY_EXPERT = "gtocore.machine.minesweeper.difficulty.expert";
    @RegisterLanguage(cn = "右键翻开，潜行右键插旗；踩雷或把非雷格全翻开后这一局结束", en = "Right-click to open, sneak-right-click to flag; the board ends when you hit a mine or open every safe cell")
    static final String TOOLTIP_PLAY = "gtocore.machine.minesweeper.tooltip.play";
    @RegisterLanguage(cn = "按插对的旗占雷数的比例给币：六成起步，没找全最高拿六成，全找到给满额", en = "Pays by the share of mines you flagged: starts at 60%, caps at 60% of full until you find them all, full payout when you do")
    static final String TOOLTIP_PAYOUT = "gtocore.machine.minesweeper.tooltip.payout";
    @RegisterLanguage(cn = "顶面中心：没铺盘就铺，铺了就重开；顶面左右角：铺盘前切难度", en = "Top centre: place the board, or restart once placed; top corners: switch difficulty before placing")
    static final String TOOLTIP_DIFFICULTY = "gtocore.machine.minesweeper.tooltip.difficulty";
    /** 难度名字，下标与 {@link MinesweeperRules.Difficulty} 的序数一一对应。 */
    static final String[] DIFFICULTY_NAMES = { DIFFICULTY_EASY, DIFFICULTY_NORMAL, DIFFICULTY_HARD, DIFFICULTY_EXPERT };
    @RegisterLanguage(cn = "%s 雷 · 全找到 %s 币", en = "%s mines · %s coins if all found")
    private static final String TEXT_DIFFICULTY = "gtocore.machine.minesweeper.text.difficulty_detail";
    @RegisterLanguage(cn = "位置被挡住了，先把核心方块旁边的空地清出来", en = "The area is obstructed — clear the ground next to the core first")
    static final String MSG_OBSTRUCTED = "gtocore.machine.minesweeper.msg.obstructed";
    @RegisterLanguage(cn = "盘面铺好了，右键翻开、潜行右键插旗", en = "Board placed: right-click to open, sneak-right-click to flag")
    static final String MSG_UNFOLDED = "gtocore.machine.minesweeper.msg.unfolded";
    @RegisterLanguage(cn = "已经重开一局（上一局按进度兑现过了）", en = "New board (the previous one was paid out by progress)")
    static final String MSG_RESTARTED = "gtocore.machine.minesweeper.msg.restarted";
    @RegisterLanguage(cn = "控制方块没接上核心方块，先拆掉重铺", en = "This control block is not linked to a core — rebuild the board")
    static final String MSG_NO_CORE = "gtocore.machine.minesweeper.msg.no_core";
    @RegisterLanguage(cn = "盘面收起来了，核心方块还在", en = "Board packed up, the core stays")
    static final String MSG_MINIMIZED = "gtocore.machine.minesweeper.msg.minimized";
    @RegisterLanguage(cn = "盘面和核心方块都收起来了", en = "Board and core both packed up")
    static final String MSG_CLOSED = "gtocore.machine.minesweeper.msg.closed";
    @RegisterLanguage(cn = "难度：%s（%s）", en = "Difficulty: %s (%s)")
    static final String MSG_DIFFICULTY = "gtocore.machine.minesweeper.msg.difficulty";
    @RegisterLanguage(cn = "通关！兑现 %s 游戏币", en = "Cleared! %s Game Coins")
    static final String TEXT_WON = "gtocore.machine.minesweeper.text.won";
    @RegisterLanguage(cn = "踩到雷了，兑现 %s 游戏币", en = "Hit a mine — %s Game Coins")
    static final String TEXT_LOST = "gtocore.machine.minesweeper.text.lost";

    /** 顶面正中的判定范围（以 16 分制的相对坐标算），其余部分按四角分。 */
    private static final double CENTER_MIN = 4;
    private static final double CENTER_MAX = 12;

    public MinesweeperCoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(UNFOLDED, false).setValue(DIFFICULTY, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UNFOLDED, DIFFICULTY);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MinesweeperCoreBlockEntity(GTOBlockEntities.MINESWEEPER_CORE.get(), pos, state);
    }

    // ==================== 交互 ====================

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!(level.getBlockEntity(pos) instanceof MinesweeperCoreBlockEntity core)) return InteractionResult.PASS;
        if (hit.getDirection() != Direction.UP) return InteractionResult.PASS;
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;

        Vec3 location = hit.getLocation();
        double u = location.x - pos.getX();
        double v = location.z - pos.getZ();
        if (u * 16 >= CENTER_MIN && u * 16 <= CENTER_MAX && v * 16 >= CENTER_MIN && v * 16 <= CENTER_MAX) {
            // 正中：没铺盘就铺，铺了就重开
            if (core.isUnfolded()) core.restart(serverPlayer);
            else core.unfold(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        if (core.isUnfolded()) return InteractionResult.PASS;
        // 逆时针方向切：右半边往后一档、左半边往前一档（与原实现相反）
        boolean forward = u >= 0.5;
        core.switchDifficulty(forward, serverPlayer);
        return InteractionResult.SUCCESS;
    }

    /** 核心方块被拆掉时把盘面收干净，免得留一地显示方块。 */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MinesweeperCoreBlockEntity core) {
            core.cleanup();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    // ==================== 提示与消息 ====================

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Tips.stat(Component.translatable(TOOLTIP_PLAY)));
        tooltip.add(Tips.stat(Component.translatable(TOOLTIP_PAYOUT)));
        tooltip.add(Tips.stat(Component.translatable(TOOLTIP_DIFFICULTY)));
    }

    static void send(@Nullable ServerPlayer player, Component message) {
        if (player != null) player.displayClientMessage(message, true);
    }

    static Component difficultyMessage(MinesweeperRules.Difficulty preset) {
        return Component.translatable(MSG_DIFFICULTY, Component.translatable(DIFFICULTY_NAMES[preset.ordinal()]),
                Component.translatable(TEXT_DIFFICULTY, FormattingUtil.formatNumbers(preset.mineCount()),
                        FormattingUtil.formatNumbers(preset.maxReward())));
    }

    static Component resultMessage(boolean won, long amount) {
        return Component.translatable(won ? TEXT_WON : TEXT_LOST, FormattingUtil.formatNumbers(amount))
                .withStyle(won ? ChatFormatting.GREEN : ChatFormatting.RED);
    }
}
