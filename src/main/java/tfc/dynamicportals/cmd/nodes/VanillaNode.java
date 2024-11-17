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
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.nbt.CompoundTag;
import tfc.dynamicportals.cmd.CommandNodeAccessor;
import tfc.dynamicportals.cmd.DypoContextBuilder;

import java.util.concurrent.CompletableFuture;

public class VanillaNode<T, A, B> extends DypoNode<T, A, B> {
    CommandNode<T> vanilla;

    public VanillaNode(CommandNode<T> vanilla) {
        this.vanilla = vanilla;
    }

    public static <Q, D, C> VanillaNode<Q, D, C> literal(String text) {
        return (VanillaNode<Q, D, C>) new VanillaNode<>(LiteralArgumentBuilder.literal(text).build());
    }

    public static <Q, D, C> VanillaNode<Q, D, C> stringArg(String argName) {
        return (VanillaNode<Q, D, C>) new VanillaNode<>(
                RequiredArgumentBuilder.argument(argName, StringArgumentType.word()).build()
        );
    }

    public static <Q, D, C> VanillaNode<Q, D, C> positionArg(String argName) {
        return (VanillaNode<Q, D, C>) new VanillaNode<>(
                RequiredArgumentBuilder.argument(argName, Vec3Argument.vec3()).build()
        );
    }

    public static <Q, D, C> VanillaNode<Q, D, C> vec2(String argName) {
        return (VanillaNode<Q, D, C>) new VanillaNode<>(
                RequiredArgumentBuilder.argument(argName, Vec2Argument.vec2()).build()
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
