import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import java.io.*; // file
import javax.imageio.ImageIO; // image read and write from file

// Demonstration of Image processing using ImageFilter subtypes to perform image
// operations such as colour conversion, cropping and rotation on images.

public class ImageOpsDemo {
    
    // An example of filtering an image with a subtype of RGBImageFilter.
    public static Image scrambleRGB(Image img, final int xs, final int ys) {
        class Scramble extends RGBImageFilter {
            public int filterRGB(int x, int y, int rgb) {
                // Extract the individual rgb values from the packed int.
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int tmp;
                // Scramble the colours depending on parity of scaled x and y.
                switch((x/xs + y/ys) % 4) {
                    case 0: tmp = r; r = g; g = tmp; break;
                    case 1: tmp = r; r = b; b = tmp; break;
                    case 2: tmp = b; b = g; g = tmp; break;
                    default:
                }
                // Pack three rgb bytes into a single int.
                return (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        
        // The magic incantations to apply an ImageFilter to an image.
        ImageFilter filter = new Scramble();
        ImageProducer producer = new FilteredImageSource(img.getSource(), filter);
        Image image = Toolkit.getDefaultToolkit().createImage(producer);
        return image;
    }
    
    public static Image rotate(Image img, int steps) {
        // Rotations are special case of affine transforms.
        AffineTransform rot90 = AffineTransform.getRotateInstance(
            steps * Math.PI / 2, img.getWidth(null) / 2, img.getHeight(null) / 2
        );
        // Create a BufferedImageOp from that transformation.
        BufferedImageOp aop = new AffineTransformOp(rot90, AffineTransformOp.TYPE_BICUBIC);
        // Build a filter around that image operation, and proceed as before.
        ImageFilter filter = new BufferedImageFilter(aop);
        
        ImageProducer producer = new FilteredImageSource(img.getSource(), filter);
        Image result = Toolkit.getDefaultToolkit().createImage(producer);
        MediaTracker m = new MediaTracker(new JPanel());
        m.addImage(result, 0);
        try { m.waitForAll(); } catch(InterruptedException e) { }
        return result;
    }
    
    // Many image processing operations can be rephrased as image convolutions
    // where different kernels achieve different ends. See the Wikipedia page
    // https://en.wikipedia.org/wiki/Kernel_(image_processing) for a taste.
    // Too bad this library does not have "deconvolution" like Wolfram...
    
    public static Image boxBlur(Image img, int r) {
        // The kernel of convolution is a rectangle of weights.
        r = 2*r + 1;
        int rr = r * r;
        float n = 1.0f / rr;
        float[] a = new float[rr];
        Arrays.fill(a, n);
        Kernel kernel = new Kernel(r, r, a);
        // A convolution is determined by its kernel.
        BufferedImageOp edge = new ConvolveOp(kernel);
        ImageFilter filter = new BufferedImageFilter(edge);
        
        ImageProducer producer = new FilteredImageSource(img.getSource(), filter);
        Image result = Toolkit.getDefaultToolkit().createImage(producer);
        MediaTracker m = new MediaTracker(new JPanel());
        m.addImage(result, 0);
        try { m.waitForAll(); } catch(InterruptedException e) { }
        return result;
    }
    
    // A little utility class to display images as Swing components.
    private static class ImagePanel extends JPanel {
        private Image img;
        private String toolTip;
        public ImagePanel(Image img, String toolTip) {
            this.img = img; this.toolTip = toolTip;
            this.setToolTipText(toolTip);
            this.setPreferredSize(new Dimension(img.getWidth(this), img.getHeight(this)));
        }
        public void paintComponent(Graphics g) {
            g.drawImage(img, 0, 0, this);
        }
    }
    
    public static void main(String[] args) throws IOException {
        // Read the image from the file.
        Image coffee = ImageIO.read(new File("coffee.jpg"));
        // Create a smaller version of the image.
        coffee = coffee.getScaledInstance(800, 600, Image.SCALE_SMOOTH);
        // Crop into a square area.
        ImageFilter cf = new CropImageFilter(100, 0, 600, 600);
        ImageProducer producer = new FilteredImageSource(coffee.getSource(), cf);
        coffee = Toolkit.getDefaultToolkit().createImage(producer);
        
        JFrame f = new JFrame("Image Operations Demo");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new FlowLayout());
        f.add(new ImagePanel(scrambleRGB(coffee, 7, 13), "Scramble"));
        f.add(new ImagePanel(rotate(coffee, 1), "Rotate 90 degrees right"));
        f.add(new ImagePanel(boxBlur(coffee, 5), "Box blur"));
        f.pack();
        f.setVisible(true);                           
    }
}
