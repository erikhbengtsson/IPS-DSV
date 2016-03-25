package pong;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Singleton class rendering the game window by drawing a BufferedImage. The Graphics2D object in this class is used to
 * draw all graphics in the game.
 */
public class Renderer extends JFrame {
    private static final Renderer renderer = new Renderer();
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private BufferedImage image;
    private Graphics2D graphics2D;

    private Renderer() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        graphics2D = (Graphics2D) image.getGraphics();
        getContentPane().setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        pack();
    }

    public static Renderer getInstance() {
        return renderer;
    }

    public void render() {
        Graphics g = getGraphics();
        g.drawImage(image, 0, getInsets().top, null);
        g.dispose();
    }

    public Graphics2D getGraphics2D() {
        return graphics2D;
    }
}
