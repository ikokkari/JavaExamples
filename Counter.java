import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * A Swing example component that contains a {@code JButton} that the user can press,
 * and a {@code JLabel} that displays a count of how many times that button has been pressed.
 * @author Ilkka Kokkarinen
 */

public class Counter extends JPanel {

    private int count = 0;
    
    /**
     * Basic constructor for this class.
     */
    public Counter() {
        this.setPreferredSize(new Dimension(200,50));
        // Every swing component can have a decorative border.
        this.setBorder(BorderFactory.createLineBorder(Color.RED));
        
        // When a local variable is accessed from a local nested class, the variable has
        // to be declared final. (Technical explanation why is quite long even for 209.)
        final JLabel lab = new JLabel("0");
        lab.setBorder(BorderFactory.createEtchedBorder());
        this.add(lab);
        
        JButton but = new JButton("Press me");
        but.setToolTipText("Click to increase the count");
        this.add(but);
        
        // A local class defined and used only inside this method.
        class MyActionListener implements ActionListener {
            // The action listener nested class must have this exact method.
            public void actionPerformed(ActionEvent ae) {
                // This code of this method gets executed each time the button is pressed.
                // As you see, nested class methods can modify the fields of the outer
                // class object directly without any special syntax...
                count++;
                // ... and use the final local variables of the method we are in.
                lab.setText(count + "");
            }    
        }
        but.addActionListener(new MyActionListener());
    }

    /**
     * Create a {@code JFrame} with three separate {@code Counter} instances inside it.
     */
    public static void main(String[] args) {
        JFrame f = new JFrame("Counter demo");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());
        // Let's add three separate Counters, just to show what happens.
        f.add(new Counter());
        f.add(new Counter());
        f.add(new Counter());        
        f.pack();
        f.setVisible(true);        
    }    
}