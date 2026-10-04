package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MainMailFrame extends JFrame {
    private final String serverIp;
    private final int serverPort;
    private final String currentUserEmail;
    private final String currentPassword;

    private DefaultListModel listModelMail;
    private JList listMails;
    private JTextArea txtReadContent, txtEmailContent;
    private JTextField txtRecipientEmail, txtRecipientIp, txtSubject;

    public MainMailFrame(String serverIp, int serverPort, String userEmail, String password, String initialFileData) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.currentUserEmail = userEmail;
        this.currentPassword = password;

        setTitle("Hộp Thư - " + currentUserEmail);
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // Panel thông tin trên cùng
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Tài khoản: "));
        JLabel lblUser = new JLabel(currentUserEmail);
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblUser.setForeground(new Color(0, 102, 204));
        topPanel.add(lblUser);

        JButton btnLogout = new JButton("Đăng xuất");
        topPanel.add(btnLogout);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // TAB 1: Xem Hộp thư đến
        JPanel inboxPanel = new JPanel(new BorderLayout(5, 5));
        listModelMail = new DefaultListModel<>();
        listMails = new JList<>(listModelMail);
        txtReadContent = new JTextArea();
        txtReadContent.setEditable(false);
        txtReadContent.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(listMails), new JScrollPane(txtReadContent));
        splitPane.setDividerLocation(280);

        JPanel inboxTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Làm mới Hộp thư");
        inboxTop.add(btnRefresh);

        inboxPanel.add(inboxTop, BorderLayout.NORTH);
        inboxPanel.add(splitPane, BorderLayout.CENTER);
        tabbedPane.addTab("Hộp Thư Đến", inboxPanel);

        // TAB 2: Soạn & Gửi email
        JPanel sendPanel = new JPanel(new BorderLayout(5, 5));
        
        JPanel headerPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Thông tin thư gửi"));

        headerPanel.add(new JLabel("Email Người Nhận:"));
        txtRecipientEmail = new JTextField();
        headerPanel.add(txtRecipientEmail);

        headerPanel.add(new JLabel("IP Client Người Nhận:"));
        txtRecipientIp = new JTextField("127.0.0.1");
        headerPanel.add(txtRecipientIp);

        headerPanel.add(new JLabel("Tiêu Đề Email:"));
        txtSubject = new JTextField();
        headerPanel.add(txtSubject);

        txtEmailContent = new JTextArea();
        JButton btnSend = new JButton("Gửi Email");

        sendPanel.add(headerPanel, BorderLayout.NORTH);
        sendPanel.add(new JScrollPane(txtEmailContent), BorderLayout.CENTER);
        sendPanel.add(btnSend, BorderLayout.SOUTH);
        tabbedPane.addTab("Soạn & Gửi Email", sendPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Đổ danh sách thư ban đầu
        updateMailList(initialFileData);

        // Sự kiện
        btnRefresh.addActionListener(e -> refreshMailList());
        btnSend.addActionListener(e -> sendMail());
        btnLogout.addActionListener(e -> {
            new LoginFrame(serverIp, String.valueOf(serverPort)).setVisible(true);
            dispose();
        });

        listMails.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listMails.getSelectedValue() != null) {
                readMail(listMails.getSelectedValue().toString());
            }
        });
    }

    private void updateMailList(String fileData) {
        listModelMail.clear();
        if (fileData != null && !fileData.isEmpty()) {
            String[] files = fileData.split(";");
            for (String f : files) {
                if (!f.trim().isEmpty()) listModelMail.addElement(f.trim());
            }
        }
    }

    private void refreshMailList() {
        try {
            String msg = "LOGIN " + currentUserEmail + " " + currentPassword;
            String res = ClientUDP.sendAndReceive(serverIp, serverPort, msg);
            if (res.startsWith("SUCCESS:")) {
                updateMailList(res.substring(8));
                JOptionPane.showMessageDialog(this, "Đã cập nhật hộp thư!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi làm mới: " + ex.getMessage());
        }
    }

    private void readMail(String fileName) {
        try {
            String msg = "READ " + currentUserEmail + " " + fileName;
            String res = ClientUDP.sendAndReceive(serverIp, serverPort, msg);
            if (res.startsWith("CONTENT:")) {
                txtReadContent.setText(res.substring(8));
            } else {
                txtReadContent.setText(res);
            }
        } catch (Exception ex) {
            txtReadContent.setText("Lỗi đọc file: " + ex.getMessage());
        }
    }

    private void sendMail() {
        String recipientEmail = txtRecipientEmail.getText().trim();
        String recipientIp = txtRecipientIp.getText().trim();
        String subject = txtSubject.getText().trim();
        String content = txtEmailContent.getText().trim();

        if (recipientEmail.isEmpty() || recipientIp.isEmpty() || subject.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ: Email người nhận, IP người nhận, Tiêu đề và Nội dung!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Tự động lấy IP của Client hiện tại
            String senderIp = InetAddress.getLocalHost().getHostAddress();
            // Lấy thời gian gửi hiện tại
            String sendTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Đóng gói gói tin phân cách bằng '|'
            // SEND|senderEmail|senderIp|recipientEmail|recipientIp|time|subject|content
            String msg = "SEND|" + currentUserEmail + "|" + senderIp + "|" + recipientEmail + "|" + recipientIp + "|" + sendTime + "|" + subject + "|" + content;

            String res = ClientUDP.sendAndReceive(serverIp, serverPort, msg);
            JOptionPane.showMessageDialog(this, res);

            if (res.startsWith("SUCCESS:")) {
                txtSubject.setText("");
                txtEmailContent.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi gửi thư: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}