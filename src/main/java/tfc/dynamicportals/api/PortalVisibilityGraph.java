package tfc.dynamicportals.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;

public class PortalVisibilityGraph {
	private final ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum = new ObjectArrayList<>(16);
	private Frustum frustum;
	private LevelRenderer renderer;
	
	public PortalVisibilityGraph(LevelRenderer renderer) {
		this.renderer = renderer;
	}
	
	public void setFrustum(Frustum frustum) {
		this.frustum = frustum;
	}
	
	// TODO: improve this
	public void update() {
		renderChunksInFrustum.clear();
		for (LevelRenderer.RenderChunkInfo renderChunk : renderer.renderChunkStorage.get().renderInfoMap.infos) {
			// TODO: side checking
			if (renderChunk == null) continue;
			AABB box = renderChunk.chunk.getBoundingBox();
			if (frustum.isVisible(box)) {
				renderChunksInFrustum.add(renderChunk);
			}
		}
	}
	
	public ObjectArrayList<LevelRenderer.RenderChunkInfo> getChunks() {
		return renderChunksInFrustum;
	}
	
	public Frustum getFrustum() {
		return frustum;
	}
}
