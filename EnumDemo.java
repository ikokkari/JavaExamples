import java.util.*;

public class EnumDemo {

    private static enum Day {
        // possible values of this particular enum type
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;
        
        // enum types can have fields and methods just like classes
        public boolean isWeekday() {
            return (this != SATURDAY && this != SUNDAY);    
        }        
    }

    public static void main(String[] args) {
        for(Day d: Day.values()) {    
            System.out.println(d.ordinal() + ":" + d.name() +
              (d.isWeekday() ? " is" : " is not") + " a weekday"); 
        }

         // Enums can also be used in switches (like int or char)
        System.out.println("Listing weekdays with a switch");
        for(Day d: Day.values()) {
            System.out.print(d + " is ");  
            switch(d) {
                case SATURDAY: case SUNDAY:
                    System.out.println("not a weekday"); break;
                default:
                    System.out.println("a weekday");
            }
        }
        
        // EnumSet is the efficient Set implementation for Enums
        EnumSet<Day> weekdays = EnumSet.range(Day.MONDAY, Day.FRIDAY);
        System.out.println("Listing weekdays with an EnumSet");
        for(Day d: weekdays) { System.out.println(d); }
        
        // EnumMap is the efficient Map implementation for Enums
        EnumMap<Day, Boolean> weekdayMap = new EnumMap<Day, Boolean>(Day.class);
        for(Day d: Day.values()) { weekdayMap.put(d, d.isWeekday()); }
        System.out.println("Listing weekdays with an EnumMap");
        for(Day d: Day.values()) {
            if(weekdayMap.get(d)) { System.out.println(d); }
        }       
    }
}