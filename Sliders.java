import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

// A demonstration of semaphores in multithreaded programming in Java. This
// component animates a bunch of sliders, each controlled by a separate thread,
// moving in a board of tiles. At no time should any two sliders collide by
// trying to slide into the same tile. It should be emphasized that in real
// games and animations, using a separate thread for each object is overkill,
// and one animation thread should move all objects, but this is intended to
// be a demonstration of semaphores, not an efficient animation. (That would
// also make these mutual exclusion problem outright trivial to handle.)

public class Sliders extends JPanel {
    
    // Choose between the solution that can or cannot deadlock.
    private static final boolean DEADLOCKING = false;
    // Whether the tiles should be drawn in bright colours or black ink.
    private static final boolean COLOURS = true;
    // Size of individual square tile in pixels on screen.
    private static final int TILESIZE = 60;
    // The RNG used by sliders to choose their random movements.
    //private static final Random rng = new Random();
    // Width and height of board, measured in tiles.
    private final int width;
    private final int height;
    // Semaphore guarding mutual exclusion to enter some tile. To be able
    // to move from tile (sx, sy) to tile (tx, ty), the slider must acquire
    // the permit to both tiles.
    private final Semaphore[][] permitToEnter;
    // The sliders that exist on the board.
    private final Slider[] sliders;
    // Whether the animation should still be running.
    private volatile boolean running = true;
    // The possible directions that an individual slider can move to.
    private static final int[][] DIRS = { {1, 0}, {0, 1}, {-1, 0}, {0, -1} };
    
    // The class that represents an individual slider.
    private class Slider {
        // The starting tile coordinates of the current move.
        public int sx, sy;
        // The ending tile coordinates of the current move.
        public int tx, ty;
        // The current pixel coordinates of the slider.
        public double x, y;
        // The interpolation time t in [0,1] for the current move.
        public double t;
        // The speed of this slider.
        public int speed;
        // Whether this slider is currently performing a move.
        public boolean moving = false;
        // The execution thread for this slider.
        public Thread thread;
        // The colour of this slider.
        public Color color;
        
        /**
         * @param sxx The starting tile x-coordinate of the slider.
         * @param syy The starting tile y-coordinate of the slider.
         */
        public Slider(int sxx, int syy, int speed) {
            // Initialize the data fields.
            this.x = this.sx = sxx; this.y = this.sy = syy;
            this.t = 0; this.speed = speed;                    
            if(COLOURS) {
                Random rng = ThreadLocalRandom.current();
                // Create a bright random colour, not some kind of bland pastel.
                float hue = (float)(rng.nextDouble());
                float saturation = (float)(rng.nextDouble() * 0.1 + 0.9);
                float brightness = (float)(rng.nextDouble() * 0.4 + 0.6);
                this.color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
            }
            else { this.color = Color.BLACK; }
            
            // Normally, having a separate thread for each animated object would be
            // overkill, and one thread would handle all objects. However, since this
            // is intended to be an example of thread handling and deadlock avoidance,
            // we now create and manage all these execution threads explicitly.
            this.thread = new Thread(() -> { // Runnable object as a lambda.
                try {
                    while(running) {
                        long startTime = System.currentTimeMillis();
                        if(moving) {                            
                            if(t <= 1.0) { // Movement is still incomplete.
                                // Cosine interpolation to smoothen the movement.
                                double tt = (1 - Math.cos(t * Math.PI))/2;
                                // The interpolated pixel coordinates.
                                this.x = (1-tt)*sx + tt*tx;
                                this.y = (1-tt)*sy + tt*ty;
                                // Advance the time for the next round.
                                t += 1.0 / speed;
                            }
                            else { // Movement has completed.
                                // Release the permit for start tile.
                                permitToEnter[sx][sy].release();
                                moving = false;
                                // The target tile becomes the start tile.
                                this.x = sx = tx; this.y = sy = ty;
                            }
                        }
                        else {
                            // The RNG for this thread.
                            Random rng = ThreadLocalRandom.current();
                            // Choose a random direction vector.
                            int d = rng.nextInt(4);
                            // Compute the target tile for that direction.
                            tx = sx + DIRS[d][0]; ty = sy + DIRS[d][1];
                            // If the tile is inside the board, it becomes target.
                            if(0 <= tx && tx < width && 0 <= ty && ty < height) {
                                // Fail-fast semaphore acquire to prevent deadlock.
                                if(!DEADLOCKING) {
                                    if(permitToEnter[tx][ty].tryAcquire(1000, TimeUnit.MILLISECONDS)) {
                                        moving = true; t = 0;
                                    }
                                }
                                // Blocking semaphore acquire will eventually cause a deadlock.
                                else {
                                    permitToEnter[tx][ty].acquire();
                                    moving = true; t = 0;
                                }
                            }
                        }
                        long endTime = System.currentTimeMillis();
                        Thread.sleep(Math.max(20, endTime - startTime));
                    }
                }
                catch(Exception ignored) { }
            });
            // We don't start this thread yet. Only after everybody is ready to go.
        }
        public void start() { thread.start(); }
    }
    
    /**
     * Constructor for this component.
     * @param width The width of the board, in tiles.
     * @param height The height of the board, in tiles.
     * @param n The number of sliders on the board.
     */
    public Sliders(int width, int height, int n) {
        this.width = width; this.height = height;
        this.setPreferredSize(new Dimension(TILESIZE * width, TILESIZE * height));
        this.setBackground(Color.WHITE);
        // Initialize the array of semaphores for each tile.
        this.permitToEnter = new Semaphore[width][height];
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                permitToEnter[x][y] = new Semaphore(1);
            }
        }
        
        // Create the individual sliders and place them randomly on board.
        this.sliders = new Slider[n];
        // Array to keep track of which tiles are already taken.
        boolean[][] occupied = new boolean[width][height];
        // The RNG for the current thread.
        Random rng = ThreadLocalRandom.current();
        for(int i = 0; i < n; i++) {
            int x, y;            
            do { // Find a tile that is still unoccupied.
                x = rng.nextInt(width); y = rng.nextInt(height);
            } while(occupied[x][y]);            
            occupied[x][y] = true;
            // Create a new slider to that tile.
            sliders[i] = new Slider(x, y, rng.nextInt(50) + 10);
            try { permitToEnter[x][y].acquire(); }
            catch(Exception ignored) { }
        }
        // Once all sliders have been placed on board, tell sliders to move.
        for(int i = 0; i < n; i++) { sliders[i].start(); }
        // Animation thread to repaint the component at 50 fps.
        new Thread(() -> {
            try {
                while (running) {
                    Thread.sleep(20);
                    repaint();
                }
            }
            catch(Exception ignored) { }
        }).start();
    }
    
    /**
     * Render this component as it currently looks like.
     * @param g The Graphics object to draw onto.
     */
    public void paintComponent(Graphics g) {
        // Erase the previous contents.
        super.paintComponent(g);
        // Convert to better Graphics2D.
        Graphics2D g2 = (Graphics2D)g;
        // Render the individual sliders.
        g2.setStroke(new BasicStroke(1.0f));
        for(Slider slider: sliders) {
            Rectangle2D.Double shape = new Rectangle2D.Double(
                TILESIZE * slider.x, TILESIZE * slider.y, TILESIZE, TILESIZE
            );
            // Fill the inside colour of that slider.
            g2.setPaint(slider.color); g2.fill(shape);
            // Draw the black outline of that slider.
            g2.setPaint(Color.BLACK); g2.draw(shape);
        }
    }
    
    // Terminates the animation and the slider threads.
    public void terminate() {
        // Inform all sliders and the animation thread that they should wrap it up.
        running = false;
        // Interrupt the slider threads to get them out of semaphore waits.
        for(Slider slider: sliders) {
            slider.thread.interrupt();
        }
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame("Sliders demo");
        final Sliders sliderPanel = new Sliders(10, 10, 30);
        // Make sure that animation terminates when user closes the window.
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {                
                sliderPanel.terminate();
            }
        });
        f.setLayout(new FlowLayout());
        f.add(sliderPanel);
        f.pack();
        f.setVisible(true);           
    }
}