package tfc.dynamicportals;

import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.util.VecMath;

public class RaytraceHelper {
	public static void trace(Minecraft minecraft, float pPartialTicks) {
		Entity entity = minecraft.getCameraEntity();
		if (entity != null) {
			if (minecraft.level != null) {
				double reach = minecraft.gameMode.getPickRange();
				Vec3 start = entity.getEyePosition(pPartialTicks);
				Vec3 look = entity.getViewVector(1.0F);
				Vec3 reachVec = new Vec3(look.x * reach, look.y * reach, look.z * reach);
				Vec3 end = start.add(reachVec);
				
				AbstractPortal[] portals = Temp.getPortals(minecraft.level);
				for (AbstractPortal portal : portals) {
					if (!portal.shouldRender(null, start.x, start.y, start.z)) {
						continue;
					}
					double dist = portal.trace(start, end);
					if (dist == 1) continue;
					double distance = reachVec.scale(dist).length();
					double d = minecraft.hitResult.getLocation().distanceTo(start);
					if (distance > d) continue;
					Vec3 interpStart = VecMath.lerp(dist, start, end);
					Vec3 interpReach = VecMath.lerp(1 - dist, Vec3.ZERO, reachVec);
					
					if (portal.requireTraceRotation()) {
						Quaternion srcQuat = portal.raytraceRotation();
						Quaternion dstQuat = portal.target.raytraceRotation();
						Vec3 srcOff = portal.raytraceOffset();
						Vec3 dstOff = portal.target.raytraceOffset();
						interpStart = VecMath.transform(interpStart, srcQuat, dstQuat, portal.getScaleRatio(), portal.target.get180DegreesRotationAroundVerticalAxis(), portal == portal.target, srcOff, dstOff);
						interpReach = VecMath.transform(interpReach, srcQuat, dstQuat, portal.getScaleRatio(), portal.target.get180DegreesRotationAroundVerticalAxis(), portal == portal.target, Vec3.ZERO, Vec3.ZERO);
					} else {
						Vec3 offset = portal.target.raytraceOffset().subtract(portal.raytraceOffset());
						interpStart = interpStart.add(offset);
					}
					Vec3 istart = interpStart;
					Vec3 ireach = interpReach;
					Vec3 iend = istart.add(ireach);

//					// this confuses me way more than it should
//					Vec3 offset = portal.target.raytraceOffset().subtract(portal.raytraceOffset());
//					interpStart = interpStart.add(offset);
//
////					Quaternion q = new Quaternion((float) interpReach.x, (float) interpReach.y, (float) interpReach.z, 0.0f);
////					q.mul(quat);
////					quat.conj();
////					quat.mul(q);
////					interpReach = new Vec3(q.i(), q.j(), q.k());
//					if (portal.requireTraceRotation()) {
//						Quaternion quat = portal.raytraceRotation();
//						quat.conj();
//						interpReach = rotateQuat(interpReach, quat);
//						interpReach = interpReach.multiply(1, 1, -1);
//						quat.conj();
//						interpReach = rotateQuat(interpReach, quat);
//						interpReach = rotateQuat(interpReach, quat);
//
//						quat = portal.target.raytraceRotation();
//						interpReach = rotateQuat(interpReach, quat);
//						interpReach = interpReach.multiply(1, 1, 1);
//						quat.conj();
//						interpReach = rotateQuat(interpReach, quat);
//						interpReach = rotateQuat(interpReach, quat);
//					}
//
//					Vec3 istart = new Vec3(interpStart.x(), interpStart.y(), interpStart.z());
//					Vec3 iend = new Vec3(interpStart.x() + interpReach.x(), interpStart.y() + interpReach.y(), interpStart.z() + interpReach.z());
					minecraft.hitResult = entity.level.clip(
							new ClipContext(
									istart, iend,
									ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY,
									entity
							)
					);
				}
			}
		}
	}
}
