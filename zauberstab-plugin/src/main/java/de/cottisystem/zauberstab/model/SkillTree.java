package de.cottisystem.zauberstab.model;


import java.util.*;

/**
 * Skill-Tree-Struktur für Spieler-Progression.
 * Verwaltet Äste, Skills und Level.
 */
public class SkillTree {

    private UUID playerUuid;
    
    // Verfügbare Äste
    private Map<String, SkillBranch> branches = new HashMap<>();
    
    // Aktuell gewählte Äste (max. 3)
    private List<String> selectedBranches = new ArrayList<>();
    
    // Gesamte Skill-Punkte
    private int totalSkillPoints;
    
    // Verwendete Skill-Punkte
    private int usedSkillPoints;

    /**
     * Gibt die verbleibenden Skill-Punkte zurück.
     */
    public int getAvailableSkillPoints() {
        return totalSkillPoints - usedSkillPoints;
    }

    /**
     * Prüft, ob ein Ast gewählt werden kann.
     */
    public boolean canSelectBranch(String branchId) {
        return selectedBranches.size() < 3 && !selectedBranches.contains(branchId);
    }

    /**
     * Wählt einen Ast.
     */
    public boolean selectBranch(String branchId) {
        if (canSelectBranch(branchId)) {
            selectedBranches.add(branchId);
            return true;
        }
        return false;
    }

    /**
     * Deselektiert einen Ast.
     */
    public boolean deselectBranch(String branchId) {
        return selectedBranches.remove(branchId);
    }

    /**
     * Gibt einen Ast zurück.
     */
    public SkillBranch getBranch(String branchId) {
        return branches.get(branchId);
    }

    /**
     * Gibt alle gewählten Äste zurück.
     */
    public List<SkillBranch> getSelectedBranches() {
        List<SkillBranch> result = new ArrayList<>();
        for (String branchId : selectedBranches) {
            SkillBranch branch = branches.get(branchId);
            if (branch != null) {
                result.add(branch);
            }
        }
        return result;
    }

    /**
     * Gibt das Level eines Skills zurück.
     */
    public int getSkillLevel(String branchId, String skillId) {
        SkillBranch branch = branches.get(branchId);
        if (branch != null) {
            return branch.getSkillLevel(skillId);
        }
        return 0;
    }

    /**
     * Erhöht das Level eines Skills.
     */
    public boolean levelUpSkill(String branchId, String skillId) {
        if (getAvailableSkillPoints() <= 0) {
            return false;
        }

        SkillBranch branch = branches.get(branchId);
        if (branch != null && branch.levelUpSkill(skillId)) {
            usedSkillPoints++;
            return true;
        }
        return false;
    }

    /**
     * Setzt alle Skills zurück.
     */
    public void resetAllSkills() {
        for (SkillBranch branch : branches.values()) {
            branch.resetAllSkills();
        }
        usedSkillPoints = 0;
    }
}
