package tfc.dynamicportals.api.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.implementation.BasicPortal;

import java.util.function.BiFunction;

public class PortalType<T extends BasicPortal> {
	BiFunction<Level, CompoundTag, T> fromNbt;
	ResourceLocation name;
	
	public PortalType(BiFunction<Level, CompoundTag, T> fromNbt) {
		this.fromNbt = fromNbt;
	}
	
	public ResourceLocation getRegistryName() {
		return name;
	}
}
