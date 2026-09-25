package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.TeamResearchContext;
import com.gtocore.api.research.TeamResearchSavedData;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.integration.emi.research.EmiResearchHelper;
import com.gtocore.integration.emi.research.ResearchTagEmiStack;
import com.gtocore.integration.emi.research.TechNodeEmiStack;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.SlotButton;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;

import com.lowdragmc.lowdraglib.gui.ingredient.IIngredientSlot;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.vfyjxf.taffy.style.FlexWrap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * 一个科技节点的详情，两端各建一次（弹出面板的工厂、EMI 页里的详情栏都调 {@link #build}）：
 * <ol>
 * <li>标题：节点图标 + 名称；</li>
 * <li>状态面板：状态（已解锁 / 可解锁 / 未解锁，悬停看原因）、数据等级（悬停看导出要求）；</li>
 * <li>说明文字；</li>
 * <li>前置科技：一排节点格，点击跳到该节点（别的树也能跳）；</li>
 * <li>解锁需求：CWU 与各领域研究点数的进度条（尤里卡加成画在进度后面），点研究点数的条查看获取途径；</li>
 * <li>可解锁：解锁后得到的配方产物（左键查配方、右键查用途）与其他奖励；</li>
 * <li>使用方追加的区块（数据中心的"启动研究"）、调试器的"强制解锁"。</li>
 * </ol>
 * 控件树两端一致：区块一律两端都建，个数只取决于注册时就定下的数据（前置、需求、附加说明）；
 * 说明文字、配方产物这类"有没有"可能因两端语言表或配方数据而不同的，区块照建、由服务端判定是否显示后下发。
 * 会变的数值都由服务端取值下发；同一节点的状态每 tick 只算一次（{@link StateMemo}）。
 */
@DataGeneratorScanned
public final class TechNodeDetails {

    @RegisterLanguage(cn = "[数据等级%s]", en = "[Tier %s]")
    public static final String TIER_LABEL = "gtocore.research.side_tab.tier";
    @RegisterLanguage(cn = "该等级的节点解锁的配方数据需要%s导出", en = "Unlocking recipes at this tier requires %s to export")
    private static final String TIER_DESC = "gtocore.research.side_tab.tier_desc";
    @RegisterLanguage(cn = "CWU", en = "CWU")
    private static final String CWU_LABEL = "gtocore.research.side_tab.cwu";
    @RegisterLanguage(cn = "尤里卡为该节点提供了%s%%的进度加成", en = "Eureka! provides %s%% progress bonus for this node")
    private static final String CWU_EUREKA_DESC = "gtocore.research.side_tab.cwu_eureka_desc";
    @RegisterLanguage(cn = "扫描%s以触发尤里卡，提供%s%%研究进度加成", en = "Scan %s to trigger Eureka! and provide %s%% research progress bonus")
    private static final String CWU_NO_EUREKA_DESC = "gtocore.research.side_tab.cwu_eureka_scan_desc";
    @RegisterLanguage(cn = "解锁需求：", en = "Unlock Requirements:")
    private static final String REQUIREMENTS_LABEL = "gtocore.research.side_tab.requirements";
    @RegisterLanguage(cn = "前置节点：", en = "Prerequisites:")
    private static final String PREREQUISITES_LABEL = "gtocore.research.side_tab.prerequisites";
    @RegisterLanguage(cn = "点击跳转", en = "Click to navigate")
    private static final String NAVIGATE_LABEL = "gtocore.research.side_tab.navigate";
    @RegisterLanguage(cn = "状态", en = "State")
    private static final String STATE = "gtocore.techtree.details.state";
    @RegisterLanguage(cn = "数据等级", en = "Data tier")
    private static final String TIER = "gtocore.techtree.details.tier";
    @RegisterLanguage(cn = "前置科技尚未全部解锁", en = "Not all prerequisites are unlocked yet")
    private static final String LOCKED_REASON = "gtocore.techtree.details.locked_reason";
    @RegisterLanguage(cn = "前置科技已全部解锁，满足解锁需求即可解锁", en = "All prerequisites are unlocked; meet the requirements to unlock it")
    private static final String AVAILABLE_REASON = "gtocore.techtree.details.available_reason";
    @RegisterLanguage(cn = "点击查看获取途径", en = "Click to see how to obtain it")
    private static final String TAG_HINT = "gtocore.techtree.details.tag_hint";
    @RegisterLanguage(cn = "强制解锁", en = "Force unlock")
    private static final String FORCE_UNLOCK = "gtocore.techtree.details.force_unlock";
    @RegisterLanguage(cn = "该科技已解锁", en = "This node is already unlocked")
    public static final String ALREADY_UNLOCKED = "gtocore.techtree.details.already_unlocked";

    /// 标题、说明文字的最大宽度：一个装满 9 槽的区块的内宽
    private static final int TEXT_WIDTH = UISizes.SLOT_ROW_WIDTH;

    private TechNodeDetails() {}

    /**
     * 往 {@code column}（纵向、区块间距 {@link UISizes#SECTION_GAP}）里加节点详情。
     *
     * @param navigator 与所在视图的连接（取打开界面的玩家、跳转节点）
     * @param force     调试器：显示"强制解锁"
     * @param extra     使用方追加的区块（可为 null），参数是新区块与节点
     */
    public static void build(UIElement column, TechNode node, TechTreeView.Navigator navigator, boolean force,
                             @Nullable BiConsumer<UIElement, TechNode> extra) {
        Supplier<Player> player = navigator::player;
        var state = new StateMemo(node);

        var name = TextLine.constant(LayoutStyle.AUTO, node.getDisplayName()).layout(l -> l.flex(1));
        var header = UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.SECTION_GAP).alignCenter())
                .addChildren(new NodeSlot(node, player, null), name);
        column.addChild(header);

        var status = new StatusPanel();
        status.addLine(STATE, () -> Component.translatable(TechTreeView.stateKey(state.get(player.get()))))
                .level(() -> switch (state.get(player.get())) {
                    case TechTreeView.UNLOCKED -> StatusLine.Level.GOOD;
                    case TechTreeView.AVAILABLE -> StatusLine.Level.WARNING;
                    default -> StatusLine.Level.ERROR;
                })
                .detail(() -> switch (state.get(player.get())) {
                    case TechTreeView.UNLOCKED -> Component.empty();
                    case TechTreeView.AVAILABLE -> Component.translatable(AVAILABLE_REASON);
                    default -> Component.translatable(LOCKED_REASON);
                });
        var tierLine = status.addLine(TIER, () -> Component.literal(Integer.toString(node.getTier())));
        var tierItem = node.getTierItem();
        if (!tierItem.isEmpty()) tierLine.detail(() -> Component.translatable(TIER_DESC, tierItem.getHoverName()));
        column.addChild(status);

        // 说明：有没有取决于语言表（两端可能不同），区块照建，由服务端判定显示
        boolean hasDesc = node.desc() != null;
        var descSection = UIElement.section().addChild(Label.of(() -> {
            var desc = node.desc();
            return desc == null ? Component.empty() : desc;
        }, TEXT_WIDTH));
        column.addChild(descSection);
        bindDisplay(column, descSection, () -> hasDesc);

        if (!node.prerequisites.isEmpty()) {
            var slots = UIElement.row(LayoutStyle.AUTO).layout(l -> l.heightAuto().flexWrap(FlexWrap.WRAP).maxWidth(UISizes.SLOT_ROW_WIDTH));
            for (var prerequisite : node.prerequisites) slots.addChild(new NodeSlot(prerequisite, player, navigator));
            column.addChild(UIElement.section().addChildren(TextLine.translatable(LayoutStyle.AUTO, PREREQUISITES_LABEL), slots));
        }

        var requirements = node.getRequirements();
        if (requirements != null && (requirements.getCwuNeeded() > 0 || !requirements.getMaterialNeeded().isEmpty())) {
            var section = UIElement.section().addChild(TextLine.translatable(LayoutStyle.AUTO, REQUIREMENTS_LABEL));
            if (requirements.getCwuNeeded() > 0) section.addChild(cwuBar(node, player));
            for (var tag : sortedTags(node)) section.addChild(new ResearchTagBar(node, tag, player));
            column.addChild(section);
        }

        // 可解锁：配方产物取决于配方数据（两端可能不同），区块与格子照建，由服务端判定显示
        boolean hasRewards = !node.getRecipePrimaryOutputs().isEmpty() || !node.getAdditionalLines().isEmpty();
        var rewards = UIElement.section().addChildren(TextLine.translatable(LayoutStyle.AUTO, TechNode.UNLOCKABLE_LABEL), new RewardGrid(node));
        for (var line : node.getAdditionalLines()) rewards.addChild(Label.of(() -> line, TEXT_WIDTH));
        column.addChild(rewards);
        bindDisplay(column, rewards, () -> hasRewards);

        if (extra != null) {
            var section = UIElement.section();
            extra.accept(section, node);
            if (!section.widgets.isEmpty()) column.addChild(section);
        }

        if (force) {
            var unlock = Button.translatable(LayoutStyle.AUTO, FORCE_UNLOCK)
                    .setVariant(UITheme.ButtonVariant.DANGER)
                    .setOnServerClick(() -> {
                        var p = player.get();
                        if (p != null) TechTreeSavedData.forceUnlock(TechTreeSavedData.getTeamUUID(p), node);
                    })
                    .disabled(() -> state.get(player.get()) == TechTreeView.UNLOCKED, ALREADY_UNLOCKED);
            column.addChild(unlock);
        }
    }

    /**
     * 区块显隐：服务端判定、下发，两端各自显隐（控件树不变）。同步值挂在父元素上——隐藏会停掉元素自己的同步，
     * 挂在被隐藏的区块上就再也收不到"重新显示"。
     */
    private static void bindDisplay(UIElement parent, UIElement section, BooleanSupplier shown) {
        section.setDisplay(false);
        parent.addSyncValue(SyncValue.of(shown::getAsBoolean, SyncValue.BOOLEAN, false).onChanged(section::setDisplay));
    }

    /**
     * 一个节点的状态，按（解锁数据修改计数，队伍）缓存：状态行的数值、等级、说明和强制解锁按钮每 tick 各取一次，只算一遍。
     */
    private static final class StateMemo {

        private final TechNode node;
        private int modCount = -1;
        @Nullable
        private UUID team;
        private byte value;

        private StateMemo(TechNode node) {
            this.node = node;
        }

        byte get(@Nullable Player player) {
            if (player == null) return TechTreeView.LOCKED;
            var currentTeam = TechTreeSavedData.getTeamUUID(player);
            int currentMod = TechTreeSavedData.getModCount();
            if (currentMod != modCount || !currentTeam.equals(team)) {
                modCount = currentMod;
                team = currentTeam;
                value = stateOf(player, node);
            }
            return value;
        }
    }

    /** 节点状态（服务端、或纯客户端界面的本端）。 */
    static byte stateOf(@Nullable Player player, TechNode node) {
        if (player == null) return TechTreeView.LOCKED;
        var team = TechTreeSavedData.getTeamUUID(player);
        if (TechTreeSavedData.isUnlocked(team, node)) return TechTreeView.UNLOCKED;
        return TechTreeView.prerequisitesUnlocked(team, node) ? TechTreeView.AVAILABLE : TechTreeView.LOCKED;
    }

    @Nullable
    private static TeamResearchContext context(@Nullable Player player) {
        return player == null ? null : TeamResearchSavedData.getOrCreateContext(player);
    }

    /** 需求里的研究领域，按名称排序（两端顺序一致）。 */
    private static List<ResearchTag> sortedTags(TechNode node) {
        var needed = node.getRequirements().getMaterialNeeded();
        var tags = new ArrayList<ResearchTag>(needed.size());
        tags.addAll(needed.keySet());
        tags.sort(Comparator.comparing(ResearchTag::getName));
        return tags;
    }

    /** CWU 进度：已累计量；扫描过尤里卡物品时，加成部分画在进度后面，悬停说明加成来源。 */
    private static ProgressBar cwuBar(TechNode node, Supplier<Player> player) {
        var requirements = node.getRequirements();
        var eurekaItem = requirements.getEurekaItem();
        int bonus = Math.round(requirements.getEurekaProgress() * 1000);
        var bar = new ProgressBar(LayoutStyle.AUTO, Component.translatable(CWU_LABEL), TechTreeStyle.get().cwuBarFill, () -> {
            var context = context(player.get());
            if (context == null) return ProgressBar.Progress.EMPTY;
            boolean eureka = eurekaItem != null && context.hasScanned(eurekaItem);
            return new ProgressBar.Progress(context.techNodeAccCWU().getOrDefault(node, 0L), requirements.getCwuNeeded(), eureka ? bonus : 0);
        });
        if (eurekaItem != null) {
            String percent = FormattingUtil.formatNumber2Places(requirements.getEurekaProgress() * 100f);
            bar.detail(() -> {
                var context = context(player.get());
                return context != null && context.hasScanned(eurekaItem) ? Component.translatable(CWU_EUREKA_DESC, percent) :
                        Component.translatable(CWU_NO_EUREKA_DESC, eurekaItem.getDisplayName(), percent);
            });
        }
        return bar;
    }

    // ==================== 研究点数进度条 ====================

    /** 某一领域研究点数的进度；悬停交给 EMI（查看获取途径），点击打开 EMI 的配方页。 */
    private static final class ResearchTagBar extends ProgressBar implements IIngredientSlot {

        private final ResearchTag tag;
        private final long needed;

        private ResearchTagBar(TechNode node, ResearchTag tag, Supplier<Player> player) {
            super(LayoutStyle.AUTO, tag.getDisplayName(), tag.getColor(), progress(node, tag, player));
            this.tag = tag;
            this.needed = node.getRequirements().getMaterialNeeded().getLong(tag);
            detail(() -> Component.translatable(TAG_HINT));
        }

        private static Supplier<Progress> progress(TechNode node, ResearchTag tag, Supplier<Player> player) {
            long needed = node.getRequirements().getMaterialNeeded().getLong(tag);
            return () -> {
                var context = context(player.get());
                return context == null ? Progress.EMPTY : new Progress(context.researchPoints().getOrDefault(tag, 0L), needed, 0);
            };
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public @Nullable Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
            return isMouseOverElement(mouseX, mouseY) ? new ResearchTagEmiStack(tag).setAmount(needed) : null;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOverElement(mouseX, mouseY) || (button != 0 && button != 1)) return false;
            EmiApi.displayRecipes(new ResearchTagEmiStack(tag));
            playButtonClickSound();
            return true;
        }
    }

    // ==================== 节点格 ====================

    /**
     * 显示一个节点的格子：节点图标，没解锁时蒙一层灰；悬停显示名称、所属树、状态；交给 EMI（节点的配方页即科技树页）。
     * {@code navigator} 不为 null 时可点击，跳到该节点。状态由服务端取值下发。
     */
    private static final class NodeSlot extends SlotButton implements IIngredientSlot {

        private final TechNode node;
        private final SyncValue<Integer> state;
        private final boolean navigable;
        /// 提示按状态缓存
        private int tooltipState = -1;
        private List<Component> tooltip = Collections.emptyList();

        private NodeSlot(TechNode node, Supplier<Player> player, @Nullable TechTreeView.Navigator navigator) {
            super(new NodeIcon(node.icon));
            this.node = node;
            this.navigable = navigator != null;
            var memo = new StateMemo(node);
            this.state = addSyncValue(SyncValue.ofInt(() -> (int) memo.get(player.get()), TechTreeView.LOCKED));
            if (navigator != null) setOnClientClick(() -> navigator.navigateTo(node));
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            if (state.getValue() == TechTreeView.UNLOCKED) return;
            // 与画布上一样：未解锁的节点图标蒙一层灰（画在物品图标之上）
            int x = getPositionX(), y = getPositionY();
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(0, 0, 200);
            graphics.fill(x + 1, y + 1, x + SIZE - 1, y + SIZE - 1, TechTreeStyle.get().lockedNodeOverlay);
            pose.popPose();
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (gui == null || gui.getModularUIGui() == null || !isMouseOverElement(mouseX, mouseY)) return;
            int current = state.getValue();
            if (current != tooltipState) {
                tooltipState = current;
                tooltip = createTooltip((byte) current);
            }
            gui.getModularUIGui().setHoverTooltip(tooltip, ItemStack.EMPTY, null, null);
        }

        private List<Component> createTooltip(byte current) {
            var lines = new ArrayList<Component>(4);
            int color = TechTreeView.stateTooltipColor(current);
            lines.add(node.getDisplayName().withStyle(s -> s.withColor(color)));
            lines.add(TechTreeManager.getTreeName(node.getManager()).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(TechTreeView.stateKey(current)).withStyle(s -> s.withColor(color)));
            if (navigable) lines.add(Component.translatable(NAVIGATE_LABEL).withStyle(ChatFormatting.DARK_GRAY));
            return lines;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public @Nullable Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
            return isMouseOverElement(mouseX, mouseY) ? new TechNodeEmiStack(node) : null;
        }
    }

    /** 节点图标（AE 物品 / 流体），没有图标时画一个问号。 */
    private record NodeIcon(@Nullable AEKey key) implements IGuiTexture {

        @Override
        @OnlyIn(Dist.CLIENT)
        public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(x, y, 0);
            pose.scale(width / 16f, height / 16f, 1);
            if (key != null) {
                AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, 0, 0, key);
            } else {
                graphics.drawString(Minecraft.getInstance().font, "?", 5, 4, TechTreeStyle.get().nodeIconFallback, false);
            }
            pose.popPose();
        }
    }

    // ==================== 可解锁的配方产物 ====================

    /**
     * 解锁后得到的配方产物，每行 9 格。一个控件画整组格子：格子数按节点的静态数据定（两端一致），
     * 格子里的 EMI 物品只在客户端取（服务端没有 EMI 的渲染数据）。左键查配方、右键查用途。
     */
    private static final class RewardGrid extends UIElement implements IIngredientSlot {

        private final TechNode node;
        private final int count;
        @Nullable
        private List<EmiStack> stacks;

        private RewardGrid(TechNode node) {
            this.node = node;
            this.count = node.getRecipePrimaryOutputs().size();
            int columns = Math.min(count, UISizes.SLOTS_PER_ROW), rows = (count + UISizes.SLOTS_PER_ROW - 1) / UISizes.SLOTS_PER_ROW;
            layout(l -> l.size(columns * UISizes.SLOT, rows * UISizes.SLOT));
        }

        @OnlyIn(Dist.CLIENT)
        private List<EmiStack> stacks() {
            if (stacks == null) stacks = EmiResearchHelper.toEmiStacks(node.getRecipePrimaryOutputs());
            return stacks;
        }

        /** 鼠标下的格子序号，不在格子上为 -1。 */
        private int slotAt(double mouseX, double mouseY) {
            if (!isMouseOverElement(mouseX, mouseY)) return -1;
            int column = (int) (mouseX - getPositionX()) / UISizes.SLOT, row = (int) (mouseY - getPositionY()) / UISizes.SLOT;
            int index = row * UISizes.SLOTS_PER_ROW + column;
            return column < UISizes.SLOTS_PER_ROW && index < count ? index : -1;
        }

        @OnlyIn(Dist.CLIENT)
        @Nullable
        private EmiStack stackAt(double mouseX, double mouseY) {
            int index = slotAt(mouseX, mouseY);
            var list = stacks();
            return index >= 0 && index < list.size() ? list.get(index) : null;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            var list = stacks();
            int hovered = slotAt(mouseX, mouseY);
            for (int i = 0; i < count; i++) {
                int x = getPositionX() + i % UISizes.SLOTS_PER_ROW * UISizes.SLOT, y = getPositionY() + i / UISizes.SLOTS_PER_ROW * UISizes.SLOT;
                UITheme.ITEM_SLOT.draw(graphics, mouseX, mouseY, x, y, UISizes.SLOT, UISizes.SLOT);
                if (i < list.size()) list.get(i).render(graphics, x + 1, y + 1, partialTicks, EmiIngredient.RENDER_ICON);
                if (i == hovered) {
                    // 与物品槽的悬停一样：盖在物品上，只写颜色不写透明度
                    RenderSystem.colorMask(true, true, true, false);
                    graphics.fill(x + 1, y + 1, x + UISizes.SLOT - 1, y + UISizes.SLOT - 1, 200, UITheme.SLOT_HOVER_OVERLAY);
                    RenderSystem.colorMask(true, true, true, true);
                }
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            var stack = stackAt(mouseX, mouseY);
            if (stack != null && gui != null && gui.getModularUIGui() != null) {
                gui.getModularUIGui().setHoverTooltip(stack.getTooltipText(), ItemStack.EMPTY, null, null);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            var stack = stackAt(mouseX, mouseY);
            if (stack == null || (button != 0 && button != 1)) return false;
            if (button == 0) EmiApi.displayRecipes(stack);
            else EmiApi.displayUses(stack);
            playButtonClickSound();
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public @Nullable Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
            return stackAt(mouseX, mouseY);
        }
    }
}
