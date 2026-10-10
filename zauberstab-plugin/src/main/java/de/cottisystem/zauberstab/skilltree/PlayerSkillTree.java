package de.cottisystem.zauberstab.skilltree;


import java.util.*;

/**
 * Repräsentiert den Skill-Tree-Status eines Spielers.
 * Speichert Leveling-Fortschritt und freigeschaltete Fähigkeiten.
 */
public class PlayerSkillTree {

    // Spieler-UUID
    private String playerId;

    // Aktuelles Level
    private int level = 1;

    // Verfügbare Skill-Punkte
    private int availableSkillPoints = SkillTree.getBaseSkillPoints();

    // Map: NodeID -> Fähigkeits-Level (0 = nicht freigeschaltet)
    private Map<String, Integer> unlockedNodes = new HashMap<>();

    // Erfahrungspunkte zum nächsten Level
    private int experience = 0;
    private int experienceForNextLevel = 100;

    /**
     * Standardkonstruktor für PlayerSkillTree.
     */
    public PlayerSkillTree() {
    }

    /**
     * Konstruktor mit Spieler-ID.
     */
    public PlayerSkillTree(String playerId) {
        this.playerId = playerId;
    }

    /**
     * Gibt die Anzahl der verfügbaren Skill-Punkte zurück.
     */
    public int getAvailableSkillPoints() {
        return availableSkillPoints;
    }

    /**
     * Gibt das Level einer Fähigkeit zurück (0 = nicht freigeschaltet).
     */
    public int getAbilityLevel(String nodeId) {
        return unlockedNodes.getOrDefault(nodeId, 0);
    }

    /**
     * Prüft, ob eine Fähigkeit freigeschaltet ist.
     */
    public boolean isUnlocked(String nodeId) {
        return unlockedNodes.getOrDefault(nodeId, 0) > 0;
    }

    /**
     * Schaltet eine Fähigkeit frei.
     */
    public boolean unlockAbility(String nodeId, int cost) {
        if (availableSkillPoints < cost) {
            return false;
        }

        unlockedNodes.put(nodeId, 1);
        availableSkillPoints -= cost;
        return true;
    }

    /**
     * Levelt eine Fähigkeit auf.
     */
    public boolean levelUpAbility(String nodeId, int cost) {
        int currentLevel = getAbilityLevel(nodeId);
        if (currentLevel == 0 || availableSkillPoints < cost || currentLevel >= 5) {
            return false;
        }

        unlockedNodes.put(nodeId, currentLevel + 1);
        availableSkillPoints -= cost;
        return true;
    }

    /**
     * Fügt Erfahrung hinzu.
     */
    public boolean addExperience(int amount) {
        experience += amount;

        if (experience >= experienceForNextLevel) {
            levelUp();
            return true;
        }

        return false;
    }

    /**
     * Levelt den Spieler auf.
     */
    public void levelUp() {
        level++;
        experience = 0;
        experienceForNextLevel = (int) (100 * Math.pow(1.1, level - 1));
        availableSkillPoints += SkillTree.getSkillPointsPerLevel();
    }

    /**
     * Gibt den Fortschritt zum nächsten Level als Prozentsatz zurück.
     */
    public int getExperiencePercent() {
        if (experienceForNextLevel == 0) {
            return 0;
        }
        return (experience * 100) / experienceForNextLevel;
    }

    /**
     * Gibt die Anzahl der freigeschalteten Fähigkeiten zurück.
     */
    public int getUnlockedAbilityCount() {
        return (int) unlockedNodes.values().stream()
                .filter(level -> level > 0)
                .count();
    }

    /**
     * Gibt die durchschnittliche Fähigkeits-Level zurück.
     */
    public double getAverageAbilityLevel() {
        if (unlockedNodes.isEmpty()) {
            return 0;
        }

        int sum = unlockedNodes.values().stream()
                .filter(level -> level > 0)
                .mapToInt(Integer::intValue)
                .sum();

        return (double) sum / getUnlockedAbilityCount();
    }

    /**
     * Fügt Skill-Punkte hinzu.
     */
    public void addSkillPoints(int amount) {
        if (amount > 0) {
            availableSkillPoints += amount;
        }
    }

    // ===== GETTER UND SETTER FÜR ALLE FELDER =====
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public void setAvailableSkillPoints(int availableSkillPoints) {
        this.availableSkillPoints = Math.max(0, availableSkillPoints);
    }

    public Map<String, Integer> getUnlockedNodes() {
        return unlockedNodes;
    }

    public void setUnlockedNodes(Map<String, Integer> unlockedNodes) {
        this.unlockedNodes = unlockedNodes;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = Math.max(0, experience);
    }

    public int getExperienceForNextLevel() {
        return experienceForNextLevel;
    }

    public void setExperienceForNextLevel(int experienceForNextLevel) {
        this.experienceForNextLevel = Math.max(1, experienceForNextLevel);
    }
}
