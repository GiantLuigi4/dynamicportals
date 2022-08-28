//package tfc.dynamicportals.mixin.common.quality;
//
//import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.SectionPos;
//import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
//import net.minecraft.server.level.*;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.chunk.LevelChunk;
//import net.minecraft.world.phys.Vec3;
//import org.apache.commons.lang3.mutable.MutableObject;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//import tfc.dynamicportals.Temp;
//import tfc.dynamicportals.access.ITrackChunks;
//import tfc.dynamicportals.api.AbstractPortal;
//
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//
//@Mixin(ChunkMap.class)
//public abstract class ChunkMapMixin {
//	@Shadow
//	@Final
//	ServerLevel level;
//	@Shadow
//	int viewDistance;
//	AbstractPortal[] portals;
//	boolean success = false;
//	@Shadow
//	@Final
//	private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;
//	@Shadow
//	@Final
//	private PlayerMap playerMap;
//	@Shadow
//	@Final
//	private ChunkMap.DistanceManager distanceManager;
//
//	@Shadow
//	@Nullable
//	protected abstract ChunkHolder getVisibleChunkIfPresent(long p_140328_);
//
//	@Shadow
//	protected abstract void updateChunkTracking(ServerPlayer pPlayer, ChunkPos pChunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, boolean pWasLoaded, boolean pLoad);
//
//	@Shadow
//	protected abstract boolean skipPlayer(ServerPlayer pPlayer);
//
//	@Shadow
//	protected abstract SectionPos updatePlayerPos(ServerPlayer p_140374_);
//
//	// TODO: is there a way to do this without replacing the entire "move" method?
//	@Inject(at = @At("HEAD"), method = "move", cancellable = true)
//	public void preMove(ServerPlayer pPlayer, CallbackInfo ci) {
//		for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values()) {
//			if (chunkmap$trackedentity.entity == pPlayer) chunkmap$trackedentity.updatePlayers(this.level.players());
//			else chunkmap$trackedentity.updatePlayer(pPlayer);
//		}
//
//		SectionPos oldPos = pPlayer.getLastSectionPos();
//		SectionPos newPos = SectionPos.of(pPlayer);
//		long oldAsLong = oldPos.chunk().toLong();
//		long newAsLong = newPos.chunk().toLong();
//		boolean ignorePlayer = this.playerMap.ignored(pPlayer);
//		boolean skipPlayer = this.skipPlayer(pPlayer);
//		boolean wat = oldPos.asLong() != newPos.asLong();
//		if (wat || ignorePlayer != skipPlayer) {
//			this.updatePlayerPos(pPlayer);
//			if (!ignorePlayer) this.distanceManager.removePlayer(oldPos, pPlayer);
//			if (!skipPlayer) this.distanceManager.addPlayer(newPos, pPlayer);
//			if (!ignorePlayer && skipPlayer) this.playerMap.ignorePlayer(pPlayer);
//			if (ignorePlayer && !skipPlayer) this.playerMap.unIgnorePlayer(pPlayer);
//			if (oldAsLong != newAsLong) this.playerMap.updatePlayer(oldAsLong, newAsLong, pPlayer);
//		}
//
//		ITrackChunks chunkTracker = (ITrackChunks) pPlayer;
//		if (!chunkTracker.setDoUpdate(false)) return;
//
//		ArrayList<ChunkPos> tracked = new ArrayList<>();
//		boolean anyFailed = false;
//		anyFailed = forAllInRange(pPlayer.position(), pPlayer, chunkTracker, tracked) || anyFailed;
//
//		portals = Temp.getPortals(level);
//		for (AbstractPortal portal : portals) {
//			if (isInRange(pPlayer, portal)) {
//				anyFailed = forAllInRange(portal.target.raytraceOffset(), pPlayer, chunkTracker, tracked) || anyFailed;
//			}
//		}
//
////		chunkTracker.trackedChunks().removeAll(tracked);
//		for (ChunkPos trackedChunk : chunkTracker.trackedChunks()) {
////			if (nonEclidianInRange(trackedChunk, pPlayer)) {
////				tracked.add(trackedChunk);
////				updateChunkTracking(
////						pPlayer, trackedChunk,
////						new MutableObject<>(),
////						true, // this will always be true, no point in checking the list
////						true // player should know about the chunk
////				);
////			} else {
//			updateChunkTracking(
//					pPlayer, trackedChunk,
//					new MutableObject<>(),
//					true, // this will always be true, no point in checking the list
//					false // unloading/untracking
//			);
////			}
//		}
//		chunkTracker.trackedChunks().clear();
//		chunkTracker.trackedChunks().addAll(tracked);
//
//		if (anyFailed) chunkTracker.setDoUpdate(true);
//		ci.cancel();
//	}
//
//	@Inject(at = @At("HEAD"), method = "updatePlayerPos")
//	public void preUpdatePos(ServerPlayer p_140374_, CallbackInfoReturnable<SectionPos> cir) {
//		((ITrackChunks) p_140374_).setDoUpdate(true);
//	}
//
//	@Unique
//	public boolean forAllInRange(Vec3 origin, ServerPlayer pPlayer, ITrackChunks chunkTracker, ArrayList<ChunkPos> tracked) {
//		boolean anyFailed = false;
//
//		ChunkPos playerChunk = pPlayer.chunkPosition();
//		Vec3 pChunkPos = new Vec3(playerChunk.x, 0, playerChunk.z);
//
//		ChunkPos center = new ChunkPos(new BlockPos(origin.x, origin.y, origin.z));
//
//		for (int x = -viewDistance; x <= viewDistance; x++) {
//			for (int z = -viewDistance; z <= viewDistance; z++) {
//				ChunkPos pos = new ChunkPos(center.x + x, center.z + z);
//				if (tracked.contains(pos)) continue;
//
////				Vec3 chunkPos = new Vec3(pos.x, 0, pos.z);
////
////				// TODO: this distance check breaks everything
////				if (chunkPos.distanceToSqr(pChunkPos) < viewDistance) {
//				boolean wasLoaded;
//				updateChunkTracking(
//						pPlayer, pos,
//						new MutableObject<>(),
//						wasLoaded = chunkTracker.trackedChunks().remove(pos), // remove it so that the next loop doesn't untrack it
//						true // start tracking
//				);
//
//				if (!wasLoaded && !success) {
//					anyFailed = true;
//				} else {
//					tracked.add(pos);
//				}
////				}
//			}
//		}
//
//		return anyFailed;
//	}
//
//	@Unique
//	public boolean isInRange(ServerPlayer pPlayer, AbstractPortal poptato /* idk */) {
//		ChunkPos playerChunk = pPlayer.chunkPosition();
//		Vec3 pChunkPos = new Vec3(playerChunk.x, 0, playerChunk.z);
//
//		Vec3 portalPos = poptato.raytraceOffset();
//		ChunkPos asChunkPos = new ChunkPos(new BlockPos(portalPos));
//
//		return new Vec3(asChunkPos.x, 0, asChunkPos.z).distanceToSqr(pChunkPos) < viewDistance;
//	}
//
//	@Unique
//	public boolean nonEclidianInRange(ChunkPos pos, ServerPlayer player) {
//		for (AbstractPortal portal : portals) {
//			if (portal.raytraceOffset().distanceTo(player.position()) / 16 < viewDistance) {
//				AbstractPortal targ = portal.target;
//				Vec3 chunkPos = new Vec3(pos.x * 16, 0, pos.z * 16);
//				if (chunkPos.distanceTo(targ.raytraceOffset().multiply(1, 0, 1)) / 16 < viewDistance) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}
//
//	@Inject(at = @At("HEAD"), method = "playerLoadedChunk")
//	public void preLoadChunk(ServerPlayer pPlaer, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, LevelChunk pChunk, CallbackInfo ci) {
//		success = true;
//	}
//
//	@Inject(at = @At("HEAD"), method = "updateChunkTracking", cancellable = true)
//	public void preUpdateTracking(ServerPlayer pPlayer, ChunkPos pChunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, boolean pWasLoaded, boolean pLoad, CallbackInfo ci) {
//		success = false;
//	}
//}
