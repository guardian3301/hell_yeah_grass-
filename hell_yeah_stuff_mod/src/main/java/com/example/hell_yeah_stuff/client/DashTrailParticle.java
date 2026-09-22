package com.example.hell_yeah_stuff.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Частица следа зачарования «Рывок» — ванильные спрайты дыма
 * раздатчика (minecraft:generic_0..7), подкрашенные в бело-серый;
 * кадры проигрываются по мере старения.
 *
 * <p>Рендер — обычный, ванильный: квад всегда развёрнут к камере
 * ({@link TextureSheetParticle#render}), никаких фиксированных
 * плоскостей и разворотов.
 */
public class DashTrailParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected DashTrailParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.lifetime = 8 + this.random.nextInt(4);
        this.gravity = 0.0F;
        this.friction = 0.9F;
        this.hasPhysics = false;
        this.quadSize = 0.18F + this.random.nextFloat() * 0.06F;
        // Цвет: белый с лёгким серым оттенком (спрайты generic_0..7
        // серые, как у дыма раздатчика, поэтому подсвечиваем их почти до белого).
        float grey = 0.90F + this.random.nextFloat() * 0.10F;
        this.setColor(grey, grey, Math.min(1.0F, grey * 1.02F));
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.sprites);
            // Плавное растворение и лёгкое уменьшение к концу жизни.
            this.alpha = 1.0F - (float) this.age / this.lifetime;
            this.quadSize *= 0.97F;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new DashTrailParticle(level, x, y, z, this.sprites);
        }
    }
}
