package dev.aura.sundial.mixin;

import dev.aura.sundial.AuraSunDial;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldInfo.class, priority = 899)
@Implements(@Interface(iface = WorldProperties.class, prefix = "worldproperties$"))
public abstract class MixinWorldInfo implements WorldProperties {
  @Inject(method = "setWorldTime(J)V", at = @At("RETURN"))
  public void onSetWorldTime(long time, CallbackInfo ci) {
    onSetWorldTimeCommon(time, "onSetWorldTime", ci);
  }

  @Inject(remap = false, method = "setWorldTime", at = @At("RETURN"))
  public void onSetWorldTimeSponge(long time, CallbackInfo ci) {
    onSetWorldTimeCommon(time, "onSetWorldTimeSponge", ci);
  }

  private void onSetWorldTimeCommon(long time, String name, CallbackInfo ci) {
    if (Sponge.getCauseStackManager()
        .getCurrentCause()
        .first(PluginContainer.class)
        .map(PluginContainer::getId)
        .filter(AuraSunDial.ID::equals)
        .isPresent()) {
      log(name, "filtered: AuraSunDial!");
      return;
    }
    if (Sponge.getCauseStackManager().getCurrentCause().containsType(Game.class)) {
      log(name, "filtered: Sponge/Vanilla!");
      return;
    }

    log(name, "Time set to " + time);
    log(name, ci);
    log(name, Sponge.getCauseStackManager().getCurrentCause());
  }

  private void log(String name, Object message) {
    AuraSunDial.getLogger().info(name + ": " + message.toString());
  }
}
