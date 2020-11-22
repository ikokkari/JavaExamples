import java.awt.image.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Mandelbrot extends JPanel {
    // The size of this component in pixels.
    private int sizeP;
    // The complex number at the top left corner of image.
    private BigComplex top;
    // The size of our current "peephole" to the complex plane.
    private BigDecimal size;
    // The size of a single pixel in this peephole.
    private BigDecimal psize;
    // The currently rendered image that is displayed to the user.
    private BufferedImage display;
    // The current selection box top (sx, sy) and bottom (bx, by).
    private int sx = -1, sy = -1, bx, by;
    // Timer to force regular repaints of the Swing component.
    private javax.swing.Timer timer;
    // The frontier of the current active rendering.
    private volatile PriorityBlockingQueue<Pixel> activeFrontier = null;
    // The singleton poison pixel that orders its recipient to stop working.
    private final Pixel POISON = new Pixel(-1, -1);
    // A global mutex for synchronization for image pixels.
    private Semaphore pixelMutex = new Semaphore(1);
    // The timestamp generator for task ID's.
    private static final AtomicInteger RENDERSTAMP = new AtomicInteger(0);
    // The timestamp generator for pixel ages.
    private static final AtomicInteger AGESTAMP = new AtomicInteger(0);
    // Threshold radius for escape for the Mandelbrow iteration.
    private static final BigDecimal THRESHOLD = new BigDecimal("4.0");
    // The number of threads to compute the image.
    private static final int THREADS = 4;
    // Multiplier for iteration steps performed for each pixel at each turn.
    private static final int IROUNDS = 200;
    // Pixel skip when initializing the start pixels of the image.
    private static final int EDGE_SKIP = 20;
    // Change this value for interesting DFS effects. Must be a positive integer.
    private static final int BUNCH = 100;
    // The ExecutorService to manage the threads behind the scenes.
    private static ExecutorService es = Executors.newFixedThreadPool(THREADS);
    // How often the screen image should be updated, delay in ms.
    private static int TIMER_FREQ = 20;    
    // The colours used to render completed pixels.
    private static final int COLS = 1024;
    private static int[] colours = new int[COLS];
    // The neighbour directions from the current pixel.
    private static final int[][] DIRS = { {0, 1}, {1, 0}, {0, -1}, {-1, 0} };

    // Utility method to compute the pixel colour based on its escape count.
    private static int getEscapeColour(int c) {
        // Enforce symmetry to avoid ugly discontinuities.
        c = (c / COLS) % 2 == 0 ? c % COLS: COLS - c % COLS - 1;
        // Look up the colour from cache if it is already computed.
        if(colours[c] == 0) {
            
            double cc = Math.pow(c, .8);            
            float hue = (float)(cc/50 - Math.floor(cc/50));            
            float saturation = (float)(.6 + .4 * Math.sin(.53*cc + Math.cos(cc * 0.16)));
            float brightness = (float)(.6 + .3 * Math.sin(-.47*cc) + .1*Math.sin(.27*cc - .1));
            colours[c] = Color.HSBtoRGB(hue, saturation, brightness) & 0x00ffffff;            
        }
        return colours[c];
    }

    // A class to reperesent the state of computation of an individual pixel.
    private class Pixel {
        public int x, y; // Image coordinates of this pixel.
        public int iter; // How many iterations this pixel has gone through.
        private int age; // The creation time of this object.
        public BigComplex c; // The original complex number that this pixel represents.
        public BigComplex z; // The current value of the iteration of this pixel.

        public Pixel(int x, int y) {
            this.x = x;
            this.y = y;
            this.age = AGESTAMP.incrementAndGet();
            // Compute the complex number that this pixel represents.
            if(x > -1) { // None for POISON, though.
                BigDecimal cx = top.getRe().add(psize.multiply(new BigDecimal(x)));
                BigDecimal cy = top.getIm().subtract(psize.multiply(new BigDecimal(y)));
                this.z = this.c = new BigComplex(cx, cy);
            }
        }

        // Iterate this pixel some number of rounds. Returns -iter if pixel did not escape,
        // otherwise returns the escape count as positive number.
        public int iterate(int rounds) {
            BigComplex zp = z;
            for(int r = 0; r < rounds; r++) {
                // z = z * z + c, original Mandelbrot formula.
                zp = zp.multiply(zp).add(c); 
                
                // You can also try cubic Mandelbrot and other powers, as seen in the
                // Wikipedia page https://en.wikipedia.org/wiki/Multibrot_set
                //zp = (zp.multiply(zp).multiply(zp).subtract(zp)).add(c);
                
                iter++;
                BigDecimal real = zp.getRe();
                BigDecimal imag = zp.getIm();
                BigDecimal d = real.multiply(real).add(imag.multiply(imag));
                if(d.compareTo(THRESHOLD) > 0) {
                    z = c = null; // Allow garbage collection release those.
                    return iter; // Iteration of this pixel completed.
                }
            }
            z = zp;
            return -iter; // Iteration of this pixel is not yet done.
        }
    }

    // Strategy classes to decide which pixel from the priority queue should be iterated
    // next. Nice little example of the template method design pattern where subclasses
    // implement some step of an algorithm in a different way.

    private abstract class PixelComparator implements Comparator<Pixel> {
        @Override final public int compare(Pixel p1, Pixel p2) {
            // Poison pixel is always the first in line.
            if(p1 == POISON) { return -1; }
            if(p2 == POISON) { return +1; }
            // The pixel that has been iterated the least will be next in line.
            if(p1.iter < p2.iter) { return -1; }
            if(p1.iter > p2.iter) { return +1; }
            // Otherwise defer the job to the template method step.
            int result = comparePixels(p1, p2);
            // If the comparePixels cannot decide, we decide based distance from center.
            if(result == 0) {
                int d1 = Math.max(Math.abs(sizeP / 2 - p1.x), Math.abs(sizeP / 2 - p1.y));
                int d2 = Math.max(Math.abs(sizeP / 2 - p2.x), Math.abs(sizeP / 2 - p2.y));
                return d1 < d2 ? -1 : +1;
            }
            return result;
        }
        // Template method pattern: subclasses must implent this comparison method.
        protected abstract int comparePixels(Pixel p1, Pixel p2);
    }

    private class DFSComparator extends PixelComparator {
        @Override public int comparePixels(Pixel p1, Pixel p2) {
            return p2.age - p1.age; // DFS ordering of pixel ages
        }
    }

    private class BFSComparator extends PixelComparator {
        @Override public int comparePixels(Pixel p1, Pixel p2) {
            return p1.age - p2.age; // BFS ordering of pixel ages
        }
    }

    // A Callable task to perform rendering of pixels into the current image.

    private class Renderer implements Callable<Integer> {
        // The blocking queue to take out pixels to be processed.
        private PriorityBlockingQueue<Pixel> localFrontier;
        // The display into which to render the escaped pixels.
        private BufferedImage localDisplay;
        // The boolean array that keeps track of which pixels are active.
        private boolean[][] localFound;
        // The index of this rendering task.
        private int idx;
        // The count of how many pixels were processed.
        private int pixelCount = 0;

        public Renderer(PriorityBlockingQueue<Pixel> frontier, BufferedImage display,
        boolean[][] localFound) {
            this.idx = RENDERSTAMP.incrementAndGet();
            this.localFrontier = frontier;
            this.localDisplay = display;
            this.localFound = localFound;
        }

        public Integer call() {
            // In an infinite loop, repeatedly pop the next pixel from the queue
            // of remaining pixels and iterate that pixel one more round. If it escapes,
            // colour the pixel, otherwise push that pixel back to the queue. We must
            // take care the synchronize the access to the priority queues of threads
            // because PriorityQueue<T> itself, as most collections, is not thread safe.
            try {
                while(localFrontier.size() > 0) {
                    Pixel p = localFrontier.take(); // The pixel to process next.
                    if(p == POISON) {
                        localFrontier.offer(POISON); // Put the poison back for the next guy.
                        break; // And this task is done.
                    }
                    int c = p.iterate(IROUNDS);
                    if(c > -1) { // The pixel has escaped!
                        pixelCount++;
                        pixelMutex.acquire();
                        localDisplay.setRGB(p.x, p.y, getEscapeColour(c));
                        // Add the undiscovered neighbours to the search frontier.
                        for(int i = 0; i < 4; i++) {
                            int[] d = DIRS[(i + p.age / BUNCH) % 4];
                            int nx = p.x + d[0];
                            int ny = p.y + d[1];
                            if(nx >= 0 && nx < sizeP && ny >= 0 && ny < sizeP) {
                                if(!localFound[nx][ny]) {
                                    localFound[nx][ny] = true;
                                    localFrontier.offer(new Pixel(nx, ny));
                                }
                            }
                        }
                        pixelMutex.release();
                    }
                    else { // The pixel p did not yet escape, so put it back.
                        localFrontier.offer(p);
                    }
                }
            } catch(Exception e) {
                // Report the crash if it is some other than being interrupted.
                if(!(e instanceof InterruptedException)) {
                    System.out.println("Task " + idx + " crashed: " + e);
                    System.out.println("Printing the stack trace: ");
                    StackTraceElement[] trace = e.getStackTrace();
                    for(int i = 0; i < trace.length; i++) {
                        System.out.print(trace[i].getClassName() + " ");
                        System.out.print(trace[i].getMethodName() + " ");
                        System.out.println(trace[i].getLineNumber() + " ");
                    }
                }
            }
            return pixelCount;
        }
    }

    // A subclass to represent the task of rendering pixels from the given PriorityQueue.

    // Compute the image using current settings.
    public void computeImage(Comparator<Pixel> frontierComp) {
        int sc = bigScale(size);
        BigComplex.mc = new MathContext(sc + 5); 
        size = new BigDecimal(size.toString(), BigComplex.mc); // convert to higher precision
        top = new BigComplex( // convert to new math context
            new BigDecimal(top.getRe().toString(), BigComplex.mc),
            new BigDecimal(top.getIm().toString(), BigComplex.mc)
        );

        // Keep track of which pixels have already been added to the frontier, so
        // that we won't add them twice to the queue as their neighbours escape.
        final boolean[][] localFound = new boolean[sizeP][sizeP];

        // Create a new localDisplay that then becomes the display for this component.
        BufferedImage localDisplay = new BufferedImage(sizeP, sizeP, BufferedImage.TYPE_INT_RGB);
        this.display = localDisplay;

        // Tell the tasks for the previous frontier that they can now stop.
        if(activeFrontier != null) {
            activeFrontier.offer(POISON);
        }

        // Create a new frontier of pixels currently being processed. 
        PriorityBlockingQueue<Pixel> frontier = new PriorityBlockingQueue<>(100, frontierComp);
        activeFrontier = frontier;

        // Complex coordinates of top left corner.
        final BigDecimal topX = top.getRe();
        final BigDecimal topY = top.getIm();
        // Size of a single pixel in complex coordinates.
        psize = size.multiply(new BigDecimal(1.0 / sizeP, BigComplex.mc));

        // Initialize the search frontiers for each edge.
        for(int y = 0; y < sizeP; y += EDGE_SKIP) {
            localFound[0][y] = true;
            frontier.offer(new Pixel(0, y));
            localFound[sizeP - 1][y] = true;
            frontier.offer(new Pixel(sizeP - 1, y));
        }

        for(int x = 0; x < sizeP; x += EDGE_SKIP) {
            localFound[x][0] = true;
            frontier.offer(new Pixel(x, 0));
            localFound[x][sizeP - 1] = true;
            frontier.offer(new Pixel(x, sizeP - 1));
        }      

        // Launch the renderer tasks.
        for(int i = 0; i < THREADS; i++) {
            es.submit(new Renderer(frontier, localDisplay, localFound));
        }
    }

    private static final BigDecimal TWO = new BigDecimal(2);
    // Estimate the integer logarithm (base 2) of decimal number x.
    private static int bigScale(BigDecimal x) {
        int count = 1;
        while(x.compareTo(BigDecimal.ONE) < 0) {
            count++;
            x = x.multiply(TWO);
        }
        return count;
    }

    // The constructor with a good default starting value for complex coordinates.
    public Mandelbrot(final int sizeP) {
        this(sizeP, new BigComplex(-2.0, 1.2), new BigDecimal(2.5, BigComplex.mc));
    }

    // The constructor to set up the component controls.
    public Mandelbrot(final int sizeP, BigComplex top, BigDecimal size) {
        this.setPreferredSize(new Dimension(sizeP, sizeP));
        this.display = new BufferedImage(sizeP, sizeP, BufferedImage.TYPE_INT_RGB);
        this.top = top;
        this.sizeP = sizeP;
        this.size = size;

        // The search discipline in carving out the Mandelbrot turkey.
        PixelComparator pixelComp = new DFSComparator();

        // When the mouse is dragged, use the new coordinates as (bx, by).
        this.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent me) {
                    bx = me.getX();
                    by = me.getY();
                    if(by < 0) { return; }
                    // Flip the start and end of selection if necessary.
                    if(bx < sx) { bx = sx; }
                    if(by < sy) { by = sy; }
                    // Enforce the selection being a square.
                    if(bx - sx < by - sy) { bx = sx + (by - sy); }
                    if(by - sy < bx - sx) { by = sy + (bx - sx); }
                }
            });

        // Detect the start and end of drag using mouse button listener.
        this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent me) {
                    bx = sx = me.getX();
                    by = sy = me.getY();
                }

                public void mouseReleased(MouseEvent me) {
                    // Treat creating a small rectangle as giving up the zoom.
                    if(sx < 0 || (bx - sx) < 4) { 
                        sx = -1; return; 
                    }
                    // Calculate the area on complex plane determined by selection.
                    BigDecimal psize = Mandelbrot.this.size.multiply(
                            new BigDecimal(1.0 / Mandelbrot.this.sizeP, BigComplex.mc)
                        );
                    Mandelbrot.this.top = new BigComplex(
                        Mandelbrot.this.top.getRe().add(psize.multiply(new BigDecimal(sx))),
                        Mandelbrot.this.top.getIm().subtract(psize.multiply(new BigDecimal(sy)))
                    );
                    double sf = (bx - sx) / (double)Mandelbrot.this.sizeP;
                    Mandelbrot.this.size = Mandelbrot.this.size.multiply(new BigDecimal(sf));
                    sx = -1; // no more selection
                    computeImage(pixelComp);
                }
            });

        computeImage(pixelComp); // Launch rendering the initial image.
        this.timer = new javax.swing.Timer(TIMER_FREQ, (ae) -> { repaint(); });
        this.timer.start(); // Launch the animation refresh timer.
    }

    // To paint this component, just draw the image that we are rendering, 
    // followed by the mouse drag rectangle, if it exists.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(this. display, 0, 0, this);
        if(sx > -1) { // Render the zooming rectangle, if one has been created.
            g.setColor(Color.WHITE);
            g.drawRect(sx, sy, (bx - sx), (by - sy));
        }
    }

    // Call this method to terminate the computing.
    public void terminate() {
        timer.stop(); // Terminate the animation timer.
        es.shutdownNow(); // Terminate the renderer tasks. 
    }

    // For demonstration purposes.
    public static void main(String[] args) {
        final JFrame f = new JFrame("Mandelbrot Carver");
        final Mandelbrot mandel = new Mandelbrot(1000);
        f.setLayout(new FlowLayout());
        f.add(mandel);
        f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    mandel.terminate();
                    f.dispose();
                    es.shutdownNow();
                }
            });
        f.pack();
        f.setVisible(true);
    }
}