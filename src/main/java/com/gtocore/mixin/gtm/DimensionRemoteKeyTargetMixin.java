package com.gtocore.mixin.gtm;

import com.gtolib.api.dimension.hooks.DimensionRemoteTarget;
import com.gtolib.api.dimension.lifecycle.DimensionRuntimeCaches;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityWatch;
import com.gregtechceu.gtceu.api.transfer.key.RemoteKeyTarget;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.suried.gtoutils.schedule.Cell;
import com.suried.gtoutils.schedule.Scope;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 使 GTM 远程目标保留维度键和坐标，并在查询时绑定当前已加载的世界。
 * <p>
 * 目的世界卸载时释放监听器、区块实体和处理器引用；后续查询不会唤醒世界，重新加载后自动重绑。
 */
@Mixin(value = RemoteKeyTarget.class, remap = false)
public abstract class DimensionRemoteKeyTargetMixin implements DimensionRemoteTarget {

    @Shadow
    private Level level;
    @Shadow
    private BlockPos pos;
    @Shadow
    private BlockEntity blockEntity;

    @Shadow
    public abstract boolean bind(Level level, BlockPos pos);

    @Shadow
    private void release(BlockEntity next) {
        throw new AssertionError();
    }

    @Unique
    private final Cell<Integer> gtolib$binding = Cell.of(0);

    @Unique
    private MinecraftServer gtolib$server;
    @Unique
    private ResourceKey<Level> gtolib$dimension;

    @Inject(method = "bind", at = @At("TAIL"))
    private void gtolib$trackRuntime(Level target, BlockPos position, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) gtolib$binding.set(gtolib$binding.peek() + 1);
        gtolib$server = target == null ? null : target.getServer();
        gtolib$dimension = target == null ? null : target.dimension();
        if (target != null && !target.isClientSide) {
            DimensionRuntimeCaches.track(target, this);
        }
    }

    @Override
    public void gtolib$bindDimension(MinecraftServer server, ResourceKey<Level> key, BlockPos position) {
        bind(server.getLevel(key), position);
        gtolib$server = server;
        gtolib$dimension = key;
    }

    @Override
    public void gtolib$releaseDimension(Level closing) {
        if (level != closing) {
            return;
        }
        var server = gtolib$server;
        var key = gtolib$dimension;
        bind(null, pos);
        gtolib$server = server;
        gtolib$dimension = key;
    }

    @Inject(method = "mount", at = @At("HEAD"), cancellable = true)
    private void gtolib$mountDimension(Scope scope, Runnable changed, CallbackInfo ci) {
        scope.select(gtolib$binding::get, (mounted, version) -> {
            var targetLevel = level;
            var targetPos = pos;
            if (targetLevel != null && targetPos != null) {
                BlockEntityWatch.listen(mounted, targetLevel, targetPos, be -> {
                    if (level != targetLevel || !targetPos.equals(pos)) return;
                    if (be.isRemoved()) {
                        if (be == blockEntity) release(null);
                    } else if (be != blockEntity) {
                        release(be);
                    }
                    changed.run();
                });
            }
            changed.run();
        });
        ci.cancel();
    }

    @Inject(method = "blockEntity", at = @At("HEAD"))
    private void gtolib$resolveLoadedTarget(CallbackInfoReturnable<BlockEntity> cir) {
        if (gtolib$server == null || gtolib$dimension == null) {
            return;
        }
        var current = gtolib$server.getLevel(gtolib$dimension);
        if (level != current) {
            var server = gtolib$server;
            var key = gtolib$dimension;
            bind(current, pos);
            gtolib$server = server;
            gtolib$dimension = key;
        }
    }
}
