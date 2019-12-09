import java.io.*;
import java.util.regex.*;

public class SomeUnixCommands {

    // Output lines that are not equal to the previous line.
    public static void uniq(Reader in, Writer out) throws IOException {
        BufferedReader br = new BufferedReader(in);
        PrintWriter pw = new PrintWriter(out);
        String prev = null;
        String line;
        while((line = br.readLine()) != null) {
            if(!line.equals(prev)) {
                pw.println(line);
            }
            prev = line;
        }
        pw.flush(); // Do not close(), otherwise System.out also closes (very bad!)
    }
    
    // Output each line reversed.
    public static void rev(Reader in, Writer out) throws IOException {
        BufferedReader br = new BufferedReader(in);
        PrintWriter pw = new PrintWriter(out);
        String line;
        while((line = br.readLine()) != null) {
            pw.println(new StringBuilder(line).reverse().toString());
        }
        pw.flush();
    }
    
    // Output only the given fields ("words") in each line.
    public static void cut(Reader in, Writer out, String sep, int[] fields) throws IOException {
        BufferedReader br = new BufferedReader(in);
        PrintWriter pw = new PrintWriter(out);
        String line;
        while((line = br.readLine()) != null) {
            if(line.length() == 0) { continue; }
            String[] split = line.split(sep);
            for(int i = 0; i < fields.length; i++) {
                if(fields[i] >= split.length) { continue; }
                if(i > 0) { pw.print(" "); }
                pw.print(split[fields[i]]);
            }
            pw.println("");
        }
        pw.flush();
    }
    
    // For demonstration purposes.
    public static void main(String[] args) throws IOException {
        
        System.out.println("Lorem ipsum:");
        Reader r = new InputStreamReader(new FileInputStream("lorem.txt"), "UTF-8");
        uniq(r, new OutputStreamWriter(System.out));
        r.close();
        
        System.out.println("Lorem ipsum reversed:");
        r = new InputStreamReader(new FileInputStream("lorem.txt"), "UTF-8");
        rev(r, new OutputStreamWriter(System.out));
        r.close();
        
        int[] fields = {1, 3, 6};
        System.out.println("Second, fourth and seventh word of each line of Lorem ipsum:");
        r = new InputStreamReader(new FileInputStream("lorem.txt"), "UTF-8");
        cut(r, new OutputStreamWriter(System.out), " ", fields);
        r.close();
    }
}