package com.parcel.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.flywaydb.core.Flyway;

/**
 * PostgreSQL connection pool and schema migration.
 *
 * Settings come from environment variables first, then JVM system properties, then defaults
 * that match docker-compose.yml, so the application runs unchanged on a developer machine
 * (database on localhost:5432) and inside Docker (database host "db").
 *
 *   DB_URL              jdbc:postgresql://localhost:5432/parcels
 *   DB_USER             parcel
 *   DB_PASSWORD         parcel_pass
 *   DB_POOL_SIZE        10
 *   DB_CONNECT_RETRIES  15      (attempts at start-up, 2 seconds apart)
 *   DB_SEED_DEMO        false   (true loads the demo parcels from db/demo)
 *   APP_TIMEZONE        Asia/Kolkata (session time zone, must match the JVM time zone)
 */
public final class Database {
    private static final Logger LOG = Logger.getLogger(Database.class.getName());
    private static volatile HikariDataSource dataSource;

    private Database() {
    }

    /** Waits for the database (with retries), then applies pending Flyway migrations. */
    public static void init() {
        init(intSetting("DB_CONNECT_RETRIES", "db.connect.retries", 15));
    }

    private static synchronized void init(int attempts) {
        if (dataSource != null) {
            return;
        }
        String zone = ZoneId.of(setting("APP_TIMEZONE", "app.timezone", "Asia/Kolkata")).getId();
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            HikariDataSource candidate;
            try {
                candidate = new HikariDataSource(newConfig(zone));
            } catch (RuntimeException e) {
                lastFailure = e;
                LOG.log(Level.WARNING, "Database not reachable (attempt {0} of {1}): {2}",
                        new Object[] {attempt, attempts, rootMessage(e)});
                if (attempt < attempts) {
                    pause(2000);
                }
                continue;
            }
            try {
                migrate(candidate);
            } catch (RuntimeException e) {
                candidate.close();
                throw e;
            }
            dataSource = candidate;
            LOG.log(Level.INFO, "Database ready: {0} as user {1}",
                    new Object[] {candidate.getJdbcUrl(), candidate.getUsername()});
            return;
        }
        throw new IllegalStateException("Database not reachable after " + attempts + " attempt(s)", lastFailure);
    }

    /** Returns a pooled connection. Closing it returns it to the pool. */
    public static Connection getConnection() throws SQLException {
        HikariDataSource ds = dataSource;
        if (ds == null) {
            init(1);
            ds = dataSource;
        }
        return ds.getConnection();
    }

    /** True when the database answers a validity check; used by the /health endpoint. */
    public static boolean ping() {
        try (Connection c = getConnection()) {
            return c.isValid(2);
        } catch (Exception e) {
            LOG.log(Level.FINE, "Database ping failed", e);
            return false;
        }
    }

    public static synchronized void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    private static HikariConfig newConfig(String zone) {
        HikariConfig cfg = new HikariConfig();
        cfg.setPoolName("parcel-pool");
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.setJdbcUrl(setting("DB_URL", "db.url", "jdbc:postgresql://localhost:5432/parcels"));
        cfg.setUsername(setting("DB_USER", "db.user", "parcel"));
        cfg.setPassword(setting("DB_PASSWORD", "db.password", "parcel_pass"));
        cfg.setMaximumPoolSize(intSetting("DB_POOL_SIZE", "db.pool.size", 10));
        cfg.setConnectionTimeout(5000);
        cfg.setConnectionInitSql("SET TIME ZONE '" + zone + "'");
        return cfg;
    }

    private static void migrate(HikariDataSource ds) {
        List<String> locations = new ArrayList<>();
        locations.add("classpath:db/migration");
        if (Boolean.parseBoolean(setting("DB_SEED_DEMO", "db.seed.demo", "false"))) {
            locations.add("classpath:db/demo");
        }
        Flyway.configure()
                .dataSource(ds)
                .locations(locations.toArray(new String[0]))
                .load()
                .migrate();
    }

    private static String setting(String env, String property, String fallback) {
        String value = System.getenv(env);
        if (value == null || value.isBlank()) {
            value = System.getProperty(property);
        }
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static int intSetting(String env, String property, int fallback) {
        try {
            return Integer.parseInt(setting(env, property, String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String rootMessage(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the database", e);
        }
    }
}
