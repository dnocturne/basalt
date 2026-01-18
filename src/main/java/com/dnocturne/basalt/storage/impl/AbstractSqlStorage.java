package com.dnocturne.basalt.storage.impl;

import com.dnocturne.basalt.storage.PlayerData;
import com.dnocturne.basalt.storage.Storage;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base class for SQL-based storage implementations.
 *
 * <p>Provides connection management, async execution via virtual threads,
 * and common SQL patterns. Subclasses implement dialect-specific SQL
 * and data serialization.</p>
 *
 * @param <T> The type of player data
 */
public abstract class AbstractSqlStorage<T extends PlayerData> implements Storage<T> {

    /**
     * Virtual thread executor for non-blocking database I/O.
     */
    protected static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    protected final Plugin plugin;
    protected final Logger logger;
    protected Connection connection;

    protected AbstractSqlStorage(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Get the connection, throwing if not available.
     */
    protected Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection not initialized");
        }
        if (connection.isClosed()) {
            throw new SQLException("Database connection is closed");
        }
        return connection;
    }

    /**
     * Create the database tables. Called during init().
     */
    protected abstract void createTables() throws SQLException;

    /**
     * Serialize player data to be stored.
     */
    protected abstract void serializeToStatement(PreparedStatement stmt, T data) throws SQLException;

    /**
     * Deserialize player data from a result set.
     */
    protected abstract T deserializeFromResultSet(ResultSet rs) throws SQLException;

    /**
     * Get the SQL for selecting by UUID.
     */
    protected abstract String getSelectByUuidSql();

    /**
     * Get the SQL for selecting by username.
     */
    protected abstract String getSelectByNameSql();

    /**
     * Get the SQL for upserting data.
     */
    protected abstract String getUpsertSql();

    /**
     * Get the SQL for deleting by UUID.
     */
    protected abstract String getDeleteSql();

    /**
     * Get the SQL for checking existence.
     */
    protected abstract String getExistsSql();

    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    logger.info(getType() + " connection closed");
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing " + getType() + " connection", e);
            }
        }, EXECUTOR);
    }

    @Override
    public CompletableFuture<Optional<T>> load(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection conn = requireConnection();
                try (PreparedStatement stmt = conn.prepareStatement(getSelectByUuidSql())) {
                    stmt.setString(1, uuid.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return Optional.of(deserializeFromResultSet(rs));
                        }
                        return Optional.empty();
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load player " + uuid, e);
                return Optional.empty();
            }
        }, EXECUTOR);
    }

    @Override
    public CompletableFuture<Optional<T>> loadByName(@NotNull String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection conn = requireConnection();
                try (PreparedStatement stmt = conn.prepareStatement(getSelectByNameSql())) {
                    stmt.setString(1, username);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return Optional.of(deserializeFromResultSet(rs));
                        }
                        return Optional.empty();
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load player by name " + username, e);
                return Optional.empty();
            }
        }, EXECUTOR);
    }

    @Override
    public CompletableFuture<Void> save(@NotNull T data) {
        return CompletableFuture.runAsync(() -> {
            try {
                Connection conn = requireConnection();
                try (PreparedStatement stmt = conn.prepareStatement(getUpsertSql())) {
                    serializeToStatement(stmt, data);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to save player " + data.getUuid(), e);
            }
        }, EXECUTOR);
    }

    @Override
    public CompletableFuture<Void> delete(@NotNull UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try {
                Connection conn = requireConnection();
                try (PreparedStatement stmt = conn.prepareStatement(getDeleteSql())) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete player " + uuid, e);
            }
        }, EXECUTOR);
    }

    @Override
    public CompletableFuture<Boolean> exists(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection conn = requireConnection();
                try (PreparedStatement stmt = conn.prepareStatement(getExistsSql())) {
                    stmt.setString(1, uuid.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next();
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to check player existence " + uuid, e);
                return false;
            }
        }, EXECUTOR);
    }
}
