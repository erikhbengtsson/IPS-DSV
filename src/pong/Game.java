package pong;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Class containing the game logic. The game loop is in the run-method which calls the update- and draw-methods
 * every frame to update movement and graphics.
 */
public class Game implements Runnable, KeyListener {
    public static final Renderer renderer = Renderer.getInstance();
    private Thread thread;  // Used to start the game loop
    private volatile boolean running;   // Used to stop the game loop
    private Paddle paddle1;
    private Paddle paddle2;
    private Ball ball;
    private BufferedImage background;
    private NetworkComponent network;   // Contains all network related methods
    private final double BOUNCE_ANGLE = (5 * Math.PI) / 15; // Used to set Balls directions when colliding with Paddle
    private int serverScore;
    private int clientScore;
    private Font scoreFont;

    public Game() {
        initGame();
        network = new NetworkComponent(this);

        // If there is no server to connect to, start one
        if (!network.connect()) {
            network.startServer();
        }
    }

    /**
     * Initialize game variables.
     */
    private void initGame() {
        renderer.addKeyListener(this);
        paddle1 = new Paddle();
        paddle1.setPosition(20, Renderer.HEIGHT / 2 - paddle1.getHeight() / 2);
        paddle2 = new Paddle();
        paddle2.setPosition(Renderer.WIDTH - (20 + paddle1.getWidth()), Renderer.HEIGHT / 2 - paddle1.getHeight() / 2);
        ball = new Ball();
        serverScore = 0;
        clientScore = 0;
        scoreFont = new Font("Arial", Font.BOLD, 40);
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/court.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the game loop.
     */
    public synchronized void start() {
        if (thread == null || !running) {
            running = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Stop the game loop.
     */
    public synchronized void stop() {
        running = false;
    }

    /**
     * Game loop in which the game is updated and drawn.
     */
    @Override
    public void run() {
        int fps = 60;
        long targetTime = 1000000000 / fps; // Target time in nanoseconds

        while (running) {
            long startTime = System.nanoTime(); // Start time for frame

            update();
            draw(renderer.getGraphics2D());
            renderer.render();  // Paints the image drawn by renderer's Graphics2D object

            if (network.isServer() && !network.isClientConnected()) {
                network.listenForClient();
            }

            long frameTime = System.nanoTime() - startTime; // Time that frame has taken so far

            // If frame is to fast, sleep to achieve target fps
            if (frameTime < targetTime) {
                try{
                    Thread.sleep((targetTime - frameTime) / 1000000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Update movement and check for collisions.
     */
    private void update() {
        paddle1.update();
        paddle2.update();

        // Ball should only be updated if player is server. Ball position is then sent to client player.
        if (network.isServer()) {
            ball.update();
            network.sendBallPosition();
        }

        network.sendPaddlePosition();   // Send your paddle position to the other player
        paddleBallCollision(paddle1, true);
        paddleBallCollision(paddle2, false);
        updateScore();
    }

    /**
     * Handle collisions between a Paddle and the Ball.
     *
     * @param paddle Paddle
     * @param isLeft boolean, true if Paddle is to the left, false if Paddle is to the right
     */
    private void paddleBallCollision(Paddle paddle, boolean isLeft) {
        if (paddle.getRectangle().intersects(ball.getRectangle())) {
            double ballCenter = ball.getY() + ball.getHeight() / 2;
            double paddleIntersect = (paddle.getY() + paddle.getHeight() / 2) - ballCenter;
            double normalizedPaddleIntersect = (paddleIntersect / (paddle.getHeight() / 2));
            double angle = normalizedPaddleIntersect * BOUNCE_ANGLE;

            if (isLeft) {
                ball.setVelocityX(ball.getSpeed() * Math.cos(angle));
            } else {
                ball.setVelocityX(ball.getSpeed() * -Math.cos(angle));
            }

            ball.setVelocityY(ball.getSpeed() * -Math.sin(angle));
            ball.setSpeed(ball.getSpeed() + 0.3);
        }
    }

    /**
     * Update the score when Ball passes through left or right border of the game window.
     */
    private void updateScore() {
        if (ball.getX() + ball.getWidth() < 0) {
            clientScore += 1;
            ball.setStartPosition();
        } else if (ball.getX() > Renderer.WIDTH) {
            serverScore += 1;
            ball.setStartPosition();
        }
    }

    /**
     * Draw the game.
     *
     * @param g Graphics2D
     */
    private void draw(Graphics2D g) {
        g.drawImage(background, 0, 0, null);
        paddle1.draw(g);
        paddle2.draw(g);
        drawScore(g);
        ball.draw(g);
    }

    /**
     * Draw score as a String on each players side of the window.
     *
     * @param g Graphics2D
     */
    private void drawScore(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(scoreFont);
        g.drawString("" + serverScore, Renderer.WIDTH / 4 - 20, 50);
        g.drawString("" + clientScore, (Renderer.WIDTH / 4) * 3 - 20, 50);
    }

    // Getters

    public Paddle getPaddle1() {
        return paddle1;
    }

    public Paddle getPaddle2() {
        return paddle2;
    }

    public Ball getBall() {
        return ball;
    }

    // Listener methods

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (network.isServer()) {
                paddle1.setMovingUp(true);
            } else {
                paddle2.setMovingUp(true);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (network.isServer()) {
                paddle1.setMovingDown(true);
            } else {
                paddle2.setMovingDown(true);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (network.isServer()) {
                paddle1.setMovingUp(false);
            } else {
                paddle2.setMovingUp(false);
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (network.isServer()) {
                paddle1.setMovingDown(false);
            } else {
                paddle2.setMovingDown(false);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
}
