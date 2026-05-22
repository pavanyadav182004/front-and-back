import java.sql.*;

public class ReadDB {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Hotel_Booking", "root", "");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM User WHERE email='admin@gmail.com'");
        System.out.println("Admin deleted.");
    }
}
