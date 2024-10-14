package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class User {

    private String password;
    private String email;
    private List<String> emails;

    public User(String email) {
        this.email = email;
        this.emails = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        super.finalize();
    }

    public User(String email, String password) {
        super();
        this.email = email;
        this.password = password;
    }

    public User() {

    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(ArrayList<String> emails) {
        this.emails = emails;
    }

    public static Boolean dangki(String serverAddress1, int serverPort, String email, String password) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(serverAddress1);

            String message = "REGISTER|" + email + "|" + password;
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

            socket.close();

            return response.equals("TRUE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Boolean dangnhap(String serverAddress1, int serverPort, String email, String password) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(serverAddress1);

            String message = "LOGIN|" + email + "|" + password;
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

            socket.close();

            return response.equals("TRUE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Boolean sendEmail(String serverAddress, int serverPort, String fromEmail, String toEmail, String subject, String content) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverInetAddress = InetAddress.getByName(serverAddress);

            String message = "SENDMAIL|" + fromEmail + "|" + toEmail + "|" + subject + "|" + content;
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverInetAddress, serverPort);
            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

            socket.close();
            return response.equals("TRUE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void loadEmails(String serverAddress, int serverPort) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverInetAddress = InetAddress.getByName(serverAddress);

            String message = "LISTEMAILS|" + this.email;
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverInetAddress, serverPort);
            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

            emails.clear();

            if (response.equals("NO_EMAILS")) {
                System.out.println("No emails found.");
            } else if (response.equals("USER_NOT_FOUND")) {
                System.out.println("User not found.");
            } else {

                String[] emailEntries = response.split("----");
                for (String emailEntry : emailEntries) {
                    emails.add(emailEntry.trim());
                }

            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] readEmail(String emailFileName) {
        List<String> emailContent = new ArrayList<>();

        File emailFile = new File(emailFileName);
        if (!emailFile.exists()) {
            System.err.println("Tệp không tồn tại: " + emailFileName);
            return new String[0];
        }

        try (BufferedReader br = new BufferedReader(new FileReader(emailFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                emailContent.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (emailContent.isEmpty()) {
            System.err.println("Nội dung email trống: " + emailFileName);
            return new String[0];
        }

        return emailContent.toArray(new String[0]);
    }

}
