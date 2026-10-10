package de.cottisystem.zauberstab.db.repository;

import de.cottisystem.zauberstab.model.PlayerProfile;
import de.cottisystem.zauberstab.db.InternalStorageProvider;
import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;

import com.google.gson.reflect.TypeToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Interne Datenbank-basiertes Player-Repository (SQLite/H2).
 * Speichert Spieler-Profile in der lokalen Datenbank.
 */
public class InternalPlayerRepository implements PlayerRepository {

    private final HikariDataSource dataSource;
    private final Executor executor;
    private final Gson gson;

    public InternalPlayerRepository(InternalStorageProvider storageProvider, Executor executor) {
        this.dataSource = storageProvider.getDataSource();
        this.executor = executor;
        this.gson = new Gson();
    }

    @Override
    public CompletableFuture<Optional<PlayerProfile>> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT uuid, username, mana_current, mana_max, level, experience, experience_next, skill_points, skill_levels, last_updated FROM player_profiles WHERE uuid = ?")) {
                
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        PlayerProfile profile = new PlayerProfile();
                        profile.setUuid(UUID.fromString(rs.getString("uuid")));
                        profile.setUsername(rs.getString("username"));
                        profile.setManaCurrent(rs.getInt("mana_current"));
                        profile.setManaMax(rs.getInt("mana_max"));
                        profile.setLevel(Math.max(1, rs.getInt("level")));
                        profile.setExperience(rs.getInt("experience"));
                        profile.setExperienceForNextLevel(Math.max(1, rs.getInt("experience_next")));
                        profile.setSkillPoints(rs.getInt("skill_points"));
                        profile.setSkillLevels(parseSkillLevels(rs.getString("skill_levels")));
                        profile.setLastUpdated(rs.getLong("last_updated"));
                        profile.setDirty(false);
                        return Optional.of(profile);
                    }
                }
                return Optional.empty();
            } catch (Exception e) {
                throw new RuntimeException("Fehler beim Laden des Spieler-Profils: " + e.getMessage(), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> savePlayer(PlayerProfile profile) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT OR REPLACE INTO player_profiles (uuid, username, mana_current, mana_max, level, experience, experience_next, skill_points, skill_levels, last_updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                stmt.setString(1, profile.getUuid().toString());
                stmt.setString(2, profile.getUsername());
                stmt.setInt(3, profile.getManaCurrent());
                stmt.setInt(4, profile.getManaMax());
                stmt.setInt(5, profile.getLevel());
                stmt.setInt(6, profile.getExperience());
                stmt.setInt(7, profile.getExperienceForNextLevel());
                stmt.setInt(8, profile.getSkillPoints());
                stmt.setString(9, gson.toJson(profile.getSkillLevels()));
                stmt.setLong(10, System.currentTimeMillis());
                
                stmt.executeUpdate();
                profile.setDirty(false);
            } catch (Exception e) {
                throw new RuntimeException("Fehler beim Speichern des Spieler-Profils: " + e.getMessage(), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<PlayerProfile> createPlayer(UUID uuid, String username) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO player_profiles (uuid, username, mana_current, mana_max, last_updated) VALUES (?, ?, ?, ?, ?)")) {
                
                PlayerProfile profile = new PlayerProfile();
                profile.setUuid(uuid);
                profile.setUsername(username);
                profile.setManaCurrent(100);
                profile.setManaMax(100);
                profile.setLastUpdated(System.currentTimeMillis());
                
                stmt.setString(1, uuid.toString());
                stmt.setString(2, username);
                stmt.setInt(3, 100);
                stmt.setInt(4, 100);
                stmt.setLong(5, System.currentTimeMillis());
                
                stmt.executeUpdate();
                return profile;
            } catch (Exception e) {
                throw new RuntimeException("Fehler beim Erstellen des Spieler-Profils: " + e.getMessage(), e);
            }
        }, executor);
    }

    /**
     * Parst die Skill-Level-Map aus dem JSON-String.
     */
    private Map<String, Integer> parseSkillLevels(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            Map<String, Integer> map = gson.fromJson(json, new TypeToken<Map<String, Integer>>() {}.getType());
            return map != null ? map : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @Override
    public CompletableFuture<Boolean> playerExists(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT 1 FROM player_profiles WHERE uuid = ? LIMIT 1")) {
                
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            } catch (Exception e) {
                throw new RuntimeException("Fehler beim Prüfen des Spieler-Profils: " + e.getMessage(), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> deletePlayer(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM player_profiles WHERE uuid = ?")) {
                
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException("Fehler beim Löschen des Spieler-Profils: " + e.getMessage(), e);
            }
        }, executor);
    }
}
