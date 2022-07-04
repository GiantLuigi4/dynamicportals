package tfc.dynamicportals.api;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;

import java.util.concurrent.BlockingQueue;

public class PortalVisibilityGraph {
	private final ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum = new ObjectArrayList<>(16);
	private final LevelRenderer renderer;
	private Frustum frustum;
	public int originX, originY, originZ;
	
	public PortalVisibilityGraph(LevelRenderer renderer) {
		this.renderer = renderer;
	}
	
	// TODO: improve this
	public void update() {
		renderChunksInFrustum.clear();
//		int dist = Minecraft.getInstance().options.renderDistance;
//		ViewArea area = ((LevelRendererAccessor) renderer).getViewArea();
//		ExtendedView extendedView = (ExtendedView) area;
//		for (int x = -dist / 2; x < dist / 2; x++) {
//			for (int y = -dist / 2; y < dist / 2; y++) {
//				for (int z = -dist / 2; z < dist / 2; z++) {
//					int ax = originX / 16 + x;
//					int ay = originY / 16 + y;
//					int az = originZ / 16 + z;
//
//					ChunkRenderDispatcher.RenderChunk renderChunk = extendedView.getChunk(new Vec3i(ax, ay, az), false);
//					if (renderChunk == null)
//						renderChunk = extendedView.makeChunk(new Vec3i(ax, ay, az), false);
//					AABB box = renderChunk.getBoundingBox();
//				}
//			}
//		}
		for (LevelRenderer.RenderChunkInfo renderChunk : renderer.renderChunkStorage.get().renderChunks) {
			// TODO: side checking
			if (renderChunk == null) continue;
			AABB box = renderChunk.chunk.getBoundingBox();
			if (frustum.isVisible(box)) {
				renderChunksInFrustum.add(renderChunk);
			}
		}
//		AsyncIterator.forEach(renderer.renderChunkStorage.get().renderChunks, (chunk) -> {
//			if (chunk == null) return;
//			AABB box = chunk.chunk.getBoundingBox();
//			if (frustum.isVisible(box)) {
//				synchronized (renderChunksInFrustum) {
//					renderChunksInFrustum.add(chunk);
//				}
//			}
//		});
	}
	
	public ObjectArrayList<LevelRenderer.RenderChunkInfo> getChunks() {
		return renderChunksInFrustum;
	}
	
	public Frustum getFrustum() {
		return frustum;
	}
	
	public void setFrustum(Frustum frustum) {
		this.frustum = frustum;
	}
	
	public void addAll(BlockingQueue<ChunkRenderDispatcher.RenderChunk> recentlyCompiledChunks) {
		// TODO: get this working properly
////		AsyncIterator.forEach(recentlyCompiledChunks, (chunk) -> {
//		ArrayList<ChunkRenderDispatcher.RenderChunk> chunks = new ArrayList<>();
//		while (!recentlyCompiledChunks.isEmpty()) {
//			ChunkRenderDispatcher.RenderChunk chunk = recentlyCompiledChunks.poll();
//			if (frustum.isVisible(chunk.getBoundingBox())) {
//				LevelRenderer.RenderChunkInfo info = renderer.renderChunkStorage.get().renderInfoMap.get(chunk);
//				if (info != null)
//					renderChunksInFrustum.add(info);
//				chunks.add(chunk);
//			}
//		}
//		recentlyCompiledChunks.addAll(chunks);
////		});
	}
	
	public void nudgeRenderer() {
		renderer.renderChunksInFrustum = new ObjectArrayList<>(renderChunksInFrustum);
	}
}
