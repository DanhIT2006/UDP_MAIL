package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerHandler {
    private static final String DATA_DIR = "mail_server_data";

    public ServerHandler() {
        File dataFolder = new File(DATA_DIR);
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public String processCommand(String rawCommand) {
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            return "ERROR: Lệnh trống.";
        }

        String[] parts = rawCommand.split(" ", 4);
        String command = parts[0].toUpperCase();

        switch (command) {
            case "REGISTER":
                return handleRegister(parts);
            case "LOGIN":
                return handleLogin(parts);
            case "SEND":
                return handleSend(parts);
            case "READ":
                return handleRead(parts);
            default:
                return "ERROR: Lệnh không hợp lệ.";
        }
    }

    private String handleRegister(String[] parts) {
        if (parts.length < 4) return "ERROR: Cú pháp: REGISTER   ";

        String emailId = parts[1].trim();
        String username = parts[2].trim();
        String password = parts[3].trim();

        File userDir = new File(DATA_DIR, emailId);
        if (userDir.exists()) {
            return "ERROR: Tài khoản '" + emailId + "' đã tồn tại.";
        }

        userDir.mkdirs();

        // 1. Tạo file profile.json
        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String profileJson = String.format(
            "{\n  \"id\": \"%s\",\n  \"username\": \"%s\",\n  \"password\": \"%s\",\n  \"created_at\": \"%s\"\n}",
            emailId, username, password, createdAt
        );
        try (FileWriter writer = new FileWriter(new File(userDir, "profile.json"))) {
            writer.write(profileJson);
        } catch (IOException e) {
            return "ERROR: Không thể ghi file profile.json.";
        }

        // 2. Tạo file new_email.txt chào mừng mặc định
        String welcomeContent = "Thank you for using this service. we hope that you will feel comfortabl........";
        try (FileWriter writer = new FileWriter(new File(userDir, "new_email.txt"))) {
            writer.write(welcomeContent);
        } catch (IOException e) {
            return "ERROR: Không thể ghi file new_email.txt.";
        }

        return "SUCCESS: Đăng ký thành công tài khoản '" + emailId + "'.";
    }

    private String handleLogin(String[] parts) {
        if (parts.length < 3) return "ERROR: Cú pháp: LOGIN  ";

        String emailId = parts[1].trim();
        String password = parts[2].trim();

        File userDir = new File(DATA_DIR, emailId);
        File profileFile = new File(userDir, "profile.json");

        if (!profileFile.exists()) {
            return "ERROR: Tài khoản '" + emailId + "' không tồn tại.";
        }

        try {
            String jsonContent = new String(Files.readAllBytes(profileFile.toPath()), "UTF-8");
            String storedPassword = extractJsonValue(jsonContent, "password");

            if (!password.equals(storedPassword)) {
                return "ERROR: Mật khẩu không chính xác.";
            }

            File[] files = userDir.listFiles();
            List mailFiles = new ArrayList<>();
            if (files != null) {
                for (File f : files) {
                    if (!f.getName().equals("profile.json")) {
                        mailFiles.add(f.getName());
                    }
                }
            }
            return "SUCCESS:" + String.join(";", mailFiles);
        } catch (Exception e) {
            return "ERROR: Lỗi hệ thống khi đọc dữ liệu.";
        }
    }

    private String handleSend(String[] parts) {
        if (parts.length < 4) return "ERROR: Cú pháp: SEND   ";

        String senderId = parts[1].trim();
        String recipientId = parts[2].trim();
        String content = parts[3].trim();

        File recipientDir = new File(DATA_DIR, recipientId);
        if (!recipientDir.exists()) {
            return "ERROR: Người nhận '" + recipientId + "' không tồn tại trên máy chủ.";
        }

        long timestamp = System.currentTimeMillis() / 1000;
        String senderPrefix = senderId.contains("@") ? senderId.split("@")[0] : senderId;
        String fileName = "email_" + timestamp + "_from_" + senderPrefix + ".txt";
        File emailFile = new File(recipientDir, fileName);

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String emailBody = "From: " + senderId + "\nTo: " + recipientId + "\nDate: " + nowStr + "\n----------------------------------------\n" + content;

        try (FileWriter writer = new FileWriter(emailFile)) {
            writer.write(emailBody);
        } catch (IOException e) {
            return "ERROR: Không thể ghi file email.";
        }

        return "SUCCESS: Đã gửi thư thành công tới '" + recipientId + "'.";
    }

    private String handleRead(String[] parts) {
        if (parts.length < 3) return "ERROR: Cú pháp: READ  ";

        String emailId = parts[1].trim();
        String fileName = parts[2].trim();

        File file = new File(new File(DATA_DIR, emailId), fileName);
        if (!file.exists()) return "ERROR: File không tồn tại.";

        try {
            return "CONTENT:" + new String(Files.readAllBytes(file.toPath()), "UTF-8");
        } catch (IOException e) {
            return "ERROR: Lỗi đọc file.";
        }
    }

    private String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\":\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }
}