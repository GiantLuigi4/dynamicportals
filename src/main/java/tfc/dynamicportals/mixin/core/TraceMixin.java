package tfc.dynamicportals.mixin.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Mixin(BlockGetter.class)
public interface TraceMixin {
//    @Inject(at = @At("TAIL"), method = "clip")
//    default void postClip(ClipContext pContext, CallbackInfoReturnable<BlockHitResult> cir) {
//        System.out.println(cir.getReturnValue());
//    }

    @Shadow
    static <T, C> T traverseBlocks(Vec3 pFrom, Vec3 pTo, C pContext, BiFunction<C, BlockPos, T> pTester, Function<C, T> pOnFail) {
        throw new RuntimeException();
    }

    @Shadow
    BlockState getBlockState(BlockPos p_45571_);

    @Shadow
    FluidState getFluidState(BlockPos pPos);

    @Shadow
    @Nullable
    BlockHitResult clipWithInteractionOverride(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos, VoxelShape pShape, BlockState pState);

    /**
     * @author
     * @reason
     */
    @Overwrite
    default BlockHitResult clip(ClipContext pContext) {
        BlockHitResult bhr = traverseBlocks(pContext.getFrom(), pContext.getTo(), pContext, (p_151359_, p_151360_) -> {
            BlockState blockstate = this.getBlockState(p_151360_);
            FluidState fluidstate = this.getFluidState(p_151360_);
            Vec3 vec3 = p_151359_.getFrom();
            Vec3 vec31 = p_151359_.getTo();
            VoxelShape voxelshape = p_151359_.getBlockShape(blockstate, (BlockGetter) this, p_151360_);
            BlockHitResult blockhitresult = this.clipWithInteractionOverride(vec3, vec31, p_151360_, voxelshape, blockstate);
            VoxelShape voxelshape1 = p_151359_.getFluidShape(fluidstate, (BlockGetter) this, p_151360_);
            BlockHitResult blockhitresult1 = voxelshape1.clip(vec3, vec31, p_151360_);
            double d0 = blockhitresult == null ? Double.MAX_VALUE : p_151359_.getFrom().distanceToSqr(blockhitresult.getLocation());
            double d1 = blockhitresult1 == null ? Double.MAX_VALUE : p_151359_.getFrom().distanceToSqr(blockhitresult1.getLocation());
            return d0 <= d1 ? blockhitresult : blockhitresult1;
        }, (p_275153_) -> {
            Vec3 vec3 = p_275153_.getFrom().subtract(p_275153_.getTo());
            return BlockHitResult.miss(p_275153_.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(p_275153_.getTo()));
        });

        // TODO: mixin plugin?
        {
            Vec3 from = pContext.getFrom();
//            Vec3 to = pContext.getTo();
            Vec3 to = bhr.getLocation();
            Vector3d temp = new Vector3d();
            double bestDist = 1.0;
            double length = bestDist;
            AbstractPortal bestPortal = null;

            for (PortalNet portalNetwork : ((NetworkHolder) this).getPortalNetworks()) {
                for (AbstractPortal portal : portalNetwork.getPortals()) {
                    //noinspection RedundantSuppression
                    //noinspection EqualsBetweenInconvertibleTypes
                    if (portal.myLevel != this) continue;

                    double dist = portal.trace(from, to, temp, length);
                    if (dist < bestDist && dist > 0) {
                        bestDist = dist;
                        bestPortal = portal;
                    }
                }
            }

            if (bestPortal != null) {
                from = from.lerp(to, bestDist);
                to = pContext.getTo();

                if (bestPortal.getConnectedNetwork().getPortals().size() == 2) {
                    from = bestPortal.transformVec(from, temp, false);
                    to = bestPortal.transformVec(to, temp, false);
                    List<AbstractPortal> portals = bestPortal.getConnectedNetwork().getPortals();

                    AbstractPortal target;
                    if ((target = portals.get(0)) == bestPortal)
                        target = portals.get(1);

                    from = target.transformVec(from, temp, true);
                    to = target.transformVec(to, temp, true);

                    ClipContext ctx = new ClipContext(
                            from, to,
                            pContext.block,
                            pContext.fluid,
                            null
                    );
                    ctx.collisionContext = pContext.collisionContext;
                    return clip(ctx);
                }
            }
        }

        return bhr;
    }
}
