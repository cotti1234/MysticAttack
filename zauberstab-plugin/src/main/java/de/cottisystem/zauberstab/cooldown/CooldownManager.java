package de.cottisystem.zauberstab.cooldown;

import org.bukkit.entity.Player;
import de.cottisystem.zauberstab.model.Ability;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Cooldowns für Spieler und Fähigkeiten.
 * Speichert Cooldown-Zeiten im RAM mit asynchronem Cleanup.
 */
public class CooldownManager {

    // Map: PlayerUUID -> (AbilityID -> Cooldown-Ende-Zeit)
    private final Map<String, Map<String, Long>> playerCooldowns = new ConcurrentHashMap<>();

    // Globaler Cooldown (in Millisekunden)
    private static final long GLOBAL_COOLDOWN = 500;

    // Map: PlayerUUID -> Globaler Cooldown-Ende-Zeit
    private final Map<String, Long> globalCooldowns = new ConcurrentHashMap<>();

    /**
     * Setzt einen Cooldown für eine Fähigkeit.
     */
    public void setCooldown(Player player, Ability ability, int cooldownSeconds) {
        String playerId = player.getUniqueId().toString();
        String abilityId = ability.getId();

        long endTime = System.currentTimeMillis() + (cooldownSeconds * 1000L);

        playerCooldowns.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .put(abilityId, endTime);
    }

    /**
     * Prüft, ob eine Fähigkeit im Cooldown ist.
     */
    public boolean isOnCooldown(Player player, Ability ability) {
        String playerId = player.getUniqueId().toString();
        String abilityId = ability.getId();

        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        if (cooldowns == null) {
            return false;
        }

        Long endTime = cooldowns.get(abilityId);
        if (endTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= endTime) {
            cooldowns.remove(abilityId);
            return false;
        }

        return true;
    }

    /**
     * Gibt die verbleibende Cooldown-Zeit in Sekunden zurück.
     */
    public int getRemainingCooldown(Player player, Ability ability) {
        String playerId = player.getUniqueId().toString();
        String abilityId = ability.getId();

        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        if (cooldowns == null) {
            return 0;
        }

        Long endTime = cooldowns.get(abilityId);
        if (endTime == null) {
            return 0;
        }

        long remaining = endTime - System.currentTimeMillis();
        if (remaining <= 0) {
            cooldowns.remove(abilityId);
            return 0;
        }

        return (int) Math.ceil(remaining / 1000.0);
    }

    /**
     * Gibt die verbleibende Cooldown-Zeit als Dezimal zurück.
     */
    public double getRemainingCooldownExact(Player player, Ability ability) {
        String playerId = player.getUniqueId().toString();
        String abilityId = ability.getId();

        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        if (cooldowns == null) {
            return 0;
        }

        Long endTime = cooldowns.get(abilityId);
        if (endTime == null) {
            return 0;
        }

        long remaining = endTime - System.currentTimeMillis();
        if (remaining <= 0) {
            cooldowns.remove(abilityId);
            return 0;
        }

        return remaining / 1000.0;
    }

    /**
     * Setzt einen globalen Cooldown für einen Spieler.
     */
    public void setGlobalCooldown(Player player) {
        String playerId = player.getUniqueId().toString();
        long endTime = System.currentTimeMillis() + GLOBAL_COOLDOWN;
        globalCooldowns.put(playerId, endTime);
    }

    /**
     * Prüft, ob ein Spieler im globalen Cooldown ist.
     */
    public boolean isOnGlobalCooldown(Player player) {
        String playerId = player.getUniqueId().toString();
        Long endTime = globalCooldowns.get(playerId);

        if (endTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= endTime) {
            globalCooldowns.remove(playerId);
            return false;
        }

        return true;
    }

    /**
     * Gibt die verbleibende globale Cooldown-Zeit in Millisekunden zurück.
     */
    public long getRemainingGlobalCooldown(Player player) {
        String playerId = player.getUniqueId().toString();
        Long endTime = globalCooldowns.get(playerId);

        if (endTime == null) {
            return 0;
        }

        long remaining = endTime - System.currentTimeMillis();
        if (remaining <= 0) {
            globalCooldowns.remove(playerId);
            return 0;
        }

        return remaining;
    }

    /**
     * Entfernt den Cooldown einer Fähigkeit.
     */
    public void removeCooldown(Player player, Ability ability) {
        String playerId = player.getUniqueId().toString();
        String abilityId = ability.getId();

        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        if (cooldowns != null) {
            cooldowns.remove(abilityId);
        }
    }

    /**
     * Entfernt alle Cooldowns eines Spielers.
     */
    public void clearAllCooldowns(Player player) {
        String playerId = player.getUniqueId().toString();
        playerCooldowns.remove(playerId);
        globalCooldowns.remove(playerId);
    }

    /**
     * Gibt die Anzahl der aktiven Cooldowns eines Spielers zurück.
     */
    public int getActiveCooldownCount(Player player) {
        String playerId = player.getUniqueId().toString();
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);

        if (cooldowns == null) {
            return 0;
        }

        // Entferne abgelaufene Cooldowns
        cooldowns.entrySet().removeIf(entry -> System.currentTimeMillis() >= entry.getValue());

        return cooldowns.size();
    }

    /**
     * Leert alle Cooldowns (z.B. beim Plugin-Shutdown).
     */
    public void clearAll() {
        playerCooldowns.clear();
        globalCooldowns.clear();
    }
}
