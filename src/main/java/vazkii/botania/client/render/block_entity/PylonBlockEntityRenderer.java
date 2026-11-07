package vazkii.botania.client.render.block_entity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import vazkii.botania.client.BotaniaShaders;
import vazkii.botania.client.model.GaiaPylonModel;
import vazkii.botania.client.model.ManaPylonModel;
import vazkii.botania.client.model.NaturaPylonModel;
import vazkii.botania.client.model.PylonModel;
import vazkii.botania.client.model.PylonModelLayers;
import vazkii.botania.common.block.PylonBlock;
import vazkii.botania.common.block.block_entity.PylonBlockEntity;

import java.util.Random;

/**
 * 水晶渲染：环与晶体反向旋转，晶体上下浮动，与原模组的动画节奏一致。
 */
public class PylonBlockEntityRenderer implements BlockEntityRenderer<PylonBlockEntity> {

    private static final ResourceLocation MANA_TEXTURE = new ResourceLocation("botania", "textures/model/pylon_mana.png");
    private static final ResourceLocation NATURA_TEXTURE = new ResourceLocation("botania", "textures/model/pylon_natura.png");
    private static final ResourceLocation GAIA_TEXTURE = new ResourceLocation("botania", "textures/model/pylon_gaia.png");

    /** 物品渲染时由物品侧设置。 */
    public static PylonBlock.Variant forceVariant = PylonBlock.Variant.MANA;
    public static ItemDisplayContext forceTransform = ItemDisplayContext.NONE;

    private final ManaPylonModel manaModel;
    private final NaturaPylonModel naturaModel;
    private final GaiaPylonModel gaiaModel;

    public PylonBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        manaModel = new ManaPylonModel(context.bakeLayer(PylonModelLayers.PYLON_MANA));
        naturaModel = new NaturaPylonModel(context.bakeLayer(PylonModelLayers.PYLON_NATURA));
        gaiaModel = new GaiaPylonModel(context.bakeLayer(PylonModelLayers.PYLON_GAIA));
    }

    @Override
    public void render(@org.jetbrains.annotations.Nullable PylonBlockEntity pylon, float partialTicks, PoseStack ms,
                       MultiBufferSource buffers, int light, int overlay) {
        boolean renderingItem = pylon == null;
        boolean direct = renderingItem && (forceTransform == ItemDisplayContext.GUI || forceTransform.firstPerson());
        PylonBlock.Variant variant = renderingItem ? forceVariant : pylon.variant();
        PylonModel model = switch (variant) {
            case NATURA, ALFSTEEL -> naturaModel;
            case GAIA -> gaiaModel;
            default -> manaModel;
        };
        RenderType layer = BotaniaShaders.pylonGlow(pylon.getBlockState().getBlock());

        float worldTime = renderingItem || pylon.getLevel() == null ? partialTicks : pylon.getLevel().getGameTime() + partialTicks;
        if (!renderingItem) {
            worldTime += new Random(pylon.getBlockPos().hashCode()).nextInt(360);
        }

        ms.pushPose();
        ms.translate(0, direct ? 1.35 : 1.5, 0);
        ms.scale(1.0F, -1.0F, -1.0F);

        ms.pushPose();
        ms.translate(0.5F, 0F, -0.5F);
        if (!direct) {
            ms.mulPose(Axis.YP.rotationDegrees(worldTime * 1.5F));
        }
        VertexConsumer ringBuffer = buffers.getBuffer(layer);
        model.renderRing(ms, ringBuffer, light, overlay);
        if (!direct) {
            ms.translate(0D, Math.sin(worldTime / 20D) / 20 - 0.025, 0D);
        }
        ms.popPose();

        ms.pushPose();
        if (!direct) {
            ms.translate(0D, Math.sin(worldTime / 20D) / 17.5, 0D);
        }
        ms.translate(0.5F, 0F, -0.5F);
        if (!direct) {
            ms.mulPose(Axis.YP.rotationDegrees(-worldTime));
        }
        VertexConsumer crystalBuffer = buffers.getBuffer(layer);
        model.renderCrystal(ms, crystalBuffer, light, overlay);
        ms.popPose();

        ms.popPose();
    }
}
