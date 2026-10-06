package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerUDP implements Runnable {
    private final String ipAddress ;
    private final int port;
    private final ServerHandler handler;
    private final LogListener logListener;
    private DatagramSocket socket;
    private boolean isRunning = false;

    public interface LogListener {
        void onLog(String message);
    }

    public ServerUDP(String ipAddress, int port, LogListener logListener) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.logListener = logListener;
        this.handler = new ServerHandler();
    }

    public void startServer() throws Exception {
        InetAddress bindAddr = InetAddress.getByName(this.ipAddress);
        socket = new DatagramSocket(port, bindAddr);
        isRunning = true;
        new Thread(this).start();
    }

    public void stopServer() {
        isRunning = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    @Override
    public void run() {
        byte[] receiveBuffer = new byte[4096];
        logListener.onLog("[MÁY CHỦ] Khởi tạo UDP Socket lắng nghe tại cổng " + port);

        while (isRunning) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String rawMsg = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8").trim();
                InetAddress clientAddr = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                String clientIP = clientAddr.getHostAddress();

                logListener.onLog(String.format("[NHẬN TỪ CLIENT] IP: %s | Port: %d", clientIP, clientPort));
                logListener.onLog("[YÊU CẦU] " + rawMsg);

                // Xử lý bằng ServerHandler
                String response = handler.processCommand(rawMsg);

                // Gửi gói tin trả lời lại cho Client IP
                byte[] sendData = response.getBytes("UTF-8");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddr, clientPort);
                socket.send(sendPacket);

                String shortResponse = response.length() > 50 ? response.substring(0, 50) + "..." : response;
                logListener.onLog(String.format("[TRẢ VỀ CLIENT] IP: %s | Kết quả: %s\n ", clientIP, shortResponse));
                

            } catch (Exception e) {
                if (!isRunning) {
                    logListener.onLog("[MÁY CHỦ] Đã dừng hoạt động.");
                }
            }
        }
    }
}