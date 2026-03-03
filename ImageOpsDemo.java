import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Demonstration of image processing using {@code BufferedImageOp} to perform
 * operations such as colour channel scrambling, rotation, and convolution-based
 * blurring. Modernized for Java 21+ — the old {@code ImageFilter} /
 * {@code FilteredImageSource} / {@code MediaTracker} pipeline is replaced
 * with direct {@code BufferedImage} manipulation throughout.
 *
 * @author Ilkka Kokkarinen
 */
public class ImageOpsDemo {

    // --- Image operations ---

    /**
     * A functional interface for transforming a single pixel's ARGB value.
     * Replaces the old {@code RGBImageFilter} subclass approach.
     */
    @FunctionalInterface
    interface PixelTransform {
        int apply(int x, int y, int argb);
    }

    /**
     * Apply a per-pixel transformation to every pixel in the source image.
     * This is the modern replacement for the {@code FilteredImageSource} /
     * {@code RGBImageFilter} / {@code Toolkit.createImage} incantation chain.
     */
    private static BufferedImage applyPixelTransform(BufferedImage src, PixelTransform xf) {
        int w = src.getWidth(), h = src.getHeight();
        var dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                dst.setRGB(x, y, xf.apply(x, y, src.getRGB(x, y)));
            }
        }
        return dst;
    }

    /**
     * Scramble the colour channels of each pixel in a pattern that depends
     * on the pixel's position, creating a tile-based colour permutation effect.
     */
    public static BufferedImage scrambleRGB(BufferedImage img, int xs, int ys) {
        return applyPixelTransform(img, (x, y, argb) -> {
            int a = (argb >> 24) & 0xFF;
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8)  & 0xFF;
            int b =  argb        & 0xFF;
            // Rotate the channel assignments based on tile parity.
            return switch ((x / xs + y / ys) % 4) {
                case 0  -> pack(a, g, r, b);  // swap R↔G
                case 1  -> pack(a, b, g, r);  // swap R↔B
                case 2  -> pack(a, r, b, g);  // swap G↔B
                default -> argb;              // unchanged
            };
        });
    }

    /** Pack four channel values into a single ARGB int. */
    private static int pack(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Rotate the image by the given number of 90-degree steps using a bicubic
     * affine transform — now applied directly as a {@code BufferedImageOp},
     * bypassing the old {@code BufferedImageFilter} → {@code FilteredImageSource}
     * → {@code MediaTracker} detour entirely.
     */
    public static BufferedImage rotate(BufferedImage img, int steps) {
        var rot = AffineTransform.getRotateInstance(
                steps * Math.PI / 2.0,
                img.getWidth() / 2.0,
                img.getHeight() / 2.0
        );
        BufferedImageOp op = new AffineTransformOp(rot, AffineTransformOp.TYPE_BICUBIC);
        return op.filter(img, null);
    }

    /**
     * Apply a box blur of the given radius using image convolution.
     * Many image processing operations can be rephrased as convolutions
     * where different kernels achieve different ends. See the Wikipedia page
     * on <a href="https://en.wikipedia.org/wiki/Kernel_(image_processing)">
     * kernel image processing</a> for a taste. Too bad the Java graphics
     * library does not also have "deconvolution" like Wolfram...
     */
    public static BufferedImage boxBlur(BufferedImage img, int radius) {
        int side = 2 * radius + 1; // kernel sizes are usually odd
        float weight = 1.0f / (side * side);
        var weights = new float[side * side];
        Arrays.fill(weights, weight);
        BufferedImageOp op = new ConvolveOp(
                new Kernel(side, side, weights), ConvolveOp.EDGE_NO_OP, null
        );
        return op.filter(img, null);
    }

    // --- Scaling and cropping utilities ---

    /** Scale a BufferedImage to the given dimensions with bilinear interpolation. */
    private static BufferedImage scale(BufferedImage src, int w, int h) {
        var dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        var g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }

    /**
     * Crop a rectangular region from a BufferedImage. Replaces the old
     * {@code CropImageFilter} → {@code FilteredImageSource} → {@code Toolkit}
     * pipeline with a single standard library call.
     */
    private static BufferedImage crop(BufferedImage src, int x, int y, int w, int h) {
        return src.getSubimage(x, y, w, h);
    }

    // --- Display panel ---

    /**
     * A named image operation pairing a label with its result, displayed
     * as one panel in the demo frame.
     */
    record ImageOp(String label, BufferedImage result) { }

    /** A simple panel that displays a BufferedImage with a tooltip. */
    private static JPanel imagePanel(ImageOp op) {
        return new JPanel() {
            {
                setToolTipText(op.label());
                setPreferredSize(new Dimension(op.result().getWidth(),
                        op.result().getHeight()));
                setBorder(BorderFactory.createEtchedBorder());
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(op.result(), 0, 0, this);
            }
        };
    }

    // --- Entry point ---

    public static void main(String[] args) throws IOException {
        var raw = ImageIO.read(Path.of("coffee.jpg").toFile());
        var scaled = scale(raw, 800, 600);
        var coffee = crop(scaled, 100, 0, 600, 600);

        var ops = List.of(
                new ImageOp("Scramble",               scrambleRGB(coffee, 7, 13)),
                new ImageOp("Rotate 90 degrees right", rotate(coffee, 1)),
                new ImageOp("Box blur",                boxBlur(coffee, 5))
        );

        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("Image Operations Demo");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(new FlowLayout());
            for (var op : ops) {
                frame.add(imagePanel(op));
            }
            frame.pack();
            frame.setVisible(true);
        });
    }
}