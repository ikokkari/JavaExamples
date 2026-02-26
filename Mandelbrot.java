import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Mandelbrot extends JPanel {

    // =======================================================================
    // Configuration constants
    // =======================================================================

    // Number of worker threads for rendering.
    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    // Iteration batch size per pixel per turn in the work queue.
    private static final int IROUNDS = 400;
    // Maximum total iterations before declaring a pixel "inside the set".
    private static final int MAX_ITER = 20000;
    // Pixel skip when seeding the initial edge frontier.
    private static final int EDGE_SKIP = 150;
    // Age-based direction rotation divisor (higher = broader DFS).
    private static final int BUNCH = 80;
    // Screen refresh interval in milliseconds.
    private static final int TIMER_FREQ = 25;
    // Zoom level below which we switch from double to BigDecimal arithmetic.
    private static final double DOUBLE_PRECISION_LIMIT = 1e-13;

    // Colour palette size (must be a power of 2 for fast modulo).
    private static final int COLS = 2048;
    private static final int[] colours = new int[COLS];
    // Neighbour offsets: right, down, left, up.
    private static final int[][] DIRS = { {0, 1}, {1, 0}, {0, -1}, {-1, 0} };

    // =======================================================================
    // Instance fields
    // =======================================================================

    private final int sizeP;                     // Panel width/height in pixels
    private BigComplex top;                      // Complex coord of top-left corner
    private BigDecimal size;                     // Width of the view in the complex plane
    private BigDecimal psize;                    // Size of one pixel in complex coords
    private BufferedImage display;               // Current rendered image
    private int sx = -1, sy = -1, bx, by;       // Mouse selection rectangle
    private final javax.swing.Timer timer;       // Repaint timer

    // Frontier for the current rendering pass.
    private volatile PriorityBlockingQueue<Pixel> activeFrontier = null;
    // Sentinel pixel that tells workers to stop.
    private final Pixel POISON = new Pixel(-1, -1, false);
    // Global age counter for pixel creation ordering.
    private static final AtomicInteger AGESTAMP = new AtomicInteger(0);
    // Thread pool.
    private static final ExecutorService es = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());

    // Zoom history for undo.
    private final Deque<BigDecimal[]> zoomHistory = new ArrayDeque<>();
    // Show info overlay?
    private boolean showInfo = true;
    // Stats from current render.
    private volatile int currentMaxIter = 0;
    private volatile int escapedCount = 0;
    private volatile int insideCount = 0;

    // =======================================================================
    // Colour palette (smooth continuous colouring)
    // =======================================================================

    private static int getEscapeColour(double smoothCount) {
        // Map the smooth iteration count into the palette with interpolation.
        double t = smoothCount % COLS;
        if (t < 0) t += COLS;
        int idx0 = (int) t;
        int idx1 = (idx0 + 1) % COLS;
        double frac = t - idx0;
        // Enforce mirror symmetry to avoid palette discontinuities.
        idx0 = mapIndex(idx0);
        idx1 = mapIndex(idx1);
        int c0 = getPaletteEntry(idx0);
        int c1 = getPaletteEntry(idx1);
        return lerpColour(c0, c1, frac);
    }

    private static int mapIndex(int c) {
        // Mirror: 0..COLS-1 -> 0..COLS-1..0
        c = c % (2 * COLS);
        if (c >= COLS) c = 2 * COLS - 1 - c;
        return Math.max(0, Math.min(c, COLS - 1));
    }

    private static int getPaletteEntry(int c) {
        if (colours[c] == 0) {
            double cc = Math.pow(c, 0.75);
            float hue = (float)(cc / 60.0 - Math.floor(cc / 60.0));
            float sat = (float)(0.6 + 0.35 * Math.sin(0.45 * cc + Math.cos(cc * 0.12)));
            float bri = (float)(0.55 + 0.35 * Math.sin(-0.40 * cc) + 0.10 * Math.sin(0.22 * cc - 0.1));
            colours[c] = Color.HSBtoRGB(hue, sat, bri) & 0x00FFFFFF;
        }
        return colours[c];
    }

    private static int lerpColour(int c0, int c1, double t) {
        int r = (int)((1 - t) * ((c0 >> 16) & 0xFF) + t * ((c1 >> 16) & 0xFF));
        int g = (int)((1 - t) * ((c0 >> 8)  & 0xFF) + t * ((c1 >> 8)  & 0xFF));
        int b = (int)((1 - t) * ( c0        & 0xFF) + t * ( c1        & 0xFF));
        return (r << 16) | (g << 8) | b;
    }

    // =======================================================================
    // Pixel class: tracks the iteration state of a single pixel
    // =======================================================================

    private class Pixel {
        final int x, y;
        final int age;
        final boolean useDouble;     // true = fast double path, false = BigDecimal
        int iter;                    // Iterations completed so far.

        // BigDecimal path state
        BigComplex c, z;
        // Double path state
        double cr, ci, zr, zi;

        Pixel(int x, int y, boolean useDouble) {
            this.x = x;
            this.y = y;
            this.age = AGESTAMP.incrementAndGet();
            this.useDouble = useDouble;
            if (x >= 0) {
                if (useDouble) {
                    cr = top.getRe().doubleValue() + psize.doubleValue() * x;
                    ci = top.getIm().doubleValue() - psize.doubleValue() * y;
                    zr = cr;
                    zi = ci;
                } else {
                    BigDecimal cx = top.getRe().add(psize.multiply(new BigDecimal(x)), BigComplex.mc);
                    BigDecimal cy = top.getIm().subtract(psize.multiply(new BigDecimal(y)), BigComplex.mc);
                    this.z = this.c = new BigComplex(cx, cy);
                }
            }
        }

        /**
         * Iterate this pixel for up to 'rounds' more steps.
         * Returns: positive double = smooth escape count (pixel escaped).
         *          negative double  = -(total iterations so far), not escaped yet.
         */
        double iterate(int rounds) {
            int limit = Math.min(iter + rounds, MAX_ITER);
            int toRun = limit - iter;
            if (toRun <= 0) return -iter; // Already at max.

            if (useDouble) {
                // Inline the hot loop so we can track zr/zi state across calls.
                double lzr = zr, lzi = zi;
                double zr2 = lzr * lzr, zi2 = lzi * lzi;
                int i = iter;
                int end = iter + toRun;
                while (zr2 + zi2 <= 65536.0 && i < end) {
                    lzi = 2.0 * lzr * lzi + ci;
                    lzr = zr2 - zi2 + cr;
                    zr2 = lzr * lzr;
                    zi2 = lzi * lzi;
                    i++;
                }
                zr = lzr; zi = lzi; iter = i;
                if (zr2 + zi2 > 65536.0) {
                    double log_zn = Math.log(zr2 + zi2) / 2.0;
                    double nu = Math.log(log_zn / Math.log(2.0)) / Math.log(2.0);
                    return iter + 1.0 - nu; // Smooth escape count.
                }
                return -iter;
            } else {
                // BigDecimal path (original logic, no smooth colouring).
                BigComplex zp = z;
                BigDecimal threshold = new BigDecimal("65536");
                for (int r = 0; r < toRun; r++) {
                    zp = zp.multiply(zp).add(c);
                    iter++;
                    BigDecimal re = zp.getRe();
                    BigDecimal im = zp.getIm();
                    BigDecimal d = re.multiply(re).add(im.multiply(im));
                    if (d.compareTo(threshold) > 0) {
                        z = c = null;
                        // Approximate smooth colouring.
                        double log_zn = Math.log(re.doubleValue() * re.doubleValue()
                                + im.doubleValue() * im.doubleValue()) / 2.0;
                        double nu = Math.log(log_zn / Math.log(2)) / Math.log(2);
                        return iter + 1.0 - nu;
                    }
                }
                z = zp;
                return -iter;
            }
        }
    }

    // =======================================================================
    // Pixel comparators (DFS / BFS frontier ordering)
    // =======================================================================

    private abstract class PixelComparator implements Comparator<Pixel> {
        @Override
        public final int compare(Pixel p1, Pixel p2) {
            if (p1 == POISON) return -1;
            if (p2 == POISON) return +1;
            // Prioritize pixels with fewer iterations.
            if (p1.iter != p2.iter) return Integer.compare(p1.iter, p2.iter);
            return comparePixels(p1, p2);
        }
        protected abstract int comparePixels(Pixel p1, Pixel p2);
    }

    private class DFSComparator extends PixelComparator {
        @Override protected int comparePixels(Pixel p1, Pixel p2) {
            return Integer.compare(p2.age, p1.age); // Newer first
        }
    }

    private class BFSComparator extends PixelComparator {
        @Override protected int comparePixels(Pixel p1, Pixel p2) {
            return Integer.compare(p1.age, p2.age); // Older first
        }
    }

    // =======================================================================
    // Rendering context and worker
    // =======================================================================

    private class RenderingContext {
        final PriorityBlockingQueue<Pixel> frontier;
        final BufferedImage image;
        final boolean[][] found;

        RenderingContext(PriorityBlockingQueue<Pixel> frontier,
                         BufferedImage image, boolean[][] found) {
            this.frontier = frontier;
            this.image = image;
            this.found = found;
        }
    }

    private class Renderer implements Callable<Integer> {
        private final RenderingContext ctx;
        private int pixelCount = 0;

        Renderer(RenderingContext ctx) { this.ctx = ctx; }

        @Override
        public Integer call() {
            try {
                while (ctx.frontier.size() > 0) {
                    Pixel p = ctx.frontier.take();
                    if (p == POISON) {
                        ctx.frontier.offer(POISON);  // Re-poison for other workers.
                        break;
                    }

                    double result = p.iterate(IROUNDS);

                    if (result > 0) {
                        // Pixel escaped — colour it and expand neighbours.
                        pixelCount++;
                        escapedCount++;
                        int rgb = getEscapeColour(result);
                        ctx.image.setRGB(p.x, p.y, rgb);
                        currentMaxIter = Math.max(currentMaxIter, p.iter);
                        expandNeighbours(ctx, p);
                    } else if (-result >= MAX_ITER) {
                        // Pixel hit max iterations — it's inside the set.
                        // Colour it black (already default) and DON'T re-queue.
                        insideCount++;
                        currentMaxIter = Math.max(currentMaxIter, (int)(-result));
                        expandNeighbours(ctx, p);
                    } else {
                        // Not yet decided — put back in queue for more iterations.
                        ctx.frontier.offer(p);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Renderer crashed: " + e.getMessage());
                e.printStackTrace();
            }
            return pixelCount;
        }

        private void expandNeighbours(RenderingContext ctx, Pixel p) {
            for (int i = 0; i < 4; i++) {
                int[] d = DIRS[(i + p.age / BUNCH) % 4];
                int nx = p.x + d[0];
                int ny = p.y + d[1];
                if (nx >= 0 && nx < sizeP && ny >= 0 && ny < sizeP) {
                    // Atomic check-and-set via synchronized on the found array.
                    synchronized (ctx.found) {
                        if (!ctx.found[nx][ny]) {
                            ctx.found[nx][ny] = true;
                            ctx.frontier.offer(new Pixel(nx, ny,
                                    size.doubleValue() > DOUBLE_PRECISION_LIMIT));
                        }
                    }
                }
            }
        }
    }

    // =======================================================================
    // Rendering orchestration
    // =======================================================================

    public void computeImage(Comparator<Pixel> frontierComp) {
        int sc = bigScale(size);
        BigComplex.mc = new MathContext(sc + 6);
        size = new BigDecimal(size.toString(), BigComplex.mc);
        top = new BigComplex(
                new BigDecimal(top.getRe().toString(), BigComplex.mc),
                new BigDecimal(top.getIm().toString(), BigComplex.mc)
        );

        boolean[][] localFound = new boolean[sizeP][sizeP];
        BufferedImage localDisplay = new BufferedImage(sizeP, sizeP, BufferedImage.TYPE_INT_RGB);
        this.display = localDisplay;
        this.currentMaxIter = 0;
        this.escapedCount = 0;
        this.insideCount = 0;

        // Poison the previous frontier to stop old workers.
        if (activeFrontier != null) {
            activeFrontier.offer(POISON);
        }

        PriorityBlockingQueue<Pixel> localFrontier =
                new PriorityBlockingQueue<>(256, frontierComp);
        activeFrontier = localFrontier;
        psize = size.multiply(new BigDecimal(1.0 / sizeP, BigComplex.mc), BigComplex.mc);

        boolean useDouble = size.doubleValue() > DOUBLE_PRECISION_LIMIT;

        // Seed the edges of the image.
        for (int y = 0; y < sizeP; y += EDGE_SKIP) {
            localFound[0][y] = true;
            localFrontier.offer(new Pixel(0, y, useDouble));
            localFound[sizeP - 1][y] = true;
            localFrontier.offer(new Pixel(sizeP - 1, y, useDouble));
        }
        for (int x = 0; x < sizeP; x += EDGE_SKIP) {
            localFound[x][0] = true;
            localFrontier.offer(new Pixel(x, 0, useDouble));
            localFound[x][sizeP - 1] = true;
            localFrontier.offer(new Pixel(x, sizeP - 1, useDouble));
        }

        RenderingContext ctx = new RenderingContext(localFrontier, localDisplay, localFound);
        for (int i = 0; i < THREADS; i++) {
            es.submit(new Renderer(ctx));
        }
    }

    // =======================================================================
    // Utility
    // =======================================================================

    private static final BigDecimal TWO = new BigDecimal(2);

    private static int bigScale(BigDecimal x) {
        int count = 1;
        while (x.compareTo(BigDecimal.ONE) < 0) {
            count++;
            x = x.multiply(TWO);
        }
        return count;
    }

    // =======================================================================
    // Constructors
    // =======================================================================

    public Mandelbrot(int sizeP) {
        this(sizeP, new BigComplex(-2.0, 1.2), new BigDecimal("2.5", BigComplex.mc));
    }

    public Mandelbrot(int sizeP, BigComplex top, BigDecimal size) {
        this.setPreferredSize(new Dimension(sizeP, sizeP));
        this.display = new BufferedImage(sizeP, sizeP, BufferedImage.TYPE_INT_RGB);
        this.top = top;
        this.sizeP = sizeP;
        this.size = size;
        this.setFocusable(true);

        PixelComparator pixelComp = new DFSComparator();

        // --- Mouse drag for zoom selection ---
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent me) {
                bx = me.getX();
                by = me.getY();
                if (by < 0 || bx < 0) return;
                if (bx < sx) bx = sx;
                if (by < sy) by = sy;
                // Enforce square selection.
                int side = Math.max(bx - sx, by - sy);
                bx = sx + side;
                by = sy + side;
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                bx = sx = me.getX();
                by = sy = me.getY();
                requestFocusInWindow();
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                if (sx < 0 || (bx - sx) < 4) {
                    sx = -1;
                    return;
                }
                // Save current view for undo.
                zoomHistory.push(new BigDecimal[]{
                        Mandelbrot.this.top.getRe(),
                        Mandelbrot.this.top.getIm(),
                        Mandelbrot.this.size
                });

                BigDecimal ps = Mandelbrot.this.size.multiply(
                        new BigDecimal(1.0 / Mandelbrot.this.sizeP, BigComplex.mc), BigComplex.mc);
                Mandelbrot.this.top = new BigComplex(
                        Mandelbrot.this.top.getRe().add(ps.multiply(new BigDecimal(sx), BigComplex.mc), BigComplex.mc),
                        Mandelbrot.this.top.getIm().subtract(ps.multiply(new BigDecimal(sy), BigComplex.mc), BigComplex.mc)
                );
                double sf = (bx - sx) / (double) Mandelbrot.this.sizeP;
                Mandelbrot.this.size = Mandelbrot.this.size.multiply(
                        new BigDecimal(sf, BigComplex.mc), BigComplex.mc);
                sx = -1;
                computeImage(pixelComp);
            }
        });

        // --- Keyboard controls ---
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_U: // Undo zoom
                    case KeyEvent.VK_BACK_SPACE:
                        if (!zoomHistory.isEmpty()) {
                            BigDecimal[] prev = zoomHistory.pop();
                            Mandelbrot.this.top = new BigComplex(prev[0], prev[1]);
                            Mandelbrot.this.size = prev[2];
                            computeImage(pixelComp);
                        }
                        break;
                    case KeyEvent.VK_R: // Reset to default view
                        zoomHistory.clear();
                        Mandelbrot.this.top = new BigComplex(-2.0, 1.2);
                        Mandelbrot.this.size = new BigDecimal("2.5", BigComplex.mc);
                        computeImage(pixelComp);
                        break;
                    case KeyEvent.VK_I: // Toggle info overlay
                        showInfo = !showInfo;
                        break;
                }
            }
        });

        computeImage(pixelComp);
        this.timer = new javax.swing.Timer(TIMER_FREQ, ae -> repaint());
        this.timer.start();
    }

    // =======================================================================
    // Painting
    // =======================================================================

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(display, 0, 0, this);

        // Selection rectangle with crosshair.
        if (sx > -1) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(255, 255, 255, 180));
            g2.drawRect(sx, sy, bx - sx, by - sy);
            // Dim the area outside the selection.
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRect(0, 0, sizeP, sy);
            g2.fillRect(0, by, sizeP, sizeP - by);
            g2.fillRect(0, sy, sx, by - sy);
            g2.fillRect(bx, sy, sizeP - bx, by - sy);
        }

        // Info overlay.
        if (showInfo) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect(6, 6, 340, 82, 8, 8);
            g2.setColor(new Color(200, 220, 255));
            int y = 22;
            g2.drawString(String.format("Zoom: %.3e  |  Threads: %d", size.doubleValue(), THREADS), 14, y);
            y += 16;
            g2.drawString(String.format("Centre: %.12f + %.12fi",
                    top.getRe().doubleValue() + size.doubleValue() / 2,
                    top.getIm().doubleValue() - size.doubleValue() / 2), 14, y);
            y += 16;
            g2.drawString(String.format("Max iter: %,d  |  Escaped: %,d  Inside: %,d",
                    currentMaxIter, escapedCount, insideCount), 14, y);
            y += 16;
            g2.drawString("Keys: [U]ndo zoom  [R]eset  [I]nfo toggle", 14, y);
        }
    }

    // =======================================================================
    // Lifecycle
    // =======================================================================

    public void terminate() {
        timer.stop();
        if (activeFrontier != null) activeFrontier.offer(POISON);
        es.shutdownNow();
    }

    // =======================================================================
    // Main
    // =======================================================================

    public static void main(String[] args) {
        JFrame f = new JFrame("Mandelbrot Explorer");
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Mandelbrot mandel = new Mandelbrot(1000);
        f.setLayout(new FlowLayout());
        f.add(mandel);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                mandel.terminate();
                f.dispose();
            }
        });
        f.pack();
        f.setLocationRelativeTo(null); // Centre on screen.
        f.setVisible(true);
        mandel.requestFocusInWindow();
    }
}