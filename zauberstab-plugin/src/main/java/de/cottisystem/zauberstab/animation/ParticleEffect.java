package de.cottisystem.zauberstab.animation;

import org.bukkit.Particle;

/**
 * Vordefinierte Particle-Effekte für Fähigkeiten.
 */
public enum ParticleEffect {
    // Wasser-Effekte
    WATER_SPLASH(Particle.SPLASH, 20, 0.5),
    WATER_DRIP(Particle.DRIPPING_WATER, 15, 0.3),
    
    // Feuer-Effekte
    FIRE_FLAME(Particle.FLAME, 25, 0.7),
    FIRE_SMOKE(Particle.SMOKE, 20, 0.5),
    
    // Eis-Effekte
    ICE_SNOW(Particle.SNOWFLAKE, 20, 0.4),
    ICE_CRYSTAL(Particle.ITEM_SNOWBALL, 15, 0.3),
    
    // Blitz-Effekte
    LIGHTNING_SPARK(Particle.ELECTRIC_SPARK, 30, 0.8),
    LIGHTNING_FLASH(Particle.FLASH, 10, 0.2),
    
    // Erde-Effekte
    EARTH_DUST(Particle.WHITE_ASH, 25, 0.6),
    EARTH_BLOCK(Particle.ASH, 20, 0.5),
    
    // Verbotene Magie
    DARK_MIST(Particle.SMOKE, 25, 0.6),
    DARK_SOUL(Particle.WITCH, 20, 0.5);

    private final Particle particle;
    private final int count;
    private final double spread;

    ParticleEffect(Particle particle, int count, double spread) {
        this.particle = particle;
        this.count = count;
        this.spread = spread;
    }

    public Particle getParticle() {
        return particle;
    }

    public int getCount() {
        return count;
    }

    public double getSpread() {
        return spread;
    }
}
