package dev.aura.sundial.config;

import com.google.common.reflect.TypeToken;
import dev.aura.sundial.AuraSunDial;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

public class Config {
  private static final String ACTIVE_WORLDS = "activeWorlds";
  private static final TypeToken<String> TOKEN_STRING = TypeToken.of(String.class);

  @NonNull private final AuraSunDial instance;
  @NonNull private final Logger logger;
  @NonNull private final Path configFile;

  private ConfigurationLoader<CommentedConfigurationNode> loader;
  private ConfigurationNode rootNode;

  private List<String> activeWorlds;

  public Config(AuraSunDial instance, Path configFile) {
    this.instance = instance;
    logger = AuraSunDial.getLogger();
    this.configFile = configFile;
    activeWorlds = Collections.emptyList();
  }

  private static List<String> getWorldNames() {
    return Sponge.getGame()
        .getServer()
        .getWorlds()
        .stream()
        .map(World::getName)
        .collect(Collectors.toList());
  }

  @SneakyThrows(value = ObjectMappingException.class)
  public void load() {
    if (!configFile.toFile().exists()) {
      try {
        Sponge.getAssetManager()
            .getAsset(
                instance,
                Optional.ofNullable(configFile.getFileName()).map(Path::toString).orElse(""))
            .get()
            .copyToFile(configFile);
      } catch (IOException | NoSuchElementException | IllegalStateException e) {
        logger.error("Could not load default config!", e);

        return;
      }
    }

    loader = HoconConfigurationLoader.builder().setPath(configFile).build();

    try {
      rootNode = loader.load();
    } catch (IOException e) {
      logger.error("Config could not be loaded!", e);

      return;
    }

    activeWorlds = rootNode.getNode(ACTIVE_WORLDS).getList(TOKEN_STRING, Config::getWorldNames);

    logger.debug("Config loaded!");
  }

  public void save() {
    try {
      rootNode
          .getNode(ACTIVE_WORLDS)
          .setValue(
              activeWorlds
                  .stream()
                  .map(Sponge.getGame().getServer()::getWorld)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .map(World::getName)
                  .collect(Collectors.toList()));

      loader.save(rootNode);

      logger.debug("Config saved!");
    } catch (IOException | NullPointerException e) {
      logger.error("Config could not be saved!", e);
    }
  }

  public List<World> getActiveWorlds() {
    return activeWorlds
        .stream()
        .map(Sponge.getGame().getServer()::getWorld)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  public void setWorld(String world, boolean enabled) {
    boolean contains = activeWorlds.contains(world);

    if (!contains && enabled) {
      activeWorlds.add(world);
    } else if (contains && !enabled) {
      activeWorlds.remove(world);
    }
  }
}
