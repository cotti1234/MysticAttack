package de.cottisystem.zauberstab.db.repository;

import de.cottisystem.zauberstab.model.PlayerProfile;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Repository-Interface für Spieler-Daten.
 * Abstrahiert den Zugriff auf die Datenbank (Redis oder interne DB).
 */
public interface PlayerRepository {

    /**
     * Lädt ein Spieler-Profil asynchron.
     */
    CompletableFuture<Optional<PlayerProfile>> loadPlayer(UUID uuid);

    /**
     * Speichert ein Spieler-Profil asynchron.
     */
    CompletableFuture<Void> savePlayer(PlayerProfile profile);

    /**
     * Erstellt ein neues Spieler-Profil.
     */
    CompletableFuture<PlayerProfile> createPlayer(UUID uuid, String username);

    /**
     * Prüft, ob ein Spieler existiert.
     */
    CompletableFuture<Boolean> playerExists(UUID uuid);

    /**
     * Löscht ein Spieler-Profil.
     */
    CompletableFuture<Void> deletePlayer(UUID uuid);
}
