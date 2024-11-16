package tfc.dynamicportals.cmd.nodes;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import tfc.dynamicportals.cmd.DypoContextBuilder;
import tfc.dynamicportals.cmd.DypoExceptionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class DypoNode<T> {
    protected List<DypoNode<T>> children = new ArrayList<>();

    public DypoNode<T> addChild(DypoNode<T> child) {
        children.add(child);
        return this;
    }

    public abstract void _parse(
            StringReader reader,
            CommandContextBuilder<T> contextBuilder,
            DypoContextBuilder dypoContextBuilder
    ) throws CommandSyntaxException;

    public CommandContextBuilder<T> parse(
            StringReader reader,
            CommandContextBuilder<T> contextBuilder,
            DypoContextBuilder dypoContextBuilder
    ) throws CommandSyntaxException {
        _parse(reader, contextBuilder, dypoContextBuilder);

        if (reader.canRead()) {
            CommandSyntaxException err = null;

            for (DypoNode<T> child : children) {
                int cursor = reader.getCursor();
                CommandContextBuilder<T> ctx = contextBuilder.copy();
                DypoContextBuilder dctx = new DypoContextBuilder(dypoContextBuilder);
                reader.skipWhitespace();

                try {
                    if (child.isValidInput(reader)) {
                        child.parse(reader, ctx, dctx);
                        return ctx;
                    }
                } catch (CommandSyntaxException err1) {
                    err = err1;
                }

                dypoContextBuilder.set(dctx);
                reader.setCursor(cursor);
            }

            if (err != null)
                CommandNodeAccessor.rethrow(err);

            throw new CommandSyntaxException(
                    new DypoExceptionType(),
                    new LiteralMessage("Incorrect argument for command"),
                    reader.getString(),
                    reader.getCursor()
            );
        }

        return contextBuilder;
    }

    public abstract boolean isValidInput(String string);

    public boolean isValidInput(StringReader reader) {
        return isValidInput(reader.getString().substring(reader.getCursor()));
    }

    public abstract Collection<String> getExamples();

    public abstract CompletableFuture<Suggestions> _listSuggestions(
            final CommandContext<T> context,
            final SuggestionsBuilder builder,
            DypoContextBuilder dypoContextBuilder
    ) throws CommandSyntaxException;

    public CompletableFuture<Suggestions> listSuggestions(
            final CommandContext<T> context,
            final SuggestionsBuilder builder,
            DypoContextBuilder dypoContextBuilder
    ) throws CommandSyntaxException {
        return CompletableFuture.completedFuture(Suggestions.create(context.getRootNode().getName(), Collections.emptyList()));
    }

    public abstract String getName();
}
