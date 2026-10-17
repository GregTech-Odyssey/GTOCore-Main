package com.gtocore.integration.ae.wtlib;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

public final class TerminalHotkeys {

    private static final Object2ObjectOpenHashMap<String, String> BY_TERMINAL = new Object2ObjectOpenHashMap<>();

    private TerminalHotkeys() {}

    public static void register(String terminal, String hotkey) {
        BY_TERMINAL.put(terminal, hotkey);
    }

    @Nullable
    public static String of(String terminal) {
        return BY_TERMINAL.get(terminal);
    }
}
