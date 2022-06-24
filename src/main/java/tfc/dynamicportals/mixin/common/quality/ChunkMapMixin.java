package tfc.dynamicportals.mixin.common.quality;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.access.ITrackChunks;
import tfc.dynamicportals.api.AbstractPortal;

import javax.annotation.Nullable;
import java.util.ArrayList;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
	@Shadow
	@Nullable
	protected abstract ChunkHolder getVisibleChunkIfPresent(long p_140328_);
	
	@Shadow
	@Final
	ServerLevel level;
	
	@Shadow
	@Final
	private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;
	@Shadow
	int viewDistance;
	
	@Shadow
	protected abstract void updateChunkTracking(ServerPlayer pPlayer, ChunkPos pChunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, boolean pWasLoaded, boolean pLoad);
	
	@Shadow
	@Final
	private PlayerMap playerMap;
	@Shadow
	@Final
	private ChunkMap.DistanceManager distanceManager;
	
	@Shadow
	protected abstract boolean skipPlayer(ServerPlayer pPlayer);
	
	@Shadow
	protected abstract SectionPos updatePlayerPos(ServerPlayer p_140374_);
	
	AbstractPortal[] portals;
	
	// TODO: is there a way to do this without replacing the entire "move" method?
	@Inject(at = @At("HEAD"), method = "move", cancellable = true)
	public void preMove(ServerPlayer pPlayer, CallbackInfo ci) {
		for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values()) {
			if (chunkmap$trackedentity.entity == pPlayer) chunkmap$trackedentity.updatePlayers(this.level.players());
			else chunkmap$trackedentity.updatePlayer(pPlayer);
		}
		
		SectionPos oldPos = pPlayer.getLastSectionPos();
		SectionPos newPos = SectionPos.of(pPlayer);
		long oldAsLong = oldPos.chunk().toLong();
		long newAsLong = newPos.chunk().toLong();
		boolean ignorePlayer = this.playerMap.ignored(pPlayer);
		boolean skipPlayer = this.skipPlayer(pPlayer);
		boolean wat = oldPos.asLong() != newPos.asLong();
		if (wat || ignorePlayer != skipPlayer) {
			this.updatePlayerPos(pPlayer);
			if (!ignorePlayer) this.distanceManager.removePlayer(oldPos, pPlayer);
			if (!skipPlayer) this.distanceManager.addPlayer(newPos, pPlayer);
			if (!ignorePlayer && skipPlayer) this.playerMap.ignorePlayer(pPlayer);
			if (ignorePlayer && !skipPlayer) this.playerMap.unIgnorePlayer(pPlayer);
			if (oldAsLong != newAsLong) this.playerMap.updatePlayer(oldAsLong, newAsLong, pPlayer);
		}
		
		ITrackChunks chunkTracker = (ITrackChunks) pPlayer;
		if (!chunkTracker.setDoUpdate(false)) return;
		
		ChunkPos center = new ChunkPos(pPlayer.getOnPos());
		ArrayList<ChunkPos> tracked = new ArrayList<>();
		boolean anyFailed = false;
		for (int x = -viewDistance; x <= viewDistance; x++) {
			for (int z = -viewDistance; z <= viewDistance; z++) {
				ChunkPos pos = new ChunkPos(center.x + x, center.z + z);
				ChunkHolder holder = getVisibleChunkIfPresent(pos.toLong());
				if (holder != null) {
					if (holder.getTickingChunk() != null) {
						updateChunkTracking(
								pPlayer, pos,
								new MutableObject<>(),
								chunkTracker.trackedChunks().remove(pos), // remove it so that the next loop doesn't untrack it
								true // start tracking
						);
						tracked.add(pos);
					} else anyFailed = true;
				} else anyFailed = true;
			}
		}
		portals = Temp.getPortals(level);
		chunkTracker.trackedChunks().removeAll(tracked);
		for (ChunkPos trackedChunk : chunkTracker.trackedChunks()) {
			if (nonEclidianInRange(trackedChunk, pPlayer)) {
				tracked.add(trackedChunk);
				updateChunkTracking(
						pPlayer, trackedChunk,
						new MutableObject<>(),
						true, // this will always be true, no point in checking the list
						true // player should know about the chunk
				);
			} else {
				updateChunkTracking(
						pPlayer, trackedChunk,
						new MutableObject<>(),
						true, // this will always be true, no point in checking the list
						false // unloading/untracking
				);
			}
		}
		chunkTracker.trackedChunks().clear();
		chunkTracker.trackedChunks().addAll(tracked);
		
		if (anyFailed) chunkTracker.setDoUpdate(true);
		ci.cancel();
	}

//	boolean success = false;
//
//	@Inject(at = @At("HEAD"), method = "playerLoadedChunk")
//	public void preLoadChunk(ServerPlayer pPlaer, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, LevelChunk pChunk, CallbackInfo ci) {
//		success = true;
//	}
	
	@Inject(at = @At("HEAD"), method = "updatePlayerPos")
	public void preUpdatePos(ServerPlayer p_140374_, CallbackInfoReturnable<SectionPos> cir) {
		((ITrackChunks) p_140374_).setDoUpdate(true);
	}
	
	public boolean nonEclidianInRange(ChunkPos pos, ServerPlayer player) {
		for (AbstractPortal portal : portals) {
			if (portal.raytraceOffset().distanceTo(player.position()) / 16 < viewDistance) {
				AbstractPortal targ = portal.target;
				Vec3 chunkPos = new Vec3(pos.x * 16, 0, pos.z * 16);
				if (chunkPos.distanceTo(targ.raytraceOffset().multiply(1, 0, 1)) / 16 < viewDistance) {
					return true;
				}
			}
		}
		return false;
	}

//	boolean success = false;
//
//	// TODO: can this be done better?
//	@Inject(at = @At("HEAD"), method = "updateChunkTracking", cancellable = true)
//	public void preUpdateTracking(ServerPlayer pPlayer, ChunkPos pChunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, boolean pWasLoaded, boolean pLoad, CallbackInfo ci) {
//		success = false;
//		if (pPlayer.level == this.level) {
//			net.minecraftforge.event.ForgeEventFactory.fireChunkWatch(pWasLoaded, pLoad, pPlayer, pChunkPos, level);
//			if (pLoad && !pWasLoaded) {
//				ChunkHolder chunkholder = this.getVisibleChunkIfPresent(pChunkPos.toLong());
//				if (chunkholder != null) {
//					LevelChunk levelchunk = chunkholder.getTickingChunk();
//					if (levelchunk != null) {
//						this.playerLoadedChunk(pPlayer, pPacketCache, levelchunk);
//						DebugPackets.sendPoiPacketsForChunk(this.level, pChunkPos);
//						success = true;
//					}
//				}
//			}
//
//			if (!pLoad && pWasLoaded) {
//				pPlayer.untrackChunk(pChunkPos);
//			}
//		}
//
//		ci.cancel();
//	}
}
