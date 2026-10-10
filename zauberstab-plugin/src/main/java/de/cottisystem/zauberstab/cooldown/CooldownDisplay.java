package de.cottisystem.zauberstab.cooldown;

import org.bukkit.entity.Player;
import de.cottisystem.zauberstab.model.Ability;

/**
 * Zeigt Cooldown-Informationen für den Spieler an.
 */
public class CooldownDisplay {

    /**
     * Zeigt den Cooldown in der Action-Bar an.
     */
    public static void displayCooldownBar(Player player, Ability ability, CooldownManager cooldownManager) {
        if (!cooldownManager.isOnCooldown(player, ability)) {
            return;
        }

        int remaining = cooldownManager.getRemainingCooldown(player, ability);
        String bar = createCooldownBar(remaining, ability.getMaxLevel());

        player.sendActionBar("§c" + ability.getDisplayName() + " §7" + bar);
    }

    /**
     * Zeigt den globalen Cooldown an.
     */
    public static void displayGlobalCooldown(Player player, CooldownManager cooldownManager) {
        if (!cooldownManager.isOnGlobalCooldown(player)) {
            return;
        }

        long remaining = cooldownManager.getRemainingGlobalCooldown(player);
        String bar = createProgressBar((int) remaining, 500);

        player.sendActionBar("§6Global Cooldown §7" + bar);
    }

    /**
     * Erstellt einen Cooldown-Balken.
     */
    private static String createCooldownBar(int remaining, int maxLevel) {
        StringBuilder sb = new StringBuilder();

        // Balken (10 Zeichen)
        int filled = Math.max(0, 10 - (remaining / 2));
        for (int i = 0; i < filled; i++) {
            sb.append("§a█");
        }
        for (int i = filled; i < 10; i++) {
            sb.append("§c█");
        }

        sb.append(" §7").append(remaining).append("s");

        return sb.toString();
    }

    /**
     * Erstellt einen Progress-Balken.
     */
    private static String createProgressBar(int current, int max) {
        StringBuilder sb = new StringBuilder();

        int percent = (current * 100) / max;
        int filled = percent / 10;

        for (int i = 0; i < filled; i++) {
            sb.append("§a█");
        }
        for (int i = filled; i < 10; i++) {
            sb.append("§c█");
        }

        return sb.toString();
    }

    /**
     * Gibt eine Cooldown-Nachricht aus.
     */
    public static void sendCooldownMessage(Player player, Ability ability, int remaining) {
        player.sendMessage("§c" + ability.getDisplayName() + " ist noch §e" + remaining + "s§c im Cooldown!");
    }

    /**
     * Gibt eine Cooldown-Ready-Nachricht aus.
     */
    public static void sendReadyMessage(Player player, Ability ability) {
        player.sendMessage("§a" + ability.getDisplayName() + " ist bereit!");
    }
}
