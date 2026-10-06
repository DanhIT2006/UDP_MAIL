package client;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox; 
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {
    private JTextField txtServerIp, txtServerPort, txtEmail;
    private JPasswordField txtPassword; // Khai báo JPasswordField riêng biệt

    // Constructor nhận IP và Port
    public LoginFrame() {
        setTitle("2. Đăng Nhập Mail Client");
        setSize(420, 360); // Điều chỉnh chiều cao cho vừa vặn giao diện
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // GridLayout 6 hàng, 2 cột
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        
        panel.add(new JLabel("Email ID:"));
        txtEmail = new JTextField();
        panel.add(txtEmail);

        panel.add(new JLabel("Mật khẩu:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword);

        // Tạo JCheckBox cho chức năng ẩn/hiện mật khẩu
        JCheckBox chkShowPassword = new JCheckBox("Hiện mật khẩu");
        char defaultEchoChar = txtPassword.getEchoChar(); // Lưu ký tự ẩn mặc định của hệ thống

        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0); // Hiển thị mật khẩu
            } else {
                txtPassword.setEchoChar(defaultEchoChar); // Ẩn mật khẩu lại
            }
        });

        // Thêm CheckBox vào hàng thứ 5 của Panel
        panel.add(new JLabel("")); // Ô trống căn lề trái
        panel.add(chkShowPassword);

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
    public LoginFrame(String ip, String port) {
        this();
    }
    
    

    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Email và Mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String msg = "LOGIN " + email + " " + password;
            // Tự động sử dụng thông số mặc định từ ClientConfig
            String res = ClientUDP.sendAndReceive(ClientConfig.SERVER_IP, ClientConfig.SERVER_PORT, msg);

            if (res.startsWith("SUCCESS:")) {
                String fileData = res.substring(8);
                // Truyền ClientConfig.SERVER_IP và ClientConfig.SERVER_PORT vào MainMailFrame
                new MainMailFrame(ClientConfig.SERVER_IP, ClientConfig.SERVER_PORT, email, password, fileData).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, res, "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối Server: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}