package tfc.dynamicportals.api.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.portals.vanilla.EndPortal;
import tfc.dynamicportals.portals.vanilla.NetherPortal;
import tfc.dynamicportals.util.Vec2d;

import java.util.HashMap;

// TODO: convert to deferred register?
public class BasicPortalTypes {
	public static final PortalType<?> BASIC;
	public static final PortalType<?> NETHER;
	public static final PortalType<?> END;
	
	protected static HashMap<ResourceLocation, PortalType<?>> TYPES = new HashMap<>();
	
	public static <T extends BasicPortal> PortalType<T> register(ResourceLocation location, PortalType<T> type) {
		TYPES.put(location, type);
		type.name = location;
		return type;
	}
	
	static {
		BASIC = register(new ResourceLocation("dynamic_portals:basic"), new PortalType<>((level, tag) -> defaultTagLoad(level, new BasicPortal(tag.getUUID("UUID")), tag)));
		NETHER = register(new ResourceLocation("dynamic_portals:nether"), new PortalType<>((level, tag) -> defaultTagLoad(level, new NetherPortal(tag.getUUID("UUID")), tag)));
		END = register(new ResourceLocation("dynamic_portals:end"), new PortalType<>((level, tag) -> defaultTagLoad(level, new EndPortal(tag.getUUID("UUID")), tag)));
	}
	
	public static BasicPortal defaultTagLoad(Level level, BasicPortal portal, CompoundTag tag) {
		ListTag list = tag.getList("Position", Tag.TAG_DOUBLE);
		portal.setPosition(new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2)));
		list = tag.getList("Size", Tag.TAG_DOUBLE);
		portal.setSize(new Vec2d(list.getDouble(0), list.getDouble(1)));
		list = tag.getList("Rotation", Tag.TAG_DOUBLE);
		portal.setRotation(new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2)));
		if (tag.contains("RenderNormal", Tag.TAG_DOUBLE)) {
			list = tag.getList("RenderNormal", Tag.TAG_DOUBLE);
			portal.setRenderNormal(new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2)));
		}
		
		// TODO: handle target stuff
		
		return portal;
	}
	
	public static BasicPortal createPortal(Level level, ResourceLocation type, CompoundTag tag) {
		return TYPES.get(type).fromNbt.apply(level, tag);
	}
}
