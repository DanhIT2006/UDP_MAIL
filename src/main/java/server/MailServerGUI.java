package server;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.net.InetAddress;

public class MailServerGUI extends JFrame {
    private JTextArea logArea;
    private JButton btnStart, btnStop;
    private JTextField txtIP;
    private JTextField txtPort;
    private ServerUDP serverUDP;


    public MailServerGUI() {
        setTitle("Mail Server Management (UDP LAN)");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        


        String defaultIP = "127.0.0.1";
        try {
            defaultIP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {}

        topPanel.add(new JLabel("Server IP:"));
        txtIP = new JTextField(defaultIP, 15);
        topPanel.add(txtIP);

        topPanel.add(new JLabel("UDP Port:"));
        txtPort = new JTextField("8080", 6);
        topPanel.add(txtPort);

        btnStart = new JButton("Khởi chạy Server");
        btnStop = new JButton("Tắt Server");
        btnStop.setEnabled(false);

        topPanel.add(btnStart);
        topPanel.add(btnStop);
        add(topPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        btnStart.addActionListener(e -> startServer());
        btnStop.addActionListener(e -> stopServer());
    }

    private void startServer() {
        try {
            String ip = txtIP.getText().trim();
            int port = Integer.parseInt(txtPort.getText().trim());
            serverUDP = new ServerUDP(ip, port, msg -> SwingUtilities.invokeLater(() -> logArea.append(msg + "\n")));
            serverUDP.startServer();

            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            txtIP.setEnabled(false);
            txtPort.setEnabled(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể khởi động Server: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopServer() {
        if (serverUDP != null) {
            serverUDP.stopServer();
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            txtIP.setEnabled(true);
            txtPort.setEnabled(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MailServerGUI().setVisible(true));
    }
}