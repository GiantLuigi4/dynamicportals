package tfc.dynamicportals.util;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class DynamicPortalsChunkCache extends ClientChunkCache {
	public DynamicPortalsChunkCache(ClientLevel pLevel, int pViewDistance) {
		super(pLevel, pViewDistance);
	}
	
	HashMap<ChunkPos, LevelChunk> chunks = new HashMap<>();
	
	@Nullable
	@Override
	public LevelChunk getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
		LevelChunk chunk = chunks.getOrDefault(new ChunkPos(pChunkX, pChunkZ), null);
		if (chunk != null) return chunk;
		return emptyChunk;
	}
	
	@Override
	public void drop(int pX, int pZ) {
		LevelChunk chunk = chunks.remove(new ChunkPos(pX, pZ));
		if (chunk != null) {
			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Unload(chunk));
			this.level.unload(chunk);
		}
	}
}
