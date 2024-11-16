package tfc.dynamicportals.cmd.nodes;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import tfc.dynamicportals.cmd.DypoContextBuilder;
import tfc.dynamicportals.cmd.exception.DypoException;
import tfc.dynamicportals.cmd.exception.DypoExceptionType;

import java.util.concurrent.CompletableFuture;

public class ChoiceNode<T, A, B> extends DypoNode<T, A, B> {
    boolean denyRepeat = false;

    public ChoiceNode() {
    }

    public ChoiceNode(boolean denyRepeat) {
        this.denyRepeat = denyRepeat;
    }

    @Override
    public CommandSyntaxException parse(StringReader reader, CommandContextBuilder<T> builder, DypoContextBuilder dpbuilder) {
        for (DypoNode<T, B, ?> child : children) {
            int cursor = reader.getCursor();
            CommandSyntaxException ex = child.parse(reader, builder.copy(), new DypoContextBuilder(child, dpbuilder));
            if (ex == null) {
                reader.setCursor(cursor);
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
    public CompletableFuture<Suggestions> mySuggestions(CommandContext<T> context, SuggestionsBuilder builder, DypoContextBuilder ctx) {
        return fillSuggestions(context, builder, ctx);
    }
}
