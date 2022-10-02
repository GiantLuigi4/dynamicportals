package tfc.dynamicportals;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.access.ITeleportTroughPacket;
import tfc.dynamicportals.api.AbstractPortal;

public class TeleportationHandler {
	public static Vec3 getTeleportedMotion(Entity entity, Vec3 motion) {
		// TODO: handle pre teleportation collision
		boolean wentThrough = false;
		AbstractPortal[] portals = Temp.getPortals(entity.level);
		for (AbstractPortal portal : portals) {
			Vec3 pos = entity.position();
			if (portal.canTeleport(entity, new Vec3(pos.x, pos.y + entity.getEyeHeight(), pos.z))) {
				if (portal.moveEntity(entity, entity.getPosition(0), motion)) {
					portal.target.finishMove(entity, entity.getPosition(0), motion);
					((ITeleportTroughPacket) entity).setTeleported();
					wentThrough = true;
					// Luigi's TODO: better handling, deny teleporting through the pair
					break;
				}
			}
		}
		return wentThrough ? entity.getDeltaMovement() : null;
	}
	
	public static void handleServerMovePlayerPacket(ServerPlayer player, ServerboundMovePlayerPacket i) {
		double x = i.getX(player.getX());
		double y = i.getY(player.getY());
		double z = i.getZ(player.getZ());
//		Vec3 motion = player.getDeltaMovement();
		double dx = x - player.position().x;
		double dy = y - player.position().y;
		double dz = z - player.position().z;
		
		getTeleportedMotion(player,
//				motion
				new Vec3(
//					x - player.position().x,
//					y - player.position().y,
//					z - player.position().z
						dx, dy, dz
				)
		);
		if (((ITeleportTroughPacket) player).hasTeleported()) {
//			player.setPosRaw(player.position().x + dx, player.position().y + dy, player.position().z + dz);
			((ITeleportTroughPacket) player).setTeleported();
		}
	}
}
