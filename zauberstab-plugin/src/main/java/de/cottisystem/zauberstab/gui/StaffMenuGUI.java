package de.cottisystem.zauberstab.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import de.cottisystem.zauberstab.model.Staff;
import de.cottisystem.zauberstab.model.Ability;

import java.util.ArrayList;
import java.util.List;

/**
 * Inventory-basiertes GUI für Stab-Menü.
 * Zeigt Stab-Informationen und ermöglicht Fähigkeits-Verwaltung.
 */
public class StaffMenuGUI {

    // Titel-Präfix zur eindeutigen Identifikation des GUIs im InventoryClickListener
    public static final String TITLE_PREFIX = "§6Stab-Menü §8» §6";

    private static final int INVENTORY_SIZE = 27; // 3x9 Inventory
    private static final Material GLASS_PANE = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
    private static final Material SLOT_ITEM = Material.LIME_STAINED_GLASS_PANE;
    private static final Material EMPTY_SLOT = Material.RED_STAINED_GLASS_PANE;

    /**
     * Öffnet das Stab-Menü für einen Spieler.
     */
    public static void openStaffMenu(Player player, Staff staff) {
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, TITLE_PREFIX + staff.getDisplayName());

        // Fülle Ränder mit Glass Panes
        fillBorders(inventory);

        // Zeige Stab-Informationen (Slot 4 = Mitte oben)
        ItemStack staffInfo = createStaffInfoItem(staff);
        inventory.setItem(4, staffInfo);

        // Zeige Durability (Slot 11 = Links)
        ItemStack durabilityItem = createDurabilityItem(staff);
        inventory.setItem(11, durabilityItem);

        // Zeige Slot-Anzahl (Slot 13 = Mitte)
        ItemStack slotCountItem = createSlotCountItem(staff);
        inventory.setItem(13, slotCountItem);

        // Zeige Element (Slot 15 = Rechts)
        ItemStack elementItem = createElementItem(staff);
        inventory.setItem(15, elementItem);

        // Zeige Fähigkeits-Slots (Zeile 2: Slots 19-25)
        displayAbilitySlots(inventory, staff);

        // Schließen-Button (Slot 22 = Mitte unten)
        inventory.setItem(22, createCloseButton());

        // Öffne Inventory
        player.openInventory(inventory);
    }

    /**
     * Füllt die Ränder des Inventorys mit Glass Panes.
     */
    private static void fillBorders(Inventory inventory) {
        // Obere Reihe (0-8)
        for (int i = 0; i < 9; i++) {
            if (i != 4) { // Mitte oben ist für Stab-Info reserviert
                ItemStack pane = createGlassPane("§7");
                inventory.setItem(i, pane);
            }
        }

        // Mittlere Reihe Ränder und Lücken (9, 10, 12, 14, 16, 17)
        for (int i : new int[]{9, 10, 12, 14, 16, 17}) {
            inventory.setItem(i, createGlassPane("§7"));
        }

        // Untere Reihe (18-26)
        for (int i = 18; i < 27; i++) {
            if (i != 22) { // Mitte unten ist für Schließen-Button reserviert
                ItemStack glasPane = createGlassPane("§7");
                inventory.setItem(i, glasPane);
            }
        }
    }

    /**
     * Zeigt die Fähigkeits-Slots an.
     */
    private static void displayAbilitySlots(Inventory inventory, Staff staff) {
        int slotCount = staff.getSlotCount();
        List<Ability> abilities = staff.getAbilities();

        // Zeige belegte Slots (Positionen 19-25, Slot 22 ist für den Schließen-Button reserviert)
        int inventorySlot = 19;
        for (int i = 0; i < slotCount; i++) {
            if (inventorySlot == 22) inventorySlot++; // Überspringe Schließen-Button
            if (inventorySlot > 25) break; // Nur bis Slot 25

            if (i < abilities.size()) {
                // Slot mit Fähigkeit
                ItemStack abilityItem = createAbilityItem(abilities.get(i), i);
                inventory.setItem(inventorySlot, abilityItem);
            } else {
                // Leerer Slot
                ItemStack emptySlot = createEmptySlotItem(i);
                inventory.setItem(inventorySlot, emptySlot);
            }
            inventorySlot++;
        }
    }

    /**
     * Erstellt ein Stab-Info-Item.
     */
    private static ItemStack createStaffInfoItem(Staff staff) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6" + staff.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7" + staff.getDescription());
            lore.add("§7Element: §b" + staff.getElement().getDisplayName());
            lore.add("§7Tier: §e" + staff.getTier().getDisplayName());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein Durability-Item.
     */
    private static ItemStack createDurabilityItem(Staff staff) {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§cDurability");
            List<String> lore = new ArrayList<>();
            lore.add("§7" + staff.getDurability() + "/" + staff.getMaxDurability());
            
            // Durability-Balken
            int percent = (staff.getDurability() * 100) / staff.getMaxDurability();
            String bar = createProgressBar(percent);
            lore.add(bar);
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein Slot-Count-Item.
     */
    private static ItemStack createSlotCountItem(Staff staff) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§aSlots");
            List<String> lore = new ArrayList<>();
            lore.add("§7" + staff.getUsedSlots() + "/" + staff.getSlotCount());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein Element-Item.
     */
    private static ItemStack createElementItem(Staff staff) {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§bElement");
            List<String> lore = new ArrayList<>();
            lore.add("§7" + staff.getElement().getDisplayName());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein Fähigkeits-Item.
     */
    private static ItemStack createAbilityItem(Ability ability, int slotIndex) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6" + ability.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add(getSlotLabel(slotIndex));
            lore.add("§7" + ability.getDescription());
            lore.add("§7Element: §b" + ability.getElement().getDisplayName());
            lore.add("§7Max Level: §e" + ability.getMaxLevel());
            lore.add("§7Tags: §d" + ability.getTagsAsString());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein leeres Slot-Item.
     */
    private static ItemStack createEmptySlotItem(int slotIndex) {
        ItemStack item = new ItemStack(EMPTY_SLOT);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§cLeerer Slot " + (slotIndex + 1));
            List<String> lore = new ArrayList<>();
            lore.add(getSlotLabel(slotIndex));
            lore.add("§7Klicke, um eine Fähigkeit hinzuzufügen");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Gibt die Beschreibung eines Stab-Slots zurück.
     * Slot 1 = Linksklick, Slot 2 = Rechtsklick, Slots 3+ = passive Verstärkung.
     */
    private static String getSlotLabel(int slotIndex) {
        return switch (slotIndex) {
            case 0 -> "§aSlot 1 §7(Aktiv: §eLinksklick§7)";
            case 1 -> "§aSlot 2 §7(Aktiv: §eRechtsklick§7)";
            default -> "§dSlot " + (slotIndex + 1) + " §7(Verstärkung: §dboostet Slot 1 & 2§7)";
        };
    }

    /**
     * Erstellt den Schließen-Button.
     */
    private static ItemStack createCloseButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§c§lSchließen");
            List<String> lore = new ArrayList<>();
            lore.add("§7Klicke, um das Menü zu schließen");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein Glass Pane Item.
     */
    private static ItemStack createGlassPane(String color) {
        ItemStack item = new ItemStack(GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color + " ");
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
        bar.append("§c");
        for (int i = 0; i < empty; i++) {
            bar.append("█");
        }
        bar.append(" §7").append(percent).append("%");

        return bar.toString();
    }
}
