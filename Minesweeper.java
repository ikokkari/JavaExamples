import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * A Java implementation of the classic Minesweeper game of Microsoft Windows.
 * @author Ilkka Kokkarinen
 */

public class Minesweeper extends JPanel {

    private final int gridWidth;
    private final int gridHeight; // Game board width and height in tiles
    private final double prob; // Probability that a tile becomes a mine
    private boolean gameOn; // Is the game on?
    private boolean first; // Is the player opening the first tile?
    private boolean[][] isMine, isOpen, isMarked; // State of the gameboard
    private int[][] value; // Precomputed numerical values to show
    private final Random rng = new Random();

    private static final int TILESIZE = 30;
    private static final int XOFFSET = 10;
    private static final int YOFFSET = 10;
    private static final int NOFF = TILESIZE / 2 - 4;
    private static final Font font = new Font("Code2000", Font.BOLD, 12);

    /**
     * Construct a new Minesweeper panel of given size.
     * @param w The width of the gamefield, measured in tiles.
     * @param h The height of the gamefields, measured in tiles.
     * @param prob The probability of a tile initially containing a mine, from range 0 to 100.
     */
    public Minesweeper(int w, int h, double prob) {
        this.setBackground(Color.GRAY);
        this.setPreferredSize(new Dimension(2 * XOFFSET + w * TILESIZE, 2 * YOFFSET + h * TILESIZE));
        this.gridWidth = w;
        this.gridHeight = h;
        this.prob = prob;
        this.addMouseListener(new MineListener());
        startNewGame();
    }

    // Count how many neighbours of tile (x, y) are true in the data array.
    private int countNeighbours(boolean[][] data, int x, int y) {
        int sum = 0;
        if(x > 0) {
            if(y > 0 && data[x-1][y-1]) ++sum;
            if(data[x-1][y]) ++sum;
            if(y < gridHeight-1 && data[x-1][y+1]) ++sum;
        }
        if(x < gridWidth-1) { 
            if(y > 0 && data[x+1][y-1]) ++sum;
            if(data[x+1][y]) ++sum;
            if(y < gridHeight-1 && data[x+1][y+1]) ++sum;
        }
        if(y > 0 && data[x][y-1]) ++sum; 
        if(y < gridHeight-1 && data[x][y+1]) ++sum;
        return sum;
    }
    
    /**
     * Initialize a new game with each tile initially closed. The mines will not be
     * planted on the field until the first tile opening click done by the player.
     */
    private void startNewGame() {
        gameOn = true;
        first = true;
        isMine = new boolean[gridWidth][gridHeight];
        isOpen = new boolean[gridWidth][gridHeight];
        isMarked = new boolean[gridWidth][gridHeight];
        value = new int[gridWidth][gridHeight];
        this.repaint();
    }
    
    // Each tile becomes a mine with the given probability, independent of other tiles.
    private void seedMines(int sx, int sy) {
        for(int x = 0; x < gridWidth; x++) {
            for(int y = 0; y < gridHeight; y++) {
                isMine[x][y] = ((Math.abs(x-sx) + Math.abs(y-sy) > 2) && rng.nextDouble() < prob);
            }
        }        
        for(int x = 0; x < gridWidth; x++) {
            for(int y = 0; y < gridHeight; y++) {
                value[x][y] = countNeighbours(isMine, x, y);
            }
        } 
    }

    /**
     * Render this component as it currently looks like.
     * @param g The {@code Graphics} object provided by Swing for us to draw on.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g; // convert to better Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON); // looks nicer

        g2.setFont(font);
        g2.setStroke(new BasicStroke(2.0f));
        for(int x = 0; x < gridWidth; x++) {
            for(int y = 0; y < gridHeight; y++) {
                int bx = XOFFSET + TILESIZE * x;
                int by = YOFFSET + TILESIZE * (y + 1);
                if(isOpen[x][y]) {
                    if(isMine[x][y]) {
                        g2.drawString("M", bx + NOFF, by - NOFF); 
                    }
                    else if(value[x][y] > 0) {
                        g2.drawString("" + value[x][y], bx + NOFF, by - NOFF); 
                    }
                }
                else {
                    RoundRectangle2D.Double rect = new RoundRectangle2D.Double(
                        bx + 2, by - TILESIZE + 2, TILESIZE - 4, TILESIZE - 4, 5, 5
                    ); 
                    g2.setPaint(isMarked[x][y]? Color.RED: Color.GREEN);
                    g2.fill(rect);
                    g2.setPaint(Color.BLACK);
                    g2.draw(rect);
                }
            }
        }
    }

    private class MineListener extends MouseAdapter {            
        @Override public void mousePressed(MouseEvent me) {
            if(!gameOn) { startNewGame(); return; }
            int x = (me.getX() - XOFFSET) / TILESIZE;
            int y = (me.getY() - YOFFSET) / TILESIZE;
            if(me.getButton() == MouseEvent.BUTTON1) {
                if(openTile(x, y, false)) {
                    hitMine();
                }
            }
            else {
                if(!isOpen[x][y]) { isMarked[x][y] = !isMarked[x][y]; }
            }
            for(x = 0; x < gridWidth; x++) {
                for(y = 0; y < gridHeight; y++) {
                    if(isOpen[x][y] && value[x][y] == countNeighbours(isMarked, x, y)) {
                        if(openTile(x, y, true)) { hitMine(); }
                    }
                }
            }
            repaint();
        }        
        // The other four MouseListener methods are inherited from MouseAdapter.
    }

    // Open the tile (x, y). If neighbours is true, do not open but check whether
    // some tiles can be safely opened. Returns true if hit a mine, false if not.
    private boolean openTile(int x, int y, boolean neighbours) {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
            return false; // out of bounds, do nothing
        }
        if(!neighbours) {
            if(isOpen[x][y] || isMarked[x][y]) { return false; }
            if(first) {
                first = false;
                seedMines(x, y);
            }
            isOpen[x][y] = true;
            if(isMine[x][y]) { return true; }
        }
        // Generalization of the zero cell opening makes the game more fun
        boolean result = false;
        if(value[x][y] == countNeighbours(isMarked, x, y)) {
            result = openTile(x - 1, y - 1, false);
            result |= openTile(x-1, y, false);
            result |= openTile(x-1, y+1, false);
            result |= openTile(x+1, y-1, false);
            result |= openTile(x+1, y, false);
            result |= openTile(x+1, y+1, false);
            result |= openTile(x, y-1, false);
            result |= openTile(x, y+1, false);            
        }
        return result;
    }

    private void hitMine() {
        gameOn = false;
        for(int x = 0; x < gridWidth; x++) {
            for(int y = 0; y < gridHeight; y++) {
                isOpen[x][y] = true;
            }
        }
        repaint();
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame("Minesweeper");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());
        f.add(new Minesweeper(35, 20, .23));
        f.pack();
        f.setVisible(true);        
    }
}