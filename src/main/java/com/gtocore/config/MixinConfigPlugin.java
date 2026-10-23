package com.gtocore.config;

import com.gtocore.integration.Mods;

import com.gtolib.api.misc.AbstractMixinConfigPlugin;

import net.minecraftforge.fml.loading.FMLLoader;

public final class MixinConfigPlugin extends AbstractMixinConfigPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("com.gtocore.mixin.balm.")) {
            return Mods.BALM.isLoaded();
        }
        if (mixinClassName.startsWith("com.gtocore.mixin.xaero.")) {
            return Mods.XAERO_WORLDMAP.isLoaded();
        }
        if (mixinClassName.startsWith("com.gtocore.mixin.inventoryessentials.")) {
            return Mods.INVENTORY_ESSENTIALS.isLoaded();
        }
        if (mixinClassName.startsWith("com.gtocore.mixin.ftbxmodcompat.")) {
            return Mods.FTB_XMOD_COMPAT.isLoaded();
        }
        if (mixinClassName.startsWith("com.gtocore.mixin.renderblender.")) {
            return Mods.RENDER_BLENDER.isLoaded();
        }
        if (mixinClassName.startsWith("com.gtocore.mixin.corpse.")) {
            return Mods.CORPSE.isLoaded();
        }
        return switch (mixinClassName) {
            case "com.gtocore.mixin.sophisticatedcore.CraftingUpgradeTweakUIPartMixin" -> Mods.SOPHISTICATEDCORE.isLoaded() && FMLLoader.getLoadingModList().getModFileById("craftingtweaks") != null;
            case "com.gtocore.mixin.ae2.search.RepoSearchMixin", "com.gtocore.mixin.ae2.search.AEKeyMixin" -> Mods.LANG.isLoaded();
            case "com.gtocore.mixin.adastra.ServerboundLandOnSpaceStationPacketMixin" -> Mods.FTBTEAMS.isLoaded();
            case "com.gtocore.mixin.mousetweaks.GuiContainerHandlerMixin" -> Mods.MOUSETWEAKS.isLoaded();
            case "com.gtocore.mixin.sophisticatedbackpacks.ToolSwapperUpgradeWrapperMixin" -> Mods.SOPHISTICATEDBACKPACKS.isLoaded();
            case "com.gtocore.mixin.sophisticatedcore.MagnetUpgradeWrapperMixin" -> Mods.SOPHISTICATEDCORE.isLoaded();
            default -> true;
        };
    }
}
