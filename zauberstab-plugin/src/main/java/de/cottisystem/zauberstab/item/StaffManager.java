package de.cottisystem.zauberstab.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.model.Staff;
import de.cottisystem.zauberstab.model.Ability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Verwaltet Stab-Instanzen und deren Zuordnung zu Spielern.
 * Koordiniert zwischen Item-Metadaten (PDC) und In-Memory-Stab-Objekten.
 */
public class StaffManager {

    private final JavaPlugin plugin;
    private final StaffItemManager itemManager;
    
    // Cache: staffId -> Staff-Objekt
    private final Map<String, Staff> staffCache = new HashMap<>();
    
    // Cache: playerUuid -> staffId (aktuell gehaltener Stab)
    private final Map<UUID, String> playerStaffCache = new HashMap<>();

    public StaffManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemManager = new StaffItemManager(plugin);
    }

    /**
     * Gibt den StaffItemManager zurück.
     */
    public StaffItemManager getItemManager() {
        return itemManager;
    }

    /**
     * Erstellt einen neuen Stab basierend auf einem StaffType.
     */
    public Staff createStaff(StaffType staffType) {
        Staff staff = new Staff();
        staff.setId(staffType.getId() + "_" + System.currentTimeMillis());
        staff.setDisplayName(staffType.getDisplayName());
        staff.setDescription(staffType.getDescription());
        staff.setElement(staffType.getElement());
        staff.setTier(staffType.getTier());
        staff.setDurability(100);
        staff.setMaxDurability(100);

        // Keine Start-Fähigkeiten mehr — Spieler müssen Fähigkeiten erst im Skill-Tree freischalten

        // Speichere im Cache
        staffCache.put(staff.getId(), staff);

        return staff;
    }

    /**
     * Gibt einen Stab anhand der ID zurück.
     */
    public Optional<Staff> getStaff(String staffId) {
        return Optional.ofNullable(staffCache.get(staffId));
    }

    /**
     * Lädt einen Stab aus einem Item.
     */
    public Optional<Staff> loadStaffFromItem(ItemStack item) {
        if (!itemManager.isStaff(item)) {
            return Optional.empty();
        }

        String staffId = itemManager.getStaffId(item);
        
        // Prüfe Cache
        if (staffCache.containsKey(staffId)) {
            return Optional.of(staffCache.get(staffId));
        }

        // Erstelle neuen Stab aus Item-Metadaten
        StaffType staffType = itemManager.getStaffType(item);
        if (staffType == null) {
            return Optional.empty();
        }

        Staff staff = new Staff();
        staff.setId(staffId);
        staff.setDisplayName(staffType.getDisplayName());
        staff.setDescription(staffType.getDescription());
        staff.setElement(staffType.getElement());
        staff.setTier(staffType.getTier());
        staff.setDurability(itemManager.getDurability(item));
        staff.setMaxDurability(100);

        // Lade Fähigkeiten aus dem Item (PDC); ohne Fallback, da Fähigkeiten erst freigeschaltet werden müssen
        List<String> storedAbilityIds = itemManager.getAbilityIds(item);
        if (storedAbilityIds != null) {
            loadAbilitiesFromIds(staff, storedAbilityIds);
        }

        // Speichere im Cache
        staffCache.put(staffId, staff);

        return Optional.of(staff);
    }

    /**
     * Weist einer Fähigkeit einen Slot zu und persistiert die Änderung im Item.
     */
    public boolean setAbilityAtSlot(Staff staff, ItemStack item, int slot, Ability ability) {
        if (slot < 0 || slot >= staff.getSlotCount()) {
            return false;
        }

        List<Ability> abilities = staff.getAbilities();
        if (slot < abilities.size()) {
            abilities.set(slot, ability);
        } else if (abilities.size() < staff.getSlotCount()) {
            abilities.add(ability);
        } else {
            return false;
        }

        itemManager.setAbilities(item, abilities);
        return true;
    }

    /**
     * Entfernt die Fähigkeit an einem Slot und persistiert die Änderung im Item.
     */
    public boolean removeAbilityAtSlot(Staff staff, ItemStack item, int slot) {
        List<Ability> abilities = staff.getAbilities();
        if (slot < 0 || slot >= abilities.size()) {
            return false;
        }

        abilities.remove(slot);
        itemManager.setAbilities(item, abilities);
        return true;
    }

    /**
     * Lädt Fähigkeiten anhand von IDs aus dem AbilityLoader.
     */
    private void loadAbilitiesFromIds(Staff staff, List<String> abilityIds) {
        if (!(plugin instanceof de.cottisystem.zauberstab.ZauberstabPlugin zauberstabPlugin)
                || zauberstabPlugin.getAbilityLoader() == null) {
            return;
        }

        for (String abilityId : abilityIds) {
            zauberstabPlugin.getAbilityLoader().getAbility(abilityId)
                    .ifPresent(staff::addAbility);
        }
    }

    /**
     * Speichert einen Stab zurück ins Item.
     */
    public void saveStaffToItem(Staff staff, ItemStack item) {
        if (!itemManager.isStaff(item)) {
            return;
        }

        itemManager.setDurability(item, staff.getDurability());
    }

    /**
     * Gibt den aktuell gehaltenen Stab eines Spielers zurück.
     */
    public Optional<Staff> getPlayerStaff(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        
        if (!itemManager.isStaff(mainHand)) {
            return Optional.empty();
        }

        return loadStaffFromItem(mainHand);
    }

    /**
     * Gibt den Offhand-Stab eines Spielers zurück (falls vorhanden).
     */
    public Optional<Staff> getPlayerOffhandStaff(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        if (!itemManager.isStaff(offHand)) {
            return Optional.empty();
        }

        return loadStaffFromItem(offHand);
    }

    /**
     * Gibt beide Stäbe eines Spielers zurück (Main + Offhand).
     */
    public Map<String, Staff> getPlayerStaffs(Player player) {
        Map<String, Staff> staffs = new HashMap<>();

        Optional<Staff> mainStaff = getPlayerStaff(player);
        mainStaff.ifPresent(staff -> staffs.put("main", staff));

        Optional<Staff> offhandStaff = getPlayerOffhandStaff(player);
        offhandStaff.ifPresent(staff -> staffs.put("offhand", staff));

        return staffs;
    }

    /**
     * Prüft, ob ein Spieler einen Stab in der Hand hält.
     */
    public boolean isHoldingStaff(Player player) {
        return itemManager.isStaff(player.getInventory().getItemInMainHand());
    }

    /**
     * Gibt die Slot-Anzahl eines Items zurück.
     */
    public int getSlotCount(ItemStack item) {
        return itemManager.getSlotCount(item);
    }

    /**
     * Gibt die Durability eines Items zurück.
     */
    public int getDurability(ItemStack item) {
        return itemManager.getDurability(item);
    }

    /**
     * Reduziert die Durability eines Items.
     */
    public void reduceDurability(ItemStack item, int amount) {
        int currentDurability = itemManager.getDurability(item);
        itemManager.setDurability(item, currentDurability - amount);
    }

    /**
     * Repariert ein Item.
     */
    public void repairItem(ItemStack item, int amount) {
        int currentDurability = itemManager.getDurability(item);
        itemManager.setDurability(item, currentDurability + amount);
    }

    /**
     * Leert den Cache (z.B. beim Plugin-Shutdown).
     */
    public void clearCache() {
        staffCache.clear();
        playerStaffCache.clear();
    }
}
