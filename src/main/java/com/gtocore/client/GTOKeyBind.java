package com.gtocore.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import org.lwjgl.glfw.GLFW;

public final class GTOKeyBind {

    public static final KeyMapping debugInspectKey = new KeyMapping("key.gtocore.debug_inspect",
            KeyConflictContext.GUI, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.keybinding.gtocore");
    public static final KeyMapping hudInteract = new KeyMapping("key.gtocore.hud_interact",
            InputConstants.KEY_Z, "key.keybinding.gtocore");
    public static final KeyMapping hudLayout = new KeyMapping("key.gtocore.hud_layout",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.keybinding.gtocore");
    public static final KeyMapping droneToggle = new KeyMapping("key.gtocore.drone_toggle",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.keybinding.gtocore");
    public static final KeyMapping implantAction = new KeyMapping("key.gtocore.implant_action",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.keybinding.gtocore");
    public static final KeyMapping implantWheel = new KeyMapping("key.gtocore.implant_wheel",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.keybinding.gtocore");

    public static void init() {
        KeyMappingRegistry.register(debugInspectKey);
        KeyMappingRegistry.register(hudInteract);
        KeyMappingRegistry.register(hudLayout);
        KeyMappingRegistry.register(droneToggle);
        KeyMappingRegistry.register(implantAction);
        KeyMappingRegistry.register(implantWheel);
    }
}
