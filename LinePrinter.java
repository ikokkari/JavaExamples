import java.io.PrintWriter;

public class LinePrinter {
    private final int lineMaxLength; // Maximum desired length of a line.
    private int lineLen; // Length of the current line.
    private final PrintWriter target; // Where to direct the printed characters.
    private boolean firstInLine = true; // Is the current word first in this line?

    public LinePrinter(PrintWriter target, int lineMaxLength) {
        this.target = target;
        this.lineMaxLength = lineMaxLength;
    }

    public void printWord(String word) {
        // If adding the current word would make the line too long, start a new line.
        if(lineLen + (firstInLine? 0: 1) + word.length() > lineMaxLength) {
            lineBreak();
        }
        // Print a space before the current word, unless it's the first word in line.
        if(!firstInLine) {
            target.print(" ");
            lineLen++;
        }
        // Print the actual word.
        target.print(word);
        firstInLine = false;
        lineLen += word.length();
    }

    public void lineBreak() {
        target.println("");
        target.flush(); // Emulate the behaviour flushing at line breaks.
        lineLen = 0;
        firstInLine = true;
    }
}
