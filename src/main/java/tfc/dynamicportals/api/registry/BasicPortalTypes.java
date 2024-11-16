package tfc.dynamicportals.api.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.cmd.CommandRegistry;
import tfc.dynamicportals.cmd.nodes.DypoNode;
import tfc.dynamicportals.itf.NetworkHolder;

import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

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
                new ResourceLocation("dynamic_portals:basic"), new PortalType<>((holder, tag) -> {
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
                }) {
                    @Override
                    public boolean supportsCommand() {
                        return true;
                    }
                }
        );
    }

    public static BasicPortal createPortal(ResourceLocation type, NetworkHolder holder, CompoundTag tag) {
        return TYPES.get(type).fromNbt.apply(holder, tag);
    }

    public static void forEach(BiConsumer<ResourceLocation, PortalType<?>> consumer) {
        TYPES.forEach(consumer);
    }
}