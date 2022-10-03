package tfc.dynamicportals.access;

import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public interface PortalTeleportationPacket {
	void setTeleport();
	boolean isTeleport();
	
	void setPortalUUID(UUID uuid);
	UUID getPortalUUID();
	
	void setTargetSpot(Vec3 vec);
	Vec3 getTargetSpot();
}
