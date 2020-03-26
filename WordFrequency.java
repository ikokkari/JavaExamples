import java.io.*;
import java.util.*;

public class WordFrequency {
    
    private static String[][] replacements = {
      { "doesn't", "does not" },
      { "don't", "do not" },
      { "you're", "you are" },
      { "i'm", "i am" },
      { "we're", "we are" },
      { "they're", "they are" },
      { "won't", "will not" },
      { "can't", "can not" },
      { "shan't", "shall not" },
      { "shouldn't", "should not" },
      { "mustn't", "must not" }
    };
    
    private static String wordSeparators = "[^a-z]+";
    
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
            for(String word: line.split(wordSeparators)) {
                if(word.length() != 0) {
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);                
                }
            }
        }
        return freqs;
    }
    
    // To make the later output nicely divided into lines of given length. 
    private static class LinePrinter {
        private int line, lineLen;
        private PrintWriter pw;
        private boolean firstInLine = true;
        public LinePrinter(PrintWriter pw, int line) {
            this.pw = pw; this.line = line;
        }
        public void printWord(String word) {
            if(lineLen + word.length() + (firstInLine? 0: 1) > line) {
                lineLen = 0; pw.println(""); firstInLine = true;
            }
            if(!firstInLine) { pw.print(" "); lineLen++; }
            pw.print(word);
            firstInLine = false;
            lineLen += word.length();
        }
        public void lineBreak() {
            if(lineLen == 0) { return; }
            pw.println("");
            pw.flush();
            lineLen = 0; firstInLine = true;
        }
    }
    
    // For demonstration purposes, some word frequencies from "War and Peace".
    public static void main(String[] args) throws IOException {
        Map<String, Integer> freqs = 
            wordFrequencies(new Scanner(new File("warandpeace.txt")));
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
        ArrayList<String> wordList = new ArrayList<String>(freqs.keySet());
        // Sort the arraylist using our frequency comparator.
        Collections.sort(wordList, new FreqComparator());
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