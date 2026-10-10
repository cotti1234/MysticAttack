package de.cottisystem.zauberstab.skilltree;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert eine Obergruppe (Hauptzweig) im Skill-Tree.
 * Z.B. Wasser, Feuer, Eis, Blitz, Erde, Mana, Verbotene Magie.
 */
public class SkillTreeBranch {

    private final String id;
    private final String displayName;
    private final String description;
    private final Material icon;
    private final String color;
    private final List<SkillTreeNode> nodes = new ArrayList<>();

    public SkillTreeBranch(String id, String displayName, String description, Material icon, String color) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public List<SkillTreeNode> getNodes() {
        return nodes;
    }

    public void addNode(SkillTreeNode node) {
        nodes.add(node);
    }

    /**
     * Prüft, ob ein Node zu dieser Branch gehört.
     */
    public boolean containsNode(String nodeId) {
        return nodes.stream().anyMatch(node -> node.getId().equals(nodeId));
    }
}
