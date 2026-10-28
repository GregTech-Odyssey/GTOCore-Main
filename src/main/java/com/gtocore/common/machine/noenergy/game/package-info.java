/**
 * 游戏机模块：一组合成在机器里的无耗电街机方块。玩家投入游戏币开玩，按成绩兑现或发奖，并把最好成绩记进全服榜单。
 *
 * <h2>包含的玩法</h2>
 * <ul>
 * <li>{@code game2048}：4×4 电路 2048，推格合并，按积分兑现；</li>
 * <li>{@code justGet10}：5×5 合成十；</li>
 * <li>{@code game24}：四张牌用 + − × ÷ 与括号凑 24；</li>
 * <li>{@code minesweeper}：扫雷，经典六档难度 + 60 关战役；</li>
 * <li>{@code numberPuzzle}：数字华容道，3×3 到 8×8 六档；</li>
 * <li>{@code sudoku}：标准 9×9 数独，六档难度与候选数标记；</li>
 * <li>{@code slotMachine}：老虎机，按下注额滚动并按中奖线结算。</li>
 * </ul>
 *
 * <h2>公共底座</h2>
 * {@link AbstractGameMachine} 负责余额与取出、界面快照通道、游玩权限、单局发起者与多人局判定，
 * 以及里程碑表格、选项按钮、余额面板等共用件；各机器只写玩法、局面字段和页面内容。
 * 子页统一用 {@link AbstractGameMachine.GamePage} 外壳；多榜页用
 * {@link AbstractGameMachine.LeaderboardTabPage}（横向选项条 + 只显示选中的一张榜），
 * 单榜页用 {@link AbstractGameMachine.SingleLeaderboardPage}。横向选项条本身由 {@code HorizontalScroller}
 * 提供，窄按钮上的选中标记由 {@code choiceButton} 绘制。
 *
 * <h2>成绩与界面</h2>
 * 全服成绩存在 {@link com.gtocore.common.machine.noenergy.game.milestone.GameMilestones}，扫雷另有
 * {@code MinesweeperMilestones}。排行榜页统一使用 {@link com.gtocore.api.gui.GTOGuiTextures#LEADERBOARD} 图标，
 * 设置/难度页用齿轮图标，战役页用地图图标；排行榜页不显示余额。
 */
package com.gtocore.common.machine.noenergy.game;
