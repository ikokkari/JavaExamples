import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.awt.image.*;

/**
 * Animate and display variations of Conway's Game of Life. The game starts running as soon
 * as the object is constructed, and keeps running one step per second until the method
 * {@code terminate} is called to stop the animation timer.
 * @author Ilkka Kokkarinen
 */

public class GameOfLife extends JPanel {
    
    private boolean[][] board, newBoard;
    private final int size;
    private final String birth;
    private final String survival; // rules for birth and survival
    private final Timer t;
    private final BufferedImage img; // draw directly to BufferedImage for speed
    private final int aliveColour = Color.BLACK.getRGB(); // colour to draw living cells
    private final int deadColour = Color.WHITE.getRGB(); // colour to draw dead cells
        
    /**
     * Constructor using the original Conway's Game of Life ruleset.
     * @param size The size of the automaton, measured in cells.
     */
    public GameOfLife(int size) { this(size, "3", "23", 0.30); }
    
    private static final int MARGIN = 100;
    private static final Random rng = new Random();
    
    /**
     * Constructor for generalized variants of Conway's Game of Life.
     * @param size The size of the automaton, measured in cells.
     * @param birth The string containing the values for which a cell comes alive, e.g. "3".
     * @param survival The string containing the values for which a cell survives, e.g. "23".
     * @param prob Probability that a cell is initially alive.
     */
    public GameOfLife(int size, String birth, String survival, double prob) {
        this.size = size;
        this.birth = birth;
        this.survival = survival;
        this.img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        this.setPreferredSize(new Dimension(size, size));
        this.setBorder(BorderFactory.createRaisedBevelBorder());
        board = new boolean[size][size]; // initialize the board arrays
        newBoard = new boolean[size][size];
        for(int x = MARGIN; x < size - MARGIN; ++x) {
            for(int y = MARGIN; y < size - MARGIN; ++y) {
                board[x][y] = rng.nextDouble() < prob;
            }
        }
        this.setBackground(new Color(deadColour));
        
        // Tick every 500 milliseconds, generating an action event
        t = new Timer(500, new MyActionListener());
        t.setInitialDelay(rng.nextInt(500)); // try to avoid lockstep with multiple games
        t.start();
    }
    
    /**
     * Terminate the internal animation timer of the component so that the JVM can terminate.
     */
    public void terminate() {
        t.stop(); // stop the timer in the end
        System.out.println("Game of Life timer terminated");
    }
    
    private class MyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            for(int x = 0; x < size; ++x) {
                for(int y = 0; y < size; ++y) {
                    int sum = 0; // count the number of live variables into variable sum
                    if(x > 0) { // look at the three cells above the cell (x,y)
                        if(y > 0 && board[x-1][y-1]) ++sum;
                        if(board[x-1][y]) ++sum;
                        if(y < size-1 && board[x-1][y+1]) ++sum;
                    }
                    if(x < size-1) { // look at the three cells below the cell (x,y)
                        if(y > 0 && board[x+1][y-1]) ++sum;
                        if(board[x+1][y]) ++sum;
                        if(y < size-1 && board[x+1][y+1]) ++sum;
                    }
                    if(y > 0 && board[x][y-1]) ++sum; // look at the cell to the left
                    if(y < size-1 && board[x][y+1]) ++sum; //  look at the cell to the right
                    
                    // the cell (x,y) is alive at next board if either
                    // (1) it is alive now, and its neighbour count is among the survivals
                    // (2) is is dead now, and its neighbour count is among the births
                    newBoard[x][y] =
                      (board[x][y] && survival.indexOf('0' + sum) > -1) ||
                      (!board[x][y] && birth.indexOf('0' + sum) > -1);
                      
                    // set the pixel (x,y) of the image according to the new state of cell
                    img.setRGB(x, y, newBoard[x][y] ? aliveColour: deadColour);
                }
            }
            boolean[][] tmp = board; // swap the references to the two board arrays
            board = newBoard;
            newBoard = tmp;
            repaint();
        }
    }

    /**
     * Render this component as it currently looks like.
     * @param g The {@code Graphics} object provided by Swing for us to draw on.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, this);
    }
    
    private static final int SIZE = 700;
    private static int idx = 0;
    
    /**
     * A utility method to create a {@code JFrame} instance to display the game.
     * @param title The title of the frame.
     * @param g The {@code GameOfLife} instance to display in this frame.
     */
    public static void createFrame(String title, final GameOfLife g) {
        final JFrame f = new JFrame(title);
        f.add(g);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                g.terminate(); // first kill the timer of the GameOfLife component
                f.dispose(); // and now we can safely dispose of the frame
            }
        });
        f.pack(); f.setLocation(idx * (SIZE - 100), 100 + 50 * idx);
        ++idx;
        f.setVisible(true);
    }
    
    public static void main(String[] args) {
        // "Three or more, use a for!"
        createFrame("Conway's Game of Life", new GameOfLife(SIZE, "3", "23", 0.20));
        createFrame("Day & Night", new GameOfLife(SIZE, "3678", "34678", 0.40));
        createFrame("Mazectric", new GameOfLife(SIZE, "3", "1234", 0.05));
    }
}