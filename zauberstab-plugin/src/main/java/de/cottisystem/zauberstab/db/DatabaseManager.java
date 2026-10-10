package de.cottisystem.zauberstab.db;

import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.config.ConfigManager;
import de.cottisystem.zauberstab.config.StorageConfig;
import de.cottisystem.zauberstab.db.repository.PlayerRepository;
import de.cottisystem.zauberstab.db.repository.RedisPlayerRepository;
import de.cottisystem.zauberstab.db.repository.InternalPlayerRepository;
import de.cottisystem.zauberstab.db.cache.PlayerCache;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Zentrale Datenbank-Verwaltung.
 * Koordiniert StorageManager, Repositories und Cache.
 */
public class DatabaseManager {

    private final JavaPlugin plugin;
    private final StorageManager storageManager;
    private final PlayerRepository playerRepository;
    private final PlayerCache playerCache;
    private final Executor asyncExecutor;

    public DatabaseManager(JavaPlugin plugin, ConfigManager configManager) throws Exception {
        this.plugin = plugin;
        this.asyncExecutor = ForkJoinPool.commonPool();

        // Initialisiere Storage Manager
        this.storageManager = new StorageManager(plugin, configManager);
        this.storageManager.initialize();

        // Erstelle Repository basierend auf Storage-Typ
        StorageConfig storageConfig = configManager.getStorageConfig();
        if (storageManager.getStorageProvider() instanceof RedisStorageProvider) {
            this.playerRepository = new RedisPlayerRepository(
                    storageManager.getStorageProvider(),
                    asyncExecutor
            );
        } else {
            this.playerRepository = new InternalPlayerRepository(
                    (InternalStorageProvider) storageManager.getStorageProvider(),
                    asyncExecutor
            );
        }

        // Initialisiere Cache (speichere alle 30 Sekunden)
        this.playerCache = new PlayerCache(plugin, playerRepository, 30000);

        plugin.getLogger().info("✓ DatabaseManager initialisiert");
    }

    /**
     * Gibt das Player-Repository zurück.
     */
    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }

    /**
     * Gibt den Player-Cache zurück.
     */
    public PlayerCache getPlayerCache() {
        return playerCache;
    }

    /**
     * Gibt den Storage Manager zurück.
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Beendet den DatabaseManager und speichert alle Daten.
     */
    public void shutdown() throws Exception {
        plugin.getLogger().info("Fahre DatabaseManager herunter...");
        
        // Speichere Cache
        playerCache.shutdown();
        plugin.getLogger().info("✓ Player-Cache gespeichert");

        // Schließe Storage
        storageManager.shutdown();
        plugin.getLogger().info("✓ Storage-Verbindungen geschlossen");
    }
}
