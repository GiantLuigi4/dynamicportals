package tfc.dynamicportals.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.function.Function;

public class PortalRegistry {
	private static final HashMap<ResourceLocation, PortalType> types = new HashMap<>();
	
	public static synchronized PortalType register(ResourceLocation name, Function<CompoundTag, AbstractPortal> portalLoader) {
		PortalType type = new PortalType(name, portalLoader);
		synchronized (types) {
			types.put(name, type);
		}
		return type;
	}
	
	public AbstractPortal load(CompoundTag tag) {
		return types.get(new ResourceLocation(tag.getString("type"))).fromNbt(tag);
	}
}
