package de.cottisystem.zauberstab.config;


/**
 * Konfigurationsklasse für Storage-Einstellungen.
 */
public class StorageConfig {

    public enum StorageType {
        REDIS,
        INTERNAL
    }

    private StorageType storageType;
    
    // Redis-Einstellungen
    private String redisHost;
    private int redisPort;
    private String redisPassword;
    private int redisDatabase;
    
    // Interne DB-Einstellungen
    private String internalDbType; // SQLITE oder H2
    private String internalDbPath;

    // Getter und Setter
    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public int getRedisDatabase() {
        return redisDatabase;
    }

    public void setRedisDatabase(int redisDatabase) {
        this.redisDatabase = redisDatabase;
    }

    public String getInternalDbType() {
        return internalDbType;
    }

    public void setInternalDbType(String internalDbType) {
        this.internalDbType = internalDbType;
    }

    public String getInternalDbPath() {
        return internalDbPath;
    }

    public void setInternalDbPath(String internalDbPath) {
        this.internalDbPath = internalDbPath;
    }
}
