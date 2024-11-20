package tfc.dynamicportals.cmd.cmdr.nodes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import tfc.dynamicportals.cmd.cmdr.CmdRContext;
import tfc.dynamicportals.cmd.cmdr.exception.DypoException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ChoiceNode<T, A, B> extends CmdRNode<T, A, B> {
    boolean denyRepeat = false;

    public ChoiceNode() {
    }

    public ChoiceNode(boolean denyRepeat) {
        this.denyRepeat = denyRepeat;
    }

    @Override
    public CommandSyntaxException parse(StringReader reader, CommandContextBuilder<T> builder, CmdRContext dpbuilder) {
        Set<CmdRNode<?, ?, ?>> nodesUsed = null;
        if (denyRepeat) {
            nodesUsed = dpbuilder.getData(this);
            if (nodesUsed == null) {
                nodesUsed = new HashSet<>();
                dpbuilder.setData(this, nodesUsed);
            }
        }

        for (CmdRNode<T, B, ?> child : children) {
            if (nodesUsed.contains(child)) continue;

            int cursor = reader.getCursor();
            CommandSyntaxException ex = child.parse(reader, builder.copy(), new CmdRContext(child, dpbuilder));
            if (ex == null) {
                reader.setCursor(cursor);
                if (denyRepeat) nodesUsed.add(child);
                return null;
            }
            reader.setCursor(cursor);
        }

        return new DypoException(
                "Incorrect argument for command",
                reader
        );
    }

    @Override
    public boolean isValidInput(String input) {
        return true;
    }

    @Override
    public CompletableFuture<Suggestions> mySuggestions(CommandContext<T> context, SuggestionsBuilder builder, CmdRContext ctx) {
        try {
            Set<CmdRNode<?, ?, ?>> nodesUsed = null;
            if (denyRepeat) {
                nodesUsed = ctx.getData(this);
                if (nodesUsed == null) {
                    nodesUsed = new HashSet<>();
                    ctx.setData(this, nodesUsed);
                }
            }

            List<Suggestions> childSuggestions = new ArrayList<>();
            for (CmdRNode<T, B, ?> child : children) {
                if (denyRepeat && nodesUsed.contains(child))
                    continue;

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

    public ChoiceNode<T, A, B> requireArg(CmdRNode<T, B, B> arg) {
        addArg(arg);
        return this;
    }
}
