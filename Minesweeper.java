import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A Java implementation of the classic Minesweeper game of Microsoft Windows.
 * Modernized for Java 21+ with records, sealed types, switch expressions,
 * pattern matching, and contemporary idioms.
 *
 * @author Ilkka Kokkarinen
 */
public class Minesweeper extends JPanel {

    // --- Coordinate record and neighbour enumeration ---

    /** A position on the grid, with utility methods for bounds and neighbours. */
    record Pos(int x, int y) {

        boolean inBounds(int w, int h) {
            return x >= 0 && x < w && y >= 0 && y < h;
        }

        private static final int[][] OFFSETS = {
                {-1, -1}, {-1, 0}, {-1, 1},
                { 0, -1},          { 0, 1},
                { 1, -1}, { 1, 0}, { 1, 1}
        };

        /** The (up to) eight neighbours of this position within the given bounds. */
        java.util.List<Pos> neighbours(int w, int h) {
            return java.util.Arrays.stream(OFFSETS)
                    .map(off -> new Pos(x + off[0], y + off[1]))
                    .filter(p -> p.inBounds(w, h))
                    .toList();
        }
    }

    // --- Tile state as a sealed hierarchy ---

    /** The visual/logical state of a single tile. */
    sealed interface TileState {
        record Closed()  implements TileState { }
        record Marked()  implements TileState { }
        record Opened()  implements TileState { }
    }

    private static final TileState CLOSED = new TileState.Closed();
    private static final TileState MARKED = new TileState.Marked();
    private static final TileState OPENED = new TileState.Opened();

    // --- Layout constants ---

    private static final int TILE_SIZE = 30;
    private static final int X_OFFSET = 10;
    private static final int Y_OFFSET = 10;
    private static final int NUM_OFFSET = TILE_SIZE / 2 - 4;
    private static final Font TILE_FONT = new Font("Code2000", Font.BOLD, 12);
    private static final BasicStroke TILE_STROKE = new BasicStroke(2.0f);

    // --- Instance state ---

    private final int gridWidth;
    private final int gridHeight;
    private final double prob;
    private final RandomGenerator rng = RandomGenerator.getDefault();

    private boolean gameOn;
    private boolean firstClick;
    private boolean[][] mines;
    private TileState[][] state;
    private int[][] value; // precomputed neighbour mine counts

    /**
     * Construct a new Minesweeper panel of given size.
     *
     * @param w    the width of the game field, in tiles
     * @param h    the height of the game field, in tiles
     * @param prob the probability of a tile containing a mine (0.0–1.0)
     */
    public Minesweeper(int w, int h, double prob) {
        this.gridWidth = w;
        this.gridHeight = h;
        this.prob = prob;
        setBackground(Color.GRAY);
        setPreferredSize(new Dimension(
                2 * X_OFFSET + w * TILE_SIZE,
                2 * Y_OFFSET + h * TILE_SIZE
        ));
        addMouseListener(new MineListener());
        startNewGame();
    }

    // --- Game lifecycle ---

    private void startNewGame() {
        gameOn = true;
        firstClick = true;
        mines = new boolean[gridWidth][gridHeight];
        state = new TileState[gridWidth][gridHeight];
        value = new int[gridWidth][gridHeight];
        for (var row : state) { java.util.Arrays.fill(row, CLOSED); }
        repaint();
    }

    /**
     * Plant the mines after the first click, keeping a Manhattan-distance
     * safe zone of radius 2 around the clicked position.
     */
    private void seedMines(Pos start) {
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                boolean safeZone = Math.abs(x - start.x()) + Math.abs(y - start.y()) <= 2;
                mines[x][y] = !safeZone && rng.nextDouble() < prob;
            }
        }
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                var pos = new Pos(x, y);
                value[x][y] = (int) pos.neighbours(gridWidth, gridHeight).stream()
                        .filter(p -> mines[p.x()][p.y()])
                        .count();
            }
        }
    }

    private int countMarkedNeighbours(Pos pos) {
        return (int) pos.neighbours(gridWidth, gridHeight).stream()
                .filter(p -> state[p.x()][p.y()] instanceof TileState.Marked)
                .count();
    }

    // --- Tile opening logic ---

    /**
     * Open the tile at the given position. Returns {@code true} if a mine
     * was hit. When {@code cascadeOnly} is true, only the neighbours are
     * cascade-opened (used for chord clicks on satisfied numbers).
     */
    private boolean openTile(Pos pos, boolean cascadeOnly) {
        if (!pos.inBounds(gridWidth, gridHeight)) { return false; }
        if (!cascadeOnly) {
            // Already open or flagged — nothing to do.
            if (!(state[pos.x()][pos.y()] instanceof TileState.Closed)) { return false; }
            if (firstClick) {
                firstClick = false;
                seedMines(pos);
            }
            state[pos.x()][pos.y()] = OPENED;
            if (mines[pos.x()][pos.y()]) { return true; }
        }
        // Cascade: if the count matches the number of marked neighbours,
        // recursively open all unmarked neighbours.
        if (value[pos.x()][pos.y()] == countMarkedNeighbours(pos)) {
            boolean hitMine = false;
            for (var neighbour : pos.neighbours(gridWidth, gridHeight)) {
                hitMine |= openTile(neighbour, false);
            }
            return hitMine;
        }
        return false;
    }

    private void revealAll() {
        gameOn = false;
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                state[x][y] = OPENED;
            }
        }
        repaint();
    }

    // --- Mouse handling ---

    private class MineListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent me) {
            if (!gameOn) { startNewGame(); return; }
            var pos = new Pos(
                    (me.getX() - X_OFFSET) / TILE_SIZE,
                    (me.getY() - Y_OFFSET) / TILE_SIZE
            );
            if (!pos.inBounds(gridWidth, gridHeight)) { return; }

            boolean hitMine = switch (me.getButton()) {
                case MouseEvent.BUTTON1 -> openTile(pos, false);
                default -> {
                    // Right click (or middle): toggle flag.
                    if (state[pos.x()][pos.y()] instanceof TileState.Closed) {
                        state[pos.x()][pos.y()] = MARKED;
                    } else if (state[pos.x()][pos.y()] instanceof TileState.Marked) {
                        state[pos.x()][pos.y()] = CLOSED;
                    }
                    yield false;
                }
            };

            // Chord-open: any open tile whose count matches its marked
            // neighbours can have its remaining neighbours auto-opened.
            if (!hitMine) {
                for (int x = 0; x < gridWidth; x++) {
                    for (int y = 0; y < gridHeight; y++) {
                        if (state[x][y] instanceof TileState.Opened) {
                            var p = new Pos(x, y);
                            if (value[x][y] == countMarkedNeighbours(p)) {
                                hitMine |= openTile(p, true);
                            }
                        }
                    }
                }
            }

            if (hitMine) { revealAll(); } else { repaint(); }
        }
    }

    // --- Rendering ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(TILE_FONT);
        g2.setStroke(TILE_STROKE);

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                int bx = X_OFFSET + TILE_SIZE * x;
                int by = Y_OFFSET + TILE_SIZE * (y + 1);

                switch (state[x][y]) {
                    case TileState.Opened _ -> {
                        if (mines[x][y]) {
                            g2.drawString("M", bx + NUM_OFFSET, by - NUM_OFFSET);
                        } else if (value[x][y] > 0) {
                            g2.drawString(String.valueOf(value[x][y]),
                                    bx + NUM_OFFSET, by - NUM_OFFSET);
                        }
                    }
                    case TileState.Closed _, TileState.Marked _ -> {
                        var rect = new RoundRectangle2D.Double(
                                bx + 2, by - TILE_SIZE + 2,
                                TILE_SIZE - 4, TILE_SIZE - 4, 5, 5
                        );
                        g2.setPaint(state[x][y] instanceof TileState.Marked
                                ? Color.RED : Color.GREEN);
                        g2.fill(rect);
                        g2.setPaint(Color.BLACK);
                        g2.draw(rect);
                    }
                }
            }
        }
    }

    // --- Entry point ---

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("Minesweeper");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(new FlowLayout());
            frame.add(new Minesweeper(35, 20, 0.23));
            frame.pack();
            frame.setVisible(true);
        });
    }
}