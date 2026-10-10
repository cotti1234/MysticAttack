package de.cottisystem.zauberstab.balance;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.model.Ability;

/**
 * Verwaltet Balancing-Werte und wendet sie auf Fähigkeiten an.
 */
public class BalanceManager {

    private final JavaPlugin plugin;
    private final BalanceConfig config;

    public BalanceManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = new BalanceConfig();
    }

    /**
     * Gibt den finalen Damage für eine Fähigkeit zurück.
     */
    public int getFinalDamage(Ability ability, int level, Entity target) {
        int baseDamage = config.getDamage(ability.getId(), level);

        // Fallback auf base_power aus abilities.yml, falls kein Balance-Eintrag existiert
        if (baseDamage <= 0) {
            baseDamage = ability.getBasePower() * level;
        }

        // Unterschiedliche Caps für PvP und PvE
        if (target instanceof Player) {
            return Math.min(baseDamage, config.getMaxPlayerDamage());
        } else {
            return Math.min(baseDamage, config.getMaxMobDamage());
        }
    }

    /**
     * Gibt den finalen Cooldown für eine Fähigkeit zurück.
     */
    public int getFinalCooldown(Ability ability, int level) {
        int cooldown = config.getCooldown(ability.getId(), level);
        if (cooldown <= 0) {
            cooldown = ability.getCooldown(level);
        }
        return cooldown;
    }

    /**
     * Gibt die finalen Mana-Kosten für eine Fähigkeit zurück.
     */
    public int getFinalManaCost(Ability ability, int level) {
        int manaCost = config.getManaCost(ability.getId(), level);
        if (manaCost <= 0) {
            manaCost = ability.getManaCost(level);
        }
        return manaCost;
    }

    /**
     * Gibt den globalen Cooldown zurück.
     */
    public int getGlobalCooldown() {
        return config.getGlobalCooldown();
    }

    /**
     * Setzt den Damage-Multiplikator.
     */
    public void setDamageMultiplier(double multiplier) {
        config.setDamageMultiplier(multiplier);
        plugin.getLogger().info("Damage Multiplier gesetzt auf: " + multiplier);
    }

    /**
     * Setzt den Cooldown-Multiplikator.
     */
    public void setCooldownMultiplier(double multiplier) {
        config.setCooldownMultiplier(multiplier);
        plugin.getLogger().info("Cooldown Multiplier gesetzt auf: " + multiplier);
    }

    /**
     * Setzt den Mana-Kosten-Multiplikator.
     */
    public void setManaCostMultiplier(double multiplier) {
        config.setManaCostMultiplier(multiplier);
        plugin.getLogger().info("Mana Cost Multiplier gesetzt auf: " + multiplier);
    }

    /**
     * Setzt den maximalen Damage gegen Spieler.
     */
    public void setMaxPlayerDamage(int damage) {
        config.setMaxPlayerDamage(damage);
        plugin.getLogger().info("Max Player Damage gesetzt auf: " + damage);
    }

    /**
     * Setzt den maximalen Damage gegen Mobs.
     */
    public void setMaxMobDamage(int damage) {
        config.setMaxMobDamage(damage);
        plugin.getLogger().info("Max Mob Damage gesetzt auf: " + damage);
    }

    /**
     * Gibt die BalanceConfig zurück.
     */
    public BalanceConfig getConfig() {
        return config;
    }

    /**
     * Gibt einen Balancing-Report aus.
     */
    public void printBalanceReport() {
        plugin.getLogger().info(config.getBalanceReport());
    }
}
