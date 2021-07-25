import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;
import java.io.*; // file stuff
import javax.imageio.ImageIO; // image read and write from file

/**
 * A Swing example class that demonstrate how to download, render and modify images.
 * @author Ilkka Kokkarinen
 */

public class ImageDemo extends JPanel {
    private Image coffee;
    private Image flappy;
    private final BufferedImage bimg1;
    private final BufferedImage bimg2;
    private final Random rng = new Random();
    
    // Utility method to pack three RGB components into bytes of single int.
    private int convertToRGB(int r, int g, int b) {
        return (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
    }
    
    public ImageDemo() throws IOException {
        // We can read an image from gif, jpeg, png file...
        coffee = ImageIO.read(new File("coffee.jpg"));
        flappy = ImageIO.read(new File("flappy.png"));
        // Take a wild guess which ImageIO method would then write an image to a file.
        
        // Images can be easily scaled to desired size.
        coffee = coffee.getScaledInstance(800, 600, Image.SCALE_AREA_AVERAGING);
        Image lilcoffee = coffee.getScaledInstance(50, 50, Image.SCALE_AREA_AVERAGING);
        flappy = flappy.getScaledInstance(25, 25, Image.SCALE_AREA_AVERAGING);
        
        // We can also create our own BufferedImage and draw stuff in it the
        // same way as you draw in paintComponent method, or read and write
        // the individual pixels directly.
        bimg1 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        bimg2 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < 256; x++) {
            for(int y = 0; y < 256; y++) {
                bimg1.setRGB(x, y, convertToRGB(x, 256 - y, (x + y) % 256));
                bimg2.setRGB(x, y, convertToRGB((2*x+y) % 256, (x + y) % 256, y));
            }
        }
        // You can ask any image for a Graphics object to draw into that image.
        Graphics2D g2 = (Graphics2D)bimg1.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);       
        g2.setStroke(new BasicStroke(5.0f));
        g2.setPaint(Color.BLACK);
        g2.draw(new Ellipse2D.Double(64, 64, 128, 128));
        
        // An image can also be used as ImageIcon shown inside JLabel, JButton or similar.
        this.setLayout(new FlowLayout());
        JButton button = new JButton(new ImageIcon(lilcoffee));
        this.add(button);
        button.addActionListener(new ButtonListener());
        this.setPreferredSize(
            new Dimension(coffee.getWidth(this) + 256, coffee.getHeight(this))
        );
    }
    
    private class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) { repaint(); }
    }
    
    /**
     * Render this component as it currently looks like.
     * @param g The {@code Graphics} object provided by Swing for us to draw on.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;        
        g2.drawImage(coffee, 0, 0, this);
        g2.drawImage(bimg1, coffee.getWidth(this), 0, this);
        g2.drawImage(bimg2, coffee.getWidth(this), 256, this);
        for(int i = 0; i < 20; i++) {
            // With PNG images, the transparency information is stored alongside the colours.
            g2.drawImage(flappy, rng.nextInt(this.getWidth()), rng.nextInt(this.getHeight()), this);
        }
    }
   
    public static void main(String[] args) throws IOException {
        JFrame f = new JFrame("Imagedemo");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());
        f.add(new ImageDemo());
        f.pack();
        f.setVisible(true);                           
    }
}