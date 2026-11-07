package vazkii.botania.client.fx;

import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 复刻内容的粒子类型与客户端工厂。
 */
public final class BotaniaParticles {

    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister
            .create(ForgeRegistries.PARTICLE_TYPES, "botania");

    public static final RegistryObject<ParticleType<WispParticleData>> WISP = PARTICLES
            .register("wisp", WispParticleType::new);
    public static final RegistryObject<ParticleType<SparkleParticleData>> SPARKLE = PARTICLES
            .register("sparkle", SparkleParticleType::new);

    private BotaniaParticles() {}

    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }

    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(SPARKLE.get(), SparkleParticleType.Factory::new);
        event.registerSpriteSet(WISP.get(), WispParticleType.Factory::new);
    }
}
