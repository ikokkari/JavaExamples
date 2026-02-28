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
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

/**
 * A custom Swing component that displays an assortment of geometric shapes
 * rendered in various ways: solid fills, gradient paints, affine transforms,
 * constructive solid geometry, and text rendering.
 * <p>
 * This example has no interaction (no listeners) — it focuses purely on the
 * rendering pipeline of Java 2D. Two instances are shown side by side so
 * you can compare anti-aliased vs. aliased rendering.
 * <p>
 * Updated for modern Java with better naming and Swing threading best practices.
 *
 * @author Ilkka Kokkarinen
 */
public class ShapePanel extends JPanel {

    private final boolean antiAlias;

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    /**
     * Create a ShapePanel with the given anti-aliasing setting.
     *
     * @param antiAlias whether rendering should use anti-aliasing
     */
    public ShapePanel(boolean antiAlias) {
        this.antiAlias = antiAlias;
        // The one setting you *must* provide for a custom Swing component.
        setPreferredSize(new Dimension(500, 300));
        // Optional cosmetic settings.
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        setToolTipText("Anti-aliasing is " + (antiAlias ? "on" : "off"));
    }

    // -----------------------------------------------------------------------
    // Rendering — the heart of this example.
    // -----------------------------------------------------------------------

    /**
     * Render the component's contents. Swing calls this method whenever the
     * component needs to be redrawn (e.g. after a resize or when first shown).
     *
     * @param g the {@code Graphics} context provided by Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Erase previous contents.
        // Downcast to Graphics2D for access to the modern Java 2D API.
        var g2 = (Graphics2D) g;
        if (antiAlias) {
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
        }

        drawConcentricSquares(g2);
        drawRoundedRectangle(g2);
        drawTransformedEllipse(g2);
        drawStar(g2);
        drawText(g2);
    }

    // -----------------------------------------------------------------------
    // Individual shape-drawing methods, factored out for clarity.
    // -----------------------------------------------------------------------

    /**
     * Concentric squares: a simple loop demonstrating the polymorphic
     * {@code draw} method that can render the outline of <em>any</em> Shape.
     */
    private void drawConcentricSquares(Graphics2D g2) {
        g2.setStroke(new BasicStroke(3.0f));
        g2.setPaint(Color.RED);
        for (int size = 0; size < 150; size += 10) {
            double offset = size / 2.0;
            g2.draw(new Rectangle2D.Double(100 - offset, 100 - offset, size, size));
        }
    }

    /**
     * A rounded rectangle filled with a gradient and outlined in orange.
     * Demonstrates: GradientPaint, fill vs. draw, RoundRectangle2D.
     */
    private void drawRoundedRectangle(Graphics2D g2) {
        var roundRect = new RoundRectangle2D.Double(200, 50, 100, 100, 30, 45);
        g2.setStroke(new BasicStroke(8.0f));
        g2.setPaint(new GradientPaint(210, 60, Color.YELLOW, 290, 140, Color.BLUE));
        g2.fill(roundRect);
        g2.setPaint(Color.ORANGE);
        g2.draw(roundRect);
    }

    /**
     * An ellipse that is rotated and then has a circular hole subtracted.
     * Demonstrates: Area (constructive solid geometry), AffineTransform,
     * and the fact that polymorphic draw/fill work on any Shape — including
     * these computed composite areas.
     */
    private void drawTransformedEllipse(Graphics2D g2) {
        // An ellipse defined by its invisible bounding rectangle.
        var ellipse = new Area(new Ellipse2D.Double(350, 50, 70, 140));

        // Rotate 13° around the center of the ellipse.
        // Note: AffineTransform uses radians, not degrees.
        var rotation = AffineTransform.getRotateInstance(
                Math.toRadians(13), 385, 120
        );
        ellipse = ellipse.createTransformedArea(rotation);

        // Constructive solid geometry: subtract a circular hole.
        // Other available operations: add, intersect, exclusiveOr.
        ellipse.subtract(new Area(new Ellipse2D.Double(400, 100, 50, 50)));

        // Fill with a gradient, then draw the outline.
        g2.setPaint(new GradientPaint(370, 60, Color.BLACK, 360, 150, Color.WHITE));
        g2.fill(ellipse);
        g2.setStroke(new BasicStroke(
                5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND
        ));
        g2.setPaint(Color.BLUE);
        g2.draw(ellipse);
    }

    /**
     * A ten-pointed star built as a polygon path, partially clipped by the
     * component bounds. Demonstrates: Path2D for custom polygon shapes, and
     * automatic clipping by the rendering engine.
     */
    private void drawStar(Graphics2D g2) {
        final int centerX = 150;
        final int centerY = 250;
        final int points = 10;
        final double outerRadius = 100;
        final double innerRadius = 50;
        int vertices = 2 * points; // A 10-point star has 20 polygon vertices.

        var path = new Path2D.Double();
        for (int i = 0; i < vertices; i++) {
            double angle = i * 2 * Math.PI / vertices;
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            if (i == 0) {
                path.moveTo(x, y); // First vertex starts the path.
            } else {
                path.lineTo(x, y); // Subsequent vertices add line segments.
            }
        }
        path.closePath();

        // Convert the closed path into an Area and render it.
        var starArea = new Area(path);
        g2.setStroke(new BasicStroke(
                5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND
        ));
        g2.setPaint(Color.WHITE);
        g2.fill(starArea);
        g2.setPaint(Color.BLACK);
        g2.draw(starArea);
    }

    /**
     * Render some text. Font glyphs are ultimately just curves and line
     * segments — their rendering is fundamentally no different from the
     * shapes above.
     */
    private void drawText(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        // The Font constructor hooks into the fonts installed on your system.
        g2.setFont(new Font("Serif", Font.ITALIC, 28));
        // Note how the letter 'y' reaches below the baseline.
        g2.drawString("Hello, wyrld!", 300, 250);
    }

    // -----------------------------------------------------------------------
    // Main — create a JFrame with two ShapePanels for comparison.
    // -----------------------------------------------------------------------

    /**
     * Launch the demo. A JPanel cannot exist alone on screen — it must be
     * placed inside a top-level container, which here is a JFrame.
     */
    public static void main(String[] args) {
        // Swing components must be created and manipulated on the Event
        // Dispatch Thread (EDT). SwingUtilities.invokeLater ensures this.
        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("ShapePanel Demo");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            // FlowLayout places the two panels side by side, unlike the
            // default BorderLayout which would stack or stretch them.
            frame.setLayout(new FlowLayout());
            frame.add(new ShapePanel(true));  // with anti-aliasing
            frame.add(new ShapePanel(false)); // without anti-aliasing
            frame.pack();
            frame.setVisible(true);
        });
    }
}