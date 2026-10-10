package de.cottisystem.zauberstab.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.ZauberstabPlugin;
import de.cottisystem.zauberstab.db.cache.PlayerCache;

/**
 * Listener für Spieler-Login.
 * Lädt Spieler-Profile in den Cache und initialisiert Mana.
 */
public class PlayerJoinListener implements Listener {

    private final JavaPlugin plugin;
    private final PlayerCache playerCache;

    public PlayerJoinListener(JavaPlugin plugin, PlayerCache playerCache) {
        this.plugin = plugin;
        this.playerCache = playerCache;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Lade Spieler-Profil asynchron
        playerCache.loadPlayer(player.getUniqueId(), player.getName())
                .thenAccept(profile -> {
                    plugin.getLogger().info("✓ Spieler-Profil geladen: " + player.getName());

                    // Übertrage Profil in Live-Systeme (auf dem Main-Thread)
                    ZauberstabPlugin zauberstabPlugin = (ZauberstabPlugin) plugin;
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (player.isOnline()) {
                            zauberstabPlugin.applyProfileToPlayer(player, profile);
                        }
                    });
                })
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Fehler beim Laden des Spieler-Profils für " + player.getName() + ": " + ex.getMessage());
                    return null;
                });
    }
}
