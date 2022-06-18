package tfc.dynamicportals;

import com.mojang.blaze3d.vertex.PoseStack;
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
					Vector4f interpStart = new Vector4f(
							(float) Mth.lerp(dist, start.x, end.x),
							(float) Mth.lerp(dist, start.y, end.y),
							(float) Mth.lerp(dist, start.z, end.z),
							1
					);
					dist = 1 - dist;
					Vector4f interpReach = new Vector4f(
							(float) (reachVec.x * dist),
							(float) (reachVec.y * dist),
							(float) (reachVec.z * dist),
							1
					);
					PoseStack stack = new PoseStack();
					stack.pushPose();
					portal.negateTrace(stack);
					interpStart.transform(stack.last().pose());
					interpReach.transform(stack.last().pose());
					stack.popPose();
					portal.target.setupTrace(stack);
					interpStart.transform(stack.last().pose());
					interpReach.transform(stack.last().pose());
					
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
