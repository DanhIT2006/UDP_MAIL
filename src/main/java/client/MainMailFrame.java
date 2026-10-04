package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

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
    private JTextField txtRecipient;

    public MainMailFrame(String serverIp, int serverPort, String userEmail, String password, String initialFileData) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.currentUserEmail = userEmail;
        this.currentPassword = password;

        setTitle("Hộp Thư - " + currentUserEmail);
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // Thanh trạng thái trên cùng
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Đang đăng nhập: "));
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

        // TAB 2: Soạn / Gửi email
        JPanel sendPanel = new JPanel(new BorderLayout(5, 5));
        JPanel sendTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sendTop.add(new JLabel("Người nhận (Email ID):"));
        txtRecipient = new JTextField(25);
        sendTop.add(txtRecipient);

        txtEmailContent = new JTextArea();
        JButton btnSend = new JButton("Gửi Email");

        sendPanel.add(sendTop, BorderLayout.NORTH);
        sendPanel.add(new JScrollPane(txtEmailContent), BorderLayout.CENTER);
        sendPanel.add(btnSend, BorderLayout.SOUTH);
        tabbedPane.addTab("Soạn & Gửi Email", sendPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Khởi tạo danh sách thư ban đầu
        updateMailList(initialFileData);

        // Sự kiện
        btnRefresh.addActionListener(e -> refreshMailList());
        btnSend.addActionListener(e -> sendMail());
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
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
            JOptionPane.showMessageDialog(this, "Lỗi làm mới hộp thư: " + ex.getMessage());
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
        String recipient = txtRecipient.getText().trim();
        String content = txtEmailContent.getText().trim();

        if (recipient.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Email người nhận và Nội dung!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String msg = "SEND " + currentUserEmail + " " + recipient + " " + content;
            String res = ClientUDP.sendAndReceive(serverIp, serverPort, msg);
            JOptionPane.showMessageDialog(this, res);

            if (res.startsWith("SUCCESS:")) {
                txtEmailContent.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi gửi thư: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}