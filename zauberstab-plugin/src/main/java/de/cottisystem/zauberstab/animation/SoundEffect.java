package de.cottisystem.zauberstab.animation;

import org.bukkit.Sound;

/**
 * Vordefinierte Sound-Effekte für Fähigkeiten.
 */
public enum SoundEffect {
    // Wasser-Sounds
    WATER_CAST(Sound.BLOCK_WATER_AMBIENT, 1.0f, 1.0f),
    WATER_HIT(Sound.ENTITY_SPLASH_POTION_BREAK, 1.0f, 1.2f),
    
    // Feuer-Sounds
    FIRE_CAST(Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f),
    FIRE_HIT(Sound.ENTITY_BLAZE_HURT, 1.0f, 0.8f),
    
    // Eis-Sounds
    ICE_CAST(Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f),
    ICE_HIT(Sound.BLOCK_GLASS_BREAK, 1.0f, 1.2f),
    
    // Blitz-Sounds
    LIGHTNING_CAST(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f),
    LIGHTNING_HIT(Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 0.9f),
    
    // Erde-Sounds
    EARTH_CAST(Sound.BLOCK_GRAVEL_BREAK, 1.0f, 0.8f),
    EARTH_HIT(Sound.BLOCK_STONE_BREAK, 1.0f, 0.7f),
    
    // Verbotene Magie-Sounds
    DARK_CAST(Sound.ENTITY_WITCH_HURT, 1.0f, 0.6f),
    DARK_HIT(Sound.ENTITY_GHAST_SCREAM, 1.0f, 0.5f),
    
    // Allgemeine Sounds
    LEVEL_UP(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f),
    UNLOCK(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f),
    ERROR(Sound.BLOCK_ANVIL_LAND, 1.0f, 0.5f);

    private final Sound sound;
    private final float volume;
    private final float pitch;

    SoundEffect(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
