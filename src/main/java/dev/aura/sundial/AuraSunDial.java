package dev.aura.sundial;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import dev.aura.sundial.command.CommandRealTime;
import dev.aura.sundial.config.Config;
import dev.aura.sundial.util.TimeCalculator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bstats.sponge.MetricsLite2;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
import org.spongepowered.api.world.storage.WorldProperties;

@Plugin(
    id = AuraSunDial.ID,
    name = AuraSunDial.NAME,
    version = AuraSunDial.VERSION,
    description = AuraSunDial.DESCRIPTION,
    url = AuraSunDial.URL,
    authors = {AuraSunDial.AUTHOR_BRAINSTONE})
public class AuraSunDial {
  public static final String ID = "@id@";
  public static final String NAME = "@name@";
  public static final String VERSION = "@version@";
  public static final String DESCRIPTION = "@description@";
  public static final String URL = "https://github.com/AuraDevelopmentTeam/AuraSunDial";
  public static final String AUTHOR_BRAINSTONE = "The_BrainStone";

  private static final TypeToken<Config> configToken = TypeToken.of(Config.class);

  @NonNull @Getter private static AuraSunDial instance = null;

  @SuppressFBWarnings(
      value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
      justification = "Metrics instance needs to be kept alive")
  protected MetricsLite2 metrics;

  @Inject protected GuiceObjectMapperFactory factory;

  @Inject
  @DefaultConfig(sharedRoot = false)
  protected ConfigurationLoader<CommentedConfigurationNode> loader;

  @Inject @NonNull protected Logger logger;
  @NonNull protected Config config;
  @Getter protected TimeCalculator timeCalculator;
  protected Task timeTask;

  @Inject
  public AuraSunDial(MetricsLite2.Factory metricsFactory) {
    if (instance != null) throw new IllegalStateException("instance cannot be instantiated twice");

    instance = this;

    // Make sure logger is initialized
    logger = getLogger();
    metrics = metricsFactory.make(1534);
  }

  public static Logger getLogger() {
    if ((instance == null) || (instance.logger == null)) return NOPLogger.NOP_LOGGER;
    else return instance.logger;
  }

  public static Config getConfig() {
    return instance.config;
  }

  protected static <T> void callSafely(T object, Consumer<T> method) {
    if (object != null) {
      method.accept(object);
    }
  }

  @Listener
  public void onInit(GameInitializationEvent event) throws IOException, ObjectMappingException {
    onInit();
  }

  public void onInit() throws IOException, ObjectMappingException {
    logger.info("Initializing " + NAME + " Version " + VERSION);

    if (VERSION.contains("SNAPSHOT")) {
      logger.warn("WARNING! This is a snapshot version!");
      logger.warn("Use at your own risk!");
    }
    if (VERSION.contains("DEV")) {
      logger.info("This is a unreleased development version!");
      logger.info("Things might not work properly!");
    }

    loadConfig();

    timeCalculator = new TimeCalculator(config.getOffset(), config.getSpeedModifier());

    if (!config.getSyncWithRealTime()) {
      config.getActiveWorlds().forEach(timeCalculator::updatePerWorldOffset);
    }

    CommandRealTime.register(this);

    logger.info("Loaded successfully!");
  }

  @Listener
  public void onLoadComplete(GameLoadCompleteEvent event) {
    onLoadComplete();
  }

  public void onLoadComplete() {
    try {
      timeTask =
          Task.builder()
              .execute(this::setTime)
              .intervalTicks(1)
              .name(ID + "-time-setter")
              .submit(this);

      logger.debug("Started \"" + timeTask.getName() + '"');
    } catch (IllegalStateException e) {
      logger.error("Sponge isn't initialized! Plugin won't work!!", e);
    }
  }

  @Listener
  public void onReload(GameReloadEvent event) throws IOException, ObjectMappingException {
    // Unregistering everything
    onStop();

    // Starting over
    onInit();
    onLoadComplete();

    logger.info("Reloaded successfully!");
  }

  @Listener
  public void onStop(GameStoppingEvent event) throws IOException, ObjectMappingException {
    onStop();
  }

  public void onStop() throws IOException, ObjectMappingException {
    logger.info("Shutting down " + NAME + " Version " + VERSION);

    Sponge.getCommandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);

    timeCalculator = null;

    callSafely(timeTask, Task::cancel);
    timeTask = null;

    config = null;

    logger.info("Unloaded successfully!");
  }

  private void loadConfig() throws IOException, ObjectMappingException {
    logger.debug("Loading config...");

    CommentedConfigurationNode node =
        loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory));

    config = node.<Config>getValue(configToken, Config::new);

    saveConfig();
  }

  public void saveConfig() throws IOException, ObjectMappingException {
    CommentedConfigurationNode node =
        loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory));

    logger.debug("Saving/Formatting config...");
    node.setValue(configToken, config);
    loader.save(node);
  }

  private void setTime() {
    if (config == null) return;

    final boolean syncTime = config.getSyncWithRealTime();
    WorldProperties properties;
    long targetWorldTime;
    long actualWorldTime;

    for (World world : config.getActiveWorlds()) {
      properties = world.getWorldStorage().getWorldProperties();
      targetWorldTime = timeCalculator.getWorldTime(world);
      actualWorldTime = properties.getWorldTime();

      // Looks for skipped nights. Of course only relevant when we're not syncing the time
      if (!syncTime && (actualWorldTime == 0) && (targetWorldTime > 10)) {
        timeCalculator.addPerWorldOffset(world, -targetWorldTime);
        targetWorldTime = 0;
      }

      // Checks if the gamerule is either not present or not set to "false"
      if (!properties
          .getGameRule(DefaultGameRules.DO_DAYLIGHT_CYCLE)
          .filter("false"::equals)
          .isPresent()) properties.setGameRule(DefaultGameRules.DO_DAYLIGHT_CYCLE, "false");
      if (actualWorldTime != targetWorldTime) properties.setWorldTime(targetWorldTime);
    }
  }
}
