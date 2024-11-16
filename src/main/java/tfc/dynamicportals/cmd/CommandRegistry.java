package tfc.dynamicportals.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.cmd.exception.DypoException;
import tfc.dynamicportals.cmd.nodes.DypoNode;
import tfc.dynamicportals.cmd.nodes.VanillaNode;
import tfc.dynamicportals.cmd.util.ContextHelper;

public class CommandRegistry {
    // java generics are so spaghetti, that this isn't able to be put into the DypoCommand class because it makes java think that Event isn't convertable to RegisterCommandsEvent when trying to compile the code to register the event listener, even though RegisterCommandsEvent should be being converted to Event
    public static void register(RegisterCommandsEvent event) {

        DypoNode root = VanillaNode.literal("dynamic_portals");
        {
            DypoNode network = VanillaNode.literal("network");

            DypoNode create = VanillaNode.literal("create").postAction((t, a) -> DypoCommand.createNetwork((CommandContext<?>) t));
            DypoNode delete = VanillaNode.literal("delete").setAction((a) -> {
                CommandNodeAccessor.throwUnchecked(new DypoException("NYI"));
                throw new RuntimeException("wth");
            });
            network.addArg(create);
            network.addArg(delete);
            DypoNode networkName = VanillaNode.stringArg("network");
            create.addArg(networkName);

            root.addArg(network);
        }
        {
            DypoNode portal = VanillaNode.literal("portal");

            DypoNode create = VanillaNode.literal("create")
                    .setAction((a) -> new CompoundTag())
                    .postAction((ctx, tag) -> DypoCommand.createPortal((CommandContext<?>) ctx, (CompoundTag) tag));
            DypoNode networkName = VanillaNode.stringArg("network");
            create.addArg(networkName);

            DypoNode modify = VanillaNode.literal("modify").setAction((a) -> {
                CommandNodeAccessor.throwUnchecked(new DypoException("NYI"));
                throw new RuntimeException("wth");
            });

            DypoNode delete = VanillaNode.literal("delete").setAction((a) -> {
                CommandNodeAccessor.throwUnchecked(new DypoException("NYI"));
                throw new RuntimeException("wth");
            });

            BasicPortalTypes.forEach((k, v) -> {
                if (v.supportsCommand()) {
                    DypoNode branchCreate = VanillaNode.literal(k.toString()).setAction((nbt) -> {
                        ((CompoundTag) nbt).putString("type", k.toString());
                        return nbt;
                    });
                    DypoNode branchModif = VanillaNode.literal(k.toString()).setAction((a) -> {
                        CommandNodeAccessor.throwUnchecked(new DypoException("NYI"));
                        throw new RuntimeException("wth");
                    });

                    v.fillCommand(branchCreate, branchModif);

                    networkName.addArg(branchCreate);
                    modify.addArg(branchModif);
                }
            });

            portal.addArg(create);
            portal.addArg(modify);
            portal.addArg(delete);
            root.addArg(portal);
        }

        //noinspection RedundantCast
        event.getDispatcher().getRoot().addChild(
                new DypoCmdNode<>(
                        (CommandDispatcher) event.getDispatcher(),
                        "dynamic_portals",
                        root,
                        new DypoCommand(),
                        (o) -> {
                            if (o instanceof CommandSourceStack stk) {
                                return stk.hasPermission(4);
                            } else if (o instanceof ClientSuggestionProvider provider) {
                                return provider.hasPermission(4);
                            }
                            throw new RuntimeException("what");
                        },
                        null, null,
                        false
                )
        );
    }

    public static <T extends CommandContext<V>, V> void fillDefault(
            PortalType<?> type,
            DypoNode<T, CompoundTag, CompoundTag> create,
            DypoNode<T, CompoundTag, CompoundTag> modify
    ) {
        create.setAction((t, a) -> {
            a.putString("type", type.getRegistryName().toString());

            CommandSourceStack src = ((CommandSourceStack) t.getSource());
            Vec3 position = src.getPosition();

            a.put("level", ContextHelper.getLevelTag(t));

            a.putLongArray(
                    "coords",
                    new long[]{
                            Double.doubleToLongBits(position.x),
                            Double.doubleToLongBits(position.y),
                            Double.doubleToLongBits(position.z)
                    }
            );

            return a;
        });
        // TODO:
    }
}
