package de.cottisystem.zauberstab.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.crafting.ForgeGUI;

/**
 * Listener für Interaktionen mit dem Forge-Block.
 * Öffnet das Forge-Menü beim Rechtsklick auf einen Smithing Table.
 */
public class ForgeBlockListener implements Listener {

    private final JavaPlugin plugin;
    private static final Material FORGE_BLOCK = Material.SMITHING_TABLE;

    public ForgeBlockListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Verarbeite nur Main-Hand-Events (verhindert doppeltes Öffnen)
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }

        // Prüfe, ob es ein Rechtsklick auf einen Block ist
        if (!event.getAction().isRightClick() || event.getClickedBlock() == null) {
            return;
        }

        // Sneakende Spieler können den Block normal nutzen (z.B. Blöcke platzieren)
        if (event.getPlayer().isSneaking()) {
            return;
        }

        Block block = event.getClickedBlock();

        // Prüfe, ob es ein Forge-Block ist
        if (block.getType() != FORGE_BLOCK) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        
        // Öffne Forge-Menü
        ForgeGUI.openForgeMenu(player);
        player.sendMessage("§6Willkommen in der Fähigkeits-Schmiede!");
    }
}
