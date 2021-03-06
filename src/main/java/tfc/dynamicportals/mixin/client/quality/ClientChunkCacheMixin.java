package tfc.dynamicportals.mixin.client.quality;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.access.IAmAChunkMap;
import tfc.dynamicportals.mixin.client.access.StorageAccessor;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin implements IAmAChunkMap {
	@Shadow
	@Final
	public ClientLevel level;
	@Shadow
	volatile ClientChunkCache.Storage storage;
	@Unique
	HashMap<ChunkPos, LevelChunk> chunks = new HashMap<>();
	
	@Inject(at = @At("HEAD"), method = "getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;", cancellable = true)
	public void preGetChunk0(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad, CallbackInfoReturnable<ChunkAccess> cir) {
		LevelChunk chunk = getChunk(new ChunkPos(pChunkX, pChunkZ));
		if (chunk != null) cir.setReturnValue(chunk);
	}
	
	@Inject(at = @At("HEAD"), method = "getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/LevelChunk;", cancellable = true)
	public void preGetChunk1(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad, CallbackInfoReturnable<LevelChunk> cir) {
		LevelChunk chunk = getChunk(new ChunkPos(pChunkX, pChunkZ));
		if (chunk != null) cir.setReturnValue(chunk);
	}
	
	@Inject(at = @At("HEAD"), method = "drop", cancellable = true)
	public void preDropChunk(int pX, int pZ, CallbackInfo ci) {
//		if (!storage.inRange(pX, pZ)) {
//			int i = storage.getIndex(pX, pZ);
//			LevelChunk chunk = storage.getChunk(i);
//			// deny off loading of chunks
//			if (chunk != null) {
//				((IHaveChunkArray) (Object) storage).getChunks().set(i, null);
//				((IHaveChunkArray) (Object) storage).removeChunk();
//				chunks.put(new ChunkPos(pX, pZ), chunk);
//				ci.cancel();
//			}
//			return;
//		}
		LevelChunk chunk = chunks.remove(new ChunkPos(pX, pZ));
		if (chunk != null) {
			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Unload(chunk));
			this.level.unload(chunk);
			ci.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "replaceWithPacketData", cancellable = true)
	public void preReplaceWithPacket(int pX, int pZ, FriendlyByteBuf pBuffer, CompoundTag pTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> pConsumer, CallbackInfoReturnable<LevelChunk> cir) {
		ChunkPos pos = new ChunkPos(pX, pZ);
		LevelChunk chunk = chunks.getOrDefault(pos, null);
		boolean wasLoaded = chunk != null;
		if (!this.storage.inRange(pX, pZ)) chunk = new LevelChunk(this.level, pos);
		if (chunk == null) return;
		chunk.replaceWithPacketData(pBuffer, pTag, pConsumer);
		if (!wasLoaded) chunks.put(pos, chunk);
		// event and whatnot
		this.level.onChunkLoaded(pos);
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Load(chunk));
		cir.setReturnValue(chunk);
	}
	
	@Unique
	public LevelChunk getChunk(ChunkPos pos) {
		return chunks.getOrDefault(pos, null);
	}
	
	@Override
	public LevelChunk[] forcedChunks() {
		return chunks.values().toArray(new LevelChunk[0]);
	}
	
	@Override
	public LevelChunk[] regularChunks() {
		// AT did weird
		AtomicReferenceArray<LevelChunk> chunksArray = ((StorageAccessor) (Object) storage).chunks();
		LevelChunk[] chunks = new LevelChunk[chunksArray.length()];
		for (int i = 0; i < chunks.length; i++)
			chunks[i] = chunksArray.get(i);
		return chunks;
	}
}
