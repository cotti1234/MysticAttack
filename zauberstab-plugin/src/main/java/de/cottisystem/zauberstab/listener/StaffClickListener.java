package de.cottisystem.zauberstab.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import de.cottisystem.zauberstab.ZauberstabPlugin;
import de.cottisystem.zauberstab.item.StaffManager;
import de.cottisystem.zauberstab.util.BedrockUtil;
import de.cottisystem.zauberstab.model.Staff;
import de.cottisystem.zauberstab.gui.StaffMenuGUI;

import java.util.Optional;

/**
 * Listener für Stab-Interaktionen (Links-/Rechtsklick).
 * Verwaltet Fähigkeits-Aktivierung und Menü-Zugriff.
 */
public class StaffClickListener implements Listener {

    private final ZauberstabPlugin plugin;
    private final StaffManager staffManager;

    public StaffClickListener(ZauberstabPlugin plugin, StaffManager staffManager) {
        this.plugin = plugin;
        this.staffManager = staffManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Verarbeite nur Main-Hand-Events (verhindert doppeltes Auslösen)
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }

        // Prüfe, ob Spieler einen Stab hält
        if (!staffManager.isHoldingStaff(player)) {
            return;
        }

        // Verhindere Block-Interaktionen mit Stäben
        if (event.getClickedBlock() != null && event.getAction().isLeftClick()) {
            event.setCancelled(true);
        }

        // Lade Stab
        Optional<Staff> staffOpt = staffManager.getPlayerStaff(player);
        if (staffOpt.isEmpty()) {
            return;
        }

        Staff staff = staffOpt.get();

        // Prüfe auf Sneak + Rechtsklick (Menü öffnen)
        if (event.getAction().isRightClick() && BedrockUtil.isSneaking(player)) {
            event.setCancelled(true);
            openStaffMenu(player, staff);
            return;
        }

        // Linksklick (Fähigkeit A)
        if (event.getAction().isLeftClick()) {
            event.setCancelled(true);
            activateAbility(player, staff, 0); // Slot 0
            return;
        }

        // Rechtsklick (Fähigkeit B)
        if (event.getAction().isRightClick()) {
            event.setCancelled(true);
            activateAbility(player, staff, 1); // Slot 1
        }
    }

    /**
     * Aktiviert eine Fähigkeit.
     */
    private void activateAbility(Player player, Staff staff, int slot) {
        // Prüfe, ob Slot vorhanden ist
        if (slot >= staff.getAbilities().size()) {
            player.sendMessage("§cKein Fähigkeit in Slot " + (slot + 1));
            return;
        }

        // Hole Fähigkeit
        var ability = staff.getAbilities().get(slot);
        if (ability == null) {
            player.sendMessage("§cFähigkeit nicht gefunden");
            return;
        }

        // Führe Fähigkeit aus (prüft Mana, Cooldowns, Kombos und wendet Effekte an)
        plugin.getAbilityExecutor().execute(player, staff, ability);
    }

    /**
     * Öffnet das Stab-Menü.
     */
    private void openStaffMenu(Player player, Staff staff) {
        StaffMenuGUI.openStaffMenu(player, staff);
    }
}
