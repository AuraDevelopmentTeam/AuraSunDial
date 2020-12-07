package dev.aura.sundial;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import dev.aura.lib.messagestranslator.MessagesTranslator;
import dev.aura.lib.messagestranslator.PluginMessagesTranslator;
import dev.aura.sundial.command.CommandRealTime;
import dev.aura.sundial.config.Config;
import dev.aura.sundial.config.ConfigMigrations;
import dev.aura.sundial.permission.PermissionRegistry;
import dev.aura.sundial.util.TimeCalculator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
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
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
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

  @Inject @Getter private PluginContainer container;
  @Inject @NonNull protected Logger logger;

  @Inject protected GuiceObjectMapperFactory factory;

  @Inject
  @DefaultConfig(sharedRoot = false)
  protected ConfigurationLoader<CommentedConfigurationNode> loader;

  @Inject
  @ConfigDir(sharedRoot = false)
  @NonNull
  protected Path configDir;

  @NonNull protected Config config;
  protected PermissionRegistry permissionRegistry;
  @NonNull protected MessagesTranslator translator;
  @Getter protected TimeCalculator timeCalculator;

  protected List<Object> eventListeners = new LinkedList<>();

  @Inject
  public AuraSunDial(MetricsLite2.Factory metricsFactory) {
    if (instance != null) throw new IllegalStateException("instance cannot be instantiated twice");

    instance = this;

    // Make sure logger is initialized
    logger = getLogger();
    // No need to save the instance if no custom graphs are registered
    metricsFactory.make(1534);
  }

  public static Logger getLogger() {
    if ((instance == null) || (instance.logger == null)) return NOPLogger.NOP_LOGGER;
    else return instance.logger;
  }

  public static Path getConfigDir() {
    return instance.configDir;
  }

  public static Config getConfig() {
    return instance.config;
  }

  public static MessagesTranslator getTranslator() {
    return instance.translator;
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

    if (permissionRegistry == null) {
      permissionRegistry = new PermissionRegistry(this);
      permissionRegistry.registerPermissions();
      logger.debug("Registered permissions");
    }

    translator =
        new PluginMessagesTranslator(
            new File(getConfigDir().toFile(), "lang"), config.getGeneral().getLanguage(), this, ID);
    timeCalculator =
        new TimeCalculator(
            config.getTimeModification().getOffset(),
            config.getTimeModification().getSpeedModifier());

    if (!config.getTimeModification().getSyncWithRealTime()) {
      config.getTimeModification().getActiveWorlds().forEach(timeCalculator::updatePerWorldOffset);
    }

    CommandRealTime.register(this);
    logger.debug("Registered commands");

    logger.debug("Registered events");

    logger.info("Loaded successfully!");
  }

  @Listener
  public void onLoadComplete(GameLoadCompleteEvent event) {
    onLoadComplete();
  }

  public void onLoadComplete() {
    try {
      Task timeTask =
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
  public void onReload(GameReloadEvent event) throws Exception {
    // Unregistering everything
    onStop();

    // Starting over
    onInit();
    onLoadComplete();

    logger.info("Reloaded successfully!");
  }

  @Listener
  public void onStop(GameStoppingEvent event) throws Exception {
    onStop();
  }

  public void onStop() throws Exception {
    logger.info("Shutting down " + NAME + " Version " + VERSION);

    Sponge.getCommandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);

    timeCalculator = null;

    stopTasks();
    logger.debug("Stopped tasks");

    removeCommands();
    logger.debug("Unregistered commands");

    removeEventListeners();
    logger.debug("Unregistered events");

    config = null;
    logger.debug("Unloaded config");

    logger.info("Unloaded successfully!");
  }

  private CommentedConfigurationNode loadConfigNode() throws IOException {
    return loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory));
  }

  private void loadConfig() throws IOException, ObjectMappingException {
    logger.debug("Loading config...");

    CommentedConfigurationNode node = loadConfigNode();
    node = ConfigMigrations.migrateConfig(node);

    config = node.<Config>getValue(configToken, Config::new);

    saveConfig(node);
  }

  public void saveConfig() throws IOException, ObjectMappingException {
    saveConfig(loadConfigNode());
  }

  public void saveConfig(CommentedConfigurationNode node)
      throws IOException, ObjectMappingException {
    logger.debug("Saving/Formatting config...");
    node.setValue(configToken, config);
    loader.save(node);
  }

  private void addEventListener(Object listener) {
    eventListeners.add(listener);

    Sponge.getEventManager().registerListeners(this, listener);
  }

  private void removeCommands() {
    final CommandManager commandManager = Sponge.getCommandManager();

    commandManager.getOwnedBy(this).forEach(commandManager::removeMapping);
  }

  private void stopTasks() {
    final Scheduler scheduler = Sponge.getScheduler();

    scheduler.getScheduledTasks(this).forEach(Task::cancel);
  }

  private void removeEventListeners() throws Exception {
    for (Object listener : eventListeners) {
      Sponge.getEventManager().unregisterListeners(listener);

      if (listener instanceof AutoCloseable) {
        ((AutoCloseable) listener).close();
      }
    }

    eventListeners.clear();
  }

  public void processTimeSkip(World world, long newTime) {
    if (!config.getTimeModification().getSyncWithRealTime()
        && config.getTimeModification().getActiveWorlds().contains(world))
      timeCalculator.addPerWorldOffset(world, newTime - timeCalculator.getWorldTime(world));
  }

  private void setTime() {
    if (config == null) return;

    final String targetDayLightCycleState = "false";
    WorldProperties properties;
    long targetWorldTime;
    long actualWorldTime;

    for (World world : config.getTimeModification().getActiveWorlds()) {
      properties = world.getWorldStorage().getWorldProperties();
      targetWorldTime = timeCalculator.getWorldTime(world);
      actualWorldTime = properties.getWorldTime();

      // Checks if the gamerule is either not present or not set to targetDayLightCycleState
      if (!properties
          .getGameRule(DefaultGameRules.DO_DAYLIGHT_CYCLE)
          .filter(targetDayLightCycleState::equals)
          .isPresent())
        properties.setGameRule(DefaultGameRules.DO_DAYLIGHT_CYCLE, targetDayLightCycleState);
      if (actualWorldTime != targetWorldTime) properties.setWorldTime(targetWorldTime);
    }
  }
}
