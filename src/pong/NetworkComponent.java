package pong;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

/**
 * Class containing the network part of the game.
 */
public class NetworkComponent {
    private Game game;  // The game the network component is used in
    private String host;
    private int port;
    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean receiving;
    private boolean isServer;   // Indicates if the player is server or client
    private boolean clientConnected;

    public NetworkComponent(Game game) {
        this.game = game;
        host = getIPAddress();
        port = getPort();
    }

    /**
     * Thread listening for messages about Ball and Paddle positions. If the connection is lost, this class
     * terminates the game.
     */
    private class ReceiverThread implements Runnable {
        public void run() {
            while (receiving) {
                try {
                    String message; // String with information from the other player

                    if ((message = reader.readLine()) != null) {
                        if (message.startsWith("p")) {
                            receivePaddlePosition(message.substring(1));
                        } else if (!isServer && message.startsWith("b")) {
                            receiveBallPosition(message.substring(1));
                        }
                    }
                } catch (IOException e) {
                    game.stop();
                    JOptionPane.showMessageDialog(
                            Game.renderer,
                            "Connection was lost, program will terminate.",
                            "Connection lost.",
                            JOptionPane.ERROR_MESSAGE
                    );
                    System.exit(0);
                }
            }
        }
    }

    /**
     * Get ip-address from the user.
     *
     * @return String with the ip-address
     */
    private String getIPAddress() {
        String ip = JOptionPane.showInputDialog(
                Game.renderer,
                "Example: 172.16.254.1 or localhost",
                "Enter IP-address",
                JOptionPane.QUESTION_MESSAGE
        );
        while (!isIPAddress(ip)) {
            ip = JOptionPane.showInputDialog(
                    Game.renderer,
                    "Example: 172.16.254.1 or localhost",
                    "Not a valid IP-address, try again",
                    JOptionPane.QUESTION_MESSAGE
            );
        }
        return ip;
    }

    /**
     * Check if the ip-address is localhost of has 4 numeric parts with 1-3 digits.
     *
     * @param ip String with the ip-address
     * @return true if the String matches the criteria
     */
    private boolean isIPAddress(String ip) {
        if (ip.equals("localhost")) {
            return true;
        } else {
            String ipPattern = "[1-9]{1,3}\\.[1-9]{1,3}\\.[1-9]{1,3}\\.[1-9]{1,3}";
            return Pattern.matches(ipPattern, ip);
        }
    }

    /**
     * Get port from the user. Checks if the port is between 0-65535.
     *
     * @return int with the port number if it matches the criteria, otherwise -1
     */
    private int getPort() {
        try {
            int port = Integer.parseInt(JOptionPane.showInputDialog(
                    Game.renderer,
                    "Example: 2000",
                    "Enter port number",
                    JOptionPane.QUESTION_MESSAGE
            ));
            while (port < 0 || port > 65535) {
                port = Integer.parseInt(JOptionPane.showInputDialog(
                        Game.renderer,
                        "Example: 2000",
                        "Not a valid port, try again",
                        JOptionPane.QUESTION_MESSAGE
                ));
            }
            return port;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Receive message with the position of the other players Paddle.
     *
     * @param message String
     */
    private void receivePaddlePosition(String message) {
        double yPos = Double.parseDouble(message);
        if (isServer) {
            game.getPaddle2().setY(yPos);
        } else {
            game.getPaddle1().setY(yPos);
        }
    }

    /**
     * Receive message with the position of the Ball. Ball is only updated by server player so this method is needed
     * to update Ball for client player.
     *
     * @param message String
     */
    private void receiveBallPosition(String message) {
        String[] ballPos = message.split(",");
        double xPos = Double.parseDouble(ballPos[0]);
        double yPos = Double.parseDouble(ballPos[1]);
        game.getBall().setPosition(xPos, yPos);
    }

    /**
     * Send the position of your Paddle to the other player. The "p" in front is so it can be distinguished from
     * Ball positions sent with the same PrintWriter.
     */
    public void sendPaddlePosition() {
        if (writer != null) {
            if (isServer) {
                writer.println("p" + game.getPaddle1().getY());
            } else {
                writer.println("p" + game.getPaddle2().getY());
            }
        }
    }

    /**
     * Server player sends Ball position to client player. The "b" in front is so it can be distinguished from
     * Paddle positions sent with the same PrintWriter.
     */
    public void sendBallPosition() {
        Ball ball = game.getBall();

        if (writer != null) {
            writer.println("b" + ball.getX() + "," + ball.getY());
        }
    }

    /**
     * Start a server.
     */
    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            isServer = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Try to establish a connection with client player.
     */
    public void listenForClient() {
        try {
            socket = serverSocket.accept();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ISO-8859-1"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1"), true);
            clientConnected = true;
            startReceiverThread();
            JOptionPane.showMessageDialog(
                    Game.renderer,
                    "Client connected, click OK to start.",
                    "Client connected",
                    JOptionPane.INFORMATION_MESSAGE
            );
            Thread.sleep(2000); // Delay two seconds before starting game
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connect client to server. If there is no server it returns false so the player can start a new server.
     *
     * @return boolean, true if client is connected, false if there is no server to connect to
     */
    public boolean connect() {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ISO-8859-1"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1"), true);
            clientConnected = true;
            startReceiverThread();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Start the receiver thread that listens for incoming messages with Paddle and Ball positions.
     */
    private void startReceiverThread() {
        receiving = true;
        Thread receiverThread = new Thread(new ReceiverThread());
        receiverThread.start();
    }

    public boolean isServer() {
        return isServer;
    }

    public boolean isClientConnected() {
        return clientConnected;
    }
}
