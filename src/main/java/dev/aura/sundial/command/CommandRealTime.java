package dev.aura.sundial.command;

import com.google.common.collect.ImmutableMap;
import dev.aura.sundial.AuraSunDial;
import dev.aura.sundial.message.PluginMessages;
import dev.aura.sundial.permission.PermissionRegistry;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.storage.WorldProperties;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandRealTime implements CommandExecutor {
  public static final String BASE_PERMISSION = PermissionRegistry.COMMAND;
  public static final String REALTIME_PERMISSION = BASE_PERMISSION + ".realtime";

  private static final String PARAM_MODE = "mode";
  private static final String PARAM_WORLD = "world";
  private static final String PARAM_ALL = "all";

  public static void register(AuraSunDial plugin) {
    CommandSpec realTime =
        CommandSpec.builder()
            .description(Text.of("Enables or disables synchronizing the world time with realtime."))
            .executor(new CommandRealTime())
            .child(CommandReload.create(plugin), "reload", "r", "rl", "re", "rel")
            .arguments(
                GenericArguments.choices(
                    Text.of(PARAM_MODE),
                    ImmutableMap.<String, Boolean>builder()
                        .put("enable", true)
                        .put("disable", false)
                        .build(),
                    true),
                GenericArguments.optional(
                    GenericArguments.firstParsing(
                        GenericArguments.allOf(GenericArguments.world(Text.of(PARAM_WORLD))),
                        GenericArguments.literal(Text.of(PARAM_ALL), PARAM_ALL))))
            .build();

    Sponge.getCommandManager().register(plugin, realTime, "realtime", "rt", "sundial", "sd");
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    Collection<WorldProperties> worlds;

    if (args.hasAny(PARAM_WORLD)) {
      worlds = args.getAll(PARAM_WORLD);
    } else if (args.hasAny(PARAM_ALL)) {
      worlds = Sponge.getGame().getServer().getAllWorldProperties();
    } else if (src instanceof Locatable) {
      worlds = Collections.singleton(((Player) src).getWorld().getProperties());
    } else throw new CommandException(PluginMessages.ERROR_SPECIFY_WORLD.getMessage(), true);

    final boolean mode = args.<Boolean>getOne(PARAM_MODE).get();
    final String permission = REALTIME_PERMISSION + '.' + (mode ? "enable" : "disable") + '.';
    final List<String> worldNames =
        worlds.stream()
            .map(WorldProperties::getWorldName)
            .filter(world -> src.hasPermission(permission + world))
            .collect(Collectors.toList());

    worldNames.forEach(
        world -> AuraSunDial.getConfig().getTimeModification().setWorld(world, mode));

    try {
      AuraSunDial.getInstance().saveConfig();
    } catch (IOException | ObjectMappingException e) {
      AuraSunDial.getLogger().error("Config could not be saved!", e);
    }

    if (worldNames.size() > 0) {
      src.sendMessage(
          (mode ? PluginMessages.ENABLE_REALTIME : PluginMessages.DISABLE_REALTIME)
              .getMessage(ImmutableMap.of("worlds", String.join(", ", worldNames))));

    } else throw new CommandPermissionException();

    return CommandResult.successCount(worldNames.size());
  }
}
