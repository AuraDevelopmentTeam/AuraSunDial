package dev.aura.sundial.mixin;

import dev.aura.sundial.AuraSunDial;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldInfo.class, priority = 1001)
@Implements(@Interface(iface = WorldProperties.class, prefix = "worldproperties$"))
public abstract class MixinWorldInfo implements WorldProperties {
  @Shadow
  public abstract void shadow$setWorldTime(long time);

  @Intrinsic
  public void worldproperties$setWorldTime(final long time) {
    this.shadow$setWorldTime(time);

    AuraSunDial.getLogger().info("worldproperties$setWorldTime: Time set to " + time);
    AuraSunDial.getLogger()
        .info(
            "worldproperties$setWorldTime: "
                + Sponge.getCauseStackManager().getCurrentCause().toString());
  }

  @Inject(method = "setWorldTime", at = @At("RETURN"))
  public void onSetWorldTime(long time, CallbackInfo ci) {
    // new NullPointerException().printStackTrace();

    // Skip our own changes
    /*if (Sponge.getCauseStackManager()
    .getContext(EventContextKeys.PLUGIN)
    .map(PluginContainer::getId)
    .filter(AuraSunDial.ID::equals)
    .isPresent()) return;*/

    AuraSunDial.getLogger().info("onSetWorldTime: Time set to " + time);
    AuraSunDial.getLogger().info("onSetWorldTime: " + ci.toString());
    AuraSunDial.getLogger()
        .info("onSetWorldTime: " + Sponge.getCauseStackManager().getCurrentCause().toString());
  }
}
