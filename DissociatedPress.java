import java.util.*;
import java.io.*;

public class DissociatedPress {
    
    // To run this program, replace FILENAME with some text file that you have.
    private static final String FILENAME = "warandpeace.txt";
    
    private static final Random rng = new Random();
    
    private Map<String, String> followMap = new HashMap<String, String>();
    private int maxPat, maxFollow;
    private String pattern;

    public DissociatedPress() { this(" ", 7, 200); }
    
    /**
     * The constructor for DissociatedPress.
     * @param pattern The initial pattern.
     * @param maxPat The maximum length for the current pattern.
     * @param maxFollow The maximum length for the follow string for any pattern.
     */
    public DissociatedPress(String pattern, int maxPat, int maxFollow) {
        this.pattern = pattern; this.maxPat = maxPat; this.maxFollow = maxFollow;
    }
    
    /**
     * Process the next character of the input text and update the followmap for all
     * the suffixes of the current pattern.
     * @param next The next character of input to process.
     */
    public void processChar(char next) {
        // Add the next character to follow strings of every suffix of current pattern. 
        for(int i = 0; i < pattern.length(); i++) {
            String partPat = pattern.substring(i);
            String follow = followMap.getOrDefault(partPat, "");
            if(follow.length() < maxFollow) {
                followMap.put(partPat, follow + next);
            }
        }
        pattern += next;
        if(pattern.length() > maxPat) { pattern = pattern.substring(1); }
    }
    
    /**
     * Use the Dissociated Press pattern map to emit a random character based on the
     * current pattern, and update the pattern accordingly.
     * @param maxEmitPat The maximum pattern length to use in emission, regardless of the
     * patterns stored in the followmap.
     * @return A randomly chosen character from the follow string of the current pattern.
     */
    public char nextChar(int maxEmitPat) {
        while(pattern.length() > maxEmitPat) {
            pattern = pattern.substring(1);
        }
        while(pattern.length() > 0) {
            String follow = followMap.getOrDefault(pattern, "");
            if(follow.length() > 0) {
                char next = follow.charAt(rng.nextInt(follow.length()));
                pattern += next;
                if(pattern.length() > maxPat) { pattern = pattern.substring(1); }
                return next;
            }
            pattern = pattern.substring(1);
        }
        return '$';
    }
    
    public void outputInfo() {
        String characters = "";
        int[] patCount = new int[maxPat + 1];
        int[] followCount = new int[maxPat + 1];
        int[] saturated = new int[maxPat + 1];
        for(String pat: followMap.keySet()) {
            patCount[pat.length()]++;
            int fl = followMap.get(pat).length();
            followCount[pat.length()] += fl;
            if(fl == maxFollow) { saturated[pat.length()]++; }
            if(pat.length() == 1) { characters += pat; }
        }
        System.out.println("Characters found in data are:\n" + characters);
        System.out.println("\nLength\tTotal\tSaturated\tAverage");
        for(int patLen = 1; patLen <= maxPat; patLen++) {
            System.out.printf("%d\t%d\t%d\t\t%.3f\n", patLen, patCount[patLen],
            saturated[patLen], followCount[patLen] / (double)patCount[patLen]);
        }
        System.out.println("\n");
    }
    
    /**
     * For demonstration purposes, read in the text file "War and Peace" to be used to
     * build up the followmap. Demonstrate the behaviour of the Dissociated Press technique
     * to produce sample random test for possible pattern lengths from 1 to 6.
     */
    public static void main(String[] args) throws IOException {
        Scanner wap = new Scanner(new File(FILENAME));
        DissociatedPress dp = new DissociatedPress();
        while(wap.hasNextLine()) {
            String line = wap.nextLine(); // nextLine() strips away newline character
            for(int i = 0; i < line.length(); i++) {
                dp.processChar(line.charAt(i));
            }
            dp.processChar(' '); // newline works as whitespace for this analysis
        }
        wap.close();
        dp.outputInfo();
        for(int maxEmitPat = 1; maxEmitPat < 8; maxEmitPat++) {
            if(maxEmitPat > 1) { System.out.println("\n---\n"); }
            System.out.println("Emit pattern length " + maxEmitPat + ".");
            int currLineLen = 0, linesRemain = 10;
            char prev = ' ';
            while(linesRemain > 0) {
                char next = dp.nextChar(maxEmitPat);
                if(!(Character.isWhitespace(next) && Character.isWhitespace(prev))) {
                    if(currLineLen++ > 60 && Character.isWhitespace(next)) {
                        next = '\n'; currLineLen = 0; linesRemain--;
                    }
                    System.out.print(next);
                }
                prev = next;
            }
        }
    }
}