package it.crystalnest.server_sided_portals.mixin;

import it.crystalnest.server_sided_portals.api.CustomPortalChecker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * Injects into {@link PortalShape} to alter dimension travel.
 */
@Mixin(PortalShape.class)
public abstract class PortalShapeMixin implements CustomPortalChecker {
  @Final
  @Shadow
  private Direction rightDir;

  @Shadow
  private int numPortalBlocks;

  @Shadow
  @Nullable
  private BlockPos bottomLeft;

  @Final
  @Shadow
  private int width;

  @Shadow
  private int height;

  @Unique
  private ResourceKey<Level> dimension = Level.NETHER;

  @Shadow
  private static boolean isEmpty(BlockState state) {
    throw new UnsupportedOperationException("Tried to call a dummy body of a shadowed method: PortalShape#isEmpty(BlockState)");
  }

  @Shadow
  public abstract boolean isValid();

  @Mutable
  @Accessor("width")
  protected abstract void setWidth(int width);

  @Override
  public ResourceKey<Level> dimension() {
    return dimension;
  }

  @Inject(method = "<init>", at = @At(value = "TAIL"))
  private void onInit(Axis p_77697_, int p_374222_, Direction p_374407_, BlockPos p_77696_, int p_374218_, int p_374477_, CallbackInfo ci) {
    if (!level.isClientSide()) {
      ServerLevel serverLevel = (ServerLevel) level;
      if (this.isValid() && CustomPortalChecker.isCustomDimension(serverLevel)) {
        // If it's a Nether Portal, and we are in a Custom Dimension, prevent creating the portal.
        this.bottomLeft = null;
        this.setWidth(1);
        height = 1;
      } else if (!isValid() && (serverLevel.dimension() == Level.OVERWORLD || CustomPortalChecker.isCustomDimension(serverLevel))) {
        // If it's not a Nether Portal, and we are either in the Overworld or in a Custom Dimension, check if it's a Custom Portal.
        for (ResourceKey<Level> dim : CustomPortalChecker.getCustomDimensions(serverLevel)) {
          TagKey<Block> frameBlock = CustomPortalChecker.getCustomPortalFrameBlockTag(dim);
          bottomLeft = calculateBottomLeftForCustomDimension(blockGetter, pos, frameBlock);
          if (bottomLeft != null) {
            setWidth(calculateWidthForCustomDimension(blockGetter, frameBlock));
            if (width > 0) {
              height = calculateHeightForCustomDimension(blockGetter, frameBlock);
              this.dimension = dim;
              // The first Custom Dimension to match breaks the loop and validates the Custom Portal.
              break;
            }
          }
        }
        // If, after checking all Custom Dimensions, the portal is not valid, prevent creating the portal.
        if (bottomLeft == null) {
          bottomLeft = pos;
          setWidth(1);
          height = 1;
        }
      }
    }
  }

  @Unique
  @Nullable
  @SuppressWarnings({"ConstantValue", "StatementWithEmptyBody"})
  private BlockPos calculateBottomLeftForCustomDimension(BlockGetter blockGetter, BlockPos pos, TagKey<Block> frameBlock) {
    for (int i = Math.max(blockGetter.getMinY(), pos.getY() - 21); pos.getY() > i && isEmpty(blockGetter.getBlockState(pos.below())); pos = pos.below());
    Direction direction = rightDir.getOpposite();
    int j = getDistanceUntilEdgeAboveFrameForCustomDimension(blockGetter, frameBlock, pos, direction) - 1;
    return j < 0 ? null : pos.relative(direction, j);
  }

  @Unique
  private int calculateWidthForCustomDimension(BlockGetter blockGetter, TagKey<Block> frameBlock) {
    int i = getDistanceUntilEdgeAboveFrameForCustomDimension(blockGetter, frameBlock, bottomLeft, rightDir);
    return i >= 2 && i <= 21 ? i : 0;
  }

  @Unique
  @SuppressWarnings("ConstantValue")
  private int getDistanceUntilEdgeAboveFrameForCustomDimension(BlockGetter blockGetter, TagKey<Block> frameBlock, BlockPos pos, Direction direction) {
    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    for (int i = 0; i <= 21; i++) {
      BlockState state = blockGetter.getBlockState(mutablePos.set(pos).move(direction, i));
      if (!isEmpty(state)) {
        if (state.is(frameBlock)) {
          return i;
        }
        break;
      }
      if (!blockGetter.getBlockState(mutablePos.move(Direction.DOWN)).is(frameBlock)) {
        break;
      }
    }
    return 0;
  }

  @Unique
  private int calculateHeightForCustomDimension(BlockGetter blockGetter, TagKey<Block> frameBlock) {
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    int i = this.getDistanceUntilTopForCustomDimension(blockGetter, frameBlock, pos);
    return i >= 3 && i <= 21 && hasTopFrameForCustomDimension(blockGetter, frameBlock, pos, i) ? i : 0;
  }

  @Unique
  private boolean hasTopFrameForCustomDimension(BlockGetter blockGetter, TagKey<Block> frameBlock, BlockPos.MutableBlockPos pos, int height) {
    for (int i = 0; i < this.width; ++i) {
      BlockPos.MutableBlockPos mutablePos = pos.set(Objects.requireNonNull(this.bottomLeft)).move(Direction.UP, height).move(this.rightDir, i);
      if (!blockGetter.getBlockState(mutablePos).is(frameBlock)) {
        return false;
      }
    }
    return true;
  }

  @Unique
  private int getDistanceUntilTopForCustomDimension(BlockGetter blockGetter, TagKey<Block> frameBlock, BlockPos.MutableBlockPos pos) {
    for (int i = 0; i < 21; i++) {
      pos.set(Objects.requireNonNull(this.bottomLeft)).move(Direction.UP, i).move(this.rightDir, -1);
      if (!blockGetter.getBlockState(pos).is(frameBlock)) {
        return i;
      }
      pos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, this.width);
      if (!blockGetter.getBlockState(pos).is(frameBlock)) {
        return i;
      }
      for (int j = 0; j < this.width; ++j) {
        pos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, j);
        BlockState blockstate = blockGetter.getBlockState(pos);
        if (!isEmpty(blockstate)) {
          return i;
        }
        if (blockstate.is(Blocks.NETHER_PORTAL)) {
          ++this.numPortalBlocks;
        }
      }
    }
    return 21;
  }
}
