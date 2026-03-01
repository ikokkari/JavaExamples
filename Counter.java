import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * A simple Swing component that counts button presses — the "Hello World"
 * of event-driven GUI programming.
 * <p>
 * This example demonstrates:
 * <ul>
 *   <li>Custom JPanel subclass with internal layout</li>
 *   <li>Event listeners — shown in three evolutionary styles:
 *       local class (old), anonymous class (less old), and lambda (modern)</li>
 *   <li>How nested code can access outer fields and effectively final locals</li>
 *   <li>GridLayout for arranging multiple independent components</li>
 * </ul>
 *
 * Updated for modern Java with lambda event handling and Swing best practices.
 *
 * @author Ilkka Kokkarinen
 */
public class Counter extends JPanel {

    private static final Font BUTTON_FONT = new Font("SansSerif", Font.PLAIN, 28);
    private static final Font LABEL_FONT = new Font("Monospaced", Font.BOLD, 50);

    private int count = 0;

    // -----------------------------------------------------------------------
    // Construction — wire up the label, button, and event listener.
    // -----------------------------------------------------------------------

    public Counter() {
        setPreferredSize(new Dimension(250, 80));
        // Every Swing component can have an arbitrarily festive border.
        setBorder(BorderFactory.createLoweredSoftBevelBorder());

        // BoxLayout arranges children along a single axis (here, left to right).
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        // The count display label.
        var countLabel = new JLabel("0");
        countLabel.setBorder(BorderFactory.createEtchedBorder());
        countLabel.setFont(LABEL_FONT);
        add(countLabel);

        // The increment button.
        var incrementButton = new JButton("Press me");
        incrementButton.setFont(BUTTON_FONT);
        incrementButton.setToolTipText("Click to increase the count");
        add(incrementButton);

        // --- THREE WAYS TO ATTACH AN EVENT LISTENER ---
        //
        // Style 1 (Java 1.1+): A local class that implements ActionListener.
        // Verbose, but makes the mechanism explicit. The local class can access
        // the enclosing method's effectively final local variables (countLabel)
        // and the outer object's fields (count).
        //
        //   class IncrementListener implements ActionListener {
        //       @Override
        //       public void actionPerformed(ActionEvent event) {
        //           count++;
        //           countLabel.setText(Integer.toString(count));
        //       }
        //   }
        //   incrementButton.addActionListener(new IncrementListener());
        //
        // Style 2 (also Java 1.1+): An anonymous class — same thing, but
        // without bothering to name the class.
        //
        //   incrementButton.addActionListener(new ActionListener() {
        //       @Override
        //       public void actionPerformed(ActionEvent event) {
        //           count++;
        //           countLabel.setText(Integer.toString(count));
        //       }
        //   });
        //
        // Style 3 (Java 8+): A lambda expression. Since ActionListener is a
        // functional interface (exactly one abstract method), the compiler can
        // infer everything from this compact syntax. This is the modern idiom.

        incrementButton.addActionListener(event -> {
            count++;
            countLabel.setText(Integer.toString(count));
        });
    }

    // -----------------------------------------------------------------------
    // Main — create a grid of independent Counter instances.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        // Swing components must be created on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("Counter Demo");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // GridLayout arranges components in a uniform grid. Every cell
            // gets the same size, regardless of its content — which is why
            // the text field in the last cell will be stretched to match.
            int gridSize = 4;
            frame.setLayout(new GridLayout(gridSize, gridSize));

            for (int i = 0; i < gridSize * gridSize - 1; i++) {
                frame.add(new Counter());
            }
            // Make the bottom-right cell something different, to demonstrate
            // that GridLayout forces uniform sizing on all its children.
            var textField = new JTextField("Look at me, I am a text field");
            frame.add(textField);

            frame.pack();
            frame.setVisible(true);
        });
    }
}