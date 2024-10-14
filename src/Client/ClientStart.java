package Client;

import javax.swing.*;
import java.awt.*;

public class ClientStart {

    final String SERVER_ADDRESS = "localhost";
    final int SERVER_PORT = 9876;
    private JTextField emailLogin;
    private JPasswordField passwordLogin;

    private JPasswordField passwordRegister;
    private JPasswordField confirmPasswordRegister;
    private JTextField emailRegister;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientStart client = new ClientStart();
            client.createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Mail Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new CardLayout());
        frame.setContentPane(contentPane);

        UIManager.put("Button.background", new Color(50, 150, 250));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Panel.background", Color.LIGHT_GRAY);
        UIManager.put("Label.font", new Font("Arial", Font.BOLD, 14));

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel("Email:"), gbc);
        emailLogin = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 0;
        loginPanel.add(emailLogin, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Mật Khẩu:"), gbc);
        passwordLogin = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(passwordLogin, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            String username = emailLogin.getText();
            String password = new String(passwordLogin.getPassword());
            User user = new User();
            Boolean kt = user.dangnhap(SERVER_ADDRESS, SERVER_PORT, username, password);
            if (kt) {
                JOptionPane.showMessageDialog(frame, "Đăng Nhập Thành Công");

                frame.dispose();

                new MailClient(SERVER_ADDRESS, SERVER_PORT, username);
            } else {
                JOptionPane.showMessageDialog(frame, "Email hoặc Mật Khẩu không đúng");
            }

        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPane.getLayout();
            cl.show(contentPane, "Register");
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        loginPanel.add(registerButton, gbc);

        // Đăng ký panel
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        registerPanel.add(new JLabel("Email:"), gbc);
        emailRegister = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 0;
        registerPanel.add(emailRegister, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        registerPanel.add(new JLabel("Mật Khẩu:"), gbc);
        passwordRegister = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        registerPanel.add(passwordRegister, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        registerPanel.add(new JLabel("Nhập Lại Mật Khẩu:"), gbc);
        confirmPasswordRegister = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        registerPanel.add(confirmPasswordRegister, gbc);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            String password = new String(passwordRegister.getPassword());
            String confirmPassword = new String(confirmPasswordRegister.getPassword());
            String email = emailRegister.getText();

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(frame, "Mật khẩu không khớp!");
            } else {
                User user = new User();
                Boolean kt = false;
                kt = user.dangki(SERVER_ADDRESS, SERVER_PORT, email, password);

                if (kt) {
                    JOptionPane.showMessageDialog(frame, "Đăng kí Thành Công Với  " + ", Email: " + email);
                    frame.dispose();

                    new MailClient(SERVER_ADDRESS, SERVER_PORT, email);

                } else {
                    JOptionPane.showMessageDialog(frame, "Email đã tồn tại!");
                }
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        registerPanel.add(submitButton, gbc);

        JButton backButton = new JButton("Quay Về Đăng Nhập");
        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPane.getLayout();
            cl.show(contentPane, "Login");
        });
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        registerPanel.add(backButton, gbc);

        // Thêm các panel vào contentPane
        contentPane.add(loginPanel, "Login");
        contentPane.add(registerPanel, "Register");

        frame.setVisible(true);
        CardLayout cl = (CardLayout) contentPane.getLayout();
        cl.show(contentPane, "Login");
    }
}
