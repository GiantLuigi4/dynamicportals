package tfc.dynamicportals.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class PortalType {
	private final ResourceLocation name;
	private final Function<CompoundTag, AbstractPortal> portalCreator;
	
	public PortalType(ResourceLocation name, Function<CompoundTag, AbstractPortal> portalCreator) {
		this.name = name;
		this.portalCreator = portalCreator;
	}
	
	public AbstractPortal fromNbt(CompoundTag tag) {
		return portalCreator.apply(tag);
	}
	
	public ResourceLocation getName() {
		return name;
	}
}
