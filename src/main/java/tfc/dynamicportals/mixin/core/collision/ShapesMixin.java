package tfc.dynamicportals.mixin.core.collision;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Shapes.class)
public class ShapesMixin {
	@Unique
	private static AABB bound = new AABB(10, 63, -1, 10 + 0.5, 64 + 1, 1);
	
	private static final double[] orientation = new double[]{
			-1, 0, 1,
			0, 1, 0,
			-1, 0, -1
	};
	
	private static boolean checkShape(VoxelShape sp) {
//		return sp.bounds().intersects(bound);
		return sp.bounds().minY >= 63 && sp.bounds().maxY <= 65;
	}
	
	private static boolean isVanilla(VoxelShape sp) {
		if (
				sp.getClass().equals(VoxelShape.class) ||
						sp.getClass().equals(ArrayVoxelShape.class) ||
						sp.getClass().equals(CubeVoxelShape.class) ||
						sp.getClass().equals(SliceShape.class)
		) return true;
		return false;
	}
	
	private static void rotate(double[] crd) {
		crd[0] -= 10.5;
		crd[1] -= 63;
		crd[2] -= 0;
		
		double x = crd[0];
		double y = crd[1];
		double z = crd[2];
		
		crd[0] = x * orientation[0] + y * orientation[1] + z * orientation[2];
		crd[1] = x * orientation[3] + y * orientation[4] + z * orientation[5];
		crd[2] = x * orientation[6] + y * orientation[7] + z * orientation[8];
	}
	
	private static boolean checkCollider(AABB from) {
		double[] min = new double[]{from.minX, from.minY, from.minZ};
		double[] max = new double[]{from.maxX, from.maxY, from.maxZ};
		rotate(min);
		rotate(max);
		
		if (min[1] >= 0 && max[1] <= 2) {
			if (min[2] >= -1 && max[2] <= 1) {
				if (min[0] <= 0.5) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public static double collide(Direction.Axis pMovementAxis, AABB pCollisionBox, Iterable<VoxelShape> pPossibleHits, double pDesiredOffset) {
		for (VoxelShape voxelshape : pPossibleHits) {
			if (Math.abs(pDesiredOffset) < 1.0E-7D) {
				return 0.0D;
			}
			
//			if (checkCollider(pCollisionBox)) {
//				if (checkShape(voxelshape)) {
//					// no-op
//				} else pDesiredOffset = voxelshape.collide(pMovementAxis, pCollisionBox, pDesiredOffset);
//			} else
				pDesiredOffset = voxelshape.collide(pMovementAxis, pCollisionBox, pDesiredOffset);
		}
		
		return pDesiredOffset;
	}
}
