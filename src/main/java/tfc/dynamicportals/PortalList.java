package tfc.dynamicportals;

import com.jozufozu.flywheel.repack.joml.Vector2d;
import com.mojang.math.Vector3d;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.BasicPortal;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class PortalList {
	//	private static final HashMap<Level, HashMap<UUID, AbstractPortal>> portals = new HashMap<>();
	private static final HashMap<UUID, AbstractPortal> portals = new HashMap<>();
	
	static {
		double width = Math.sqrt(2 * 4);
		{
			BasicPortal portal = new BasicPortal(new UUID(System.nanoTime(), new Random().nextLong()), new Vector3d(5, 5, 5), new Vector2d(width, 2), null, false);
			BasicPortal other = new BasicPortal(new UUID(System.nanoTime(), new Random().nextLong()), new Vector3d(-5, 5, -5), new Vector2d(width, 2), portal.uuid, true);
			portal.target = other.uuid;
			portal.rotation = new Vector2d(Math.toRadians(45 + 180), 0);
			other.rotation = new Vector2d(Math.toRadians(45), 0);
			portals.put(portal.uuid, portal);
			portals.put(other.uuid, other);
		}
		{
			BasicPortal portal = new BasicPortal(new UUID(System.nanoTime(), new Random().nextLong()), new Vector3d(0, 5, 5.001), new Vector2d(2, 2), null, false);
//			Portal other = new Portal(new Vector3d(0, 5, -5), new Vector2d(2, 2), null, null, null);
			portal.rotation = new Vector2d(Math.toRadians(180), 0);
//			other.rotation = new Vector2d(Math.toRadians(0), 0);
			portal.target = portal.uuid;
			portals.put(portal.uuid, portal);
//			portals.put(other.uuid, other);
		}
	}
	
	public static AbstractPortal getPortal(UUID uuid) {
//		for (HashMap<UUID, AbstractPortal> value : portals.values())
//			return value.get(uuid);
//		return null;
		return portals.get(uuid);
	}
	
	public static AbstractPortal getPortal(Level level, UUID uuid) {
//		return portals.get(level).get(uuid);
		return portals.get(uuid);
	}
}
