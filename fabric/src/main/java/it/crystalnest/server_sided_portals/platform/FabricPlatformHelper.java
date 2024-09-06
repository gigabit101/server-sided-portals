package it.crystalnest.server_sided_portals.platform;

import it.crystalnest.cobweb.platform.model.Platform;
import it.crystalnest.server_sided_portals.platform.services.PlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric platform helper.
 */
public final class FabricPlatformHelper implements PlatformHelper {
  @Override
  public Platform getPlatformName() {
    return Platform.FABRIC;
  }

  @Override
  public boolean isModLoaded(String modId) {
    return FabricLoader.getInstance().isModLoaded(modId);
  }

  @Override
  public boolean isDevEnv() {
    return FabricLoader.getInstance().isDevelopmentEnvironment();
  }
}
