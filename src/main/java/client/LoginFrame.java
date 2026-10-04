package client;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {
    private JTextField txtServerIp, txtServerPort, txtEmail, txtPassword;

    // Constructor nhận IP và Port
    public LoginFrame(String defaultIp, String defaultPort) {
        setTitle("2. Đăng Nhập Mail Client");
        setSize(400, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        panel.add(new JLabel("IP Server:"));
        txtServerIp = new JTextField(defaultIp);
        panel.add(txtServerIp);

        panel.add(new JLabel("Port Server:"));
        txtServerPort = new JTextField(defaultPort);
        panel.add(txtServerPort);

        panel.add(new JLabel("Email ID:"));
        txtEmail = new JTextField();
        panel.add(txtEmail);

        panel.add(new JLabel("Mật khẩu:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        JButton btnLogin = new JButton("Đăng Nhập");
        JButton btnGoRegister = new JButton("Chưa có TK? Đăng ký");
        panel.add(btnLogin);
        panel.add(btnGoRegister);

        add(panel);

        btnLogin.addActionListener(e -> handleLogin());
        btnGoRegister.addActionListener(e -> {
            new RegisterFrame(txtServerIp.getText().trim(), txtServerPort.getText().trim()).setVisible(true);
            dispose();
        });
    }

    // Constructor mặc định không tham số (Overloading)
    public LoginFrame() {
        this("127.0.0.1", "8080");
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
                new MainMailFrame(ip, port, email, password, fileData).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, res, "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối Server: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}