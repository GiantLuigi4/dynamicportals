package tfc.dynamicportals.mixin.client.quality.camera;

import com.mojang.math.Quaternion;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.util.render.CameraShifter;
import tfc.dynamicportals.util.render.DPCamera;
import tfc.dynamicportals.util.render.TransformationInfo;

@Mixin(Camera.class)
public abstract class CameraMixin implements DPCamera {
	//@formatter:off
	@Shadow protected abstract void setPosition(double pX, double pY, double pZ);
	@Shadow @Final private Quaternion rotation;
	@Shadow protected abstract void setRotation(float pYRot, float pXRot);
	
	@Unique Quaternion q;
	//@formatter:on
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.AFTER), method = "setup")
	public void postMove(BlockGetter pLevel, Entity pEntity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick, CallbackInfo ci) {
		this.q = null;
		TransformationInfo dst = CameraShifter.postMove(((Camera) (Object) this), pLevel, pEntity, pDetached, pThirdPersonReverse, pPartialTick, ci);
		if (dst != null) {
			this.setPosition(dst.pos.x, dst.pos.y, dst.pos.z);
			this.setRotation(dst.look.y, dst.look.x);
			rotation.set(dst.quaternion.i(), dst.quaternion.j(), dst.quaternion.k(), dst.quaternion.r());
			this.q = dst.quaternion;
		}
	}
	
	@Override
	public boolean useQuat() {
		return q != null;
	}
	
	@Override
	public Quaternion getQuat() {
		return q;
	}
}
