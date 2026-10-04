package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientUDP {
    public static String sendAndReceive(String serverIp, int serverPort, String message) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000); // Quá 3 giây không có phản hồi sẽ ném ra Exception Timeout

            InetAddress serverAddr = InetAddress.getByName(serverIp);
            byte[] sendData = message.getBytes("UTF-8");

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddr, serverPort);
            socket.send(sendPacket);

            byte[] receiveData = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            return new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
        }
    }
}