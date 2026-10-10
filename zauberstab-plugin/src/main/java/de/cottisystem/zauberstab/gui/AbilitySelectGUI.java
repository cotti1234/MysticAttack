package de.cottisystem.zauberstab.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.ZauberstabPlugin;
import de.cottisystem.zauberstab.model.Ability;
import de.cottisystem.zauberstab.skilltree.PlayerSkillTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * GUI zur Auswahl einer Fähigkeit für einen Stab-Slot.
 * Zeigt alle Fähigkeiten aus abilities.yml an.
 */
public class AbilitySelectGUI {

    // Titel zur eindeutigen Identifikation des GUIs im InventoryClickListener
    public static final String TITLE = "§6Fähigkeit wählen";
    public static final int TITLE_PAGE_SUFFIX_START = TITLE.length(); // nur Titel ohne Seite

    private static final int INVENTORY_SIZE = 36; // 4x9 Inventory
    private static final int ABILITIES_PER_PAGE = 18;
    private static final Material GLASS_PANE = Material.LIGHT_GRAY_STAINED_GLASS_PANE;

    private static NamespacedKey abilityIdKey;
    private static NamespacedKey actionKey;

    /**
     * Initialisiert den NamespacedKey (einmalig beim Plugin-Start).
     */
    public static void init(JavaPlugin plugin) {
        abilityIdKey = new NamespacedKey(plugin, "select_ability_id");
        actionKey = new NamespacedKey(plugin, "select_ability_action");
    }

    /**
     * Öffnet das Auswahl-Menü mit nur freigeschalteten Fähigkeiten.
     */
    public static void openSelectMenu(Player player, ZauberstabPlugin plugin, int targetSlot, int page) {
        PlayerSkillTree playerSkillTree = plugin.getSkillTree().getOrCreatePlayerSkillTree(player.getUniqueId());

        // Nur freigeschaltete Fähigkeiten anzeigen
        List<Ability> sorted = plugin.getAbilityLoader().getAllAbilities().stream()
                .filter(a -> playerSkillTree.getAbilityLevel(a.getId() + "_node") > 0)
                .sorted(Comparator.comparing(a -> a.getElement().getId() + a.getId()))
                .toList();

        int maxPage = Math.max(0, (sorted.size() - 1) / ABILITIES_PER_PAGE);
        int safePage = Math.max(0, Math.min(page, maxPage));

        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, TITLE + " §7Seite " + (safePage + 1));

        // Info-Item (Slot 4)
        inventory.setItem(4, createInfoItem(targetSlot, sorted.size()));

        // Fähigkeiten (ab Slot 9)
        int startIndex = safePage * ABILITIES_PER_PAGE;
        int endIndex = Math.min(startIndex + ABILITIES_PER_PAGE, sorted.size());
        int slot = 9;
        for (int i = startIndex; i < endIndex; i++) {
            inventory.setItem(slot, createAbilityItem(sorted.get(i)));
            slot++;
        }

        // Restliche Fähigkeits-Reihen füllen
        for (int i = slot; i < 27; i++) {
            inventory.setItem(i, createGlassPane());
        }

        // Untere Reihe: Navigation
        for (int i = 27; i < 36; i++) {
            if (i == 27 && safePage > 0) {
                inventory.setItem(i, createNavItem(Material.ARROW, "§7Vorherige Seite", "prev_page"));
            } else if (i == 31) {
                inventory.setItem(i, createNavItem(Material.ARROW, "§e§lZurück", "back"));
            } else if (i == 35 && safePage < maxPage) {
                inventory.setItem(i, createNavItem(Material.ARROW, "§7Nächste Seite", "next_page"));
            } else {
                inventory.setItem(i, createGlassPane());
            }
        }

        // Obere Reihe füllen
        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                inventory.setItem(i, createGlassPane());
            }
        }

        player.openInventory(inventory);
    }

    /**
     * Liest die Fähigkeits-ID aus einem GUI-Item (oder null).
     */
    public static String getAbilityId(ItemStack item) {
        if (item == null || item.getItemMeta() == null || abilityIdKey == null) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer()
                .get(abilityIdKey, PersistentDataType.STRING);
    }

    public static String getAction(ItemStack item) {
        if (item == null || item.getItemMeta() == null || actionKey == null) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer()
                .get(actionKey, PersistentDataType.STRING);
    }

    /**
     * Erstellt das Info-Item.
     */
    private static ItemStack createInfoItem(int targetSlot, int unlockedCount) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6Fähigkeit für Slot " + (targetSlot + 1));
            List<String> lore = new ArrayList<>();
            lore.add("§7Wähle eine Fähigkeit aus,");
            lore.add("§7um sie diesem Slot zuzuweisen.");
            lore.add("§7Freigeschaltet: §a" + unlockedCount);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Erstellt ein auswählbares Fähigkeits-Item.
     */
    private static ItemStack createAbilityItem(Ability ability) {
        ItemStack item = new ItemStack(getMaterialForElement(ability.getElement().getId()));
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§b" + ability.getDisplayName());
            List<String> lore = new ArrayList<>();
            lore.add("§7" + ability.getDescription());
            lore.add("§7Element: §b" + ability.getElement().getDisplayName());
            lore.add("§7Mana: §3" + ability.getManaCost(1) + " §7| Cooldown: §e" + ability.getCooldown(1) + "s");
            lore.add("§7Tags: §d" + ability.getTagsAsString());
            lore.add("");
            lore.add("§aKlicke, um zuzuweisen");
            meta.setLore(lore);

            if (abilityIdKey != null) {
                meta.getPersistentDataContainer()
                        .set(abilityIdKey, PersistentDataType.STRING, ability.getId());
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Gibt ein passendes Material für ein Element zurück.
     */
    private static Material getMaterialForElement(String elementId) {
        return switch (elementId) {
            case "water" -> Material.HEART_OF_THE_SEA;
            case "fire" -> Material.BLAZE_POWDER;
            case "ice" -> Material.PACKED_ICE;
            case "lightning" -> Material.LIGHTNING_ROD;
            case "earth", "stone" -> Material.COBBLESTONE;
            default -> Material.ENCHANTED_BOOK;
        };
    }

    /**
     * Erstellt einen Navigations-Button (Zurück, Seite, etc.).
     */
    private static ItemStack createNavItem(Material material, String displayName, String action) {
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
