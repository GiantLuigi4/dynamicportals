package tfc.dynamicportals.api.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.implementation.BasicPortal;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PortalType<T extends BasicPortal> {
	Function<CompoundTag, T> fromNbt;
	ResourceLocation name;
	
	public PortalType(Function<CompoundTag, T> fromNbt) {
		this.fromNbt = fromNbt;
	}
	
	public ResourceLocation getRegistryName() {
		return name;
	}
}