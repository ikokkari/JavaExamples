import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

/**
 * A Swing component to display one-dimensional cellular automata, with
 * time flowing in the vertical direction, as is customary in this genre.
 * @author Ilkka Kokkarinen
 */

public class ElementaryCellular extends JPanel {

    private static final int TOP = 35;
    private final boolean[][] state;
    
    // All sorts of Swing components with different listeners.
    private final JSlider ruleSlider;
    private final JCheckBox fredkinBox;
    private final JLabel ruleLabel;
    
    private int rule = 110;
    private final BufferedImage img;
    
    // Compute the value of the cell (x,y) based on its ancestors.
    private boolean evaluateCell(int x, int y, boolean fredkin) {
        int v = 0;
        int len = state.length;
        v += state[(x + len - 1) % len][y-1] ? 4: 0;
        v += state[x][y-1] ? 2: 0;
        v += state[(x + 1) % len][y-1] ? 1: 0;
        boolean v1 = (rule & (1 << v)) != 0;        
        if(fredkin) { // Fredkin rule
            boolean v2 = (y > 1 && state[x][y-2]);
            return (v1 || v2) && !(v1 && v2); // xor
        }
        else { // Wolfram rule
            return v1;
        }
    }
    
    /**
     * Compute the state of the entire board, given the first row.
     */
    public void evaluateBoard() {
        boolean fredkin = fredkinBox.isSelected();
        for(int y = 0; y < state[0].length; y++) {
            for(int x = 0; x < state.length; x++) {
               state[x][y] = y > 0 ? evaluateCell(x, y, fredkin) : state[x][y];
               int col = state[x][y] ? 0 : 0x00FFFFFF;
               int xx = 2 * x, yy = 2 * y;
               img.setRGB(xx, yy, col);
               img.setRGB(xx + 1, yy, col);
               img.setRGB(xx, yy + 1, col);
               img.setRGB(xx + 1, yy + 1, col);
            }
        }
        repaint();
    }
    
    /**
     * The constructor for desired width and height.
     * @param width The width of the computed image, in pixels.
     * @param height, The height of the computed image, in pixels.
     */
    public ElementaryCellular(int width, int height) {
        this.setPreferredSize(new Dimension(2 * width, 2 * height + TOP));
        img = new BufferedImage(2 * width, 2 * height, BufferedImage.TYPE_INT_BGR);
        
        fredkinBox = new JCheckBox("Fredkin");
        this.add(fredkinBox);
        fredkinBox.addItemListener(new MyFredkinListener());
        this.add(new JLabel("Rule:"));
        ruleLabel = new JLabel(rule + "");
        this.add(ruleLabel);
        ruleSlider = new JSlider(0, 255);
        this.add(ruleSlider);
        ruleSlider.addChangeListener(new MySliderListener());
        
        state = new boolean[width][height];
        state[width / 2][0] = true;
        evaluateBoard();
    }
    
    /**
     * Render this component as it currently looks like.
     * @param g The {@code Graphics} object provided by Swing for us to draw on.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, TOP, this);
    }
    
    private class MySliderListener implements ChangeListener {
        public void stateChanged(ChangeEvent ce) {
            rule = ruleSlider.getValue();
            ruleLabel.setText(rule + "");
            evaluateBoard();
        }
    }
    
    private class MyFredkinListener implements ItemListener {
        public void itemStateChanged(ItemEvent ie) {
            evaluateBoard();
        }
    }
    
    
    public static void main(String[] args) {
        JFrame f = new JFrame("Elementary Cellular Automata");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());
        f.add(new ElementaryCellular(500, 500));
        f.pack();
        f.setVisible(true);        
    }
}