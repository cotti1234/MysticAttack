package de.cottisystem.zauberstab.model;


/**
 * Ein einzelner Skill mit bis zu 5 Leveln.
 */
public class Skill {

    private String id;
    private String displayName;
    private String description;
    
    // Maximales Level
    private static final int MAX_LEVEL = 5;
    
    // Aktuelles Level
    private int currentLevel;
    
    // Effekt-Beschreibung pro Level
    private String[] effectDescriptions = new String[MAX_LEVEL];

    /**
     * Standardkonstruktor für Skill.
     */
    public Skill() {
    }

    /**
     * Konstruktor mit Parametern.
     */
    public Skill(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Erhöht das Level um 1.
     */
    public boolean levelUp() {
        if (currentLevel < MAX_LEVEL) {
            currentLevel++;
            return true;
        }
        return false;
    }

    /**
     * Setzt das Level zurück.
     */
    public void reset() {
        currentLevel = 0;
    }

    /**
     * Gibt die Effekt-Beschreibung für das aktuelle Level zurück.
     */
    public String getCurrentEffectDescription() {
        if (currentLevel > 0 && currentLevel <= MAX_LEVEL) {
            return effectDescriptions[currentLevel - 1];
        }
        return "Nicht freigeschaltet";
    }

    /**
     * Gibt die Effekt-Beschreibung für ein bestimmtes Level zurück.
     */
    public String getEffectDescription(int level) {
        if (level > 0 && level <= MAX_LEVEL) {
            return effectDescriptions[level - 1];
        }
        return "Nicht freigeschaltet";
    }

    /**
     * Prüft, ob der Skill freigeschaltet ist.
     */
    public boolean isUnlocked() {
        return currentLevel > 0;
    }

    /**
     * Gibt den Fortschritt als Prozentsatz zurück.
     */
    public int getProgressPercent() {
        return (currentLevel * 100) / MAX_LEVEL;
    }

    // ===== GETTER UND SETTER =====
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = Math.max(0, Math.min(currentLevel, MAX_LEVEL));
    }

    public String[] getEffectDescriptions() {
        return effectDescriptions;
    }

    public void setEffectDescriptions(String[] effectDescriptions) {
        this.effectDescriptions = effectDescriptions;
    }

    public static int getMaxLevel() {
        return MAX_LEVEL;
    }

    @Override
    public String toString() {
        return displayName + " (Level " + currentLevel + "/" + MAX_LEVEL + ")";
    }
}
