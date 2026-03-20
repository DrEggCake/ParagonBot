package com.dreggcake.src.Database;

import java.sql.*;

public class DAO {

    private final String url;
    private Connection conn;

    public DAO(String url) {
        this.url = url;

        try {
            init();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the database connection
     * Call this once from your Main class after loading config
     */
    public void init() throws SQLException {
        if (conn != null && !conn.isClosed()) return; // Already connected
        conn = DriverManager.getConnection(url);
        System.out.println(url + " DAO connected!");
    }

    /**
     * Get the raw connection if you really need it
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Prepare a statement easily
     */
    public PreparedStatement prepare(String sql) throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("DAO connection is not initialized!");
        }
        return conn.prepareStatement(sql);
    }

    public Statement createStatement() throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("DAO connection is not initialized!");
        }
        return conn.createStatement();
    }

    /**
     * Execute a query (SELECT)
     */
    public <T> T query(
            String sql,
            ResultHandler<T> handler,
            Object... params
    ) throws SQLException {

        try (PreparedStatement stmt = prepare(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return handler.handle(rs);
            }
        }
    }

    /**
     * Execute an update (INSERT, UPDATE, DELETE) and return affected rows
     */
    public int update(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = prepare(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        }
    }

    /**
     * Close the connection (call on server shutdown)
     */
    public void close() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("DAO connection closed!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
