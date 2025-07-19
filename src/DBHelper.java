import java.sql.*;
import java.util.*;

public class DBHelper {
    private static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:booking.db");
    }

    public static void saveBooking(String name, String film, String date, String time, List<String> seats) {
        try (Connection conn = connect()) {

            // Ambil ID terakhir
            String lastId = null;
            Statement stmt1 = conn.createStatement();
            ResultSet rs = stmt1.executeQuery("SELECT id FROM bookings ORDER BY id DESC LIMIT 1");
            if (rs.next()) {
                lastId = rs.getString("id");
            }

            int lastNumber = 0;
            if (lastId != null && lastId.startsWith("Reg")) {
                try {
                    lastNumber = Integer.parseInt(lastId.substring(3));
                } catch (NumberFormatException e) {
                    lastNumber = 0;
                }
            }

            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO bookings (id, name, film, date, time, seat) VALUES (?, ?, ?, ?, ?, ?)"
            );

            for (String seat : seats) {
                lastNumber++; // Naikkan untuk setiap kursi
                String newId = String.format("Reg%03d", lastNumber);

                stmt.setString(1, newId);
                stmt.setString(2, name);
                stmt.setString(3, film);
                stmt.setString(4, date);
                stmt.setString(5, time);
                stmt.setString(6, seat);

                stmt.addBatch();

                System.out.println("Pemesanan berhasil disimpan dengan ID " + newId);
            }

            stmt.executeBatch();
            // System.out.println("Pemesanan berhasil disimpan. ID terakhir: " + newId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void deleteBooking(String name, String film, String time) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db");
            PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM bookings WHERE name = ? AND film = ? AND time = ?")) {

            stmt.setString(1, name);
            stmt.setString(2, film);
            stmt.setString(3, time);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getBookedSeats(String filmTitle, String time, String date) {
        List<String> booked = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:booking.db")) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT seat FROM bookings WHERE film = ? AND time = ? AND date = ?"
            );
            stmt.setString(1, filmTitle);
            stmt.setString(2, time);
            stmt.setString(3, date);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String[] seats = rs.getString("seat").split(",");
                for (String seat : seats) {
                    booked.add(seat.trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return booked;
    }




    public static boolean loginAdmin(String username, String password) {
        String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static List<String[]> getAllBookings() {
    List<String[]> list = new ArrayList<>();
    String query = "SELECT * FROM bookings";
    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        while (rs.next()) {
            String[] row = {
                String.valueOf(rs.getString("id")),
                rs.getString("name"),
                rs.getString("film"),
                rs.getString("time"),
                rs.getString("seat")
            };
            list.add(row);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}

    public static void deleteBooking1(String bookingId) {
        try (Connection conn = connect()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM bookings WHERE id = ?");
            stmt.setString(1, bookingId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}