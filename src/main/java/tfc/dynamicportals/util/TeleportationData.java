package tfc.dynamicportals.util;

import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class TeleportationData {
	public final Vec3 motion;
	public final Vec3 targetPos;
	public final UUID portalUUID;
	
	public TeleportationData(Vec3 motion, Vec3 targetPos, UUID portalUUID) {
		this.motion = motion;
		this.targetPos = targetPos;
		this.portalUUID = portalUUID;
	}
}
