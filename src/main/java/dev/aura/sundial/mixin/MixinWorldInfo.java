package dev.aura.sundial.mixin;

import dev.aura.sundial.AuraSunDial;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldInfo.class)
public class MixinWorldInfo {
  @Inject(method = "setWorldTime", at = @At("RETURN"))
  public void onSetWorldTime(long time, CallbackInfo ci) {
    // Skip our own changes
    if (Sponge.getCauseStackManager()
        .getContext(EventContextKeys.PLUGIN)
        .map(PluginContainer::getId)
        .filter(AuraSunDial.ID::equals)
        .isPresent()) return;

    AuraSunDial.getLogger().info("WorldInfo: Time set to " + time);
    AuraSunDial.getLogger().info(ci.toString());
    AuraSunDial.getLogger().info(Sponge.getCauseStackManager().getCurrentCause().toString());
  }
}
