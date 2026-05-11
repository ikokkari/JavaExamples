import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

// As seen in http://paulbourke.net/fractals/randomtile/

/**
 * Fractal space-filling with random non-overlapping shapes.
 * <p>
 * The algorithm repeatedly generates a random shape, checks it against
 * existing shapes for overlap, and if it fits, grows it to the maximum
 * size that still avoids collision. Over many iterations, the shapes
 * pack the plane in a visually fractal pattern.
 * <p>
 * Improvements over the original implementation:
 * <ul>
 *   <li><strong>Spatial grid</strong> for overlap queries — only shapes
 *       in nearby cells are checked, reducing the per-shape cost from
 *       O(n) to roughly O(1) for well-distributed shapes.</li>
 *   <li><strong>Binary search</strong> for the maximum non-overlapping
 *       radius, replacing the linear 1.05× growth loop.</li>
 *   <li><strong>Adaptive radius</strong> with faster shrinking (0.97×
 *       instead of 0.999×) and a minimum radius floor, so the algorithm
 *       doesn't waste thousands of iterations on tiny decrements.</li>
 * </ul>
 *
 * @see <a href="http://paulbourke.net/fractals/randomtile/">Paul Bourke:
 *      Random Space Filling</a>
 * @author Ilkka Kokkarinen
 */
public class SpaceFiller {

    private static final SplittableRandom RNG = new SplittableRandom();

    // -----------------------------------------------------------------------
    // Shape representation
    // -----------------------------------------------------------------------

    /**
     * A shape together with its bounding circle, used for fast rejection
     * in overlap tests. The bounding circle avoids expensive Area
     * intersection for shapes that are clearly apart.
     */
    public record ShapeInfo(Area area, double cx, double cy, double r) {

        /** Create a deep copy with an independent Area. */
        public ShapeInfo copy() {
            return new ShapeInfo(new Area(area), cx, cy, r);
        }
    }

    // -----------------------------------------------------------------------
    // Shape factories — sealed interface so the compiler knows all variants.
    // -----------------------------------------------------------------------

    /**
     * A factory that generates a random shape of given radius centred at
     * a random position within the bounds.
     */
    public sealed interface ShapeFactory
            permits CircleFactory, RingFactory, PolygonFactory,
                    PlusFactory, StarFactory {
        ShapeInfo create(int w, int h, double r);
    }

    /** Filled circles. */
    public record CircleFactory() implements ShapeFactory {
        @Override
        public ShapeInfo create(int w, int h, double r) {
            double cx = r + RNG.nextDouble() * (w - 2 * r);
            double cy = r + RNG.nextDouble() * (h - 2 * r);
            var ellipse = new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r);
            return new ShapeInfo(new Area(ellipse), cx, cy, r);
        }
    }

    /** Rings (circle with a concentric hole). */
    public record RingFactory() implements ShapeFactory {
        @Override
        public ShapeInfo create(int w, int h, double r) {
            double cx = r + RNG.nextDouble() * (w - 2 * r);
            double cy = r + RNG.nextDouble() * (h - 2 * r);
            var outer = new Area(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
            var inner = new Area(new Ellipse2D.Double(
                    cx - 0.8 * r, cy - 0.8 * r, 1.6 * r, 1.6 * r));
            outer.subtract(inner);
            return new ShapeInfo(outer, cx, cy, r);
        }
    }

    /** Regular n-gons with random rotation, where n is in [minSides, maxSides]. */
    public record PolygonFactory(int minSides, int maxSides) implements ShapeFactory {
        @Override
        public ShapeInfo create(int w, int h, double r) {
            double cx = r + RNG.nextDouble() * (w - 2 * r);
            double cy = r + RNG.nextDouble() * (h - 2 * r);
            double angle = RNG.nextDouble() * Math.PI;
            int n = (minSides < maxSides)
                    ? RNG.nextInt(maxSides - minSides + 1) + minSides
                    : minSides;
            var path = new Path2D.Double();
            path.moveTo(cx + r * Math.cos(angle), cy + r * Math.sin(angle));
            for (int i = 1; i <= n; i++) {
                double a = angle + i * 2.0 * Math.PI / n;
                path.lineTo(cx + r * Math.cos(a), cy + r * Math.sin(a));
            }
            path.closePath();
            return new ShapeInfo(new Area(path), cx, cy, r);
        }
    }

    /** Plus/cross signs with a slight rotation that varies by position. */
    public record PlusFactory() implements ShapeFactory {
        @Override
        public ShapeInfo create(int w, int h, double r) {
            double cx = r + RNG.nextDouble() * (w - 2 * r);
            double cy = r + RNG.nextDouble() * (h - 2 * r);
            double angle = RNG.nextDouble() * Math.PI / 30
                    + Math.sin(0.02 * cx + 0.035 * cy);
            var path = new Path2D.Double();
            double a0 = angle;
            path.moveTo(cx + r * Math.cos(a0), cy + r * Math.sin(a0));
            for (int i = 1; i <= 12; i++) {
                double rr = (i % 3 != 0) ? r * 0.37 : r;
                double a = angle + i * Math.PI / 6;
                path.lineTo(cx + rr * Math.cos(a), cy + rr * Math.sin(a));
            }
            path.closePath();
            return new ShapeInfo(new Area(path), cx, cy, r);
        }
    }

    /** Star polygons with curved (quadratic Bézier) edges. */
    public record StarFactory() implements ShapeFactory {
        @Override
        public ShapeInfo create(int w, int h, double r) {
            int n = 2 * (RNG.nextInt(5) + 5); // 10, 12, 14, 16, or 18 points
            double cx = r + RNG.nextDouble() * (w - 2 * r);
            double cy = r + RNG.nextDouble() * (h - 2 * r);
            double[] xp = new double[n], yp = new double[n];
            for (int i = 0; i < n; i++) {
                double rr = (i % 2 == 0)
                        ? RNG.nextDouble() * r / 3 + 2 * r / 3  // tip
                        : RNG.nextDouble() * r / 10 + r / 10;   // groove
                double a = i * 2.0 * Math.PI / n;
                xp[i] = cx + Math.cos(a) * rr;
                yp[i] = cy + Math.sin(a) * rr;
            }
            var path = new Path2D.Double();
            path.moveTo(xp[0], yp[0]);
            for (int i = n - 1; i > 0; i -= 2) {
                path.quadTo(xp[i], yp[i], xp[i - 1], yp[i - 1]);
            }
            path.closePath();
            return new ShapeInfo(new Area(path), cx, cy, r);
        }
    }

    // -----------------------------------------------------------------------
    // Spatial grid — the key to efficient overlap queries.
    // Instead of checking every existing shape (O(n) per query), we
    // partition the plane into cells and only check shapes whose
    // bounding circles could possibly reach the query's bounding circle.
    // -----------------------------------------------------------------------

    private static final class SpatialGrid {
        private final double cellSize;
        private final int cols, rows;
        private final List<List<ShapeInfo>> cells;

        SpatialGrid(int w, int h, double cellSize) {
            this.cellSize = cellSize;
            this.cols = (int) Math.ceil(w / cellSize) + 1;
            this.rows = (int) Math.ceil(h / cellSize) + 1;
            this.cells = new ArrayList<>(cols * rows);
            for (int i = 0; i < cols * rows; i++) {
                cells.add(new ArrayList<>());
            }
        }

        /** Add a shape to every cell its bounding circle touches. */
        void add(ShapeInfo si) {
            int minCol = Math.max(0, (int) ((si.cx() - si.r()) / cellSize));
            int maxCol = Math.min(cols - 1, (int) ((si.cx() + si.r()) / cellSize));
            int minRow = Math.max(0, (int) ((si.cy() - si.r()) / cellSize));
            int maxRow = Math.min(rows - 1, (int) ((si.cy() + si.r()) / cellSize));
            for (int row = minRow; row <= maxRow; row++) {
                for (int col = minCol; col <= maxCol; col++) {
                    cells.get(row * cols + col).add(si);
                }
            }
        }

        /** Return all shapes in cells that a bounding circle touches. */
        List<ShapeInfo> candidates(ShapeInfo si) {
            var result = new ArrayList<ShapeInfo>();
            int minCol = Math.max(0, (int) ((si.cx() - si.r()) / cellSize));
            int maxCol = Math.min(cols - 1, (int) ((si.cx() + si.r()) / cellSize));
            int minRow = Math.max(0, (int) ((si.cy() - si.r()) / cellSize));
            int maxRow = Math.min(rows - 1, (int) ((si.cy() + si.r()) / cellSize));
            for (int row = minRow; row <= maxRow; row++) {
                for (int col = minCol; col <= maxCol; col++) {
                    result.addAll(cells.get(row * cols + col));
                }
            }
            return result;
        }
    }

    // -----------------------------------------------------------------------
    // Overlap testing
    // -----------------------------------------------------------------------

    private static long boundingTests = 0;
    private static long heavyTests = 0;

    /** Do the bounding circles of two shapes overlap? */
    private static boolean boundingOverlap(ShapeInfo a, ShapeInfo b) {
        double dx = a.cx() - b.cx();
        double dy = a.cy() - b.cy();
        double rr = a.r() + b.r();
        return dx * dx + dy * dy < rr * rr;
    }

    /**
     * Does the candidate shape overlap any shape in the grid?
     * The spatial grid narrows candidates; bounding-circle rejection
     * eliminates most of those; only the survivors need the expensive
     * Area intersection test.
     */
    private static boolean overlapsAny(ShapeInfo candidate, SpatialGrid grid) {
        for (var existing : grid.candidates(candidate)) {
            boundingTests++;
            if (boundingOverlap(candidate, existing)) {
                heavyTests++;
                var tmp = new Area(candidate.area());
                tmp.intersect(existing.area());
                if (!tmp.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Scaling
    // -----------------------------------------------------------------------

    /**
     * Return a new ShapeInfo that is the given shape scaled by {@code sf}
     * around its own centre. Does not mutate the input.
     */
    private static ShapeInfo scaled(ShapeInfo si, double sf) {
        var tr = AffineTransform.getTranslateInstance(si.cx(), si.cy());
        tr.concatenate(AffineTransform.getScaleInstance(sf, sf));
        tr.concatenate(AffineTransform.getTranslateInstance(-si.cx(), -si.cy()));
        var newArea = new Area(si.area());
        newArea.transform(tr);
        return new ShapeInfo(newArea, si.cx(), si.cy(), si.r() * sf);
    }

    // -----------------------------------------------------------------------
    // The main fill algorithm
    // -----------------------------------------------------------------------

    /**
     * Fill a region of size {@code w × h} with up to {@code n} random
     * non-overlapping shapes, using binary search to maximize each
     * shape's size.
     *
     * @param w         width of the region
     * @param h         height of the region
     * @param n         target number of shapes
     * @param g2        if non-null, shapes are rendered here as they are placed
     * @param factories the shape factories to choose from at random
     * @return the list of placed shapes
     */
    public static List<ShapeInfo> fillSpace(int w, int h, int n,
                                             Graphics2D g2,
                                             ShapeFactory... factories) {
        var placed = new ArrayList<ShapeInfo>(n);
        double maxR = w / 30.0;
        double minR = 1.0;  // don't waste time on sub-pixel shapes
        double cellSize = maxR * 2.5;
        var grid = new SpatialGrid(w, h, cellSize);
        boundingTests = heavyTests = 0;

        if (g2 != null) {
            g2.setColor(Color.WHITE);
            g2.fill(new Rectangle2D.Double(0, 0, w, h));
        }

        double r = maxR;
        int failStreak = 0;

        while (placed.size() < n) {
            var factory = factories[RNG.nextInt(factories.length)];
            var candidate = factory.create(w, h, r);

            if (overlapsAny(candidate, grid)) {
                failStreak++;
                // Shrink faster than the original's 0.999 — adaptive rate
                // based on how many consecutive failures we've had.
                if (failStreak > 50) {
                    r *= 0.95;
                } else {
                    r *= 0.97;
                }
                if (r < minR) {
                    // Reset to medium size — sometimes a larger shape fits
                    // elsewhere even though tiny ones keep colliding locally.
                    r = maxR * 0.3;
                    failStreak = 0;
                }
                continue;
            }
            failStreak = 0;

            // Binary search for the largest scale factor that fits.
            // This replaces the linear 1.05× growth loop.
            double lo = 1.0, hi = maxR / r;
            if (hi > 1.0) {
                // First, verify the upper bound actually overlaps (otherwise
                // we can jump straight to the maximum).
                var upper = scaled(candidate, hi);
                if (!overlapsAny(upper, grid) && isInsideBounds(upper, w, h)) {
                    candidate = upper;
                } else {
                    // Binary search: ~10 iterations gives <0.1% precision.
                    for (int iter = 0; iter < 10; iter++) {
                        double mid = (lo + hi) / 2;
                        var test = scaled(candidate, mid);
                        if (overlapsAny(test, grid) || !isInsideBounds(test, w, h)) {
                            hi = mid;
                        } else {
                            lo = mid;
                        }
                    }
                    candidate = scaled(candidate, lo);
                }
            }

            // Render and record.
            if (g2 != null) {
                int base = RNG.nextInt(100);
                g2.setColor(new Color(
                        base + RNG.nextInt(50),
                        base + RNG.nextInt(50),
                        base + RNG.nextInt(50)));
                g2.fill(candidate.area());
            }
            grid.add(candidate);
            placed.add(candidate);
            r = Math.min(maxR, 1.2 * r);
        }

        System.out.printf("Space fill complete: %,d shapes placed.%n", placed.size());
        System.out.printf("Overlap checks: %,d bounding, %,d heavy (%.1f%% rejection rate).%n",
                boundingTests, heavyTests,
                boundingTests > 0
                        ? 100.0 * (1 - (double) heavyTests / boundingTests) : 0);
        return placed;
    }

    /** Check that a shape's bounding circle is fully inside the region. */
    private static boolean isInsideBounds(ShapeInfo si, int w, int h) {
        return si.cx() - si.r() >= 0 && si.cx() + si.r() <= w
            && si.cy() - si.r() >= 0 && si.cy() + si.r() <= h;
    }

    // -----------------------------------------------------------------------
    // Swing display — periodic repaint while the fill runs in a worker thread.
    // -----------------------------------------------------------------------

    private static final class FillPanel extends JPanel {
        private final Image image;
        private Timer timer;

        FillPanel(Image image) {
            this.image = image;
            setPreferredSize(new Dimension(
                    image.getWidth(null), image.getHeight(null)));
            setBorder(BorderFactory.createEtchedBorder());
            timer = new Timer(100, _ -> repaint());
            timer.start();
        }

        void stopTimer() {
            if (timer != null) { timer.stop(); timer = null; }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, this);
        }
    }

    /**
     * Create a panel that fills with shapes in a background thread,
     * repainting periodically so the user can watch it happen.
     */
    public static FillPanel createPanel(int w, int h, int n,
                                         ShapeFactory... factories) {
        var img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        var panel = new FillPanel(img);
        Thread.ofVirtual().name("space-filler").start(() -> {
            var g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            fillSpace(w, h, n, g2, factories);
            g2.dispose();
            panel.stopTimer();
            panel.repaint(); // one final repaint to show the finished image
        });
        return panel;
    }

    /**
     * Render a high-resolution fill to PNG and JPEG files.
     */
    public static void createBigOne(int w, int h, int n, String filename,
                                     ShapeFactory... factories) {
        Thread.ofVirtual().name("big-render").start(() -> {
            var img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            var g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            fillSpace(w, h, n, g2, factories);
            g2.dispose();
            System.out.println("Rendering complete: " + filename);
            try {
                ImageIO.write(img, "PNG", new java.io.File(filename + ".png"));
                ImageIO.write(img, "JPG", new java.io.File(filename + ".jpg"));
            } catch (Exception e) {
                System.err.println("Write failed: " + e.getMessage());
            }
        });
    }

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var panel = createPanel(1000, 1000, 15000,
                    new CircleFactory(),
                    new RingFactory(),
                    new StarFactory(),
                    new PlusFactory(),
                    new PolygonFactory(3, 6));

            var frame = new JFrame(
                    "Fractal space filling with random shapes");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    panel.stopTimer();
                }
            });
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
