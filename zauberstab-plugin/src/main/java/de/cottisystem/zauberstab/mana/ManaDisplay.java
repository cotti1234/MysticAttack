package de.cottisystem.zauberstab.mana;

import org.bukkit.entity.Player;
import de.cottisystem.zauberstab.model.Ability;

/**
 * Zeigt Mana-Informationen für den Spieler an.
 */
public class ManaDisplay {

    /**
     * Zeigt den Mana-Balken in der Action-Bar an.
     */
    public static void displayManaBar(Player player, ManaManager manaManager) {
        int mana = manaManager.getMana(player);
        int maxMana = manaManager.getMaxMana(player);
        int percent = manaManager.getManaPercent(player);

        String bar = createManaBar(percent);
        player.sendActionBar("§bMana §7" + bar + " §b" + mana + "/" + maxMana);
    }

    /**
     * Zeigt den Mana-Balken mit Fähigkeits-Info an.
     */
    public static void displayManaBarWithAbility(Player player, Ability ability, int abilityLevel, ManaManager manaManager) {
        int mana = manaManager.getMana(player);
        int maxMana = manaManager.getMaxMana(player);
        int manaCost = manaManager.getManaCost(ability, abilityLevel);
        int percent = manaManager.getManaPercent(player);

        String bar = createManaBar(percent);
        String costColor = manaManager.hasEnoughMana(player, ability, abilityLevel) ? "§a" : "§c";

        player.sendActionBar("§bMana §7" + bar + " §b" + mana + "/" + maxMana + " §7| " + costColor + manaCost + "§7 Kosten");
    }

    /**
     * Erstellt einen Mana-Balken.
     */
    private static String createManaBar(int percent) {
        StringBuilder sb = new StringBuilder();

        int filled = percent / 10;
        for (int i = 0; i < filled; i++) {
            sb.append("§b█");
        }
        for (int i = filled; i < 10; i++) {
            sb.append("§7█");
        }

        return sb.toString();
    }

    /**
     * Gibt eine Mana-Nachricht aus.
     */
    public static void sendManaMessage(Player player, Ability ability, int abilityLevel, ManaManager manaManager) {
        int manaCost = manaManager.getManaCost(ability, abilityLevel);
        int currentMana = manaManager.getMana(player);

        if (currentMana < manaCost) {
            int missing = manaManager.getMissingMana(player, ability, abilityLevel);
            player.sendMessage("§cNicht genug Mana! Du brauchst noch §e" + missing + "§c Mana.");
        } else {
            player.sendMessage("§a" + ability.getDisplayName() + " kostet §b" + manaCost + "§a Mana.");
        }
    }

    /**
     * Gibt eine Mana-Verbrauch-Nachricht aus.
     */
    public static void sendManaConsumedMessage(Player player, Ability ability, int manaCost) {
        player.sendMessage("§b-" + manaCost + " Mana §7(§b" + ability.getDisplayName() + "§7)");
    }

    /**
     * Gibt eine Mana-Regenerations-Nachricht aus.
     */
    public static void sendManaRegenMessage(Player player, int amount) {
        player.sendMessage("§a+" + amount + " Mana");
    }

    /**
     * Zeigt Mana-Informationen in der Action-Bar an (vereinfacht).
     */
    public static void displaySimpleManaBar(Player player, ManaManager manaManager) {
        int percent = manaManager.getManaPercent(player);
        String color = percent >= 50 ? "§a" : percent >= 25 ? "§e" : "§c";
        String bar = createManaBar(percent);

        player.sendActionBar(color + "Mana §7" + bar + " " + color + percent + "%");
    }
}
