package tfc.dynamicportals;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.Portal;

public class RaytraceHelper {
	public static void trace(Minecraft minecraft, float pPartialTicks) {
		Entity entity = minecraft.getCameraEntity();
		if (entity != null) {
			if (minecraft.level != null) {
				double reach = (double) minecraft.gameMode.getPickRange();
				Vec3 start = entity.getEyePosition(pPartialTicks);
				Vec3 look = entity.getViewVector(1.0F);
				Vec3 reachVec = new Vec3(look.x * reach, look.y * reach, look.z * reach);
				Vec3 end = start.add(reachVec);
				
				Portal[] portals = Temp.getPortals(minecraft.level);
				for (Portal portal : portals) {
					double dist = portal.trace(start, end);
					if (dist == 1) continue;
					double distance = reachVec.scale(dist).length();
					double d = minecraft.hitResult.getLocation().distanceTo(start);
					if (distance > d) continue;
					Vec3 interpStart = new Vec3(
							Mth.lerp(dist, start.x, end.x),
							Mth.lerp(dist, start.y, end.y),
							Mth.lerp(dist, start.z, end.z)
					);
					dist = 1 - dist;
					Vec3 interpReach = new Vec3(
							reachVec.x * dist,
							reachVec.y * dist,
							reachVec.z * dist
					);
					
					Vec3 offset = portal.raytraceOffset();
					interpStart = interpStart.subtract(offset);
					offset = portal.target.raytraceOffset();
					interpStart = interpStart.add(offset);
					
					Quaternion quat = portal.raytraceRotation();
					Quaternion q = new Quaternion((float) interpReach.x, (float) interpReach.y, (float) interpReach.z, 0.0f);
					q.mul(quat);
					quat.conj();
					quat.mul(q);
					interpReach = new Vec3(q.i(), q.j(), q.k());
					
//					quat = portal.target.raytraceRotation();
//					q = new Quaternion((float) interpReach.x, (float) interpReach.y, (float) interpReach.z, 0.0f);
//					q.mul(quat);
//					quat.conj();
//					quat.mul(q);
//					interpReach = new Vec3(quat.i(), quat.j(), quat.k());
					
					Vec3 istart = new Vec3(interpStart.x(), interpStart.y(), interpStart.z());
					Vec3 iend = new Vec3(interpStart.x() + interpReach.x(), interpStart.y() + interpReach.y(), interpStart.z() + interpReach.z());
					HitResult result = entity.level.clip(
							new ClipContext(
									istart, iend,
									ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY,
									entity
							)
					);
					minecraft.hitResult = result;
//						System.out.println(result);
				}
			}
		}
	}
}
