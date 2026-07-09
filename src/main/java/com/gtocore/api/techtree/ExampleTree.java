package com.gtocore.api.techtree;

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

    public static final TechNode<Void> WOOD = MANAGER.builder(
            "wood",
            "木头",
            "Wood")
            .icon(AEItemKey.of(Blocks.OAK_LOG))
            .description("示例根节点", "Example root node")
            .build();

    public static final TechNode<Void> COBBLESTONE = MANAGER.builder(
            "cobblestone",
            "圆石",
            "Cobblestone")
            .icon(AEItemKey.of(Blocks.COBBLESTONE))
            .prerequisites(WOOD)
            .build();

    public static final TechNode<Void> IRON = MANAGER.builder(
            "iron",
            "铁",
            "Iron")
            .icon(AEItemKey.of(Items.IRON_INGOT))
            .description("示例多分支铁节点", "Example iron node with multiple branches")
            .prerequisites(COBBLESTONE)
            .build();

    public static final TechNode<Void> GOLD = MANAGER.builder(
            "gold",
            "金",
            "Gold")
            .icon(AEItemKey.of(Items.GOLD_INGOT))
            .description("示例金节点", "Example gold node")
            .prerequisites(IRON)
            .build();

    public static final TechNode<Void> DIAMOND = MANAGER.builder(
            "diamond",
            "钻石",
            "Diamond")
            .icon(AEItemKey.of(Items.DIAMOND))
            .description("示例钻石节点", "Example diamond node")
            .prerequisites(IRON)
            .build();

    public static final TechNode<Void> NETHERITE = MANAGER.builder(
            "netherite",
            "下界合金",
            "Netherite")
            .icon(AEItemKey.of(Items.NETHERITE_INGOT))
            .description("示例多依赖下界合金节点", "Example netherite node with multiple dependencies")
            .prerequisites(GOLD, DIAMOND)
            .build();

    private ExampleTree() {}

    public static void init() {}
}
