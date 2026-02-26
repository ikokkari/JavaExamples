import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A simple gzip compressor that demonstrates IO stream decoration: wrapping
 * one stream inside another to add behaviour (buffering, compression) without
 * changing the client code. Updated for Java 21+ with NIO Path/Files API.
 * @author Ilkka Kokkarinen
 */
public class GZip {

    /**
     * Compress a file using GZIP, verify the result, then delete the original.
     * @param filename The path to the file to compress.
     * @throws IOException if reading, writing, or verification fails.
     */
    private static void gzipFile(String filename) throws IOException {
        // java.nio.file.Path is the modern replacement for java.io.File.
        // It works with the Files utility class for all file operations.
        Path original = Path.of(filename);
        Path compressed = Path.of(filename + ".gz");

        // --- Step 1: Compress ---
        // Flat try-with-resources (Java 9+): multiple resources in one try header,
        // no nesting needed. Resources are closed in reverse declaration order.
        //
        // The stream decoration chain from inside out:
        //   FileOutputStream  → raw bytes to disk
        //   BufferedOutputStream → batches small writes into larger OS calls
        //   GZIPOutputStream  → compresses bytes before passing them down
        //
        // InputStream.transferTo (Java 9+) replaces the manual read/write loop.
        // It bulk-copies all bytes from source to target efficiently.
        try (InputStream source = new BufferedInputStream(Files.newInputStream(original));
             OutputStream target = new GZIPOutputStream(
                     new BufferedOutputStream(Files.newOutputStream(compressed)))) {
            source.transferTo(target);
        }

        // --- Step 2: Verify ---
        // Before deleting the original, ensure compression was successful.
        if (!Files.exists(compressed) || Files.size(compressed) == 0) {
            throw new IOException("Unable to create compressed file");
        }

        // Decompress and compare byte-by-byte against the original.
        // Files.mismatch (Java 12+) compares two regular files efficiently,
        // but here one stream is a GZIPInputStream (not a regular file), so
        // we fall back to a manual comparison loop.
        try (InputStream originalStream = new BufferedInputStream(Files.newInputStream(original));
             InputStream decompressed = new BufferedInputStream(
                     new GZIPInputStream(Files.newInputStream(compressed)))) {
            int b1, b2;
            do {
                b1 = originalStream.read();
                b2 = decompressed.read();
                if (b1 != b2) {
                    Files.deleteIfExists(compressed);
                    throw new IOException("Compressed content does not match original");
                }
            } while (b1 != -1);
        }

        // Verification passed — safe to delete the original.
        Files.delete(original);
        System.out.println("Compressed: " + original + " → " + compressed);
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: java GZip <file1> [file2] ...");
            System.exit(1);
        }
        for (String filename : args) {
            gzipFile(filename);
        }
    }
}