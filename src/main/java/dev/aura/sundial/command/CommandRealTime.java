package dev.aura.sundial.command;

import com.google.common.collect.ImmutableMap;
import dev.aura.sundial.AuraSunDial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.storage.WorldProperties;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandRealTime implements CommandExecutor {
  public static final String BASE_PERMISSION = "sundial.command.realtime";

  private static final String PARAM_MODE = "mode";
  private static final String PARAM_WORLD = "world";
  private static final String PARAM_ALL = "all";

  public static void register(AuraSunDial plugin) {
    CommandSpec realTime =
        CommandSpec.builder()
            .description(Text.of("Enables or disables synchronizing the world time with realtime."))
            .executor(new CommandRealTime())
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

    Sponge.getServiceManager()
        .provide(PermissionService.class)
        .ifPresent(
            permissionService -> {
              permissionService
                  .newDescriptionBuilder(plugin)
                  .id(BASE_PERMISSION)
                  .description(Text.of("Allows the user to execute the realtime command."))
                  .assign(PermissionDescription.ROLE_STAFF, true)
                  .register();
              permissionService
                  .newDescriptionBuilder(plugin)
                  .id(BASE_PERMISSION + ".enable")
                  .description(Text.of("Allows the user to to enable realtime on all worlds."))
                  .assign(PermissionDescription.ROLE_STAFF, true)
                  .register();
              permissionService
                  .newDescriptionBuilder(plugin)
                  .id(BASE_PERMISSION + ".enable.<world>")
                  .description(
                      Text.of("Allows the user to to enable realtime on the specific world."))
                  .assign(PermissionDescription.ROLE_STAFF, true)
                  .register();
              permissionService
                  .newDescriptionBuilder(plugin)
                  .id(BASE_PERMISSION + ".disable")
                  .description(Text.of("Allows the user to to disable realtime on all worlds."))
                  .assign(PermissionDescription.ROLE_STAFF, true)
                  .register();
              permissionService
                  .newDescriptionBuilder(plugin)
                  .id(BASE_PERMISSION + ".disable.<world>")
                  .description(
                      Text.of("Allows the user to to disable realtime on the specific world."))
                  .assign(PermissionDescription.ROLE_STAFF, true)
                  .register();
            });
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    Collection<WorldProperties> worlds;

    if (args.hasAny(PARAM_WORLD)) {
      worlds = args.<WorldProperties>getAll(PARAM_WORLD);
    } else if (args.hasAny(PARAM_ALL)) {
      worlds = Sponge.getGame().getServer().getAllWorldProperties();
    } else if (src instanceof Locatable) {
      worlds = Collections.singleton(((Player) src).getWorld().getProperties());
    } else
      throw new CommandException(
          Text.of("You have to enter a world when using this from the console!"), true);

    final boolean mode = args.<Boolean>getOne(PARAM_MODE).get();
    final String permission = BASE_PERMISSION + '.' + (mode ? "enable" : "disable") + '.';
    final List<String> worldNames =
        worlds
            .stream()
            .map(WorldProperties::getWorldName)
            .filter(world -> src.hasPermission(permission + world))
            .collect(Collectors.toList());

    worldNames.stream().forEach(world -> AuraSunDial.getConfig().setWorld(world, mode));

    AuraSunDial.getConfig().save();

    if (worldNames.size() > 0) {
      src.sendMessage(
          Text.of(
              (mode ? "Enabled" : "Disabled")
                  + " realtime on these worlds: "
                  + worldNames.stream().collect(Collectors.joining(", "))));
    } else throw new CommandPermissionException();

    return CommandResult.successCount(worldNames.size());
  }
}
