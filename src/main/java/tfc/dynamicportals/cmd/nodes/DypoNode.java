package tfc.dynamicportals.cmd.nodes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import tfc.dynamicportals.cmd.CommandNodeAccessor;
import tfc.dynamicportals.cmd.DypoContextBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class DypoNode<T, A, B> {
    List<DypoNode<T, B, ?>> children = new ArrayList<>();
    @SuppressWarnings("unchecked")
    BiFunction<T, A, B> action = (t, a) -> (B) a;
    BiFunction<T, ?, Integer> postAction = null;

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
            for (DypoNode<T, B, ?> child : children) {
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

    public DypoNode<T, A, B> addArg(DypoNode<T, B, ?> test) {
        children.add(test);
        return this;
    }

    public abstract CompletableFuture<Suggestions> mySuggestions(CommandContext<T> context, SuggestionsBuilder builder, DypoContextBuilder ctx);

    public CompletableFuture<Suggestions> fillSuggestions(CommandContext<T> context, SuggestionsBuilder builder, DypoContextBuilder ctx) {
        try {
            List<Suggestions> childSuggestions = new ArrayList<>();
            for (DypoNode<T, B, ?> child : children) {
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

    public B execute(T t, A obj) {
        return action.apply(t, obj);
    }

    public DypoNode<T, A, B> setAction(BiFunction<T, A, B> action) {
        this.action = action;
        return this;
    }

    public DypoNode<T, A, B> setAction(Function<A, B> action) {
        this.action = (t, a) -> action.apply(a);
        return this;
    }

    public DypoNode<T, A, B> postAction(BiFunction<T, ?, Integer> action) {
        postAction = action;
        return this;
    }

    public BiFunction<T, ?, Integer> getPostAction() {
        return postAction;
    }
}
