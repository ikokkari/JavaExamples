import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class Counter extends JPanel {

    private static final Font buttonFont = new Font("Arial", Font.PLAIN, 28);
    private int count = 0;
    
    public Counter() {
        this.setPreferredSize(new Dimension(300, 80));
        // Every Swing component can have an arbitrarily festive and decorative border.
        this.setBorder(BorderFactory.createSoftBevelBorder(
            BevelBorder.RAISED, Color.BLUE, Color.CYAN, Color.RED, Color.YELLOW)
        );
        
        // When a local variable is accessed from a local nested class, that variable has
        // to be effectively final; that is, either declared final, or never assigned to
        // after initialization.
        JLabel lab = new JLabel("0");
        lab.setBorder(BorderFactory.createEtchedBorder());
        lab.setFont(new Font("Courier", Font.BOLD, 50));
        this.add(lab);
        
        JButton but = new JButton("Press me");
        but.setFont(buttonFont);
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
                // ... and use the effectively final local variables of the method.
                lab.setText(count + "");
            }    
        }
        but.addActionListener(new MyActionListener());
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Counter demo");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Let's add sixteen separate Counter instances, just to show what happens.
        int n = 4; 
        f.setLayout(new GridLayout(n, n));
        for(int i = 0; i < n*n; i++) {
            f.add(new Counter());
        }
        f.pack();
        f.setVisible(true);        
    }    
}