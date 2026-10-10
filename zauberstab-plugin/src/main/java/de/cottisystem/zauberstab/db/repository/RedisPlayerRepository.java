package de.cottisystem.zauberstab.db.repository;

import de.cottisystem.zauberstab.model.PlayerProfile;
import de.cottisystem.zauberstab.db.StorageProvider;
import com.google.gson.Gson;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Redis-basiertes Player-Repository.
 * Speichert Spieler-Profile als JSON in Redis.
 */
public class RedisPlayerRepository implements PlayerRepository {

    private final StorageProvider storageProvider;
    private final Executor executor;
    private final Gson gson;
    private static final String KEY_PREFIX = "player:";

    public RedisPlayerRepository(StorageProvider storageProvider, Executor executor) {
        this.storageProvider = storageProvider;
        this.executor = executor;
        this.gson = new Gson();
    }

    @Override
    public CompletableFuture<Optional<PlayerProfile>> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = storageProvider.get(KEY_PREFIX + uuid);
                if (json == null) {
                    return Optional.empty();
                }
                PlayerProfile profile = gson.fromJson(json, PlayerProfile.class);
                profile.setDirty(false);
                return Optional.of(profile);
            } catch (Exception e) {
                throw new RuntimeException("Fehler beim Laden des Spieler-Profils: " + e.getMessage(), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> savePlayer(PlayerProfile profile) {
        return CompletableFuture.runAsync(() -> {
            try {
                String json = gson.toJson(profile);
                storageProvider.set(KEY_PREFIX + profile.getUuid(), json);
                profile.setDirty(false);
            } catch (Exception e) {
                throw new RuntimeException("Fehler beim Speichern des Spieler-Profils: " + e.getMessage(), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<PlayerProfile> createPlayer(UUID uuid, String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerProfile profile = new PlayerProfile();
                profile.setUuid(uuid);
                profile.setUsername(username);
                profile.setManaCurrent(100);
                profile.setManaMax(100);
                profile.setLastUpdated(System.currentTimeMillis());
                
                String json = gson.toJson(profile);
                storageProvider.set(KEY_PREFIX + uuid, json);
                
                return profile;
            } catch (Exception e) {
                throw new RuntimeException("Fehler beim Erstellen des Spieler-Profils: " + e.getMessage(), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> playerExists(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return storageProvider.exists(KEY_PREFIX + uuid);
            } catch (Exception e) {
                throw new RuntimeException("Fehler beim Prüfen des Spieler-Profils: " + e.getMessage(), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> deletePlayer(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try {
                storageProvider.delete(KEY_PREFIX + uuid);
            } catch (Exception e) {
                throw new RuntimeException("Fehler beim Löschen des Spieler-Profils: " + e.getMessage(), e);
            }
        }, executor);
    }
}
