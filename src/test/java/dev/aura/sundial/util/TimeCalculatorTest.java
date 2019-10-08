package dev.aura.sundial.util;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Test;

public class TimeCalculatorTest {
  private static final TimeCalculator NO_OFFSET = new TimeCalculator(0);
  private static final TimeCalculator SOME_OFFSET = new TimeCalculator(1.2345);

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
    final Calendar midnight = new GregorianCalendar(0, 0, 0, 0, 0, 0);
    final Calendar morning = new GregorianCalendar(0, 0, 0, 6, 0, 0);
    final Calendar noon = new GregorianCalendar(0, 0, 0, 12, 0, 0);
    final Calendar evening = new GregorianCalendar(0, 0, 0, 18, 0, 0);
    final Calendar someTime1 = new GregorianCalendar(0, 0, 0, 23, 18, 57);
    final Calendar someTime2 = new GregorianCalendar(0, 0, 0, 4, 23, 12);
    final Calendar someTime3 = new GregorianCalendar(0, 0, 0, 13, 12, 11);

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
}
