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
        // The policy to name the resulting compressed version of the file.
        String resultFileName = filename + ".gz";

        try (InputStream source = new FileInputStream(original)) {
            try (OutputStream target = new GZIPOutputStream(new FileOutputStream(resultFileName))) {
                int b = source.read();
                while (b != -1) {
                    target.write(b); // Read and write bytes in lockstep
                    b = source.read();
                }
            }
        }
        // try-with-resources silently generates the finally-blocks to close both streams.
        
        // Before deleting the original, let's at least ensure that compression was successful.
        File compressedFile = new File(resultFileName);
        if(!(compressedFile.exists() && compressedFile.length() > 0)) {
            throw new IOException("Unable to create compressed file");
        }
        try (InputStream originalStream = new FileInputStream(original)) {
            try (InputStream compressedStream = new GZIPInputStream(new FileInputStream(compressedFile))) {
                int b1, b2;
                do {
                    b1 = originalStream.read();
                    b2 = compressedStream.read();
                    if (b1 != b2) {
                        compressedFile.delete();
                        throw new IOException("Compression result not equal to original");
                    }
                } while(b1 > -1);
                // Having come this far, we are willing to put our head on the chopping block.
                original.delete();
            }
        }
    }
        
    public static void main(String[] args) throws IOException {
        for(String filename: args) {
            gzipFile(filename);
        }
    }
}