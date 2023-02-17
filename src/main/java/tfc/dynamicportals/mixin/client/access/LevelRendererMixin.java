package tfc.dynamicportals.mixin.client.access;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.dynamicportals.access.client.LevelRendererAccessor;

// AT was being dumb
@Mixin(LevelRenderer.class)
public class LevelRendererMixin implements LevelRendererAccessor {
	@Shadow private Frustum cullingFrustum;
	
	@Override
	public void setCullingFrustum(Frustum value) {
		cullingFrustum = value;
	}
	
	@Override
	public Frustum getCullingFrustum() {
		return cullingFrustum;
	}
}
