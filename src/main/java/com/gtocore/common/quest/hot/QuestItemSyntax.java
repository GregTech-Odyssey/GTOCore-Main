package com.gtocore.common.quest.hot;

import com.gtolib.GTOCore;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestBook;
import com.gtolib.api.quest.task.ItemTask;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreeScanner;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Supplier;

final class QuestItemSyntax {

    record Syntax(String args, List<String> imports) {}

    private static final String[] HOLDERS = {
            "net.minecraft.world.item.Items",
            "net.minecraft.world.level.block.Blocks",
            "com.gregtechceu.gtceu.common.data.GTItems",
            "com.gregtechceu.gtceu.common.data.GTBlocks",
            "com.gregtechceu.gtceu.common.data.GTMachines",
            "com.gregtechceu.gtceu.common.data.machines.GTMultiMachines",
            "com.gtocore.common.data.GTOItems",
            "com.gtocore.common.data.GTOBlocks",
            "com.gtocore.common.data.GTOMachines",
            "com.gtocore.common.data.machines.MultiBlockA",
            "com.gtocore.common.data.machines.MultiBlockB",
            "com.gtocore.common.data.machines.MultiBlockC",
            "com.gtocore.common.data.machines.MultiBlockD",
            "com.gtocore.common.data.machines.MultiBlockE",
            "com.gtocore.common.data.machines.MultiBlockF",
            "com.gtocore.common.data.machines.MultiBlockG",
            "com.gtocore.common.data.machines.MultiBlockH",
            "com.gtocore.common.data.machines.MultiBlockI",
            "com.gtocore.common.data.machines.ManaMachine",
            "com.gtocore.common.data.machines.ManaMultiBlock",
            "com.gtocore.common.data.machines.GeneratorMultiblock",
            "com.gtocore.common.data.machines.GCYMMachines",
            "com.gtocore.common.data.machines.GTAEMachines",
            "com.gtocore.common.data.machines.ExResearchMachines",
            "com.gtocore.common.data.machines.SpaceMultiblock",
            "com.gtocore.common.data.machines.StationModules",
            "com.gtocore.common.data.machines.Towers",
            "com.gtocore.common.data.machines.OptionalMachine",
    };
    private static final String[] MATERIAL_HOLDERS = {
            "com.gregtechceu.gtceu.common.data.GTMaterials",
            "com.gtocore.common.data.GTOMaterials",
    };
    private static final String[] PREFIX_HOLDERS = {
            "com.gregtechceu.gtceu.api.data.tag.TagPrefix",
            "com.gtocore.api.data.tag.GTOTagPrefix",
    };
    private static final String[] TIER_NAMES = { "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "UHV", "UEV", "UIV", "UXV", "OpV", "MAX" };

    @Nullable
    private static Reference2ObjectOpenHashMap<Item, Syntax> reflected;

    private QuestItemSyntax() {}

    @Nullable
    static Syntax of(QuestSource source, Item item) {
        if (item == Items.AIR) return null;
        var fromSource = fromSource(source, item);
        if (fromSource != null) return fromSource;
        var map = reflected();
        var found = map.get(item);
        if (found != null) return found;
        return fromMaterial(item);
    }

    @Nullable
    private static Syntax fromSource(QuestSource source, Item item) {
        for (var file : source.files) {
            for (var decl : file.quests) {
                var quest = QuestBook.byKey(decl.key()) instanceof Quest q ? q : null;
                if (quest == null) continue;
                var args = new ObjectArrayList<com.sun.source.tree.ExpressionTree>();
                for (var call : decl.calls("task")) args.addAll(call.getArguments());
                var tasks = quest.tasks();
                if (args.size() != tasks.size()) continue;
                for (int k = 0; k < args.size(); k++) {
                    if (!(tasks.get(k) instanceof ItemTask task) || task.isTag() || task.item() != item) continue;
                    var call = taskRoot(args.get(k));
                    if (call == null) continue;
                    var itemArgs = itemArgs(call);
                    if (itemArgs == null) continue;
                    var start = file.start(itemArgs.get(0));
                    var end = file.end(itemArgs.get(itemArgs.size() - 1));
                    var imports = new ObjectArrayList<String>();
                    for (var arg : itemArgs) collectImports(file, arg, imports);
                    return new Syntax(file.content.substring(start, end), imports);
                }
            }
        }
        return null;
    }

    @Nullable
    static MethodInvocationTree taskRoot(com.sun.source.tree.ExpressionTree arg) {
        var e = arg;
        while (e instanceof MethodInvocationTree call) {
            if (isTaskItem(call)) return call;
            if (!(call.getMethodSelect() instanceof MemberSelectTree select)) return null;
            e = select.getExpression();
        }
        return null;
    }

    static boolean isTaskItem(MethodInvocationTree call) {
        return call.getMethodSelect() instanceof MemberSelectTree select && select.getIdentifier().contentEquals("item") &&
                select.getExpression() instanceof IdentifierTree owner && owner.getName().contentEquals("Task");
    }

    @Nullable
    static List<? extends com.sun.source.tree.ExpressionTree> itemArgs(MethodInvocationTree call) {
        var args = call.getArguments();
        int n = args.size();
        if (n == 0 || n > 3) return null;
        boolean counted = QuestSource.number(args.get(n - 1)) != null;
        int itemCount = counted ? n - 1 : n;
        return itemCount == 1 || itemCount == 2 ? args.subList(0, itemCount) : null;
    }

    private static void collectImports(QuestSource.FileModel file, com.sun.source.tree.Tree tree, ObjectArrayList<String> out) {
        new TreeScanner<Void, Void>() {

            @Override
            public Void visitIdentifier(IdentifierTree node, Void unused) {
                var name = node.getName().toString();
                for (var imp : file.imports) {
                    if (imp.endsWith("." + name) && !out.contains(imp)) out.add(imp);
                }
                return null;
            }
        }.scan(tree, null);
    }

    private static Reference2ObjectOpenHashMap<Item, Syntax> reflected() {
        var map = reflected;
        if (map != null) return map;
        map = new Reference2ObjectOpenHashMap<>();
        var gtValues = GTValues.class.getName();
        for (var holder : HOLDERS) {
            Class<?> cls;
            try {
                cls = Class.forName(holder);
            } catch (ClassNotFoundException | LinkageError e) {
                continue;
            }
            var simple = cls.getSimpleName();
            for (Field field : cls.getFields()) {
                if (!Modifier.isStatic(field.getModifiers())) continue;
                Object value;
                try {
                    value = field.get(null);
                } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                    continue;
                }
                if (value instanceof Object[] array) {
                    for (int i = 0; i < array.length && i < TIER_NAMES.length; i++) {
                        var item = itemOf(array[i]);
                        if (item != null) map.putIfAbsent(item, new Syntax(simple + "." + field.getName() + "[GTValues." + TIER_NAMES[i] + "]", List.of(holder, gtValues)));
                    }
                } else {
                    var item = itemOf(value);
                    if (item != null) map.putIfAbsent(item, new Syntax(simple + "." + field.getName(), List.of(holder)));
                }
            }
        }
        GTOCore.LOGGER.info("[任务热修改] 物品写法对照表：{} 种", map.size());
        reflected = map;
        return map;
    }

    @Nullable
    private static Item itemOf(@Nullable Object value) {
        try {
            if (value instanceof ItemLike like) {
                var item = like.asItem();
                return item == Items.AIR ? null : item;
            }
            if (value instanceof Supplier<?> supplier && supplier.get() instanceof ItemLike like) {
                var item = like.asItem();
                return item == Items.AIR ? null : item;
            }
        } catch (RuntimeException e) {
            return null;
        }
        return null;
    }

    @Nullable
    private static Syntax fromMaterial(Item item) {
        var entry = ChemicalHelper.getMaterialEntry(item);
        if (entry == null || entry.isEmpty()) return null;
        var prefix = staticField(PREFIX_HOLDERS, TagPrefix.class, entry.tagPrefix());
        var material = staticField(MATERIAL_HOLDERS, Material.class, entry.material());
        if (prefix == null || material == null) return null;
        return new Syntax(prefix[1] + ", " + material[1], List.of(prefix[0], material[0]));
    }

    @Nullable
    private static String[] staticField(String[] holders, Class<?> type, Object value) {
        for (var holder : holders) {
            try {
                var cls = Class.forName(holder);
                for (Field field : cls.getFields()) {
                    if (Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType()) && field.get(null) == value) {
                        return new String[] { holder, cls.getSimpleName() + "." + field.getName() };
                    }
                }
            } catch (ReflectiveOperationException | LinkageError e) {
                continue;
            }
        }
        return null;
    }
}
