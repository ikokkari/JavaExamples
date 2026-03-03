import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A Swing example class demonstrating how to load, render, and modify images.
 * Modernized for Java 21+ with {@code BufferedImage} throughout (no raw
 * {@code Image}), modern random generation, resource cleanup, and
 * contemporary Swing idioms.
 *
 * @author Ilkka Kokkarinen
 */
public class ImageDemo extends JPanel {

    private final BufferedImage coffee;
    private final BufferedImage flappy;
    private final BufferedImage bimg1;
    private final BufferedImage bimg2;
    private final RandomGenerator rng = RandomGenerator.getDefault();

    // --- Pixel utilities ---

    /** Pack three colour components (each 0–255) into a single RGB int. */
    private static int rgb(int r, int g, int b) {
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    /**
     * Scale a {@code BufferedImage} to the given dimensions using bilinear
     * interpolation, returning a new {@code BufferedImage}. This replaces
     * the old {@code Image.getScaledInstance} which returned a lazy toolkit
     * {@code Image} requiring a {@code MediaTracker} or {@code ImageObserver}.
     */
    private static BufferedImage scale(BufferedImage src, int w, int h) {
        var scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        var g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return scaled;
    }

    // --- Procedural image generation ---

    /**
     * A functional interface for computing a pixel colour from coordinates.
     * Used to generate the two gradient images declaratively.
     */
    @FunctionalInterface
    interface PixelFunction {
        int colourAt(int x, int y);
    }

    /** Create a 256×256 image whose pixels are defined by a function. */
    private static BufferedImage generateImage(PixelFunction fn) {
        var img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        IntStream.range(0, 256).forEach(x ->
                IntStream.range(0, 256).forEach(y ->
                        img.setRGB(x, y, fn.colourAt(x, y))
                )
        );
        return img;
    }

    // --- Construction ---

    public ImageDemo() throws IOException {
        // Read images — ImageIO.read returns BufferedImage directly.
        var rawCoffee = ImageIO.read(Path.of("coffee.jpg").toFile());
        var rawFlappy = ImageIO.read(Path.of("flappy.png").toFile());
        // Take a wild guess which ImageIO method would then write an image to a file.

        // Scale to desired sizes, staying in BufferedImage land throughout.
        coffee = scale(rawCoffee, 800, 600);
        flappy = scale(rawFlappy, 25, 25);
        var lilCoffee = scale(rawCoffee, 50, 50);

        // Generate two procedural gradient images from pixel functions.
        bimg1 = generateImage((x, y) -> rgb(x, 256 - y, (x + y) % 256));
        bimg2 = generateImage((x, y) -> rgb((2 * x + y) % 256, (x + y) % 256, y));

        // Draw an antialiased circle into the first generated image.
        var g2 = bimg1.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(5.0f));
        g2.setPaint(Color.BLACK);
        g2.draw(new Ellipse2D.Double(64, 64, 128, 128));
        g2.dispose();

        // An image can also serve as an ImageIcon inside JButton or JLabel.
        setLayout(new FlowLayout());
        var button = new JButton(new ImageIcon(lilCoffee));
        button.addActionListener(_ -> repaint());
        add(button);

        setPreferredSize(new Dimension(coffee.getWidth() + 256, coffee.getHeight()));
    }

    // --- Painting ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2 = (Graphics2D) g;
        g2.drawImage(coffee, 0, 0, this);
        g2.drawImage(bimg1, coffee.getWidth(), 0, this);
        g2.drawImage(bimg2, coffee.getWidth(), 256, this);
        // Scatter some Flappy Birds — PNG transparency is preserved automatically.
        for (int i = 0; i < 20; i++) {
            g2.drawImage(flappy,
                    rng.nextInt(getWidth()), rng.nextInt(getHeight()), this);
        }
    }

    // --- Entry point ---

    public static void main(String[] args) throws IOException {
        var demo = new ImageDemo();
        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("Image Demo");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(new FlowLayout());
            frame.add(demo);
            frame.pack();
            frame.setVisible(true);
        });
    }
}