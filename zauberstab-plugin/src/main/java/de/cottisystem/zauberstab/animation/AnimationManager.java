package de.cottisystem.zauberstab.animation;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.model.Ability;
import de.cottisystem.zauberstab.model.Element;

/**
 * Verwaltet Animations-Effekte für Fähigkeiten.
 * Spielt Particle- und Sound-Effekte ab.
 */
public class AnimationManager {

    private final JavaPlugin plugin;

    public AnimationManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Spielt einen Fähigkeits-Cast-Effekt ab.
     */
    public void playCastEffect(Player player, Ability ability) {
        Location loc = player.getLocation().add(0, 1, 0);
        Element element = ability.getElement();

        // Wähle Effekte basierend auf Element
        ParticleEffect particleEffect = getParticleEffect(element, true);
        SoundEffect soundEffect = getSoundEffect(element, true);

        playParticleEffect(loc, particleEffect);
        playSoundEffect(player, soundEffect);
    }

    /**
     * Spielt einen Fähigkeits-Hit-Effekt ab.
     */
    public void playHitEffect(Location loc, Ability ability) {
        Element element = ability.getElement();

        ParticleEffect particleEffect = getParticleEffect(element, false);
        SoundEffect soundEffect = getSoundEffect(element, false);

        playParticleEffect(loc, particleEffect);
        playSoundEffectAtLocation(loc, soundEffect);
    }

    /**
     * Spielt einen Level-Up-Effekt ab.
     */
    public void playLevelUpEffect(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);

        // Spiele mehrere Particle-Effekte ab
        playParticleEffect(loc, ParticleEffect.LIGHTNING_SPARK);
        playParticleEffect(loc, ParticleEffect.FIRE_FLAME);

        playSoundEffect(player, SoundEffect.LEVEL_UP);
    }

    /**
     * Spielt einen Unlock-Effekt ab.
     */
    public void playUnlockEffect(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);

        playParticleEffect(loc, ParticleEffect.LIGHTNING_SPARK);
        playSoundEffect(player, SoundEffect.UNLOCK);
    }

    /**
     * Spielt einen Error-Effekt ab.
     */
    public void playErrorEffect(Player player) {
        Location loc = player.getLocation();

        playParticleEffect(loc, ParticleEffect.FIRE_SMOKE);
        playSoundEffect(player, SoundEffect.ERROR);
    }

    /**
     * Spielt einen Particle-Effekt ab.
     */
    private void playParticleEffect(Location loc, ParticleEffect effect) {
        loc.getWorld().spawnParticle(
                effect.getParticle(),
                loc,
                effect.getCount(),
                effect.getSpread(),
                effect.getSpread(),
                effect.getSpread()
        );
    }

    /**
     * Spielt einen Sound-Effekt für einen Spieler ab.
     */
    private void playSoundEffect(Player player, SoundEffect effect) {
        player.playSound(
                player.getLocation(),
                effect.getSound(),
                effect.getVolume(),
                effect.getPitch()
        );
    }

    /**
     * Spielt einen Sound-Effekt an einer Location ab.
     */
    private void playSoundEffectAtLocation(Location loc, SoundEffect effect) {
        loc.getWorld().playSound(
                loc,
                effect.getSound(),
                effect.getVolume(),
                effect.getPitch()
        );
    }

    /**
     * Gibt den Particle-Effekt für ein Element zurück.
     */
    private ParticleEffect getParticleEffect(Element element, boolean isCast) {
        String elementId = element.getId().toLowerCase();
        
        if (elementId.contains("water")) {
            return isCast ? ParticleEffect.WATER_SPLASH : ParticleEffect.WATER_DRIP;
        } else if (elementId.contains("fire")) {
            return isCast ? ParticleEffect.FIRE_FLAME : ParticleEffect.FIRE_SMOKE;
        } else if (elementId.contains("ice")) {
            return isCast ? ParticleEffect.ICE_SNOW : ParticleEffect.ICE_CRYSTAL;
        } else if (elementId.contains("lightning")) {
            return isCast ? ParticleEffect.LIGHTNING_SPARK : ParticleEffect.LIGHTNING_FLASH;
        } else if (elementId.contains("earth") || elementId.contains("stone")) {
            return isCast ? ParticleEffect.EARTH_DUST : ParticleEffect.EARTH_BLOCK;
        } else if (elementId.contains("dark") || elementId.contains("shadow")) {
            return isCast ? ParticleEffect.DARK_MIST : ParticleEffect.DARK_SOUL;
        }
        
        return ParticleEffect.LIGHTNING_SPARK;
    }

    /**
     * Gibt den Sound-Effekt für ein Element zurück.
     */
    private SoundEffect getSoundEffect(Element element, boolean isCast) {
        String elementId = element.getId().toLowerCase();
        
        if (elementId.contains("water")) {
            return isCast ? SoundEffect.WATER_CAST : SoundEffect.WATER_HIT;
        } else if (elementId.contains("fire")) {
            return isCast ? SoundEffect.FIRE_CAST : SoundEffect.FIRE_HIT;
        } else if (elementId.contains("ice")) {
            return isCast ? SoundEffect.ICE_CAST : SoundEffect.ICE_HIT;
        } else if (elementId.contains("lightning")) {
            return isCast ? SoundEffect.LIGHTNING_CAST : SoundEffect.LIGHTNING_HIT;
        } else if (elementId.contains("earth") || elementId.contains("stone")) {
            return isCast ? SoundEffect.EARTH_CAST : SoundEffect.EARTH_HIT;
        } else if (elementId.contains("dark") || elementId.contains("shadow")) {
            return isCast ? SoundEffect.DARK_CAST : SoundEffect.DARK_HIT;
        }
        
        return SoundEffect.LIGHTNING_CAST;
    }
}
