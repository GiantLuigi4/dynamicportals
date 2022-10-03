package tfc.dynamicportals.mixin.common.core;

import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.access.GameListenerAccessor;
import tfc.dynamicportals.access.PortalTeleportationPacket;
import tfc.dynamicportals.api.AbstractPortal;

@Mixin(ServerboundMovePlayerPacket.class)
public abstract class MotionPacketMixin {
	@Shadow
	public abstract float getYRot(float pDefaultValue);
	
	@Shadow
	public abstract float getXRot(float pDefaultValue);
	
	@Inject(at = @At("HEAD"), method = "handle(Lnet/minecraft/network/protocol/game/ServerGamePacketListener;)V", cancellable = true)
	public void preHandle(ServerGamePacketListener pHandler, CallbackInfo ci) {
		if (this instanceof PortalTeleportationPacket pkt) {
			if (pkt.isTeleport()) {
				if (((Object) this) instanceof ServerboundMovePlayerPacket.PosRot pr) {
					Player player = ((GameListenerAccessor) pHandler).getPlayer();
					
					float yRot = Mth.wrapDegrees(this.getYRot(player.getYRot()));
					float xRot = Mth.wrapDegrees(this.getXRot(player.getXRot()));
					
					player.absMoveTo(player.getX(), player.getY(), player.getZ(), yRot, xRot);
					
					for (AbstractPortal portal : Temp.getPortals(player.level)) {
						if (portal.uuid.equals(pkt.getPortalUUID())) {
							Vec3 vec = player.getPosition(0);
							portal.finishMove(player, player.position(), new Vec3(0, 0, 0));
							if (
									player.getPosition(0).distanceToSqr(portal.target.nearestPoint(pkt.getTargetSpot())) >
											player.getSpeed() * 20 // one second of lag
							) {
								player.absMoveTo(vec.x, vec.y, vec.z, yRot, xRot);
							}
						}
					}
				}
				ci.cancel();
			}
		}
	}
}
