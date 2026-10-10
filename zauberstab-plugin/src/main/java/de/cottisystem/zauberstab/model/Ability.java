package de.cottisystem.zauberstab.model;


import java.util.HashSet;
import java.util.Set;

/**
 * Fähigkeit (Ability) - kann auf Stäben platziert werden.
 * Hat Level, Kosten, Element und Tags für die Kombinations-Engine.
 */
public class Ability {

    private String id;
    private String displayName;
    private String description;
    
    // Element der Fähigkeit
    private Element element;
    
    // Maximales Level
    private int maxLevel;
    
    // Kosten pro Level (Mana)
    private int[] manaCostPerLevel;
    
    // Cooldown pro Level (in Sekunden)
    private int[] cooldownPerLevel;
    
    // Tags für die Kombinations-Engine
    private Set<String> tags = new HashSet<>();
    
    // Effektstärke (Basis)
    private int basePower;

    /**
     * Standardkonstruktor für Ability.
     */
    public Ability() {
    }

    /**
     * Konstruktor mit allen Parametern.
     */
    public Ability(String id, String displayName, String description, Element element, int maxLevel,
                   int[] manaCostPerLevel, int[] cooldownPerLevel, int basePower) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.element = element;
        this.maxLevel = maxLevel;
        this.manaCostPerLevel = manaCostPerLevel;
        this.cooldownPerLevel = cooldownPerLevel;
        this.basePower = basePower;
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

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int[] getManaCostPerLevel() {
        return manaCostPerLevel;
    }

    public void setManaCostPerLevel(int[] manaCostPerLevel) {
        this.manaCostPerLevel = manaCostPerLevel;
    }

    public int[] getCooldownPerLevel() {
        return cooldownPerLevel;
    }

    public void setCooldownPerLevel(int[] cooldownPerLevel) {
        this.cooldownPerLevel = cooldownPerLevel;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public int getBasePower() {
        return basePower;
    }

    public void setBasePower(int basePower) {
        this.basePower = basePower;
    }

    /**
     * Gibt die Mana-Kosten für ein bestimmtes Level zurück.
     */
    public int getManaCost(int level) {
        if (level < 1 || level > maxLevel) {
            return 0;
        }
        return manaCostPerLevel[level - 1];
    }

    /**
     * Gibt den Cooldown für ein bestimmtes Level zurück (in Sekunden).
     */
    public int getCooldown(int level) {
        if (level < 1 || level > maxLevel) {
            return 0;
        }
        return cooldownPerLevel[level - 1];
    }

    /**
     * Prüft, ob die Fähigkeit ein bestimmtes Tag hat.
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    /**
     * Gibt alle Tags als komma-separierte Liste zurück.
     */
    public String getTagsAsString() {
        return String.join(", ", tags);
    }

    @Override
    public String toString() {
        return displayName + " (Level 1-" + maxLevel + ")";
    }
}
