package com.gtocore.client;

import com.gtocore.common.weather.WeatherSystem;
import com.gtocore.common.weather.WeatherTypes;

import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "gtocore", value = Dist.CLIENT)
public final class WeatherClient {

    private static final BlockPos.MutableBlockPos PARTICLE_POS = new BlockPos.MutableBlockPos();

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var player = minecraft.player;
        if (level == null || player == null || minecraft.isPaused()) return;
        if (ILevel.getCapability(level, WeatherSystem.CLIENT_WEATHER) != WeatherTypes.DUST_STORM) return;
        var random = level.random;
        for (int i = 0; i < 8; i++) {
            double x = player.getX() + random.nextDouble() * 24 - 12;
            double y = player.getY() + random.nextDouble() * 8;
            double z = player.getZ() + random.nextDouble() * 24 - 12;
            if (level.canSeeSky(PARTICLE_POS.set(x, y, z))) level.addParticle(ParticleTypes.ASH, x, y, z, 0.2, -0.02, 0.05);
        }
    }

    @SubscribeEvent
    public static void fog(ViewportEvent.ComputeFogColor event) {
        var level = Minecraft.getInstance().level;
        if (level == null || ILevel.getCapability(level, WeatherSystem.CLIENT_WEATHER) != WeatherTypes.DUST_STORM) return;
        if (!level.canSeeSky(event.getCamera().getBlockPosition())) return;
        event.setRed(event.getRed() * 0.4F + 0.42F);
        event.setGreen(event.getGreen() * 0.4F + 0.24F);
        event.setBlue(event.getBlue() * 0.4F + 0.12F);
    }

    private WeatherClient() {}
}
