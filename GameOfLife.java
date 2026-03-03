import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.BitSet;
import java.util.random.RandomGenerator;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Animate and display variations of Conway's Game of Life. The game starts
 * running as soon as the object is constructed and keeps running until the
 * method {@link #terminate()} is called to stop the animation timer.
 *
 * <p>Modernized for Java 21+ with records, sealed types, {@code BitSet}
 * for the rule encoding, and contemporary Swing idioms.
 *
 * @author Ilkka Kokkarinen
 */
public class GameOfLife extends JPanel {

    private static final int PIX_SIZE = 3;
    private static final int MARGIN = 40;
    private static final RandomGenerator RNG = RandomGenerator.getDefault();

    // --- Rule encoding ---

    /**
     * A Life-like cellular automaton rule, encoded as two {@link BitSet}s
     * for O(1) lookup instead of the old {@code String.indexOf} approach.
     *
     * @param birth    neighbour counts that cause a dead cell to become alive
     * @param survival neighbour counts that let a living cell survive
     */
    record Rule(BitSet birth, BitSet survival) {

        /** Parse a rule from the traditional "B.../S..." digit strings. */
        static Rule of(String birthDigits, String survivalDigits) {
            return new Rule(parseBits(birthDigits), parseBits(survivalDigits));
        }

        private static BitSet parseBits(String digits) {
            var bits = new BitSet(9); // neighbour counts range 0..8
            for (int i = 0; i < digits.length(); i++) {
                bits.set(digits.charAt(i) - '0');
            }
            return bits;
        }

        boolean shouldLive(boolean currentlyAlive, int neighbours) {
            return currentlyAlive ? survival.get(neighbours) : birth.get(neighbours);
        }
    }

    // --- Preset rule definitions ---

    /**
     * A named Game of Life variant bundling its rule, initial fill
     * probability, and suggested window position.
     */
    record Variant(String title, Rule rule, double fillProbability, int windowX, int windowY) { }

    // Eight neighbours as (dx, dy) offsets, unrolled for clarity.
    private static final int[][] NEIGHBOUR_OFFSETS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            { 0, -1},          { 0, 1},
            { 1, -1}, { 1, 0}, { 1, 1}
    };

    // --- Instance state ---

    private boolean[][] board;
    private boolean[][] nextBoard;
    private final int size;
    private final Rule rule;
    private final Timer timer;
    private final BufferedImage img;
    private final int aliveRGB = Color.BLACK.getRGB();
    private final int deadRGB = Color.WHITE.getRGB();

    /**
     * Constructor using the original Conway's Game of Life ruleset.
     *
     * @param size the side length of the square grid, in cells
     */
    public GameOfLife(int size) {
        this(size, Rule.of("3", "23"), 0.30);
    }

    /**
     * Constructor for generalized Life-like cellular automata.
     *
     * @param size the side length of the square grid, in cells
     * @param rule the birth/survival rule
     * @param prob probability that each interior cell is initially alive
     */
    public GameOfLife(int size, Rule rule, double prob) {
        this.size = size;
        this.rule = rule;

        int pix = size * PIX_SIZE;
        this.img = new BufferedImage(pix, pix, BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(pix, pix));
        setBorder(BorderFactory.createRaisedBevelBorder());
        setBackground(new Color(deadRGB));

        board = new boolean[size][size];
        nextBoard = new boolean[size][size];
        for (int x = MARGIN; x < size - MARGIN; x++) {
            for (int y = MARGIN; y < size - MARGIN; y++) {
                board[x][y] = RNG.nextDouble() < prob;
            }
        }

        // Tick every 500 ms; stagger initial delay to avoid lockstep.
        timer = new Timer(500, _ -> advance());
        timer.setInitialDelay(RNG.nextInt(500));
        timer.start();
    }

    /** Stop the internal animation timer so that the JVM can exit. */
    public void terminate() {
        timer.stop();
        System.out.println("Game of Life timer terminated");
    }

    // --- Simulation step ---

    private int countNeighbours(int x, int y) {
        int count = 0;
        for (var offset : NEIGHBOUR_OFFSETS) {
            int nx = x + offset[0];
            int ny = y + offset[1];
            if (nx >= 0 && nx < size && ny >= 0 && ny < size && board[nx][ny]) {
                count++;
            }
        }
        return count;
    }

    private void advance() {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int neighbours = countNeighbours(x, y);
                nextBoard[x][y] = rule.shouldLive(board[x][y], neighbours);
                int rgb = nextBoard[x][y] ? aliveRGB : deadRGB;
                for (int px = 0; px < PIX_SIZE; px++) {
                    for (int py = 0; py < PIX_SIZE; py++) {
                        img.setRGB(x * PIX_SIZE + px, y * PIX_SIZE + py, rgb);
                    }
                }
            }
        }
        // Swap board references for the next generation.
        var tmp = board;
        board = nextBoard;
        nextBoard = tmp;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, this);
    }

    // --- Frame factory ---

    private static void createFrame(Variant variant, GameOfLife game) {
        var frame = new JFrame(variant.title());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                game.terminate();
            }
        });
        frame.add(game);
        frame.pack();
        frame.setLocation(variant.windowX(), variant.windowY());
        frame.setVisible(true);
    }

    // --- Entry point ---

    private static final int SIZE = 150;

    public static void main(String[] args) {
        var variants = java.util.List.of(
                new Variant("Conway's Game of Life", Rule.of("3", "23"),     0.20, 100,  100),
                new Variant("Day & Night",           Rule.of("3678", "34678"), 0.40, 100,  600),
                new Variant("Mazectric",             Rule.of("3", "1234"),   0.05, 600,  100),
                new Variant("Diamoeba",              Rule.of("35678", "5678"), 0.50, 600,  600),
                new Variant("Serviettes",            Rule.of("234", ""),     0.03, 1100, 100),
                new Variant("Gnarl",                 Rule.of("1", "1"),      0.01, 1100, 600)
        );

        SwingUtilities.invokeLater(() -> {
            for (var variant : variants) {
                var game = new GameOfLife(SIZE, variant.rule(), variant.fillProbability());
                createFrame(variant, game);
            }
        });
    }
}