package tfc.dynamicportals.cmd.nodes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import tfc.dynamicportals.cmd.CommandNodeAccessor;
import tfc.dynamicportals.cmd.DypoContextBuilder;

import java.util.concurrent.CompletableFuture;

public class VanillaNode<T, A> extends DypoNode<T, A, A> {
    CommandNode<T> vanilla;

    public VanillaNode(CommandNode<T> vanilla) {
        this.vanilla = vanilla;
    }

    public static <Q, D> VanillaNode<Q, D> literal(String text) {
        return (VanillaNode<Q, D>) new VanillaNode<>(LiteralArgumentBuilder.literal(text).build());
    }

    public static <Q, D> VanillaNode<Q, D> stringArg(String argName) {
        return (VanillaNode<Q, D>) new VanillaNode<>(
                RequiredArgumentBuilder.argument(argName, StringArgumentType.word()).build()
        );
    }

    @Override
    public CommandSyntaxException parse(StringReader reader, CommandContextBuilder<T> builder, DypoContextBuilder dpbuilder) {
        try {
            vanilla.parse(reader, builder);
            return null; // successful parse
        } catch (CommandSyntaxException exception) {
            return exception;
        }
    }

    @Override
    public boolean isValidInput(String input) {
        return CommandNodeAccessor.isValidInput(vanilla, input);
    }

    @Override
    public CompletableFuture<Suggestions> mySuggestions(CommandContext<T> context, SuggestionsBuilder builder, DypoContextBuilder ctx) {
        try {
            return CommandNodeAccessor.listSuggestions(vanilla, context, builder);
        } catch (Throwable err) {
            err.printStackTrace();
            return builder.buildFuture();
        }
    }
}
