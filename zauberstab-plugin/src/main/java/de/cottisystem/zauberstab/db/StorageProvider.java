package de.cottisystem.zauberstab.db;

/**
 * Interface für Storage-Provider (Redis oder interne DB).
 * Alle Datenbankoperationen laufen über diese abstrakte Schnittstelle.
 */
public interface StorageProvider {

    /**
     * Verbindet sich mit dem Storage-Backend.
     */
    void connect() throws Exception;

    /**
     * Trennt die Verbindung zum Storage-Backend.
     */
    void disconnect() throws Exception;

    /**
     * Prüft, ob die Verbindung aktiv ist.
     */
    boolean isConnected();

    /**
     * Speichert einen String-Wert.
     */
    void set(String key, String value) throws Exception;

    /**
     * Ruft einen String-Wert ab.
     */
    String get(String key) throws Exception;

    /**
     * Löscht einen Wert.
     */
    void delete(String key) throws Exception;

    /**
     * Prüft, ob ein Schlüssel existiert.
     */
    boolean exists(String key) throws Exception;

    /**
     * Speichert einen Wert mit Ablaufzeit (in Sekunden).
     */
    void setWithExpire(String key, String value, long expirationSeconds) throws Exception;
}
