package com.gtocore.client;

import com.gtocore.integration.Mods;
import com.gtocore.integration.emi.NotDevCompat;
import com.gtocore.mixin.balm.ForgeBalmModelsAccessor;
import com.gtocore.mixin.balm.ModelManagerAccessor;
import com.gtocore.mixin.inventoryessentials.InventoryEssentialsClientAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;

import com.google.common.collect.MapMaker;
import dev.emi.emi.runtime.EmiHistory;
import thetadev.constructionwand.ConstructionWand;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;

public final class ClientWorldLeakFixes {

    // Models can be baked off-thread. Weak identity keys must not become another model cache.
    private static final Set<ClientReferenceHolder> MODELS = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());
    // Xaero 的 MapProcessor / WorldDataHandler 生命周期较长，弱键集合本身不会造成新的泄漏。
    private static final Set<ClientReferenceHolder> XAERO_REFERENCES = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());
    // -Dgtocore.leakfix.balm=false 可关闭 Balm/ModelManager 的 ModelBakery 清理
    private static final boolean BALM_CLEAR_ENABLED = !"false".equalsIgnoreCase(System.getProperty("gtocore.leakfix.balm", "true"));
    private static volatile WeakReference<CraftingUIReferenceHolder> craftingUI = new WeakReference<>(null);

    private ClientWorldLeakFixes() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(ClientWorldLeakFixes::onLogout);
        MinecraftForge.EVENT_BUS.addListener(ClientWorldLeakFixes::onPlayerClone);
        MinecraftForge.EVENT_BUS.addListener(ClientWorldLeakFixes::onLevelUnload);
        MinecraftForge.EVENT_BUS.addListener(ClientWorldLeakFixes::onScreenClosing);
        MinecraftForge.EVENT_BUS.addListener(ClientWorldLeakFixes::onMouseReleased);
    }

    public static void trackModel(ClientReferenceHolder model) {
        MODELS.add(model);
    }

    public static void trackXaeroReference(ClientReferenceHolder reference) {
        XAERO_REFERENCES.add(reference);
    }

    public static void trackCraftingUI(CraftingUIReferenceHolder part) {
        craftingUI = new WeakReference<>(part);
    }

    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        // EMI clears its registry in Minecraft.clearLevel; a second clear can restart its worker.
        releaseWorldReferences();
        releaseLogoutOnlyReferences();
        if (Mods.FTB_XMOD_COMPAT.isLoaded()) NotDevCompat.clearRuntime();
    }

    private static void onPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        releaseWorldReferences();
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel) releaseWorldReferences();
    }

    private static void releaseWorldReferences() {
        EmiHistory.clear();
        clearDragSlot();
        if (Mods.CONSTRUCTION_WAND.isLoaded()) ConstructionWandReferences.clear();
        for (var model : MODELS) {
            model.gtocore$releaseClientReferences();
        }
        var part = craftingUI.get();
        if (part != null) part.gtocore$releaseClientReferences();
    }

    /**
     * 只在真正退出世界（回标题 / 断线）时清理这两类引用：
     * <ul>
     * <li>Xaero 的 MapProcessor 在换维度、重生时还要继续用，不能跟着 LevelEvent.Unload 清；</li>
     * <li>Balm 的 ModelBakery 只在资源重载时才会重新赋值，提前清掉会让 getModelBakery() 失效。</li>
     * </ul>
     */
    private static void releaseLogoutOnlyReferences() {
        if (Mods.XAERO_WORLDMAP.isLoaded()) {
            for (var reference : XAERO_REFERENCES) {
                reference.gtocore$releaseClientReferences();
            }
        }
        if (Mods.BALM.isLoaded()) clearBalmModelBakery();
    }

    /**
     * Balm 的静态 {@code ForgeBalmModels.modelBakery} 和 Forge 的 {@code ModelManager.modelBakery}
     * 指向同一个 ModelBakery（约 70 MB，含 ModelBakery$LoadedJson）；只断一边等于没断。
     */
    private static void clearBalmModelBakery() {
        if (!BALM_CLEAR_ENABLED) return;
        ModelBakery bakery = ForgeBalmModelsAccessor.gtocore$getModelBakery();
        ForgeBalmModelsAccessor.gtocore$setModelBakery(null);
        if (bakery == null) return;
        var manager = Minecraft.getInstance().getModelManager();
        if (manager != null && ((ModelManagerAccessor) manager).gtocore$getModelBakery() == bakery) {
            ((ModelManagerAccessor) manager).gtocore$setModelBakery(null);
        }
    }

    private static void onScreenClosing(ScreenEvent.Closing event) {
        clearDragSlot();
        var part = craftingUI.get();
        if (part != null) part.gtocore$releaseScreen(event.getScreen());
    }

    private static void onMouseReleased(ScreenEvent.MouseButtonReleased.Post event) {
        clearDragSlot();
    }

    private static void clearDragSlot() {
        if (Mods.INVENTORY_ESSENTIALS.isLoaded()) InventoryEssentialsClientAccessor.gtocore$setLastDragHoverSlot(null);
    }

    private static final class ConstructionWandReferences {

        private static void clear() {
            var preview = ConstructionWand.instance.renderBlockPreview;
            if (preview != null) {
                preview.reset();
                preview.undoBlocks = null;
            }
        }
    }
}
