package tfc.dynamicportals.cmd.nodes;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import tfc.dynamicportals.cmd.DypoContextBuilder;
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

    @Override
    public CommandContextBuilder<T> parse(
            StringReader reader,
            CommandContextBuilder<T> contextBuilder,
            DypoContextBuilder dypoContextBuilder,
            CommandContextBuilder<T>[] bOut
    ) throws CommandSyntaxException {
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
            CommandContextBuilder<T> tctx = null;
            DypoContextBuilder dtctx = null;
            DypoNode<T> bestChild = null;
            int maxLength = 0;

            if (!children.isEmpty())
                dypoContextBuilder.setPropagate();

            for (DypoNode<T> child : children) {
                if (child == this) continue; // infinitely recursive if this is attempted

                int cursor = reader.getCursor();
                CommandContextBuilder<T> ctx = contextBuilder.copy();
                DypoContextBuilder dctx = new DypoContextBuilder(child, dypoContextBuilder);

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
                        tctx = ctx = child.parse(reader, ctx, dctx, null);
                        dctx.setPropagate();
                        dypoContextBuilder.set(dctx);
                        return ctx;
                    }
                    throw new CommandSyntaxException(
                            new DypoExceptionType(),
                            new LiteralMessage("Incorrect argument for command"),
                            reader.getString(),
                            reader.getCursor()
                    );
                } catch (CommandSyntaxException err1) {
                    if (reader.getCursor() > maxLength) {
                        bestChild = child;
                        tctx = ctx;
                        dtctx = dctx;
//                        dtctx.setPropagate();
                        maxLength = reader.getCursor();
                        err = err1;
                    }
                }

                if (!used)
                    data.used.remove(child);
                reader.setCursor(cursor);
            }

            if (tctx != null) {
                data.used.add(bestChild);
                reader.setCursor(maxLength);
                CommandNodeAccessor.setCtx(contextBuilder, tctx);
                if (dtctx.isPropagate()) {
                    dypoContextBuilder.set(dtctx);
                }
            }
            if (err != null) {
                if (bOut != null)
                    bOut[0] = contextBuilder;
                CommandNodeAccessor.rethrow(err);
            }

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

    public CompletableFuture<Suggestions> listSuggestions(
            final CommandContext<T> context,
            final SuggestionsBuilder builder,
            DypoContextBuilder dypoContextBuilder
    ) throws CommandSyntaxException {
        try {
            int offset = dypoContextBuilder.getSuggestionOffset();

            SuggestionsBuilder builder1 = new SuggestionsBuilder(
                    builder.getInput(), builder.getStart()
            );
            builder1 = builder1.createOffset(offset);
            CompletableFuture<Suggestions> cfuture = _listSuggestions(
                    context, builder1,
                    dypoContextBuilder
            );
            List<CompletableFuture<Suggestions>> childSuggestions = new ArrayList<>();
            UsageData data = dypoContextBuilder.getData(this);
            for (DypoNode<T> child : children) {
                if (child == this) continue;
                if (data.used.contains(child)) continue;

                builder1 = new SuggestionsBuilder(
                        builder.getInput(), builder.getStart()
                );
                builder1 = builder1.createOffset(offset);
                childSuggestions.add(child._listSuggestions(context, builder1, dypoContextBuilder));
            }

            SuggestionsBuilder builder2 = new SuggestionsBuilder(
                    builder.getInput(), offset
            );
            try  {
                Suggestions suggestions = cfuture.get();
                for (Suggestion suggestion : suggestions.getList()) {
                    builder2.suggest(
                            suggestion.getText(),
                            suggestion.getTooltip()
                    );
                }
                for (CompletableFuture<Suggestions> childSuggestion : childSuggestions) {
                    suggestions = childSuggestion.get();
                    for (Suggestion suggestion : suggestions.getList()) {
                        builder2.suggest(
                                suggestion.getText(),
                                suggestion.getTooltip()
                        );
                    }
                }
            } catch (Throwable err) {
                err.printStackTrace();
            }

            return CompletableFuture.completedFuture(builder2.build());
        } catch (Throwable err) {
            return CompletableFuture.completedFuture(Suggestions.create(context.getRootNode().getName(), Collections.emptyList()));
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
