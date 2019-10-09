package dev.aura.sundial.util;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Test;

public class TimeCalculatorTest {
  private static final TimeCalculator NO_OFFSET = new TimeCalculator(0, 1);
  private static final TimeCalculator SOME_OFFSET = new TimeCalculator(1.2345, 1);

  private static final TimeCalculator NO_OFFSET_DOUBLE = new TimeCalculator(0, 2);
  private static final TimeCalculator SOME_OFFSET_DOUBLE = new TimeCalculator(1.2345, 2);
  private static final TimeCalculator NO_OFFSET_HALF = new TimeCalculator(0, 0.5);
  private static final TimeCalculator SOME_OFFSET_HALF = new TimeCalculator(1.2345, 0.5);

  @Test
  public void getOffsetTicksTest() {
    assertEquals(0, NO_OFFSET.getOffsetTicks());
    assertEquals(1234, SOME_OFFSET.getOffsetTicks());
  }

  @Test
  public void getWorldTimeTest() {
    final Calendar now = Calendar.getInstance();

    assertEquals(
        "AuraSunDial.getWorldTime() did not return the current time!",
        NO_OFFSET.getWorldTime(now),
        NO_OFFSET.getWorldTime());

    assertEquals(
        "AuraSunDial.getWorldTime() did not return the current time!",
        SOME_OFFSET.getWorldTime(now),
        SOME_OFFSET.getWorldTime());
  }

  @Test
  public void timePointTest() {
    final Calendar midnight = new GregorianCalendar(2000, 1, 1, 0, 0, 0);
    final Calendar morning = new GregorianCalendar(2000, 1, 1, 6, 0, 0);
    final Calendar noon = new GregorianCalendar(2000, 1, 1, 12, 0, 0);
    final Calendar evening = new GregorianCalendar(2000, 1, 1, 18, 0, 0);
    final Calendar someTime1 = new GregorianCalendar(2000, 1, 1, 23, 18, 57);
    final Calendar someTime2 = new GregorianCalendar(2000, 1, 1, 4, 23, 12);
    final Calendar someTime3 = new GregorianCalendar(2000, 1, 1, 13, 12, 11);

    someTime1.setTimeZone(TimeZone.getTimeZone("CTT"));
    someTime2.setTimeZone(TimeZone.getTimeZone("ACT"));
    someTime3.setTimeZone(TimeZone.getTimeZone("ECT"));

    assertEquals("Conversion is incorrect!", 18000, NO_OFFSET.getWorldTime(midnight));
    assertEquals("Conversion is incorrect!", 0, NO_OFFSET.getWorldTime(morning));
    assertEquals("Conversion is incorrect!", 6000, NO_OFFSET.getWorldTime(noon));
    assertEquals("Conversion is incorrect!", 12000, NO_OFFSET.getWorldTime(evening));
    assertEquals("Conversion is incorrect!", 17315, NO_OFFSET.getWorldTime(someTime1));
    assertEquals("Conversion is incorrect!", 22386, NO_OFFSET.getWorldTime(someTime2));
    assertEquals("Conversion is incorrect!", 7203, NO_OFFSET.getWorldTime(someTime3));

    assertEquals("Conversion is incorrect!", 19234, SOME_OFFSET.getWorldTime(midnight));
    assertEquals("Conversion is incorrect!", 1234, SOME_OFFSET.getWorldTime(morning));
    assertEquals("Conversion is incorrect!", 7234, SOME_OFFSET.getWorldTime(noon));
    assertEquals("Conversion is incorrect!", 13234, SOME_OFFSET.getWorldTime(evening));
    assertEquals("Conversion is incorrect!", 18549, SOME_OFFSET.getWorldTime(someTime1));
    assertEquals("Conversion is incorrect!", 23620, SOME_OFFSET.getWorldTime(someTime2));
    assertEquals("Conversion is incorrect!", 8437, SOME_OFFSET.getWorldTime(someTime3));
  }

  @Test
  public void speedModifierTest() {
    final Calendar midnight1 = new GregorianCalendar(2000, 1, 1, 0, 0, 0);
    final Calendar morning1 = new GregorianCalendar(2000, 1, 1, 6, 0, 0);
    final Calendar noon1 = new GregorianCalendar(2000, 1, 1, 12, 0, 0);
    final Calendar evening1 = new GregorianCalendar(2000, 1, 1, 18, 0, 0);
    final Calendar midnight2 = new GregorianCalendar(2000, 1, 2, 0, 0, 0);
    final Calendar morning2 = new GregorianCalendar(2000, 1, 2, 6, 0, 0);
    final Calendar noon2 = new GregorianCalendar(2000, 1, 2, 12, 0, 0);
    final Calendar evening2 = new GregorianCalendar(2000, 1, 2, 18, 0, 0);

    assertEquals("Conversion is incorrect!", 18000, NO_OFFSET_DOUBLE.getWorldTime(midnight1));
    assertEquals("Conversion is incorrect!", 6000, NO_OFFSET_DOUBLE.getWorldTime(morning1));
    assertEquals("Conversion is incorrect!", 18000, NO_OFFSET_DOUBLE.getWorldTime(noon1));
    assertEquals("Conversion is incorrect!", 6000, NO_OFFSET_DOUBLE.getWorldTime(evening1));
    assertEquals("Conversion is incorrect!", 18000, NO_OFFSET_DOUBLE.getWorldTime(midnight2));
    assertEquals("Conversion is incorrect!", 6000, NO_OFFSET_DOUBLE.getWorldTime(morning2));
    assertEquals("Conversion is incorrect!", 18000, NO_OFFSET_DOUBLE.getWorldTime(noon2));
    assertEquals("Conversion is incorrect!", 6000, NO_OFFSET_DOUBLE.getWorldTime(evening2));

    assertEquals("Conversion is incorrect!", 19234, SOME_OFFSET_DOUBLE.getWorldTime(midnight1));
    assertEquals("Conversion is incorrect!", 7234, SOME_OFFSET_DOUBLE.getWorldTime(morning1));
    assertEquals("Conversion is incorrect!", 19234, SOME_OFFSET_DOUBLE.getWorldTime(noon1));
    assertEquals("Conversion is incorrect!", 7234, SOME_OFFSET_DOUBLE.getWorldTime(evening1));
    assertEquals("Conversion is incorrect!", 19234, SOME_OFFSET_DOUBLE.getWorldTime(midnight2));
    assertEquals("Conversion is incorrect!", 7234, SOME_OFFSET_DOUBLE.getWorldTime(morning2));
    assertEquals("Conversion is incorrect!", 19234, SOME_OFFSET_DOUBLE.getWorldTime(noon2));
    assertEquals("Conversion is incorrect!", 7234, SOME_OFFSET_DOUBLE.getWorldTime(evening2));

    assertEquals("Conversion is incorrect!", 18000, NO_OFFSET_HALF.getWorldTime(midnight1));
    assertEquals("Conversion is incorrect!", 21000, NO_OFFSET_HALF.getWorldTime(morning1));
    assertEquals("Conversion is incorrect!", 0, NO_OFFSET_HALF.getWorldTime(noon1));
    assertEquals("Conversion is incorrect!", 3000, NO_OFFSET_HALF.getWorldTime(evening1));
    assertEquals("Conversion is incorrect!", 6000, NO_OFFSET_HALF.getWorldTime(midnight2));
    assertEquals("Conversion is incorrect!", 9000, NO_OFFSET_HALF.getWorldTime(morning2));
    assertEquals("Conversion is incorrect!", 12000, NO_OFFSET_HALF.getWorldTime(noon2));
    assertEquals("Conversion is incorrect!", 15000, NO_OFFSET_HALF.getWorldTime(evening2));

    assertEquals("Conversion is incorrect!", 19234, SOME_OFFSET_HALF.getWorldTime(midnight1));
    assertEquals("Conversion is incorrect!", 22234, SOME_OFFSET_HALF.getWorldTime(morning1));
    assertEquals("Conversion is incorrect!", 1234, SOME_OFFSET_HALF.getWorldTime(noon1));
    assertEquals("Conversion is incorrect!", 4234, SOME_OFFSET_HALF.getWorldTime(evening1));
    assertEquals("Conversion is incorrect!", 7234, SOME_OFFSET_HALF.getWorldTime(midnight2));
    assertEquals("Conversion is incorrect!", 10234, SOME_OFFSET_HALF.getWorldTime(morning2));
    assertEquals("Conversion is incorrect!", 13234, SOME_OFFSET_HALF.getWorldTime(noon2));
    assertEquals("Conversion is incorrect!", 16234, SOME_OFFSET_HALF.getWorldTime(evening2));
  }
}
