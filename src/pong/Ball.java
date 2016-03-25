package pong;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * Class representing the ball.
 */
public class Ball {
    private double x;   // Horizontal position
    private double y;   // Vertical position
    private double velocityX;   // Horizontal direction, negative value is left, positive is right
    private double velocityY;   // Vertical direction, negative value is up, positive is down
    private double speed;
    private double maxSpeed;
    private int width;
    private int height;
    private BufferedImage image;

    public Ball() {
        width = height = 32;
        speed = 7;
        maxSpeed = 12;
        setStartPosition();
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/ball.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset the position to the middle of the window and set a random direction.
     */
    public void setStartPosition() {
        x = Renderer.WIDTH / 2 - width / 2;
        y = Renderer.HEIGHT / 2 - height / 2;
        Random random = new Random();
        speed = 7;
        velocityX = speed;
        velocityY = (double) random.nextInt(7);
        boolean negativeX = random.nextBoolean();
        boolean negativeY = random.nextBoolean();

        if (negativeX) {
            velocityX = -speed;
        }

        if (negativeY) {
            velocityY = -velocityY;
        }
    }

    /**
     * Update position.
     */
    public void update() {
        x += velocityX;
        y += velocityY;

        if (speed > maxSpeed) {
            speed = maxSpeed;
        }
        checkBounds();
    }

    /**
     * Check if the Ball is on the top or bottom bounds of the window. If so, the vertical direction is reversed to
     * simulate bouncing of a wall.
     */
    private void checkBounds() {
        if (y < 0) {
            y = 0;
            velocityY = Math.abs(velocityY);
        } else if (y + height > Renderer.HEIGHT) {
            y = Renderer.HEIGHT - height;
            velocityY = -velocityY;
        }
    }

    /**
     * Draw the Ball.
     *
     * @param g Graphics2D
     */
    public void draw(Graphics2D g) {
        g.drawImage(image, (int) x, (int) y, null);
    }

    /**
     * Get the Rectangle surrounding the Ball. This is used to check collision with the Paddle.
     *
     * @return Rectangle
     */
    public Rectangle getRectangle() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    // Getters and setters

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getSpeed() {
        return speed;
    }
}
