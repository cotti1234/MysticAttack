package de.cottisystem.zauberstab.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.ZauberstabPlugin;
import de.cottisystem.zauberstab.db.cache.PlayerCache;

/**
 * Listener für Spieler-Logout.
 * Entlädt Spieler-Profile aus dem Cache und speichert sie.
 */
public class PlayerQuitListener implements Listener {

    private final JavaPlugin plugin;
    private final PlayerCache playerCache;

    public PlayerQuitListener(JavaPlugin plugin, PlayerCache playerCache) {
        this.plugin = plugin;
        this.playerCache = playerCache;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Live-Daten ins Profil übernehmen, dann Mana-Daten entfernen
        ZauberstabPlugin zauberstabPlugin = (ZauberstabPlugin) plugin;
        zauberstabPlugin.syncPlayerToProfile(player);
        zauberstabPlugin.getManaManager().removePlayer(player);

        // Entlade Spieler-Profil asynchron
        playerCache.unloadPlayer(player.getUniqueId())
                .thenAccept(v -> {
                    plugin.getLogger().info("✓ Spieler-Profil entladen: " + player.getName());
                })
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Fehler beim Entladen des Spieler-Profils für " + player.getName() + ": " + ex.getMessage());
                    return null;
                });
    }
}
