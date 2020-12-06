package dev.aura.sundial.mixin;

import dev.aura.sundial.AuraSunDial;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldInfo.class, priority = 1001)
@Implements(@Interface(iface = WorldProperties.class, prefix = "worldproperties$"))
public abstract class MixinWorldInfo implements WorldProperties {
  @Inject(method = "setWorldTime(J)V", at = @At("RETURN"))
  public void onSetWorldTime(long time, CallbackInfo ci) {
    onSetWorldTimeCommon(time);
  }

  // Not using "setWorldTime(J)V" because mixins use strings to determine the target, so the method
  // strings need to be different because we actually want to target different methods
  @Inject(remap = false, method = "setWorldTime", at = @At("RETURN"))
  public void onSetWorldTimeSponge(long time, CallbackInfo ci) {
    onSetWorldTimeCommon(time);
  }

  private void onSetWorldTimeCommon(long time) {
    if (Sponge.getCauseStackManager()
        .getCurrentCause()
        .first(PluginContainer.class)
        .map(PluginContainer::getId)
        .filter(AuraSunDial.ID::equals)
        .isPresent()) return;
    if (Sponge.getCauseStackManager().getCurrentCause().containsType(Game.class)) return;

    final World world =
        Sponge.getServer()
            .getWorld(this.getUniqueId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No corresponding World instance for this WorldProperties instance found"));

    AuraSunDial.getInstance().processTimeSkip(world, time);
  }
}
