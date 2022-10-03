package tfc.dynamicportals.mixin.client.core;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tfc.dynamicportals.TeleportationHandler;
import tfc.dynamicportals.access.PortalTeleportationPacket;
import tfc.dynamicportals.util.TeleportationData;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
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
				pkt.setTargetSpot(teleportationData.targetPos);
				pkt.setPortalUUID(teleportationData.portalUUID);
				teleportationTick = false;
			}
		}
		return pPacket;
	}
}
