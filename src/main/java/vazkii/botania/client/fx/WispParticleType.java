/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

public class WispParticleType extends ParticleType<WispParticleData> {

    public WispParticleType() {
        super(false, WispParticleData.DESERIALIZER);
    }

    @NotNull
    @Override
    public Codec<WispParticleData> codec() {
        return WispParticleData.CODEC;
    }

    public static class Factory implements ParticleProvider<WispParticleData> {

        private final SpriteSet sprite;

        public Factory(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(WispParticleData data, ClientLevel world, double x, double y, double z, double mx, double my, double mz) {
            float size = (world.random.nextFloat() * 0.5F + 0.5F) * 2.0F * data.size;
            FXWisp particle = new FXWisp(world, x, y, z, mx, my, mz, size, data.r, data.g, data.b, data.depthTest,
                    data.maxAgeMul, data.noClip, data.gravity);
            // 精灵图必须在这里指定，否则渲染时 sprite 为空会让客户端崩溃
            particle.pickSprite(sprite);
            return particle;
        }
    }

    static int rgb(float channel) {
        return Math.max(0, Math.min(255, (int) (channel * 255)));
    }
}
