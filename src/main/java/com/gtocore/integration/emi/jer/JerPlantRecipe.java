package com.gtocore.integration.emi.jer;

import com.gtolib.GTOCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// 改写自 Just Enough Resources（way2muchnoise，"Don't Be a Jerk" 非商业许可）
// https://github.com/way2muchnoise/JustEnoughResources
public final class JerPlantRecipe implements EmiRecipe {

    private static final int WIDTH = 160;
    private static final int COLUMNS = 8;
    private static final int GRID_Y = 56;
    private static final long CYCLE_MS = 500;

    private final ResourceLocation id;
    private final Block plant;
    private final Block soil;
    private final EmiStack input;
    private final List<PlantDrop> drops;
    private final List<EmiStack> outputs;
    private @Nullable BlockState state;
    private @Nullable Property<?> age;
    private long nextCycle;

    private JerPlantRecipe(Block plant, Block soil, PlantDrop... drops) {
        this.id = GTOCore.id("/jer/plant/" + BuiltInRegistries.BLOCK.getKey(plant).getPath());
        this.plant = plant;
        this.soil = soil;
        this.input = EmiStack.of(plant.asItem());
        List<PlantDrop> list = new ArrayList<>(drops.length);
        List<EmiStack> out = new ArrayList<>(drops.length);
        for (PlantDrop drop : drops) {
            list.add(drop);
            out.add(EmiStack.of(drop.stack));
        }
        this.drops = list;
        this.outputs = out;
    }

    static List<JerPlantRecipe> create() {
        List<JerPlantRecipe> recipes = new ArrayList<>(8);
        recipes.add(new JerPlantRecipe(Blocks.POTATOES, Blocks.FARMLAND,
                PlantDrop.range(new ItemStack(Items.POTATO), 1, 4), PlantDrop.chance(new ItemStack(Items.POISONOUS_POTATO), 0.02F)));
        recipes.add(new JerPlantRecipe(Blocks.CARROTS, Blocks.FARMLAND, PlantDrop.range(new ItemStack(Items.CARROT), 1, 4)));
        recipes.add(new JerPlantRecipe(Blocks.WHEAT, Blocks.FARMLAND,
                PlantDrop.range(new ItemStack(Items.WHEAT), 1, 1), PlantDrop.range(new ItemStack(Items.WHEAT_SEEDS), 0, 3)));
        recipes.add(new JerPlantRecipe(Blocks.MELON_STEM, Blocks.FARMLAND, PlantDrop.range(new ItemStack(Items.MELON_SLICE), 3, 7)));
        recipes.add(new JerPlantRecipe(Blocks.PUMPKIN_STEM, Blocks.FARMLAND, PlantDrop.range(new ItemStack(Blocks.PUMPKIN), 1, 1)));
        recipes.add(new JerPlantRecipe(Blocks.BEETROOTS, Blocks.FARMLAND,
                PlantDrop.range(new ItemStack(Items.BEETROOT), 1, 1), PlantDrop.range(new ItemStack(Items.BEETROOT_SEEDS), 0, 3)));
        recipes.add(new JerPlantRecipe(Blocks.NETHER_WART, Blocks.SOUL_SAND, PlantDrop.range(new ItemStack(Items.NETHER_WART), 2, 4)));
        recipes.add(new JerPlantRecipe(Blocks.SWEET_BERRY_BUSH, Blocks.GRASS_BLOCK, PlantDrop.range(new ItemStack(Items.SWEET_BERRIES), 1, 3)));
        return recipes;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return JerEmi.PLANT;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return Collections.singletonList(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return GRID_Y + 5 + (drops.size() + COLUMNS - 1) / COLUMNS * 20;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addDrawable(0, 0, 60, 50, (g, mouseX, mouseY, delta) -> {
            renderBlock(g, soil.defaultBlockState(), 30, 30, -10);
            renderBlock(g, plantState(), 30, 12, 10);
        });
        widgets.addSlot(input, 80, 11);
        for (int i = 0; i < drops.size(); i++) {
            widgets.addSlot(outputs.get(i), 7 + (i % COLUMNS) * 20, GRID_Y + 5 + (i / COLUMNS) * 20)
                    .appendTooltip(Component.literal(drops.get(i).text())).recipeContext(this);
        }
    }

    private BlockState plantState() {
        if (state == null) {
            state = plant.defaultBlockState();
            for (Property<?> property : state.getProperties()) {
                if ("age".equals(property.getName())) age = property;
            }
            nextCycle = System.currentTimeMillis() + CYCLE_MS;
        } else if (age != null && System.currentTimeMillis() > nextCycle) {
            state = state.cycle(age);
            nextCycle = System.currentTimeMillis() + CYCLE_MS;
        }
        return state;
    }

    private static void renderBlock(GuiGraphics g, BlockState block, float x, float y, float z) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, z);
        pose.scale(-20, -20, -20);
        pose.translate(-0.5F, -0.5F, 0);
        pose.mulPose(Axis.XP.rotationDegrees(-30F));
        pose.translate(0.5F, 0, -0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(20F));
        pose.translate(-0.5F, 0, 0.5F);
        pose.translate(0, 0, -1);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        mc.getBlockRenderer().renderSingleBlock(block, pose, buffers, 15728880, OverlayTexture.NO_OVERLAY);
        buffers.endBatch();
        pose.popPose();
    }

    private record PlantDrop(ItemStack stack, int min, int max, float chance) {

        static PlantDrop range(ItemStack stack, int min, int max) {
            return new PlantDrop(stack, min, max, Float.NaN);
        }

        static PlantDrop chance(ItemStack stack, float chance) {
            return new PlantDrop(stack, 0, 1, chance);
        }

        String text() {
            if (!Float.isNaN(chance)) return String.format(Locale.ROOT, "%2.2f", chance * 100) + "%";
            return min == max ? Integer.toString(min) : min + " - " + max;
        }
    }
}
