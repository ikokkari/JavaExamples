import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;

// See http://www.oftenpaper.net/sierpinski.htm for all about this classic fractal.

/**
 * Renders the Sierpinski triangle inside a {@code JPanel}.
 */
public class Sierpinski extends JPanel {

    private boolean drawAll = true;
    private Random rng = new Random();
    
    /**
     * Default constructor for suitably chosen component dimensions.
     */
    public Sierpinski() {
        this.setPreferredSize(new Dimension(600,600));
        this.setBackground(Color.WHITE);
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                drawAll = !drawAll; repaint();
            }
        });
    }

    /**
     * Render this component as it currently looks like.
     * @param g The {@code Graphics} object provided by Swing for us to draw on.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(0.2f));
        g2.setColor(Color.BLACK);
        sierpinski(g2, 5, 5, getWidth() - 10, getHeight() - 10);
    }

    private void sierpinski(Graphics2D g, double x, double y, double w, double h) {       
        if(w < 10 && h < 10) { // base case when shape is small enough
            if(drawAll || rng.nextBoolean()) g.draw(new Line2D.Double(x,y,x+w,y));
            if(drawAll || rng.nextBoolean()) g.draw(new Line2D.Double(x,y,x+w/2,y+h));
            if(drawAll || rng.nextBoolean()) g.draw(new Line2D.Double(x+w/2,y+h,x+w,y));
        }
        else { // otherwise draw three smaller Sierpinski triangles
            sierpinski(g,x,y,w/2,h/2);
            sierpinski(g,x+w/2,y,w/2,h/2);
            sierpinski(g,x+w/4,y+h/2,w/2,h/2);
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());
        f.add(new Sierpinski());
        f.pack();
        f.setVisible(true);
    }
}