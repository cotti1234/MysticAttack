package de.cottisystem.zauberstab.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.mana.ManaManager;
import de.cottisystem.zauberstab.mana.ManaDisplay;

/**
 * Listener für Mana-Regeneration.
 * Regeneriert Mana, wenn Spieler sich bewegt.
 */
public class ManaRegenListener implements Listener {

    private final JavaPlugin plugin;
    private final ManaManager manaManager;

    public ManaRegenListener(JavaPlugin plugin, ManaManager manaManager) {
        this.plugin = plugin;
        this.manaManager = manaManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Regeneriere Mana
        manaManager.regenerateMana(player);

        // Zeige Mana-Balken (optional)
        // ManaDisplay.displaySimpleManaBar(player, manaManager);
    }
}
