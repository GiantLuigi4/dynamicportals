package tfc.dynamicportals.access;

import com.mojang.brigadier.context.CommandContext;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.command.FullPortalFilter;
import tfc.dynamicportals.command.portals.CommandPortal;

import java.util.List;
import java.util.UUID;

//@formatter:off
public interface IHoldPortals {
	void addPortal(AbstractPortal portal);
	void addCommandPortal(CommandPortal portal);
	CommandPortal getCmdPortal(int id);
	int addPortal(CommandPortal portal);
	AbstractPortal[] getPortals();
	AbstractPortal getPortal(UUID uuid);
	CommandPortal[] filter(FullPortalFilter i, CommandContext<?> ctx);
	void remove(int myId);
	void remove(UUID uuid);
	
	List<AbstractPortal> getNewPortals();
	boolean isNew(AbstractPortal portal);
}
