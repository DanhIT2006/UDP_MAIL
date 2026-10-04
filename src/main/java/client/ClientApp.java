package client;

import javax.swing.SwingUtilities;
public class ClientApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RegisterFrame("", "8080").setVisible(true);
        });
    }
}
