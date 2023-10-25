package tfc.dynamicportals.api.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.implementation.BasicPortal;

import java.util.HashMap;
import java.util.Map;

// TODO: convert to deferred register?
public class BasicPortalTypes {
    public static final PortalType<BasicPortal> BASIC;

    protected static Map<ResourceLocation, PortalType<?>> TYPES = new HashMap<>();

    public static <T extends BasicPortal> PortalType<T> register(ResourceLocation location, PortalType<T> type) {
        TYPES.put(location, type);
        type.name = location;
        return type;
    }

    static {
        BASIC = register(
                new ResourceLocation("dynamic_portals:basic"), new PortalType<>((tag) -> {
                    BasicPortal bp = new BasicPortal();
                    bp.load(tag);
                    return bp;
                })
        );
    }

    public static BasicPortal createPortal(ResourceLocation type, CompoundTag tag) {
        return TYPES.get(type).fromNbt.apply(tag);
    }
}