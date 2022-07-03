package tfc.dynamicportals.access;

import net.minecraft.world.level.chunk.LevelChunk;

public interface IAmAChunkMap {
	LevelChunk[] forcedChunks();
	LevelChunk[] regularChunks();
}
