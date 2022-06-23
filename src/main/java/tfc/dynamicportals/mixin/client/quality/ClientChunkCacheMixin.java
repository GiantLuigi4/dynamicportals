//package tfc.dynamicportals.mixin.client.quality;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.ClientChunkCache;
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.core.BlockPos;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.chunk.ChunkStatus;
//import net.minecraft.world.level.chunk.LevelChunk;
//import net.minecraft.world.phys.Vec3;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//import tfc.dynamicportals.Temp;
//import tfc.dynamicportals.access.IHaveChunkArray;
//import tfc.dynamicportals.api.AbstractPortal;
//
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.function.BooleanSupplier;
//import java.util.function.Consumer;
//
//// TODO: this works, but it works at a horrendous speed
//@Mixin(ClientChunkCache.class)
//public abstract class ClientChunkCacheMixin {
//	@Shadow
//	volatile ClientChunkCache.Storage storage;
//	@Shadow
//	@Final
//	ClientLevel level;
//
//	@Shadow
//	@Nullable
//	public abstract LevelChunk getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad);
//
//	@Shadow
//	@Final
//	private LevelChunk emptyChunk;
//
//	@Shadow
//	private static native boolean isValidChunk(@org.jetbrains.annotations.Nullable LevelChunk pChunk, int pX, int pZ);
//
//	private final HashMap<ChunkPos, LevelChunk> chunkMap = new HashMap<>();
//	private final HashMap<ChunkPos, LevelChunk> newChunks = new HashMap<>();
//
//	private LevelChunk getChunk(ChunkPos pos) {
//		LevelChunk chunk = newChunks.getOrDefault(pos, null);
//		if (chunk == null) return chunkMap.getOrDefault(pos, null);
//		return chunk;
//	}
//
//	private LevelChunk moveChunk(ChunkPos pos) {
//		LevelChunk chunk = newChunks.getOrDefault(pos, null);
//		if (chunk != null) return chunk;
//		chunk = chunkMap.getOrDefault(pos, null);
//		if (chunk != null) {
////			chunkMap.remove(pos);
//			if (storage.inRange(pos.x, pos.z)) {
//				int i = this.storage.getIndex(pos.x, pos.z);
//				if (storage.getChunk(i) != chunk) {
//					this.storage.replace(i, chunk);
//				}
//			}
//		}
//		return chunk;
//	}
//
//	private LevelChunk removeChunk(ChunkPos pos) {
//		LevelChunk chunk = newChunks.remove(pos);
//		if (chunk != null) return chunk;
//		return chunkMap.remove(pos);
//	}
//
//	@Inject(at = @At("HEAD"), method = "getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/LevelChunk;", cancellable = true)
//	public void preGetChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad, CallbackInfoReturnable<LevelChunk> cir) {
//		if (!this.storage.inRange(pChunkX, pChunkZ)) {
//			ChunkPos pos = new ChunkPos(pChunkX, pChunkZ);
//			LevelChunk chunk = getChunk(pos);
//			if (chunk == null) return;
//			cir.setReturnValue(chunk);
//		} else {
//			ChunkPos pos = new ChunkPos(pChunkX, pChunkZ);
//			LevelChunk chunk = chunkMap.getOrDefault(pos, null);
//			if (chunk != null) {
//				cir.setReturnValue(moveChunk(pos));
////				BlockPos pos1 = pos.getMiddleBlockPosition(0);
////				BlockState state = chunk.getBlockState(pos1);
////				level.setBlocksDirty(pos1, state, state);
//				return;
//			}
//			chunk = newChunks.getOrDefault(pos, null);
//			if (chunk != null) cir.setReturnValue(chunk);
//		}
//	}
//
//	@Inject(at = @At(value = "HEAD"), method = "replaceWithPacketData", cancellable = true)
//	public void preReplace(int pX, int pZ, FriendlyByteBuf pBuffer, CompoundTag pTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> pConsumer, CallbackInfoReturnable<LevelChunk> cir) {
//		if (!this.storage.inRange(pX, pZ)) {
//			ChunkPos pos = new ChunkPos(pX, pZ);
//			LevelChunk chunk = getChunk(pos);
//			if (chunk != null) {
//				chunk.replaceWithPacketData(pBuffer, pTag, pConsumer);
//			} else {
//				chunk = new LevelChunk(level, pos);
//				chunk.replaceWithPacketData(pBuffer, pTag, pConsumer);
//				chunkMap.put(pos, chunk);
//			}
//			level.onChunkLoaded(pos);
//			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Load(chunk));
//			cir.setReturnValue(chunk);
//		} else {
//			ChunkPos pos = new ChunkPos(pX, pZ);
//			moveChunk(pos);
//		}
//	}
//
//	@Inject(at = @At("HEAD"), method = "drop", cancellable = true)
//	public void preDropChunk(int pX, int pZ, CallbackInfo ci) {
////		if (!this.storage.inRange(pX, pZ)) {
//		for (AbstractPortal portal : Temp.getPortals(level)) {
//			// TODO: check this
//			if (inRange(portal, Minecraft.getInstance().player, new ChunkPos(pX, pZ))) {
//				ChunkPos pos = new ChunkPos(pX, pZ);
//				int i = this.storage.getIndex(pX, pZ);
////					LevelChunk chunk = chunkMap.put(pos, getChunk(pX, pZ, ChunkStatus.FULL, false));
//				IHaveChunkArray chunkHolder = ((IHaveChunkArray) (Object) storage);
////					if (chunk == null) chunk = chunkHolder.getChunks().get(i);
//				LevelChunk chunk = chunkHolder.getChunks().get(i);
//				if (chunk == null) return;
//				newChunks.put(pos, chunk);
//				if (chunkHolder.getChunks().compareAndSet(i, chunk, null))
//					chunkHolder.removeChunk();
//				ci.cancel();
//				return;
//			}
//		}
////		}
//		ChunkPos pos = new ChunkPos(pX, pZ);
//		LevelChunk chunk = removeChunk(pos);
//		if (chunk != null) {
//			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Unload(chunk));
//			level.unload(chunk);
//			ci.cancel();
//		}
//	}
//
//	private boolean inRange(AbstractPortal portal, Player player, ChunkPos pos) {
//		Vec3 offset = portal.raytraceOffset();
//		offset = offset.multiply(1, 0, 1);
//		// TODO: check this
//		if ((offset.distanceTo(new Vec3((pos.getMinBlockX() + pos.getMaxBlockX()) / 2d, 0, (pos.getMinBlockZ() + pos.getMaxBlockZ()) / 2d)) / 16) < storage.chunkRadius) {
//			offset = portal.target.raytraceOffset();
//			offset = offset.multiply(1, 0, 1);
//			if ((offset.distanceTo(player.position()) / 16) < storage.chunkRadius) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	@Inject(at = @At("HEAD"), method = "tick")
//	public void preTick(BooleanSupplier p_202421_, boolean p_202422_, CallbackInfo ci) {
//		newChunks.forEach(chunkMap::put);
//		newChunks.clear();
//		if ((level.getGameTime() / 30) % 2 == 0) {
//			ArrayList<ChunkPos> toYeet = new ArrayList<>();
//			for (ChunkPos chunkPos : chunkMap.keySet()) {
//				boolean inRange = false;
//				for (AbstractPortal portal : Temp.getPortals(level)) {
//					if (inRange(portal, Minecraft.getInstance().player, chunkPos)) {
//						inRange = true;
//						break;
//					}
//				}
//				if (!inRange) {
//					toYeet.add(chunkPos);
//				}
//			}
//			for (ChunkPos chunkPos : toYeet) {
//				LevelChunk chunk = removeChunk(chunkPos);
//				net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Unload(chunk));
//				level.unload(chunk);
//			}
//		}
//	}
//}
