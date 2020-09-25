package dev.aura.sundial.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

@SuppressFBWarnings(
    value = "JLM_JSR166_UTILCONCURRENT_MONITORENTER",
    justification = "Code is generated by lombok which means I don\'t have any influence on it.")
@Value
public class TimeCalculator {
  private static final long secondsInRealDay = TimeUnit.DAYS.toSeconds(1);
  private static final long ticksInMinecraftDay = 24000;
  private static final long ticksInMinecraftHour = ticksInMinecraftDay / 24;
  private static final long midnightOffset = (ticksInMinecraftDay * 3) / 4;

  private final double offset;
  private final double speedModifier;
  private final Map<World, Long> perWorldOffset = new HashMap<>();

  @Getter(value = AccessLevel.PACKAGE, lazy = true)
  private final long offsetTicks = generateOffsetTicks();

  public long getWorldTime() {
    return getWorldTime(Calendar.getInstance());
  }

  public long getWorldTime(final World world) {
    return getWorldTime(Calendar.getInstance(), world);
  }

  public long getWorldTime(final Calendar calendar) {
    return getWorldTime(calendar, null);
  }

  public long getWorldTime(final Calendar calendar, final World world) {
    final long seconds =
        ((long)
                ((calendar.getTimeInMillis() + calendar.getTimeZone().getRawOffset())
                    * speedModifier))
            / 1000L;

    return Math.floorMod(
        ((seconds * ticksInMinecraftDay) / secondsInRealDay)
            + midnightOffset
            + getOffsetTicks()
            + getPerWorldOffset(world),
        ticksInMinecraftDay);
  }

  public void addPerWorldOffset(World world, long offset) {
    // ((a % b) + b) % b is the positive modulo (a simple modulo allows negative values)
    perWorldOffset.compute(
        world,
        (w, o) ->
            ((((o == null) ? offset : (o + offset)) % ticksInMinecraftDay) + ticksInMinecraftDay)
                % ticksInMinecraftDay);
  }

  public void updatePerWorldOffset(World world) {
    final WorldProperties properties = world.getProperties();
    final long targetWorldTime = getPerWorldOffset(world);
    final long actualWorldTime = properties.getWorldTime();

    addPerWorldOffset(world, actualWorldTime - targetWorldTime);
  }

  public long getPerWorldOffset(World world) {
    return (world == null) ? 0L : perWorldOffset.getOrDefault(world, 0L);
  }

  private long generateOffsetTicks() {
    return (long) (offset * ticksInMinecraftHour);
  }
}