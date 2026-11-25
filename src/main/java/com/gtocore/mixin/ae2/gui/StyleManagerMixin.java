package com.gtocore.mixin.ae2.gui;

import appeng.client.gui.style.StyleManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StyleManager.class)
public class StyleManagerMixin {

    @ModifyVariable(method = "loadStyleDoc", at = @At("HEAD"), argsOnly = true, remap = false)
    private static String loadStyleDocHooks(String path) {
        if (path.contains("wireless_pattern_encoding_terminal.json")) {
            return "/screens/wtlib/modify_wireless_pattern_encoding_terminal.json";
        } else if (path.contains("wireless_crafting_terminal.json")) {
            return "/screens/wtlib/modify_wireless_crafting_terminal.json";
        } else if (path.contains("wireless_pattern_access_terminal.json")) {
            return "/screens/wtlib/modify_wireless_pattern_access_terminal.json";
        } else if (path.contains("wtlib/magnet.json")) {
            return "/screens/wtlib/modify_magnet.json";
        } else if (path.contains("wtlib/trash.json")) {
            return "/screens/wtlib/modify_trash.json";
        } else if (path.contains("pattern_encoding_terminal.json")) {
            return "/screens/terminals/modify_pattern_encoding_terminal.json";
        } else if (path.contains("ex_pattern_provider.json")) {
            return "/screens/modify_pattern_provider.json";
        } else if (path.contains("tag_export_bus.json")) {
            return "/screens/modifying/tag_export_bus.json";
        } else if (path.contains("tag_storage_bus.json")) {
            return "/screens/modifying/tag_storage_bus.json";
        }
        return path;
    }

    @ModifyVariable(method = "loadMergedJsonTree", at = @At("HEAD"), argsOnly = true, remap = false)
    private static String loadMergedJsonTreeHooks(String path) {
        if (path.endsWith("/wtlib/universal_terminal_no_viewcells.json")) {
            return "/screens/wtlib/modify_universal_terminal_no_viewcells.json";
        } else if (path.endsWith("/wtlib/universal_terminal_with_viewcells.json")) {
            return "/screens/wtlib/modify_universal_terminal_with_viewcells.json";
        }
        return path;
    }
}
