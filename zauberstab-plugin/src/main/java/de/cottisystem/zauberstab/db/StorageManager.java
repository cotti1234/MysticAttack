package de.cottisystem.zauberstab.db;

import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.config.ConfigManager;
import de.cottisystem.zauberstab.config.StorageConfig;

/**
 * Verwaltet die Datenbank-Verbindungen und wählt zwischen Redis und interner DB.
 * Diese Klasse ist der zentrale Einstiegspunkt für alle Datenbankoperationen.
 */
public class StorageManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private StorageProvider storageProvider;

    public StorageManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Initialisiert den Storage-Provider basierend auf der Konfiguration.
     */
    public void initialize() throws Exception {
        StorageConfig config = configManager.getStorageConfig();

        try {
            if (config.getStorageType() == StorageConfig.StorageType.REDIS) {
                plugin.getLogger().info("Versuche, Redis-Verbindung herzustellen...");
                storageProvider = new RedisStorageProvider(config);
                storageProvider.connect();
                plugin.getLogger().info("✓ Redis-Verbindung erfolgreich");
            } else {
                plugin.getLogger().info("Verwende interne Datenbank...");
                storageProvider = new InternalStorageProvider(plugin, config);
                storageProvider.connect();
                plugin.getLogger().info("✓ Interne Datenbank initialisiert");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Fehler beim Initialisieren des primären Storage-Providers: " + e.getMessage());
            
            // Fallback auf interne DB wenn Redis fehlschlägt
            if (config.getStorageType() == StorageConfig.StorageType.REDIS) {
                plugin.getLogger().warning("Fallback auf interne Datenbank...");
                storageProvider = new InternalStorageProvider(plugin, config);
                storageProvider.connect();
                plugin.getLogger().info("✓ Fallback auf interne Datenbank erfolgreich");
            } else {
                throw e;
            }
        }
    }

    /**
     * Gibt den aktuellen Storage-Provider zurück.
     */
    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    /**
     * Beendet alle Datenbankverbindungen.
     */
    public void shutdown() throws Exception {
        if (storageProvider != null) {
            storageProvider.disconnect();
        }
    }

    /**
     * Prüft, ob die Datenbankverbindung aktiv ist.
     */
    public boolean isConnected() {
        return storageProvider != null && storageProvider.isConnected();
    }
}
