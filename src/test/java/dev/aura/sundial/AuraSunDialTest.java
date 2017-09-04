package dev.aura.sundial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Paths;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;

import lombok.Getter;

public class AuraSunDialTest {
	@Test
	public void eventsTest() {
		final AuraSunDial instance = new AuraSunDial();
		instance.configFile = Paths.get(System.getProperty("java.io.tmpdir"), "sundial", "sundial.conf");

		Cause cause = Cause.source(instance).build();

		// Starting
		GameConstructionEvent gameConstructionEvent = SpongeEventFactory.createGameConstructionEvent(cause);
		instance.gameConstruct(gameConstructionEvent);
		GameInitializationEvent gameInitializationEvent = SpongeEventFactory.createGameInitializationEvent(cause);
		instance.init(gameInitializationEvent);
		GameLoadCompleteEvent gameLoadCompleteEvent = SpongeEventFactory.createGameLoadCompleteEvent(cause);
		instance.loadComplete(gameLoadCompleteEvent);

		// Reload
		GameReloadEvent gameReloadEvent = SpongeEventFactory.createGameReloadEvent(cause);
		instance.reload(gameReloadEvent);

		// Stop
		GameStoppingEvent gameStoppingEvent = SpongeEventFactory.createGameStoppingEvent(cause);
		instance.stop(gameStoppingEvent);

		assertSame("The instance should be the same!", instance, AuraSunDial.getInstance());
	}

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

		assertEquals("AuraSunDial.getWorldTime() did not return the current time!", AuraSunDial.getWorldTime(now),
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

	private class TestHelper {
		@Getter
		private boolean toggled = false;

		public void toggle() {
			toggled = true;
		}
	}
}
