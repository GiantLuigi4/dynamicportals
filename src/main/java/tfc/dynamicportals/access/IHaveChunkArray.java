package tfc.dynamicportals.access;

import net.minecraft.world.level.chunk.LevelChunk;

import java.util.concurrent.atomic.AtomicReferenceArray;

public interface IHaveChunkArray {
	AtomicReferenceArray<LevelChunk> getChunks();
	int removeChunk();
}
