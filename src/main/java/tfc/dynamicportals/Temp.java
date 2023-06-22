package tfc.dynamicportals;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.access.IHoldPortals;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.command.FullPortalFilter;
import tfc.dynamicportals.command.portals.CommandPortal;

import java.util.UUID;

public class Temp {
	public static CommandPortal get(Level lvl, int id) {
		return ((IHoldPortals) lvl).getCmdPortal(id);
	}
	
	public static void addRegularPortal(Level lvl, AbstractPortal portal) {
		((IHoldPortals) lvl).addPortal(portal);
	}
	
	public static int addPortal(Level lvl, CommandPortal portal) {
		return ((IHoldPortals) lvl).addPortal(portal);
	}
	
	public static AbstractPortal[] getPortals(Level level) {
		return ((IHoldPortals) level).getPortals();
	}
	
	public static CommandPortal[] filter(Level level, FullPortalFilter i, CommandContext<?> ctx) {
		return ((IHoldPortals) level).filter(i, ctx);
	}
	
	public static void remove(Level level, int myId) {
		((IHoldPortals) level).remove(myId);
	}
	
	public static void remove(Level level, UUID uuid) {
		((IHoldPortals) level).remove(uuid);
	}
	
	public static AbstractPortal getPortal(Level level, UUID target) {
		return ((IHoldPortals)level).getPortal(target);
	}
}
