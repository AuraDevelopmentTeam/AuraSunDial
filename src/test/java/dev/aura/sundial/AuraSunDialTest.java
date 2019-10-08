package dev.aura.sundial;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

  private static class TestHelper {
    @Getter private boolean toggled = false;

    public void toggle() {
      toggled = true;
    }
  }
}
