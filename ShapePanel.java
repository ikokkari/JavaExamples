import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * An example Swing component that has no interaction, but displays an
 * assortment of geometric shapes rendered in various ways.
 * @author Ilkka Kokkarinen
 */

public class ShapePanel extends JPanel {
  
    private boolean antiAlias;
    
    /**
     * Constructor the class.
     * @param antiAlias Whether the rendering should be done using anti-aliasing.
     */
    public ShapePanel(boolean antiAlias) {
        // The one setting you must provide for every Swing component.
        this.setPreferredSize(new Dimension(500, 300));
        // Many other settings and options can also be given.
        this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        this.setToolTipText("Antialiasing is " + (antiAlias? "on": "off"));
        this.antiAlias = antiAlias;
    }

    /**
     * Render this component as it currently looks like.
     * @param g The {@code Graphics} object provided by Swing for us to draw on.
     */
    public void paintComponent(Graphics g) {
        // The first two lines are magic incantations to be explained in 209.
        super.paintComponent(g); // erase previous contents
        Graphics2D g2 = (Graphics2D)g; // convert to better Graphics2D
        if(antiAlias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON); // looks nicer
        }
          
        // https://docs.oracle.com/javase/tutorial/2d/TOC.html
        
        // A rectangle shape.
        g2.setStroke(new BasicStroke(3.0f));
        g2.setPaint(Color.RED); // uniform colour
        for(int r = 0; r < 150; r += 10) {
            g2.draw(new Rectangle2D.Double(100-r/2,100-r/2,r,r));
        }
        
        // A rectangle with round corners.
        g2.setStroke(new BasicStroke(8.0f));
        g2.setPaint(new GradientPaint(210, 60, Color.YELLOW, 
          290, 140, Color.BLUE)); // colour gradient
        g2.fill(new RoundRectangle2D.Double(200,50,100,100,10,15));
        g2.setPaint(Color.GREEN);
        g2.draw(new RoundRectangle2D.Double(200,50,100,100,10,15));

        // An ellipse shape, defined with invisible bounding rectangle.
        g2.setPaint(new GradientPaint(370, 100, Color.BLACK,
          360, 130, Color.WHITE));
        g2.fill(new Ellipse2D.Double(350,50,70,140));
        
        // A more complex polygon shape that is partially outside the component bounds.
        Path2D.Double path = new Path2D.Double(); // outline path of the shape
        for(int i = 0; i < 20; i++) { // 10 point star has 20 lines
            double a = i * 2 * Math.PI / 20; // angle in radians from [0, 2 * Math.PI]
            double r = (i % 2 == 0) ? 100 : 50; // tip or groove?
            if(i == 0) { 
                path.moveTo(150 + r * Math.cos(a), 250 + r * Math.sin(a)); // starting point
            }
            else {
                path.lineTo(150 + r * Math.cos(a), 250 + r * Math.sin(a)); // line segment edge
            }
        }
        Area area = new Area(path); // turn the closed path into Area object that it encloses
        g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.setPaint(Color.WHITE);
        g2.fill(area);
        g2.setPaint(Color.BLACK);
        g2.draw(area);
        
        // Last, render some text.
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Times", Font.ITALIC, 28));
        g2.drawString("Hello, wyrld!", 300, 250); // y reaches below baseline
    }
    
    // A JPanel cannot exist alone on the screen; it has to be inside some top-level component.
    /**
     * Create a JFrame, a free-moving window component, and put a ShapePanel inside it.
     */
    public static void main(String[] args) {
        JFrame f = new JFrame("ShapePanel demo");
        // Tell the frame to obey the close button
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());
        // Let's add two separate ShapePanel instances, just to see how it works.
        f.add(new ShapePanel(true)); // with antialiasing
        f.add(new ShapePanel(false)); // without antialiasing
        f.pack();
        f.setVisible(true);        
    }
}