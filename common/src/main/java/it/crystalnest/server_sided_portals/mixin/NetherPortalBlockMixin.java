package it.crystalnest.server_sided_portals.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.crystalnest.server_sided_portals.api.CustomPortalChecker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Injects into {@link NetherPortalBlock} to alter Custom Portals mob spawn and dimension travel.
 */
@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {
  /**
   * Modifies the condition returned by {@link BlockState#isValidSpawn(BlockGetter, BlockPos, EntityType)} inside the method {@link NetherPortalBlock#randomTick(BlockState, ServerLevel, BlockPos, RandomSource)}.<br />
   * Prevents Zombified Piglins spawn when it's a Custom Portal.
   *
   * @param original original condition value.
   * @param state block state.
   * @param level dimension.
   * @param pos position.
   * @param rand random source.
   * @return whether spawning is allowed.
   */
  @ModifyExpressionValue(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isValidSpawn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntityType;)Z"))
  private boolean modifyIsValidSpawn(boolean original, BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
    return original && !CustomPortalChecker.isCustomPortal(level, pos.above());
  }
}