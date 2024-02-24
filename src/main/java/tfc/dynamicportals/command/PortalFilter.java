package tfc.dynamicportals.command;

import com.mojang.brigadier.context.CommandContext;
import tfc.dynamicportals.api.AbstractPortal;

import java.util.List;

@FunctionalInterface
public interface PortalFilter {
	AbstractPortal[] apply(List<AbstractPortal> portals, CommandContext<?> ctx);
}