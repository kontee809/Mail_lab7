package Server;

import java.sql.*;

public class DatabaseConnection {

    public static Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:mysql://localhost:3306/email";
            String user = "root"; // Tên người dùng CSDL
            String password = ""; // Mật khẩu CSDL
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Kết nối thành công!");
        } catch (SQLException e) {
            System.out.println("Kết nối thất bại!");
            e.printStackTrace();
        }
        return conn;
    }

    public static void main(String[] args) {
        DatabaseConnection c = new DatabaseConnection();
        c.connect();
    }
}
