package de.cottisystem.zauberstab.model;


import java.util.*;

/**
 * Ein Ast im Skill-Tree mit 5 Skills à 5 Level.
 */
public class SkillBranch {

    private String id;
    private String displayName;
    private String description;
    
    // Skills in diesem Ast (max. 5)
    private Map<String, Skill> skills = new HashMap<>();
    
    // Ist dieser Ast versteckt? (z.B. "Die verborgene Kraft")
    private boolean hidden;
    
    // Freischaltbedingung (z.B. "Level 3 in 2 anderen Ästen")
    private String unlockCondition;

    public SkillBranch(String id, String displayName, String description, boolean hidden, String unlockCondition) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.hidden = hidden;
        this.unlockCondition = unlockCondition;
    }

    // Getter und Setter
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

    public Map<String, Skill> getSkills() {
        return skills;
    }

    public void setSkills(Map<String, Skill> skills) {
        this.skills = skills;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getUnlockCondition() {
        return unlockCondition;
    }

    public void setUnlockCondition(String unlockCondition) {
        this.unlockCondition = unlockCondition;
    }

    /**
     * Gibt ein Skill-Objekt zurück.
     */
    public Skill getSkill(String skillId) {
        return skills.get(skillId);
    }

    /**
     * Gibt das Level eines Skills zurück.
     */
    public int getSkillLevel(String skillId) {
        Skill skill = skills.get(skillId);
        if (skill != null) {
            return skill.getCurrentLevel();
        }
        return 0;
    }

    /**
     * Erhöht das Level eines Skills.
     */
    public boolean levelUpSkill(String skillId) {
        Skill skill = skills.get(skillId);
        if (skill != null) {
            return skill.levelUp();
        }
        return false;
    }

    /**
     * Setzt alle Skills in diesem Ast zurück.
     */
    public void resetAllSkills() {
        for (Skill skill : skills.values()) {
            skill.reset();
        }
    }

    /**
     * Gibt die Gesamtzahl der Skill-Punkte in diesem Ast zurück.
     */
    public int getTotalSkillPoints() {
        int total = 0;
        for (Skill skill : skills.values()) {
            total += skill.getCurrentLevel();
        }
        return total;
    }

    @Override
    public String toString() {
        return displayName + (hidden ? " (versteckt)" : "");
    }
}
