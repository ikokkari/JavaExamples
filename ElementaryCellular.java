import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.event.*;
import java.awt.image.*;

/**
 * A Swing component to display one-dimensional cellular automata, with
 * time flowing to the vertical direction.
 * @author Ilkka Kokkarinen
 */
public class ElementaryCellular extends JPanel {

    private static final int TOP = 35;
    private boolean[][] state;
    
    // All sorts of Swing components with different listeners.
    private JSlider ruleSlider;
    private JCheckBox fredkinBox;
    private JLabel ruleLabel;
    private JTextField seedTF;
    
    private int rule = 110;
    private Random rng = new Random();
    private BufferedImage img;
    
    // Getters and setters for those who use this class as a tool,
    // instead of an interactive Swing component.
    /**
     * Returns the image calculated by this component.
     * @return The image of the cellular automaton.
     */
    public BufferedImage getImage() { return img; }
    
    /**
     * Programmatically select the rule to use.
     * @param rule The cellular automaton rule to use, between 0 to 255.
     */
    public void setRule(int rule) { this.rule = rule; }
    
    /**
     * Programmatically set the random number generator to use.
     * @param rng The random number generator to use.
     */
    public void setRandom(Random rng) { this.rng = rng; }
    
    // Compute the value of the cell (x,y) based on its ancestors.
    private boolean evaluateCell(int x, int y, boolean fredkin) {
        int v = 0;
        v += (x > 0 && state[x-1][y-1])? 4: 0;
        v += (state[x][y-1])? 2: 0;
        v += (x < state.length - 1 && state[x+1][y-1])? 1: 0;
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
        for(int y = 1; y < state[0].length; y++) {
            for(int x = 0; x < state.length; x++) {
               state[x][y] = evaluateCell(x, y, fredkin);
               if(state[x][y]) { img.setRGB(x, y, 0); }
               else { img.setRGB(x, y, Color.WHITE.getRGB()); }
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
        this.setPreferredSize(new Dimension(width, height + TOP));
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        
        fredkinBox = new JCheckBox("Fredkin");
        this.add(fredkinBox);
        fredkinBox.addItemListener(new MyFredkinListener());
        this.add(new JLabel("Rule:"));
        ruleLabel = new JLabel(rule + "");
        this.add(ruleLabel);
        ruleSlider = new JSlider(0, 255);
        this.add(ruleSlider);
        ruleSlider.addChangeListener(new MySliderListener());
        this.add(new JLabel("RNG seed:"));
        seedTF = new JTextField(10);
        this.add(seedTF);
        seedTF.addActionListener(new MySeedListener());
        
        state = new boolean[width][height];
        randomBoard(123);
    }
    
    private void randomBoard(int seed) {
        rng.setSeed(seed);
        for(int x = 0; x < state.length; x++) {
            state[x][0] = rng.nextBoolean();
        }
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
    
    private class MySeedListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            try {
                randomBoard(Integer.parseInt(seedTF.getText()));
            } catch(Exception e) { }
        }
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame("Elementary Cellular Automata");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());
        f.add(new ElementaryCellular(800, 800));
        f.pack();
        f.setVisible(true);        
    }
}