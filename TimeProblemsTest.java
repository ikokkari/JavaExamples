import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.*;
import java.util.*;
import java.util.zip.CRC32;

public class TimeProblemsTest {

    private static final int[] daysInMonth = {
            0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        };

    @Test public void testCountFridayThirteens() {
        Random rng = new Random(12345);
        CRC32 check = new CRC32();
        for(int i = 0; i < 1000; i++) {
            int y1 = 1900 + rng.nextInt(2000);
            int y2 = y1 + rng.nextInt(i + 3);
            int m1 = rng.nextInt(12) + 1;
            int m2 = rng.nextInt(12) + 1;
            int d1 = rng.nextDouble() < .2 ? 13 : rng.nextInt(daysInMonth[m1]) + 1;
            int d2 = rng.nextDouble() < .2 ? 13 : rng.nextInt(daysInMonth[m2]) + 1;
            LocalDate startDate = LocalDate.of(y1, m1, d1);
            LocalDate endDate = LocalDate.of(y2, m2, d2);
            if(startDate.compareTo(endDate) > 0) {
                LocalDate tmp = startDate; startDate = endDate; endDate = tmp;
            }
            int result = TimeProblems.countFridayThirteens(startDate, endDate);
            //System.out.println(startDate + " " + endDate + " " + result);
            check.update(result);
        }
        assertEquals(737343005L, check.getValue());
    }

    @Test public void testDayAfterSeconds() {
        Random rng = new Random(12345);
        CRC32 check = new CRC32();
        for(int i = 0; i < 10000; i++) {
            int y = 1900 + rng.nextInt(2000);
            int mo = rng.nextInt(12) + 1;
            int d = rng.nextInt(daysInMonth[mo]) + 1;
            int h = rng.nextInt(24);
            int mi = rng.nextInt(60);
            int s = rng.nextInt(60);
            LocalDateTime now = LocalDateTime.of(y, mo, d, h, mi, s);
            long seconds = 1000;
            while(seconds <= 1_000_000_000L) {
                String result = TimeProblems.dayAfterSeconds(now, seconds);
                //System.out.println(now + " " + seconds + " " + result);
                check.update(result.getBytes());
                seconds = 10 * seconds;
            }
        }
        assertEquals(1758684803L, check.getValue());
    }

    // Since the set of Zone Ids might change in future versions of Java, here is
    // a random sampler that will remain fixed in this tester.
    private static String[] ourZones = {
        "America/Fort_Nelson", "Arctic/Longyearbyen", "Africa/Casablanca", "Europe/Kirov",
        "Atlantic/Canary", "Asia/Chongqing", "Europe/Amsterdam", "America/Indiana/Knox",
        "Atlantic/Faroe", "Pacific/Marquesas", "Africa/Douala", "America/Hermosillo",
        "Canada/Central", "Europe/Minsk", "Pacific/Kosrae", "Europe/Madrid", "Indian/Mayotte",
        "Navajo", "America/North_Dakota/New_Salem", "Pacific/Guadalcanal", "Africa/Lubumbashi",
        "America/Martinique", "America/Argentina/Jujuy", "Indian/Maldives", "Asia/Ho_Chi_Minh",
        "Pacific/Pitcairn", "Australia/Canberra", "Canada/Newfoundland", "Eire", "US/Hawaii",
        "Asia/Vladivostok", "America/Cayman", "America/Anchorage", "Antarctica/Rothera",
        "Asia/Novokuznetsk", "Indian/Antananarivo", "Africa/Timbuktu", "Hongkong"        
    };
    
    @Test public void testWhatHourIsItThere() {
        Random rng = new Random(12345);
        CRC32 check = new CRC32();
        for(int i = 0; i < 100_000; i++) {
            String hereZone = ourZones[rng.nextInt(ourZones.length)];
            String thereZone = ourZones[rng.nextInt(ourZones.length)];
            int y = 2020;
            int mo = rng.nextInt(12) + 1;
            int d = rng.nextInt(daysInMonth[mo]) + 1;
            int h = rng.nextInt(24);
            int mi = rng.nextInt(60);
            int s = rng.nextInt(60);
            LocalDateTime hereTime = LocalDateTime.of(y, mo, d, h, mi, s);
            int result = TimeProblems.whatHourIsItThere(hereTime, hereZone, thereZone);
            //System.out.println(hereTime + " " + hereZone + " " + thereZone + " " + result);
            check.update(result);
        }
        assertEquals(3627001304L, check.getValue());
    }
}