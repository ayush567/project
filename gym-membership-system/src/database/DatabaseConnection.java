package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Singleton Pattern: Manages a single shared database connection
public class DatabaseConnection {

    // ==========================================================
    // CHANGE DATABASE CREDENTIALS HERE EASILY IN ONE PLACE
    // ==========================================================
    private static final String URL = "jdbc:mysql://localhost:3306/gym_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "ayush@007";

    // Singleton instance
    private static DatabaseConnection instance;
    private Connection connection;

    // Private constructor prevents external instantiation
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Please add mysql-connector-j.jar to your classpath.");
        }
    }

    // Thread-safe access to the singleton instance
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // Returns active connection, re-establishing if closed or null
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
        return connection;
    }

    // Safely closes connection when application terminates
    public synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    // Tests whether connection succeeds
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}