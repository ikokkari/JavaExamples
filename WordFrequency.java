import java.io.*;
import java.util.*;

public class WordFrequency {
    
    // To run this program, change the FILENAME to some text file that you have.
    private static final String FILENAME = "warandpeace.txt";

    private static final String[][] replacements = new String[][]{
            {"doesn't", "does not"},
            {"don't", "do not"},
            {"you're", "you are"},
            {"i'm", "i am"},
            {"we're", "we are"},
            {"they're", "they are"},
            {"won't", "will not"},
            {"can't", "can not"},
            {"shan't", "shall not"},
            {"shouldn't", "should not"},
            {"mustn't", "must not"},
            {"aren't", "are not"}
    };

    public static Map<String, Integer> wordFrequencies(Scanner s) {
        Map<String, Integer> freqs = new HashMap<>();
        while(s.hasNextLine()) {
            String line = s.nextLine().trim().toLowerCase();
            for(String[] repl: replacements) {
                line = line.replaceAll(repl[0], repl[1]);
            }
            line = line.replaceAll("'s\\b", ""); // In regex, \b is word boundary marker
            line = line.replaceAll("'ll\\b", " will");
            line = line.replaceAll("'t\\b", "");
            String wordSeparators = "[^a-z]+";
            for(String word: line.split(wordSeparators)) {
                // Lines that start with the quote character will end up having an
                // empty word at the front of the split line array. Thus this check.
                if(word.length() != 0) {
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);                
                }
            }
        }
        return freqs;
    }
    
    // To make the later output nicely divided into lines of given length. 
    private static class LinePrinter {
        private final int lineMax; // Maximum desired length of a line.
        private int lineLen; // Length of the current line.
        private final PrintWriter out; // Where to direct the printed characters.
        private boolean firstInLine = true; // Is the current word first in this line?
        public LinePrinter(PrintWriter out, int lineMax) {
            this.out = out;
            this.lineMax = lineMax;
        }
        public void printWord(String word) {
            if(lineLen + word.length() + (firstInLine? 0: 1) > lineMax) {
                lineLen = 0;
                out.println("");
                firstInLine = true;
            }
            if(!firstInLine) { out.print(" "); lineLen++; }
            out.print(word);
            firstInLine = false;
            lineLen += word.length();
        }
        public void lineBreak() {
            if(lineLen > 0) {
                out.println("");
                out.flush();
                lineLen = 0;
                firstInLine = true;
            }
        }
    }
    
    // For demonstration purposes, some word frequencies from "War and Peace".
    public static void main(String[] args) throws IOException {
        Map<String, Integer> freqs = 
            wordFrequencies(new Scanner(new File(FILENAME)));
        System.out.println("Found " + freqs.size() + " distinct words.\n");
        System.out.println("Some occurrence counts are: ");
        String[] words = {
            "chicken", "prince", "Russia", "train", "I", "supercalifragilisticexpialidocius"
        };
        for(String word: words) {
            word = word.toLowerCase();
            System.out.println(word + ": " + (freqs.getOrDefault(word, 0)));
        }
        
        // Custom comparator to compare strings by their frequency in the map, resolving cases
        // for equal frequency using the ordinary string comparison as secondary criterion.
        class FreqComparator implements Comparator<String> {
            public int compare(String word1, String word2) {
                int f2 = freqs.get(word2);
                int f1 = freqs.get(word1);
                if(f2 != f1) { return f2 - f1; }
                return word2.compareTo(word1);
            }
        }
        
        // Create an arraylist of words that we can sort them.
        ArrayList<String> wordList = new ArrayList<>(freqs.keySet());
        // Sort the arraylist using our frequency comparator.
        wordList.sort(new FreqComparator());
        System.out.println("\nThe three hundred most frequent words of 'War and Peace' are:\n");
        LinePrinter lp = new LinePrinter(new PrintWriter(System.out), 80);
        for(int i = 0; i < 300; i++) {
            String word = wordList.get(i);
            lp.printWord(word + " (" + freqs.get(word) + ")");
        }
        lp.lineBreak();
        System.out.println("\nHere are the words that occur only once in 'War and Peace':\n");
        int i = wordList.size() - 1;
        while(true) {
            String word = wordList.get(i--);
            if(freqs.get(word) > 1) { break; }
            lp.printWord(word);
        }
        lp.lineBreak();
    }
}