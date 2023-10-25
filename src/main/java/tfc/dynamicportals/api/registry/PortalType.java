package tfc.dynamicportals.api.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import tfc.dynamicportals.api.implementation.BasicPortal;
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
}