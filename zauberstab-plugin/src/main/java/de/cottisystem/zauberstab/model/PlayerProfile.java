package de.cottisystem.zauberstab.model;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Spieler-Profil mit allen wichtigen Daten.
 * Wird im RAM gecacht und periodisch in die DB persistiert.
 */
public class PlayerProfile {

    private UUID uuid;
    private String username;
    
    // Mana-System
    private int manaCurrent;
    private int manaMax;
    
    // Cooldowns pro Fähigkeitstyp
    private Map<String, Long> cooldowns = new HashMap<>();
    
    // Skill-Tree-Fortschritt
    private Map<String, Integer> skillLevels = new HashMap<>();

    // Leveling-Fortschritt
    private int level = 1;
    private int experience = 0;
    private int experienceForNextLevel = 100;
    private int skillPoints = 5;
    
    // Stab-Statistiken
    private Map<String, Integer> staffStats = new HashMap<>();
    
    // Trank-Ladungen
    private Map<String, Integer> potionCharges = new HashMap<>();
    
    // Zeitstempel der letzten Änderung
    private long lastUpdated;
    
    // Markiert, ob Profil ungespeicherte Änderungen hat
    private transient boolean dirty = false;

    /**
     * Standardkonstruktor für PlayerProfile.
     */
    public PlayerProfile() {
    }

    /**
     * Konstruktor mit UUID und Username.
     */
    public PlayerProfile(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Markiert das Profil als geändert.
     */
    public void markDirty() {
        this.dirty = true;
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Setzt den Cooldown für eine Fähigkeit.
     */
    public void setCooldown(String abilityType, long durationMillis) {
        cooldowns.put(abilityType, System.currentTimeMillis() + durationMillis);
        markDirty();
    }

    /**
     * Prüft, ob eine Fähigkeit noch im Cooldown ist.
     */
    public boolean isOnCooldown(String abilityType) {
        Long cooldownUntil = cooldowns.get(abilityType);
        if (cooldownUntil == null) {
            return false;
        }
        if (System.currentTimeMillis() >= cooldownUntil) {
            cooldowns.remove(abilityType);
            markDirty();
            return false;
        }
        return true;
    }

    /**
     * Gibt die verbleibende Cooldown-Zeit in Millisekunden zurück.
     */
    public long getRemainingCooldown(String abilityType) {
        Long cooldownUntil = cooldowns.get(abilityType);
        if (cooldownUntil == null) {
            return 0;
        }
        long remaining = cooldownUntil - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * Verbraucht Mana.
     */
    public boolean consumeMana(int amount) {
        if (manaCurrent >= amount) {
            manaCurrent -= amount;
            markDirty();
            return true;
        }
        return false;
    }

    /**
     * Regeneriert Mana.
     */
    public void regenerateMana(int amount) {
        manaCurrent = Math.min(manaMax, manaCurrent + amount);
        markDirty();
    }

    // Getter und Setter
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getManaCurrent() {
        return manaCurrent;
    }

    public void setManaCurrent(int manaCurrent) {
        this.manaCurrent = manaCurrent;
    }

    public int getManaMax() {
        return manaMax;
    }

    public void setManaMax(int manaMax) {
        this.manaMax = manaMax;
    }

    public Map<String, Long> getCooldowns() {
        return cooldowns;
    }

    public void setCooldowns(Map<String, Long> cooldowns) {
        this.cooldowns = cooldowns;
    }

    public Map<String, Integer> getSkillLevels() {
        return skillLevels;
    }

    public void setSkillLevels(Map<String, Integer> skillLevels) {
        this.skillLevels = skillLevels;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getExperienceForNextLevel() {
        return experienceForNextLevel;
    }

    public void setExperienceForNextLevel(int experienceForNextLevel) {
        this.experienceForNextLevel = experienceForNextLevel;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public Map<String, Integer> getStaffStats() {
        return staffStats;
    }

    public void setStaffStats(Map<String, Integer> staffStats) {
        this.staffStats = staffStats;
    }

    public Map<String, Integer> getPotionCharges() {
        return potionCharges;
    }

    public void setPotionCharges(Map<String, Integer> potionCharges) {
        this.potionCharges = potionCharges;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
