package it.crystalnest.server_sided_portals.api;

import net.minecraft.world.level.portal.PortalInfo;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Any Entity that can travel between dimensions.
 */
@ApiStatus.Internal
public interface Teleportable {
  /**
   * Current custom portal info.
   *
   * @return custom portal info.
   */
  @Nullable
  PortalInfo getCustomPortalInfo();

  /**
   * Sets the current custom portal info.
   *
   * @param info custom portal info.
   */
  void setCustomPortalInfo(@Nullable PortalInfo info);

  /**
   * Returns a copy of the current custom portal info and sets to {@code null} the reference instance.
   *
   * @return current custom portal info.
   */
  @Nullable
  PortalInfo consumeCustomPortalInfo();
}
