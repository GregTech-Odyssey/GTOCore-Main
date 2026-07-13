package com.gtocore.api.techtree;

import com.gtocore.api.techtree.ui.TechTreeLayout;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.gtocore.api.techtree.TechTreeManager.getNodeName;

public final class TechTreeCommands {

    private static final DynamicCommandExceptionType MANAGER_NOT_FOUND = new DynamicCommandExceptionType(id -> Component.literal("Unknown tech tree manager: " + id));
    private static final Dynamic2CommandExceptionType NODE_NOT_FOUND = new Dynamic2CommandExceptionType((node, manager) -> Component.literal("Unknown tech node " + node + " in manager " + manager));
    private static final SuggestionProvider<CommandSourceStack> MANAGER_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(
            TechTreeManager.getManagers().stream().map(TechTreeManager::getId).sorted(),
            builder);
    private static final SuggestionProvider<CommandSourceStack> NODE_SUGGESTIONS = (context, builder) -> {
        TechTreeManager manager = findManager(StringArgumentType.getString(context, "manager"));
        if (manager == null) {
            return builder.buildFuture();
        }
        return SharedSuggestionProvider.suggest(manager.definitions.keySet().stream().sorted(), builder);
    };

    private TechTreeCommands() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("techtree")
                .requires(source -> source.hasPermission(2))
                .then(unlock())
                .then(reset())
                .then(list());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> unlock() {
        return Commands.literal("unlock")
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("manager", StringArgumentType.word())
                                .suggests(MANAGER_SUGGESTIONS)
                                .then(Commands.argument("node", StringArgumentType.word())
                                        .suggests(NODE_SUGGESTIONS)
                                        .executes(TechTreeCommands::executeUnlock))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> reset() {
        return Commands.literal("reset")
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("manager", StringArgumentType.word())
                                .suggests(MANAGER_SUGGESTIONS)
                                .executes(TechTreeCommands::executeReset)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> list() {
        return Commands.literal("list")
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("manager", StringArgumentType.word())
                                .suggests(MANAGER_SUGGESTIONS)
                                .executes(TechTreeCommands::executeList)));
    }

    private static int executeUnlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
        TechTreeManager manager = getManager(context);
        TechNode node = getNode(context, manager);
        int changed = 0;
        for (var target : collectTargets(players)) {
            boolean unlocked = forceUnlock(target.teamUUID(), node);
            if (unlocked) {
                changed++;
            }
            String prefix = unlocked ? "Unlocked " : "Already unlocked ";
            context.getSource().sendSuccess(() -> Component.literal(prefix)
                    .append(getNodeName(node))
                    .append(Component.literal(" in manager " + manager.getId() + " for " + target.label() + " [" + target.teamUUID() + "]")), false);
        }
        return changed;
    }

    private static int executeReset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
        TechTreeManager manager = getManager(context);
        int changed = 0;
        for (var target : collectTargets(players)) {
            boolean reset = TechTreeSavedData.reset(target.teamUUID(), manager);
            if (reset) {
                changed++;
            }
            String message = reset ? "Reset manager " + manager.getId() : "No unlock data to reset for manager " + manager.getId();
            context.getSource().sendSuccess(() -> Component.literal(message + " for " + target.label() + " [" + target.teamUUID() + "]"), false);
        }
        return changed;
    }

    private static int executeList(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
        TechTreeManager manager = getManager(context);
        TechTreeLayout layout = manager.getLayout();
        Map<TechNode, Integer> nodeOrder = createNodeOrder(layout);
        int totalUnlocked = 0;
        for (var target : collectTargets(players)) {
            TechTree tree = TechTreeSavedData.findTree(target.teamUUID(), manager);
            List<TechNode> unlockedNodes = new ArrayList<>();
            if (tree != null) {
                tree.getUnlockedNodes().stream()
                        .sorted(java.util.Comparator.comparingInt(node -> nodeOrder.getOrDefault(node, Integer.MAX_VALUE)))
                        .forEach(unlockedNodes::add);
            }
            totalUnlocked += unlockedNodes.size();
            context.getSource().sendSuccess(() -> Component.literal("Unlocked nodes in manager " + manager.getId() + " for " + target.label() + " [" + target.teamUUID() + "]: " + unlockedNodes.size()), false);
            if (unlockedNodes.isEmpty()) {
                context.getSource().sendSuccess(() -> Component.literal(" - none"), false);
                continue;
            }
            for (var node : unlockedNodes) {
                context.getSource().sendSuccess(() -> Component.literal(" - ")
                        .append(getNodeName(node))
                        .append(Component.literal(" (" + node.name + ", layer=" + layout.getPlacement(node).layer() + ", row=" + layout.getPlacement(node).row() + ")")), false);
            }
        }
        return totalUnlocked;
    }

    private static TechTreeManager getManager(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String id = StringArgumentType.getString(context, "manager");
        TechTreeManager manager = findManager(id);
        if (manager == null) {
            throw MANAGER_NOT_FOUND.create(id);
        }
        return manager;
    }

    private static TechNode getNode(CommandContext<CommandSourceStack> context, TechTreeManager manager) throws CommandSyntaxException {
        String nodeId = StringArgumentType.getString(context, "node");
        TechNode node = manager.definitions.get(nodeId);
        if (node == null) {
            throw NODE_NOT_FOUND.create(nodeId, manager.getId());
        }
        return node;
    }

    private static TechTreeManager findManager(String id) {
        return TechTreeManager.getManager(id);
    }

    private static List<TeamTarget> collectTargets(Collection<ServerPlayer> players) {
        Map<UUID, List<ServerPlayer>> grouped = new LinkedHashMap<>();
        for (var player : players) {
            grouped.computeIfAbsent(TechTreeSavedData.getTeamUUID(player), ignored -> new ArrayList<>()).add(player);
        }
        List<TeamTarget> result = new ArrayList<>(grouped.size());
        for (var entry : grouped.entrySet()) {
            String names = entry.getValue().stream()
                    .map(player -> player.getGameProfile().getName())
                    .distinct()
                    .collect(Collectors.joining(", "));
            result.add(new TeamTarget(entry.getKey(), names));
        }
        return result;
    }

    private static boolean forceUnlock(UUID teamUUID, TechNode node) {
        return TechTreeSavedData.forceUnlock(teamUUID, node);
    }

    private static Map<TechNode, Integer> createNodeOrder(TechTreeLayout layout) {
        Map<TechNode, Integer> order = new IdentityHashMap<>();
        int index = 0;
        for (TechNode node : layout.orderedNodes()) {
            order.put(node, index++);
        }
        return order;
    }

    private record TeamTarget(UUID teamUUID, String label) {}
}
