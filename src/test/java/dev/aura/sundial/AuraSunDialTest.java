package dev.aura.sundial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.GregorianCalendar;
import lombok.Getter;
import org.junit.Test;

public class AuraSunDialTest {
  @Test
  public void callSafelyTest() {
    TestHelper nullObj = null;
    TestHelper tester = new TestHelper();

    try {
      AuraSunDial.callSafely(nullObj, TestHelper::toggle);
    } catch (NullPointerException e) {
      fail("No NullPointerException should have been thrown!");
    }

    AuraSunDial.callSafely(tester, TestHelper::toggle);

    assertTrue("TestHelper should have been toggled", tester.isToggled());
  }

  @Test
  public void getWorldTimeTest() {
    Calendar now = Calendar.getInstance();

    assertEquals(
        "AuraSunDial.getWorldTime() did not return the current time!",
        AuraSunDial.getWorldTime(now),
        AuraSunDial.getWorldTime());
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

    assertEquals("Conversion is incorrect!", 18000, AuraSunDial.getWorldTime(midnight));
    assertEquals("Conversion is incorrect!", 0, AuraSunDial.getWorldTime(morning));
    assertEquals("Conversion is incorrect!", 6000, AuraSunDial.getWorldTime(noon));
    assertEquals("Conversion is incorrect!", 12000, AuraSunDial.getWorldTime(evening));
    assertEquals("Conversion is incorrect!", 17315, AuraSunDial.getWorldTime(someTime1));
    assertEquals("Conversion is incorrect!", 22386, AuraSunDial.getWorldTime(someTime2));
    assertEquals("Conversion is incorrect!", 7203, AuraSunDial.getWorldTime(someTime3));
  }

  private static class TestHelper {
    @Getter private boolean toggled = false;

    public void toggle() {
      toggled = true;
    }
  }
}
