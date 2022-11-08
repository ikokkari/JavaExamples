import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class WordFrequency {
    
    // To run this program, change the FILENAME to some text file that you have.
    private static final String FILENAME = "warandpeace.txt";

    private static final String[][] replacements = {
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
        Map<String, Integer> frequencies = new HashMap<>();
        while(s.hasNextLine()) {
            String line = s.nextLine().trim().toLowerCase();
            for(String[] replacement: replacements) {
                line = line.replaceAll(replacement[0], replacement[1]);
            }
            line = line.replaceAll("'s\\b", ""); // \b is regex word boundary
            line = line.replaceAll("'ll\\b", " will");
            line = line.replaceAll("'t\\b", "");
            String wordSeparators = "[^a-z]+";
            for(String word: line.split(wordSeparators)) {
                // Lines that start with the quote character will end up having an
                // empty word at the front of the split line array. Thus, this check.
                if(word.length() != 0) {
                    frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                }
            }
        }
        return frequencies;
    }
    
    // For demonstration purposes, some word frequencies from "War and Peace".
    public static void main(String[] args) throws IOException {
        Map<String, Integer> frequencies = wordFrequencies(new Scanner(new File(FILENAME)));
        System.out.println("Found " + frequencies.size() + " distinct words.\n");
        System.out.println("Some occurrence counts are: ");
        String[] words = {
            "chicken", "prince", "Russia", "train", "I", "supercalifragilisticexpialidocius"
        };
        for(String word: words) {
            word = word.toLowerCase();
            System.out.println(word + ": " + (frequencies.getOrDefault(word, 0)));
        }
        
        // Custom comparator to compare strings by their frequency in the map, resolving cases
        // for equal frequency using the ordinary string comparison as secondary criterion.
        class FreqComparator implements Comparator<String> {
            public int compare(String word1, String word2) {
                int f1 = frequencies.get(word1);
                int f2 = frequencies.get(word2);
                return f2 != f1 ? (f1 < f2 ? +1 : -1) : word2.compareTo(word1);
            }
        }
        
        // Create an arraylist of words so that we can sort these words by frequency.
        ArrayList<String> wordList = new ArrayList<>(frequencies.keySet());
        // Sort the arraylist using our frequency comparator.
        wordList.sort(new FreqComparator());

        // Let's print out the results.
        System.out.println("\nThe three hundred most frequent words of 'War and Peace' are:\n");
        LinePrinter lp = new LinePrinter(new PrintWriter(System.out), 80);
        for(int i = 0; i < 300; i++) {
            String word = wordList.get(i);
            lp.printWord(word + " (" + frequencies.get(word) + ")");
        }
        lp.lineBreak();
        System.out.println("\nHere are the words that occur only once in 'War and Peace':\n");
        int i = wordList.size()-1;
        String word = wordList.get(i--);
        while(frequencies.get(word) == 1) {
            lp.printWord(word);
            word = wordList.get(i--);
        }
        lp.lineBreak();
    }
}