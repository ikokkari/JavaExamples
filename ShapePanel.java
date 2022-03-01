import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * An example Swing component that has no interaction, but displays an
 * assortment of geometric shapes rendered in various ways.
 * @author Ilkka Kokkarinen
 */

public class ShapePanel extends JPanel {
  
    private final boolean antiAlias;
    
    /**
     * Constructor for the class.
     * @param antiAlias Whether the rendering should be done using anti-aliasing.
     */
    public ShapePanel(boolean antiAlias) {
        // Subclasses can always define additional fields and behaviour.
        this.antiAlias = antiAlias;
        // The one setting you must provide for your custom Swing component.
        this.setPreferredSize(new Dimension(500, 300));
        // Many other settings and options can also be given.
        this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        this.setToolTipText("Antialiasing is " + (antiAlias? "on": "off"));
    }

    /**
     * Render this component as it currently looks like.
     * @param g The {@code Graphics} object provided by Swing for us to draw on.
     */
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g); // Erase the previous contents of the component.
        Graphics2D g2 = (Graphics2D)g; // Downcast the reference to modern Graphics2D.
        if(antiAlias) {
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            ); // looks nicer
        }
          
        // To render shapes, you need to define the stroke and the paint strategies used.
        // This is just a fancy way to say that you need to choose the pen and the colour.
        g2.setStroke(new BasicStroke(3.0f));
        g2.setPaint(Color.RED); // Solid uniform colour
        for(int r = 0; r < 150; r += 10) {
            // If nothing else, let's admire the existence of the method "draw"
            // that can draw the outline of any Shape that anybody will ever think up.
            g2.draw(new Rectangle2D.Double(100 - r / 2.0,100 - r / 2.0, r, r));
        }
        
        // A rectangle with round corners.
        g2.setStroke(new BasicStroke(8.0f));
        g2.setPaint(new GradientPaint(210, 60, Color.YELLOW, 290, 140, Color.BLUE));
        RoundRectangle2D.Double rect = new RoundRectangle2D.Double(200, 50, 100, 100, 30, 45);
        g2.fill(rect); // Insides of the shape
        g2.setPaint(Color.ORANGE);
        g2.draw(rect); // Outline

        // An ellipse shape is defined using an invisible bounding rectangle. Decorating any
        // Shape with Area unlocks the power of affine transforms and computational geometry.
        Area elli = new Area(new Ellipse2D.Double(350, 50, 70, 140));
        // AffineTransform has factory methods to create all kinds of transforms. Note that
        // the rotation angle is radians in [0, 2*Math.PI] instead of angles in [0, 360].
        AffineTransform trans = AffineTransform.getRotateInstance(-Math.PI / 20, 385, 120);
        elli = elli.createTransformedArea(trans);
        // Constructive solid geometry operations add, subtract, intersect and exclusiveOr
        // can create complex shapes by combining simpler shapes together. 
        elli.subtract(new Area(new Ellipse2D.Double(400, 100, 50, 50)));
        
        // Now we get to admire the results.
        g2.setPaint(new GradientPaint(370, 60, Color.BLACK, 360, 150, Color.WHITE));
        // Polymorphic draw and fill methods deal with our new custom Area no problemo.
        // These methods expect some kind of Shape, and every Area is a Shape. That's
        // all there is to it with polymorphic methods, really.
        g2.fill(elli);
        // When rendering piecewise shapes, make sure to use nice cap and join settings.
        g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.setPaint(Color.BLUE);
        g2.draw(elli);
        
        // A more complex polygon shape that is partially outside the component bounds. The
        // rendering engine will automatically clip each shape into the visible "peephole"
        // defined by the component bounds.
        final int CENTER_X = 150, CENTER_Y = 250;
        Path2D.Double path = new Path2D.Double(); // outline path of the shape
        int points = 10; // Try out different values to see how these formulas work.
        int n = 2 * points; // As a polygon, a ten-point star has twenty corner points.
        for(int i = 0; i < n; i++) {
            double a = i * 2 * Math.PI / n; // Angles are given in radians.
            double r = (i % 2 == 0) ? 100 : 50; // Tip or groove?
            if(i == 0) { // Starting point of the polygon starts the path.
                path.moveTo(CENTER_X + r * Math.cos(a), CENTER_Y + r * Math.sin(a));
            }
            else { // Consecutive line segment edges.
                path.lineTo(CENTER_X + r * Math.cos(a), CENTER_Y + r * Math.sin(a));
            }
        }
        
        // A closed path into can be turned into an Area that it encloses.
        Area area = new Area(path);
        g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.setPaint(Color.WHITE);
        g2.fill(area);
        g2.setPaint(Color.BLACK);
        g2.draw(area);
        
        // Finally, here is how you can render some text. The glyphs defined in each font
        // are just curves and line segments, and their rendering is fundamentally no
        // different from the rendering of the above shapes.
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Times", Font.ITALIC, 28)); // hook to the fonts in your system
        g2.drawString("Hello, wyrld!", 300, 250); // letter y reaches below baseline
    }
   
    /**
     * Create a JFrame, a free-moving window component, and put a ShapePanel inside it.
     */
    public static void main(String[] args) {
        // A JPanel cannot exist alone on the screen, and must be place inside some
        // top-level container, which in all our examples will be JFrame.
        JFrame f = new JFrame("ShapePanel demo");
        // Tell the frame to obey the close button.
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Default layout manager of JFrame is BorderLayout, which is too fancy here.
        f.setLayout(new FlowLayout());
        // Let's add two separate ShapePanel instances, just to see how it works.
        f.add(new ShapePanel(true)); // one with antialiasing
        f.add(new ShapePanel(false)); // one without antialiasing
        f.pack();
        f.setVisible(true);        
    }
}