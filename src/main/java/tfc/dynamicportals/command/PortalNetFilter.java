package tfc.dynamicportals.command;

import com.mojang.brigadier.context.CommandContext;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.PortalNet;

import java.util.List;

@FunctionalInterface
public interface PortalNetFilter {
	AbstractPortal[] apply(PortalNet net, CommandContext<?> ctx);
}