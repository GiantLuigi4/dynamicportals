package tfc.dynamicportals.cmd;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.cmd.nodes.ChoiceNode;
import tfc.dynamicportals.cmd.nodes.DypoNode;
import tfc.dynamicportals.cmd.nodes.VanillaNode;

public class CommandRegistry {
    // java generics are so spaghetti, that this isn't able to be put into the DypoCommand class because it makes java think that Event isn't convertable to RegisterCommandsEvent when trying to compile the code to register the event listener, even though RegisterCommandsEvent should be being converted to Event
    public static void register(RegisterCommandsEvent event) {

        DypoNode root = VanillaNode.literal("dynamic_portals");
        {
            DypoNode network = VanillaNode.literal("network");
            root.addArg(network);
        }
        {
            DypoNode portal = VanillaNode.literal("portal");
            DypoNode create = VanillaNode.literal("create");
            DypoNode modify = VanillaNode.literal("modify");
            DypoNode delete = VanillaNode.literal("delete");

            BasicPortalTypes.forEach((k, v) -> {
                if (v.supportsCommand()) {
                    DypoNode branchCreate = VanillaNode.literal(k.toString());
                    DypoNode branchModif = VanillaNode.literal(k.toString());

                    v.fillCommand(branchCreate, branchModif);

                    create.addArg(branchCreate);
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

    public static <T> void fillDefault(DypoNode<T> create, DypoNode<T> modify) {
        // TODO:
    }
}
