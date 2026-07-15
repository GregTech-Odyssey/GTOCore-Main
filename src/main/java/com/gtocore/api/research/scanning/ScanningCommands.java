package com.gtocore.api.research.scanning;

import com.gtocore.api.research.TeamResearchSavedDtat;

import com.gtolib.utils.AEChemicalHelper;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import com.hepdd.gtmthings.utils.TeamUtil;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import static com.gregtechceu.gtceu.common.data.GTMaterials.NULL;

public class ScanningCommands {

    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(
            Component.translatable("argument.id.invalid"));

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("scan")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add_hand").executes(ScanningCommands::addHandHeldItem))
                .then(Commands.literal("remove_hand").executes(ScanningCommands::removeHandHeldItem))
                .then(Commands.literal("get_self").executes(ScanningCommands::getSelfScannedItems));
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

        var teamContext = TeamResearchSavedDtat.getOrCreateContext(TeamUtil.getTeamUUID(player.getUUID()));

        if (mat != NULL) {
            teamContext.getScannedMaterials().add(mat);
        }
        teamContext.getScannedItems().add(key);
        return 1;
    }

    private static int removeHandHeldItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var key = getHandHeldAEKey(player);
        var mat = AEChemicalHelper.getMaterial(key);

        var teamContext = TeamResearchSavedDtat.getOrCreateContext(TeamUtil.getTeamUUID(player.getUUID()));

        if (mat != NULL) {
            teamContext.getScannedMaterials().remove(mat);
        }
        teamContext.getScannedItems().remove(key);
        return 1;
    }

    private static int getSelfScannedItems(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var teamContext = TeamResearchSavedDtat.getOrCreateContext(TeamUtil.getTeamUUID(player.getUUID()));
        var scannedItems = teamContext.getScannedItems();
        var scannedMaterials = teamContext.getScannedMaterials();
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
