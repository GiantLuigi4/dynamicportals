package tfc.dynamicportals.mixin.client.access;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.dynamicportals.itf.access.LevelRendererAccessor;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class LevelRenderAccess implements LevelRendererAccessor {
	@Shadow private Frustum cullingFrustum;
	
	@Shadow @Nullable private Frustum capturedFrustum;
	
	@Shadow public abstract void captureFrustum();
	
	@Shadow protected abstract void captureFrustum(Matrix4f pViewMatrix, Matrix4f pProjectionMatrix, double pCamX, double pCamY, double pCamZ, Frustum pCapturedFrustrum);
	
	@Shadow private boolean captureFrustum;
	
	@Override
	public void dynamic_portals$setFrustum(Frustum frustum) {
		// TODO: proper?
		this.cullingFrustum = frustum;
		captureFrustum = false;
	}
}
