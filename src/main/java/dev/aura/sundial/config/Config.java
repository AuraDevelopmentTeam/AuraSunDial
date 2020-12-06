package dev.aura.sundial.config;

import com.google.common.collect.ImmutableList;
import dev.aura.lib.messagestranslator.MessagesTranslator;
import dev.aura.sundial.AuraSunDial;
import dev.aura.sundial.util.TimeCalculator;
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
  @Setting private General general = new General();
  @Setting private TimeModification timeModification = new TimeModification();

  @ConfigSerializable
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class General {
    @Setting(
        comment =
            "Select which language from the lang dir to use.\n"
                + "You can add your own translations in there. If you name your file \"test.lang\", choose \"test\" here.")
    private String language = MessagesTranslator.DEFAULT_LANGUAGE;
  }

  @ConfigSerializable
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TimeModification {
    @Setting(comment = "A list of all the worlds AuraSunDial synchronizes the in-game time with.")
    private List<String> activeWorlds = getWorldNames();

    @Setting(
        comment =
            "Allows the time to be offset. The value is in hours and accepts decimal and negative values.\n"
                + "So to offset the time to 1:30 earlier, use the value -1.5.\n"
                + "Useless if `syncWithRealTime` is disabled")
    @Getter
    private double offset = 0.0;

    @Setting(
        comment =
            "Allows you to speed up or slow down the passage of time. Values above 1.0 make the days go faster, values below make it go\n"
                + "slower.\n"
                + "So if you want two game days to pass in a single real life day, set the value to 2.0. If you want to two real life days to\n"
                + "pass for a single in game day, set the value to 0.5.")
    @Getter
    private double speedModifier = 1.0;

    @Setting(
        comment =
            "This controls whether the plugin syncs the in game time with real time or just modifies the speed the day passes.\n"
                + "Having this enabled also prevents skipping the night.")
    @Getter
    private boolean syncWithRealTime = true;

    private ImmutableList<World> activeWorldObjects = null;

    private static List<String> getWorldNames() {
      return Sponge.getGame().getServer().getWorlds().stream()
          .map(World::getName)
          .collect(Collectors.toList());
    }

    public ImmutableList<World> getActiveWorlds() {
      synchronized (activeWorlds) {
        if (activeWorldObjects == null)
          activeWorldObjects =
              activeWorlds.stream()
                  .map(Sponge.getGame().getServer()::getWorld)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .collect(ImmutableList.toImmutableList());
      }

      return activeWorldObjects;
    }

    public void setWorld(String world, boolean enabled) {
      boolean contains = activeWorlds.contains(world);

      if (!contains && enabled) {
        synchronized (activeWorlds) {
          activeWorlds.add(world);
          activeWorldObjects = null;

          if (!syncWithRealTime) {
            // We know the world exists
            final World worldObj = Sponge.getGame().getServer().getWorld(world).get();
            final TimeCalculator timeCalculator = AuraSunDial.getInstance().getTimeCalculator();

            timeCalculator.updatePerWorldOffset(worldObj);
          }
        }
      } else if (contains && !enabled) {
        synchronized (activeWorlds) {
          activeWorlds.remove(world);
          activeWorldObjects = null;
        }
      }
    }
  }
}
