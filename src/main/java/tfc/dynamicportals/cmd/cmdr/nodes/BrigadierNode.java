package tfc.dynamicportals.cmd.cmdr.nodes;

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
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import tfc.dynamicportals.cmd.cmdr.CommandNodeAccessor;
import tfc.dynamicportals.cmd.cmdr.CmdRContext;

import java.util.concurrent.CompletableFuture;

public class BrigadierNode<T, A, B> extends CmdRNode<T, A, B> {
    CommandNode<T> vanilla;

    public BrigadierNode(CommandNode<T> vanilla) {
        this.vanilla = vanilla;
    }

    public static <Q, D, C> BrigadierNode<Q, D, C> literal(String text) {
        return (BrigadierNode<Q, D, C>) new BrigadierNode<>(LiteralArgumentBuilder.literal(text).build());
    }

    public static <Q, D, C> BrigadierNode<Q, D, C> stringArg(String argName) {
        return (BrigadierNode<Q, D, C>) new BrigadierNode<>(
                RequiredArgumentBuilder.argument(argName, StringArgumentType.word()).build()
        );
    }

    public static <Q, D, C> BrigadierNode<Q, D, C> positionArg(String argName) {
        return (BrigadierNode<Q, D, C>) new BrigadierNode<>(
                RequiredArgumentBuilder.argument(argName, Vec3Argument.vec3()).build()
        );
    }

    public static <Q, D, C> BrigadierNode<Q, D, C> vec2Arg(String argName) {
        return (BrigadierNode<Q, D, C>) new BrigadierNode<>(
                RequiredArgumentBuilder.argument(argName, Vec2Argument.vec2(false)).build()
        );
    }

    @Override
    public CommandSyntaxException parse(StringReader reader, CommandContextBuilder<T> builder, CmdRContext dpbuilder) {
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
    public CompletableFuture<Suggestions> mySuggestions(CommandContext<T> context, SuggestionsBuilder builder, CmdRContext ctx) {
        try {
            return CommandNodeAccessor.listSuggestions(vanilla, context, builder);
        } catch (Throwable err) {
            err.printStackTrace();
            return builder.buildFuture();
        }
    }
}
