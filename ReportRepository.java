import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {
    private Connection connection;

    public ReportRepository() {
        connect();
        createTables();
        seedData();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:molms.db");
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    private void createTables() {
        String createUsersTable =
                "CREATE TABLE IF NOT EXISTS users (" +
                        "username TEXT PRIMARY KEY, " +
                        "password TEXT, " +
                        "rank TEXT)";

        String createAuditLogsTable =
                "CREATE TABLE IF NOT EXISTS audit_logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user TEXT, " +
                        "action TEXT, " +
                        "time TEXT)";

        String createReservationsTable =
                "CREATE TABLE IF NOT EXISTS reservations (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "unit_id TEXT, " +
                        "occupant_name TEXT, " +
                        "occupant_id TEXT, " +
                        "occupant_rank TEXT, " +
                        "room_number TEXT, " +
                        "status TEXT, " +
                        "weapons_clearance TEXT, " +
                        "check_in_date TEXT)";

        String createRoomsTable =
                "CREATE TABLE IF NOT EXISTS rooms (" +
                        "room_number TEXT PRIMARY KEY, " +
                        "status TEXT)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createAuditLogsTable);
            stmt.execute(createReservationsTable);
            stmt.execute(createRoomsTable);

            stmt.execute("INSERT OR IGNORE INTO rooms VALUES ('101', 'Ready')");
            stmt.execute("INSERT OR IGNORE INTO rooms VALUES ('102', 'Ready')");
            stmt.execute("INSERT OR IGNORE INTO rooms VALUES ('200', 'Ready')");
            stmt.execute("INSERT OR IGNORE INTO rooms VALUES ('300', 'Ready')");
            stmt.execute("INSERT OR IGNORE INTO rooms VALUES ('Barracks Bunk 2', 'Ready')");
            stmt.execute("INSERT OR IGNORE INTO rooms VALUES ('Barracks Bunk 4', 'Ready')");
        } catch (SQLException e) {
            System.out.println("Table creation error: " + e.getMessage());
        }
    }

private void seedData() {
        try {
            PreparedStatement checkStmt = connection.prepareStatement("SELECT COUNT(*) FROM users");
            ResultSet rs = checkStmt.executeQuery();
            int count = rs.getInt(1);

            if (count == 0) {
                String insertUser = "INSERT INTO users (username, password, rank) VALUES (?, ?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(insertUser);

                pstmt.setString(1, "Ciara");
                pstmt.setString(2, hashPassword("admin123"));
                pstmt.setString(3, "CPT");
                pstmt.executeUpdate();

                pstmt.setString(1, "Aldrian");
                pstmt.setString(2, hashPassword("user123"));
                pstmt.setString(3, "PVT");
                pstmt.executeUpdate();

                pstmt.setString(1, "SgtMiller");
                pstmt.setString(2, hashPassword("nco123"));
                pstmt.setString(3, "SGT");
                pstmt.executeUpdate();

                System.out.println("Seed data inserted successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Seed data error: " + e.getMessage());
        }
    }
