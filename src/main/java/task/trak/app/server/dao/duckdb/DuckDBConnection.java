package task.trak.app.server.dao.duckdb;

import task.trak.app.client.cli.TTApp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DuckDBConnection {

    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                String path = TTApp.storedir + "/trak.duckdb";
                connection = DriverManager.getConnection("jdbc:duckdb:" + path);
                createTables();
            } catch (SQLException e) {
                System.err.println("Failed to connect to DuckDB: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return connection;
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_name VARCHAR PRIMARY KEY,
                        id BIGINT,
                        first_name VARCHAR,
                        last_name VARCHAR,
                        email VARCHAR,
                        password_hash VARCHAR,
                        tasks VARCHAR,
                        projects VARCHAR
                    )""");

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS projects (
                        id BIGINT PRIMARY KEY,
                        project_name VARCHAR,
                        summary VARCHAR,
                        created_at BIGINT,
                        owner_json VARCHAR,
                        members_json VARCHAR
                    )""");

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS tasks (
                        id BIGINT PRIMARY KEY,
                        project_name VARCHAR,
                        assigned_to VARCHAR,
                        title VARCHAR,
                        status VARCHAR,
                        created_at BIGINT,
                        completed_at BIGINT,
                        summary VARCHAR,
                        deadline BIGINT,
                        estimate VARCHAR,
                        time_started BIGINT,
                        time_spent_ms BIGINT
                    )""");

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS sprints (
                        id BIGINT PRIMARY KEY,
                        project_name VARCHAR,
                        name VARCHAR,
                        task_ids VARCHAR,
                        start_date BIGINT,
                        end_date BIGINT
                    )""");

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS backlogs (
                        id BIGINT PRIMARY KEY,
                        name VARCHAR,
                        project_name VARCHAR,
                        task_ids VARCHAR,
                        created_at BIGINT
                    )""");
        }
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Failed to close DuckDB connection: " + e.getMessage());
            }
            connection = null;
        }
    }
}
