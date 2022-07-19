package tfc.dynamicportals.mixin.client.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.Renderer;

// Luigi's TODO: sodium level renderer mixin when sodium is present
@Mixin(LevelRenderer.class)
// I noticed this way later than I should've, and honestly, at this point, I'm leaving it
public class LevenRendererMixin {
	@Inject(at = @At("HEAD"), method = "renderLevel")
	public void preDrawLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
		Renderer.preDrawLevel(pCamera);
	}
}
