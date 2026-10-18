package com.gtocore.client;

import com.gtocore.integration.Mods;
import com.gtocore.integration.emi.NotDevCompat;
import com.gtocore.mixin.inventoryessentials.InventoryEssentialsClientAccessor;

import net.minecraft.client.multiplayer.ClientLevel;
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

    public static void trackCraftingUI(CraftingUIReferenceHolder part) {
        craftingUI = new WeakReference<>(part);
    }

    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        // EMI clears its registry in Minecraft.clearLevel; a second clear can restart its worker.
        releaseWorldReferences();
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
