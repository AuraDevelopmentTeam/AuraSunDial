package dev.aura.sundial.permission;

import dev.aura.sundial.AuraSunDial;
import dev.aura.sundial.command.CommandRealTime;
import dev.aura.sundial.command.CommandReload;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

@RequiredArgsConstructor
public class PermissionRegistry {
  public static final String BASE = AuraSunDial.ID;
  public static final String COMMAND = BASE + ".command";

  private final AuraSunDial plugin;
  private final PermissionService service =
      Sponge.getServiceManager().provide(PermissionService.class).get();

  public void registerPermissions() {
    registerPermission(BASE, PermissionDescription.ROLE_ADMIN);
    registerPermission(COMMAND, "Permission for all commands", PermissionDescription.ROLE_ADMIN);

    registerPermission(
        CommandReload.RELOAD_PERMISSION,
        "Permission to be able to reload the plugin",
        PermissionDescription.ROLE_ADMIN);

    registerPermission(
        CommandRealTime.REALTIME_PERMISSION,
        "Allows the user to execute the realtime command.",
        PermissionDescription.ROLE_STAFF);
    registerPermission(
        CommandRealTime.REALTIME_PERMISSION + ".enable",
        "Allows the user to to enable realtime on all worlds.",
        PermissionDescription.ROLE_STAFF);
    registerPermission(
        CommandRealTime.REALTIME_PERMISSION + ".enable.<world>",
        "Allows the user to to enable realtime on the specific world.",
        PermissionDescription.ROLE_STAFF);
    registerPermission(
        CommandRealTime.REALTIME_PERMISSION + ".disable",
        "Allows the user to to disable realtime on all worlds.",
        PermissionDescription.ROLE_STAFF);
    registerPermission(
        CommandRealTime.REALTIME_PERMISSION + ".disable.<world>",
        "Allows the user to to disable realtime on the specific world.",
        PermissionDescription.ROLE_STAFF);
  }

  private Builder getBuilder() {
    return service.newDescriptionBuilder(plugin);
  }

  private void registerPermission(String permission, String role) {
    registerPermission(permission, null, role);
  }

  private void registerPermission(String permission, @Nullable String description, String role) {
    getBuilder()
        .id(permission)
        .description((description == null) ? Text.of() : Text.of(description))
        .assign(role, true)
        .register();
  }
}
