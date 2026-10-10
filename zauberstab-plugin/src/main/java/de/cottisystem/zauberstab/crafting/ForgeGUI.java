package de.cottisystem.zauberstab.crafting;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI für die Fähigkeits-Schmiede (Forge).
 * Zeigt Upgrade-Optionen und Erfolgsquoten.
 */
public class ForgeGUI {

    // Titel zur eindeutigen Identifikation des GUIs im InventoryClickListener
    public static final String TITLE = "§6Fähigkeits-Schmiede";

    private static final int INVENTORY_SIZE = 27; // 3x9 Inventory
    private static final Material GLASS_PANE = Material.LIGHT_GRAY_STAINED_GLASS_PANE;

    /**
     * Öffnet das Forge-Menü für einen Spieler.
     */
    public static void openForgeMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, TITLE);

        // Fülle Ränder
        fillBorders(inventory);

        // Zeige Informationen
        ItemStack infoItem = createInfoItem();
        inventory.setItem(4, infoItem);

        // Zeige Erfolgsquote
        ItemStack successRateItem = createSuccessRateItem();
        inventory.setItem(11, successRateItem);

        // Zeige Fehlschlag-Konsequenz
        ItemStack failureItem = createFailureItem();
        inventory.setItem(13, failureItem);

        // Zeige Reparatur-Info
        ItemStack repairItem = createRepairItem();
        inventory.setItem(15, repairItem);

        // Upgrade-Button (Slot 22)
        ItemStack upgradeButton = createUpgradeButton();
        inventory.setItem(22, upgradeButton);

        player.openInventory(inventory);
    }

    /**
     * Füllt die Ränder des Inventorys.
     */
    private static void fillBorders(Inventory inventory) {
        // Obere Reihe
        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                ItemStack pane = createGlassPane();
                inventory.setItem(i, pane);
            }
        }

        // Mittlere Reihe Ränder und Lücken (9, 10, 12, 14, 16, 17)
        for (int i : new int[]{9, 10, 12, 14, 16, 17}) {
            inventory.setItem(i, createGlassPane());
        }

        // Untere Reihe
        for (int i = 18; i < 27; i++) {
            if (i != 22) {
                ItemStack glasPane = createGlassPane();
                inventory.setItem(i, glasPane);
            }
        }
    }

    /**
     * Erstellt das Info-Item.
     */
    private static ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.SMITHING_TABLE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6Fähigkeits-Schmiede");
            List<String> lore = new ArrayList<>();
            lore.add("§7Upgrade oder Freischalt");
            lore.add("§7deine Fähigkeiten!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt das Erfolgsquoten-Item.
     */
    private static ItemStack createSuccessRateItem() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§aErfolgsquote");
            List<String> lore = new ArrayList<>();
            lore.add("§7" + ForgeMachine.getSuccessRatePercent() + "%");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt das Fehlschlag-Item.
     */
    private static ItemStack createFailureItem() {
        ItemStack item = new ItemStack(Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§cFehlschlag-Konsequenz");
            List<String> lore = new ArrayList<>();
            lore.add("§7Durability: -" + ForgeMachine.getFailureDurabilityLoss() + "%");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt das Reparatur-Info-Item.
     */
    private static ItemStack createRepairItem() {
        ItemStack item = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§eReparatur");
            List<String> lore = new ArrayList<>();
            lore.add("§7Kosten: " + (int)(ForgeMachine.getRepairCostMultiplier() * 100) + "%");
            lore.add("§7der Upgrade-Kosten");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt den Upgrade-Button.
     */
    private static ItemStack createUpgradeButton() {
        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§a§lUPGRADE");
            List<String> lore = new ArrayList<>();
            lore.add("§7Klicke, um zu upgraden");
            meta.setLore(lore);
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
}
