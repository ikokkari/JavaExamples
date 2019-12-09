import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.*;

// Diffusion limited aggregation, adapted from http://paulbourke.net/fractals/dla/

// In this example, particles move around randomly until they come close enough to
// some particle that is already frozen. At that time, that particle also becomes
// frozen, and the particle it connected with becomes its parent.

public class DLA {
    
    private static final Random rng = new Random();
    private static final double STEP = 0.03;
    
    // A nested class to represent an individual particle.
    private static class Particle {
        public double x, y; // Coordinates of this particle, in [0,1] * [0,1];
        public Particle parent; // The parent of this particle once it is frozen.
        
        public Particle() {
            x = rng.nextDouble();
            y = rng.nextDouble();
            parent = null;
        }
        
        public double distSq(Particle other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            return dx*dx + dy*dy;
        }

        public void move() {
            x += rng.nextGaussian() * STEP;
            y += rng.nextGaussian() * STEP;
            // Bounce from the edges.
            if(x < 0) { x = -x; }
            else if(x > 1) { x = 2 - x; }
            if(y < 0) { y = -y; }
            else if(y > 1) { y = 2 - y; }
        }
    }
    
    private static final double SNAP = 0.7;
    
    public static ArrayList<Particle> computeDLA(int n, int m, int points, double d) {
        // Seed the particles randomly. At all times, we have n + m particles so that
        // first n particles are frozen, and the rest are still moving.
        ArrayList<Particle> particles = new ArrayList<Particle>();
        for(int i = 0; i < n + m; i++) {
            particles.add(new Particle());
        }
        
        // Simulate until enough points are frozen.
        while(n < points) {
            // Move each still moving particle a random amount.
            for(int pi = n; pi < particles.size(); pi++) {
                particles.get(pi).move();
            }
            // Loop through moving particles to see if they are close enough to
            // some frozen particles, and if so, make that particle also frozen.
            for(int pi = n; pi < particles.size(); pi++) {
                Particle p = particles.get(pi);
                for(int fi = 0; fi < n; fi++) {
                    if(particles.get(fi).distSq(p) < d) {
                        p.parent = particles.get(fi);
                        // Snap a bit closer
                        p.x = SNAP * p.x + (1-SNAP) * p.parent.x;
                        p.y = SNAP * p.y + (1-SNAP) * p.parent.y;
                        break;
                    }
                }
            }
            // Loop through moving particles and swap those that froze to the
            // area that contains the frozen particles, growing that area by one.
            for(int pi = n; pi < particles.size(); pi++) {
                if(particles.get(pi).parent != null) {
                    Particle tmp = particles.get(n);
                    particles.set(n++, particles.get(pi));
                    particles.set(pi, tmp);
                }
            }
            // Finally, create more moving particles to replace those frozen.
            while(particles.size() - n < m) {
                particles.add(new Particle());
            }
        }
        
        // Return the frozen particles as a separate sublist.
        ArrayList<Particle> result = new ArrayList<Particle>(n);
        for(int i = 0; i < n; i++) {
            result.add(particles.get(i));
        }
        return result;
    }
    
    // A utility method to create a JPanel that displays the result.
    public static JPanel createPanel(final int w, int n, int m, int points, double d) {
        final ArrayList<Particle> particles = computeDLA(n, m, points, d);
        
        class DLAPanel extends JPanel {
            public DLAPanel() {
                this.setPreferredSize(new Dimension(w, w));
                this.setBackground(Color.WHITE);
            }
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setPaint(new Color(30, 25, 20));
                g2.setStroke(new BasicStroke(4.0f));
                for(Particle p: particles) {
                    Particle pa = p.parent;
                    if(pa != null) {
                        Particle ppa = pa.parent;
                        if(ppa != null) {
                            g2.draw(new CubicCurve2D.Double(
                                w*p.x, w*p.y, w*pa.x, w*pa.y, w*pa.x, w*pa.y, w*ppa.x, w*ppa.y
                            ));
                        }
                    }
                }
            } 
        }
        
        return new DLAPanel();
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame("DLA");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());
        f.add(createPanel(800, 10, 20, 2000, 0.002));
        f.pack();
        f.setVisible(true);        
    }
}