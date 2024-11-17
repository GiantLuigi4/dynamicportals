package tfc.dynamicportals.cmd;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.UUID;

public class DypoCommand<T> {
    public static <T> Integer createNetwork(CommandContext<T> ctx) {
        CommandSourceStack stack = (CommandSourceStack) ctx.getSource();
        NetworkHolder holder = (NetworkHolder) stack.getUnsidedLevel();

        String name = ctx.getArgument("network", String.class);
        for (PortalNet portalNetwork : holder.getPortalNetworks()) {
            String identifier = portalNetwork.getCommandIdentifier();
            if (identifier == null) continue;
            if (identifier.equals(name)) {
                stack.sendFailure(new TranslatableComponent(
                        "dynamicportals.command.cheese.already_network",
                        name
                ));
                return 0;
            }
        }

        holder.getPortalNetworks().add(new PortalNet(UUID.randomUUID(), name));
        stack.sendSuccess(
                new TranslatableComponent(
                        "dynamicportals.command.bread.network.success",
                        name
                ),
                true
        );

        return 1;
    }

    public static <V> Integer createPortal(CommandContext<V> ctx, CompoundTag tag) {
        CommandSourceStack stack = (CommandSourceStack) ctx.getSource();
        NetworkHolder holder = (NetworkHolder) stack.getUnsidedLevel();

        PortalNet bindTo = null;
        String network = ctx.getArgument("network", String.class);
        for (PortalNet portalNetwork : holder.getPortalNetworks()) {
            String identifier = portalNetwork.getCommandIdentifier();
            if (identifier == null) continue;
            if (identifier.equals(network)) {
                bindTo = portalNetwork;
            }
        }

        if (bindTo == null) {
            stack.sendFailure(new TranslatableComponent(
                    "dynamicportals.command.cheese.no_network",
                    network
            ));
            return 0;
        }

        BasicPortal portal = BasicPortalTypes.createPortal(
                new ResourceLocation(tag.getString("type")),
                holder,
                tag
        );
        bindTo.link(portal);
        // TODO: network mutated packet

        stack.sendSuccess(
                new TranslatableComponent(
                        "dynamicportals.command.bread.create.success"
                ),
                true
        );
        return 1;
    }
}
