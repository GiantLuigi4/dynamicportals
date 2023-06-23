package tfc.dynamicportals;

import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.util.VecMath;

public class RaytraceHelper {
	//lorenzo: I hate this copy-paste-type code
	public static double calculateXOffset(AABB first, AABB other, double offsetX) {
		if (other.maxY > first.minY && other.minY < first.maxY && other.maxZ > first.minZ && other.minZ < first.maxZ) {
			if (offsetX > 0.0D && other.maxX <= first.minX) {
				double deltaX = first.minX - other.maxX;
				
				if (deltaX < offsetX) offsetX = deltaX;
			} else if (offsetX < 0.0D && other.minX >= first.maxX) {
				double deltaX = first.maxX - other.minX;
				
				if (deltaX > offsetX) offsetX = deltaX;
			}
		}
		return offsetX;
	}
	
	public static double calculateYOffset(AABB first, AABB other, double offsetY) {
		if (other.maxX > first.minX && other.minX < first.maxX && other.maxZ > first.minZ && other.minZ < first.maxZ) {
			if (offsetY > 0.0D && other.maxY <= first.minY) {
				double d1 = first.minY - other.maxY;
				
				if (d1 < offsetY) {
					offsetY = d1;
				}
			} else if (offsetY < 0.0D && other.minY >= first.maxY) {
				double d0 = first.maxY - other.minY;
				
				if (d0 > offsetY) {
					offsetY = d0;
				}
			}
		}
		return offsetY;
	}
	
	public static double calculateZOffset(AABB first, AABB other, double offsetZ) {
		if (other.maxX > first.minX && other.minX < first.maxX && other.maxY > first.minY && other.minY < first.maxY) {
			if (offsetZ > 0.0D && other.maxZ <= first.minZ) {
				double d1 = first.minZ - other.maxZ;
				
				if (d1 < offsetZ) {
					offsetZ = d1;
				}
			} else if (offsetZ < 0.0D && other.minZ >= first.maxZ) {
				double d0 = first.maxZ - other.minZ;
				
				if (d0 > offsetZ) {
					offsetZ = d0;
				}
			}
		}
		return offsetZ;
	}
	
	public static AbstractPortal tracePortal(Level level, Vec3 start, Vec3 end) {
		AbstractPortal[] portals = Temp.getPortals(level);
		
		Vec3 reach = end.subtract(start).normalize();
		
		double bDist = Double.POSITIVE_INFINITY;
		AbstractPortal hit = null;
		for (AbstractPortal portal : portals) {
			if (!portal.canTeleport(new Vec3(start.x, start.y, start.z))) continue;
			double dist = portal.trace(start, end);
			if (dist == 1) continue;
			double distance = reach.scale(dist).length();
			if (distance > bDist) continue;
			
			bDist = distance;
			hit = portal;
		}
		
		return hit;
	}
	
	public static void trace(Minecraft minecraft, float pPartialTicks) {
		Entity entity = minecraft.getCameraEntity();
		if (entity != null && minecraft.level != null) {
			Vec3 start = entity.getEyePosition(pPartialTicks);
			Vec3 reach = entity.getViewVector(1.0F).scale(minecraft.gameMode.getPickRange());
			Vec3 end = start.add(reach);
			
			AbstractPortal[] portals = Temp.getPortals(minecraft.level);
			for (AbstractPortal portal : portals) {
				if (!portal.canTeleport(entity, new Vec3(start.x, start.y, start.z))) continue;
				double dist = portal.trace(start, end);
				if (dist == 1) continue;
				double distance = reach.scale(dist).length();
				if (distance > minecraft.hitResult.getLocation().distanceTo(start)) continue;
				
				Vec3 iStart = VecMath.lerp(dist, start, end);
				Vec3 iReach = VecMath.lerp(dist, reach, Vec3.ZERO);
				
				Vec3 srcOff = portal.raytraceOffset();
				Vec3 dstOff = portal.target.raytraceOffset();
				Quaternion srcRot = portal.raytraceRotation();
				Quaternion dstRot = portal.target.raytraceRotation();
				iStart = VecMath.transform(iStart, srcRot, dstRot, portal.getScaleRatio(), portal.target.get180DegreesRotationAroundVerticalAxis(), portal == portal.target, srcOff, dstOff);
				iReach = VecMath.transform(iReach, srcRot, dstRot, portal.getScaleRatio(), portal.target.get180DegreesRotationAroundVerticalAxis(), portal == portal.target, Vec3.ZERO, Vec3.ZERO);
				
				minecraft.hitResult = entity.level.clip(
						new ClipContext(
								iStart, iStart.add(iReach),
								ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY,
								entity
						)
				);
			}
		}
	}
}
