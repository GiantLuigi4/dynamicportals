package tfc.dynamicportals.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import tfc.dynamicportals.cmd.nodes.VanillaNode;

import java.util.UUID;
import java.util.function.Predicate;

public class DypoCommand<T> implements Command<T> {
    @Override
    public int run(CommandContext<T> context) throws CommandSyntaxException {
        if (context.getSource() instanceof CommandSourceStack stk) {
            stk.sendSuccess(
                    new TextComponent("test"),
                    true
            );
        }
        return 0;
    }
}
