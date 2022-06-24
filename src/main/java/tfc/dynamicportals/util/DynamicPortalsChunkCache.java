package tfc.dynamicportals.util;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Consumer;

// TODO: attempt better compatibility, maybe?
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
	
	@Nullable
	@Override
	public LevelChunk getChunk(int pChunkX, int pChunkZ, boolean pLoad) {
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
	
	@Nullable
	@Override
	public LevelChunk replaceWithPacketData(int pX, int pZ, FriendlyByteBuf pBuffer, CompoundTag pTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> pConsumer) {
		ChunkPos chunkpos = new ChunkPos(pX, pZ);
		LevelChunk chunk;
		if (chunks.containsKey(chunkpos)) chunk = getChunk(pX, pZ, false);
		else {
			chunk = new LevelChunk(this.level, chunkpos);
			chunks.put(chunkpos, chunk);
		}
		chunk.replaceWithPacketData(pBuffer, pTag, pConsumer);
		
		this.level.onChunkLoaded(chunkpos);
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Load(chunk));
		return chunk;
	}
	
	@Override
	public int getLoadedChunksCount() {
		return chunks.size();
	}
	
	@Override
	public void updateViewRadius(int pViewDistance) {
	}
	
	@Override
	public void updateViewCenter(int pX, int pZ) {
	}
	
	@Override
	public boolean hasChunk(int pChunkX, int pChunkZ) {
		return chunks.containsKey(new ChunkPos(pChunkX, pChunkZ));
	}
}
