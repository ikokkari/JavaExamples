import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * Produces a fixed-palette colour image from original image using Floyd-Steinberg
 * dithering algorithm to compensate for the pixel colour errors. See e.g.
 * {@code http://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering}. This
 * implementation works for an arbitrary colour palette, not just black and white.
 * @author Ilkka Kokkarinen
 */

public class FloydSteinberg {
     
    // The perceptual distance between two colours. I am not an expert on colour theory,
    // so this might be pretty badly off. Use at own risk.
    private static float colourDistance(float[] c1, float[] c2) {
        float dr = c1[0] - c2[0]; // red distance
        if(dr < 0) { dr = -dr; }
        float dg = c1[1] - c2[1]; // green distance
        if(dg < 0) { dg = -dg; }
        float db = c1[2] - c2[2]; // blue distance
        if(db < 0) { db = -db; }
        // Some colour expert might know if the following formula corresponds
        // to the way that the human visual system actually works.
        return 0.21f * dr + 0.72f * dg + 0.07f * db; 
    }
    
    // Find from palette the closest colour to given colour c. Copy the answer to out.
    private static void computeClosestColour(float[] c, float[][] palette, float[] out) {
        float d = 1000; // big enough that any real distance is less than this
        for(float[] colour: palette) {
            float nd = colourDistance(c, colour);
            if(nd < d) { 
                d = nd;
                out[0] = colour[0]; out[1] = colour[1]; out[2] = colour[2];
            }
        }
    }
    
    // Given an RGB colour as a 4-byte int, convert it into given float[]. Again,
    // copy the answer to out for efficiency, instead of creating a new array.
    private static void convertRGBToFloat(int rgb, float[] out) {
        int r = (rgb >> 16) & 255; // some bitwise arithmetic I will explain in 209
        int g = (rgb >> 8) & 255;
        int b = rgb & 255;
        out[2] = b / 255.0f;
        out[1] = g / 255.0f;
        out[0] = r / 255.0f;
    }
    
    // Given an RGB colour as a float[], return that colour as a 4-byte int.
    private static int convertFloatToRGB(float[] c) {
        int b = (int)(c[2] * 255);
        int g = (int)(c[1] * 255);
        int r = (int)(c[0] * 255);
        return (r << 16) | (g << 8) | b;
    }
    
    // Compute the difference between actual colour and intended colour.
    private static void computeError(float[] intended, float[] actual, float[] out) {
        out[0] = actual[0] - intended[0];
        out[1] = actual[1] - intended[1];
        out[2] = actual[2] - intended[2];
    }
    
    // Propagate the fraction mul of error to the float[] target.
    private static void propagateError(float[] error, float[] target, float mul) {
        target[0] -= error[0] * mul;
        target[1] -= error[1] * mul;
        target[2] -= error[2] * mul;
    }
    
    /** 
     * Compute the Floyd-Steinberg dithering for the given image and palette.
     * @param orig The original RGB image to be dithered.
     * @param palette The colours allowed to appear in the result image.
     */
    public static BufferedImage dither(BufferedImage orig, Color[] palette) {
        BufferedImage img = new BufferedImage(
            orig.getWidth(), orig.getHeight(), BufferedImage.TYPE_INT_RGB
        );
        
        // Some arrays to use as workspace for the current pixel.
        float[] closest = new float[3];
        float[] error = new float[3];
        // Extract the colour component of each colour in the palette.
        float[][] paletteC = new float[palette.length][3];
        for(int i = 0; i < palette.length; i++) {
            palette[i].getColorComponents(paletteC[i]);
        }
        
        // The Floyd-Steinberg algorithm needs to store only two consecutive rows of pixels
        // using higher resolution information for each colour.
        float[][] currLine = new float[orig.getWidth() + 1][3];
        float[][] nextLine = new float[orig.getWidth() + 1][3];
        // Initialize the current (first) and next (second) row of pixel colour data.
        for(int x = 0; x < img.getWidth(); x++) {
            convertRGBToFloat(orig.getRGB(x, 0), currLine[x]);
            convertRGBToFloat(orig.getRGB(x, 1), nextLine[x]);
        }
        
        // Process the image one row at the time.
        for(int y = 0; y < img.getHeight(); y++) {
            // Process each row from left to right.
            for(int x = 0; x < img.getWidth(); x++) {
                // Find the closest colour in the palette to the current pixel's colour.
                computeClosestColour(currLine[x], paletteC, closest);
                // Put that into the result image.
                img.setRGB(x, y, convertFloatToRGB(closest));
                // Compute the difference between the intended and actual colour.
                computeError(currLine[x], closest, error); //result in array: error
                // Propagage the previous error to the four neighbouring pixels.
                propagateError(error, currLine[x+1], 7.0f / 16); // E
                if(x > 0) { propagateError(error, nextLine[x-1], 3.0f / 16); } // SW
                propagateError(error, nextLine[x], 5.0f / 16); // S
                propagateError(error, nextLine[x+1], 1.0f / 16); // SE
            }
            // After processing the current line, copy next line data to current line...
            System.arraycopy(nextLine, 0, currLine, 0, nextLine.length);
            // ... and read the next line data from the original image to nextLine.
            if(y + 2 < orig.getHeight()) { // ...assuming that there exists a next line...
                for(int x = 0; x < img.getWidth(); x++) {
                    convertRGBToFloat(orig.getRGB(x, y + 2), nextLine[x]);
                }
            }
        }
        // All done!
        return img;
    }
    
    /**
     * A utility method to create a {@code JPanel} instance customized to render the
     * dithered version of the given {@code BufferedImage}.
     * @param img The image to dither and display.
     * @param palette The palette of colours to used in dithering.
     * @param toolTip The tooltip displayed on this component.
     */
    public static JPanel createPanel(final BufferedImage img, Color[] palette, final String toolTip) {
        final BufferedImage result = dither(img, palette);
        class FSPanel extends JPanel {
            public FSPanel() {
                this.setPreferredSize(new Dimension(result.getWidth(), result.getHeight()));
                this.setToolTipText(toolTip);
            }
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(result, 0, 0, this);
            }
        }
        return new FSPanel();
    }
    
    public static void main(String[] args) {
        Image coffee = Toolkit.getDefaultToolkit().getImage("coffee.jpg");
        coffee = coffee.getScaledInstance(600, 375, Image.SCALE_SMOOTH);
        MediaTracker m = new MediaTracker(new JPanel());
        m.addImage(coffee, 0);
        try { m.waitForAll(); } catch(InterruptedException ignored) { }
        
        // The trivial way to convert an arbitrary Image to BufferedImage...
        BufferedImage img = new BufferedImage(
            coffee.getWidth(null), coffee.getHeight(null), BufferedImage.TYPE_INT_RGB
        );
        img.getGraphics().drawImage(coffee, 0, 0, null); // ... is to draw it inside one
        
        // Demonstrate the Floyd-Steinberg algorithm with some simple restricted palettes.
        
        JFrame f = new JFrame("Floyd-Steinberg Dithering Demo");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new GridLayout(2, 3));
        
        Color[][] palettes = {
            { Color.BLUE, Color.WHITE, Color.BLACK, Color.GREEN },
            { Color.RED, Color.YELLOW, Color.BLUE },
            { Color.CYAN, Color.GREEN, Color.PINK },
            { Color.BLACK, Color.RED, Color.BLUE, Color.GREEN },
            { Color.BLACK, Color.WHITE },
            { Color.BLACK, new Color(64, 64, 64), new Color(128,128,128),
              new Color(192, 192, 192), Color.WHITE }
        };
        String[] toolTips = {
            "Blue, white, black, green", "Red, yellow, blue", "Cyan, green, pink",
            "Primary colors", "Binary black and white", "5 levels of gray"
        };
        for(int i = 0; i < palettes.length; i++) {
            f.add(createPanel(img, palettes[i], toolTips[i]));
        }
        
        f.pack();
        f.setVisible(true);        
    }
}