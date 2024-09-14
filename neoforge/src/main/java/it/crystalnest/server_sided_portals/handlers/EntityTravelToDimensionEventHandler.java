package it.crystalnest.server_sided_portals.handlers;

import it.crystalnest.server_sided_portals.api.CustomPortalChecker;
import it.crystalnest.server_sided_portals.api.Teleportable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

import java.util.Objects;

/**
 * {@link EntityTravelToDimensionEvent} handler.
 */
@EventBusSubscriber(bus = Bus.FORGE)
public final class EntityTravelToDimensionEventHandler {
  private EntityTravelToDimensionEventHandler() {}

  /**
   * Handles the {@link EntityTravelToDimensionEvent} by optionally setting the custom portal info for the entity.
   *
   * @param event {@link EntityTravelToDimensionEvent}.
   */
  @SubscribeEvent
  public static void handle(EntityTravelToDimensionEvent event) {
    Entity entity = event.getEntity();
    MinecraftServer server = entity.getServer();
    if (server != null && !entity.isRemoved() && (CustomPortalChecker.isCustomDimension(entity.level().dimension()) || CustomPortalChecker.isCustomDimension(event.getDimension()))) {
      ((Teleportable) entity).setCustomPortalInfo(CustomPortalChecker.getCustomPortalInfo(entity, Objects.requireNonNull(server).getLevel(event.getDimension())));
    }
  }
}
