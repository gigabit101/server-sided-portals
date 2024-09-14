package it.crystalnest.server_sided_portals;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common shared constants across all loaders.
 */
@ApiStatus.Internal
public final class Constants {
  /**
   * Mod ID.
   */
  public static final String MOD_ID = "server_sided_portals";

  /**
   * Mod logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  /**
   * {@link ThreadLocal} to keep track of a player's origin dimension when teleporting through a custom portal.
   */
  public static final ThreadLocal<ResourceKey<Level>> DIMENSION_ORIGIN_THREAD = ThreadLocal.withInitial(() -> Level.OVERWORLD);

  private Constants() {}
}
