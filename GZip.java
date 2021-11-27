import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZip {

    private static void gzipFile(String filename) throws IOException {
        // Create the compressed version of the original file.
        File original = new File(filename);
        try(InputStream i1 = new FileInputStream(original);
            OutputStream o = new GZIPOutputStream(new FileOutputStream(filename + ".gz"))) {
            int b = i1.read();
            while(b != -1) {
                o.write(b); // Read and write bytes in lockstep
                b = i1.read();
            }
        }
        // try-with-resources autogenerates the finally-block to close the streams.
        
        // Before deleting the original, ensure that compression was successful.
        File zipped = new File(filename + ".gz");
        if(!(zipped.exists() && zipped.length() > 0)) { return; }
        try (InputStream i1 = new FileInputStream(original);
             InputStream i2 = new GZIPInputStream(new FileInputStream(zipped))) {
            int b1, b2;
            do {
                b1 = i1.read();
                b2 = i2.read();
                if(b1 != b2) {
                    zipped.delete();
                    return;
                }
            } while(b1 > -1);
            original.delete();
        }
    }
        
    public static void main(String[] args) throws IOException {
        for(String filename: args) {
            gzipFile(filename);
        }
    }
}