package vazkii.botania.client;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;

/**
 * 水晶辉光着色器与渲染类型。
 *
 * <p>
 * 与原模组一致：使用自定义片元着色器做流光与边缘发光，顶点格式为实体格式，
 * 非直接渲染时输出到物品实体目标。
 * </p>
 */
public final class BotaniaShaders {

    private static final ResourceLocation MANA_TEXTURE = new ResourceLocation("botania", "textures/model/pylon_mana.png");
    private static final ResourceLocation NATURA_TEXTURE = new ResourceLocation("botania", "textures/model/pylon_natura.png");
    private static final ResourceLocation GAIA_TEXTURE = new ResourceLocation("botania", "textures/model/pylon_gaia.png");
    private static final ResourceLocation ALFSTEEL_TEXTURE = new ResourceLocation("mythicbotany", "textures/model/pylon_alfsteel.png");

    @Nullable
    private static ShaderInstance pylon;
    @Nullable
    private static ShaderInstance doppleganger;

    private static final java.util.Map<ResourceLocation, RenderType> PYLON_LAYERS = new java.util.HashMap<>();

    private BotaniaShaders() {}

    public static void register(RegisterShadersEvent event) {
        try {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), new ResourceLocation("botania", "pylon"),
                            DefaultVertexFormat.NEW_ENTITY),
                    shader -> pylon = shader);
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), new ResourceLocation("botania", "doppleganger"),
                            DefaultVertexFormat.NEW_ENTITY),
                    shader -> doppleganger = shader);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load botania shaders", e);
        }
    }

    @Nullable
    public static ShaderInstance doppleganger() {
        return doppleganger;
    }

    /** 盖亚守护者的噪点层，按纹理缓存。 */
    public static RenderType getDopplegangerLayer(ResourceLocation texture) {
        return DOPPLEGANGER.apply(texture);
    }

    private static final Function<ResourceLocation, RenderType> DOPPLEGANGER = Util.memoize(
            texture -> GlowLayers.doppleganger(texture));

    @Nullable
    public static ShaderInstance pylon() {
        return pylon;
    }

    /** 取对应水晶的辉光层；着色器尚未加载时退回普通半透明层，避免显示异常。 */
    public static RenderType pylonGlow(net.minecraft.world.level.block.Block block) {
        ResourceLocation texture = switch (block instanceof vazkii.botania.common.block.PylonBlock pylon ? pylon.variant : vazkii.botania.common.block.PylonBlock.Variant.MANA) {
            case NATURA -> NATURA_TEXTURE;
            case GAIA -> GAIA_TEXTURE;
            case ALFSTEEL -> ALFSTEEL_TEXTURE;
            default -> MANA_TEXTURE;
        };
        if (pylon == null) {
            return RenderType.entityTranslucent(texture);
        }
        return PYLON_LAYERS.computeIfAbsent(texture, GlowLayers::create);
    }

    /** 继承 {@link RenderType} 以便访问其受保护的渲染状态常量。 */
    private static final class GlowLayers extends RenderType {

        private GlowLayers() {
            super("botania_glow_layers", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false,
                    () -> {}, () -> {});
        }

        private static RenderType doppleganger(ResourceLocation texture) {
            CompositeState state = CompositeState.builder()
                    .setShaderState(new ShaderStateShard(BotaniaShaders::doppleganger))
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true);
            return RenderType.create("botania:doppleganger", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                    256, true, true, state);
        }

        private static RenderType create(ResourceLocation texture) {
            CompositeState state = CompositeState.builder()
                    .setShaderState(new ShaderStateShard(BotaniaShaders::pylon))
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .createCompositeState(false);
            return RenderType.create("botania:pylon_glow_" + texture.getPath().replace('/', '_'),
                    DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 128, false, false, state);
        }
    }
}
