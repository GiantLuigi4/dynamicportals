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
	private static AABB bound = new AABB(10, 63, -1, 10 + 0.5, 64 + 1, 0 + 1);
	
	private static boolean checkShape(VoxelShape sp) {
		if (sp.bounds().intersects(bound))
			return true;
		return false;
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
	
	private static boolean checkCollider(AABB from) {
		if (from.maxY > 63 && from.minY < 65) {
			if (from.maxZ > -1 && from.minZ < 1) {
				if (from.maxX <= 11.5) {
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
			
			if (checkCollider(pCollisionBox)) {
				if (pMovementAxis.equals(Direction.Axis.X)) {
					if (checkShape(voxelshape)) {
						double oset = voxelshape.collide(pMovementAxis, pCollisionBox, pDesiredOffset);
						
						boolean sign = pDesiredOffset > 0;
						double crd = sign ? pCollisionBox.max(pMovementAxis) : pCollisionBox.min(pMovementAxis);
						crd += oset;
						
						if (crd > 10.5)
							pDesiredOffset += 10.5 - crd;
					} else pDesiredOffset = voxelshape.collide(pMovementAxis, pCollisionBox, pDesiredOffset);
				} else {
					if (checkShape(voxelshape)) {
						continue;
					}
					pDesiredOffset = voxelshape.collide(pMovementAxis, pCollisionBox, pDesiredOffset);
				}
			} else pDesiredOffset = voxelshape.collide(pMovementAxis, pCollisionBox, pDesiredOffset);
		}
		
		return pDesiredOffset;
	}
}
