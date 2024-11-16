package tfc.dynamicportals.cmd.nodes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import tfc.dynamicportals.cmd.DypoContextBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class DypoNode<T> {
    List<DypoNode<T>> children = new ArrayList<>();

    public abstract CommandSyntaxException parse(
            StringReader reader,
            CommandContextBuilder<T> builder,
            DypoContextBuilder dpbuilder
    );

    public CommandSyntaxException parseChildren(
            StringReader reader,
            CommandContextBuilder<T> builder,
            DypoContextBuilder dpbuilder
    ) {
        if (!children.isEmpty() && reader.canRead()) {
            int c0 = reader.getCursor();
            reader.skipWhitespace();
            for (DypoNode<T> child : children) {
                int cursor = reader.getCursor();

                CommandContextBuilder<T> cpy = builder.copy();
                DypoContextBuilder dctx = new DypoContextBuilder(child, dpbuilder);
                CommandSyntaxException ex = child.parse(reader, cpy, dctx);
                if (ex == null)
                    ex = child.parseChildren(reader, cpy, dctx);
                if (ex == null) {
                    dpbuilder.set(reader.getCursor(), dctx);
                    CommandNodeAccessor.setCtx(builder, cpy);
                    return null;
                }

                reader.setCursor(cursor);
            }
            reader.setCursor(c0);
        }
        return null; // TODO: rethrow exception if children attempted to be parsed but failed
    }

    public abstract boolean isValidInput(String input);

    public boolean isValidInput(StringReader input) {
        return isValidInput(input.getRemaining());
    }

    public DypoNode<T> addArg(DypoNode<T> test) {
        children.add(test);
        return this;
    }

    public abstract CompletableFuture<Suggestions> mySuggestions(CommandContext<T> context, SuggestionsBuilder builder, DypoContextBuilder ctx);

    public CompletableFuture<Suggestions> fillSuggestions(CommandContext<T> context, SuggestionsBuilder builder, DypoContextBuilder ctx) {
        try {
            List<Suggestions> childSuggestions = new ArrayList<>();
            for (DypoNode<T> child : children) {
                SuggestionsBuilder builder1 = new SuggestionsBuilder(
                        builder.getInput(),
                        ctx.getSuggestionOffset()
                ).createOffset(ctx.getSuggestionOffset());
                CompletableFuture<Suggestions> cSuggestions = child.mySuggestions(context, builder1, ctx);
                try {
                    childSuggestions.add(cSuggestions.get());
                } catch (Throwable err) {
                    err.printStackTrace();
                }
            }

            builder = builder.createOffset(ctx.getSuggestionOffset());
            for (Suggestions childSuggestion : childSuggestions) {
                for (Suggestion suggestion : childSuggestion.getList()) {
                    builder.suggest(suggestion.getText(), suggestion.getTooltip());
                }
            }

            return CompletableFuture.completedFuture(builder.build());
        } catch (Throwable err) {
            err.printStackTrace();
            return CompletableFuture.completedFuture(builder.build());
        }
    }
}
