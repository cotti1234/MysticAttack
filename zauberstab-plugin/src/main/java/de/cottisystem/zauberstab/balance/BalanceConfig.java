package de.cottisystem.zauberstab.balance;


/**
 * Konfiguration für Balancing-Werte.
 * Speichert Damage, Cooldowns, Mana-Kosten und andere Balance-Parameter.
 */
public class BalanceConfig {

    // ===== DAMAGE-WERTE =====
    // Basis-Damage pro Fähigkeit und Level
    private int[] waterBlastDamage = {5, 7, 9, 11, 13};
    private int[] waterHealDamage = {8, 10, 12, 14, 16};
    private int[] fireExplosionDamage = {10, 13, 16, 19, 22};
    private int[] fireShieldDamage = {3, 4, 5, 6, 7};
    private int[] iceSpearDamage = {8, 10, 12, 14, 16};
    private int[] iceFreezeDamage = {4, 5, 6, 7, 8};
    private int[] lightningStrikeDamage = {12, 15, 18, 21, 24};
    private int[] lightningChainDamage = {8, 10, 12, 14, 16};
    private int[] earthquakeDamage = {15, 18, 21, 24, 27};
    private int[] earthWallDamage = {5, 6, 7, 8, 9};

    // ===== COOLDOWN-WERTE (in Sekunden) =====
    private int[] waterBlastCooldown = {5, 4, 3, 2, 1};
    private int[] waterHealCooldown = {8, 7, 6, 5, 4};
    private int[] fireExplosionCooldown = {6, 5, 4, 3, 2};
    private int[] fireShieldCooldown = {10, 9, 8, 7, 6};
    private int[] iceSpearCooldown = {4, 3, 2, 1, 1};
    private int[] iceFreezeCooldown = {7, 6, 5, 4, 3};
    private int[] lightningStrikeCooldown = {8, 7, 6, 5, 4};
    private int[] lightningChainCooldown = {10, 9, 8, 7, 6};
    private int[] earthquakeCooldown = {9, 8, 7, 6, 5};
    private int[] earthWallCooldown = {12, 11, 10, 9, 8};

    // ===== MANA-KOSTEN =====
    private int[] waterBlastMana = {20, 25, 30, 35, 40};
    private int[] waterHealMana = {25, 30, 35, 40, 45};
    private int[] fireExplosionMana = {30, 35, 40, 45, 50};
    private int[] fireShieldMana = {20, 25, 30, 35, 40};
    private int[] iceSpearMana = {25, 30, 35, 40, 45};
    private int[] iceFreezeMana = {20, 25, 30, 35, 40};
    private int[] lightningStrikeMana = {35, 40, 45, 50, 55};
    private int[] lightningChainMana = {40, 45, 50, 55, 60};
    private int[] earthquakeMana = {30, 35, 40, 45, 50};
    private int[] earthWallMana = {25, 30, 35, 40, 45};

    // ===== PVE/PVP BALANCE CAPS =====
    // Maximaler Damage gegen Spieler (PvP)
    private int maxPlayerDamage = 30;
    
    // Maximaler Damage gegen Mobs (PvE)
    private int maxMobDamage = 50;
    
    // Minimaler Damage
    private int minDamage = 1;

    // ===== GLOBALE MULTIPLIKATOREN =====
    // Damage-Multiplikator (1.0 = 100%)
    private double damageMultiplier = 1.0;
    
    // Cooldown-Multiplikator (1.0 = 100%)
    private double cooldownMultiplier = 1.0;
    
    // Mana-Kosten-Multiplikator (1.0 = 100%)
    private double manaCostMultiplier = 1.0;

    // ===== SPEZIELLE EFFEKTE =====
    // Knockback-Kraft für Fähigkeiten
    private double knockbackForce = 0.5;
    
    // Heilungs-Multiplikator
    private double healingMultiplier = 1.0;
    
    // Slowness-Effekt-Dauer (in Ticks)
    private int slownessDuration = 100;
    
    // Slowness-Effekt-Level
    private int slownessLevel = 1;

    // ===== GLOBALER COOLDOWN =====
    // Globaler Cooldown zwischen Fähigkeiten (in ms)
    private int globalCooldown = 500;

    /**
     * Gibt den Damage für eine Fähigkeit zurück.
     */
    public int getDamage(String abilityId, int level) {
        int[] damages = getDamageArray(abilityId);
        if (damages == null || level < 1 || level > damages.length) {
            return 0;
        }
        
        int baseDamage = damages[level - 1];
        int finalDamage = (int) (baseDamage * damageMultiplier);
        return Math.max(minDamage, Math.min(finalDamage, maxPlayerDamage));
    }

    /**
     * Gibt den Cooldown für eine Fähigkeit zurück.
     */
    public int getCooldown(String abilityId, int level) {
        int[] cooldowns = getCooldownArray(abilityId);
        if (cooldowns == null || level < 1 || level > cooldowns.length) {
            return 0;
        }
        
        int baseCooldown = cooldowns[level - 1];
        return (int) (baseCooldown * cooldownMultiplier);
    }

    /**
     * Gibt die Mana-Kosten für eine Fähigkeit zurück.
     */
    public int getManaCost(String abilityId, int level) {
        int[] manaCosts = getManaCostArray(abilityId);
        if (manaCosts == null || level < 1 || level > manaCosts.length) {
            return 0;
        }
        
        int baseCost = manaCosts[level - 1];
        return (int) (baseCost * manaCostMultiplier);
    }

    /**
     * Gibt das Damage-Array für eine Fähigkeit zurück.
     */
    private int[] getDamageArray(String abilityId) {
        return switch (abilityId.toLowerCase()) {
            case "water_blast" -> waterBlastDamage;
            case "water_heal" -> waterHealDamage;
            case "fire_burst" -> fireExplosionDamage;
            case "fire_shield" -> fireShieldDamage;
            case "ice_spike" -> iceSpearDamage;
            case "ice_freeze" -> iceFreezeDamage;
            case "lightning_strike" -> lightningStrikeDamage;
            case "lightning_chain" -> lightningChainDamage;
            case "earth_quake" -> earthquakeDamage;
            case "earth_wall" -> earthWallDamage;
            default -> null;
        };
    }

    /**
     * Gibt das Cooldown-Array für eine Fähigkeit zurück.
     */
    private int[] getCooldownArray(String abilityId) {
        return switch (abilityId.toLowerCase()) {
            case "water_blast" -> waterBlastCooldown;
            case "water_heal" -> waterHealCooldown;
            case "fire_burst" -> fireExplosionCooldown;
            case "fire_shield" -> fireShieldCooldown;
            case "ice_spike" -> iceSpearCooldown;
            case "ice_freeze" -> iceFreezeCooldown;
            case "lightning_strike" -> lightningStrikeCooldown;
            case "lightning_chain" -> lightningChainCooldown;
            case "earth_quake" -> earthquakeCooldown;
            case "earth_wall" -> earthWallCooldown;
            default -> null;
        };
    }

    /**
     * Gibt das Mana-Kosten-Array für eine Fähigkeit zurück.
     */
    private int[] getManaCostArray(String abilityId) {
        return switch (abilityId.toLowerCase()) {
            case "water_blast" -> waterBlastMana;
            case "water_heal" -> waterHealMana;
            case "fire_burst" -> fireExplosionMana;
            case "fire_shield" -> fireShieldMana;
            case "ice_spike" -> iceSpearMana;
            case "ice_freeze" -> iceFreezeMana;
            case "lightning_strike" -> lightningStrikeMana;
            case "lightning_chain" -> lightningChainMana;
            case "earth_quake" -> earthquakeMana;
            case "earth_wall" -> earthWallMana;
            default -> null;
        };
    }

    /**
     * Setzt den Damage-Multiplikator.
     */
    public void setDamageMultiplier(double multiplier) {
        this.damageMultiplier = Math.max(0.1, Math.min(multiplier, 5.0));
    }

    /**
     * Setzt den Cooldown-Multiplikator.
     */
    public void setCooldownMultiplier(double multiplier) {
        this.cooldownMultiplier = Math.max(0.1, Math.min(multiplier, 5.0));
    }

    /**
     * Setzt den Mana-Kosten-Multiplikator.
     */
    public void setManaCostMultiplier(double multiplier) {
        this.manaCostMultiplier = Math.max(0.1, Math.min(multiplier, 5.0));
    }

    /**
     * Gibt einen Balancing-Report aus.
     */
    public String getBalanceReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BALANCING REPORT ===\n");
        sb.append("Damage Multiplier: ").append(damageMultiplier).append("\n");
        sb.append("Cooldown Multiplier: ").append(cooldownMultiplier).append("\n");
        sb.append("Mana Cost Multiplier: ").append(manaCostMultiplier).append("\n");
        sb.append("Max Player Damage: ").append(maxPlayerDamage).append("\n");
        sb.append("Max Mob Damage: ").append(maxMobDamage).append("\n");
        sb.append("Global Cooldown: ").append(globalCooldown).append("ms\n");
        return sb.toString();
    }

    // ===== GETTER FÜR DAMAGE-ARRAYS =====
    public int[] getWaterBlastDamage() {
        return waterBlastDamage;
    }

    public int[] getWaterHealDamage() {
        return waterHealDamage;
    }

    public int[] getFireExplosionDamage() {
        return fireExplosionDamage;
    }

    public int[] getFireShieldDamage() {
        return fireShieldDamage;
    }

    public int[] getIceSpearDamage() {
        return iceSpearDamage;
    }

    public int[] getIceFreezeDamage() {
        return iceFreezeDamage;
    }

    public int[] getLightningStrikeDamage() {
        return lightningStrikeDamage;
    }

    public int[] getLightningChainDamage() {
        return lightningChainDamage;
    }

    public int[] getEarthquakeDamage() {
        return earthquakeDamage;
    }

    public int[] getEarthWallDamage() {
        return earthWallDamage;
    }

    // ===== GETTER FÜR COOLDOWN-ARRAYS =====
    public int[] getWaterBlastCooldown() {
        return waterBlastCooldown;
    }

    public int[] getWaterHealCooldown() {
        return waterHealCooldown;
    }

    public int[] getFireExplosionCooldown() {
        return fireExplosionCooldown;
    }

    public int[] getFireShieldCooldown() {
        return fireShieldCooldown;
    }

    public int[] getIceSpearCooldown() {
        return iceSpearCooldown;
    }

    public int[] getIceFreezeCooldown() {
        return iceFreezeCooldown;
    }

    public int[] getLightningStrikeCooldown() {
        return lightningStrikeCooldown;
    }

    public int[] getLightningChainCooldown() {
        return lightningChainCooldown;
    }

    public int[] getEarthquakeCooldown() {
        return earthquakeCooldown;
    }

    public int[] getEarthWallCooldown() {
        return earthWallCooldown;
    }

    // ===== GETTER FÜR MANA-ARRAYS =====
    public int[] getWaterBlastMana() {
        return waterBlastMana;
    }

    public int[] getWaterHealMana() {
        return waterHealMana;
    }

    public int[] getFireExplosionMana() {
        return fireExplosionMana;
    }

    public int[] getFireShieldMana() {
        return fireShieldMana;
    }

    public int[] getIceSpearMana() {
        return iceSpearMana;
    }

    public int[] getIceFreezeMana() {
        return iceFreezeMana;
    }

    public int[] getLightningStrikeMana() {
        return lightningStrikeMana;
    }

    public int[] getLightningChainMana() {
        return lightningChainMana;
    }

    public int[] getEarthquakeMana() {
        return earthquakeMana;
    }

    public int[] getEarthWallMana() {
        return earthWallMana;
    }

    // ===== GETTER FÜR GLOBALE WERTE =====
    public int getMaxPlayerDamage() {
        return maxPlayerDamage;
    }

    public void setMaxPlayerDamage(int maxPlayerDamage) {
        this.maxPlayerDamage = maxPlayerDamage;
    }

    public int getMaxMobDamage() {
        return maxMobDamage;
    }

    public void setMaxMobDamage(int maxMobDamage) {
        this.maxMobDamage = maxMobDamage;
    }

    public int getMinDamage() {
        return minDamage;
    }

    public void setMinDamage(int minDamage) {
        this.minDamage = minDamage;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public double getCooldownMultiplier() {
        return cooldownMultiplier;
    }

    public double getManaCostMultiplier() {
        return manaCostMultiplier;
    }

    public double getKnockbackForce() {
        return knockbackForce;
    }

    public void setKnockbackForce(double knockbackForce) {
        this.knockbackForce = knockbackForce;
    }

    public double getHealingMultiplier() {
        return healingMultiplier;
    }

    public void setHealingMultiplier(double healingMultiplier) {
        this.healingMultiplier = healingMultiplier;
    }

    public int getSlownessDuration() {
        return slownessDuration;
    }

    public void setSlownessDuration(int slownessDuration) {
        this.slownessDuration = slownessDuration;
    }

    public int getSlownessLevel() {
        return slownessLevel;
    }

    public void setSlownessLevel(int slownessLevel) {
        this.slownessLevel = slownessLevel;
    }

    public int getGlobalCooldown() {
        return globalCooldown;
    }

    public void setGlobalCooldown(int globalCooldown) {
        this.globalCooldown = globalCooldown;
    }
}
