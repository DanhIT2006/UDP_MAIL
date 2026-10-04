package client;

import javax.swing.*;
import java.awt.*;

public class MailClientGUI extends JFrame {
    private JTextField txtServerIp, txtServerPort, txtEmail, txtUsername, txtPassword, txtRecipient;
    private JTextArea txtEmailContent, txtReadContent;
    private DefaultListModel listModelMail;
    private JList listMails;
    private JLabel lblStatus;

    private String currentUser = null;

    public MailClientGUI() {
        setTitle("Mail Client App (UDP LAN)");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // Panel cấu hình IP máy Server
        JPanel netPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        netPanel.setBorder(BorderFactory.createTitledBorder("Cấu hình Kết nối Máy chủ Server"));
        netPanel.add(new JLabel("IP Server:"));
        txtServerIp = new JTextField("127.0.0.1", 12);
        netPanel.add(txtServerIp);

        netPanel.add(new JLabel("Port:"));
        txtServerPort = new JTextField("8080", 5);
        netPanel.add(txtServerPort);

        lblStatus = new JLabel(" Trạng thái: Chưa đăng nhập ");
        lblStatus.setForeground(Color.RED);
        netPanel.add(lblStatus);
        add(netPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // TAB 1: Đăng ký / Đăng nhập
        JPanel authPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        authPanel.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));
        authPanel.add(new JLabel("Email ID (vd: nam@gmail.com):"));
        txtEmail = new JTextField();
        authPanel.add(txtEmail);

        authPanel.add(new JLabel("Username (Họ tên):"));
        txtUsername = new JTextField();
        authPanel.add(txtUsername);

        authPanel.add(new JLabel("Mật khẩu:"));
        txtPassword = new JPasswordField();
        authPanel.add(txtPassword);

        JButton btnRegister = new JButton("1. Đăng ký tài khoản");
        JButton btnLogin = new JButton("2. Đăng nhập");
        authPanel.add(btnRegister);
        authPanel.add(btnLogin);

        tabbedPane.addTab("Tài Khoản", authPanel);

        // TAB 2: Xem Hộp thư đến
        JPanel inboxPanel = new JPanel(new BorderLayout(5, 5));
        listModelMail = new DefaultListModel<>();
        listMails = new JList<>(listModelMail);
        txtReadContent = new JTextArea();
        txtReadContent.setEditable(false);
        txtReadContent.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(listMails), new JScrollPane(txtReadContent));
        splitPane.setDividerLocation(280);

        JPanel inboxTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Làm mới Hộp Thư");
        inboxTop.add(btnRefresh);

        inboxPanel.add(inboxTop, BorderLayout.NORTH);
        inboxPanel.add(splitPane, BorderLayout.CENTER);
        tabbedPane.addTab("Hộp Thư Đến", inboxPanel);

        // TAB 3: Soạn / Gửi email
        JPanel sendPanel = new JPanel(new BorderLayout(5, 5));
        JPanel sendTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sendTop.add(new JLabel("Gửi đến Email:"));
        txtRecipient = new JTextField(25);
        sendTop.add(txtRecipient);

        txtEmailContent = new JTextArea();
        JButton btnSend = new JButton("Gửi Email");

        sendPanel.add(sendTop, BorderLayout.NORTH);
        sendPanel.add(new JScrollPane(txtEmailContent), BorderLayout.CENTER);
        sendPanel.add(btnSend, BorderLayout.SOUTH);
        tabbedPane.addTab("Gửi Email", sendPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Sự kiện
        btnRegister.addActionListener(e -> executeRegister());
        btnLogin.addActionListener(e -> executeLogin());
        btnRefresh.addActionListener(e -> fetchMailList());
        btnSend.addActionListener(e -> executeSendMail());

        listMails.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listMails.getSelectedValue() != null) {
                readSelectedMail(listMails.getSelectedValue());
            }
        });
    }

    private String getIp() { return txtServerIp.getText().trim(); }
    private int getPort() { return Integer.parseInt(txtServerPort.getText().trim()); }

    private void executeRegister() {
        try {
            String msg = "REGISTER " + txtEmail.getText().trim() + " " + txtUsername.getText().trim() + " " + txtPassword.getText().trim();
            String res = ClientUDP.sendAndReceive(getIp(), getPort(), msg);
            JOptionPane.showMessageDialog(this, res);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối UDP: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeLogin() {
        try {
            String email = txtEmail.getText().trim();
            String msg = "LOGIN " + email + " " + txtPassword.getText().trim();
            String res = ClientUDP.sendAndReceive(getIp(), getPort(), msg);

            if (res.startsWith("SUCCESS:")) {
                currentUser = email;
                lblStatus.setText(" Đã đăng nhập: " + currentUser + " ");
                lblStatus.setForeground(new Color(0, 128, 0));
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
                updateMailListFromResponse(res.substring(8));
            } else {
                JOptionPane.showMessageDialog(this, res, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối UDP: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fetchMailList() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Bạn cần phải Đăng nhập trước!");
            return;
        }
        executeLogin();
    }

    private void updateMailListFromResponse(String fileData) {
        listModelMail.clear();
        if (!fileData.isEmpty()) {
            String[] files = fileData.split(";");
            for (String f : files) {
                if (!f.trim().isEmpty()) listModelMail.addElement(f.trim());
            }
        }
    }

    private void readSelectedMail(String fileName) {
        try {
            String msg = "READ " + currentUser + " " + fileName;
            String res = ClientUDP.sendAndReceive(getIp(), getPort(), msg);
            if (res.startsWith("CONTENT:")) {
                txtReadContent.setText(res.substring(8));
            } else {
                txtReadContent.setText(res);
            }
        } catch (Exception ex) {
            txtReadContent.setText("Lỗi đọc thư: " + ex.getMessage());
        }
    }

    private void executeSendMail() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Bạn cần Đăng nhập trước khi gửi email!");
            return;
        }
        try {
            String recipient = txtRecipient.getText().trim();
            String content = txtEmailContent.getText().trim();
            String msg = "SEND " + currentUser + " " + recipient + " " + content;

            String res = ClientUDP.sendAndReceive(getIp(), getPort(), msg);
            JOptionPane.showMessageDialog(this, res);
            if (res.startsWith("SUCCESS:")) {
                txtEmailContent.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi gửi mail: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MailClientGUI().setVisible(true));
    }
}