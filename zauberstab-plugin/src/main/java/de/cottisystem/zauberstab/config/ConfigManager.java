package de.cottisystem.zauberstab.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Verwaltet die Konfiguration des Plugins.
 * Lädt config.yml und validiert die Einstellungen.
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private Map<String, Object> config;
    private StorageConfig storageConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Lädt die Konfiguration aus der config.yml.
     * Falls die Datei nicht existiert, wird eine Standard-Konfiguration erstellt.
     */
    public void loadConfig() throws IOException {
        Path dataFolder = plugin.getDataFolder().toPath();
        Path configFile = dataFolder.resolve("config.yml");

        // Erstelle Datenordner falls nicht vorhanden
        Files.createDirectories(dataFolder);

        // Falls config.yml nicht existiert, erstelle Standard-Konfiguration
        if (!Files.exists(configFile)) {
            createDefaultConfig(configFile);
        }

        // Lade YAML-Datei
        Yaml yaml = new Yaml();
        try (InputStream input = Files.newInputStream(configFile)) {
            config = yaml.load(input);
            if (config == null) {
                config = Map.of();
            }
        }

        // Validiere und lade Storage-Konfiguration
        loadStorageConfig();

        plugin.getLogger().info("Konfiguration erfolgreich geladen.");
    }

    /**
     * Lädt die Storage-Konfiguration (Redis oder interne DB).
     */
    private void loadStorageConfig() {
        Map<String, Object> storageMap = (Map<String, Object>) config.getOrDefault("storage", Map.of());
        
        String storageType = (String) storageMap.getOrDefault("type", "INTERNAL");
        
        storageConfig = new StorageConfig();
        storageConfig.setStorageType(StorageConfig.StorageType.valueOf(storageType.toUpperCase()));

        // Redis-Konfiguration
        Map<String, Object> redisMap = (Map<String, Object>) storageMap.getOrDefault("redis", Map.of());
        storageConfig.setRedisHost((String) redisMap.getOrDefault("host", "localhost"));
        storageConfig.setRedisPort(((Number) redisMap.getOrDefault("port", 6379)).intValue());
        storageConfig.setRedisPassword((String) redisMap.getOrDefault("password", ""));
        storageConfig.setRedisDatabase(((Number) redisMap.getOrDefault("database", 0)).intValue());

        // Interne DB-Konfiguration
        Map<String, Object> internalMap = (Map<String, Object>) storageMap.getOrDefault("internal", Map.of());
        storageConfig.setInternalDbType((String) internalMap.getOrDefault("type", "SQLITE"));
        storageConfig.setInternalDbPath((String) internalMap.getOrDefault("path", "plugins/MysticCore1/data.db"));

        plugin.getLogger().info("Storage-Typ: " + storageConfig.getStorageType());
    }

    /**
     * Erstellt eine Standard-Konfigurationsdatei.
     */
    private void createDefaultConfig(Path configFile) throws IOException {
        String defaultConfig = """
                # Zauberstab-Plugin Konfiguration
                
                # Storage-Einstellungen
                storage:
                  # Speichertyp: REDIS oder INTERNAL
                  type: INTERNAL
                  
                  # Redis-Konfiguration (nur wenn type: REDIS)
                  redis:
                    host: localhost
                    port: 6379
                    password: ""
                    database: 0
                  
                  # Interne Datenbank-Konfiguration (nur wenn type: INTERNAL)
                  internal:
                    type: SQLITE  # SQLITE oder H2
                    path: plugins/MysticCore1/data.db
                
                # Gameplay-Einstellungen (werden in späteren Phasen erweitert)
                gameplay:
                  debug: false
                """;

        Files.createDirectories(configFile.getParent());
        Files.writeString(configFile, defaultConfig);
        plugin.getLogger().info("Standard-Konfiguration erstellt: " + configFile);
    }

    /**
     * Gibt die Storage-Konfiguration zurück.
     */
    public StorageConfig getStorageConfig() {
        return storageConfig;
    }

    /**
     * Gibt einen Konfigurationswert zurück.
     */
    public Object get(String key) {
        return config.get(key);
    }

    /**
     * Gibt einen Konfigurationswert mit Default-Wert zurück.
     */
    public Object get(String key, Object defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }
}
