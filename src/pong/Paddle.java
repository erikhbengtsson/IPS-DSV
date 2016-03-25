package pong;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Class representing the players paddles/rackets.
 */
public class Paddle {
    private double x;   // Horizontal position
    private double y;   // Vertical position
    private double acceleration;
    private double deceleration;
    private double speed;
    private double maxSpeed;
    private boolean movingUp;
    private boolean movingDown;
    private int width;
    private int height;
    private BufferedImage image;

    public Paddle() {
        width = 32;
        height = 128;
        acceleration = 1;
        deceleration = 0.6;
        maxSpeed = 10;
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/paddle.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update Paddle position. Speed is increased and decreased by acceleration and deceleration variables.
     */
    public void update() {
        if (movingUp) {
            speed -= acceleration;
            if (speed < -maxSpeed) {
                speed = -maxSpeed;
            }
        } else if (movingDown) {
            speed += acceleration;
            if (speed > maxSpeed) {
                speed = maxSpeed;
            }
        } else {
            if (speed < 0) {
                speed += deceleration;
                if (speed > 0) {
                    speed = 0;
                }
            } else if (speed > 0) {
                speed -= deceleration;
                if (speed < 0) {
                    speed = 0;
                }
            }
        }
        y += speed;
        checkBounds();
    }

    /**
     * Check if the Paddle is moving outside the window.
     */
    private void checkBounds() {
        if (y < 0) {
            y = 0;
        } else if (y + height > Renderer.HEIGHT) {
            y = Renderer.HEIGHT - height;
        }
    }

    /**
     * Draw the Paddle.
     *
     * @param g Graphics2D
     */
    public void draw(Graphics2D g) {
        g.drawImage(image, (int) x, (int) y, null);
    }

    /**
     * Get the Rectangle surrounding the Paddle. This is used to check collision with the Ball.
     *
     * @return Rectangle
     */
    public Rectangle getRectangle() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    // Getters and setters

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setMovingUp(boolean movingUp) {
        this.movingUp = movingUp;
    }

    public void setMovingDown(boolean movingDown) {
        this.movingDown = movingDown;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getY() {
        return y;
    }
}
