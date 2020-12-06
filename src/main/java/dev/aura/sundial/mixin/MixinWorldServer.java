package dev.aura.sundial.mixin;

import dev.aura.sundial.AuraSunDial;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldServer.class)
public abstract class MixinWorldServer {
  @Inject(method = "wakeAllPlayers", at = @At("RETURN"))
  protected void onWakeAllPlayers(CallbackInfo ci) {
    AuraSunDial.getInstance().processTimeSkip((World) this, 0);
  }
}
