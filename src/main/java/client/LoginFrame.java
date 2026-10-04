package client;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtServerIp, txtServerPort, txtEmail, txtPassword;

    public LoginFrame() {
        setTitle("Đăng Nhập Mail Client");
        setSize(400, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        panel.add(new JLabel("IP Server:"));
        txtServerIp = new JTextField("127.0.0.1");
        panel.add(txtServerIp);

        panel.add(new JLabel("Port Server:"));
        txtServerPort = new JTextField("8080");
        panel.add(txtServerPort);

        panel.add(new JLabel("Email ID:"));
        txtEmail = new JTextField();
        panel.add(txtEmail);

        panel.add(new JLabel("Mật khẩu:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        JButton btnLogin = new JButton("Đăng Nhập");
        JButton btnGoRegister = new JButton("Đăng Ký Mới");
        panel.add(btnLogin);
        panel.add(btnGoRegister);

        add(panel);

        btnLogin.addActionListener(e -> handleLogin());
        btnGoRegister.addActionListener(e -> {
            new RegisterFrame(this, txtServerIp.getText().trim(), txtServerPort.getText().trim()).setVisible(true);
            setVisible(false);
        });
    }

    private void handleLogin() {
        String ip = txtServerIp.getText().trim();
        String portStr = txtServerPort.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Email và Mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            String msg = "LOGIN " + email + " " + password;
            String res = ClientUDP.sendAndReceive(ip, port, msg);

            if (res.startsWith("SUCCESS:")) {
                String fileData = res.substring(8);
                // Đăng nhập thành công -> Mở màn hình Hộp thư chính & Đóng cửa sổ Đăng nhập
                new MainMailFrame(ip, port, email, password, fileData).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, res, "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối Server: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}