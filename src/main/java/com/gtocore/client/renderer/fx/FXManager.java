package com.gtocore.client.renderer.fx;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class FXManager {

    public static final List<AbstractFX> FX_LIST = new CopyOnWriteArrayList<>();
    private static final Map<Object, AbstractFX> KEYED_FX = new ConcurrentHashMap<>();

    public static void dispatchFXs(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            ScreenSpaceSceneCapture.beginFrame();
            StellarForgeVortexFX.beginBatchFrame();
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            // Late refraction needs the completed scene, including translucent fluids and particles.
            ScreenSpaceSceneCapture.beginFrame();
        }
        for (AbstractFX fx : FX_LIST) {
            if (!fx.isDiscarded()) {
                fx.render(event.getStage(), event.getLevelRenderer(), event.getPoseStack(), event.getProjectionMatrix(), event.getPartialTick(), event.getCamera(), event.getFrustum());
            }
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            StellarForgeVortexFX.flushBatch(event.getLevelRenderer());
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            ScreenSpaceSceneCapture.beginFrame();
            SolarStormFX.render(event);
        }
    }

    public static void tickFXs() {
        SolarSurfaceFX.beginTick();
        List<AbstractFX> discarded = new ArrayList<>();
        FX_LIST.removeIf(fx -> {
            fx.tick();
            if (fx.shouldDiscard()) {
                discarded.add(fx);
                return true;
            }
            return false;
        });
        discarded.forEach(AbstractFX::onDiscard);
        if (!discarded.isEmpty()) {
            KEYED_FX.entrySet().removeIf(entry -> discarded.contains(entry.getValue()) || entry.getValue().isDiscarded());
        }
        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (FX_LIST.isEmpty() && (!SolarStormFX.isSolarSurface(level) || level.getRainLevel(1.0F) <= 0.001F)) {
            ScreenSpaceSceneCapture.release();
        }
    }

    public static void addFX(AbstractFX fx) {
        FX_LIST.add(fx);
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractFX> T upsertFX(Object key, Supplier<T> factory, Consumer<T> refresh) {
        AbstractFX existing = KEYED_FX.get(key);
        T fx;
        if (existing == null || existing.isDiscarded() || !FX_LIST.contains(existing)) {
            fx = factory.get();
            KEYED_FX.put(key, fx);
            FX_LIST.add(fx);
        } else {
            fx = (T) existing;
        }
        refresh.accept(fx);
        return fx;
    }

    public static void clearFXs() {
        List<AbstractFX> discarded = new ArrayList<>(FX_LIST);
        FX_LIST.clear();
        KEYED_FX.clear();
        discarded.forEach(AbstractFX::onDiscard);
        ScreenSpaceSceneCapture.release();
        SolarStormFX.release();
    }
}
