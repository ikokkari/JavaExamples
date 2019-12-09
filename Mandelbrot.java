import java.awt.image.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.math.*;
import java.util.*;
import java.util.concurrent.*;

public class Mandelbrot extends JPanel {

    // The size of this component in pixels.
    private int sizeP;
    // The complex number at the top left corner of image.
    private BigComplex top;
    // The size of our current "peephole" to the complex plane.
    private BigDecimal size;
    // The size of a single pixel in this peephole.
    private BigDecimal psize;
    // Running tally of pixels that have been created so far.
    private static int pixelCount = 0;
    // The buffered image to draw the fractal on.
    private BufferedImage img;
    // The smoothed image displayed to the user.
    private BufferedImage display;
    // The current selection box top (sx, sy) and bottom (bx, by).
    private int sx = -1, sy = -1, bx, by;
    // Are we currently rendering?
    private volatile boolean busy = false;
    // Does the user want to stop the rendering?
    private volatile boolean stopRequested = false;
    // Timer to force regular repaints of the Swing component.
    private javax.swing.Timer timer;
    // The labels used to display the current scale and completed pixel count.
    private JLabel scale, completed;
    // How many pixels still remain to compute.
    private int remainCount;
    // A global mutex for synchronization of remainCount and image pixels.
    private Semaphore globalMutex = new Semaphore(1);
    // Size of the margin above image where the components reside.
    private static final int OFFSET = 35;
    // Threshold radius for escape for the Mandelbrow iteration.
    private static final BigDecimal THRES = new BigDecimal("4");
    // Whether the current frontier pixels are rendered as white.
    private static final boolean RENDER_FRONTIER_WHITE = false;
    // The number of threads to compute the image.
    private static final int THREADS = 7;
    // Multiplier for iteration steps performed for each pixel at each turn.
    private static final int IROUNDS = 200;
    // Pixel skip when initializing the start pixels of the image.
    private static final int EDGE_SKIP = 20;
    // The ExecutorService to manage the threads behind the scenes.
    private static ExecutorService es = Executors.newFixedThreadPool(THREADS + 1);
    // How often the screen image should be updated, delay in ms.
    private static int TIMER_FREQ = 40;
    // The colours used to render completed pixels. Somebody could surely think up some
    // other, more artistic colour schemes.
    private static final int COLS = 1024;
    private static int[] colours = new int[COLS];
    private static int getEscapeColour(int c) {
        // enforce symmetry to avoid ugly discontinuities
        if((c / COLS) % 2 == 0) { c = c % COLS; } else { c = COLS - c % COLS - 1; }
        // look up from cache if needed
        if(colours[c] == 0) {
            double cc = Math.pow(c, 1);            
            float hue = (float)(.5 + .3 * Math.sin(-.08*cc) + .2 * Math.sin(.11*cc + .2));            
            float saturation = (float)(.7 + .2 * Math.sin(.05*cc+Math.cos(cc/100)));
            float brightness = (float)(.6 + .3 * Math.sin(-.11*cc) + .1*Math.sin(.07*cc + .1));
            colours[c] = Color.HSBtoRGB(hue, saturation, brightness) & 0x00ffffff;            
        }
        return colours[c];
    }

    // An interface to something that can produce Pixel objects at command.
    private interface PixelFactory {
        public Pixel create(int x, int y);
    }

    // A class to reperesent the state of computation of an individual pixel.
    private class Pixel {
        public int x, y; // Image coordinates of this pixel.
        public int iter; // How many iterations this pixel has gone through.
        private int age; // The creation time of this object.
        public BigComplex c; // The original complex number that this pixel represents.
        public BigComplex z; // The current value of the iteration of this pixel.

        public Pixel(int x, int y) {
            this.x = x; this.y = y;
            this.age = pixelCount++;
            // The complex number that this pixel represents.
            BigDecimal cx = top.getRe().add(psize.multiply(new BigDecimal(x)));
            BigDecimal cy = top.getIm().subtract(psize.multiply(new BigDecimal(y)));
            this.z = this.c = new BigComplex(cx, cy);
        }

        // Iterate this pixel some number of rounds. Returns -iter if the pixel has not escaped,
        // otherwise returns the RGB colour that the pixel should be rendered with.
        public int iterate(int rounds) {
            BigComplex zp = z;
            for(int r = 0; r < rounds; r++) {
                zp = zp.mul(zp).add(c); // z = z * z + c, original Mandelbrot formula
                // zp = zp.mul(zp).mul(zp).add(c); // try also cubic Mandelbrot and other powers,
                // as in the Wikipedia page https://en.wikipedia.org/wiki/Multibrot_set
                iter++;
                if(zp.getRe().abs().compareTo(THRES) > 0 || zp.getIm().abs().compareTo(THRES) > 0) {
                    int col = getColour(iter, zp, z, c);
                    z = c = null;
                    return col;
                }
            }
            z = zp;
            return -iter;
        }

        // A template method to compute the pixel colour based on number of iterations, the
        // escape point zp, the previous point z, and the starting point c.
        public int getColour(int iter, BigComplex z1, BigComplex z2, BigComplex c) {
            return getEscapeColour(iter);
        }
    }

    private static final BigComplex TRAP = new BigComplex(-1,1);
    
    // An example subclass for a more interesting formula. Also keeps track of closest
    // distance to orbit trap during the iteration.
    private class AlternatingSignFormula extends Pixel {
        public AlternatingSignFormula(int x, int y) { super(x, y); }
        private double dmin = Math.PI;
       
        public final int iterate(int rounds) {
            BigComplex zp = z;
            for(int r = 0; r < rounds; r++) {
                // A more interesting formula adds and subtracts in alternating steps
                if(iter % 2 == 0) { 
                    zp = zp.mul(zp).add(c);
                }
                else {
                    zp = zp.mul(zp).sub(c);
                }
                iter++;
                BigComplex trapv = zp.sub(TRAP);
                double tx = trapv.getRe().doubleValue();
                double ty = trapv.getIm().doubleValue();
                double td = tx*tx + ty*ty;
                if(td < dmin) { dmin = td; }
                if(zp.getRe().abs().compareTo(THRES) > 0 || zp.getIm().abs().compareTo(THRES) > 0) {
                    int col = getColour(iter, zp, z, c);
                    z = c = null;
                    return col;
                }
            }
            z = zp;
            return -1;
        }    

        @Override public int getColour(int iter, BigComplex z1, BigComplex z2, BigComplex c) {
            double x = (Math.sin(0.05 * Math.pow(dmin, .5)) + 1) / 2;
            return getEscapeColour((int)(COLS * x));
        }
        
    }

    // An example subclass for a more interesting colouring effect.
    private class EscapeSinePixel extends Pixel {
        public EscapeSinePixel(int x, int y) { super(x, y); }

        @Override public int getColour(int iter, BigComplex z1, BigComplex z2, BigComplex c) {
            BigComplex zd = z1.sub(z2);
            double dr = zd.getRe().doubleValue();
            double di = zd.getIm().doubleValue();
            return getEscapeColour(iter + 10 + (int)(5*Math.sin(dr) + 5*Math.cos(di)));
        }
    }

    // Strategy classes to decide which pixel from the priority queue should be iterated next.

    private class DFSComparator implements Comparator<Pixel> {
        @Override public int compare(Pixel p1, Pixel p2) {
            int result = p1.iter - p2.iter;
            if(result == 0) { return p2.age - p1.age; } // DFS ordering of equals
            else { return result; }
        }
    }

    private class BFSComparator implements Comparator<Pixel> {
        @Override public int compare(Pixel p1, Pixel p2) {
            int result = p1.iter - p2.iter;
            if(result == 0) { return p1.age - p2.age; } // BFS ordering of equals
            else { return result; }
        }
    }

    private class CenterDistanceComparator implements Comparator<Pixel> {
        @Override public int compare(Pixel p1, Pixel p2) {
            int result = p1.iter - p2.iter;
            if(result == 0) { // resolve equals by their chessboard distance to image center
                int d1 = Math.max(Math.abs(sizeP / 2 - p1.x), Math.abs(sizeP / 2 - p1.y));
                int d2 = Math.max(Math.abs(sizeP / 2 - p2.x), Math.abs(sizeP / 2 - p2.y));
                return d1 < d2 ? -1 : +1;
            } 
            else { return result; }
        }
    }

    // The neighbour direction offsets.
    private static final int[][] dirs = {
            //{0, 1}, {1, 0}, {-1, -1}
            {0, 1}, {0, -1}, {1, 0}, {-1, 0} // main axes
            //{0, 1}, {0, -1}, {1, 0}, {-1, 0}, 1, 1}, {1, -1}, {-1, 1}, {-1, -1} // compass eight
        };

    // Compute the image using current settings.
    public void computeImage(final PixelFactory pf) throws InterruptedException {
        busy = true;
        int sc = bigScale(size);
        scale.setText("Scale: " + sc);
        // decrease +7 to something less for blocky quantization artifacts
        BigComplex.mc = new MathContext(sc + 2); 
        size = new BigDecimal(size.toString(), BigComplex.mc); // convert to higher precision
        top = new BigComplex( // convert to new math context
            new BigDecimal(top.getRe().toString(), BigComplex.mc),
            new BigDecimal(top.getIm().toString(), BigComplex.mc)
        );
        Graphics2D g2 = (Graphics2D)img.getGraphics();
        g2.setPaint(Color.BLACK);
        g2.fill(new java.awt.geom.Rectangle2D.Double(0, 0, sizeP, sizeP));
        g2 = (Graphics2D)display.getGraphics();
        g2.setPaint(Color.BLACK);
        g2.fill(new java.awt.geom.Rectangle2D.Double(0, 0, sizeP, sizeP));
        
        // Keep track of which pixels have already been added to the frontier,
        // so that we won't add them twice as new pixels escape.
        final boolean[][] found = new boolean[sizeP][sizeP];

        // Count of how many pixels remain to be completed.
        remainCount = sizeP * sizeP;

        // Create the initial frontiers of background threads.
        final PriorityBlockingQueue<Pixel> frontier = new PriorityBlockingQueue<>(100,
                //new BFSComparator() // BFS
                //new DFSComparator() // DFS
                new CenterDistanceComparator() // greedy from center out
            );

        // Complex coordinates of top left corner.
        final BigDecimal topX = top.getRe();
        final BigDecimal topY = top.getIm();
        // Size of a single pixel in complex coordinates.
        psize = size.multiply(new BigDecimal(1.0 / sizeP, BigComplex.mc));

        // Initialize the search frontiers for each edge.
        for(int y = 0; y < sizeP; y += EDGE_SKIP) {
            found[0][y] = true;
            Pixel p = pf.create(0, y);
            frontier.offer(p);

            found[sizeP - 1][y] = true;
            p = pf.create(sizeP - 1, y);
            frontier.offer(p);
        }

        for(int x = 0; x < sizeP; x += EDGE_SKIP) {
            found[x][0] = true;
            Pixel p = pf.create(x, 0);
            frontier.offer(p);

            found[x][sizeP - 1] = true;
            p = pf.create(x, sizeP - 1);
            frontier.offer(p);
        }

        // A local Runnable subclass to render the pixels in the given PriorityQueue.
        class Renderer implements Runnable {
            // The semaphore that this thread signals when it is finished.
            private Semaphore signalWhenDone;
            // The index of this background thread.
            private int idx;
            public Renderer(int idx, Semaphore signalWhenDone) {
                this.idx = idx;
                this.signalWhenDone = signalWhenDone;
            }

            // Neighbour pixel offsets for antialiasing calculations.
            private int[][] off = { {0,0}, {0,1}, {-1,-1}, {1,-1} };
            
            private void updatePixel(int x, int y) {
                if(x < 0 || x >= sizeP || y < 0 || y >= sizeP) { return; }
                int newC = 0;
                for(int c = 0; c < 3; c++) {
                    int total = 0, count = 0;
                    for(int[] d: off) {
                        int nx = x + d[0], ny = y + d[1];
                        if(nx < 0 || nx >= sizeP || ny < 0 || ny >= sizeP) { continue; }
                        int comp = img.getRGB(nx, ny);
                        if(comp != 0) {
                            count++;
                            total += (comp >> (8 * c)) & 0xFF;
                        }
                    }
                    newC |= (total / count) << (8 * c);                    
                }
                display.setRGB(x, y, newC);
            }
            
            public void run() {
                // In an infinite loop, repeatedly pop the next pixel from the queue
                // of remaining pixels and iterate that pixel one more round. If it escapes,
                // colour the pixel, otherwise push that pixel back to the queue. We must
                // take care the synchronize the access to the priority queues of threads
                // because PriorityQueue<T> itself, as most collections, is not thread safe.
                try {
                    while(!stopRequested && remainCount > 0) {
                        Pixel p = frontier.take(); // The pixel to process next.
                        if(p.x == -1) { break; }
                        int c = p.iterate(sc * IROUNDS);
                        if(c > -1) { // The pixel (p.x, p.y) has escaped!
                            globalMutex.acquire();
                            remainCount--; 
                            img.setRGB(p.x, p.y, c);
                            for(int[] d: off) {
                                updatePixel(p.x - d[0], p.y - d[1]);
                            }
                            // Add all undiscovered neighbours to the search frontier.
                            for(int[] d: dirs) {
                                int nx = p.x + d[0];
                                int ny = p.y + d[1];
                                if(nx >= 0 && nx < sizeP && ny >= 0 && ny < sizeP) {
                                    if(!found[nx][ny]) {
                                        found[nx][ny] = true;
                                        frontier.offer(pf.create(nx, ny));
                                        if(RENDER_FRONTIER_WHITE) {
                                            img.setRGB(nx, ny, 0xFFFFFF);
                                        }
                                    }
                                }
                            }
                            globalMutex.release();
                        }
                        else { // The pixel p did not yet escape.
                            frontier.offer(p); // Put the pixel back to the queue.
                        }
                    }
                } catch(Exception e) {
                    System.out.println("Renderer " + idx + " crashed: " + e);
                }
                finally {
                    // Make sure that another thread waiting in the queue will get out.
                    frontier.offer(new Pixel(-1, -1));
                    signalWhenDone.release();
                }
            }
        }

        // Launch the renderer subtasks and wait for their termination.
        stopRequested = false;
        final Semaphore allFinished = new Semaphore(1 - THREADS);
        for(int i = 0; i < THREADS; i++) {
            es.submit(new Renderer(i, allFinished));
        }
        allFinished.acquire(); // Wait for all subtasks to complete
        busy = false;
        completed.setText("Completed");
    }

    // Create a new thread to compute the image.
    private void submitRender(PixelFactory pf) {
        es.submit(() -> {
                try { computeImage(pf); }
                catch(Exception e) {
                    System.err.println("Error: " + e);
                }
            });      
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
        PixelFactory pf = (x, y) -> new EscapeSinePixel(x, y);
        this.setPreferredSize(new Dimension(sizeP, sizeP + OFFSET));
        this.img = new BufferedImage(sizeP, sizeP, BufferedImage.TYPE_INT_RGB);
        this.display= new BufferedImage(sizeP, sizeP, BufferedImage.TYPE_INT_RGB);
        this.top = top;
        this.sizeP = sizeP;
        this.size = size;
        JButton stopB = new JButton("Stop");
        this.add(stopB);
        stopB.addActionListener((ae) -> {
                stopRequested = true;
                completed.setText("Stopping...");
            });
        this.scale = new JLabel("" + bigScale(size));
        this.add(scale);
        this.add(new JLabel("Black pixels:"));
        this.completed = new JLabel("");
        this.add(completed);
        // A background time to repaint the component.
        timer = new javax.swing.Timer(TIMER_FREQ, (ae) -> {
                if(busy) { completed.setText("" + remainCount); }
                repaint();
            });
        timer.start();
        submitRender(pf); // the initial image

        // When the mouse is dragged, use the new coordinates as (bx, by).
        this.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent me) {
                    if(busy) { return; }
                    bx = me.getX();
                    by = me.getY() - OFFSET;
                    if(by < 0) { return; }
                    // Flip the start and end of selection if necessary
                    if(bx < sx) { bx = sx; }
                    if(by < sy) { by = sy; }
                    // Enforce the selection being a square
                    if(bx - sx < by - sy) { bx = sx + (by - sy); }
                    if(by - sy < bx - sx) { by = sy + (bx - sx); }
                }
            });

        // Detect the start and end of drag using mouse button listener.
        this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent me) {
                    if(busy) { return; }
                    if(me.getY() < OFFSET) { return; }
                    bx = sx = me.getX();
                    by = sy = me.getY() - OFFSET;
                }

                public void mouseReleased(MouseEvent me) {
                    if(sx < 0 || (bx - sx) < 4) { sx = -1; return; }
                    if(busy) return;
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
                    submitRender(pf);
                }
            });
    }

    // To paint this component, just draw the image that we are rendering, 
    // followed by the mouse drag rectangle, if it exists.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(display, 0, OFFSET, this);
        if(!busy && sx > -1) {
            g.setColor(Color.WHITE);
            g.drawRect(sx, sy + OFFSET, (bx - sx), (by - sy));
        }
    }

    // Call this method to terminate the computing.
    public void terminate() {
        timer.stop(); // terminate the animation timer
        stopRequested = true; // terminate the background rendering threads also...
    }

    // For demonstration purposes.
    public static void main(String[] args) {
        final JFrame f = new JFrame("Mandelbrot");
        final Mandelbrot m = new Mandelbrot(1000);
        f.setLayout(new FlowLayout());
        f.add(m);
        f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    m.terminate();
                    f.dispose();
                    es.shutdownNow();
                }
            });
        f.pack();
        f.setVisible(true);
    }
}