import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Produces a fixed-palette colour image from an original image using the
 * Floyd-Steinberg dithering algorithm to compensate for pixel colour errors.
 * See <a href="https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering">
 * Floyd-Steinberg dithering</a> on Wikipedia.
 *
 * <p>This implementation works for an arbitrary colour palette, not just
 * black and white. Modernized for Java 21+ with records, sealed types,
 * and contemporary idioms.
 *
 * @author Ilkka Kokkarinen
 */
public class FloydSteinberg {

    // --- Colour representation as a record instead of raw float arrays ---

    /**
     * A colour represented as three floating-point components in [0, 1].
     * Immutable by nature of being a record; mutable work is done via
     * the parallel {@link MutableColour} helper.
     */
    record Colour(float r, float g, float b) {

        /** Construct from an RGB integer (the format used by {@code BufferedImage}). */
        static Colour fromRGB(int rgb) {
            return new Colour(
                    ((rgb >> 16) & 0xFF) / 255.0f,
                    ((rgb >> 8) & 0xFF) / 255.0f,
                    (rgb & 0xFF) / 255.0f
            );
        }

        /** Pack back into a 24-bit RGB integer. */
        int toRGB() {
            int ri = Math.clamp((int) (r * 255), 0, 255);
            int gi = Math.clamp((int) (g * 255), 0, 255);
            int bi = Math.clamp((int) (b * 255), 0, 255);
            return (ri << 16) | (gi << 8) | bi;
        }

        /** Construct from a {@link Color} (AWT). */
        static Colour fromAWT(Color c) {
            float[] comp = c.getColorComponents(null);
            return new Colour(comp[0], comp[1], comp[2]);
        }

        /**
         * Perceptual distance to another colour, weighted by approximate
         * human luminance sensitivity (ITU BT.709 coefficients).
         */
        float distanceTo(Colour other) {
            float dr = Math.abs(r - other.r);
            float dg = Math.abs(g - other.g);
            float db = Math.abs(b - other.b);
            return 0.2126f * dr + 0.7152f * dg + 0.0722f * db;
        }

        /** Component-wise difference: {@code this - other}. */
        Colour minus(Colour other) {
            return new Colour(r - other.r, g - other.g, b - other.b);
        }
    }

    // --- Mutable colour buffer for the working scanlines ---

    /**
     * A mutable triple of colour components, used as workspace in the
     * scanline buffers where we accumulate error diffusion values.
     */
    static final class MutableColour {
        float r, g, b;

        void setFrom(Colour c) { r = c.r; g = c.g; b = c.b; }

        Colour toColour() { return new Colour(r, g, b); }

        /** Add a scaled error: {@code this += error * weight}. */
        void propagate(Colour error, float weight) {
            r += error.r * weight;
            g += error.g * weight;
            b += error.b * weight;
        }
    }

    // --- Error diffusion weights as a sealed hierarchy ---

    /**
     * The four neighbours that receive portions of the quantization error
     * in Floyd-Steinberg dithering. A sealed interface lets us enumerate
     * the cases exhaustively, and each case carries its own weight and
     * relative offset.
     */
    sealed interface Neighbour {
        int dx();
        int dy();
        float weight();

        record East()      implements Neighbour {
            @Override public int dx() { return  1; }
            @Override public int dy() { return  0; }
            @Override public float weight() { return 7.0f / 16; }
        }
        record SouthWest() implements Neighbour {
            @Override public int dx() { return -1; }
            @Override public int dy() { return  1; }
            @Override public float weight() { return 3.0f / 16; }
        }
        record South()     implements Neighbour {
            @Override public int dx() { return  0; }
            @Override public int dy() { return  1; }
            @Override public float weight() { return 5.0f / 16; }
        }
        record SouthEast() implements Neighbour {
            @Override public int dx() { return  1; }
            @Override public int dy() { return  1; }
            @Override public float weight() { return 1.0f / 16; }
        }
    }

    /** All four diffusion neighbours, instantiated once. */
    private static final List<Neighbour> NEIGHBOURS = List.of(
            new Neighbour.East(),
            new Neighbour.SouthWest(),
            new Neighbour.South(),
            new Neighbour.SouthEast()
    );

    // --- Core algorithm ---

    /**
     * Find the palette colour closest to the given target.
     */
    private static Colour closestInPalette(Colour target, List<Colour> palette) {
        Colour best = palette.getFirst();
        float bestDist = target.distanceTo(best);
        for (var candidate : palette.subList(1, palette.size())) {
            float d = target.distanceTo(candidate);
            if (d < bestDist) {
                bestDist = d;
                best = candidate;
            }
        }
        return best;
    }

    /**
     * Create a scanline buffer of the given width (plus one extra slot
     * for the east-neighbour overflow on the rightmost pixel).
     */
    private static MutableColour[] createScanline(int width) {
        var line = new MutableColour[width + 1];
        for (int x = 0; x <= width; x++) {
            line[x] = new MutableColour();
        }
        return line;
    }

    /** Load one row of the source image into the given scanline buffer. */
    private static void loadRow(BufferedImage src, int y, MutableColour[] line) {
        for (int x = 0; x < src.getWidth(); x++) {
            line[x].setFrom(Colour.fromRGB(src.getRGB(x, y)));
        }
    }

    /**
     * Compute the Floyd-Steinberg dithering for the given image and palette.
     *
     * @param orig    the original RGB image to dither
     * @param palette the colours allowed in the result image
     * @return a new {@code BufferedImage} containing the dithered result
     */
    public static BufferedImage dither(BufferedImage orig, List<Color> palette) {
        int w = orig.getWidth(), h = orig.getHeight();
        var img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        // Pre-convert the palette to our Colour record.
        var paletteColours = palette.stream().map(Colour::fromAWT).toList();

        // Two scanline buffers: current and next row.
        var currLine = createScanline(w);
        var nextLine = createScanline(w);
        loadRow(orig, 0, currLine);
        if (h > 1) { loadRow(orig, 1, nextLine); }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                var current = currLine[x].toColour();
                var closest = closestInPalette(current, paletteColours);
                img.setRGB(x, y, closest.toRGB());

                // Quantization error: what we wanted minus what we got.
                var error = current.minus(closest);

                // Diffuse the error to each neighbour, if in bounds.
                for (var neighbour : NEIGHBOURS) {
                    int nx = x + neighbour.dx();
                    int ny = y + neighbour.dy();
                    if (nx < 0 || nx >= w || ny < y || ny >= h) continue;
                    var target = (ny == y) ? currLine[nx] : nextLine[nx];
                    target.propagate(error, neighbour.weight());
                }
            }

            // Advance: current ← next, then read the row two ahead.
            var temp = currLine;
            currLine = nextLine;
            nextLine = temp;
            if (y + 2 < h) { loadRow(orig, y + 2, nextLine); }
        }
        return img;
    }

    // --- GUI helpers ---

    /**
     * Create a {@link JPanel} that displays the dithered version of the
     * given image, with the specified tooltip.
     */
    public static JPanel createPanel(BufferedImage img, List<Color> palette, String toolTip) {
        var result = dither(img, palette);
        return new JPanel() {
            {
                setPreferredSize(new Dimension(result.getWidth(), result.getHeight()));
                setToolTipText(toolTip);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(result, 0, 0, this);
            }
        };
    }

    // --- Demo entry point ---

    /**
     * A single palette definition: the colours and a human-readable label.
     */
    record PaletteDemo(String label, List<Color> colours) { }

    public static void main(String[] args) throws IOException {
        var sourceImage = ImageIO.read(Path.of("coffee.jpg").toFile());
        var scaled = new BufferedImage(600, 375, BufferedImage.TYPE_INT_RGB);
        var g = scaled.createGraphics();
        g.drawImage(sourceImage, 0, 0, 600, 375, null);
        g.dispose();

        var demos = List.of(
                new PaletteDemo("Blue, white, black, green",
                        List.of(Color.BLUE, Color.WHITE, Color.BLACK, Color.GREEN)),
                new PaletteDemo("Red, yellow, blue",
                        List.of(Color.RED, Color.YELLOW, Color.BLUE)),
                new PaletteDemo("Cyan, green, pink",
                        List.of(Color.CYAN, Color.GREEN, Color.PINK)),
                new PaletteDemo("Primary colours",
                        List.of(Color.BLACK, Color.RED, Color.BLUE, Color.GREEN)),
                new PaletteDemo("Binary black and white",
                        List.of(Color.BLACK, Color.WHITE)),
                new PaletteDemo("5 levels of grey",
                        List.of(Color.BLACK, new Color(64, 64, 64), new Color(128, 128, 128),
                                new Color(192, 192, 192), Color.WHITE))
        );

        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("Floyd-Steinberg Dithering Demo");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(new GridLayout(2, 3));
            for (var demo : demos) {
                frame.add(createPanel(scaled, demo.colours(), demo.label()));
            }
            frame.pack();
            frame.setVisible(true);
        });
    }
}