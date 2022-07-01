package tfc.dynamicportals.command;

import com.mojang.brigadier.context.CommandContext;

import java.util.List;

@FunctionalInterface
public interface FullPortalFilter {
	CommandPortal[] filter(List<CommandPortal> portals, CommandContext<?> ctx);
}
