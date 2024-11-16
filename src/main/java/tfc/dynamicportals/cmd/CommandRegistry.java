package tfc.dynamicportals.cmd;

import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import tfc.dynamicportals.cmd.nodes.VanillaNode;

public class CommandRegistry {
    // java generics are so spaghetti, that this isn't able to be put into the DypoCommand class because it makes java think that Event isn't convertable to RegisterCommandsEvent when trying to compile the code to register the event listener, even though RegisterCommandsEvent should be being converted to Event
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().getRoot().addChild(
                new DypoCmdNode<>(
                        "dynamic_portals",
                        VanillaNode.literal("dynamic_portals"),
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
