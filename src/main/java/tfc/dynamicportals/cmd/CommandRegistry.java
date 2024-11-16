package tfc.dynamicportals.cmd;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import tfc.dynamicportals.cmd.nodes.ArgumentListNode;
import tfc.dynamicportals.cmd.nodes.DypoNode;
import tfc.dynamicportals.cmd.nodes.VanillaNode;

public class CommandRegistry {
    // java generics are so spaghetti, that this isn't able to be put into the DypoCommand class because it makes java think that Event isn't convertable to RegisterCommandsEvent when trying to compile the code to register the event listener, even though RegisterCommandsEvent should be being converted to Event
    public static void register(RegisterCommandsEvent event) {
        DypoNode root = VanillaNode.literal("dynamic_portals");
        ArgumentListNode list = new ArgumentListNode("option_list", true);

        list.addChild(VanillaNode.literal("test0").addChild(list));
        list.addChild(VanillaNode.literal("test1").addChild(list));
        list.addChild(VanillaNode.literal("test2").addChild(list));
        list.addChild(VanillaNode.literal("test3").addChild(list));

        root.addChild(list);

        event.getDispatcher().getRoot().addChild(
                new DypoCmdNode<>(
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
}
