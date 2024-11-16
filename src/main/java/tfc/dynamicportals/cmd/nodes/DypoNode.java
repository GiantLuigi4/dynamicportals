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
            DypoContextBuilder dypoContextBuilder,
            CommandContextBuilder<T>[] bOut
    ) throws CommandSyntaxException {
        _parse(reader, contextBuilder, dypoContextBuilder);

        if (reader.canRead()) {
            CommandSyntaxException err = null;
            CommandContextBuilder<T> tctx = null;
            DypoContextBuilder dtctx = null;
            int maxLength = 0;

            if (!children.isEmpty())
                dypoContextBuilder.setPropagate();

            for (DypoNode<T> child : children) {
                int cursor = reader.getCursor();
                CommandContextBuilder<T> ctx = contextBuilder.copy();
                DypoContextBuilder dctx = new DypoContextBuilder(child, dypoContextBuilder);
                reader.skipWhitespace();

                try {
                    if (child.isValidInput(reader)) {
                        tctx = ctx = child.parse(reader, ctx, dctx, null);
                        dctx.setPropagate();
                        dypoContextBuilder.propagate(dctx);
                        return ctx;
                    }
                } catch (CommandSyntaxException err1) {
                    if (reader.getCursor() > maxLength) {
                        tctx = ctx;
                        dtctx = dctx;
//                        dtctx.setPropagate();
                        maxLength = reader.getCursor();
                        err = err1;
                    }
                }

                dypoContextBuilder.set(dctx);
                reader.setCursor(cursor);
            }

            if (tctx != null) {
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
            for (DypoNode<T> child : children) {
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

    public abstract String getName();
}
