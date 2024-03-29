package tfc.dynamicportals.api.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.HashMap;
import java.util.Map;

// TODO: convert to deferred register?
public class PortalTypes {
    public static final PortalType<BasicPortal> BASIC;
    public static final PortalType<BasicPortal> NETHER;

    protected static Map<ResourceLocation, PortalType<?>> TYPES = new HashMap<>();

    public static <T extends AbstractPortal> PortalType<T> register(ResourceLocation location, PortalType<T> type) {
        TYPES.put(location, type);
        type.name = location;
        return type;
    }

    static {
        BASIC = register(
                new ResourceLocation("dynamicportals:basic"), new PortalType<>((holder, tag) -> {
                    CompoundTag key = tag.getCompound("level");
                    BasicPortal bp = new BasicPortal(
                            holder.getLoader().get(
                                    ResourceKey.create(
                                            ResourceKey.createRegistryKey(new ResourceLocation(key.getString("registry"))),
                                            new ResourceLocation(key.getString("location"))
                                    )
                            )
                    );
                    bp.load(holder, tag);
                    return bp;
                })
        );
        {
            PortalType<BasicPortal>[] lambdasAreStupid = new PortalType[1];
            NETHER = register(
                    new ResourceLocation("dynamicportals:nether"), new PortalType<>((holder, tag) -> {
                        CompoundTag key = tag.getCompound("level");
                        BasicPortal bp = new BasicPortal(
                                holder.getLoader().get(
                                        ResourceKey.create(
                                                ResourceKey.createRegistryKey(new ResourceLocation(key.getString("registry"))),
                                                new ResourceLocation(key.getString("location"))
                                        )
                                ),
                                lambdasAreStupid[0]
                        );
                        bp.load(holder, tag);
                        return bp;
                    })
            );
            lambdasAreStupid[0] = NETHER;
        }
    }

    public static AbstractPortal createPortal(ResourceLocation type, NetworkHolder holder, CompoundTag tag) {
        return TYPES.get(type).fromNbt.apply(holder, tag);
    }
}