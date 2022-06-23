//package tfc.dynamicportals.mixin.common.quality;
//
//import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
//import net.minecraft.server.level.ChunkMap;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.chunk.LevelChunk;
//import net.minecraft.world.phys.Vec3;
//import org.apache.commons.lang3.mutable.MutableObject;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import tfc.dynamicportals.Temp;
//import tfc.dynamicportals.access.ITrackChunks;
//import tfc.dynamicportals.api.AbstractPortal;
//
//// TODO: this works, but it works at a horrendous speed
//@Mixin(ChunkMap.class)
//public abstract class ChunkMapMixin {
//	@Shadow
//	@Final
//	private ServerLevel level;
//
//	@Shadow
//	public static native boolean isChunkInRange(int p_200879_, int p_200880_, int p_200881_, int p_200882_, int p_200883_);
//
//	@Shadow
//	private int viewDistance;
//
////	@ModifyArgs(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;updateChunkTracking(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;Lorg/apache/commons/lang3/mutable/MutableObject;ZZ)V"))
////	public void modifyTrackingArgs0(Args args) {
////		handleTrackingArgs(args);
////	}
////
////	@ModifyArgs(method = "updatePlayerStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;updateChunkTracking(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;Lorg/apache/commons/lang3/mutable/MutableObject;ZZ)V"))
////	public void modifyTrackingArgs1(Args args) {
////		handleTrackingArgs(args);
////	}
////
////	// TODO: some sort of nonsense to get this to work?
////	@ModifyArgs(method = {"lambda$setViewDistance$45", "m_212853_"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;updateChunkTracking(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;Lorg/apache/commons/lang3/mutable/MutableObject;ZZ)V"))
////	private static void modifyTrackingArgs2(Args args) {
////		handleTrackingArgs(args);
////	}
//
//	@Shadow
//	protected abstract void updateChunkTracking(ServerPlayer pPlayer, ChunkPos pChunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, boolean pWasLoaded, boolean pLoad);
//
//	@Shadow
//	protected abstract void playerLoadedChunk(ServerPlayer pPlaer, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, LevelChunk pChunk);
//
//	@Inject(at = @At("HEAD"), method = "updateChunkTracking", cancellable = true)
//	public void preUpdateChunkTracking(ServerPlayer pPlayer, ChunkPos pChunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, boolean pWasLoaded, boolean pLoad, CallbackInfo ci) {
//		if (!(pLoad && pWasLoaded)) {
//			if (!pWasLoaded) {
//				ITrackChunks chunkTracker = ((ITrackChunks) pPlayer);
//				if (!chunkTracker.oldTrackedChunks().contains(pChunkPos)) {
//					return;
//				}
//			}
//			boolean[] booleans = new boolean[2];
//			if (pLoad) {
//				ITrackChunks chunkTracker = ((ITrackChunks) pPlayer);
//				booleans[0] = chunkTracker.oldTrackedChunks().contains(pChunkPos);
//				booleans[1] = true;
//			} else {
//				booleans = handleTrackingArgs(pPlayer, pChunkPos, pPacketCache, pWasLoaded, pLoad);
//			}
//			if (booleans[0] != pWasLoaded || booleans[1] != pLoad) {
//				// TODO: this is dumb
//				if (booleans[0] != booleans[1]) {
//					updateChunkTracking(pPlayer, pChunkPos, pPacketCache, booleans[0], booleans[1]);
//				} else {
//					ITrackChunks chunkTracker = ((ITrackChunks) pPlayer);
//					chunkTracker.trackedChunks().add(pChunkPos);
//					chunkTracker.oldTrackedChunks().remove(pChunkPos);
//					ci.cancel();
//					return;
//				}
//				ci.cancel();
//				return;
//			}
//		}
//		ITrackChunks chunkTracker = ((ITrackChunks) pPlayer);
//		if (!chunkTracker.oldTrackedChunks().contains(pChunkPos)) {
//			if (pLoad) {
//				chunkTracker.trackedChunks().add(pChunkPos);
//				chunkTracker.oldTrackedChunks().remove(pChunkPos);
//			}
////		} else if (!pLoad) chunkTracker.trackedChunks().remove(pChunkPos);
//		}
//	}
//
//	@Inject(at = @At("HEAD"), method = "move")
//	public void preMove(ServerPlayer pPlayer, CallbackInfo ci) {
//		ITrackChunks tracker = (ITrackChunks) pPlayer;
//		tracker.tickTracking();
//	}
//
//	@Inject(at = @At("TAIL"), method = "move")
//	public void postMove(ServerPlayer pPlayer, CallbackInfo ci) {
//		ITrackChunks tracker = (ITrackChunks) pPlayer;
//		for (ChunkPos trackedChunk : tracker.oldTrackedChunks()) {
//			for (AbstractPortal portal : Temp.getPortals(pPlayer.level)) {
//				if (!inRange(portal, pPlayer, trackedChunk)) {
//					updateChunkTracking(pPlayer, trackedChunk, new MutableObject<>(), true, false);
//				}
//			}
//		}
//	}
//
//	private boolean inRange(AbstractPortal portal, Player player, ChunkPos pos) {
//		Vec3 offset = portal.raytraceOffset();
//		offset = offset.multiply(1, 0, 1);
//		// TODO: check this
//		if ((offset.distanceTo(new Vec3((pos.getMinBlockX() + pos.getMaxBlockX()) / 2d, 0, (pos.getMinBlockZ() + pos.getMaxBlockZ()) / 2d)) / 16) < viewDistance) {
//			offset = portal.target.raytraceOffset();
//			offset = offset.multiply(1, 0, 1);
//			if ((offset.distanceTo(player.position()) / 16) < viewDistance) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private boolean[] handleTrackingArgs(ServerPlayer pPlayer, ChunkPos pChunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, boolean pWasLoaded, boolean pLoad) {
//		// TODO: I'm sure there's a better way to do this
//		ITrackChunks chunkTracker = (ITrackChunks) pPlayer;
//		for (AbstractPortal portal : Temp.getPortals(level)) {
//			if (inRange(portal, pPlayer, pChunkPos)) {
//				return new boolean[]{chunkTracker.oldTrackedChunks().contains(pChunkPos), true};
//			}
//		}
//		return new boolean[]{chunkTracker.oldTrackedChunks().contains(pChunkPos), pLoad};
//	}
//}
