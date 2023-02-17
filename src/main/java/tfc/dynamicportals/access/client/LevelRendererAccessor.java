package tfc.dynamicportals.access.client;

import net.minecraft.client.renderer.culling.Frustum;

public interface LevelRendererAccessor {
	void setCullingFrustum(Frustum value);
	Frustum getCullingFrustum();
}
