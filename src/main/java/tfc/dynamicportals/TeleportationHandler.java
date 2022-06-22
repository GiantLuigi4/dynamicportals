package tfc.dynamicportals;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.access.IMaySkipPacket;
import tfc.dynamicportals.api.AbstractPortal;

public class TeleportationHandler {
	public static void handle(Entity entity, Vec3 motion) {
		AbstractPortal[] portals = Temp.getPortals(entity.level);
		for (AbstractPortal portal : portals) {
			Vec3 pos = entity.position();
			if (portal.shouldRender(null, pos.x, pos.y, pos.z)) {
				if (portal.moveEntity(entity, entity.getPosition(0), motion)) {
					((IMaySkipPacket) entity).setSkipTeleportPacket();
					// TODO: better handling, deny teleporting through the pair
					break;
				}
			}
		}
	}
	
	public static void handlePacket(ServerPlayer player, ServerboundMovePlayerPacket i) {
		double x = i.getX(player.getX());
		double y = i.getY(player.getY());
		double z = i.getZ(player.getZ());
//		Vec3 motion = player.getDeltaMovement();
		double dx = x - player.position().x;
		double dy = y - player.position().y;
		double dz = z - player.position().z;
		handle(player,
//				motion
				new Vec3(
//					x - player.position().x,
//					y - player.position().y,
//					z - player.position().z
						dx, dy, dz
				)
		);
		if (((IMaySkipPacket) player).skip()) {
			player.setPosRaw(player.position().x + dx, player.position().y + dy, player.position().z + dz);
			((IMaySkipPacket) player).setSkipTeleportPacket();
		}
	}
}