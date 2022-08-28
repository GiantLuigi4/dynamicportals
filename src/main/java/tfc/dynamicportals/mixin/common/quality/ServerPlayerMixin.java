package tfc.dynamicportals.mixin.common.quality;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.access.ITrackChunks;
import tfc.dynamicportals.api.AbstractPortal;

import java.util.ArrayList;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements ITrackChunks {
	@Shadow
	public abstract Level getLevel();
	
	@Unique
	boolean doUpdate = false;
	@Unique
	private ArrayList<ChunkPos> chunksBeingTracked;
	@Unique
	private ArrayList<ChunkPos> lastChunksBeingTracked;
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(MinecraftServer p_143384_, ServerLevel p_143385_, GameProfile p_143386_, CallbackInfo ci) {
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
	
	@Override
	public boolean setDoUpdate(boolean val) {
		boolean doUpdate = this.doUpdate;
		this.doUpdate = val;
		return doUpdate;
	}
	
	@Unique private double dp_xo;
	@Unique private double dp_yo;
	@Unique private double dp_zo;
	
	// TODO: allow this to run when a portal is created, removed, or modified
	@Inject(at = @At("TAIL"), method = "tick")
	public void postTick(CallbackInfo ci) {
		Entity e = (Entity) (Object) this;
		if (
				dp_xo != e.position().x ||
						dp_yo != e.position().y ||
						dp_zo != e.position().z
		) {
			for (AbstractPortal portal : Temp.getPortals(getLevel())) {
				portal.tickChunkTracking((Player) (Object) this);
			}
			dp_xo = e.position().x;
			dp_yo = e.position().y;
			dp_zo = e.position().z;
		}
	}
}
