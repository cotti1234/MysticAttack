package de.cottisystem.zauberstab.skilltree;


/**
 * Repräsentiert einen Knoten im Skill-Tree.
 * Enthält Informationen über eine Fähigkeit und deren Freischaltung.
 */
public class SkillTreeNode {

    // Eindeutige ID des Knotens
    private String id;

    // Fähigkeits-ID
    private String abilityId;

    // Anzeigename
    private String displayName;

    // Beschreibung
    private String description;

    // Erforderliche Level zum Freischalten
    private int requiredLevel;

    // Erforderliche Skill-Punkte zum Freischalten
    private int requiredSkillPoints;

    // Kosten in Skill-Punkten pro Level
    private int costPerLevel;

    // Maximales Level für diese Fähigkeit
    private int maxLevel;

    // Abhängigkeiten (IDs von Knoten, die zuerst freigeschaltet sein müssen)
    private String[] dependencies;

    // Position im Tree (für GUI)
    private int row;
    private int column;

    public SkillTreeNode(String id, String abilityId, String displayName, String description,
                         int requiredLevel, int requiredSkillPoints, int costPerLevel, int maxLevel,
                         String[] dependencies, int row, int column) {
        this.id = id;
        this.abilityId = abilityId;
        this.displayName = displayName;
        this.description = description;
        this.requiredLevel = requiredLevel;
        this.requiredSkillPoints = requiredSkillPoints;
        this.costPerLevel = costPerLevel;
        this.maxLevel = maxLevel;
        this.dependencies = dependencies;
        this.row = row;
        this.column = column;
    }

    // Getter und Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAbilityId() {
        return abilityId;
    }

    public void setAbilityId(String abilityId) {
        this.abilityId = abilityId;
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

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public int getRequiredSkillPoints() {
        return requiredSkillPoints;
    }

    public void setRequiredSkillPoints(int requiredSkillPoints) {
        this.requiredSkillPoints = requiredSkillPoints;
    }

    public int getCostPerLevel() {
        return costPerLevel;
    }

    public void setCostPerLevel(int costPerLevel) {
        this.costPerLevel = costPerLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public String[] getDependencies() {
        return dependencies;
    }

    public void setDependencies(String[] dependencies) {
        this.dependencies = dependencies;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * Prüft, ob dieser Knoten freigeschaltet werden kann.
     */
    public boolean canUnlock(int playerLevel, int availableSkillPoints) {
        return playerLevel >= requiredLevel && availableSkillPoints >= requiredSkillPoints;
    }

    /**
     * Prüft, ob dieser Knoten gelevelt werden kann.
     */
    public boolean canLevelUp(int currentLevel, int availableSkillPoints) {
        return currentLevel < maxLevel && availableSkillPoints >= costPerLevel;
    }

    /**
     * Gibt die Gesamtkosten für ein bestimmtes Level zurück.
     */
    public int getTotalCostForLevel(int level) {
        if (level == 0) {
            return requiredSkillPoints;
        }
        return requiredSkillPoints + (level * costPerLevel);
    }
}
