package dev.aura.sundial.command;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
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

import com.google.common.collect.ImmutableMap;

import dev.aura.sundial.AuraSunDial;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandRealTime implements CommandExecutor {
	public static void register(AuraSunDial plugin) {
		CommandSpec realTime = CommandSpec.builder()
				.description(Text.of("Enables or disables synchronizing the world time with realtime."))
				.permission("sundial.command.realtime").executor(new CommandRealTime())
				.arguments(
						GenericArguments.choices(Text.of("mode"),
								ImmutableMap.<String, Boolean>builder().put("enable", true).put("disable", false)
										.build(),
								true),
						GenericArguments.optional(GenericArguments.firstParsing(
								GenericArguments.allOf(GenericArguments.world(Text.of("world"))),
								GenericArguments.literal(Text.of("all"), "all"))))
				.build();

		Sponge.getCommandManager().register(plugin, realTime, "realtime", "rt", "sundial", "sd");
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Collection<WorldProperties> worlds;

		if (args.hasAny("world")) {
			worlds = args.<WorldProperties>getAll("world");
		} else if (args.hasAny("all")) {
			worlds = Sponge.getGame().getServer().getAllWorldProperties();
		} else if (src instanceof Locatable) {
			worlds = Collections.singleton(((Player) src).getWorld().getProperties());
		} else {
			throw new CommandException(Text.of("You have to enter a world when using this from the console!"), true);
		}

		final boolean mode = args.<Boolean>getOne("mode").get();
		final String permission = "sundial.command.realtime." + (mode ? "enable" : "disable") + '.';

		worlds.stream().map(WorldProperties::getWorldName).filter(world -> src.hasPermission(permission + world))
				.forEach(world -> AuraSunDial.getConfig().setWorld(world, mode));
		
		AuraSunDial.getConfig().save();

		src.sendMessage(Text.of((mode ? "Enabled" : "Disabled") + " realtime on these worlds: "
				+ worlds.stream().map(WorldProperties::getWorldName).collect(Collectors.joining(", "))));

		return CommandResult.successCount(worlds.size());
	}
}
