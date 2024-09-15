package it.crystalnest.server_sided_portals.api;

import it.crystalnest.server_sided_portals.Constants;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Objects;

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
    return ((CustomPortalChecker) new PortalShape(level, pos, level.getBlockState(pos).getOptionalValue(NetherPortalBlock.AXIS).orElse(Axis.X))).dimension();
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
    return TagKey.create(Registry.BLOCK_REGISTRY, dimension.location());
  }

  /**
   * Returns a random Block for the Custom Portal frame related to the given dimension.
   *
   * @param level dimension.
   * @return a random Block for the Custom Portal frame.
   */
  static Block getCustomPortalFrameBlock(Level level) {
    return Registry.BLOCK.getTag(getCustomPortalFrameBlockTag(level.dimension())).map(holders -> holders.getRandomElement(level.getRandom()).orElse(Holder.direct(Blocks.OBSIDIAN)).value()).orElse(Blocks.OBSIDIAN);
  }

  /**
   * Returns the correct portal destination.
   *
   * @param origin current dimension.
   * @param destination default destination dimension.
   * @param pos entrance position.
   * @return the correct dimension the entity should travel to.
   */
  static ServerLevel getPortalDestination(ServerLevel origin, ServerLevel destination, BlockPos pos) {
    if (destination.dimension() == Level.NETHER && CustomPortalChecker.isCustomPortal(origin, pos)) {
      return origin.getServer().getLevel(origin.dimension() == Level.OVERWORLD ? Objects.requireNonNull(CustomPortalChecker.getPortalDimension(origin, pos)) : Level.OVERWORLD);
    }
    return destination;
  }

  /**
   * Returns the exit portal info for traveling from or to a custom dimension.
   *
   * @param entity entity traveling to a new dimension.
   * @param destination default dimension.
   * @return {@link PortalInfo} for a custom dimension portal.
   */
  @ApiStatus.Internal
  static PortalInfo getCustomPortalInfo(Entity entity, ServerLevel destination) {
    return ((EntityPortal) entity).exitPortal(destination, entity.blockPosition(), false, destination.getWorldBorder()).map(rect -> {
      Vec3 vec3d;
      Axis axis;
      BlockState blockState = entity.level.getBlockState(((EntityPortal) entity).portalEntrancePos());
      if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
        axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
        BlockUtil.FoundRectangle rectangle = BlockUtil.getLargestRectangleAround(((EntityPortal) entity).portalEntrancePos(), axis, PortalShape.MAX_WIDTH, Axis.Y, PortalShape.MAX_HEIGHT, pos -> entity.level.getBlockState(pos) == blockState);
        vec3d = ((EntityPortal) entity).relativePortalPosition(axis, rectangle);
      } else {
        axis = Axis.X;
        vec3d = new Vec3(0.5, 0.0, 0.0);
      }
      return PortalShape.createPortalInfo(destination, rect, axis, vec3d, entity.getDimensions(entity.getPose()), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
    }).orElse(null);
  }

  /**
   * Whether the portal is a Custom Portal.
   *
   * @return whether the portal is a Custom Portal.
   */
  ResourceKey<Level> dimension();
}
