package com.gtocore.common.quest.hot;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.integration.Mods;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.QuestIcon;
import com.gtolib.api.quest.QuestLang;
import com.gtolib.api.quest.QuestScreenLang;
import com.gtolib.api.quest.reward.TableReward;
import com.gtolib.api.rule.RulePreset;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.server.ServerLifecycleHooks;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * 任务编辑器能添加的目标与奖励类型：每种类型登记输入方式、候选项来源，以及把选中的值写成 Java 表达式的办法。
 */
public final class QuestKinds {

    public enum Input {
        NONE,
        ITEM,
        ITEMS,
        BLOCK,
        STACK,
        LIST,
        TABLE,
        TEXT
    }

    public record Choice(Component label, String detail, @Nullable Supplier<QuestIcon> icon, Object value) {}

    record Expr(@Nullable String text, List<String> imports, String error) {

        static Expr of(String text, String... imports) {
            return new Expr(text, List.of(imports), "");
        }

        static Expr fail(String error) {
            return new Expr(null, Collections.emptyList(), error);
        }

        Expr wrap(String before, String after, String... more) {
            if (text == null) return this;
            var all = new ObjectArrayList<String>(imports);
            Collections.addAll(all, more);
            return new Expr(before + text + after, all, "");
        }
    }

    public interface Kind {

        QuestLang label();

        QuestLang hint();

        Input input();

        List<Choice> choices();
    }

    private record Spec(QuestLang label, QuestLang hint, Input input, @Nullable Supplier<List<Choice>> choices, BiFunction<QuestSource, Object, Expr> builder) {

        List<Choice> list() {
            return choices == null ? Collections.emptyList() : choices.get();
        }
    }

    public enum TaskKind implements Kind {

        ITEM(QuestScreenLang.EDIT_TASK_ITEM, QuestScreenLang.EDIT_TASK_ITEM_HINT, Input.ITEM, null, (src, v) -> itemArgs(src, (Item) v).wrap("Task.item(", ")")),
        TAG(QuestScreenLang.EDIT_TASK_TAG, QuestScreenLang.EDIT_TASK_TAG_HINT, Input.LIST, QuestKinds::tags, (src, v) -> tag((TagKey<?>) v).wrap("Task.tag(", ", 1)")),
        ANY_OF(QuestScreenLang.EDIT_TASK_ANY_OF, QuestScreenLang.EDIT_TASK_ANY_OF_HINT, Input.ITEMS, null, (src, v) -> anyOf(src, (List<?>) v)),
        CHECKMARK(QuestScreenLang.EDIT_TASK_CHECKMARK, QuestScreenLang.EDIT_TASK_CHECKMARK_HINT, Input.NONE, null, (src, v) -> Expr.of("Task.checkmark()")),
        TECH_NODE(QuestScreenLang.EDIT_TASK_TECH_NODE, QuestScreenLang.EDIT_TASK_TECH_NODE_HINT, Input.LIST, QuestKinds::techNodes,
                (src, v) -> NODES.find(v, "科技节点 " + ((TechNode) v).name).wrap("Task.techNode(", ")")),
        DIMENSION(QuestScreenLang.EDIT_TASK_DIMENSION, QuestScreenLang.EDIT_TASK_DIMENSION_HINT, Input.LIST, QuestKinds::dimensions,
                (src, v) -> DIMENSIONS.find(v, "该维度").wrap("Task.dimension(", ")")),
        KILL(QuestScreenLang.EDIT_TASK_KILL, QuestScreenLang.EDIT_TASK_KILL_HINT, Input.LIST, QuestKinds::entities, (src, v) -> entity((EntityType<?>) v).wrap("Task.kill(", ", 1)")),
        STAT(QuestScreenLang.EDIT_TASK_STAT, QuestScreenLang.EDIT_TASK_STAT_HINT, Input.LIST, QuestKinds::stats, (src, v) -> stat((ResourceLocation) v).wrap("Task.stat(", ", 1)")),
        STRUCTURE(QuestScreenLang.EDIT_TASK_STRUCTURE, QuestScreenLang.EDIT_TASK_STRUCTURE_HINT, Input.LIST, QuestKinds::structures,
                (src, v) -> structure((ResourceKey<?>) v).wrap("Task.structure(", ")")),
        OBSERVE(QuestScreenLang.EDIT_TASK_OBSERVE, QuestScreenLang.EDIT_TASK_OBSERVE_HINT, Input.BLOCK, null,
                (src, v) -> itemLike(src, (Item) v).wrap("Task.observe(Block.byItem(", ".asItem()), 0)", Block.class.getName())),
        DIFFICULTY(QuestScreenLang.EDIT_TASK_DIFFICULTY, QuestScreenLang.EDIT_TASK_DIFFICULTY_HINT, Input.LIST, QuestKinds::difficulties, (src, v) -> Expr.of("Task.difficulty(" + v + ")")),
        MOD(QuestScreenLang.EDIT_TASK_MOD, QuestScreenLang.EDIT_TASK_MOD_HINT, Input.LIST, QuestKinds::mods,
                (src, v) -> Expr.of("Task.modLoaded(Mods." + ((Mods) v).name() + ")", Mods.class.getName()));

        private final Spec spec;

        TaskKind(QuestLang label, QuestLang hint, Input input, @Nullable Supplier<List<Choice>> choices, BiFunction<QuestSource, Object, Expr> builder) {
            spec = new Spec(label, hint, input, choices, builder);
        }

        @Override
        public QuestLang label() {
            return spec.label;
        }

        @Override
        public QuestLang hint() {
            return spec.hint;
        }

        @Override
        public Input input() {
            return spec.input;
        }

        @Override
        public List<Choice> choices() {
            return spec.list();
        }

        Expr build(QuestSource src, @Nullable Object value) {
            return spec.builder.apply(src, value);
        }
    }

    public enum RewardKind implements Kind {

        ITEM(QuestScreenLang.EDIT_REWARD_ITEM, QuestScreenLang.EDIT_REWARD_ITEM_HINT, Input.ITEM, null, (src, v) -> itemArgs(src, (Item) v).wrap("Reward.item(", ", 1)")),
        STACK(QuestScreenLang.EDIT_REWARD_STACK, QuestScreenLang.EDIT_REWARD_STACK_HINT, Input.STACK, null, (src, v) -> stack(src, (ItemStack) v)),
        XP_LEVELS(QuestScreenLang.EDIT_REWARD_XP_LEVELS, QuestScreenLang.EDIT_REWARD_XP_LEVELS_HINT, Input.NONE, null, (src, v) -> Expr.of("Reward.xpLevels(1)")),
        XP(QuestScreenLang.EDIT_REWARD_XP, QuestScreenLang.EDIT_REWARD_XP_HINT, Input.NONE, null, (src, v) -> Expr.of("Reward.xp(10)")),
        COMMAND(QuestScreenLang.EDIT_REWARD_COMMAND, QuestScreenLang.EDIT_REWARD_COMMAND_HINT, Input.TEXT, null, (src, v) -> Expr.of("Reward.command(" + javaString((String) v) + ")")),
        RANDOM(QuestScreenLang.EDIT_REWARD_RANDOM, QuestScreenLang.EDIT_REWARD_RANDOM_HINT, Input.TABLE, QuestKinds::tables, (src, v) -> table(src, (String) v).wrap("Reward.random(", ")")),
        LOOT(QuestScreenLang.EDIT_REWARD_LOOT, QuestScreenLang.EDIT_REWARD_LOOT_HINT, Input.TABLE, QuestKinds::tables, (src, v) -> table(src, (String) v).wrap("Reward.loot(", ")")),
        ALL_OF(QuestScreenLang.EDIT_REWARD_ALL_OF, QuestScreenLang.EDIT_REWARD_ALL_OF_HINT, Input.TABLE, QuestKinds::tables, (src, v) -> table(src, (String) v).wrap("Reward.allOf(", ")")),
        CHOICE(QuestScreenLang.EDIT_REWARD_CHOICE, QuestScreenLang.EDIT_REWARD_CHOICE_HINT, Input.TABLE, QuestKinds::tables, (src, v) -> table(src, (String) v).wrap("Reward.choice(", ")"));

        private final Spec spec;

        RewardKind(QuestLang label, QuestLang hint, Input input, @Nullable Supplier<List<Choice>> choices, BiFunction<QuestSource, Object, Expr> builder) {
            spec = new Spec(label, hint, input, choices, builder);
        }

        @Override
        public QuestLang label() {
            return spec.label;
        }

        @Override
        public QuestLang hint() {
            return spec.hint;
        }

        @Override
        public Input input() {
            return spec.input;
        }

        @Override
        public List<Choice> choices() {
            return spec.list();
        }

        Expr build(QuestSource src, @Nullable Object value) {
            return spec.builder.apply(src, value);
        }
    }

    private static final String RL_UTILS = "com.gtolib.utils.RLUtils";
    private static final Constants TAGS = new Constants("net.minecraft.tags.ItemTags", "net.minecraftforge.common.Tags$Items", "com.gregtechceu.gtceu.data.recipe.CustomTags",
            "appeng.datagen.providers.tags.ConventionTags", "com.gtocore.data.quest.QuestExternalRefs");
    private static final Constants NODES = new Constants("com.gtocore.data.techtree.BaseNodes", "com.gtocore.data.techtree.MachinesNode", "com.gtocore.data.techtree.ComponentNodes",
            "com.gtocore.data.techtree.EnergyNodes", "com.gtocore.data.techtree.SpaceNodes", "com.gtocore.data.techtree.NanitesNodes", "com.gtocore.data.techtree.AENodes");
    private static final Constants DIMENSIONS = new Constants("com.gtolib.api.data.Dimension");
    private static final Constants ENTITIES = new Constants("net.minecraft.world.entity.EntityType", "vazkii.botania.common.entity.BotaniaEntities");
    private static final Constants STATS = new Constants("net.minecraft.stats.Stats");
    private static final Constants STRUCTURES = new Constants("net.minecraft.world.level.levelgen.structure.BuiltinStructures", "com.gtocore.data.quest.QuestExternalRefs");

    private QuestKinds() {}

    private static final class Constants {

        private final String[] holders;
        @Nullable
        private O2OOpenCacheHashMap<Object, Expr> byValue;

        Constants(String... holders) {
            this.holders = holders;
        }

        private O2OOpenCacheHashMap<Object, Expr> index() {
            var map = byValue;
            if (map != null) return map;
            map = new O2OOpenCacheHashMap<>();
            for (var holder : holders) {
                Class<?> cls;
                try {
                    cls = Class.forName(holder);
                } catch (ClassNotFoundException | LinkageError e) {
                    continue;
                }
                int nested = holder.indexOf('$');
                var imported = nested < 0 ? holder : holder.substring(0, nested);
                var prefix = holder.substring(holder.lastIndexOf('.') + 1).replace('$', '.');
                for (var field : cls.getFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) continue;
                    Object value;
                    try {
                        value = field.get(null);
                    } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                        continue;
                    }
                    if (value == null || map.containsKey(value)) continue;
                    map.put(value, Expr.of(prefix + "." + field.getName(), imported));
                }
            }
            byValue = map;
            return map;
        }

        @Nullable
        Expr get(Object value) {
            return index().get(value);
        }

        Expr find(Object value, String what) {
            var found = get(value);
            return found != null ? found : Expr.fail(what + " 无可用 Java 写法，请在源码中手写");
        }
    }

    static String javaString(String text) {
        var sb = new StringBuilder(text.length() + 2).append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"', '\\' -> sb.append('\\').append(c);
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    private static String location(ResourceLocation id) {
        return "RLUtils.fromNamespaceAndPath(" + javaString(id.getNamespace()) + ", " + javaString(id.getPath()) + ")";
    }

    private static Expr noSyntax(Item item) {
        return Expr.fail(BuiltInRegistries.ITEM.getKey(item) + " 无可用 Java 写法，请在源码中手写");
    }

    private static Expr itemArgs(QuestSource src, Item item) {
        var syntax = QuestItemSyntax.of(src, item);
        return syntax == null ? noSyntax(item) : new Expr(syntax.args(), syntax.imports(), "");
    }

    private static Expr itemLike(QuestSource src, Item item) {
        var syntax = QuestItemSyntax.itemLike(src, item);
        return syntax == null ? noSyntax(item) : new Expr(syntax.args(), syntax.imports(), "");
    }

    private static Expr anyOf(QuestSource src, List<?> items) {
        if (items.size() < 2) return Expr.fail("至少选择两种物品");
        var sb = new StringBuilder("Task.anyOf(1");
        var imports = new ObjectArrayList<String>();
        for (var item : items) {
            var one = itemLike(src, (Item) item);
            if (one.text == null) return one;
            sb.append(", ").append(one.text);
            for (var name : one.imports) {
                if (!imports.contains(name)) imports.add(name);
            }
        }
        return new Expr(sb.append(')').toString(), imports, "");
    }

    private static Expr stack(QuestSource src, ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null || tag.isEmpty()) return Expr.fail("该物品不带数据，请用物品奖励");
        return itemLike(src, stack.getItem()).wrap("Reward.stack(() -> QuestStacks.withNbt(new ItemStack(", "), " + javaString(tag.toString()) + "), " + stack.getCount() + ")",
                ItemStack.class.getName(), "com.gtolib.api.quest.reward.QuestStacks");
    }

    private static Expr tag(TagKey<?> key) {
        var constant = TAGS.get(key);
        if (constant != null) return constant;
        return Expr.of("TagKey.create(Registries.ITEM, " + location(key.location()) + ")", TagKey.class.getName(), Registries.class.getName(), RL_UTILS);
    }

    private static Expr entity(EntityType<?> type) {
        var constant = ENTITIES.get(type);
        if (constant != null) return constant;
        return Expr.of("ForgeRegistries.ENTITY_TYPES.getValue(" + location(EntityType.getKey(type)) + ")", "net.minecraftforge.registries.ForgeRegistries", RL_UTILS);
    }

    private static Expr stat(ResourceLocation id) {
        var constant = STATS.get(id);
        return constant != null ? constant : Expr.of("BuiltInRegistries.CUSTOM_STAT.get(" + location(id) + ")", BuiltInRegistries.class.getName(), RL_UTILS);
    }

    private static Expr structure(ResourceKey<?> key) {
        var constant = STRUCTURES.get(key);
        if (constant != null) return constant;
        return Expr.of("ResourceKey.create(Registries.STRUCTURE, " + location(key.location()) + ")", ResourceKey.class.getName(), Registries.class.getName(), RL_UTILS);
    }

    private static List<Choice> tags() {
        var keys = new ObjectArrayList<TagKey<Item>>();
        BuiltInRegistries.ITEM.getTagNames().forEach(keys::add);
        keys.sort((a, b) -> a.location().compareTo(b.location()));
        var out = new ObjectArrayList<Choice>(keys.size());
        for (var key : keys) out.add(new Choice(Component.literal("#" + key.location()), "", () -> QuestIcon.tag(key), key));
        return out;
    }

    private static List<Choice> techNodes() {
        var out = new ObjectArrayList<Choice>();
        for (var manager : TechTreeManager.managersById()) {
            var tree = TechTreeManager.getTreeName(manager).getString();
            for (var node : manager.getAllNodes()) {
                out.add(new Choice(TechTreeManager.getNodeName(node), tree + " · " + node.name, () -> switch (node.icon) {
                    case AEItemKey item -> QuestIcon.of(item.toStack());
                    case AEFluidKey fluid -> QuestIcon.fluid(fluid.getFluid());
                    case null, default -> QuestIcon.EMPTY;
                }, node));
            }
        }
        return out;
    }

    private static List<Choice> dimensions() {
        var out = new ObjectArrayList<Choice>();
        for (var dimension : Dimension.all()) {
            out.add(new Choice(Component.translatable(dimension.getKey()), dimension.getResourceKey().location().toString(), () -> {
                var stack = dimension.getItemKey() == null ? ItemStack.EMPTY : BuiltInRegistries.ITEM.get(dimension.getItemKey()).getDefaultInstance();
                return stack.isEmpty() ? QuestIcon.EMPTY : QuestIcon.of(stack);
            }, dimension));
        }
        return out;
    }

    private static List<Choice> entities() {
        var out = new ObjectArrayList<Choice>();
        for (var type : BuiltInRegistries.ENTITY_TYPE) {
            if (!type.canSummon()) continue;
            out.add(new Choice(type.getDescription(), EntityType.getKey(type).toString(), () -> {
                var egg = ForgeSpawnEggItem.fromEntityType(type);
                return egg == null ? QuestIcon.EMPTY : QuestIcon.of(egg.getDefaultInstance());
            }, type));
        }
        out.sort((a, b) -> a.detail.compareTo(b.detail));
        return out;
    }

    private static List<Choice> stats() {
        var out = new ObjectArrayList<Choice>();
        for (var id : BuiltInRegistries.CUSTOM_STAT.keySet()) {
            if (id.equals(BuiltInRegistries.CUSTOM_STAT.get(id))) out.add(new Choice(Component.translatable(Util.makeDescriptionId("stat", id)), id.toString(), null, id));
        }
        out.sort((a, b) -> a.detail.compareTo(b.detail));
        return out;
    }

    private static List<Choice> structures() {
        var out = new ObjectArrayList<Choice>();
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return out;
        for (var key : server.registryAccess().registryOrThrow(Registries.STRUCTURE).registryKeySet()) out.add(new Choice(Component.literal(key.location().toString()), "", null, key));
        out.sort((a, b) -> a.label.getString().compareTo(b.label.getString()));
        return out;
    }

    private static List<Choice> difficulties() {
        var out = new ObjectArrayList<Choice>();
        for (var tier : RulePreset.TIERS) out.add(new Choice(RulePreset.tagOfLevel(tier.level()), "", null, tier.level()));
        return out;
    }

    private static List<Choice> mods() {
        var out = new ObjectArrayList<Choice>();
        for (var mod : Mods.values()) out.add(new Choice(Component.literal(mod.name().toLowerCase(Locale.ROOT)), mod.isLoaded() ? "已安装" : "未安装", null, mod));
        return out;
    }

    private static Expr table(QuestSource src, String id) {
        var field = src.find("t:" + id) instanceof QuestSource.TableDecl decl ? decl.field : id.toUpperCase(Locale.ROOT);
        return Expr.of(QuestSource.TABLES_CLASS + "." + field, "com.gtocore.data.quest." + QuestSource.TABLES_CLASS);
    }

    private static List<Choice> tables() {
        var out = new ObjectArrayList<Choice>();
        var file = QuestHot.source().tableFile();
        if (file == null) return out;
        for (var decl : file.tables) {
            var live = QuestHot.liveTable(decl.tableId);
            out.add(new Choice(live == null ? Component.literal(decl.tableId) : live.title(), live == null ? decl.tableId : decl.tableId + " · " + live.entries().size() + " 项",
                    live == null ? null : live::icon, decl.tableId));
        }
        return out;
    }

    static RewardKind tableKind(TableReward reward) {
        return reward.isLoot() ? RewardKind.LOOT : reward.isAllOf() ? RewardKind.ALL_OF : reward.isChoice() ? RewardKind.CHOICE : RewardKind.RANDOM;
    }

    public static boolean isBlock(Item item) {
        return Block.byItem(item) != Blocks.AIR;
    }
}
