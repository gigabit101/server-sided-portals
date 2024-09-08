package it.crystalnest.server_sided_portals.mixin;

import it.crystalnest.server_sided_portals.api.CustomPortalChecker;
import it.crystalnest.server_sided_portals.api.EntityPortal;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

/**
 * Injects into {@link Entity} to alter dimension travel.
 */
@Mixin(Entity.class)
public abstract class ForgeEntityMixin implements EntityPortal {
  /**
   * Shadowed {@link Entity#level()}.
   *
   * @return level.
   */
  @Shadow
  public abstract Level level();

  /**
   * Redirects the call to {@link Entity#level()} inside the method {@link Entity#handleNetherPortal()}.<br />
   * Optionally changes the destination dimension.
   *
   * @param instance {@link MinecraftServer} owning the redirected method.
   * @param worldKey dimension key.
   * @return correct destination dimension.
   */
  @Redirect(method = "handleNetherPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"))
  private ServerLevel redirectGetLevel(MinecraftServer instance, ResourceKey<Level> worldKey) {
    return CustomPortalChecker.getPortalDestination((ServerLevel) level(), Objects.requireNonNull(instance.getLevel(worldKey)), portalEntrancePos());
  }
}
