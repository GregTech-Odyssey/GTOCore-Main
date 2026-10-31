/**
 * 游戏机模块：一组合成在机器里的无耗电街机方块。玩家投入游戏币开玩，按成绩兑现或发奖，并把最好成绩记进全服榜单。
 *
 * <p>
 * 玩法：{@code game2048}（4×4 推格合并）、{@code justGet10}（5×5 合成十）、{@code game24}（四则运算凑 24）、
 * {@code minesweeper}（五档预设难度 + 自定义 + 60 关战役）、{@code numberPuzzle}（3×3~8×8 华容道）、
 * {@code sudoku}（9×9 六档难度 + 可选一条变体）、{@code slotMachine}（按下注滚动结算）。
 *
 * <p>
 * {@link AbstractGameMachine} 提供余额与取出、界面快照通道、游玩权限、单局发起者与多人局判定，以及里程碑表格、
 * 选项按钮、余额面板等共用件；各机器只写玩法、局面字段和页面内容。子页用 {@link AbstractGameMachine.GamePage}
 * 外壳，多榜页用 {@code LeaderboardTabPage}，单榜页用 {@code SingleLeaderboardPage}。
 *
 * <p>
 * 全服成绩存在 {@link com.gtocore.common.machine.noenergy.game.milestone.GameMilestones}，扫雷与数独各有独立的
 * 里程碑存档。排行榜页统一用 {@link com.gtocore.api.gui.GTOGuiTextures#LEADERBOARD} 图标，且不显示余额。
 */
package com.gtocore.common.machine.noenergy.game;
