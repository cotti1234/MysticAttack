package de.cottisystem.zauberstab.skilltree;


import java.util.*;

/**
 * Repräsentiert den kompletten Skill-Tree.
 * Verwaltet alle Knoten und deren Abhängigkeiten.
 */
public class SkillTree {

    // Map: NodeID -> SkillTreeNode
    private Map<String, SkillTreeNode> nodes = new HashMap<>();

    // Map: BranchID -> SkillTreeBranch
    private Map<String, SkillTreeBranch> branches = new LinkedHashMap<>();

    // Map: PlayerUUID -> PlayerSkillTree
    private Map<UUID, PlayerSkillTree> playerSkillTrees = new HashMap<>();

    // Skill-Punkte pro Level
    private static final int SKILL_POINTS_PER_LEVEL = 1;

    // Basis-Skill-Punkte beim Start (3 freie Punkte bei Erststart)
    private static final int BASE_SKILL_POINTS = 3;

    /**
     * Standardkonstruktor für SkillTree.
     */
    public SkillTree() {
    }

    /**
     * Fügt einen Branch hinzu.
     */
    public void addBranch(SkillTreeBranch branch) {
        branches.put(branch.getId(), branch);
    }

    /**
     * Gibt einen Branch zurück.
     */
    public Optional<SkillTreeBranch> getBranch(String branchId) {
        return Optional.ofNullable(branches.get(branchId));
    }

    /**
     * Gibt alle Branches in Reihenfolge zurück.
     */
    public List<SkillTreeBranch> getBranches() {
        return new ArrayList<>(branches.values());
    }

    /**
     * Fügt einen Knoten zum Tree hinzu.
     */
    public void addNode(SkillTreeNode node) {
        nodes.put(node.getId(), node);
    }

    /**
     * Fügt einen Knoten zu einem Branch und zum Tree hinzu.
     */
    public void addNode(String branchId, SkillTreeNode node) {
        addNode(node);
        SkillTreeBranch branch = branches.get(branchId);
        if (branch != null) {
            branch.addNode(node);
        }
    }

    /**
     * Gibt einen Knoten anhand der ID zurück.
     */
    public Optional<SkillTreeNode> getNode(String nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }

    /**
     * Gibt alle Knoten zurück.
     */
    public Collection<SkillTreeNode> getAllNodes() {
        return nodes.values();
    }

    /**
     * Gibt alle Knoten eines bestimmten Elements zurück.
     */
    public List<SkillTreeNode> getNodesByElement(String element) {
        return nodes.values().stream()
                .filter(node -> node.getAbilityId().contains(element))
                .toList();
    }

    /**
     * Prüft, ob alle Abhängigkeiten eines Knotens erfüllt sind.
     */
    public boolean areDependenciesMet(String nodeId, Map<String, Integer> unlockedNodes) {
        Optional<SkillTreeNode> nodeOpt = getNode(nodeId);
        if (nodeOpt.isEmpty()) {
            return false;
        }

        SkillTreeNode node = nodeOpt.get();
        if (node.getDependencies() == null || node.getDependencies().length == 0) {
            return true;
        }

        for (String depId : node.getDependencies()) {
            if (!unlockedNodes.containsKey(depId) || unlockedNodes.get(depId) == 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gibt die verfügbaren Knoten für einen Spieler zurück.
     */
    public List<SkillTreeNode> getAvailableNodes(int playerLevel, Map<String, Integer> unlockedNodes) {
        return nodes.values().stream()
                .filter(node -> node.getRequiredLevel() <= playerLevel)
                .filter(node -> areDependenciesMet(node.getId(), unlockedNodes))
                .toList();
    }

    /**
     * Berechnet die Skill-Punkte für ein bestimmtes Level.
     */
    public static int calculateSkillPoints(int level) {
        return BASE_SKILL_POINTS + ((level - 1) * SKILL_POINTS_PER_LEVEL);
    }

    /**
     * Gibt die Skill-Punkte pro Level zurück.
     */
    public static int getSkillPointsPerLevel() {
        return SKILL_POINTS_PER_LEVEL;
    }

    /**
     * Gibt die Basis-Skill-Punkte zurück.
     */
    public static int getBaseSkillPoints() {
        return BASE_SKILL_POINTS;
    }

    /**
     * Gibt den PlayerSkillTree eines Spielers zurück oder erstellt einen neuen.
     */
    public PlayerSkillTree getOrCreatePlayerSkillTree(UUID playerUUID) {
        return playerSkillTrees.computeIfAbsent(playerUUID, uuid -> new PlayerSkillTree());
    }

    /**
     * Gibt den PlayerSkillTree eines Spielers zurück.
     */
    public PlayerSkillTree getPlayerSkillTree(UUID playerUUID) {
        return playerSkillTrees.get(playerUUID);
    }

    /**
     * Speichert den PlayerSkillTree eines Spielers.
     */
    public void setPlayerSkillTree(UUID playerUUID, PlayerSkillTree skillTree) {
        playerSkillTrees.put(playerUUID, skillTree);
    }

    // ===== GETTER UND SETTER FÜR MAPS =====
    public Map<String, SkillTreeNode> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, SkillTreeNode> nodes) {
        this.nodes = nodes;
    }

    public Map<UUID, PlayerSkillTree> getPlayerSkillTrees() {
        return playerSkillTrees;
    }

    public void setPlayerSkillTrees(Map<UUID, PlayerSkillTree> playerSkillTrees) {
        this.playerSkillTrees = playerSkillTrees;
    }
}
