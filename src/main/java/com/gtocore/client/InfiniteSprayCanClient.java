package com.gtocore.client;

import com.gtocore.mixin.ftbu.FTBUltimineClientAccessor;

import net.minecraft.core.BlockPos;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * 无尽喷漆罐连锁上色的客户端部分（FTB Ultimine 的按键与高亮形状）。
 * <p>
 * 单独成类：{@code InfiniteSprayCanBehaviourMixin} 是两端都会应用的 mixin，方法体里若直接出现
 * {@link FTBUltimineClient}、{@code KeyMapping} 等客户端类，专用服务器处理 mixin 时就会因找不到这些类而崩溃。
 * 只在客户端（{@code level.isClientSide}）调用本类。类本身不能标 {@code @OnlyIn(Dist.CLIENT)}：
 * mixin 处理时会读取本类的元数据，专用服务器上带该注解的类一读就抛异常。
 */
public final class InfiniteSprayCanClient {

    private InfiniteSprayCanClient() {}

    /** FTB Ultimine 连锁键是否按下。 */
    public static boolean isUltimineKeyDown() {
        return FTBUltimineClient.keyBinding != null && FTBUltimineClient.keyBinding.isDown();
    }

    /** 客户端当前显示的连锁高亮形状（与服务端计算的集合一致时用于预先上色）。 */
    @Nullable
    public static Collection<BlockPos> shapeBlocks() {
        if (!(FTBUltimine.instance.proxy instanceof FTBUltimineClient client)) {
            return null;
        }
        List<BlockPos> shape = ((FTBUltimineClientAccessor) (Object) client).gto$getShapeBlocks();
        if (shape == null || shape.isEmpty()) {
            return null;
        }
        return shape;
    }
}
