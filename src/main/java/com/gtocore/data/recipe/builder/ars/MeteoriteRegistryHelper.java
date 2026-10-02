package com.gtocore.data.recipe.builder.ars;

import com.gtolib.GTOCore;
import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import com.arsmeteorites.arsmeteorites.common.RecipeRegistry;
import com.google.common.collect.ObjectArrays;
import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class MeteoriteRegistryHelper {

    public static void registerMeteoriteType(String inputItemId, double source, int model, Item consumeItem,
                                             String[] blockIds, int[] weights) {
        if (requiresLayers(model)) {
            GTOCore.LOGGER.error("无法注册陨石类型: 输入物品 {}, 陨石模型 {} 需要层信息", inputItemId, model);
            return;
        }
        int[] layerData = {0};
        registerMeteoriteType(inputItemId, source, model, consumeItem, blockIds, weights, layerData);
    }

    public static void registerMeteoriteType(String inputItemId, double source, int model, Item consumeItem,
                                             String[] blockIds, int[] weights, int[] layerData) {
        Item input = RegistriesUtils.getItem(inputItemId);
        if (input == Items.AIR) {
            GTOCore.LOGGER.error("无法注册陨石类型 {}: 物品不存在", inputItemId);
            return;
        }

        Block[] blocks = new Block[blockIds.length];
        for (int i = 0; i < blockIds.length; i++) {
            blocks[i] = RegistriesUtils.getBlock(blockIds[i]);
            if (blocks[i] == Blocks.AIR) {
                GTOCore.LOGGER.error("无法注册陨石类型 {}: 方块 {} 不存在", inputItemId, blockIds[i]);
                return;
            }
        }

        if (weights.length != blockIds.length) {
            GTOCore.LOGGER.error("无法注册陨石类型 {}: 权重数组长度不匹配", inputItemId);
            return;
        }

        registerMeteoriteType(input, source, model, consumeItem, blocks, weights, layerData);
    }

    public static void registerMeteoriteType(Item input, double source, int model, Item consumeItem,
                                             Block[] meteorites, int[] weights) {
        if (requiresLayers(model)) {
            GTOCore.LOGGER.error("无法注册陨石类型 {}: 陨石模型 {} 需要层信息", input, model);
            return;
        }
        int[] layerData = {0};
        registerMeteoriteType(input, source, model, consumeItem, meteorites, weights, layerData);
    }

    public static void registerMeteoriteType(Item input, double source, Item consumeItem, Block[] stones,
                                             int[] stoneWeights, TagPrefix tagPrefix, Material[] oreMaterials,
                                             int[] oreWeights) {
        Block[] ores = new Block[oreMaterials.length];
        for (int i = 0; i < oreMaterials.length; i++) {
            ores[i] = ChemicalHelper.getBlock(tagPrefix, oreMaterials[i]);
            if (ores[i] == Blocks.AIR) {
                GTOCore.LOGGER.error("陨石的 {}: 对应矿石 {} 不存在", oreMaterials[i], tagPrefix);
                return;
            }
        }
        Block[] innerBlocks = ObjectArrays.concat(stones, ores, Block.class);
        Block[] allBlocks = ObjectArrays.concat(innerBlocks, stones, Block.class);
        int[] weights = Ints.concat(stoneWeights, oreWeights, stoneWeights);
        int[] layerData = {innerBlocks.length, stones.length, 60, 10};
        registerMeteoriteType(input, source, 3, consumeItem, allBlocks, weights, layerData);
    }

    public static void registerMeteoriteType(@NotNull Item input, double source, int model, Item consumeItem,
                                             Block[] meteorites, int[] weights, int[] layerData) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(input);
        String id = itemId.getNamespace() + "-" + itemId.getPath();
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }

        int[] registrationLayers = layerData;
        if (requiresLayers(model)) {
            if (!validateLayers(input, meteorites.length, layerData)) return;
            registrationLayers = expandLayerData(weights, layerData);
        }

        try {
            RecipeRegistry.registerMeteoriteType(new RecipeRegistry.MeteoriteType(
                    id, input, source, model, consumeItem, meteorites, weights, totalWeight, registrationLayers));
        } catch (IllegalStateException e) {
            GTOCore.LOGGER.error("注册陨石类型失败: {}", e.getMessage());
        }
    }

    private static boolean requiresLayers(int model) {
        return model == 3 || model == 4;
    }

    private static boolean validateLayers(Item input, int blockCount, int[] layerData) {
        if (layerData.length % 2 != 0) {
            GTOCore.LOGGER.error("无法注册陨石类型 {}: 层级参数错误", input);
            return false;
        }
        int layerCount = layerData.length / 2;
        int remainingBlocks = blockCount;
        for (int layerIndex = 0; layerIndex < layerCount; layerIndex++) {
            remainingBlocks -= layerData[layerIndex];
        }
        if (remainingBlocks < 0) {
            GTOCore.LOGGER.error("无法注册陨石类型 {}: 层中方块数量大于总方块数", input);
            return false;
        }
        return true;
    }

    /**
     * 输入布局为 [各层方块数, 各层参数]，两个区段长度相同。
     * 注册时追加 [各层方块权重和, 层数, 各层参数之和]，原始输入数组保持不变。
     */
    private static int[] expandLayerData(int[] weights, int[] layerData) {
        int layerCount = layerData.length / 2;
        int metadataOffset = layerData.length;
        int[] expanded = Arrays.copyOf(layerData, metadataOffset + layerCount + 2);
        expanded[metadataOffset + layerCount] = layerCount;
        for (int layerIndex = 0; layerIndex < layerCount; layerIndex++) {
            expanded[metadataOffset + layerCount + 1] += layerData[layerCount + layerIndex];
        }

        int weightOffset = 0;
        for (int layerIndex = 0; layerIndex < layerCount; layerIndex++) {
            int blocksInLayer = layerData[layerIndex];
            for (int blockIndex = 0; blockIndex < blocksInLayer; blockIndex++) {
                expanded[metadataOffset + layerIndex] += weights[weightOffset + blockIndex];
            }
            weightOffset += blocksInLayer;
        }
        return expanded;
    }
}
