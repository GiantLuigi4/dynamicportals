package tfc.dynamicportals.api.registry;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.cmd.CommandRegistry;
import tfc.dynamicportals.cmd.nodes.DypoNode;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.function.BiFunction;

public class PortalType<T extends BasicPortal> {
	BiFunction<NetworkHolder, CompoundTag, T> fromNbt;
	ResourceLocation name;
	
	public PortalType(BiFunction<NetworkHolder, CompoundTag, T> fromNbt) {
		this.fromNbt = fromNbt;
	}
	
	public ResourceLocation getRegistryName() {
		return name;
	}

	public boolean supportsCommand() {
		return false;
	}

	public <T extends CommandContext<V>, V> void fillCommand(
			DypoNode<T, CompoundTag, CompoundTag> create,
			DypoNode<T, CompoundTag, CompoundTag> modify
	) {
		CommandRegistry.fillBasic(this, create, modify);
	}
}