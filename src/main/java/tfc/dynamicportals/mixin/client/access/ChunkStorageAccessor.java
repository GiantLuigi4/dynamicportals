package tfc.dynamicportals.mixin.client.access;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.dynamicportals.access.IHaveChunkArray;

import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ClientChunkCache.Storage.class)
public class ChunkStorageAccessor implements IHaveChunkArray {
	@Shadow
	@Final
	private AtomicReferenceArray<LevelChunk> chunks;
	
	@Shadow
	private int chunkCount;
	
	@Override
	public AtomicReferenceArray<LevelChunk> getChunks() {
		return chunks;
	}
	
	@Override
	public int removeChunk() {
		return chunkCount--;
	}
}
