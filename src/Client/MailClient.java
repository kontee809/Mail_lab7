package Client;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class MailClient {

    private String ip;
    private int port;
    private String userEmail;
    private JFrame frame;
    private JList<String> emailList;
    private DefaultListModel<String> emailModel;
    private JTextArea emailContent;
    private List<String> emailData;
    private Timer emailPollingTimer;

    public MailClient(String IP, int port, String email) {
        this.ip = IP;
        this.port = port;
        this.userEmail = email;
        emailData = new ArrayList<>();
        createAndShowGUI();
        startEmailPolling();
    }

    private void startEmailPolling() {
        emailPollingTimer = new Timer(5000, e -> loadEmailsFromUser());
        emailPollingTimer.start();
    }

    private void createAndShowGUI() {

        frame = new JFrame("Mail Client - " + userEmail);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel danh sách email (bên trái)
        JPanel emailListPanel = new JPanel(new BorderLayout());
        emailModel = new DefaultListModel<>();
        emailList = new JList<>(emailModel);
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showEmailContent(emailList.getSelectedIndex());
            }
        });

        JScrollPane emailListScrollPane = new JScrollPane(emailList);
        emailListPanel.add(emailListScrollPane, BorderLayout.CENTER);

        JButton sendEmailButton = new JButton("+");
        sendEmailButton.addActionListener(e -> showSendEmailDialog());
        emailListPanel.add(sendEmailButton, BorderLayout.SOUTH);

        // Panel nội dung email (bên phải)
        JPanel emailContentPanel = new JPanel(new BorderLayout());
        emailContent = new JTextArea();
        emailContent.setEditable(false);
        JScrollPane contentScrollPane = new JScrollPane(emailContent);
        emailContentPanel.add(contentScrollPane, BorderLayout.CENTER);

        mainPanel.add(emailListPanel, BorderLayout.WEST);
        mainPanel.add(emailContentPanel, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);

        loadEmailsFromUser();
    }

    private void showEmailContent(int selectedIndex) {
        if (selectedIndex != -1 && selectedIndex < emailData.size()) {
            String emailDetails = emailData.get(selectedIndex);
            String time = "", from = "", subject = "", content = "";

            for (String line : emailDetails.split(", ")) {
                if (line.startsWith("Thời Gian:")) {
                    time = line;
                } else if (line.startsWith("Từ:")) {
                    from = line;
                } else if (line.startsWith("Tiêu Đề:")) {
                    subject = line;
                } else if (line.startsWith("Nội Dung:")) {
                    content = line;
                }
            }

            String formattedContent = String.format("%s\n%s\n%s\n%s", time, from, subject, content);
            emailContent.setText(formattedContent);
        }
    }

    // Hiển thị hộp thoại gửi email
    private void showSendEmailDialog() {
        JDialog sendEmailDialog = new JDialog(frame, "Gửi Email", true);
        sendEmailDialog.setSize(400, 300);
        sendEmailDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel toLabel = new JLabel("Tới:");
        JTextField toField = new JTextField(20);
        JLabel subjectLabel = new JLabel("Tiêu Đề:");
        JTextField subjectField = new JTextField(20);
        JLabel contentLabel = new JLabel("Nội Dung:");
        JTextArea contentArea = new JTextArea(5, 20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        sendEmailDialog.add(toLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        sendEmailDialog.add(toField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        sendEmailDialog.add(subjectLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        sendEmailDialog.add(subjectField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        sendEmailDialog.add(contentLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        sendEmailDialog.add(new JScrollPane(contentArea), gbc);

        JButton sendButton = new JButton("Gửi");
        sendButton.addActionListener(e -> {
            String to = toField.getText();
            String subject = subjectField.getText();
            String content = contentArea.getText();
            User user = new User();
            boolean isSent = user.sendEmail(ip, port, userEmail, to, subject, content);
            if (isSent) {
                JOptionPane.showMessageDialog(sendEmailDialog, "Email đã gửi");
                sendEmailDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(sendEmailDialog, "Email không tồn tại");
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        sendEmailDialog.add(sendButton, gbc);

        sendEmailDialog.setLocationRelativeTo(frame);
        sendEmailDialog.setVisible(true);
    }

    private void loadEmailsFromUser() {
        User user = new User(userEmail);
        user.loadEmails(ip, port);

        if (emailData == null || emailData.size() != user.getEmails().size()) {
            emailModel.clear();
            emailData = user.getEmails();

            for (String email : emailData) {
                // Tách các trường thông tin từ email
                String[] lines = email.split(", ");
                String id = "", from = "", subject = "", timestamp = "";
                for (String line : lines) {
                    if (line.startsWith("ID:")) {
                        id = line.replace("ID:", "").trim();
                    } else if (line.startsWith("Từ:")) {
                        from = line.replace("Từ:", "").trim();
                    } else if (line.startsWith("Tiêu Đề:")) {
                        subject = line.replace("Tiêu Đề:", "").trim();
                    } else if (line.startsWith("Thời Gian:")) {
                        timestamp = line.replace("Thời Gian:", "").trim();
                    }
                }
                // Hiển thị người gửi, tiêu đề và thời gian ở panel bên trái
                emailModel.addElement(String.format("%s - %s - %s", from, subject, timestamp));
            }
        }
    }

    public static void main(String[] args) {
        new MailClient("127.0.0.1", 9876, "123");
    }
}
