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
		if (entity != null && minecraft.level != null) {
			Vec3 start = entity.getEyePosition(pPartialTicks);
			Vec3 reach = entity.getViewVector(1.0F).scale(minecraft.gameMode.getPickRange());
			Vec3 end = start.add(reach);
			
			AbstractPortal[] portals = Temp.getPortals(minecraft.level);
			for (AbstractPortal portal : portals) {
				if (!portal.shouldRender(null, start.x, start.y, start.z)) continue;
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
