import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import java.util.ArrayList;

// As seen in http://paulbourke.net/fractals/randomtile/

public class SpaceFiller {

    private static final Random rng = new Random();

    // A simple dataclass that encapsulates an Area and its bounding circle.
    public static class AreaInfo {
        public Area area; // The actual area object stored in this AreaInfo.
        public double cx, cy, r; // The bounding circle center and radius.
        public AreaInfo(Area area, double cx, double cy, double r) {
            // The normal dataclass stuff.
            this.area = area; this.cx = cx; this.cy = cy; this.r = r;
        }
    }

    // An abstract factory that generates a random Area object.
    public static interface AreaFactory {
        public AreaInfo createArea(int w, int h, double r);
    }

    // An implementation that generates circles.
    public static class CircleFactory implements AreaFactory {
        public AreaInfo createArea(int w, int h, double r) {
            double cx = r + rng.nextDouble() * (w - 2*r);
            double cy = r + rng.nextDouble() * (h - 2*r);
            return new AreaInfo(
                new Area(new Ellipse2D.Double(cx - r, cy - r, 2*r, 2*r)),
                cx, cy, r
            );
        }
    }

    // An implementation that generates rings.
    public static class RingFactory implements AreaFactory {
        public AreaInfo createArea(int w, int h, double r) {
            double cx = r + rng.nextDouble() * (w - 2*r);
            double cy = r + rng.nextDouble() * (h - 2*r);
            Area a1 = new Area(new Ellipse2D.Double(cx - r, cy - r, 2*r, 2*r));
            Area a2 = new Area(new Ellipse2D.Double(cx - 0.8*r, cy - 0.8 * r, 1.6*r, 1.6*r));
            a1.subtract(a2);
            return new AreaInfo(a1, cx, cy, r);
        }
    }
    
    // Another implementation that generates rotated regular n-gons.
    public static class RegularPolygonFactory implements AreaFactory {
        private int n1, n2; // minimum and maximum possible value for n
        public RegularPolygonFactory(int n) { this.n1 = this.n2 = n; }
        public RegularPolygonFactory(int n1, int n2) { this.n1 = n1; this.n2 = n2; }
        public AreaInfo createArea(int w, int h, double r) {
            double cx = r + rng.nextDouble() * (w - 2 * r);
            double cy = r + rng.nextDouble() * (w - 2 * r);
            double a = rng.nextDouble() * Math.PI;
            int n = n1 < n2 ? rng.nextInt(n2 - n1 + 1) + n1 : n1;
            // A polygon shape is defined as a path of its edge segments.
            Path2D.Double path = new Path2D.Double();
            path.moveTo(cx + r * Math.cos(a), cy + r * Math.sin(a));
            for(int i = 0; i <= n; i++) {
                double aa = a + (i % n) * 2 * Math.PI / n;
                path.lineTo(cx + r * Math.cos(aa), cy + r * Math.sin(aa));
            }
            return new AreaInfo(new Area(path), cx, cy, r);
        }
    }

    // Another implementation that generates rotated plus signs.
    public static class PlusFactory implements AreaFactory {
        public AreaInfo createArea(int w, int h, double r) {
            double cx = r + rng.nextDouble() * (w - 2 * r);
            double cy = r + rng.nextDouble() * (w - 2 * r);
            double a = rng.nextDouble() * Math.PI / 30 + Math.sin(0.02*cx + 0.035*cy);
            Path2D.Double path = new Path2D.Double();
            path.moveTo(cx + r * Math.cos(a), cy + r * Math.sin(a));
            for(int i = 0; i < 13; i++) {
                double rr = (i % 3 > 0) ? r : r * 0.37;
                double aa = a + (i % 12) * Math.PI / 6;  
                path.lineTo(cx + rr * Math.cos(aa), cy + rr * Math.sin(aa));
            }
            return new AreaInfo(new Area(path), cx, cy, r);
        }
    }

    // Then, an implementation that generates blammo star polygons with curved edges.
    public static class StarFactory implements AreaFactory {
        public AreaInfo createArea(int w, int h, double r) {
            int n = 2 * (rng.nextInt(5) + 5);
            // Center of the star
            double cx = r + rng.nextDouble() * (w - 2*r);
            double cy = r + rng.nextDouble() * (w - 2*r);
            double[] xp = new double[n];
            double[] yp = new double[n];
            double r2;
            // Generate the random points at constant angle increment.
            for(int i = 0; i < n; i++) {
                if(i % 2 == 0) { // tip
                    r2 = rng.nextDouble() * r/3 + 2*r/3;
                }
                else { // groove
                    r2 = rng.nextDouble() * r/10 + r/10;
                }
                xp[i] = cx + Math.cos(i * 2 * Math.PI / n) * r2;
                yp[i] = cy + Math.sin(i * 2 * Math.PI / n) * r2;
            }
            // A polygon shape is defined as a path of its edge segments.
            Path2D.Double path = new Path2D.Double();
            path.moveTo(xp[0], yp[0]); // starting point
            for(int i = n-1; i > 0; i = i - 2) {
                path.quadTo(xp[i], yp[i], xp[i-1], yp[i-1]);
            }

            return new AreaInfo(new Area(path), cx, cy, r);
        }
    }

    // Check whether the bounding circles of two areas partially overlap.
    private static boolean boundingOverlap(AreaInfo a1, AreaInfo a2) {
        double dx = a1.cx - a2.cx; // difference in x-direction
        double dy = a1.cy - a2.cy; // difference in y-direction
        double rr = (a1.r + a2.r); // sum of radii
        return (dx*dx + dy*dy) < rr * rr; // no need to compute expensive square root
    }

    private static long overlapTestCount = 0, heavyTestCount = 0;
    
    // Determine if the next area overlaps any one of the existing areas.
    private static boolean overlapsSomething(AreaInfo next, ArrayList<AreaInfo> existingAreas) {
        for(AreaInfo ai: existingAreas) {
            overlapTestCount++;
            if(boundingOverlap(next, ai)) { // quick rejection with bounding circles
                Area tmp = (Area)(next.area).clone();
                heavyTestCount++;
                tmp.intersect(ai.area); // expensive, so thank goodness for bounding circles
                if(!tmp.isEmpty()) {
                    return true; // and that's that about that
                }
            }
        }
        return false; // all is good
    }

    // Scale the next area by the given scaling factor sf. Note that when combining affine
    // transformations of geometry, the concentenations have to be done in reverse order to
    // the order in which these transformations are supposed to work. That is just how
    // matrices and linear algebra work.
    private static void scaleArea(AreaInfo next, double sf) {
        // Step 3. bring origin to center
        AffineTransform tr = AffineTransform.getTranslateInstance(next.cx, next.cy);
        // Step 2. scale around the origin
        tr.concatenate(AffineTransform.getScaleInstance(sf, sf));
        // Step 1. bring center to origin
        tr.concatenate(AffineTransform.getTranslateInstance(-next.cx, -next.cy));
        // Update the area information
        next.r = sf * next.r;
        next.area.transform(tr);
    }

    /**
     * Fill the space of given width and height with random shapes without overlapping.
     * @param w The width of the space.
     * @param h The height of the space.
     * @param n How many random shapes are to be put in.
     * @param g2 If not {@code null}, each shape is rendered to this when created.
     * @param afs The array of AreaFactory objects to be used to generate the random shapes.
     * @return A list that contains all the created shapes as AreaInfo objects.
     */
    public static java.util.List<AreaInfo> fillSpace(int w, int h, int n, Graphics2D g2, AreaFactory... afs) {
        ArrayList<AreaInfo> areasCreated = new ArrayList<AreaInfo>(n);
        overlapTestCount = heavyTestCount = 0;
        double maxR = w / 30;
        double r = maxR; // Current radius to try
        if(g2 != null) {
            g2.setColor(Color.WHITE); // fill the image with white
            g2.fill(new Rectangle2D.Double(0, 0, w, h));
        }
        
        while(areasCreated.size() < n) {
            // Pick a random AreaFactory to use this round.
            AreaFactory af = afs[rng.nextInt(afs.length)];
            // Generate a random area using the provided generator.
            AreaInfo next = af.createArea(w, h, r);
            // Check that this area does not overlap previously added areas.
            boolean isGood = true;

            if(overlapsSomething(next, areasCreated)) {
                r = 0.999 * r; // Whenever failing, try slightly smaller radius next
            }
            else {
                do {
                    scaleArea(next, 1.05); 
                } while(next.r <= maxR && !overlapsSomething(next, areasCreated));
                scaleArea(next, 1.0 / 1.05); // back to the scale that worked
                if(g2 != null) { // render this shape if required
                    int c = rng.nextInt(100);
                    g2.setColor(new Color(c + rng.nextInt(50), c + rng.nextInt(50), c + rng.nextInt(50)));
                    g2.fill(next.area);
                }
                areasCreated.add(next);
                r = Math.min(maxR, 1.2 * r); // Whenever successful, try larger radius next
            }
        }
        System.out.println("Random space fill complete using " + areasCreated.size() + " shapes.");
        System.out.println("Performed " + overlapTestCount + " overlap checks, only " + heavyTestCount +
        " of which were heavy.");
        return areasCreated;
    }

    private static class ImageTimerPanel extends JPanel {
        private javax.swing.Timer timer;
        private Image img;
        public ImageTimerPanel(Image img) {
            this.setPreferredSize(new Dimension(img.getWidth(this), img.getHeight(this)));
            this.timer = new Timer(100, (ae) -> repaint());
            this.img = img;
            this.timer.start();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, this);
        }

        public void stopTimer() {
            timer.stop(); 
        }
    }

    public static ImageTimerPanel createPanel(final int w, final int h, int n, AreaFactory... ags) {
        final Image img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);    
        final ImageTimerPanel result = new ImageTimerPanel(img);
        result.setBorder(BorderFactory.createEtchedBorder());
        new Thread(new Runnable() { // sneak preview of the concurrency lecture
                public void run() { // the method executed by the new execution thread
                    Graphics2D g2 = (Graphics2D)(img.getGraphics());
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    fillSpace(w, h, n, g2, ags);
                    result.stopTimer();
                }
            }).start();
        return result;
    }

    public static void createBigOne(int w, int h, int n, String filename) {
        new Thread(new Runnable() { 
                public void run() {
                    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = (Graphics2D)(img.getGraphics());
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    fillSpace(w, h, n, g2,
                        new RegularPolygonFactory(3, 6),
                        new CircleFactory(),
                        new RingFactory(),
                        new StarFactory(),
                        new PlusFactory()
                    );
                    System.out.println("Completed calculating image: " + filename);
                    try {
                        ImageIO.write(img, "PNG", new java.io.File(filename + ".png"));
                        ImageIO.write(img, "JPG", new java.io.File(filename + ".jpg"));
                    }
                    catch(Exception e) {
                        System.out.println("Write failed: " + e);
                    }
                }
            }).start();
    }

    public static void main(String[] args) {
        ImageTimerPanel itp = createPanel(1000, 1000, 15000,
                new CircleFactory(),
                new RingFactory(),
                new StarFactory(),
                new PlusFactory(),
                new RegularPolygonFactory(3, 6)
            );
        JFrame f = new JFrame("Fractal filling of plane with various random shapes");
        f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    itp.stopTimer(); // first stop the timer of the ImageTimerPanel component
                    f.dispose(); // and now we can safely dispose of the frame
                }
            });
        f.setLayout(new FlowLayout());
        f.add(itp);
        f.pack();
        f.setVisible(true);
    }
}