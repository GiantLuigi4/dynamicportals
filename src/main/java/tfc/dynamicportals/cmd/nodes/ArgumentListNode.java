package tfc.dynamicportals.cmd.nodes;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import tfc.dynamicportals.cmd.DypoContextBuilder;
import tfc.dynamicportals.cmd.nodes.CommandNodeAccessor;
import tfc.dynamicportals.cmd.nodes.DypoNode;
import tfc.dynamicportals.cmd.DypoExceptionType;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class ArgumentListNode<T> extends DypoNode<T> {
    boolean denyRepeat;
    String name;

    public ArgumentListNode(String name, boolean denyRepeat) {
        this.name = name;
        this.denyRepeat = denyRepeat;
    }

    class UsageData {
        Set<DypoNode<T>> used = new HashSet<>();
    }

    public CommandContextBuilder<T> parse(StringReader reader, CommandContextBuilder<T> contextBuilder, DypoContextBuilder dypoContextBuilder) throws CommandSyntaxException {
        UsageData data = null;
        if (denyRepeat) {
            data = dypoContextBuilder.getData(this);
            if (data == null) {
                data = new UsageData();
                dypoContextBuilder.setData(this, data);
            }
        }

        if (reader.canRead()) {
            CommandSyntaxException err = null;

            for (DypoNode<T> child : children) {
                if (child == this) continue; // infinitely recursive if this is attempted

                int cursor = reader.getCursor();
                CommandContextBuilder<T> ctx = contextBuilder.copy();
                DypoContextBuilder dctx = new DypoContextBuilder(dypoContextBuilder);

                boolean used = data.used.contains(child);
                try {
                    if (child.isValidInput(reader)) {
                        if (denyRepeat) {
                            if (used) {
                                throw new CommandSyntaxException(
                                        new DypoExceptionType(),
                                        new LiteralMessage(
                                                "Argument of name " + child.getName() + " repeated."
                                        ),
                                        reader.getString(),
                                        reader.getCursor()
                                );
                            }
                        }
                        data.used.add(child);
                        child.parse(reader, ctx, dctx);
                        return ctx;
                    }
                } catch (CommandSyntaxException err1) {
                    err = err1;
                }

                if (!used) data.used.remove(child);
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

    @Override
    public void _parse(
            StringReader reader,
            CommandContextBuilder<T> contextBuilder,
            DypoContextBuilder dypoContextBuilder
    ) throws CommandSyntaxException {
    }

    @Override
    public boolean isValidInput(String string) {
        return true;
    }

    @Override
    public Collection<String> getExamples() {
        return Collections.emptyList();
    }

    @Override
    public CompletableFuture<Suggestions> _listSuggestions(
            CommandContext<T> context,
            SuggestionsBuilder builder,
            DypoContextBuilder contextBuilder
    ) throws CommandSyntaxException {
        return CompletableFuture.completedFuture(Suggestions.create(context.getRootNode().getName(), Collections.emptyList()));
    }

    @Override
    public String getName() {
        return name;
    }
}
