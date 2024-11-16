package tfc.dynamicportals.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.cmd.nodes.DypoNode;
import tfc.dynamicportals.itf.NetworkHolder;

import javax.json.JsonObject;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DypoCommand<T> implements Command<T> {
    public static <V> Integer createPortal(CommandContext<V> ctx, CompoundTag tag) {
        CommandSourceStack stack = (CommandSourceStack) ctx.getSource();
        NetworkHolder holder = (NetworkHolder) stack.getUnsidedLevel();

        PortalNet bindTo = null;
        for (PortalNet portalNetwork : holder.getPortalNetworks()) {
            String identifier = portalNetwork.getCommandIdentifier();
            if (identifier == null) continue;
            if (identifier.equals(
                    "test"
            )) {
                bindTo = portalNetwork;
            }
        }

        if (bindTo == null) {
            stack.sendFailure(new TranslatableComponent(
                    "dynamicportals.command.cheese.no_network"
            ));
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

    @Override
    public int run(CommandContext<T> context) throws CommandSyntaxException {
        DataHolderNode<T> node = CommandNodeAccessor.getDpCtx(context);
        Object data = null;
        BiFunction<Object, Object, Integer> postAction = null;
        for (DypoNode dypoNode : node.dctx.nodes) {
            data = dypoNode.execute(context, data);
            if (dypoNode.getPostAction() != null) postAction = dypoNode.getPostAction();
        }
        if (postAction != null) return postAction.apply(context, data);
        return 0;
    }
}
