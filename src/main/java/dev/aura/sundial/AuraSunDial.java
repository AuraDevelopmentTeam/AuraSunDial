package dev.aura.sundial;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.bstats.sponge.MetricsLite;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
import org.spongepowered.api.world.storage.WorldProperties;

import com.google.inject.Inject;

import lombok.Getter;
import lombok.NonNull;

@Plugin(id = AuraSunDial.ID, name = AuraSunDial.NAME, version = AuraSunDial.VERSION, description = AuraSunDial.DESCRIPTION, url = AuraSunDial.URL, authors = {
		AuraSunDial.AUTHOR_BRAINSTONE })
public class AuraSunDial {
	public static final String ID = "@id@";
	public static final String NAME = "@name@";
	public static final String VERSION = "@version@";
	public static final String DESCRIPTION = "@description@";
	public static final String URL = "https://github.com/AuraDevelopmentTeam/AuraSunDial";
	public static final String AUTHOR_BRAINSTONE = "The_BrainStone";

	protected static final long secondsInRealDay = TimeUnit.DAYS.toSeconds(1);
	protected static final long ticksInMinecraftDay = 24000;
	protected static final long midnightOffset = (ticksInMinecraftDay * 3) / 4;

	@NonNull
	@Getter
	protected static AuraSunDial instance = null;
	@Inject
	protected MetricsLite metrics;
	@Inject
	@NonNull
	protected Logger logger;
	protected Task timeTask;

	public static Logger getLogger() {
		if ((instance == null) || (instance.logger == null))
			return NOPLogger.NOP_LOGGER;
		else
			return instance.logger;
	}

	protected static long getWorldTime() {
		return getWorldTime(Calendar.getInstance());
	}

	protected static long getWorldTime(final Calendar calendar) {
		final long seconds = TimeUnit.HOURS.toSeconds(calendar.get(Calendar.HOUR_OF_DAY))
				+ TimeUnit.MINUTES.toSeconds(calendar.get(Calendar.MINUTE)) + calendar.get(Calendar.SECOND);

		return (((seconds * ticksInMinecraftDay) / secondsInRealDay) + midnightOffset) % ticksInMinecraftDay;
	}

	@Listener
	public void gameConstruct(GameConstructionEvent event) {
		instance = this;

		// Make sure logger is initialized
		logger = getLogger();
	}

	@Listener
	public void init(GameInitializationEvent event) {
		logger.info("Initializing " + NAME + " Version " + VERSION);

		if (VERSION.contains("SNAPSHOT")) {
			logger.warn("WARNING! This is a snapshot version!");
			logger.warn("Use at your own risk!");
		}
		if (VERSION.contains("DEV")) {
			logger.info("This is a unreleased development version!");
			logger.info("Things might not work properly!");
		}

		logger.info("Loaded successfully!");
	}

	@Listener
	public void loadComplete(GameLoadCompleteEvent event) {
		try {
			timeTask = Task.builder().execute(this::setTime).intervalTicks(1).name(ID + "-time-setter").submit(this);

			logger.debug("Started \"" + timeTask.getName() + '"');
		} catch (IllegalStateException e) {
			logger.error("Sponge isn't initialized! Plugin won't work!!", e);
		}
	}

	@Listener
	public void reload(GameReloadEvent event) {
		Cause cause = Cause.source(this).build();

		// Unregistering everything
		GameStoppingEvent gameStoppingEvent = SpongeEventFactory.createGameStoppingEvent(cause);
		stop(gameStoppingEvent);

		// Starting over
		GameInitializationEvent gameInitializationEvent = SpongeEventFactory.createGameInitializationEvent(cause);
		init(gameInitializationEvent);
		GameLoadCompleteEvent gameLoadCompleteEvent = SpongeEventFactory.createGameLoadCompleteEvent(cause);
		loadComplete(gameLoadCompleteEvent);

		logger.info("Reloaded successfully!");
	}

	@Listener
	public void stop(GameStoppingEvent event) {
		logger.info("Shutting down " + NAME + " Version " + VERSION);

		if (timeTask != null) {
			timeTask.cancel();
			timeTask = null;
		}

		logger.info("Unloaded successfully!");
	}

	private void setTime() {
		WorldProperties properties;
		final long worldTime = getWorldTime();

		for (World world : Sponge.getGame().getServer().getWorlds()) {
			properties = world.getWorldStorage().getWorldProperties();

			properties.setGameRule(DefaultGameRules.DO_DAYLIGHT_CYCLE, "false");
			properties.setWorldTime(worldTime);
		}
	}
}
