package dev.aura.sundial.config;

import dev.aura.sundial.AuraSunDial;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.transformation.ConfigurationTransformation;

@UtilityClass
public class ConfigMigrations {
  private static final int VERSION_LATEST = 1;
  private static final Object[] versionPath = {"version"};

  public static <N extends ConfigurationNode> N migrateConfig(final N node) {
    if (!node.isVirtual()) {
      final ConfigurationTransformation trans = create();
      final int start_version = node.getNode(versionPath).getInt(-1);

      trans.apply(node);

      if (start_version != VERSION_LATEST) {
        AuraSunDial.getLogger()
            .info("Updated config schema from " + start_version + " to " + VERSION_LATEST);

        // Getting it again because it might have been initialized
        final ConfigurationNode versionNode = node.getNode(versionPath);

        if (versionNode instanceof CommentedConfigurationNode)
          ((CommentedConfigurationNode) versionNode)
              .setComment("Config version\n!!! DO NOT CHANGE !!!");
      }
    }

    return node;
  }

  private static ConfigurationTransformation create() {
    return ConfigurationTransformation.versionedBuilder()
        .addVersion(VERSION_LATEST, initialToV1())
        .build();
  }

  private static ConfigurationTransformation initialToV1() {
    final MoveActionHelper moveHelper =
        new MoveActionHelper(new String[] {}, new String[] {"timeModification"});

    ConfigurationTransformation.Builder builder = ConfigurationTransformation.builder();

    builder = moveHelper.addMoveAction("activeWorlds", builder);
    builder = moveHelper.addMoveAction("offset", builder);
    builder = moveHelper.addMoveAction("speedModifier", builder);
    builder = moveHelper.addMoveAction("syncWithRealTime", builder);

    return builder.build();
  }

  @RequiredArgsConstructor
  private static class MoveActionHelper {
    private final Object[] startBasePath;
    private final Object[] endBasePath;

    public ConfigurationTransformation.Builder addMoveAction(
        Object nodePath, ConfigurationTransformation.Builder builder) {
      final Object[] startPath = appendNodePath(startBasePath, nodePath);
      final Object[] endPath = appendNodePath(endBasePath, nodePath);

      return builder.addAction(startPath, (path, value) -> endPath);
    }

    private static Object[] appendNodePath(Object[] basePath, Object nodePath) {
      final Object[] path = Arrays.copyOf(basePath, basePath.length + 1);
      path[basePath.length] = nodePath;

      return path;
    }
  }
}
