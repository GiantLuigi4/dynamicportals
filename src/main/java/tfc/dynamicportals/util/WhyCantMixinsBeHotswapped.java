package tfc.dynamicportals.util;

import com.mojang.logging.LogUtils;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.access.GameListenerAccessor;
import tfc.dynamicportals.access.PortalTeleportationPacket;
import tfc.dynamicportals.api.AbstractPortal;

public class WhyCantMixinsBeHotswapped {
	private static final Logger LOGGER = LogUtils.getLogger();
	
	public static void handleMotionPacket(ServerboundMovePlayerPacket packet, ServerGamePacketListener pHandler, CallbackInfo ci) {
		if (packet instanceof PortalTeleportationPacket pkt) {
			if (pkt.isTeleport()) {
				Player player = ((GameListenerAccessor) pHandler).getPlayer();
				// 20: 20 ticks in a second, so one second of lag
				// 2: the player actually moves even faster than that, so I need to double it
				double speed = player.getSpeed() * 2 * 20;
				
				float yRot = Mth.wrapDegrees(packet.getYRot(player.getYRot()));
				float xRot = Mth.wrapDegrees(packet.getXRot(player.getXRot()));
				
				// TODO: swap dimensions if the portal crosses dimensions
				player.absMoveTo(player.getX(), player.getY(), player.getZ(), yRot, xRot);
				
				for (AbstractPortal portal : Temp.getPortals(player.level)) {
					if (portal.uuid.equals(pkt.getPortalUUID())) {
						Vec3 vec = player.getPosition(0);
						Vec3 eye = player.getEyePosition(0);
						double d = 0;
						if (
								(d = Math.sqrt(vec.distanceToSqr(portal.nearestPoint(eye)))) <
										speed
						) {
							if (
								// TODO: better validation on this
									(d = Math.sqrt(pkt.getTargetSpot().distanceToSqr(portal.target.nearestPoint(pkt.getTargetSpot())))) <
											speed
							) {
								portal.finishMove(player, player.position(), new Vec3(0, 0, 0));
								
								player.absMoveTo(pkt.getTargetSpot().x, pkt.getTargetSpot().y, pkt.getTargetSpot().z);
								((GameListenerAccessor) pHandler).setPosition(
//										getX(pkt.getTargetSpot().x), getY(pkt.getTargetSpot().y), getZ(pkt.getTargetSpot().z)
										pkt.getTargetSpot().x, pkt.getTargetSpot().y, pkt.getTargetSpot().z
								);
//								ci.cancel();
							} else {
								player.absMoveTo(vec.x, vec.y, vec.z, yRot, xRot);
								LOGGER.warn("{} teleported wrongly! (Teleported to {} blocks away from the target portal, allowed to be {} blocks away.)", player.getScoreboardName(), d, speed);
							}
						} else {
							// TODO: this gets run when teleporting due to doubled packets
							player.absMoveTo(vec.x, vec.y, vec.z, yRot, xRot);
							LOGGER.warn("{} teleported wrongly! (Teleported while {} blocks away from the portal, allowed to be {} blocks away.)", player.getScoreboardName(), d, speed);
						}
					}
				}
			}
		}
	}
}
