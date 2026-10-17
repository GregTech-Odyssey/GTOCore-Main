package com.gtocore.api.research;

import com.gtolib.utils.AEChemicalHelper;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import com.hepdd.gtmthings.utils.TeamUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import static com.gregtechceu.gtceu.common.data.GTMaterials.NULL;

public final class ResarchTagCommands {

    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(
            Component.translatable("argument.id.invalid"));
    private static final DynamicCommandExceptionType RESEARCH_TAG_NOT_FOUND = new DynamicCommandExceptionType(
            name -> Component.literal("Unknown research tag: " + name));
    private static final SimpleCommandExceptionType POINTS_OVERFLOW = new SimpleCommandExceptionType(
            Component.literal("Research points exceed the maximum supported value"));
    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(
            context.getSource().getOnlinePlayerNames().stream(), builder);
    private static final SuggestionProvider<CommandSourceStack> RESEARCH_TAG_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(
            ResearchTag.TAGS.keys().stream().sorted(), builder);

    private ResarchTagCommands() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("researchtag")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                        .then(researchTagArguments(ResarchTagCommands::addResearchPoints)))
                .then(Commands.literal("remove")
                        .then(researchTagArguments(ResarchTagCommands::removeResearchPoints)));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> researchTagArguments(Command<CommandSourceStack> command) {
        return Commands.argument("player", EntityArgument.player())
                .suggests(PLAYER_SUGGESTIONS)
                .then(Commands.argument("research_tag", StringArgumentType.word())
                        .suggests(RESEARCH_TAG_SUGGESTIONS)
                        .then(Commands.argument("points", LongArgumentType.longArg(1L))
                                .executes(command)));
    }

    private static int addResearchPoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResearchTag tag = getResearchTag(context);
        long points = LongArgumentType.getLong(context, "points");
        var researchPoints = TeamResearchSavedData.getOrCreateContext(player).researchPoints();
        long updated;
        try {
            updated = Math.addExact(researchPoints.getLong(tag), points);
        } catch (ArithmeticException ignored) {
            throw POINTS_OVERFLOW.create();
        }
        researchPoints.put(tag, updated);
        TeamResearchSavedData.INSTANCE.setDirty(true);
        context.getSource().sendSuccess(() -> Component.literal("Added " + points + " " + tag.getName() + " research points to " + player.getGameProfile().getName()), true);
        return 1;
    }

    private static int removeResearchPoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResearchTag tag = getResearchTag(context);
        long requested = LongArgumentType.getLong(context, "points");
        var researchPoints = TeamResearchSavedData.getOrCreateContext(player).researchPoints();
        long current = researchPoints.getLong(tag);
        long removed = Math.min(current, requested);
        if (removed <= 0) {
            context.getSource().sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " has no " + tag.getName() + " research points"), false);
            return 0;
        }
        long remaining = current - removed;
        if (remaining == 0) {
            researchPoints.removeLong(tag);
        } else {
            researchPoints.put(tag, remaining);
        }
        TeamResearchSavedData.INSTANCE.setDirty(true);
        context.getSource().sendSuccess(() -> Component.literal("Removed " + removed + " " + tag.getName() + " research points from " + player.getGameProfile().getName()), true);
        return 1;
    }

    private static ResearchTag getResearchTag(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "research_tag");
        ResearchTag tag = ResearchTag.TAGS.get(name);
        if (tag == null) {
            throw RESEARCH_TAG_NOT_FOUND.create(name);
        }
        return tag;
    }

    public static ArgumentBuilder<CommandSourceStack, ?> registerScan() {
        return Commands.literal("scan")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add_hand").executes(ResarchTagCommands::addHandHeldItem))
                .then(Commands.literal("remove_hand").executes(ResarchTagCommands::removeHandHeldItem))
                .then(Commands.literal("get_self").executes(ResarchTagCommands::getSelfScannedItems));
    }

    private static AEKey getHandHeldAEKey(Player player) throws CommandSyntaxException {
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw ERROR_INVALID.create();
        }
        GenericStack contained = ContainerItemStrategies.getContainedStack(stack);
        return contained == null ? AEItemKey.of(stack) : contained.what();
    }

    private static int addHandHeldItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var key = getHandHeldAEKey(player);
        var mat = AEChemicalHelper.getMaterial(key);

        var teamContext = TeamResearchSavedData.getOrCreateContext(TeamUtil.getTeamUUID(player.getUUID()));

        if (mat != NULL) {
            teamContext.scannedMaterials().add(mat);
        }
        teamContext.scannedItems().add(key);
        TeamResearchSavedData.INSTANCE.setDirty(true);
        return 1;
    }

    private static int removeHandHeldItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var key = getHandHeldAEKey(player);
        var mat = AEChemicalHelper.getMaterial(key);

        var teamContext = TeamResearchSavedData.getOrCreateContext(TeamUtil.getTeamUUID(player.getUUID()));

        if (mat != NULL) {
            teamContext.scannedMaterials().remove(mat);
        }
        teamContext.scannedItems().remove(key);
        TeamResearchSavedData.INSTANCE.setDirty(true);
        return 1;
    }

    private static int getSelfScannedItems(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var teamContext = TeamResearchSavedData.getOrCreateContext(TeamUtil.getTeamUUID(player.getUUID()));
        var scannedItems = teamContext.scannedItems();
        var scannedMaterials = teamContext.scannedMaterials();
        player.sendSystemMessage(Component.literal("Scanned Items: " + scannedItems.size()));
        for (var item : scannedItems) {
            player.sendSystemMessage(Component.literal(" - " + item.toString()));
        }
        player.sendSystemMessage(Component.literal("Scanned Materials: " + scannedMaterials.size()));
        for (var mat : scannedMaterials) {
            player.sendSystemMessage(Component.literal(" - " + mat.toString()));
        }
        return 1;
    }
}
