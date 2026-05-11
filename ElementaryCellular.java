import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * A Swing component that displays one-dimensional elementary cellular
 * automata with time flowing downward, as is customary in this genre.
 * <p>
 * The 256 possible rules are selected with a slider (Wolfram numbering),
 * and a checkbox toggles the Fredkin XOR variant, which XORs each cell
 * with its state from two generations ago — producing self-similar fractal
 * patterns from many otherwise "boring" rules.
 * <p>
 * Each cell's next state is determined by its own state and those of its
 * two neighbours in the previous generation. The three-bit neighbourhood
 * encodes to a number 0–7, and the corresponding bit of the rule number
 * gives the output. The grid wraps horizontally (toroidal boundary).
 *
 * @see <a href="https://en.wikipedia.org/wiki/Elementary_cellular_automaton">
 *      Wikipedia: Elementary Cellular Automaton</a>
 * @author Ilkka Kokkarinen
 */
public class ElementaryCellular extends JPanel {

    private static final int CELL_SIZE = 2;  // pixels per cell
    private static final Color ALIVE = Color.BLACK;
    private static final Color DEAD = Color.WHITE;

    private final int cols;
    private final int rows;
    private final boolean[][] grid;    // grid[x][y]
    private final BufferedImage image;

    private int rule = 110;
    private final JLabel ruleLabel = new JLabel(formatRule(rule));
    private final JCheckBox fredkinBox = new JCheckBox("Fredkin XOR");

    // -----------------------------------------------------------------------
    // Construction
    // -----------------------------------------------------------------------

    /**
     * Create a cellular automaton display with the given grid dimensions.
     * The initial configuration is a single live cell at the top centre.
     *
     * @param cols number of cells across
     * @param rows number of generations (rows) to display
     */
    public ElementaryCellular(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.grid = new boolean[cols][rows];
        this.image = new BufferedImage(
                cols * CELL_SIZE, rows * CELL_SIZE,
                BufferedImage.TYPE_INT_RGB);

        grid[cols / 2][0] = true;  // seed: single cell at top centre

        setLayout(new BorderLayout());
        add(buildControlPanel(), BorderLayout.NORTH);

        recomputeAndRepaint();
    }

    private JPanel buildControlPanel() {
        var slider = new JSlider(0, 255, rule);
        slider.setMajorTickSpacing(50);
        slider.setPaintTicks(true);

        // Lambdas instead of named inner listener classes.
        slider.addChangeListener(_ -> {
            rule = slider.getValue();
            ruleLabel.setText(formatRule(rule));
            recomputeAndRepaint();
        });
        fredkinBox.addItemListener(_ -> recomputeAndRepaint());

        var controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.LINE_AXIS));
        controls.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        controls.add(new JLabel("Rule: "));
        controls.add(ruleLabel);
        controls.add(Box.createHorizontalStrut(8));
        controls.add(slider);
        controls.add(Box.createHorizontalStrut(12));
        controls.add(fredkinBox);
        return controls;
    }

    private static String formatRule(int rule) {
        // Show both decimal and binary so students see which bits are set.
        return "%d (%s)".formatted(rule, 
                String.format("%8s", Integer.toBinaryString(rule))
                      .replace(' ', '0'));
    }

    // -----------------------------------------------------------------------
    // Cellular automaton logic
    // -----------------------------------------------------------------------

    /**
     * Evaluate cell (x, y) from its row-(y−1) neighbourhood.
     * The three-bit neighbourhood [left, centre, right] is read as
     * a number 0–7, and the corresponding bit of {@code rule} gives
     * the Wolfram output. If the Fredkin variant is enabled, the result
     * is XORed with the cell's state two generations ago.
     */
    private boolean evaluateCell(int x, int y) {
        int neighbourhood =
                (grid[(x + cols - 1) % cols][y - 1] ? 4 : 0)
              | (grid[x][y - 1]                     ? 2 : 0)
              | (grid[(x + 1) % cols][y - 1]        ? 1 : 0);
        boolean wolfram = (rule & (1 << neighbourhood)) != 0;
        if (fredkinBox.isSelected() && y > 1) {
            return wolfram ^ grid[x][y - 2];
        }
        return wolfram;
    }

    /**
     * Recompute the entire grid from the seed row and render it
     * into the offscreen image.
     */
    private void recomputeAndRepaint() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (y > 0) {
                    grid[x][y] = evaluateCell(x, y);
                }
                // Paint the cell into the image.
                int rgb = grid[x][y] ? ALIVE.getRGB() : DEAD.getRGB();
                int px = x * CELL_SIZE;
                int py = y * CELL_SIZE;
                for (int dy = 0; dy < CELL_SIZE; dy++) {
                    for (int dx = 0; dx < CELL_SIZE; dx++) {
                        image.setRGB(px + dx, py + dy, rgb);
                    }
                }
            }
        }
        repaint();
    }

    // -----------------------------------------------------------------------
    // Rendering
    // -----------------------------------------------------------------------

    @Override
    public Dimension getPreferredSize() {
        // Let the layout manager know how big we'd like to be.
        int controlHeight = 40; // approximate height of control panel
        return new Dimension(
                cols * CELL_SIZE,
                rows * CELL_SIZE + controlHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // Draw the image below the control panel.
        int imageY = getHeight() - rows * CELL_SIZE;
        g2.drawImage(image, 0, imageY, this);
    }

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("Elementary Cellular Automata");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new ElementaryCellular(500, 500));
            frame.pack();
            frame.setLocationRelativeTo(null); // centre on screen
            frame.setVisible(true);
        });
    }
}
