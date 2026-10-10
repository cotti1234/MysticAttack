package de.cottisystem.zauberstab.db;

import de.cottisystem.zauberstab.config.StorageConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis-basierter Storage-Provider.
 * Nutzt Jedis als Redis-Client mit Connection Pooling.
 */
public class RedisStorageProvider implements StorageProvider {

    private final StorageConfig config;
    private JedisPool jedisPool;

    public RedisStorageProvider(StorageConfig config) {
        this.config = config;
    }

    @Override
    public void connect() throws Exception {
        try {
            // Konfiguriere Connection Pool
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(2);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            poolConfig.setMinEvictableIdleTimeMillis(60000);
            poolConfig.setTimeBetweenEvictionRunsMillis(30000);
            poolConfig.setNumTestsPerEvictionRun(3);

            // Erstelle Jedis Pool
            if (config.getRedisPassword() != null && !config.getRedisPassword().isEmpty()) {
                jedisPool = new JedisPool(poolConfig, config.getRedisHost(), config.getRedisPort(),
                        2000, config.getRedisPassword());
            } else {
                jedisPool = new JedisPool(poolConfig, config.getRedisHost(), config.getRedisPort());
            }

            // Teste Verbindung
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.select(config.getRedisDatabase());
                jedis.ping();
            }
        } catch (Exception e) {
            throw new Exception("Fehler beim Verbinden mit Redis: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() throws Exception {
        try {
            if (jedisPool != null && !jedisPool.isClosed()) {
                jedisPool.close();
            }
        } catch (Exception e) {
            throw new Exception("Fehler beim Trennen von Redis: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return jedisPool != null && !jedisPool.isClosed();
    }

    @Override
    public void set(String key, String value) throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        } catch (Exception e) {
            throw new Exception("Fehler beim Speichern in Redis: " + e.getMessage(), e);
        }
    }

    @Override
    public String get(String key) throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            throw new Exception("Fehler beim Abrufen aus Redis: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String key) throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            throw new Exception("Fehler beim Löschen aus Redis: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String key) throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        } catch (Exception e) {
            throw new Exception("Fehler beim Prüfen in Redis: " + e.getMessage(), e);
        }
    }

    @Override
    public void setWithExpire(String key, String value, long expirationSeconds) throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, expirationSeconds, value);
        } catch (Exception e) {
            throw new Exception("Fehler beim Speichern mit Ablauf in Redis: " + e.getMessage(), e);
        }
    }
}
