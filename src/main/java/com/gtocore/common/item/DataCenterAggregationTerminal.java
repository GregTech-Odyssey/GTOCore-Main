package com.gtocore.common.item;

import com.gtocore.common.machine.multiblock.electric.research.DataCenter;
import com.gtocore.common.machine.multiblock.electric.research.DataCenterAggregationUI;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.item.armor.ArmorTooltips;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@DataGeneratorScanned
public enum DataCenterAggregationTerminal implements IItemUIFactory, IAddInformation {

    INSTANCE;

    public static final int MAX_BINDINGS = 16;
    private static final int TOOLTIP_LOCATIONS = 3;
    public static final String TAG_BINDINGS = "dataCenters";
    private static final String TAG_DIMENSION = "dim";
    private static final String TAG_POS = "pos";

    @RegisterLanguage(cn = "绑定", en = "Binding")
    private static final String SECTION_BINDING = "gtocore.data_center_aggregation_terminal.section.binding";
    @RegisterLanguage(cn = "功能", en = "Features")
    private static final String SECTION_FEATURES = "gtocore.data_center_aggregation_terminal.section.features";
    @RegisterLanguage(cn = "数据中心 %s", en = "Data Centers %s")
    private static final String INFO_COUNT = "gtocore.data_center_aggregation_terminal.info.count";
    @RegisterLanguage(cn = "%s / %s", en = "%s / %s")
    private static final String VALUE_COUNT = "gtocore.data_center_aggregation_terminal.value.count";
    @RegisterLanguage(cn = "未绑定", en = "None")
    private static final String VALUE_UNBOUND = "gtocore.data_center_aggregation_terminal.value.unbound";
    @RegisterLanguage(cn = "%s, %s, %s · %s", en = "%s, %s, %s · %s")
    public static final String VALUE_LOCATION = "gtocore.data_center_aggregation_terminal.value.location";
    @RegisterLanguage(cn = "另有 %s 台", en = "%s more")
    private static final String VALUE_MORE = "gtocore.data_center_aggregation_terminal.value.more";
    @RegisterLanguage(cn = "打开聚合终端", en = "Open terminal")
    private static final String FEATURE_OPEN = "gtocore.data_center_aggregation_terminal.feature.open";
    @RegisterLanguage(cn = "绑定数据中心", en = "Bind Data Center")
    private static final String FEATURE_BIND = "gtocore.data_center_aggregation_terminal.feature.bind";
    @RegisterLanguage(cn = "解除绑定", en = "Unbind")
    private static final String FEATURE_UNBIND = "gtocore.data_center_aggregation_terminal.feature.unbind";
    @RegisterLanguage(cn = "可用", en = "Ready")
    private static final String STATE_READY = "gtocore.data_center_aggregation_terminal.state.ready";
    @RegisterLanguage(cn = "需要绑定数据中心", en = "Needs a Data Center")
    private static final String STATE_NEED_BIND = "gtocore.data_center_aggregation_terminal.state.need_bind";
    @RegisterLanguage(cn = "已满", en = "Full")
    private static final String STATE_FULL = "gtocore.data_center_aggregation_terminal.state.full";
    @RegisterLanguage(cn = "[右键]", en = "[Right Click]")
    private static final String HINT_USE = "gtocore.data_center_aggregation_terminal.hint.use";
    @RegisterLanguage(cn = "[Shift + 右键数据中心]", en = "[Shift + Right Click a Data Center]")
    private static final String HINT_BIND = "gtocore.data_center_aggregation_terminal.hint.bind";
    @RegisterLanguage(cn = "[终端总览页]", en = "[Terminal overview]")
    private static final String HINT_UNBIND = "gtocore.data_center_aggregation_terminal.hint.unbind";
    @RegisterLanguage(cn = "总览每台数据中心的状态、研究节点、发起人与算力上限，并可在科技树中启动或取消研究", en = "Overview of every bound Data Center's status, research node, initiator and CWU cap; launch or cancel research from the tech tree")
    private static final String DETAIL_OPEN = "gtocore.data_center_aggregation_terminal.detail.open";
    @RegisterLanguage(cn = "研究由各台数据中心执行，照常消耗电力与冷却液、获取算力，CWU 累加到团队进度；其区块须保持加载", en = "Research runs in each Data Center, consuming power, coolant and computation as usual; CWU adds to team progress. Their chunks must stay loaded")
    private static final String DETAIL_RESEARCH = "gtocore.data_center_aggregation_terminal.detail.research";
    @RegisterLanguage(cn = "启动研究时交给空闲且算力上限最高的一台；多台可同时研究不同节点", en = "Launching research picks the idle one with the highest CWU cap; several can research different nodes at once")
    private static final String DETAIL_DISPATCH = "gtocore.data_center_aggregation_terminal.detail.dispatch";
    @RegisterLanguage(cn = "最多绑定 %s 台；终端本身无需充电", en = "Up to %s Data Centers; the terminal needs no charge")
    private static final String DETAIL_LIMIT = "gtocore.data_center_aggregation_terminal.detail.limit";

    @RegisterLanguage(cn = "%s 已绑定数据中心 (%s, %s, %s)，共 %s 台", en = "%s bound the Data Center at (%s, %s, %s); %s in total")
    private static final String MESSAGE_BOUND = "gtocore.data_center_aggregation_terminal.message.bound";
    @RegisterLanguage(cn = "该数据中心已绑定", en = "This Data Center is already bound")
    private static final String MESSAGE_ALREADY = "gtocore.data_center_aggregation_terminal.message.already";
    @RegisterLanguage(cn = "最多绑定 %s 台数据中心，请先在终端中解除绑定", en = "Up to %s Data Centers can be bound; unbind one in the terminal first")
    private static final String MESSAGE_FULL = "gtocore.data_center_aggregation_terminal.message.full";
    @RegisterLanguage(cn = "%s 尚未绑定数据中心，潜行右键数据中心进行绑定", en = "%s has no bound Data Center; sneak and right click one to bind")
    private static final String MESSAGE_UNBOUND = "gtocore.data_center_aggregation_terminal.message.unbound";
    @RegisterLanguage(cn = "无权访问该数据中心", en = "You have no access to this Data Center")
    private static final String MESSAGE_NO_PERMISSION = "gtocore.data_center_aggregation_terminal.message.no_permission";

    public record Binding(ResourceKey<Level> dimension, BlockPos pos) {}

    public static List<Binding> getBindings(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_BINDINGS, Tag.TAG_LIST)) return Collections.emptyList();
        var list = tag.getList(TAG_BINDINGS, Tag.TAG_COMPOUND);
        var result = new ArrayList<Binding>(list.size());
        for (int i = 0; i < list.size(); i++) {
            var entry = list.getCompound(i);
            var id = ResourceLocation.tryParse(entry.getString(TAG_DIMENSION));
            if (id != null) result.add(new Binding(ResourceKey.create(Registries.DIMENSION, id), BlockPos.of(entry.getLong(TAG_POS))));
        }
        return result;
    }

    private static void setBindings(ItemStack stack, List<Binding> bindings) {
        if (bindings.isEmpty()) {
            var tag = stack.getTag();
            if (tag != null) {
                tag.remove(TAG_BINDINGS);
                if (tag.isEmpty()) stack.setTag(null);
            }
            return;
        }
        var list = new ListTag();
        for (var binding : bindings) {
            var entry = new CompoundTag();
            entry.putString(TAG_DIMENSION, binding.dimension().location().toString());
            entry.putLong(TAG_POS, binding.pos().asLong());
            list.add(entry);
        }
        stack.getOrCreateTag().put(TAG_BINDINGS, list);
    }

    public static boolean unbind(ItemStack stack, Binding binding) {
        var bindings = new ArrayList<>(getBindings(stack));
        if (!bindings.remove(binding)) return false;
        setBindings(stack, bindings);
        return true;
    }

    public static void unbindAll(ItemStack stack) {
        setBindings(stack, Collections.emptyList());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;
        var level = context.getLevel();
        var pos = context.getClickedPos();
        if (!(MetaMachine.getMachine(level, pos) instanceof DataCenter machine)) return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.displayClientMessage(bind(serverPlayer, stack, machine, new Binding(level.dimension(), pos.immutable())), true);
        return InteractionResult.SUCCESS;
    }

    private static Component bind(ServerPlayer player, ItemStack stack, DataCenter machine, Binding binding) {
        if (!MachineOwner.canOpenOwnerMachine(player, machine)) return Component.translatable(MESSAGE_NO_PERMISSION).withStyle(ChatFormatting.RED);
        var bindings = new ArrayList<>(getBindings(stack));
        if (bindings.contains(binding)) return Component.translatable(MESSAGE_ALREADY).withStyle(ChatFormatting.YELLOW);
        if (bindings.size() >= MAX_BINDINGS) return Component.translatable(MESSAGE_FULL, MAX_BINDINGS).withStyle(ChatFormatting.RED);
        bindings.add(binding);
        setBindings(stack, bindings);
        var pos = binding.pos();
        return Component.translatable(MESSAGE_BOUND, stack.getHoverName(), pos.getX(), pos.getY(), pos.getZ(), bindings.size()).withStyle(ChatFormatting.GREEN);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        if (player instanceof ServerPlayer serverPlayer) {
            if (getBindings(stack).isEmpty()) {
                serverPlayer.displayClientMessage(Component.translatable(MESSAGE_UNBOUND, stack.getHoverName()).withStyle(ChatFormatting.YELLOW), true);
            } else {
                HeldItemUIFactory.INSTANCE.openUI(serverPlayer, usedHand);
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return DataCenterAggregationUI.createUI(holder, entityPlayer);
    }

    public static MutableComponent location(Binding binding) {
        var pos = binding.pos();
        var id = binding.dimension().location();
        return Component.translatable(VALUE_LOCATION, pos.getX(), pos.getY(), pos.getZ(), "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString());
    }

    @Override
    public void appendTooltips(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag isAdvanced) {
        var bindings = getBindings(stack);
        int count = bindings.size();
        lines.add(ArmorTooltips.section(SECTION_BINDING));
        lines.add(ArmorTooltips.info(INFO_COUNT, count == 0 ? Component.translatable(VALUE_UNBOUND).withStyle(ChatFormatting.YELLOW) :
                Component.translatable(VALUE_COUNT, count, MAX_BINDINGS).withStyle(ChatFormatting.WHITE)));
        for (int i = 0; i < Math.min(count, TOOLTIP_LOCATIONS); i++) {
            lines.add(Component.literal("   · ").append(location(bindings.get(i))).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (count > TOOLTIP_LOCATIONS) lines.add(Component.literal("   · ").append(Component.translatable(VALUE_MORE, count - TOOLTIP_LOCATIONS)).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(ArmorTooltips.section(SECTION_FEATURES));
        feature(lines, FEATURE_OPEN, count > 0 ? ArmorTooltips.state(STATE_READY, ChatFormatting.GREEN) : ArmorTooltips.state(STATE_NEED_BIND, ChatFormatting.YELLOW), HINT_USE);
        ArmorTooltips.addDetail(lines, DETAIL_OPEN);
        ArmorTooltips.addDetail(lines, DETAIL_RESEARCH);
        ArmorTooltips.addDetail(lines, DETAIL_DISPATCH);
        feature(lines, FEATURE_BIND, count >= MAX_BINDINGS ? ArmorTooltips.state(STATE_FULL, ChatFormatting.YELLOW) : null, HINT_BIND);
        feature(lines, FEATURE_UNBIND, null, HINT_UNBIND);
        ArmorTooltips.addDetail(lines, DETAIL_LIMIT, MAX_BINDINGS);
        if (!ArmorTooltips.showDetails()) lines.add(ArmorTooltips.SHIFT_HINT);
    }

    private static void feature(List<Component> lines, String name, @Nullable Component state, String hint) {
        MutableComponent line = Component.literal(" ▸ ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable(name).withStyle(ChatFormatting.WHITE));
        if (state != null) line.append("  ").append(state);
        line.append(" ").append(Component.translatable(hint).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(line);
    }
}
