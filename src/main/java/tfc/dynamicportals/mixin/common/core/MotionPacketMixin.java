package tfc.dynamicportals.mixin.common.core;

import com.mojang.logging.LogUtils;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
	
	@Unique
	private static final Logger LOGGER = LogUtils.getLogger();
	
	@Inject(at = @At("HEAD"), method = "handle(Lnet/minecraft/network/protocol/game/ServerGamePacketListener;)V", cancellable = true)
	public void preHandle(ServerGamePacketListener pHandler, CallbackInfo ci) {
		if (this instanceof PortalTeleportationPacket pkt) {
			if (pkt.isTeleport()) {
				Player player = ((GameListenerAccessor) pHandler).getPlayer();
				double speed = player.getSpeed() * 5; // TODO: figure out an exact multiplier?
				
				float yRot = Mth.wrapDegrees(this.getYRot(player.getYRot()));
				float xRot = Mth.wrapDegrees(this.getXRot(player.getXRot()));
				
				// TODO: swap dimensions if the portal crosses dimensions
				player.absMoveTo(player.getX(), player.getY(), player.getZ(), yRot, xRot);
				
				for (AbstractPortal portal : Temp.getPortals(player.level)) {
					if (portal.uuid.equals(pkt.getPortalUUID())) {
						Vec3 vec = player.getPosition(0);
						Vec3 eye = player.getEyePosition(0);
						double d = 0;
						if (
								(d = vec.distanceToSqr(portal.nearestPoint(eye))) <
										speed * 20 // one second of lag
						) {
							portal.finishMove(player, player.position(), new Vec3(0, 0, 0));
							if (
									// TODO: better validation on this
									(d = player.distanceToSqr(portal.target.nearestPoint(pkt.getTargetSpot()))) >
											speed * 20 // one second of lag
							) {
								player.absMoveTo(pkt.getTargetSpot().x, pkt.getTargetSpot().y, pkt.getTargetSpot().z);
								((GameListenerAccessor) pHandler).setPosition(pkt.getTargetSpot().x, pkt.getTargetSpot().y, pkt.getTargetSpot().z);
								ci.cancel();
							} else {
								player.absMoveTo(vec.x, vec.y, vec.z, yRot, xRot);
								LOGGER.warn("{} teleported wrongly! (Teleported while {} blocks away from the target portal.)", player.getScoreboardName(), d);
							}
						} else {
							player.absMoveTo(vec.x, vec.y, vec.z, yRot, xRot);
							LOGGER.warn("{} teleported wrongly! (Teleported while {} blocks away from the source portal.)", player.getScoreboardName(), d);
						}
					}
				}
			}
		}
	}
}
