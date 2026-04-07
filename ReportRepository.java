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

public UserSession authenticate(String username, String password) {
        String query = "SELECT rank FROM users WHERE username = ? COLLATE NOCASE AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new UserSession(username, rs.getString("rank"));
            }
        } catch (SQLException e) {
            System.out.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    public void saveLog(String user, String action) {
        String query = "INSERT INTO audit_logs (user, action, time) VALUES (?, ?, datetime('now'))";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user);
            pstmt.setString(2, action);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Log save error: " + e.getMessage());
        }
    }

    public void saveReservation(String unitId, ServiceMember member, String roomNumber) {
        String query =
                "INSERT INTO reservations (unit_id, occupant_name, occupant_id, occupant_rank, " +
                        "room_number, status, weapons_clearance, check_in_date) " +
                        "VALUES (?, ?, ?, ?, ?, 'ACTIVE', 'PENDING', date('now'))";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitId);
            pstmt.setString(2, member.getName());
            pstmt.setString(3, member.getId());
            pstmt.setString(4, member.getRank());
            pstmt.setString(5, roomNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Save reservation error: " + e.getMessage());
        }
    }

    public void modifyReservation(String unitId, String newRoom, String occupantName) {
        String query =
                "UPDATE reservations SET room_number = ?, occupant_name = ? " +
                        "WHERE unit_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newRoom);
            pstmt.setString(2, occupantName);
            pstmt.setString(3, unitId);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                System.out.println("|  |    No active reservation found for Unit ID: " + unitId);
            } else {
                updateRoomStatus(newRoom, "Occupied");
            }
        } catch (SQLException e) {
            System.out.println("Modify reservation error: " + e.getMessage());
        }
    }

public void cancelReservation(String unitId) {
        String roomNumber = getRoomNumberForUnit(unitId);

        String query =
                "UPDATE reservations SET status = 'CANCELLED' " +
                        "WHERE unit_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitId);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                System.out.println("|  |    No active reservation found for Unit ID: " + unitId);
            } else if (roomNumber != null) {
                updateRoomStatus(roomNumber, "Ready");
            }
        } catch (SQLException e) {
            System.out.println("Cancel reservation error: " + e.getMessage());
        }
    }

    public String getRoomNumberForUnit(String unitId) {
        String query = "SELECT room_number FROM reservations WHERE unit_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("room_number");
        } catch (SQLException e) {
            System.out.println("Get room error: " + e.getMessage());
        }
        return null;
    }

    public String getOccupantNameForUnit(String unitId) {
        String query = "SELECT occupant_name FROM reservations WHERE unit_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("occupant_name");
        } catch (SQLException e) {
            System.out.println("Get occupant error: " + e.getMessage());
        }
        return null;
    }

    public void updateWeaponsClearance(String unitId, String status) {
        String query =
                "UPDATE reservations SET weapons_clearance = ? " +
                        "WHERE unit_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setString(2, unitId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update weapons clearance error: " + e.getMessage());
        }
    }

    public void releaseQuarters(String unitId) {
        String query =
                "UPDATE reservations SET status = 'CHECKED_OUT' " +
                        "WHERE unit_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Release quarters error: " + e.getMessage());
        }
    }

    public void searchReservationByUnitId(String unitId) {
        String query =
                "SELECT occupant_name, occupant_rank, room_number, status " +
                        "FROM reservations WHERE unit_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, unitId);
            ResultSet rs = pstmt.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("|  |    Found: " + rs.getString("occupant_name") +
                        " (" + rs.getString("occupant_rank") + ") in Room " + rs.getString("room_number"));
                System.out.println("|  |    Status: " + rs.getString("status"));
            }
            if (!found) {
                System.out.println("|  |    No reservation found for Unit ID: " + unitId);
            }
        } catch (SQLException e) {
            System.out.println("Search error: " + e.getMessage());
        }
    }

    public void updateRoomStatus(String roomNumber, String status) {
        String query = "UPDATE rooms SET status = ? WHERE room_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setString(2, roomNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update room status error: " + e.getMessage());
        }
    }

    public void printAllRooms() {
        String query = "SELECT room_number, status FROM rooms ORDER BY room_number";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("|  |    Room " + rs.getString("room_number") +
                        ": " + rs.getString("status"));
            }
            if (!found) System.out.println("|  |    No rooms found.");
        } catch (SQLException e) {
            System.out.println("Get rooms error: " + e.getMessage());
        }
    }

    public List<AuditLog> getAllAuditLogs() {
        List<AuditLog> logs = new ArrayList<>();
        String query = "SELECT id, user, action, time FROM audit_logs ORDER BY time DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                logs.add(new AuditLog(
                        rs.getInt("id"), rs.getString("user"),
                        rs.getString("action"), rs.getString("time")));
            }
        } catch (SQLException e) {
            System.out.println("Get audit logs error: " + e.getMessage());
        }
        return logs;
    }

    public List<AuditLog> getAuditLogsByDateRange(String startDate, String endDate) {
        List<AuditLog> logs = new ArrayList<>();
        String query =
                "SELECT id, user, action, time FROM audit_logs " +
                        "WHERE date(time) BETWEEN ? AND ? ORDER BY time DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(new AuditLog(
                        rs.getInt("id"), rs.getString("user"),
                        rs.getString("action"), rs.getString("time")));
            }
        } catch (SQLException e) {
            System.out.println("Get audit logs by date error: " + e.getMessage());
        }
        return logs;
    }
