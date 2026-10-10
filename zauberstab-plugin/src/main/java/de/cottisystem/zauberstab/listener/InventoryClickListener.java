package de.cottisystem.zauberstab.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import de.cottisystem.zauberstab.ZauberstabPlugin;
import de.cottisystem.zauberstab.crafting.ForgeGUI;
import de.cottisystem.zauberstab.crafting.ForgeMachine;
import de.cottisystem.zauberstab.gui.AbilitySelectGUI;
import de.cottisystem.zauberstab.gui.StaffMenuGUI;
import de.cottisystem.zauberstab.model.Ability;
import de.cottisystem.zauberstab.model.Staff;
import de.cottisystem.zauberstab.skilltree.PlayerSkillTree;
import de.cottisystem.zauberstab.skilltree.SkillTreeGUI;
import de.cottisystem.zauberstab.skilltree.SkillTreeNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener für Inventory-Klicks.
 * Verwaltet Interaktionen mit GUI-Inventories (Stab-Menü, Fähigkeits-Auswahl, etc.).
 */
public class InventoryClickListener implements Listener {

    private final ZauberstabPlugin plugin;

    // Merkt sich, für welchen Stab-Slot ein Spieler gerade eine Fähigkeit auswählt
    private final Map<UUID, Integer> pendingSlotSelection = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> pendingAbilitySelectPage = new ConcurrentHashMap<>();

    public InventoryClickListener(ZauberstabPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Prüfe, ob es ein Zauberstab-GUI ist
        if (!isPluginGui(title)) {
            return;
        }

        // Verhindere Standard-Inventory-Bewegungen in GUI
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        // Schließen-Button (Barrier)
        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Stab-Menü: Klick auf Fähigkeits-Slots
        if (title.startsWith(StaffMenuGUI.TITLE_PREFIX)) {
            handleStaffMenuClick(player, event);
            return;
        }

        // Fähigkeits-Auswahl: Klick auf eine Fähigkeit oder Zurück
        if (title.startsWith(AbilitySelectGUI.TITLE)) {
            handleAbilitySelectClick(player, clickedItem);
            return;
        }

        // Skill-Tree: Klick auf einen Knoten (Freischalten/Aufleveln)
        if (title.equals(SkillTreeGUI.TITLE) || title.startsWith(SkillTreeGUI.BRANCH_TITLE)) {
            handleSkillTreeClick(player, clickedItem);
            return;
        }

        // Schmiede: Klick auf den Upgrade-Button
        if (title.equals(ForgeGUI.TITLE)) {
            handleForgeClick(player, clickedItem);
        }
    }

    /**
     * Behandelt Klicks im Skill-Tree-Hauptmenü oder Sub-Menü.
     * Klick auf einen Knoten schaltet ihn frei oder levelt ihn auf (kostet Skill-Punkte).
     */
    private void handleSkillTreeClick(Player player, ItemStack clickedItem) {
        PlayerSkillTree playerSkillTree = plugin.getSkillTree().getOrCreatePlayerSkillTree(player.getUniqueId());

        // Aktionen (Zurück, Seite wechseln)
        String action = SkillTreeGUI.getAction(clickedItem);
        if (action != null) {
            switch (action) {
                case "back" -> SkillTreeGUI.openSkillTreeMenu(player, playerSkillTree, plugin.getSkillTree());
                case "prev_page" -> changeBranchPage(player, -1);
                case "next_page" -> changeBranchPage(player, 1);
            }
            return;
        }

        // Kategorie öffnen
        String branchId = SkillTreeGUI.getBranchId(clickedItem);
        if (branchId != null) {
            setSkillTreeBranch(player, branchId);
            SkillTreeGUI.openBranchMenu(player, playerSkillTree, plugin.getSkillTree(), branchId, 0);
            return;
        }

        // Skill-Knoten freischalten / leveln
        String nodeId = SkillTreeGUI.getNodeId(clickedItem);
        if (nodeId == null) {
            return;
        }

        Optional<SkillTreeNode> nodeOpt = plugin.getSkillTree().getNode(nodeId);
        if (nodeOpt.isEmpty()) {
            return;
        }
        SkillTreeNode node = nodeOpt.get();

        int currentLevel = playerSkillTree.getAbilityLevel(nodeId);

        if (currentLevel >= node.getMaxLevel()) {
            player.sendMessage("§6" + node.getDisplayName() + " §7ist bereits maximal gelevelt!");
            return;
        }

        if (currentLevel == 0) {
            if (playerSkillTree.getLevel() < node.getRequiredLevel()) {
                player.sendMessage("§cDu benötigst Level " + node.getRequiredLevel() + " für §6" + node.getDisplayName() + "§c!");
                return;
            }
            if (!plugin.getSkillTree().areDependenciesMet(nodeId, playerSkillTree.getUnlockedNodes())) {
                player.sendMessage("§cDu musst zuerst die Voraussetzungen freischalten!");
                return;
            }
            if (!playerSkillTree.unlockAbility(nodeId, node.getRequiredSkillPoints())) {
                player.sendMessage("§cNicht genug Skill-Punkte! §7(benötigt: " + node.getRequiredSkillPoints() + ")");
                return;
            }
            player.sendMessage("§a✓ §6" + node.getDisplayName() + " §afreigeschaltet!");
            plugin.getAnimationManager().playUnlockEffect(player);
        } else {
            if (!playerSkillTree.levelUpAbility(nodeId, node.getCostPerLevel())) {
                player.sendMessage("§cNicht genug Skill-Punkte! §7(benötigt: " + node.getCostPerLevel() + ")");
                return;
            }
            player.sendMessage("§a✓ §6" + node.getDisplayName() + " §aauf Level §b"
                    + playerSkillTree.getAbilityLevel(nodeId) + "§a gelevelt!");
        }

        // Passive Boni sofort anwenden
        plugin.applySkillTreeBonuses(player);

        // Sub-Menü neu öffnen
        String currentBranch = getSkillTreeBranch(player);
        int currentPage = getSkillTreeBranchPage(player);
        SkillTreeGUI.openBranchMenu(player, playerSkillTree, plugin.getSkillTree(), currentBranch, currentPage);
    }

    /**
     * Merkt sich die aktuell geöffnete Skill-Tree-Kategorie und Seite.
     */
    private final Map<UUID, String> skillTreeBranch = new HashMap<>();
    private final Map<UUID, Integer> skillTreeBranchPage = new HashMap<>();

    private String getSkillTreeBranch(Player player) {
        return skillTreeBranch.getOrDefault(player.getUniqueId(), "");
    }

    private void setSkillTreeBranch(Player player, String branch) {
        skillTreeBranch.put(player.getUniqueId(), branch);
    }

    private int getSkillTreeBranchPage(Player player) {
        return skillTreeBranchPage.getOrDefault(player.getUniqueId(), 0);
    }

    private void setSkillTreeBranchPage(Player player, int page) {
        skillTreeBranchPage.put(player.getUniqueId(), page);
    }

    private void changeBranchPage(Player player, int delta) {
        String branch = getSkillTreeBranch(player);
        if (branch.isEmpty()) return;
        int newPage = Math.max(0, getSkillTreeBranchPage(player) + delta);
        setSkillTreeBranchPage(player, newPage);
        SkillTreeGUI.openBranchMenu(player,
                plugin.getSkillTree().getOrCreatePlayerSkillTree(player.getUniqueId()),
                plugin.getSkillTree(), branch, newPage);
    }

    /**
     * Behandelt Klicks in der Schmiede.
     * Der Upgrade-Button levelt die erste nicht maximal gelevelte Fähigkeit des
     * gehaltenen Stabs (mit Erfolgsquote; Fehlschlag kostet Durability).
     */
    private void handleForgeClick(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() != Material.LIME_CONCRETE) {
            return;
        }

        Optional<Staff> staffOpt = plugin.getStaffManager().getPlayerStaff(player);
        if (staffOpt.isEmpty()) {
            player.sendMessage("§cDu musst einen Zauberstab in der Hand halten!");
            return;
        }
        Staff staff = staffOpt.get();

        PlayerSkillTree playerSkillTree =
                plugin.getSkillTree().getOrCreatePlayerSkillTree(player.getUniqueId());

        for (Ability ability : staff.getAbilities()) {
            String nodeId = ability.getId() + "_node";
            Optional<SkillTreeNode> nodeOpt = plugin.getSkillTree().getNode(nodeId);
            if (nodeOpt.isEmpty()) {
                continue;
            }
            SkillTreeNode node = nodeOpt.get();
            int currentLevel = playerSkillTree.getAbilityLevel(nodeId);
            if (currentLevel >= node.getMaxLevel()) {
                continue;
            }

            int cost = currentLevel == 0 ? node.getRequiredSkillPoints() : node.getCostPerLevel();
            if (playerSkillTree.getAvailableSkillPoints() < cost) {
                player.sendMessage("§cNicht genug Skill-Punkte! §7(benötigt: " + cost + ")");
                return;
            }

            if (!ForgeMachine.attemptUpgrade()) {
                playerSkillTree.setAvailableSkillPoints(playerSkillTree.getAvailableSkillPoints() - cost);
                ItemStack staffItem = player.getInventory().getItemInMainHand();
                plugin.getStaffManager().reduceDurability(staffItem, ForgeMachine.getFailureDurabilityLoss());
                player.sendMessage("§c✗ Upgrade fehlgeschlagen! §7(-" + ForgeMachine.getFailureDurabilityLoss() + "% Durability)");
                plugin.getAnimationManager().playErrorEffect(player);
                return;
            }

            boolean success = currentLevel == 0
                    ? playerSkillTree.unlockAbility(nodeId, cost)
                    : playerSkillTree.levelUpAbility(nodeId, cost);

            if (success) {
                player.sendMessage("§a✓ §b" + ability.getDisplayName() + "§a auf Level §b"
                        + playerSkillTree.getAbilityLevel(nodeId) + "§a geschmiedet!");
                plugin.getAnimationManager().playUnlockEffect(player);
                plugin.applySkillTreeBonuses(player);
            }
            return;
        }

        player.sendMessage("§7Alle Fähigkeiten deines Stabs sind bereits maximal gelevelt!");
    }

    /**
     * Behandelt Klicks im Stab-Menü.
     * Klick auf einen Fähigkeits-Slot öffnet die Fähigkeits-Auswahl.
     * Shift-Klick auf eine belegte Fähigkeit entfernt sie.
     */
    private void handleStaffMenuClick(Player player, InventoryClickEvent event) {
        int staffSlot = guiSlotToStaffSlot(event.getRawSlot());
        if (staffSlot < 0) {
            return;
        }

        Optional<Staff> staffOpt = plugin.getStaffManager().getPlayerStaff(player);
        if (staffOpt.isEmpty()) {
            player.sendMessage("§cDu musst den Stab in der Hand halten!");
            player.closeInventory();
            return;
        }

        Staff staff = staffOpt.get();
        if (staffSlot >= staff.getSlotCount()) {
            return;
        }

        // Shift-Klick auf belegten Slot: Fähigkeit entfernen
        if (event.isShiftClick() && staffSlot < staff.getAbilities().size()) {
            ItemStack staffItem = player.getInventory().getItemInMainHand();
            Ability removed = staff.getAbilities().get(staffSlot);
            plugin.getStaffManager().removeAbilityAtSlot(staff, staffItem, staffSlot);
            player.sendMessage("§c✗ Fähigkeit entfernt: §7" + removed.getDisplayName());
            StaffMenuGUI.openStaffMenu(player, staff);
            return;
        }

        // Normaler Klick: Fähigkeits-Auswahl öffnen
        pendingSlotSelection.put(player.getUniqueId(), staffSlot);
        pendingAbilitySelectPage.put(player.getUniqueId(), 0);
        AbilitySelectGUI.openSelectMenu(player, plugin, staffSlot, 0);
    }

    /**
     * Behandelt Klicks in der Fähigkeits-Auswahl.
     */
    private void handleAbilitySelectClick(Player player, ItemStack clickedItem) {
        Optional<Staff> staffOpt = plugin.getStaffManager().getPlayerStaff(player);
        if (staffOpt.isEmpty()) {
            player.sendMessage("§cDu musst den Stab in der Hand halten!");
            player.closeInventory();
            return;
        }
        Staff staff = staffOpt.get();

        // Navigations-Button (Zurück / Seiten)
        String action = AbilitySelectGUI.getAction(clickedItem);
        if (action != null) {
            Integer targetSlot = pendingSlotSelection.get(player.getUniqueId());
            if (targetSlot == null) {
                pendingSlotSelection.remove(player.getUniqueId());
                pendingAbilitySelectPage.remove(player.getUniqueId());
                StaffMenuGUI.openStaffMenu(player, staff);
                return;
            }

            if (action.equals("back")) {
                pendingSlotSelection.remove(player.getUniqueId());
                pendingAbilitySelectPage.remove(player.getUniqueId());
                StaffMenuGUI.openStaffMenu(player, staff);
                return;
            }

            int currentPage = pendingAbilitySelectPage.getOrDefault(player.getUniqueId(), 0);
            int newPage = currentPage;
            if (action.equals("prev_page")) {
                newPage = Math.max(0, currentPage - 1);
            } else if (action.equals("next_page")) {
                newPage = currentPage + 1;
            }

            pendingAbilitySelectPage.put(player.getUniqueId(), newPage);
            AbilitySelectGUI.openSelectMenu(player, plugin, targetSlot, newPage);
            return;
        }

        // Fähigkeits-Item
        String abilityId = AbilitySelectGUI.getAbilityId(clickedItem);
        if (abilityId == null) {
            return;
        }

        Integer targetSlot = pendingSlotSelection.remove(player.getUniqueId());
        pendingAbilitySelectPage.remove(player.getUniqueId());
        if (targetSlot == null) {
            player.closeInventory();
            return;
        }

        Optional<Ability> abilityOpt = plugin.getAbilityLoader().getAbility(abilityId);
        if (abilityOpt.isEmpty()) {
            player.sendMessage("§cFähigkeit nicht gefunden: " + abilityId);
            return;
        }

        ItemStack staffItem = player.getInventory().getItemInMainHand();
        boolean success = plugin.getStaffManager().setAbilityAtSlot(staff, staffItem, targetSlot, abilityOpt.get());

        if (success) {
            player.sendMessage("§a✓ §b" + abilityOpt.get().getDisplayName() + "§a auf Slot " + (targetSlot + 1) + " gelegt!");
            plugin.getAnimationManager().playUnlockEffect(player);
        } else {
            player.sendMessage("§cSlot " + (targetSlot + 1) + " konnte nicht belegt werden!");
        }

        StaffMenuGUI.openStaffMenu(player, staff);
    }

    /**
     * Wandelt einen GUI-Inventory-Slot in einen Stab-Slot-Index um.
     * Slots 19-21 und 23-25 (22 ist der Schließen-Button).
     */
    private int guiSlotToStaffSlot(int guiSlot) {
        return switch (guiSlot) {
            case 19 -> 0;
            case 20 -> 1;
            case 21 -> 2;
            case 23 -> 3;
            case 24 -> 4;
            case 25 -> 5;
            default -> -1;
        };
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Verhindere Drag-Aktionen in Zauberstab-GUIs
        if (isPluginGui(event.getView().getTitle())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prüft, ob ein Inventory-Titel zu einem Zauberstab-GUI gehört.
     */
    private boolean isPluginGui(String title) {
        return title.startsWith(StaffMenuGUI.TITLE_PREFIX)
                || title.equals(SkillTreeGUI.TITLE)
                || title.startsWith(SkillTreeGUI.BRANCH_TITLE)
                || title.equals(ForgeGUI.TITLE)
                || title.startsWith(AbilitySelectGUI.TITLE);
    }
}
