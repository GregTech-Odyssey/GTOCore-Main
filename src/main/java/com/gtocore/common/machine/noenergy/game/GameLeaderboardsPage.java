package com.gtocore.common.machine.noenergy.game;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.api.gui.ui_element.SelectBox;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.SwitchedContent;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.AlignContent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 全部榜单页：把各游戏机的全服榜聚合成一页，顶部按「游戏 → 一级维度 → 二级维度」逐级筛选，
 * 每级一个下拉栏（{@link SelectBox}），下面只显示选中的那张榜；目录见 {@link GameBoardCatalog}。
 *
 * <p>
 * 独立于任何游戏机：由 {@link GameLeaderboardBrowser} 的命令入口以单独窗口打开，数据直接从三份里程碑存档取，
 * 所以不经过机器实例，也没有余额面板。选择项压成一个整数作为 {@link SwitchedContent} 的键，任一维变化都重建整页。
 */
@DataGeneratorScanned
public final class GameLeaderboardsPage implements IFancyUIProvider {

    @RegisterLanguage(cn = "全部榜单", en = "All leaderboards")
    private static final String PAGE_TITLE = "gtocore.machine.game.page.all_leaderboards";
    @RegisterLanguage(cn = "汇总所有游戏机的全服排行榜，顶部按游戏与维度逐级筛选",
                      en = "Every game machine's global leaderboard in one place, filtered by game and category")
    private static final String PAGE_TOOLTIP = "gtocore.machine.game.page.all_leaderboards.tooltip";

    /** 选择项打包：游戏与两级维度各占 {@link #LEVEL_BITS} 位。 */
    private static final int LEVEL_BITS = 6;
    private static final int GAME_SHIFT = 2 * LEVEL_BITS;
    private static final int LEVEL_MASK = (1 << LEVEL_BITS) - 1;

    private int game;
    private int first;
    private int second;

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var page = Form.page();
        page.addChild(new SwitchedContent(this::selectionKey, (key, remote) -> selectionContent(key)));
        return page;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return GTOGuiTextures.LEADERBOARD;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(PAGE_TITLE);
    }

    @Override
    public List<Component> getTabTooltips() {
        return List.of(Component.translatable(PAGE_TOOLTIP));
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    private int selectionKey() {
        return game << GAME_SHIFT | first << LEVEL_BITS | second;
    }

    /** 当前选择：第一行是逐级下拉栏，下面是选中的榜。 */
    private UIElement selectionContent(int key) {
        var groups = GameBoardCatalog.ALL;
        int selectedGame = Math.clamp(key >> GAME_SHIFT & LEVEL_MASK, 0, groups.length - 1);
        var group = groups[selectedGame];
        int selectedFirst = group.first() == null ? 0 :
                Math.clamp(key >> LEVEL_BITS & LEVEL_MASK, 0, group.first().count() - 1);
        int selectedSecond = group.second() == null ? 0 :
                Math.clamp(key & LEVEL_MASK, 0, group.second().count() - 1);

        int levels = 1 + (group.first() == null ? 0 : 1) + (group.second() == null ? 0 : 1);
        int width = (UISizes.CONTENT_WIDTH - (levels - 1) * UISizes.GAP) / levels;
        var row = UIElement.row(Button.HEIGHT).layout(l -> l.gapAll(UISizes.GAP)
                .justifyContent(AlignContent.CENTER));
        row.addChild(new SelectBox(width, GameBoardCatalog.GAMES.titleKey(), GameBoardCatalog.GAMES.nameKeys(),
                selectedGame, this::selectGame));
        if (group.first() != null) {
            row.addChild(new SelectBox(width, group.first().titleKey(), group.first().nameKeys(),
                    selectedFirst, this::selectFirst));
        }
        if (group.second() != null) {
            row.addChild(new SelectBox(width, group.second().titleKey(), group.second().nameKeys(),
                    selectedSecond, this::selectSecond));
        }

        var section = Form.section(boardTitleKey(group, selectedFirst, selectedSecond));
        section.addChild(AbstractGameMachine.milestoneTable("game_all_leaderboards_" + key, group.board().columns(),
                () -> group.board().version(server()),
                () -> group.board().rows(server(), selectedFirst, selectedSecond)));

        var content = UIElement.column(LayoutStyle.AUTO)
                .layout(l -> l.minWidth(UISizes.CONTENT_WIDTH).gapAll(UISizes.SECTION_GAP));
        content.addChildren(row, section);
        return content;
    }

    /** 区块标题：有二级维度用它，否则用一级维度，再否则用游戏名。 */
    private static String boardTitleKey(GameBoardCatalog.Group group, int selectedFirst, int selectedSecond) {
        if (group.second() != null) return group.second().nameKey(selectedSecond);
        if (group.first() != null) return group.first().nameKey(selectedFirst);
        return group.nameKey();
    }

    /** 换游戏：两级维度都回到第一项。 */
    private void selectGame(int index) {
        if (isRemote() || index == game || index < 0 || index >= GameBoardCatalog.ALL.length) return;
        game = index;
        first = 0;
        second = 0;
    }

    /** 换一级维度：二级维度回到第一项。 */
    private void selectFirst(int index) {
        if (isRemote() || index == first) return;
        var group = GameBoardCatalog.ALL[Math.clamp(game, 0, GameBoardCatalog.ALL.length - 1)];
        if (group.first() == null || index < 0 || index >= group.first().count()) return;
        first = index;
        second = 0;
    }

    private void selectSecond(int index) {
        if (isRemote() || index == second) return;
        var group = GameBoardCatalog.ALL[Math.clamp(game, 0, GameBoardCatalog.ALL.length - 1)];
        if (group.second() == null || index < 0 || index >= group.second().count()) return;
        second = index;
    }

    private static boolean isRemote() {
        return com.gregtechceu.gtceu.GTCEu.isClientThread();
    }

    /** 服务端从存档取数用的服务器；客户端没有，返回 {@code null}（表格数据本来也只在服务端构建）。 */
    @Nullable
    private static MinecraftServer server() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
