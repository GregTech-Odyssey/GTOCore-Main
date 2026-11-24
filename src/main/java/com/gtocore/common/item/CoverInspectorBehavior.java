package com.gtocore.common.item;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.data.GTODimensions;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.common.cover.AdvancedWirelessTransferCover;
import com.hepdd.gtmthings.common.cover.WirelessCoverIndex;
import com.hepdd.gtmthings.common.cover.WirelessTransferCover;
import com.hepdd.gtmthings.common.item.Behaviour.WirelessTransferCoverPlaceBehavior;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@DataGeneratorScanned
public final class CoverInspectorBehavior implements IInteractionItem, IAddInformation {

    @RegisterLanguage(cn = "覆盖板检查结果：%s", en = "Cover inspection: %s")
    private static final String HEADER = "gtocore.cover_inspector.header";
    @RegisterLanguage(cn = "%s 面：%s", en = "%s side: %s")
    private static final String COVER = "gtocore.cover_inspector.cover";
    @RegisterLanguage(cn = "绑定目标：%s（%s）", en = "Bound target: %s (%s)")
    private static final String TARGET = "gtocore.cover_inspector.target";
    @RegisterLanguage(cn = "目标面：%s", en = "Target side: %s")
    private static final String TARGET_SIDE = "gtocore.cover_inspector.target_side";
    @RegisterLanguage(cn = "绑定来源：%s（%s）", en = "Bound from: %s (%s)")
    private static final String SOURCE = "gtocore.cover_inspector.source";
    @RegisterLanguage(cn = "%s 面覆盖板：%s", en = "%s-side cover: %s")
    private static final String SOURCE_COVER = "gtocore.cover_inspector.source_cover";
    @RegisterLanguage(cn = "未安装覆盖板", en = "No covers installed")
    private static final String EMPTY = "gtocore.cover_inspector.empty";
    @RegisterLanguage(cn = "潜行右击机器或绑定目标检查覆盖板", en = "Sneak-right click a machine or bound target to inspect covers")
    private static final String HINT = "gtocore.cover_inspector.hint";
    @RegisterLanguage(cn = "下方", en = "Down")
    private static final String DOWN = "gtocore.direction.down";
    @RegisterLanguage(cn = "上方", en = "Up")
    private static final String UP = "gtocore.direction.up";
    @RegisterLanguage(cn = "北方", en = "North")
    private static final String NORTH = "gtocore.direction.north";
    @RegisterLanguage(cn = "南方", en = "South")
    private static final String SOUTH = "gtocore.direction.south";
    @RegisterLanguage(cn = "西方", en = "West")
    private static final String WEST = "gtocore.direction.west";
    @RegisterLanguage(cn = "东方", en = "East")
    private static final String EAST = "gtocore.direction.east";

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        if (!context.isSecondaryUseActive()) return InteractionResult.PASS;
        var machine = MetaMachine.getMachine(context.getLevel(), context.getClickedPos());
        if (context.getPlayer() instanceof ServerPlayer player) {
            if (machine != null) player.sendSystemMessage(Component.translatable(HEADER, Component.translatable(machine.getDefinition().getDescriptionId())));
            boolean hasCover = false;
            if (machine != null) {
                for (var side : Direction.values()) {
                    CoverBehavior cover = machine.getCoverContainer().getCoverAtSide(side);
                    if (cover == null) continue;
                    hasCover = true;
                    player.sendSystemMessage(Component.translatable(COVER, Component.translatable("gtocore.direction." + side.getName()), cover.getPickItem().getHoverName()));
                    if (!(cover instanceof WirelessTransferCover || cover instanceof AdvancedWirelessTransferCover)) continue;
                    var tag = cover.getPickItem().getTag();
                    var target = WirelessTransferCoverPlaceBehavior.getStoredGlobalPos(tag);
                    if (target == null) continue;
                    String blockId = tag.getString(WirelessTransferCoverPlaceBehavior.TAG_BLOCK_ID);
                    var targetLevel = player.server.getLevel(target.dimension());
                    var targetMachine = targetLevel == null ? null : MetaMachine.getMachine(targetLevel, target.pos());
                    Component targetName = targetMachine == null ? Component.translatable(blockId) : Component.translatable(targetMachine.getDefinition().getDescriptionId());
                    player.sendSystemMessage(Component.translatable(TARGET, targetName, target.pos().toShortString())
                            .append(" · ").append(Component.translatable(GTODimensions.getTranslationKey(target.dimension()))));
                    player.sendSystemMessage(Component.translatable(TARGET_SIDE, Component.translatable("gtocore.direction." + tag.getString(WirelessTransferCoverPlaceBehavior.TAG_FACING))));
                }
            }
            var clickedPos = GlobalPos.of(context.getLevel().dimension(), context.getClickedPos());
            for (var sourceLevel : player.server.getAllLevels()) {
                var sources = WirelessCoverIndex.sources(sourceLevel, clickedPos);
                if (sources == null) continue;
                for (var source : sources) {
                    var blockEntity = sourceLevel.getBlockEntity(source.pos());
                    var sourceMachine = MetaMachine.getMachine(blockEntity);
                    CoverBehavior cover = sourceMachine.getCoverContainer().getCoverAtSide(source.side());
                    if (!(cover instanceof WirelessTransferCover || cover instanceof AdvancedWirelessTransferCover)) continue;
                    if (!hasCover) {
                        Component targetName = machine == null ? context.getLevel().getBlockState(context.getClickedPos()).getBlock().getName() : Component.translatable(machine.getDefinition().getDescriptionId());
                        player.sendSystemMessage(Component.translatable(HEADER, targetName));
                    }
                    hasCover = true;
                    player.sendSystemMessage(Component.translatable(SOURCE, Component.translatable(sourceMachine.getDefinition().getDescriptionId()), source.pos().toShortString())
                            .append(" · ").append(Component.translatable(GTODimensions.getTranslationKey(sourceLevel.dimension()))));
                    player.sendSystemMessage(Component.translatable(SOURCE_COVER, Component.translatable("gtocore.direction." + source.side().getName()), cover.getPickItem().getHoverName()));
                    var tag = cover.getPickItem().getTag();
                    player.sendSystemMessage(Component.translatable(TARGET_SIDE, Component.translatable("gtocore.direction." + tag.getString(WirelessTransferCoverPlaceBehavior.TAG_FACING))));
                }
            }
            if (machine == null) return hasCover ? InteractionResult.SUCCESS : InteractionResult.PASS;
            if (!hasCover) {
                player.sendSystemMessage(Component.translatable(EMPTY));
            }
        }
        return machine == null ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    @Override
    public void appendTooltips(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag isAdvanced) {
        tooltips.add(Tips.rightClick(Component.translatable(HINT)));
    }
}
