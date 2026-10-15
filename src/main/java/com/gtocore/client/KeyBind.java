package com.gtocore.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import org.lwjgl.glfw.GLFW;

public final class KeyBind {

    public static final KeyMapping debugInspectKey = new KeyMapping("key.gtocore.debug_inspect",
            KeyConflictContext.GUI, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.keybinding.gtocore");
    public static final KeyMapping movableHudToggle = new KeyMapping("key.gtocore.movable_hud_toggle",
            InputConstants.KEY_Z, "key.keybinding.gtocore");

    public static void init() {
        KeyMappingRegistry.register(debugInspectKey);
        KeyMappingRegistry.register(movableHudToggle);
    }
}
