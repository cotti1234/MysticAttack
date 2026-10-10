package de.cottisystem.zauberstab.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.item.StaffManager;
import de.cottisystem.zauberstab.item.StaffType;

/**
 * Listener für Stab-Wechsel (Hotbar-Wechsel).
 * Zeigt Informationen an, wenn Spieler einen Stab wechselt.
 */
public class StaffHoldListener implements Listener {

    private final JavaPlugin plugin;
    private final StaffManager staffManager;

    public StaffHoldListener(JavaPlugin plugin, StaffManager staffManager) {
        this.plugin = plugin;
        this.staffManager = staffManager;
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        // Prüfe, ob neues Item ein Stab ist
        if (staffManager.getItemManager().isStaff(newItem)) {
            String description = staffManager.getItemManager().getStaffDescription(newItem);
            player.sendActionBar("§6" + description);
        }
    }
}
