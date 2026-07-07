package com.gtocore.api.techtree;

import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import appeng.api.stacks.AEItemKey;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;

public final class ExampleTree {

    public static final TechTreeManager<Void> MANAGER = new TechTreeManager<>(
            "example_tree",
            "test树",
            "Example Tree",
            new ItemStackTexture(Blocks.DIRT.asItem()));

    public static final TechNode<Void> DIRT = MANAGER.builder(
            "dirt",
            "泥土",
            "Dirt",
            "Example root node")
            .icon(AEItemKey.of(Blocks.DIRT))
            .requirements(ignored -> ActionResult.SUCCESS)
            .build();

    public static final TechNode<Void> COBBLESTONE = MANAGER.builder(
            "cobblestone",
            "圆石",
            "Cobblestone",
            "Unlocked after dirt")
            .icon(AEItemKey.of(Blocks.COBBLESTONE))
            .requirements(ignored -> ActionResult.SUCCESS)
            .prerequisites(DIRT)
            .build();

    public static final TechNode<Void> IRON = MANAGER.builder(
            "iron",
            "铁",
            "Iron",
            "Unlocked after cobblestone")
            .icon(AEItemKey.of(Items.IRON_INGOT))
            .requirements(ignored -> ActionResult.SUCCESS)
            .prerequisites(COBBLESTONE)
            .build();

    public static final TechNode<Void> GOLD = MANAGER.builder(
            "gold",
            "金",
            "Gold",
            "Unlocked after iron")
            .icon(AEItemKey.of(Items.GOLD_INGOT))
            .requirements(ignored -> ActionResult.SUCCESS)
            .prerequisites(IRON)
            .build();

    public static final TechNode<Void> DIAMOND = MANAGER.builder(
            "diamond",
            "钻石",
            "Diamond",
            "Unlocked after iron")
            .icon(AEItemKey.of(Items.DIAMOND))
            .requirements(ignored -> ActionResult.SUCCESS)
            .prerequisites(IRON)
            .build();

    public static final TechNode<Void> NETHERITE = MANAGER.builder(
            "netherite",
            "下界合金",
            "Netherite",
            "Unlocked after gold and diamond")
            .icon(AEItemKey.of(Items.NETHERITE_INGOT))
            .requirements(ignored -> ActionResult.SUCCESS)
            .prerequisites(GOLD, DIAMOND)
            .build();

    private ExampleTree() {}

    public static void init() {}
}
