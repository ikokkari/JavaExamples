import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.util.function.*;

// First demonstration of lambdas in Java 8.

public class ImageLoops {

    // Objects of class Color represent individual colours. However, for low-level
    // drawing, the colours are represented and encoded as four-byte integers.
    private static final int WHITE = Color.WHITE.getRGB();
    
    // Define the size and padding as named constants.
    private static final int SIZE = 400;
    private static final int PAD = 40;
    private static final int BOX = 25;

    // Render the pixels (x, y) that satisfy the predicate given as parameter.
    public static BufferedImage computeImage(int w, int h, BiPredicate<Integer, Integer> pred) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < w; x++) {
            for(int y = 0; y < h; y++) {
                if(pred.test(x, y)) {
                    img.setRGB(x, y, WHITE);
                }
            }
        }
        return img;
    }

    // A main method to display the images inside a panel inside a JFrame window. 
    public static void main(String[] args) {
        JFrame f = new JFrame("Some images made with lambda expressions");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());

        class ImagePanel extends JPanel {
            private BufferedImage[] imgs = new BufferedImage[6];
            public ImagePanel() {
                this.setPreferredSize(new Dimension(3*SIZE + 4*PAD, 2*SIZE + 3*PAD));
                this.setBackground(new Color(30, 30, 60));
                imgs[0] = computeImage(SIZE, SIZE,
                    (x, y) -> (x / BOX) % 2 != (y / BOX) % 2
                );
                imgs[1] = computeImage(SIZE, SIZE,
                    (x, y) -> (3*x + 2*y) / (BOX * 3) % 2 == 0
                );
                imgs[2] = computeImage(SIZE, SIZE,
                    (x, y) -> (x / BOX) % 2 == 0 && ((y + 3*x/BOX) / BOX) % 2 == 0
                );
                imgs[3] = computeImage(SIZE, SIZE, (x, y) ->
                  (x + y) / BOX % 2 == 0 &&
                  (x >= y ? (x - y) / BOX % 2 == 0 : (y - x) / BOX % 2 == 1)
                );
                imgs[4] = computeImage(SIZE, SIZE, (x, y) -> {
                    int xd = Math.abs(SIZE/2 - 1 - x) / BOX;
                    int yd = Math.abs(SIZE/2 - 1 - y) / BOX;
                    int d = xd > yd? xd: yd; 
                    return (d % 2) == 0;
                });
                imgs[5] = computeImage(SIZE, SIZE, (x, y) -> {
                    int c = x*x-2*(x|y)+y*y + (x^y);
                    c = c % 256;
                    return c < -127 || c > 127;
                });
            }

            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                for(int i = 0; i < imgs.length; i++) {
                    if(imgs[i] != null) {
                        int row = i / 3;
                        int col = i % 3;
                        g.drawImage(imgs[i], PAD + (PAD + SIZE) * col, PAD + (PAD + SIZE) * row, this);
                    }
                }
            }
        }

        f.add(new ImagePanel());
        f.pack();
        f.setVisible(true);        
    }
}