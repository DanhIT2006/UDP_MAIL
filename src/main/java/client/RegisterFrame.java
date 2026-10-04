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

public class RegisterFrame extends JFrame {
    private JTextField txtServerIp, txtServerPort, txtEmail, txtUsername, txtPassword;

    public RegisterFrame(String defaultIp, String defaultPort) {
        setTitle("1. Đăng Ký Tài Khoản Mới");
        setSize(450, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        panel.add(new JLabel("IP Server:"));
        txtServerIp = new JTextField(defaultIp);
        panel.add(txtServerIp);

        panel.add(new JLabel("Port Server:"));
        txtServerPort = new JTextField(defaultPort);
        panel.add(txtServerPort);

        panel.add(new JLabel("Email ID (vd: nam@gmail.com):"));
        txtEmail = new JTextField();
        panel.add(txtEmail);

        panel.add(new JLabel("Username (Tên viết liền):"));
        txtUsername = new JTextField();
        panel.add(txtUsername);

        panel.add(new JLabel("Mật khẩu:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        JButton btnSubmit = new JButton("Đăng Ký");
        JButton btnCancel = new JButton("Hủy / Quay lại");
        panel.add(btnSubmit);
        panel.add(btnCancel);

        add(panel);

        btnSubmit.addActionListener(e -> handleRegister());

        // Đã sửa: Mở LoginFrame mới và truyền IP, Port vừa nhập sang
        btnCancel.addActionListener(e -> {
            new LoginFrame(txtServerIp.getText().trim(), txtServerPort.getText().trim()).setVisible(true);
            dispose();
        });
    }

    private void handleRegister() {
        String ip = txtServerIp.getText().trim();
        String portStr = txtServerPort.getText().trim();
        String email = txtEmail.getText().trim();
        String username = txtUsername.getText().trim().replace(" ", "_");
        String password = txtPassword.getText().trim();

        if (ip.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            String msg = "REGISTER " + email + " " + username + " " + password;
            String res = ClientUDP.sendAndReceive(ip, port, msg);

            JOptionPane.showMessageDialog(this, res);

            // Đã sửa: Khởi tạo màn hình LoginFrame mới thay vì gọi biến loginFrame bị null
            if (res.startsWith("SUCCESS:")) {
                new LoginFrame(ip, portStr).setVisible(true);
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xử lý: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}