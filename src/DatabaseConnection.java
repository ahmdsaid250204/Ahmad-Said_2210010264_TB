import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author User
 */
public class DatabaseConnection {
    // URL, username, dan password untuk koneksi ke database
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pemesanantiket"; 
    private static final String DB_USERNAME = "root"; 
    private static final String DB_PASSWORD = ""; 

    // Method untuk membuat koneksi ke database
    public static Connection getConnection() {
        Connection connection = null;

        try {
            // Load driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Membuat koneksi ke database
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Koneksi ke database berhasil!");

        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Gagal koneksi ke database: " + e.getMessage());
        }

        return connection;
    }

    public static void main(String[] args) {
        // Uji koneksi database
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Koneksi ditutup!");
            } catch (SQLException e) {
                System.err.println("Gagal menutup koneksi: " + e.getMessage());
            }
        }
    }
}
