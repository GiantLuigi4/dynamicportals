package tfc.dynamicportals.mixin.client.quality.camera;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.util.render.DPCamera;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow
	@Final
	private Camera mainCamera;
	
	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lcom/mojang/math/Quaternion;)V", ordinal = 4, shift = At.Shift.AFTER), method = "renderLevel")
	public void postOrient(float pPartialTicks, long pFinishTimeNano, PoseStack pMatrixStack, CallbackInfo ci) {
		DPCamera dpCam = (DPCamera) mainCamera;
		
		if (dpCam.useQuat()) {
			pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-(mainCamera.getYRot() + 180.0F)));
			pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-mainCamera.getXRot()));

			pMatrixStack.mulPose(dpCam.getQuat());
		}
	}
}
