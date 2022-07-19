package tfc.dynamicportals.command;

import com.mojang.brigadier.context.CommandContext;
import tfc.dynamicportals.command.portals.CommandPortal;

import java.util.List;

@FunctionalInterface
public interface PortalFilter extends FullPortalFilter {
	default CommandPortal[] filter(List<CommandPortal> portals, CommandContext<?> ctx) {
		return filter(portals);
	}
	
	CommandPortal[] filter(List<CommandPortal> portals);
}
