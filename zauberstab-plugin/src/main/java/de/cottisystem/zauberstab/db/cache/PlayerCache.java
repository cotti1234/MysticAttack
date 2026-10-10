package de.cottisystem.zauberstab.db.cache;

import de.cottisystem.zauberstab.model.PlayerProfile;
import de.cottisystem.zauberstab.db.repository.PlayerRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.*;

/**
 * In-Memory-Cache für aktive Spieler-Profile.
 * Lädt Profile beim Login, hält sie im RAM und persistiert sie periodisch.
 */
public class PlayerCache {

    private final JavaPlugin plugin;
    private final PlayerRepository repository;
    private final ConcurrentHashMap<UUID, PlayerProfile> cache;
    private final ScheduledExecutorService scheduler;
    private final long saveIntervalMillis;

    public PlayerCache(JavaPlugin plugin, PlayerRepository repository, long saveIntervalMillis) {
        this.plugin = plugin;
        this.repository = repository;
        this.cache = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "Zauberstab-Cache-Saver");
            t.setDaemon(true);
            return t;
        });
        this.saveIntervalMillis = saveIntervalMillis;
        
        startPeriodicSave();
    }

    /**
     * Lädt ein Spieler-Profil in den Cache.
     */
    public CompletableFuture<PlayerProfile> loadPlayer(UUID uuid, String username) {
        // Prüfe, ob bereits im Cache
        PlayerProfile cached = cache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        // Lade aus Repository
        return repository.loadPlayer(uuid).thenCompose(optionalProfile -> {
            PlayerProfile profile;
            if (optionalProfile.isPresent()) {
                profile = optionalProfile.get();
            } else {
                // Erstelle neues Profil
                return repository.createPlayer(uuid, username).thenApply(newProfile -> {
                    cache.put(uuid, newProfile);
                    return newProfile;
                });
            }
            
            cache.put(uuid, profile);
            return CompletableFuture.completedFuture(profile);
        });
    }

    /**
     * Gibt ein Profil aus dem Cache zurück.
     */
    public Optional<PlayerProfile> getProfile(UUID uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }

    /**
     * Entfernt ein Profil aus dem Cache und speichert es.
     */
    public CompletableFuture<Void> unloadPlayer(UUID uuid) {
        PlayerProfile profile = cache.remove(uuid);
        if (profile != null && profile.isDirty()) {
            return repository.savePlayer(profile);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Startet die periodische Speicherung aller geänderten Profile.
     */
    private void startPeriodicSave() {
        scheduler.scheduleAtFixedRate(
                this::saveAllDirtyProfiles,
                saveIntervalMillis,
                saveIntervalMillis,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Speichert alle geänderten Profile.
     */
    private void saveAllDirtyProfiles() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (PlayerProfile profile : cache.values()) {
            if (profile.isDirty()) {
                futures.add(repository.savePlayer(profile));
            }
        }

        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .exceptionally(ex -> {
                        plugin.getLogger().warning("Fehler beim Speichern von Spieler-Profilen: " + ex.getMessage());
                        return null;
                    });
        }
    }

    /**
     * Speichert alle Profile und beendet den Cache.
     */
    public void shutdown() {
        // Speichere alle Profile
        saveAllDirtyProfiles();
        
        // Warte kurz, damit alle Saves abgeschlossen sind
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        cache.clear();
    }

    /**
     * Gibt die Anzahl der gecachten Profile zurück.
     */
    public int getCachedPlayerCount() {
        return cache.size();
    }
}
