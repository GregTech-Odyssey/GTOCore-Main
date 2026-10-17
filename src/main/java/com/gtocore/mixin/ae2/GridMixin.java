package com.gtocore.mixin.ae2;

import com.gtocore.integration.jade.provider.AEGridProvider;

import com.gtolib.api.ae2.IExpandedGrid;

import com.gregtechceu.gtceu.api.misc.TickTimeSampler;

import appeng.api.networking.IGridNode;
import appeng.hooks.ticking.TickHandler;
import appeng.me.Grid;

import com.google.common.collect.SetMultimap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AE 网络的 tick 耗时统计：一个游戏刻里有三段服务调用（serverStart / levelStart / serverEnd），每段前后各插一次
 * {@link TickTimeSampler}，采样与平均都在那个类里。
 */
@Mixin(Grid.class)
public abstract class GridMixin implements IExpandedGrid {

    @Unique
    private final TickTimeSampler gtocore$tickTimeSampler = new TickTimeSampler();

    @Shadow(remap = false)
    @Final
    private SetMultimap<Class<?>, IGridNode> machines;

    @Override
    public SetMultimap<Class<?>, IGridNode> getMachines() {
        return machines;
    }

    @Override
    public long getAverageTickTimeMicros() {
        return gtocore$tickTimeSampler.getAverageTickTimeMicros();
    }

    @Unique
    private void gtocore$beginMeasure() {
        // 性能监控机器打开时，所有网络都要测
        if (AEGridProvider.OBSERVE) gtocore$tickTimeSampler.getAverageTickTimeMicros();
        gtocore$tickTimeSampler.insertStart((int) TickHandler.instance().getCurrentTick());
    }

    @Unique
    private void gtocore$endMeasure() {
        gtocore$tickTimeSampler.insertEnd();
        if (AEGridProvider.OBSERVE) IExpandedGrid.PERFORMANCE_MAP.put(this, gtocore$tickTimeSampler.getAverageTickTimeMicros());
    }

    //////////////////////////////////////
    // ******* 注入 *******//
    //////////////////////////////////////

    @Inject(method = "onServerStartTick", at = @At(value = "HEAD"), remap = false)
    private void gtocore$onServerStartTick(CallbackInfo ci) {
        gtocore$beginMeasure();
    }

    @Inject(method = "onServerStartTick", at = @At("TAIL"), remap = false)
    private void gtocore$onServerStartTickEnd(CallbackInfo ci) {
        gtocore$endMeasure();
    }

    @Inject(method = "onLevelStartTick", at = @At(value = "HEAD"), remap = false)
    private void gtocore$onLevelStartTick(CallbackInfo ci) {
        gtocore$beginMeasure();
    }

    @Inject(method = "onLevelStartTick", at = @At("TAIL"), remap = false)
    private void gtocore$onLevelStartTickEnd(CallbackInfo ci) {
        gtocore$endMeasure();
    }

    @Inject(method = "onServerEndTick", at = @At(value = "HEAD"), remap = false)
    private void gtocore$onServerEndTick(CallbackInfo ci) {
        gtocore$beginMeasure();
    }

    @Inject(method = "onServerEndTick", at = @At("TAIL"), remap = false)
    private void gtocore$onServerEndTickEnd(CallbackInfo ci) {
        gtocore$endMeasure();
    }
}
