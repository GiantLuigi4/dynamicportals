package tfc.dynamicportals.access;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.Vec3i;

public interface ExtendedView {
	ChunkRenderDispatcher.RenderChunk makeChunk(Vec3i vec, boolean relative);
	ChunkRenderDispatcher.RenderChunk getChunk(Vec3i vec, boolean relative);
}
