package tfc.dynamicportals;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.command.FullPortalFilter;
import tfc.dynamicportals.command.portals.CommandPortal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Temp {
	private static final List<CommandPortal> cmdPortals = new ArrayList<>();
	private static ArrayList<AbstractPortal> portals = new ArrayList<>();

//	static {
//		ArrayList<AbstractPortal> portals = new ArrayList<>();
//		double rotation = 0;
//		int count = 2;
//		for (int i = 0; i < count; i++) {
//			rotation += 360 / (count * 2d);
//			double c = Math.cos(Math.toRadians(rotation));
//			double s = Math.sin(Math.toRadians(rotation));
//			double s1 = Math.sin(Math.toRadians((rotation / 3.) + 180));
//			double c1 = Math.sin(Math.toRadians((rotation / 8.) + 180));
//			BasicPortal other = new BasicPortal(new UUID(2372, i * 2))
//					.setSize(2, 3)
//					.setPosition((int) (s * -7 - c1 * 10), 5, (int) (c * -7 - c1 * 10) - 0.5)
//					.setRotation(Math.toRadians(0), Math.toRadians(0), 0);
//			portals.add(other);
//			{
//				BasicPortal portal = new BasicPortal(new UUID(2372, i * 2 + 1))
//						.setSize(2, 3)
//						.setPosition((int) (s * 7 + s1 * 10), 5, (int) (c * 7 + s1 * 10) + 0.5)
//						.setRotation(Math.toRadians(90), Math.toRadians(0), 0);
//				portals.add(portal);
//				other.target = portal;
//				portal.target = other;
//			}
//		}
//
//		Temp.portals = portals;
//	}
	
	public static CommandPortal get(Level lvl, int id) {
		synchronized (cmdPortals) {
			for (CommandPortal cmdPortal : cmdPortals) {
				if (cmdPortal.myId() == id) {
					return cmdPortal;
				}
			}
		}
		return null;
	}
	
	public static int addPortal(Level lvl, CommandPortal portal) {
		ArrayList<Integer> ints = new ArrayList<>();
		for (CommandPortal cmdPortal : cmdPortals) ints.add(cmdPortal.myId());
		int id = -1;
		int max = 0;
		for (int i = 0; i < ints.size(); i++) {
			max = Math.max(ints.get(i), max);
			if (!ints.contains(i)) {
				id = i;
				break;
			}
		}
		if (ints.size() == 0) max = -1;
		if (id == -1) id = max + 1;
		
		int v = portal.setId(id);
		synchronized (cmdPortals) {
			cmdPortals.add(portal);
		}
		if (id != v)
			// TODO: use unsafe to throw unchecked
			// lorenzo: what
			throw new RuntimeException(new IllegalArgumentException("Portal was created with an id of " + v + " even though its id was meant to be " + id));
		portals.add((AbstractPortal) portal);
		return v;
	}
	
	public static AbstractPortal[] getPortals(Level level) {
//		double time = (System.currentTimeMillis() / 30.) % 360;
//		BasicPortal portal = (BasicPortal) portals[1];
//		portal.setPosition(-23.5, 3, 0);
//		portal.setSize(3, 3);
//		portal.setRotation(Math.toRadians(0), Math.toRadians(-90), 0);
//		portal.computeRenderNormal();
//
//		portal = (BasicPortal) portals[2];
//		portal.setPosition(3, 5, 9);
//		portal = (BasicPortal) portals[3];
//		portal.setPosition(-7.5, 5, -15);
//		portal.setSize(2, 3);
//		portal.setRotation(Math.toRadians(time), Math.toRadians(0), Math.toRadians(0));
//		portal = (BasicPortal) portals[0];
//		portal.setRotation(Math.toRadians(180), Math.toRadians(90), 0);
//		portal.setPosition(-5.5, 4.75, -2);
//		portal.setSize(3, 3);
//		portal.computeRenderNormal();
		
		synchronized (portals) {
			ArrayList<AbstractPortal> allPortals = portals;
			return allPortals.toArray(new AbstractPortal[0]);
		}
	}
	
	public static CommandPortal[] filter(Level lvl, FullPortalFilter i, CommandContext<?> ctx) {
		return i.filter(List.copyOf(cmdPortals), ctx);
	}
	
	public static void remove(Level lvl, int myId) {
		synchronized (cmdPortals) {
			for (CommandPortal cmdPortal : cmdPortals) {
				if (cmdPortal.myId() == myId) {
					cmdPortals.remove(cmdPortal);
					return;
				}
			}
		}
	}
	
	public static void remove(Level lvl, UUID uuid) {
		synchronized (portals) {
			for (AbstractPortal portal : portals) {
				if (portal.uuid.equals(uuid)) {
					portals.remove(portal);
					synchronized (cmdPortals) {
						if (portal instanceof CommandPortal)
							cmdPortals.remove(portal);
						return;
					}
				}
			}
		}
	}
	
	public static AbstractPortal getPortal(Level level, UUID target) {
		for (AbstractPortal portal : getPortals(level))
			if (portal.uuid.equals(target))
				return portal;
		return null;
	}
	
	public static void addRegularPortal(Level level, BasicPortal portal) {
		synchronized (portals) {
			portals.add(portal);
		}
	}
}
