import java.util.Random;

public class CardExamples {
    
    // A private utility method to convert a card character to its numerical value.
    // Could have also been written longer as an if-else ladder, which would be
    // better if the parameter were not guaranteed to be a legal rank.
    private static int getRank(char c) {
        return 2 + "23456789TJQKA".indexOf(c); // clever String method to use
    }
    
    /**
     * Compute and return the numerical rank of the highest card in the hand.
     * @param hand The hand of cards to analyze.
     * @return The numerical rank of the highest card in the hand.
     */
    public static int highestRank(String hand) {
        int highest = 0;
        for(int i = 0; i < hand.length(); i += 2) {
            int rank = getRank(hand.charAt(i));
            if(rank > highest) { highest = rank; }
        }
        return highest;
    }
    
    /**
     * Create a random hand of cards so that no card can be repeated. Useful for
     * JUnit tester methods to try out other card methods with random hands. 
     * @param rng The random number generator to use.
     * @param len Number of cards in the hand to create.
     * @return The hand of cards as string.
     */
    private static String createHand(Random rng, int len) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < len; i++) {
            String card;
            outer:
            while(true) {
                // Create a random card.
                card = "" + "23456789TJQKA".charAt(rng.nextInt(13));
                card += "cdhs".charAt(rng.nextInt(4));
                // Verify that the result does not contain that card already.
                for(int j = 0; j < result.length(); j += 2) {
                    if(result.substring(j, j + 2).equals(card)) { continue outer; }
                }
                break;
            }
            result.append(card);
        }
        return result.toString();
    }
    
    // A private utility method to convert a card character to its blackjack value.
    private static int blackjackValue(char c) {
        int idx = getRank(c);
        if(idx > 10 && idx < 14) { idx = 10; } // all faces are counted as 10
        if(idx == 14) { idx = 11; } // aces are 11's in blackjack for now
        return idx;
    }
    
    /**
     * Given a blackjack hand as a string (as in the labs), compute its
     * value and return it as a string. If the value is greater than 21,
     * return "bust" regardless of value. If the first two cards are
     * equal to 21, return "blackjack". Otherwise, return the value as
     * number preceded by either "soft" or "hard" depending on whether
     * the hand has an ace that was counted as 11 instead of 1.
     * @param hand The blackjack hand to analyze.
     * @return The value of the hand.
     */
    public static String blackjackCount(String hand) {
        int count = 0;
        int soft = 0; // how many soft aces there are in the hand
        for(int i = 0; i < hand.length(); i += 2) { // suits don't matter
            int p = blackjackValue(hand.charAt(i)); // numerical value of this card
            count += p;
            if(p == 11) { soft++; }
            if(count > 21) {
                if(soft > 0) { // saved from the bust by the soft ace
                    soft--; count -= 10;
                }
                else { return "bust"; }
            }
        }
        if(hand.length() == 4 && count == 21) { return "blackjack"; }
        return (soft > 0 ? "soft " : "hard ") + count;
    }
    
    /**
     * Given a five-card poker hand as string, determine if it contains a
     * straight, that is, five cards of consecutive ranks. Also, an ace can
     * be either low or high, for the possible straights A2345 and TJQKA.
     * For simplicity, we ignore the possibility of straight flushes.
     * @param hand The poker hand given as a string.
     * @return Whether the poker hand is a straight.
     */
    public static boolean hasStraight(String hand) {
        int min = 20, max = 0;
        int aceCount = 0;
        // Find the highest and lower ranks of cards in this hand separately
        // from aces, and also count the number of aces along the way.
        for(int i = 0; i < 10; i += 2) {
            int rank = getRank(hand.charAt(i));
            if(rank == 14) { aceCount++; }
            if(rank < 14 && rank < min) { min = rank; }
            if(rank < 14 && rank > max) { max = rank; }
        }
        // Quick rejection of many cases that cannot be a straight.
        if(aceCount > 1) { return false; }
        if(aceCount == 0 && max - min != 4) { return false; }
        if(aceCount == 1) {
            if(max == 5) { min = 2; }
            else if(min == 10) { max = 13; }
            else { return false; }
        }
        // Verify that hand contains each rank between min and max.
        for(int r = min; r <= max; r++) {
            boolean containsRank = false;
            for(int i = 0; i < 10; i += 2) {
                char c = hand.charAt(i);
                if(getRank(c) == r) { containsRank = true; break; }
            }
            if(!containsRank) { return false; }
        }
        return true;
    }
}