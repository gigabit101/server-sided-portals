package it.crystalnest.server_sided_portals.api;

import it.crystalnest.server_sided_portals.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.PortalShape;

import java.util.List;
import java.util.Optional;

/**
 * Handles checking whether a portal frame is for a Custom Portal.
 */
public interface CustomPortalChecker {
  /**
   * Gets the Custom Dimension related to the Custom Portal.
   *
   * @param level dimension.
   * @param pos position.
   * @return portal related dimension.
   */
  static ResourceKey<Level> getPortalDimension(Level level, BlockPos pos) {
    Optional<PortalShape> op = PortalShape.findEmptyPortalShape(level, pos, level.getBlockState(pos).getOptionalValue(NetherPortalBlock.AXIS).orElse(Axis.X));
    PortalShape shape = op.orElse(null);
    return ((CustomPortalChecker) shape).dimension();
  }

  /**
   * Checks whether the Portal at the given position is for the given dimension.
   *
   * @param level current dimension.
   * @param pos position.
   * @param dimension target dimension.
   * @return whether the Portal at the given position is for the given dimension.
   */
  static boolean isPortalForDimension(Level level, BlockPos pos, ResourceKey<Level> dimension) {
    return getPortalDimension(level, pos) == dimension;
  }

  /**
   * Checks whether the Portal at the given position is for the specified dimension.
   *
   * @param level current dimension.
   * @param pos position.
   * @param dimension name of the target dimension.
   * @return whether the Portal at the given position is for the specified dimension.
   */
  static boolean isPortalForDimension(Level level, BlockPos pos, String dimension) {
    ResourceLocation dimensionKey = getPortalDimension(level, pos).location();
    return dimensionKey.getNamespace().equals(Constants.MOD_ID) && dimensionKey.getPath().equalsIgnoreCase(dimension);
  }

  /**
   * Checks whether there is a Custom Portal in the given dimension at the given position.
   *
   * @param level dimension.
   * @param pos position.
   * @return whether there is a Nightworld portal.
   */
  static boolean isCustomPortal(Level level, BlockPos pos) {
    return isCustomDimension(getPortalDimension(level, pos));
  }

  /**
   * Returns the list of Custom Dimensions.
   *
   * @param server {@link ServerLevel}.
   * @return the list of Custom Dimensions.
   */
  static List<ResourceKey<Level>> getCustomDimensions(ServerLevel server) {
    return server.getServer().levelKeys().stream().filter(CustomPortalChecker::isCustomDimension).toList();
  }

  /**
   * Whether the given dimension is a Custom one.
   *
   * @param level dimension.
   * @return whether the given dimension is a Custom one.
   */
  static boolean isCustomDimension(Level level) {
    return isCustomDimension(level.dimension());
  }

  /**
   * Whether the given dimension is a Custom one.
   *
   * @param dimension dimension key.
   * @return whether the given dimension is a Custom one.
   */
  static boolean isCustomDimension(ResourceKey<Level> dimension) {
    return Constants.MOD_ID.equals(dimension.location().getNamespace());
  }

  /**
   * Returns the Block Tag for the Custom Portal frame related to the given dimension.
   *
   * @param dimension dimension.
   * @return Block Tag for the Custom Portal frame.
   */
  static TagKey<Block> getCustomPortalFrameBlockTag(ResourceKey<Level> dimension) {
    return TagKey.create(Registries.BLOCK, dimension.location());
  }

  /**
   * Returns a random Block for the Custom Portal frame related to the given dimension.
   *
   * @param level dimension.
   * @return a random Block for the Custom Portal frame.
   */
  static Block getCustomPortalFrameBlock(Level level) {
    return BuiltInRegistries.BLOCK.get(getCustomPortalFrameBlockTag(level.dimension())).map(holders -> holders.getRandomElement(level.getRandom()).orElse(Holder.direct(Blocks.OBSIDIAN)).value()).orElse(Blocks.OBSIDIAN);
  }

  /**
   * Whether the portal is a Custom Portal.
   *
   * @return whether the portal is a Custom Portal.
   */
  ResourceKey<Level> dimension();
}
