package de.cottisystem.zauberstab.db;

import org.bukkit.plugin.java.JavaPlugin;
import de.cottisystem.zauberstab.config.StorageConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Interne Datenbank-Provider (SQLite oder H2).
 * Nutzt HikariCP für Connection Pooling.
 */
public class InternalStorageProvider implements StorageProvider {

    private final JavaPlugin plugin;
    private final StorageConfig config;
    private HikariDataSource dataSource;

    public InternalStorageProvider(JavaPlugin plugin, StorageConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void connect() throws Exception {
        try {
            String dbType = config.getInternalDbType().toUpperCase();
            String dbPath = config.getInternalDbPath();

            // Erstelle Datenbank-Verzeichnis falls nicht vorhanden
            Path dbPathObj = Paths.get(dbPath);
            Files.createDirectories(dbPathObj.getParent());

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionTimeout(30000);
            hikariConfig.setIdleTimeout(600000);
            hikariConfig.setMaxLifetime(1800000);

            if ("SQLITE".equals(dbType)) {
                hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbPath);
                hikariConfig.setDriverClassName("org.sqlite.JDBC");
            } else if ("H2".equals(dbType)) {
                hikariConfig.setJdbcUrl("jdbc:h2:" + dbPath + ";MODE=MySQL");
                hikariConfig.setDriverClassName("org.h2.Driver");
            } else {
                throw new Exception("Unbekannter Datenbank-Typ: " + dbType);
            }

            dataSource = new HikariDataSource(hikariConfig);

            // Teste Verbindung
            try (Connection conn = dataSource.getConnection()) {
                plugin.getLogger().info("✓ Datenbankverbindung erfolgreich getestet");
            }

            // Initialisiere Datenbank-Schema
            initializeSchema();
        } catch (Exception e) {
            throw new Exception("Fehler beim Verbinden mit interner Datenbank: " + e.getMessage(), e);
        }
    }

    /**
     * Initialisiert das Datenbank-Schema.
     */
    private void initializeSchema() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Erstelle Tabellen (werden in Phase 1 erweitert)
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS player_profiles (
                        uuid TEXT PRIMARY KEY,
                        username TEXT NOT NULL,
                        mana_current INTEGER DEFAULT 0,
                        mana_max INTEGER DEFAULT 100,
                        level INTEGER DEFAULT 1,
                        experience INTEGER DEFAULT 0,
                        experience_next INTEGER DEFAULT 100,
                        skill_points INTEGER DEFAULT 5,
                        skill_levels TEXT DEFAULT '{}',
                        last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            // Migration: fehlende Spalten in bestehenden Tabellen ergänzen
            String[] migrations = {
                    "ALTER TABLE player_profiles ADD COLUMN level INTEGER DEFAULT 1",
                    "ALTER TABLE player_profiles ADD COLUMN experience INTEGER DEFAULT 0",
                    "ALTER TABLE player_profiles ADD COLUMN experience_next INTEGER DEFAULT 100",
                    "ALTER TABLE player_profiles ADD COLUMN skill_points INTEGER DEFAULT 5",
                    "ALTER TABLE player_profiles ADD COLUMN skill_levels TEXT DEFAULT '{}'"
            };
            for (String migration : migrations) {
                try (Statement migrationStmt = conn.createStatement()) {
                    migrationStmt.execute(migration);
                } catch (Exception ignored) {
                    // Spalte existiert bereits
                }
            }

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cooldowns (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        uuid TEXT NOT NULL,
                        ability_type TEXT NOT NULL,
                        cooldown_until TIMESTAMP NOT NULL,
                        UNIQUE(uuid, ability_type)
                    )
                    """);

            // Erstelle Indizes für Performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_uuid ON player_profiles(uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_cooldown_uuid ON cooldowns(uuid)");

            plugin.getLogger().info("✓ Datenbank-Schema initialisiert");
        } catch (Exception e) {
            throw new Exception("Fehler beim Initialisieren des Schemas: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() throws Exception {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        } catch (Exception e) {
            throw new Exception("Fehler beim Trennen der Datenbank: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    @Override
    public void set(String key, String value) throws Exception {
        // Diese Methode wird für die interne DB nicht direkt verwendet
        // Sie dient nur zur Kompatibilität mit dem StorageProvider-Interface
        throw new UnsupportedOperationException("Verwende stattdessen spezifische Repository-Methoden");
    }

    @Override
    public String get(String key) throws Exception {
        // Diese Methode wird für die interne DB nicht direkt verwendet
        throw new UnsupportedOperationException("Verwende stattdessen spezifische Repository-Methoden");
    }

    @Override
    public void delete(String key) throws Exception {
        // Diese Methode wird für die interne DB nicht direkt verwendet
        throw new UnsupportedOperationException("Verwende stattdessen spezifische Repository-Methoden");
    }

    @Override
    public boolean exists(String key) throws Exception {
        // Diese Methode wird für die interne DB nicht direkt verwendet
        throw new UnsupportedOperationException("Verwende stattdessen spezifische Repository-Methoden");
    }

    @Override
    public void setWithExpire(String key, String value, long expirationSeconds) throws Exception {
        // Diese Methode wird für die interne DB nicht direkt verwendet
        throw new UnsupportedOperationException("Verwende stattdessen spezifische Repository-Methoden");
    }

    /**
     * Gibt die HikariDataSource zurück für direkte Datenbankzugriffe.
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
