package tfc.dynamicportals.mixin.common.core;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.access.PortalTeleportationPacket;

import java.util.UUID;

@Mixin(ServerboundMovePlayerPacket.PosRot.class)
public class PosRotPacketMixin implements PortalTeleportationPacket {
	boolean isTeleport = false;
	UUID portalUUID;
	Vec3 targetSpot;
	
	@Inject(at = @At("HEAD"), method = "write")
	public void preWrite(FriendlyByteBuf pBuffer, CallbackInfo ci) {
		pBuffer.writeBoolean(isTeleport);
		if (isTeleport) {
			pBuffer.writeUUID(portalUUID);
			
			pBuffer.writeDouble(targetSpot.x);
			pBuffer.writeDouble(targetSpot.y);
			pBuffer.writeDouble(targetSpot.z);
		}
	}
	
	private static final ThreadLocal<Boolean> isTeleportTL = new ThreadLocal<>();
	private static final ThreadLocal<UUID> portalUUIDTL = new ThreadLocal<>();
	private static final ThreadLocal<Vec3> targetPosTL = new ThreadLocal<>();
	
	@Inject(at = @At("HEAD"), method = "read")
	private static void preRead(FriendlyByteBuf pBuffer, CallbackInfoReturnable<ServerboundMovePlayerPacket.Pos> cir) {
		boolean bl;
		isTeleportTL.set(bl = pBuffer.readBoolean());
		if (bl) {
			portalUUIDTL.set(pBuffer.readUUID());
			targetPosTL.set(new Vec3(pBuffer.readDouble(), pBuffer.readDouble(), pBuffer.readDouble()));
		}
	}
	
	@Inject(at = @At("RETURN"), method = "read")
	private static void postRead(FriendlyByteBuf pBuffer, CallbackInfoReturnable<ServerboundMovePlayerPacket.Pos> cir) {
		PortalTeleportationPacket teleportationPacket = (PortalTeleportationPacket) cir.getReturnValue();
		if (isTeleportTL.get()) {
			teleportationPacket.setTeleport();
			teleportationPacket.setPortalUUID(portalUUIDTL.get());
			teleportationPacket.setTargetSpot(targetPosTL.get());
		}
	}
	
	@Override
	public void setTeleport() {
		isTeleport = true;
	}
	
	@Override
	public boolean isTeleport() {
		return isTeleport;
	}
	
	@Override
	public void setPortalUUID(UUID uuid) {
		portalUUID = uuid;
	}
	
	@Override
	public UUID getPortalUUID() {
		return portalUUID;
	}
	
	@Override
	public void setTargetSpot(Vec3 vec) {
		targetSpot = vec;
	}
	
	@Override
	public Vec3 getTargetSpot() {
		return targetSpot;
	}
}
