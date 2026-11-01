package com.gtolib.api.gui.hud;

import dev.toma.configuration.config.ConfigHolder;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;
import dev.toma.configuration.config.io.ConfigIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HudSettingsTest {

    @TempDir
    Path directory;

    @Test
    void dronePanelsRestoreTheirOwnLayoutsAfterConfigReloadAndRestart() {
        var config = newConfig();
        var ids = new String[] { "drone_mode", "drone_speed", "drone_preview", "drone_power" };
        var layouts = new HudLayout[ids.length];
        for (int i = 0; i < ids.length; i++) {
            var layout = new HudLayout();
            layout.load(0.5 + i * 0.25, 15 + i * 20, 80 - i * 20);
            layout.update(1000, 800, 0, 0, 200, 100);
            layouts[i] = layout;
            HudSettings.save(config, ids[i], layout);
        }

        assertLayouts(config.getConfigInstance().client.hud.panels, layouts);
        ConfigIO.reloadClientValues(config);
        assertLayouts(config.getConfigInstance().client.hud.panels, layouts);

        var restarted = newConfig();
        ConfigIO.reloadClientValues(restarted);
        assertLayouts(restarted.getConfigInstance().client.hud.panels, layouts);
    }

    @Test
    void savingWhileWindowIsSmallKeepsThePreferredScaleForRestart() {
        var layout = new HudLayout();
        layout.load(2, 75, 60);
        layout.update(100, 40, 0, 0, 200, 100);
        assertEquals(0.4, layout.scale, 0.0001);
        HudSettings.save(newConfig(), "drone_preview", layout);

        var restarted = newConfig();
        ConfigIO.reloadClientValues(restarted);
        var panel = restarted.getConfigInstance().client.hud.panels.drone_preview;
        var restored = new HudLayout();
        restored.load(panel.scale, panel.x, panel.y);
        restored.update(1000, 800, 0, 0, 200, 100);
        assertEquals(2, restored.scale, 0.0001);
        assertEquals(450, restored.bounds.getX());
        assertEquals(360, restored.bounds.getY());
    }

    private ConfigHolder<Fixture> newConfig() {
        return new ConfigHolder<>(Fixture.class, "hud_settings_test", directory.resolve("hud").toString(), "hud_settings_test", ConfigFormats.YAML);
    }

    private static void assertLayouts(HudSettings settings, HudLayout[] layouts) {
        var panels = new HudSettings.Panel[] { settings.drone_mode, settings.drone_speed, settings.drone_preview, settings.drone_power };
        for (int i = 0; i < panels.length; i++) {
            assertEquals(layouts[i].requestedScale(), panels[i].scale);
            assertEquals(layouts[i].relativeX(), panels[i].x);
            assertEquals(layouts[i].relativeY(), panels[i].y);
            var restored = new HudLayout();
            restored.load(panels[i].scale, panels[i].x, panels[i].y);
            restored.update(1000, 800, 0, 0, 200, 100);
            assertEquals(layouts[i].bounds.getX(), restored.bounds.getX());
            assertEquals(layouts[i].bounds.getY(), restored.bounds.getY());
            assertEquals(layouts[i].bounds.getWidth(), restored.bounds.getWidth());
            assertEquals(layouts[i].bounds.getHeight(), restored.bounds.getHeight());
        }
        assertEquals(1, settings.wireless_energy_hud.scale);
        assertEquals(-1, settings.wireless_energy_hud.x);
        assertEquals(-1, settings.wireless_energy_hud.y);
    }

    public static class Fixture {

        @Configurable
        public Client client = new Client();
    }

    public static class Client {

        @Configurable
        public Hud hud = new Hud();
    }

    public static class Hud {

        @Configurable
        public HudSettings panels = new HudSettings();
    }
}
