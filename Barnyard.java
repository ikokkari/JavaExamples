import java.util.*;

public class Barnyard {

    // The cliched first example of inheritance, akin to factorial for recursion or
    // that "output a rectangle of asterisks" for loops. But there is a reason why
    // every cliche became a cliche in the first place. For convenience, all classes
    // are given here as static nested classes, but they might as well be separate
    // top level classes.
    
    // An abstract superclass that defines the common behaviour of each animal.
    // Cannot be used to create objects, but serves as abstract parameter type for
    // polymorphic methods that works for all animals.
    
    public abstract static class Animal {
        private static int count = 0; // counter shared by all animals
        public static int getCount() { return count; }
        
        public Animal() {
            System.out.println("Default constructor of Animal"); // for educational purposes
            count++;
        }
        
        public abstract String getSound();
        public abstract String getSpecies();
        
        // All species of animals have same implementation of toString.
        @Override final public String toString() { 
            return getSpecies() + " that says " + getSound();
        }
    }
    
    // A concrete subclass of the previous abstract superclass.
    public static class Cat extends Animal {
        private static Random rng = new Random();
        public Cat() {
            System.out.println("Default constructor of Cat");
        }
        
        @Override public String getSound() { 
            return rng.nextInt(100) < 50? "meow": "purrrr"; 
        }
        @Override public String getSpecies() { return "cat"; }
    }
    
    // A subclass can also be itself abstract.
    public abstract static class Bird extends Animal {
        public Bird() {
            System.out.println("Default constructor of Bird");
        }
        
        // New methods that don't exist in the superclass can be defined.
        public abstract void fly();
    }
    
    // Two concrete subclasses of Bird.
    public static class Chicken extends Bird {
        public Chicken() {
            System.out.println("Default constructor of Chicken");
        }
        @Override public String getSound() { return "cluck"; }
        @Override public String getSpecies() { return "chicken"; }
        @Override public void fly() {
            System.out.println("The chicken flaps its wings without much success.");
        }
    }
    
    public static class Goose extends Bird {
        public Goose() {
            System.out.println("Default constructor of Goose");
        }
        @Override public String getSound() { return "honk"; }
        @Override public String getSpecies() { return "goose"; }
        @Override public void fly() {
            System.out.println("The goose soars majestically to the skies.");
        }
    }
    
    // The power of inheritance hierarchies comes from the ability to write
    // polymorphic methods in accordance to the DRY principle. The same method,
    // written once, works for any subtype of its parameter type in the future.
    
    public static void flyMultipleTimes(Bird b, int n) {
        for(int i = 0; i < n; i++) {
            b.fly(); // dynamically bound method call
        }
    }
    
    public static void outputSounds(Animal[] as) {
        for(Animal a: as) {
            System.out.println(a.getSound());
        }
    }
    
    // A decorator subclass of Animal. Every decorator object contains a private
    // reference to the underlying object, and overrides its methods to call the
    // methods of the underlying object.
    
    public static class LoudAnimal extends Animal {
        private Animal animal;
        public LoudAnimal(Animal animal) {
            this.animal = animal;
            System.out.println("Constructor of LoudAnimal with " + animal);
        }
        @Override public String getSound() { 
            return animal.getSound().toUpperCase();
        }
        @Override public String getSpecies() {
            return "loud " + animal.getSpecies();
        }
    }
    
    // Another decorator subclass with the same idea.
    public static class MirroredAnimal extends Animal {
        private Animal animal;
        public MirroredAnimal(Animal animal) {
            this.animal = animal;
            System.out.println("Constructor of MirroredAnimal with " + animal);
        }
        @Override public String getSound() { 
            return new StringBuilder(animal.getSound()).reverse().toString();
        }
        @Override public String getSpecies() {
            return "mirrored " + animal.getSpecies();
        }
    }
    
    // For demonstration and educational purposes.
    public static void main(String[] args) {
        Animal a1 = new Goose();
        System.out.println("Our first animal is " + a1 + ".");
        // a1.fly() would not compile, make sure you understand why
        flyMultipleTimes(new Chicken(), 3); // calling the polymorphic method
        flyMultipleTimes(new Goose(), 2); // calling the polymorphic method
        
        Animal[] as = {
            new MirroredAnimal(new Goose()),
            new LoudAnimal(new Cat()),
            new MirroredAnimal(new LoudAnimal(new Chicken()))
        };
        System.out.println(Arrays.toString(as));
        System.out.println("Let's hear the sounds that these creatures make: ");
        outputSounds(as);
    }
}