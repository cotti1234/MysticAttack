package de.cottisystem.zauberstab.skilltree;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * GUI für den Skill-Tree.
 * Zeigt verfügbare Fähigkeiten und Leveling-Status.
 */
public class SkillTreeGUI {

    // Titel zur eindeutigen Identifikation der GUIs im InventoryClickListener
    public static final String TITLE = "§6Skill-Tree";
    public static final String BRANCH_TITLE = "§6Skill-Tree » ";

    private static final int INVENTORY_SIZE = 54; // 6x9 Inventory
    private static final int NODES_PER_PAGE = 15; // 3 Spalten x 5 innere Reihen
    private static final Material GLASS_PANE = Material.LIGHT_GRAY_STAINED_GLASS_PANE;

    private static NamespacedKey nodeIdKey;
    private static NamespacedKey branchIdKey;
    private static NamespacedKey actionKey;

    /**
     * Initialisiert den NamespacedKey (einmalig beim Plugin-Start).
     */
    public static void init(JavaPlugin plugin) {
        nodeIdKey = new NamespacedKey(plugin, "skilltree_node_id");
        branchIdKey = new NamespacedKey(plugin, "skilltree_branch_id");
        actionKey = new NamespacedKey(plugin, "skilltree_action");
    }

    /**
     * Liest die Branch-ID aus einem GUI-Item (oder null).
     */
    public static String getBranchId(ItemStack item) {
        if (item == null || item.getItemMeta() == null || branchIdKey == null) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer()
                .get(branchIdKey, PersistentDataType.STRING);
    }

    /**
     * Liest die Aktion aus einem GUI-Item (oder null).
     */
    public static String getAction(ItemStack item) {
        if (item == null || item.getItemMeta() == null || actionKey == null) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer()
                .get(actionKey, PersistentDataType.STRING);
    }

    /**
     * Liest die Node-ID aus einem GUI-Item (oder null).
     */
    public static String getNodeId(ItemStack item) {
        if (item == null || item.getItemMeta() == null || nodeIdKey == null) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer()
                .get(nodeIdKey, PersistentDataType.STRING);
    }

    /**
     * Öffnet das Haupt-Skill-Tree-Menü (nur Obergruppen/Kategorien).
     */
    public static void openSkillTreeMenu(Player player, PlayerSkillTree playerSkillTree, SkillTree skillTree) {
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, TITLE);

        // Fülle Ränder
        fillBorders(inventory, GLASS_PANE);

        // Zeige Spieler-Info oben rechts
        inventory.setItem(6, createLevelItem(playerSkillTree));
        inventory.setItem(7, createSkillPointsItem(playerSkillTree));

        // Zeige alle Kategorien/Obergruppen
        displayBranches(inventory, playerSkillTree, skillTree);

        player.openInventory(inventory);
    }

    /**
     * Füllt die Ränder des Inventorys mit dem angegebenen Glass-Pane.
     */
    private static void fillBorders(Inventory inventory, Material paneMaterial) {
        // Obere Reihe (0-8)
        for (int i = 0; i < 9; i++) {
            if (i < 4 || i > 5) {
                inventory.setItem(i, createColoredPane(paneMaterial));
            }
        }

        // Ränder (9, 17, 26, 35, 44, 53)
        for (int row = 1; row < 6; row++) {
            inventory.setItem(row * 9, createColoredPane(paneMaterial));
            inventory.setItem(row * 9 + 8, createColoredPane(paneMaterial));
        }

        // Untere Reihe (45-53)
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, createColoredPane(paneMaterial));
        }
    }

    /**
     * Zeigt alle Obergruppen/Kategorien als Icons an.
     * Zentriertes 2-Reihen-Layout (3 oben, 4 unten).
     */
    private static void displayBranches(Inventory inventory, PlayerSkillTree playerSkillTree, SkillTree skillTree) {
        List<SkillTreeBranch> branches = skillTree.getBranches();
        int[] slots = {11, 13, 15, 19, 21, 23, 25};

        for (int i = 0; i < branches.size() && i < slots.length; i++) {
            inventory.setItem(slots[i], createBranchItem(branches.get(i), playerSkillTree));
        }
    }

    /**
     * Öffnet das Menü für eine einzelne Kategorie (Skills können hier gelevelt werden).
     */
    public static void openBranchMenu(Player player, PlayerSkillTree playerSkillTree, SkillTree skillTree, String branchId, int page) {
        SkillTreeBranch branch = skillTree.getBranch(branchId).orElse(null);
        if (branch == null) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE,
                BRANCH_TITLE + branch.getColor() + branch.getDisplayName());
        fillBorders(inventory, getBranchGlassPane(branch));

        // Spieler-Info oben mittig
        inventory.setItem(4, createBranchTitleItem(branch));
        inventory.setItem(6, createLevelItem(playerSkillTree));
        inventory.setItem(7, createSkillPointsItem(playerSkillTree));

        displayBranchNodes(inventory, playerSkillTree, skillTree, branch, page);

        // Zurück-Button zentriert unten
        inventory.setItem(49, createActionItem(Material.ARROW, "§c§lZurück", "back"));

        player.openInventory(inventory);
    }

    /**
     * Zeigt die Skills einer Kategorie auf einer Seite.
     */
    private static void displayBranchNodes(Inventory inventory, PlayerSkillTree playerSkillTree, SkillTree skillTree, SkillTreeBranch branch, int page) {
        int playerLevel = playerSkillTree.getLevel();
        Map<String, Integer> unlockedNodes = playerSkillTree.getUnlockedNodes();
        List<SkillTreeNode> availableNodes = skillTree.getAvailableNodes(playerLevel, unlockedNodes);

        List<SkillTreeNode> nodes = new ArrayList<>(branch.getNodes());
        nodes.sort(Comparator.comparingInt(SkillTreeNode::getRow)
                .thenComparingInt(SkillTreeNode::getColumn));

        int startIndex = page * NODES_PER_PAGE;
        int endIndex = Math.min(startIndex + NODES_PER_PAGE, nodes.size());

        for (int i = startIndex; i < endIndex; i++) {
            SkillTreeNode node = nodes.get(i);
            int relativeIndex = i - startIndex;
            int slot = nodeSlotForIndex(relativeIndex);
            boolean isAvailable = availableNodes.contains(node);
            inventory.setItem(slot, createNodeItem(node, playerSkillTree, isAvailable));
        }

        // Seiten-Navigation
        int maxPage = (nodes.size() - 1) / NODES_PER_PAGE;
        if (page > 0) {
            inventory.setItem(45, createActionItem(Material.ARROW, "§c§l◄ Seite " + page, "prev_page"));
        }
        if (page < maxPage) {
            inventory.setItem(53, createActionItem(Material.ARROW, "§a§lSeite " + (page + 2) + " ►", "next_page"));
        }
        if (maxPage > 0) {
            inventory.setItem(49, createActionItem(Material.PAPER, "§7Seite " + (page + 1) + "/" + (maxPage + 1), "current_page"));
        }
    }

    /**
     * Rechnet den Slot für einen Node-Index im 6x9 Grid aus.
     * 3 zentrierte Spalten (2, 4, 6) über 5 innere Reihen.
     */
    private static int nodeSlotForIndex(int index) {
        int row = index / 3;
        int col = (index % 3) * 2 + 2; // Spalten 2, 4, 6
        return (row + 1) * 9 + col;
    }

    /**
     * Gibt ein passendes farbiges Glass-Pane für einen Zweig zurück.
     */
    private static Material getBranchGlassPane(SkillTreeBranch branch) {
        return switch (branch.getId()) {
            case "water" -> Material.BLUE_STAINED_GLASS_PANE;
            case "fire" -> Material.RED_STAINED_GLASS_PANE;
            case "ice" -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            case "lightning" -> Material.YELLOW_STAINED_GLASS_PANE;
            case "earth" -> Material.GREEN_STAINED_GLASS_PANE;
            case "mana" -> Material.PURPLE_STAINED_GLASS_PANE;
            case "dark" -> Material.BLACK_STAINED_GLASS_PANE;
            default -> GLASS_PANE;
        };
    }

    /**
     * Erstellt ein farbiges Glass-Pane Item.
     */
    private static ItemStack createColoredPane(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§7 ");
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt das obere Titel-Item für ein Branch-Menü.
     */
    private static ItemStack createBranchTitleItem(SkillTreeBranch branch) {
        ItemStack item = new ItemStack(branch.getIcon());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(branch.getColor() + "§l" + branch.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7" + branch.getDescription());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt einen Action-Button (Navigation etc.).
     */
    private static ItemStack createActionItem(Material material, String displayName, String action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);
            if (actionKey != null) {
                meta.getPersistentDataContainer()
                        .set(actionKey, PersistentDataType.STRING, action);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein Level-Item.
     */
    private static ItemStack createLevelItem(PlayerSkillTree playerSkillTree) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§aLevel " + playerSkillTree.getLevel());
            List<String> lore = new ArrayList<>();
            lore.add("§7Erfahrung: §b" + playerSkillTree.getExperience() + "/" + playerSkillTree.getExperienceForNextLevel());
            
            int percent = playerSkillTree.getExperiencePercent();
            String bar = createProgressBar(percent);
            lore.add(bar);
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein Skill-Punkte-Item.
     */
    private static ItemStack createSkillPointsItem(PlayerSkillTree playerSkillTree) {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§eSkill-Punkte");
            List<String> lore = new ArrayList<>();
            lore.add("§7Verfügbar: §a" + playerSkillTree.getAvailableSkillPoints());
            lore.add("§7Freigeschaltet: §b" + playerSkillTree.getUnlockedAbilityCount());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein Knoten-Item.
     */
    private static ItemStack createNodeItem(SkillTreeNode node, PlayerSkillTree playerSkillTree, boolean isAvailable) {
        int abilityLevel = playerSkillTree.getAbilityLevel(node.getId());

        Material material;
        if (abilityLevel >= node.getMaxLevel()) {
            material = Material.GOLD_BLOCK; // Maximal gelevelt
        } else if (abilityLevel > 0) {
            material = Material.LIME_CONCRETE; // Freigeschaltet
        } else if (isAvailable) {
            material = Material.GRAY_CONCRETE; // Freischaltbar
        } else {
            material = Material.RED_CONCRETE; // Noch gesperrt
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6" + node.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7" + node.getDescription());

            if (abilityLevel == 0) {
                lore.add("§7Level erforderlich: §e" + node.getRequiredLevel());
                lore.add("§7Skill-Punkte: §a" + node.getRequiredSkillPoints());
                if (isAvailable) {
                    lore.add("");
                    lore.add("§aKlicke zum Freischalten");
                } else {
                    lore.add("§cNoch nicht verfügbar");
                    if (node.getDependencies() != null && node.getDependencies().length > 0) {
                        lore.add("§7Voraussetzungen: §e" + String.join(", ", node.getDependencies()));
                    }
                }
            } else if (abilityLevel >= node.getMaxLevel()) {
                lore.add("§aLevel: §b" + abilityLevel + "/" + node.getMaxLevel());
                lore.add("§6Maximal gelevelt!");
            } else {
                lore.add("§aLevel: §b" + abilityLevel + "/" + node.getMaxLevel());
                lore.add("§7Kosten pro Level: §a" + node.getCostPerLevel());
                lore.add("");
                lore.add("§aKlicke zum Aufleveln");
            }

            meta.setLore(lore);

            if (nodeIdKey != null) {
                meta.getPersistentDataContainer()
                        .set(nodeIdKey, PersistentDataType.STRING, node.getId());
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein Kategorie-Icon für die Skill-Tree-Hauptseite.
     */
    private static ItemStack createBranchItem(SkillTreeBranch branch, PlayerSkillTree playerSkillTree) {
        ItemStack item = new ItemStack(branch.getIcon());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            long unlocked = branch.getNodes().stream()
                    .filter(node -> playerSkillTree.getAbilityLevel(node.getId()) > 0)
                    .count();
            long total = branch.getNodes().size();

            meta.setDisplayName(branch.getColor() + "§l" + branch.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7" + branch.getDescription());
            lore.add("§7Skills: §a" + unlocked + "§7/§e" + total + " §7freigeschaltet");
            lore.add("");
            lore.add("§aKlicke, um Skills anzuzeigen");
            meta.setLore(lore);

            if (branchIdKey != null) {
                meta.getPersistentDataContainer()
                        .set(branchIdKey, PersistentDataType.STRING, branch.getId());
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein Glass Pane Item.
     */
    private static ItemStack createGlassPane() {
        ItemStack item = new ItemStack(GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§7 ");
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt einen Progress-Bar-String.
     */
    private static String createProgressBar(int percent) {
        int filled = percent / 10;
        int empty = 10 - filled;

        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append("§7");
        for (int i = 0; i < empty; i++) {
            bar.append("█");
        }
        bar.append(" §7").append(percent).append("%");

        return bar.toString();
    }
}
