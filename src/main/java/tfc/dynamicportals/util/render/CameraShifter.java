package tfc.dynamicportals.util.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector4f;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.RaytraceHelper;
import tfc.dynamicportals.api.AbstractPortal;

public class CameraShifter {
	public static TransformationInfo postMove(Camera camera, BlockGetter pLevel, Entity pEntity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick, CallbackInfo ci) {
		Vec3 oldVec = new Vec3(pEntity.xo, pEntity.yo + camera.eyeHeight, pEntity.zo);
		Vec3 newVec = new Vec3(pEntity.getX(), pEntity.getY() + camera.eyeHeightOld, pEntity.getZ());
		
		AbstractPortal prtl = RaytraceHelper.tracePortal(pEntity.getLevel(), newVec, oldVec);
		if (prtl != null) {
//			Minecraft.getInstance().setScreen(new PauseScreen(
//					false
//			));
			
			// calculate intersection point
			double pct = 1 - prtl.trace(newVec, oldVec);
			if (pct <= pPartialTick) return null;
			
			// get transformed vectors
			Vec3 vec = prtl.getDestination(oldVec);
			Vec3 vec1 = prtl.getDestination(newVec);
			
			// setup quaternion for rotation
			Quaternion q = new Quaternion(0, 0, 0, 1);
			q.mul(new Quaternion(camera.getXRot(), 0, 0, true));
			q.mul(new Quaternion(0, camera.getYRot() + 180, 0, true));
			q.mul(prtl.raytraceRotation());
			q.mul(prtl.target.get180DegreesRotationAroundVerticalAxis());
			
			// setup 2d rotation
			Vector4f look = new Vector4f(camera.getLookVector());
			PoseStack stk = new PoseStack();
			prtl.renderer.fullSetupMatrix(stk);
			stk.mulPose(new Quaternion(0, 180, 0, true));
			prtl.target.renderer.setupAsTarget(stk);
			Vector4f ZERO = new Vector4f(0, 0, 0, 1);
			ZERO.transform(stk.last().pose());
			look.transform(stk.last().pose());
			look.add(-ZERO.x(), -ZERO.y(), -ZERO.z(), 0);
			
			Vec3 pTarget = new Vec3(look.x(), look.y(), look.z());
			double d0 = pTarget.x;
			double d1 = pTarget.y;
			double d2 = pTarget.z;
			double d3 = Math.sqrt(d0 * d0 + d2 * d2);
			float xR = (Mth.wrapDegrees((float) (-(Mth.atan2(d1, d3) * (double) (180F / (float) Math.PI)))));
			float yR = (Mth.wrapDegrees((float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F));
			
			// return it
			double delta = 1 - pPartialTick;
			return new TransformationInfo(
					vec.scale(delta).add(vec1.scale(1 - delta)),
					q, new Vec2(xR, yR)
			);
		}
		
		return null;
	}
}
