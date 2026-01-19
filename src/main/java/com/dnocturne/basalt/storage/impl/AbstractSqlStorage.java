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
 * and common SQL patterns. Supports both simple single-table schemas and
 * complex multi-table schemas with transactions.</p>
 *
 * <p>For simple single-table storage, implement the abstract SQL methods.
 * For complex multi-table storage, override the CRUD methods directly.</p>
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

    // ============================================================
    // Connection management
    // ============================================================

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
     * Called after connection is established. Override for dialect-specific setup
     * (e.g., SQLite PRAGMA statements).
     */
    protected void onConnectionEstablished() throws SQLException {
        // Override if needed
    }

    /**
     * Perform any necessary database migrations.
     */
    protected void migrateDatabase() throws SQLException {
        // Override if needed
    }

    // ============================================================
    // Abstract methods - subclasses must implement
    // ============================================================

    /**
     * Create the database tables. Called during init().
     */
    protected abstract void createTables() throws SQLException;

    // ============================================================
    // SQL methods - override for simple single-table storage
    // ============================================================

    /**
     * Serialize player data to a prepared statement.
     * Override for simple single-table storage.
     */
    protected void serializeToStatement(PreparedStatement stmt, T data) throws SQLException {
        throw new UnsupportedOperationException("Override serializeToStatement or save() method");
    }

    /**
     * Deserialize player data from a result set.
     * Override for simple single-table storage.
     */
    protected T deserializeFromResultSet(ResultSet rs) throws SQLException {
        throw new UnsupportedOperationException("Override deserializeFromResultSet or load() method");
    }

    /**
     * Get the SQL for selecting by UUID.
     */
    protected String getSelectByUuidSql() {
        throw new UnsupportedOperationException("Override getSelectByUuidSql or load() method");
    }

    /**
     * Get the SQL for selecting by username.
     */
    protected String getSelectByNameSql() {
        throw new UnsupportedOperationException("Override getSelectByNameSql or loadByName() method");
    }

    /**
     * Get the SQL for upserting data.
     */
    protected String getUpsertSql() {
        throw new UnsupportedOperationException("Override getUpsertSql or save() method");
    }

    /**
     * Get the SQL for deleting by UUID.
     */
    protected String getDeleteSql() {
        throw new UnsupportedOperationException("Override getDeleteSql or delete() method");
    }

    /**
     * Get the SQL for checking existence.
     */
    protected String getExistsSql() {
        throw new UnsupportedOperationException("Override getExistsSql or exists() method");
    }

    // ============================================================
    // Storage interface - override for complex multi-table storage
    // ============================================================

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
                return loadSync(uuid);
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
                return loadByNameSync(username);
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
                saveSync(data);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to save player " + data.getUuid(), e);
            }
        }, EXECUTOR);
    }

    @Override
    public CompletableFuture<Void> delete(@NotNull UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try {
                deleteSync(uuid);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete player " + uuid, e);
            }
        }, EXECUTOR);
    }

    @Override
    public CompletableFuture<Boolean> exists(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return existsSync(uuid);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to check player existence " + uuid, e);
                return false;
            }
        }, EXECUTOR);
    }

    // ============================================================
    // Synchronous methods - override for custom implementations
    // ============================================================

    /**
     * Synchronous load implementation. Override for complex schemas.
     */
    protected Optional<T> loadSync(@NotNull UUID uuid) throws SQLException {
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
    }

    /**
     * Synchronous loadByName implementation. Override for complex schemas.
     */
    protected Optional<T> loadByNameSync(@NotNull String username) throws SQLException {
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
    }

    /**
     * Synchronous save implementation. Override for complex schemas or transactions.
     */
    protected void saveSync(@NotNull T data) throws SQLException {
        Connection conn = requireConnection();
        try (PreparedStatement stmt = conn.prepareStatement(getUpsertSql())) {
            serializeToStatement(stmt, data);
            stmt.executeUpdate();
        }
    }

    /**
     * Synchronous delete implementation. Override for complex schemas.
     */
    protected void deleteSync(@NotNull UUID uuid) throws SQLException {
        Connection conn = requireConnection();
        try (PreparedStatement stmt = conn.prepareStatement(getDeleteSql())) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        }
    }

    /**
     * Synchronous exists implementation. Override for complex schemas.
     */
    protected boolean existsSync(@NotNull UUID uuid) throws SQLException {
        Connection conn = requireConnection();
        try (PreparedStatement stmt = conn.prepareStatement(getExistsSql())) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ============================================================
    // Utility methods
    // ============================================================

    /**
     * Execute a block within a transaction. Rolls back on exception.
     *
     * @param action The action to execute
     * @throws SQLException if the action fails
     */
    protected void executeInTransaction(SqlAction action) throws SQLException {
        Connection conn = requireConnection();
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            action.execute();
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                logger.log(Level.SEVERE, "Rollback failed", rollbackEx);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to reset auto-commit", e);
            }
        }
    }

    /**
     * Functional interface for transaction actions.
     */
    @FunctionalInterface
    protected interface SqlAction {
        void execute() throws SQLException;
    }
}
