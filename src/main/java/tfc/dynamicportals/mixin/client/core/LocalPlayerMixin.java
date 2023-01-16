package tfc.dynamicportals.mixin.client.core;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.TeleportationHandler;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.access.ITrackChunks;
import tfc.dynamicportals.access.PortalTeleportationPacket;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.util.TeleportationData;

import java.util.ArrayList;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin implements ITrackChunks {
	@Unique
	boolean teleportationTick = false;
	@Unique
	TeleportationData teleportationData;
	
	@ModifyVariable(at = @At("HEAD"), method = "move", index = 2, argsOnly = true)
	public Vec3 preMove(Vec3 motion) {
		TeleportationData data = TeleportationHandler.getTeleportationData((Entity) (Object) this, motion);
		if (data != null) {
			teleportationTick = true;
			teleportationData = data;
		}
		return data == null ? motion : data.motion;
	}
	
	// TODO: I would prefer a modify args here
	@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"), method = "sendPosition")
	public Packet<?> modifyPacket(Packet<?> pPacket) {
		if (teleportationTick) {
			if (pPacket instanceof PortalTeleportationPacket pkt) {
				pkt.setTeleport();
				pkt.setTargetSpot(teleportationData.targetPos.add(teleportationData.motion));
				pkt.setPortalUUID(teleportationData.portalUUID);
				teleportationTick = false;
			}
		}
		return pPacket;
	}
	
	/* chunk tracking */
	@Unique
	boolean doUpdate = false;
	@Unique
	private ArrayList<ChunkPos> chunksBeingTracked;
	@Unique
	private ArrayList<ChunkPos> lastChunksBeingTracked;
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(Minecraft pMinecraft, ClientLevel pClientLevel, ClientPacketListener pConnection, StatsCounter pStats, ClientRecipeBook pRecipeBook, boolean pWasShiftKeyDown, boolean pWasSprinting, CallbackInfo ci) {
		chunksBeingTracked = new ArrayList<>();
		lastChunksBeingTracked = new ArrayList<>();
	}
	
	@Override
	public void tickTracking() {
		lastChunksBeingTracked = chunksBeingTracked;
		chunksBeingTracked = new ArrayList<>();
	}
	
	@Override
	public ArrayList<ChunkPos> oldTrackedChunks() {
		return lastChunksBeingTracked;
	}
	
	@Override
	public ArrayList<ChunkPos> trackedChunks() {
		return chunksBeingTracked;
	}
	/* */
	
	@Override
	public boolean setDoUpdate(boolean val) {
		boolean doUpdate = this.doUpdate;
		this.doUpdate = val;
		return doUpdate;
	}
	
	@Unique
	private double dp_xo;
	@Unique
	private double dp_yo;
	@Unique
	private double dp_zo;
	
	// TODO: allow this to run when a portal is created, removed, or modified
	@Inject(at = @At("TAIL"), method = "tick")
	public void postTick(CallbackInfo ci) {
		Entity e = (Entity) (Object) this;
		if (
				dp_xo != e.position().x ||
						dp_yo != e.position().y ||
						dp_zo != e.position().z
		) {
			for (AbstractPortal portal : Temp.getPortals(((Entity) (Object) this).getLevel())) {
				portal.tickChunkTracking((Player) (Object) this);
			}
			dp_xo = e.position().x;
			dp_yo = e.position().y;
			dp_zo = e.position().z;
		}
	}
}
