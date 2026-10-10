package de.cottisystem.zauberstab.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.model.Staff;
import de.cottisystem.zauberstab.model.Ability;

import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltet Custom Stab-Items mit PDC (PersistentDataContainer).
 * Speichert Stab-Metadaten direkt im Item.
 * Integriert ItemAdder für Custom Items (optional).
 */
public class StaffItemManager {

    private final JavaPlugin plugin;
    private final NamespacedKey staffTypeKey;
    private final NamespacedKey staffIdKey;
    private final NamespacedKey slotCountKey;
    private final NamespacedKey durabilityKey;
    private final NamespacedKey abilitiesKey;
    private boolean itemAdderAvailable = false;

    public StaffItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.staffTypeKey = new NamespacedKey(plugin, "staff_type");
        this.staffIdKey = new NamespacedKey(plugin, "staff_id");
        this.slotCountKey = new NamespacedKey(plugin, "slot_count");
        this.durabilityKey = new NamespacedKey(plugin, "durability");
        this.abilitiesKey = new NamespacedKey(plugin, "abilities");
        this.itemAdderAvailable = Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
    }

    /**
     * Erstellt ein neues Stab-Item basierend auf einem StaffType.
     * Versucht zuerst, das Item aus ItemAdder zu laden.
     * Fallback: Verwendet Stick.
     */
    public ItemStack createStaffItem(StaffType staffType) {
        // Versuche, Item aus ItemAdder zu laden
        ItemStack item = getItemFromItemAdder(staffType);
        if (item == null) {
            // Fallback: Stick
            item = new ItemStack(Material.STICK);
        }
        
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Setze Display-Name
            meta.setDisplayName("§6" + staffType.getDisplayName());

            // Setze Lore mit Beschreibung
            List<String> lore = new ArrayList<>();
            lore.add("§7" + staffType.getDescription());
            lore.add("§7Element: §b" + staffType.getElement().getDisplayName());
            lore.add("§7Tier: §e" + staffType.getTier().getDisplayName());
            lore.add("§7Slots: §a" + staffType.getTier().getSlotCount());
            meta.setLore(lore);

            // Speichere Metadaten im PDC
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(staffTypeKey, PersistentDataType.STRING, staffType.getId());
            pdc.set(staffIdKey, PersistentDataType.STRING, staffType.getId() + "_" + System.currentTimeMillis());
            pdc.set(slotCountKey, PersistentDataType.INTEGER, staffType.getTier().getSlotCount());
            pdc.set(durabilityKey, PersistentDataType.INTEGER, 100);

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Prüft, ob ein Item ein Stab ist.
     */
    public boolean isStaff(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(staffTypeKey, PersistentDataType.STRING);
    }

    /**
     * Gibt den StaffType eines Items zurück.
     */
    public StaffType getStaffType(ItemStack item) {
        if (!isStaff(item)) {
            return null;
        }

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String typeId = pdc.get(staffTypeKey, PersistentDataType.STRING);
        return StaffType.fromId(typeId);
    }

    /**
     * Gibt die Stab-ID zurück.
     */
    public String getStaffId(ItemStack item) {
        if (!isStaff(item)) {
            return null;
        }

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(staffIdKey, PersistentDataType.STRING);
    }

    /**
     * Gibt die Slot-Anzahl zurück.
     */
    public int getSlotCount(ItemStack item) {
        if (!isStaff(item)) {
            return 0;
        }

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        Integer slotCount = pdc.get(slotCountKey, PersistentDataType.INTEGER);
        return slotCount != null ? slotCount : 0;
    }

    /**
     * Gibt die Durability zurück.
     */
    public int getDurability(ItemStack item) {
        if (!isStaff(item)) {
            return 0;
        }

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        Integer durability = pdc.get(durabilityKey, PersistentDataType.INTEGER);
        return durability != null ? durability : 100;
    }

    /**
     * Setzt die Durability.
     */
    public void setDurability(ItemStack item, int durability) {
        if (!isStaff(item) || item.getItemMeta() == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int finalDurability = Math.max(0, Math.min(100, durability));

        // Stelle sicher, dass Durability zwischen 0 und 100 bleibt
        pdc.set(durabilityKey, PersistentDataType.INTEGER, finalDurability);

        // Aktualisiere Lore mit Durability-Anzeige
        List<String> lore = meta.getLore();
        if (lore != null) {
            // Entferne alte Durability-Zeile
            lore.removeIf(line -> line.contains("Durability"));
            // Füge neue Durability-Zeile hinzu
            lore.add("§7Durability: §c" + finalDurability + "%");
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
    }

    /**
     * Gibt die im Item gespeicherten Fähigkeits-IDs zurück.
     * Gibt null zurück, wenn noch keine Fähigkeiten gespeichert wurden.
     */
    public List<String> getAbilityIds(ItemStack item) {
        if (!isStaff(item)) {
            return null;
        }

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String stored = pdc.get(abilitiesKey, PersistentDataType.STRING);
        if (stored == null) {
            return null;
        }
        if (stored.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(List.of(stored.split(",")));
    }

    /**
     * Speichert die Fähigkeiten eines Stabs im Item (PDC + Lore).
     */
    public void setAbilities(ItemStack item, List<Ability> abilities) {
        if (!isStaff(item) || item.getItemMeta() == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String joined = String.join(",", abilities.stream().map(Ability::getId).toList());
        pdc.set(abilitiesKey, PersistentDataType.STRING, joined);

        // Aktualisiere Lore mit Fähigkeits-Anzeige
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.removeIf(line -> line.contains("Fähigkeit "));
        for (int i = 0; i < abilities.size(); i++) {
            lore.add("§7Fähigkeit " + (i + 1) + ": §b" + abilities.get(i).getDisplayName());
        }
        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    /**
     * Gibt eine Beschreibung des Stabs zurück.
     */
    public String getStaffDescription(ItemStack item) {
        if (!isStaff(item)) {
            return "Kein Stab";
        }

        StaffType type = getStaffType(item);
        int durability = getDurability(item);
        int slots = getSlotCount(item);

        return String.format("%s | Durability: %d%% | Slots: %d",
                type.getDisplayName(), durability, slots);
    }

    /**
     * Versucht, ein Item aus ItemAdder zu laden.
     * Nutzt Reflection, um ItemAdder optional zu laden.
     */
    private ItemStack getItemFromItemAdder(StaffType staffType) {
        if (!itemAdderAvailable || staffType == null) {
            return null;
        }

        try {
            // ItemsAdder Item-IDs (Namespace: mystic-attack)
            String itemId = switch (staffType) {
                case BASIC -> "mystic-attack:staff_stab";
                case WATER -> "mystic-attack:wasser_stab";
                case OBSIDIAN -> "mystic-attack:moon_staff";
            };

            // Versuche, das Item aus ItemAdder zu laden
            // ItemAdder API: dev.lone.itemsadder.api.CustomStack.getInstance(itemId).getItemStack()
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Object customStack = customStackClass.getMethod("getInstance", String.class).invoke(null, itemId);

            if (customStack != null) {
                ItemStack item = (ItemStack) customStackClass.getMethod("getItemStack").invoke(customStack);
                if (item != null) {
                    plugin.getLogger().info("[MysticCore1] ✓ Item aus ItemsAdder geladen: " + itemId);
                    return item;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[MysticCore1] ⚠️  Fehler beim Laden von ItemsAdder Item für " + staffType.getId());
        }

        return null;
    }

    /**
     * Gibt den ItemAdder-Status zurück.
     */
    public boolean isItemAdderAvailable() {
        return itemAdderAvailable;
    }
}
