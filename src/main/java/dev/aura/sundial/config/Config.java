package dev.aura.sundial.config;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

@ConfigSerializable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {
  @Setting(comment = "A list of all the worlds AuraSunDial synchronizes the in-game time with.")
  private List<String> activeWorlds = getWorldNames();

  @Setting(
    comment =
        "Allows the time to be offset. The value is in hours and accepts decimal and negative values.\n"
            + "So to offset the time to 1:30 earlier, use the value -1.5."
  )
  @Getter
  private double offset = 0.0;

  @Setting(
    comment =
        "Allows you to speed up or slow down the passage of time. Values above 1.0 make the days go faster, values below make it go\n"
            + "slower.\n"
            + "So if you want two game days to pass in a single real life day, set the value to 2.0. If you want to two real life days to\n"
            + "pass for a single in game day, set the value to 0.5."
  )
  @Getter
  private double speedModifier = 1.0;

  private static List<String> getWorldNames() {
    return Sponge.getGame()
        .getServer()
        .getWorlds()
        .stream()
        .map(World::getName)
        .collect(Collectors.toList());
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
