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

import java.util.HashMap;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin {
	@Shadow
	@Final
	public ClientLevel level;
	@Shadow
	private volatile ClientChunkCache.Storage storage;
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
		LevelChunk chunk = chunks.remove(new ChunkPos(pX, pZ));
		if (chunk != null) {
			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Unload(chunk));
			this.level.unload(chunk);
			ci.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "replaceWithPacketData")
	public void preReplaceWithPacket(int pX, int pZ, FriendlyByteBuf pBuffer, CompoundTag pTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> pConsumer, CallbackInfoReturnable<LevelChunk> cir) {
		ChunkPos pos = new ChunkPos(pX, pZ);
		LevelChunk chunk = chunks.getOrDefault(pos, null);
		boolean wasLoaded = chunk != null;
		if (!this.storage.inRange(pX, pZ)) chunk = new LevelChunk(this.level, pos);
		if (chunk == null) return;
		chunk.replaceWithPacketData(pBuffer, pTag, pConsumer);
		if (!wasLoaded) chunks.put(pos, chunk);
		cir.setReturnValue(chunk);
	}
	
	@Unique
	public LevelChunk getChunk(ChunkPos pos) {
		return chunks.getOrDefault(pos, null);
	}
}
