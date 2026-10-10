package de.cottisystem.zauberstab.mana;

import org.bukkit.entity.Player;
import de.cottisystem.zauberstab.model.Ability;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verwaltet Mana für Spieler.
 * Speichert Mana-Werte im RAM mit asynchroner Regeneration.
 */
public class ManaManager {

    // Map: PlayerUUID -> Mana-Wert
    private final Map<String, Integer> playerMana = new ConcurrentHashMap<>();

    // Map: PlayerUUID -> Max-Mana
    private final Map<String, Integer> playerMaxMana = new ConcurrentHashMap<>();

    // Map: PlayerUUID -> Letzte Regenerations-Zeit
    private final Map<String, Long> lastRegenTime = new ConcurrentHashMap<>();

    // Map: PlayerUUID -> Regenerations-Bonus (aus dem Skill-Tree)
    private final Map<String, Integer> regenBonus = new ConcurrentHashMap<>();

    // Mana-Regenerations-Werte
    private static final int BASE_MAX_MANA = 100;
    private static final int MANA_REGEN_PER_SECOND = 5;
    private static final long REGEN_TICK_MS = 1000; // 1 Sekunde

    /**
     * Initialisiert Mana für einen Spieler.
     */
    public void initializeMana(Player player) {
        String playerId = player.getUniqueId().toString();
        playerMana.put(playerId, BASE_MAX_MANA);
        playerMaxMana.put(playerId, BASE_MAX_MANA);
        lastRegenTime.put(playerId, System.currentTimeMillis());
    }

    /**
     * Gibt das aktuelle Mana eines Spielers zurück.
     */
    public int getMana(Player player) {
        String playerId = player.getUniqueId().toString();
        return playerMana.getOrDefault(playerId, BASE_MAX_MANA);
    }

    /**
     * Gibt das Max-Mana eines Spielers zurück.
     */
    public int getMaxMana(Player player) {
        String playerId = player.getUniqueId().toString();
        return playerMaxMana.getOrDefault(playerId, BASE_MAX_MANA);
    }

    /**
     * Setzt das Mana eines Spielers.
     */
    public void setMana(Player player, int mana) {
        String playerId = player.getUniqueId().toString();
        int maxMana = getMaxMana(player);
        int newMana = Math.max(0, Math.min(mana, maxMana));
        playerMana.put(playerId, newMana);
    }

    /**
     * Setzt das Max-Mana eines Spielers.
     */
    public void setMaxMana(Player player, int maxMana) {
        String playerId = player.getUniqueId().toString();
        playerMaxMana.put(playerId, Math.max(1, maxMana));

        // Stelle sicher, dass aktuelles Mana nicht über Max liegt
        int currentMana = getMana(player);
        if (currentMana > maxMana) {
            setMana(player, maxMana);
        }
    }

    /**
     * Regeneriert Mana für einen Spieler.
     */
    public void regenerateMana(Player player) {
        String playerId = player.getUniqueId().toString();
        Long lastRegen = lastRegenTime.get(playerId);

        if (lastRegen == null) {
            lastRegenTime.put(playerId, System.currentTimeMillis());
            return;
        }

        long timeSinceRegen = System.currentTimeMillis() - lastRegen;

        if (timeSinceRegen >= REGEN_TICK_MS) {
            int currentMana = getMana(player);
            int maxMana = getMaxMana(player);

            if (currentMana < maxMana) {
                int regen = MANA_REGEN_PER_SECOND + getRegenBonus(player);
                int newMana = Math.min(currentMana + regen, maxMana);
                setMana(player, newMana);
            }

            lastRegenTime.put(playerId, System.currentTimeMillis());
        }
    }

    /**
     * Verbraucht Mana für eine Fähigkeit.
     */
    public boolean consumeMana(Player player, Ability ability, int abilityLevel) {
        int manaCost = getManaCost(ability, abilityLevel);
        int currentMana = getMana(player);

        if (currentMana < manaCost) {
            return false;
        }

        setMana(player, currentMana - manaCost);
        return true;
    }

    /**
     * Gibt die Mana-Kosten für eine Fähigkeit zurück.
     */
    public int getManaCost(Ability ability, int abilityLevel) {
        int[] manaCosts = ability.getManaCostPerLevel();
        if (manaCosts == null || manaCosts.length == 0) {
            return 0;
        }

        int level = Math.max(0, Math.min(abilityLevel - 1, manaCosts.length - 1));
        return manaCosts[level];
    }

    /**
     * Gibt das Mana als Prozentsatz zurück.
     */
    public int getManaPercent(Player player) {
        int mana = getMana(player);
        int maxMana = getMaxMana(player);

        if (maxMana == 0) {
            return 0;
        }

        return (mana * 100) / maxMana;
    }

    /**
     * Prüft, ob ein Spieler genug Mana für eine Fähigkeit hat.
     */
    public boolean hasEnoughMana(Player player, Ability ability, int abilityLevel) {
        int manaCost = getManaCost(ability, abilityLevel);
        int currentMana = getMana(player);
        return currentMana >= manaCost;
    }

    /**
     * Gibt die fehlende Mana-Menge zurück.
     */
    public int getMissingMana(Player player, Ability ability, int abilityLevel) {
        int manaCost = getManaCost(ability, abilityLevel);
        int currentMana = getMana(player);
        return Math.max(0, manaCost - currentMana);
    }

    /**
     * Setzt den Regenerations-Bonus eines Spielers (aus dem Skill-Tree).
     */
    public void setRegenBonus(Player player, int bonus) {
        regenBonus.put(player.getUniqueId().toString(), Math.max(0, bonus));
    }

    /**
     * Gibt den Regenerations-Bonus eines Spielers zurück.
     */
    public int getRegenBonus(Player player) {
        return regenBonus.getOrDefault(player.getUniqueId().toString(), 0);
    }

    /**
     * Erhöht das Max-Mana um einen bestimmten Betrag.
     */
    public void increaseMana(Player player, int amount) {
        int currentMax = getMaxMana(player);
        setMaxMana(player, currentMax + amount);
    }

    /**
     * Heilt Mana (wie eine Trank-Mechanic).
     */
    public void healMana(Player player, int amount) {
        int currentMana = getMana(player);
        setMana(player, currentMana + amount);
    }

    /**
     * Entfernt einen Spieler (z.B. beim Logout).
     */
    public void removePlayer(Player player) {
        String playerId = player.getUniqueId().toString();
        playerMana.remove(playerId);
        playerMaxMana.remove(playerId);
        lastRegenTime.remove(playerId);
        regenBonus.remove(playerId);
    }

    /**
     * Leert alle Mana-Daten (z.B. beim Plugin-Shutdown).
     */
    public void clearAll() {
        playerMana.clear();
        playerMaxMana.clear();
        lastRegenTime.clear();
        regenBonus.clear();
    }
}
