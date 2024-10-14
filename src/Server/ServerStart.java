package Server;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import javax.swing.*;

public class ServerStart {

    private static JTextArea logArea;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> createAndShowGUI());

        final int PORT = 9876;
        String port = "9876";

        try {
            DatagramSocket socket = new DatagramSocket(PORT);

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                if (message.startsWith("REGISTER|")) {
                    handleRegister(socket, message, clientAddress, clientPort);
                } else if (message.startsWith("LOGIN|")) {
                    handleLogin(socket, message, clientAddress, clientPort);
                } else if (message.startsWith("SENDMAIL|")) {
                    handleSendMail(socket, message, clientAddress, clientPort);
                } else if (message.startsWith("LISTEMAILS|")) {
                    handleListEmails(socket, message, clientAddress, clientPort);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleRegister(DatagramSocket socket, String message, InetAddress clientAddress, int clientPort) throws IOException {
        String[] parts = message.split("\\|");
        String email = parts[1];
        String password = parts[2];

        String response;
        try (Connection conn = DatabaseConnection.connect()) {
            String checkUserQuery = "SELECT * FROM users WHERE email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkUserQuery);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                response = "FALSE"; // Email đã tồn tại
            } else {
                String insertQuery = "INSERT INTO users (email, password) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, email);
                insertStmt.setString(2, password);
                insertStmt.executeUpdate();

                response = "TRUE"; // Đăng ký thành công
                logMessage("Email: " + email + " đã được đăng ký!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = "FALSE";
        }

        sendResponse(socket, response, clientAddress, clientPort);
    }

    private static void handleLogin(DatagramSocket socket, String message, InetAddress clientAddress, int clientPort) throws IOException {
        String[] parts = message.split("\\|");
        String email = parts[1];
        String password = parts[2];

        String response;
        try (Connection conn = DatabaseConnection.connect()) {
            String query = "SELECT password FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                response = storedPassword.equals(password) ? "TRUE" : "FALSE";
            } else {
                response = "FALSE"; // Email không tồn tại
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = "FALSE";
        }

        sendResponse(socket, response, clientAddress, clientPort);
        logMessage("Email: " + email + " vừa đăng nhập!");
    }

    private static void handleSendMail(DatagramSocket socket, String message, InetAddress clientAddress, int clientPort)
            throws IOException {
        String[] parts = message.split("\\|");
        String fromEmail = parts[1];
        String toEmail = parts[2];
        String subject = parts[3];
        String content = parts[4];

        String response;

        try (Connection conn = DatabaseConnection.connect()) {
            // Kiểm tra xem người nhận có tồn tại không
            String checkRecipientQuery = "SELECT * FROM users WHERE email = ?";
            PreparedStatement checkRecipientStmt = conn.prepareStatement(checkRecipientQuery);
            checkRecipientStmt.setString(1, toEmail);
            ResultSet rs = checkRecipientStmt.executeQuery();

            if (rs.next()) {
                // Chèn email vào bảng email
                String insertEmailQuery = "INSERT INTO emails (sender, receiver, subject, body, date_sent) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertEmailStmt = conn.prepareStatement(insertEmailQuery);
                insertEmailStmt.setString(1, fromEmail);
                insertEmailStmt.setString(2, toEmail);

                insertEmailStmt.setString(3, subject);
                insertEmailStmt.setString(4, content);
                insertEmailStmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));

                insertEmailStmt.executeUpdate();
                response = "TRUE";
                logMessage(fromEmail + " vừa gửi mail tới " + toEmail);
            } else {
                response = "FALSE"; // Người nhận không tồn tại
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = "FALSE";
        }

        sendResponse(socket, response, clientAddress, clientPort);
    }

    private static void handleListEmails(DatagramSocket socket, String message, InetAddress clientAddress, int clientPort)
            throws IOException {
        String[] parts = message.split("\\|");
        String email = parts[1];

        StringBuilder response = new StringBuilder();

        try (Connection conn = DatabaseConnection.connect()) {
            // Kiểm tra xem người dùng có tồn tại không
            String checkUserQuery = "SELECT * FROM users WHERE email = ?";
            PreparedStatement checkUserStmt = conn.prepareStatement(checkUserQuery);
            checkUserStmt.setString(1, email);
            ResultSet userRs = checkUserStmt.executeQuery();

            if (userRs.next()) {
                // Lấy danh sách email của người dùng từ bảng email
                String listEmailsQuery = "SELECT id, sender, subject,body, date_sent FROM emails WHERE receiver = ? ORDER BY date_sent DESC";
                PreparedStatement listEmailsStmt = conn.prepareStatement(listEmailsQuery);
                listEmailsStmt.setString(1, email);
                ResultSet emailRs = listEmailsStmt.executeQuery();

                if (!emailRs.isBeforeFirst()) {
                    response.append("NO_EMAILS"); // Không có email
                } else {
                    while (emailRs.next()) {
                        response.append("ID: ").append(emailRs.getInt("id"))
                                .append(", Từ: ").append(emailRs.getString("sender"))
                                .append(", Tiêu Đề: ").append(emailRs.getString("subject"))
                                .append(", Nội Dung: ").append(emailRs.getString("body"))
                                .append(", Thời Gian: ").append(emailRs.getTimestamp("date_sent"))
                                .append("\n----\n");
                    }
                }
            } else {
                response.append("USER_NOT_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.append("ERROR");
        }

        sendResponse(socket, response.toString(), clientAddress, clientPort);
    }

    private static void sendResponse(DatagramSocket socket, String response, InetAddress clientAddress, int clientPort)
            throws IOException {
        byte[] sendData = response.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
        socket.send(sendPacket);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Server Logs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        frame.add(scrollPane);
        frame.setVisible(true);
        logMessage("Server đang chạy ở cổng 9876");
    }

    private static void logMessage(String message) {
        logArea.append(message + "\n");
    }
}
