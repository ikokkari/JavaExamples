import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * The clichéd first example of inheritance, akin to factorial for recursion or
 * "output a rectangle of asterisks" for loops. But there is a reason why every
 * cliché became a cliché in the first place. For convenience, all classes are
 * given here as static nested classes, but they could just as well be separate
 * top-level classes in their own files.
 *
 * Modernized for Java 21+ to showcase sealed hierarchies, pattern matching,
 * enhanced switch expressions, RandomGenerator, and other post-Java-17 idioms.
 */
public class Barnyard {

    // -----------------------------------------------------------------------
    // SEALED HIERARCHY: The "sealed" modifier restricts which classes may extend
    // Animal. This turns an open hierarchy into a closed one, letting the compiler
    // verify that every subtype is handled in pattern-matching switches. Each
    // permitted subclass must be final, sealed, or non-sealed.
    // -----------------------------------------------------------------------

    public abstract static sealed class Animal
            permits Cat, Bird, AnimalDecorator {

        private static int count = 0; // counter shared by all animals
        public static int getCount() { return count; }

        public Animal() {
            System.out.println("Default constructor of Animal");
            count++;
        }

        // Methods we wish to impose on all future subclasses, but cannot at this
        // level give a meaningful implementation, are declared abstract.
        public abstract String getSound();
        public abstract String getSpecies();

        // All species of animals share the same toString. Marking it final
        // prevents subclasses from accidentally breaking the contract.
        @Override
        public final String toString() {
            return getSpecies() + " that says " + getSound();
        }
    }

    // -----------------------------------------------------------------------
    // CONCRETE LEAF CLASS: "final" satisfies the sealed requirement and
    // signals that no further subclassing is intended.
    // -----------------------------------------------------------------------

    public static final class Cat extends Animal {
        // Java 17+ RandomGenerator interface: a modern, more flexible replacement
        // for java.util.Random. The factory method lets you swap algorithms easily.
        private static final RandomGenerator rng = RandomGenerator.getDefault();

        public Cat() {
            System.out.println("Default constructor of Cat");
        }

        @Override
        public String getSound() {
            // Ternary remains idiomatic for simple binary choices.
            return rng.nextInt(100) < 50 ? "meow" : "purrrr";
        }

        @Override
        public String getSpecies() { return "cat"; }
    }

    // -----------------------------------------------------------------------
    // INTERMEDIATE SEALED CLASS: Bird is itself sealed, extending the hierarchy
    // one more level while still keeping it closed. A subclass of a sealed class
    // that wants to allow further extension must declare itself sealed or non-sealed.
    // -----------------------------------------------------------------------

    public abstract static sealed class Bird extends Animal
            permits Chicken, Goose {

        public Bird() {
            System.out.println("Default constructor of Bird");
        }

        // A method unique to birds — not present in the Animal superclass.
        public abstract void fly();
    }

    // Two concrete (final) subclasses of Bird.

    public static final class Chicken extends Bird {
        public Chicken() {
            System.out.println("Default constructor of Chicken");
        }

        @Override public String getSound()   { return "cluck"; }
        @Override public String getSpecies()  { return "chicken"; }

        @Override
        public void fly() {
            System.out.println("The chicken flaps its wings without much success.");
        }
    }

    public static final class Goose extends Bird {
        public Goose() {
            System.out.println("Default constructor of Goose");
        }

        @Override public String getSound()   { return "honk"; }
        @Override public String getSpecies()  { return "goose"; }

        @Override
        public void fly() {
            System.out.println("The goose soars majestically to the skies.");
        }
    }

    // -----------------------------------------------------------------------
    // POLYMORPHIC METHODS: Written once, they work for any subtype of their
    // parameter type — present or future. This is the payoff of inheritance.
    // -----------------------------------------------------------------------

    public static void flyMultipleTimes(Bird b, int n) {
        for (int i = 0; i < n; i++) {
            b.fly(); // dynamically bound method call
        }
    }

    public static void outputSounds(Animal[] animals) {
        for (Animal a : animals) {
            System.out.println(a.getSound());
        }
    }

    // -----------------------------------------------------------------------
    // PATTERN MATCHING WITH SEALED TYPES: Because the Animal hierarchy is sealed,
    // the compiler knows every possible subtype. This lets us write exhaustive
    // switches without a default branch — the compiler proves we covered all cases.
    // -----------------------------------------------------------------------

    public static String describeAnimal(Animal a) {
        // Java 21 pattern matching for switch: we can match on type and bind a
        // variable in one step. "when" guards add further conditions.
        return switch (a) {
            case Chicken c  -> "A flightless farmyard bird: " + c;
            case Goose g    -> "A majestic waterfowl: " + g;
            case Cat c      -> "An independent feline: " + c;
            // AnimalDecorator is non-sealed, so we need this catch-all for it
            // and any of its subclasses. Without it, the compiler would complain.
            case AnimalDecorator d -> "A decorated " + d.getClient().getSpecies()
                    + " that now says " + d.getSound();
        };
    }

    // -----------------------------------------------------------------------
    // DECORATOR PATTERN: To avoid repeating the boilerplate "store a client,
    // delegate by default" code in every decorator, we extract an abstract
    // superclass. Each concrete decorator overrides only the methods it modifies.
    //
    // Marked non-sealed so new decorator variants can be added freely — the
    // decoration axis is intentionally open for extension.
    // -----------------------------------------------------------------------

    public abstract static non-sealed class AnimalDecorator extends Animal {
        private final Animal client;

        public AnimalDecorator(Animal client) {
            this.client = client;
            System.out.println("Constructor of AnimalDecorator wrapping " + client);
        }

        // Expose client for pattern-matching inspection (see describeAnimal).
        public Animal getClient() { return client; }

        // Default delegation — subclasses override whichever methods they modify.
        @Override public String getSound()   { return client.getSound(); }
        @Override public String getSpecies() { return client.getSpecies(); }
    }

    // Three concrete decorators. Each overrides only what it changes.

    public static class LoudAnimal extends AnimalDecorator {
        public LoudAnimal(Animal client) { super(client); }

        @Override
        public String getSound() {
            return super.getSound().toUpperCase();
        }

        @Override
        public String getSpecies() {
            return "loud " + super.getSpecies();
        }
    }

    public static class MirroredAnimal extends AnimalDecorator {
        public MirroredAnimal(Animal client) { super(client); }

        @Override
        public String getSound() {
            // The canonical way to reverse a String in Java: StringBuilder.
            return new StringBuilder(super.getSound()).reverse().toString();
        }

        @Override
        public String getSpecies() {
            return "mirrored " + super.getSpecies();
        }
    }

    public static class AngryAnimal extends AnimalDecorator {
        public AngryAnimal(Animal client) { super(client); }

        @Override
        public String getSound() {
            return "grrr " + super.getSound() + " hisss!";
        }

        @Override
        public String getSpecies() {
            return "angry " + super.getSpecies();
        }
    }

    // -----------------------------------------------------------------------
    // MAIN: Demonstration and educational driver.
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        Animal a1 = new Goose();
        System.out.println("Our first animal is " + a1 + ".");
        // a1.fly() would NOT compile — make sure you understand why.
        // (Hint: the declared type of a1 is Animal, which has no fly() method.
        // The compiler checks the declared type, not the runtime type.)

        flyMultipleTimes(new Chicken(), 3);
        flyMultipleTimes(new Goose(), 2);

        Animal[] menagerie = {
                new MirroredAnimal(new Goose()),
                new MirroredAnimal(new LoudAnimal(new Chicken())),
                // Note: the order of the same decorators can change the result.
                new AngryAnimal(new LoudAnimal(new Cat())),
                new LoudAnimal(new AngryAnimal(new Cat()))
        };

        System.out.println(Arrays.toString(menagerie));
        System.out.println("Let's hear the sounds these creatures make:");
        outputSounds(menagerie);

        // Demonstrate Java 21 pattern matching with our sealed hierarchy.
        System.out.println("\nDescribing each animal with pattern matching:");
        for (Animal a : menagerie) {
            System.out.println(describeAnimal(a));
        }

        // List.of produces an unmodifiable list — prefer it over raw arrays
        // when mutability is not needed. Good habit for modern Java.
        List<Animal> extras = List.of(new Cat(), new Goose(), new Chicken());
        System.out.println("\nExtras: " + extras);

        System.out.println("\nTotal animals created: " + Animal.getCount());
    }
}