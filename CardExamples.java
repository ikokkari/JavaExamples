import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Card game examples demonstrating records, enums, and clean data modeling.
 * <p>
 * The original version encoded cards as two-character substrings within a
 * single String (e.g. {@code "Ah5c9dTs3h"} for a five-card hand). This works
 * but forces every method to manually step through the string two characters
 * at a time. The modernized version uses a {@code Card} record and
 * {@code List<Card>} for hands, making the code clearer and less error-prone.
 * <p>
 * Updated for Java 21+ with records, sealed types, switch expressions, and streams.
 *
 * @author Ilkka Kokkarinen
 */
public class CardExamples {

    // -----------------------------------------------------------------------
    // Rank and Suit enums — give names and structure to the 13 + 4 values.
    // -----------------------------------------------------------------------

    /**
     * The thirteen card ranks, ordered from Two (lowest) to Ace (highest).
     * Each rank knows its numeric value and its single-character symbol.
     */
    public enum Rank {
        TWO(2, '2'), THREE(3, '3'), FOUR(4, '4'), FIVE(5, '5'),
        SIX(6, '6'), SEVEN(7, '7'), EIGHT(8, '8'), NINE(9, '9'),
        TEN(10, 'T'), JACK(11, 'J'), QUEEN(12, 'Q'), KING(13, 'K'),
        ACE(14, 'A');

        private final int value;
        private final char symbol;

        Rank(int value, char symbol) {
            this.value = value;
            this.symbol = symbol;
        }

        public int value()  { return value; }
        public char symbol() { return symbol; }

        /** Parse a rank from its character symbol. */
        public static Rank fromSymbol(char ch) {
            for (Rank rank : values()) {
                if (rank.symbol == ch) { return rank; }
            }
            throw new IllegalArgumentException("Unknown rank symbol: " + ch);
        }
    }

    /** The four card suits. */
    public enum Suit {
        CLUBS('c'), DIAMONDS('d'), HEARTS('h'), SPADES('s');

        private final char symbol;

        Suit(char symbol) { this.symbol = symbol; }
        public char symbol() { return symbol; }

        /** Parse a suit from its character symbol. */
        public static Suit fromSymbol(char ch) {
            for (Suit suit : values()) {
                if (suit.symbol == ch) { return suit; }
            }
            throw new IllegalArgumentException("Unknown suit symbol: " + ch);
        }
    }

    // -----------------------------------------------------------------------
    // Card record — an immutable value type. Records (Java 16+) automatically
    // generate the constructor, accessors, equals, hashCode, and toString.
    // -----------------------------------------------------------------------

    /**
     * An immutable playing card with a rank and a suit.
     *
     * @param rank the card's rank
     * @param suit the card's suit
     */
    public record Card(Rank rank, Suit suit) {

        /** The two-character string representation, e.g. "Ah" or "Tc". */
        @Override
        public String toString() {
            return "" + rank.symbol() + suit.symbol();
        }

        /** Parse a card from a two-character string like "Ah" or "9d". */
        public static Card parse(String text) {
            if (text.length() != 2) {
                throw new IllegalArgumentException("Card string must be 2 chars: " + text);
            }
            return new Card(Rank.fromSymbol(text.charAt(0)),
                    Suit.fromSymbol(text.charAt(1)));
        }
    }

    // -----------------------------------------------------------------------
    // Hand utilities.
    // -----------------------------------------------------------------------

    /**
     * Parse a hand string like {@code "Ah5c9dTs3h"} into a list of Cards.
     * This bridges the old encoding to the new data model.
     */
    public static List<Card> parseHand(String hand) {
        return IntStream.iterate(0, i -> i < hand.length(), i -> i + 2)
                .mapToObj(i -> Card.parse(hand.substring(i, i + 2)))
                .toList();
    }

    /**
     * Format a list of cards back into the compact string encoding.
     */
    public static String handToString(List<Card> hand) {
        return hand.stream()
                .map(Card::toString)
                .collect(Collectors.joining());
    }

    // -----------------------------------------------------------------------
    // Highest rank in a hand.
    // -----------------------------------------------------------------------

    /**
     * Return the highest numeric rank among all cards in the hand.
     *
     * @param hand the hand of cards
     * @return the highest rank value (2–14, where 14 = Ace)
     */
    public static int highestRank(List<Card> hand) {
        return hand.stream()
                .map(Card::rank)
                .max(Comparator.comparingInt(Rank::value))
                .map(Rank::value)
                .orElse(0);
    }

    // -----------------------------------------------------------------------
    // Random hand generation (no duplicate cards).
    // -----------------------------------------------------------------------

    /**
     * Create a random hand of unique cards by shuffling a full deck and
     * taking the first {@code size} cards. Much simpler and faster than
     * rejection sampling, especially for larger hands.
     *
     * @param rng  the random number generator
     * @param size the number of cards to deal
     * @return the dealt hand
     */
    public static List<Card> createRandomHand(Random rng, int size) {
        // Build a full 52-card deck.
        var deck = new ArrayList<Card>();
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                deck.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(deck, rng);
        return List.copyOf(deck.subList(0, size));
    }

    // -----------------------------------------------------------------------
    // Blackjack hand evaluation.
    // -----------------------------------------------------------------------

    /**
     * Return the blackjack point value of a rank: face cards are 10,
     * aces start as 11 (soft), and number cards are their face value.
     */
    private static int blackjackPoints(Rank rank) {
        return switch (rank) {
            case JACK, QUEEN, KING -> 10;
            case ACE -> 11;
            default -> rank.value();
        };
    }

    /**
     * Evaluate a blackjack hand and return a description string.
     * <ul>
     *   <li>{@code "blackjack"} — first two cards total exactly 21</li>
     *   <li>{@code "bust"} — total exceeds 21 even with all aces counted as 1</li>
     *   <li>{@code "soft N"} — total is N and at least one ace counts as 11</li>
     *   <li>{@code "hard N"} — total is N with no aces counted as 11</li>
     * </ul>
     *
     * @param hand the blackjack hand
     * @return the hand evaluation as a descriptive string
     */
    public static String blackjackCount(List<Card> hand) {
        int total = 0;
        int softAces = 0; // How many aces are currently counted as 11.

        for (Card card : hand) {
            int points = blackjackPoints(card.rank());
            total += points;
            if (points == 11) { softAces++; }

            // If we bust and have a soft ace, convert it from 11 to 1.
            while (total > 21 && softAces > 0) {
                total -= 10;
                softAces--;
            }
            if (total > 21) { return "bust"; }
        }

        if (hand.size() == 2 && total == 21) { return "blackjack"; }
        return (softAces > 0 ? "soft " : "hard ") + total;
    }

    // -----------------------------------------------------------------------
    // Poker straight detection.
    // -----------------------------------------------------------------------

    /**
     * Determine whether a five-card poker hand contains a straight (five
     * cards of consecutive ranks). An ace can be low (A-2-3-4-5) or high
     * (10-J-Q-K-A). For simplicity, straight flushes are not distinguished.
     *
     * @param hand a five-card poker hand
     * @return whether the hand is a straight
     */
    public static boolean hasStraight(List<Card> hand) {
        // Collect the distinct rank values present in the hand.
        var rankValues = hand.stream()
                .mapToInt(c -> c.rank().value())
                .distinct()
                .sorted()
                .toArray();

        // A straight requires five distinct ranks.
        if (rankValues.length != 5) { return false; }

        int spread = rankValues[4] - rankValues[0];

        // Normal straight: five consecutive ranks have a spread of exactly 4.
        if (spread == 4) { return true; }

        // Ace-low straight (wheel): A-2-3-4-5 has rank values {2,3,4,5,14}.
        // The spread is 12, but if we check for {2,3,4,5} plus an ace, it works.
        return spread == 12
                && rankValues[0] == 2
                && rankValues[3] == 5;
        // rankValues[4] must be 14 (Ace) since spread is 12 and min is 2.
    }

    // -----------------------------------------------------------------------
    // Main — demonstrate each method.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        var rng = new Random(42);

        // --- Parsing and display ---
        System.out.println("--- Card parsing ---");
        String encoded = "Ah5c9dTs3h";
        var hand = parseHand(encoded);
        System.out.printf("Parsed \"%s\" → %s%n", encoded, hand);
        System.out.printf("Back to string: \"%s\"%n%n", handToString(hand));

        // --- Highest rank ---
        System.out.println("--- highestRank ---");
        System.out.printf("Highest rank in %s: %d%n%n", hand, highestRank(hand));

        // --- Random hands ---
        System.out.println("--- Random 5-card hands ---");
        for (int i = 0; i < 5; i++) {
            var randomHand = createRandomHand(rng, 5);
            System.out.printf("%s  highest=%d  straight=%s%n",
                    randomHand, highestRank(randomHand), hasStraight(randomHand));
        }

        // --- Blackjack ---
        System.out.println("\n--- Blackjack hands ---");
        // Some interesting test cases.
        String[] blackjackHands = {
                "AhKd",     // blackjack
                "Ah2c3d",   // soft 16
                "Kd7h",     // hard 17
                "TdTh5s",   // bust (25)
                "AhAd9c",   // soft 21 (two aces, one demoted)
                "5h6d",     // hard 11
        };
        for (String handStr : blackjackHands) {
            var bjHand = parseHand(handStr);
            System.out.printf("%-20s → %s%n", bjHand, blackjackCount(bjHand));
        }

        // --- Straight detection with known hands ---
        System.out.println("\n--- Straight detection ---");
        String[] straightTests = {
                "3h4d5c6s7h",   // normal straight
                "Ah2c3d4s5h",   // ace-low (wheel)
                "TdJhQcKsAh",   // ace-high (broadway)
                "2h4d6c8sTh",   // not a straight (even ranks)
                "3h3d5c6s7h",   // not a straight (duplicate rank)
        };
        for (String handStr : straightTests) {
            var pokerHand = parseHand(handStr);
            System.out.printf("%-20s straight=%s%n", pokerHand, hasStraight(pokerHand));
        }
    }
}