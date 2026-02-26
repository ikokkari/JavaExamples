import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * A demonstration of semaphores in multithreaded programming. Animated sliders
 * move on a tile board, each controlled by its own thread. At no time should
 * two sliders occupy the same tile — enforced by per-tile semaphores.
 *
 * <p>Toggle {@code DEADLOCKING} to compare fail-fast tryAcquire (safe) versus
 * blocking acquire (will eventually deadlock when two sliders try to swap tiles
 * simultaneously).
 *
 * <p>In real games, one animation thread would move all objects (making mutual
 * exclusion trivial). The per-slider threading here is deliberate overkill —
 * this is a concurrency demo, not an efficient animation. Updated for Java 21+
 * with virtual threads, modern random API, and Swing best practices.
 *
 * @author Ilkka Kokkarinen
 */
public class Sliders extends JPanel {

    // --- Configuration ---
    /** Toggle to demonstrate deadlock vs. safe operation. */
    private static final boolean DEADLOCKING = false;
    /** Whether sliders are drawn in bright random colours or black. */
    private static final boolean COLOURS = true;
    /** Size of one tile in pixels. */
    private static final int TILESIZE = 60;
    /** Possible movement directions: right, down, left, up. */
    private static final int[][] DIRS = { {1, 0}, {0, 1}, {-1, 0}, {0, -1} };

    // --- Board state ---
    private final int width;
    private final int height;
    /** Per-tile semaphore: to move from (sx,sy) to (tx,ty), a slider must
     *  hold permits for both tiles simultaneously. */
    private final Semaphore[][] permitToEnter;
    private final Slider[] sliders;
    /** Volatile flag: when set to false, all slider threads exit their loop. */
    private volatile boolean running = true;

    // -----------------------------------------------------------------------
    // SLIDER (inner class)
    // -----------------------------------------------------------------------

    private class Slider {
        // Tile coordinates: (sx,sy) = start of current move, (tx,ty) = target.
        int sx, sy, tx, ty;
        // Interpolated pixel coordinates for smooth rendering.
        double x, y;
        // Interpolation parameter t ∈ [0, 1] for the current move.
        double t;
        // Speed factor (higher = slower animation, more frames per move).
        final int speed;
        // Whether this slider is mid-move.
        boolean moving = false;
        // The virtual thread driving this slider.
        Thread thread;
        // Display colour.
        final Color color;

        Slider(int startX, int startY, int speed) {
            this.x = this.sx = startX;
            this.y = this.sy = startY;
            this.t = 0;
            this.speed = speed;

            if (COLOURS) {
                // RandomGenerator (Java 17+): the modern replacement for Random.
                RandomGenerator rng = ThreadLocalRandom.current();
                float hue = rng.nextFloat();
                float saturation = rng.nextFloat() * 0.1f + 0.9f;
                float brightness = rng.nextFloat() * 0.4f + 0.6f;
                this.color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
            } else {
                this.color = Color.BLACK;
            }

            // Each slider gets its own virtual thread (Java 21). Virtual threads
            // are ideal here: each slider spends most of its time sleeping or
            // blocked on a semaphore, which is exactly what virtual threads
            // handle efficiently (they unmount from the carrier thread while
            // blocked, freeing it for other work).
            this.thread = Thread.ofVirtual()
                    .name("slider-" + startX + "," + startY)
                    .unstarted(() -> {
                        try {
                            RandomGenerator rng = ThreadLocalRandom.current();
                            while (running) {
                                long frameStart = System.nanoTime();
                                if (moving) {
                                    if (t <= 1.0) {
                                        // Cosine interpolation for smooth acceleration/deceleration.
                                        // At t=0, tt=0; at t=0.5, tt=0.5; at t=1, tt=1.
                                        // The curve accelerates from rest, then decelerates to rest.
                                        double tt = (1 - Math.cos(t * Math.PI)) / 2;
                                        this.x = (1 - tt) * sx + tt * tx;
                                        this.y = (1 - tt) * sy + tt * ty;
                                        t += 1.0 / speed;
                                    } else {
                                        // Move complete: release the start tile and snap to target.
                                        permitToEnter[sx][sy].release();
                                        moving = false;
                                        this.x = sx = tx;
                                        this.y = sy = ty;
                                    }
                                } else {
                                    // Choose a random direction.
                                    int d = rng.nextInt(4);
                                    tx = sx + DIRS[d][0];
                                    ty = sy + DIRS[d][1];
                                    if (0 <= tx && tx < width && 0 <= ty && ty < height) {
                                        if (!DEADLOCKING) {
                                            // Fail-fast: tryAcquire with timeout prevents deadlock.
                                            // If two sliders try to swap tiles simultaneously,
                                            // one will time out and try a different direction.
                                            if (permitToEnter[tx][ty].tryAcquire(
                                                    1000, TimeUnit.MILLISECONDS)) {
                                                moving = true;
                                                t = 0;
                                            }
                                        } else {
                                            // Blocking acquire: WILL deadlock when two adjacent
                                            // sliders each hold their own tile and block waiting
                                            // for the other's. Classic dining philosophers.
                                            permitToEnter[tx][ty].acquire();
                                            moving = true;
                                            t = 0;
                                        }
                                    }
                                }
                                // Aim for ~50 fps (20 ms per frame), subtracting computation time.
                                long elapsed = (System.nanoTime() - frameStart) / 1_000_000;
                                Thread.sleep(Math.max(1, 20 - elapsed));
                            }
                        } catch (InterruptedException ignored) {
                            // Normal shutdown: the terminate() method interrupts us.
                        }
                    });
        }

        void start() { thread.start(); }
    }

    // -----------------------------------------------------------------------
    // CONSTRUCTOR
    // -----------------------------------------------------------------------

    /**
     * @param width  Board width in tiles.
     * @param height Board height in tiles.
     * @param n      Number of sliders.
     */
    public Sliders(int width, int height, int n) {
        this.width = width;
        this.height = height;
        this.setPreferredSize(new Dimension(TILESIZE * width, TILESIZE * height));
        this.setBackground(Color.WHITE);

        // One semaphore per tile, each with one permit.
        this.permitToEnter = new Semaphore[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                permitToEnter[x][y] = new Semaphore(1);
            }
        }

        // Place sliders randomly on unoccupied tiles.
        this.sliders = new Slider[n];
        boolean[][] occupied = new boolean[width][height];
        RandomGenerator rng = ThreadLocalRandom.current();
        for (int i = 0; i < n; i++) {
            int x, y;
            do {
                x = rng.nextInt(width);
                y = rng.nextInt(height);
            } while (occupied[x][y]);
            occupied[x][y] = true;
            sliders[i] = new Slider(x, y, rng.nextInt(50) + 10);
            // Acquire the permit for the starting tile (slider is "on" it).
            try { permitToEnter[x][y].acquire(); }
            catch (InterruptedException ignored) { }
        }

        // Start all slider threads.
        for (Slider slider : sliders) { slider.start(); }

        // Swing Timer (fires on the EDT) for repainting at ~50 fps.
        // Unlike the original raw Thread + repaint() approach, this is
        // EDT-safe: paintComponent is always called on the Event Dispatch
        // Thread, which is what Swing requires.
        var repaintTimer = new Timer(20, e -> repaint());
        repaintTimer.start();
    }

    // -----------------------------------------------------------------------
    // RENDERING
    // -----------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2 = (Graphics2D) g;
        // Anti-aliasing for smoother edges.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.5f));

        for (Slider slider : sliders) {
            // RoundRectangle2D for slightly softer tiles.
            double px = TILESIZE * slider.x;
            double py = TILESIZE * slider.y;
            var shape = new RoundRectangle2D.Double(
                    px + 2, py + 2, TILESIZE - 4, TILESIZE - 4, 8, 8);
            g2.setPaint(slider.color);
            g2.fill(shape);
            g2.setPaint(Color.BLACK);
            g2.draw(shape);
        }
    }

    // -----------------------------------------------------------------------
    // SHUTDOWN
    // -----------------------------------------------------------------------

    /** Stop all slider threads and the repaint timer. */
    public void terminate() {
        running = false;
        for (Slider slider : sliders) {
            slider.thread.interrupt();
        }
    }

    // -----------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        // SwingUtilities.invokeLater ensures the GUI is built on the EDT.
        // This has always been the correct way to launch Swing applications,
        // but older examples (including this one's predecessor) often skipped it.
        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("Sliders — Semaphore Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            var sliderPanel = new Sliders(10, 10, 30);

            // Clean up threads when the window closes.
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    sliderPanel.terminate();
                }
            });

            frame.setLayout(new FlowLayout());
            frame.add(sliderPanel);
            frame.pack();
            frame.setLocationRelativeTo(null); // centre on screen
            frame.setVisible(true);
        });
    }
}